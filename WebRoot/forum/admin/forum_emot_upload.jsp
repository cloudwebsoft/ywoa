<%@ page contentType="text/html;charset=utf-8"
import = "java.io.File"
import = "cn.js.fan.db.*"
import = "cn.js.fan.util.*"
import = "cn.js.fan.web.Global"
import = "cn.js.fan.web.SkinUtil"
import = "com.redmoon.forum.ui.*"
import = "com.redmoon.forum.*"
%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%@ include file="../inc/nocache.jsp" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

try {
    EmotMgr em = new EmotMgr();
    em.uploadImg(application, request);
	String group = em.fileUpload.getFieldValue("group");
	out.println(fchar.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "forum_emot.jsp?group=" + group));
}
catch (ErrMsgException e) {
	out.print(StrUtil.Alert_Back(e.getMessage()));
}
%>