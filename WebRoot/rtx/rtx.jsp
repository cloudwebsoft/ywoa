<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "rtx.*"%>
<html>
<head>
<title>RTX</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" href="common.css" type="text/css">
</head>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%
String name = ParamUtil.get(request, "name");
RTXSvrApi RtxsvrapiObj=new RTXSvrApi(); 
String sessionKey = "";
if(RtxsvrapiObj.Init()) { 
	String userID=name; // new OperatorAdminObj().getCurrentUserId(request); 
	sessionKey=RtxsvrapiObj.getSessionKey(userID); 
	RtxsvrapiObj.UnInit();
}
%>
<OBJECT id=RTXAX
data=data:application/x-oleobject;base64,fajuXg4WLUqEJ7bDM/7aTQADAAAaAAAAGgAAAA== 
classid=clsid:5EEEA87D-160E-4A2D-8427-B6C333FEDA4D VIEWASTEXT>
</OBJECT>
</body>
<script language="vbscript"> 
Sub window_onload 
Set objProp = RTXAX.GetObject("Property") 
objProp.Value("RTXUsername") = "<%=name%>" 
objProp.Value("LoginSessionKey") = "<%=sessionKey%>"
<%
rtx.RTXConfig rc = rtx.RTXConfig.getInstance();
String serverAddr = rc.getProperty("serverAddr");
%>
objProp.Value("ServerAddress") = "<%=serverAddr%>" 
objProp.Value("ServerPort") = 8000 
RTXAX.Call 2, objProp 
window.close 
End Sub 
</script> 
</html>