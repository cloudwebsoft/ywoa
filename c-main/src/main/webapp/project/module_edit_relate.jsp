<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String myname = privilege.getUser( request );

String op = ParamUtil.get(request, "op");

String formCode = ParamUtil.get(request, "formCode"); // 主模块编码
if (formCode.equals("")) {
	out.print(SkinUtil.makeErrMsg(request, "编码不能为空！"));
	return;
}

String formCodeRelated = ParamUtil.get(request, "formCodeRelated"); // 从模块编码
String menuItem = ParamUtil.get(request, "menuItem");
int projectId = ParamUtil.getInt(request, "projectId", -1);

ModulePrivDb mpd = new ModulePrivDb(formCodeRelated);
if (!mpd.canUserManage(privilege.getUser(request))) {
	%>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />	
	<%
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int id = ParamUtil.getInt(request, "id");
// 置嵌套表需要用到的cwsId
request.setAttribute("cwsId", "" + id);
// 置页面类型
request.setAttribute("pageType", "edit");

request.setAttribute("formCode", formCodeRelated); // 这里是为了使嵌套表在getNestSheet方法中，传递给当前编辑的表单中的嵌套表

// 用于表单域选择窗体宏控件，但是与上面的formCode存在有重复，@task应替代上面的这一行
request.setAttribute("formCodeRelated", formCodeRelated);

int parentId = ParamUtil.getInt(request, "parentId", -1); // 父模块的ID，仅用于导航，如果导航不显示，则不用传递该参数，用例：module_show_realte.jsp编辑按钮

FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCodeRelated);

com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);

int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);

if (op.equals("saveformvalue")) {
	boolean re = false;
	try {
		re = fdm.update(application, request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
	if (re) {
		projectId = StrUtil.toInt(fdm.getFieldValue("projectId"), -1);		
		%>
		<script>
		// 如果有父窗口，则自动刷新父窗口
		if (window.opener!=null) {
		  window.opener.location.reload();
		  window.opener.focus();
		  window.close();		  
		}
		</script>		
		<%
		out.print(StrUtil.Alert_Redirect("操作成功！", request.getRequestURL() + "?projectId=" + projectId + "&parentId=" + parentId + "&id=" + id + "&menuItem=" + menuItem + "&formCodeRelated=" + formCodeRelated + "&formCode=" + StrUtil.UrlEncode(formCode) + "&isShowNav=" + isShowNav));
	}
	else
		out.print(StrUtil.Alert_Back("操作失败！"));
	return;
}
else if (op.equals("delAttach")) {
	int attachId = ParamUtil.getInt(request, "attachId");
	com.redmoon.oa.visual.Attachment att = new com.redmoon.oa.visual.Attachment(attachId);
	att.del();
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>智能设计-编辑内容</title>
<meta name="renderer" content="ie-stand">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery.raty.min.js"></script>
<script src="../inc/livevalidation_standalone.js"></script>
<script src="../inc/upload.js"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_js.jsp"></script>
<script src="<%=request.getContextPath()%>/inc/ajax_getpage.jsp"></script>

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

<script>
$(function() {
	SetNewDate();
});

function setradio(myitem,v) {
     var radioboxs = document.all.item(myitem);
     if (radioboxs!=null)
     {
       for (i=0; i<radioboxs.length; i++)
          {
            if (radioboxs[i].type=="radio")
              {
                 if (radioboxs[i].value==v)
				 	radioboxs[i].checked = true;
              }
          }
     }
}

function SubmitResult() {
	// 检查是否已选择意见
	if (getradio("resultValue")==null || getradio("resultValue")=="") {
		alert("您必须选择一项意见!");
		return false;
	}
	visualForm.op.value='finish';
	visualForm.submit();
}

// 控件完成上传后，调用Operate()
function Operate() {
	// alert(redmoonoffice.ReturnMessage);
}
</script>
</head>
<body>
<%
if (isShowNav==1) {
%>
<%@ include file="prj_inc_menu_top.jsp"%>
<script>
o("menu<%=menuItem%>").className="current";
</script>
<div class="spacerH"></div>
<%}
else
	out.print("<BR>");
%>
<form action="module_edit_relate.jsp?op=saveformvalue&parentId=<%=parentId%>&id=<%=id%>&formCodeRelated=<%=formCodeRelated%>&formCode=<%=StrUtil.UrlEncode(formCode)%>&isShowNav=<%=isShowNav%>&menuItem=<%=menuItem %>" method="post" enctype="multipart/form-data" name="visualForm" id="visualForm">
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
      <td align="left"><table width="100%">
        <tr>
          <td><%
			com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, id, fd);
			out.print(rd.rend());
		  %>
      		<input id="parentId" name="parentId" value="<%=parentId%>" type="hidden" />                    
          </td>
        </tr>
      </table></td>
    </tr>
	<%if (fd.isHasAttachment()) {%>	
    <tr>
      <td align="left"><script>initUpload()</script>      </td>
    </tr>
    <tr>
      <td align="left"><%
com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(id);
Iterator ir = fdao.getAttachments().iterator();
				  while (ir.hasNext()) {
				  	com.redmoon.oa.visual.Attachment am = (com.redmoon.oa.visual.Attachment) ir.next(); %>
          <table width="82%"  border="0" cellpadding="0" cellspacing="0">
            <tr>
              <td width="5%" height="31" align="right"><img src="<%=Global.getRootPath()%>/images/attach.gif" /></td>
              <td>&nbsp; <a target="_blank" href="<%=Global.getRootPath()%>/visual_getfile.jsp?attachId=<%=am.getId()%>"><%=am.getName()%></a>&nbsp;&nbsp;&nbsp;&nbsp;[<a href="#" onClick="if (confirm('您确定要删除吗？')) window.location.href='?op=delAttach&parentId=<%=parentId%>&id=<%=id%>&formCode=<%=StrUtil.UrlEncode(formCode)%>&formCodeRelated=<%=formCodeRelated%>&attachId=<%=am.getId()%>'">删除</a>]<br />              </td>
            </tr>
          </table>
        <%}
			  %></td>
    </tr>
	<%}%>
    <tr>
      <td height="30" align="center"><input name="id" value="<%=id%>" type="hidden" />
      	<input type="submit" class="btn" name="Submit" value=" 修 改 " />
	    <input name="projectId" value="<%=projectId%>" type="hidden" />        
        &nbsp;&nbsp;
		<%if (isShowNav==0) {%>
      	<input type="button" class="btn" value=" 关 闭 " onClick="window.close()" />
		<%}%>
		</td>
    </tr>
</table>
</form>
</body>
</html>
