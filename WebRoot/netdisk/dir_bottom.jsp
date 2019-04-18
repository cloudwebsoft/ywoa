<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.netdisk.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title></title>
<LINK href="../admin/default.css" type=text/css rel=stylesheet>
<script>
function form1_onsubmit() {
	form1.type.value = form1.seltype.value;
	form1.root_code.value = window.parent.dirmainFrame.getRootCode();
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
if (!privilege.isUserLogin(request))
{
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
<TABLE 
style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" 
cellSpacing=0 cellPadding=3 width="95%" align=center>
  <!-- Table Head Start-->
  <TBODY>
    <TR>
      <TD class=thead style="PADDING-LEFT: 10px" noWrap width="70%">目录增加或修改</TD>
    </TR>
    <TR class=row style="BACKGROUND-COLOR: #fafafa">
      <TD align="center" style="PADDING-LEFT: 10px"><table class="frame_gray" width="335" border="0" cellpadding="0" cellspacing="1">
        <tr>
          <td width="411" align="center"><table width="98%">
            <form name="form1" method="post" action="dir_top.jsp?op=<%=op%>" target="dirmainFrame" onClick="return form1_onsubmit()">
              
              <tr>
                <td width="78" rowspan="5" align="left" valign="top"><br>
                  当前结点：<br>
                  <font color=blue><%=parent_name.equals("")?"根结点":parent_name%></font>					</td>
                <td width="243" align="left">名称
                    <input name="name" value="<%=name%>">
                    <input type=hidden name="code" value="<%=op.equals("modify")?code:Leaf.getAutoCode()%>"></td>
              </tr>
              <tr>
                <td align="left">描述
                    <input name="description" value="<%=description%>">
                    <input type=hidden name=parent_code value="<%=parent_code%>">                    <input name="seltype" type="hidden" value="<%=Leaf.TYPE_DOCUMENT%>">
                    <input type=hidden name=root_code value="">
                    <input type=hidden name="type" value="<%=type%>">
                    <input type=hidden name="templateId" value="-1"></td>
              </tr>
              
              <tr>
                <td align="left"><span class="unnamed2">
                  <%if (op.equals("modify")) {
				  		if (!leaf.getParentCode().equals("-1")) {
				  %>
							<script>
							var bcode = "<%=leaf.getCode()%>";
							</script>
							&nbsp;父结点：
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
							<input name="parentCode" type="hidden" value="<%=leaf.getParentCode()%>">
						<%}%>
				<%}%>
                </span></td>
              </tr>
              <tr>
                <td align="left">
</td>
              </tr>
              <tr>
                <td align="center"><input name="Submit" type="submit" class="btn" value="提交">
                  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                  <input name="Submit" type="reset" class="btn" value="重置">                </tr>
            </form>
          </table></td>
        </tr>
      </table>
      </TD>
    </TR>
    <!-- Table Body End -->
    <!-- Table Foot -->
    <TR>
      <TD class=tfoot align=right><DIV align=right> </DIV></TD>
    </TR>
    <!-- Table Foot -->
  </TBODY>
</TABLE>
</body>
</html>
