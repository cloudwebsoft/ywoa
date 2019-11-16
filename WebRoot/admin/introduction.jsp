<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" contentType="text/html; charset=utf-8"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@page import="com.redmoon.oa.kernel.License"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>操作引导</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/introduction/introduction.css" />  
<script src="../js/jquery.js"></script>
<script src="../inc/common.js"></script>
<script type="text/javascript" src="js/tabpanel/TabPanel.js"></script>
<script>
function closeWindow(){
	window.top.refreshWindow();
}
</script>
</head>
<body>
<%
if (!privilege.isUserPrivValid(request, "admin")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<div class="introduction-layout">
<!--第一步用户导入-->
  <div class="introduction-stepnor" onClick="addTab('导入用户', 'admin/user_import.jsp?flag=introduction')" onMouseOver="this.className='introduction-stepsel'" onMouseOut="this.className='introduction-stepnor'">
    <div class="introduction-step1">
      <p class="introduction-p1" ><a href="javascript:;">导入用户</a></p>
      <p class="introduction-p2">导入用户信息（部门、邮箱、手机号、通讯地址...）</p>
    </div>
  </div>
<!--第二步角色修改-->
  <div class="introduction-stepnor" onClick="addTab('角色列表', 'admin/user_role_m.jsp?flag=introduction')" onMouseOver="this.className='introduction-stepsel'" onMouseOut="this.className='introduction-stepnor'">
    <div class="introduction-step2">
      <p class="introduction-p1"><a href="javascript:;">角色管理</a></p>
      <p class="introduction-p2">管理角色、权限</p>
    </div>
  </div>
<!--第三步权限配置-->
  <div class="introduction-stepnor" onClick="addTab('权限中心', 'admin/priv_center.jsp')" onMouseOver="this.className='introduction-stepsel'" onMouseOut="this.className='introduction-stepnor'">
     <div class="introduction-step3">
      <p class="introduction-p1"><a href="javascript:;">权限中心</a></p>
      <p class="introduction-p2">配置模块相关的管理权限</p>
    </div>
  </div>
<!--第四步考勤时间-->
  <div class="introduction-stepnor" onClick="addTab('配置管理', 'admin/config_m.jsp?flag=introduction')" onMouseOver="this.className='introduction-stepsel'" onMouseOut="this.className='introduction-stepnor'">
     <div class="introduction-step4">
      <p class="introduction-p1"><a href="javascript:;">考勤配置</a></p>
      <p class="introduction-p2">配置考勤时间、工作日历</p>
    </div>
  </div>
<!--第五步流程修改-->
  <div class="introduction-stepnor" onClick="addTab('流程管理', 'admin/flow_predefine_frame.jsp')" onMouseOver="this.className='introduction-stepsel'" onMouseOut="this.className='introduction-stepnor'">
     <div class="introduction-step5">
      <p class="introduction-p1"><a href="javascript:;">流程配置</a></p>
      <p class="introduction-p2">对流程、表单进行配置</p>
    </div>
  </div>
<!--第六步功能中心-->
  <div class="introduction-stepnor" onClick="addTab('功能中心', 'admin/functionManage.jsp?flag=introduction')" onMouseOver="this.className='introduction-stepsel'" onMouseOut="this.className='introduction-stepnor'">
     <div class="introduction-step6">
      <p class="introduction-p1"><a href="javascript:;">功能中心</a></p>
      <p class="introduction-p2">选择用户的系统功能</p>
    </div>
  </div>
<!--第七步-->
  <div class="introduction-stepnor" onClick="closeWindow()" onMouseOver="this.className='introduction-stepsel'" onMouseOut="this.className='introduction-stepnor'">
     <div class="introduction-step7">
      <p class="introduction-p1">
      	<a href="#">开始您的<%=com.redmoon.oa.Config.getInstance().get("enterprise")%>之旅！</a>
      </p>
    </div>
  </div>
</div>
</body>
</html>
