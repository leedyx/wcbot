package org.lee.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueue;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.lee.Constants;
import org.lee.pojo.MsgInfo;
import org.lee.pojo.PicMsg;
import org.lee.pojo.PicMsgDetail;
import org.lee.pojo.TxtMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Data
public class DispatchListener implements WebSocketListener {

    private int state = Constants.DispatchState.WORKING;

    private final MpscArrayQueue<MsgInfo> defaultQueue = new MpscArrayQueue<>(2048);

    private ObjectReader reader = new ObjectMapper().reader();

    /**
     * 确认下是否是多线程场景
     * 我猜不是
     * 但是为了保险起见，以多线程场景考虑
     */
    private ConcurrentMap<String, MpscArrayQueue<MsgInfo>> queueMap = new ConcurrentHashMap<>();

    public DispatchListener() {

    }


    private MsgInfo picMsg2MsgInfo(PicMsg picMsg) {
        MsgInfo res = new MsgInfo();
        PicMsgDetail picMsgDetail = picMsg.getPicMsgDetail();
        res.setContent(picMsgDetail.getDetail());
        res.setRoomId(picMsgDetail.getRoomId());
        res.setId(picMsg.getId());
        res.setType(3);
        return res;
    }

    private MsgInfo txtMsg2MsgInfo(TxtMsg txtMsg) {
        MsgInfo res = new MsgInfo();

        res.setContent(txtMsg.getContent());
        res.setRoomId(txtMsg.getRoomId());
        res.setId(txtMsg.getId());
        res.setType(1);
        return res;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DispatchListener.class);

    private static final String KEY_WORDS = "\"content\":\"heart beat\"";

    @Override
    public void onOpen(WebSocket websocket) {
    }

    @Override
    public void onClose(WebSocket websocket, int code, String reason) {
        this.state = Constants.DispatchState.END;
        LOGGER.info("================ ws close ================!");
        LOGGER.info("the code : {} , reason : {}", code, reason);
    }

    @Override
    public void onError(Throwable t) {
        this.state = Constants.DispatchState.END;
        LOGGER.error("================ ws error ================!", t);
    }

    @Override
    public void onTextFrame(String payload, boolean finalFragment, int rsv) {
        if (state != Constants.DispatchState.WORKING) {
            throw new IllegalStateException("state is invalid !");
        }

        if (finalFragment) {
            if (StringUtils.contains(payload, KEY_WORDS)) {
                LOGGER.info("*** &&& HEART_BEAT &&& ***");
            } else {

                try {

                    MsgInfo msgInfo = null;
                    MpscArrayQueue<MsgInfo> queue = defaultQueue;
                    /**
                     * 图片
                     */
                    if (StringUtils.contains(payload, "\"type\":3")) {
                        PicMsg res = reader.readValue(payload, PicMsg.class);
                        if (Objects.nonNull(res.getPicMsgDetail()) && StringUtils.isNotBlank(res.getPicMsgDetail().getDetail())) {
                            msgInfo = picMsg2MsgInfo(res);
                        }

                    } else if (StringUtils.contains(payload, "\"type\":1")) {
                        TxtMsg res = reader.readValue(payload, TxtMsg.class);
                        msgInfo = txtMsg2MsgInfo(res);
                    } else {
                        LOGGER.info("please check message : {}", payload);
                    }

                    if (Objects.isNull(msgInfo)) {
                        return;
                    }

                    if (StringUtils.isNotBlank(msgInfo.getRoomId())) {
                        queue = queueMap.getOrDefault(msgInfo.getRoomId(), defaultQueue);
                    }

                    if (!queue.offer(msgInfo)) {
                        LOGGER.error("****** THE QUEUE IS FULL ******");
                    }
                } catch (Exception e) {
                    LOGGER.error("error", e);
                }
            }

        } else {
            LOGGER.error("****** THE finalFragment IS FALSE ******");
        }
    }


}
