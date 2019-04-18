package com.redmoon.oa.netdisk;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

import org.pdfbox.cos.COSDocument;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.ui.menu.MostRecentlyUsedMenuDb;

import cn.js.fan.util.NumberUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.file.FileUtil;

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

	/**
	 * @Description: 文件夹拷贝
	 * @param src
	 * @param dst
	 * @return
	 */
	public static void copyDir(File src, File dst) {
		if (!dst.exists()) {
			dst.mkdirs();
		}
		File[] files = src.listFiles();
		if (files == null) {
			return;
		}
		for (File file : files) {
			String srcName = file.getAbsolutePath();
			String dstName = dst + File.separator + file.getName();
			if (file.isFile()) {
				FileUtil.CopyFile(srcName, dstName);
			} else if (file.isDirectory()) {
				copyDir(file, new File(dstName));
			}
		}
	}

	/**
	 * @Description: 获取某个文件夹下所有文件(含子文件夹下文件)
	 * @param dir
	 * @return
	 */
	public static void getAllFileInDir(String dir, ArrayList<File> list) {
		File root = new File(dir);
		if (!root.exists()) {
			return;
		}
		File[] files = root.listFiles();
		if (files == null) {
			return;
		}
		for (File file : files) {
			if (file.isFile()) {
				list.add(file);
			} else if (file.isDirectory()) {
				getAllFileInDir(file.getAbsolutePath(), list);
			}
		}
	}

	/**
	 * 获取XML文件里的图片格式，在页面判断以区分
	 * 
	 * @return
	 */
	public static HashMap<String, Boolean> getExtType(String type) {
		com.redmoon.clouddisk.Config rcfgImg = com.redmoon.clouddisk.Config
				.getInstance();
		String extTypeImg = rcfgImg.getProperty(type);
		HashMap<String, Boolean> map = new HashMap<String, Boolean>();

		String[] exts = extTypeImg.split(",");
		for (String ext : exts) {
			map.put(ext, true);
		}
		return map;
	}

	public static HashMap<String, String> uploadFileTypeByExplorer(String type) {
		HashMap<String, String> map = new HashMap<String, String>();
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String netdisk_ext = cfg.get(type);
		StringBuilder uploadIeExt = new StringBuilder();
		StringBuilder uploadFixfoxExt = new StringBuilder();
		String[] netdisk_ext_array = netdisk_ext.split(",");
		if (netdisk_ext_array != null && netdisk_ext_array.length > 0) {
			for (String fileType : netdisk_ext_array) {
				if (uploadIeExt.length() == 0) {
					uploadIeExt.append("*.").append(fileType);
				} else {
					uploadIeExt.append(";*.").append(fileType);
				}
				if (uploadFixfoxExt.length() == 0) {
					uploadFixfoxExt.append("*.").append(fileType);
				} else {
					uploadFixfoxExt.append(";,*.").append(fileType);
				}
			}
		}
		map.put("ie_upload_file_types", uploadIeExt.toString());
		map.put("fixfox_upload_file_types", uploadFixfoxExt.toString());
		return map;
	}

	/**
	 * 获取xml文件里格式，并判断
	 * 
	 */
	public static int getConfigType(String ext) {
		HashMap mapImg = getExtType("exttype_1");
		HashMap mapTxt = getExtType("exttype_5");
		HashMap mapWord = getExtType("exttype_6");
		int i = 0;
		if (mapImg.containsKey(ext)) {
			return i = 1;
		} else if (mapTxt.containsKey(ext)) {
			return i = 5;
		} else if (mapWord.containsKey(ext)) {
			return i = 6;
		} else {
			return i;
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
		String code = "GBK";
		try {
			inputStream = new FileInputStream(filePath);
			byte[] head = new byte[3];
			inputStream.read(head);

			if (head[0] == -2 && head[1] == -1) {
				code = "UNICODE";
			} else if (head[0] == -17 && head[1] == -69 && head[2] == -65) {
				code = "utf-8";
			} else {
				code = "GBK";
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return code;
	}

	/**
	 * 判断文件的编码格式
	 * 
	 * @param fileName
	 *            :file
	 * @return 文件编码格式
	 * @throws Exception
	 */
	public static String codeString(String fileName) throws Exception {
		BufferedInputStream bin = new BufferedInputStream(new FileInputStream(
				fileName));
		int p = (bin.read() << 8) + bin.read();
		String code = null;

		switch (p) {
		case 0xefbb:
			code = "UTF-8";
			break;
		case 0xfffe:
			code = "Unicode";
			break;
		case 0xfeff:
			code = "UTF-16BE";
			break;
		default:
			code = "GBK";
		}
		return code;
	}

	public static String getTextFromTxt(String filePath) throws Exception {
		StringBuilder sb = new StringBuilder();
		BufferedReader in = null;
		try {
			String[] encode = { "utf-8", "GBK" }; // 默认返回最后一种编码类型
			String str = "";
			File file = new File(filePath);
			// 首先判断是否是Unicode
			String type = codeString(filePath);
			if (type.equals("Unicode")) {
				sb = new StringBuilder();
				in = new BufferedReader(new InputStreamReader(
						new FileInputStream(filePath), type));
				while ((str = in.readLine()) != null) {
					if (!sb.toString().equals("")) {
						sb.append("\r\n");
					}
					sb.append(str);
				}
				in.close();
			} else {
				for (String temp : encode) {
					sb = new StringBuilder();
					in = new BufferedReader(new InputStreamReader(
							new FileInputStream(filePath), temp));

					int rows = 0; // 统计行数,防止换行符为\n的情况导致字符数的差异
					while ((str = in.readLine()) != null) {
						if (!sb.toString().equals("")) {
							sb.append("\r\n");
						}
						sb.append(str);
						rows++;
					}
					in.close();
					// System.out.println(file.length() + "----"
					// + sb.toString().getBytes(temp).length + temp);
					if (file.length() == sb.toString().getBytes(temp).length
							|| file.length() == sb.toString().getBytes(temp).length - 2
							|| file.length() == sb.toString().getBytes(temp).length + 2
							|| file.length() == sb.toString().getBytes(temp).length
									- rows
							|| file.length() == sb.toString().getBytes(temp).length
									- 2 - rows
							|| file.length() == sb.toString().getBytes(temp).length
									+ 2 - rows) {
						break;
					}
				}
			}
			return sb.toString().replaceAll("\\\r\\\n", "<br/>");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	public static String getTextFromRtf(String filePath) {
		String result = null;
		File file = new File(filePath);
		try {
			DefaultStyledDocument styledDoc = new DefaultStyledDocument();
			InputStream is = new FileInputStream(file);
			new RTFEditorKit().read(is, styledDoc, 0);
			result = new String(styledDoc.getText(0, styledDoc.getLength())
					.getBytes("UTF-8"));
			// 提取文本，读取中文需要使用UTF-8编码，否则会出现乱码
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return result;
	}

	// PDF预览 (纯文字版)
	public static String getTextFormPdf(String filepath) {
		PDDocument pdfDocument = null;
		FileInputStream fis = null;
		String msg = "";
		try {
			// 读PDF内容
			fis = new FileInputStream(filepath);
			// 创建PDFTextStripper对象
			PDFTextStripper stripper = new PDFTextStripper();
			// 获取PDDocument
			pdfDocument = PDDocument.load(fis);
			// 创建StringWriter对象
			StringWriter w = new StringWriter();
			stripper.writeText(pdfDocument, w);
			// 内容
			msg = w.getBuffer().toString();

		} catch (Exception e) {
			LogUtil.getLog("UtilTools").error(StrUtil.trace(e));
		} finally {
			if (pdfDocument != null) {
				try {
					// 关闭PDF
					COSDocument cos = pdfDocument.getDocument();
					// 关闭cos
					cos.close();
					// 关闭pdfDocument
					pdfDocument.close();
					// 关闭FileInputStream
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return msg;
	}

	/**
	 * 获得某个物理文件夹的层级,以及物理文件夹是否存在
	 * 
	 * @param absolutePath
	 * @param name
	 * @param i
	 * @return
	 */
	public static JSONObject getLayerByDir(String absolutePath, String name,
			JSONObject json) {
		int layer;
		try {
			layer = json.getInt("layer");
			File root = new File(absolutePath);
			if (root.exists()) {
				File[] files = root.listFiles();
				for (File file : files) {
					if (file.isDirectory()) {
						if (!file.getName().equals(name)) {
							layer++;
							json.put("layer", layer);
							return getLayerByDir(file.getAbsolutePath(), name,
									json);
						} else {
							layer++;
							json.put("layer", layer);
							json.put("isExists", true);
							break;
						}
					}
				}
			}
		} catch (JSONException e) {
			Logger.getLogger(UtilTools.class).error(
					"getLayerByDir:" + e.toString());
		}// 获得层级

		return json;
	}

	public static String ShowMenuAsOption(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		Privilege privilege = new Privilege();
		MostRecentlyUsedMenuDb mrum = new MostRecentlyUsedMenuDb();
		Vector<MostRecentlyUsedMenuDb> v = mrum.getMenuBeingStored(privilege
				.getUser(request));
		String[] clouddiskMenu = new String[] { "我的云盘", "我发起的协作", "我参与的协作",
				"回收站", "资源库" };
		sb.append("<option value=\"oa.jsp\">云盘首页</option>");
		sb
				.append("<optgroup label='├『常用菜单』' value=\"-1\" class=\"a_option\" id=\"-1\">├『常用菜单』</optgroup>");

		if (v != null && !v.isEmpty()) {
			Iterator<MostRecentlyUsedMenuDb> iterator = v.iterator();
			String menuCode = "";
			int k = 0;
			while (iterator.hasNext()) {
				mrum = iterator.next();
				menuCode = mrum.getString("menu_code");
				com.redmoon.oa.ui.menu.Leaf leaf = new com.redmoon.oa.ui.menu.Leaf();
				leaf = leaf.getLeaf(menuCode);
				if (leaf == null || !leaf.isUse()) {
					continue;
				}
				sb.append("<option value=\"").append(leaf.getLink()).append(
						"\"  class=\"a_option\" id=\"").append(leaf.getCode())
						.append("\">　").append(leaf.getName()).append(
								"</option>");
				k++;
				if (k == 10)
					break;
			}
		}

		com.redmoon.oa.ui.menu.LeafChildrenCacheMgr lccm = new com.redmoon.oa.ui.menu.LeafChildrenCacheMgr(
				com.redmoon.oa.ui.menu.Leaf.CODE_ROOT);
		Iterator ir = lccm.getChildren().iterator();
		int k = 2;
		while (ir.hasNext()) {
			com.redmoon.oa.ui.menu.Leaf lf = (com.redmoon.oa.ui.menu.Leaf) ir
					.next();
			if (!lf.canUserSee(request)
					|| lf.getCode().equals(
							com.redmoon.oa.ui.menu.Leaf.CODE_BOTTOM)
					|| !lf.isUse()) {
				continue;
			}
			com.redmoon.oa.ui.menu.LeafChildrenCacheMgr lccm2 = new com.redmoon.oa.ui.menu.LeafChildrenCacheMgr(
					lf.getCode());
			Vector v2 = lccm2.getChildren();

			if (!lf.getLink(request).equals("")) {
				sb.append("<option value=\"").append(lf.getLink()).append(
						"\"  class=\"a_option\" id=\"").append(lf.getCode())
						.append("\">").append(lf.getName()).append("</option>");
			} else {
				sb.append("<optgroup label='├『"+lf.getName()+"』' value=\"").append(lf.getLink()).append(
						"\"  class=\"a_option\" id=\"").append(lf.getCode())
						.append("\">├『").append(lf.getName()).append(
								"』</optgroup>");
			}

			if (v2.size() > 0) {
				k++;
				Iterator ir2 = v2.iterator();
				while (ir2.hasNext()) {
					com.redmoon.oa.ui.menu.Leaf lf2 = (com.redmoon.oa.ui.menu.Leaf) ir2
							.next();
					if (!lf2.canUserSee(request) || !lf2.isUse()) {
						continue;
					}
					com.redmoon.oa.ui.menu.LeafChildrenCacheMgr lccm3 = new com.redmoon.oa.ui.menu.LeafChildrenCacheMgr(
							lf2.getCode());
					Vector v3 = lccm3.getChildren();
					if (v3.size() == 0) {
						sb.append("<option value=\"").append(lf2.getLink())
								.append("\"  class=\"a_option\" id=\"").append(
										lf2.getCode()).append("\">　").append(
										lf2.getName()).append("</option>");
						if (lf2.getCode().equals("netdisk")) {
							for (int i = 0; i < clouddiskMenu.length; i++) {
								sb
										.append(
												"<option value=\"netdisk/clouddisk.jsp?page_no=")
										.append(i + 1).append(
												"\" class=\"a_option\" id=\"")
										.append(lf.getCode()).append("_")
										.append(i + 1).append("\">　　").append(
												clouddiskMenu[i]).append(
												"</option>");
							}
						}
					} else {
						sb.append("<optgroup label='╋"+lf2.getName()+"' value=\"").append(lf2.getLink())
								.append("\"  class=\"a_option\" id=\"").append(
										lf2.getCode()).append("\">　╋").append(
										lf2.getName()).append("</optgroup>");
						k++;
						Iterator ir3 = v3.iterator();
						while (ir3.hasNext()) {
							com.redmoon.oa.ui.menu.Leaf lf3 = (com.redmoon.oa.ui.menu.Leaf) ir3
									.next();
							if (!lf3.canUserSee(request) || !lf3.isUse()) {
								continue;
							}
							sb.append("<option value=\"").append(lf3.getLink())
									.append("\"  class=\"a_option\" id=\"")
									.append(lf3.getCode()).append("\">　　")
									.append(lf3.getName()).append("</option>");
						}
					}
				}
			} else {
				k++;
			}
		}
		return sb.toString();
	}
}
