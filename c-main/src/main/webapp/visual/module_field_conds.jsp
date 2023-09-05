<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.ParamUtil"%>
<%@ page import="cn.js.fan.util.StrUtil"%>
<%@ page import="cn.js.fan.web.Global"%>
<%@ page import="com.cloudweb.oa.api.IBasicSelectCtl"%>
<%@ page import="com.cloudweb.oa.api.ICloudUtil"%>
<%@ page import="com.cloudweb.oa.service.MacroCtlService"%>
<%@ page import="com.cloudweb.oa.utils.SpringUtil"%>
<%@ page import="com.cloudwebsoft.framework.util.IPUtil"%>
<%@ page import="com.redmoon.oa.Config"%>
<%@ page import="com.redmoon.oa.basic.SelectDb"%>
<%@ page import="com.redmoon.oa.basic.SelectMgr"%>
<%@ page import="com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.flow.FormMgr"%>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlMgr"%>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlUnit"%>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="com.redmoon.oa.visual.ModuleRelateDb" %>
<%@ page import="com.redmoon.oa.visual.SQLBuilder" %>
<%@ page import="org.apache.http.client.utils.URIBuilder" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Vector" %>
<%@ page import="com.redmoon.oa.visual.ModuleUtil" %>
<%
	String op = ParamUtil.get(request, "op");
	String code = ParamUtil.get(request, "code"); // 模块编码
	String formCode = ParamUtil.get(request, "formCode");

	ModuleSetupDb msd = new ModuleSetupDb();
	msd = msd.getModuleSetupDb(code);
	String tName = StrUtil.getNullStr(msd.getString("btn_name"));
	String tOrder = StrUtil.getNullStr(msd.getString("btn_order"));
	String tScript = StrUtil.getNullStr(msd.getString("btn_script"));
	String tBclass = StrUtil.getNullStr(msd.getString("btn_bclass"));
	String tRole = StrUtil.getNullStr(msd.getString("btn_role"));

	Config cfg = new Config();
	boolean isServerConnectWithCloud = cfg.getBooleanProperty("isServerConnectWithCloud");

	ICloudUtil cloudUtil = SpringUtil.getBean(ICloudUtil.class);
	String userSecret = cloudUtil.getUserSecret();
	String ip = IPUtil.getRemoteAddr(request);
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>模块查询设置</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
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
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexbox/flexbox.css" />
<script type="text/javascript" src="../js/jquery.flexbox.js"></script>

<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

<link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
<script src="../js/layui/layui.js" charset="utf-8"></script>

<script type="text/javascript" src="../js/formpost.js"></script>
<script src="../js/json2.js"></script>
<style>
	.form-box {
		width:570px;
		height:400px;
		margin:0 0 10px 10px;
		padding-left:10px;
		border:1px solid #eeeeee;
		overflow-x:auto;
		overflow-y:auto;
		float:left;
	}
	.cond-title {
		width: 70px !important;
	}
	.form-box div {
		margin-top: 5px;
		white-space: nowrap;
	}
	.cond-width {
		width: 50px !important;
	}
	.cond-default {
		width: 120px !important;
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
if ("".equals(btn_script.replaceAll("#", ""))) {
	btnScripts = null;
}
if (btnNames!=null && btnScripts==null) {
	btnScripts = new String[btnNames.length];
	for (i=0; i<btnNames.length; i++) {
		btnScripts[i] = "";
	}
}

String btn_role = StrUtil.getNullStr(vsd.getString("btn_role"));
String[] btnRoles = StrUtil.split(btn_role, "#");
if ("".equals(btn_role.replaceAll("#", ""))) {
	btnRoles = null;
}
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
		for (i=0; i<btnNames.length; i++) {
			btnBclasses[i] = "";
		}
	}
}

boolean hasCond = false;
int len = 0;
if (btnNames!=null) {
	len = btnNames.length;
}
JSONObject json = null;
for (i=0; i<len; i++) {
	String btnName = btnNames[i];	
	if (btnScripts[i].startsWith("{") && btnScripts[i].endsWith("}")) {
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
	if (msdRelate==null) {
		DebugUtil.e("module_field_conds.jsp", "", "表单 " + mainForm.getName() + " 的关联模块 " + mrd.getString("relate_code") + " 不存在");
		msdRelate = new ModuleSetupDb();
		continue;
	}
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

String url = cfg.get("cloudUrl");
URIBuilder uriBuilder = new URIBuilder(url);
String host = uriBuilder.getHost();
int port = uriBuilder.getPort();
if (port==-1) {
	port = 80;
}
String path = uriBuilder.getPath();
if (path.startsWith("/")) {
	path = path.substring(1);
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
	boolean isAutoExpand = true;
	if (json.has("isAutoExpand")) {
		isAutoExpand = json.getInt("isAutoExpand")==1;
	}
	boolean isShowEmptyValMenu = true;
	if (json.has("isShowEmptyValMenu")) {
		isShowEmptyValMenu = json.getInt("isShowEmptyValMenu")==1;
	}
	%>
		<input type="checkbox" id="isAutoExpand" name="isAutoExpand" value="1" <%=isAutoExpand?"checked":""%>  title="如勾选，则显示全部条件，如不勾选，则条件默认只显示一行" />&nbsp;自动展开
		<input type="checkbox" id="isShowEmptyValMenu" name="isShowEmptyValMenu" value="1" <%=isShowEmptyValMenu?"checked":""%>  title="如勾选，则显示全部条件，如不勾选，则条件默认只显示一行" />&nbsp;显示空值选择菜单
    </td>
  </tr>
    <tr >
      <td>
      <%
	  irForm = vt.iterator();
	  int m = 0;
	  while (irForm.hasNext()) {
	  	fd = (FormDb)irForm.next();
	  	boolean isSub = !fd.getCode().equals(mainForm.getCode());
	  	m++;
      %>
      	<div class="form-box">
        <div style="text-align:center; margin-bottom:10px"><%=fd.getName()%></div>
        <%
          Iterator ir = fd.getFields().iterator();
          while (ir.hasNext()) {
              FormField ff = (FormField)ir.next();
              if (!ff.isCanQuery()) {
				  continue;
			  }
			  String fieldDesc = ff.getName();
			  if (isSub) {
			  	fieldDesc = "sub:" + fd.getCode() + ":" + ff.getName();
			  }                  
              %>
          <div>
            <input type="checkbox" name="queryFields" value="<%=fieldDesc%>" />
			  <%=ff.getTitle()%>&nbsp;&nbsp;
			  <input name="<%=fieldDesc%>_title" class="cond-title" title="别名"/>
            <%if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {%>
            <select name="<%=fieldDesc%>_cond">
              <option value="0">时间段</option>
              <option value="1">时间点</option>
              </select>
            <%
            }else if(ff.getType().equals(FormField.TYPE_MACRO)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                if (mu == null) {
					out.print(ff.getTitle() + " 宏控件类型不存在");
					continue;
				}
                int fieldType = mu.getIFormMacroCtl().getFieldType(ff);
                if ("module_field_select".equals(ff.getMacroType())) {
					%>
					  <select name="<%=fieldDesc%>_cond">
						<option value="1">等于</option>
						<option value="0" selected="selected">包含</option>
					  </select>
			  		<%
				}
				else {
					if (fieldType==FormField.FIELD_TYPE_INT || fieldType==FormField.FIELD_TYPE_DOUBLE || fieldType==FormField.FIELD_TYPE_FLOAT || fieldType==FormField.FIELD_TYPE_LONG || fieldType==FormField.FIELD_TYPE_PRICE) {
				  %>
				  <select name="<%=fieldDesc%>_cond">
					  <option value="=" selected="selected">等于</option>
					  <option value="&gt;">大于</option>
					  <option value="&lt;">小于</option>
					  <option value="&gt;=">大于等于</option>
					  <option value="&lt;=">小于等于</option>
					  <option value="<%=SQLBuilder.COND_TYPE_SCOPE%>">数值范围</option>
				  </select>
				  <input name="<%=fieldDesc%>" type="hidden" />
				  <%
					}
					else {
					%>
				<select name="<%=fieldDesc%>_cond">
				  <option value="1">等于</option>
				  <option value="0" selected="selected">包含</option>
					<%
						// 基础数据
						if ("macro_flow_select".equals(ff.getMacroType())) {
							MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
							IBasicSelectCtl basicSelectCtl = macroCtlService.getBasicSelectCtl();
							SelectMgr sm = new SelectMgr();
							SelectDb sd = sm.getSelect(basicSelectCtl.getCode(ff));
							if (sd.getType() == SelectDb.TYPE_LIST) {
						%>
						<option value="<%=SQLBuilder.COND_TYPE_MULTI%>">多选</option>
						<%
							}
						} else if ("macro_basic_tree_select_ctl".equals(ff.getMacroType())) {
						%>
						<option value="<%=SQLBuilder.COND_TYPE_MULTI%>">多选</option>
						<%
						}
					%>
				  </select>
				<%
					}
				}
            }
            else if (ff.getFieldType()==FormField.FIELD_TYPE_INT || ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE || ff.getFieldType()==FormField.FIELD_TYPE_FLOAT || ff.getFieldType()==FormField.FIELD_TYPE_LONG || ff.getFieldType()==FormField.FIELD_TYPE_PRICE) {
            %>
            <select name="<%=fieldDesc%>_cond">
              <option value="=" selected="selected">等于</option>
              <option value="&gt;">大于</option>
              <option value="&lt;">小于</option>
              <option value="&gt;=">大于等于</option>
              <option value="&lt;=">小于等于</option>
				<option value="<%=SQLBuilder.COND_TYPE_SCOPE%>">数值范围</option>
            </select>
            <input name="<%=fieldDesc%>" type="hidden" />			
            <%
            }            
            else{
			%>
            <select name="<%=fieldDesc%>_cond">
              <option value="1">等于</option>
              <%if (ff.getType().equals(FormField.TYPE_TEXTFIELD) || ff.getType().equals(FormField.TYPE_TEXTAREA)) {%>
              <option value="0" selected="selected">包含</option>
              <%}%>
				<%
					if (ff.getType().equals(FormField.TYPE_SELECT)) {
				%>
				<option value="<%=SQLBuilder.COND_TYPE_MULTI%>">多选</option>
				<%
					}
				%>
              </select>
            <%
            }
			%>
			  宽度
			  <input name="<%=fieldDesc%>_width" value="" class="cond-width"/>
			  默认
			  <input name="<%=fieldDesc%>_default" value="" class="cond-default"/>
			  <%
				  if ("macro_year_ctl".equals(ff.getMacroType())) {
			  %>
			  <a href="javascript:;" onclick="$('[name=<%=fieldDesc%>_default]').val('{#curYear}')">当年</a>
			  <%
				  } else if ("macro_month_ctl".equals(ff.getMacroType())) {
			  %>
			  <a href="javascript:;" onclick="$('[name=<%=fieldDesc%>_default]').val('{#curMonth}')">当月</a>
			  <%
				  }
			  %>
		  </div>
          <%
	  	}
		
		if (!isSub) {	  	
		  	String fieldTitle = "";
		  	// 使列表中的main:及other:字段也可以参与查询
			for (int n=0; n<fields.length; n++) {
				String field = fields[n];	
				FormField ff = null;
				boolean isOtherShowNameId = false;
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
						if (ary.length>=5) {
							FormDb otherFormDb = fm.getFormDb(ary[2]);
							if (!otherFormDb.isLoaded()) {
								out.print("<div>" + ary[2] + "不存在</div>");
							}
							else {
								if ("id".equalsIgnoreCase(ary[4])) {
									isOtherShowNameId = true;
									fieldTitle = otherFormDb.getName() + "：" + "ID";
								}
								else {
									ff = otherFormDb.getFormField(ary[4]);
									fieldTitle = otherFormDb.getName() + "：" + ff.getTitle();
								}
							}
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
					// 当映射字段且显示字段为ID时
					if (isOtherShowNameId) {
						ff = new FormField();
						ff.setFieldType(FormField.FIELD_TYPE_LONG);
						ff.setType(FormField.TYPE_TEXTFIELD);
					}
					else {
					%>
		          		<div><%=field%> 不存在</div>
		          	<%
						continue;
					}
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
						if (mu == null) {
							out.print(ff.getTitle() + " 宏控件类型不存在");
							continue;
						}
	                    int fieldType = mu.getIFormMacroCtl().getFieldType(ff);
						if (fieldType == FormField.FIELD_TYPE_INT || fieldType == FormField.FIELD_TYPE_LONG
								|| fieldType == FormField.FIELD_TYPE_FLOAT || fieldType == FormField.FIELD_TYPE_DOUBLE || fieldType == FormField.FIELD_TYPE_PRICE) {
						%>
						  <select name="<%=fields[n]%>_cond">
							  <option value="=" selected="selected">等于</option>
							  <option value="&gt;">大于</option>
							  <option value="&lt;">小于</option>
							  <option value="&gt;=">大于等于</option>
							  <option value="&lt;=">小于等于</option>
						  </select>
				  		<%
						}
						else {
						%>
						  <select name="<%=fields[n]%>_cond">
							  <option value="1">等于</option>
							  <option value="0" selected="selected">包含</option>
						  </select>
				  		<%
						}
	                    %>
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
	              <%if (ff.getType().equals(FormField.TYPE_TEXTFIELD) || ff.getType().equals(FormField.TYPE_TEXTAREA)) {%>
	              <option value="0" selected="selected">包含</option>
	              <%}%>
	              </select>
	            <%}%>
				  宽度
				  <input name="<%=fields[n]%>_width" value="" class="cond-width"/>
				  默认
				  <input name="<%=fields[n]%>_default" value="" class="cond-default"/>
	            </div>
	          <%
			}

			if (m==1) {
			%>
			<div>
			<input type="checkbox" name="queryFields" value="ID" />
			ID&nbsp;&nbsp;
			<select name="ID_cond">
				<option value="=" selected="selected">等于</option>
				<option value="&gt;">大于</option>
				<option value="&lt;">小于</option>
				<option value="&gt;=">大于等于</option>
				<option value="&lt;=">小于等于</option>
			</select>
				宽度
				<input name="ID_width" value="" class="cond-width"/>
			</div>
			<%
				if (fd.isFlow()) {
			%>
			<div>
				<input type="checkbox" name="queryFields" value="flowId" />
				流程号&nbsp;&nbsp;
				<select name="flowId_cond">
					<option value="=" selected="selected">等于</option>
					<option value="&gt;">大于</option>
					<option value="&lt;">小于</option>
					<option value="&gt;=">大于等于</option>
					<option value="&lt;=">小于等于</option>
				</select>
				宽度
				<input name="flowId_width" value="" class="cond-width"/>
			</div>
			<div>
				<input type="checkbox" name="queryFields" value="flow:begin_date"/>
				流程开始时间
				<select name="flow:begin_date_cond">
					<option value="0">时间段</option>
					<option value="1">时间点</option>
				</select>
				宽度
				<input name="flow:begin_date_width" value="" class="cond-width"/>
			</div>
			<div>
				<input type="checkbox" name="queryFields" value="flow:end_date"/>
				流程结束时间
				<select name="flow:end_date_cond">
					<option value="0">时间段</option>
					<option value="1">时间点</option>
				</select>
				宽度
				<input name="flow:end_date_width" value="" class="cond-width"/>
			</div>
			<%
				}
			}
			%>
	          <div>
	            <input type="checkbox" name="queryFields" value="cws_status" />	    
	            记录状态
	            <select name="cws_status_cond">
	              <option value="=" selected="selected">等于</option>
	              </select>
	            <input name="cws_status" type="hidden" />
				  宽度
				  <input name="cws_status_width" value="" class="cond-width"/>
	          </div>		
	          <div>
	            <input type="checkbox" name="queryFields" value="cws_flag" />	    
	            冲抵状态
	            <select name="cws_flag_cond">
	              <option value="=" selected="selected">等于</option>
	              </select>
	            <input name="cws_flag" type="hidden" />
				  宽度
				  <input name="cws_flag_width" value="" class="cond-width"/>
	          </div>
			<div>
				<input type="checkbox" name="queryFields" value="cws_id" />
				关联ID&nbsp;&nbsp;
				<select name="cws_id_cond">
					<option value="=" selected="selected">等于</option>
					<option value="&gt;">大于</option>
					<option value="&lt;">小于</option>
					<option value="&gt;=">大于等于</option>
					<option value="&lt;=">小于等于</option>
				</select>
				宽度
				<input name="cws_id_width" value="" class="cond-width"/>
			</div>
	      <%}%>
      	</div>
      	<%} %>
      </td>
    </tr>
    <tr>
      <td align="center">
  		<input class="btn btn-default" type="button" value="修改" onclick="submitFormBtn()" />
        &nbsp;&nbsp;
        <input class="btn btn-default" name="button" type="button" onclick="delCond()" value="删除" />
        <input name="formCode" value="<%=formCode%>" type="hidden" />
        <input name="code" value="<%=code%>" type="hidden" />          
        <input name="btnName" value="<%=btnName%>" type="hidden" /> 
        <input name="btnOrder" type="hidden" size="5" value="<%=btnOrders[i]%>" />        
        <input type="hidden" name="btnBclass" size="5" value="<%=btnBclasses[i]%>" />              
      </td>
    </tr>
	<tr><td>
		注：<BR/>
		一行的总宽度为24，宽度如果为空，则默认为6，即1/4的宽度<br/>
		当条件为时间段或数值范围时，默认值无效
	</td></tr>
</table>
</form>
<script>
	function delCond() {
		jConfirm('您确定要删除么？', '提示', function (r) {
			if (!r) {
				return;
			} else {
				$.ajax({
					type: "post",
					url: "condDel.do",
					contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
					data: {
						code: "<%=code%>",
						formCode: "<%=formCode%>",
						btnName: "<%=btnName%>"
					},
					dataType: "html",
					beforeSend: function (XMLHttpRequest) {
						$('body').showLoading();
					},
					success: function (data, status) {
						data = $.parseJSON(data);
						if (data.ret=="1") {
							layer.alert(data.msg, {
								yes: function() {
									window.location.reload();
								}
							});
						}
						else {
							layer.msg(data.msg, {
								offset: '6px'
							});
						}
					},
					complete: function (XMLHttpRequest, status) {
						$('body').hideLoading();
					},
					error: function (XMLHttpRequest, textStatus) {
						// 请求出错处理
						alert(XMLHttpRequest.responseText);
					}
				});
			}
		})
	}
	function submitFormBtn() {
		<%
        if (isServerConnectWithCloud) {
        %>
		$.ajax({
			type: "post",
			url: "condModify.do",
			contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
			data: $('#formBtn<%=i%>').serialize(),
			dataType: "html",
			beforeSend: function (XMLHttpRequest) {
				$('body').showLoading();
			},
			success: function (data, status) {
				data = $.parseJSON(data);
				layer.msg(data.msg, {
					offset: '6px'
				});
			},
			complete: function (XMLHttpRequest, status) {
				$('body').hideLoading();
			},
			error: function (XMLHttpRequest, textStatus) {
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});
		<%
        } else {
        %>

		var we = o("webedit");
		we.PostScript = "<%=path%>/public/module/modifyBtn.do";

		loadDataToWebeditCtrl(o("formBtn<%=i%>"), we);
		we.AddField("cwsVersion", "<%=cfg.get("version")%>");
		we.AddField("userSecret", "<%=userSecret%>");
		we.AddField("ip", "<%=ip%>");

		we.AddField("tName", "<%=tName%>");
		we.AddField("tOrder", "<%=tOrder%>");
		we.AddField("tScript", "<%=tScript.replaceAll("\"", "\\\\\"")%>");
		we.AddField("tBclass", "<%=tBclass%>");
		we.AddField("tRole", "<%=tRole%>");

		we.AddField("isWebedit", "true");

		we.UploadToCloud();

		consoleLog(we.ReturnMessage);

		var data = $.parseJSON(we.ReturnMessage);
		if (data.ret=="1") {
			$.ajax({
				type: "post",
				url: "btnSave.do",
				contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
				data: {
					code: "<%=code%>",
					formCode: "<%=formCode%>",
					result: JSON.stringify(data.result)
				},
				dataType: "html",
				beforeSend: function (XMLHttpRequest) {
					$('body').showLoading();
				},
				success: function (data, status) {
					data = $.parseJSON(data);
					layer.msg(data.msg, {
						offset: '6px'
					});
				},
				complete: function (XMLHttpRequest, status) {
					$('body').hideLoading();
				},
				error: function (XMLHttpRequest, textStatus) {
					// 请求出错处理
					alert(XMLHttpRequest.responseText);
				}
			});
		}
		else {
			layer.msg(data.msg, {
				offset: '6px'
			});
		}
		<%
        }
        %>
	}

<%
if (json.has("fields")) {
	String queryFields = json.getString("fields");
	String titles = "", widths = "", defaults = "";
	if (json.has("titles")) {
		titles = json.getString("titles");
	}
	if (json.has("widths")) {
		widths = json.getString("widths");
	}
	if (json.has("defaults")) {
		defaults = ModuleUtil.decodeFilter(json.getString("defaults"));
	}
	String[] ary = StrUtil.split(queryFields, ",");
	String[] aryTitle = StrUtil.split(titles, ",");
	String[] aryW = StrUtil.split(widths, ",");
	String[] aryDefault = StrUtil.split(defaults, ",");
	if (ary!=null) {
		for (int k=0; k<ary.length; k++) {
			if (json.has(ary[k])) {
				String cond = json.getString(ary[k]);
				%>
				setCheckboxChecked("queryFields", "<%=ary[k]%>");
				if (o("<%=ary[k]%>_cond")) {
                    o("<%=ary[k]%>_cond").value = "<%=cond%>";
                }
				else {
				    console.log("条件中的字段：<%=ary[k]%>已不存在");
                }
				<%
				// 向下兼容
				if (aryTitle!=null) {
				%>
				if (o("<%=ary[k]%>_title")) {
					o("<%=ary[k]%>_title").value = "<%=aryTitle[k]%>";
				}
				<%
				}
				if (aryW != null) {
				%>
				if (o("<%=ary[k]%>_width")) {
					o("<%=ary[k]%>_width").value = "<%=aryW[k]%>";
				}
				<%
				}
				if (aryDefault != null) {
				%>
				if (o("<%=ary[k]%>_default")) {
					o("<%=ary[k]%>_default").value = "<%=aryDefault[k]%>";
				}
				<%
				}
			}
		}
	}
}
%>
</script>
<%} else {%>
<form action="module_field_conds.jsp?op=addCond" method="post" name="formBtn" id="formBtn">
<table cellspacing="0" class="tabStyle_1 percent98" cellpadding="3" width="95%" align="center">
    <tr>
      <td align="center" class="tabStyle_1_title">条件</td>
    </tr>
    <tr>
		<td>
			<input type="checkbox" id="isAutoExpand" name="isAutoExpand" value="1" checked
				   title="如勾选，则显示全部条件，如不勾选，则条件默认只显示一行"/>&nbsp;自动展开
			<input type="checkbox" id="isShowEmptyValMenu" name="isShowEmptyValMenu" value="1" checked
				   title="如勾选，则显示全部条件，如不勾选，则条件默认只显示一行"/>&nbsp;显示空值选择菜单
		</td>
    </tr>
    <tr>
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
		  if (!ff.isCanQuery()) {
			  continue;
		  }
		  String fieldDesc = ff.getName();
		  if (isSub) {
		  	fieldDesc = "sub:" + fd.getCode() + ":" + ff.getName();
		  }
		  %>
          <div>
            <input type="checkbox" name="queryFields" value="<%=fieldDesc%>" />
            <%=ff.getTitle()%>
			  <input name="<%=fieldDesc%>_title" class="cond-title" title="别名"/>
			  <%if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {%>
            <select name="<%=fieldDesc%>_cond">
              <option value="0">时间段</option>
              <option value="1">时间点</option>
              </select>
            <%
			} else if(ff.getType().equals(FormField.TYPE_MACRO)) {
                %>
            <select name="<%=fieldDesc%>_cond">
              <option value="1">等于</option>
              <option value="0" selected="selected">包含</option>
				<%
					// 基础数据
					if ("macro_flow_select".equals(ff.getMacroType())) {
						MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
						IBasicSelectCtl basicSelectCtl = macroCtlService.getBasicSelectCtl();
						SelectMgr sm = new SelectMgr();
						SelectDb sd = sm.getSelect(basicSelectCtl.getCode(ff));
						if (sd.getType() == SelectDb.TYPE_LIST) {
				%>
				<option value="<%=SQLBuilder.COND_TYPE_MULTI%>">多选</option>
				<%
					}
				} else if ("macro_basic_tree_select_ctl".equals(ff.getMacroType())) {
				%>
				<option value="<%=SQLBuilder.COND_TYPE_MULTI%>">多选</option>
				<%
					}
				%>
              </select>
            <%
            } else if (ff.getFieldType()==FormField.FIELD_TYPE_INT || ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE || ff.getFieldType()==FormField.FIELD_TYPE_FLOAT || ff.getFieldType()==FormField.FIELD_TYPE_LONG || ff.getFieldType()==FormField.FIELD_TYPE_PRICE) {
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
			  宽度
			  <input name="<%=fieldDesc%>_width" value="" class="cond-width"/>
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
				  if (ary.length>=5) {
					  FormDb otherFormDb = fm.getFormDb(ary[2]);
					  if (!otherFormDb.isLoaded()) {
					  	out.print("<div>" + field + "不存在</div>");
					  }
					  else {
				  		if ("id".equalsIgnoreCase(ary[4])) {
				  			fieldTitle = otherFormDb.getName() + "：" + "ID";
						}
						else {					  
						  ff = otherFormDb.getFormField(ary[4]);
						  fieldTitle = otherFormDb.getName() + "：" + ff.getTitle();
						}
					  }
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
              <%if (ff.getType().equals(FormField.TYPE_TEXTFIELD) || ff.getType().equals(FormField.TYPE_TEXTAREA)) {%>
              <option value="0" selected="selected">包含</option>
              <%}%>
              </select>
            <%}%>
			  宽度
			  <input name="<%=fields[n]%>_width" value="" class="cond-width"/>
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
			  宽度
			  <input name="cws_status_width" value="" class="cond-width"/>
          </div>
          <div>
            <input type="checkbox" name="queryFields" value="cws_flag" />	    
            冲抵状态
            <select name="cws_flag_cond">
              <option value="=" selected="selected">等于</option>
            </select>
            <input name="cws_flag" type="hidden" />
			  宽度
			  <input name="cws_flag_width" value="" class="cond-width"/>
          </div>
		  <div>
			  <input type="checkbox" name="queryFields" value="cws_id" />
			  关联ID&nbsp;&nbsp;
			  <select name="cws_id_cond">
				  <option value="=" selected="selected">等于</option>
				  <option value="&gt;">大于</option>
				  <option value="&lt;">小于</option>
				  <option value="&gt;=">大于等于</option>
				  <option value="&lt;=">小于等于</option>
			  </select>
			  宽度
			  <input name="cws_id_width" value="" class="cond-width"/>
		  </div>
		<%}%>
		</div>
		<%}%>
      </td>
    </tr>
    <tr >
      <td align="center">
		<input class="btn btn-default" type="button" value="添加查询" onclick="submitFormBtn()"/>
        <input name="formCode" value="<%=formCode%>" type="hidden" />
        <input name="code" value="<%=code%>" type="hidden" />
        <input name="btnName" value="查询" type="hidden" />     
        <input name="btnOrder" size="5" type="hidden" value="<%=btnNames!=null?StrUtil.toDouble(btnOrders[i-1])+1:1%>" />      
      </td>
    </tr>
</table>
</form>
<script>
	function submitFormBtn() {
		<%
        if (isServerConnectWithCloud) {
        %>
		$.ajax({
			type: "post",
			url: "condAdd.do",
			contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
			data: $('#formBtn').serialize(),
			dataType: "html",
			beforeSend: function (XMLHttpRequest) {
				$('body').showLoading();
			},
			success: function (data, status) {
				data = $.parseJSON(data);
				// console.log(data);
				if (data.ret=="1") {
					layer.alert(data.msg, {
						yes: function() {
							window.location.reload();
						}
					});
				}
				else {
					layer.msg(data.msg, {
						offset: '6px'
					});
				}
			},
			complete: function (XMLHttpRequest, status) {
				$('body').hideLoading();
			},
			error: function (XMLHttpRequest, textStatus) {
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});
		<%
        } else {
        %>

		var we = o("webedit");
		we.PostScript = "<%=path%>/public/module/addCond.do";

		loadDataToWebeditCtrl(o("formBtn"), o("webedit"));
		we.AddField("cwsVersion", "<%=cfg.get("version")%>");
		we.AddField("userSecret", "<%=userSecret%>");
		we.AddField("ip", "<%=ip%>");

		we.AddField("tName", "<%=tName%>");
		we.AddField("tOrder", "<%=tOrder%>");
		we.AddField("tScript", "<%=tScript.replaceAll("\"", "\\\\\"")%>");
		we.AddField("tBclass", "<%=tBclass%>");
		we.AddField("tRole", "<%=tRole%>");

		we.AddField("isWebedit", "true");

		we.UploadToCloud();

		consoleLog(we.ReturnMessage);
		var data = $.parseJSON(we.ReturnMessage);
		if (data.ret=="1") {
			$.ajax({
				type: "post",
				url: "btnSave.do",
				contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
				data: {
					code: "<%=code%>",
					formCode: "<%=formCode%>",
					result: JSON.stringify(data.result)
				},
				dataType: "html",
				beforeSend: function (XMLHttpRequest) {
					$('body').showLoading();
				},
				success: function (data, status) {
					data = $.parseJSON(data);
					if (data.ret=="1") {
						layer.alert(data.msg, {
							yes: function() {
								window.location.reload();
							}
						});
					}
					else {
						layer.msg(data.msg, {
							offset: '6px'
						});
					}
				},
				complete: function (XMLHttpRequest, status) {
					$('body').hideLoading();
				},
				error: function (XMLHttpRequest, textStatus) {
					// 请求出错处理
					alert(XMLHttpRequest.responseText);
				}
			});
		}
		else {
			layer.msg(data.msg, {
				offset: '6px'
			});
		}
		<%
        }
        %>
	}
</script>
<%}%>
<%
	License license = License.getInstance();
	if (!isServerConnectWithCloud) {
%>
<TABLE align="center" class="tabStyle_1 percent60" style="margin-top: 20px; width:450px">
	<TR>
		<TD align="left" class="tabStyle_1_title">上传助手</TD>
	</TR>
	<TR>
		<td align="center">
			<object classid="CLSID:DE757F80-F499-48D5-BF39-90BC8BA54D8C" codebase="../activex/cloudym.CAB#version=1,3,0,0" width=450 height=86 align="middle" id="webedit">
				<param name="Encode" value="utf-8">
				<param name="MaxSize" value="<%=Global.MaxSize%>">
				<!--上传字节-->
				<param name="ForeColor" value="(255,255,255)">
				<param name="BgColor" value="(107,154,206)">
				<param name="ForeColorBar" value="(255,255,255)">
				<param name="BgColorBar" value="(0,0,255)">
				<param name="ForeColorBarPre" value="(0,0,0)">
				<param name="BgColorBarPre" value="(200,200,200)">
				<param name="FilePath" value="">
				<param name="Relative" value="2">
				<!--上传后的文件需放在服务器上的路径-->
				<param name="Server" value="<%=host%>">
				<param name="Port" value="<%=port%>">
				<param name="VirtualPath" value="<%=Global.virtualPath%>">
				<param name="PostScript" value="">
				<param name="PostScriptDdxc" value="">
				<param name="SegmentLen" value="204800">
				<param name="BasePath" value="">
				<param name="InternetFlag" value="">
				<param name="Organization" value="<%=license.getCompany()%>" />
				<param name="Key" value="<%=license.getKey()%>" />
			</object>
		</TD>
	</TR>
</table>
<%
	}
%>
</body>
<script>
	$(function() {
		$('input, select, textarea').each(function() {
			if (!$('body').hasClass('form-inline')) {
				$('body').addClass('form-inline');
			}
			if (!$(this).hasClass('ueditor') && !$(this).hasClass('btnSearch') && !$(this).hasClass('tSearch') && $(this).attr('type') != 'hidden' && $(this).attr('type') != 'file') {
				$(this).addClass('form-control');
				$(this).attr('autocomplete', 'off');
			}
		});
	})

	<%
    if (!isServerConnectWithCloud) {
	%>
	function checkWebEditInstalled() {
		var bCtlLoaded = false;
		try	{
			if (typeof(o("webedit").AddField)=="undefined")
				bCtlLoaded = false;
			if (typeof(o("webedit").AddField)=="unknown") {
				bCtlLoaded = true;
			}
		}
		catch (ex) {
		}
		if (!bCtlLoaded) {
			$('<div></div>').html('您还没有安装客户端控件，请点击确定此处下载安装！').activebar({
				'icon': 'images/alert.gif',
				'highlight': '#FBFBB3',
				'url': 'activex/oa_client.exe',
				'button': 'images/bar_close.gif'
			});
		}
	}

	$(function() {
		checkWebEditInstalled();
	})
	<%
    }
    %>
</script>
</html>