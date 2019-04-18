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
	
	// 原有判断过期方法:超过当天即为过期
	//String date = DateUtil.format(DateUtil.parse(keys[1], "yyyy-MM-dd"), "yyyy-MM-dd");
	//if (!DateUtil.format(new Date(), "yyyy-MM-dd").equals(date)) {
	//	out.print(SkinUtil.makeErrMsg(request, "你的下载链接已过期，请重新获取新链接！"));
	//	return;
	//}
	
	// 从配置文件中读取有效期天数,有效期至当前时间+有效期天数*24小时
	com.redmoon.clouddisk.Config cloudcfg = com.redmoon.clouddisk.Config.getInstance();
	int urlDays = cloudcfg.getIntProperty("url_days");
	Date endDate = DateUtil.parse(keys[1], "yyyy-MM-dd HH:mm:ss");
	endDate = DateUtil.addHourDate(endDate, urlDays * 24);
	if (new Date().after(endDate)) {
		out.print(SkinUtil.makeErrMsg(request, "您的下载链接已过期，请重新获取新链接！"));
		return;
	}
}

com.redmoon.oa.Config conifg = new com.redmoon.oa.Config();
String filePath = Global.getRealPath() + conifg.get("file_netdisk") + File.separator;
String name = ParamUtil.get(request, "name");

int isDoc = ParamUtil.getInt(request, "isDoc", -1);

if (isDoc == 0) {
	filePath += "download" + File.separator + userName + File.separator + name;
} else {
	int id = ParamUtil.getInt(request, "id", 0);
	if (id == 0) {
		out.print(SkinUtil.makeErrMsg(request, "文件不存在！"));
		return;
	}

	if (name == null || name.equals("")) {
		out.print(SkinUtil.makeErrMsg(request, "文件不存在！"));
		return;
	}

	NetDiskBean netDiskBean = new NetDiskBean();
	netDiskBean.setId((long)id);
	NetDiskDb netDiskDb = new NetDiskDb(netDiskBean);
	netDiskDb.load();

	filePath += netDiskBean.getVisualPath() + File.separator + netDiskBean.getDiskName();
}

File file = new File(filePath);
if (!file.exists()) {
	out.print(SkinUtil.makeErrMsg(request, "文件不存在！"));
	return;
}

String ext = StrUtil.getFileExt(name);

response.setContentType("application/" + ext);
response.setHeader("Content-disposition","attachment; filename=" + StrUtil.GBToUnicode(name));

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
