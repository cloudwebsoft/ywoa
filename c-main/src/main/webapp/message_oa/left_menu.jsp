<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.message.*"%>
<%@page import="cn.js.fan.db.ListResult"%>
<%@page import="cn.js.fan.db.Conn"%>
<%@page import="cn.js.fan.web.Global"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<% 
	if (!privilege.isUserLogin(request)) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	String name = privilege.getUser(request);
	String op = ParamUtil.get(request, "op");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" />
<title>内部邮箱左侧菜单</title>
<link href="<%=SkinMgr.getSkinPath(request)%>/css/message/message.css" rel="stylesheet" type="text/css" />
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script>
    $(document).ready(function(){ 
		$("dt").each(function(index){ 
		$(this).click(function(){ 
			$("dt").removeClass("first"); 
			$("dt").eq(index).addClass("first");
		}); 
		}); 
    });
     
</script>
</head>

<body onload="reset()">
<div class="message_list" style="margin-left:12px;">
  <div class="message_list_bgtop"></div>
  <div class="message_list_bgmiddle">
     <div class="message_list_det">
        <dl>
           <dt><img src="<%=SkinMgr.getSkinPath(request)%>/images/message/message_list_icon01.png" width="29" height="29" /><a href="javascript:;"  onclick="parent.rightFrame.location.href='send.jsp'"> 写信</a></dt>
           <dt class="first"><img src="<%=SkinMgr.getSkinPath(request)%>/images/message/message_list_icon02.png" width="29" height="29" /><a href="javascript:;"  onclick="parent.rightFrame.location.href='message.jsp'">收件箱
           <%
			MessageDb md = new MessageDb();
			String sql = "select count(id) from oa_message where isreaded=0 and box=0 and is_dustbin=0 and type=0 and receiver="+StrUtil.sqlstr(name);
			int total = 0;
			Conn conn = new Conn(Global.getDefaultDB());
			ResultSet rs = null;
			try {
				rs = conn.executeQuery(sql);
				if(rs != null && rs.next()) {
		            total = rs.getInt(1);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if(rs != null) {
		            rs.close();
		            rs = null;
		        }
			}
           if(total !=0 ){ %>
           		<font color="red" id="unreaded">(<%=total %>)</font>
           <%} %>
           </a>
           </dt>
           <dt><img src="<%=SkinMgr.getSkinPath(request)%>/images/message/message_list_icon03.png" width="29" height="29" /><a href="javascript:;"  onclick="parent.rightFrame.location.href='listdraft.jsp'">草稿箱</a></dt>
           <dt><img src="<%=SkinMgr.getSkinPath(request)%>/images/message/message_list_icon04.png" width="29" height="29" /><a href="javascript:;"  onclick="parent.rightFrame.location.href='listoutbox.jsp'">已发送</a></dt>
           <dt><img src="<%=SkinMgr.getSkinPath(request)%>/images/message/message_list_icon05.png" width="29" height="29" /><a href="javascript:;"  onclick="parent.rightFrame.location.href='listrecycle.jsp'">垃圾箱</a></dt>
        </dl>
     </div>
  </div>
  <div class="message_list_bgbottom"></div>
</div>
</body>
<script>
	function reset(){
  	if("<%=op%>"=="1"){
	    $("dt").removeClass("first"); 
	    $("dt").eq(2).addClass("first"); 
	 }
  }
</script>
</html>
