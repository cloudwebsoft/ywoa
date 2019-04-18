package com.redmoon.oa.util;

import java.io.File;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.file.FileUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

public class GetNewestFile {

	//private final static String separator = "oem_oa2.0";
	private final static String separator = "oa\\WebRoot";
	private final static String srcpath = "d:\\" + separator + "\\";
	private final static String dstpath = "C:\\MyProjects\\for submit\\"
			+ separator + "\\";
	private final static String date = "2016-06-02 09:00:00";
	//private final static String date = "2016-08-08 09:30:00";
	private static boolean flag;
	private final static boolean SAVE_CLASS = true;
	private final static boolean SAVE_BAK = false;

	/**
	 * @param File
	 * @param ZipOutputStream
	 * @param String
	 * @throws IOException
	 */
	public static void zipFile(File inFile, ZipOutputStream zos, String dir)
			throws IOException {
		if (inFile.isDirectory()) {
			File[] files = inFile.listFiles();
			for (File file : files)
				zipFile(file, zos, dir + (dir.equals("") ? "" : "\\")
						+ inFile.getName());
		} else {
			String entryName = null;
			if (!"".equals(dir))
				entryName = dir + "\\" + inFile.getName();
			else
				entryName = inFile.getName();
			ZipEntry entry = new ZipEntry(entryName);
			zos.putNextEntry(entry);
			FileInputStream fis = new FileInputStream(inFile);
			int len = 0;
			while ((len = fis.read()) != -1)
				zos.write(len);
			fis.close();
		}
	}

	/**
	 * @param String
	 */
	public static void getNewestDoc(String filePathSrc) {
		File fSrc = new File(filePathSrc);
		int index = filePathSrc.lastIndexOf(separator);
		if (index == -1) {
			index = filePathSrc.lastIndexOf(separator.toUpperCase());
		}
		String filePathDes = dstpath
				+ filePathSrc.substring(index + separator.length());
		if (!fSrc.exists()) {
			System.out.println(filePathSrc + " does not exist!");
			return;
		}
		File[] files = fSrc.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				if (files[i].getPath().startsWith(srcpath + "src")
						|| files[i].getPath().startsWith(srcpath + "WebRoot")
						|| files[i].getPath().startsWith(srcpath + "oabiz")
						|| files[i].getPath().startsWith(srcpath + "oamail")
						|| files[i].getPath().startsWith(srcpath + "mvc")
						|| files[i].getPath().startsWith(srcpath + "exam")
						|| files[i].getPath().startsWith(srcpath + "clouddisk")) {
					getNewestDoc(files[i].getPath());
				}
			} else {
				if (!SAVE_CLASS) {
					if (files[i].getPath().toLowerCase().endsWith(".class")) {
						continue;
					}
					if (files[i].getPath().toLowerCase().indexOf("web-inf") != -1) {
						continue;
					}
				}
				if (!SAVE_BAK) {
					if (files[i].getPath().toLowerCase().indexOf("bak") != -1) {
						continue;
					}
				}
				if (files[i].getPath().toLowerCase().indexOf(".svn") != -1) {
					continue;
				}
				if (files[i].getName().toLowerCase().equals("config_cws.xml")) {
					continue;
				}
				if (files[i].getName().toLowerCase().equals(".classpath")
						|| files[i].getName().toLowerCase().equals(
								".mymetadata")
						|| files[i].getName().toLowerCase()
								.equals(".myumldata")
						|| files[i].getName().toLowerCase().equals(".project")) {
					continue;
				}
				long last = files[i].lastModified();
				long today = DateUtil.toLong(DateUtil.parse(date,
						"yyyy-MM-dd HH:mm:ss"));
				if (last > today) {
					File fDst = new File(filePathDes);
					if (!fDst.exists()) {
						fDst.mkdirs();
					}
					FileUtil.CopyFile(files[i].getPath(), filePathDes + "\\"
							+ files[i].getName());
				}
			}
		}
	}

	/**
	 * 根据路径删除指定的目录或文件，无论存在与否
	 * 
	 * @param sPath
	 *            要删除的目录或文件
	 *@return 删除成功返回 true，否则返回 false。
	 */
	public static boolean deleteFolder(String sPath) {
		flag = false;
		File file = new File(sPath);
		// 判断目录或文件是否存在
		if (!file.exists()) { // 不存在返回 false
			return flag;
		} else {
			// 判断是否为文件
			if (file.isFile()) { // 为文件时调用删除文件方法
				return deleteFile(sPath);
			} else { // 为目录时调用删除目录方法
				return deleteDirectory(sPath);
			}
		}
	}

	/**
	 * 删除单个文件
	 * 
	 * @param sPath
	 *            被删除文件的文件名
	 * @return 单个文件删除成功返回true，否则返回false
	 */
	public static boolean deleteFile(String sPath) {
		flag = false;
		File file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}

	/**
	 * 删除目录（文件夹）以及目录下的文件
	 * 
	 * @param sPath
	 *            被删除目录的文件路径
	 * @return 目录删除成功返回true，否则返回false
	 */
	public static boolean deleteDirectory(String sPath) {
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		if (!sPath.endsWith(File.separator)) {
			sPath = sPath + File.separator;
		}
		File dirFile = new File(sPath);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		flag = true;
		// 删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 删除子文件
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} // 删除子目录
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag)
			return false;
		// 删除当前目录
		if (dirFile.delete()) {
			return true;
		} else {
			return false;
		}
	}

	public static void generateFileForSubmitting() {
		File inFile = new File(dstpath);
		File outFile = new File(inFile.getPath() + ".rar");
		if (inFile.exists()) {
			deleteFolder(dstpath);
		}
		if (outFile.exists()) {
			deleteFile(inFile.getPath() + ".rar");
		}
		getNewestDoc(srcpath);
		ZipOutputStream zos = null;
		try {
			zos = new ZipOutputStream(new FileOutputStream(outFile));
			zos.setEncoding("GBK");
			zos.setComment("The updated decision");
			zipFile(inFile, zos, "");
			System.out.println("success");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (zos != null) {
				try {
					zos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 */
	public static void simpleFileForSubmitting() {
		File inFile = new File(dstpath);
		File outFile = new File(inFile.getPath() + ".rar");
		if (inFile.exists()) {
			deleteFolder(dstpath);
		}
		if (outFile.exists()) {
			deleteFile(inFile.getPath() + ".rar");
		}
		getNewestDoc(srcpath);
		System.out.println("success");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		generateFileForSubmitting();
		// simpleFileForSubmitting();
	}
}
