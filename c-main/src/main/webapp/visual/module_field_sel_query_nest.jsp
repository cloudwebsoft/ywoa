<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.flow.query.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="org.json.JSONObject"%>
<%@ page import="org.json.JSONArray"%>
<%@page import="com.redmoon.oa.flow.macroctl.domain.NestFieldMaping"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
//当从模式对话框打开本窗口时，因为分属于不同的IE进程，SESSION会丢失，可以用cookie中置sessionId来解决这个问题
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
    out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
    return;
}
String op = ParamUtil.get(request, "op");
//主表单的编码
String openerFormCode = ParamUtil.get(request, "openerFormCode");
//原关联嵌套表编码
String oldRelateCode = ParamUtil.get(request, "oldRelateCode");
String newRelateCode = ParamUtil.get(request, "newRelateCode");
ModuleRelateDb mrd = new ModuleRelateDb();

if (op.equals("getQueryCondField")) {
	int id = ParamUtil.getInt(request, "id", -1);
	FormQueryDb aqd = new FormQueryDb();
	if (id!=-1) {
		// 检查用户是否具备权限（是本人创建的，或者被授权）
		FormQueryPrivilegeMgr fqpm = new FormQueryPrivilegeMgr();
		if (!fqpm.canUserQuery(request, id)) {
			out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
		
		aqd = aqd.getFormQueryDb(id);

		QueryScriptUtil qsu = new QueryScriptUtil();
		HashMap map = qsu.getCondFields(request, aqd);
		boolean isIDExist = false;
		if (map!=null) {
			Iterator ir = map.keySet().iterator();
			%>
			<select id="sourceCondField" name="sourceCondField">
			<option value="">无</option>	
			<%
			while (ir.hasNext()) {
				String keyName = (String) ir.next();
			%>
				<option value="<%=keyName%>"><%=map.get(keyName)%></option>
			<%
			}
			%>
			</select>
			<%
		}
		else {
			%>
			<font color="red">SQL语句中没有条件</font>
			<%
		}

		ResultIterator ri = qsu.executeQuery(request, aqd);
		map = qsu.getMapFieldTitle();
		Iterator ir = map.keySet().iterator();
		%>
        |
		<select id="sourceField" name="sourceField">
		<option value="">无</option>	
		<%
		while (ir.hasNext()) {
			String keyName = (String) ir.next();
			if (keyName.equalsIgnoreCase("id")) {
				isIDExist = true;
			}			
		%>
			<option value="<%=keyName%>"><%=map.get(keyName)%></option>
		<%
		}
		%>
		</select>
        <%
		if (!isIDExist) {
			// 在form_query_script_list_ajax.jsp中加入了判断，如果没有id（用于生成module_list_sel.jsp中的checkbox的值），则自动生成
			// out.print("<font color='red'>错误：结果集中没有ID列！</font>");
		}	
	}

	return;
}else if (op.equals("add")) {

    mrd = mrd.getModuleRelateDb(openerFormCode, newRelateCode);
    if (mrd != null) {
        return;
    }
    mrd = new ModuleRelateDb();
    //获取所有关联模块
    Vector tempV = mrd.getModulesRelated(openerFormCode);

    boolean re = mrd.create(new JdbcTemplate(), new Object[] {
            openerFormCode, newRelateCode, "id",
            new Integer(ModuleRelateDb.TYPE_MULTI),
            new Double(tempV.size() + 1) });
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
            openerFormCode, newRelateCode, "id",
            new Integer(ModuleRelateDb.TYPE_MULTI),
            new Double(v.size() + 1) });
    return;
}

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>嵌套表单域选择</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexbox/flexbox.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="../js/jquery.flexbox.js"></script>
</head>
<body>
<%
String editFlag = ParamUtil.get(request,"editFlag");
if (editFlag.equals("")){
%>
<%@ include file="module_field_sel_inc_menu_top.jsp"%>
<script>
o("menu3").className="current";
</script>
<%} %>
<div class="spacerH"></div>

<%
int isTab = 0;
String canAdd = "", canEdit = "", canImport = "", canDel = "", canSel="", canExport="";

String nestType = ParamUtil.get(request, "nestType");
String params = ParamUtil.get(request, "params");
JSONObject jsonObject = null;
int queryId = -1;
String queryName = "";
JSONArray conMaps = null;// new JSONArray();
JSONArray nestMaps = null;// new JSONArray();
List<NestFieldMaping> conList = null;
List<NestFieldMaping> nestList = null;
String destFormCode = "";
String destFormName = "";
if (params != null && !params.equals("")){
    jsonObject = new JSONObject(params);
    String queryIdObj = (String)jsonObject.get("queryId");
    if (queryIdObj != null && !queryIdObj.equals("")){
        queryId = Integer.valueOf(queryIdObj);
    }
    destFormCode = jsonObject.getString("destForm");
    //获取查询名称
    FormQueryMgr aqm = new FormQueryMgr();
    FormQueryDb fqd = aqm.getFormQueryDb(queryId);
    HashMap queryMap = new HashMap();
    HashMap queryConMap = new HashMap();
    if ( fqd != null && queryId != -1){
        queryName = fqd.getQueryName();
        //获取查询对应条件
        QueryScriptUtil qsu = new QueryScriptUtil();
        queryMap = qsu.getCondFields(request, fqd);
        //获取查询对应字段
        ResultIterator ri = qsu.executeQuery(request, fqd);
        queryConMap = qsu.getMapFieldTitle();
    }
    
    
    //获取条件映射
    conList = new ArrayList<NestFieldMaping>();
    conMaps = jsonObject.getJSONArray("mapsCond");
    for(int i = 0 ; i < conMaps.length(); i++){
    	JSONObject temp = new JSONObject();
    	temp = (JSONObject)conMaps.get(i);
    	NestFieldMaping nfm = new NestFieldMaping();
    	nfm.setSourceFieldCode(temp.getString("sourceField"));
    	nfm.setSourceFieldName((String)queryMap.get(temp.getString("sourceField")));
        nfm.setDestFieldCode(temp.getString("destField"));
        conList.add(nfm);
    }
    //获取字段映射
    nestList = new ArrayList<NestFieldMaping>();
    nestMaps = jsonObject.getJSONArray("mapsNest");
    for(int i = 0 ; i < nestMaps.length(); i++){
    	JSONObject temp = new JSONObject();
        temp = (JSONObject)nestMaps.get(i);
        NestFieldMaping nfm = new NestFieldMaping();
        nfm.setSourceFieldCode(temp.getString("sourceField"));
        nfm.setSourceFieldName((String)queryConMap.get(temp.getString("sourceField")));
        nfm.setDestFieldCode(temp.getString("destField"));
        nestList.add(nfm);
    }
	
	if (jsonObject.has("canAdd")) {
		canAdd = jsonObject.getString("canAdd");
		canEdit = jsonObject.getString("canEdit");
		canImport = jsonObject.getString("canImport");
		canDel = jsonObject.getString("canDel");
		canSel = jsonObject.getString("canSel");
		canExport = jsonObject.getString("canExport");
	}	
	if (jsonObject.has("isTab")) {
		isTab = jsonObject.getInt("isTab");
	}	
}
//若原嵌套表单编码为空，则获取设置嵌套表单编码
if (oldRelateCode == null || oldRelateCode.equals("")){
    oldRelateCode = destFormCode;
}
%>
<table width="100%" align="center" cellPadding="0" cellSpacing="0" class="tabStyle_1" id="mapTable" style="padding:0px; margin:0px;">
  <tbody>
    <tr>
      <td height="28" colspan="4" class="tabStyle_1_title">嵌套表格</td>
    </tr>
    <tr>
      <td width="13%" height="42" align="center">来源</td>
      <td height="42" align="left">
      	<span id="queryTitle"></span>&nbsp;&nbsp;
		<a href="javascript:;" onClick="selQuery()">请选择查询</a>
        <input id="queryId" name="queryId" type="hidden" />
      </td>
      <td width="14%" colspan="-1" align="center">嵌套表单</td>
      <td width="43%" align="center">
		<div id="destForm"></div>
<%
FormDb fd = new FormDb();
String sql = "select code from " + fd.getTableName() + " where unit_code=" + StrUtil.sqlstr(privilege.getUserUnitCode(request)) + " order by code asc";	
Vector v = fd.list(sql);
Iterator ir = v.iterator();
String json = "";
while (ir.hasNext()) {
	fd = (FormDb)ir.next();
	if (fd.getCode().equals(destFormCode)){
		destFormName =  fd.getName();
	}
	if (json.equals(""))
		json = "{\"id\":\"" + fd.getCode() + "\", \"name\":\"" + fd.getName() + "\"}";
	else
		json += ",{\"id\":\"" + fd.getCode() + "\", \"name\":\"" + fd.getName() + "\"}";
}
%>
<script>
var destField;
var dests = [];
var destCode = "";
var destForm = $('#destForm').flexbox({
		"results":[<%=json%>], 
		"total":<%=v.size()%>
	},{
	initialValue:'<%=destFormName%>',
    watermark: '请选择表单',
    paging: false,
	maxVisibleRows: 10,
	onSelect: function() {
		o("nestFieldTd").innerHTML = "<div id='destField'></div>";
		destCode = $("input[name=destForm]").val();
		$.getJSON('../flow/form_data_map_ajax.jsp', {"sourceFormCode":destCode}, function(data) {
					dests = data.result;
					destField = $('#destField').flexbox(
						{
							"results":data.result,
							"total":data.total
						},
						{
							watermark: '请选择表单域',
							paging: false,
							maxVisibleRows: 8
						}
					);
				});
		$("#params").val(combinationStr4DestFormChane());
        $("#changeForm").submit();
	}
});
function doSelQuery(id, title) {
    if (id==$('#queryId').val())
        return;
    
    $("#queryId").val(id);
        
    $("#queryTitle").html(title);
    
    $.ajax({
        type: "POST",
        url: "module_field_sel_query_nest.jsp",
        data: "op=getQueryCondField&id=" + id,
        success: function(html){
          var ary = html.split("|");
          $("#sourceCondFieldDiv").html(ary[0]);
          $("#sourceFieldDiv").html(ary[1]);
        },
        complete: function(XMLHttpRequest, status){
        },
        error: function(XMLHttpRequest, textStatus){
          alert(XMLHttpRequest.responseText);
        }      
    });
}
function addMapNest1(sourceFieldVal,sourceFieldName,destFieldVal,destFieldName) {

    var trId = "tr_" + sourceFieldVal + "_" + destFieldVal;
    
    var tr = "<tr id='" + trId + "' sourceField='" + sourceFieldVal + "' destField='" + destFieldVal + "'>";
    tr += "<td align='center'>来源</td>";
    tr += "<td align='center'>" + sourceFieldName + "</td>";
    tr += "<td align='center'>目标</td>";
    tr += "<td align='center'>" + destFieldName + "</td>";
    tr += "<td align='center'>";
    tr += "&nbsp;&nbsp<a href='javascript:;' onclick=\"$('#" + trId + "').remove()\">删除</a></td>";
    tr += "</tr>";
    $("#mapTableNest tr:last").after(tr);
}

function addMapCond1(sourceFieldVal,sourceFieldName,destFieldVal,destFieldName) {

    var trId = "tr_" + sourceFieldVal + "_" + destFieldVal;
   
    var tr = "<tr id='" + trId + "' sourceField='" + sourceFieldVal + "' destField='" + destFieldVal + "'>";
    tr += "<td align='center'>来源</td>";
    tr += "<td align='center'>" + sourceFieldName + "</td>";
    tr += "<td align='center'>目标</td>";
    tr += "<td align='center'>" + destFieldName + "</td>";
    tr += "<td align='center'>";
    tr += "&nbsp;&nbsp<a href='javascript:;' onclick=\"$('#" + trId + "').remove()\">删除</a></td>";
    tr += "</tr>";
    $("#mapTableCond tr:last").after(tr);
  
}
//显示原设置
var queryId = <%=queryId%>;
var queryName = '<%=queryName%>';
if (queryId != -1){
    doSelQuery(queryId,queryName);
    $("#nestFieldTd").html("<div id='destField'></div>");
    $.getJSON('../flow/form_data_map_ajax.jsp', {"sourceFormCode":'<%=destFormCode%>'}, function(data) {
                dests = data.result;
                destField = $('#destField').flexbox(
                    {
                        "results":data.result,
                        "total":data.total
                    },
                    {
                        watermark: '请选择表单域',
                        paging: false,
                        maxVisibleRows: 8
                    }
                );
            });
}
</script>        
      </td>
    </tr>
    <tr>
      <td height="42" align="center"> 显示为选项卡</td>
      <td height="42" align="left"><input type="checkbox" id="isTab" name="isTab" value="1" <%=isTab==1?"checked":"" %> /></td>
      <td height="42" align="left">&nbsp;</td>
      <td height="42" align="left">&nbsp;</td>
    </tr>
  </tbody>
</table>
<table id="mapTableCond" style="margin-top:10px" class="tabStyle_1" width="100%" border="0" align="center" cellspacing="0">
        <tr>
          <td colspan="5" class="tabStyle_1_title">条件映射</td>
  </tr>
        <tr>
          <td width="13%" align="center">查询中的条件</td>
          <td width="30%" id="sourceFieldTd2">
          <div id="sourceCondFieldDiv" style="float:left"><div id="sourceCondField" style="float:left"></div></div>
          </td>
          <td width="14%" align="center">主表单中的</td>
          <td width="23%" align="center" id="nestFieldTd2"><select id="fieldOpener" name="fieldOpener">
          <%
	  FormDb fdOpener = new FormDb();
	  fdOpener = fdOpener.getFormDb(openerFormCode);
	  ir = fdOpener.getFields().iterator();
	  while (ir.hasNext()) {
	  	FormField ff = (FormField)ir.next();
		%>
          <option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
          <%
	  }
	  %>
        </select>          
          </td>
          <td width="20%" align="center"><input type="button" class="btn" value="添加" onClick="addMapCond()" /></td>
        </tr>
        <tr></tr>
        <%
          if (conList != null){
        	  for (NestFieldMaping nfm : conList){
        		  String sCode = nfm.getSourceFieldCode();
        		  String sName = nfm.getSourceFieldName();
        		  String destCode = nfm.getDestFieldCode();
        		  String destName = fdOpener.getFieldTitle(destCode);
        %>		  
        	<script>
        	   addMapCond1('<%=sCode%>','<%=sName%>','<%=destCode%>','<%=destName%>')
        	</script>  
       <% 		  
        	  }
        	 
        	  
          }
        %>
</table>
<table id="mapTableNest" class="tabStyle_1" width="100%" border="0" align="center" cellspacing="0">
  <tr>
    <td colspan="5" class="tabStyle_1_title">字段映射</td>
  </tr>
  <tr>
    <td width="13%" align="center">源字段</td>
    <td width="30%" id="sourceFieldTd"><div id="sourceFieldDiv" style="float:left"></div></td>
    <td width="14%" align="center">目标字段
    </td>
    <td width="23%" align="center" id="nestFieldTd"><div id="destField" style="float:left"></div></td>
    <td width="20%" align="center"><input type="button" class="btn" value="添加" onClick="addMapNest()" /></td>
  </tr>
  <tr>
   <%
          if (nestList != null){
        	  FormDb destFd = new FormDb();
        	  destFd = destFd.getFormDb(destFormCode);
              for (NestFieldMaping nfm : nestList){
                  String sCode = nfm.getSourceFieldCode();
                  String sName = nfm.getSourceFieldName();
                  String destCode = nfm.getDestFieldCode();
                  String destName = destFd.getFieldTitle(destCode);
        %>        
            <script>
               addMapNest1('<%=sCode%>','<%=sName%>','<%=destCode%>','<%=destName%>')
            </script>  
       <%         
              }
             
              
          }
        %>
</table>
<table id="mapTableNest2" class="tabStyle_1" width="100%" border="0" align="center" cellspacing="0">
  <tr>
    <td colspan="5" class="tabStyle_1_title">权限</td>
  </tr>
  <tr>
    <td colspan="5" class="tabStyle_1_title"><input id="canAdd" name="canAdd" type="checkbox" value="true" />
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
      &nbsp;选择 </td>
  </tr>
</table>
<table width="100%" align="center" cellPadding="0" cellSpacing="0"
            class="tabStyle_1" id="mapTable" style="padding: 0px; margin: 0px;">
        <tr>
                <td >
                        <jsp:include page="module_field_inc_preview.jsp">
                            <jsp:param name="code" value="<%=destFormCode%>" />
                            <jsp:param name="formCode" value="<%=destFormCode%>" />
                            <jsp:param name="resource" value="nest" />
                        </jsp:include>
                        
                    </td>
           </tr>
           
</table> 
 <form id="changeForm">
    <input name="params" id="params" type="hidden" value="<%=params%>"></input>
    <input name="nestType" id="nestType" type="hidden" value="<%=nestType%>"></input>
    <input name="openerFormCode" id="openerFormCode" type="hidden" value="<%=openerFormCode%>"></input>
    <input name="oldRelateCode" id="oldRelateCode" type="hidden" value="<%=oldRelateCode%>"></input>
    <input name="editFlag" id="editFlag" type="hidden" value="<%=editFlag%>"></input>
 </form> 
<div style="text-align:center; margin-top:5px;"><input type="button" class="btn" value="确定" onClick="makeMap()" /></div>
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

function addMapNest() {
	if (sourceField==null || destField==null) {
		alert("请选择查询及嵌套表单！");
		return;
	}

	if (o("sourceField").value=="" || destField.getValue()=="") {
		alert("请选择查询结果中的字段及嵌套表单字段！");
		return;
	}

	var trId = "tr_" + o("sourceField").value + "_" + destField.getValue();
	// 检测trId是否已存在
	var isFound = false;
	$("#mapTableNest tr").each(function(k){
		if ($(this).attr("id")==trId) {
			isFound = true;
			return;
		}
	});
	
	if (isFound) {
		alert("存在重复映射！");
		return;
	}
	
	var tr = "<tr id='" + trId + "' sourceField='" + o("sourceField").value + "' destField='" + destField.getValue() + "'>";
	tr += "<td align='center'>来源</td>";
	tr += "<td align='center'>" + $('#sourceField option:selected').text() + "</td>";
	tr += "<td align='center'>目标</td>";
	tr += "<td align='center'>" + destField.getText() + "</td>";
	tr += "<td align='center'>";
	tr += "&nbsp;&nbsp<a href='javascript:;' onclick=\"$('#" + trId + "').remove()\">删除</a></td>";
	tr += "</tr>";
	$("#mapTableNest tr:last").after(tr);
	
	o("sourceField").value = "";
	destField.setValue('');
}

function addMapCond() {
	if (sourceCondField==null) {
		alert("请选择查询及嵌套表单！");
		return;
	}
	if (o("sourceCondField").value=="" || o("fieldOpener").value=="") {
		alert("请选择查询结果中的字段及主表单中的字段！");
		return;
	}

	var trId = "tr_" + o("sourceCondField").value + "_" + o("fieldOpener").value;
	// 检测trId是否已存在
	var isFound = false;
	$("#mapTableCond tr").each(function(k){
		if ($(this).attr("id")==trId) {
			isFound = true;
			return;
		}
	});
	
	if (isFound) {
		alert("存在重复映射！");
		return;
	}
	
	var tr = "<tr id='" + trId + "' sourceField='" + o("sourceCondField").value + "' destField='" + o("fieldOpener").value + "'>";
	tr += "<td align='center'>来源</td>";
	tr += "<td align='center'>" + $('#sourceCondField option:selected').text() + "</td>";
	tr += "<td align='center'>目标</td>";
	tr += "<td align='center'>" + $('#fieldOpener option:selected').text() + "</td>";
	tr += "<td align='center'>";
	tr += "&nbsp;&nbsp<a href='javascript:;' onclick=\"$('#" + trId + "').remove()\">删除</a></td>";
	tr += "</tr>";
	$("#mapTableCond tr:last").after(tr);
	
	o("sourceCondField").value = "";
	// o("fieldOpener").value = "";	// 没有为空的value
}

function makeMap() {
	if (destForm.getValue()=="") {
		alert("请选择嵌套表单！");
		return;
	}
	var str = combinationStr();
	// alert(str);
	str = encodeJSON(str);
	
	window.opener.setSequence(str, destForm.getText());
	//写关联模块
	var editFlag = $("#editFlag").val();
    var opFlag = "";
    if (editFlag == ""){
       opFlag = "add";
    }else{
       opFlag = "edit";
    }
    $.ajax({
        type: "post",
        async: false, // 设为同步，以免窗口关闭致调用不成功
        url: "module_field_sel_query_nest.jsp",
        data: {
            openerFormCode: "<%=openerFormCode%>",
            newRelateCode: "<%=destFormCode%>",
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
	closeWindow();
}

// 对字符串中的引号进行编码，以免引起json解析问题
function encodeJSON(jsonString) {
	jsonString = jsonString.replace(/\"/gi, "%dq");
	return jsonString.replace(/'/gi, "%sq");
}

function selQuery() {
	openWin("../flow/form_query_list_sel.jsp?type=script", 800, 600);	
}
function closeWindow(){
        window.opener=null;
        window.open('', '_self', ''); 
        window.close(); 
}
function combinationStr(){
    
    // 组合成json字符串{maps:[{sourceField:..., destField:..., editable:true, appendable:true},...{...}]}
    var mapsNest = "";
    $("#mapTableNest tr").each(function(k){
        // 判断是否为描述映射的行
        if ($(this)[0].id!="") {
            if ($(this).attr("id").indexOf("tr_")==0) {
                if (mapsNest=="") {
                    mapsNest = "{\"sourceField\": \"" + $(this).attr('sourceField') + "\", \"destField\":\"" + $(this).attr('destField') + "\"}";
                }
                else {
                    mapsNest += ",{\"sourceField\": \"" + $(this).attr('sourceField') + "\", \"destField\":\"" + $(this).attr('destField') + "\"}";
                }
            }
        }
    });
    
    var mapsCond = "";
    $("#mapTableCond tr").each(function(k){
        // 判断是否为描述映射的行
        if ($(this)[0].id!="") {
            if ($(this).attr("id").indexOf("tr_")==0) {
                if (mapsCond=="") {
                    mapsCond = "{\"sourceField\": \"" + $(this).attr('sourceField') + "\", \"destField\":\"" + $(this).attr('destField') + "\"}";
                }
                else {
                    mapsCond += ",{\"sourceField\": \"" + $(this).attr('sourceField') + "\", \"destField\":\"" + $(this).attr('destField') + "\"}";
                }
            }
        }
    }); 
    var destFormVal = destForm.getValue();
    if (destFormVal == '<%=destFormName%>'){
       destFormVal = '<%=destFormCode%>';
    }
	
	var canAdd = getCheckboxValue("canAdd");
	var canEdit = getCheckboxValue("canEdit");
	var canImport = getCheckboxValue("canImport");
	var canExport = getCheckboxValue("canExport");
	var canDel = getCheckboxValue("canDel");
	var canSel = getCheckboxValue("canSel");
	var canStr = "\"canAdd\":\"" + canAdd + "\", \"canEdit\":\"" + canEdit + "\", \"canImport\":\"" + canImport + "\", \"canDel\":\"" + canDel + "\", \"canSel\":\"" + canSel + "\", \"canExport\":\"" + canExport + "\"";
	
	var isTab = 0;
	if (o("isTab").checked) {
		isTab = 1;
	}	
    var str = "{\"queryId\":\"" + o("queryId").value + "\", \"destForm\":\"" + destFormVal + "\", \"isTab\":" + isTab + ", \"mapsCond\":[" + mapsCond + "], \"mapsNest\":[" + mapsNest + "], " + canStr + "}";
    return str;
}
function combinationStr4DestFormChane(){
    
    // 组合成json字符串{maps:[{sourceField:..., destField:..., editable:true, appendable:true},...{...}]}
    var mapsNest = "";
    
    var mapsCond = "";
    $("#mapTableCond tr").each(function(k){
        // 判断是否为描述映射的行
        if ($(this)[0].id!="") {
            if ($(this).attr("id").indexOf("tr_")==0) {
                if (mapsCond=="") {
                    mapsCond = "{\"sourceField\": \"" + $(this).attr('sourceField') + "\", \"destField\":\"" + $(this).attr('destField') + "\"}";
                }
                else {
                    mapsCond += ",{\"sourceField\": \"" + $(this).attr('sourceField') + "\", \"destField\":\"" + $(this).attr('destField') + "\"}";
                }
            }
        }
    }); 
    
    var destFormVal = destForm.getValue();
    if (destFormVal == '<%=destFormName%>'){
       destFormVal = '<%=destFormCode%>';
    }
    var str = "{\"queryId\":\"" + o("queryId").value + "\", \"destForm\":\"" + destFormVal + "\", \"mapsCond\":[" + mapsCond + "], \"mapsNest\":[" + mapsNest + "]}";
    return str;
}
</script>
</html>