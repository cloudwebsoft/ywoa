package com.redmoon.oa.fileark;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

import com.cloudwebsoft.framework.util.LogUtil;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.fileark.Attachment;
import com.redmoon.oa.fileark.DocPriv;
import com.redmoon.oa.fileark.Document;
import com.redmoon.oa.fileark.Leaf;
import com.redmoon.oa.fileark.LeafPriv;
import com.redmoon.oa.pvg.Privilege;

public class FileBakUp {

	/**
	 * 生成zip压缩文件供美编下载
	 * 
	 * @param qkid
	 * @throws SQLException
	 * @throws IOException
	 */
	/*
	 * public static String zipFiles(int qkid) throws SQLException, IOException{
	 * //获得期刊所有入选主编划版的稿件：路径和稿件名称 Vector<Vector<String>> files = new
	 * Vector<Vector<String>>(); String sql =
	 * "select name, fullpath from document_attach where doc_id in (select docId from "
	 * +ConstantDb.ft_NAME_GJGL+" where kdqs = "+qkid+" and sfzsly = "+
	 * StrUtil.sqlstr(ConstantDb.ZBHB_SFZSLY_Y)+")"; JdbcTemplate jt = new
	 * JdbcTemplate(); ResultIterator ri = jt.executeQuery(sql); ResultRecord rd
	 * = null; String pathtmp = ""; while(ri.hasNext()){ rd =
	 * (ResultRecord)ri.next(); String name = rd.getString(1); String fullpath =
	 * rd.getString(2);
	 * LogUtil.getLog(getClass()).info(FileBakUp.class+":::::::::::"+name+","+fullpath);
	 * Vector<String> item = new Vector<String>(); item.add(name);
	 * item.add(fullpath); files.add(item); if(pathtmp.equals("")){ pathtmp =
	 * fullpath.substring(0,fullpath.lastIndexOf("/"));
	 * LogUtil.getLog(getClass()).info(FileBakUp
	 * .class+":::::aaaaaa::::::"+name+","+fullpath+","+pathtmp); } }
	 * //压缩这些稿件并输出 String outputfile = ""; if(files.size()>0){ outputfile =
	 * pathtmp+"/美编下载.zip"; FileOutputStream f = new FileOutputStream(new
	 * File(outputfile)); CheckedOutputStream ch = new CheckedOutputStream(f,
	 * new CRC32()); ZipOutputStream zipOut = new ZipOutputStream( new
	 * BufferedOutputStream(ch)); Iterator<Vector<String>> ir =
	 * files.iterator(); while(ir.hasNext()){ Vector<String> item = ir.next();
	 * String name = item.get(0); String fullpath = item.get(1);
	 * doZipDir2(zipOut, fullpath, name); } zipOut.close(); f.close(); } return
	 * outputfile; }
	 */

	/**
	 * 打包下载一个文档目录中的所有文件
	 */
	public Map<String, DocZipHelp> getMaps(Map<String, DocZipHelp> map,
			String userName, String dirCode, String flag, String basePath)
			throws SQLException {
		//LogUtil.getLog(getClass()).info(getClass() + ":::::::::::::::::::::::" + flag + ","+ userName + "," + dirCode);
		// String basePath = "zip/";
		if (flag.equals("0")) {// 只打包本文件夹中的文件，不打包子文件夹中的文件
			// 添加文件夹本身
			Leaf leaf = new Leaf(dirCode);
			String dirName = leaf.getName();
			dirName = getDirName(dirName, map, basePath);
			DocZipHelp help = new DocZipHelp();
			help.setDirCode(dirCode);
			help.setDocId("");
			help.setAttPath("");
			dirName = getDirName(dirName, map, basePath);
			help.setFilename(dirName);
			help.setPath(basePath + "/" + dirName);
			help.setType(DocZipHelp.TYPE_DOC);
			map.put(basePath + "/" + dirName, help);
			//LogUtil.getLog(getClass()).info(FileBakUp.class + "::::::::::" + map.size()+ "," + dirName);
			// 添加文件夹中的内容
			map = getDocChildMaps(map, dirCode, userName, basePath + "/"
					+ dirName);
		} else {
			// 先把本文件夹及里面的文件放入
			Leaf leaf = new Leaf(dirCode);
			String dirName = leaf.getName();
			dirName = getDirName(dirName, map, basePath);
			DocZipHelp help = new DocZipHelp();
			help.setDirCode(dirCode);
			help.setDocId("");
			help.setAttPath("");
			dirName = getDirName(dirName, map, basePath);
			help.setFilename(dirName);
			help.setPath(basePath + "/" + dirName);
			help.setType(DocZipHelp.TYPE_DOC);
			map.put(basePath + "/" + dirName, help);
			map = getDocChildMaps(map, dirCode, userName, basePath + "/"
					+ dirName);
			// 获得所有子文件夹，判断权限，如果有下载权限，则建到map中去，如果没有下载权限则跳过
			Vector children = leaf.getChildren();
			if (children != null) {
				Iterator ir = children.iterator();
				while (ir.hasNext()) {
					leaf = (Leaf) ir.next();
					LeafPriv lp = new LeafPriv(leaf.getCode());
					if (lp.canUserDownLoad(userName)) {
						map = getMaps(map, userName, dirCode, flag, basePath
								+ "/" + dirName);
					}
				}
			}
		}

		return map;
	}

	/**
	 * 获得单一文件夹中的文档
	 * 
	 * @Description:
	 * @param map
	 * @param dirCode
	 * @return
	 * @throws SQLException
	 */
	public Map<String, DocZipHelp> getDocChildMaps(Map<String, DocZipHelp> map,
			String dirCode, String userName, String basePath)
			throws SQLException {
		// 判断文档权限，如果可下载，则进行打包，如果不可下载，则跳过
		//LogUtil.getLog(getClass()).info(getClass() + ":::::::::::::::::::::::::::::::::AB");
		DocPriv dp = new DocPriv();
		String sql = "select id from document where class1 = "
				+ StrUtil.sqlstr(dirCode) + " and examine<>"
				+ Document.EXAMINE_DUSTBIN;
		Document doc = new Document();
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = jt.executeQuery(sql);
		ResultRecord rd = null;
		DocZipHelp help = null;
		//LogUtil.getLog(getClass()).info(getClass() + ":::::" + ri.getRows());
		while (ri.hasNext()) {
			rd = (ResultRecord) ri.next();
			int docId = rd.getInt(1);
			dp.canUserDownload(userName, docId);
			doc = new Document();
			doc = doc.getDocument(docId);
			int type = doc.getType();
			if (type == 2) {// 直接上传的文当
				Vector vatts = doc.getAttachments(1);
				Attachment att = (Attachment) vatts.get(0);
				help = new DocZipHelp();
				help.setDirCode(dirCode);
				help.setDocId(docId + "");
				help.setAttPath(att.getFullPath());
				String fileName = att.getName();
				fileName = getFileName(fileName, map, basePath);
				help.setFilename(fileName);
				help.setPath(basePath + "/" + fileName);
				help.setType(DocZipHelp.TYPE_DOC);
				map.put(basePath + "/" + fileName, help);
			//	LogUtil.getLog(getClass()).info(getClass() + "::::::::::::::::AAAAAAAAAAA1"				+ fileName);
			} else if (type == 0) {// 老办法建立的文档
				Vector vatts = doc.getAttachments(1);
				if (vatts != null) {
					if (vatts.size() > 0) {// 有附件才建到map中去，没有附件则不建到map中去
						// 先把文档本身建到map中去
						help = new DocZipHelp();
						help.setDirCode(dirCode);
						help.setDocId(docId + "");
						help.setAttPath("");
						String dirName = doc.getTitle();
						dirName = getDirName(dirName, map, basePath);
						help.setFilename(dirName);
						help.setPath(basePath + "/" + dirName);
						help.setType(DocZipHelp.TYPE_DIR);
						map.put(basePath + "/" + dirName, help);
						//LogUtil.getLog(getClass()).info(getClass()		+ "::::::::::::::::AAAAAAAAAAA2" + dirName);
						// 把附件放到map中去
						map = getAttMapChild(map, vatts, dirCode, docId + "",
								basePath + "/" + dirName);
					}
				}
			}
		}
		//LogUtil.getLog(getClass()).info(getClass() + "::::" + map.size());
		return map;
	}

	private Map<String, DocZipHelp> getAttMapChild(Map<String, DocZipHelp> map,
			Vector atts, String dirCode, String docId, String basePath) {
		Attachment att = null;
		DocZipHelp help = null;
		Iterator ir = atts.iterator();
		while (ir.hasNext()) {
			att = (Attachment) ir.next();
			help = new DocZipHelp();
			help.setDirCode(dirCode);
			help.setDocId(docId + "");
			help.setAttPath(att.getFullPath());
			String fileName = att.getName();
			fileName = getFileName(fileName, map, basePath);
			help.setFilename(fileName);
			help.setPath(basePath + "/" + fileName);
			help.setType(DocZipHelp.TYPE_DOC);
			map.put(basePath + "/" + fileName, help);
		}
		return map;
	}

	private String getDirName(String dirName, Map<String, DocZipHelp> map,
			String basePath) {
		String path = basePath + "/" + dirName;
		DocZipHelp help = map.get(path);
		if (help == null) {// map中没有路径没有重复，可以使用
			return dirName;
		} else {
			int i = 1;
			boolean flag = true;
			while (flag) {
				dirName = dirName + "(" + i + ")";
				path = basePath + "/" + dirName;
				help = map.get(path);
				if (help == null) {
					flag = false;
				}
				i++;
			}
			return dirName;
		}
	}

	public String getFileName(String fileName, Map<String, DocZipHelp> map,
			String basePath) {
		String path = basePath + "/" + fileName;
		DocZipHelp help = map.get(path);
		if (help == null) {// map中没有路径没有重复，可以使用
			return fileName;
		} else {// 路径重复，文档要重命名
			String ext = getExt(fileName);
			String name = getName(fileName);
			int i = 1;
			boolean flag = true;
			while (flag) {
				fileName = name + "(" + i + ")." + ext;
				path = basePath + "/" + fileName;
				help = map.get(path);
				if (help == null) {
					flag = false;
				}
				i++;
			}
			return fileName;
		}
	}

	private String getExt(String fileName) {
		int point = fileName.lastIndexOf(".");
		String ext = fileName.substring(point + 1, fileName.length());
		return ext;
	}

	private String getName(String fileName) {
		int point = fileName.lastIndexOf(".");
		String name = fileName.substring(0, point);
		return name;
	}

	/**
	 * 打包下载一个文件夹中的所有文件，通过文档id，获得这个文档中的所有附件，生成打包文件
	 * 
	 * @Description:
	 * @param dirCode
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public static String zipDirFiles(String dirCode, String userName,
			String flag, String realPath) throws SQLException, IOException {
		String docName = "打包下载";
		FileBakUp bakUp = new FileBakUp();
		Map<String, DocZipHelp> map = new HashMap<String, DocZipHelp>();
		map = bakUp.getMaps(map, userName, dirCode, flag, realPath);
		// 压缩这些稿件并输出
		String outputfile = "";
		if (map != null && map.size() > 0) {
			outputfile = realPath + "/" + dirCode + "/" + dirCode + ".zip";
			File outPutFile = new File(outputfile);
			if (!outPutFile.exists()) {
				File parent = outPutFile.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				boolean res = outPutFile.createNewFile();
			}
			FileOutputStream f = new FileOutputStream(outPutFile);
			CheckedOutputStream ch = new CheckedOutputStream(f, new CRC32());
			ZipOutputStream zipOut = new ZipOutputStream(
					new BufferedOutputStream(ch));
			Set<String> keys = map.keySet();
			Iterator<String> ir = keys.iterator();
			while (ir.hasNext()) {
				String key = ir.next();
				DocZipHelp help = map.get(key);
				String type = help.getType();
				if (type.equals(DocZipHelp.TYPE_DIR)) {
					continue;
				}
				String name = help.getFilename();
				String fullpath2 = help.getPath();
				String fullpath = help.getAttPath();
				File tmpfile = new File(fullpath);
				if (!tmpfile.exists()) {
					continue;
				}
				tmpfile = new File(fullpath2);
				if (!tmpfile.exists()) {
					File parent = tmpfile.getParentFile();
					if (!parent.exists()) {
						parent.mkdirs();
					}
					parent.createNewFile();
				}
				FileUtil.CopyFile(fullpath, fullpath2);
				doZipDir2(zipOut, fullpath2, name, realPath);
				tmpfile = new File(fullpath2);
				if (tmpfile.exists()) {
					tmpfile.delete();
				}
			}
			zipOut.close();
			f.close();
		}

		return outputfile;
	}

	/**
	 * 打包下载多个文档中的所有文件，通过文档id，获得这个文档中的所有附件，生成打包文件
	 * 
	 * @Description:
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public static String zipDocsFiles(String ids, String realPath)
			throws SQLException, IOException, ErrMsgException {
		String[] aryIds = StrUtil.split(ids, ",");
		if (aryIds==null) {
			throw new ErrMsgException("请选择文件！");
		}
		
		String dirCode = "";
		Vector<Vector<String>> files = new Vector<Vector<String>>();
		
		for (int i=0; i<aryIds.length; i++) {
			int doc_id = StrUtil.toInt(aryIds[i]);
			Document doc = new Document();
			doc = doc.getDocument(doc_id);
			if ("".equals(dirCode)) {
				dirCode = doc.getDirCode();
			}
			// 获得文档中的所有附件：路径和文件名称
			String sql = "select name, visualpath,diskname from document_attach where doc_id = " + doc_id;
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(sql);
			ResultRecord rd = null;
			String pathtmp = "";
			int k = 0;
			while (ri.hasNext()) {
				rd = ri.next();
				String name = rd.getString(1);
				String fullpath = Global.getRealPath() + rd.getString(2) + "/" + rd.getString(3);
				String fullpath2 = realPath + "/" + dirCode + "/" + name;
				Vector<String> item = new Vector<String>();
				item.add(name);
				item.add(fullpath);
				item.add(fullpath2);
				item.add(k + "");
				files.add(item);
				if (pathtmp.equals("")) {
					pathtmp = fullpath.substring(0, fullpath.lastIndexOf("/"));
				}
			}			
		}

		// 获得这些文件，拷贝到zip临时文件夹下(虚拟拷贝，只生成路径，在压缩时先拷贝在压缩，压缩完成以后删除)
		if (files.size() > 0) {
			String directoryTmp = realPath + "/" + dirCode;
			File rootDirectory = new File(directoryTmp);
			// 因为路径为doc_id,不可能重复
			if (!rootDirectory.exists()) {// 不存在同名文件夹，新建这个文件夹作为打包文件的临时根文件夹
				rootDirectory.mkdirs();
			}

			// 获取所有文件名，如果有相同，第二个要改名,改名规则，在原名称后加(i),若有多个同名则i自增
			Map<String, String> map = new HashMap<String, String>();
			Vector<Vector<String>> filesTmp = new Vector<Vector<String>>();
			filesTmp.addAll(files);
			boolean flag = false;
			Iterator<Vector<String>> ir = filesTmp.iterator();
			int k = 0;
			while (ir.hasNext()) {
				Vector<String> item = ir.next();
				String name = item.get(0);
				String tmpName = name;
				String fullpath = item.get(1);
				flag = true;
				int i = 1;
				String perName = name;
				tmpName = map.get(perName);
				while (flag) {
					if (tmpName == null) {
						map.put(perName, perName);
						flag = false;
					} else {
						int point = perName.lastIndexOf(".");
						String sname = perName.substring(0, point);
						String ext = perName.substring(point, perName.length());
						tmpName = sname + "(" + i + ")" + ext;
						perName = tmpName;
						tmpName = map.get(perName);
					}
					if (flag) {
						i++;
					}
				}
				if (i > 1) {
					String itemflag = item.get(3);
					files.remove(item);
					item = new Vector<String>();
					item.add(perName);
					item.add(fullpath);
					item.add(realPath + "/" + dirCode + "/" + perName);
					item.add(itemflag);
					files.add(item);
				}
				k++;
			}
		}
		
		String outputfile = "";
		// 压缩并输出
		if (files.size() > 0) {
			outputfile = realPath + "/" + dirCode + "/" + dirCode + ".zip";
			FileOutputStream f = new FileOutputStream(new File(outputfile));
			CheckedOutputStream ch = new CheckedOutputStream(f, new CRC32());
			ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(ch));			
			Iterator<Vector<String>> ir = files.iterator();
			while (ir.hasNext()) {
				Vector<String> item = ir.next();
				String name = item.get(0);
				String fullpath = item.get(1);
				String fullpath2 = item.get(2);
				File tmpfile = new File(fullpath);
				if (!tmpfile.exists()) {
					continue;
				}
				FileUtil.CopyFile(fullpath, fullpath2);
				doZipDir3(zipOut, fullpath2, name, realPath);
				tmpfile = new File(fullpath2);
				if (tmpfile.exists()) {
					tmpfile.delete();
				}
			}
			zipOut.close();
			f.close();
		}
		return outputfile;
	}	

	/**
	 * 打包下载一个文档中的所有文件，通过文档id，获得这个文档中的所有附件，生成打包文件
	 * 
	 * @Description:
	 * @param doc_id
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public static String zipDocFiles(int doc_id, String realPath)
			throws SQLException, IOException {
		File zipfile = new File(realPath + "/" + doc_id + "/" + doc_id + ".zip");
		if (zipfile.exists()) {
			zipfile.delete();
		}
		
		Document doc = new Document();
		doc = doc.getDocument(doc_id);
		// 获得文档中的所有附件：路径和文件名称
		Vector<Vector<String>> files = new Vector<Vector<String>>();
		String sql = "select name, visualpath, diskname from document_attach where doc_id = " + doc_id;
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = jt.executeQuery(sql);
		ResultRecord rd = null;
		String pathtmp = "";
		int k = 0;
		while (ri.hasNext()) {
			rd = ri.next();
			String name = rd.getString(1);
			String fullpath = Global.getRealPath() + rd.getString(2) + "/" + rd.getString(3);
			String fullpath2 = realPath + "/" + doc_id + "/" + name;
			Vector<String> item = new Vector<String>();
			item.add(name);
			item.add(fullpath);
			item.add(fullpath2);
			item.add(k + "");
			files.add(item);
			if (pathtmp.equals("")) {
				pathtmp = fullpath.substring(0, fullpath.lastIndexOf("/"));
			}
		}

		// 获得这些文件，拷贝到zip临时文件夹下(虚拟拷贝，只生成路径，在压缩时先拷贝在压缩，压缩完成以后删除)
		if (files.size() > 0) {
			String directoryTmp = realPath + "/" + doc_id;
			File rootDirectory = new File(directoryTmp);
			// 因为路径为doc_id,不可能重复
			if (!rootDirectory.exists()) {// 不存在同名文件夹，新建这个文件夹作为打包文件的临时根文件夹
				rootDirectory.mkdirs();
			}

			// 获取所有文件名，如果有相同，第二个要改名,改名规则，在原名称后加(i),若有多个同名则i自增
			Map<String, String> map = new HashMap<String, String>();
			Vector<Vector<String>> filesTmp = new Vector<Vector<String>>();
			filesTmp.addAll(files);
			boolean flag = false;
			Iterator<Vector<String>> ir = filesTmp.iterator();
			k = 0;
			while (ir.hasNext()) {
				Vector<String> item = ir.next();
				String name = item.get(0);
				String tmpName = name;
				String fullpath = item.get(1);
				flag = true;
				int i = 1;
				String perName = name;
				tmpName = map.get(perName);
				while (flag) {
					if (tmpName == null) {
						map.put(perName, perName);
						flag = false;
					} else {
						int point = perName.lastIndexOf(".");
						String sname = perName.substring(0, point);
						String ext = perName.substring(point, perName.length());
						tmpName = sname + "(" + i + ")" + ext;
						perName = tmpName;
						tmpName = map.get(perName);
					}
					if (flag) {
						i++;
					}
				}
				if (i > 1) {
					String itemflag = item.get(3);
					files.remove(item);
					item = new Vector<String>();
					item.add(perName);
					item.add(fullpath);
					item.add(realPath + "/" + doc_id + "/" + perName);
					item.add(itemflag);
					files.add(item);
				}
				k++;
			}
		}

		// 压缩并输出
		String outputfile = "";
		if (files.size() > 0) {
			outputfile = realPath + "/" + doc_id + "/" + doc_id + ".zip";
			FileOutputStream f = new FileOutputStream(new File(outputfile));
			CheckedOutputStream ch = new CheckedOutputStream(f, new CRC32());
			ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(ch));
			Iterator<Vector<String>> ir = files.iterator();
			while (ir.hasNext()) {
				Vector<String> item = ir.next();
				String name = item.get(0);
				String fullpath = item.get(1);
				String fullpath2 = item.get(2);
				File tmpfile = new File(fullpath);
				if (!tmpfile.exists()) {
					continue;
				}
				FileUtil.CopyFile(fullpath, fullpath2);
				doZipDir3(zipOut, fullpath2, name, realPath);
				tmpfile = new File(fullpath2);
				if (tmpfile.exists()) {
					tmpfile.delete();
				}
			}
			zipOut.close();
			f.close();
		}
		return outputfile;
	}

	/**
	 * 测试方法
	 * 
	 * @Description:
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public static void doziptest() throws IOException, IllegalArgumentException {
		FileOutputStream f = new FileOutputStream(new File("e:/aa.zip"));
		CheckedOutputStream ch = new CheckedOutputStream(f, new CRC32());
		ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream( ch));
		/*
		 * doZipDir2(zipOut,
		 * "D:/prj/ymxm/zzwhoa/WebRoot/upfile/file_flow/2015/8/14392958442511045830219.docx"
		 * ,"测试稿件1.docx"); doZipDir2(zipOut,
		 * "D:/prj/ymxm/zzwhoa/WebRoot/upfile/file_flow/2015/8/14392958804781684467511.docx"
		 * ,"测试稿件2.docx"); doZipDir2(zipOut,
		 * "D:/prj/ymxm/zzwhoa/WebRoot/upfile/file_flow/2015/8/14392959122231043188131.docx"
		 * ,"测试稿件3.docx"); doZipDir2(zipOut,
		 * "D:/prj/ymxm/zzwhoa/WebRoot/upfile/file_flow/2015/8/14392959659651258350221.docx"
		 * ,"测试稿件4.docx"); doZipDir2(zipOut,
		 * "D:/prj/ymxm/zzwhoa/WebRoot/upfile/file_flow/2015/8/14392960063491295482512.docx"
		 * ,"测试稿件5.docx");
		 */
		//doZipDir2(zipOut, "D:/aa/aaa.txt", "aaa.txt");
		//doZipDir2(zipOut, "D:/bb/bbb.txt", "测试稿件3.txt");
		doZipDir3(zipOut, "D:\\home8.0\\FileUploadTmp\\zip\\16786034226881794934", "", "");
		zipOut.close();
		f.close();
	}

	public static void main(String[] args) {
		try {
			FileBakUp.doziptest();
		} catch (IllegalArgumentException e) {
			LogUtil.getLog(FileBakUp.class).error(e);
		} catch (IOException e) {
			LogUtil.getLog(FileBakUp.class).error(e);
		}
	}

	public static void zipDirectory(String dir, String zipfile)
			throws IOException, IllegalArgumentException {
		//LogUtil.getLog(getClass()).info(FileBakUp.class + "aaaaaaaaaaaaaaaaaa");
		FileOutputStream f = new FileOutputStream(new File(zipfile));
		CheckedOutputStream ch = new CheckedOutputStream(f, new CRC32());
		ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(
				ch));
		doZipDir(zipOut, dir);
		zipOut.close();
		f.close();
	}
	
	
	/**
	 * 打包文件
	 * 
	 * @Description:
	 * @param out
	 * @param path
	 * @param fileName
	 * @throws IOException
	 */
	public static void doZipDir3(ZipOutputStream out, String path,
			String fileName,String basePath) throws IOException {
		File file = new File(path);
		if (!(file.isDirectory())) {
			FileInputStream in = new FileInputStream(path);
			String fpath = file.getPath();
			int point = fpath.lastIndexOf(File.separator);
			fpath = fpath.substring(point+1,fpath.length());
			ZipEntry entry = new ZipEntry(fpath);
			out.putNextEntry(entry);
			int nNumber;
			while ((nNumber = in.read()) != -1) {
				out.write(nNumber);
			}
			out.setEncoding("gbk");
			in.close();
		} else {
			/*
			 * FileInputStream in = new FileInputStream(path); ZipEntry entry =
			 * new ZipEntry(file.getName()); out.putNextEntry(entry); int
			 * nNumber; while ((nNumber = in.read()) != -1) {
			 * out.write(nNumber); } in.close();
			 */
			String[] entries = file.list();
			String pathTemp = file.getPath();
			for (int i = 0; i < entries.length; ++i) {
				doZipDir(out, pathTemp + "/" + entries[i]);
			}
		}
	}

	/**
	 * 进行打包文件的方法
	 * 
	 * @Description:
	 * @param out
	 * @param path
	 * @param fileName
	 * @throws IOException
	 */
	public static void doZipDir2(ZipOutputStream out, String path,
			String fileName,String basePath) throws IOException {
		//LogUtil.getLog(getClass()).info(FileBakUp.class			+ ":::::::path===================================" + path);
		File file = new File(path);
		//LogUtil.getLog(getClass()).info(FileBakUp.class + ":1:" + file.getName());
		//LogUtil.getLog(getClass()).info(FileBakUp.class + ":2:" + file.getParent());
		if (!(file.isDirectory())) {
			// BufferedReader in = new BufferedReader(new InputStreamReader(new
			// FileInputStream(path)));
			String parent = file.getParent();
			FileInputStream in = new FileInputStream(path);
			//LogUtil.getLog(getClass()).info(FileBakUp.class + "::::::::::::::bakupPath="	+ parent.substring(parent.lastIndexOf("\\") + 1) + "\\"				+ fileName);
			String fpath = file.getPath();
			int length = basePath.length();
			//LogUtil.getLog(getClass()).info(FileBakUp.class+":::::::::::basePath="+basePath);
			//LogUtil.getLog(getClass()).info(FileBakUp.class+":::::::::::fpath1="+fpath);
			//fpath = fpath.replace(basePath, "");
			fpath = fpath.substring(length+1,fpath.length());
			//LogUtil.getLog(getClass()).info(FileBakUp.class+":::::::::::fpath2="+fpath);
			//fpath.substring(beginIndex)
			// ZipEntry entry = new
			// ZipEntry(parent.substring(parent.lastIndexOf("\\")+1)+"\\"+fileName);
			ZipEntry entry = new ZipEntry(fpath);
			out.putNextEntry(entry);
			int nNumber;
			while ((nNumber = in.read()) != -1) {
				out.write(nNumber);
			}
			out.setEncoding("gbk");
			in.close();
			return;
		} else {
			/*
			 * FileInputStream in = new FileInputStream(path); ZipEntry entry =
			 * new ZipEntry(file.getName()); out.putNextEntry(entry); int
			 * nNumber; while ((nNumber = in.read()) != -1) {
			 * out.write(nNumber); } in.close();
			 */

			String[] entries = file.list();
			for (int i = 0; i < entries.length; i++) {
				//LogUtil.getLog(getClass()).info(FileBakUp.class + ":::" + entries[i]);

			}
			String pathTemp = file.getPath();
			//LogUtil.getLog(getClass()).info(FileBakUp.class + "::::" + entries.length);
			for (int i = 0; i < entries.length; ++i)
				doZipDir(out, pathTemp + "/" + entries[i]);
		}
	}

	/**
	 * 进行打包文件的方法
	 * 
	 * @Description:
	 * @param out
	 * @param path
	 * @throws IOException
	 */
	public static void doZipDir(ZipOutputStream out, String path)
			throws IOException {
		File file = new File(path);
		if (!(file.isDirectory())) {
			String parent = file.getParent();
			FileInputStream in = new FileInputStream(path);
			ZipEntry entry = new ZipEntry(parent.substring(parent
					.lastIndexOf(File.separator) + 1)
					+ File.separator + file.getName());
			out.putNextEntry(entry);
			int nNumber;
			while ((nNumber = in.read()) != -1) {
				out.write(nNumber);
			}
			in.close();
		} else {
			String[] entries = file.list();
			String pathTemp = file.getPath();
			//LogUtil.getLog(getClass()).info(FileBakUp.class + "::::" + entries.length);
			for (int i = 0; i < entries.length; ++i) {
				doZipDir(out, pathTemp + "/" + entries[i]);
			}
		}
	}

	public void dozip(String srcfile, String zipfile) throws IOException {
		File src = new File(srcfile);
		if (!src.exists()) {
			return;
		}

		FileInputStream in = new FileInputStream(src);
		FileOutputStream f = new FileOutputStream(new File(zipfile));
		CheckedOutputStream ch = new CheckedOutputStream(f, new CRC32());
		ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(ch));
		ZipEntry entry = new ZipEntry(src.getName());
		zipOut.putNextEntry(entry);
		int nNumber;
		while ((nNumber = in.read()) != -1) {
			zipOut.write(nNumber);
		}
		zipOut.close();
		in.close();
		return;
	}
	
	public void deleteDirs(String path){
		File file = new File(path);
		if(file.exists()){
			if(file.isFile()){
				file.delete();
			}else if(file.isDirectory()){
				File[] files = file.listFiles();
				for(File tmp :files){
					deleteDirs(tmp.getPath());
				}
			}
		}
	}
	
	
}
