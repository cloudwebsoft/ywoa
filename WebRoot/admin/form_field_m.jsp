<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "org.json.*"%>
<%
String formCode = ParamUtil.get(request, "code");
String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "orders";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>表单字段管理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%@ include file="../inc/nocache.jsp"%>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>

<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />

<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.min.css" />

<link type="text/css" rel="stylesheet" href="../js/appendGrid/jquery.appendGrid-1.5.1.css" />
<script type="text/javascript" src="../js/appendGrid/jquery.appendGrid-1.5.1.js"></script>

<script>
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "form_field_m.jsp?code=<%=StrUtil.UrlEncode(formCode)%>&orderBy=" + orderBy + "&sort=" + sort;
}
</script>
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%		
if (!privilege.isUserPrivValid(request, "admin.flow")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

String op = ParamUtil.get(request, "op");
if (op.equals("edit")) {
   	String rowOrder = ParamUtil.get(request, "tblFields_rowOrder");
   	String[] uniqueIndexes = null;
   	if (rowOrder==null) {
   		uniqueIndexes = new String[0];
   	}
   	else {
   		uniqueIndexes = rowOrder.split(",");
   	}

	boolean re = false;
    for (int i = 0; i < uniqueIndexes.length; i++) {
    	String name = ParamUtil.get(request, "tblFields_name_" + uniqueIndexes[i]);
    	String canNull = ParamUtil.get(request, "tblFields_canNull_" + uniqueIndexes[i]);
    	String canQuery = ParamUtil.get(request, "tblFields_canQuery_" + uniqueIndexes[i]);
    	String canList = ParamUtil.get(request, "tblFields_canList_" + uniqueIndexes[i]);
    	String orders = String.valueOf(uniqueIndexes.length -1 - i); // ParamUtil.get(request, "tblFields_orders_" + uniqueIndexes[i]);
    	String width = ParamUtil.get(request, "tblFields_width_" + uniqueIndexes[i]);
    	String isMobileDisplay = ParamUtil.get(request, "tblFields_isMobileDisplay_" + uniqueIndexes[i]);
    	String isHide = ParamUtil.get(request, "tblFields_isHide_" + uniqueIndexes[i]);
    	String moreThan = ParamUtil.get(request, "tblFields_moreThan_" + uniqueIndexes[i]);
    	String morethanMode = ParamUtil.get(request, "tblFields_morethanMode_" + uniqueIndexes[i]);

    	int isUnique = ParamUtil.getInt(request, "tblFields_isUnique_" + uniqueIndexes[i]);

		String sql = "update form_field set canNull=" + canNull + ", canQuery=" + canQuery + ", canList=" + canList + ", orders=" + orders + ", width=" + width + ", is_mobile_display=" + isMobileDisplay + ", is_hide=" + isHide + ",more_than=" + StrUtil.sqlstr(moreThan) + ",more_than_mode=" + StrUtil.sqlstr(morethanMode) + ",is_unique=" + isUnique + " where formCode=" + StrUtil.sqlstr(formCode) + " and name=" + StrUtil.sqlstr(name);
		JdbcTemplate jt = new JdbcTemplate();
		re = jt.executeUpdate(sql)==1;
	}
	if (re) {
		FormCache fc = new FormCache(fd);
		fd.getPrimaryKey().setValue(formCode);
		fc.refreshSave(fd.getPrimaryKey());
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "form_field_m.jsp?code=" + StrUtil.UrlEncode(formCode)));
	}
	else
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	return;
}

%>
<%@ include file="form_edit_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<div class="spacerH"></div>
<table width="60%" border="0" align="center">
  <tr>
    <td align="center"><strong><%=fd.getName()%>字段</strong></td>
  </tr>
</table>
<%
int k = 0;
String sql = "select name,title,type,macroType,defaultValue,fieldType,canNull,fieldRule,canQuery,canList,orders,width,is_mobile_display,is_hide,more_than,more_than_mode,is_unique from form_field where formCode=?";
sql += " order by " + orderBy + " " + sort;
JdbcTemplate jt = new JdbcTemplate();
ArrayList<FormField> ffList = new ArrayList<FormField>();
ResultIterator ri = jt.executeQuery(sql, new Object[]{formCode});
while (ri.hasNext()) {
	ResultRecord rr = (ResultRecord) ri.next();
	int fieldType = rr.getInt("fieldType");
	if (fieldType == FormField.FIELD_TYPE_DATETIME || fieldType == FormField.FIELD_TYPE_DATE || 
			fieldType == FormField.FIELD_TYPE_DOUBLE || fieldType == FormField.FIELD_TYPE_FLOAT ||
			fieldType == FormField.FIELD_TYPE_INT || fieldType == FormField.FIELD_TYPE_LONG ||
			fieldType == FormField.FIELD_TYPE_PRICE) {
		FormField ff = new FormField();
		ff.setName(rr.getString("name"));
		ff.setTitle(rr.getString("title"));
		ff.setFieldType(fieldType);
		ffList.add(ff);
	}
}
%>
<form id=form1 name=form1 action="form_field_m.jsp?op=edit&code=<%=formCode %>" method=post>
<table id="tblFields" align="center">
</table>
</form>
<div style="text-align:center; margin-top:10px">
<div style="margin-bottom:10px">(“显示于手机”仅适用于智能模块)</div>
<input type="button" class="btn" id="btnSubmit" value="确定" />
</div>
</body>
<script>
$(function () {
    // Initialize appendGrid
    $('#tblFields').appendGrid({
        // caption: '<%=fd.getName()%>字段',
        i18n: {
            append: '添加新行',
            rowDrag: '拖动',
            removeLast: '删除最后一行',
            insert: '添加一行在上方',
            moveUp: '上移',
            moveDown: '下移',
            remove: '删除'
        },        
        initRows: 0,
        rowDragging: true,      
        hideButtons: {
            append: true,
        	remove: true,
        	removeLast: true,        	
            insert: true,
            remove: true,
            removeLast: true
        },      
        columns: [
 			//  宽度隐藏比较字段操作
            { name: 'orders', display: '序号', type: 'hidden', ctrlAttr: { maxlength: 100 }, ctrlCss: { width: '30px'} },
            { name: 'fieldName', display: '字段名', type: 'text', ctrlAttr: { maxlength: 100, readonly: 'true', disabled:true }, ctrlCss: { width: '100px', border:'0'} },
            { name: 'title', display: '字段描述', type: 'text', ctrlAttr: { maxlength: 4, readonly: 'true', disabled:true }, ctrlCss: { width: '100px', border:'0'} },
            { name: 'canNull', display: '允许空', type: 'select', ctrlOptions: { 0: '否', 1: '是'} },
            { name: 'fieldType', display: '字段类型', type: 'text', ctrlAttr: { maxlength: 100, readonly: 'true', disabled:true }, ctrlCss: { width: '100px', border:'0'} },
            { name: 'ctlType', display: '控件类型', type: 'text', ctrlAttr: { maxlength: 10, readonly: 'true', disabled:true }, ctrlCss: { width: '180px', 'text-align': 'left', border:0}, value: 0 },
			{ name: 'canQuery', display: '参与查询', type: 'select', ctrlOptions: { 0: '否', 1: '是'}, ctrlAttr: { maxlength: 10 }, ctrlCss: { width: '50px', 'text-align': 'right' }, value: 0 },
			{ name: 'isUnique', display: '唯一', type: 'select', ctrlOptions: { 0: '否', 1: '是'}, ctrlAttr: { maxlength: 10, title:'字段值在表中是否唯一' }, ctrlCss: { width: '50px', 'text-align': 'right' }, value: 0 },
            { name: 'canList', display: '列表显示', type: 'select', ctrlOptions: { 0: '否', 1: '是'}, ctrlAttr: { maxlength: 10, title:'用于查询设计器、消息或邮件中显示表单概要信息' }, ctrlCss: { width: '50px', 'text-align': 'right' }, value: 0 },
            { name: 'isMobileDisplay', display: '显示于手机', type: 'select', ctrlOptions: { 0: '否', 1: '是'}, ctrlAttr: { maxlength: 10, title:'用于控制模块中的字段显示' }, ctrlCss: { width: '50px', 'text-align': 'right' }, value: 0 },
            { name: 'width', display: '宽度', type: 'text', ctrlAttr: { maxlength: 10 }, ctrlCss: { width: '50px', 'text-align': 'right' }, value: 0 },
            { name: 'isHide', display: '隐藏', type: 'select', ctrlOptions: { 0: '否', 1: '查看及编辑时', 2:'仅编辑时'}, ctrlAttr: { maxlength: 10 }, ctrlCss: { width: '50px', 'text-align': 'right' }, value: 0 },
            { name: 'morethanMode', display: '比较', type: 'select', ctrlOptions: { '': '无', '>': '>', '>=':'>=', '<':'<', '<=':'<=', '=':'=' }, ctrlAttr: { maxlength: 10 }, ctrlCss: { width: '50px', 'text-align': 'right' }, value: 0 },
	      	<%
	      	StringBuffer sb = new StringBuffer();
	      	sb.append("'':'无'");
	        for (FormField ff : ffList) {
	        	if (ff.getFieldType() == FormField.FIELD_TYPE_DATETIME || ff.getFieldType() == FormField.FIELD_TYPE_DATE || ff.getFieldType() == FormField.FIELD_TYPE_INT || ff.getFieldType()==FormField.FIELD_TYPE_LONG
	        		|| ff.getFieldType()==FormField.FIELD_TYPE_PRICE || ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE || ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE) {
	        		StrUtil.concat(sb, ",", "'" + ff.getName() + "':'" + ff.getTitle() + "'");
	        	}
	        }
	        %>            
            { name: 'moreThan', display: '字段', type: 'select', ctrlOptions: {<%=sb%>}, ctrlAttr: { maxlength: 10 }, ctrlCss: { width: '100px', 'text-align': 'right' }, value: 0 },
			{ name: 'name', type: 'hidden', value: ''}            
        ]
    });

<%  
ri.beforeFirst();
JSONArray arr = new JSONArray();
while (ri.hasNext()) {
	ResultRecord rr = (ResultRecord) ri.next();
	k++;
	
	JSONObject json = new JSONObject();
	json.put("orders", rr.getInt("orders"));
	json.put("fieldName", rr.getString("name"));
	json.put("title", rr.getString("title"));
	json.put("canNull", rr.getString("canNull"));
	json.put("fieldType", FormField.getFieldTypeDesc(rr.getInt("fieldType")));
    json.put("ctlType", FormField.getTypeDesc(rr.getString("type"), rr.getString("macroType")));
    json.put("canQuery", rr.getString("canQuery"));
	json.put("canList", rr.getString("canList"));

	json.put("isUnique", rr.getString("is_unique"));

	json.put("isMobileDisplay", rr.getString("is_mobile_display"));
	json.put("width", rr.getInt("width"));
    json.put("isHide", rr.getString("is_hide"));
	json.put("morethanMode", rr.getString("more_than_mode"));
	json.put("moreThan", rr.getString("more_than"));
	
	json.put("name", rr.getString("name"));
	
	arr.put(json);
}%>	    

    // Handle `Load` button click
    $(function () {
        $('#tblFields').appendGrid('load', <%=arr%>);
    });
    // Handle `Serialize` button click
    $('#btnSubmit').button().click(function () {
        $(document.forms[0]).serialize();
        $(document.forms[0]).submit();
    });
});

</script>
</html>
