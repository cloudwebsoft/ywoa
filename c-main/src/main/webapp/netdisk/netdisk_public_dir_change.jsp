<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.netdisk.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.netdisk.DocumentMgr"/><%
if (!privilege.isUserLogin(request))
{
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int attachId = ParamUtil.getInt(request, "attachId");
PublicAttachment att = new PublicAttachment();
att = att.getPublicAttachment(attachId);

PublicLeafPriv lp = new PublicLeafPriv(att.getPublicDir());
if (!lp.canUserManage(privilege.getUser(request))) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("change")) {
	String newDirCode = ParamUtil.get(request, "newDirCode");
	if (!newDirCode.equals("")) {
		att.setPublicDir(newDirCode); 
		att.save();
		out.print(StrUtil.Alert_Redirect("修改成功！", "netdisk_public_attach_list.jsp?dir_code=" + StrUtil.UrlEncode(newDirCode)));
		return;
	}
}
else if (op.equals("changeattachname")) {
	boolean re = false;
	try {
		String newname = ParamUtil.get(request, "newname").trim();
		if (newname.equals("")) {
			throw new ErrMsgException("文件名不能为空！");
		}	
		att.setName(newname);
		re = att.save();
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	if (re) {
		out.print(StrUtil.Alert_Back("修改成功！"));
	}
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>转移</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
<body>
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">文件操作</td>
    </tr>
  </tbody>
</table>
<TABLE class="tabStyle_1 percent80">
  <form action="?op=change" method="post" name="form1" id="form1">
    <TBODY>
      <TR>
        <TD class="tabStyle_1_title">将<%=att.getName()%>转移至目录</TD>
      </TR>
      <tr>
        <td align="center"><select name="newDirCode">
            <%
				PublicLeaf rootlf = new PublicLeaf();
				rootlf = rootlf.getLeaf(PublicLeaf.ROOTCODE);
				PublicDirectoryView dv = new PublicDirectoryView(rootlf);
				dv.ShowDirectoryAsOptionsWithCode(request, out, rootlf, rootlf.getLayer());
			%>
          </select>
          <script>
				form1.newDirCode.value = "<%=att.getPublicDir()%>";
							</script>
        </td>
      </tr>
      <tr>
        <td align="center"><input name="Submit" type="submit" class="btn" value="提交" />
          &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
          <input name="Submit" type="reset" class="btn" value="重置" />
          <input name="attachId" value="<%=attachId%>" type="hidden" />
        </td>
      </tr>
    </TBODY>
  </form>
</TABLE>
<br>
<TABLE class="tabStyle_1 percent80">
<form action="?op=changeattachname" method="post" name="form2" id="form2">
  <TBODY>
    <TR>
      <TD class="tabStyle_1_title"><%=att.getName()%> 重命名 </TD>
    </TR>
	<tr>
	<td align="center"> 请输入新名称
	  <input name="newname" value="<%=att.getName()%>" />
	</td>
	</tr>
	<tr>
	<td align="center"><input name="Submit2" type="submit" class="btn" value="提交" />
	  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	  <input name="Submit2" type="reset" class="btn" value="重置" />
	  <input name="attachId" value="<%=attachId%>" type="hidden" />
	</td>
	</tr>
  </TBODY>
  </form>
</TABLE>
</body>
</html>
