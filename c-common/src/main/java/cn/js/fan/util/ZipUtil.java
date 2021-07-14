package cn.js.fan.util;

import java.io.File;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * @author Administrator
 * 
 */
public class ZipUtil {

	private final static String DEFAULT_CHARSET = "GBK";
	private final static String DEFAULT_EXTENSION = "zip";

	/**
	 * @Description:
	 * @param inFile
	 * @param visualFiles
	 * @param zos
	 * @param dir
	 * @throws IOException
	 */
	private static void zipFile(String inFile, ArrayList<String> visualFiles,
			ZipOutputStream zos, String dir) throws IOException {
		for (String file : visualFiles) {
			ZipEntry entry = new ZipEntry(file);
			zos.putNextEntry(entry);
			BufferedInputStream fis = new BufferedInputStream(
					new FileInputStream(inFile + File.separator + file));
			int len = 0;
			byte[] b = new byte[8192];
			while ((len = fis.read(b)) != -1) {
				zos.write(b, 0, len);
			}
			fis.close();
		}
	}

	/**
	 * @param pathsMap
	 *            key fullPath 文件全路径 value visualPath 打压缩包 文件的路径
	 * @param zos
	 * @param dir
	 * @throws IOException
	 */
	public static void zipFile(HashMap<String, String> pathsMap,
			ZipOutputStream zos, String dir) throws IOException {
		for (Map.Entry<String, String> entrys : pathsMap.entrySet()) {
			String fullPath = entrys.getKey();
			String visualPath = entrys.getValue();
			ZipEntry entry = new ZipEntry(visualPath);
			zos.putNextEntry(entry);
			BufferedInputStream fis = new BufferedInputStream(
					new FileInputStream(fullPath));
			int len = 0;
			byte[] b = new byte[8192];
			while ((len = fis.read(b)) != -1) {
				zos.write(b, 0, len);
			}
			fis.close();
		}
	}

	/**
	 * @Description: 将文件夹压缩至指定文件夹
	 * @param inPath
	 *            源文件夹路径
	 * @param inPath
	 *            需要打包的文件
	 * @param outPath
	 *            压缩文件路径
	 * @param outName
	 *            压缩文件名
	 * @return
	 */
	public static boolean zip(String inPath, ArrayList<String> visualFiles,
			String outPath, String outName) {
		return zip(inPath, visualFiles, outPath, outName, DEFAULT_EXTENSION,
				DEFAULT_CHARSET);
	}

	/**
	 * @Description: 将文件夹压缩至指定文件夹
	 * @param inPath
	 *            源文件夹路径
	 * @param inPath
	 *            需要打包的文件
	 * @param outPath
	 *            压缩文件路径
	 * @param outName
	 *            压缩文件名
	 * @param outExtension
	 *            压缩文件扩展名
	 * @param charset
	 *            压缩编码
	 * @return
	 */
	public static boolean zip(String inPath, ArrayList<String> visualFiles,
			String outPath, String outName, String outExtension, String charset) {
		File outFile = new File(outPath + File.separator + outName + "."
				+ outExtension);
		ZipOutputStream zos = null;
		try {
			zos = new ZipOutputStream(new FileOutputStream(outFile));
			zos.setEncoding(charset);
			zipFile(inPath, visualFiles, zos, "");
			return true;
		} catch (FileNotFoundException e) {
			LogUtil.getLog(ZipUtil.class).error("zip" + e.getMessage());
		} catch (IOException e) {
			LogUtil.getLog(ZipUtil.class).error("zip" + e.getMessage());
		} finally {
			if (zos != null) {
				try {
					zos.close();
				} catch (IOException e) {
					LogUtil.getLog(ZipUtil.class).error("zip" + e.getMessage());
				}
			}
		}
		return false;
	}

	/**
	 * @Description: 将不同路径文件夹压缩至指定文件夹
	 * @param pathsMap
	 *            key fullPath 文件全路径 value visualPath 打压缩包 文件的路径 * @param
	 *            outPath 压缩文件路径
	 * @param outName
	 *            压缩文件名
	 * @param outExtension
	 *            压缩文件扩展名
	 * @param charset
	 *            压缩编码
	 * @return
	 */
	public static boolean zip(HashMap<String, String> pathsMap, String outPath,
			String outName, String outExtension, String charset) {
		File outFile = new File(outPath + File.separator + outName + "."
				+ outExtension);
		ZipOutputStream zos = null;
		try {
			zos = new ZipOutputStream(new FileOutputStream(outFile));
			zos.setEncoding(charset);
			zipFile(pathsMap, zos, "");
			return true;
		} catch (FileNotFoundException e) {
			LogUtil.getLog(ZipUtil.class).error("zip" + e.getMessage());
		} catch (IOException e) {
			LogUtil.getLog(ZipUtil.class).error("zip" + e.getMessage());
		} finally {
			if (zos != null) {
				try {
					zos.close();
				} catch (IOException e) {
					LogUtil.getLog(ZipUtil.class).error("zip" + e.getMessage());
				}
			}
		}
		return false;
	}

	/**
	 * @Description: 将不同路径文件夹压缩至指定文件夹
	 * @param pathsMap
	 *            key fullPath 文件全路径
	 *            value visualPath 打压缩包 文件的路径
	 * @param outPath 压缩文件路径
	 * @param outName
	 *            压缩文件名
	 * @return
	 */
	public static boolean zip(HashMap<String, String> pathsMap, String outPath,
			String outName) {
		return zip(pathsMap, outPath, outName, DEFAULT_EXTENSION,
				DEFAULT_CHARSET);
	}

	/**
	 * @param File
	 * @param ZipOutputStream
	 * @param String
	 * @throws IOException
	 */
	private static void zipFile(File inFile, ZipOutputStream zos, String dir)
			throws IOException {
		if (inFile.isDirectory()) {
			File[] files = inFile.listFiles();
			for (int i = 0; i < files.length; i++) {
				zipFile(files[i], zos, dir + (dir.equals("") ? "" : "\\")
						+ inFile.getName());
			}
		} else {
			String entryName = null;
			if (!"".equals(dir)) {
				entryName = dir + "\\" + inFile.getName();
			} else {
				entryName = inFile.getName();
			}
			ZipEntry entry = new ZipEntry(entryName);
			zos.putNextEntry(entry);
			FileInputStream fis = new FileInputStream(inFile);
			int len = 0;
			while ((len = fis.read()) != -1) {
				zos.write(len);
			}
			fis.close();
		}
	}

	/**
	 * @Description: 将文件夹压缩至指定文件夹
	 * @param inPath
	 *            源文件夹路径
	 * @param outPath
	 *            压缩文件路径
	 * @param outName
	 *            压缩文件名
	 * @return
	 */
	public static boolean zip(String inPath, String outPath, String outName) {
		return zip(inPath, outPath, outName, DEFAULT_EXTENSION, DEFAULT_CHARSET);
	}

	/**
	 * @Description: 将文件夹压缩至指定文件夹
	 * @param inPath
	 *            源文件夹路径
	 * @param outPath
	 *            压缩文件路径
	 * @param outName
	 *            压缩文件名
	 * @param outExtension
	 *            压缩文件扩展名
	 * @param charset
	 *            压缩编码
	 * @return
	 */
	public static boolean zip(String inPath, String outPath, String outName,
			String outExtension, String charset) {
		File inFile = new File(inPath);
		File outFile = new File(outPath + File.separator + outName + "."
				+ outExtension);
		ZipOutputStream zos = null;
		try {
			zos = new ZipOutputStream(new FileOutputStream(outFile));
			zos.setEncoding(charset);
			zipFile(inFile, zos, "");
			return true;
		} catch (FileNotFoundException e) {
			LogUtil.getLog(ZipUtil.class).error("zip" + e.getMessage());
		} catch (IOException e) {
			LogUtil.getLog(ZipUtil.class).error("zip" + e.getMessage());
		} finally {
			if (zos != null) {
				try {
					zos.close();
				} catch (IOException e) {
					LogUtil.getLog(ZipUtil.class).error("zip" + e.getMessage());
				}
			}
		}
		return false;
	}
}
