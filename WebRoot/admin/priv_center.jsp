<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" contentType="text/html; charset=utf-8"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.kernel.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>权限中心</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/admin/privCenter/priv_center.css" />  
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script>
function addClasses(obj){
	if ($(obj).hasClass('priv_center_boxnormal')) {
		$(obj).removeClass('priv_center_boxnormal');
		$(obj).addClass('priv_center_boxsel');
	}
} 

function removeClasses(obj){
	if ($(obj).hasClass('priv_center_boxsel')) {
		$(obj).removeClass('priv_center_boxsel');
		$(obj).addClass('priv_center_boxnormal');
	}
}
	
</script>
</head>
<body>
<div class="priv_center_layout">
<%
	if (!privilege.isUserPrivValid(request, "admin")) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	//判断许可证
	License license = License.getInstance();
	String kind = license.getKind().trim();
	String sql = (new StringBuilder("select id,code,name,image from oa_privilege_center where parent_id=-1 and license_type = ")).append(StrUtil.sqlstr(kind)).toString();
 	JdbcTemplate rmconn = new JdbcTemplate();
	ResultIterator ri = rmconn.executeQuery(sql);
	ResultRecord rr = null;
	int id = 0;
	String code = null;
	String name = null;
	String image = null;
 	while (ri.hasNext()) {
 		rr = (ResultRecord)ri.next();
 		id = rr.getInt(1);
 		code = rr.getString(2);
 		name = rr.getString(3);
 		image = rr.getString(4);
 		%>
 		<div class="priv_center_boxnormal" onMouseOver="addClasses(this)" onMouseOut="removeClasses(this)"  onClick="addTab('权限分配', 'admin/priv_center_allot.jsp?parent_id=<%=id %>')">
	      <div class="priv_center_box1_icon"><img src="<%=SkinMgr.getSkinPath(request)%>/images/priv_center/<%=image %>" width="50" height="50" /></div>
	      <div class="priv_center_box1_text">
	        <ul>
	        	<li class="priv_center_blue18"><a href="#"><%=name %></a></li>
	        	<%
	        		sql = (new StringBuilder("select name from oa_privilege_center where parent_id=")).append(id).toString();
	        		ResultIterator ri1 = rmconn.executeQuery(sql);
	        		ResultRecord rr1 = null;
	        		String childName = null;
	        		int count = 0;
	        		while (ri1.hasNext()) {
	        			count++;
	        			rr1 = (ResultRecord)ri1.next();
	        			childName = rr1.getString(1);
	        			%>
	        			<li class="priv_center_grey"><%=childName %></li>
	        			<%
	        			if(count == 2){
	        				break;
	        			}
	        		}
	        	 %>
	        	</br>
	        </ul>
	      </div>
	  	</div>
	 	<%
 	}
 %>   
</div>
</body>
</html>
