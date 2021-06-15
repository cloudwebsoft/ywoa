<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.redmoon.oa.emailpop3.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.util.file.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="org.json.*"%>
<%@ page import="java.io.File"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil" />
<jsp:useBean id="privilege" scope="page"
	class="com.redmoon.oa.pvg.Privilege" />
<%
	String op = ParamUtil.get(request, "op");
	if (op.equals("saveToNetdisk")) {
		MailMsgMgr mmm = new MailMsgMgr();

		int id = ParamUtil.getInt(request, "mailId");
		int attId = ParamUtil.getInt(request, "attachmentId");
		MailMsgDb mmd = mmm.getMailMsgDb(request, id);

		String dirCode = ParamUtil.get(request, "dirCode");
		Leaf lf = new Leaf();
		lf = lf.getLeaf(dirCode);
		JSONObject json = new JSONObject();
		if (lf == null) {
			json.put("ret", "0");
			json.put("msg", "目录不存在！");
			out.print(json);
			return;
		}

		com.redmoon.oa.emailpop3.Attachment attMail = new com.redmoon.oa.emailpop3.Attachment(
				attId);

		// 将转发邮件的附件同时也另存至草稿箱
		String fullPath = Global.getRealPath() + "/"
				+ attMail.getVisualPath() + "/" + attMail.getDiskName();

		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String file_netdisk = cfg.get("file_netdisk");

		String filePath = Global.getRealPath() + file_netdisk + "/"
				+ lf.getFilePath();

		String newFullPath = filePath + "/" + attMail.getName();

		File f = new File(filePath);
		if (!f.isDirectory())
			f.mkdirs();
		// 拷贝文件
		FileUtil.CopyFile(fullPath, newFullPath);

		com.redmoon.oa.netdisk.Attachment att = new com.redmoon.oa.netdisk.Attachment();

		if (att.isExist(attMail.getName(), lf.getDocId())) {
			json.put("ret", "0");
			json.put("msg", lf.getName() + "中已存在同名文件！");
			out.print(json);
			return;
		}

		att.setFullPath(newFullPath);

		att.setName(attMail.getName());
		att.setDiskName(attMail.getName());
		att.setVisualPath(lf.getFilePath());
		att.setDocId(lf.getDocId());

		att.setPageNum(1);
		att.setExt(StrUtil.getFileExt(attMail.getName()));
		String userName = lf.getRootCode();

		att.setUserName(userName);

		f = new File(newFullPath);
		att.setVersionDate(new java.util.Date(f.lastModified()));
		att.setSize(f.length());

		boolean re = att.create();
		if (re) {
			com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb();
			ud = ud.getUserDb(privilege.getUser(request));
			ud.setDiskSpaceUsed(ud.getDiskSpaceUsed() + f.length());
			ud.save();

			json.put("ret", "1");
			json.put("msg", "操作成功！");
			out.print(json);		
		}
		else {
			json.put("ret", "0");
			json.put("msg", "操作失败！");
			out.print(json);		
		}
	}
%>