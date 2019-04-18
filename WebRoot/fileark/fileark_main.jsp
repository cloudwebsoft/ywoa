<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.fileark.plugin.*" %>
<%@ page import="com.redmoon.oa.fileark.plugin.base.*" %>
<%@ page import="cn.js.fan.module.cms.plugin.wiki.*" %>
<%
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();
%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.fileark.Directory"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>文件柜</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/frame.css" />
</head>
<body>
<table border="0" cellpadding="0" cellspacing="0" class="frameRightMain">
	<tr><td>
<%
	String dir_code = ParamUtil.get(request, "dir_code");
	if (dir_code.equals("") || dir_code.equals(Leaf.ROOTCODE)) {
		dir_code = Leaf.ROOTCODE;
		
		LeafPriv lp = new LeafPriv();
		lp.setDirCode(dir_code);
		if (privilege.isUserPrivValid(request, "admin") || lp.canUserSee(privilege.getUser(request))) {
			response.sendRedirect("document_list_m.jsp");
			return;
		}
	}
	Leaf leaf = dir.getLeaf(dir_code);
	if (leaf==null) {
		out.print(StrUtil.p_center("该目录已不存在"));
		return;
	}
	int type = leaf.getType();
	if (type==0) {
		LeafPriv lp = new LeafPriv();
		if (!dir_code.equals("")) {
			lp.setDirCode(dir_code);
		}
		if (lp.canUserModify(privilege.getUser(request))) {
%>
        <!--
        >>&nbsp;<a href="dir_frame.jsp?root_code=<%=StrUtil.UrlEncode(dir_code)%>" target="_parent">管理目录</a>
        -->
<%
		} else {
		out.print(StrUtil.p_center("请选择目录！"));
		}
	} else {
		if (leaf.getType()==Leaf.TYPE_DOCUMENT) {
			response.sendRedirect("../doc_show.jsp?dir_code=" + StrUtil.UrlEncode(leaf.getCode()));
		} else {
			PluginMgr pm = new PluginMgr();
			PluginUnit pu = pm.getPluginUnitOfDir(leaf.getCode());
			if (pu!=null && pu.getCode().equals(WikiUnit.code)) {
				response.sendRedirect("wiki_list.jsp?dir_code=" + StrUtil.UrlEncode(leaf.getCode()));
				return;
			}
			int projectId = ParamUtil.getInt(request, "projectId", 0);
			if (projectId == 0) {
				response.sendRedirect("document_list_m.jsp?dir_code=" + 
            					StrUtil.UrlEncode(leaf.getCode()) + 
								"&dir_name=" + StrUtil.UrlEncode(leaf.getName()));
			} else {
				response.sendRedirect("document_list_m.jsp?dir_code=" + 
    					StrUtil.UrlEncode(leaf.getCode()) + 
						"&dir_name=" + StrUtil.UrlEncode(leaf.getName()) + "&projectId=" + projectId + "&parentId=" + projectId + "&formCode=project");
			}
		}
	}
%>
	</td></tr>
</table>
</body>
</html>
