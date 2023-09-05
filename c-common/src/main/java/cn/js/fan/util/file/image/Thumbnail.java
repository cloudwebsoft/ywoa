package cn.js.fan.util.file.image;

import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import javax.swing.ImageIcon;

import com.cloudwebsoft.framework.util.LogUtil;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import java.io.FileNotFoundException;
import java.awt.Toolkit;
import java.io.BufferedOutputStream;
import com.sun.image.codec.jpeg.JPEGEncodeParam;

public class Thumbnail {
    /**
     * Reads an image in a file and creates
     * a thumbnail in another file.
     * Will be created if necessary.
     * @param maxDim The width and height of
     * the thumbnail must
     * be maxDim pixels or less.
     */
    public static void createThumbnail(
            String srcPath, String thumbPath, int maxDim) {
        try {
            // Get the image from a file.
            Image inImage = new ImageIcon(
                    srcPath).getImage();

            // Determine the scale.
            double scale = (double) maxDim / (
                    double) inImage.getHeight(null);
            if (inImage.getWidth(
                    null) > inImage.getHeight(null)) {
                scale = (double) maxDim / (
                        double) inImage.getWidth(null);
            }

            // Determine size of new image.
            //One of them
            // should equal maxDim.
            int scaledW = (int) (
                    scale * inImage.getWidth(null));
            int scaledH = (int) (
                    scale * inImage.getHeight(null));

            // Create an image buffer in
            //which to paint on.
            BufferedImage outImage =
                    new BufferedImage(scaledW, scaledH,
                                      BufferedImage.TYPE_INT_RGB);

            // Set the scale.
            AffineTransform tx =
                    new AffineTransform();

            // If the image is smaller than
            // the desired image size,
            // don't bother scaling.
            if (scale < 1.0d) {
                tx.scale(scale, scale);
            }

            // Paint image.
            Graphics2D g2d =
                    outImage.createGraphics();
            g2d.drawImage(inImage, tx, null);
            g2d.dispose();

            // JPEG-encode the image
            //and write to file.
            OutputStream os =
                    new FileOutputStream(thumbPath);
            JPEGImageEncoder encoder =
                    JPEGCodec.createJPEGEncoder(os);
            encoder.encode(outImage);
            os.close();
        } catch (IOException e) {
            LogUtil.getLog(Thumbnail.class).error(e);
        }
    }

    /**
     * 根据限定的宽度生成缩略图(会生成黑色背景)
     * @param srcPath String
     * @param thumbPath String
     * @param widthDim int
     */
    public static void createThumbnailLimitWidth(
            String srcPath, String thumbPath, int widthDim) {
        try {
            // Get the image from a file.
            Image inImage = new ImageIcon(
                    srcPath).getImage();

            // Determine the scale.
            double scale = (double) widthDim / (double) inImage.getWidth(null);

            // Determine size of new image.
            //One of them
            // should equal maxDim.
            int scaledW = (int) (
                    scale * inImage.getWidth(null));
            int scaledH = (int) (
                    scale * inImage.getHeight(null));

            // Create an image buffer in
            // which to paint on.
            BufferedImage outImage =
                    new BufferedImage(scaledW, scaledH,
                                      BufferedImage.TYPE_INT_RGB);

            // Set the scale.
            AffineTransform tx = new AffineTransform();

            // If the image is smaller than
            // the desired image size,
            // don't bother scaling.
            // if (scale < 1.0d) {
                tx.scale(scale, scale);
            // }

            // Paint image.
            Graphics2D g2d =
                    outImage.createGraphics();
            g2d.drawImage(inImage, tx, null);
            g2d.dispose();

            // JPEG-encode the image
            //and write to file.
            OutputStream os =
                    new FileOutputStream(thumbPath);
            JPEGImageEncoder encoder =
                    JPEGCodec.createJPEGEncoder(os);
            encoder.encode(outImage);
            os.close();
        } catch (IOException e) {
            LogUtil.getLog(Thumbnail.class).error(e);
        }
    }

}

