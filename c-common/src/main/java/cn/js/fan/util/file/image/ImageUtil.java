package cn.js.fan.util.file.image;

import cn.js.fan.util.file.FileUtil;
import com.cloudwebsoft.framework.util.LogUtil;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;

public class ImageUtil {
    /**
     * 缩放图片
     * @param source BufferedImage
     * @param targetW int
     * @param targetH int
     * @return BufferedImage
     */
    public static BufferedImage resize(BufferedImage source, int targetW,
                                       int targetH) {
        // targetW，targetH分别表示目标长和宽
        int type = source.getType();
        BufferedImage target = null;
        double sx = (double) targetW / source.getWidth();
        double sy = (double) targetH / source.getHeight();
        /*
        // 在targetW，targetH范围内实现等比缩放。如果不需要等比缩放
        if (sx > sy) {
            sx = sy;
            targetW = (int) (sx * source.getWidth());
        } else {
            sy = sx;
            targetH = (int) (sy * source.getHeight());
        }
         */
        if (type == BufferedImage.TYPE_CUSTOM) { // handmade
            ColorModel cm = source.getColorModel();
            WritableRaster raster = cm.createCompatibleWritableRaster(targetW,
                    targetH);
            boolean alphaPremultiplied = cm.isAlphaPremultiplied();
            target = new BufferedImage(cm, raster, alphaPremultiplied, null);
        } else
            target = new BufferedImage(targetW, targetH, type);
        Graphics2D g = target.createGraphics();
        // smoother than exlax:
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                           RenderingHints.VALUE_RENDER_QUALITY);
        g.drawRenderedImage(source, AffineTransform.getScaleInstance(sx, sy));
        g.dispose();
        return target;
    }

    /**
     * 缩放图片
     * @param fromFileStr String 图片绝对路径
     * @param saveToFileStr String 保存文件的绝对路径
     * @param width int
     * @param hight int
     * @throws Exception
     */
    public static void resizeImage(String fromFileStr, String saveToFileStr,
                                 int width, int hight) throws Exception {
        BufferedImage srcImage;
        // String ex = fromFileStr.substring(fromFileStr.indexOf("."),fromFileStr.length());
        String imgType = "JPEG";
        if (fromFileStr.toLowerCase().endsWith(".png")) {
            imgType = "PNG";
        }
        File saveFile = new File(saveToFileStr);
        File fromFile = new File(fromFileStr);
        srcImage = ImageIO.read(fromFile);
        if (width > 0 || hight > 0) {
            srcImage = resize(srcImage, width, hight);
        }
        ImageIO.write(srcImage, imgType, saveFile);
    }

    /**
     * 同比缩放，根据长宽比,同比例缩放，如果宽大于高，则以高为比例缩放，反之，则以宽为比例缩放
     * 超出指定的宽高的多余部分将会被截去，以左上角为顶点裁剪
     * @param source BufferedImage
     * @param oriTargetW int
     * @param oriTargetH int
     * @return BufferedImage
     */
    public static BufferedImage resizePoportionAndClip(BufferedImage source, int oriTargetW, int oriTargetH) {
        // targetW，targetH分别表示目标长和宽
        int type = source.getType();
        BufferedImage target = null;
        double scale = 1.0;

        int targetW = oriTargetW;
        int targetH = oriTargetH;

        double sx = (double) targetW / source.getWidth();
        double sy = (double) targetH / source.getHeight();

        // 根据长宽比,同比例缩放，如果宽大于高，则以高为比例缩放，反之，则以宽为比例缩放
        if (source.getWidth() > source.getHeight()) {
            double oldSx = sx;
            sx = sy;
            targetW = (int) (sx * source.getWidth());
            if (targetW < oriTargetW) {
                sx = oldSx;
            }
        } else {
            double oldSy = sy;
            sy = sx;
            targetH = (int) (sy * source.getHeight());
            if (targetH < oriTargetH) {
                sy = oldSy;
            }
        }




       /*
       if (oriTargetW > oriTargetH) {
           sy = sx;
           targetH = (int) (sy * source.getHeight());
       } else {
           sx = sy;
           targetW = (int) (sx * source.getWidth());
       }
       */

        if (type == BufferedImage.TYPE_CUSTOM) { // handmade
            ColorModel cm = source.getColorModel();
            WritableRaster raster = cm.createCompatibleWritableRaster(oriTargetW,
                    oriTargetH);
            boolean alphaPremultiplied = cm.isAlphaPremultiplied();
            target = new BufferedImage(cm, raster, alphaPremultiplied, null);
        } else
            target = new BufferedImage(oriTargetW, oriTargetH, type);
        Graphics2D g = target.createGraphics();
        // smoother than exlax:
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                           RenderingHints.VALUE_RENDER_QUALITY);
        g.drawRenderedImage(source, AffineTransform.getScaleInstance(sx, sy));
        g.dispose();
        return target;
    }

    /**
     * 等比例缩放并裁剪超出预定义大小范围的图片
     * @param fromFileStr String
     * @param saveToFileStr String
     * @param width int
     * @param hight int
     * @throws Exception
     */
    public static void resizeImagePoportionAndClip(String fromFileStr, String saveToFileStr,
                                 int width, int hight) {
        BufferedImage srcImage;
        // String ex = fromFileStr.substring(fromFileStr.indexOf("."),fromFileStr.length());
        String imgType = "JPEG";
        if (fromFileStr.toLowerCase().endsWith(".png")) {
            imgType = "PNG";
        }
        try {
            File saveFile = new File(saveToFileStr);
            File fromFile = new File(fromFileStr);
            srcImage = ImageIO.read(fromFile);
            if (width > 0 || hight > 0) {
                srcImage = resizePoportionAndClip(srcImage, width, hight);
            }
            ImageIO.write(srcImage, imgType, saveFile);
        }
        catch (IOException e) {
            LogUtil.getLog(ImageUtil.class).error(e);
        }
    }

    /*
    public static void main(String argv[]) {
        try {
            //参数1(from),参数2(to),参数3(宽),参数4(高)
            ImageUtil.resizeImagePoportionAndClip("c:/111.gif",
                                "c:/2.gif", 75,
                                75);
            ImageUtil.resizeImagePoportionAndClip("c:/222.gif",
                                "c:/3.gif", 75,
                                75);
            // Thumbnail.createThumbnail("c:/1.gif", "c:/2.jpg", 76);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }
    */
}
