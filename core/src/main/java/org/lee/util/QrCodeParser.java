package org.lee.util;

import org.apache.commons.lang3.StringUtils;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.StringVector;
import org.bytedeco.opencv.opencv_wechat_qrcode.WeChatQRCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;

public class QrCodeParser {

    public static final Logger LOGGER = LoggerFactory.getLogger(QrCodeParser.class);

    private WeChatQRCode weChatQRCode;

    public QrCodeParser(final String wechatQrCodePath) {
        this.weChatQRCode = new WeChatQRCode(wechatQrCodePath + "detect.prototxt", wechatQrCodePath + "detect.caffemodel", wechatQrCodePath + "sr.prototxt", wechatQrCodePath + "sr.caffemodel");
    }

    public List<String> tryParseUrls(final String imgPath) {

        List<String> res = null;
        Mat imread = imread(imgPath);
        StringVector stringVector = weChatQRCode.detectAndDecode(imread);

        if (stringVector.size() > 0) {
            res = new ArrayList<>();
            for (int i = 0; i < stringVector.size(); ++i) {
                String url = stringVector.get(i).getString();
                //LOGGER.info("parsed url : {}", url);
                if (StringUtils.contains(url, "www.wjx.cn") || StringUtils.contains(url, "www.wjx.top")) {
                    res.add(url);
                }
            }
        }

        return res;
    }
}
