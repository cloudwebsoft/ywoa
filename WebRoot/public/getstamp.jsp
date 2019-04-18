<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="cn.js.fan.web.Global"%>
<%@page import="java.net.*"%><%@page import = "cn.js.fan.util.*"%><%@page import="com.redmoon.oa.person.*"%><%@page import="com.redmoon.oa.stamp.*"%><%@page import="com.redmoon.oa.kernel.*"%><jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/><%
if (License.getInstance().isEnterprise() || License.getInstance().isGroup() || com.redmoon.oa.kernel.License.getInstance().isPlatform())
	;
else {
	out.print("系统版本无此功能！");
	return;
}
String userName = StrUtil.UnicodeToGB(request.getParameter("user"));
String pwd = StrUtil.UnicodeToGB(request.getParameter("pwd"));
String filename = "";

if (userName==null) {
	out.print("用户名不存在！");
}
else {
	UserDb user = new UserDb();
	user = user.getUserDb(userName);
	if (!user.isLoaded()) {
		out.print("用户不存在！");
		return;
	}
	boolean re = user.Auth(userName, pwd);
	if (!re) {
		out.print("警告非法用户，你没有访问权限！");
		return;
	}
	
	String st = StrUtil.Unicode2GB(request.getParameter("stampId"));
	if (st.equals("")) {
		out.print("您没有输入或选择印章名称！");
		return;
	}
	int stampId = StrUtil.toInt(st, -1);
	StampDb stamp = new StampDb();
	if (stampId!=-1) {
		stamp = stamp.getStampDb(stampId);
		if (!stamp.isLoaded()) {
			out.print("印章" + request.getParameter("stampId") + "不存在！");
			return;
		}
	}
	else {
		// 将stampId作为名称
		// System.out.println(getClass() + " " + st);
		stamp = stamp.getStampDbByName(st);
		if (stamp==null) {
			out.print("印章" + st + "不存在！");
			return;
		}
		stampId = stamp.getId();
	}
	if (!stamp.canUserUse(userName)) {
		out.print("警告：您没有使用此签章的权限！");
		return;
	}
	filename = stamp.getImage();
	
	StampLogDb sld = new StampLogDb();
	sld.create(new com.cloudwebsoft.framework.db.JdbcTemplate(), new Object[]{new Long(com.redmoon.oa.db.SequenceManager.nextID(com.redmoon.oa.db.SequenceManager.OA_STAMP_LOG)),userName,new Integer(stampId),new java.util.Date(),StrUtil.getIp(request)});
}

response.setContentType("application/bmp");
response.setHeader("Content-disposition","attachment; filename="+filename);

BufferedInputStream bis = null;
BufferedOutputStream bos = null;

try {
//bis = new BufferedInputStream(new FileInputStream(application.getRealPath("/") + "upfile/" + StampDb.linkBasePath + "/" + filename));
bis = new BufferedInputStream(new FileInputStream(Global.getRealPath() + "upfile/" + StampDb.linkBasePath + "/" + filename));
bos = new BufferedOutputStream(response.getOutputStream());

byte[] buff = new byte[2048];
int bytesRead;

while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
bos.write(buff,0,bytesRead);
}

} catch(final IOException e) {
System.out.println ( "出现IOException." + e );
} finally {
if (bis != null)
bis.close();
if (bos != null)
bos.close();
}
%>