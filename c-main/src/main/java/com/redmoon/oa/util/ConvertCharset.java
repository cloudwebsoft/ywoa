package com.redmoon.oa.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.visual.ModuleUtil;
import com.redmoon.oa.visual.func.ConnStrFuncImpl;
import org.json.JSONException;
import org.json.JSONObject;

public class ConvertCharset {

	private final static String path = "D:\\oagroupprj\\forum\\src\\";

	// 读取的文件
	private static String fileIn;

	// 读取時文件用的编码
	private static String fileInEn;

	// 写出的文件
	private static String fileOut;

	// 写出時文件用的编码
	private static String fileOutEn;

	private static final String GBK = "GBK";
	private static final String UTF8 = "UTF-8";
	private static final String UTF16 = "UTF-16";
	private static final String UNICODE = "UNICODE";
	private static final String ISO = "ISO-8859-1";

	public static void ConvertAll(String sPath) {
		File dirFile = new File(sPath);
		File[] files = dirFile.listFiles();

		for (File file : files) {
			if (file.isDirectory()) {
				ConvertAll(file.getPath());
			} else if (file.isFile()) {
				String fullpath = file.getPath();
				String fileCode = getFileEnCode(fullpath);

				if (!fileCode.equals(UTF8)) {
					setFileIn(fullpath, fileCode);
					setFileOut(fullpath, UTF8);
					start();
				}
			}
		}
	}

	/**
	 * 获取源文件的编码
	 * 
	 * @param filePath
	 *            源文件所在的绝对路径
	 * @return
	 */
	public static String getFileEnCode(String filePath) {
		InputStream inputStream = null;
		String code = GBK;
		try {
			inputStream = new FileInputStream(filePath);
			byte[] head = new byte[3];
			inputStream.read(head);

			if (head[0] == -1 && head[1] == -2) {
				code = UTF16;
			} else if (head[0] == -2 && head[1] == -1) {
				code = UNICODE;
			} else if (head[0] == -17 && head[1] == -69 && head[2] == -65) {
				code = UTF8;
			} else {
				code = GBK;
			}
		} catch (Exception e) {
			LogUtil.getLog(ConvertCharset.class).error(e);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				LogUtil.getLog(ConvertCharset.class).error(e);
			}
		}
		return code;
	}

	public static void setFileIn(String fileInPath, String fileInEncoding) {
		setFileIn(fileInPath);
		setFileInEn(fileInEncoding);
	}

	public static void setFileOut(String fileOutPath, String fileOutEncoding) {
		setFileOut(fileOutPath);
		setFileOutEn(fileOutEncoding);
	}

	public static void start() {
		String str = read(fileIn);
		write(fileOut, fileOutEn, str);
	}

	/**
	 * 读文件
	 * 
	 * @param fileName
	 */
	private static String read(String fileName) {
		BufferedReader in = null;
		try {
			String[] encode = { UTF8, GBK, ISO, UTF16, UNICODE };
			StringBuilder sb = null;
			String str = "";
			File file = new File(fileName);
			for (String temp : encode) {
				sb = new StringBuilder();
				in = new BufferedReader(new InputStreamReader(
						new FileInputStream(fileName), temp));

				int rows = 0;	// 统计行数,防止换行符为\n的情况导致字符数的差异
				while ((str = in.readLine()) != null) {
					sb.append(str).append("\r\n");
					rows++;
				}
				in.close();
				if (file.length() == sb.toString().getBytes(temp).length
						|| file.length() == sb.toString().getBytes(temp).length - 2
						|| file.length() == sb.toString().getBytes(temp).length
								- rows
						|| file.length() == sb.toString().getBytes(temp).length
								- 2 - rows) {
					fileInEn = temp;
					break;
				}
			}

			return sb.toString();
		} catch (Exception ex) {
			LogUtil.getLog(ConvertCharset.class).error(ex);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				LogUtil.getLog(ConvertCharset.class).error(e);
			}
		}
		return "";
	}

	/**
	 * 写文件
	 * 
	 * @param fileName
	 *            新的文件名
	 * @param encoding
	 *            写出的文件的编码方式
	 * @param str
	 */
	private static void write(String fileName, String encoding, String str) {
		try {
			Writer out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileName), encoding));
			out.write(str);
			out.close();
		} catch (Exception ex) {
			LogUtil.getLog(ConvertCharset.class).error(ex);
		}
	}

	public String getFileIn() {
		return fileIn;
	}

	public static void setFileIn(String fileIn) {
		ConvertCharset.fileIn = fileIn;
	}

	public String getFileInEn() {
		return fileInEn;
	}

	public static void setFileInEn(String fileInEn) {
		ConvertCharset.fileInEn = fileInEn;
	}

	public String getFileOut() {
		return fileOut;
	}

	public static void setFileOut(String fileOut) {
		ConvertCharset.fileOut = fileOut;
	}

	public String getFileOutEn() {
		return fileOutEn;
	}

	public static void setFileOutEn(String fileOutEn) {
		ConvertCharset.fileOutEn = fileOutEn;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ConvertAll(path);
		if (true)
			return;

		String str = "{formCode:aaa, sourceFormCode:shouwen, idField:id, showField:wh, filter:cwsId%eq$parentId, isParentSaveAndReload:true, maps:[]}";
		JSONObject json = null;
		try {
			json = new JSONObject(str);
		} catch (JSONException e) {
			LogUtil.getLog(ConvertCharset.class).error(e);
			str = "json 格式非法";
		}
	}

}
