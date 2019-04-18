<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.licenceValidate.*"%>
<%@ page import = "com.redmoon.oa.Config"%>
<%@ page import = "org.json.*"%>
<%@ page import = "java.io.*"%>
<%@ page import = "java.net.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<script src="inc/livevalidation_standalone.js"></script>
<link type="text/css" rel="stylesheet" href="skin/popup_box.css" />
<link type="text/css" rel="stylesheet" href="skin/common.css" />
<%
Config cg = new Config();
String yimihomeURL = cg.get("yimihome_url");

RegistInfoMgr rr = new RegistInfoMgr();
// 是否已注册
boolean getEnterpriseNo = rr.getEnterpriseNo();

int status = -1;
String reason ="";
String email = "";
String EnterpriseNo ="";
String msg = "";

boolean flag = false;
java.io.BufferedReader in = null;
URLConnection con = null;
try {
	 URL url = new URL(yimihomeURL + "/httpClientServer/check_net.jsp");
	 con = url.openConnection();

	 in = new java.io.BufferedReader(new java.io.InputStreamReader(con.getInputStream()));
	 con.setConnectTimeout(3000);
	 con.setReadTimeout(3000);
	 String s;
	 while ((s = in.readLine()) != null) {
		 if (s.length() > 0) {// 如果能够读取到页面则证明网络正常
			 flag=true;
		 	 break;
	 	}
	 }
	 in.close();
}catch (Exception ex) {
	ex.printStackTrace();
}finally{
	if(in!=null){
		in.close();
	}
}

// 如果已注册，则请求获得目前的审核状态
if(getEnterpriseNo){
	JSONObject messageObj = new JSONObject();
	messageObj = rr.getEnterpriseJsonObject();
	
	email = messageObj.getString("email");
	EnterpriseNo = messageObj.getString("enterpriseNo");
	
	if(flag){
		if(messageObj != null){
			try {
				messageObj = HttpClientLoginValidate.checkEnterpriseNoStatus(yimihomeURL + "/httpClientServer/httpclient_server_check_enterpriseno_status.jsp",messageObj);
			}catch (JSONException e){
				out.println(e.getMessage());
			}catch (IOException e){
				out.println(e.getMessage());
			}
			
			int flagStatus = messageObj.getInt("status");  // 1 通过；0 未审核； 2 没通过 ; -1 无数据
			if(flagStatus==-1){
				rr.deleteTable();
			}else{
				status = flagStatus;
				reason = messageObj.getString("reason");
			}
			msg = messageObj.getString("msg");
		}else{
			status = -2;//regist_info表读不到数据
		}
	}
}
%>
<script>
var flag = false;
var flag1 = false;
</script>
<div id="popup_box" class="popup_box" >
  <div class="popup_fram">
    <div class="popup_title">提示
       <div class="popup_close"><a id="popCloseBtn" href="#"><img src="images/popup_close-1.png" width="30" height="30" /></a></div>
    </div>
    <br>
<!--tab标签-->    

<!--激活-->     
  <div id="popup_son_tab1" class="popup_fram_login" style="display:none">
        
		 <div class="popup_fram_login_cont" id="activeDiv" >
	       <ul>
	       <li><span class="titleClass">企&nbsp;业&nbsp;号</span>
             <input id="enterprise_num"  name="enterprise_num" type="text" onpropertychange="checkEntNum()" oninput="checkEntNum()" title="企业号作为企业标识用于生成许可证，同时可用于登录一米之家论坛">
           		<span class="red1">*</span> 
           		<span id="numInfo" name="numInfo" style="color:#cc0000;font-weight:700;font-size:15px;"></span> 
           </li>
           <li><span class="titleClass">邮&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;箱</span>
             <input id="entEmail" name="entEmail" type="text" onpropertychange="checkEntEmail()" oninput="checkEntEmail()"  title="注册完成后论坛账号激活链接、审核通过后的OA许可证将分别发送到该邮箱"/>
          		<span class="red1">*</span>
           		<span id="entemailInfo" name="entemailInfo" style="color:#cc0000;font-weight:700;font-size:15px;"></span>
           </li>  
         </ul>
       </div>
       <center><span id="msgNoticeReg" name="msgNoticeReg" style="font-size:15px;color:#A9A9A9;">请输入您在一米之家网站注册时的邮箱，以及发送至该邮箱的企业号。</span></center>
       <div class="popup_bottomDiv" id="bottomRegDiv">
       <div class="popup_button" id="popup_button_activeLoginBtnDiv"><a id="activeLoginBtn" href="#">激&nbsp;活</a></div>
       <div class="popup_button_cancel" id="popup_button_loginInSysRegDiv"><a id="loginInSysReg" href="#">试&nbsp;用</a></div>
       <div class="popup_button" id="popup_button_confirmRegDiv" style="display:none"><a id="confirmRegBtn" href="#">确&nbsp;定</a></div>
       </div>
       <input id="enterpriseNumPass" type="hidden" value="false">
       <input id="enterpriseEmailPass" type="hidden" value="false">
 </div> 
    
<!--注册内容-->      
  <div id="popup_son_tab2" class="popup_fram_register" >
       <div class="popup_fram_register_cont" id="registDiv" >
         <ul>
           <li><span class="titleClass">企业全称</span>
             <input id="full_name" name="full_name" type="text" title="请填写全称,名称应以所在地开头,如：南京云网汇联软件技术有限公司"  onpropertychange="checkFullName()" oninput="checkFullName()"/> 
           		<span class="red1">*</span> 
           		<span id="fullNameInfo" class="fullNameInfo" name="fullNameInfo" style="color: #cc0000; font-weight:700;font-size:15px;"></span>
           </li>
           <li><span class="titleClass">邮&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;箱</span>
             <input id="email" name="email" type="text" onpropertychange="checkEmail()" oninput="checkEmail()"  title="注册完成后论坛账号激活链接、审核通过后的OA许可证将分别发送到该邮箱"/>
          		<span class="red1">*</span>
           		<span id="emailInfo" name="emailInfo" style="color:#cc0000;font-weight:700;font-size:15px;"></span>
           </li>
           <li><span class="titleClass">联&nbsp;&nbsp;系&nbsp;&nbsp;人</span>
             <input id="linkman" name="linkman" type="text" onpropertychange="checkName()" oninput="checkName()"/> 
           <span class="red1">*</span> 
           <span id="linkmanInfo" name="linkmanInfo" style="color:#cc0000;font-weight:700;font-size:15px;"></span>
           </li>
           <li><span class="titleClass">手&nbsp;&nbsp;机&nbsp;&nbsp;号</span>
             <input id="mobile" name="mobile" type="text" onpropertychange="checkMobile()" oninput="checkMobile()" /> 
           		<span class="red1">*</span>
           		<span id="mobileInfo" name="mobileInfo" style="color:#cc0000;font-weight:700;font-size:15px;"></span>
           </li>
         </ul>
       </div>
       		<div id="protocolDiv"  class="protocolClass"><input id="protocol" name="protocol" type="checkbox" checked="checked" onclick="protocolChange(this);"/>&nbsp;&nbsp;
       		   <a href="public/protocol/yimi_protocol.html" target="_blank" style="text-decoration:underline;">我已阅读并同意《一米OA注册协议》</a>
               <span class="red1">*</span>
           	   <span id="protocolInfo" name="protocolInfo" style="color:#cc0000;font-weight:700;"></span>
        	</div>
       <center><span id="msgNotice" name="msgNotice" style="font-size:15px;"></span></center>
       <center>
       <div class="popup_bottomDiv" id="bottomDiv">
       <div class="popup_button" id="popup_button_registDiv"><a id="popRegistBtn" href="#">注&nbsp;册</a></div>
       <div class="popup_button_active" id="popup_button_activeDiv"><a id="activeBtn" href="#">激&nbsp;活</a></div>
       <div class="popup_button_cancel" id="popup_button_cancelDiv"><a id="loginInSys" href="#">试&nbsp;用</a></div>
       </div>
       </center>
       <input id="fullNamePass" type="hidden" value="false">
       <input id="emailPass" type="hidden" value="false">
       <input id="linkManPass" type="hidden" value="false">
       <input id="mobilePass" type="hidden" value="false">
 </div>
  </div>
</div>
<script type="text/javascript">  
    function ShowTab1() {
        document.getElementById("popup_tab1").className = "popup_fram_tab_sel"; 
        document.getElementById("popup_tab2").className = "";     
        document.getElementById("popup_son_tab1").style.display = "";  
        document.getElementById("popup_son_tab2").style.display = "none";  

    }  

    function ShowTab2() {  
        document.getElementById("popup_tab1").className = "";  
        document.getElementById("popup_tab2").className = "popup_fram_tab_sel";  
        document.getElementById("popup_son_tab1").style.display = "none";  
        document.getElementById("popup_son_tab2").style.display = ""; 
    }  
    
	//检测企业号
    function checkEntNum() {
		var entNum =  $("#enterprise_num").val();
		$('#numInfo').html("");

        $.ajax({
    		type: "post",
    		url: "public/popup_box_validate.jsp",
    		data : "op=checkEntNo&enterpriseNo=" + entNum,
    		success: function(data, status){
    			if(data.trim()==1){
    				o("enterpriseNumPass").value ="true";
        		    $("#numInfo").html("企业号通过审核");
    			  	return true;
    			} else {
        			if (data.trim()==-1){
            		    $("#numInfo").html("企业号检测出错");
        			}else if (data.trim()==0){
            		    $("#numInfo").html("企业号不存在");
        			}else if (data.trim()==2){
            		    $("#numInfo").html("企业号未审核");
        			}else if (data.trim()==3){
            		    $("#numInfo").html("企业号未通过审核");
            		}
    				o("enterpriseNumPass").value ="false";
    				return false;
    			}
    		},
    		error: function(XMLHttpRequest, textStatus){
    			// 请求出错处理
    			flag = false
    			alert(XMLHttpRequest.responseText);
    		}
    	  });
    }   
        
    //检测邮箱号（激活）
    function checkEntEmail(){
		var entNum =  $("#enterprise_num").val();
		var email =  $("#entEmail").val();
   	  	$('#entemailInfo').html("");

    	$.ajax({
    			type: "post",
    			url: "public/popup_box_validate.jsp",
    			data : "op=checkEntEmail&email="  + email + "&enterpriseNo=" + entNum,
    			success: function(data, status){
			
    				if(data.trim()==""){
            		    $("#entemailInfo").html("邮箱与企业号匹配");
    				  	o("enterpriseEmailPass").value ="true";
    				  	return true;
    				}else {	
            		    $("#entemailInfo").html("邮箱与企业号不匹配");
    					o("enterpriseEmailPass").value ="false";
    					return false;
    					}
    				},
    				error: function(XMLHttpRequest, textStatus){
    					// 请求出错处理
    					o("emailPass").value ="false";
    					return false;
    					alert(XMLHttpRequest.responseText);
    				}
    		   });

    }

	//检测企业全称（注册）
    function checkFullName(){
  	  var full_name = $("#full_name").val().replace(/\s+/g,"");
  	  if($.trim(full_name).length>=8){
  	  	$('#fullNameInfo').html("");
	  	o("fullNamePass").value ="true";
  	  	return true;
  	  }else{
  	    $('#fullNameInfo').html("必须大于等于8位");
	  	o("fullNamePass").value ="false";
  	    return false;
  	  }
  	}

	//检测邮箱（注册）
    function checkEmail(){
		var email =  $("#email").val();
   	  	$('#emailInfo').html("");

        if(email.length==0)
        {
      	   $('#emailInfo').html("邮箱不能为空");
		   $('#email').focus();
		   o("emailPass").value ="false";
           return false;
        } 

		//邮箱格式验证
    	var szReg=/^([\.a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(\.[a-zA-Z0-9_-])+/; 
    	if(!szReg.test(email)){
       	   $('#emailInfo').html("邮箱格式错误");
		   $('#email').focus();
		   o("emailPass").value ="false";
           return false;
        }
    	  
    	$.ajax({
    			type: "post",
    			url: "public/popup_box_validate.jsp",
    			data : "op=checkEmail&email=" + email,
    			success: function(data, status){
    				if(data.trim()==""){
        				$("#emailInfo").html("");
    				  	o("emailPass").value ="true";
    	    		}else {	
        				$("#emailInfo").html("邮箱已注册过");
    					o("emailPass").value ="false";
    					}
    				},
    				error: function(XMLHttpRequest, textStatus){
    					// 请求出错处理
    					o("emailPass").value ="false";
    					return false;
    					alert(XMLHttpRequest.responseText);
    				}
    		   });

    }

    function checkName(){
    	var linkman = $("#linkman").val().replace(/\s+/g,"");
    	if($.trim(linkman).length==0){
    	  	$('#linkmanInfo').html("姓名不能为空");
  	  		o("linkManPass").value ="false";
    	  	return false;
    	}else{
      	  	$('#linkmanInfo').html("");  
    	  	o("linkManPass").value ="true";
    	    return true;
    	}
   	}

    
    //先验证手机格式（注册），再验证是否注册
    function checkMobile(){
		var mobile =  $("#mobile").val();
   	  	$('#mobileInfo').html("");
   	  	
        if(mobile.length==0)
        {
      	   $('#mobileInfo').html("请输入11位手机号码");
		   $('#mobile').focus();
		   o("mobilePass").value ="false";
           return false;
        }    
        if(mobile.length!=11)
        {
       	   $('#mobileInfo').html("请输入11位手机号码");
		   $('#mobile').focus();
		   o("mobilePass").value ="false";
           return false;
           
        }
        
        var myreg = /^(13|15|18|17)[0-9]{9}$/;
        if(!myreg.test(mobile))
        {
       	   $('#mobileInfo').html("请输入有效的手机号码");
		   $('#mobile').focus();
		   o("mobilePass").value ="false";
           return false;
        }
          
  	    $.ajax({
  			type: "post",
  			url: "public/popup_box_validate.jsp",
  			data : "op=checkMobile&mobile=" + mobile,
  			success: function(data, status){
  				$("#mobileInfo").html(data.trim());

  				if(data.trim()==""){
  				  	o("mobilePass").value ="true";
  				}else {	
  				  	o("mobilePass").value ="false";
  					}
  				},
  				error: function(XMLHttpRequest, textStatus){
  					// 请求出错处理
  				  	o("mobilePass").value ="false";
  				}
  		   });
  	}

    //试用系统
   	function loginInSys() {
		$('#popup_box').hide();
		return false;
    }

   	function showRegDiv() {
      	$('#msgNotice').html("");
		$('#registDiv').show();
      	$('#protocolDiv').show();
		$('#popRegistBtn').click(popRegistBtnBundling);
    }
    
	//注册按钮	
	function popRegistBtnBundling() {
		
		if($("#protocol").attr("checked")!="checked"){
		  	$('#protocolInfo').html("请阅读并同意协议");
			return false;
		}
		
		var fullName =  $("#full_name").val();
		if(fullName.length==0){
		  	$('#fullNameInfo').html("企业全称不能为空");
		  	$('#full_name').focus();
		}
		if ($('#fullNamePass').val()=="false") {
			$('#full_name').focus();
			return false;
		}

		var email =  $("#email").val();
		if(email.length==0){
		  	$('#emailInfo').html("邮箱不能为空");
		  	$('#email').focus();
		}
		if ($('#emailPass').val()=="false") {
			$('#email').focus();
			return false;
		}

		var linkman =  $("#linkman").val();
		if(linkman.length==0){
		  	$('#linkmanInfo').html("联系人不能为空");
		  	$('#linkman').focus();
		}

		if ($('#linkManPass').val()=="false") {
			$('#linkman').focus();
			return false;
		}
		
		var mobile =  $("#mobile").val();
		if(mobile.length==0){
		  	$('#mobileInfo').html("手机不能为空");
		  	$('#mobile').focus();
		}
		if ($('#mobilePass').val()=="false") {
			$('#mobile').focus();
			return false;
		}
		
		regist();
	}
	
	//激活 在一米网站注册的用户
	function activeBtnBundling() {
		$('#popup_son_tab2').hide();
		$('#popup_son_tab1').show();
	    $('#loginInSysReg').click(loginInSys);
		$('#activeLoginBtn').click(activeBtn);
    }

	//激活 通过审核
	function activeBtn () {

		var enterprise_num =  $("#enterprise_num").val();
		if(enterprise_num.length==0){
		  	$('#numInfo').html("企业号不能为空");
		  	$('#enterprise_num').focus();
		}
		
		if ($('#enterpriseNumPass').val()=="false") {
			$('#enterprise_num').focus();
			return false;
		}		
		
		var entEmail =  $("#entEmail").val();
		if(entEmail.length==0){
		  	$('#entemailInfo').html("邮箱不能为空");
		  	$('#entEmail').focus();
		}
		if ($('#enterpriseEmailPass').val()=="false") {
			$('#entEmail').focus();
			return false;
		}
		

		enterpiseLogin();
    }
    
	//激活提交
	function enterpiseLogin() {
		$.ajax({
			type: "post",
			url: "public/popup_box_validate.jsp",
			data: {
				op:"enterpiseLogin",
				entNo: $('#enterprise_num').val(),
				entEmail: $('#entEmail').val()
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
			},
			success: function(data, status){
				var json = $.parseJSON(data);
				if(json.ret=="0"){
			      	$('#activeDiv').html("很抱歉，企业号激活失败，请联系“一米之家”客服，或致电：400-619-0600。");
					$('#msgNoticeReg').hide();
			      	$('#popup_button_activeLoginBtnDiv').hide();
			      	setTimeout("$('#popup_box').hide()",10000);
				} else{
					//alert(data.trim());
					$('#msgNoticeReg').hide();
			      	// $('#activeDiv').html("!<br>请点击“确定”按钮，等待系统重启后开始您的“****”之旅。");
			      	$('#activeDiv').html(json.msg);
				    $('#popup_button_activeLoginBtnDiv').hide();
			      	$('#popup_button_loginInSysRegDiv').hide();
			      	$('#popup_button_confirmRegDiv').show();
					$('#confirmRegBtn').click(restartTomcat);
					$('#popCloseBtn').click(restartTomcat);
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

	function regist() {
		$.ajax({
			type: "post",
			url: "public/popup_box_validate.jsp",
			data: {
				op:"regist",
				entName: $('#full_name').val(),
				entEmail: $('#email').val(),
				linkMan: $('#linkman').val(),
				mobile: $('#mobile').val()
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
		      	$('#bottomDiv').hide();
		      	
			},
			success: function(data, status){
				//var dataJson = JSON.parse(data);
				
				if(data.trim()==""){
			      	$('#protocolDiv').hide();
			      	$('#msgNotice').html("");
			      	$('#registDiv').html("注册失败，请联系客服人员。");
			      	setTimeout("$('#popup_box').hide()",3000);
				} else{
			      	$('#protocolDiv').hide();
					$('#bottomDiv').hide();
			      	$('#msgNotice').html("");
			      	$('#registDiv').html("注册成功，请登录系统。");
			      	setTimeout("$('#popup_box').hide()",3000);
				}
					
			},
			complete: function(XMLHttpRequest, status){
		
			},
			error: function(XMLHttpRequest, textStatus){
				alert(XMLHttpRequest.responseText);
			}
		});	
	}

	function restartTomcat() {
		$('#popup_box').hide();
		// window.location.href = "public/restart_tomcat.jsp";
	}

	function protocolChange(obj) {
		if($("#protocol").attr("checked")=="checked"){
		  	$('#protocolInfo').html("");
		}
	}
	
    $(document).ready(function(){
        var getEnterpriseNo = <%=getEnterpriseNo%>;
       	var status = <%=status%>;
		var reason = "<%=reason%>";
		if(reason==""){
			reason="<br>";
		}
		var EnterpriseNo = "<%=EnterpriseNo%>";
		var entEmail = "<%=email%>";
		var flag = <%=flag%>;
		var msg = "<%=msg%>";
		
		if(getEnterpriseNo==true){
			$('#popup_son_tab1').hide();
			$('#popup_son_tab2').show();
			
			if(status==-1){
		      	$('#msgNotice').html("");
				$('#bottomDiv').hide();
		      	$('#registDiv').html(msg); // 查无资料"<font color='red'>网络联接错误，您需要联机激活系统才能永久使用！</font>");
		      	$('#protocolDiv').hide();
		      	setTimeout("$('#popup_box').hide()",3000);
			}else if(status==-2){
		      	$('#msgNotice').html("");
				$('#bottomDiv').hide();
		      	$('#registDiv').html("<font color='red'>注册反馈信息错误，请继续试用系统。</font>");
		      	$('#protocolDiv').hide();
		      	setTimeout("$('#popup_box').hide()",3000);
			}else if(status==0){
		      	$('#msgNotice').html("");
		        $('#registDiv').html(msg); // "您的注册信息正在审核中，请耐心等待。");
		      	$('#protocolDiv').hide();
				$('#popup_button_registDiv').hide();
				$('#popup_button_activeDiv').hide();
			    $('#loginInSys').click(loginInSys);
			}else if(status==1){
				$('#popup_son_tab2').hide();
				$('#popup_son_tab1').show();
				o("enterprise_num").value =EnterpriseNo;
				o("entEmail").value =entEmail;
				$("#enterprise_num").attr("readonly",true);
				$("#entEmail").attr("readonly",true);
				$("#loginName").blur();
				$("#loginPwd").blur();
			    $('#msgNoticeReg').html(msg); // "您的注册已审核通过！请点击“激活”按钮，等待系统重启后，开始您的“****”之旅。");
				$('#popup_button_loginInSysRegDiv').hide();
				$('#activeLoginBtn').click(activeBtn);
			}else if(status==2){
		      	$('#msgNotice').html(msg); // "很抱歉，您的注册因为以下原因未通过审核，请重新注册。"+ "<br>" + reason);		
			    $('#registDiv').hide();	
		      	$('#protocolDiv').hide();
			    $('#popup_button_activeDiv').hide();
			    $('#popRegistBtn').click(showRegDiv);
			    $('#loginInSys').click(loginInSys);
			}
		}else if(getEnterpriseNo==false){
			if(flag==true){
				$('#popup_son_tab1').hide();
				$('#popup_son_tab2').show();
		      	$('#msgNotice').html("<font color='#A9A9A9'>如果已在“一米之家”注册过企业号，请点击“激活”按钮。</font>");
				$('#popRegistBtn').click(popRegistBtnBundling);
				$('#activeBtn').click(activeBtnBundling);
			    $('#loginInSys').click(loginInSys);
			}else if(flag==false){
				$('#popup_son_tab1').hide();
				$('#popup_son_tab2').show();
		      	$('#msgNotice').html("");
				$('#bottomDiv').hide();
		      	$('#registDiv').html("<font color='red'>网络联接错误，您需要联机激活系统才能永久使用！</font>");
		      	$('#protocolDiv').hide();
		      	setTimeout("$('#popup_box').hide()",3000);
			}

		    /*
	    	o("mobile").value ="15666666666";
	    	o("email").value ="156666@163.com";
	    	o("full_name").value ="核问题erHJ23何物22";
	    	o("linkman").value ="哈j阿福2";*/
		}

		//关闭按钮
		$('#popCloseBtn').click(loginInSys);

		$("#loginName").blur();
		$("#loginPwd").blur();
    });
</script>  