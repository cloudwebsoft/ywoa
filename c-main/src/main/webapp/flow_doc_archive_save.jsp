<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.flow.WorkflowPredefineDb"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.WorkflowDb"%>
<%@ page import="com.redmoon.oa.flow.Render"%>
<%@ page import="com.redmoon.oa.flow.WorkflowMgr"%>
<%@ page import="com.redmoon.oa.flow.FormDb"%>
<%@ page import="com.redmoon.oa.flow.WorkflowActionDb"%>
<%@ page import="com.redmoon.oa.fileark.DirView"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<script src="inc/common.js"></script>
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-migrate-1.2.1.min.js"></script>
<script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script src="js/jquery.my.js"></script>
<script src="js/jstree/jstree.js"></script>
<link type="text/css" rel="stylesheet" href="js/jstree/themes/default/style.css" />
<script src="inc/flow_dispose_js.jsp"></script>
<script src="inc/flow_js.jsp"></script>
<script src="inc/ajax_getpage.jsp"></script>
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

function ShowChild(imgobj, name)
{
	var tableobj = findObj("childof"+name);
	if (tableobj.style.display=="none")
	{
		tableobj.style.display = "";
		if (imgobj.src.indexOf("i_puls-root-1.gif")!=-1)
			imgobj.src = "images/i_puls-root.gif";
		if (imgobj.src.indexOf("i_plus-1-1.gif")!=-1)
			imgobj.src = "images/i_plus2-2.gif";
		if (imgobj.src.indexOf("i_plus-1.gif")!=-1)
			imgobj.src = "images/i_plus2-1.gif";
	}
	else
	{
		tableobj.style.display = "none";
		if (imgobj.src.indexOf("i_puls-root.gif")!=-1)
			imgobj.src = "images/i_puls-root-1.gif";
		if (imgobj.src.indexOf("i_plus2-2.gif")!=-1)
			imgobj.src = "images/i_plus-1-1.gif";
		if (imgobj.src.indexOf("i_plus2-1.gif")!=-1)
			imgobj.src = "images/i_plus-1.gif";
	}
}

function selectDir(dirCode, dirName) {
	flowForm.dirCode.value = dirCode;
	spanDirName.innerHTML = dirName;
}

function flowForm_onsubmit() {
	if (flowForm.dirCode.value=="") {
		jAlert("请选择所属目录！","提示");
		return false;
	}
	if (flowForm.title.value=="") {
		jAlert("标题不能为空！","提示");
		return false;
	}
	flowForm.content.value = formDiv.innerHTML;
}
</script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
int flowId = ParamUtil.getInt(request, "flowId");
int actionId = ParamUtil.getInt(request, "actionId");
WorkflowMgr wfm = new WorkflowMgr();
WorkflowDb wf = wfm.getWorkflowDb(flowId);
WorkflowActionDb wa = new WorkflowActionDb();
wa = wa.getWorkflowActionDb(actionId);

com.redmoon.oa.flow.Leaf flowlf = new com.redmoon.oa.flow.Leaf();
flowlf = flowlf.getLeaf(wf.getTypeCode());

FormDb fd = new FormDb();
fd = fd.getFormDb(flowlf.getFormCode());

// 置NestSheetCtl需要用到的formCode
request.setAttribute("formCode", flowlf.getFormCode());

WorkflowPredefineDb wpd = new WorkflowPredefineDb();
wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
String dirCode = wpd.getDirCode();
String dirName = "";
if (!dirCode.equals("")) {
	com.redmoon.oa.fileark.Leaf lf = new com.redmoon.oa.fileark.Leaf();
	lf = lf.getLeaf(dirCode);
	if (lf!=null)
		dirName = lf.getName();
}
%>
<title>存档</title>
<table width="494" height="89" border="0" align="center" cellpadding="0" cellspacing="0" class="percent98">
  <thead>
  <tr>
    <td height="23" class="tabStyle_1_title">&nbsp;&nbsp;<span>存档 - 请选择存档目录和文件 </span></td>
  </tr>
  </thead>
  <tr>
    <td valign="top">
    <table width="100%"  border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td width="26%" align="left" id="userList" valign="top"><%
Directory dir = new Directory();
Leaf rootLeaf = dir.getLeaf(Leaf.ROOTCODE);
DirectoryView dv = new DirectoryView(rootLeaf);
//DirView dv = new DirView(request, rootLeaf);
//dv.ListFunc(out, "_self", "selectDir", "", "" );
String jsonData = dv.getJsonString(Leaf.ROOTCODE);
%></td>
          <td width="74%" valign="top">
		  <table width="98%" border="0" cellpadding="5" cellspacing="0" class="tableframe">
		  <form id=flowForm name=flowForm action="flow_doc_archive_save_do.jsp" method=post onsubmit="return flowForm_onsubmit()" target="_self">
            <tr>
              <td>目录：<input name=dirCode value="<%=dirCode%>" type=hidden><span id=spanDirName name=spanDirName><%=dirName%></span></td>
            </tr>
            <tr>
              <td>标题：<input id="title" name="title" value="<%=wf.getTitle()%>" size="50" reserve="true">
			  <input name="content" type="hidden" value="" />
			  <input name="flowId" type="hidden" value="<%=flowId%>" />
			  <input name="actionId" type="hidden" value="<%=actionId%>" />
			  </td>
            </tr>
            <tr>
              <td>表单：</td>
            </tr>
            <tr>
              <td>
			  <div id="formDiv">
					<%
				  int doc_id = wf.getDocId();
				  com.redmoon.oa.flow.DocumentMgr dm = new com.redmoon.oa.flow.DocumentMgr();
				  com.redmoon.oa.flow.Document doc = dm.getDocument(doc_id);
				  Render rd = new Render(request, wf, doc);
				  out.print(rd.reportForArchive(wf, fd));
					%>
			  </div>
			  </td>
            </tr>
            <tr>
              <td>附件：</td>
            </tr>
            <tr>
              <td><%
		  java.util.Vector attachments = doc.getAttachments(1);
		  java.util.Iterator ir = attachments.iterator();
  
		  while (ir.hasNext()) {
		  	com.redmoon.oa.flow.Attachment am = (com.redmoon.oa.flow.Attachment) ir.next(); %>
                <table width="100%"  border="0" cellpadding="0" cellspacing="0" bordercolor="#D6D3CE">
                  <tr>
                    <td width="5%" height="24" align="right"><img src="images/attach.gif" /></td>
                    <td width="73%">&nbsp; <a target="_blank" href="<%=am.getVisualPath() + "/" + am.getDiskName()%>"><%=am.getName()%></a><br />                    </td>
                    <td width="22%"><input type="checkbox" name="attachIds" value="<%=am.getId()%>" checked="checked" />
                      是否存档</td>
                  </tr>
                </table>
                <%}%></td>
            </tr>
            <tr>
              <td align="center"><input type="submit" value=" 保 存 " class="btn" />
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <input type="reset" name="Submit2" value=" 重 置 " class="btn" /></td>
            </tr>
			</form>
			</table>
          </td>
        </tr>
        <tr>
          <td colspan="2" align="center">&nbsp;</td>
        </tr>
    </table></td>
  </tr>
</table>
<script>
var deptCode;
jQuery(function(){
	jQuery('#floater').css({'left':jQuery(window).width() - jQuery('#floater').width() - 8});
	jQuery(".userList").bind("mouseenter",function(){
		jQuery(this).find("td").css({"background":"#f0f8fd"});
	});
	jQuery(".userList").bind("mouseleave",function(){
		jQuery(this).find("td").css({"background":""});
	});
	jQuery('#userList').jstree({
    	"core" : {
            "data" :  <%=jsonData%>,
            "themes" : {
			   "theme" : "default" ,
			   "dots" : true,  
			   "icons" : true  
			},
			"check_callback" : true,	
 		},
 		"plugins" : ["wholerow", "themes", "ui", ,"types","state"],
	}).bind('select_node.jstree', function (e, data) {//绑定选中事件
			deptCode = data.node.id;
			var user = $("#"+deptCode).find("a").html();
			var a  = user.indexOf("</I>");
			if(a < 0){
				a = user.indexOf("</i>")
			}
			user = user.substring(a+4);
			selectDir(deptCode,user);
		});
	$('#formDiv').find('input').each(function() {
		$(this).remove();
	});
})
</script>
