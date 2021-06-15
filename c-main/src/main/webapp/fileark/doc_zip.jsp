<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@page import="java.io.File"%>
<%@page import="com.redmoon.oa.fileark.FileBakUp"%>
<%@page import="cn.js.fan.web.Global"%>
<%@page import="com.redmoon.oa.ui.SkinMgr"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
// 本文件用于打包整个目录，暂时没有启用！
String priv="flow.init";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String userName = privilege.getUser(request);
String dirCode = ParamUtil.get(request,"dir_code");
String realPath = Global.getRealPath()+"upfile/zip";
String flag = ParamUtil.get(request,"flag");
if(flag.equals("")){
	flag = "0";
}

String op = ParamUtil.get(request,"op");
String zipfile = "";
if(!op.equals("rezip")){
	File zipFile = new File(realPath+"/"+dirCode+"/"+dirCode+".zip");
	if(!zipFile.exists()){
		zipfile = FileBakUp.zipDirFiles(dirCode,userName,flag,realPath+"/"+dirCode);
	}else{
		zipFile.delete();
		zipfile = FileBakUp.zipDirFiles(dirCode,userName,flag,realPath+"/"+dirCode);
	}
}else if(op.equals("rezip")){
	zipfile = FileBakUp.zipDirFiles(dirCode,userName,flag,realPath);
	out.println(StrUtil.Alert_Redirect("操作成功!","doc_zip.jsp?dir_code="+dirCode+"&flag="+flag));
}
//System.out.println(getClass()+"::::::::::::::::::::::::::::::::::::BBBBBBBBBBBBBBBBBBBBBBBBbb"+zipFile);
op = "";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>打包下载</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="inc/common.js"></script>
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="js/jquery.bgiframe.js"></script>
<script src="inc/flow_js.jsp"></script>
<script type="text/javascript" src="inc/livevalidation_standalone.js"></script>
<script language="JavaScript" type="text/JavaScript">
<!--
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

//-->
</script>
</head>
<body onload="onload()">

<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">打包下载</td>
    </tr>
  </tbody>
</table>
<form action="doc_zip.jsp" name="zipform" method="get">
<table align="center" class="percent98">
	
  	<tr> 
    	<td>
			<div style="line-height:1.5; margin-top:5px; margin-bottom:10px; padding:5px;">
			<div style="margin-right:20px;float:left; width:500px; height:40px;overflow:hidden">
			<img src="../images/zip.png"/>&nbsp;<a target=_blank href="../zip_getfile.jsp?fileDiskPath=<%=StrUtil.UrlEncode(zipfile)%>">点击此处下载</a>
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="checkbox" id="ckb" onchange="myck()" />包含子目录&nbsp;&nbsp;<input type="submit" value="重新打包"  /></div>
			</div>
			<input type="hidden" name="dir_code" value="<%=dirCode%>" />
			<input type="hidden" id="flag" name="flag" value="<%=flag%>" />
			<input type="hidden" id="op" name="op" value="rezip" />
        </td>
	</tr>
</table>
</form>
<script>
	function myck(){
		var ckb =  document.getElementById("ckb");
		if(ckb.checked){
			document.getElementById("flag").value = "2";
		}else{
			document.getElementById("flag").value = "1";
		}
	}
</script>
</body>
</html>