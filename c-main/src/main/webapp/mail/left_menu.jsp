<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="com.redmoon.oa.emailpop3.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="cn.js.fan.db.ResultIterator"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
	String myname = privilege.getUser(request);
	int i = 0,k = 1;
	String email = "",email_user="",email_pwd="",mailserver="";
	int id,port;
	
	
	int subMenu = ParamUtil.getInt(request,"subMenu",-1);
	int subMenuButton = ParamUtil.getInt(request,"subMenuButton",-1);
	int popId = ParamUtil.getInt(request,"popId",-1);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>邮件左侧菜单</title>
<script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link href="../skin/outside_mail.css" type="text/css" rel="stylesheet" />

<style>
html,body {
	width:100%;
	height:100%;
	padding:0;
	margin:0;
}
td {
	vertical-align:middle;
}

</style>
<script>
	var current = 1;
	function $() {
		var elements = new Array(); 
  		for (var i = 0; i < arguments.length; i++) { 
			var element = arguments[i]; 
			if (typeof element == 'string') {
      			element = document.getElementById(element);
			} 
    		if (arguments.length == 1) {
      			return element;
			}
    		elements.push(element); 
  		} 
  		return elements; 
	}

	function addListener(element, evt, fun){
		if(element.addEventListener){
			 element.addEventListener(evt, fun, false);
		} else {
			 element.attachEvent("on" + evt, fun);
		}
	}
	
	function subMenuHighLight(i) {
		var elem = o("subMenuButton_" + i);
		if (elem==null)
			return;
		elem.className = "subMenuButton subMenuActive";
	}
	
	function subMenuHighCurrent(i) {
		current = i;
		var elements = document.getElementsByTagName("div");
		for(j=0; j<elements.length; j++) {
			if(elements[j].className=="subMenuButton" || elements[j].className=="subMenuButton subMenuActive") {
				elements[j].className="subMenuButton";
			}
		}
		subMenuHighLight(i);
		
	}
	
	function subMenuNormal(i) {
		var elem = o("subMenuButton_" + i);
		if(i != current) {
			elem.className = "subMenuButton";
		}
	}

	function switchSubMenu(i) {
			if(document.getElementById("menuButton_"+i) != null){
				document.getElementById("menuButton_"+i).style.backgroundColor="#c1dcf1";
			
				var elem = o("subMenu_" + i);
				if (elem==null)
					return;
				var img = o("menuButtonImg_" + i);
				
				
				if (elem.style.display == "") {
					elem.style.display = "none";
					img.src = "images/closed.gif";
				} else {
					elem.style.display = "";
					img.src = "images/open.gif";
				}
			}
		}

	function setEmail(){
		parent.rightFrame.location.href="set_email_pop.jsp";
	}


	function window_load(){
		if("<%=subMenu%>" != "-1"){
			if("<%=popId%>" != "-1"){
				o('emailId').value=<%=popId%>;
			}
			current = <%=subMenuButton%>;
			switchSubMenu(<%=subMenu%>);
			subMenuHighLight(<%=subMenuButton%>);
		}else{
			switchSubMenu(1);
			subMenuHighLight(1);
		}

		if (GetCookie("scroll")!=null){
			document.getElementById("body").scrollTop=GetCookie("scroll");
		}   
		
	}

    
	  function Trim(strValue)   
	  {   
	  	return strValue.replace(/^\s*|\s*$/g,"");   
	  }   
	  function SetCookie()   
	  {   
		 var sValue= document.getElementById("body").scrollTop;
	  	document.cookie = "scroll" + "=" + escape(sValue);   
	  }   
	  function GetCookie(sName)   
	  {   
	  	  var aCookie = document.cookie.split(";");   
		  for (var i=0; i < aCookie.length; i++)   
		  {   
		  var aCrumb = aCookie[i].split("=");   
		  if (sName == Trim(aCrumb[0]))   
		  {   
		  return (aCrumb[1]);   
		  }   
		  }   
		  return null;   
	  }   
	  
		
	
</script>
</head>

<body onload="window_load()"  >
<div id="leftMenu" >
  <div class="menuBoxA">
  	<a href="#" id="inBox" onclick="if (!o('emailId')) {return;} parent.rightFrame.location.href='in_box.jsp?id=' + o('emailId').value">
  		<img src="images/receive_mail.png" />
  	</a>
  	<a href="#" onclick="if (!o('emailId')) {return;} parent.rightFrame.location.href='send_mail.jsp?emailId=' + o('emailId').value+'&subMenu='+o('menuButton').value" >
  		<img src="images/send_mail.png" />
  	</a>
  	
	<a href="#" onclick="setEmail()"><img src="images/email_set.png" /></a>
	  
  </div>
  <div class="menuBoxB">
  	<div style="height:100%;">
    <div class="menu" style="overflow:auto;height:97%;" id="body" onscroll="SetCookie()">
<%
	boolean first = true;
	EmailPop3Db epd = new EmailPop3Db();
	Iterator ir = epd.getEmailPop3DbOfUser(myname).iterator();
	while (ir.hasNext()) {
		epd = (EmailPop3Db)ir.next();
		
		
		email = epd.getEmail();
		email_user = epd.getEmailUser();
		email_pwd = epd.getEmailPwd();
		id = epd.getId();
		
		
		
		
		if(first) {
			first = false;
%>
		<input type="hidden" name="emailId" id="emailId" value="<%=id%>" />
		<input type="hidden" name="menuButton" id="menuButton" value="<%=subMenu==-1?1:subMenu %>" />
<%
		}
		mailserver = epd.getServer();
		port = epd.getPort();
		if(!epd.getServer().equals("")){
			String inBox_sql = "select * from email where email_addr=" + StrUtil.sqlstr(email) + " and msg_type=" + MailMsgDb.TYPE_INBOX+" and is_readed = 0";
			
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(inBox_sql);
			int inBoxSize = ri.size();
			
			
			String listBox_sql = "select * from email where email_addr=" + StrUtil.sqlstr(email) + " and msg_type=" + MailMsgDb.TYPE_SENDED+" and is_readed = 0";
			JdbcTemplate jt1 = new JdbcTemplate();
			ResultIterator ri1 = jt1.executeQuery(listBox_sql);
			int listBoxSize = ri1.size();
			i++;
%>
      <div class="menuButton" id="menuButton_<%=i%>" onclick="o('emailId').value=<%=id%>;o('menuButton').value=<%=i %>;switchSubMenuMore(<%=i%>);">
        <table width="100%"  border="0" cellpadding="0" cellspacing="0">
          <tr>
	            <td width="10">
	          	 <img src="images/person.png" style="width:13px;height:13px;padding-left:8px;"/>
	            </td>
	            <td width="15"><img class="menuButtonImg" id="menuButtonImg_<%=i%>" src="images/closed.gif" /></td>
	            <td title="<%=email%>">
	            	<%
	            		String newEmail = "";
	            		String subStartEmail = "";
	            		String subEndEmail = "";
	            		if(email.length()>=18){
	            			subStartEmail = email.substring(0,5);
	            			subEndEmail = email.substring(email.lastIndexOf("@"),email.length());
	            			newEmail  = subStartEmail +"..."+subEndEmail;
	            		}else{
	            			newEmail = email;
	            		}
	            	%>
	            	<%=newEmail %>
	            </td>
	            
          </tr>
        </table>
      </div>
      <div class="subMenu" id="subMenu_<%=i%>" style="display:none">
        <div class="subMenuButton" id="subMenuButton_<%=k%>" onclick="o('emailId').value=<%=id%>;o('menuButton').value=<%=i %>;subMenuHighCurrent(<%=k%>);parent.rightFrame.location.href='in_box.jsp?id=<%=epd.getId()%>&subMenu=<%=i %>&subMenuButton=<%=k %>'" onmouseover="subMenuHighLight(<%=k%>)" onmouseout="subMenuNormal(<%=k++%>)">
          <table width="100%" height="100%" border="0" cellpadding="0" cellspacing="2">
            <tr>
              <td width="36"><img class="subMenuButtonImg" src="images/in_box.png" /></td>
              <td>收件箱
              <%if(inBoxSize != 0){ %>
              <font color="red">(<%=inBoxSize %>)</font>
              <%} %>
              </td>
            </tr>
          </table>
        </div>
		<div class="subMenuButton" id="subMenuButton_<%=k%>" onclick="o('emailId').value=<%=id%>;o('menuButton').value=<%=i %>;subMenuHighCurrent(<%=k%>);parent.rightFrame.location.href='list_box.jsp?id=<%=epd.getId()%>&box=<%=MailMsgDb.TYPE_SENDED%>&subMenu=<%=i %>&subMenuButton=<%=k %>'" onmouseover="subMenuHighLight(<%=k%>)" onmouseout="subMenuNormal(<%=k++%>)">
          <table width="100%" height="100%" border="0" cellpadding="0" cellspacing="2">
            <tr>
              <td width="36"><img class="subMenuButtonImg" src="images/out_box.png" /></td>
              <td>发件箱
              <%if(listBoxSize != 0){ %>
              <font color="red">(<%=listBoxSize %>)</font>
              <%} %>
              </td>
            </tr>
          </table>
        </div>
        <div class="subMenuButton" id="subMenuButton_<%=k%>" onclick="o('emailId').value=<%=id%>;o('menuButton').value=<%=i %>;subMenuHighCurrent(<%=k%>);parent.rightFrame.location.href='list_draft.jsp?id=<%=epd.getId()%>&box=<%=MailMsgDb.TYPE_DRAFT%>&subMenu=<%=i %>&subMenuButton=<%=k %>'" onmouseover="subMenuHighLight(<%=k%>)" onmouseout="subMenuNormal(<%=k++%>)">
          <table width="100%" height="100%" border="0" cellpadding="0" cellspacing="2">
            <tr>
              <td width="36"><img class="subMenuButtonImg" src="images/draft_box.png" /></td>
              <td>草稿箱</td>
            </tr>
          </table>
        </div>
        <div class="subMenuButton" id="subMenuButton_<%=k%>" onclick="o('emailId').value=<%=id%>;o('menuButton').value=<%=i %>;subMenuHighCurrent(<%=k%>);parent.rightFrame.location.href='list_delete.jsp?id=<%=epd.getId()%>&box=<%=MailMsgDb.TYPE_DUSTBIN%>&subMenu=<%=i %>&subMenuButton=<%=k %>'" onmouseover="subMenuHighLight(<%=k%>)" onmouseout="subMenuNormal(<%=k++%>)">
          <table width="100%" height="100%" border="0" cellpadding="0" cellspacing="2">
            <tr>
              <td width="36"><img class="subMenuButtonImg" src="images/del_box.png" /></td>
              <td>已删除</td>
            </tr>
          </table>
        </div>
      </div>
<%
		}
	}
%>
    </div>
	  
  </div>
  </div>
</div>
</body>
<script>
function switchSubMenuMore(i) {
	for(var a =1;a<=<%=i%>;a++){
		if(a == i){
			document.getElementById("menuButton_"+a).style.backgroundColor="#c1dcf1";
			
			
			var elem = o("subMenu_" + a);
			if (elem==null)
				return;
			var img = o("menuButtonImg_" + a);
			if (elem.style.display == "") {
				elem.style.display = "none";
				img.src = "images/closed.gif";
			} else {
				parent.rightFrame.location.href="in_box.jsp?id="+o('emailId').value+"&box=<%=MailMsgDb.TYPE_SENDED%>&subMenu="+a+"&subMenuButton="+(a * 4 -3);
				subMenuHighCurrent((a * 4 -3));
				elem.style.display = "";
				img.src = "images/open.gif";
			}
		}else{

			document.getElementById("menuButton_"+a).style.backgroundColor="";
			
			var elem = o("subMenu_" + a);
			if (elem==null)
				return;
			var img = o("menuButtonImg_" + a);
			elem.style.display = "none";
			img.src = "images/closed.gif";
		}
	}
}
</script>
</html>
