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
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
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
ModulePrivDb mpd = new ModulePrivDb(formCodeRelated);
if (!mpd.canUserView(privilege.getUser(request))) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int id = ParamUtil.getInt(request, "id");
// 置嵌套表需要用到的cwsId
request.setAttribute("cwsId", "" + id);
// 置页面类型
request.setAttribute("pageType", "show");

request.setAttribute("formCode", formCodeRelated);

FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCodeRelated);

com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>智能模块设计-显示内容</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="<%=Global.getRootPath()%>/inc/common.js"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=request.getContextPath()%>/inc/flow_js.jsp"></script>
<script src="<%=request.getContextPath()%>/inc/ajax_getpage.jsp"></script>
<script>
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
</script>
</head>
<body>
<%
int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);
if (isShowNav==1) {
%>
<%@ include file="module_inc_menu_top.jsp"%>
<%}%>
<br />
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0" bgcolor="#FFFFFF">
  <tr>
    <td align="left">
    <form name="visualForm" id="visualForm">    
    <table width="100%">
        <tr>
          <td>
		  <%
			com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, id, fd);
			out.print(rd.report());
		  %>
		  </td>
        </tr>
    </table>
    </form>    
    </td>
  </tr>
  <tr>
    <td align="left"></td>
  </tr>
  <tr>
    <td align="left"><%
com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(id);
Iterator ir = fdao.getAttachments().iterator();
				  while (ir.hasNext()) {
				  	com.redmoon.oa.visual.Attachment am = (com.redmoon.oa.visual.Attachment) ir.next();
					if (!StrUtil.getNullStr(am.getFieldName()).equals("")) {
						continue;
					}
					%>
        <table width="85%"  border="0" cellpadding="0" cellspacing="0">
          <tr>
            <td width="5%" height="31" align="right"><img src="<%=Global.getRootPath()%>/images/attach.gif" /></td>
            <td>&nbsp; <a target="_blank" href="<%=Global.getRootPath()%>/visual_getfile.jsp?attachId=<%=am.getId()%>"><%=am.getName()%></a>&nbsp;&nbsp;<a href="?op=delAttach&amp;id=<%=id%>&amp;formCode=<%=StrUtil.UrlEncode(formCode)%>&amp;attachId=<%=am.getId()%>"></a><br />
            </td>
          </tr>
        </table>
      <%}%></td>
  </tr>
  <tr>
    <td height="30" align="center"><input name="id" value="<%=id%>" type="hidden" />
      <input name="Submit" type="button" class="btn" onclick="showFormReport()" value="打印"/>
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	  <%if (mpd.canUserManage(privilege.getUser(request))) {%>
	  <input class="btn" type="button" value="编辑" onclick="window.location.href='<%=request.getContextPath() %>/visual/module_edit_relate.jsp?id=<%=id%>&formCodeRelated=<%=formCodeRelated%>&formCode=<%=formCode%>&isShowNav=0'" />
	  <%}%>
		<!--
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		<input name="button" type="button" class="btn" onclick="exportToWord()" value="导出至Word"/>
		-->
	</td>
  </tr>
</table>
<form name="formWord" target="_blank" action="module_show_word.jsp" method="post">
<textarea name="cont" style="display:none"></textarea>
</form>
</body>
<script>
function showFormReport() {
	var preWin=window.open('preview','','left=0,top=0,width=550,height=400,resizable=1,scrollbars=1, status=1, toolbar=1, menubar=1');
	preWin.document.open();
	preWin.document.write("<style>TD{ TABLE-LAYOUT: fixed; FONT-SIZE: 12px; WORD-BREAK: break-all; FONT-FAMILY:}</style>" + formDiv.innerHTML);
	preWin.document.close();
	preWin.document.title="表单";
	preWin.document.charset="UTF-8";
}

function exportToWord() {
	formWord.cont.value = formDiv.innerHTML;
	formWord.submit();
}
</script>
</html>
