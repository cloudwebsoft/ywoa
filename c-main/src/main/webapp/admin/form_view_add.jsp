<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.flow")) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String formCode = ParamUtil.get(request, "formCode");
String op = ParamUtil.get(request, "op");

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>创建表单</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script language="JavaScript" type="text/JavaScript">
<!--
function setFormContent(htmlCode) {
	divContent.innerHTML = htmlCode;
}

function getFormContent() {
	return divContent.innerHTML;
}

function form1_onsubmit() {
	o("content").value = getFormContent();
}

function openFormWin() {
	// var preWin=window.open('../editor_full/form_view.jsp?op=edit&formCode=<%=StrUtil.UrlEncode(formCode)%>','','left=0,top=0,width=' + (screen.width-6) + ',height=' + (screen.height-78) + ',resizable=1,scrollbars=1, status=1, toolbar=0, menubar=0');
    var preWin = window.open('../ueditor/form_view.jsp?op=edit&formCode=<%=StrUtil.UrlEncode(formCode)%>', '', 'left=0,top=0,width=' + (screen.width - 6) + ',height=' + (screen.height - 78) + ',resizable=1,scrollbars=1, status=1, toolbar=0, menubar=0');
}
//-->
</script>
<style>
	.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
	-moz-opacity: 0.20;
	opacity: 0.20;
	z-index: 1500;
	}
	.treeBackground {
	display: none;
	position: absolute;
	top: -2%;
	left: 0%;
	width: 100%;
	margin: auto;
	height: 200%;
	background-color: #EEEEEE;
	z-index: 1800;
	-moz-opacity: 0.8;
	opacity: .80;
	filter: alpha(opacity = 80);
	}
</style>
</head>
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
if (op.equals("add")) {
	FormViewMgr ftm = new FormViewMgr();
	boolean re = false;
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		re = ftm.create(request);
		%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
		if (re) {
			out.print(StrUtil.jAlert_Redirect("创建成功！","提示", "form_view_list.jsp?formCode=" + StrUtil.UrlEncode(formCode)));
		}
		else {
			out.print(StrUtil.jAlert_Back("创建失败！","提示"));
		}
		return;
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
}
 %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr> 
    <td class="tdStyle_1">视图管理&nbsp;-&nbsp;<%=fd.getName()%></td>
  </tr>
</table>
<br />
<table width="100%"  border="0" cellpadding="0" cellspacing="0" class="tableframe">
	<form id=form1 name=form1 action="form_view_add.jsp?op=add" method=post onSubmit="return form1_onsubmit()">
      <tr>
        <td height="100" align="center" class="p14"><table class="tabStyle_1 percent80" width="98%"  border="0" cellpadding="5" cellspacing="0">
          <tr>
            <td class="tabStyle_1_title" colspan="2" >创建视图</td>
          </tr>
          <tr>
            <td width="20%" >名称</td>
            <td width="80%" align="left" ><input type="text" name="name" value="<%=fd.getName()%>视图" maxlength="50"></td>
          </tr>
          <tr style="display:none">
            <td>带有附件</td>
            <td align="left" >
			<select name="hasAttachment">
			<option value="1" selected>是</option>
			<option value="0">否</option>
			</select>
			</td>
          </tr>
          <tr style="display:none">
            <td >类型</td>
            <td align="left" >
			<select id="kind" name="kind">
            <option value="<%=FormViewDb.KIND_SHOW%>"><%=FormViewDb.getKindDesc(FormViewDb.KIND_SHOW)%></option>
            <option value="<%=FormViewDb.KIND_EDIT%>"><%=FormViewDb.getKindDesc(FormViewDb.KIND_EDIT)%></option>
            </select>
            </td>
          </tr>
    </table>
        </td>
      </tr>
      <tr>
        <td align="center">
	    <input type="button" class="btn" onclick="openFormWin()" value="编辑" />
	    &nbsp;&nbsp;
        <input id="content" name="content" type="hidden" />        
        <input id="ieVersion" name="ieVersion" type="hidden" />        
        <input id="formCode" name="formCode" type="hidden" value="<%=formCode%>" />        
        <input class="btn" type="submit" value="确定">
        </td>
      </tr>
  </form>
      <tr>
        <td align="center"><table width="100%" align="center">
          <tr>
            <td align="left" class="tabStyle_1_title">视图内容</td>
          </tr>
          <tr>
            <td><div id="divContent" name="divContent"></div></td>
          </tr>
        </table></td>
      </tr>
</table>
<br>
<br>
</body>
</html>
