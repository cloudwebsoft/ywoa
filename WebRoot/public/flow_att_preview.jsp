<%@page contentType="text/html;charset=utf-8"%>
<%@page import="cn.js.fan.util.*"%>
<%@page import="cn.js.fan.web.*"%>
<%@page import="com.redmoon.oa.flow.*"%>
<%@page import="java.io.*"%>
<%@page import="java.net.*"%>
<%@page import="com.redmoon.oa.stamp.StampPriv"%>
<%@page import="com.redmoon.oa.stamp.StampDb"%><jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="fsecurity" scope="page" class="cn.js.fan.security.SecurityUtil"/>
<%@page import="org.json.JSONObject"%><%@page import="org.json.JSONException"%>
<%
 /*
- 功能描述：移动手机端使用
- 访问规则：手机流程模块
- 过程描述：用于手机客户端流程附件的预览
- 注意事项：
- 创建者：fgf 
- 创建时间：20170218
*/
String skey = ParamUtil.get(request, "skey");
JSONObject json = new JSONObject(); 
com.redmoon.oa.android.Privilege privilege = new com.redmoon.oa.android.Privilege();
String userName = privilege.getUserName(skey);
if ("".equals(userName)) {
	out.print(SkinUtil.makeErrMsg(request, "请先登录！"));	
	return;
}
privilege.doLogin(request, skey);

int attachId = ParamUtil.getInt(request, "attachId", -1);
if (attachId==-1) {
	out.print(SkinUtil.makeErrMsg(request, "缺少ID"));
	return;
}
Attachment att = new Attachment(attachId);
String ext = StrUtil.getFileExt(att.getDiskName());

boolean isHtml = false;
if (ext.equals("doc") || ext.equals("docx") || ext.equals("xls") || ext.equals("xlsx")) {
	isHtml = true;
}
else if (ext.equals("pdf")) {
    isHtml = true;
}	
else if ("jpg".equals(ext) || "png".equals(ext) || "gif".equals(ext) || "bmp".equals(ext)) {
	isHtml = false;
}

String path = request.getContextPath() + "/" + att.getVisualPath() + "/" + att.getDiskName();
if (isHtml) {
	String htmlFile = path.substring(0, path.lastIndexOf(".")) + ".html";
	response.sendRedirect(htmlFile);
}
else {
	response.sendRedirect(path);
}
%>