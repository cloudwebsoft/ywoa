<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.ui.menu.*"%>
<%@ page import="java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>管理角色菜单</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>

<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />

<script type="text/javascript" src="../js/TreeGrid/TreeGrid.js"></script>
<link type="text/css" rel="stylesheet" href="../js/TreeGrid/TreeGrid.css"/>

<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

<script src="../js/jquery.toaster.flow.js"></script>

</head>
<body>
<jsp:useBean id="privmgr" scope="page" class="com.redmoon.oa.pvg.PrivMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.user")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String roleCode = ParamUtil.get(request, "roleCode");
if (roleCode.equals("")) {
	out.print(StrUtil.makeErrMsg("角色编码不能为空！"));
	return;
}

RoleDb rd = new RoleDb();
rd = rd.getRoleDb(roleCode);
%>
<%@ include file="user_role_op_inc_menu_top.jsp"%>
<script>
o("menu4").className="current";
</script>
<%
Leaf lf = new Leaf();
lf = lf.getLeaf(Leaf.CODE_ROOT);
DirectoryView dv = new DirectoryView(request, lf);
%>
<div class="spacerH"></div>
<div>
	<input style="display:none" type="button" value="关闭所有节点" onclick="expandAll('N')" />
</div>
	<div id="divTree"></div>
	<script language="javascript">
		var config = {
			id: "treeTable",
			width: "800",
			renderTo: "divTree",
			headerAlign: "left",
			headerHeight: "30",
			dataAlign: "left",
			indentation: "20",
			folderOpenIcon: "images/folderOpen.gif",
			folderCloseIcon: "images/folderClose.gif",
			defaultLeafIcon: "images/defaultLeaf.gif",
			hoverRowBackground: "false",
			folderColumnIndex: "1",
			itemClick: "itemClickEvent",
			folderOpenIcon: "<%=request.getContextPath()%>/js/TreeGrid/images/folderOpen.gif",
			folderCloseIcon: "<%=request.getContextPath()%>/js/TreeGrid/images/folderClose.gif",
			defaultLeafIcon: "<%=request.getContextPath()%>/js/TreeGrid/images/defaultLeaf.gif",		
			columns:[
				{headerText: "", headerAlign: "center", dataAlign: "center", width: "20", handler: "customCheckBox"},
				{headerText: "菜单项", dataField: "name", headerAlign: "center", handler: "showName"},
				{headerText: "类型", dataField: "privName", headerAlign: "center", dataAlign: "center", width: "200", handler: "getTypeName"},
				{headerText: "权限/模块说明", dataField: "privName", headerAlign: "center", dataAlign: "center", width: "200"},
				{headerText: "模块/流程/基础数据", dataField: "moduleName", headerAlign: "center", dataAlign: "center", width: "200", handler: "openModule"}			],
			data:<%=dv.getJsonTreeString(roleCode)%>
		};

		/*
			单击数据行后触发该事件
			id：行的id
			index：行的索引。
			data：json格式的行数据对象。
		*/
		function itemClickEvent(id, index, data){
			jQuery("#currentRow").val(id + ", " + index + ", " + TreeGrid.json2str(data));
		}
		
		function getTypeName(row, col) {
			if (row.type==<%=Leaf.TYPE_FLOW%>) {
				return "流程";
			}
			else if (row.type==<%=Leaf.TYPE_LINK%>) {
				return "链接";
			}
			else if (row.type==<%=Leaf.TYPE_MODULE%>) {
				return "模块";
			}
			else if (row.type==<%=Leaf.TYPE_BASICDATA%>) {
				return "基础数据";
			}
			else {
				return "预设菜单项";
			}
		}

		function customCheckBox(row, col) {
			var disableStr = "", checkStr="", title = "";
			if (row.type==<%=Leaf.TYPE_MODULE%> || row.type==<%=Leaf.TYPE_BASICDATA%>) {
				disableStr = "disabled";
			}
			else {
				if (row.priv=="" && row.type=="<%=com.redmoon.oa.ui.menu.Leaf.TYPE_LINK%>") {
					disableStr = "disabled";
					title = "没有权限限制";
				}
			}
			if　(row.canSee) {
				checkStr = "checked";
			}
			var priv = row.priv;
			var moduleCode = row.moduleCode;
			var canSee = row.canSee;
			var menuCode = row.id;
			return "<input id='chk" + row.id + "' title='" + title + "' " + checkStr + " " + disableStr + " type='checkbox' value='1' onclick='doCheck(\"" + menuCode + "\", " + canSee + ", \"" + priv + "\", \"" + moduleCode + "\", this.checked)' />";
		}
		
		function doCheck(menuCode, canSee, priv, moduleCode, isChecked) {		
			if (priv!="") {
				$.ajax({
					type: "post",
					url: "setMenuPriv.do",
					data: {
						isPriv: isChecked,
						priv: priv,
						roleCode: "<%=roleCode%>",
						menuCode: menuCode
					},
					contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
					dataType: "html",
					beforeSend: function(XMLHttpRequest){
						$('#divTree').showLoading();
					},
					success: function(data, status){
						data = $.parseJSON(data);
						$.toaster({priority : 'info', message : data.msg });
					},
					complete: function(XMLHttpRequest, status){
						$('#divTree').hideLoading();
					},
					error: function(XMLHttpRequest, textStatus){
						// 请求出错处理
						alert(XMLHttpRequest.responseText);
					}
				});					
			}
		}

		function showName(row, col){
			var name = row[col.dataField] || "";
			var re = name;
			if (row.link!="") {
				re = "<a href='javascript:;' onclick=\"addTab('" + name + "', '<%=request.getContextPath()%>/" + row.link + "')\">" + name + "</a>";
			}
			return re;
		}	

		function openModule(row, col) {
			var tabId = "";
			if (window.top.mainFrame) {
				tabId = window.top.mainFrame.getActiveTab().id;		
			}
			else {
				tabId = window.top.getActiveTabName();
			}
			if (row.type==<%=Leaf.TYPE_FLOW%>) {
				return "<a href='javascript:;' onclick='addTab(\"" + row.aliasName + "\", \"<%=request.getContextPath()%>/admin/flow_dir_priv_m.jsp?dirCode=" + row.aliasCode + "&tabIdOpener=" + tabId + "\")'>" + row.aliasName + "</a>";
			}
			else if (row.type==<%=Leaf.TYPE_LINK%>) {
				return "";
			}
			else if (row.type==<%=Leaf.TYPE_MODULE%>) {
				var moduleName = row.moduleName;
				var moduleCode = row.moduleCode;
				if (moduleCode) {
					return "<a href='javascript:;' onclick='addTab(\"" + moduleName + "\", \"<%=request.getContextPath()%>/visual/module_priv_list.jsp?code=" + moduleCode + "&tabIdOpener=" + tabId + "&formCode=" + row.formCode + "\")'>" + row.moduleName + "</a>";
				}
			}
			else if (row.type==<%=Leaf.TYPE_BASICDATA%>) {
				return "<a href='javascript:;' onclick='addTab(\"" + row.aliasName + "\", \"<%=request.getContextPath()%>/admin/basic_select_kind_priv_m.jsp?kindId=" + row.aliasCode + "&tabIdOpener=" + tabId + "\")'>" + row.aliasName + "</a>";
			}
			else {
				// return "预设菜單項";
			}		
		}

		//创建一个组件对象
		var treeGrid = new TreeGrid(config);
		treeGrid.show();
		
		/*
			展开、关闭所有节点。
			isOpen=Y表示展开，isOpen=N表示关闭
		*/
		function expandAll(isOpen){
			treeGrid.expandAll(isOpen);
		}
		
		/*
			取得当前选中的行，方法返回TreeGridItem对象
		*/
		function selectedItem(){
			var treeGridItem = treeGrid.getSelectedItem();
			if(treeGridItem!=null){
				//获取数据行属性值
				alert(treeGridItem.id + ", " + treeGridItem.index + ", " + treeGridItem.data.code);
				
				//获取父数据行
				var parent = treeGridItem.getParent();
				if(parent!=null){
					//jQuery("#currentRow").val(parent.data.name);
				}
				
				//获取子数据行集
				var children = treeGridItem.getChildren();
				if(children!=null && children.length>0){
					jQuery("#currentRow").val(children[0].data.name);
					console.log(children[0].data.name);
				}
			}
		}
		
		$(function() {
			$('#treeTable').addClass("tabStyle_1 percent80");
		});
	</script>
</body>
</html>