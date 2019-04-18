<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.module.pvg.*" %>
<%@ page import="org.jdom.*"%>
<%@ page import="cn.js.fan.module.cms.ui.menu.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>菜单管理-bottom</title>
<LINK href="default.css" type=text/css rel=stylesheet>
<script>
function form1_onsubmit() {
	if (form1.preCode.value=="module") {
		if (form1.formCode.value=="") {
			alert("请选择模块！");
			return false;
		}
	}
	window.location.reload();
}
</script>
<script src="../inc/common.js"></script>
<script>
function selIcon(icon) {
	form1.icon.value = icon;
	$("iconDiv").innerHTML = "<img src='<%=request.getContextPath()%>/images/icons/" + icon + "'>";
}
</script>
</head>
<body topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin")) {
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
String pvg = "", icon="";
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
	pvg = leaf.getPvg();
	icon = leaf.getIcon();
	
	Leaf pLeaf = new Leaf();
	pLeaf = pLeaf.getLeaf(leaf.getParentCode());
	if (pLeaf!=null)
		parent_name = pLeaf.getName();
}
%>
<form name="form1" method="post" action="menu_top.jsp?op=<%=op%>" target="dirmainFrame" onsubmit="return form1_onsubmit()">
  <table width="100%" class="frame_gray">
    <tr>
      <td colspan="2" align="left" class="head">
        <lt:Label res="res.label.forum.admin.menu_bottom" key="dir_add_or_del" />      </td>
    </tr>
    <tr>
      <td width="116" rowspan="8" align="left" valign="top"><br />
        当前父节点：<br>
		<font color=blue><%=parent_name.equals("")?SkinUtil.LoadString(request, "res.label.forum.admin.menu_bottom", "dir_root"):parent_name%></font>
        <div id="iconDiv">
          <%if (!icon.equals("")) {%>
          <img src="<%=request.getContextPath()%>/images/icons/<%=icon%>" />
          <%}%>
        </div></td>
      <td width="377" align="left">
          编码&nbsp;<input name="code" value="<%=code%>" <%=op.equals("modify")?"readonly":""%> />
		  <lt:Label res="res.label.forum.admin.menu_bottom" key="name"/>&nbsp;<input name="name" value="<%=name%>" />
          <br />
        图标&nbsp;<input name="icon" value="<%=icon%>" />
          <input name="button" type="button" onclick="openWin('menu_icon_sel.jsp', 800, 600)" value="选择" />
      </td>
    </tr>
    <tr>
      <td align="left"><lt:Label res="res.label.forum.admin.menu_bottom" key="link"/>&nbsp;<input name="link" value="<%=link%>" />
		target
                <select name="target">
                  <option value="mainFrame">右侧页面</option>
                  <option value="_blank">_blank</option>
                  <option value="_self">_self</option>
                  <option value="_parent">_parent</option>
                  <option value="_top">_top</option>
                </select>
          <input name="width" value="0" type="hidden" />
        <input type=hidden name=parent_code value="<%=parent_code%>" />
        <input type=hidden name=root_code value="" />				
<%if (op.equals("modify")) {%>
<script>
form1.target.value = "<%=leaf.getTarget()%>";
</script>
<%}%>				
				</td>
    </tr>
    <tr>
      <td align="left">能够看到菜单项的权限
        <input title="选择或填写权限编码，拥有该权限或者以该权限编码开头的权限的用户，才能看到此项菜单" name="pvg" value="<%=pvg%>" size="15" />
        (
        <select name="pvgCode" onchange="form1.pvg.value=this.value">
		<option value="">请选择</option>
		<%
		PrivDb pd = new PrivDb();
		Iterator irpv = pd.list().iterator();
		while (irpv.hasNext()) {
			pd = (PrivDb)irpv.next();
		%>
		<option value="<%=pd.getPriv()%>"><%=pd.getDesc()%></option>
		<%
		}
		%>
		</select>
		<%if (!pvg.equals("")) {%>
		<script>
		form1.pvgCode.value = "<%=pvg%>";
		</script>
		<%}%>
        )</td>
    </tr>
    <tr>
      <td align="left">
	  菜单类型
<%
String pcode = leaf==null?"":leaf.getPreCode();
String disabled = "";
if (op.equals("modify")) {
	disabled = "disabled";
}%>
	  
          <select name="preCode" <%=disabled%>>
            <option value="">
            <lt:Label res="res.label.forum.admin.menu_bottom" key="none"/>
            </option>
            <%
				String opts = "";
				cn.js.fan.module.cms.ui.menu.Config cfg = cn.js.fan.module.cms.ui.menu.Config.getInstance();
				List list = cfg.root.getChild("items").getChildren();
				if (list!=null) {
					Iterator ir = list.iterator();
					while (ir.hasNext()) {
						Element e = (Element)ir.next();
						opts += "<option value='" + e.getChildText("code") + "' " + (pcode.equals(e.getChildText("code"))?"selected":"") + ">" + e.getChildText("desc") + "</option>";
					}
				}
				%>
            <%=opts%>
          </select>
<%if (!disabled.equals("")) {%>
<input name="preCode" value="<%=leaf.getPreCode()%>" type="hidden">
<%}%>
</td>
    </tr>
    <tr>
      <td align="left"><%
				String pChecked = "";
				String rChecked = "checked";
				String nChecked = "";
				if (op.equals("modify")) {
					if (leaf.isHasPath())
						pChecked = "checked";
					if (leaf.isUse())
						rChecked = "checked";
					else
						rChecked = "";
					if (leaf.isNav())
						nChecked = "checked";
					else
						nChecked = "";						
				}
				%>
          <input name="isUse" value="1" type="checkbox" <%=rChecked%> />
        是否启用
        <!--<input name="isNav" value="1" type="checkbox" <%=nChecked%> />
        是否置于导航条(仅对一级目录有效)-->
        <!--<input name="isHasPath" value="1" type="checkbox" <%=pChecked%>>
                  <lt:Label res="res.label.forum.admin.menu_bottom" key="link_replace"/>$u--></td>
    </tr>
    <tr>
      <td align="left"><span class="unnamed2">
        <%if (op.equals("modify")) {%>
        <%if (leaf.getCode().equals(Leaf.CODE_ROOT)) {%>
        <input type="hidden" name="parentCode" value="-1" />
        <%}else{%>
        <lt:Label res="res.label.forum.admin.menu_bottom" key="dir_parent"/>
        <select name="parentCode">
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
        <input type="hidden" name="isHome" value="true" />
        <input name="templateId" type="hidden" value="-1" />
      </span></td>
    </tr>
    <tr>
      <td align="center"><input name="Submit" type="submit" class="singleboarder" value="<lt:Label key="ok"/>" />
        &nbsp;&nbsp;&nbsp;
        <input name="Submit" type="reset" class="singleboarder" value="<lt:Label key="reset"/>" />
        &nbsp;&nbsp;&nbsp;</td>
    </tr>
  </table>
</form>
</body>
</html>
