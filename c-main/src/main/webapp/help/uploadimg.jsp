<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "com.redmoon.oa.help.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="pvg" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
// 安全验证
if (!pvg.isUserLogin(request)) {
	out.print(StrUtil.p_center(SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN)));
	return;
}
int pageNum = ParamUtil.getInt(request, "pageNum", 1);
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<head>
<title>Upload Image</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" href="../common.css" type="text/css">
<script src="../inc/common.js"></script>
<%
String op = ParamUtil.get(request, "op");
%>
<script language="JavaScript" type="text/JavaScript">
function window_onload() {
}

function form1_onsubmit() {
	var fileName = form1.filename.value;
	var p = fileName.lastIndexOf(".");
	if (p==-1) {
		alert("文件无扩展名！");
		return false;
	}
	else {
		var len = fileName.length;
		var ext = fileName.substring(p + 1, len).toLowerCase();
		if (ext=="gif" || ext=="jpg" || ext=="png" || ext=="bmp" || ext=="swf" || ext=="mpg" ||  ext=="asf" || ext=="wma" || ext=="wmv" || ext=="avi" || ext=="mov" || ext=="mp3" || ext=="rm" || ext=="ra" || ext=="rmvb" || ext=="mid" || ext=="ram" || ext=="flv")
			;
		else {
			alert("文件类型非法，只允许gif、jpg、png、bmp、swf");
			return false;
		}
	}
}
</script>
</head>
<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onLoad="return window_onload()">
<%
String serialNo = ParamUtil.get(request, "uploadSerialNo");
String[] re = null;
if (op.equals("upload")) {
	try {
		DocumentMgr dm = new DocumentMgr();
		re = dm.uploadImg(application, request);
	}
	catch (ErrMsgException e) {
		out.print("<a href='uploadimg.jsp?uploadSerialNo=" + serialNo + "&pageNum=" + pageNum + "'>重新上传</a>&nbsp;&nbsp;" + e.getMessage());
		return;
	}
	if (re!=null) {
		// System.out.println(getClass() + " re[1]=" + re[1]);
	%>
		<script>
		window.parent.addImg("<%=re[0]%>", "<%=re[1]%>", "<%=re[2]%>");
		window.location.href = "uploadimg.jsp?pageNum=<%=pageNum%>&uploadSerialNo=<%=serialNo%>";
		</script>
	<%	// out.print("<a href='uploadimg.jsp'>返回继续添加图片</a>");
		return;
	}
}
%>
<table width="100%" align="center" class="p9">
  <form name="form1" enctype="MULTIPART/FORM-DATA" action="uploadimg.jsp?op=upload&uploadSerialNo=<%=serialNo%>" method="post" onSubmit="return form1_onsubmit()">
    <tr>
      <td height="28" valign="middle">
	  <div id="uploadDiv">
	  <script>
	  if (isIE())
		  document.write("图片、Flash、视频&nbsp;");
	  </script>
	  <input name=filename type=file id="filename" onChange="submitFile('<%=serialNo%>')">
	  <!--<input type=submit value="插入图片、Flash或视频">-->
	  <input name="pageNum" value="1" type="hidden"><%=cn.js.fan.security.Form.getTokenHideInput(request)%>
	  <!--[<a href='uploadimg_normal.jsp?pageNum=<%=pageNum%>'>传统方式</a>]-->
	  </div>
	  </td>
    </tr>
  </form>
</table>
</body>
<script>
function getFileSize(filePath){
   var image=new Image();
   return image.fileSize;
}

function submitFile(serialNo) {
	form1.submit();
	form1.filename.disabled = true;
	window.parent.showProgress(serialNo, getFileSize(""));
}
</script>
</html>