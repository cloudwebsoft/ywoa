<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.exam.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.JdbcTemplate"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%
String path = request.getContextPath(); 
%>
<head>
<title>考试专业权限管理</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../js/pagination/jquery.pagination.js"></script>
<script type="text/javascript" src="<%=path %>/js/jstree/jstree_inc_children.js"></script>
<link type="text/css" rel="stylesheet" href="<%=path %>/js/jstree/themes/default/style.css" />
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" /> 
<link href="../js/pagination/pagination.css" rel="stylesheet"	type="text/css" media="screen" />
<style type="text/css">
</style>
</head>
<body>
	<div id =" q_menu" style="margin-left: 20px;margin-top: 30px;">
 		<input type="hidden" id="major" name ="major" value =""/>
	<%
		TreeSelectDb tsd = new TreeSelectDb();
		tsd = tsd.getTreeSelectDb(MajorView.ROOT_CODE);
		MajorView mv = new MajorView(tsd);
		String jsonData = mv.getJsonString();
	%>
	<div id="examMajorTree" class="examMajorTree" ></div>
	</div>
	<script>
		function ajaxPost(path,parameter,func){
			$.ajax({
				type: "post",
				url: path,
				data: parameter,
				dataType: "html",
				contentType:"application/x-www-form-urlencoded; charset=iso8859-1",			
				success: function(data, status){
					func(data);
				},
				error: function(XMLHttpRequest, textStatus){
					alert(XMLHttpRequest.responseText);
				}
			});
		}
		var rootCode = "<%=MajorView.ROOT_CODE%>";
		var framec = window.parent.document.getElementById("examMajorLeft");
		$("#examMajorTree").jstree({  
			'core' : {  
		    "multiple" : false,  
		    'data' : <%=jsonData%>,  
		    "check_callback" : true,
		     "themes" : {
						   "theme" : "default" ,
						   "dots" : true,  
						   "icons" : true  
						},
			'dblclick_toggle': false
		                //禁用tree的双击展开  
		},
		"ui" : {"initially_select" : [ "root" ]  },
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
						framec.src = "major_priv_right_add.jsp?code="+code+"&name="+name+"&op=add";
                    }
                },
                "rename": {  
                    "label": "修改",
					"icon" : "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_alter.png",
                    "action": function (data) { 
                    	var inst = $.jstree.reference(data.reference);
						node = inst.get_node(data.reference);
						code = node.id;
						var name = node.text;
						var parentCode = node.parent;
						if(code==rootCode){
							parent.parent.jAlert("没有权限","提示");
						}else{
							framec.src = "major_priv_right_edit.jsp?code="+code+"&name="+name+"&op=edit"+"&parentCode="+parentCode;
						}
                    }
                },
                "remove": {  
					"label": "删除",
					"icon" : "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_close.png",
					"action": function (data) { 
						inst = $.jstree.reference(data.reference);
						var obj = inst.get_node(data.reference);
						node = inst.get_node(data.reference);
						code = node.id;
						if( rootCode == code){
                    		parent.parent.jAlert("根节点不能被删除!","提示");
                    		return;
                    	}
                    	parent.parent.jConfirm("删除后其子节点相应被删除，您确定要删除吗?","提示",function(r){
							if(!r){
								return;
							}else{
								ajaxPost('../question/questionKindDel.do',{delcode:code+""},function(data){
									data = $.parseJSON(data);
									if(data.ret=="1"){
										inst.delete_node(obj);
										parent.parent.jAlert(data.msg,"提示");
									}else if(data.ret=="0"){
										parent.parent.jAlert(data.msg,"提示");
									}else if(data.ret=="2"){
										parent.parent.jAlert(data.msg,"提示");
									}
								});								
							}
						})
					} 
				}
            }
        }
 		 
		})
		.bind('select_node.jstree', function (e, data) {//绑定选中事件
		    o("major").value =data.node.id; 
		    	framec.src = "major_priv_manage.jsp?majorCode="+data.node.id;
		});
	</script>
</body>
</html>

