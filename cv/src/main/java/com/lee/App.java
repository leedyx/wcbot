package com.lee;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.StringVector;
import org.bytedeco.opencv.opencv_wechat_qrcode.WeChatQRCode;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;

/**
 * Hello world!
 *
 */
public class App 
{

    public static final String prefix = "D:\\local\\we_chat_qr_code\\";

    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        WeChatQRCode weChatQRCode = new WeChatQRCode(prefix + "detect.prototxt",
                prefix + "detect.caffemodel",
                prefix + "sr.prototxt",
                prefix + "sr.caffemodel");

        System.out.println(weChatQRCode);
        Mat imread = imread("D:\\tmp\\res.jpg");
        StringVector res = weChatQRCode.detectAndDecode(imread);
        long length = res.size();
        for(int i = 0 ; i < length ; ++i){
            BytePointer pointer = res.get(i);
            System.out.println(pointer.getString());
        }
        //System.out.println(res.get(0));
    }
}
