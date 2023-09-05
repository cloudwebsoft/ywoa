package com.redmoon.oa.util;

import cn.js.fan.web.Global;
import com.cloudweb.oa.utils.QRCodeUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TwoDimensionCode {
    /**
     * 生成IOS Android下载路径
     */
    public static void generate2DCodeByMobileClient() {
        String oaRealPath = Global.getRealPath();
        String fullRootPath = Global.getFullRootPath() + "/";
        String filePath = oaRealPath + "public/images/";
        File f = new File(filePath);
        if (!f.isDirectory()) {
            f.mkdirs();
        }

        // 动态生成二维码图片
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        f = new File(filePath + cfg.get("qrcode_mobile_png_path"));
        if (f.exists()) {
            f.delete();
        }
        BufferedImage bufferedImage = QRCodeUtil.createImage("utf-8", fullRootPath
                + "public/download_app.html", 200, 200);
        Resource resource = new ClassPathResource("static/images/oa_logo.png");
        try {
            Image image = ImageIO.read(resource.getInputStream());
            QRCodeUtil.insertLogoImage(bufferedImage, image, 40, 40);
            QRCodeUtil.encode(bufferedImage, "png", filePath
                    + cfg.get("qrcode_mobile_png_path"));
        } catch (IOException e) {
            LogUtil.getLog(TwoDimensionCode.class).error(e);
        }
    }

    /**
     * 生成二维码(QRCode)图片
     *
     * @param content 存储内容
     * @param imgPath 图片路径
     * @param imgType 图片类型
     */
    public static void encoderQRCode(String content, String imgPath, String imgType) {
        int size = (int) (Math.log(content.getBytes().length) / Math.log(2) + 1);
        try {
            encoderQRCode(content, imgPath, imgType, size);
        } catch (Exception e) {
            LogUtil.getLog(TwoDimensionCode.class).error(e);
        }
    }

    /**
     * 生成二维码(QRCode)图片
     *
     * @param content 存储内容
     * @param imgPath 图片路径
     * @param imgType 图片类型
     * @param size    二维码尺寸
     * @throws Exception
     */
    public static void encoderQRCode(String content, String imgPath,
                                     String imgType, int size) throws IOException {
        File imgFile = new File(imgPath);
        File pFile = imgFile.getParentFile();
        if (!pFile.exists()) {
            pFile.mkdirs();
        }

        BufferedImage bufferedImage = QRCodeUtil.createImage("utf-8", content, 200, 200);
        Resource resource = new ClassPathResource("static/images/oa_logo.png");
        Image image = ImageIO.read(resource.getInputStream());
        QRCodeUtil.insertLogoImage(bufferedImage, image, 40, 40);
        QRCodeUtil.encode(bufferedImage, imgType, imgPath);
    }

    public static void generate2DCode(String content, String path, String surfix) {
        encoderQRCode(content, path, "png");
    }
}