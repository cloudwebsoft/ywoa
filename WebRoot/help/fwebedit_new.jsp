<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.help.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="java.util.Calendar" %>
<%@ page import="cn.js.fan.db.Paginator"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link href="../fileark/default.css" rel="stylesheet" type="text/css">
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../inc/map.js"></script>
<script src="../inc/livevalidation_standalone.js"></script>
<script>
var isLeftMenuShow = true;
function closeLeftMenu() {
	if (isLeftMenuShow) {
		window.parent.setCols("0,*");
		isLeftMenuShow = false;
		btnName.innerHTML = "打开菜单";
	}
	else {
		window.parent.setCols("200,*");
		isLeftMenuShow = true;
		btnName.innerHTML = "关闭菜单";
	}
}

function onAddFile() {
}
</script>
<%
response.setHeader("Pragma","No-cache");
response.setHeader("Cache-Control","no-cache");
response.setDateHeader("Expires", 0);
%>
<jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.help.DocumentMgr"/>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.help.Directory"/>
<%
String dir_code = ParamUtil.get(request, "dir_code");
String dir_name = ParamUtil.get(request, "dir_name");
int id = 0;

Privilege privilege = new Privilege();

String correct_result = "操作成功！";
Document doc = null;
Document template = null;

String op = ParamUtil.get(request, "op");
if (op.equals("edit")) {
	id = ParamUtil.getInt(request, "id");
	doc = docmanager.getDocument(id);
	dir_code = doc.getDirCode();
}

Leaf leaf = dir.getLeaf(dir_code);

String strtemplateId = ParamUtil.get(request, "templateId");
int templateId = Document.NOTEMPLATE;
if (!strtemplateId.trim().equals("")) {
	if (StrUtil.isNumeric(strtemplateId))
		templateId = Integer.parseInt(strtemplateId);
}
if (templateId==Document.NOTEMPLATE) {
	templateId = leaf.getTemplateId();
}

if (templateId!=Document.NOTEMPLATE) {
	template = docmanager.getDocument(templateId);
}

String action = ParamUtil.get(request, "action");
if (op.equals("add")) {
	if (action.equals("selTemplate")) {
		int tid = ParamUtil.getInt(request, "templateId");
		template = docmanager.getDocument(tid);
	}
}
else if (op.equals("edit")) {
	try {
		if (action.equals("selTemplate")) {
			int tid = ParamUtil.getInt(request, "templateId");
			doc.setTemplateId(tid);
			doc.updateTemplateId();
		}
		if (doc!=null) {
			template = doc.getTemplate();
		}
	} catch (ErrMsgException e) {
		out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
	
	if (action.equals("changeAttachOrders")) {
		int attachId = ParamUtil.getInt(request, "attachId");
		String direction = ParamUtil.get(request, "direction");
		// 取得第一页的内容
		DocContent dc = new DocContent();
		dc = dc.getDocContent(id, 1);
		dc.moveAttachment(attachId, direction);		
	}
}
if (op.equals("editarticle")) {
	op = "edit";
	try {
		doc = docmanager.getDocumentByCode(request, dir_code, privilege);
		dir_code = doc.getDirCode();
	} catch (ErrMsgException e) {
		out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
}

if (doc!=null) {
	id = doc.getID();
	Leaf lfn = new Leaf();
	lfn = lfn.getLeaf(doc.getDirCode());
	dir_name = lfn.getName();
}
%>
<title><%=doc!=null?doc.getTitle():""%></title>
<style type="text/css">
<!--
td {font-family: "Arial", "Helvetica", "sans-serif"; font-size: 14px; font-style: normal; line-height: 150%; font-weight: normal}
.style2 {color: #FF3300}
-->
</style>
<script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="ckeditor/ckeditor.js"></script>
<script type="text/javascript" src='../ckeditor/formpost.js'></script>
<script language="JavaScript">
<!--
<%
if (doc!=null) {
	out.println("var id=" + doc.getID() + ";");
}
%>
var op = "<%=op%>";

function showvote(isshow)
{
	if (addform.isvote.checked)
	{
		divVote.style.display = "";
	}
	else
	{
		divVote.style.display = "none";		
	}
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
	//$("uploadStatus_" + uploadSerialNo).innerHTML = "&nbsp;<a href='javascript:;' onclick=\"delUpload('" + uploadSerialNo + "')\">删除</a>";
}

var attachCount = 1;

function AddAttach() {
	updiv.insertAdjacentHTML("BeforeEnd", "<div><input type='file' name='attachment" + attachCount + "'></div>");
	attachCount += 1;
}

function selectNode(code, name) {
	addform.dir_code.value = code;
	$("dirNameSpan").innerHTML = name;
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

	// window.status = i;
	i++;
	
	var timeoutid = window.setTimeout("refreshProgress('" + serialNo + "'," + fileSize + ")", "2000");
	if (!uploadMap.containsKey(serialNo)) {
		uploadMap.put(serialNo, timeoutid);
	}
	// alert(uploadMap.get(serialNo).value);
}
//-->
</script>
</head>
<body>
<TABLE width="98%" BORDER=0 align="center" CELLPADDING=0 CELLSPACING=0>
  <TR valign="top" bgcolor="#FFFFFF">
    <TD width="" height="430" colspan="2" style="background-attachment: fixed; background-image: url(images/bg_bottom.jpg); background-repeat: no-repeat">
          <TABLE cellSpacing=0 cellPadding=0 width="100%">
            <TBODY>
              <TR>
                <TD width="90%" class=head>
				<%
				String pageUrl = "document_list_m.jsp?";
				
				if (op.equals("add")) {
				%>
					添加&nbsp;-&nbsp;<a href="<%=pageUrl%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>">&nbsp;<%=dir_name%></a>
				<%}else{%>
					修改&nbsp;-
<%
					Leaf dlf = new Leaf();
					if (doc!=null) {
						dlf = dlf.getLeaf(doc.getDirCode());
					}
					if (doc!=null && dlf.getType()==2) {%>
						<a href="<%=pageUrl%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>"><%=dlf.getName()%></a>
					<%}else{%>
						<%=dir_name%>
					<%}%>
				<%}%>
		<script>
		if (typeof(window.parent.leftFileFrame)=="object"){
			var btnN = "关闭菜单";
			if (window.parent.getCols()!="200,*"){
				btnN = "打开菜单";
				isLeftMenuShow=false;
			}
			document.write("&nbsp;&nbsp;<a href=\"javascript:closeLeftMenu()\"><span id=\"btnName\">");
			document.write(btnN);
			document.write("</span></a>");
		}
		</script>				
				</TD>
                <TD width="10%" align="right" class=head><a href="fwebedit.jsp?op=<%=op%>&id=<%=id%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>">高级方式</a>&nbsp;&nbsp;</TD>
              </TR>
            </TBODY>
          </TABLE>
	<form name="addform" action="fwebedit_do.jsp?action=fckwebedit_new" method="post" enctype="MULTIPART/FORM-DATA">
          <table border="0" cellspacing="1" width="100%" cellpadding="2" align="center">
            <tr align="center" bgcolor="#F2F2F2">
              <td height="20" colspan=2 align=center><b><%=doc!=null?doc.getTitle():""%></b>&nbsp;<input type="hidden" name=isuploadfile value="true">
			  <input type="hidden" name=id value="<%=doc!=null?""+doc.getID():""%>">
<%=doc!=null?"(id:"+doc.getID()+")":""%>
<%if (doc!=null) {%>
<!--( <a href="fileark/comment_m.jsp?doc_id=<%=doc.getID()%>">管理评论</a> )-->
<%}%></td>
            </tr>
            <tr>
              <td colspan="2" align="left" valign="middle">标&nbsp;&nbsp;&nbsp;题：
                <input name="title" id=me type="text" size=50 maxlength=100 value="<%=doc!=null?doc.getTitle():""%>">                  
                <script>
                var title = new LiveValidation('title');
                title.add(Validate.Presence);
                </script>
                  作者：
                  <%
			  String userName = "";
			  userName = (doc!=null)?doc.getAuthor():privilege.getUser(request);
			  UserDb ud = new UserDb();
			  ud = ud.getUserDb(userName);
			  if (ud!=null && ud.isLoaded())
			  	userName = ud.getRealName();
			  %>
              <input name="author" id="author" type="text" size=10 maxlength=100 value="<%=userName%>" readonly>
              <input type="hidden" name="op" value="<%=op%>">
			  </td>
            </tr>
            <tr>
              <td colspan="2" align="left" valign="middle">关键字：
                <input title="请用&quot;，&quot;号分隔" name="keywords" id=keywords type="text" size=20 maxlength=100 value="<%=StrUtil.getNullStr(doc==null?dir_name:doc.getKeywords())%>">             
			    <input type="hidden" name="isRelateShow" value="1">
			    <%
			String strChecked = "";
			if (doc!=null) {
				if (doc.getCanComment())
					strChecked = "checked";
			}
			else
				strChecked = "checked";
			%>
                <input type="checkbox" name="canComment" value="1" <%=strChecked%>>
允许评论
<%if (doc!=null) {%>
[<a href="fileark/comment_m.jsp?doc_id=<%=doc.getID()%>">管理评论</a>]
<%}%>
<%
LeafPriv lp = new LeafPriv(dir_code);
if (lp.canUserExamine(privilege.getUser(request))) {
%>&nbsp;&nbsp; <span class="style2">审核</span>
<select id="examine" name="examine">
  <option value="<%=Document.EXAMINE_PASS%>">已通过</option>
  <option value="<%=Document.EXAMINE_NOT%>">未审核</option>
  <option value="<%=Document.EXAMINE_NOTPASS%>">未通过</option>
  <option value="<%=Document.EXAMINE_DUSTBIN%>">回收站</option>
</select>
<%if (doc!=null) {%>
<script>
		addform.examine.value = "<%=doc.getExamine()%>";
		</script>
<%}%>
<%}else{%>
<input type="hidden" name="examine" value="<%=(doc!=null)?""+doc.getExamine():"0"%>">
<%}%>
<%
String checknew = "";
if (doc!=null && doc.getIsNew()==1)
	checknew = "checked";
%>
<!--
<input type="checkbox" name="isNew" value="1" <%=checknew%>>
<img src="images/i_new.gif" width="18" height="7">
--></td>
            </tr>
            
            <tr align="left">
              <td colspan="2" valign="middle">
			  <%if (doc!=null) {%>
				  <script>
				  var bcode = "<%=doc.getDirCode()%>";
				  </script>目&nbsp;&nbsp;&nbsp;&nbsp;录：
			      <%
				  if (leaf.getType()==leaf.TYPE_DOCUMENT) {
					out.print("<input name=dir_code type=hidden value='" + doc.getDirCode() + "'>" + leaf.getName());
				  }else{
				  %>
					<select id="dir_code" name="dir_code" onChange="if(this.options[this.selectedIndex].value=='not'){alert(this.options[this.selectedIndex].text+' 不能被选择！'); this.value=bcode; return false;}">
					<option value="not" selected>请选择目录</option>
					<%
					Leaf lf = dir.getLeaf("root");
					DirectoryView dv = new DirectoryView(lf);
					dv.ShowDirectoryAsOptions(out, lf, lf.getLayer());
					%>
					</select>
						<script>
						addform.dir_code.value = "<%=doc.getDirCode()%>";
						</script>
						&nbsp;( <span class="style3">蓝色</span>表示可选 )
				  <%}%>			  
				<%}else{%>
					<input type=hidden name="dir_code" value="<%=dir_code%>">
				<%}%>
				<input name="templateId" class="btn" value="<%=templateId%>" type=hidden>
				排序号：&nbsp;
				<input name="level" value="<%=doc!=null?doc.getLevel():"0"%>" size="2" />
				(<a href="javascript:;" onclick="o('level').value=100">置顶</a>)				</td>
            </tr>
            
            <tr align="left" bgcolor="#F2F2F2">
              <td colspan="2" valign="middle" bgcolor="#FFFFFF">颜&nbsp;&nbsp;&nbsp;&nbsp;色：
                <select name="color">
                <option value="" style="COLOR: black" selected>显示颜色</option>
                <option style="BACKGROUND: #000088" value="#000088"></option>
                <option style="BACKGROUND: #0000ff" value="#0000ff"></option>
                <option style="BACKGROUND: #008800" value="#008800"></option>
                <option style="BACKGROUND: #008888" value="#008888"></option>
                <option style="BACKGROUND: #0088ff" value="#0088ff"></option>
                <option style="BACKGROUND: #00a010" value="#00a010"></option>
                <option style="BACKGROUND: #1100ff" value="#1100ff"></option>
                <option style="BACKGROUND: #111111" value="#111111"></option>
                <option style="BACKGROUND: #333333" value="#333333"></option>
                <option style="BACKGROUND: #50b000" value="#50b000"></option>
                <option style="BACKGROUND: #880000" value="#880000"></option>
                <option style="BACKGROUND: #8800ff" value="#8800ff"></option>
                <option style="BACKGROUND: #888800" value="#888800"></option>
                <option style="BACKGROUND: #888888" value="#888888"></option>
                <option style="BACKGROUND: #8888ff" value="#8888ff"></option>
                <option style="BACKGROUND: #aa00cc" value="#aa00cc"></option>
                <option style="BACKGROUND: #aaaa00" value="#aaaa00"></option>
                <option style="BACKGROUND: #ccaa00" value="#ccaa00"></option>
                <option style="BACKGROUND: #ff0000" value="#ff0000"></option>
                <option style="BACKGROUND: #ff0088" value="#ff0088"></option>
                <option style="BACKGROUND: #ff00ff" value="#ff00ff"></option>
                <option style="BACKGROUND: #ff8800" value="#ff8800"></option>
                <option style="BACKGROUND: #ff0005" value="#ff0005"></option>
                <option style="BACKGROUND: #ff88ff" value="#ff88ff"></option>
                <option style="BACKGROUND: #ee0005" value="#ee0005"></option>
                <option style="BACKGROUND: #ee01ff" value="#ee01ff"></option>
                <option style="BACKGROUND: #3388aa" value="#3388aa"></option>
                <option style="BACKGROUND: #000000" value="#000000"></option>
                </select>
                <%if (doc!=null) {%>
                <script>
				addform.color.value = "<%=StrUtil.getNullStr(doc.getColor())%>";
				  </script>
                <%}%>
                <%
				  String strExpireDate = "";
				  if (doc!=null) {
				  	strExpireDate = DateUtil.format(doc.getExpireDate(), "yyyy-MM-dd");
				  %>
                <input type="checkbox" name="isBold" value="true" <%=doc.isBold()?"checked":""%> >
                <%}else{%>
                <input type="checkbox" name="isBold" value="true" >
                <%}%>
                标题加粗 
                  &nbsp;到期时间：
                <input type="text" id="expireDate" name="expireDate" size="10" value="<%=strExpireDate%>">
              </td>
            </tr>
            <tr align="center">
              <td colspan="2" valign="top">
<%
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
        <div style="clear:both">              
<textarea class="ckeditor" id="htmlcode" name="htmlcode"><%
	if (template!=null) {
		out.print(template.getContent(1));
    }
	else if (!op.equals("add")) {
		out.print(doc.getDocContent(1).getContent());
	}
%></textarea>
</div>
<script>
// CKEDITOR.replace('htmlcode');
/*
CKEDITOR.replace('htmlcode',
	{skin : 'kama'
	});
*/
</script> 
				</td>
            </tr>
            <tr>
              <td width="10%" align="left">提示：</td>
              <td width="90%">
			  回车可用Shift+Enter	
		      <%
Calendar cal = Calendar.getInstance();
String year = "" + (cal.get(cal.YEAR));
String month = "" + (cal.get(cal.MONTH) + 1);
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String filepath = cfg.get("file_folder") + "/" + year + "/" + month;
%>
		      <input type="hidden" name="filepath" value="<%=filepath%>"></td>
            </tr>
            <tr>
              <td align="left" valign="top">网盘文件：</td>
              <td>
              <a href="javascript:;" onClick="openWin('../netdisk/netdisk_frame.jsp?mode=select', 800, 600)">选择文件</a>
			  <div id="netdiskFilesDiv" style="line-height:1.5"></div>      
              </td>
            </tr>
            <tr>
              <td align="left" valign="top">附件：</td>
              <td><input type="file" name="attachment0">
                <input class="btn" type=button onClick="AddAttach()" value="增加">
                <div id="updiv"></div></td>
            </tr>
            <%
			if (doc!=null) {
			%>
            <tr>
              <td height="25" colspan=2 align="center" bgcolor="#FFFFFF">
		<%
        com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();  
		if (!cfg.get("isUseNTKO").equals("true")) {
		%>
        <table id="rmofficeTable" name="rmofficeTable" style="display:none;margin-top:10px;margin-bottom:10px" width="29%"  border="0" align="center" cellpadding="0" cellspacing="1" bgcolor="#CCCCCC">
            <tr>
              <td height="22" align="center" bgcolor="#eeeeee"><strong>&nbsp;编辑Office文件</strong></td>
            </tr>
            <tr>
              <td align="center"><div style="width:400px;height:43"><object id="redmoonoffice" classid="CLSID:D01B1EDF-E803-46FB-B4DC-90F585BC7EEE" codebase="../activex/cloudym.CAB#version=1,2,0,1" width="316" height="43" viewastext="viewastext">
                  <param name="Encode" value="utf-8" />
                  <param name="BackColor" value="0000ff00" />
                  <param name="Server" value="<%=Global.server%>" />
                  <param name="Port" value="<%=Global.port%>" />
                  <!--设置是否自动上传-->
                  <param name="isAutoUpload" value="1" />
                  <!--设置文件大小不超过1M-->
                  <param name="MaxSize" value="<%=Global.FileSize%>" />
                  <!--设置自动上传前出现提示对话框-->
                  <param name="isConfirmUpload" value="1" />
                  <!--设置IE状态栏是否显示信息-->
                  <param name="isShowStatus" value="0" />
                  <param name="PostScript" value="<%=Global.virtualPath%>/fileark/upload_office_file.jsp" />
                  <param name="Organization" value="<%=license.getCompany()%>" />
                  <param name="Key" value="<%=license.getKey()%>" />
                </object></div>
                <!--<input name="remsg" type="button" onclick='alert(redmoonoffice.ReturnMessage)' value="查看上传后的返回信息" />--></td>
            </tr>
          </table>
          <%}%>
			  <%
				  Vector attachments = doc.getAttachments(1);
				  Iterator ir = attachments.iterator();
				  while (ir.hasNext()) {
				  	Attachment am = (Attachment) ir.next(); %>
					<table width="100%"  border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td width="8%" align="center"><img src="../images/attach.gif"></td>
                        <td width="92%" align="left">
                        <input name="attach_name<%=am.getId()%>" value="<%=am.getName()%>" size="30">
                        <a href="javascript:changeAttachName('<%=am.getId()%>', '<%=doc.getID()%>', '<%="attach_name"+am.getId()%>')">重命名</a>&nbsp;&nbsp;
                        <a href="javascript:delAttach('<%=am.getId()%>', '<%=doc.getID()%>')">删除</a>&nbsp;&nbsp;
                        <!--<a target=_blank href="<%=am.getVisualPath() + "/" + am.getDiskName()%>">查看</a>&nbsp;&nbsp;-->
                        <a target=_blank href="fileark/getfile.jsp?docId=<%=doc.getID()%>&attachId=<%=am.getId()%>">下载</a>&nbsp;&nbsp;                        
						<%if (StrUtil.getFileExt(am.getDiskName()).equals("doc") || StrUtil.getFileExt(am.getDiskName()).equals("docx") || StrUtil.getFileExt(am.getDiskName()).equals("xls") || StrUtil.getFileExt(am.getDiskName()).equals("xlsx")) {%>
                        <a href="javascript:;" onClick="editdoc(<%=doc.getID()%>, <%=am.getId()%>)">编辑</a>&nbsp;&nbsp;
                        <%}%>                        
                        <a href="?op=edit&id=<%=doc.getID()%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&action=changeAttachOrders&direction=up&attachId=<%=am.getId()%>"><img src="../images/arrow_up.gif" alt="往上" width="16" height="20" border="0" align="absmiddle"></a>&nbsp;<a href="?op=edit&id=<%=doc.getID()%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&action=changeAttachOrders&direction=down&attachId=<%=am.getId()%>"><img src="../images/arrow_down.gif" alt="往下" width="16" height="20" border="0" align="absmiddle"></a>
                        </td>
                      </tr>
                    </table>
				<%}%>
              </td>
            </tr>
			<%}%>
            <tr>
              <td height="30" colspan=2 align=center bgcolor="#FFFFFF">
			  <%
			  if (op.equals("add"))
			  	action = "添 加";
			  else
			  	action = "保 存";
			  %>
			  <input name="cmdok" type="submit" class="btn" value=" <%=action%> ">
			  <%if (op.equals("edit")) {
              		String viewPage = request.getContextPath() + "/help/doc_show.jsp";
              %>
			  &nbsp;<input name="remsg" type="button" class="btn" onClick='addTab("<%=doc.getTitle()%>", "<%=viewPage%>?id=<%=id%>")' value=" 预 览 ">
              <%}%>
              </td>
            </tr>
        </table>
  </form>
		<table width="100%"  border="0" style="display:none">
          <tr>
            <td align="center">
			<%if (doc!=null) {
				int pageNum = 1;
			%>
			文章共<%=doc.getPageCount()%>页&nbsp;&nbsp;页码
            <%
					int pagesize = 1;
					int total = DocContent.getContentCount(doc.getID());
					int curpage,totalpages;
					Paginator paginator = new Paginator(request, total, pagesize);
					// 设置当前页数和总页数
					totalpages = paginator.getTotalPages();
					curpage	= paginator.getCurrentPage();
					if (totalpages==0)
					{
						curpage = 1;
						totalpages = 1;
					}
					
					String querystr = "op=edit&doc_id=" + id;
					out.print(paginator.getCurPageBlock("doc_editpage.jsp?"+querystr));
					%>
            <%if (op.equals("edit")) {
						if (doc.getPageCount()!=pageNum) {					
					%>
&nbsp;<a href="doc_editpage.jsp?op=add&action=insertafter&doc_id=<%=doc.getID()%>&afterpage=<%=pageNum%>">当前页之后插入一页</a>
<%	}
					}%>
&nbsp;<a href="doc_editpage.jsp?op=add&doc_id=<%=doc.getID()%>">增加一页</a>
<%}%>		
			</td>
          </tr>
          <tr>
            <form name="form3" action="?" method="post"><td align="center">
			<input name="newname" type="hidden">
			</td></form>
          </tr>
        </table>
	</TD>
  </TR>
</TABLE>

<iframe id="hideframe" name="hideframe" src="" width=0 height=0></iframe>
</body>
<script>
$(function(){
	$('#expireDate').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d'
    });
})
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

function changeAttachName(attach_id, doc_id, nm) {
	var obj = findObj(nm);
	// document.frames.hideframe.location.href = "fwebedit_do.jsp?op=changeattachname&page_num=1&doc_id=" + doc_id + "&attach_id=" + attach_id + "&newname=" + obj.value
	form3.action = "fwebedit_do.jsp?op=changeattachname&page_num=1&doc_id=" + doc_id + "&attach_id=" + attach_id;
	form3.newname.value = obj.value;
	form3.submit();
}

function delAttach(attach_id, doc_id) {
	if (!window.confirm("您确定要删除吗？")) {
		return;
	}
	document.frames.hideframe.location.href = "fwebedit_do.jsp?op=delAttach&page_num=1&doc_id=" + doc_id + "&attach_id=" + attach_id
}

// 编辑文件
function editdoc(doc_id, file_id) {
<%if (cfg.get("isUseNTKO").equals("true")) {%>
	openWin("fileark/fileark_ntko_edit.jsp?docId=" + doc_id + "&attachId=" + file_id + "&isRevise=0", 1024, 768);
<%}else{%>	
	rmofficeTable.style.display = "";
	addform.redmoonoffice.AddField("doc_id", doc_id);
	addform.redmoonoffice.AddField("file_id", file_id);
	addform.redmoonoffice.Open("<%=Global.getFullRootPath(request)%>/fileark/getfile.jsp?docId=" + doc_id + "&attachId=" + file_id);
<%}%>
}

function OfficeOperate() {
	alert(addform.redmoonoffice.ReturnMessage.substring(0, 4)); // 防止后面跟乱码
}

CKEDITOR.plugins.add( '$PLUGINNAMEALLLOWERCASE$', {
	init : function( editor )
	{
		var pluginName = '$PLUGINNAMEALLLOWERCASE$';
		// Register the dialog.
		CKEDITOR.dialog.addIframe(pluginName, pluginName, '/path/to/load/the/html.html', 410, 508, function() {});
		// Register the command.
		var command = editor.addCommand(pluginName, {exec: function() { editor.openDialog(pluginName); }});
		command.modes = { wysiwyg:1, source:0 };
		command.canUndo = false;
		editor.ui.addButton('$PLUGINNAMEPASCALCASE$', {
			label: $BUTTONLABEL$,
			className: 'cke_button_' + pluginName,
			command: pluginName
			});
			editor.on( 'doubleclick', function( evt )             {
				var element = evt.data.element;
				if ( element.is( '$NODENAME$' ) && !element.data( 'cke-realelement' ) ) {
					evt.data.dialog = '$PLUGINNAMEALLLOWERCASE$';
					evt.cancel();
					}
					});
					// If the "menu" plugin is loaded, register the menu items.
					if ( editor.addMenuItems )         {
						editor.addMenuItems(                 {
							$PLUGINNAMEALLLOWERCASE$ :                     {
								label : $EDITLABEL$,
								command : '$PLUGINNAMEALLLOWERCASE$',
								group : '$PLUGINNAMEALLLOWERCASE$'
								}
								});         }
								// If the "contextmenu" plugin is loaded, register the listeners.
								if ( editor.contextMenu )         {
									editor.contextMenu.addListener( function( element, selection )                 {
										if ( !element || !element.is('$NODENAME$') || element.data( 'cke-realelement' ) || element.isReadOnly() )                         return null;                      return { $PLUGINNAMEALLLOWERCASE$ : CKEDITOR.TRISTATE_OFF };                 });         }     } } ); 


function setNetdiskFiles(ids) {
	getNetdiskFiles(ids);
}

function doGetNetdiskFiles(response){
	var rsp = response.responseText.trim();
	o("netdiskFilesDiv").innerHTML += rsp;
}

var errFunc = function(response) {
	// alert('Error ' + response.status + ' - ' + response.statusText);
	alert(response.responseText);
}

function getNetdiskFiles(ids) {
	var str = "ids=" + ids;
	var myAjax = new cwAjax.Request( 
		"<%=cn.js.fan.web.Global.getFullRootPath(request)%>/netdisk/ajax_getfile.jsp", 
		{ 
			method:"post",
			parameters:str,
			onComplete:doGetNetdiskFiles,
			onError:errFunc
		}
	);
}
</script>
</html>