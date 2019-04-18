<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="org.json.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.util.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="org.json.JSONObject"%>
<%@ page import="org.json.JSONArray"%>
<%
String op = ParamUtil.get(request, "op");
String code = ParamUtil.get(request, "code"); // 模块编码
String formCode = ParamUtil.get(request, "formCode");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>模块查询设置</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script src="../inc/common.js"></script>
<script src="../inc/livevalidation_standalone.js"></script>
<script src="<%=request.getContextPath()%>/js/jquery-1.9.1.min.js"></script>
<script src="<%=request.getContextPath()%>/js/jquery-migrate-1.2.1.min.js"></script>
<script src="../inc/map.js"></script>

<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css" />  
<script src="<%=request.getContextPath()%>/js/bootstrap/js/bootstrap.min.js"></script>

<script src="../js/select2/select2.js"></script>
<link href="../js/select2/select2.css" rel="stylesheet" />

<script src="../js/jquery.toaster.js"></script>

<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />

<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexbox/flexbox.css" />
<script type="text/javascript" src="../js/jquery.flexbox.js"></script>
<style>
.form-box {
	width:300px; 
	height:400px; 
	margin:0px 0px 10px 10px; 
	padding-left:10px; 
	border:1px solid #eeeeee; 
	overflow-x:auto; 
	overflow-y:auto; 
	float:left;
}
</style>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.flow")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCode);
if (!fd.isLoaded()) {
	out.print(StrUtil.jAlert_Back("该表单不存在！","提示"));
	return;
}

if (op.equals("addCond")) {
	ModuleViewMgr mvm = new ModuleViewMgr();
	boolean re = false;
	try {
		re = mvm.addCond(request, code);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;		
	}	
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_field_conds.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
} else if (op.equals("delBtn")) {
	ModuleViewMgr mvm = new ModuleViewMgr();
	boolean re = false;
	try {
		re = mvm.delBtn(request, code);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_field_conds.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
else if (op.equals("modifyBtn")) {
	ModuleViewMgr mvm = new ModuleViewMgr();
	boolean re = false;
	try {
		re = mvm.modifyBtn(request, code);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
		return;		
	}

	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_field_conds.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}

ModuleSetupDb vsd = new ModuleSetupDb();
vsd = vsd.getModuleSetupDb(code);

int i = 0;
MacroCtlMgr mm = new MacroCtlMgr();

String listField = StrUtil.getNullStr(vsd.getString("list_field"));
String[] fields = StrUtil.split(listField, ",");

String btn_name = StrUtil.getNullStr(vsd.getString("btn_name"));
String[] btnNames = StrUtil.split(btn_name, ",");

String btn_order = StrUtil.getNullStr(vsd.getString("btn_order"));
String[] btnOrders = StrUtil.split(btn_order, ",");

String btn_script = StrUtil.getNullStr(vsd.getString("btn_script"));
String[] btnScripts = StrUtil.split(btn_script, "#");
if (btn_script.replaceAll("#", "").equals(""))
	btnScripts = null;
if (btnNames!=null && btnScripts==null) {
	btnScripts = new String[btnNames.length];
	for (i=0; i<btnNames.length; i++)
		btnScripts[i] = "";
}

String btn_role = StrUtil.getNullStr(vsd.getString("btn_role"));
String[] btnRoles = StrUtil.split(btn_role, "#");
if (btn_role.replaceAll("#", "").equals(""))
	btnRoles = null;
if (btnNames!=null && btnRoles==null) {
	btnRoles = new String[btnNames.length];
	for (i=0; i<btnNames.length; i++) {
		btnRoles[i] = "";
	}
}

String btn_bclass = StrUtil.getNullStr(vsd.getString("btn_bclass"));
String[] btnBclasses = StrUtil.split(btn_bclass, ",");
// 为了与以前的版本兼容,bluewind20140420
if (btnNames!=null) {
	if (btnBclasses==null || (btnBclasses.length!=btnNames.length)) {
		btnBclasses = new String[btnNames.length];
		for (i=0; i<btnNames.length; i++)
			btnBclasses[i] = "";
	}
}

boolean hasCond = false;
int len = 0;
if (btnNames!=null)
	len = btnNames.length;
JSONObject json = null;
for (i=0; i<len; i++) {
	String btnName = btnNames[i];	
	if (btnScripts[i].startsWith("{")) {
		json = new JSONObject(btnScripts[i]);
		if (json.getString("btnType").equals("queryFields")) {
		  	hasCond = true;
		  	break;
		}
	}
}	

FormDb mainForm = fd;
Vector vt = new Vector();
// 加入主表
vt.addElement(fd);
ModuleRelateDb mrd = new ModuleRelateDb();
ModuleSetupDb msdRelate = new ModuleSetupDb();
java.util.Iterator irTop = mrd.getModulesRelated(formCode).iterator();
while (irTop.hasNext()) {
	mrd = (ModuleRelateDb)irTop.next();
	msdRelate = msdRelate.getModuleSetupDb(mrd.getString("relate_code"));
	boolean isFound = false;
	Iterator ir = vt.iterator();
	while (ir.hasNext()) {
		FormDb fdb = (FormDb)ir.next();
		// 去除重复的表单，因为副模块可能会有重复
		if (fdb.getCode().equals(msdRelate.getString("form_code"))) {
			isFound = true;
			break;
		}
	}
	if (!isFound) {
		vt.addElement(fd.getFormDb(msdRelate.getString("form_code")));
	}
}
Iterator irForm = vt.iterator();
%>
<%@ include file="module_setup_inc_menu_top.jsp"%>
<script>
o("menu10").className="current"; 
</script>
<div class="spacerH">
</div>
<%
if (hasCond) {
	String btnName = btnNames[i];
%>
<form action="module_field_conds.jsp?op=modifyBtn" method="post" name="formBtn<%=i%>" id="formBtn<%=i%>">
<table cellspacing="0" class="tabStyle_1 percent98" cellpadding="3" width="95%" align="center">
  <tr>
    <td align="center" class="tabStyle_1_title">条件</td>
  </tr>
  <tr>
    <td align="left">
    <%
	boolean isToolbar = true;
	if (json.has("isToolbar")) {
		isToolbar = json.getInt("isToolbar")==1;
	}		  
	%>
    <input type="checkbox" id="isToolbar" name="isToolbar" value="1" <%=isToolbar?"checked":""%> />&nbsp;置于工具条
    </td>
  </tr>
    <tr >
      <td>
      <%
	  irForm = vt.iterator();
	  while (irForm.hasNext()) {
	  	fd = (FormDb)irForm.next();
	  	boolean isSub = !fd.getCode().equals(mainForm.getCode());      
      %>
      	<div class="form-box">
        <div style="text-align:center; margin-bottom:10px"><%=fd.getName()%></div>
        <%
          Iterator ir = fd.getFields().iterator();
          while (ir.hasNext()) {
              FormField ff = (FormField)ir.next();
              if (!ff.isCanQuery())
                  continue;
			  String fieldDesc = ff.getName();
			  if (isSub) {
			  	fieldDesc = "sub:" + fd.getCode() + ":" + ff.getName();
			  }                  
              %>
          <div>
            <input type="checkbox" name="queryFields" value="<%=fieldDesc%>" />
            <%=ff.getTitle()%>&nbsp;&nbsp;
            <%if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {%>
            <select name="<%=fieldDesc%>_cond">
              <option value="0">时间段</option>
              <option value="1">时间点</option>
              </select>
            <%
            }else if(ff.getType().equals(FormField.TYPE_MACRO)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                %>
            <select name="<%=fieldDesc%>_cond">
              <option value="1">等于</option>
              <option value="0" selected="selected">包含</option>
              </select>
            <%
            }
            else if (ff.getFieldType()==FormField.FIELD_TYPE_INT || ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE || ff.getFieldType()==FormField.FIELD_TYPE_FLOAT || ff.getFieldType()==FormField.FIELD_TYPE_LONG || ff.getFieldType()==FormField.FIELD_TYPE_PRICE) {
            %>
            <select name="<%=fieldDesc%>_cond">
              <option value="=" selected="selected">等于</option>
              <option value="&gt;">大于</option>
              <option value="&lt;">小于</option>
              <option value="&gt;=">大于等于</option>
              <option value="&lt;=">小于等于</option>
            </select>
            <input name="<%=fieldDesc%>" type="hidden" />			
            <%
            }            
            else{%>
            <select name="<%=fieldDesc%>_cond">
              <option value="1">等于</option>
              <%if (ff.getType().equals(FormField.TYPE_TEXTFIELD) || ff.getType().equals(ff.TYPE_TEXTAREA)) {%>
              <option value="0" selected="selected">包含</option>
              <%}%>
              </select>
            <%}%>
          </div>
          <%
	  	}
		
		if (!isSub) {	  	
		  	String fieldTitle = "";
			for (int n=0; n<fields.length; n++) {
				String field = fields[n];	
				FormField ff = null;
				if (field.startsWith("main:")) { // 关联的主表
					String[] ary = StrUtil.split(field, ":");			
					field = field.substring(5);
					if (ary.length==3) {
						FormDb mainFormDb = fm.getFormDb(ary[1]);
						ff = mainFormDb.getFormField(ary[2]);
						fieldTitle = mainFormDb.getName() + "：" + ff.getTitle();
					}
					else {
						fieldTitle = field + " 不存在";
					}
				}
				else if (field.startsWith("other")) { // 映射的字段，多重映射不支持
					String[] ary = StrUtil.split(field, ":");
					if (field.length()>6) {			
						field = field.substring(6);
					}
					if (ary.length<5) {
						fieldTitle = "<font color='red'>格式非法</font>";
					}
					else {
						FormDb otherFormDb = fm.getFormDb(ary[2]);
						if (ary.length>=5) {
							ff = otherFormDb.getFormField(ary[4]);
							fieldTitle = otherFormDb.getName() + "：" + ff.getTitle();
						}
						if (ary.length>=8) {
							FormDb oFormDb = fm.getFormDb(ary[5]);
							fieldTitle += "：" + oFormDb.getFieldTitle(ary[7]);
						}
					}
				}	
				else {
					continue;
				}		
				if (ff==null) {
					%>
		          	<div><%=field%> 不存在</div>
		          	<%
					continue;
				}
				%>
	          <div>
	            <input type="checkbox" name="queryFields" value="<%=fields[n] %>" />
	            <%=fieldTitle %>&nbsp;&nbsp;
	            <%if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {%>
	            <select name="<%=fields[n]%>_cond">
	              <option value="0">时间段</option>
	              <option value="1">时间点</option>
	              </select>
	            <%
	                }else if(ff.getType().equals(FormField.TYPE_MACRO)) {
	                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
	                    %>
	            <select name="<%=fields[n]%>_cond">
	              <option value="1">等于</option>
	              <option value="0" selected="selected">包含</option>
	              </select>
	            <%
	                }
	                else if (ff.getFieldType()==FormField.FIELD_TYPE_INT || ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE || ff.getFieldType()==FormField.FIELD_TYPE_FLOAT || ff.getFieldType()==FormField.FIELD_TYPE_LONG || ff.getFieldType()==FormField.FIELD_TYPE_PRICE) {
	            %>
	            <select name="<%=fields[n]%>_cond">
	              <option value="=" selected="selected">等于</option>
	              <option value="&gt;">大于</option>
	              <option value="&lt;">小于</option>
	              <option value="&gt;=">大于等于</option>
	              <option value="&lt;=">小于等于</option>
	              </select>
	            <input name="<%=fields[n]%>" type="hidden" />			
	            <%
	                }            
	                else{%>
	            <select name="<%=fields[n]%>_cond">
	              <option value="1">等于</option>
	              <%if (ff.getType().equals(FormField.TYPE_TEXTFIELD) || ff.getType().equals(ff.TYPE_TEXTAREA)) {%>
	              <option value="0" selected="selected">包含</option>
	              <%}%>
	              </select>
	            <%}%>			
	            </div>
	          <%
			}
			%>
	          <div>
	            <input type="checkbox" name="queryFields" value="cws_status" />	    
	            记录状态
	            <select name="cws_status_cond">
	              <option value="=" selected="selected">等于</option>
	              </select>
	            <input name="cws_status" type="hidden" />
	          </div>		
	          <div>
	            <input type="checkbox" name="queryFields" value="cws_flag" />	    
	            冲抵状态
	            <select name="cws_flag_cond">
	              <option value="=" selected="selected">等于</option>
	              </select>
	            <input name="cws_flag" type="hidden" />	
	          </div>	        
	      <%}%>
      	</div>
      	<%} %>
      </td>
    </tr>
    <tr >
      <td align="center">
  		<input class="btn btn-default" type="submit" value="修改" />
        &nbsp;&nbsp;
        <input class="btn btn-default" name="button" type="button" onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{window.location.href='module_field_conds.jsp?op=delBtn&code=<%=code%>&formCode=<%=formCode%>&btnName=<%=StrUtil.UrlEncode(btnName)%>'}}) " value="删除" /> 
        <input name="formCode" value="<%=formCode%>" type="hidden" />
        <input name="code" value="<%=code%>" type="hidden" />          
        <input name="btnName" value="<%=btnName%>" type="hidden" /> 
        <input name="btnOrder" type="hidden" size="5" value="<%=btnOrders[i]%>" />        
        <input type="hidden" name="btnBclass" size="5" value="<%=btnBclasses[i]%>" />              
      </td>
    </tr>
</table>
</form>
<script>
<%
if (json.has("fields")) {
	String queryFields = json.getString("fields");
	String[] ary = StrUtil.split(queryFields, ",");
	if (ary!=null) {
		for (int k=0; k<ary.length; k++) {
			String cond = json.getString(ary[k]);
			%>
			setCheckboxChecked("queryFields", "<%=ary[k]%>");
			o("<%=ary[k]%>_cond").value = "<%=cond%>";
			<%
		}
	}
}
%>
</script>
<%} else {%>
<form action="module_field_conds.jsp?op=addCond" method="post" name="formBtn" id="formBtn">
<table cellspacing="0" class="tabStyle_1 percent98" cellpadding="3" width="95%" align="center">
    <tr >
      <td align="center" class="tabStyle_1_title">条件</td>
    </tr>
    <tr >
      <td>
      <input type="checkbox" id="isToolbar" name="isToolbar" value="1" checked />&nbsp;置于工具条      
      </td>
    </tr>
    <tr >
      <td>
      <%
	  irForm = vt.iterator();
	  while (irForm.hasNext()) {
	  	fd = (FormDb)irForm.next();
	  	boolean isSub = !fd.getCode().equals(mainForm.getCode());
	  %>    
      <div class="form-box">     
      <div style="text-align:center; margin-bottom:10px"><%=fd.getName()%></div>       
      <%
	  Iterator ir = fd.getFields().iterator();
	  while (ir.hasNext()) {
		  FormField ff = (FormField)ir.next();
		  if (!ff.isCanQuery())
			  continue;
		  String fieldDesc = ff.getName();
		  if (isSub) {
		  	fieldDesc = "sub:" + fd.getCode() + ":" + ff.getName();
		  }
		  %>
          <div>
            <input type="checkbox" name="queryFields" value="<%=fieldDesc%>" />
            <%=ff.getTitle()%>
            <%if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {%>
            <select name="<%=fieldDesc%>_cond">
              <option value="0">时间段</option>
              <option value="1">时间点</option>
              </select>
            <%
			}else if(ff.getType().equals(FormField.TYPE_MACRO)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                %>
            <select name="<%=fieldDesc%>_cond">
              <option value="1">等于</option>
              <option value="0" selected="selected">包含</option>
              </select>
            <%
            }
			else if (ff.getFieldType()==FormField.FIELD_TYPE_INT || ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE || ff.getFieldType()==FormField.FIELD_TYPE_FLOAT || ff.getFieldType()==FormField.FIELD_TYPE_LONG || ff.getFieldType()==FormField.FIELD_TYPE_PRICE) {
			%>
            <select name="<%=fieldDesc%>_cond">
              <option value="=" selected="selected">等于</option>
              <option value="&gt;">大于</option>
              <option value="&lt;">小于</option>
              <option value="&gt;=">大于等于</option>
              <option value="&lt;=">小于等于</option>
              </select>
            <input name="<%=fieldDesc%>" type="hidden" />			
            <%
			}            
            else{%>
            <select name="<%=fieldDesc%>_cond">
              <option value="1">等于</option>
              <%if (ff.getType().equals(FormField.TYPE_TEXTFIELD) || ff.getType().equals(FormField.TYPE_TEXTAREA)) {%>
              <option value="0" selected="selected">包含</option>
              <%}%>
              </select>
            <%}%>
          </div>
          <%	
	  }
	  if (!isSub) {
	  String fieldTitle = "";
	  for (int n=0; n<fields.length; n++) {
		  String field = fields[n];	
		  FormField ff = null;
		  if (field.startsWith("main:")) { // 关联的主表
			  String[] ary = StrUtil.split(field, ":");			
			  field = field.substring(5);
			  if (ary.length==3) {
				  FormDb mainFormDb = fm.getFormDb(ary[1]);
				  ff = mainFormDb.getFormField(ary[2]);
				  fieldTitle = mainFormDb.getName() + "：" + ff.getTitle();
			  }
			  else {
				  fieldTitle = field + " 不存在";
			  }
		  }
		  else if (field.startsWith("other")) { // 映射的字段，多重映射不支持
			  String[] ary = StrUtil.split(field, ":");
			  if (field.length()>6) {			
				  field = field.substring(6);
			  }
			  if (ary.length<5) {
				  fieldTitle = "<font color='red'>格式非法</font>";
			  }
			  else {
				  FormDb otherFormDb = fm.getFormDb(ary[2]);
				  if (ary.length>=5) {
					  ff = otherFormDb.getFormField(ary[4]);
					  fieldTitle = otherFormDb.getName() + "：" + ff.getTitle();
				  }
				  if (ary.length>=8) {
					  FormDb oFormDb = fm.getFormDb(ary[5]);
					  fieldTitle += "：" + oFormDb.getFieldTitle(ary[7]);
				  }
			  }
		  }	
		  else {
			  continue;
		  }		
		  if (ff==null) {
			  %>
          <div><%=field%> 不存在</div>
          <%
			  continue;
		  }
		  %>
          <div>
            <input type="checkbox" name="queryFields" value="<%=fields[n] %>" />
            <%=fieldTitle %>&nbsp;&nbsp;
            <%if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {%>
            <select name="<%=fields[n]%>_cond">
              <option value="0">时间段</option>
              <option value="1">时间点</option>
              </select>
            <%
			  }else if(ff.getType().equals(FormField.TYPE_MACRO)) {
				  MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
				  %>
            <select name="<%=fields[n]%>_cond">
              <option value="1">等于</option>
              <option value="0" selected="selected">包含</option>
              </select>
            <%
			  }
			  else if (ff.getFieldType()==FormField.FIELD_TYPE_INT || ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE || ff.getFieldType()==FormField.FIELD_TYPE_FLOAT || ff.getFieldType()==FormField.FIELD_TYPE_LONG || ff.getFieldType()==FormField.FIELD_TYPE_PRICE) {
				  %>
            <select name="<%=fields[n]%>_cond">
              <option value="=" selected="selected">等于</option>
              <option value="&gt;">大于</option>
              <option value="&lt;">小于</option>
              <option value="&gt;=">大于等于</option>
              <option value="&lt;=">小于等于</option>
              </select>
            <input name="<%=fields[n]%>" type="hidden" />			
            <%
			  }            
			  else{%>
            <select name="<%=fields[n]%>_cond">
              <option value="1">等于</option>
              <%if (ff.getType().equals(FormField.TYPE_TEXTFIELD) || ff.getType().equals(ff.TYPE_TEXTAREA)) {%>
              <option value="0" selected="selected">包含</option>
              <%}%>
              </select>
            <%}%>			
          </div>
          <%
	  }	  
	  %>
          <div>
            <input type="checkbox" name="queryFields" value="cws_status" />	    
            记录状态
            <select name="cws_status_cond">
              <option value="=" selected="selected">等于</option>
              </select>
            <input name="cws_status" type="hidden" />
          </div>
          <div>
            <input type="checkbox" name="queryFields" value="cws_flag" />	    
            冲抵状态
            <select name="cws_flag_cond">
              <option value="=" selected="selected">等于</option>
            </select>
            <input name="cws_flag" type="hidden" />	
          </div>   
		<%}%>
		</div>
		<%}%>
      </td>
    </tr>
    <tr >
      <td align="center">
		<input class="btn btn-default" type="submit" value="添加查询" />
        <input name="formCode" value="<%=formCode%>" type="hidden" />
        <input name="code" value="<%=code%>" type="hidden" />
        <input name="btnName" value="查询" type="hidden" />     
        <input name="btnOrder" size="5" type="hidden" value="<%=btnNames!=null?StrUtil.toDouble(btnOrders[i-1])+1:1%>" />      
      </td>
    </tr>
</table>
</form>
<%}%>
</body>
</html>