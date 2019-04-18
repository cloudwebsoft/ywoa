<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.security.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@page import="com.redmoon.oa.video.VideoMgr"%>
<%@page import="com.cloudwebsoft.framework.util.LogUtil"%>
<%@ include file="inc/inc.jsp"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>修改初始密码</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="inc/common.js"></script>
<script src="js/jquery.js"></script>
</head>
<body>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("set")) {
	String action = ParamUtil.get(request, "action");
	
	String url = "";
	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();	
	boolean isSpecified = cfg.get("styleMode").equals("2"); 
	// 指定风格
	if (isSpecified) {
		int styleSpecified = StrUtil.toInt(cfg.get("styleSpecified"), -1);
		if (styleSpecified!=-1) {
			if (styleSpecified==UserSetupDb.UI_MODE_PROFESSION) {
				// response.sendRedirect("oa.jsp" + queryStr);
				url = "oa.jsp";
			} else if (styleSpecified==UserSetupDb.UI_MODE_FLOWERINESS) {
				// response.sendRedirect("mydesktop.jsp" + queryStr);
				url = "mydesktop.jsp";					
			} else if (styleSpecified == UserSetupDb.UI_MODE_PROFESSION_NORMAL) {
				// 经典型传统菜单
				url = "oa_main.jsp";
			}
			else if (styleSpecified == UserSetupDb.UI_MODE_LTE) {
				url = "lte/index.jsp";
			}				
			else {
				url = "main.jsp";
			}
		}
	}
	else {
		String os = ParamUtil.get(request, "os");
		// Safari
		if (os.equals("4")) {			
			url = "main.jsp";
		}	
		else {
			UserSetupDb usd = new UserSetupDb();
			usd = usd.getUserSetupDb(privilege.getUser(request));
			if (usd.getUiMode()==UserSetupDb.UI_MODE_NONE) {
				com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();    
				if(license.isVip()) {
					url = "ui_mode_guide.jsp";
				}else{
					if (usd.getMenuMode()==UserSetupDb.MENU_MODE_NEW) {
						url = "oa.jsp";
					}
					else {
						url = "oa_main.jsp";
					}			
				}
			}
			else if (usd.getUiMode()==UserSetupDb.UI_MODE_PROFESSION) {
				if (usd.getMenuMode()==UserSetupDb.MENU_MODE_NEW) {
					url = "oa.jsp";
				}
				else {
					url = "oa_main.jsp";
				}		
			}
			else if (usd.getUiMode() == UserSetupDb.UI_MODE_LTE) {
				url = "lte/index.jsp";
			}				
			else if (usd.getUiMode()==UserSetupDb.UI_MODE_FLOWERINESS) {
				url = "mydesktop.jsp";
		}
		else
			url = "main.jsp";
		}
	}
		
	if ("enter".equals(action)) {
		response.sendRedirect(url);
		return;		
	}
	
	String pwd = ParamUtil.get(request, "pwd");
	String confirmPwd = ParamUtil.get(request, "confirmPwd");
	if (!pwd.equals(confirmPwd)) {
		out.print(StrUtil.Alert_Back("密码与确认密码不一致！"));
		return;
	}
		
	UserMgr um = new UserMgr();
	boolean re = false;
	if (pwd.equals("") && confirmPwd.equals("")) {
		re = true;
	}
	else {
		try {
			re = um.modifyPwd(request, privilege.getUser(request), pwd);
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
			return;
		}
	}
	if (re) {
		VideoMgr vmgr = new VideoMgr();                        
		if(vmgr.validate()){                             //校验成功后，同步添加视频会议用户
			String name = privilege.getUser(request);
			String returnString = vmgr.modifyUserPassword(name,pwd);
			boolean isFinish = vmgr.getResultByParseXML(returnString);
			if (isFinish)
				LogUtil.getLog("同步视频会议用户:").info("修改用户密码成功。");
			else 
				LogUtil.getLog("同步视频会议用户:").info("修改用户密码失败。");
		}
		
		response.sendRedirect(url);
	}
	else {
		out.print(StrUtil.Alert_Back("操作失败！"));
	}
	return;
}
%>
<br />
<br />
<br />
<form id="form1" action="oa_change_initpwd.jsp?op=set" method="post" onsubmit="return chkForm()">
<table width="70%" border="0" cellpadding="0" cellspacing="0" class="tabStyle_1 percent60">
  <tr>
	<td colspan="2" align="center" class="tabStyle_1_title">请修改初始密码</td>
  </tr>
  <tr>
    <td width="50%" align="right">密码&nbsp;</td>
    <td align="left">
    <input name="pwd" type="password" id="pwd" onkeyup="checkPwd(this.value)" />
    <font color="#FF0000"><span id="checkResult"></span></font>
    </td>
  </tr>
  <tr>
    <td align="right">确认密码&nbsp;</td>
    <td align="left"><input name="confirmPwd" type="password" id="confirmPwd" /></td>
  </tr>
  <tr>
    <td colspan="2" align="center">
    <input type="submit" class="btn" value="确定" />
    &nbsp;&nbsp;&nbsp;&nbsp;
    <input type="button" class="btn" value="直接进入" onclick="o('action').value='enter'; form1.submit();" />
    <input name="action" value="" type="hidden" />
    </td>
  </tr>
</table>
</form>
</body>
<script>
function chkForm() {
	if ($("#pwd").val()!="") {
		if ($("#pwd").val()!=$("#confirmPwd").val()) {
			alert("密码与确认密码不一致！");
			return false;
		}
	}
	else {
		alert("密码不能为空！");
		return false;
	}
}

function checkPwd(pwd) {
		$.ajax({
			type: "post",
			url: "user/user_edit_do.jsp",
			data: {
				op: "checkPwd",
				pwd: pwd
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
			},
			success: function(data, status){
				data = $.parseJSON(data);
				o("checkResult").innerHTML = data.msg;
			},
			complete: function(XMLHttpRequest, status){
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});	
}
</script>
</html>