package com.redmoon.oa.tigase.upload.commons.utils;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class ThumUtils {

	private static final String BMP = "bmp";
	private static final String PNG = "png";
	private static final String GIF = "gif";
	private static final String JPEG = "jpeg";
	private static final String JPG = "jpg";

	public static void getThumbnail(String source, String output, int width, int height) {
		boolean adjustSize = true;
		if (source == null || source.equals("") || width < 1 || height < 1) {
			return;
		}
		if (output == null || output.equals("")) {
			return;
		}
		source = source.toLowerCase();
		if (source.endsWith(BMP)) {
			BMPThumbnailHandler(source, output, width, height, adjustSize);
		} else if (source.endsWith(PNG) || source.endsWith(GIF) || source.endsWith(JPEG) || source.endsWith(JPG)) {
			thumbnailHandler(source, output, width, height, adjustSize);
		}
	}

	private static void thumbnailHandler(String source, String output, int width, int height, boolean adjustSize) {
		try {
			File sourceFile = new File(source);
			if (sourceFile.exists()) {
				Image image = ImageIO.read(sourceFile);
				int theImgWidth = image.getWidth(null);
				int theImgHeight = image.getHeight(null);
				int[] size = { theImgWidth, theImgHeight };
				if (adjustSize) {
					size = adjustImageSize(theImgWidth, theImgHeight, width, height);
				}
				StringBuffer thumbnailFile = new StringBuffer();
				// thumbnailFile.append(sourceFile.getParent());
				// thumbnailFile.append(File.separatorChar);
				thumbnailFile.append(output);
				writeFile(image, size[0], size[1], thumbnailFile.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	private static void BMPThumbnailHandler(String source, String output, int width, int height, boolean adjustSize) {
		try {
			File sourceFile = new File(source);
			if (sourceFile.exists()) {
				Image image = getBMPImage(source);
				int theImgWidth = image.getWidth(null);
				int theImgHeight = image.getHeight(null);
				int[] size = { theImgWidth, theImgHeight };
				if (adjustSize) {
					size = adjustImageSize(theImgWidth, theImgHeight, width, height);
				}
				StringBuffer thumbnailFile = new StringBuffer();
				// thumbnailFile.append(sourceFile.getParent());
				// thumbnailFile.append(File.separatorChar);
				thumbnailFile.append(output);
				writeFile(image, size[0], size[1], thumbnailFile.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	private static Image getBMPImage(String source) throws Exception {

		FileInputStream fs = null;
		Image image = null;
		try {
			fs = new FileInputStream(source);
			int bfLen = 14;
			byte bf[] = new byte[bfLen];
			fs.read(bf, 0, bfLen); // 读取14字节BMP文件头
			int biLen = 40;
			byte bi[] = new byte[biLen];
			fs.read(bi, 0, biLen); // 读取40字节BMP信息头

			// 源图宽度
			int nWidth = (((int) bi[7] & 0xff) << 24) | (((int) bi[6] & 0xff) << 16) | (((int) bi[5] & 0xff) << 8) | (int) bi[4] & 0xff;

			// 源图高度
			int nHeight = (((int) bi[11] & 0xff) << 24) | (((int) bi[10] & 0xff) << 16) | (((int) bi[9] & 0xff) << 8) | (int) bi[8] & 0xff;

			// 位数
			int nBitCount = (((int) bi[15] & 0xff) << 8) | (int) bi[14] & 0xff;

			// 源图大小
			int nSizeImage = (((int) bi[23] & 0xff) << 24) | (((int) bi[22] & 0xff) << 16) | (((int) bi[21] & 0xff) << 8) | (int) bi[20] & 0xff;

			// 对24位BMP进行解析
			if (nBitCount == 24) {
				int nPad = (nSizeImage / nHeight) - nWidth * 3;
				int nData[] = new int[nHeight * nWidth];
				byte bRGB[] = new byte[(nWidth + nPad) * 3 * nHeight];
				fs.read(bRGB, 0, (nWidth + nPad) * 3 * nHeight);
				int nIndex = 0;
				for (int j = 0; j < nHeight; j++) {
					for (int i = 0; i < nWidth; i++) {
						nData[nWidth * (nHeight - j - 1) + i] = (255 & 0xff) << 24 | (((int) bRGB[nIndex + 2] & 0xff) << 16)
								| (((int) bRGB[nIndex + 1] & 0xff) << 8) | (int) bRGB[nIndex] & 0xff;
						nIndex += 3;
					}
					nIndex += nPad;
				}
				Toolkit kit = Toolkit.getDefaultToolkit();
				image = kit.createImage(new MemoryImageSource(nWidth, nHeight, nData, 0, nWidth));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		} finally {
			if (fs != null) {
				fs.close();
			}
		}
		return image;
	}

	private static void writeFile(Image image, int width, int height, String thumbnailFile) throws Exception {

		if (image == null)
			return;

		BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		tag.getGraphics().drawImage(image, 0, 0, width, height, null);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(thumbnailFile);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			encoder.encode(tag);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		} finally {
			if (out != null) {
				out.close();
			}
		}

	}

	private static int[] adjustImageSize(int theImgWidth, int theImgHeight, int defWidth, int defHeight) {
		int[] size = { 0, 0 };

		float theImgHeightFloat = Float.parseFloat(String.valueOf(theImgHeight));
		float theImgWidthFloat = Float.parseFloat(String.valueOf(theImgWidth));
		if (theImgWidth < theImgHeight) {
			float scale = theImgHeightFloat / theImgWidthFloat;
			size[0] = Math.round(defHeight / scale);
			size[1] = defHeight;
		} else {
			float scale = theImgWidthFloat / theImgHeightFloat;
			size[0] = defWidth;
			size[1] = Math.round(defWidth / scale);
		}
		return size;
	}

	public static void main(String[] agrs) {
		// zoomPicture("E:\\1.jpg","E:\\test.jpg",60, 80);
		getThumbnail("E:\\9a504fc2d562853582cbf92392ef76c6a7ef638c.jpg", "E:\\test.jpg", 250, 358);
	}

}
