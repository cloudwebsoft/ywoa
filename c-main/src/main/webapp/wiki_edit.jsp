<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.oa.fileark.plugin.*"%>
<%@ page import="cn.js.fan.module.cms.plugin.wiki.*"%>
<%@ page import="com.redmoon.oa.fileark.plugin.base.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.forum.Config"%>
<%@ page import="java.util.Calendar" %>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="org.json.*"%>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<%
int id = ParamUtil.getInt(request, "id");
Document doc = docmanager.getDocument(id);

WikiDocumentDb wdd = new WikiDocumentDb();
wdd = wdd.getWikiDocumentDb(id);
int pageNum = wdd.getBestPageNum();
if (pageNum==-1) {
	out.print(SkinUtil.makeErrMsg(request, "文章页不存在或审核未通过！"));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("wikiEdit")) {
	WikiDocumentAction ada = new WikiDocumentAction();
	int re = 0;
	try {
		re = ada.edit(application, request);
	}
	catch (ErrMsgException e) {
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		out.print(json);
		return;
	}
	// response.setContentType("application/x-json"); 

	if (re==-1) {
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("msg", "操作失败！");
		out.print(json);
	}
	else {
		JSONObject json = new JSONObject();
		json.put("ret", re);
		if (re==WikiDocUpdateDb.CHECK_STATUS_PASSED) {
			json.put("msg", "操作成功！");
		}
		else if (re==WikiDocUpdateDb.CHECK_STATUS_WAIT) {
			json.put("msg", "编辑成功，请等待审核通过！");
		}
		else
			json.put("msg", "编辑成功！");
		out.print(json);
	}
	
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=doc.getTitle()%></title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="util/jscalendar/calendar-setup.js"></script>
<style type="text/css">
@import url("util/jscalendar/calendar-win2k-2.css");
</style>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.fileark.Directory"/>
<%
String dir_code = doc.getDirCode();
Leaf leaf = dir.getLeaf(dir_code);
String dir_name = leaf.getName();

request.setAttribute("docId", new Integer(id));
%>
<script src="inc/common.js"></script>
<script src="inc/map.js"></script>

<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-migrate-1.2.1.min.js"></script>

<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="js/jquery-ui/jquery-ui-1.10.4.min.js"></script>

<script src="js/jquery.form.js"></script>
<script language="JavaScript">
<!--
var id="<%=doc.getID()%>";
var op = "<%=op%>";

function ClearAll() {
	document.addform.title.value="";
	oEdit1.putHTML(" ");
}
	
// 编辑文件
function editdoc(doc_id, file_id){
	addform.redmoonoffice.AddField("doc_id", doc_id);
	addform.redmoonoffice.AddField("file_id", file_id);
	addform.redmoonoffice.Open("http://<%=Global.server%>:<%=Global.port%>/<%=Global.virtualPath%>/word_get.jsp?doc_id=" + doc_id + "&file_id=" + file_id);
}

function insertHTMLToEditor(value) {
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
	//o("uploadStatus_" + uploadSerialNo).innerHTML = "&nbsp;<a href='javascript:;' onclick=\"delUpload('" + uploadSerialNo + "')\">删除</a>";
}

var attachCount = 1;

function AddAttach() {
	updiv.insertAdjacentHTML("BeforeEnd", "<div><input type='file' name='attachment" + attachCount + "'></div>");
	attachCount += 1;
}

function selectNode(code, name) {
	addform.dir_code.value = code;
	o("dirNameSpan").innerHTML = name;
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
	var items = response.responseXML.getElementsByTagName("item");
	// alert(response.responseText);
	
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
						
			o("uploadStatusProgress_" + serialNo).style.width = per*100 + "%"
			if (isFinish!="true")
				o("uploadStatus_" + serialNo).innerHTML = "&nbsp;<a href='javascript:;' onclick=\"cancelUpload('" + serialNo + "')\">取消</a>";
			else
				o("uploadStatus_" + serialNo).innerHTML = "";
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
				//o("uploadStatus_" + serialNo).innerHTML = "&nbsp;<a href='javascript:;' onclick=\"delUpload('" + element.value + "')\">删除</a>";
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
<script type="text/javascript" src="ckeditor/ckeditor.js" mce_src="ckeditor/ckeditor.js"></script>
</head>
<body>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
  <TBODY>
    <TR>
      <TD width="79%" class="tdStyle_1"><%
			Leaf lf = leaf;
			String navstr = "";
			String parentcode = lf.getParentCode();
			Leaf plf = new Leaf();
			while (!parentcode.equals("root")) {
				plf = plf.getLeaf(parentcode);
				if (plf==null || !plf.isLoaded())
					break;
				if (plf.getType()==Leaf.TYPE_LIST && plf.getChildCount()!=0)
					navstr = "<a href='cms/dir_frame.jsp?root_code=" + StrUtil.UrlEncode(plf.getCode()) + "'>" + plf.getName() + "</a>&nbsp;>>&nbsp;" + navstr;
				else if (plf.getType()==Leaf.TYPE_LIST && plf.getChildCount()==0)
					navstr = "<a href='cms/document_list_m.jsp?dir_code=" + StrUtil.UrlEncode(plf.getCode()) + "'>" + plf.getName() + "</a>&nbsp;>>&nbsp;" + navstr;
				else if (plf.getType()==Leaf.TYPE_NONE) {
					navstr = "<a href='cms/dir_frame.jsp?root_code=" + StrUtil.UrlEncode(plf.getCode()) + "'>" + plf.getName() + "</a>&nbsp;>>&nbsp;" + navstr;				
				}
				else
					navstr = "<a href='cms/document_list_m.jsp?dir_code=" + StrUtil.UrlEncode(plf.getCode()) + "'>" + plf.getName() + "</a>&nbsp;>>&nbsp;" + navstr;
			
				parentcode = plf.getParentCode();
			}
			out.print(navstr);
				
				  if (op.equals("add")) {%>
        <a href="cms/document_list_m.jsp?dir_code=<%=StrUtil.UrlEncode(dir_code)%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>"><%=dir_name%></a>&nbsp;
        <%}else{
					Leaf dlf = new Leaf();
					if (doc!=null) {
						dlf = dlf.getLeaf(doc.getDirCode());
					}
					if (doc!=null) {
						if (dlf.getType()!=Leaf.TYPE_DOCUMENT) {
					%>
        <a href="fileark/wiki_list.jsp?dir_code=<%=StrUtil.UrlEncode(dlf.getCode())%>"><%=dlf.getName()%></a>
        <%	}else{
						%>
        <%=dlf.getName()%>
        <%
						}
					}else{%>
        <%=dir_name%>
        <%}%>
      <%}%></TD>
    </TR>
  </TBODY>
</TABLE>
<br>
<form id="wikiForm" name="wikiForm" action="wiki_edit_do.jsp?id=<%=doc.getId()%>&op=wikiEdit" method="post" enctype="multipart/form-data">
  <table class="eTab" border="0" cellspacing="1" width="100%" cellpadding="2" align="center">
    <thead>
      <tr align="center">
        <td height="20" colspan=2 align=center style="font-size:16px"><b> <%=doc!=null?doc.getTitle():""%>&nbsp;
          <input type="hidden" name="id" value="<%=doc.getID()%>">
          <%=doc!=null?"(id:"+doc.getID()+")":""%>
          <%if (op.equals("add")) {%>
          <lt:Label res="res.label.webedit" key="add_content_to"/>
          <%}%>
          </b></td>
      </tr>
    </thead>
    <tr>
      <td colspan="2" align="left" valign="middle">修改原因
      <input id="reason" name="reason" value="" />
      <input type="hidden" name="isuploadfile" value="false">
      </td>
    </tr>
    <tr>
      <td colspan="2" valign="top"><%
			  String serialNo = RandomSecquenceCreator.getId(20);
			  %>
        <iframe id="uploadFrm" src="uploadimg.jsp?pageNum=1&uploadSerialNo=<%=serialNo%>" width="320px" height="28" frameborder="0" scrolling="no" style="float:left"></iframe>
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
        <div id="divTmpAttachId" style="display:none"></div>
</pre>
<div style="clear:both">
<textarea class="ckeditor" id="htmlcode" name="htmlcode">
<%
	out.print(doc.getDocContent(pageNum).getContent());
%>
</textarea>
</div>
<script>
// CKEDITOR.replace('htmlcode');
</script> 
	</td>
    </tr>
    <tr>
      <td height="30" colspan=2 align=center>
        <input type="button" value="确定" class="btn" onclick="$('#wikiForm').submit()">&nbsp;&nbsp;
        <input type="button" value="返回" class="btn" onclick="window.history.back()">
        </td>
    </tr>
  </table>
</form>
<div id="result"></div>
</body>
<script>
function showResponse(data)  {
	data = data.replace("<HEAD></HEAD>", "");
	data = data.replace("<BODY>", "");
	data = data.replace("</BODY>", "");
	data = jQuery.parseJSON(data);
	if (data.ret=="1") {
		$("#result").html(data.msg);
	}
	else {
		$("#result").html(data.msg);
	}
	$("#result").dialog({title:"提示", modal: true, buttons: { "确定": function() { $(this).dialog("close"); window.location.href="wiki_show.jsp?id=<%=id%>"}}, closeOnEscape: true, draggable: true, resizable:true });
}

function showError(pRequest, pStatus, pErrorText) {
	alert('pStatus='+pStatus+'\r\n\r\n'+'pErrorText='+pErrorText);
}

$(document).ready(function() { 
    var options = { 
        //target:        '#output2',   // target element(s) to be updated with server response 
        //beforeSubmit:  function() {alert('d');},  // pre-submit callback 
        success:       showResponse,  // post-submit callback 
		error: 		   showError,

        // other available options: 
        //url:       url         // override for form's 'action' attribute 
        //type:      type        // 'get' or 'post', override for form's 'method' attribute 
        dataType:  'text'   // 'xml', 'script', or 'json' (expected server response type)  表单为multipart/form-data即上传文件时，json无法解析
        //clearForm: true        // clear all form fields after successful submit 
        //resetForm: true        // reset the form after successful submit 
 
        // $.ajax options can be used here too, for example: 
        //timeout:   3000 
    }; 

    // bind to the form's submit event 
    $('#wikiForm').submit(function() {
		// ajaxForm提交前，必须先提取赋值
		$('#htmlcode').val(CKEDITOR.instances.htmlcode.getData());
		
        $(this).ajaxSubmit(options);
        return false; 
    }); 
});
</script>
</html>