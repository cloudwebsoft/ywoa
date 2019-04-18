<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.oa.fileark.plugin.*"%>
<%@ page import="com.redmoon.oa.fileark.plugin.base.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="java.util.Calendar" %>
<%@ page import="cn.js.fan.db.Paginator"%>
<html><head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="common.css" rel="stylesheet" type="text/css">
<link href="fileark/default.css" rel="stylesheet" type="text/css">
<%@ include file="inc/nocache.jsp"%>
<jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.fileark.Directory"/>
<%
String dir_code = ParamUtil.get(request, "dir_code");
String dir_name = ParamUtil.get(request, "dir_name");
int id = 0;

Privilege privilege = new Privilege();

String correct_result = "操作成功！";
Document doc = null;
Document template = null;
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
String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	LeafPriv lp = new LeafPriv();
	lp.setDirCode(dir_code);
	if (!lp.canUserAppend(privilege.getUser(request))) {
		out.print(StrUtil.Alert(privilege.MSG_INVALID));
		return;
	}

	String action = ParamUtil.get(request, "action");
	if (action.equals("selTemplate")){
		int tid = ParamUtil.getInt(request, "templateId");
		template = docmanager.getDocument(tid);
	}
}
if (op.equals("edit")) {
	String action = ParamUtil.get(request, "action");
	try {
		id = ParamUtil.getInt(request, "id");
		doc = docmanager.getDocument(id);
		dir_code = doc.getDirCode();

		LeafPriv lp = new LeafPriv(doc.getDirCode());
		if (!lp.canUserModify(privilege.getUser(request))) {
			out.print(StrUtil.makeErrMsg(privilege.MSG_INVALID));
			return;
		}
		
		if (action.equals("selTemplate")) {
			int tid = ParamUtil.getInt(request, "templateId");
			doc.setTemplateId(tid);
			doc.updateTemplateId();
		}
		if (doc!=null) {
			template = doc.getTemplate();
		}
	} catch (ErrMsgException e) {
		out.print(strutil.makeErrMsg(e.getMessage(),"red", "green"));
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

		LeafPriv lp = new LeafPriv();
		lp.setDirCode(doc.getDirCode());
		if (!lp.canUserModify(privilege.getUser(request))) {
			out.print(StrUtil.makeErrMsg(privilege.MSG_INVALID));
			return;
		}
		
	} catch (ErrMsgException e) {
		out.print(strutil.makeErrMsg(e.getMessage(),"red", "green"));
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
td {  font-family: "Arial", "Helvetica", "sans-serif"; font-size: 14px; font-style: normal; line-height: 150%; font-weight: normal}
.style2 {color: #FF3300}
-->
</style>
<script language=JavaScript src='scripts/formpost.js'></script>
<script language="JavaScript">
<!--
<%
if (doc!=null) {
	out.println("var id=" + doc.getID() + ";");
}
%>
	var op = "<%=op%>";

	function SubmitWithFileDdxc() {
		addform.webedit.isDdxc = 1;
		if (document.addform.title.value.length == 0) {
			alert("请输入文章标题.");
			document.addform.title.focus();			
			return false;
		}
		loadDataToWebeditCtrl(addform, addform.webedit);
		addform.webedit.MTUpload();
		// 因为Upload()中启用了线程的，所以函数在执行后，会立即反回，使得下句中得不到ReturnMessage的值
		// 原因是此时服务器的返回信息还没收到
		// alert("ReturnMessage=" + addform.webedit.ReturnMessage);
	}

	function SubmitWithFileThread() {
		if (document.addform.title.value.length == 0) {
			alert("请输入文章标题.");
			document.addform.title.focus();			
			return false;
		}
		loadDataToWebeditCtrl(addform, addform.webedit);
		addform.webedit.Upload();
		// 因为Upload()中启用了线程的，所以函数在执行后，会立即反回，使得下句中得不到ReturnMessage的值
		// 原因是此时服务器的返回信息还没收到
		// alert("ReturnMessage=" + addform.webedit.ReturnMessage);
	}

	function SubmitWithFile(){
		if (document.addform.title.value.length == 0) {
			alert("请输入文章标题.");
			document.addform.title.focus();			
			return false;
		}
		loadDataToWebeditCtrl(addform, addform.webedit);
		addform.webedit.UploadArticle();
		if (addform.webedit.ReturnMessage == "<%=correct_result%>")
			doAfter(true);
		else
			doAfter(false);
	}
	
	function SubmitWithoutFile() {
		if (document.addform.title.value.length == 0) {
			alert("请输入文章标题.");
			document.addform.title.focus();	
			return false;
		}

		addform.isuploadfile.value = "false";
		loadDataToWebeditCtrl(addform, addform.webedit);
		addform.webedit.UploadMode = 0;
		addform.webedit.UploadArticle();
		addform.isuploadfile.value = "true";
		if (addform.webedit.ReturnMessage == "<%=correct_result%>")
			doAfter(true);
		else
			doAfter(false);		
	}

	function ClearAll() {
		document.addform.title.value=""
		oEdit1.putHTML(" ");
	}
	
	function doAfter(isSucceed) {
		if (isSucceed) {
			if (op=="edit")
			{
				if (confirm("<%=correct_result%> 请点击确定按钮刷新页面\r\n(如果您确定文件是来自其它服务器，可以不刷新！)。"))
					// 此处一定要reload，否则会导致再点击上传（连同文件）时，因为images已被更改，而content中路径未变，从而下载不到，导到最终会丢失			
					// 以前未注意到此问题，可能是因为再点击上传时，获取的图片在服务器端虽然已丢失，但是缓存中可能还有的原因
					// 也可能是因为在编辑文件时，编辑完了并未重新刷新页面，content中的图片还是来源的位置（来源自别的服务器），所以依然能够上传，但是只要此时再一刷新，再连续上传两次，问题就会出现
					window.location.reload(true); 
			}
			else {
				alert("<%=correct_result%>");
		    }
		}
		else {
			alert(addform.webedit.ReturnMessage);
		}
	}
	
function showvote(isshow)
{
	if (addform.isvote.checked)
	{
		addform.vote.style.display = "";
	}
	else
	{
		addform.vote.style.display = "none";		
	}
}

function selTemplate(id)
{
	if (addform.templateId.value!=id) {
		addform.templateId.value = id;
		// 此处注意当模式对话框的路径在admin下时，退出后本页路径好象被改为admin了
<%if (doc!=null) {%>
		window.location.href="../fwebedit.jsp?op=edit&action=selTemplate&id=<%=id%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>&templateId=" + id;
<%}else{%>
		if (id!=-1)
			window.location.href="../fwebedit.jsp?op=add&action=selTemplate&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>&templateId=" + id;		
<%}%>
	}
}

var recordFilePath = "";
function Operate() {
	recordFilePath = addform.Recorder.FilePath;
	addform.webedit.InsertFileToList(recordFilePath);
}

//-->
</script>
<script language=JavaScript src='scripts/language/schi/editor_lang.js'></script>
<%
if (request.getHeader("User-Agent").indexOf("MSIE")!=-1){
	out.println("<script language=JavaScript src='scripts/editor.js'></script>");
}
else{
	out.println("<script language=JavaScript src='scripts/moz/editor.js'></script>");
}
%>
</head>
<body bgcolor="#FFFFFF" text="#000000">
<TABLE width="98%" BORDER=0 align="center" CELLPADDING=0 CELLSPACING=0>
  <TR valign="top" bgcolor="#FFFFFF">
    <TD width="" height="430" colspan="2" style="background-attachment: fixed; background-image: url(images/bg_bottom.jpg); background-repeat: no-repeat">
          <TABLE cellSpacing=0 cellPadding=0 width="100%">
            <TBODY>
              <TR>
                <TD class=head>
				<%
				if (op.equals("add")) {%>
					添加内容至--<a href="fileark/document_list_m.jsp?dir_code=<%=StrUtil.UrlEncode(dir_code)%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>"><%=dir_name%></a>
				<%}else{%>
					修改--
					<%
					Leaf dlf = new Leaf();
					if (doc!=null) {
						dlf = dlf.getLeaf(doc.getDirCode());
					}
					if (doc!=null && dlf.getType()==2) {%>
						<a href="fileark/document_list_m.jsp?dir_code=<%=StrUtil.UrlEncode(dir_code)%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>"><%=dlf.getName()%></a>
					<%}else{%>
						<%=dir_name%>
					<%}%>
					(<%=doc.getDirCode()%>)
				<%}%>
				</TD>
              </TR>
            </TBODY>
          </TABLE>
	<form name="addform" action="/admin/fwebedit_do.jsp" method="post">
          <table border="0" cellspacing="1" width="100%" cellpadding="2" align="center">
            <tr align="center" bgcolor="#F2F2F2">
              <td height="20" colspan=2 align=center><b><%=doc!=null?doc.getTitle():""%></b>&nbsp;<input type="hidden" name=isuploadfile value="true">
			  <input type="hidden" name=id value="<%=doc!=null?""+doc.getID():""%>">
<%=doc!=null?"(id:"+doc.getID()+")":""%>	<%if (doc!=null) {%>
<!--( <a href="fileark/comment_m.jsp?doc_id=<%=doc.getID()%>">管理评论</a> )-->
<%}%></td>
            </tr>
            <tr>
              <td colspan="2" align="left" valign="middle" bgcolor="#FFFFFF" class="unnamed2">
<%
// 如果是加入新文章
if (doc==null) {			  
	PluginMgr pm = new PluginMgr();
	PluginUnit pu = pm.getPluginUnitOfDir(dir_code);
	if (pu!=null) {
		IPluginUI ipu = pu.getUI(request);
		IPluginViewAddDocument pv = ipu.getViewAddDocument(dir_code);
		out.print(pu.getName(request) + ":&nbsp;" + pv.render(UIAddDocument.POS_TITLE) + "<BR>");
		out.print(pv.render(UIAddDocument.POS_FORM_ELEMENT) + "<BR>");
	}
}
else {
	PluginMgr pm = new PluginMgr();
	PluginUnit pu = pm.getPluginUnitOfDir(dir_code);
	if (pu!=null) {
		IPluginUI ipu = pu.getUI(request);
		IPluginViewEditDocument pv = ipu.getViewEditDocument(doc);
		out.print(pu.getName(request) + ":&nbsp;" + pv.render(UIAddDocument.POS_TITLE) + "<BR>");
		out.print(pv.render(UIAddDocument.POS_FORM_ELEMENT) + "<BR>");
	}
}
%>			  </td>
            </tr>
            <tr>
              <td align="left" class="unnamed2" valign="middle">作&nbsp;&nbsp;&nbsp;&nbsp;者：</td>
              <td bgcolor="#F2F2F2"><input name="author" id="author" type="TEXT" size=30 maxlength=100 style="background-color:ffffff;color:000000;border: 1 double" value="<%=doc!=null?doc.getAuthor():privilege.getUser(request)%>">
			  <input type="hidden" name="op" value="<%=op%>"></td>
            </tr>
            <tr>
              <td align="left" class="unnamed2" valign="middle">标&nbsp;&nbsp;&nbsp;&nbsp;题：</td>
              <td width="92%" bgcolor="#FFFFFF">
                  <input name="title" id=me type="TEXT" size=50 maxlength=100 style="background-color:ffffff;color:000000;border: 1 double" value="<%=doc!=null?doc.getTitle():""%>">                  
                  <span class="unnamed2"><font color="#FF0000">＊</font></span>			  </td>
            </tr>
            <tr bgcolor="#F2F2F2">
              <td align="left" class="unnamed2" valign="middle">关键字：</td>
              <td bgcolor="#F2F2F2"><input name="keywords" id=keywords type="TEXT" size=30 maxlength=100 style="background-color:ffffff;color:000000;border: 1 double" value="<%=StrUtil.getNullStr(doc==null?dir_code:doc.getKeywords())%>">
              ( 请用&quot;，&quot;号分隔)
			<input type="hidden" name="isRelateShow" value="1">
			  </td>
            </tr>
            <tr align="left">
              <td align="left" valign="middle" class="unnamed2">&nbsp;评&nbsp;&nbsp;&nbsp;&nbsp;论：</td>
            <td valign="middle" class="unnamed2"><%
			String strChecked = "";
			if (doc!=null) {
				if (doc.getCanComment())
					strChecked = "checked";
			}
			else
				strChecked = "checked";
			%>
              <input type="checkbox" name="canComment" value="1" <%=strChecked%>>
允许
<%if (doc!=null) {%>
[<a href="fileark/comment_m.jsp?doc_id=<%=doc.getID()%>">管理评论</a>]
<%}%>
&nbsp;
<%if (doc!=null) {%>
<input type="checkbox" name="isHome" value="<%=doc.getIsHome()?"false":"true"%>" <%=doc.getIsHome()?"checked":""%>>
<%}else{%>
<input type="checkbox" name="isHome" value="true" checked>
<%}%>
置于首页
<%
LeafPriv lp = new LeafPriv(dir_code);
if (lp.canUserExamine(privilege.getUser(request))) {
%>
	&nbsp;&nbsp;&nbsp;&nbsp;
	<span class="style2">审核</span>	
	<select name="examine">
	  <option value="<%=Document.EXAMINE_NOT%>">未审核</option>
	  <option value="<%=Document.EXAMINE_NOTPASS%>">未通过</option>
	  <option value="<%=Document.EXAMINE_PASS%>">已通过</option>
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
<input type="checkbox" name="isNew" value="1" <%=checknew%>>
<img src="images/i_new.gif" width="18" height="7"></td>
            </tr>
            <tr align="left" bgcolor="#F2F2F2">
              <td colspan="2" valign="middle">
			  <%if (doc!=null) {%>
				  <script>
				  var bcode = "<%=doc.getDirCode()%>";
				  </script>
				  &nbsp;分&nbsp;&nbsp;&nbsp;&nbsp;类：
					<select name="dir_code" onChange="if(this.options[this.selectedIndex].value=='not'){alert(this.options[this.selectedIndex].text+' 不能被选择！'); this.value=bcode; return false;}">
						  <option value="not" selected>请选择目录</option>
					<%
					Leaf lf = dir.getLeaf("root");
					DirectoryView dv = new DirectoryView(request, lf);
					dv.ShowDirectoryAsOptions(out, lf, lf.getLayer());
					%>
					</select>
						<script>
						addform.dir_code.value = "<%=doc.getDirCode()%>";
						</script>
						&nbsp;( <span class="style3">蓝色</span>表示可选 )			  
				<%}else{%>
					<input type=hidden name="dir_code" value="<%=dir_code%>">
				<%}%>				  </td>
            </tr>
            <tr align="left" bgcolor="#F2F2F2">
              <td colspan="2" valign="middle" class="unnamed2">&nbsp;模板ID&nbsp;
			  <%
			  if (doc!=null)
			  	templateId = doc.getTemplateId();
			  %>
                <input name="templateId" class="btn" value="<%=templateId%>" size=3 readonly>
&nbsp;<a href="javascript:showModalDialog('fileark/doc_template_select_frame.jsp',window.self,'dialogWidth:640px;dialogHeight:480px;status:no;help:no;')">选模板</a> <span id=templateInfo>
<%if (doc!=null && doc.getTemplateId()!=doc.NOTEMPLATE) {%>
<a target=_blank href="fileark/doc_template_show.jsp?id=<%=doc.getTemplateId()%>">预览模板</a>
<a href="javascript:oEdit1.putHTML(divTemplate.innerHTML)">重新应用模板</a>
<a href="javascript:window.location.href='fwebedit.jsp?op=edit&action=selTemplate&id=<%=id%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>&templateId=-1'">取消模板</a>
<%}else{%> 
<a href="#" onClick="if (addform.templateId.value!=-1) window.open('fileark/doc_template_show.jsp?id=' + addform.templateId.value); else alert('当前未选择模板')">预览模板</a>
&nbsp;<a href="#" onClick="addform.templateId.value='-1';oEdit1.putHTML(' ')">取消模板</a>
<%}%>
</span></td>
            </tr>
            <tr bgcolor="#F2F2F2">
              <td align="right" class="unnamed2" valign="middle">
			  <script>
			  var vp = "";
			  </script>
		<%
		String display="none",ischecked="false", isreadonly = "";
		if (doc!=null) {
			if (doc.getType()==1) {
				display = "";
				ischecked = "checked disabled";
				isreadonly = "readonly";
				%>
				<script>
				var voteoption = "<%=doc.getVoteOption()%>";
				var votes = voteoption.split("|");
				var len = votes.length;
				for (var i=0; i<len; i++) {
					if (vp=="")
						vp = votes[i];
					else
						vp += "\r\n" + votes[i];
				}
				</script>
			<%}
		}%>
					  <input type="checkbox" name="isvote" value="1" onClick="showvote()" <%=ischecked%>>
              投票</td>
              <td valign="middle"><textarea <%=isreadonly%> style="display:<%=display%>" cols="60" name="vote" rows="8" wrap="VIRTUAL" title="输入投票选项">
			  </textarea>
			  <script>
  				addform.vote.value = vp;
			  </script>
每行代表一个选项</td>
            </tr>
            <tr align="center">
              <td colspan="2" valign="top" bgcolor="#F2F2F2" class="unnamed2">

<pre id="idTemporary" name="idTemporary" style="display:none">
<%
if (!op.equals("add")) {
%>
<%=strutil.HTMLEncode(strutil.getNullString(doc.getContent(1)))%>
<%}%>
</pre>

<pre id="divTemplate" name="divTemplate" style="display:none">
<%if (template!=null) {%>
	<%=template.getContent(1)%>
<%}%>
</pre>

 <script>
		var oEdit1 = new InnovaEditor("oEdit1");
		oEdit1.width="100%";
		oEdit1.height="500";

		oEdit1.features=["FullScreen","Preview","Print","Search","SpellCheck",
					"Cut","Copy","Paste","PasteWord","PasteText","|","Undo","Redo","|",
					"ForeColor","BackColor","|","Bookmark","Hyperlink",
					"HTMLFullSource","HTMLSource","XHTMLFullSource",
					"XHTMLSource","BRK","Numbering","Bullets","|","Indent","Outdent","LTR","RTL","|","Image","Flash","Media","|","InternalLink","CustomObject","|",
					"Table","Guidelines","Absolute","|","Characters","Line",
					"Form","Clean","ClearAll","BRK",
					"StyleAndFormatting","TextFormatting","ListFormatting","BoxFormatting",
					"ParagraphFormatting","CssText","Styles","|",
					"Paragraph","FontName","FontSize","|",
					"Bold","Italic",
					"Underline","Strikethrough","|","Superscript","Subscript","|",
					"JustifyLeft","JustifyCenter","JustifyRight","JustifyFull"]; 
<%if (templateId!=-1 && doc==null) {%>	
	oEdit1.RENDER(document.getElementById("divTemplate").innerHTML);
<%}else{%>
	oEdit1.RENDER(document.getElementById("idTemporary").innerHTML);
<%}%>
</script>             </td>
            </tr>
            <tr>
              <td width="8%" align="right" bgcolor="#FFFFFF">提示：</td>
              <td bgcolor="#FFFFFF">
			  回车可用Shift+Enter			  </td>
            </tr>
            <tr>
              <td height="25" colspan=2 align="center" bgcolor="#FFFFFF">
			  <%
			  if (doc!=null) {
				  Vector attachments = doc.getAttachments(1);
				  Iterator ir = attachments.iterator();
				  while (ir.hasNext()) {
				  	Attachment am = (Attachment) ir.next(); %>
					<table width="98%"  border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td width="7%" align="center"><img src=images/attach.gif width="17" height="17"></td>
                        <td width="93%">&nbsp;
                          <input name="attach_name<%=am.getId()%>" value="<%=am.getName()%>" size="30">
&nbsp;<a href="javascript:changeAttachName('<%=am.getId()%>', '<%=doc.getID()%>', '<%="attach_name"+am.getId()%>')">更改</a>                        &nbsp;<a href="javascript:delAttach('<%=am.getId()%>', '<%=doc.getID()%>')">删除</a>&nbsp;&nbsp;<a target=_blank href="<%=Global.getRootPath()%>/<%=am.getVisualPath() + am.getDiskName()%>">查看</a>&nbsp;<a href="?op=edit&id=<%=doc.getID()%>&action=changeAttachOrders&direction=up&attachId=<%=am.getId()%>"><img src="images/arrow_up.gif" alt="往上" width="16" height="20" border="0" align="absmiddle"></a>&nbsp;<a href="?op=edit&id=<%=doc.getID()%>&action=changeAttachOrders&direction=down&attachId=<%=am.getId()%>"><img src="images/arrow_down.gif" alt="往下" width="16" height="20" border="0" align="absmiddle"></a></td>
                      </tr>
                    </table>
				<%}
			  }
			  %>			  </td>
            </tr>
            <tr>
              <td height="153" colspan=2 align=center bgcolor="#FFFFFF">
			  <table  border="0" align="center" cellpadding="0" cellspacing="1" bgcolor="#CCCCCC">
                <tr>
                  <td bgcolor="#FFFFFF"><%
Calendar cal = Calendar.getInstance();
String year = "" + (cal.get(cal.YEAR));
String month = "" + (cal.get(cal.MONTH) + 1);
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String filepath = cfg.get("file_folder") + "/" + year + "/" + month;
%><object classid="CLSID:DE757F80-F499-48D5-BF39-90BC8BA54D8C" codebase="activex/cloudym.CAB#version=1,2,0,1" width=400 height=280 align="middle" id="webedit">
                      <param name="Encode" value="utf-8">
					  <param name="MaxSize" value="<%=Global.MaxSize%>"> <!--上传字节-->
                      <param name="ForeColor" value="(0,255,0)">
                      <param name="BgColor" value="(0,0,0)">
                      <param name="ForeColorBar" value="(255,255,255)">
                      <param name="BgColorBar" value="(0,0,255)">
                      <param name="ForeColorBarPre" value="(0,0,0)">
                      <param name="BgColorBarPre" value="(200,200,200)">
                      <param name="FilePath" value="<%=filepath%>">
                      <param name="Relative" value="1">
                      <!--上传后的文件需放在服务器上的路径-->
                      <param name="Server" value="<%=request.getServerName()%>">
                      <param name="Port" value="<%=request.getServerPort()%>">
                      <param name="VirtualPath" value="<%=Global.virtualPath%>">
                      <param name="PostScript" value="<%=Global.virtualPath%>/fwebedit_do.jsp">
                      <param name="PostScriptDdxc" value="<%=Global.virtualPath%>/ddxc.jsp">
                      <param name="SegmentLen" value="204800">
                    </object>
					</td>
                </tr>
              </table>              </td>
            </tr>
            <tr>
              <td height="" colspan=2 align=center bgcolor="#FFFFFF">
			  <div id=recorderDiv style="display:none"><!-- <OBJECT ID="Recorder" CLASSID="CLSID:E4A3D135-E189-48AF-B348-EF5DFFD99A67" codebase="activex/cloudym.CAB#version=1,2,0,1"></OBJECT> --></div></td>
            </tr>
            <tr>
              <td height="30" colspan=2 align=center bgcolor="#FFFFFF">
			  <%
			  String action = "";
			  if (op.equals("add"))
			  	action = "添 加";
			  else
			  	action = "修 改";
			  %>
			  <%if (templateId==-1) {%>
              <input name="cmdok2" type="button" class="btn" value="<%=action%>(断点续传)" onClick="return SubmitWithFileDdxc()">
              <%}
			  if (templateId==-1) {%>
              <input name="cmdok3" type="button" class="btn" value="<%=action%>(单线程)" onClick="return SubmitWithFileThread()">
              <%}
			  if (templateId==-1) {%>
			  <input name="cmdok" type="button" class="btn" value=" <%=action%> " onClick="return SubmitWithFile()">
			  <%}%>
&nbsp;
<input name="notuploadfile" type="button" class="btn" value="<%=action%>(不上传文件)" onClick="return SubmitWithoutFile()">
&nbsp;
      <input name="cmdcancel" type="button" class="btn" onClick="ClearAll()" value=" 清 空 ">
      <br>
&nbsp;<br>
      <%if (op.equals("edit")) {%>
	  <input name="editbtn" type="button" class="btn" onClick="location.href='doc_abstract.jsp?id=<%=doc.getID()%>'" value=" 摘要 ">
	  <%}%>
&nbsp;
<input name="remsg" type="button" class="btn" onClick='alert(webedit.ReturnMessage)' value="返回信息">
&nbsp;
      <%if (op.equals("edit")) {
	  		String viewPage = "doc_show.jsp";
			PluginMgr pm = new PluginMgr();
			PluginUnit pu = pm.getPluginUnitOfDir(dir_code);
			if (pu!=null) {
				IPluginUI ipu = pu.getUI(request);
				viewPage = ipu.getViewPage();
			}
	  %>
<input name="remsg" type="button" class="btn" onClick='window.open("<%=viewPage%>?id=<%=id%>")' value="预览">
&nbsp;&nbsp; <input name="remsg2" type="button" class="btn" onClick="recorderDiv.style.display=''" value="录制语音"></td>
	  <%}%>
            </tr>
        </table>
    </form>
		<table width="100%"  border="0">
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

<iframe id="hideframe" name="hideframe" src="fwebedit_do.jsp" width=0 height=0></iframe>
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
</script>
</html>