<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skincode = UserSet.getSkin(request);
if (skincode.equals(""))
	skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
if (skin==null)
	skin = skm.getSkin(UserSet.defaultSkin);
String skinPath = skin.getPath();

String privurl = StrUtil.getNullString(request.getParameter("privurl"));
privurl = StrUtil.toHtml(privurl);
%>
<html>
<head>
<LINK href="forum/<%=skinPath%>/skin.css" type=text/css rel=stylesheet>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"></head>
<body>
<div id="welcome">
  <div style="background-color:#cccccc;width:480px;height:280px;padding:10px">
    <div style="width:480px;height:280px;background-color:white">
	<div style="text-align:right"><a style="padding:0px;margin:0px" href="javascript:hidePopLayer()"><img border="0" src="<%=request.getContextPath()%>/images/close1.gif"></a></div>
      <br>
        <form name=form1 action="<%=request.getContextPath()%>/login.jsp" method="post" onSubmit="return fmlogin_onsubmit()">
		<table width="88%" align="center" class="tableCommon80">
			<thead>
            <tr>
              <td height="22" colspan="2" align="left"><%=Global.AppName%>&nbsp;-&nbsp;<lt:Label res="res.label.door" key="user_login"/></td></tr>
			</thead>
            <tr>
              <td width="23%" height="22" align="left"><lt:Label res="res.label.door" key="user_name"/></td>
                    <td width="77%" height="22"><input name="name" style="width:120"></td>
          </tr>
            <tr>
              <td height="22" align="left"><lt:Label res="res.label.door" key="pwd"/></td>
                    <td height="22"><input name=pwd type=password autocomplete="off" style="width:120">
                      <input name="privurl" type="hidden" value="<%=privurl%>">                  </td>
          </tr>
            <%
        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        if (cfg.getBooleanProperty("forum.loginUseValidateCode")) {
		%>
            <tr>
              <td height="22" align="left"><lt:Label res="res.label.door" key="validate_code"/></td>
                    <td height="22"><input name="validateCode" type="text" size="1">
                      <img src='<%=request.getContextPath()%>/validatecode.jsp' border=0 align="absmiddle" style="cursor:hand" onClick="this.src='<%=request.getContextPath()%>/validatecode.jsp'" alt="<lt:Label res="res.label.forum.index" key="refresh_validatecode"/>"></td>
          </tr>
            <%}%>
            <tr>
              <td height="22" align="left"><lt:Label res="res.label.door" key="save"/></td>
                    <td height="22"><select id="loginSaveDate" name="loginSaveDate">
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
                      </select>
					  <script>
                      o("loginSaveDate").value = "<%=cfg.getProperty("forum.loginSaveDate")%>";
                      </script>                      
              </td>
          </tr>
            <tr>
              <td height="22" align="left"><lt:Label res="res.label.forum.index" key="login_hide"/></td>
                    <td height="22"><select id="covered" name="covered">
                      <option value=0 selected type='checkbox' checked>
                        <lt:Label res="res.label.forum.index" key="login_not_hide"/>
                      </option>
                      <option value=1>
                        <lt:Label res="res.label.forum.index" key="login_hide"/>
                      </option>
                      </select></td>
          </tr>
            <tr align="center">
              <td height="35" colspan="3"><input name="submit" type=submit value="<lt:Label res="res.label.forum.index" key="commit"/>">
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <input name="button" type="button" onClick="window.location.href='<%=request.getContextPath()%>/regist.jsp'" value="<lt:Label res="res.label.door" key="regist"/>"></td>
          </tr>
          </table>
      </form>
        <br>
    </div>
  </div>
</div>
<script language="javascript">
<!--
function fmlogin_onsubmit()
{
	errmsg = "";
	if (form1.name.value=="")
		errmsg += '<lt:Label res="res.label.door" key="input_name"/>\n'
	if (form1.pwd.value=="")
		errmsg += '<lt:Label res="res.label.door" key="input_pwd"/>\n'
	if (errmsg!="")
	{
		alert(errmsg);
		return false;
	}
}
//-->
</script>
</body>
</html>
