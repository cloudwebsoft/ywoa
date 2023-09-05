package com.cloudweb.oa.utils;

import com.cloudwebsoft.framework.util.LogUtil;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import sun.font.FontDesignMetrics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

/**
 * FileName: QRCodeUtil
 * Author:   xuan zongjun
 * Date:     2022/6/15 11:00
 * Description: 二维码生成工具类
 */
public class QRCodeUtil {

    /**
     * 创建二维码
     * @param charSet 编码方式
     * @param content 二维码内容
     * @param qrWidth 二维码长度
     * @param qrHeight 二维码高度
     * @return
     */
    public static BufferedImage createImage(String charSet, String content, int qrWidth, int qrHeight){
        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, charSet);
        hints.put( EncodeHintType.MARGIN, 1);
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE,qrWidth , qrHeight, // 修改二维码底部高度
                    hints);
        } catch (WriterException e) {
            LogUtil.getLog(QRCodeUtil.class).error(e);
        }
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return image;
    }

    /**
     * 对已经生成好的二维码设置logo
     * @param source 二维码
     * @param logo logo图片
     * @param logoWidth logo宽度
     * @param logoHeight logo高度
     */
    public static void insertLogoImage(BufferedImage source,Image logo,int logoWidth,int logoHeight){
        Graphics2D graph = source.createGraphics();
        int qrWidth = source.getWidth();
        int qrHeight = source.getHeight();
        int x = (qrWidth - logoWidth) / 2;
        int y = (qrHeight - logoHeight) / 2;
        graph.drawImage(logo, x, y, logoWidth, logoHeight, null);
        Shape shape = new RoundRectangle2D.Float(x, y, logoWidth, logoHeight, 6, 6);
        graph.setStroke(new BasicStroke(3f));
        graph.draw(shape);
        graph.dispose();
    }

    /**
     * 缩小logo图片
     * @param logoPath
     * @param logoWidth
     * @param logoHeight
     * @return
     */
    public static Image compressLogo(String logoPath, int logoWidth, int logoHeight){
        File file = new File(logoPath);
        if (!file.exists()) {
            System.err.println("" + logoPath + "   该文件不存在！");
            return null;
        }
        Image original = null;
        try {
            original = ImageIO.read(new File(logoPath));
        } catch (IOException e) {
            LogUtil.getLog(QRCodeUtil.class).error(e);
        }
        int width = original.getWidth(null);
        int height = original.getHeight(null);
        if (width > logoWidth) {
            width = logoWidth;
        }
        if (height > logoHeight) {
            height = logoHeight;
        }
        Image image = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = tag.getGraphics();
        g.drawImage(image, 0, 0, null); // 绘制缩小后的图
        g.dispose();
        return image;
    }

    /**
     * 增加底部的说明文字
     * @param source 二维码
     * @param text 说明内容
     * @param step
     */
    public static BufferedImage addBottomFont(BufferedImage source, String text,int step) {
        int qrWidth = source.getWidth();
        int qrHeight = source.getHeight();
        BufferedImage textImage = textToImage(text, qrWidth, 20,16);
        Graphics2D graph = source.createGraphics();
        //开启文字抗锯齿
        graph.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = textImage.getWidth(null);
        int height = textImage.getHeight(null);

        Image src = textImage;
        graph.drawImage(src, 0, qrHeight - (20 * step) - 10, width, height, null);
        graph.dispose();
        return  source;
    }

    /**
     * 将文明说明增加到二维码上
     * @param str       文字
     * @param width     宽
     * @param height    高
     * @param fontSize 字体大小
     * @return
     */
    public static BufferedImage textToImage(String str, int width, int height,int fontSize) {
        BufferedImage textImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D)textImage.getGraphics();
        //开启文字抗锯齿
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setBackground(Color.WHITE);
        g2.clearRect(0, 0, width, height);
        g2.setPaint(Color.BLACK);
        FontRenderContext context = g2.getFontRenderContext();
        Font font = new Font("微软雅黑", Font.PLAIN, fontSize);
        g2.setFont(font);
        LineMetrics lineMetrics = font.getLineMetrics(str, context);
        FontMetrics fontMetrics = FontDesignMetrics.getMetrics(font);
        float offset = (width - fontMetrics.stringWidth(str)) / 2;
        float y = (height + lineMetrics.getAscent() - lineMetrics.getDescent() - lineMetrics.getLeading()) / 2;
        g2.drawString(str, (int)offset, (int)y);
        return textImage;
    }

    /**
     * 顶部增加说明文字
     * @param source
     * @param text
     */
    public static void addUpFont(BufferedImage source, String text) {
        int qrWidth = source.getWidth();
        int qrHeight = source.getHeight();

        BufferedImage textImage = textToImage(text, qrWidth, 10,10);
        Graphics2D graph = source.createGraphics();
        //开启文字抗锯齿
        graph.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = textImage.getWidth(null);
        int height = textImage.getHeight(null);

        Image src = textImage;
        graph.drawImage(src, 0, 4, width, height, null);
        graph.dispose();
    }

    /**
     * 生成二维码图片
     * @param charSet 二维码编码方式
     * @param content 内容
     * @param qrWidth 宽度
     * @param qrHeight 长度
     * @param formatName jpg等图片格式
     * @param imgPath 二维码存放路径
     */
    public static void encode(String charSet,String content,int qrWidth,int qrHeight,String formatName,String imgPath){
        BufferedImage image = QRCodeUtil.createImage(charSet,content,qrWidth,qrHeight);
        try {
            ImageIO.write(image, formatName, new File(imgPath));
        } catch (IOException e) {
            LogUtil.getLog(QRCodeUtil.class).error(e);
        }
    }

    public static void encode(BufferedImage image, String formatName,String imgPath){
        try {
            ImageIO.write(image, formatName, new File(imgPath));
        } catch (IOException e) {
            LogUtil.getLog(QRCodeUtil.class).error(e);
        }
    }

    public static void mkdirs(String destPath) {
        File file = new File(destPath);
        // 当文件夹不存在时，mkdirs会自动创建多层目录，区别于mkdir．(mkdir如果父目录不存在则会抛出异常)
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }
    }

    /*
     * 解析二维码
     */
    public static String decode(File file, String cherSet) throws Exception {
        BufferedImage image;
        image = ImageIO.read(file);
        if (image == null) {
            return null;
        }
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result;
        Hashtable hints = new Hashtable();
        hints.put(DecodeHintType.CHARACTER_SET, cherSet);
        result = new MultiFormatReader().decode(bitmap, hints);
        return result.getText();
    }
}