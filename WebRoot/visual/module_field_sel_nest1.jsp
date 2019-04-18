<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="org.json.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<title>嵌套表</title>
		<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
		<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/flexbox/flexbox.css" />
		<script type="text/javascript" src="../inc/common.js"></script>
		<script type="text/javascript" src="../js/jquery.js"></script>
		<script type="text/javascript" src="../js/jquery.flexbox.js"></script>
	</head>
	<body>
		<%
			String editFlag = ParamUtil.get(request, "editFlag");
			String nestType = ParamUtil.get(request, "nestType");

			if (editFlag.equals("")) {
		%>
		<%@ include file="module_field_sel_inc_menu_top.jsp"%>
		<script>
		o("menu1").className="current"; 
		</script>
		<%
			}
		%>
		<div class="spacerH"></div>
		<jsp:useBean id="privilege" scope="page"
			class="com.redmoon.oa.pvg.Privilege" />
		<%
			// 当从模式对话框打开本窗口时，因为分属于不同的IE进程，SESSION会丢失，可以用cookie中置sessionId来解决这个问题
			String priv = "read";
			if (!privilege.isUserPrivValid(request, priv)) {
				out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request,
						cn.js.fan.web.SkinUtil.LoadString(request,
								"pvg_invalid")));
				return;
			}
			String params = ParamUtil.get(request, "params");
			String openerFormCode = ParamUtil.get(request, "openerFormCode");
			String op = ParamUtil.get(request, "op");
			String oldRelateCode = ParamUtil.get(request, "oldRelateCode");
			
			int isTab = ParamUtil.getInt(request, "isTab", 0);
			String jsonStr = ParamUtil.get(request, "jsonStr");
			String canAdd = "", canEdit = "", canImport = "", canDel = "", canSel="", canExport="";
			JSONObject jsonObject = null;
			if (!jsonStr.equals("")){
				jsonObject = new JSONObject(jsonStr);
				try {
					canAdd = jsonObject.getString("canAdd");
					canEdit = jsonObject.getString("canEdit");
					canImport = jsonObject.getString("canImport");
					canDel = jsonObject.getString("canDel");
					canSel = jsonObject.getString("canSel");
					canExport = jsonObject.getString("canExport");
				}
				catch (Exception e) {
				}				
			}			
			
			ModuleRelateDb mrd = new ModuleRelateDb();
			String selectedValue = "";
			if (op.equals("add")) {
				mrd = mrd.getModuleRelateDb(openerFormCode, params);
				if (mrd != null) {
					return;
				}
				mrd = new ModuleRelateDb();
				//获取所有关联模块
				Vector tempV = mrd.getModulesRelated(openerFormCode);

				boolean re = mrd.create(new JdbcTemplate(), new Object[] {
						openerFormCode, params, "id",
						new Integer(ModuleRelateDb.TYPE_MULTI),
						new Double(tempV.size() + 1), new Integer(com.redmoon.oa.flow.FormDAO.STATUS_DONE) });
				return;
			} else if(op.equals("edit")){
				//删除原关联
				mrd = mrd.getModuleRelateDb(openerFormCode, oldRelateCode);
				if (mrd != null){
				    mrd.del();
				}
				mrd = null;
				//添加新关联
				mrd = new ModuleRelateDb();
				//获取所有关联模块
				Vector v = mrd.getModulesRelated(openerFormCode);

				boolean re = mrd.create(new JdbcTemplate(), new Object[] {
						openerFormCode, params, "id",
						new Integer(ModuleRelateDb.TYPE_MULTI),
						new Double(v.size() + 1) });
				return;
			}
		%>
		<table width="100%" align="center" cellPadding="0" cellSpacing="0"
			class="tabStyle_1" id="mapTable" style="padding: 0px; margin: 0px;">
			<tbody>
				<tr>
					<td height="28" colspan="5" class="tabStyle_1_title">
						嵌套表
					</td>
				</tr>
				<tr>
					<%
					/*
						FormDb fd = new FormDb();
						String sql = "select code from " + fd.getTableName()
								+ " where unit_code="
								+ StrUtil.sqlstr(privilege.getUserUnitCode(request))
								+ " order by code asc";
						Vector v = fd.list(sql);
						Iterator ir = v.iterator();
						String json = "";

						while (ir.hasNext()) {
							fd = (FormDb) ir.next();
							if (params != null && params.equals(fd.getCode())) {
								selectedValue = fd.getName();
							}
							if (json.equals(""))
								json = "{\"id\":\"" + fd.getCode() + "\", \"name\":\""
										+ fd.getName() + "\"}";
							else
								json += ",{\"id\":\"" + fd.getCode() + "\", \"name\":\""
										+ fd.getName() + "\"}";
						}
					*/
					
						ModuleSetupDb msd = new ModuleSetupDb();
						FormDb fd = new FormDb();
						
						// String sql = "select code from visual_module_setup where is_use=1 order by code asc";
						String sql = "select code from " + fd.getTableName() + " order by code asc";
						Vector v = fd.list(sql);
						Iterator ir = v.iterator();
						String json = "";

						while (ir.hasNext()) {
							fd = (FormDb) ir.next();
							if (params != null && params.equals(fd.getCode())) {
								selectedValue = fd.getName();
							}
							if (json.equals(""))
								json = "{\"id\":\"" + fd.getCode() + "\", \"name\":\""
										+ fd.getName() + "\"}";
							else
								json += ",{\"id\":\"" + fd.getCode() + "\", \"name\":\""
										+ fd.getName() + "\"}";
						}	
					%>
					<td width="14%" colspan="-1" align="center">
						表单
					</td>
					<td align="center">
						<div id="destForm"></div>
						<script>
var dests = [];
var sourceCode = "";
var destForm = $('#destForm').flexbox({
		"results":[<%=json%>], 
		"total":<%=v.size()%>
	},{
	initialValue:'<%=selectedValue%>',
    watermark: '请选择表单',    
    paging: false,
	maxVisibleRows: 10,
	onSelect: function() {
        sourceCode = $("input[name=destForm]").val();
        $("#code").val(sourceCode);
        $("#formCode").val(sourceCode);
        $("#params").val(sourceCode);
        $("#subForm").submit();
        //openWin("module_field_sel_nest1.jsp?nestType=nest_table&openerFormCode=<%=StrUtil.UrlEncode(openerFormCode)%>&params=" + sourceCode,800,600)
    }
});

</script>
					</td>
					<td align="left">
                    	<input type="checkbox" id="isTab" name="isTab" value="1" <%=isTab==1?"checked":"" %> />
                        显示为选项卡
                    </td>
				</tr>
				<tr>
				  <td colspan="3" align="left">权限</td>
			  </tr>
				<tr>
				  <td colspan="3" align="left"><span class="tabStyle_1_title">
				    <input id="canAdd" name="canAdd" type="checkbox" value="true" />
&nbsp;增加
    &nbsp;&nbsp;
    <input id="canEdit" name="canEdit" type="checkbox" value="true" />
&nbsp;修改
    &nbsp;&nbsp;
    <input id="canImport" name="canImport" type="checkbox" value="true" />
&nbsp;导入
    &nbsp;&nbsp;
    <input id="canExport" name="canExport" type="checkbox" value="true" />
&nbsp;导出
    &nbsp;&nbsp;
    <input id="canDel" name="canDel" type="checkbox" value="true" />
&nbsp;删除
    &nbsp;&nbsp;
    <input id="canSel" name="canSel" type="checkbox" value="true" />
&nbsp;选择 </span></td>
			  </tr>
				
			</tbody>

		</table>
		<table  align="center" cellPadding="0" cellSpacing="0"
            class="tabStyle_1" id="mapTable" style="padding: 0px; margin: 0px;">
	   <tr>
               <td>
                   <jsp:include page="module_field_inc_preview.jsp">
                       <jsp:param name="code" value="<%=params%>" />
                       <jsp:param name="formCode" value="<%=params%>" />
                       <jsp:param name="resource" value="nest" />
                   </jsp:include>
                   
               </td>

           </tr>
		</table>
		<form id="subForm">
			<input name="code" id="code" type="hidden" value="<%=params%>"></input>
			<input name="formCode" id="formCode" type="hidden"
				value="<%=params%>"></input>
			<input name="openerFormCode" id="openerFormCode" type="hidden"
				value="<%=openerFormCode%>"></input>
			<input name="params" id="params" type="hidden" value="<%=params%>"></input>
			<input name="editFlag" id="editFlag" type="hidden"
				value="<%=editFlag%>"></input>
			<input name="nestType" id="nestType" type="hidden"
				value="<%=nestType%>"></input>
			<input name="oldRelateCode" id="oldRelateCode" type="hidden"
				value="<%=oldRelateCode%>"></input>
		</form>
		<div style="text-align: center; margin-top: 5px;">
			<input type="button" class="btn" value="确定" onclick="makeMap()" />
		</div>
	</body>
<script>
<%
if (canAdd.equals("true")) {
%>
setCheckboxChecked("canAdd", "true");
<%
}
%>
<%
if (canEdit.equals("true")) {
%>
setCheckboxChecked("canEdit", "true");
<%
}
%>
<%
if (canImport.equals("true")) {
%>
setCheckboxChecked("canImport", "true");
<%
}
%>
<%
if (canExport.equals("true")) {
%>
setCheckboxChecked("canExport", "true");
<%
}
%>
<%
if (canDel.equals("true")) {
%>
setCheckboxChecked("canDel", "true");
<%
}
%>
<%
if (canSel.equals("true")) {
%>
setCheckboxChecked("canSel", "true");
<%
}
%>
	
function makeMap() {
	if (destForm.getValue()=="") {
		alert("请选择嵌套表单！");
		return;
	}
	// 组合成json字符串{maps:[{sourceField:..., destField:..., editable:true, appendable:true},...{...}]}
	var str = "{\"sourceForm\":\"\", \"destForm\":\""+ destForm.getValue() +"\", \"filter\":\"\", \"maps\":[]}";
	if (destForm.getValue() == '<%=selectedValue%>'){
	  var isTab = 0;
	  if (o("isTab").checked) {
		  isTab = 1;
	  }
	  
	  var canAdd = getCheckboxValue("canAdd");
	  var canEdit = getCheckboxValue("canEdit");
	  var canImport = getCheckboxValue("canImport");
	  var canExport = getCheckboxValue("canExport");
	  var canDel = getCheckboxValue("canDel");
	  var canSel = getCheckboxValue("canSel");
	  var canStr = "\"canAdd\":\"" + canAdd + "\", \"canEdit\":\"" + canEdit + "\", \"canImport\":\"" + canImport + "\", \"canDel\":\"" + canDel + "\", \"canSel\":\"" + canSel + "\", \"canExport\":\"" + canExport + "\"";
	  
	  str = "{\"sourceForm\":\"\", \"destForm\":\""+ $("#params").val() +"\", \"filter\":\"\", \"isTab\":" + isTab + ", " + canStr + ", \"maps\":[]}";
	}
	
	str = encodeJSON(str);
	window.opener.setSequence(str, destForm.getText());
	var editFlag = $("#editFlag").val();
	var opFlag = "";
	if (editFlag == ""){
	   opFlag = "add";
	}else{
	   opFlag = "edit";
	}
	//写关联模块
	$.ajax({
        type: "post",
        url: "module_field_sel_nest1.jsp",
        data: {
            openerFormCode: "<%=openerFormCode%>",
            params: "<%=params%>",
            oldRelateCode:"<%=oldRelateCode%>",
            op : opFlag
        },
        dataType: "json",
        beforeSend: function(XMLHttpRequest){
            //ShowLoading();
        },
        success: function(data, status){
            
        },
        complete: function(XMLHttpRequest, status){
            //HideLoading();
        },
        error: function(){
            //请求出错处理
        }
    }); 
	window.close();
	
}
// 对字符串中的引号进行编码，以免引起json解析问题
function encodeJSON(jsonString) {
	jsonString = jsonString.replace(/\"/gi, "%dq");
	return jsonString.replace(/'/gi, "%sq");
}


</script>
</html>