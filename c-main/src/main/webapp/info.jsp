<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=Global.AppName%></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="forum/<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
</head>
<body>
<div id="wrapper">
  <%@ include file="forum/inc/header.jsp"%>
  <div id="main">
    <%
String info = StrUtil.toHtml(ParamUtil.get(request, "info"));
String op = ParamUtil.get(request, "op");
String privurl = StrUtil.HtmlEncode(ParamUtil.get(request, "privurl"));
Privilege privilege = new Privilege();
%>
    <br>
    <br>
    <table align="center" class="tableCommon80">
      <thead>
        <tr align="center">
          <td height="25"><%=Global.AppName%> -
            <lt:Label res="res.label.info" key="info"/></td>
        </tr>
      </thead>
      <tr valign="middle">
        <td><div class="infoBox">
            <ul>
              <li><%=info%></li>
              <%if (info.trim().equals(SkinUtil.LoadString(request, "pvg_invalid"))) {%>
              <BR>
              <strong>
              <lt:Label res="res.label.info" key="reason"/>
              </strong><BR>
              <%if (!privilege.isUserLogin(request)) {%>
              <lt:Label res="res.label.info" key="not_login"/>
              <BR>
              <%}%>
              <lt:Label res="res.label.info" key="user_pvg_invalid"/>
              <BR>
              <%}%>
            </ul>
          </div>
          <%if (op.equals("login") || !privilege.isUserLogin(request)) {%>
          <form name=form1 action="login.jsp" method="post">
            <table width="90%" align="center" cellpadding="0" cellspacing="0" class="tableBorder1">
              <tr>
                <td width="20%" height="22" align="left"><lt:Label res="res.label.door" key="user_name"/></td>
                <td width="80%" height="22"><input name="name" style="width:120"></td>
              </tr>
              <tr>
                <td height="22" align="left"><lt:Label res="res.label.door" key="pwd"/></td>
                <td height="22"><input name=pwd type=password style="width:120">
                  <input name="privurl" type="hidden" value="<%=privurl%>"></td>
              </tr>
              <%
        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        if (cfg.getBooleanProperty("forum.loginUseValidateCode")) {
		%>
              <tr>
                <td height="22" align="left"><lt:Label res="res.label.door" key="validate_code"/></td>
                <td height="22"><input name="validateCode" type="text" size="1">
                  <img src='validatecode.jsp' border=0 align="absmiddle" onClick="this.src='validatecode.jsp'" style="cursor:hand"></td>
              </tr>
              <%}%>
              <tr>
                <td height="22" align="left"><lt:Label res="res.label.door" key="save"/></td>
                <td height="22"><select name="loginSaveDate">
                    <option value="<%=com.redmoon.forum.Privilege.LOGIN_SAVE_NONE%>" selected>
                    <lt:Label res="res.label.door" key="not_save"/>
                    </option>
                    <option value="<%=com.redmoon.forum.Privilege.LOGIN_SAVE_DAY%>">
                    <lt:Label res="res.label.door" key="save_one_day"/>
                    </option>
                    <option value="<%=com.redmoon.forum.Privilege.LOGIN_SAVE_MONTH%>">
                    <lt:Label res="res.label.door" key="save_one_month"/>
                    </option>
                    <option value="<%=com.redmoon.forum.Privilege.LOGIN_SAVE_YEAR%>">
                    <lt:Label res="res.label.door" key="save_one_year"/>
                    </option>
                  </select></td>
              </tr>
              <tr>
                <td height="22" align="left"><lt:Label res="res.label.forum.index" key="login_hide"/></td>
                <td height="22"><select name=covered>
                    <option value=0 selected type='checkbox' checked>
                    <lt:Label res="res.label.forum.index" key="login_not_hide"/>
                    </option>
                    <option value=1>
                    <lt:Label res="res.label.forum.index" key="login_hide"/>
                    </option>
                  </select></td>
              </tr>
              <tr align="center">
                <td height="35" colspan="2">
                <input name="submit" type=submit value="<lt:Label res="res.label.door" key="login"/>">              &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                </td>
              </tr>
            </table>
          </form>
          <br />
          <%}else{%>
          <div class="infoBox">
            <ul>
              <li><a href="javascript:history.back(-1)">
                <lt:Label res="res.label.info" key="back"/>
                </a></li>
              <li><a href="forum/index.jsp">
                <lt:Label res="res.label.info" key="back_to_home"/>
                </a></li>
            </ul>
          </div>
          <%}
%></td>
      </tr>
    </table>
    <br>
  </div>
  <%@ include file="forum/inc/footer.jsp"%>
</div>
</body>
<script language="javascript">
<!--
function form1_onsubmit(){
	errmsg = "";
	if (form1.name.value=="")
		errmsg += '<lt:Label res="res.label.door" key="input_name"/>\n'
	if (form1.pwd.value=="")
		errmsg += '<lt:Label res="res.label.door" key="input_pwd"/>\n'
	if (errmsg!=""){
		alert(errmsg);
		return false;
	}
}
//-->
</script>
</html>