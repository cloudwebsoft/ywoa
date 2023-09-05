package cn.js.fan.util.file.image;

import com.cloudwebsoft.framework.util.LogUtil;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import java.awt.Image;
import java.io.File;
import com.sun.image.codec.jpeg.JPEGCodec;
import java.io.FileOutputStream;
import java.awt.image.BufferedImage;
import java.awt.Font;
import java.awt.Color;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class WaterMarkUtil {
    public static final int POS_CENTER = 0;
    public static final int POS_RIGHT_BOTTOM = 3;
    public static final int POS_RIGHT_TOP = 4;
    public static final int POS_LEFT_TOP = 1;
    public static final int POS_LEFT_BOTTOM = 2;
    public static int offsetX = 12;
    public static int offsetY = 12;
    public int pos;

    public int srcImgWidth, srcImgHeight;
    public int markImgWidth, markImgHeight;

    public WaterMarkUtil(int pos) {
        this.pos = pos;
    }

    public int getMarkTop() {
        int n = offsetY;
        switch(pos) {
        case POS_RIGHT_TOP :
            n = offsetY;
            break;
        case POS_RIGHT_BOTTOM:
            n = srcImgHeight - offsetY - markImgHeight;
            break;
        case POS_LEFT_TOP:
            n = offsetY;
            break;
        case POS_LEFT_BOTTOM:
            n = srcImgHeight - offsetY - markImgHeight;
            break;
        default:
            n = srcImgHeight - offsetY - markImgHeight;
        }
        return n;
    }

    public int getMarkLeft() {
        int n = offsetX ;
        switch(pos) {
        case POS_RIGHT_TOP :
            n = srcImgWidth - offsetX - markImgWidth ;
            break;
        case POS_RIGHT_BOTTOM:
            n = srcImgWidth - offsetX - markImgWidth;
            break;
        case POS_LEFT_TOP:
            n = offsetX ;
            break;
        case POS_LEFT_BOTTOM:
            n = offsetX;
            break;
        default:
            n = offsetX;
        }
        return n;
    }

    public int getMarkStringTop() {
        int n = offsetY;
        switch(pos) {
        case POS_RIGHT_TOP :
            n = offsetY;
            break;
        case POS_RIGHT_BOTTOM:
            n = srcImgHeight - offsetY;
            break;
        case POS_LEFT_TOP:
            n = offsetY;
            break;
        case POS_LEFT_BOTTOM:
            n = srcImgHeight - offsetY;
            break;
        default:
            n = offsetY;
        }
        return n;
    }

    public int getMarkStringLeft() {
        int n = offsetX ;
        switch(pos) {
        case POS_RIGHT_TOP :
            n = srcImgWidth - offsetX ;
            break;
        case POS_RIGHT_BOTTOM:
            n = srcImgWidth - offsetX;
            break;
        case POS_LEFT_TOP:
            n = offsetX ;
            break;
        case POS_LEFT_BOTTOM:
            n = offsetX;
            break;
        default:
            n = offsetX;
        }
        return n;
    }

    /**
     * 文字水印
     * @param srcFilePath String
     * @param newFilePath String
     * @param waterMarkStr String
     * @param font Font
     * @param color Color
     * @param alpha float
     */
    public void mark(String srcFilePath, String newFilePath, String waterMarkStr, Font font, Color color, float alpha) {
        try {
            File srcFile = new File(srcFilePath);
            Image srcImg = javax.imageio.ImageIO.read(srcFile);
            srcImgWidth = srcImg.getWidth(null);
            srcImgHeight = srcImg.getHeight(null);
            BufferedImage bufImg = new BufferedImage(srcImgWidth, srcImgHeight,
                                                     BufferedImage.TYPE_INT_RGB);
            Graphics2D g = bufImg.createGraphics();
            g.drawImage(srcImg, 0, 0, srcImgWidth, srcImgHeight, null);
            if (font==null)
                g.setFont(new Font("隶书", Font.BOLD, 20)); // 设定文本字体
            else
                g.setFont(font);
            if (color==null)
                g.setColor(Color.BLUE); // 设定文本颜色
            else
                g.setColor(color);

            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.
                    SRC_OVER, alpha);
            g.setComposite(ac);

            /*
             FontMetrics fm = g.getFontMetrics();
             int stringWidth(String str)返回字符串宽度
            */

            g.drawString(waterMarkStr, getMarkStringLeft(), getMarkStringTop()); // 向BufferedImage写入文本字符
            g.dispose(); // 使更改生效

            FileOutputStream out = new FileOutputStream(newFilePath);
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            encoder.encode(bufImg);

            // JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bufImg);
            // param.setQuality(qualNum, true);
            // encoder.encode(bufImg, param);

            out.close();
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }

    /**
     * 图片水印
     * @param srcFilePath String
     * @param newFilePath String
     * @param waterMarkImgPath String 水印图片的路径
     * @param alpha float 透明度 0 完全透明
     */
    public void mark(String srcFilePath, String newFilePath, String waterMarkImgPath, float alpha) {
        try {
            File srcFile = new File(srcFilePath);
            Image srcImg = javax.imageio.ImageIO.read(srcFile);
            srcImgWidth = srcImg.getWidth(null);
            srcImgHeight = srcImg.getHeight(null);
            BufferedImage bufImg = new BufferedImage(srcImgWidth, srcImgHeight,
                    BufferedImage.TYPE_INT_RGB);

            Graphics2D g = bufImg.createGraphics();
            g.drawImage(srcImg, 0, 0, srcImgWidth, srcImgHeight, null);
            Image img1 = javax.imageio.ImageIO.read(new File(waterMarkImgPath));
            markImgWidth = img1.getWidth(null);
            markImgHeight = img1.getHeight(null);

            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.
                    SRC_OVER, alpha);
            g.setComposite(ac);

            g.drawImage(img1, getMarkLeft(), getMarkTop(), markImgWidth,
                        markImgHeight, null);

            g.dispose(); // 使更改生效

            FileOutputStream out = new FileOutputStream(newFilePath);
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            encoder.encode(bufImg);

            // JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bufImg);
            // param.setQuality(qualNum, true);
            // encoder.encode(bufImg, param);

            out.close();
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("water mark img path:" + waterMarkImgPath);
            LogUtil.getLog(getClass()).error(e);
        }
    }

    public void setOffsetX(int x) {
        offsetX = x;
    }

    public void setOffsetY(int y) {
        offsetY = y;
    }
}
