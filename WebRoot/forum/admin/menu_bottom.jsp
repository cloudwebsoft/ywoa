<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.forum.ui.menu.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="org.jdom.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title></title>
<LINK href="default.css" type=text/css rel=stylesheet>
<script>
function form1_onsubmit() {
}
</script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request))
{
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%
String parent_code = ParamUtil.get(request, "parent_code");
if (parent_code.equals(""))
	parent_code = "root";
String parent_name = ParamUtil.get(request, "parent_name");
String code = ParamUtil.get(request, "code");
String name = ParamUtil.get(request, "name");
String link = ParamUtil.get(request, "link");
int width = ParamUtil.getInt(request, "width", 60);
String op = ParamUtil.get(request, "op");
boolean isHome = false;
int type = 0;
if (op.equals("")) {
	op = "AddChild";
}

if (op.equals("AddChild")) {
	code = RandomSecquenceCreator.getId(10);
}

Leaf leaf = null;
if (op.equals("modify")) {
	Directory dir = new Directory();
	leaf = dir.getLeaf(code);
	name = leaf.getName();
	link = leaf.getLink();
	type = leaf.getType();
	isHome = leaf.getIsHome();
	width = leaf.getWidth();
}
%>
<TABLE 
style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" 
cellSpacing=0 cellPadding=3 width="95%" align=center>
  <!-- Table Head Start-->
  <TBODY>
    <TR>
      <TD class=thead style="PADDING-LEFT: 10px" noWrap width="70%"><lt:Label res="res.label.forum.admin.menu_bottom" key="dir_add_or_del"/></TD>
    </TR>
    <TR class=row style="BACKGROUND-COLOR: #fafafa">
      <TD align="center" style="PADDING-LEFT: 10px"><table class="frame_gray" width="415" border="0" cellpadding="0" cellspacing="1">
        <tr>
          <td width="411" align="center"><table width="98%">
            <form name="form1" method="post" action="menu_top.jsp?op=<%=op%>" target="dirmainFrame" onClick="return form1_onsubmit()">
              <tr>
                <td width="78" rowspan="8" align="left" valign="top"><br>
                  <lt:Label res="res.label.forum.admin.menu_bottom" key="dir"/>：<br>
                  <font color=blue><%=parent_name.equals("")?SkinUtil.LoadString(request, "res.label.forum.admin.menu_bottom", "dir_root"):parent_name%></font>					</td>
                <td width="312" align="left"><lt:Label res="res.label.forum.admin.menu_bottom" key="name"/>
                    <input name="name" value="<%=name%>">
                    <input name="code" value="<%=code%>" type="hidden"></td>
              </tr>
              <tr>
                <td align="left"><lt:Label res="res.label.forum.admin.menu_bottom" key="link"/>
                    <input name="link" value="<%=link%>">
                    <input type=hidden name=parent_code value="<%=parent_code%>">
                    <input type=hidden name=root_code value=""></td>
              </tr>
              <tr>
                <td align="left"><lt:Label res="res.label.forum.admin.menu_bottom" key="width"/>
                  <input name="width" value="<%=width%>"></td>
              </tr>
              <tr>
                <td align="left">
				<lt:Label res="res.label.forum.admin.menu_bottom" key="set_menu"/>
				<select name="preCode">
				<option value=""><lt:Label res="res.label.forum.admin.menu_bottom" key="none"/></option>
				<%
				String opts = "";
				com.redmoon.forum.ui.menu.Config cfg = com.redmoon.forum.ui.menu.Config.getInstance();
				List list = cfg.root.getChild("items").getChildren();
				if (list!=null) {
					Iterator ir = list.iterator();
					while (ir.hasNext()) {
						Element e = (Element)ir.next();
						opts += "<option value='" + e.getChildText("code") + "'>" + SkinUtil.LoadString(request, "res.label.forum.menu", e.getChildText("code")) + "</option>";
					}
				}
				%>
				<%=opts%>
				</select>

				target
				<select name="target">
				<option value=""><lt:Label key="wu"/></option>
				<option value="_blank">_blank</option>
				<option value="_self">_self</option>
				<option value="_parent">_parent</option>
				<option value="_top">_top</option>
				</select>
				<%
				if (op.equals("modify")) {
				%>
				<script>
				form1.preCode.value = "<%=leaf.getPreCode()%>";
				form1.target.value = "<%=leaf.getTarget()%>";
				</script>
				<%}%></td>
              </tr>
              <tr>
                <td align="left">
				<%
				String pChecked = "";
				String rChecked = "";
				if (op.equals("modify")) {
					if (leaf.isHasPath())
						pChecked = "checked";
					if (leaf.isResource())
						rChecked = "checked";
				}
				%>
				<input name="isHasPath" value="1" type="checkbox" <%=pChecked%>>
                  <lt:Label res="res.label.forum.admin.menu_bottom" key="link_replace"/>$u&nbsp;
                <input name="isResource" value="1" type="checkbox" <%=rChecked%>>
                  <lt:Label res="res.label.forum.admin.menu_bottom" key="name_from_res"/> </td>
              </tr>
              <tr>
                <td align="left"><span class="unnamed2">
                  <%if (op.equals("modify")) {%>
<%if (leaf.getCode().equals(Leaf.CODE_ROOT)) {%>
	<input type="hidden" name="parentCode" value="-1">
<%}else{%>
&nbsp;<lt:Label res="res.label.forum.admin.menu_bottom" key="dir_parent"/>：<select name="parentCode">
<%
	Leaf rootlf = leaf.getLeaf("root");
	DirectoryView dv = new DirectoryView(request, rootlf);
	dv.ShowDirectoryAsOptionsWithCode(out, rootlf, rootlf.getLayer());
%>
</select>
<script>
form1.parentCode.value = "<%=leaf.getParentCode()%>";
</script>
<%}
}%>
                <input type="hidden" name="isHome" value="true">
				<input name="templateId" type="hidden" value="-1">
                </span></td>
              </tr>
              <tr>
                <td align="center"><input name="Submit" type="submit" class="singleboarder" value="<lt:Label key="ok"/>">
                  &nbsp;&nbsp;&nbsp;
                  <input name="Submit" type="reset" class="singleboarder" value="<lt:Label key="reset"/>">
                  &nbsp;&nbsp;&nbsp;</td>
              </tr>
            </form>
          </table></td>
        </tr>
      </table>
      </TD>
    </TR>
    <!-- Table Body End -->
    <!-- Table Foot -->
    <TR>
      <TD class=tfoot align=right>&nbsp;</TD>
    </TR>
    <!-- Table Foot -->
  </TBODY>
</TABLE>
</body>
</html>
