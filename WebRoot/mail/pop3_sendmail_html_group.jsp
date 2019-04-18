<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><jsp:useBean id="userpop3setup" scope="page" class="com.redmoon.oa.emailpop3.UserPop3Setup"/><%
if (!privilege.isUserPrivValid(request, "read")) { // "admin.emailgroup")) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String to = fchar.getNullStr(fchar.UnicodeToGB(request.getParameter("to")));
String email = fchar.getNullString(request.getParameter("email"));
// 防XSS
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "email", email, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>发邮件</title>
<link href="mail.css" type="text/css" rel="stylesheet" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
<script src="../inc/common.js"></script>
<script src="../inc/upload.js"></script>

<script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>

<script>
function form1_onsubmit() {
	if (form1.content.value.length>3000) {
		// alert("您输入的数据太长，不允许超过3000字！");
		// return false;
	}
}

function saveDrafe() {
	form1.action = "pop3_draft_save.jsp";
	if (form1.content.value.length>3000) {
		// alert("您输入的数据太长，不允许超过3000字！");
		// return false;
	}
	form1.submit();
}
</script>
</head>
<body>
<div id="rightMain" style="overflow:auto;height:100%;">
  <div class="menuBoxC">
    <ul>
      <li id="sendButton" onclick="form1.submit()">
        <table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
          <tr>
            <td><img src="images/send.gif" /></td>
            <td><font class="topMenuButtonFont">发送</font></td>
          </tr>
        </table>
      </li>
      <li id="saveButton" onclick="saveDrafe()">
        <table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
          <tr>
            <td><img src="images/save_draft.gif" /></td>
            <td><font class="topMenuButtonFont">存草稿</font></td>
          </tr>
        </table>
      </li>
    </ul>
  </div>
  <div id="sendBox">
<form id=form1 enctype="MULTIPART/FORM-DATA" name="form1" action="pop3_sendmail_group_do.jsp" method="post" onSubmit="return form1_onsubmit()">
  <table width="100%" border="0" cellpadding="0" cellspacing="0">
    <tr>
      <td width="60">邮&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;箱：</td>
      <td><%
			String[] emails = userpop3setup.getUserEmails(privilege.getUser(request));
			String options = "";
			int len = 0;
			if (emails!=null)
				len = emails.length;
			for (int i=0; i<len; i++) {
				options += "<option value='"+emails[i]+"'>"+emails[i]+"</option>";
			}
			%>
        <select name="email">
          <%=options%>
        </select>
        <input name=username type="hidden" value="<%=privilege.getUser(request)%>">
        <%
			  if (!email.equals("")) { %>
        <script language="JavaScript">
			  form1.email.value = "<%=email%>"
			  </script>
        <%}
			  %></td>
    </tr>
    <tr>
      <td>方&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;式：</td>
      <td><select name="type" onChange="selType(this.value)">
          <option value="input" selected>手工输入Email地址</option>
          <option value="excel_addr">Excel导入Email地址</option>
          <option value="excel_addr_content">Excel导入Email地址及邮件内容</option>
        </select>      </td>
    </tr>
    <tr>
      <td>收&nbsp;&nbsp;件&nbsp;&nbsp;人：</td>
      <td><div id="emailTo">
          <textarea name="to" cols="60" rows="5"><%=to%></textarea>
        </div>
        <div id="emailExcel" style="display:none">
          <input name="excelFile" type="file">
        </div></td>
    </tr>
    <tr>
      <td>主&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;题：</td>
      <td><input size="40" name="subject" style="width:400px"></td>
    </tr>
    <tr>
	  <td>内&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;容：</td>
      <td><textarea id="content" name="content" rows="20" wrap="physical" cols="65" style="display:none"></textarea>
		<script>
        CKEDITOR.replace('content',
            {
                // skin : 'kama',
                toolbar : 'Middle'
            });
        </script>      
      </td>
    </tr>
    <tr>
      <td>附&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;件：</td>
      <td><script>initUpload()</script></td>
    </tr>
    <tr>
      <td colspan="2" align="center"><input type="submit" class="btn" value="发送">
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <input name="button" type="button" class="btn" onClick="saveDrafe()" value="存草稿">
      </td>
    </tr>
  </table>
</form>
</div>
</div>
</body>
<script language="javascript">
function selType(type) {
	if (type=="input") {
		$("emailTo").style.display = "";
		$("emailExcel").style.display = "none";
	}
	else {
		$("emailTo").style.display = "none";
		$("emailExcel").style.display = "";
	}
}
</script>
</html>
