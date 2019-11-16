<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import="cn.js.fan.security.AntiXSS" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONException" %>
<%
String rootpath = request.getContextPath();
// response.addHeader("X-XSS-Protection", "X-XSS-Protection: 0; mode=block");
response.setHeader("X-xss-protection", "0;mode=block");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8;" />
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<title>编辑表单</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%@ include file="../inc/nocache.jsp"%>
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script>
function getFormContent() {
	return divContent.innerHTML;
}

function myFormEdit_onsubmit() {
	$('#content').val(getFormContent());
}
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
<div id='loading' class='loading'><img src='../images/loading.gif' /></div>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String code = ParamUtil.get(request, "code");
FormDb fd = new FormDb();
fd = fd.getFormDb(code);

String name = ParamUtil.get(request, "name");
String content = ParamUtil.get(request, "content");
String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
int hasAttachment = ParamUtil.getInt(request, "hasAttachment", 1);
String isProgress = ParamUtil.get(request, "isProgress");
String isOnlyCamera = ParamUtil.get(request, "isOnlyCamera");

int isLog = ParamUtil.getInt(request, "isLog", 0);

String unitCode = ParamUtil.get(request, "unitCode");
int isFlow = ParamUtil.getInt(request, "isFlow", 1);

String fieldsAry = ParamUtil.get(request, "fieldsAry");
FormParser fp = new FormParser();
try {
	JSONArray ary = new JSONArray(fieldsAry);
	fp.getFields(ary);
}
catch (JSONException e) {
	e.printStackTrace();
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, "表单域解析错误：" + fieldsAry, true));
	return;
}

/*FormParser fp = null;
try {
	fp = new FormParser(content);
}
catch (ResKeyException e) {
	out.print(StrUtil.jAlert_Back(e.getMessage(request), "提示"));
	return;
}
*/
Vector newv = fp.getFields();

try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	fp.validateFields();
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
}
catch (ErrMsgException e) {
	e.printStackTrace();
	out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	return;
}

Vector[] vt = fd.checkFieldChange(fd.getFields(), newv, fd.getFields());
Vector delv = vt[0];
int dellen = delv.size();
Vector addv = vt[1];
int addlen = addv.size();
%>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td class="tdStyle_1"> 对比表单域
    <span id="infoSpan" style="color:red"></span>
    </td>
  </tr>
</table>
<form name="myFormEdit" action="form_edit.jsp?op=modify" method="post" onsubmit="return myFormEdit_onsubmit()">
<table width="100%" height="89" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr> 
    <td valign="top">
      <table width="100%" border="0" align="center" cellpadding="3" cellspacing="0">
        
        <tr>
          <td colspan="3" align="center">(红色表示将被删除的字段，蓝色表示将被添加的字段，黄色背景表示字段类型被改变)</td>
          </tr>
        <tr>
          <td width="49%" valign="top">
            <table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
              <tr>
                <td class="tabStyle_1_title" height="24" colspan="4">原来的表单域</td>
                </tr>
                <tr>
                  <td height="24"><strong>字段</strong></td>
                  <td><strong>名称</strong></td>
                  <td><strong>类型</strong></td>
                  <td><strong>默认值</strong></td>
                </tr>				
				<%
// 排序
Vector vtmp = fd.getFields();
Comparator ct = new FormFieldComparator();
Collections.sort(vtmp, ct);
				
Iterator ir = vtmp.iterator();
while (ir.hasNext()) {
	FormField ff = (FormField)ir.next();
%>
                <tr>
                <td width="18%" height="24">
				<%
				// 检查是否将被删除
				boolean isDel = false;
				for (int i=0; i<dellen; i++) {
					FormField fld = (FormField)delv.get(i);
					if (fld.getName().equals(ff.getName())) {
						isDel = true;
						break;
					}
				}
				%>
				<%if (isDel) {%>
					<font color=red><%=ff.getName()%></font>
				<%}else{%>
					<%=ff.getName()%>
				<%}%>				</td>
                <td width="28%">
				<%if (isDel) {%>
					<font color=red><%=ff.getTitle()%></font>
				<%}else{%>
					<%=ff.getTitle()%>
				<%}%>				</td>
                <td width="34%">
				<%if (isDel) {%>
					<font color=red><%=ff.getTypeDesc()%></font>
				<%}else{%>
					<%=ff.getTypeDesc()%>
				<%}%>				</td>
                <td width="20%"><%if (isDel) {%>
                  <font color="red"><%=(ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) ? ff.getDefaultValueRaw() : ff.getDefaultValueRaw()%></font>
                  <%}else{%>
                  <%=(ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) ? ff.getDefaultValueRaw() : ff.getDefaultValueRaw()%>
                  <%}%></td>
              </tr>
          <%}%></table></td>
          <td width="1%">&nbsp;</td>
          <td width="50%" valign="top">
            <table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
              <tr>
                <td class="tabStyle_1_title" height="24" colspan="4">新的表单域</td>
                </tr>
                <tr>
                  <td height="24"><strong>字段</strong></td>
                  <td><strong>名称</strong></td>
                  <td><strong>类型</strong></td>
                  <td><strong>默认值</strong></td>
                </tr>				
				<%
// 排序				
Collections.sort(newv, ct);
				
// 解析content，在表form_field中建立相应的域
boolean isFieldChanged = false;
ir = newv.iterator();
while (ir.hasNext()) {
	FormField ff = (FormField)ir.next();
	
	boolean isCurFieldChanged = false;
	// 与原来的表单域比较判断类型是否被更改
	Iterator irOld = vtmp.iterator();
	while (irOld.hasNext()) {
		FormField ffOld = (FormField)irOld.next();
		if (ffOld.getName().equals(ff.getName())) {
			if (!ffOld.getType().equals(ff.getType())) {
				isFieldChanged = true;
				isCurFieldChanged = true;
			}
			else if (ffOld.getType().equals(FormField.TYPE_MACRO)) {
				// 如果是宏控件，但类型不一致
				if (!ffOld.getMacroType().equals(ff.getMacroType())) {
					isFieldChanged = true;
					isCurFieldChanged = true;
				}
			}
			break;
		}
	}
%>

              <tr>
                <td width="18%" height="24">
				<%
				// 检查是否将被增加
				boolean isAdd = false;
				for (int i=0; i<addlen; i++) {
					FormField fld = (FormField)addv.get(i);
					if (fld.getName().equals(ff.getName())) {
						isAdd = true;
						break;
					}
				}
				%>
				<%if (isAdd) {%>
					<font color=blue><%=ff.getName()%></font>
				<%}
				else if (isCurFieldChanged) {%>
					<font style="background-color:#FFFF00"><%=ff.getName()%></font>				
				<%}
				else{%>
					<%=ff.getName()%>
				<%}%>
				</td>
                <td width="28%">
				<%if (isAdd) {%>
					<font color=blue><%=ff.getTitle()%></font>
				<%}
				else if (isCurFieldChanged) {%>
					<font style="background-color:#FFFF00"><%=ff.getTitle()%></font>				
				<%}
				else{%>
					<%=ff.getTitle()%>
				<%}%>
				</td>
                <td width="34%">
				<%if (isAdd) {%>
					<font color=blue><%=ff.getTypeDesc()%></font>
				<%}
				else if (isCurFieldChanged) {%>
					<font style="background-color:#FFFF00"><%=ff.getTypeDesc()%></font>				
				<%}else{%>
					<%=ff.getTypeDesc()%>
				<%}%>
				</td>
                <td width="20%">
                <%if (isAdd) {%>
                  <font color="blue"><%=(ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) ? ff.getDefaultValueRaw() : ff.getDefaultValueRaw()%></font>
                <%}
                else if (isCurFieldChanged) {%>
                  <font style="background-color:#FFFF00"><%=(ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) ? ff.getDefaultValueRaw() : ff.getDefaultValueRaw()%></font>                
                <%}else{%>
                  <%=(ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) ? ff.getDefaultValueRaw() : ff.getDefaultValueRaw()%>
                <%}%>
                </td>
              </tr>
          <%}%></table></td>
        </tr>
        <tr>
          <td height="30" colspan="3" align="center">
		  <input type="hidden" name="code" value="<%=code%>" />
		  <input type="hidden" name="name" value="<%=name%>" />
		  <input type="hidden" name="flowTypeCode" value="<%=flowTypeCode%>" />
		  <input type="hidden" id="content" name="content" value="" />
		  <input type="hidden" name="hasAttachment" value="<%=hasAttachment%>" />
          <input type="hidden" name="isLog" value="<%=isLog%>" />
          <input type="hidden" name="unitCode" value="<%=unitCode%>" />
          <input type="hidden" name="isProgress" value="<%=isProgress%>" />
          <input type="hidden" name="isOnlyCamera" value="<%=isOnlyCamera%>" />
          <input type="hidden" name="isFlow" value="<%=isFlow%>" />
		  <textarea name="fieldsAry" style="display: none;"><%=fieldsAry%></textarea>
          <%if (isFieldChanged) { %>
          <div style="color:red; weight:bold; margin-bottom:10px;">字段类型被改变，如确定需改变，则先从表单中删除，然后再添加，注意删除后数据将丢失！</div>
          <%} %>
          <%if (!isFieldChanged) { %>
		  <input type="submit" id="submitBtn" name="Submit" value="  确定  " class="btn" />
          &nbsp;&nbsp;
          <%} %>
		  <!--<input type="button" value="  返回  " class="btn" onclick="window.location.href='form_edit.jsp?code=<%=StrUtil.UrlEncode(code)%>'" />-->
		  <input type="button" value="  返回  " class="btn" onclick="window.history.back()" />          
          </td>
        </tr>
      </table>
	</td>
  </tr>
</table>
</form>
<script>
$(function() {
	if (!isIE()) {
		// $('#infoSpan').html("设计器只能在IE内核浏览器使用，请返回!");
		// $('#submitBtn').hide();
	}
});
</script>
<br>
<br>
<table width="100%" align="center" bgcolor="#FFFFFF">
  <tr>
    <td><strong>&nbsp;&nbsp;以下为表单内容：</strong></td>
  </tr>
  <tr>
    <td><div id="divContent" name="divContent"><%=content%></div></td>
  </tr></table>
</body>
</html>
