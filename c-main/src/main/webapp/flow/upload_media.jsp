<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<jsp:useBean id="pvg" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
// 安全验证
if (!pvg.isUserLogin(request)) {
	out.print(StrUtil.p_center(SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN)));
	return;
}

int flowId = ParamUtil.getInt(request, "flowId");
String fieldCode = ParamUtil.get(request, "fieldCode");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"><head>
<title>Upload Media</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script src="<%=request.getContextPath()%>/inc/common.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%
String op = ParamUtil.get(request, "op");
%>
<script language="JavaScript" type="text/JavaScript">
function findObj(theObj, theDoc)
{
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++) 
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}

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

function insertHTMLToEditor(value) {
	var oEditor = window.parent.CKEDITOR.instances.<%=fieldCode%>;
	if ( oEditor.mode == 'wysiwyg' ) {
		oEditor.insertHtml( value );
	}
	else
		alert( '请切换编辑器至设计视图!' );
}

function addImg(attachId, imgPath, uploadSerialNo) {
	var ext = imgPath.substring(imgPath.length-3, imgPath.length);
	if (ext=="swf") {
		 var str = '<object classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" codebase="http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0" width="200" height="150">';
		 str += '<param name="movie" value="' + imgPath + '"><param name="quality" value="high">';
		 str += '</object>';
		insertHTMLToEditor("<BR>" + str + "<BR>");
	}
	else if (ext=="gif" || ext=="jpg" || ext=="png" || ext=="bmp") {
		var img = "<img alt='点击在新窗口中打开' style='cursor:hand' onclick=\"window.open('" + imgPath + "')\" onload=\"if(this.width>screen.width-333)this.width=screen.width-333\" src='" + imgPath + "'>";
		insertHTMLToEditor("<BR>" + img + "<BR>");
	}
	else if (ext=="flv") {
	　　insertHTMLToEditor("<embed width=320 height=260 flashvars=\"file="　+　imgPath　+　"\" allowfullscreen=\"true\" allowscriptaccess=\"always\" bgcolor=\"#ffffff\" src=\"<%=request.getContextPath()%>/ckeditor/plugins/cwvideo/jwplayer.swf\"></embed>");		
	}
	else {
		str = "<object classid=\"clsid:22D6F312-B0F6-11D0-94AB-0080C74C7E95\" id=\"MediaPlayer1\" width=\"428\" height=\"330\">";
		str += "<param name=\"menu\" value=\"true\"/>";
		str += "<param name=\"Filename\" value=\"" + imgPath + "\"/>";
		str += "<param name=\"AutoStart\" value=\"1\"/>";
		str += "<param name=\"AutoRewind\" value=\"0\"/>";
		str += "</object>";
		insertHTMLToEditor("<BR>" + str + "<BR>");
	}
	
	//$("uploadStatus_" + uploadSerialNo).innerHTML = "&nbsp;<a href='javascript:;' onclick=\"delUpload('" + uploadSerialNo + "')\">删除</a>";
}
</script>
</head>
<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onLoad="return window_onload()">
<%
String serialNo = ParamUtil.get(request, "uploadSerialNo");
// String serialNo = RandomSecquenceCreator.getId(20);

String[] re = null;
if (op.equals("upload")) {
	try {
		DocumentMgr dm = new DocumentMgr();
		re = dm.uploadMedia(application, request);
	}
	catch (ErrMsgException e) {
		out.print("<a href='upload_media.jsp?flowId=" + flowId + "&fieldCode=" + fieldCode + "&uploadSerialNo=" + serialNo + "'>重新上传</a>&nbsp;&nbsp;" + e.getMessage());
		return;
	}
	if (re!=null) {
		// System.out.println(getClass() + " re[1]=" + re[1]);
	%>
		<script>
		addImg("<%=re[0]%>", "<%=re[1]%>", "<%=re[2]%>");
		window.location.href = "upload_media.jsp?flowId=<%=flowId%>&fieldCode=<%=fieldCode%>&uploadSerialNo=<%=serialNo%>";
		</script>
	<%	// out.print("<a href='uploadimg.jsp'>返回继续添加图片</a>");
		return;
	}
}
%>
<table width="100%" align="center" class="p9">
  <form name="form1" enctype="MULTIPART/FORM-DATA" action="upload_media.jsp?op=upload&flowId=<%=flowId%>&fieldCode=<%=fieldCode%>&uploadSerialNo=<%=serialNo%>" method="post" onSubmit="return form1_onsubmit()">
    <tr> 
      <td height="28" valign="middle">
	  <div id="uploadDiv">
	  图片、Flash、视频
	  <input name=filename style="width:200px" type=file id="filename" onChange="submitFile('<%=serialNo%>')">
      <input name="flowId" value="<%=flowId%>" type="hidden" />
	  </div>
	  </td>
    </tr>
  </form>
</table>
</body>
<script>
function submitFile(serialNo) {
	form1.submit();
	form1.filename.disabled = true;
	window.parent.showProgress(serialNo);
}
</script>
</html>