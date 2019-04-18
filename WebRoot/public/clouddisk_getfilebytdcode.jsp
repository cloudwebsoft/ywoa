<%@ page contentType="text/html;charset=utf-8"%>
<%@page import="cn.js.fan.util.*"%>
<%@page import="cn.js.fan.web.Global"%>
<%@page import="com.redmoon.oa.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.net.*"%>
<%@page import="cn.js.fan.web.*" %>
<%@page import="cn.js.fan.security.ThreeDesUtil"%>
<%@page import="com.redmoon.oa.person.UserDb"%>
<%@page import="com.redmoon.clouddisk.db.*" %>
<%@page import="com.redmoon.clouddisk.bean.*" %>
<%@page import="com.redmoon.oa.netdisk.Attachment"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="fsecurity" scope="page" class="cn.js.fan.security.SecurityUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String authKey = ParamUtil.get(request, "authKey");

//com.redmoon.oa.sso.Config cfg = new com.redmoon.oa.sso.Config();
//authKey = ThreeDesUtil.decrypthexstr(cfg.get("key"), authKey);
authKey = cn.js.fan.security.ThreeDesUtil.decrypthexstr(com.redmoon.clouddisk.socketServer.CloudDiskThread.OA_KEY, authKey);
String[] keys = authKey.split("\\|");
String userName = "";

if (keys == null || keys.length != 2) {
	out.print(SkinUtil.makeErrMsg(request, "操作非法！"));
	return;
} else {
	userName = keys[0];
	UserDb user = new UserDb();
	user = user.getUserDb(userName);

	if (!user.isLoaded()) {
		out.print(SkinUtil.makeErrMsg(request, "用户不存在！"));
		return;
	}

	if (!user.isValid()) {
		out.print(SkinUtil.makeErrMsg(request, "用户非法！"));
		return;
	}
}

com.redmoon.oa.Config conifg = new com.redmoon.oa.Config();
String filePath = Global.getRealPath() + conifg.get("file_netdisk") + File.separator;
int attId = ParamUtil.getInt(request, "id", 0);
Attachment att = new Attachment(attId);
filePath += att.getVisualPath() + File.separator + att.getName();

File file = new File(filePath);
if (!file.exists()) {
	out.print(SkinUtil.makeErrMsg(request, "文件不存在！"));
	return;
}

response.setContentType("application/" + att.getExt());
response.setHeader("Content-disposition","attachment; filename=" + StrUtil.GBToUnicode(att.getName()));

BufferedInputStream bis = null;
BufferedOutputStream bos = null;

try {
	bis = new BufferedInputStream(new FileInputStream(file));
	bos = new BufferedOutputStream(response.getOutputStream());
	
	byte[] buff = new byte[2048];
	int bytesRead;
	
	while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
		bos.write(buff,0,bytesRead);
	}
} catch(IOException e) {
	e.printStackTrace();
} finally {
	if (bis != null)
		bis.close();
	if (bos != null)
		bos.close();
	out.clear();
	out = pageContext.pushBody();
}
%>
