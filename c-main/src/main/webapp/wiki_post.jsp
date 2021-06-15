<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.oa.fileark.plugin.*"%>
<%@ page import="com.redmoon.oa.fileark.plugin.base.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="java.util.Calendar" %>
<%@ page import="cn.js.fan.db.Paginator"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="util/jscalendar/calendar-setup.js"></script>
<script type="text/javascript" src="ckeditor/ckeditor.js" mce_src="ckeditor/ckeditor.js"></script>
<style type="text/css"> @import url("util/jscalendar/calendar-win2k-2.css"); </style>
<jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.fileark.Directory"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
// 安全验证
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN)));
	return;
}
int id = 0;

String dir_code = ParamUtil.get(request, "dir_code");
if (dir_code.equals(""))
	dir_code = Leaf.ROOTCODE;

String op = ParamUtil.get(request, "op");
op = "add";

if (op.equals("add")) {
	LeafPriv lp = new LeafPriv();
	lp.setDirCode(dir_code);
	/*
	if (!lp.canUserAppend(privilege.getUser(request))) {
		out.print(StrUtil.Alert_Back(privilege.MSG_INVALID));
		return;
	}
	*/
}

String info = ParamUtil.get(request, "info");
if (!info.equals("")) {
	out.print(StrUtil.Alert_Redirect("投稿成功！请等待管理员审核通过！", "index_wiki.jsp"));
	return;
}
%>
<title>创建词条 -<%=Global.AppName%></title>
<script src="inc/common.js"></script>
<script src="inc/map.js"></script>
<script language="JavaScript">
<!--
function submitForm(){
	if (document.addform.title.value.length == 0) {
		alert("<lt:Label res="res.label.webedit" key="input_artical_title"/>");
		document.addform.title.focus();			
		return false;
	}
	
	addform.submit();
}

function ClearAll() {
	document.addform.title.value=""
	oEdit1.putHTML(" ");
}
	
// 向编辑器插入指定代码 
function insertHTMLToEditor(value){ 
	var oEditor = CKEDITOR.instances.htmlcode;
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
		// var img = "<img alt='点击在新窗口中打开' style='cursor:hand' onclick=\"window.open('" + imgPath + "')\" onload=\"if(this.width>screen.width-333)this.width=screen.width-333\" src='" + imgPath + "'>";
		str += "<param name=\"menu\" value=\"true\"/>";
		str += "<param name=\"url\" value=\"" + imgPath + "\"/>";
		str += "<param name=\"autostart\" value=\"true\"/>";
		str += "<param name=\"loop\" value=\"true\"/>";
		str += "</object>";
		insertHTMLToEditor("<BR>" + str + "<BR>");
	}
	divTmpAttachId.innerHTML += "<input type=hidden name=tmpAttachId value='" + attachId + "'>";
	
	uploadMap.put(uploadSerialNo + "_attId", attachId);
	
}

var uploadMap=new Map();
var errFunc = function(response) {
    alert('Error ' + response.status + ' - ' + response.statusText);
	alert(response.responseText);
}

function doClear(response) {
	//alert(response.responseText);
}
function doGetProgress(response) {
	//alert(response.responseText);
	var items = response.responseXML.getElementsByTagName("item");
	for (var i=0; i<items.length; i++) {
		var item = items[i];
		var serialNo = item.getElementsByTagName("serialNo")[0].firstChild.data;
		var bytesRead = item.getElementsByTagName("bytesRead")[0].firstChild.data;
		var isFinish = item.getElementsByTagName("isFinish")[0].firstChild.data;
		var contentLength = item.getElementsByTagName("contentLength")[0].firstChild.data;
		
		if (bytesRead!=-1) {
			var sizeObj = uploadMap.get(serialNo + "_size");
			if (sizeObj==null) // 上传被取消时
				return;
			var fileSize = contentLength; // sizeObj.value;
			var per = bytesRead/fileSize;
						
			$("uploadStatusProgress_" + serialNo).style.width = per*100 + "%"
			if (isFinish!="true")
				$("uploadStatus_" + serialNo).innerHTML = "&nbsp;<a href='javascript:;' onclick=\"cancelUpload('" + serialNo + "')\">取消</a>";
			else
				$("uploadStatus_" + serialNo).innerHTML = "";
		}
		
		if (isFinish=="true") {
			var element=uploadMap.get(serialNo);
			window.clearTimeout(element.value);
			uploadMap.remove(serialNo);
			uploadMap.remove(serialNo + "_size");
			
			var str = "op=clear&serialNo=" + serialNo;
			var myAjax = new cwAjax.Request( 
				"ajax_upload_progress.jsp", 
				{
					method:"post",
					parameters:str,
					onComplete:doClear,
					onError:errFunc
				}
			);
			
			var element = uploadMap.get(serialNo + "_attId");
			if (element!=null){
				//$("uploadStatus_" + serialNo).innerHTML = "&nbsp;<a href='javascript:;' onclick=\"delUpload('" + element.value + "')\">删除</a>";
			}
		}		
	}
}
function doCancelUpload(response) {
	var items = response.responseXML.getElementsByTagName("item");
	// alert(response.responseText);
	
	for (var i=0; i<items.length; i++) {
		var item = items[i];
		var serialNo = item.getElementsByTagName("serialNo")[0].firstChild.data;
		window.status = serialNo + " is canceled";
	}
}
function cancelUpload(serialNo) {
	var element=uploadMap.get(serialNo);
	window.clearTimeout(element.value);
	uploadMap.remove(serialNo);
	uploadMap.remove(serialNo + "_size");
	
	//var ifrm = document.getElementById("uploadFrm");
	//ifrm.contentWindow.document.getElementById("filename").disabled=false; 	
	
	var str = "op=cancel&serialNo=" + serialNo;
	var myAjax = new cwAjax.Request( 
		"ajax_upload_progress.jsp", 
		{
			method:"post", 
			parameters:str, 
			onComplete:doCancelUpload,
			onError:errFunc
		}
	);
}
function getProgress(serialNo) {
	var str = "serialNo=" + serialNo;
	var myAjax = new cwAjax.Request( 
		"ajax_upload_progress.jsp", 
		{
			method:"post", 
			parameters:str, 
			onComplete:doGetProgress,
			onError:errFunc
		}
	);
}

function showProgress(serialNo, fileSize) {
	uploadMap.put(serialNo+"_size", fileSize);
	
	refreshProgress(serialNo, fileSize);
}

var i = 0;
function refreshProgress(serialNo, fileSize) {
	getProgress(serialNo);

	window.status = i;
	i++;
	
	var timeoutid = window.setTimeout("refreshProgress('" + serialNo + "'," + fileSize + ")", "2000");
	if (!uploadMap.containsKey(serialNo)) {
		uploadMap.put(serialNo, timeoutid);
	}
	// alert(uploadMap.get(serialNo).value);
}
//-->
</script>
<script type="text/javascript" src="FCKeditor/fckeditor.js"></script>
</head>
<body>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
  <TBODY>
    <TR>
      <TD class="tdStyle_1"><%
      if (op.equals("add")) {%>
        创建词条
        <%}%>
      </TD>
    </TR>
  </TBODY>
</TABLE>
<TABLE width="100%" BORDER=0 align="center" CELLPADDING=0 CELLSPACING=0>
  <TR valign="top" bgcolor="#FFFFFF">
    <TD width="" height="430" colspan="2">

	<form name="addform" action="fwebedit_do.jsp?action=wikiPost&dir_code=<%=dir_code%>" method="post" enctype="multipart/form-data">
          <table border="0" cellspacing="1" width="100%" cellpadding="2" align="center">
            <tr>
              <td align="left" valign="middle">作&nbsp;&nbsp;者</td>
              <td>
              <%
			  UserDb user = new UserDb();
			  user = user.getUserDb(privilege.getUser(request));
			  %>
              <input name="author" id="author" type="text" size=30 maxlength=100 value="<%=user.getRealName()%>" />
			  <input type="hidden" name="docType" value="0" />
			  <input type="hidden" name="color" value="" />
			  <input type="hidden" name="level" value="0" />
			  <input type="hidden" name="op" value="contribute" />
			  <input type="hidden" name="pageTemplateId" value=-1 />
			  <input type="hidden" name=isuploadfile value="true" /></td>
            </tr>
            <tr>
              <td align="left" valign="middle">标&nbsp;&nbsp;题</td>
              <td width="92%" bgcolor="#FFFFFF">
                  <input name="title" id="title" type="text" size=50 maxlength=100 />
                  <%
				  LeafPriv lp = new LeafPriv(dir_code);
				  if (lp.canUserExamine(privilege.getUser(request))) {%>
                  <input name="examine" value="<%=Document.EXAMINE_PASS%>" type="hidden" />
                  <%}else{%>
				  <input type="hidden" name="examine" value="0" />                  
                  <%}%>
              </td>
            </tr>
            <tr>
              <td align="left" valign="middle">目&nbsp;&nbsp;录</td>
              <td bgcolor="#FFFFFF">
			  <script>
			  var bcode="";
			  </script>
                <select name="dir_code" onChange="if(this.options[this.selectedIndex].value=='not'){alert(this.options[this.selectedIndex].text+' <lt:Label res="res.label.webedit" key="can_not_be_selected"/>'); this.value=bcode; return false;}">
                  <%
					Leaf lf = dir.getLeaf(dir_code);
					if (!lf.getCode().equals(Leaf.ROOTCODE) && !lf.getParentCode().equals(Leaf.ROOTCODE)) {
						lf = lf.getLeaf(lf.getParentCode()); // Leaf.CODE_WIKI);
					}
					DirectoryView dv = new DirectoryView(request, lf);
					dv.ShowDirectoryAsOptions(out, lf, lf.getLayer());
				  %>
                </select>
			  <script>
			  bcode=addform.dir_code.value;
			  addform.dir_code.value = "<%=dir_code%>";
			  </script>
              </td>
            </tr>
            <tr align="center">
              <td colspan="2" valign="top">
		<%
		String serialNo = RandomSecquenceCreator.getId(20);
		%>
		<iframe src="uploadimg.jsp?pageNum=1&uploadSerialNo=<%=serialNo%>" width="320px" height="28" frameborder="0" scrolling="no" style="float:left"></iframe>
        <table width="200px" border="0" cellpadding="0" cellspacing="0" style="float:left; height:5px; margin-left:5px;background-color:#eeeeee;height:10px;margin-top:10px">
          <tr>
            <td><table border="0" cellpadding="0" cellspacing="0" id="uploadStatusProgress_<%=serialNo%>" style="height:5px; background-color:#00CC66;width:0px">
                <tr>
                  <td></td>
                </tr>
              </table></td>
          </tr>
        </table>
        <div style="float:left;height:20px;margin-top:5px" id="uploadStatus_<%=serialNo%>"></div>
        </td>
        </tr><tr><td colspan="2">
        <div id="divTmpAttachId" style="display:none;"></div>
              
<textarea id="htmlcode" name="htmlcode">
</textarea>

<script>
CKEDITOR.replace('htmlcode');
</script> 
            </td>
            </tr>
            <tr>
              <td width="8%" align="right" bgcolor="#FFFFFF"><lt:Label res="res.label.webedit" key="notice"/></td>
              <td bgcolor="#FFFFFF">
			  <lt:Label res="res.label.webedit" key="enter_can_use"/>Shift+Enter			  </td>
            </tr>
            <tr>
              <td height="30" colspan=2 align=center >
			  <input class="btn" name="cmdok" type="button" value="确定" onClick="return submitForm()">&nbsp;&nbsp;
              <input class="btn" type="button" value="返回" onclick="window.history.back()" />
			</td></tr>
        </table>
    </form></TD>
  </TR>
</TABLE>
<iframe id="hideframe" name="hideframe" src="" width=0 height=0></iframe>
</body>
<script>
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
</script>
</html>