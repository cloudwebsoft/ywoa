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
<%@ page import="org.json.JSONObject" %>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb" %>
<%@ page import="com.redmoon.oa.flow.macroctl.ModuleFieldSelectCtl" %>
<%
response.setHeader("X-xss-protection", "0;mode=block");
%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8;"/>
	<title>编辑表单</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
	<style>
		.nest-link {
			margin: 5px 0px;
		}

		.loading {
			display: none;
			position: fixed;
			z-index: 1801;
			top: 45%;
			left: 45%;
			width: 100%;
			margin: auto;
			height: 100%;
		}

		.SD_overlayBG2 {
			background: #FFFFFF;
			filter: alpha(opacity=20);
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
			filter: alpha(opacity=80);
		}
	</style>
	<%@ include file="../inc/nocache.jsp" %>
	<script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<script src="../js/bootstrap/js/bootstrap.min.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	<script>
		function getFormContent() {
			return divContent.innerHTML;
		}

		function myFormEdit_onsubmit() {
			$('#content').val(getFormContent());
		}
	</script>
</head>
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif' /></div>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String priv = "read";
	if (!privilege.isUserPrivValid(request, priv)) {
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
	} catch (JSONException e) {
		e.printStackTrace();
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, "表单域解析错误：" + fieldsAry, true));
		return;
	}

	Vector newv = fp.getFields();

	try {
%>
<script>
	$(".treeBackground").addClass("SD_overlayBG2");
	$(".treeBackground").css({"display": "block"});
	$(".loading").css({"display": "block"});
</script>
<%
	fp.validateFields();
%>
<script>
	$(".loading").css({"display": "none"});
	$(".treeBackground").css({"display": "none"});
	$(".treeBackground").removeClass("SD_overlayBG2");
</script>
<%
	} catch (ErrMsgException e) {
		e.printStackTrace();
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;
	}

	Vector[] vt = FormDb.checkFieldChange(fd.getFields(), newv, fd.getFields());
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
<form id="myFormEdit" action="form_edit.jsp?op=modify" method="post" onsubmit="return myFormEdit_onsubmit()">
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
Vector<FormField> vtmp = fd.getFields();
Comparator ct = new FormFieldComparator();
Collections.sort(vtmp, ct);

List<FormField> delList = new ArrayList<FormField>();
List<FormField> addList = new ArrayList<FormField>();
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
						delList.add(fld);
						break;
					}
				}
				%>
				<%if (isDel) {%>
					<span style="color:red"><%=ff.getName()%></span>
				<%}else{%>
					<%=ff.getName()%>
				<%}%>
				</td>
                <td width="28%">
				<%if (isDel) {%>
					<font color=red><%=ff.getTitle()%></font>
				<%}else{%>
					<%=ff.getTitle()%>
				<%}

				if ("nest_table".equals(ff.getMacroType()) || "nest_sheet".equals(ff.getMacroType())) {
					String defaultVal = StrUtil.decodeJSON(ff.getDescription());
					JSONObject jsonObj = new JSONObject(defaultVal);
					String nestModuleCode = jsonObj.getString("destForm");
					String sourceModuleCode = jsonObj.getString("sourceForm");

					ModuleSetupDb nestMsd = new ModuleSetupDb();
					nestMsd = nestMsd.getModuleSetupDb(nestModuleCode);
					StringBuilder sb = new StringBuilder();
					sb.append("<div class='nest-link'><a href='javascript:;' onclick=\"addTab('" + nestMsd.getString("name") + "', '" + request.getContextPath() + "/visual/module_field_list.jsp?formCode=" + nestMsd.getString("form_code") + "&code=" + nestModuleCode + "')\">嵌套模块</a>：");
					sb.append("<a href='javascript:;' onclick=\"addTab('" + nestMsd.getString("name") + "', '" + request.getContextPath() + "/admin/form_edit.jsp?code=" + nestMsd.getString("form_code") + "')\">" + nestMsd.getString("name") + "</a>");
					sb.append("</div>");
					out.print(sb);

					if (!"".equals(sourceModuleCode)) {
						ModuleSetupDb sourceMsd = new ModuleSetupDb();
						sourceMsd = sourceMsd.getModuleSetupDb(sourceModuleCode);
						sb = new StringBuilder();
						sb.append("<div class='nest-link'><a href='javascript:;' onclick=\"addTab('" + sourceMsd.getString("name") + "', '" + request.getContextPath() + "/visual/module_field_list.jsp?formCode=" + sourceMsd.getString("form_code") + "&code=" + sourceModuleCode + "')\">来源模块</a>：");
						sb.append("<a href='javascript:;' onclick=\"addTab('" + sourceMsd.getString("name") + "', '" + request.getContextPath() + "/admin/form_edit.jsp?code=" + sourceMsd.getString("form_code") + "')\">" + sourceMsd.getString("name") + "</a>");
						sb.append("</div>");
						out.print(sb);
					}
				}
				else if ("module_field_select".equals(ff.getMacroType())) {
					String strDesc = StrUtil.getNullStr(ff.getDescription());
					// 向下兼容
					if ("".equals(strDesc)) {
						strDesc = ff.getDefaultValueRaw();
					}
					JSONObject json;
					try {
						strDesc = ModuleFieldSelectCtl.formatJSONStr(strDesc);
						json = new JSONObject(strDesc);
						String moduleCode = json.getString("sourceFormCode");
						ModuleSetupDb msd = new ModuleSetupDb();
						msd = msd.getModuleSetupDb(moduleCode);
						StringBuilder sb = new StringBuilder();
						sb.append("<div class=nest-link><a href='javascript:;' onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/visual/module_field_list.jsp?formCode=" + msd.getString("form_code") + "&code=" + moduleCode + "')\">来源模块</a>：");
						sb.append("<a href='javascript:;' onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/admin/form_edit.jsp?code=" + msd.getString("form_code") + "')\">" + msd.getString("name") + "</a>");
						sb.append("</div>");
						out.print(sb);
					}
					catch (JSONException e) {
						e.printStackTrace();
					}
				}
				%>
				</td>
                <td width="34%">
				<%if (isDel) {%>
					<span style="color:red"><%=ff.getTypeDesc()%></span>
				<%}else{%>
					<%=ff.getTypeDesc()%>
				<%}%>
				</td>
                <td width="20%"><%if (isDel) {%>
                  <span style="color:red"><%=(ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) ? ff.getDefaultValueRaw() : ff.getDefaultValueRaw()%></span>
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
						addList.add(fld);
						break;
					}
				}
				%>
				<%if (isAdd) {%>
					<span style="color:blue"><%=ff.getName()%></span>
				<%}
				else if (isCurFieldChanged) {%>
					<span style="background-color:#FFFF00"><%=ff.getName()%></span>
				<%}
				else{%>
					<%=ff.getName()%>
				<%
				}
				%>
				</td>
                <td width="28%">
				<%if (isAdd) {%>
					<span style="color:blue"><%=ff.getTitle()%></span>
				<%}
				else if (isCurFieldChanged) {%>
					<span style="background-color:#FFFF00"><%=ff.getTitle()%></span>
				<%}
				else{%>
					<%=ff.getTitle()%>
				<%
				}

					if ("nest_table".equals(ff.getMacroType()) || "nest_sheet".equals(ff.getMacroType())) {
						String defaultVal = StrUtil.decodeJSON(ff.getDescription());
						JSONObject jsonObj = new JSONObject(defaultVal);
						String nestModuleCode = jsonObj.getString("destForm");
						String sourceModuleCode = jsonObj.getString("sourceForm");

						ModuleSetupDb nestMsd = new ModuleSetupDb();
						nestMsd = nestMsd.getModuleSetupDb(nestModuleCode);
						StringBuilder sb = new StringBuilder();
						sb.append("<div class='nest-link'><a href='javascript:;' onclick=\"addTab('" + nestMsd.getString("name") + "', '" + request.getContextPath() + "/visual/module_field_list.jsp?formCode=" + nestMsd.getString("form_code") + "&code=" + nestModuleCode + "')\">嵌套模块</a>：");
						sb.append("<a href='javascript:;' onclick=\"addTab('" + nestMsd.getString("name") + "', '" + request.getContextPath() + "/admin/form_edit.jsp?code=" + nestMsd.getString("form_code") + "')\">" + nestMsd.getString("name") + "</a>");
						sb.append("</div>");
						out.print(sb);

						if (!"".equals(sourceModuleCode)) {
							ModuleSetupDb sourceMsd = new ModuleSetupDb();
							sourceMsd = sourceMsd.getModuleSetupDb(sourceModuleCode);
							sb = new StringBuilder();
							sb.append("<div class=nest-link><a href='javascript:;' onclick=\"addTab('" + sourceMsd.getString("name") + "', '" + request.getContextPath() + "/visual/module_field_list.jsp?formCode=" + sourceMsd.getString("form_code") + "&code=" + sourceModuleCode + "')\">来源模块</a>：");
							sb.append("<a href='javascript:;' onclick=\"addTab('" + sourceMsd.getString("name") + "', '" + request.getContextPath() + "/admin/form_edit.jsp?code=" + sourceMsd.getString("form_code") + "')\">" + sourceMsd.getString("name") + "</a>");
							sb.append("</div>");
							out.print(sb);
						}
					}
					else if ("module_field_select".equals(ff.getMacroType())) {
						String strDesc = StrUtil.getNullStr(ff.getDescription());
						// 向下兼容
						if ("".equals(strDesc)) {
							strDesc = ff.getDefaultValueRaw();
						}
						JSONObject json;
						try {
							strDesc = ModuleFieldSelectCtl.formatJSONStr(strDesc);
							json = new JSONObject(strDesc);
							String moduleCode = json.getString("sourceFormCode");
							ModuleSetupDb msd = new ModuleSetupDb();
							msd = msd.getModuleSetupDb(moduleCode);
							StringBuilder sb = new StringBuilder();
							sb.append("<div class=nest-link><a href='javascript:;' onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/visual/module_field_list.jsp?formCode=" + msd.getString("form_code") + "&code=" + moduleCode + "')\">来源模块</a>：");
							sb.append("<a href='javascript:;' onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/admin/form_edit.jsp?code=" + msd.getString("form_code") + "')\">" + msd.getString("name") + "</a>");
							sb.append("</div>");
							out.print(sb);
						}
						catch (JSONException e) {
							e.printStackTrace();
						}
					}
				%>
				</td>
                <td width="34%">
				<%if (isAdd) {%>
					<span style="color:blue"><%=ff.getTypeDesc()%></span>
				<%}
				else if (isCurFieldChanged) {%>
					<span style="background-color:#FFFF00"><%=ff.getTypeDesc()%></span>
				<%}else{%>
					<%=ff.getTypeDesc()%>
				<%}%>
				</td>
                <td width="20%">
                <%if (isAdd) {%>
                  <span style="color:blue"><%=(ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) ? ff.getDefaultValueRaw() : ff.getDefaultValueRaw()%></span>
                <%}
                else if (isCurFieldChanged) {%>
                  <span style="background-color:#FFFF00"><%=(ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) ? ff.getDefaultValueRaw() : ff.getDefaultValueRaw()%></span>
                <%}else{%>
                  <%=(ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) ? ff.getDefaultValueRaw() : ff.getDefaultValueRaw()%>
                <%}%>
                </td>
              </tr>
          <%}%></table></td>
        </tr>
		  <tr>
			  <td style="padding-left: 20px">
				  <%
					  if (addList.size() == 0 && delList.size() == 0) {
						  out.print("<span style='color:green'>未增加或删除字段</span>");
					  }

					  boolean hasFieldAdded = false;
					  StringBuffer sbAdded = new StringBuffer();
					  if (addList.size() > 0) {
						  hasFieldAdded = true;
						  for (FormField ff : addList) {
							  StrUtil.concat(sbAdded, "，", ff.getTitle());
						  }
						  out.print("<span style='color:blue'>增加字段：" + sbAdded.toString() + "</span><br/>");
					  }

					  boolean hasFieldDeleted = false;
					  StringBuffer sbDeleted = new StringBuffer();
					  if (delList.size() > 0) {
						  hasFieldDeleted = true;
						  for (FormField ff : delList) {
							  StrUtil.concat(sbDeleted, "，", ff.getTitle());
						  }
						  out.print("<span style='color:red'>删除字段：" + sbDeleted.toString() + "</span>");
					  }
				  %>
			  </td>
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
		  <input type="button" id="submitBtn" value="  确定  " class="btn btn-default" />
          &nbsp;&nbsp;
          <%} %>
		  <input type="button" value="  返回  " class="btn btn-default" onclick="window.location.href='form_edit.jsp?code=<%=StrUtil.UrlEncode(code)%>'" />
		  <%--<input type="button" value="  返回  " class="btn btn-default" onclick="window.history.back()" />--%>
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

	$('#submitBtn').click(function(e) {
		e.preventDefault();
		<%
		if (hasFieldAdded && hasFieldDeleted) {
		%>
		jConfirm("您确定要添加下列字段：<%=sbAdded%> \r\n 删除下列字段：<%=sbDeleted%> ？", "提示", function (r) {
			if (r) {
				$('#myFormEdit').submit();
			}
		});
		<%
		}
		else if (hasFieldDeleted) {
		%>
		jConfirm("您确定要删除下列字段：<%=sbDeleted%> ？", "提示", function (r) {
			if (r) {
				$('#myFormEdit').submit();
			}
		});
		<%
		}
		else if (hasFieldAdded) {
		%>
		jConfirm("您确定要添加下列字段：<%=sbAdded%> ？", "提示", function (r) {
			if (r) {
				$('#myFormEdit').submit();
			}
		});
		<%
		}
		else {
		%>
		jConfirm("您确定要修改么？", "提示", function (r) {
			if (r) {
				$('#myFormEdit').submit();
			}
		});
		<%
		}
		%>
	});
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
