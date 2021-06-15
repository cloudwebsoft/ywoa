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

String ids = ParamUtil.get(request, "ids");
if (ids.equals("")) {
	out.print(StrUtil.Alert_Back("请选择文件！"));
	return;
}

String dir_code = ParamUtil.get(request, "dir_code");
PublicLeafPriv lp = new PublicLeafPriv(dir_code);
if (!lp.canUserManage(privilege.getUser(request))) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

PublicLeaf leaf = new PublicLeaf();
leaf = leaf.getLeaf(dir_code);

String op = ParamUtil.get(request, "op");
if (op.equals("change")) {
	String newDirCode = ParamUtil.get(request, "newDirCode");
	if (!newDirCode.equals("")) {
		String[] ary = StrUtil.split(ids, ",");
		PublicAttachment patt = new PublicAttachment();
		for (int i=0; i<ary.length; i++) {
			int id = StrUtil.toInt(ary[i]);
			patt = patt.getPublicAttachment(id);
			patt.setPublicDir(newDirCode);
			patt.save();
		}
	
		out.print(StrUtil.Alert_Redirect("操作成功！", "netdisk_public_attach_list.jsp?dir_code=" + StrUtil.UrlEncode(newDirCode)));
		return;
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>移动</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
<body>
<TABLE cellSpacing=0 cellPadding=3 width="100%" align=center>
  <TBODY>
    <TR>
      <TD class="tdStyle_1">移动文件</TD>
    </TR>
</TABLE>
<TABLE class="tabStyle_1 percent80">
  <form action="?op=change" method="post" name="form1" id="form1" onsubmit="form1.btnSubmit.disabled=true">
    <TBODY>
      <TR>
        <TD class="tabStyle_1_title">请选择目标目录</TD>
      </TR>
      <tr>
        <td align="center" style="height:30px"><select name="newDirCode">
            <%
				PublicLeaf rootlf = leaf.getLeaf(PublicLeaf.ROOTCODE);
				PublicDirectoryView dv = new PublicDirectoryView(rootlf);
				dv.ShowDirectoryAsOptionsWithCode(request, out, rootlf, rootlf.getLayer());
			%>
			</select>
       		<script>
			form1.newDirCode.value = "<%=leaf.getCode()%>";
			</script>
			</td>
      </tr>
      <tr>
        <td align="center"><input class="btn" name="btnSubmit" type="submit" value="提交" />
          &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
          <input class="btn" type="reset" value="重置" />
          <input name="ids" value="<%=ids%>" type="hidden" />
        </td>
      </tr>
    </TBODY>
  </form>
</TABLE>
</body>
</html>
