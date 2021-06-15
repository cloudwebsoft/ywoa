<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.redmoon.oa.emailpop3.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@page import="org.json.JSONObject"%>
<%@page import="org.jdom.Element"%>
<%@page import="cn.js.fan.security.ThreeDesUtil"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String userName = privilege.getUser(request);
	String op = ParamUtil.get(request,"op");
	int emailId = ParamUtil.getInt(request,"emailId",-1);
	int delEmailId = ParamUtil.getInt(request,"delEmailId",-1);
	int indexNum = ParamUtil.getInt(request,"indexNum",1);
	
	boolean re = false;
	if(op.equals("edit")){
		EmailPop3Mgr epm = new EmailPop3Mgr();
		try {
		re = epm.modify(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
		if (re) {
			JSONObject json = new JSONObject();
			json.put("ret","1");
			json.put("msg","操作成功!");
			out.print(json);
			return;
		}
	}
	
	if(op.equals("del")){
		EmailPop3Mgr epm = new EmailPop3Mgr();
		try {
			EmailPop3Db emailPop3Db = new EmailPop3Db();
			emailPop3Db = epm.getEmailPop3Db(delEmailId);
			re = emailPop3Db.del();
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
			return;
		}
		if (re) {
			JSONObject json = new JSONObject();
			json.put("ret","1");
			json.put("msg","操作成功!");
			out.print(json);
			return;
		}
	}
	
	
%>

<%
XMLConfig cfg = new XMLConfig("config_email.xml", false, "iso-8859-1");
Element root = cfg.getRootElement();
Iterator xml_ir = root.getChildren().iterator();

%>

<%

String errmsg = "";
String emailName = ParamUtil.get(request,"emailName");
String emailPass = ParamUtil.get(request,"emailPass");
String name = privilege.getUser(request);
if(op.equals("add")){
	EmailPop3Db emailPop3Db = new EmailPop3Db();
	int num = emailPop3Db.getEmailPop3DbOfUser(userName).size();
	
	JSONObject json = new JSONObject();
	if (emailName.equals("")){
        errmsg += "请输入EMAIL！<br/>";
	}
    if (!StrUtil.IsValidEmail(emailName)){
         errmsg += "Email的格式错误！<br/>";
    }
    if (emailPass.equals("")){
          errmsg += "请输入密码！<br/>";
    }
   
	
    if(errmsg.equals("")){
    	EmailPop3Db ep = new EmailPop3Db();
    	
    	EmailPop3Db epop = new EmailPop3Db();
    	epop = epop.getEmailPop3Db(userName,emailName);
    	
    	if(epop != null){
    		json.put("ret","2");
    		json.put("msg","邮箱已经在存！");
    		json.put("num","0");
    		json.put("epdId","0");
    		out.print(json);
    		return;
    	}
    	
    	
        ep.setUserName(name);
        ep.setEmail(emailName);
        ep.setEmailUser(emailName.split("@")[0]);
        emailPass = ThreeDesUtil.encrypt2hex("cloudwebcloudwebcloudweb",emailPass);
        ep.setEmailPwd(emailPass);
        ep.setServer("");
        ep.setPort(0);
        ep.setSmtpPort(0);
        ep.setServerPop3("");
        ep.setDelete(false);
        ep.setSsl(false);
        if(num == 0){
        	ep.setDefault(true);
        }else{
        	ep.setDefault(false);
        }
        re = ep.create();
    }
    if(re){
    	EmailPop3Db epd = new EmailPop3Db();
    	epd = epd.getEmailPop3Db(userName,emailName);
		json.put("ret","1");
		json.put("msg","操作成功!");
		json.put("num",(num+1));
		json.put("epdId",epd.getId());
		out.print(json);
		return;
    }else{
    	json.put("ret","0");
		json.put("msg",errmsg);
		json.put("num","0");
		json.put("epdId","0");
		out.print(json);
		return;
    }
}
	
%>

<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>新增邮箱</title>
<link href="../skin/outside_mail.css" type="text/css" rel="stylesheet" />
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"type="text/css" media="screen" />
<script src="../js/jquery.form.js"></script>
<script>
function addEmail(){
	var addTimes = $("#addTimes").val();
	if(addTimes == "0"){
		$("#addEmail").attr("style","display:block");
		$("#alphaDiv").attr("style","display:block");
		$("#addTimes").val("1");
	}else{
		$("#addEmail").attr("style","display:none");
		$("#alphaDiv").attr("style","display:none");
		$("#addTimes").val("0");
	}
}
function colseAdd(){
	$("#addEmail").attr("style","display:none");
	$("#alphaDiv").attr("style","display:none");
	$("#addTimes").val("0");
	
}

function form_submit(){	
	var id = $("#id").val();
	if(id == "0"){
		jAlert("请先新增邮箱!","提示");
	}
	
}
</script>
</head>
<body>
<div class="alphaDiv" id="alphaDiv" style="display:none;">
</div>
<div>
  <div class="inbox-wrap">
   <div class="inbox-right">
	 <div class="inbox-toolbar">
       <div class="inbox-right-btnbox" onclick="addEmail()"><img src="images/email-add.png" width="20" height="20"/>新增</div>
       <div class="inbox-right-btnbox" onclick="delEmail()"><img src="images/email-del.png" width="20" height="20"/>删除</div>
     </div>
	</div>
</div>
  <div class="inbox-install-leftbox">
  	<div style="overflow:auto;height:82%;">
    <ul>
      <%
      	EmailPop3Db epd = new EmailPop3Db();
  		Iterator ir = epd.getEmailPop3DbOfUser(userName).iterator();
  		String default_email = "";
  		String emailUser = "";
  		String emailPwd = "";
  		boolean isSsl = false;
  		boolean isDelete = false;
  		boolean isDefault = false;
  		int id = 0;
  		String smtpServer = "";
  		String popServer = "";
  		String popPort = "";
  		String smtpPort = "";
  		String SSLPopPort = "";
  		String SSLSmtpPort = "";
  		String smtpServer_db = "";
  		String popServer_db = "";
  		String smptPort_db = "";
  		String popPort_db = "";
  		int i = 1;
  		while (ir.hasNext()){
	  		epd = (EmailPop3Db)ir.next();
	  		if(i == 1){
	  			id = epd.getId();
	  			default_email = epd.getEmail();
	  			emailUser = epd.getEmailUser();
	  			emailPwd = epd.getEmailPwd();
	  			isSsl = epd.isSsl();
	  			isDelete = epd.isDelete();
	  			isDefault = epd.isDefault();
		  		smtpServer_db = epd.getServer();
		  		popServer_db = epd.getServerPop3();
		  		smptPort_db = String.valueOf(epd.getSmtpPort()).equals("0")?"":String.valueOf(epd.getSmtpPort());
		  		popPort_db = String.valueOf(epd.getPort()).equals("0")?"":String.valueOf(epd.getPort());
	  		}
	  		
	  		String email = epd.getEmail();
	   		String newEmail = "";
	   		String subStartEmail = "";
	   		String subEndEmail = "";
	   		if(email.length()>=18){
	   			subStartEmail = email.substring(0,5);
	   			subEndEmail = email.substring(email.indexOf("@"),email.length());
	   			newEmail  = subStartEmail +"..."+subEndEmail;
	   		}else{
	   			newEmail = email;
	   		}
	   		
	   		if(i == indexNum){
      %>
      <li class="inbox-install-leftbox-sel" onclick="selectEmail(<%=i %>,<%=epd.getId() %>)" id="email<%=i %>" value="<%=epd.getId() %>" ><%=newEmail %></li>
      <%}else{%>
      <li onclick="selectEmail(<%=i %>,<%=epd.getId() %>)" id="email<%=i %>" value="<%=epd.getId() %>" ><%=newEmail %></li>
      <%}
      	i++;
  		}%>
    </ul>
    </div>
  </div>
  <%
  	if(emailId != -1){
  		epd = epd.getEmailPop3Db(emailId);
  		id = epd.getId();
		default_email = epd.getEmail();
		emailUser = epd.getEmailUser();
		emailPwd = epd.getEmailPwd();
		isSsl = epd.isSsl();
		isDelete = epd.isDelete();
		isDefault = epd.isDefault();
		smtpServer_db = epd.getServer();
  		popServer_db = epd.getServerPop3();
  		smptPort_db = String.valueOf(epd.getSmtpPort()).equals("0")?"":String.valueOf(epd.getSmtpPort());
  		popPort_db = String.valueOf(epd.getPort()).equals("0")?"":String.valueOf(epd.getPort());
		
  	}
  
  while (xml_ir.hasNext()) {
		Element e = (Element) xml_ir.next();
		if(!default_email.equals("")){
			if(e.getAttributeValue("type").equals(default_email.split("@")[1])){
				smtpServer = e.getChildText("smtp");
				popServer = e.getChildText("pop3");
				popPort = e.getChildText("popPort");
				smtpPort = e.getChildText("smtpPort");
				SSLPopPort = e.getChildText("SSLPopPort");
				SSLSmtpPort = e.getChildText("SSLSmtpProt");
			}
		}
	}
  %>
  <div  class="inbox-install-rightbox">
  	<form name="form1" id="form1" action="" method="post" onsubmit="return form_submit();">
	  	<table class="inbox_table" width="100%">
	  		<tr>
	  			<td width="40%">邮箱</td>
	  			<td>
	  				<input type="text" name="email" id="email" value="<%=default_email %>" size="30" readonly style="background:#e4e4e4;"/>
	  				<input type="hidden" name="id" id="id" value="<%=id%>">
	  			</td>
	  		</tr>
	  		<tr>
	  			<td>用户名</td>
	  			<td><input type="text" name="emailUser" id="emailUser" value="<%=emailUser %>" size="30" maxlength="50"/></td>
	  		</tr>
	  		<tr>
	  			<td>密码</td>
	  			<td><input type="password" name="emailPwd" id="emailPwd" value="<%=ThreeDesUtil.decrypthexstr("cloudwebcloudwebcloudweb",emailPwd) %>" size="30" maxlength="50"/></td>
	  		</tr>
	  		<tr>
	  			<td>SMTP服务器</td>
	  			<td>
	  				<%
	  					if(smtpServer_db.equals("")){
	  						smtpServer_db = smtpServer;
	  					}
	  				%>
	  				<input type="text" name="server" id="server" value="<%=smtpServer_db %>" size="30"/>
	  			</td>
	  		</tr>
	  		<tr>
	  			<td>SMTP端口</td>
	  			<td>
	  				<%
	  					if(isSsl){
	  						if(smptPort_db.equals("")){
	  							smptPort_db = SSLSmtpPort;
	  						}
	  					}else{
	  						if(smptPort_db.equals("")){
	  							smptPort_db = smtpPort;
	  						}
	  					}
	  				%>
	  				<input type="text" name="smtpPort" id="smtpPort" value="<%=smptPort_db %>" size="30"/>
	  			</td>
	  		</tr>
	  		<tr>
	  			<td>POP3服务器</td>
	  			<td>
	  				<%
	  					if(popServer_db.equals("")){
	  						popServer_db = popServer;
	  					}
	  				%>
	  				<input type="text" name="serverPop3" id="serverPop3" value="<%=popServer_db %>" size="30"/>
	  			</td>
	  		</tr>
	  		<tr>
	  			<td>POP3端口</td>
	  			<td>
	  				<%
	  					if(isSsl){
	  						if(popPort_db.equals("")){
	  							popPort_db = SSLPopPort;
	  						}
	  					}else{
	  						if(popPort_db.equals("")){
	  							popPort_db = popPort;
	  						}
	  					}
	  				%>
	  				<input type="text" name="port" id="port" value="<%=popPort_db %>" size="30"/>
	  			</td>
	  		</tr>
	  		<tr>
	  			<td colspan="2">
	  				<div class="checkboxDiv">
	  					<input type="checkbox" name="isSsl" id="isSsl" value="1" <%=isSsl?"checked":""%> onchange="changeSSL()"/> 使用SSL<br/>
	  					<input type="checkbox" name="isDelete" id="isDelete" value="1" <%=isDelete?"checked":""%>/> 邮件收时删除服务器上邮件<br/>
	  					<input type="checkbox" name="isDefault" id="isDefault" value="1" <%=isDefault?"checked":""%>/> 是否为默认邮箱
	  				</div>
	  				
	  			</td>
	  		</tr>
	  		<tr align="center">
	  			<td colspan="2" style=""><input type="submit" class="setEmailButton" style="width:100px;height:34px;margin-top:20px;" value="确认"/></td>
	  		</tr>
	  	</table>
  	</form>
  </div>
</div>

<div class="inbox-pop" style="display:none;" id="addEmail">
  <div class="inbox-pop-colose" onclick="colseAdd()">
  </div>
  <div class="inbox-pop-txt">新增邮箱</div>
  <form name="form2" id="form2" action="" method="post">
	 <div class="inbox-pop-conbox">
	   <input type="hidden" name="addTimes" id="addTimes" value="0" />
	   <p>邮箱地址:<input type="text" name="emailName" id="emailName" value="" maxlength="50"></p> 
	   <p>密&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;码:<input type="password" name="emailPass" id="emailPass" value="" maxlength="50"></p>
	  </div>
	 <div >
    	<input type="submit" class="setAddEmailButton" style="width:100px;height:34px;margin-top:20px;" value="确认"/>
    </div>
  </form>
 
</div>

</body>
<script>
$(document).ready(function(){ 
	var options = { 
		success:showResponse,  // post-submit callback 
		url:"?op=edit"
		}; 
		$('#form1').submit(function() { 
			$(this).ajaxSubmit(options); 
			return false; 
		});

		var addOptions = {
				success:showResponse1,  // post-submit callback 
				url:"?op=add"
		};
		$('#form2').submit(function() { 
			$(this).ajaxSubmit(addOptions); 
			return false; 
		});
		
	});


function showResponse1(data1){
	data1 = $.parseJSON(data1);
	
	if(data1.ret == "1"){
		//document.getElementById("email"+data1.num).className="inbox-install-leftbox-sel";
		window.location.href = "set_email_pop.jsp?emailId="+data1.epdId+"&indexNum="+data1.num;
		//window.location.href = "set_email_pop.jsp";
		parent.leftFrame.location.reload();
	}
	if(data1.ret == "0"){
		jAlert(data1.msg,"提示");
	}
	if(data1.ret == "2"){
		jAlert(data1.msg,"提示");
	}
}

function showResponse(data){
	data = $.parseJSON(data);
	if(data.ret == "1"){
		window.location.href = "in_box.jsp";
		parent.leftFrame.location.reload();
	}
}

function selectEmail(obj,id){
	for(var a=1;a < <%=i%>;a++){
		if( a == parseInt(obj)){
			document.getElementById("email"+a).className="inbox-install-leftbox-sel";
			window.location.href = "set_email_pop.jsp?emailId="+id+"&indexNum="+obj;
		}else{
			document.getElementById("email"+a).className="";
		}
	}
}

function delEmail(){
	var be = false;
	for(var a=1;a < <%=i%>;a++){
		be = $("#email"+a).hasClass("inbox-install-leftbox-sel");
		if(be){
			var val = $("#email"+a).val();
			 jConfirm("您确定要删除么?","提示",function(r){
				if(!r){
					return;
					}else{
						$.ajax({
							type: "post",
							url: "set_email_pop.jsp",
							data : {
								op: "del",
								delEmailId: val
					        },
							dataType: "html",
							beforeSend: function(XMLHttpRequest){
							},
							success: function(data, status){
								data = $.parseJSON(data);
								if(data.ret == "1"){
									window.location.href = "set_email_pop.jsp";
									parent.leftFrame.location.reload();
								}
							},
							complete: function(XMLHttpRequest, status){
							},
							error: function(XMLHttpRequest, textStatus){
								// 请求出错处理
								alert(XMLHttpRequest.responseText);
							}
						});
					}
		 		})
			

			return;
		}
	}

	if(!re){
		jAlert("请选择邮箱","提示");
	}
}

function changeSSL(){
	if($("#isSsl").attr("checked")){
		$("#smtpPort").val(<%=SSLSmtpPort%>);
		$("#port").val(<%=SSLPopPort%>);
	}else{
		$("#smtpPort").val(<%=smtpPort%>);
		$("#port").val(<%=popPort%>);
	}
}

</script>
</html>
