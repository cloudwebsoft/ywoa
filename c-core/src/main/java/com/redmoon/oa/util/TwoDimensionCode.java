package com.redmoon.oa.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import jp.sourceforge.qrcode.QRCodeDecoder;
import jp.sourceforge.qrcode.exception.DecodingFailedException;
import cn.js.fan.web.Global;

import com.swetake.util.Qrcode;

public class TwoDimensionCode {
	// 创建文件夹存放二位码图片文件
	private static String FILE_PAHT;
	private static String FULL_PATH = Global.getFullRootPath() + "/";
	public static int imageWidth = 40;
	public static  int imageHeight = 40;
	
	/**
	 * 生成IOS Android下载路径
	 */
	public static void generate2DCodeByMobileClient() {
		String oaRealPath = Global.getAppPath();
		FULL_PATH = Global.getFullRootPath()+"/";
		FILE_PAHT = oaRealPath + "images/";
		// 动态生成二维码图片
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		File f = new File(FILE_PAHT + cfg.get("qrcode_mobile_png_path"));
		if (f.exists()) {
			f.delete();
		}
		TwoDimensionCode.generate2DCode(FULL_PATH
				+ "public/download_app.html", FILE_PAHT
				+ cfg.get("qrcode_mobile_png_path"), "png");
	}
	
	public static void generate2DCodeByImage() {
		String oaRealPath = Global.getAppPath();
		FILE_PAHT = oaRealPath + "images/";
		// 动态生成二维码图片
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		File f = new File(FILE_PAHT + cfg.get("qrcode_andriod_png_path"));
		if (f.exists())
			f.delete();

		TwoDimensionCode.generate2DCode(FULL_PATH
				+ cfg.get("qrcode_andriod_download_path"), FILE_PAHT
				+ cfg.get("qrcode_andriod_png_path"), "png");

	}
		/**
	 * IOS应用 APPSTORE链接 生成二维码(QRCode)图片
	 */
	public static void generate2DCodeByImage4IOS() {
		
		String oaRealPath = Global.getAppPath();
		
		FILE_PAHT = oaRealPath + "images/";
		// 动态生成二维码图片
		
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		//com.redmoon.oa.android.CloudConfig cloudcfg = new com.redmoon.oa.android.CloudConfig();
		File f = new File(FILE_PAHT + cfg.get("qrcode_ios_png_path"));
		if (f.exists())
			f.delete();
		com.redmoon.oa.android.CloudConfig cloudcfg = com.redmoon.oa.android.CloudConfig.getInstance();
		StringBuffer sb = new StringBuffer();
		sb.append(cfg.get("qrcode_ios_download_path"));
		sb.append(cloudcfg.getProperty("mobile.iosAppId"));
		
		TwoDimensionCode.generate2DCode(sb.toString(), FILE_PAHT
				+ cfg.get("qrcode_ios_png_path"), "png");

	}

	/**
	 * 生成二维码(QRCode)图片
	 * 
	 * @param content
	 *            存储内容
	 * @param imgPath
	 *            图片路径
	 */
	public void encoderQRCode(String content, String imgPath) {
		int size = (int) (Math.log(content.getBytes().length) / Math.log(2) + 1);
		try {
			encoderQRCode(content, imgPath, "png", size);
		} catch (Exception e) {
			try {
				encoderQRCode(content, imgPath, "png", size + 1);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * 生成二维码(QRCode)图片
	 * 
	 * @param content
	 *            存储内容
	 * @param output
	 *            输出流
	 */
	public void encoderQRCode(String content, OutputStream output) {
		int size = (int) (Math.log(content.getBytes().length) / Math.log(2) + 1);
		try {
			encoderQRCode(content, output, "png", size);
		} catch (Exception e) {
			encoderQRCode(content, output, "png", size + 1);
		}
	}

	/**
	 * 生成二维码(QRCode)图片
	 * 
	 * @param content
	 *            存储内容
	 * @param imgPath
	 *            图片路径
	 * @param imgType
	 *            图片类型
	 */
	public static void encoderQRCode(String content, String imgPath,
			String imgType) {
		int size = (int) (Math.log(content.getBytes().length) / Math.log(2) + 1);
		try {
			encoderQRCode(content, imgPath, imgType, size);
		} catch (Exception e) {
			try {
				encoderQRCode(content, imgPath, imgType, size + 1);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * 生成二维码(QRCode)图片
	 * 
	 * @param content
	 *            存储内容
	 * @param output
	 *            输出流
	 * @param imgType
	 *            图片类型
	 */
	public void encoderQRCode(String content, OutputStream output,
			String imgType) {
		int size = (int) (Math.log(content.getBytes().length) / Math.log(2) + 1);
		try {
			encoderQRCode(content, output, imgType, size);
		} catch (Exception e) {
			encoderQRCode(content, output, imgType, size + 1);
		}
	}

	/**
	 * 生成二维码(QRCode)图片
	 * 
	 * @param content
	 *            存储内容
	 * @param imgPath
	 *            图片路径
	 * @param imgType
	 *            图片类型
	 * @param size
	 *            二维码尺寸
	 * @throws Exception 
	 */
	public static void encoderQRCode(String content, String imgPath,
			String imgType, int size) throws Exception {
		try {
			BufferedImage bufImg = qRCodeCommon(content, imgType, size);
			createPhotoAtCenter(bufImg);
			File imgFile = new File(imgPath);
			File pFile=imgFile.getParentFile();
			if(!pFile.exists()){
				pFile.mkdirs();
			}
			// 生成二维码QRCode图片
			ImageIO.write(bufImg, imgType, imgFile);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
	
	

	  /**
     * 在二维码中间加入图片
     * 
     * @param bufImg
     * @return
     */
    public static BufferedImage createPhotoAtCenter(BufferedImage bufImg) throws Exception {
    
    	 Image im = ImageIO.read(new File(Global.getAppPath()+"/images/oa_logo.png"));
         Graphics2D g = bufImg.createGraphics();
         //获取bufImg的中间位置
         int centerX = bufImg.getMinX() + bufImg.getWidth()/2 - imageWidth/2;
         int centerY = bufImg.getMinY() + bufImg.getHeight()/2 - imageHeight/2;
         g.drawImage(im,centerX,centerY,imageWidth,imageHeight,null);
         g.dispose();
         bufImg.flush();
    	return bufImg;
    }


	/**
	 * 生成二维码(QRCode)图片
	 * 
	 * @param content
	 *            存储内容
	 * @param output
	 *            输出流
	 * @param imgType
	 *            图片类型
	 * @param size
	 *            二维码尺寸
	 */
	public void encoderQRCode(String content, OutputStream output,
			String imgType, int size) {
		try {
			BufferedImage bufImg = qRCodeCommon(content, imgType, size);
			// 生成二维码QRCode图片
			ImageIO.write(bufImg, imgType, output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 生成二维码(QRCode)图片的公共方法
	 * 
	 * @param content
	 *            存储内容
	 * @param imgType
	 *            图片类型
	 * @param size
	 *            二维码尺寸
	 * @return
	 * @throws Exception 
	 */
	private static BufferedImage qRCodeCommon(String content, String imgType,
			int size) throws Exception {
		BufferedImage bufImg = null;
		try {
			Qrcode qrcodeHandler = new Qrcode();
			// 设置二维码排错率，可选L(7%)、M(15%)、Q(25%)、H(30%)，排错率越高可存储的信息越少，但对二维码清晰度的要求越小
			qrcodeHandler.setQrcodeErrorCorrect('M');
			qrcodeHandler.setQrcodeEncodeMode('B');
			// 设置设置二维码尺寸，取值范围1-40，值越大尺寸越大，可存储的信息越大
			qrcodeHandler.setQrcodeVersion(size);
			// 获得内容的字节数组，设置编码格式
			byte[] contentBytes = content.getBytes("utf-8");
			// 图片尺寸
			int imgSize = 67 + 12 * (size - 1);
			bufImg = new BufferedImage(imgSize, imgSize,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D gs = bufImg.createGraphics();
			// 设置背景颜色
			gs.setBackground(Color.WHITE);
			gs.clearRect(0, 0, imgSize, imgSize);

			// 设定图像颜色> BLACK
			gs.setColor(Color.BLACK);
			// 设置偏移量，不设置可能导致解析出错
			int pixoff = 2;
			// 输出内容> 二维码
			if (contentBytes.length > 0 && contentBytes.length < 800) {
				boolean[][] codeOut = null;
				try {
					codeOut = qrcodeHandler.calQrcode(contentBytes);
				} catch (Exception e) {
					throw new Exception(e);
				}
				for (int i = 0; i < codeOut.length; i++) {
					for (int j = 0; j < codeOut.length; j++) {
						if (codeOut[j][i]) {
							gs.fillRect(j * 3 + pixoff, i * 3 + pixoff, 3, 3);
						}
					}
				}
			} else {
				throw new Exception("QRCode content bytes length = "
						+ contentBytes.length + " not in [0, 800].");
			}
			gs.dispose();
			bufImg.flush();
		} catch (Exception e1) {
			throw new Exception(e1);
		}
		return bufImg;
	}

	/**
	 * 解析二维码（QRCode）
	 * 
	 * @param imgPath
	 *            图片路径
	 * @return
	 */
	public static String decoderQRCode(String imgPath) {
		// QRCode 二维码图片的文件
		File imageFile = new File(imgPath);
		BufferedImage bufImg = null;
		String content = null;
		try {
			bufImg = ImageIO.read(imageFile);
			QRCodeDecoder decoder = new QRCodeDecoder();
			content = new String(decoder.decode(new TwoDimensionCodeImage(
					bufImg)), "utf-8");
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		} catch (DecodingFailedException dfe) {
			System.out.println("Error: " + dfe.getMessage());
			dfe.printStackTrace();
		}
		return content;
	}

	/**
	 * 解析二维码（QRCode）
	 * 
	 * @param input
	 *            输入流
	 * @return
	 */
	public String decoderQRCode(InputStream input) {
		BufferedImage bufImg = null;
		String content = null;
		try {
			bufImg = ImageIO.read(input);
			QRCodeDecoder decoder = new QRCodeDecoder();
			content = new String(decoder.decode(new TwoDimensionCodeImage(
					bufImg)), "utf-8");
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		} catch (DecodingFailedException dfe) {
			System.out.println("Error: " + dfe.getMessage());
			dfe.printStackTrace();
		}
		return content;
	}

	public static void generate2DCode(String content, String path, String surfix) {
		encoderQRCode(content, path, "png");
	}

	public static void generate2DCode2(String content, String path, String surfix) {
		try {
			encoderQRCode(content, path, "png", 11);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}