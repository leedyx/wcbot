package org.lee.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DatDecoder {

    public static final Logger LOGGER = LoggerFactory.getLogger(DatDecoder.class);

    private final static int[][] ARRAY = new int[][]{{0x89, 0x50}, {0xff, 0xd8}, {0x47, 0x49}};

    private final static Map<Byte, String> TYPE_SUFFIX = new HashMap();

    static {
        TYPE_SUFFIX.put((byte) 0, ".jpg");
        TYPE_SUFFIX.put((byte) 1, ".png");
        TYPE_SUFFIX.put((byte) 2, ".gif");
    }

    private byte[] byteCache = new byte[16 * 1024];

    private final String localPrefix;

    private final String outPrefix;

    public DatDecoder(String localPrefix, String outPrefix) {
        this.localPrefix = localPrefix;
        this.outPrefix = outPrefix;
    }

    /**
     * @param first
     * @param second
     * @return 首字节计算
     * 第二个字节进行校验
     */
    private byte[] getCode(byte first, byte second) {

        for (int i = 0; i < 3; ++i) {
            int[] pair = ARRAY[i];
            byte code = (byte) (pair[0] ^ first);
            if (code == (byte) (pair[1] ^ second)) {
                return new byte[]{(byte) i, code};
            }
        }

        throw new IllegalStateException("dat file is no jpg or png or gif !");
    }

    public String tryDecodeDat(String datFile) {

        Path res = null;
        if (StringUtils.isBlank(datFile)) {
            throw new IllegalArgumentException("detail is blank !");
        }
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream buffer = null;
        try {
            Path path = Paths.get(localPrefix, datFile);
            int count = 30;
            File file = path.toFile();
            while (--count >= 0) {
                if (!file.exists()) {
                    Utils.safeSleep(TimeUnit.MILLISECONDS, 100);
                } else {
                    break;
                }
            }

            if (count < 0 && !file.exists()) {
                throw new IllegalStateException("dat file not exist");
            } else {
                LOGGER.info("count : {}", count);
            }

            bufferedInputStream = new BufferedInputStream(Files.newInputStream(path));
            byte[] bytes = byteCache;
            int nread = 0;

            buffer = null;
            boolean firstRead = true;
            byte code = 0;
            while ((nread = bufferedInputStream.read(bytes, 0, bytes.length)) != -1) {
                if (firstRead) {
                    byte[] codeRes = getCode(bytes[0], bytes[1]);
                    code = codeRes[1];
                    String imgName = StringUtils.replace(path.getFileName().toString(), ".dat", TYPE_SUFFIX.get(codeRes[0]));
                    res = Paths.get(outPrefix, imgName);
                    buffer = new BufferedOutputStream(Files.newOutputStream(res, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING));
                    firstRead = false;
                }

                for (int i = 0; i < nread; ++i) {
                    bytes[i] ^= code;
                }

                buffer.write(bytes, 0, nread);
            }
            if (Objects.nonNull(buffer)) {
                buffer.flush();
            }

            if (Objects.nonNull(res)) {
                return res.toString();
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

        throw new IllegalStateException("tryDecodeDat error");

    }
}
