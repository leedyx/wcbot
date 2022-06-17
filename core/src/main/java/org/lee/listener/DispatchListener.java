package org.lee.listener;

import io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueue;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.lee.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DispatchListener implements WebSocketListener {

    private int state = Constants.DispatchState.WORKING;

    private final MpscArrayQueue<String> queue;

    public DispatchListener(MpscArrayQueue<String> queue) {
        this.queue = queue;
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
                if (!queue.offer(payload)) {
                    LOGGER.error("****** THE QUEUE IS FULL ******");
                }
            }
        } else {
            LOGGER.error("****** THE finalFragment IS FALSE ******");
        }
    }


}
