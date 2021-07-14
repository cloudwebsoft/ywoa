package com.redmoon.oa.netdisk;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Vector;
import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.db.Conn;
import cn.js.fan.util.*;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.*;

import org.apache.log4j.*;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.kit.util.FileUploadExt;

public class DocumentMgr {
	Logger logger = Logger.getLogger(DocumentMgr.class.getName());

	public DocumentMgr() {
	}

	public Document getDocument(int id) {
		Document doc = new Document();
		return doc.getDocument(id);
	}

	public Document getDocument(HttpServletRequest request, int id,
			IPrivilege privilege) throws ErrMsgException {
		boolean isValid = false;
		Document doc = getDocument(id);
		LeafPriv lp = new LeafPriv(doc.getDirCode());
		if (lp.canUserSee(privilege.getUser(request))) {
			return doc;
		}

		if (!isValid)
			throw new ErrMsgException(Privilege.MSG_INVALID);
		return getDocument(id);
	}

	/**
	 * 当directory的结点code的类型为文章时，取其文章，如果文章不存在，则创建文章
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param code
	 *            String
	 * @param privilege
	 *            IPrivilege
	 * @return Document
	 * @throws ErrMsgException
	 */
	public Document getDocumentByCode(HttpServletRequest request, String code,
			IPrivilege privilege) throws ErrMsgException {

		boolean isValid = false;

		LeafPriv lp = new LeafPriv(code);
		if (lp.canUserSee(privilege.getUser(request)))
			isValid = true;

		if (!isValid)
			throw new ErrMsgException(Privilege.MSG_INVALID);
		Document doc = new Document();
		int id = -1;
		String nick;
		if ("system".equals(code)) {
			nick = "system";
		} else {
			nick = privilege.getUser(request);
		}
		id = doc.getIDOrCreateByCode(code, nick);

		return getDocument(id);
	}

	public CMSMultiFileUploadBean doUpload(ServletContext application,
			HttpServletRequest request) throws ErrMsgException {
		CMSMultiFileUploadBean mfu = new CMSMultiFileUploadBean();
		// mfu.setDebug(true);
		mfu.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
		// String[] ext = {"htm", "gif", "bmp", "jpg", "png", "rar", "doc",
		// "hs", "ppt", "rar", "zip", "jar"};
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String exts = cfg.get("netdisk_ext").replaceAll("，", ",");
		exts = "," + exts; // 加上空扩展名, android, 注意逗号加在后面会被split忽略,必须加在前面
		String[] ext = StrUtil.split(exts, ",");

		// System.out.println(getClass() + " " + exts + " " + ext.length);

		if (ext != null)
			mfu.setValidExtname(ext);

		int ret = 0;
		// logger.info("ret=" + ret);
		try {
			ret = mfu.doUpload(application, request);
			if (ret == -3) {
				throw new ErrMsgException(mfu.getErrMessage());
			}
			if (ret == -4) {
				throw new ErrMsgException(mfu.getErrMessage());
			}
		} catch (IOException e) {
			logger.error("doUpload:" + e.getMessage());
			throw new ErrMsgException(e.getMessage());
		}
		return mfu;
	}

	/**
	 * 批量逻辑删除文件
	 * 
	 * @param request
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean delAttachmentBatch(HttpServletRequest request)
			throws ErrMsgException {
		int doc_id = ParamUtil.getInt(request, "doc_id");
		Document doc = new Document();
		doc = doc.getDocument(doc_id);
		LeafPriv lp = new LeafPriv(doc.getDirCode());
		Privilege privilege = new Privilege();
		if (!lp.canUserDel(privilege.getUser(request))) {
			throw new ErrMsgException(SkinUtil.LoadString(request,
					"pvg_invalid"));
		}

		int page_num = ParamUtil.getInt(request, "page_num");
		DocContent dc = doc.getDocContent(page_num);

		String ids = ParamUtil.get(request, "ids");
		String[] ary = StrUtil.split(ids, ",");
		if (ary == null)
			return false;
		for (int i = 0; i < ary.length; i++) {
			dc.delAttachmentLogical(StrUtil.toInt(ary[i]));
		}
		return true;
	}

	/**
	 * 逻辑删除
	 * 
	 * @param request
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean delAttachment(HttpServletRequest request)
			throws ErrMsgException {
		int doc_id = ParamUtil.getInt(request, "doc_id");
		int attach_id = ParamUtil.getInt(request, "attach_id");
		int page_num = ParamUtil.getInt(request, "page_num");
		Document doc = new Document();
		doc = doc.getDocument(doc_id);
		LeafPriv lp = new LeafPriv(doc.getDirCode());
		Privilege privilege = new Privilege();
		if (!lp.canUserDel(privilege.getUser(request))) {
			throw new ErrMsgException(SkinUtil.LoadString(request,
					"pvg_invalid"));
		}
		DocContent dc = doc.getDocContent(page_num);
		boolean re = dc.delAttachmentLogical(attach_id);
		return re;
	}

	/**
	 * 彻底删除
	 * 
	 * @param request
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean remove(HttpServletRequest request) throws ErrMsgException {
		int doc_id = ParamUtil.getInt(request, "doc_id");
		int attach_id = ParamUtil.getInt(request, "attach_id");
		int page_num = ParamUtil.getInt(request, "page_num");
		Document doc = new Document();
		doc = doc.getDocument(doc_id);
		LeafPriv lp = new LeafPriv(doc.getDirCode());
		Privilege privilege = new Privilege();
		if (!lp.canUserDel(privilege.getUser(request))) {
			throw new ErrMsgException(SkinUtil.LoadString(request,
					"pvg_invalid"));
		}
		DocContent dc = doc.getDocContent(page_num);
		boolean re = dc.delAttachment(attach_id);
		return re;
	}

	/**
	 * 批量彻底删除文件
	 * 
	 * @param request
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean removeBatch(HttpServletRequest request)
			throws ErrMsgException {
		int doc_id = ParamUtil.getInt(request, "doc_id");
		Document doc = new Document();
		doc = doc.getDocument(doc_id);
		LeafPriv lp = new LeafPriv(doc.getDirCode());
		Privilege privilege = new Privilege();
		if (!lp.canUserDel(privilege.getUser(request))) {
			throw new ErrMsgException(SkinUtil.LoadString(request,
					"pvg_invalid"));
		}

		int page_num = ParamUtil.getInt(request, "page_num");
		DocContent dc = doc.getDocContent(page_num);

		String ids = ParamUtil.get(request, "ids");
		String[] ary = StrUtil.split(ids, ",");
		if (ary == null)
			return false;
		for (int i = 0; i < ary.length; i++) {
			dc.delAttachment(StrUtil.toInt(ary[i]));
		}
		return true;
	}

	/**
	 * 删除恢复
	 * 
	 * @param request
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean restore(HttpServletRequest request) throws ErrMsgException {
		int doc_id = ParamUtil.getInt(request, "doc_id");
		int attach_id = ParamUtil.getInt(request, "attach_id");
		int page_num = ParamUtil.getInt(request, "page_num");
		Document doc = new Document();
		doc = doc.getDocument(doc_id);
		LeafPriv lp = new LeafPriv(doc.getDirCode());
		Privilege privilege = new Privilege();
		if (!lp.canUserDel(privilege.getUser(request))) {
			throw new ErrMsgException(SkinUtil.LoadString(request,
					"pvg_invalid"));
		}
		DocContent dc = doc.getDocContent(page_num);
		boolean re = dc.restore(attach_id);
		return re;
	}

	public boolean updateAttachmentName(HttpServletRequest request)
			throws ErrMsgException {
		int doc_id = ParamUtil.getInt(request, "doc_id");
		int attach_id = ParamUtil.getInt(request, "attach_id");
		int page_num = ParamUtil.getInt(request, "page_num");
		String newname = ParamUtil.get(request, "newname");
		Document doc = getDocument(doc_id);

		// 检查用户是否有权限
		LeafPriv lp = new LeafPriv();
		lp.setDirCode(doc.getDirCode());

		Privilege privilege = new Privilege();
		if (!lp.canUserModify(privilege.getUser(request)))
			throw new ErrMsgException(SkinUtil.LoadString(request,
					"pvg_invalid"));

		DocContent dc = doc.getDocContent(page_num);
		boolean re = dc.updateAttachmentName(attach_id, newname);
		return re;
	}

	public boolean Operate(ServletContext application,
			HttpServletRequest request, IPrivilege privilege)
			throws ErrMsgException {
		CMSMultiFileUploadBean mfu = doUpload(application, request);
		String op = StrUtil.getNullStr(mfu.getFieldValue("op"));
		String dir_code = StrUtil.getNullStr(mfu.getFieldValue("dir_code"));

		// System.out.println(getClass() + " dir_code=" + dir_code + " op=" +
		// op);

		// logger.info("op=" + op);
		boolean isValid = false;
		LeafPriv lp = new LeafPriv(dir_code);
		if (lp.canUserAppend(privilege.getUser(request)))
			isValid = true;

		if (!isValid)
			throw new ErrMsgException(SkinUtil.LoadString(request,
					"pvg_invalid"));

		Document doc = new Document();
		if (op.equals("edit")) {
			String idstr = StrUtil.getNullString(mfu.getFieldValue("id"));
			if (!StrUtil.isNumeric(idstr))
				throw new ErrMsgException("标识id=" + idstr + "非法，必须为数字！");
			id = Integer.parseInt(idstr);
			doc = doc.getDocument(id);
			if (!lp.canUserAppend(privilege.getUser(request)))
				throw new ErrMsgException(SkinUtil.LoadString(request,
						"pvg_invalid"));
			boolean re = doc.Update(application, mfu);
			return re;
		}
		/*
		 * else { if (!lp.canUserAppend(privilege.getUser(request))) throw new
		 * ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid")); boolean
		 * re = doc.create(application, mfu, privilege.getUser(request)); return
		 * re; }
		 */
		return false;
	}

	public boolean del(HttpServletRequest request, int id, IPrivilege privilege)
			throws ErrMsgException {
		boolean isValid = false;

		Document doc = new Document();
		doc = getDocument(id);
		LeafPriv lp = new LeafPriv(doc.getDirCode());
		if (lp.canUserDel(privilege.getUser(request)))
			return doc.del();

		if (!isValid)
			throw new ErrMsgException(Privilege.MSG_INVALID);

		return doc.del();
	}

	public boolean UpdateSummary(ServletContext application,
			HttpServletRequest request, IPrivilege privilege)
			throws ErrMsgException {

		CMSMultiFileUploadBean mfu = doUpload(application, request);
		int id = 0;
		try {
			id = Integer.parseInt(mfu.getFieldValue("id"));
		} catch (Exception e) {
			throw new ErrMsgException("id 非法！");
		}
		Document doc = new Document();
		doc = getDocument(id);

		LeafPriv lp = new LeafPriv(doc.getDirCode());
		if (!lp.canUserModify(privilege.getUser(request))) {
			throw new ErrMsgException(IPrivilege.MSG_INVALID);
		}

		return doc.UpdateSummary(application, mfu);
	}

	public boolean increaseHit(HttpServletRequest request, int id,
			IPrivilege privilege) throws ErrMsgException {
		Document doc = getDocument(id);
		boolean re = doc.increaseHit();
		return re;
	}

	public boolean UpdateIsHome(HttpServletRequest request, int id,
			IPrivilege privilege) throws ErrMsgException {

		Document doc = new Document();
		String v = ParamUtil.get(request, "value");
		doc.setID(id);
		boolean re = doc.UpdateIsHome(v.equals("y") ? true : false);
		return re;

	}

	public boolean vote(HttpServletRequest request, int id)
			throws ErrMsgException {
		int votesel = ParamUtil.getInt(request, "votesel");

		Document doc = getDocument(id);
		boolean re = doc.vote(id, votesel);
		return re;
	}

	public boolean OperatePage(ServletContext application,
			HttpServletRequest request, IPrivilege privilege)
			throws ErrMsgException {
		CMSMultiFileUploadBean mfu = doUpload(application, request);
		String op = StrUtil.getNullStr(mfu.getFieldValue("op"));
		String dir_code = StrUtil.getNullStr(mfu.getFieldValue("dir_code"));

		boolean isValid = false;

		LeafPriv lp = new LeafPriv();
		lp.setDirCode(dir_code);
		if (op.equals("add")) {
			if (lp.canUserAppend(privilege.getUser(request)))
				isValid = true;
		}
		if (op.equals("edit")) {
			if (lp.canUserModify(privilege.getUser(request)))
				isValid = true;
		}

		if (!isValid)
			throw new ErrMsgException(Privilege.MSG_INVALID);

		String strdoc_id = StrUtil.getNullStr(mfu.getFieldValue("id"));
		int doc_id = Integer.parseInt(strdoc_id);
		Document doc = new Document();
		doc = doc.getDocument(doc_id);

		// logger.info("filepath=" + mfu.getFieldValue("filepath"));

		if (op.equals("add")) {
			String content = StrUtil.getNullStr(mfu.getFieldValue("htmlcode"));
			return doc.AddContentPage(application, mfu, content);
		}

		if (op.equals("edit")) {
			// return doc.EditContentPage(content, pageNum);
			return doc.EditContentPage(application, mfu);
		}

		return false;
	}

	public boolean uploadDocument(ServletContext application,
			HttpServletRequest request) throws ErrMsgException {
		FileUpload fu = new FileUpload();
		fu.setMaxFileSize(Global.FileSize); // 35000 // 最大35000K

		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String exts = cfg.get("netdisk_ext").replaceAll("，", ",");
		String[] ext = StrUtil.split(exts, ",");
		if (ext != null)
			fu.setValidExtname(ext);

		int ret = 0;
		try {
			ret = fu.doUpload(application, request);
			if (ret != FileUpload.RET_SUCCESS) {
				throw new ErrMsgException(fu.getErrMessage());
			}
		} catch (Exception e) {
			logger.error("uploadDocument:" + e.getMessage());
		}

		if (ret == FileUpload.RET_SUCCESS) {
			String strId = fu.getFieldValue("id");
			String strAttachId = fu.getFieldValue("attachId");

			if (!StrUtil.isNumeric(strId))
				throw new ErrMsgException("id 必须为数字");
			if (!StrUtil.isNumeric(strAttachId))
				throw new ErrMsgException("attachId 必须为数字");

			Document doc = new Document();
			doc = doc.getDocument(StrUtil.toInt(strId));
			if (doc == null || !doc.isLoaded()) {
				throw new ErrMsgException("文件不存在！");
			}

			Attachment att = doc.getAttachment(1, StrUtil.toInt(strAttachId));
			if (att == null || !att.isLoaded()) {
				throw new ErrMsgException("文件不存在！");
			}

			LeafPriv lp = new LeafPriv(att.getDirCode());
			Privilege privilege = new Privilege();
			if (!lp.canUserModify(privilege.getUser(request))) {
				throw new ErrMsgException(SkinUtil.LoadString(request,
						"pvg_invalid"));
			}

			String file_netdisk = cfg.get("file_netdisk");
			String dirPath = Global.getRealPath() + file_netdisk + "/"
					+ att.getVisualPath() + "/";
			String filePath = dirPath + att.getName();
			File file = new File(filePath);
			com.redmoon.kit.util.FileInfo fi = (com.redmoon.kit.util.FileInfo) (fu
					.getFiles().get(0));

			Leaf lf = new Leaf();
			lf = lf.getLeaf(doc.getDirCode());
			String userName = lf.getRootCode();

			long newSize = fi.getSize();
			long oldSize = att.getSize();
			UserDb ud = new UserDb();
			ud = ud.getUserDb(userName);
			if (ud.getDiskSpaceUsed() - oldSize + newSize > ud
					.getDiskSpaceAllowed())
				throw new ErrMsgException("您上传的文件太大了，超出了允许的磁盘空间！");

			// 为修改后的office文件添加备份文件
			Date versionDate = att.getVersionDate();
			if (versionDate == null) {
				versionDate = new Date(file.lastModified());
			}
			String vdate = DateUtil.format(versionDate, "yyyyMMddHHmmss");
			String oldDiskName = att.getName() + "_" + vdate;
			String oldDirPath = Global.getRealPath() + file_netdisk + "/"
					+ att.getVisualPath() + "/";
			File oldFile = new File(oldDirPath + oldDiskName);
			if (oldFile.exists()) {
				oldFile.delete();
			}

			boolean re = FileUtil.CopyFile(filePath, oldDirPath + oldDiskName);

			if (re) {
				// 数据库的文件备份添加
				int oldId = att.getId();
				att.setDiskName(oldDiskName);
				re = att.create();

				if (re) {
					att.setCurrent(false);
					att.save();
					// 上传新文件
					re = fi.write(dirPath, att.getName());
					file = new File(filePath);
					if (re) {
						// 更新数据库原来记录的信息
						att.setId(oldId);
						att.setDiskName(att.getName());
						att.setCurrent(true);
						att.setVersionDate(new Date(file.lastModified()));
						att.setSize(fi.getSize());
						re = att.save();
						if (re) {
							// 更新已用空间大小
							ud.setDiskSpaceUsed(ud.getDiskSpaceUsed() - oldSize
									+ newSize);
							ud.save();
						}
					}
				}
			}
			return re;
		} else
			return false;
	}

	/**
	 * 用于netdisk_list_new.jsp上传文件
	 * 
	 * @param application
	 *            ServletContext
	 * @param request
	 *            HttpServletRequest
	 * @return String[] 图片的ID数组
	 * @throws ErrMsgException
	 */
	public boolean uploadFile(ServletContext application,
			HttpServletRequest request) throws ErrMsgException {
		Privilege privilege = new Privilege();
		if (!privilege.isUserLogin(request))
			throw new ErrMsgException(SkinUtil.LoadString(request,
					SkinUtil.ERR_NOT_LOGIN));

		FileUploadExt fu = new FileUploadExt();
		fu.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
		// String[] ext = {"htm", "gif", "bmp", "jpg", "png", "rar", "doc",
		// "hs", "ppt", "rar", "zip", "jar"};
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String exts = cfg.get("netdisk_ext").replaceAll("，", ",");
		String[] ext = StrUtil.split(exts, ",");
		if (ext != null)
			fu.setValidExtname(ext);

		int ret = 0;
		try {
			ret = fu.doUpload(application, request);
			if (ret != FileUpload.RET_SUCCESS) {
				throw new ErrMsgException(fu.getErrMessage(request));
			}
			if (fu.getFiles().size() == 0)
				throw new ErrMsgException("请上传文件！");
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error("doUpload:" + e.getMessage());
			throw new ErrMsgException(e.getMessage());
		}

		int docId = StrUtil.toInt(fu.getFieldValue("docId"));
		Document doc = new Document();
		doc = doc.getDocument(docId);
		Leaf lf = new Leaf();
		lf = lf.getLeaf(doc.getDirCode());
		String userName = lf.getRootCode();

		String visualPath = StrUtil.getNullString(fu.getFieldValue("filepath"));

		String FilePath = cfg.get("file_netdisk") + "/" + visualPath;

		// 处理附件
		// String tempAttachFilePath = application.getRealPath("/") +
		// FilePath +
		// "/";
		String tempAttachFilePath = Global.getRealPath() + FilePath + "/";

		long allSize = 0;

		fu.setSavePath(tempAttachFilePath); // 取得目录

		File f = new File(tempAttachFilePath);
		if (!f.isDirectory()) {
			f.mkdirs();
		}
		// 检查上传的文件大小有没有超出磁盘空间
		Vector attachs = fu.getFiles();
		Iterator attir = attachs.iterator();
		while (attir.hasNext()) {
			FileInfo fi = (FileInfo) attir.next();
			allSize += fi.getSize();
		}

		UserDb ud = new UserDb();
		ud = ud.getUserDb(userName);
		if (ud.getDiskSpaceUsed() + allSize > ud.getDiskSpaceAllowed())
			throw new ErrMsgException("您上传的文件太大了，超出了允许的磁盘空间！");

		String sql = "";
		DocContent dc = doc.getDocContent(1);
		int orders = dc.getAttachmentMaxOrders() + 1;
		Iterator ir = attachs.iterator();
		Attachment att = new Attachment();
		if (ir.hasNext()) {
			FileInfo fi = (FileInfo) ir.next();

			String myVisualPath = visualPath;
			int myDocId = docId;
			String savePath = fu.getSavePath();

			fi.write(savePath, "");

			// 检查该目录下,是否已有同名文件,如果有,则不写入数据库
			if (!att.isExist(fi.getName(), myDocId)) {
				LogUtil.getLog(getClass()).info(
						"File " + fi.getName() + " is not exist");

				int pageNum = 1;

				Conn conn = new Conn(Global.getDefaultDB());
				sql = "insert into netdisk_document_attach (doc_id,name,diskname,visualpath,page_num,orders,file_size,ext,uploadDate,user_name) values (?,?,?,?,?,?,?,?,?,?)";
				PreparedStatement ps;
				try {
					ps = conn.prepareStatement(sql);
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
					conn.executePreUpdate();

					DocContentCacheMgr dcm = new DocContentCacheMgr();
					dcm.refreshUpdate(myDocId, pageNum);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				} finally {
					if (conn != null) {
						conn.close();
						conn = null;
					}
				}

				orders++;
			}
		}

		// 更新用户的磁盘已用空间
		ud.setDiskSpaceAllowed(ud.getDiskSpaceUsed() + allSize);
		ud.save();

		return true;
	}

	public int getId() {
		return id;
	}

	/**
	 * 用于clouddisk
	 * 
	 * @param request
	 * @param parentCode
	 * @param name
	 * @param privilege
	 * @return
	 * @throws ErrMsgException
	 */
	public Document getDocumentByName(HttpServletRequest request,
			String parentCode, String name, IPrivilege privilege)
			throws ErrMsgException {
		boolean isValid = false;

		LeafPriv lp = new LeafPriv(parentCode);
		if (lp.canUserSee(privilege.getUser(request)))
			isValid = true;

		if (!isValid)
			throw new ErrMsgException(Privilege.MSG_INVALID);
		Document doc = new Document();
		return doc.getDocumentByName(parentCode, name);
	}

	public boolean commonUpload(ServletContext application,
			HttpServletRequest request) throws ErrMsgException {
		boolean flag = false;
		String contentType = request.getContentType();
		if (contentType.indexOf("multipart/form-data") == -1) {
			throw new IllegalStateException(
					"The content type of request is not multipart/form-data");
		}
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String exts = cfg.get("netdisk_ext").replaceAll("，", ",");
		FileUpload fu = new FileUpload();
		String[] extAry = exts.split(",");
		fu.setValidExtname(extAry);
		fu.setMaxFileSize(Global.FileSize); // 35000); // 最大35000K
		int ret = -1;
		try {
			ret = fu.doUpload(application, request);
		} catch (IOException e) {
			throw new ErrMsgException(e.getMessage());
		}

		flag = writeFile(request, fu);
		return flag;
	}

	public boolean writeFile(HttpServletRequest request, FileUpload fu)
			throws ErrMsgException {
		String userName = ParamUtil.get(request,"userName");
		Privilege privilege = new Privilege();
		if(userName == null || userName.equals("")){
			userName = privilege.getUser(request);
		}
		boolean flag = true;
		String dirCode = ParamUtil.get(request, "dirCode");
		Leaf leaf = new Leaf(dirCode);
		if (leaf != null && leaf.isLoaded()) {
			com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
			String myVisualPath = leaf.getFilePath();
			String savePath = Global.getRealPath() + cfg.get("file_netdisk")
					+ "/" + myVisualPath + "/";
			// 如果没有物理文件夹创建
			File f = new File(savePath);
			if (!f.isDirectory()) {
				f.mkdirs();
			}
			
			if (fu.getRet() == FileUpload.RET_SUCCESS) {
				Vector v = fu.getFiles();
				Iterator ir = v.iterator();
				// 置路径
				fu.setSavePath(savePath);
				if (ir.hasNext()) {
					FileInfo fi = (FileInfo) ir.next();
					// 使用随机名称写入磁盘
					fi.write(fu.getSavePath(), false);
					Attachment att = new Attachment();
					if (!att.isExist(fi.getName(), leaf.getDocId())) {
						LogUtil.getLog(getClass()).info(
								"OK, File " + fi.getName() + " is not exist");

						att.setName(fi.getName());
						att.setDiskName(fi.getName());
						att.setVisualPath(myVisualPath);
						att.setSize(fi.getSize());
						att.setExt(fi.getExt());
						att.setUserName(userName);
						att.setDocId(leaf.getDocId());
						f = new File(fu.getSavePath() + "/" + fi.getName());
						att.setVersionDate(new Date(f.lastModified()));
						flag &= att.create();
						if(flag){
							long newSize = fi.getSize();
							UserDb ud = new UserDb();
							ud = ud.getUserDb(privilege.getUser(request));
							ud.setDiskSpaceUsed(ud.getDiskSpaceUsed() + newSize);
							ud.save();
						
						}
					}
				}

			} else {
				flag = false;
			}

		} else {
			flag = false;
		}

		return flag;
	}

	private int id;
}
