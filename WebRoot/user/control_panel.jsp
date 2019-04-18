<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.ui.menu.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.forum.ui.FileViewer"%>
<%@ page import="org.json.*"%>
<%@ page import="com.redmoon.oa.netdisk.UtilTools"%>
<%@ page import="com.redmoon.oa.video.VideoMgr"%>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String username = privilege.getUser(request);
if (!SecurityUtil.isValidSqlParam(username)) {
	out.print(StrUtil.Alert("参数非法！"));
	return;
}
UserMgr um = new UserMgr();
UserDb user = um.getUserDb(username);
if (user==null || !user.isLoaded()) {
	out.print(StrUtil.Alert_Back("该用户已不存在！"));
	return;
}

UserSetupDb usd = new UserSetupDb();
usd = usd.getUserSetupDb(username);

String op = ParamUtil.get(request, "op");
 
if (op.equals("edit")) {
	String oldskincode = UserSet.getSkin(request);
	int oldUiMode = usd.getUiMode();
	UserSetupMgr usm = new UserSetupMgr();
	usm.modify(request, response);
	
	String skinCode = ParamUtil.get(request, "skinCode");
	if (!skinCode.equals(oldskincode)) {
		%>
		<script>
		window.top.location.reload();
		</script>
		<%
		return;
	}
	int uiMode = ParamUtil.getInt(request, "uiMode", UserSetupDb.UI_MODE_NONE);
	int menuMode = ParamUtil.getInt(request, "menuMode", UserSetupDb.MENU_MODE_NEW);
	if (uiMode!=UserSetupDb.UI_MODE_NONE) { // && uiMode!=oldUiMode) {
		if (uiMode==UserSetupDb.UI_MODE_PROFESSION) {
			%>
			<script>
			<%if (menuMode==UserSetupDb.MENU_MODE_NEW) {%>
			window.top.location.href = "<%=request.getContextPath()%>/oa.jsp";
			<%}else{%>
			window.top.location.href = "<%=request.getContextPath()%>/oa_main.jsp";			
			<%}%>
			</script>
			<%
			return;
		}
		else if (uiMode==UserSetupDb.UI_MODE_FLOWERINESS) {
			%>
			<script>
			window.top.location.href = "<%=request.getContextPath()%>/mydesktop.jsp";
			</script>
			<%
			return;
		}
		else if (uiMode==UserSetupDb.UI_MODE_LTE) {
			%>
			<script>
			window.top.location.href = "<%=request.getContextPath()%>/lte/index.jsp";
			</script>
			<%
			return;
		}		
		else {
			%>
			<script>
			window.top.location.href = "<%=request.getContextPath()%>/main.jsp";
			</script>
			<%
			return;
		}
	}
	out.print(StrUtil.Alert_Redirect("操作成功!", "control_panel.jsp"));
	return;
}
else if (op.equals("checkPwd")) {
	com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
	int minLen = scfg.getIntProperty("password.minLen");
	int maxLen = scfg.getIntProperty("password.maxLen");
	int strenth = scfg.getIntProperty("password.strenth");
		
	String pwd = ParamUtil.get(request, "pwd");
	PasswordUtil pu = new PasswordUtil();
	JSONObject json = new JSONObject();

	int ret = pu.check(pwd, minLen, maxLen, strenth);
	json.put("ret", ret);
	json.put("msg", pu.getResultDesc(request));
	
	out.print(json);
	return;
}
else if (op.equals("savepwd")) {
	boolean isSuccess = false;
	String pwdOld = user.getPwdRaw();
	String pwd3 = ParamUtil.get(request, "Password3");
	String pwd = ParamUtil.get(request, "Password");
	String pwd2 = ParamUtil.get(request, "Password2");
	JSONObject json = new JSONObject();
	
	if(!pwdOld.equals(pwd3)){
		json.put("ret", -1);
		json.put("msg", "您输入的旧密码有误，请重新输入！");	
		out.print(json.toString());
		return;
	}
	if(!pwd2.equals(pwd)){
		json.put("ret", -1);
		json.put("msg", "您输入的两次密码不匹配，请重新输入！");	
		out.print(json.toString());
		return;
	}
	if(pwd.length()<1 || pwd.length()>20){
		json.put("ret", -1);
		json.put("msg", "密码长度必须在1~20之间，请重新输入！");
		out.print(json.toString());	
		return;
	}
	try {
		isSuccess = um.modifyPwd(request, ParamUtil.get(request, "name"), ParamUtil.get(request, "Password"));
	}
	catch (ErrMsgException e) {
		out.println(fchar.Alert_Back(e.getMessage()));
	}
	if (isSuccess) {
		VideoMgr vmgr = new VideoMgr();                        
		if(vmgr.validate()){                             //校验成功后，同步添加视频会议用户
			String name = ParamUtil.get(request, "name");
			String password = ParamUtil.get(request, "Password");
			String returnString = vmgr.modifyUserPassword(name,password);
			boolean isFinish = vmgr.getResultByParseXML(returnString);
			if (isFinish)
				LogUtil.getLog("同步视频会议用户:").info("修改用户密码成功。");
			else 
				LogUtil.getLog("同步视频会议用户:").info("修改用户密码失败。");
		}
		json.put("ret", 1);
		json.put("msg", "修改成功！");
		out.print(json.toString());		
	}
	return ;
} else if (op.equals("saveLanguage")) {
	boolean isSuccess = false;
	
	JSONObject json = new JSONObject();
	String local = ParamUtil.get(request,"local");
	String userName = ParamUtil.get(request,"name");
	
	//UserSetupDb userSetupDb = new UserSetupDb();
	//userSetupDb = userSetupDb.getUserSetupDb(userName);
	usd.setLocal(local);
	isSuccess = usd.save();
	
	if (isSuccess) {
		json.put("ret", 1);
		json.put("msg", "修改成功！");
		out.print(json.toString());		
	}
	return ;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=8">
<title>控制面板</title>

<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script src="../js/jquery-ui/jquery-ui.js"></script>
<script src="../js/jquery.bgiframe.js"></script>
<script src="../js/jquery.form.js"></script>
<script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>
<link href="<%=SkinMgr.getSkinPath(request)%>/main.css" rel="stylesheet" type="text/css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />
<script type="text/javascript">
function New(para_URL) {
	var URL=new String(para_URL);
	window.open(URL,'','resizable,scrollbars');
}
function CheckRegName() {
	var Name=document.form.RegName.value;
	window.open("checkregname.jsp?RegName="+Name,"","width=200,height=20");
}

function check_checkbox(myitem,myvalue) {
	var checkboxs = document.all.item(myitem); 
	var myary = myvalue.split("|"); 
    if(checkboxs!=null) {
		for(i=0; i<checkboxs.length; i++) {
			if(checkboxs[i].type=="checkbox" ) {
				for(k=0; k<myary.length; k++) {
					if(checkboxs[i].value==myary[k])checkboxs[i].checked = true;
				}
            }
        }
	}
}

function showQuickMenu()
{
try
{
	top.mainFrame.show();
	
}
catch(e)
{
	jAlert(e,"提示");
}
}

function checkPwd(pwdNew) {
	$.ajax({
		type: "post",
		url: "control_panel.jsp",
		data: {
			op: "checkPwd",
			pwd: pwdNew
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
		},
		success: function(data, status){
			data = $.parseJSON(data);
			$("#showmsg").text(data.msg);
		},
		complete: function(XMLHttpRequest, status){
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			jAlert(XMLHttpRequest.responseText,"提示");
		}
	});	
}
</script>
<style>
	.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
	-moz-opacity: 0.20;
	opacity: 0.20;
	z-index: 1500;
	}
	.treeBackground {
	display: none;
	position: absolute;
	top: -2%;
	left: 0%;
	width: 100%;
	margin: auto;
	height: 200%;
	background-color: #EEEEEE;
	z-index: 1800;
	-moz-opacity: 0.8;
	opacity: .80;
	filter: alpha(opacity = 80);
	}
</style>
</head>
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<div style='overflow:hidden;'>
<!--控制面板-->
<div class="control_layout_disk">
    <span><img src="<%=SkinMgr.getSkinPath(request)%>/icons/control_disk.png" width="16" height="16" /> 磁盘空间<%=UtilTools.getFileSize(user.getDiskSpaceAllowed())%></span>
    <span>已用<span class="disk_font"><%=UtilTools.getFileSize(user.getDiskSpaceUsed())%></span></span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <span><img src="<%=SkinMgr.getSkinPath(request)%>/icons/control_mailbox.png" width="16" height="16" /> 内部邮箱空间 <%=UtilTools.getFileSize(usd.getMsgSpaceAllowed())%></span>
    <span>已用<span class="disk_font"><%=UtilTools.getFileSize(usd.getMsgSpaceUsed())%></span></span>
</div>
<div class="control_layout" >
<!--1_个人信息--><div class="control_modular" onMouseOver="addClasses(this)" onMouseOut="removeClasses(this)" onClick="addTab('个人信息', 'user/user_edit.jsp')">
                   <div class="icon_box"><img src="<%=SkinMgr.getSkinPath(request)%>/icons/control_1.png" width="49" height="47" /></div>
                   <div class="font_box">
                      <div class="title"><a>个人信息</a></div>
                      <div class="subtitle">查看、修改个人信息</div>
                   </div>
                </div>
<!--2_我的权限--><div class="control_modular" onMouseOver="addClasses(this)" onMouseOut="removeClasses(this)" onClick="addTab('我的权限', 'user/control_panel_user_priv.jsp')">
                   <div class="icon_box"><img src="<%=SkinMgr.getSkinPath(request)%>/icons/control_2.png" width="49" height="47" /></div>
                   <div class="font_box">
                      <div class="title"><a>我的权限</a></div>
                      <div class="subtitle">查看个人权限</div>
                   </div>
                </div>
<!--3_修改密码--><div class="control_modular" onMouseOver="addClasses(this)" onMouseOut="removeClasses(this)" onClick="mypasswd();">
                   <div class="icon_box"><img src="<%=SkinMgr.getSkinPath(request)%>/icons/control_3.png" width="49" height="47" /></div>
                   <div class="font_box">
                      <div class="title"><a>修改密码</a></div>
                      <div class="subtitle">修改个人密码</div>
                      <div id="mypasswd" style="display:none">
                      	<form id="form4" action="control_panel.jsp?op=savepwd" method="post" name="memberform">
                      		<div style="display:none">
								<input type="text" name="name" id="name" value=<%=username %>>
							</div>
							<div class="dialog_margin">
								<span>输入旧密码&nbsp;&nbsp;</span>
								<input type=password id="Password3" name="Password3" size=20 >
							</div>
                      		<div class="dialog_margin">
								<span>输入新密码&nbsp;&nbsp;</span>
								<input type=password id="Password" name="Password" size=20 onKeyUp="checkPwd(this.value)" ><br/>
								<span id="showmsg" style="color:red"></span>
							</div>
							<div>
								<span>确认新密码&nbsp;&nbsp;</span>
								<input type=password id="Password2" name="Password2" size=20 />
							</div>
                      	</form>
                      </div>
                   </div>
                </div>
<%
	 String opt = "";
	 com.redmoon.oa.ui.SkinMgr sm = new com.redmoon.oa.ui.SkinMgr();
	 Iterator irskin = sm.getAllSkin().iterator();
	 String defaultSkinCode = "";
	 while (irskin.hasNext()) {
	 	com.redmoon.oa.ui.Skin sk = (com.redmoon.oa.ui.Skin)irskin.next();
		String d = "";
		if (sk.isDefaultSkin()) {
			d = "selected";
			defaultSkinCode = sk.getCode();
		}
		opt += "<option value=" + sk.getCode() + " " + d + ">" + sk.getName() + "</option>";
	 }
	 
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		boolean isSpecified = cfg.get("styleMode").equals("specified") || cfg.get("styleMode").equals("2");
		boolean isIntegrateEmail = cfg.get("isIntegrateEmail").equals("true");
		String dispalyStyleMode = "";
		int uiMode = 0;
		if (isSpecified) {
			dispalyStyleMode = "display:none";
			uiMode = StrUtil.toInt(cfg.get("styleSpecified"), -1);
		}
		else {
			uiMode = usd.getUiMode();
		}
		
		 String skinCode = usd.getSkinCode();
		 if (skinCode.equals("")){
		  	skinCode = defaultSkinCode;
		 }
%>
<!--4_我的门户--><div class="control_modular" onMouseOver="addClasses(this)" onMouseOut="removeClasses(this)" onClick="addTab('我的门户', 'user/portal.jsp')">
                   <div class="icon_box"><img src="<%=SkinMgr.getSkinPath(request)%>/icons/control_8.png" width="49" height="47" /></div>
                   <div class="font_box">
                      <div class="title"><a>我的门户</a></div>
                      <div class="subtitle">进入我的门户</div>
                   </div>
                </div>
<!--11_界面设置--><%
					String displayModular = "display:none";
					com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();    
					if(true || license.isPlatformSrc()) {
						displayModular = dispalyStyleMode;
					}
				%>
				<div style="<%=displayModular%>" class="control_modular" onMouseOver="addClasses(this)" onMouseOut="removeClasses(this)" onClick="topic();">
                 <div class="icon_box"><img src="<%=SkinMgr.getSkinPath(request)%>/icons/control_4.png" width="49" height="47" /></div>
                 <div class="font_box">
                    <div class="title"><a >界面设置</a></div>
                    <div class="subtitle">更换主题</div>
                    <div id="topic" style="display:none">
                   	<form name="form2" id="form2" action="control_panel.jsp?op=edit" method="post">
                   	<div class="dialog_margin" style="display:none">
					     界面皮肤&nbsp;&nbsp;&nbsp;
					  <select name="skinCode" id="skinCode1">
						  <%=opt%>
					  </select>
					  <script>
						  $('#skinCode1').val("<%=skinCode%>");
					  </script>
					 </div>
					 <div style="<%=dispalyStyleMode%>">
					      界面模式&nbsp;&nbsp;&nbsp;
					      <select id="uiMode" name="uiMode">
					      <option value="0">请选择</option>
					      <option value="<%=UserSetupDb.UI_MODE_PROFESSION%>">经典型</option>
					      <option value="<%=UserSetupDb.UI_MODE_FASHION%>">时尚型</option>
						  <option value="<%=UserSetupDb.UI_MODE_FLOWERINESS%>">绚丽型</option>
						  <option value="<%=UserSetupDb.UI_MODE_LTE%>">轻简型</option>						  
						  </select>
						  <br />
						  菜单模式&nbsp;&nbsp;&nbsp;
						  <select id="menuMode" name="menuMode">
						  <option value="<%=UserSetupDb.MENU_MODE_NEW %>">简洁</option>
						  <option value="<%=UserSetupDb.MENU_MODE_NORMAL %>">传统</option>
						  </select>&nbsp;&nbsp;(仅用于经典型界面模式)
						  <script>
						  $("#uiMode").val("<%=usd.getUiMode()%>");
						  $("#menuMode").val("<%=usd.getMenuMode()%>");
						  </script>
					  </div>
					  <div style="display:none">
					   	<input type="text" name="isMsgWinPopup" value = "<%=usd.isMsgWinPopup()?1:0%>"></input>
						<input type="text" name="isMessageSoundPlay" value = "<%=usd.isMessageSoundPlay()?1:0%>"></input>
						<input type="text" name="isChatIconShow" value = "<%=usd.isChatIconShow()?1:0%>"></input>
						<input type="text" name="isChatSoundPlay" value = "<%=usd.isChatSoundPlay()?1:0%>"></input>
						<input type="text" name="isWebedit" value = "<%=usd.isWebedit()?1:0%>"></input>
						<input type="text" name="is_show_sidebar" id="is_show_sidebar3" value="<%=usd.isShowSidebar()?1:0%>"></input>
						<input type="text" name="emailName" value="<%=usd.getEmailName()%>"/>
						<input type="text" name="emailPwd"  value="<%=usd.getEmailPwd()%>"/>
					  </div>
			          <div style="display:none">
				        <input class="btn" id="topicbtn" type="submit" name="Submit" value=" 确 定 "/>
                      </div>
                    </form>
                    </div>
                 </div>
              </div>
<!--5_个性设置--><div class="control_modular" onMouseOver="addClasses(this)" onMouseOut="removeClasses(this)" onClick="individuality();">
                   <div class="icon_box"><img src="<%=SkinMgr.getSkinPath(request)%>/icons/control_5.png" width="49" height="47" /></div>
                   <div class="font_box">
                      <div class="title"><a>个性设置</a></div>
                      <div class="subtitle">个性化的设置</div>
                      <div id="individuality" style="display:none">
                      <form name="form1" id="form1" action="control_panel.jsp?op=edit" method="post">
                      	<div class="dialog_margin">
					      消息到来时是否弹出窗口&nbsp;
					      <select name="isMsgWinPopup" id="isMsgWinPopup">
					        <option value="1" selected="selected">是</option>
					        <option value="0">否</option>
					      </select>
					  	  <script>
								$('#form1 #isMsgWinPopup').val("<%=usd.isMsgWinPopup()?1:0%>");
								$('#form2 #isMsgWinPopup').val("<%=usd.isMsgWinPopup()?1:0%>");
						  </script>
						 </div>
						 <div class="dialog_margin">
						    消息到来时是否声音提示&nbsp;
						    <select name="isMessageSoundPlay" id="isMessageSoundPlay">
						       <option value="1" selected="selected">是</option>
						       <option value="0">否</option>
						    </select>
						    <script>
						        $('#form1 #isMessageSoundPlay').val("<%=usd.isMessageSoundPlay()?1:0%>");
						        $('#form2 #isMessageSoundPlay').val("<%=usd.isMessageSoundPlay()?1:0%>");
						    </script>
						  </div>
						  <div class="dialog_margin">
						     讨论信息到来时是否闪动图标&nbsp;
						     <select name="isChatIconShow" id="isChatIconShow">
						        <option value="1" selected="selected">是</option>
						        <option value="0">否</option>
						      </select>
						      <script>
								$('#form1 #isChatIconShow').val("<%=usd.isChatIconShow()?1:0%>");
								$('#form2 #isChatIconShow').val("<%=usd.isChatIconShow()?1:0%>");
							</script>
						   </div>
						   <div class="dialog_margin">
						      <td align="left">讨论信息到来时是否声音提示&nbsp;</td>
						      <td><select name="isChatSoundPlay" id="isChatSoundPlay">
						        <option value="1" selected="selected">是</option>
						        <option value="0">否</option>
						      </select>
							  <script>
								$('#form1 #isChatSoundPlay').val("<%=usd.isChatSoundPlay()?1:0%>");
								$('#form2 #isChatSoundPlay').val("<%=usd.isChatSoundPlay()?1:0%>");
							  </script>
						    </div>
						    <div class="dialog_margin" style="display:none">
						       采用高级发布方式&nbsp;
						      <td><select name="isWebedit" id="isWebedit" title="在文件柜及网盘中采用高级发布方式(使用WebEdit控件)">
						        <option value="1">是</option>
						        <option value="0" selected="selected">否</option>
						      </select>
						        <script>
									$('#form1 #isWebedit').val("<%=usd.isWebedit()?1:0%>");
									$('#form2 #isWebedit').val("<%=usd.isWebedit()?1:0%>");
								</script>
						    </div>
								<%
							    String displaySlideBar = "";
							    if (uiMode!=UserSetupDb.UI_MODE_FASHION) {
							      displaySlideBar = "display:none";
							    }
							    %>
							 <div style="<%=displaySlideBar%>">
							     右侧边栏&nbsp;
							      <select id="is_show_sidebar" name="is_show_sidebar">
							      <option value="1">显示边栏</option>
							      <option value="0">隐藏边栏</option>
							      </select>
								  <script>
								  $("#is_show_sidebar").val("<%=usd.isShowSidebar()?1:0%>");
								  $("#is_show_sidebar2").val("<%=usd.isShowSidebar()?1:0%>");
								  </script>  
							  </div>
							  <div style="display:none">
								  <input type="text" name="skinCode" id="skinCode" value = "<%=skinCode%>"/>
								  <input type="text" name="uiMode" id="uiMode1" value="<%=usd.getUiMode()%>"/>
								  <input type="text" name="emailName" id="emailName" value="<%=usd.getEmailName()%>"/>
								  <input type="text" name="emailPwd" id="emailPwd" value="<%=usd.getEmailPwd()%>"/>
							  </div>
                      	</form>
                      </div>
                   </div>
                </div>
<!--6_语言设置--><div class="control_modular" onMouseOver="addClasses(this)" onMouseOut="removeClasses(this)"  onClick="changeLanguage();">
                   <div class="icon_box"><img src="<%=SkinMgr.getSkinPath(request)%>/icons/control_langue.png" width="49" height="47" /></div>
                   <div class="font_box">
                      <div class="title"><a>语言设置</a></div>
                      <div class="subtitle">修改语言设置</div>
                      <div id="myLanguage" style="display:none">
                      	<form id="form5" action="control_panel.jsp?op=saveLanguage" method="post" name="form5">
                      		<div style="display:none">
								<input type="text" name="name" id="name" value=<%=username %>/>
							</div>
							语言&nbsp;&nbsp;&nbsp;<select name="local" id="local" style="width:80px;">
								<option value="zh-CN" selected>中文</option>
								<option value="en-US">英文</option>
							</select>
							<script>
								o("local").value="<%=usd.getLocal()%>";
							</script>
                      	</form>
                      </div>
                   </div>
                </div> 
                
<!--7_论坛中心--><div class="control_modular" onMouseOver="addClasses(this)" onMouseOut="removeClasses(this)" onClick="jump()" >
                   <div class="icon_box"><img src="<%=SkinMgr.getSkinPath(request)%>/icons/control_9.png" width="49" height="47" /></div>
                   <div class="font_box">
                      <div class="title"><a>论坛中心</a></div>
                      <div class="subtitle">进入论坛用户中心</div>
                   </div>
                </div>
<!--9_快速入口--><%
					String displayQuickEnter = "display:none";
					String displaySlideMenu = "";
					if (uiMode == UserSetupDb.UI_MODE_PROFESSION || uiMode == UserSetupDb.UI_MODE_NONE) {
					 // displayQuickEnter = "";
					  displaySlideMenu = "display:none";
					}
				%>
				<div style="<%=displayQuickEnter%>" class="control_modular" onMouseOver="addClasses(this)" onMouseOut="removeClasses(this)" onClick="showQuickMenu();">
                   <div class="icon_box"><img src="<%=SkinMgr.getSkinPath(request)%>/icons/control_6.png" width="49" height="47" /></div>
                   <div class="font_box">
                      <div class="title"><a>快速入口</a></div>
                      <div class="subtitle">方便快捷</div>
                   </div>
                </div>
<!--10_滑动菜单--><div style="<%=displaySlideMenu%>" class="control_modular" onMouseOver="addClasses(this)" onMouseOut="removeClasses(this)" onClick="addTab('滑动菜单组', 'admin/slide_menu_group.jsp')" >
                   <div class="icon_box"><img src="<%=SkinMgr.getSkinPath(request)%>/icons/control_10.png" width="49" height="47" /></div>
                   <div class="font_box">
                      <div class="title"><a>滑动菜单</a></div>
                      <div class="subtitle">编辑滑动菜单组</div>
                   </div>
                </div>

 <!--12_邮箱配置--><%
					String displayIntegrateEmail = "display:none";
					if (isIntegrateEmail) {
						displayIntegrateEmail = "";
					}
				%>
				<div style="<%=displayIntegrateEmail%>" class="control_modular" onMouseOver="addClasses(this)" onMouseOut="removeClasses(this)" onClick="mail();">
                   <div class="icon_box"><img src="<%=SkinMgr.getSkinPath(request)%>/icons/control_7.png" width="49" height="47" /></div>
                   <div class="font_box">
                      <div class="title"><a>邮箱配置</a></div>
                      <div class="subtitle">邮箱用户名、密码修改</div>
                   	  <div id="mail" style="display:none">
                   		 <form name="form3" id="form3" action="control_panel.jsp?op=edit" method="post">
						    <div style="<%=displayIntegrateEmail%>" class="dialog_margin">
						      邮箱用户名
						      <input name="emailName" value="<%=usd.getEmailName()%>" />
						    </div>
						    <div style="<%=displayIntegrateEmail%>">
						      邮箱密码&nbsp;&nbsp;&nbsp;
						      <input name="emailPwd" value="<%=usd.getEmailPwd()%>" />
						    </div>
						    <div style="display:none">
							   	<input type="text" name="isMsgWinPopup" value = "<%=usd.isMsgWinPopup()?1:0%>"></input>
								<input type="text" name="isMessageSoundPlay" value = "<%=usd.isMessageSoundPlay()?1:0%>"></input>
								<input type="text" name="isChatIconShow" value = "<%=usd.isChatIconShow()?1:0%>"></input>
								<input type="text" name="isChatSoundPlay" value = "<%=usd.isChatSoundPlay()?1:0%>"></input>
								<input type="text" name="isWebedit" value = "<%=usd.isWebedit()?1:0%>"></input>
								<input type="text" name="is_show_sidebar" id="is_show_sidebar3" value="<%=usd.isShowSidebar()?1:0%>"></input>
								<input type="text" name="skinCode" value = "<%=skinCode%>"></input>
								<input type="text" name="uiMode" id="uiMode3" value="<%=usd.getUiMode()%>"></input>
							</div>
                      	</form>
                      </div>
                   </div>
                </div>	
</div>
</div>
<script type="text/javascript">
function addClasses(obj){
	if ($(obj).hasClass('control_modular')) {
		$(obj).removeClass('control_modular');
		$(obj).addClass('selected');
	}
} 

function removeClasses(obj){
	if ($(obj).hasClass('selected')) {
		$(obj).removeClass('selected');
		$(obj).addClass('control_modular');
	}
}

function individuality(){
	$("#individuality").dialog("open");
}
function topic(){
	$("#topic").dialog("open");
}
function mail(){
	$("#mail").dialog("open");
}

function jump(){
	window.open("../jump.jsp?fromWhere=oa&toWhere=forum&action=usercenter");
}

function mypasswd(){
	$("#Password").val("");
	$("#Password2").val("");
	$("#Password3").val("");
	$("#showmsg").text("");
	$("#mypasswd").dialog({
		title: "密码修改",
		modal: true,
		bgiframe:true,
		closeText : "关闭", 
		buttons: {
			"取消": function() {
				$(this).dialog("close");
			},
			"确定": function() {
				if($("#Password3").val() == ""){
					jAlert("旧密码不能为空","提示");
					return;
				}else if($("#Password").val() == ""){
					jAlert("新密码不能为空","提示");
					return;
				}else if($("#Password2").val() == ""){
					jAlert("确认新密码不能为空","提示");
					return;
				}
				jConfirm('您确定要修改密码么？','提示',function(r){
					if(!r){return;}
					else{
						$(".treeBackground").addClass("SD_overlayBG2");
						$(".treeBackground").css({"display":"block"});
						$(".loading").css({"display":"block"});
						$.ajax({
							url: "control_panel.jsp?op=savepwd",
							type: "post",
							dataType: "json",
							data: $('#form4').serialize(),
							success: function(data, status){
								$(".loading").css({"display":"none"});
								$(".treeBackground").css({"display":"none"});
								$(".treeBackground").removeClass("SD_overlayBG2");
								jAlert(data.msg,"提示");
								if(data.ret==1){
									//window.location.reload();
									$("#mypasswd").dialog("close");					
								}
							},
							error: function(XMLHttpRequest, textStatus){
								$(".loading").css({"display":"none"});
								$(".treeBackground").css({"display":"none"});
								$(".treeBackground").removeClass("SD_overlayBG2");
								jAlert("修改出错！","提示");
							}
						});
					}
				})
			}
		},
		closeOnEscape: true,
		draggable: true,
		resizable:true
		});	
}

function changeLanguage(){

	$("#myLanguage").dialog({
		title: "选择语言",
		modal: true,
		bgiframe:true,
		closeText : "关闭", 
		buttons: {
			"取消": function() {
				$(this).dialog("close");
			},
			"确定": function() {
				$.ajax({
					url: "control_panel.jsp?op=saveLanguage",
					type: "post",
					dataType: "json",
					data: $('#form5').serialize(),
					success: function(data, status){
						if(data.ret==1){
							window.top.location.reload();
							$(this).dialog("close");					
						}
					},
					error: function(XMLHttpRequest, textStatus){
						jAlert("修改出错！","提示");
					}
				});	
			}
		},
		closeOnEscape: true,
		draggable: true,
		resizable:true
		});	
}

$(function(){
	$("#individuality").dialog({
		autoOpen : false,
		title : "个性设置",
		closeText : "关闭",
		modal: true,
		buttons: {
					"取消": function() {
						$(this).dialog("close");
					},
					"确定": function() {
						$.ajax({
							url: "control_panel.jsp?op=edit",
							type: "post",
							data: $('#form1').serialize(),
							success: function(data, status){
								jAlert("修改成功！","提示"); 
							},
							error: function(XMLHttpRequest, textStatus){
								jAlert("修改出错！","提示");
							}
						});	
						$(this).dialog("close");
					}
				}
	});
	$("#topic").dialog({
		autoOpen : false,
		modal: true,
		title : "界面设置",
		closeText : "关闭",
		buttons: {
					"取消": function() {
						$(this).dialog("close");
					},
					"确定": function() {
						document.getElementById("topicbtn").click();
					}
				}
	});
	$("#mail").dialog({
		autoOpen : false,
		modal: true,
		title : "邮箱设置",
		closeText : "关闭",
		buttons: {
					"取消": function() {
						$(this).dialog("close");
					},
					"确定": function() {
						$.ajax({
							url: "control_panel.jsp?op=edit",
							type: "post",
							data: $('#form3').serialize(),
							success: function(data, status){
								jAlert("修改成功！","提示"); 
							},
							error: function(XMLHttpRequest, textStatus){
								jAlert("修改出错！","提示");
							}
						});	
						$(this).dialog("close");
					}
				}
	});

})
</script>
</body>
</html>
