<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "java.io.InputStream" %>
<%@ page import = "java.util.*" %>
<%@ page import = "cn.js.fan.db.*" %>
<%@ page import = "com.redmoon.oa.basic.*" %>
<%@ page import = "cn.js.fan.util.*" %>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "cn.js.fan.cache.jcs.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@ page import = "com.redmoon.oa.basic.TreeSelectMgr"%>
<%@ page import = "com.redmoon.oa.basic.TreeSelectView"%>
<%@ page import = "com.redmoon.oa.basic.TreeSelectDb"%>
<%@ page import = "cn.js.fan.cache.jcs.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "com.redmoon.oa.kernel.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<meta http-equiv="X-UA-Compatible" content="IE=8"/>
<meta HTTP-EQUIV="pragma" CONTENT="no-cache"/> 
<meta HTTP-EQUIV="Cache-Control" CONTENT= "no-cache, must-revalidate"/> 
<meta HTTP-EQUIV="expires" CONTENT= "Wed, 26 Feb 1997 08:21:57 GMT"/>
<title>树形基础数据管理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath() %>/js/jstree/themes/default/style.css" />
<script src="<%=request.getContextPath() %>/js/jquery.my.js"></script>
<script src="<%=request.getContextPath() %>/js/jstree/jstree.js"></script>
<script src="<%=request.getContextPath() %>/js/jquery.toaster.js"></script>

<link href="../js/select2/select2.css" rel="stylesheet" />
<script src="../js/select2/select2.js"></script>

<style>
td {
	height:20px;
}
.unit {
	font-weight:bold; 
}
.deptNodeHidden {
	color:#cccccc;
}
</style>
<script>
function form1_onsubmit() {
	o("root_code").value = getRootCode();
}

var inst;
var node;
var code;
</script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="dm" scope="page" class="com.redmoon.oa.basic.TreeSelectMgr"/>
<%
String userName = privilege.getUser(request);
int kind = ParamUtil.getInt(request, "kind", -1);
SelectKindPriv skp = new SelectKindPriv();
if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
	if (skp.canUserAppend(userName, kind) || skp.canUserModify(userName, kind) || skp.canUserDel(userName, kind)) {
	}
	else {	  
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

String root_code = ParamUtil.get(request, "root_code");
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "root_code", root_code, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

if (root_code.equals("")) {
	root_code = privilege.getUserUnitCode(request);
}
%>
<Script>
var root_code = "<%=root_code%>";
// 使框架的bottom能得到此root_code
function getRootCode() {
	return root_code;
}
</Script>
<%
TreeSelectDb leaf = dm.getTreeSelectDb(root_code);
String root_name = leaf.getName();
int root_layer = leaf.getLayer();
String root_description = leaf.getDescription();
boolean isHome = false;

Privilege pvg = new Privilege();
String parent_code = ParamUtil.get(request, "parent_code");
//String root_code = ParamUtil.get(request, "root_code");
if (parent_code.equals("")){
	parent_code = root_code;
}

TreeSelectMgr tsm = new TreeSelectMgr();
TreeSelectDb tsd = tsm.getTreeSelectDb(root_code);
String rootName = tsd.getName();
%>
<div>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head">&nbsp;</td>
  </tr>
</table>
<table cellSpacing=0 cellPadding=0 width="95%" align=center>
  <TBODY>
    <TR>
      <TD height=200 valign="top">
		<%
			//DeptView dv = new DeptView(leaf);
			TreeSelectView dv = new TreeSelectView(leaf);
			//dv.listAjax(request, out, true);
			String jsonData = dv.getJsonString();
			List<String> list = dv.getAllUnit();
			
		%>
		<div id="officeequipmentTree"></div>
		<script>
		var listCode = new Array();
		var i = 0;
		<%	
			
			for(String str : list){
			%>
			listCode[i]= "<%=str%>";
			i++;
		<%
		}
		%>
		var myjsTree;
		  $(function () {
			myjsTree = $('#officeequipmentTree')
			  	.jstree({
			    	"core" : {
			            "data" :  <%=jsonData%>,
			            "themes" : {
						   "theme" : "default" ,
						   "dots" : true,  
						   "icons" : true  
						},
						"check_callback" : true,	
			 		},
			 		"ui" : {"initially_select" : [ "<%=root_code %>" ]  },
			 		"plugins" : ["unique", "dnd", "wholerow", "themes", "ui", "contextmenu" ,"types","crrm","state"],
			 		"contextmenu": {	//绑定右击事件
			 			"items": {
			 				"create": {  
			                    "label": "添加子项",
								"icon" : "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_add.png",
			                    "action": function (data) { 
			                    	var inst = $.jstree.reference(data.reference);
									node = inst.get_node(data.reference);
									code = node.id;
									var name = node.text;

									$.ajax({
										type: "post",
										url: "basic_tree_select_do.jsp",
										dataType: "json",
										data: {
											op: "new_code",
											parent_code: code,
											root_code: "<%=root_code %>"
										},
										success: function(data, status){
											if (data.ret == "1") {
												$("#quipbottom").show();
												$("#showParent").hide();
												$('#span_code').html(data.msg);
												$('#code').val(data.msg);
												$('#name').val("");
												$('#parent_code').val(code);
												$('#parent_name').html(name);
												$('#parentCode').val(code);
												$('#op').val("AddChild");
												
												$('#preCode').val('');
												onChangePreCode();
											}
										},
										complete: function(XMLHttpRequest, status){
										},
										error: function(XMLHttpRequest, textStatus){
											jAlert(XMLHttpRequest.responseText,"提示");
										}
									});	

									//window.location.href="officeequip_frame.jsp?flag1=1&op=AddChild&root_code=<%=StrUtil.UrlEncode(root_code)%>&parent_code="+code+"&parent_name="+name+"&number="+Math.random(),"dirbottomFrame";
									//window.open("officeequip_frame.jsp?op=AddChild&root_code=<%=StrUtil.UrlEncode(root_code)%>&parent_code="+code+"&parent_name="+name+"&number="+Math.random(),"dirbottomFrame");
			                    }
			                },  
			                "rename": {  
			                    "label": "修改",  
								"icon" : "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_alter.png",
			                    "action": function (data) { 
			                    	inst = $.jstree.reference(data.reference);
			                    	node = inst.get_node(data.reference);
			                    	var code = node.id;
			                    	var name = node.text;

			                    	$.ajax({
										type: "post",
										url: "basic_tree_select_do.jsp",
										dataType: "json",
										data: {
											op: "parent_name",
											code: node.parent,
											myCode: code
										},
										success: function(data, status){
											if (data.ret == "1") {
						                    	$("#quipbottom").show();
						                    	$("#showParent").show();
												$('#span_code').html(code);
												$('#code').val(code);
												$('#name').val(name);
												$('#parent_code').val(node.parent);
												$('#parent_name').html(data.msg);
												$('#parentCode').val(node.parent);
												$('#op').val("modify");
												
												$('#link').val(data.link);
												$('#preCode').val(data.preCode);
												if (data.preCode=="flow") {
													$('#flowTypeCode').val(data.formCode);												
												}
												else if (data.preCode=="module") {
													$('#formCode').val(data.formCode).trigger("change");
												}
												
												// console.log(data);
												onChangePreCode();
											}
										},
										complete: function(XMLHttpRequest, status){
										},
										error: function(XMLHttpRequest, textStatus){
											jAlert(XMLHttpRequest.responseText,"提示");
										}
									});	
									
									//window.location.href="officeequip_frame.jsp?flag1=1&op=modify&root_code=<%=StrUtil.UrlEncode(root_code)%>&code="+code+"&parent_name="+name+"&number="+Math.random(),"dirbottomFrame";
			                    }  
			                },   
			                "remove": {  
			                    "label": "删除",
								"icon" : "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_close.png",
			                    "action": function (data) { 
			                    	var inst = $.jstree.reference(data.reference);
			                    	var obj = inst.get_node(data.reference);
			                    	var code = obj.id;
			                    	if( "<%=root_code %>" == code){
			                    		jAlert("根节点不能被删除!","提示");
			                    		return;
			                    	}
			                    	jConfirm('您确定要删除吗?','提示',function(r){
			                    		if(!r){return;}
			                    		else{
			                    			$.ajax({
											type: "post",
											url: "basic_tree_select_do.jsp",
											dataType: "json",
											data: {
												op: "del",
												root_code: " <%=root_code %>",
												delcode:code+""
											},
											success: function(data, status){
												//注释代码能支持批量删除
		                    					//if(inst.is_selected(obj)) {
												//	inst.delete_node(inst.get_selected());
												//}
												//else {
												//	inst.delete_node(obj);
												//}
												inst.delete_node(obj);
											},
											complete: function(XMLHttpRequest, status){
											},
											error: function(XMLHttpRequest, textStatus){
												jAlert(XMLHttpRequest.responseText,"提示");
											}
											});	
			                    		}
			                    	})
			                    } 
			                },
			            "daochu": {  
			            "label": "导出",
								"icon" : "<%=request.getContextPath() %>/js/jstree/themes/default/export.png",
			                    "action": function (data) {
			                    window.open("officeequip_excel.jsp?sql=select code from oa_tree_select"); 
			                    } 
			                }
			 			}
			 		}
				}).bind('move_node.jstree', function (e, data) {//绑定移动节点事件
				    //data.node.id移动节点的id
				    //data.parent移动后父节点的id
				    //data.position移动后所在父节点的位置，第一个位置为0
				    $.ajax({
						type: "post",
						url: "basic_tree_select_do.jsp",
						dataType: "json",
						data: {
							op: "move",
							code: data.node.id+"",
							parent_code: data.parent+"",
							position : data.position+"" 
						},
						success: function(data, status){
							if(data.ret == 0){
								jAlert(data.msg,"提示");
								window.location.reload(true);   
							}  
						},
						complete: function(XMLHttpRequest, status){
						},
						error: function(XMLHttpRequest, textStatus){
							jAlert("移动失败！","提示");
							window.location.reload(true); 
						}
					});	
					for(var i=0;i<listCode.length;i++){
						//$("#"+listCode[i]+" a").first().css("font-weight","bold");
					} 
           		}).bind('select_node.jstree', function (e, data) {//绑定选中事件
           		}).bind('click.jstree', function(event) {   
               		$('#quipbottom').hide();
			    });
           		$.toaster({priority : 'info', message : '右键菜单可管理，菜单项可拖动' });
			  });
			  function addNewNode(myId,myText){
			  		if (code == undefined){ 
			  			code = "root";
			  		}
           			myjsTree.jstree('create_node', code+"", {'id' : myId+"", 'text' : myText+""}, 'last');
           			if (code != '<%=root_code%>') {
           				myjsTree.jstree("toggle_node", code);
           			}
           			myjsTree.jstree("deselect_all");
           			myjsTree.jstree("select_node", myId);
           	  }
           	  function modifyTitle(name){
					inst.set_text(node, name, "zh");
					myjsTree.jstree("deselect_all");
					myjsTree.jstree("select_node", node.id);
	            	for(var i=0;i<listCode.length;i++){
						if(listCode[i] == node.id+""){
							listCode.splice(i,1);
							break;
						}
					}
				}
		  </script>
		</TD>
    </TR>
  </tbody>
</table>
</div>
<div id="quipbottom" style="width: 100%;position:absolute; bottom:0; left:0;" >
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1"><span class="thead" style="PADDING-LEFT: 10px"><%=rootName %>增加或修改</span></td>
    </tr>
  </tbody>
</table>
<form action="basic_tree_select_do.jsp" method="post" name="form1" target="dirhidFrame" id="form1" onsubmit="return form1_onsubmit()">
<table width="434" align="center" class="tabStyle_1 percent80" >
    <tr>
      <td width="120" rowspan="9" align="left" valign="top" style="word-break:break-all"><br />
        当前节点：<br />
        <font color="blue" id="parent_name"></font> </td>
        <td align="left" id='codeText'>编码&nbsp;<span id="span_code"></span><input type="hidden" name="code" id="code" /></td>
    </tr>
    <tr>
      <td align="left">名称
        <input type="hidden" name="op" id="op" />
      <input name="name" id="name" size=12/>
      <input type="hidden" name="parent_code" id="parent_code" />
      <input type="hidden" name="root_code" id="root_code" /></td>
    </tr>
    <tr>
      <td align="left">
		<lt:Label res="res.label.forum.admin.menu_bottom" key="link"/>
        <input id="link" name="link" value="" />      
      </td>
    </tr>
    <tr>
      <td align="left">
      	类型
        <select id="preCode" name="preCode" onchange="onChangePreCode()">
          <option value="">
          <lt:Label res="res.label.forum.admin.menu_bottom" key="none"/>
          </option>
		  <option value="<%=TreeSelectDb.PRE_CODE_FLOW %>">流程</option>
		  <option value="<%=TreeSelectDb.PRE_CODE_MODULE %>">模块</option>
        </select>
        <span id="spanModule">
        <select id="formCode" name="formCode" onchange="o('preCode').value='module';onChangePreCode()">
        <option value="">选择智能模块</option>
        <%
        com.redmoon.oa.flow.FormDb fd = new com.redmoon.oa.flow.FormDb();
        com.redmoon.oa.visual.ModuleSetupDb msd = new com.redmoon.oa.visual.ModuleSetupDb();
        String sql = "select code from visual_module_setup where is_use=1 order by code";
        Iterator mir = msd.list(sql).iterator();
        while (mir.hasNext()) {
            msd = (com.redmoon.oa.visual.ModuleSetupDb)mir.next();
        %>
          <option value="<%=msd.getString("code")%>"><%=msd.getString("name")%></option>
        <%}%>
        </select>
        </span>
        <span id="spanFlow">
        <select id="flowTypeCode" name="flowTypeCode" onchange="if (this.value=='not') {jAlert('请选择流程类型！','提示'); return;} o('preCode').value='flow'; onChangePreCode()">
        <%
        com.redmoon.oa.flow.Leaf flowrootlf = new com.redmoon.oa.flow.Leaf();
        flowrootlf = flowrootlf.getLeaf(com.redmoon.oa.flow.Leaf.CODE_ROOT);
        if (flowrootlf != null) {
            com.redmoon.oa.flow.DirectoryView flowdv = new com.redmoon.oa.flow.DirectoryView(flowrootlf);
            flowdv.ShowDirectoryAsOptions(request, out, flowrootlf, flowrootlf.getLayer());
        }
        %>
        </select>
        </span>        
      </td>
    </tr>
    <tr>
      <td>
      	<span id="showParent" align="left" style="display:none">
       	 父节点
        <input id="parentCode" name="parentCode" type="hidden" value=""/>
        <input id="flag" name="flag" type="hidden" value=""/>
        <span id="parentName"></span>
		&nbsp;&nbsp;<a href="javascript:;" onclick="window.open('../officeequip/officeequip_sel.jsp?root_code=<%=root_code %>','_blank','toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width=640,height=480')">选择</a>
        </span></td>
    </tr>
    <tr>
      <td align="center" colspan="2">
      	<input type="button" class="btn" onclick="mysubmit()" value="确定" />&nbsp;&nbsp;&nbsp;<input type="button" class="btn" onclick="myreset()" value="重置" />      </td>
    </tr>
</table>
</form>
</div>
<script>
$(function() {
    $('#formCode').select2();
	$('#quipbottom').hide();
	$('#spanFlow').hide();
	$('#spanModule').hide();
});

function mysubmit(){
	o("root_code").value = getRootCode();
	$.ajax({
		url: "basic_tree_select_do.jsp?" + $('#form1').serialize(),
		type: "post",
		dataType: "json",
		//data: $('#form1').serialize(),
		success: function(data, status){
			if(data.ret == 1){
				$("#quipbottom").hide();
				if("modify"==$("#op").val()){
					modifyTitle($("#name").val());
					jAlert_Redirect(data.msg, "提示", "basic_tree_select_frame.jsp?root_code=<%=root_code%>");
				}else if("AddChild"==$("#op").val()){
					addNewNode($("#code").val(),$("#name").val());
				}
			}else if(data.ret == 2){
				jAlert(data.msg,"提示");
			}
		},
		error: function(XMLHttpRequest, textStatus){
			jAlert(XMLHttpRequest.responseText,"提示");
		}
	});	
}

function myreset(){
	document.getElementById("name").value = "";
}

function onChangePreCode() {
	if (o("preCode").value=="") {
		o("link").disabled = false;
		$('#spanFlow').hide();
		$('#spanModule').hide();		
	}
	else if (o("preCode").value=="flow") {
		o("link").value = "";
		o("link").disabled = true;
		$('#spanFlow').show();
		$('#spanModule').hide();
	}
	else if (o("preCode").value=="module") {
		o("link").value = "";
		o("link").disabled = true;
		$('#spanFlow').hide();
		$('#spanModule').show();	
	}
}
</script>
</body>
</html>
