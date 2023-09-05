<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@page import="com.redmoon.oa.pvg.RoleDb"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = ParamUtil.get(request, "userName");
UserMgr um = new UserMgr();
UserDb user = um.getUserDb(userName);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<title>用户信息</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<style>
.photoImg {
	width:230px;
}
</style>
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-migrate-1.2.1.min.js"></script>
<script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
<%
	if (user==null || !user.isLoaded()) {
		// out.print(StrUtil.jAlert_Back("该用户已不存在！","提示"));
		out.print(SkinUtil.makeErrMsg(request, "用户 " + userName + " 不存在！"));		
		return;
	}
 %>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td class="tdStyle_1"></b>&nbsp;用户信息</td>
  </tr>
</table>
<br/>    
<form method="post" action="user_edit_do.jsp"  name="memberform">
<table width="80%" align="center" class="tabStyle_1 percent60">
  <thead>
      <tr>
        <td class="tabStyle_1_title" colspan="4"><%=user.getRealName()%></td>
      </tr>
  </thead>
      <tr>
        <td width="15%">性别</td>
        <td width="33%" align="center"><% 
				  String strGender = "";
				  if (user.getGender()==0)
				  	strGender = "男";
				  else
				  	strGender = "女";
				  %>
            <%=strGender%> </td>
        <td colspan="2" rowspan="8" align="center" valign="middle" id="photoTd">
		<%if (!"".equals(user.getPhoto())) {%>
        <img class="photoImg" src="<%="showImg.do?path=" + user.getPhoto()%>" />
        <%}%>        
		<script>
          $(function() {
              <%if (user.getGender()==0) {%>
                $("#photoTd").css('background', 'url(images/man.png) center center no-repeat');
              <%}else{%>
                $("#photoTd").css('background', 'url(images/woman.png) center center no-repeat');
              <%}%>
          });
        </script>        
        </td>
      </tr>
  <tr>
        <td>部门</td>
        <td align="center"><%
			DeptMgr dm = new DeptMgr();
			DeptUserDb du = new DeptUserDb();
			Iterator ir2 = du.getDeptsOfUser(user.getName()).iterator();
			int k = 0;
			while (ir2.hasNext()) {
				DeptDb dd = (DeptDb)ir2.next();
				String deptName = "";
				if (!dd.getParentCode().equals(DeptDb.ROOTCODE) && !dd.getCode().equals(DeptDb.ROOTCODE)) {					
					deptName = dm.getDeptDb(dd.getParentCode()).getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dd.getName();
				}
				else {
                    deptName = dd.getName();
                }
				if (k==0) {
					out.print(deptName);
				}
				else {
					out.print("，&nbsp;" + deptName + "");
				}
				k++;
			} 
			%></td>
      </tr>
      <tr>
        <td>E-mail</td>
        <td align="center"><%=StrUtil.getNullString(user.getEmail())%></td>
      </tr>
      <tr>
        <td>QQ</td>
        <td align="center"><%=StrUtil.getNullStr(user.getQQ())%></td>
      </tr>
      <tr>
        <td>电话</td>
        <td align="center"><%=StrUtil.getNullString(user.getPhone())%></td>
      </tr>
      <tr>
        <td>手机号码</td>
        <td align="center"><%=user.getMobile()%></td>
      </tr>
      <tr style="display:none">
        <td>职务</td>
        <td align="center"><%=user.getDuty()%></td>
      </tr>
      <tr>
        <td>角色</td>
        <td align="center">
        <%
		RoleDb[] rld = user.getRoles();
      	int num = 0;
		int rolelen = 0;
		if (rld!=null) {
            rolelen = rld.length;
        }
		String roleNames = "";
		for (int i = 0; i < rolelen; i++) {
			if (rld[i].getCode().equals(RoleDb.CODE_MEMBER)) {
				if (++num == 2) {
					continue;
				}
			}
			if (roleNames.equals("")) {
                roleNames = rld[i].getDesc() !=null ? rld[i].getDesc() : "";
            } else {
                roleNames += "，" + (rld[i].getDesc() !=null ? rld[i].getDesc() : "");
            }
		}
		out.print(roleNames);
		%>      
        </td>
      </tr>
      <tr>
        <td>政治面貌</td>
        <td align="center"><%=StrUtil.getNullStr(user.getParty())%></td>
      </tr>
      <%if (License.getInstance().isGov()) {%>
      <tr>
        <td height="27" align=left>个人简历 </td>
        <td height="27" colspan="3"><%=user.getResume()%></td>
      </tr>
      <%}else{%>      
      <tr>
        <td>省份</td>
        <td align="center"><%=StrUtil.getNullString(user.getState())%> </td>
        <td style="width:80px;">城市</td>
        <td align="center">&nbsp;<%=StrUtil.getNullString(user.getCity())%></td>
      </tr>
      <tr>
        <td>地址</td>
        <td align="center"><%=StrUtil.getNullString(user.getAddress())%> </td>
        <td>邮政编码</td>
        <td align="center"><%=user.getPostCode()%></td>
      </tr>
      <tr>
        <td>兴趣爱好</td>
        <td align="center"><%=StrUtil.getNullString(user.getHobbies())%></td>
        <td>&nbsp;</td>
        <td align="center">&nbsp;</td>
      </tr>
      <%}%>
      <tr>
        <td colspan="4" align="center">
        <input class="btn" type="button" value="短消息" onclick="window.location.href='message_oa/message_frame.jsp?op=send&receiver=<%=StrUtil.UrlEncode(user.getName())%>'" />
		<%
        if (com.redmoon.oa.sms.SMSFactory.isUseSMS() && privilege.isUserPrivValid(request, "sms") && !user.getMobile().equals("")) {
        %>
        &nbsp;&nbsp;&nbsp;&nbsp;<input type="button" class="btn" onclick="window.location.href='message_oa/sms_send_message.jsp?receiver=<%=StrUtil.UrlEncode(user.getName())%>';" value="短信" /></a>
        <%}%>        
        </td>
    </tr>
</table>
</form>
</body>
<SCRIPT>
function VerifyInput()
{
var newDateObj = new Date()
if (document.memberform.username.value == "")
{
jAlert("请输入您的用户名","提示");
document.memberform.username.focus();
return false;
}

if (document.memberform.userpass.value == "")
{
jAlert("请输入您的密码","提示");
document.memberform.userpass.focus();
return false;
}
if (document.memberform.userpass2.value == "")
{
jAlert("请重复您的密码","提示");
document.memberform.userpass2.focus();
return false;
}
if (document.memberform.userpass.value != document.memberform.userpass2.value)
{
jAlert("两遍输入的口令不一致","提示");
document.memberform.userpass.focus();
return false;
}

if (document.memberform.usermail.value == "")
{
jAlert("请输入您的EMAIL地址","提示");
document.memberform.usermail.focus();
return false;
}

if (document.memberform.question.value == "")
{
jAlert("请输入提示问题","提示");
document.memberform.question.focus();
return false;
}

if (document.memberform.answer.value == "")
{
jAlert("请输入答案","提示");
document.memberform.answer.focus();
return false;
}

if (document.memberform.birthyear.value > 0)  {
if (isNaN(document.memberform.birthyear.value) || document.memberform.birthyear.value > newDateObj.getFullYear()  || document.memberform.birthyear.value < 1900)
{
jAlert("请输入正确的出生年份","提示");
document.memberform.birthyear.focus();
return false;
}}

return true;
}
</SCRIPT>
</html>