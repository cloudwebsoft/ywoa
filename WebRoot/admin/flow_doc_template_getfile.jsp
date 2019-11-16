<%@ page contentType="text/html;charset=gb2312"%><%@page import="cn.js.fan.util.*"%><%@page import="java.io.*"%><%@page import="java.net.*"%>
<%@page import="com.redmoon.oa.mobileskins.MobileSkinsMgr"%>
<%@page import="com.redmoon.oa.mobileskins.MobileSkinsDb"%>
<%@page import="com.redmoon.oa.visual.Attachment"%>
<%@page import="com.redmoon.oa.android.Privilege"%>
<%@page import="com.redmoon.oa.person.UserDb"%>
<%@page import="com.redmoon.oa.android.base.BaseAction"%>
<%@page import="com.redmoon.oa.worklog.WorkLogAttachmentDb"%>
<%@page import="cn.js.fan.web.Global"%><jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="fsecurity" scope="page" class="cn.js.fan.security.SecurityUtil"/><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%@page import="org.json.JSONObject"%><%@page import="org.json.JSONException"%>
<%@ page import="com.redmoon.oa.flow.DocTemplateMgr" %>
<%@ page import="com.redmoon.oa.flow.DocTemplateDb" %>
<%
	int id = ParamUtil.getInt(request, "id");
	DocTemplateMgr dtm = new DocTemplateMgr();
	DocTemplateDb dtd = dtm.getDocTemplateDb(id);

	String fileName = dtd.getFileName();
	String url = Global.getRealPath()+"/upfile/" + DocTemplateDb.linkBasePath + "/" + fileName;

	response.setContentType("application/octet-stream");
	response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode(fileName));
	BufferedInputStream bis = null;
	BufferedOutputStream bos = null;

	try {
		bis = new BufferedInputStream(new FileInputStream(url));
		bos = new BufferedOutputStream(response.getOutputStream());

		byte[] buff = new byte[2048];
		int bytesRead;

		while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
			bos.write(buff,0,bytesRead);
		}
	} catch(final IOException e) {
		System.out.println( "IOException." + e );
	} finally {
		if (bis != null)
			bis.close();
		if (bos != null)
			bos.close();
	}

	out.clear();
	out = pageContext.pushBody();
%>