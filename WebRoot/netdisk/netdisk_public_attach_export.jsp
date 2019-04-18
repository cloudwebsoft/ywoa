<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.util.file.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.io.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="admin";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

PublicLeaf plf = new PublicLeaf();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>公共共享导出</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">全局共享</td>
    </tr>
  </tbody>
</table>
<form action="?op=export" method="post">
<table width="767" class="tabStyle_1 percent98">
  <tbody>
    <tr>
      <td width="34%" class="tabStyle_1_title">导出</td>
    </tr>
    <tr class="highlight">
      <td align="left">文件导出后将置于<%=Global.getRealPath()%>bak目录下<br />
        <%
String op = ParamUtil.get(request, "op");
PublicAttachment att = new PublicAttachment();

if (op.equals("export")) {
	// 遍历目录，导出文件
	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	String file_netdisk = cfg.get("file_netdisk");
	String file_netdisk_public = cfg.get("file_netdisk_public");
		
	PublicLeaf publf = new PublicLeaf();
	publf = publf.getLeaf(PublicLeaf.ROOTCODE);
	Vector v = new Vector();
	publf.getAllChild(v, publf);
	Iterator ir = v.iterator();
	int count = 0;
	while (ir.hasNext()) {
		publf = (PublicLeaf)ir.next();
		String sql = "select id from netdisk_public_attach where public_dir=" + StrUtil.sqlstr(publf.getCode());
		sql += " order by create_date desc";
		
		String path = publf.getName();
		String parentCode = publf.getParentCode();
		while (!parentCode.equals(PublicLeaf.ROOTCODE)) {
			publf = publf.getLeaf(parentCode);
			path = publf.getName() + "/" + path;
			parentCode = publf.getParentCode();
		}
		
		Iterator ir2 = att.list(sql).iterator();
		
		while (ir2.hasNext()) {
 			att = (PublicAttachment)ir2.next();
					
			String fullPath;
			if (att.getAttId()!=0) {
				Attachment at = new Attachment();
				at = at.getAttachment(att.getAttId());
				fullPath = Global.getRealPath() + file_netdisk + "/" + at.getVisualPath() + "/" + at.getDiskName();
			}
			else {
				fullPath = Global.getRealPath() + file_netdisk_public + "/" + att.getVisualPath() + "/" + att.getDiskName();
			}
									
			File f = new File(Global.getAppPath() + "bak/publicshare/" + path);
			if (!f.isDirectory()) {
				f.mkdirs();
			}
			
			String newFullPath = Global.getRealPath() + "bak/publicshare/" + path + "/" + att.getName();
			out.print("从&nbsp;" + fullPath + "&nbsp;至&nbsp;" + newFullPath + "<BR>");
			
			// 拷贝至新目录
			FileUtil.CopyFile(fullPath, newFullPath);
			count++;
		}
	}
	out.print("导出完毕，共导出文件" + count + "个！");
}
%>	  
	  
	  </td>
    </tr>
    <tr class="highlight">
      <td align="center">
	  <input class="btn" value="导出" type="submit" />	  </td>
    </tr>
  </tbody>
</table>
</form>
</body>
</html>