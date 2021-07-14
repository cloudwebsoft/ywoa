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
<%@ page import = "com.redmoon.oa.pvg.Privilege"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<%
String path = request.getContextPath(); 

%>
<head>
<title>题库管理</title>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../js/pagination/jquery.pagination.js"></script>
<script type="text/javascript" src="<%=path %>/js/jstree/jstree_inc_children.js"></script>
<link type="text/css" rel="stylesheet" href="<%=path %>/js/jstree/themes/default/style.css" />
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" /> 
<link href="../js/pagination/pagination.css" rel="stylesheet" type="text/css" media="screen" />
<style type="text/css">
</style>
</head>
<body>
<%
	String type = ParamUtil.get(request,"type");
	String code = ParamUtil.get(request,"major");
 %>
	<div id =" q_menu" style="margin-left: 30px;margin-top: 90px">
      	<input type="hidden" id="major" name ="major" value =""/>
		<%
			TreeSelectDb tsd = new TreeSelectDb();
			tsd = tsd.getTreeSelectDb(MajorView.ROOT_CODE);
			MajorView mv = new MajorView(tsd); 
			String jsonData = mv.getJsonByCode(code);
			// System.out.println(getClass() + "json:" + jsonData);
		%>
		<div id="examMajorTree" class="departmentTree" ></div>
	</div>
	<script >
		var framec = window.parent.document.getElementById("questionListId");
		var type = "<%=type%>";
		$("#examMajorTree").jstree({  
			'core' : {  
			    "multiple" : false,  
			    'data' : <%=jsonData%>,  
			    "check_callback" : true,
			    "ui" : {"initially_select" : [ "root" ]  },
			"plugins" : ["wholerow", "themes", "ui", ,"types","state"],
			'dblclick_toggle': false
			                //禁用tree的双击展开  
			} 
		})
		.bind('select_node.jstree', function (e, data) {//绑定选中事件
		    o("major").value =data.node.id; 
		    framec.src = "exam_question_select.jsp?type="+type+"&op=search&major="+data.node.id;
		})
		.on("loaded.jstree", function (e, data) {//默认选中根节点
        $("#examMajorTree").jstree("deselect_all",true);
        $('#examMajorTree').jstree('select_node','<%=MajorView.ROOT_CODE%>',true);
   	});
	$(function(){
	})
	</script>
</body>
</html>

