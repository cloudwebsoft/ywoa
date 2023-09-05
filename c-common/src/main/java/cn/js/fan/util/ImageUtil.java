package cn.js.fan.util;

import com.cloudwebsoft.framework.util.LogUtil;
import org.apache.commons.codec.binary.Base64;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.*;
import javax.swing.ImageIcon;

/**
 * Image utilities class.
 *
 * @author T55555
 * @version 1.0 2003-01-29
 */
public class ImageUtil {

    /**
     * Return scaled image.
     * Pre-conditions: (source != null) && (width > 0) && (height > 0)
     *
     * @param source the image source
     * @param width the new image's width
     * @param height the new image's height
     * @return the new image scaled
     */
    public static BufferedImage getScaleImage(BufferedImage source,
                                              int width, int height) {
        //assert(source != null && width > 0 && height > 0);
		int type = source.getType();
		if (type==0) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
        BufferedImage image = new BufferedImage(width, height, type);
        image.createGraphics().drawImage(source, 0, 0, width, height, null);
        return image;
    }

    /**
     * Return scaled image.
     * Pre-conditions: (source != null) && (xscale > 0) && (yscale > 0)
     *
     * @param source the image source
     * @param xscale the percentage of the source image's width
     * @param yscale the percentage of the source image's height
     * @return the new image scaled
     */
    public static BufferedImage getScaleImage(BufferedImage source,
                                              double xscale, double yscale) {
        //assert(source != null && width > 0 && height > 0);
        return getScaleImage(source,
                (int)(source.getWidth() * xscale), (int)(source.getHeight() * yscale));
    }

    /**
     * Read the input image file, scaled then write the output image file.
     *
     * @param input the input image file
     * @param output the output image file
     * @param width the output image's width
     * @param height the output image's height
     * @return true for sucessful,
     * false if no appropriate reader or writer is found.
     */
    public static boolean scaleImage(File input, File output,
                                     int width, int height) throws IOException {
        BufferedImage image = ImageIO.read(input);
        if (image == null) { return false; }
        image = getScaleImage(image, width, height);
        String name = output.getName();
        String format = name.substring(name.lastIndexOf('.')+1).toLowerCase();
        return ImageIO.write(image, format, output);
    }

    /**
     * Read the input image file, scaled then write the output image file.
     *
     * @param input the input image file
     * @param output the output image file
     * @param xscale the percentage of the input image's width for output image's width
     * @param yscale the percentage of the input image's height for output image's height
     * @return true for sucessful,
     * false if no appropriate reader or writer is found.
     */
    public static boolean scaleImage(File input, File output,
                                     double xscale, double yscale) throws IOException {
        BufferedImage image = ImageIO.read(input);
        if (image == null) { return false; }
        image = getScaleImage(image, xscale, yscale);
        String name = output.getName();
        String format = name.substring(name.lastIndexOf('.')+1).toLowerCase();
        return ImageIO.write(image, format, output);
    }

    /**
     * 固定图的宽度，高度按比例变化，如果图的宽度小于指定的宽度，则用其原来的宽度
     * @param input File
     * @param output File
     * @return boolean
     * @throws IOException
     */
    public static boolean Image2Thumb(File input, File output,
                                     int thumbWidth) throws IOException {
       BufferedImage image = ImageIO.read(input);
       if (image == null) {
           return false;
       }
       int w = image.getWidth();
       int h = image.getHeight();

        double dHeight = ((double)thumbWidth)/w * h;
        int height = (int)dHeight;
        if (w>thumbWidth) {
			image = getScaleImage(image, thumbWidth, height);
		} else {
			image = getScaleImage(image, w, h);
		}
        String name = output.getName();
        String format = name.substring(name.lastIndexOf('.')+1).toLowerCase();
        return ImageIO.write(image, format, output);
    }

    /**
     * 文件分割，隻是切成了小文件，並不是小圖片，需要合並後纔能看到圖片
     * @param imgPath String 文件路径
     * @param m int 要分割的份数
     */
	public static void splitFile(String imgPath, int m) {
		File src = new File(imgPath);
		if (src.isFile()) {
			// 获取文件的总长度
			long fileLength = src.length();
			// 获取文件名
			String fileName = src.getName().substring(0,
					src.getName().indexOf("."));
			// 获取文件后缀
			String ext = src.getName().substring(
					src.getName().lastIndexOf("."));
			InputStream in = null;
			try {
				in = new FileInputStream(src);
				for (int i = 1; i <= m; i++) {
					StringBuffer sb = new StringBuffer();
					sb.append(src.getParent()).append("\\").append(fileName)
							.append("_data").append(i).append(ext);
					File file2 = new File(sb.toString());
					// 创建写文件的输出流
					OutputStream out = new FileOutputStream(file2);
					boolean isWrited = false;
					int len = -1;
					byte[] bytes = new byte[600 * 600];
					while ((len = in.read(bytes)) != -1) {
						isWrited = true;
						out.write(bytes, 0, len);
						if (file2.length() > (fileLength / m)) {
							break;
						}
					}
					out.close();
					
					if (!isWrited) {
						file2.delete();
						break;
					}
				}
			} catch (Exception e) {
				LogUtil.getLog(ImageUtil.class).error(e);
			} finally {
				try {
					in.close();
				} catch (IOException e) {
					LogUtil.getLog(ImageUtil.class).error(e);
				}
			}
		}
	}

	// 文件合并的方法（传入要合并的文件路径）
	public static void joinFile(String... src) {
		for (int i = 0; i < src.length; i++) {
			File file = new File(src[i]);
			String fileName = file.getName().substring(0,
					file.getName().indexOf("_"));
			String endName = file.getName().substring(
					file.getName().lastIndexOf("."));
			StringBuffer sb = new StringBuffer();
			sb.append(file.getParent()).append("\\").append(fileName).append(
					endName);
			try {
				// 读取小文件的输入流
				InputStream in = new FileInputStream(file);
				// 写入大文件的输出流
				File file2 = new File(sb.toString());
				OutputStream out = new FileOutputStream(file2, true);
				int len = -1;
				byte[] bytes = new byte[10 * 1024 * 1024];
				while ((len = in.read(bytes)) != -1) {
					out.write(bytes, 0, len);
				}
				out.close();
				in.close();
			} catch (Exception e) {
				LogUtil.getLog(ImageUtil.class).error(e);
			}
		}
	}
	
	public static BufferedImage readImage(String imgUrl) {
        BufferedImage img = null;
        try {
            if (imgUrl.startsWith("http:")) {
                img = ImageIO.read(new URL(imgUrl));
            } else {
            	// 會導ImageIO.write時致圖片發紅
                // img = ImageIO.read(new File(imgUrl));
            	img = toBufferedImage(new File(imgUrl));
            }
        } catch (MalformedURLException e) {
            LogUtil.getLog(ImageUtil.class).error(e);
        } catch (IOException e) {
            LogUtil.getLog(ImageUtil.class).error(e);
        }

        return img;
    }	
	
	public static BufferedImage toBufferedImage(File img) throws IOException {
        Image image = Toolkit.getDefaultToolkit().getImage(img.getPath());
        BufferedImage bimage = null;
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        image = new ImageIcon(image).getImage();
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        if (width < 0 || height < 0) {
            /*Map<String,Integer> map = getImageSize(img);
            width = map.get(IMAGE_WIDTH);
            height = map.get(IMAGE_HEIGHT);*/
            return null;
        }
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            int transparency = Transparency.OPAQUE;
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(width, height, transparency);
        } catch (HeadlessException e) {
            LogUtil.getLog(ImageUtil.class).error(e);
        } catch (Exception e) {
            LogUtil.getLog(ImageUtil.class).error(e);
        }

        if (bimage == null) {
            int type = BufferedImage.TYPE_INT_RGB;
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }
        Graphics g = bimage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bimage;
    }	
	
	private static final int[] RGB_MASKS = {0xFF0000, 0xFF00, 0xFF};
	private static final ColorModel RGB_OPAQUE =
	    new DirectColorModel(32, RGB_MASKS[0], RGB_MASKS[1], RGB_MASKS[2]);
	
    public static BufferedImage cat(int x, int y, int wight, int hight,
    		BufferedImage img, String fileName, String suffix, String ext) {
    	int w = img.getWidth();
    	int h = img.getHeight();
        int[] simgRgb = new int[wight * hight];
        img.getRGB(x, y, wight, hight, simgRgb, 0, wight);
        
        BufferedImage newImage = new BufferedImage(wight, hight,
                BufferedImage.TYPE_INT_ARGB);
        newImage.setRGB(0, 0, wight, hight, simgRgb, 0, wight);
        /*
         * 原來導齣的JPG图片包含了一个Alpha（透明）通道，一共四个通道，jpg不支持Alpha通道，所以导致了直接write變紅的现象
        try {
            ImageIO.write(newImage, "JPEG", new File(fileName + suffix + "." + ext));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            LogUtil.getLog(ImageUtil.class).error(e);
        }  
        */
    	PixelGrabber pg = new PixelGrabber(newImage, 0, 0, -1, -1, true);
    	try {
			pg.grabPixels();
	    	int width = pg.getWidth(), height = pg.getHeight();

	    	DataBuffer buffer = new DataBufferInt((int[]) pg.getPixels(), pg.getWidth() * pg.getHeight());
	    	WritableRaster raster = Raster.createPackedRaster(buffer, width, height, width, RGB_MASKS, null);
	    	BufferedImage bi = new BufferedImage(RGB_OPAQUE, raster, false, null);   
            ImageIO.write(bi, "JPEG", new File(fileName + suffix + "." + ext));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(ImageUtil.class).error(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(ImageUtil.class).error(e);
		}
             
        return newImage;
    }	
	
    public static void main(String argv[]) {
/*      try {
            ImageUtil.splitFile("d:\\hysht.jpg", 10);
        } catch (Exception e) {
            LogUtil.getLog(ImageUtil.class).error(e);
        }*/
    	String imgUrl = "d:\\test\\hysht.jpg";
    	
    	File src = new File(imgUrl);
		String fileName = src.getName().substring(0,
				src.getName().indexOf("."));
		// 获取文件后缀
		String ext = src.getName().substring(
				src.getName().lastIndexOf("."));
		
		fileName = "d:\\test\\" + fileName;

    	BufferedImage img = readImage(imgUrl);
    	int w = 500, h = 500;
    	int imgW = img.getWidth();
    	int imgH = img.getHeight();
    	
    	int wCount = imgW/w;
    	if (imgW % w != 0) {
    		wCount++;
    	}
    	
    	int hCount = imgH/h;
    	if (imgH % h !=0) {
    		hCount++;
    	}

    	for (int i=0; i<hCount; i++) {
    		for (int j=0; j<wCount; j++) {
    			int x = j*w;
    			int y = i*h;
    			int blockW = w;
    			int blockH = h;
    			if (x+w>imgW) {
    				blockW = imgW-x;
    			}
    			if (y+h>imgH) {
    				blockH = imgH - y;
    			}
    			
    			String suffix = "_" + i + "_" + j;
    	    	ImageUtil.cat(x, y, blockW, blockH, img, fileName, suffix, ext);
    		}
    	}
    }

	// 参数imgFile：图片完整路径
	public static String getImgBase64Str(String imgFile) {
		// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
		InputStream in = null;
		byte[] data = null;
		// 读取图片字节数组
		try {
			in = new FileInputStream(imgFile);
			data = new byte[in.available()];
			in.read(data);
			in.close();
		} catch (IOException e) {
			LogUtil.getLog(ImageUtil.class).error(e);
		}
		return Base64.encodeBase64String(data);
	}
}

