package org.lee.util;

import org.apache.commons.lang3.StringUtils;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.StringVector;
import org.bytedeco.opencv.opencv_wechat_qrcode.WeChatQRCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;

public class Parsers {

    public static final Logger LOGGER = LoggerFactory.getLogger(Parsers.class);

    private final static String PATH_PREFIX = "D:\\Data\\wechat\\WeChat Files\\";

    private final static String OUTPUT_PERFIX = "D:\\tmp\\images\\";

    private final static int[][] ARRAY = new int[][]{{0x89, 0x50}, {0xff, 0xd8}, {0x47, 0x49}};

    private final static String QR_CODE_PATH_PREFIX = "D:\\local\\we_chat_qr_code\\";

    private static final ThreadLocal<byte[]> byteCaches = new ThreadLocal<byte[]>() {
        @Override
        protected byte[] initialValue() {
            return new byte[16 * 1024];
        }
    };

    private static final ThreadLocal<WeChatQRCode> WeChatQRCodeCaches = new ThreadLocal<WeChatQRCode>() {
        @Override
        protected WeChatQRCode initialValue() {
            return new WeChatQRCode(QR_CODE_PATH_PREFIX + "detect.prototxt",
                    QR_CODE_PATH_PREFIX + "detect.caffemodel",
                    QR_CODE_PATH_PREFIX + "sr.prototxt",
                    QR_CODE_PATH_PREFIX + "sr.caffemodel");
        }
    };


    /**
     * @param first
     * @param second
     * @return 首字节计算
     * 第二个字节进行校验
     */
    private static byte getCode(byte first, byte second) {

        for (int[] pair : ARRAY) {
            byte code = (byte) (pair[0] ^ first);
            if (code == (byte) (pair[1] ^ second)) {
                return code;
            }
        }
        throw new IllegalStateException("no jpg , png , gif !");
    }


    public static List<String> tryGetUrl(String detail) {

        List<String> res = null;
        if (StringUtils.isBlank(detail)) {
            throw new IllegalArgumentException("detail is blank !");
        }
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream buffer = null;
        try {
            String fileName = PATH_PREFIX + detail;
            File file = new File(fileName);
            int count = 20;
            while (--count >= 0) {
                if (!file.exists()) {
                    Utils.safeSleep(TimeUnit.MILLISECONDS, 100);
                } else {
                    break;
                }
            }

            if (count < 0 && !file.exists()) {
                LOGGER.error("can not find file ! {}", fileName);
                return res;
            } else {
                LOGGER.info("count : {}", count);
            }

            bufferedInputStream = new BufferedInputStream(Files.newInputStream(file.toPath()));
            byte[] bytes = byteCaches.get();
            int nread = 0;

            buffer = new BufferedOutputStream(Files.newOutputStream(Paths.get(OUTPUT_PERFIX + detail), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING));
            boolean firstRead = true;
            byte code = 0;
            while ((nread = bufferedInputStream.read(bytes, 0, bytes.length)) != -1) {
                if (firstRead) {
                    code = getCode(bytes[0], bytes[1]);
                    firstRead = false;
                }
                for (int i = 0; i < nread; ++i) {
                    bytes[i] ^= code;
                }

                buffer.write(bytes, 0, nread);
            }
            buffer.flush();

            WeChatQRCode weChatQRCode = WeChatQRCodeCaches.get();
            Mat imread = imread(OUTPUT_PERFIX + detail);
            StringVector stringVector = weChatQRCode.detectAndDecode(imread);

            if (stringVector.size() > 0) {
                res = new ArrayList<>();
                for (int i = 0; i < stringVector.size(); ++i) {
                    String url = stringVector.get(i).getString();
                    LOGGER.info("parsed url : {}", url);
                    if (StringUtils.contains(url, "www.wjx.cn")
                            || StringUtils.contains(url, "www.wjx.top")) {
                        res.add(url);
                    }
                }
            }


        } catch (Exception e) {
            LOGGER.error("tryGetUrl ERROR !", e);
        } finally {
            if (Objects.nonNull(bufferedInputStream)) {
                try {
                    bufferedInputStream.close();
                } catch (Exception ignore) {

                }
            }

            if (Objects.nonNull(buffer)) {
                try {
                    buffer.close();
                } catch (Exception ignore) {

                }
            }
        }
        return res;
    }

    public static void main(String[] args) {
        List<String> res = tryGetUrl("wxid_l518lj9v2axh22\\FileStorage\\Image\\2022-06\\2d04f01296bf2b0e6726a0bdeb6c50b2.dat");
        LOGGER.info("res : {}", res);
    }


}
