package com.redmoon.oa.netdisk;

import cn.js.fan.db.Conn;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileInfo;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import java.io.File;
import java.util.Iterator;
import java.util.Vector;
import javax.servlet.ServletContext;

import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.person.UserDb;
import java.sql.Timestamp;

public class DocContent implements java.io.Serializable {
	public static final String IMGKIND = "netdisk_document";

	String content = " ";
	int docId;
	int pageNum = 1;

	String connname = "";

	private static final String INSERT = "INSERT into netdisk_doc_content (doc_id, content, page_num) VALUES (?,?,?)";
	private static final String LOAD = "SELECT content from netdisk_doc_content WHERE doc_id=? and page_num=?";
	private static final String DEL = "DELETE FROM netdisk_doc_content WHERE doc_id=? and page_num=?";
	private static final String SAVE = "UPDATE netdisk_doc_content SET content=? WHERE doc_id=? and page_num=?";

	private static final String LOAD_DOCUMENT_ATTACHMENTS = "SELECT id FROM netdisk_document_attach WHERE doc_id=? and page_num=? and is_current<>0 and is_deleted=0 order by orders";

	public DocContent() {
		connname = Global.getDefaultDB();
		if (connname.equals(""))
			LogUtil.getLog(getClass()).info("DocContent:默认数据库名为空！");
	}

	public DocContent(int doc_id, int page_num) {
		this.docId = doc_id;
		this.pageNum = page_num;

		connname = Global.getDefaultDB();
		if (connname.equals(""))
			LogUtil.getLog(getClass()).info("DocContent:默认数据库名为空！");
		loadFromDb(doc_id, page_num);
	}

	public boolean createWithoutFile(ServletContext application,
			CMSMultiFileUploadBean mfu, int doc_id, String content, int pageNum)
			throws ErrMsgException {
		String FilePath = StrUtil.getNullString(mfu.getFieldValue("filepath"));
		// 因为mysql JDBC的原因，使得使用下句后路径会变为d:oaupfile/...
		// String tempAttachFilePath = application.getRealPath("/") + FilePath +
		// "/";
		String tempAttachFilePath = Global.getRealPath() + FilePath + "/";

		mfu.setSavePath(tempAttachFilePath); // 取得目录
		File f = new File(tempAttachFilePath);
		if (!f.isDirectory()) {
			f.mkdirs();
		}

		this.docId = doc_id;
		this.content = content;
		this.pageNum = pageNum;

		Conn conn = new Conn(connname);
		boolean re = true;
		try {
			// 处理附件
			int ret = mfu.getRet();
			String sisDdxc = StrUtil.getNullString(mfu.getFieldValue("isDdxc"));
			// 断点续传
			if (sisDdxc.equals("true")) {
				String[] attachFileNames = mfu.getFieldValues("attachFileName");
				String[] clientFilePaths = mfu.getFieldValues("clientFilePath");

				int len = 0;
				if (attachFileNames != null)
					len = attachFileNames.length;
				String sql = "";
				int orders = 1;
				for (int i = 0; i < len; i++) {
					String filepath = mfu.getSavePath() + attachFileNames[i];
					String name = CMSMultiFileUploadBean
							.getUploadFileName(clientFilePaths[i]);

					sql = "insert into netdisk_document_attach (fullpath,doc_id,name,diskname,visualpath,page_num,orders,uploadDate,id) values (?,?,?,?,?,?,?,?,?)";
					PreparedStatement ps = conn.prepareStatement(sql);
					ps.setString(1, filepath);
					ps.setInt(2, docId);
					ps.setString(3, name);
					ps.setString(4, attachFileNames[i]);
					ps.setString(5, FilePath);
					ps.setInt(6, pageNum);
					ps.setInt(7, orders);
					ps.setTimestamp(8, new Timestamp(new java.util.Date()
							.getTime()));
					long id = SequenceManager
							.nextID(SequenceManager.OA_DOCUMENT_NETDISK_ATTACHMENT);
					ps.setLong(9, id);
					conn.executePreUpdate();
					if (ps != null) {
						ps.close();
						ps = null;
					}
					orders++;
				}
			} else {
				if (ret == CMSMultiFileUploadBean.RET_SUCCESS) {
					String filepath = "";
					String sql = "";
					// 处理附件
					mfu.writeAttachment(true); // 用随机名称命名文件
					Vector attachs = mfu.getAttachments();
					Iterator ir = attachs.iterator();
					sql = "";
					while (ir.hasNext()) {
						FileInfo fi = (FileInfo) ir.next();
						filepath = mfu.getSavePath() + fi.getDiskName();

						sql = "insert into netdisk_document_attach (fullpath,doc_id,name,diskname,visualpath,page_num,file_size,ext,uploadDate,id) values (?,?,?,?,?,?,?,?,?,?)";
						PreparedStatement ps = conn.prepareStatement(sql);
						ps.setString(1, filepath);
						ps.setInt(2, docId);
						ps.setString(3, fi.getName());
						ps.setString(4, fi.getDiskName());
						ps.setString(5, FilePath);
						ps.setInt(6, pageNum);
						ps.setLong(7, fi.getSize());
						ps.setString(8, fi.getExt());
						ps.setTimestamp(9, new Timestamp(new java.util.Date()
								.getTime()));

						long id = SequenceManager
								.nextID(SequenceManager.OA_DOCUMENT_NETDISK_ATTACHMENT);
						ps.setLong(10, id);
						conn.executePreUpdate();
						if (ps != null) {
							ps.close();
							ps = null;
						}
					}
				} else
					throw new ErrMsgException("上传失败！ret=" + ret);
			}
			// 如果页面是从中间某页后插入，而不是被加至末尾
			Document doc = new Document();
			doc = doc.getDocument(docId);
			PreparedStatement pstmt = null;
			// pageNum=1的时候，是插入新文章，而不是从中间某页后插入新页
			if (pageNum <= doc.getPageCount() && pageNum != 1) {
				// logger.info("pageNum=" + pageNum);
				// 更新其后的页面的页码
				String sql = "update netdisk_doc_content set page_num=page_num+1 where doc_id=? and page_num>=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, docId);
				pstmt.setInt(2, pageNum);
				conn.executePreUpdate();
				pstmt.close();
				pstmt = null;

				sql = "update cms_images set subkey=subkey+1 where mainkey=? and subkey>=? and kind='"
						+ IMGKIND + "'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, docId);
				pstmt.setInt(2, pageNum);
				conn.executePreUpdate();
				pstmt.close();
				pstmt = null;

				sql = "update netdisk_document_attach set page_num=page_num+1 where doc_id=? and page_num>=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, docId);
				pstmt.setInt(2, pageNum);
				conn.executePreUpdate();
				pstmt.close();
				pstmt = null;
			}

			// 更新缓存
			DocContentCacheMgr dcm = new DocContentCacheMgr();
			for (int i = pageNum; i < doc.getPageCount(); i++)
				dcm.refreshUpdate(docId, pageNum);

			pstmt = conn.prepareStatement(INSERT);
			pstmt.setInt(1, doc_id);
			pstmt.setString(2, content);
			pstmt.setInt(3, pageNum);
			re = conn.executePreUpdate() == 1 ? true : false;

			if (re) {
				// 更新缓存
				dcm.refreshCreate(docId);
			}

			// 更新文章的总页数
			if (pageNum != 1)
				doc.UpdatePageCount(doc.getPageCount() + 1);
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}

		return re;
	}

	public boolean create(int doc_id, String content) {
		this.docId = doc_id;
		this.content = content;
		this.pageNum = 1;

		if (content.equals(""))
			content = " ";

		Conn conn = new Conn(connname);
		boolean re = false;
		try {
			PreparedStatement pstmt = conn.prepareStatement(INSERT);
			pstmt.setInt(1, doc_id);
			pstmt.setString(2, content);
			pstmt.setInt(3, pageNum);
			re = conn.executePreUpdate() == 1 ? true : false;

			if (re) {
				// 更新缓存
				DocContentCacheMgr dcm = new DocContentCacheMgr();
				dcm.refreshCreate(docId);
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return re;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public DocContent getDocContent(int doc_id, int page_num) {
		DocContentCacheMgr dccm = new DocContentCacheMgr();
		return dccm.getDocContent(doc_id, page_num);
	}

	public void loadFromDb(int doc_id, int page_num) {
		Conn conn = new Conn(connname);
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(LOAD);
			this.docId = doc_id;
			this.pageNum = page_num;
			pstmt.setInt(1, doc_id);
			pstmt.setInt(2, page_num);
			rs = conn.executePreQuery();
			if (rs != null) {
				if (rs.next()) {
					content = rs.getString(1);
				}
			}
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}

			// 取得附件
			pstmt = conn.prepareStatement(LOAD_DOCUMENT_ATTACHMENTS);
			// logger.info(LOAD_DOCUMENT_ATTACHMENTS);
			pstmt.setInt(1, docId);
			pstmt.setInt(2, pageNum);
			rs = conn.executePreQuery();
			if (rs != null) {
				while (rs.next()) {
					int aid = rs.getInt(1);
					Attachment am = new Attachment(aid);
					attachments.addElement(am);
				}
			}
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error("loadFromDb: " + e.getMessage());
		} finally {
			/*
			 * if (pstmt != null) { try { pstmt.close(); } catch (Exception e)
			 * {} pstmt = null; }
			 */
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
	}

	public void delDocContentOfDocument(int docId) throws ErrMsgException {
		Conn conn = new Conn(connname);
		ResultSet rs = null;
		try {
			// 删除文章中的页
			String sql = "select page_num from netdisk_doc_content where doc_id="
					+ docId;
			// logger.info("del:sql=" + sql);
			rs = conn.executeQuery(sql);
			if (rs != null) {
				while (rs.next()) {
					int pn = rs.getInt(1);
					DocContent dc = getDocContent(docId, pn);
					dc.del();
				}
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(
					"delDocContentOfDocument:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
	}

	public boolean del() throws ErrMsgException {
		Conn conn = new Conn(connname);
		boolean re = false;
		ResultSet rs = null;
		try {
			// 从磁盘删除图像文件
			String sql = "select path from cms_images where mainkey=" + docId
					+ " and kind='" + IMGKIND + "' and subkey=" + pageNum;
			rs = conn.executeQuery(sql);
			if (rs != null) {
				String fpath = "";
				while (rs.next()) {
					fpath = rs.getString(1);
					if (fpath != null) {
						File virtualFile = new File(Global.getRealPath()
								+ fpath);
						virtualFile.delete();
					}
				}
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
			// 从数据库中删除图像文件
			sql = "delete from cms_images where mainkey=" + docId
					+ " and kind='" + IMGKIND + "' and subkey=" + pageNum;
			conn.executeUpdate(sql);

			// 从磁盘删除附件
			Iterator ir = attachments.iterator();
			while (ir.hasNext()) {
				Attachment att = (Attachment) ir.next();
				LogUtil.getLog(getClass()).info(
						"del: attach id=" + att.getId() + " att name="
								+ att.getName());
				att.del();
			}
			/*
			 * sql =
			 * "select fullpath from netdisk_document_attach where doc_id=" +
			 * docId + " and page_num=" + pageNum; rs = conn.executeQuery(sql);
			 * if (rs != null) { String fpath = ""; while (rs.next()) { fpath =
			 * rs.getString(1); if (fpath != null) { File virtualFile = new
			 * File(fpath); virtualFile.delete(); } } } if (rs != null) {
			 * rs.close(); rs = null; } // 从数据库中删除附件 sql =
			 * "delete from netdisk_document_attach where doc_id=" + docId +
			 * " and page_num=" + pageNum; conn.executeUpdate(sql);
			 */
			// 从数据库中删除
			PreparedStatement pstmt = conn.prepareStatement(DEL);
			pstmt.setInt(1, docId);
			pstmt.setInt(2, pageNum);
			re = conn.executePreUpdate() == 1 ? true : false;
			pstmt.close();
			pstmt = null;

			// 如果该页是最后一页，则不用更新
			Document doc = new Document();
			doc = doc.getDocument(docId);
			if (pageNum != doc.getPageCount()) {
				// 更新其后的页面的页码
				sql = "update netdisk_doc_content set page_num=page_num-1 where doc_id=? and page_num>?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, docId);
				pstmt.setInt(2, pageNum);
				conn.executePreUpdate();
				pstmt.close();
				pstmt = null;

				sql = "update cms_images set subkey=subkey-1 where mainkey=? and subkey>? and kind='"
						+ IMGKIND + "'";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, docId);
				pstmt.setInt(2, pageNum);
				conn.executePreUpdate();
				pstmt.close();
				pstmt = null;

				sql = "update netdisk_document_attach set page_num=page_num-1 where doc_id=? and page_num>?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, docId);
				pstmt.setInt(2, pageNum);
				conn.executePreUpdate();
				pstmt.close();
				pstmt = null;
			}
			// 更新文章的总页数
			if (doc.getPageCount() > 1)
				doc.UpdatePageCount(doc.getPageCount() - 1);

			re = true;
			// 更新缓存
			DocContentCacheMgr dcm = new DocContentCacheMgr();
			dcm.refreshDel(docId, pageNum);

			for (int i = pageNum + 1; i < doc.getPageCount(); i++)
				dcm.refreshUpdate(docId, pageNum);

		} catch (SQLException e) {
			re = false;
			LogUtil.getLog(getClass()).error(e.getMessage());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				rs = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return re;
	}

	public synchronized boolean save(ServletContext application,
			CMSMultiFileUploadBean mfu) throws ErrMsgException {
		String isuploadfile = StrUtil.getNullString(mfu
				.getFieldValue("isuploadfile"));
		// logger.info("filepath=" + mfu.getFieldValue("filepath"));
		if (isuploadfile.equals("false"))
			return saveWithoutFile(application, mfu);
		String FilePath = StrUtil.getNullString(mfu.getFieldValue("filepath"));
		// String tempAttachFilePath = application.getRealPath("/") + FilePath +
		// "/";

		// 此处的tempAttachFilePath似乎无用，而且反会可能导致创建错误的文件夹
		/*
		 * String tempAttachFilePath = Global.getRealPath() + FilePath + "/";
		 * mfu.setSavePath(tempAttachFilePath); //取得目录
		 * 
		 * File f = new File(tempAttachFilePath); if (!f.isDirectory()) {
		 * f.mkdirs(); }
		 */

		ResultSet rs = null;
		Conn conn = new Conn(connname);
		try {
			// 删除图像文件
			String sql = "select path from cms_images where mainkey=" + docId
					+ " and kind='" + IMGKIND + "' and subkey=" + pageNum + "";

			rs = conn.executeQuery(sql);
			if (rs != null) {
				String fpath = "";
				while (rs.next()) {
					fpath = rs.getString(1);
					if (fpath != null) {
						File virtualFile = new File(Global.getRealPath()
								+ fpath);
						virtualFile.delete();
					}
				}
			}

			if (rs != null) {
				rs.close();
				rs = null;
			}
			// 从数据库中删除图像
			sql = "delete from cms_images where mainkey=" + docId
					+ " and kind='" + IMGKIND + "' and subkey=" + pageNum + "";
			conn.executeUpdate(sql);

			// 处理图片
			int ret = mfu.getRet();
			if (ret == 1) {
				mfu.writeFile(false);
				Vector files = mfu.getFiles();
				// logger.info("files size=" + files.size());
				java.util.Enumeration e = files.elements();
				String filepath = "";
				sql = "";
				while (e.hasMoreElements()) {
					FileInfo fi = (FileInfo) e.nextElement();
					filepath = FilePath + "/" + fi.getName();
					sql = "insert into cms_images (path,mainkey,kind,subkey) values ("
							+ StrUtil.sqlstr(filepath)
							+ ","
							+ docId
							+ ",'"
							+ IMGKIND + "'," + pageNum + ")";
					conn.executeUpdate(sql);
				}
			} else
				throw new ErrMsgException("save:上传失败！ret=" + ret);
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error("save:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}

		saveWithoutFile(application, mfu);
		return true;
	}

	/**
	 * 保存附件
	 * 
	 * @param application
	 *            ServletContext
	 * @param mfu
	 *            CMSMultiFileUploadBean
	 * @return boolean
	 * @throws ErrMsgException
	 */
	public synchronized boolean saveWithoutFile(ServletContext application,
			CMSMultiFileUploadBean mfu) throws ErrMsgException {
		content = StrUtil.getNullString(mfu.getFieldValue("htmlcode"));
		if (content.equals(""))
			content = " "; // 兼容sqlserver 2000 text字段
		Document doc = new Document();
		doc = doc.getDocument(docId);
		Leaf lf = new Leaf();
		lf = lf.getLeaf(doc.getDirCode());
		String userName = lf.getRootCode();

		Conn conn = new Conn(connname);
		boolean re = true;
		try {
			String visualPath = StrUtil.getNullString(mfu
					.getFieldValue("filepath"));

			com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
			String FilePath = cfg.get("file_netdisk") + "/" + visualPath;

			// 处理附件
			// String tempAttachFilePath = application.getRealPath("/") +
			// FilePath +
			// "/";
			String tempAttachFilePath = Global.getRealPath() + FilePath + "/";

			mfu.setSavePath(tempAttachFilePath); // 取得目录

			LogUtil.getLog(getClass()).info(
					"tempAttachFilePath=" + tempAttachFilePath);
			// System.out.println(getClass() + " tempAttachFilePath=" +
			// tempAttachFilePath);

			long allSize = 0;

			String sisDdxc = StrUtil.getNullString(mfu.getFieldValue("isDdxc"));
			if (sisDdxc.equals("true")) {

				visualPath = visualPath.substring(cfg.get("file_netdisk")
						.length() + 1);

				String[] attachFileNames = mfu.getFieldValues("attachFileName");
				String[] clientFilePaths = mfu.getFieldValues("clientFilePath");

				int len = 0;
				if (attachFileNames != null)
					len = attachFileNames.length;
				String sql = "";
				int orders = getAttachmentMaxOrders() + 1;
				for (int i = 0; i < len; i++) {
					String name = CMSMultiFileUploadBean
							.getUploadFileName(clientFilePaths[i]);

					sql = "insert into netdisk_document_attach (doc_id,name,diskname,visualpath,page_num,orders,uploadDate,user_name) values (?,?,?,?,?,?,?,?)";
					PreparedStatement ps = conn.prepareStatement(sql);
					ps.setInt(1, docId);
					ps.setString(2, name);
					ps.setString(3, name); // attachFileNames[i]);
					ps.setString(4, visualPath);
					ps.setInt(5, pageNum);
					ps.setInt(6, orders);
					ps.setTimestamp(7, new Timestamp(new java.util.Date()
							.getTime()));
					ps.setString(8, userName);
					conn.executePreUpdate();
					if (ps != null) {
						ps.close();
						ps = null;
					}

					orders++;
				}
			} else {
				File f = new File(tempAttachFilePath);
				if (!f.isDirectory()) {
					f.mkdirs();
				}
				// 检查上传的文件大小有没有超出磁盘空间
				Vector attachs = mfu.getFiles();
				Iterator attir = attachs.iterator();
				while (attir.hasNext()) {
					FileInfo fi = (FileInfo) attir.next();
					allSize += fi.getSize();
				}
				UserDb ud = new UserDb();
				ud = ud.getUserDb(doc.getNick());
				if (ud.getDiskSpaceUsed() + allSize > ud.getDiskSpaceAllowed())
					throw new ErrMsgException("您上传的文件太大了，超出了允许的磁盘空间！");

				LogUtil.getLog(getClass()).info(
						"ud.getDiskSpaceUsed()=" + ud.getDiskSpaceUsed()
								+ " allSize=" + allSize
								+ " ud.getDiskSpaceAllowed()="
								+ ud.getDiskSpaceAllowed());

				// 检查是不是按目录上传
				String upDirCode = "";
				String uploadDirName = StrUtil.getNullStr(mfu
						.getFieldValue("uploadDirName"));
				if (!uploadDirName.equals("")) {
					// 取得uploadDirName中的目录名称，检测是否已存在，如不存在，则创建该目录
					int index = uploadDirName.lastIndexOf("\\");
					String upDirName = uploadDirName.substring(index + 1);
					// 检查是否存在同名目录
					boolean isExist = false;
					Iterator irlf = lf.getChildren().iterator();
					while (irlf.hasNext()) {
						Leaf child = (Leaf) irlf.next();
						if (child.getName().equals(upDirName)) {
							upDirCode = child.getCode();
							isExist = true;
							break;
						}
					}
					// 如果不存在同名目录，则创建
					if (!isExist) {
						// 创建uploadDirName节点及物理目录
						Leaf lfCh = new Leaf();
						lfCh.setName(upDirName);
						upDirCode = Leaf.getAutoCode();
						lfCh.setCode(upDirCode);
						lfCh.setParentCode(doc.getDirCode());
						lfCh.setDescription(doc.getDirCode());
						lfCh.setType(Leaf.TYPE_DOCUMENT);
						lf.AddChild(lfCh);
						String savePath = Global.getRealPath()
								+ cfg.get("file_netdisk") + "/"
								+ lfCh.getFilePath() + "/";
						// 检查物理目录是否存在，如果不存在，则创建
						// System.out.println(getClass() + savePath +
						// " cfg.get(\"file_netdisk\")=" +
						// cfg.get("file_netdisk"));
						f = new File(savePath);
						if (!f.isDirectory()) {
							f.mkdirs();
						}
					}
				}

				LogUtil.getLog(getClass()).info(
						"uploadDirName=" + uploadDirName + " size="
								+ attachs.size());

				Iterator ir = attachs.iterator();
				String sql = "";
				int orders = getAttachmentMaxOrders() + 1;
				while (ir.hasNext()) {
					FileInfo fi = (FileInfo) ir.next();

					String myVisualPath = visualPath;
					int myDocId = docId;
					String savePath = mfu.getSavePath();

					LogUtil.getLog(getClass()).info(
							"fi.clientPath=" + fi.clientPath
									+ " uploadDirName=" + uploadDirName
									+ " savePath=" + savePath);

					if (!uploadDirName.equals("")) {
						// 检查目录是否包含于客户端路径中
						int p = fi.clientPath.indexOf(uploadDirName);
						if (p != -1) {
							// 在循环中，lf的child_count会变化，缓存会被刷新，因此在这里要重新获取
							lf = lf.getLeaf(upDirCode);

							myVisualPath = visualPath + "/" + lf.getName();
							myDocId = lf.getDocId();
							savePath = Global.getRealPath()
									+ cfg.get("file_netdisk") + "/"
									+ lf.getFilePath() + "/";

							// 取得upoadDirName后的路径
							String path = fi.clientPath.substring(p
									+ uploadDirName.length() + 1);
							// 检查path在树形结构上是否已存在，如果不存在，则创建目录节点，并创建物理目录
							String[] ary = path.split("\\\\");

							LogUtil.getLog(getClass()).info(
									"doc.getDirCode()=" + doc.getDirCode()
											+ " lf.getName()=" + lf.getName());

							Leaf plf = lf;
							Leaf lfCh = null;
							// 数组中最后一位是文件名，因此不用处理
							for (int i = 0; i < ary.length - 1; i++) {
								// 检查在孩子节点中是否存在
								boolean isFound = false;
								Iterator irLf = plf.getChildren().iterator();
								while (irLf.hasNext()) {
									Leaf lf2 = (Leaf) irLf.next();
									LogUtil.getLog(getClass()).info(
											"lf2.getName()=" + lf2.getName()
													+ " ary[i]=" + ary[i]);
									if (lf2.getName().equals(ary[i])) {
										isFound = true;
										lfCh = lf2;
										plf = lf2;
										break;
									}
								}

								LogUtil.getLog(getClass()).info(
										"isFound=" + isFound);

								if (!isFound) {
									// 创建节点及物理目录
									lfCh = new Leaf();
									lfCh.setName(ary[i]);
									lfCh.setCode(Leaf.getAutoCode());
									lfCh.setParentCode(plf.getCode());
									lfCh.setDescription(ary[i]);
									lfCh.setType(Leaf.TYPE_DOCUMENT);

									plf.AddChild(lfCh);

									lfCh = lfCh.getLeaf(lfCh.getCode());

									plf = lfCh;

								}

								myDocId = lfCh.getDocId();
							}

							if (lfCh != null) {
								myVisualPath = lfCh.getFilePath();
								savePath = Global.getRealPath()
										+ cfg.get("file_netdisk") + "/"
										+ lfCh.getFilePath() + "/";
								// 检查物理目录是否存在，如果不存在，则创建
								f = new File(savePath);
								if (!f.isDirectory()) {
									f.mkdirs();
								}
							}
						}
					}

					LogUtil.getLog(getClass()).info(
							"fi.getName()=" + fi.getName() + " myDocId="
									+ myDocId + " docId=" + docId
									+ " savePath=" + savePath);

					fi.write(savePath, "");

					// 检查该目录下,是否已有同名文件,如果有,则不入库
					Attachment att = new Attachment();
					if (!att.isExist(fi.getName(), myDocId)) {
						LogUtil.getLog(getClass()).info(
								"File " + fi.getName() + " is not exist");

						sql = "insert into netdisk_document_attach (doc_id,name,diskname,visualpath,page_num,orders,file_size,ext,uploadDate,user_name,id,version_date) values (?,?,?,?,?,?,?,?,?,?,?,?)";
						PreparedStatement ps = conn.prepareStatement(sql);
						ps.setInt(1, myDocId);
						ps.setString(2, fi.getName());
						ps.setString(3, fi.getName()); // fi.getDiskName());
						ps.setString(4, myVisualPath);
						ps.setInt(5, pageNum);
						ps.setInt(6, orders);
						ps.setLong(7, fi.getSize());
						ps.setString(8, fi.getExt());
						ps.setTimestamp(9, new Timestamp(new java.util.Date()
								.getTime()));
						ps.setString(10, userName);

						long id = SequenceManager
								.nextID(SequenceManager.OA_DOCUMENT_NETDISK_ATTACHMENT);
						ps.setLong(11, id);
						f = new File(savePath + fi.getDiskName());
						ps.setTimestamp(12, new Timestamp(f.lastModified()));
						conn.executePreUpdate();

						DocContentCacheMgr dcm = new DocContentCacheMgr();
						dcm.refreshUpdate(myDocId, pageNum);

						if (ps != null) {
							ps.close();
							ps = null;
						}
						orders++;
					}
				}
				// 更新用户的磁盘已用空间
				ud.setDiskSpaceUsed(ud.getDiskSpaceUsed() + allSize);
				ud.save();
			}
			/*
			 * PreparedStatement pstmt = conn.prepareStatement(SAVE);
			 * pstmt.setString(1, content); pstmt.setInt(2, docId);
			 * pstmt.setInt(3, pageNum); re = conn.executePreUpdate() == 1 ?
			 * true : false; pstmt.close();
			 */

			// 更新缓存
			DocContentCacheMgr dcm = new DocContentCacheMgr();
			dcm.refreshUpdate(docId, pageNum);
		} catch (SQLException e) {
			re = false;
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return re;
	}

	public int getDocId() {
		return docId;
	}

	public int getPageNum() {
		return pageNum;
	}

	/**
	 * 
	 * @param sql
	 *            String
	 * @return int -1 表示sql语句不合法
	 */
	public static int getContentCount(int doc_id) {
		DocContentCacheMgr dcm = new DocContentCacheMgr();
		return dcm.getContentCount(doc_id);
	}

	public boolean create(ServletContext application,
			CMSMultiFileUploadBean mfu, int doc_id, String content, int pageNum)
			throws ErrMsgException {
		String sql = "";
		String FilePath = StrUtil.getNullString(mfu.getFieldValue("filepath"));
		// String tempAttachFilePath = application.getRealPath("/") + FilePath +
		// "/";
		String tempAttachFilePath = Global.getRealPath() + FilePath + "/";
		mfu.setSavePath(tempAttachFilePath); // 取得目录
		File f = new File(tempAttachFilePath);
		if (!f.isDirectory()) {
			f.mkdirs();
		}

		Conn conn = new Conn(connname);
		boolean re = true;
		try {
			// 处理附件
			int ret = mfu.getRet();
			// 如果上传成功
			if (ret == CMSMultiFileUploadBean.RET_SUCCESS) {
				// 处理HTMLCODE中的文件
				mfu.writeFile(false); // 用文件本来的名称命名文件
				Vector files = mfu.getFiles();
				java.util.Enumeration e = files.elements();
				String filepath = "";
				sql = "";
				while (e.hasMoreElements()) {
					FileInfo fi = (FileInfo) e.nextElement();
					filepath = FilePath + "/" + fi.getName();
					sql = "insert into cms_images (path,mainkey,kind,subkey) values ("
							+ StrUtil.sqlstr(filepath)
							+ ","
							+ docId
							+ ",'"
							+ IMGKIND + "'," + this.pageNum + ")";
					conn.executeUpdate(sql);
				}
			} else
				throw new ErrMsgException("上传失败！ret=" + ret);

			re = createWithoutFile(application, mfu, doc_id, content, pageNum);
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error("create:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return re;
	}

	private Vector attachments = new Vector();

	public Vector getAttachments() {
		return attachments;
	}

	/**
	 * 逻辑删除附件
	 * 
	 * @param attachId
	 * @return
	 */
	public boolean delAttachmentLogical(int attachId) {
		Attachment am = new Attachment(attachId);
		boolean re = am.delLogical();
		// 更新缓存
		if (re) {
			// 更新缓存
			DocContentCacheMgr dcm = new DocContentCacheMgr();
			dcm.refreshUpdate(docId, pageNum);
		}
		return re;
	}

	// 删除恢复---------

	public boolean restore(int attachId) {
		Attachment am = new Attachment(attachId);
		boolean re = am.restore();
		// 更新缓存
		if (re) {
			// 更新缓存
			DocContentCacheMgr dcm = new DocContentCacheMgr();
			dcm.refreshUpdate(docId, pageNum);
		}
		return re;
	}

	// 文件重命名-------

	public boolean changeName(int attachId, String newName) {
		Attachment am = new Attachment(attachId);
		int attDocId = am.getDocId();
		boolean re = am.changeName(newName, attDocId);
		// 更新缓存
		if (re) {
			// 更新缓存
			DocContentCacheMgr dcm = new DocContentCacheMgr();
			dcm.refreshUpdate(docId, pageNum);
		}
		return re;
	}

	// -------

	public synchronized boolean delAttachment(int attachId) {
		Attachment am = new Attachment(attachId);
		boolean re = am.del();
		return re;
	}

	public synchronized boolean updateAttachmentName(int attachId,
			String newname) {
		String sql = "update netdisk_document_attach set name=? where id=?";
		Conn conn = new Conn(connname);
		boolean re = false;
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, newname);
			pstmt.setInt(2, attachId);
			re = conn.executePreUpdate() == 1 ? true : false;
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e.getMessage());
		} finally {
			// 更新缓存
			DocContentCacheMgr dcm = new DocContentCacheMgr();
			dcm.refreshUpdate(docId, pageNum);
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return re;
	}

	public int getAttachmentMaxOrders() {
		String GETMAXORDERS = "select max(orders) from netdisk_document_attach where doc_id=? and page_num=?";
		Conn conn = new Conn(connname);
		ResultSet rs = null;
		int maxorders = -1;
		try {
			// 更新文件内容
			PreparedStatement pstmt = conn.prepareStatement(GETMAXORDERS);
			pstmt.setInt(1, docId);
			pstmt.setInt(2, pageNum);
			rs = conn.executePreQuery();
			if (rs != null) {
				if (rs.next()) {
					maxorders = rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error("getMaxOrders:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return maxorders;
	}

	public void renew() {

	}

	public boolean moveAttachment(int attachId, String direction) {
		Attachment attach = new Attachment(attachId);
		boolean re = false;
		int orders = attach.getOrders();
		if (direction.equals("up")) {
			if (orders == 1)
				return true;
			else {
				Attachment upperAttach = new Attachment(orders - 1, docId,
						pageNum);
				if (upperAttach.isLoaded()) {
					upperAttach.setOrders(orders);
					upperAttach.save();
				}
				attach.setOrders(orders - 1);
				re = attach.save();
			}
		} else {
			int maxorders = getAttachmentMaxOrders();
			if (orders == maxorders) {
				return true;
			} else {
				Attachment lowerAttach = new Attachment(orders + 1, docId,
						pageNum);
				if (lowerAttach.isLoaded()) {
					lowerAttach.setOrders(orders);
					lowerAttach.save();
				}
				attach.setOrders(orders + 1);
				re = attach.save();
			}
		}
		// 更新缓存
		DocContentCacheMgr dcm = new DocContentCacheMgr();
		dcm.refreshUpdate(docId, pageNum);
		return re;
	}

}
