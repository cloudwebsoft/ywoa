<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.netdisk.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>目录属性</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script>
function form1_onsubmit() {
	form1.type.value = form1.seltype.value;
	// form1.root_code.value = window.parent.dirmainFrame.getRootCode();
}

function selTemplate(id)
{
	if (form1.templateId.value!=id) {
		form1.templateId.value = id;
	}
}
</script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	//out.println(StrUtil.makeErrMsg(privilege.MSG_INVALID, "red","green"));
	//return;
}
%>
<%
String parent_code = ParamUtil.get(request, "parent_code");
if (parent_code.equals(""))
	parent_code = privilege.getUser(request);
String parent_name = ParamUtil.get(request, "parent_name");
String code = ParamUtil.get(request, "code");
String name = ParamUtil.get(request, "name");
String description = ParamUtil.get(request, "description");
String op = ParamUtil.get(request, "op");
boolean isHome = false;
int type = 0;
if (op.equals(""))
	op = "AddChild";
if (op.equals("AddChild")) {
	LeafPriv lp = new LeafPriv();
	lp.setDirCode(parent_code);
	if (!lp.canUserAppend(privilege.getUser(request))) {
		// out.println(StrUtil.makeErrMsg(privilege.MSG_INVALID, "red", "green"));
		// return;
	}
}

Leaf leaf = null;
if (op.equals("modify")) {
	LeafPriv lp = new LeafPriv(code);
	if (!lp.canUserModify(privilege.getUser(request))) {
		// out.print(StrUtil.makeErrMsg(privilege.MSG_INVALID));
		// return;
	}

	Directory dir = new Directory();
	leaf = dir.getLeaf(code);
	name = leaf.getName();
	description = leaf.getDescription();
	type = leaf.getType();
	isHome = leaf.getIsHome();
}
%>
<TABLE cellSpacing=0 cellPadding=3 width="100%" align=center>
  <TBODY>
    <TR>
      <TD class="tdStyle_1">目录属性</TD>
    </TR>
    <TR>
      <TD align="center" style="PADDING-LEFT: 10px"><table width="98%" class="tabStyle_1 percent80">
        <form action="netdisk_left.jsp?op=<%=op%>" method="post" name="form1" target="leftFileFrame" id="form1" onclick="return form1_onsubmit()">
              <tr>
                <td align="left" class="tabStyle_1_title">
				当前结点：
                <%=parent_name.equals("")?"根结点":parent_name%>
				</td>
              </tr>
            <tr>
            <td width="243" align="left">名&nbsp;&nbsp;&nbsp;&nbsp;称
              <input name="name" value="<%=name%>" />
                <input type="hidden" name="code" value="<%=op.equals("modify")?code:Leaf.getAutoCode()%>" />
                <input type="hidden" name="root_code" value="<%=op.equals("modify")?leaf.getRootCode():""%>" />
				<input name="description" value="<%=description%>" type="hidden" />
                <input type="hidden" name="parent_code" value="<%=parent_code%>" />
                <input name="seltype" type="hidden" value="<%=Leaf.TYPE_DOCUMENT%>" />
                <input type="hidden" name="root_code" value="" />
                <input type="hidden" name="type" value="<%=type%>" />
                <input type="hidden" name="templateId" value="-1" />
				</td>
          </tr>
          <tr>
            <td align="left"><span class="unnamed2">
              <%if (op.equals("modify")) {
				  		if (!leaf.getParentCode().equals("-1")) {
				  %>
              <script>
							var bcode = "<%=leaf.getCode()%>";
							</script>
              父结点
              <select name="parentCode">
                <%
								Leaf rootlf = leaf.getLeaf(privilege.getUser(request));
								DirectoryView dv = new DirectoryView(rootlf);
								dv.ShowDirectoryAsOptionsWithCode(out, rootlf, rootlf.getLayer());
							%>
              </select>
              <script>
								form1.parentCode.value = "<%=leaf.getParentCode()%>";
							</script>
              <%}else{%>
              <input name="parentCode" type="hidden" value="<%=leaf.getParentCode()%>" />
              <%}%>
              <%}%>
            </span></td>
          </tr>
          <tr>
            <td align="center"><input name="Submit" type="submit" class="btn" value="提交" />
              &nbsp;&nbsp;
              <input name="Submit" type="reset" class="btn" value="重置" />
              &nbsp;&nbsp;
              <input title="当目录不正常时，修复目录" type="button" value="修复" onClick="repair()" />
			  </td>
          </tr>
        </form>
      </table></TD>
    </TR>
  </TBODY>
</TABLE>
</body>
<script>
function repair() {
	window.parent.leftFileFrame.location.href="netdisk_left.jsp?op=repair&root_code=<%=StrUtil.UrlEncode(leaf.getRootCode())%>";
}
</script>
</html>
