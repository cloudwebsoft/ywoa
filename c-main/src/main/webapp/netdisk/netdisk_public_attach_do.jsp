<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String correct_result = "操作成功！";	
PublicAttachmentMgr pam = new PublicAttachmentMgr();
int type = ParamUtil.getInt(request,"type",1);//1代表极速上传 0代表普通上传
boolean flag = true;

try {
	if(type==1){
		flag = pam.upload(application,request);
	}else{
		flag = pam.commonUpload(application, request);
	}
	if (flag) {
		out.print(correct_result);
	}
	else {
		out.print("操作失败！");
	}
}
catch (ErrMsgException e) {
	out.print(e.getMessage());
}
%>