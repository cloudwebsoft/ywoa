package com.redmoon.oa.netdisk;

import cn.js.fan.util.NumberUtil;

public class UtilTools {

	/**
	 * @Description: 将字节数转为适合的单位对应的大小
	 * @param src
	 * @return
	 */
	public static String getFileSize(long src) {
		boolean isNegative = src < 0;
		long temp = Math.abs(src);
		double dst = 0.0f;
		int i = 0;
		char unit = ' ';

		for (i = 0; temp >= 1024; i++) {
			dst = (dst == 0.0f ? temp : dst) / 1024.0;
			temp = temp / 1024;
		}
		switch (i) {
		case 0:
			dst = (double) src;
			unit = ' ';
			break;
		case 1:
			unit = 'K';
			break;
		case 2:
			unit = 'M';
			break;
		case 3:
			unit = 'G';
			break;
		case 4:
			unit = 'T';
			break;
		default:
			unit = 'B';
			break;
		}

		return NumberUtil.round((isNegative ? -1 : 1) * dst + 0.05, 1) + " "
				+ unit + 'B';
	}

	public static String getIcon(String ext) {
		if (ext == null) {
			return "default.png";
		}
		switch (ext) {
			case "gif":
			case "jpg":
			case "png":
			case "jpeg":
			case "bmp":
			case "jpe":
				return "bmp.png";
			case "doc":
			case "docx":
			case "wps":
			case "wpt":
				return "doc.png";
			case "pdf":
				return "pdf.png";
			case "apk":
				return "apk.png";
			case "txt":
				return "txt.png";
			case "xls":
			case "xlsx":
			case "et":
				return "xls.png";
			case "html":
			case "htm":
				return "html.png";
			case "ppt":
			case "pptx":
			case "dps":
				return "ppt.png";
			case "rar":
			case "zip":
				return "zip.png";
			case "wma":
			case "mp3":
				return "mp3.png";
			case "mov":
			case "avi":
			case "rmvb":
			case "flv":
			case "wmv":
			case "3gp":
			case "mp4":
				return "mp4.png";
		}
		return "default.png";
	}
}
