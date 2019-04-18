<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title><lt:Label res="res.label.forum.admin.move_board" key="move_board"/></title>
<link href="default.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
.style1 {
	color: #FFFFFF;
	font-weight: bold;
}
.style2 {color: #FFFFFF}
-->
</style>
</head>
<body bgcolor="#FFFFFF" text="#000000">
<jsp:useBean id="cfg" scope="page" class="cn.js.fan.web.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String rootpath = request.getContextPath();
String bakpath = cfg.getProperty("Application.bak_path");
String op = ParamUtil.get(request, "op");
if (op.equals("move")) {
	String fromCode = ParamUtil.get(request, "fromCode");
	String toCode = ParamUtil.get(request, "toCode");
	try {
		MsgDb md = new MsgDb();
		int count = md.moveBoardMessages(request, fromCode, toCode);
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "res.label.forum.admin.move_board", "op_success") + count));
	}
	catch (ResKeyException e) {
		out.print(StrUtil.Alert_Back(e.getMessage(request)));
		return;
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="head"><lt:Label res="res.label.forum.admin.move_board" key="data_manage"/></td>
    </tr>
  </tbody>
</table>
<br>
<TABLE 
style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" 
cellSpacing=0 cellPadding=3 width="95%" align=center>
  <TBODY>
    <TR>
      <TD class=thead style="PADDING-LEFT: 10px" noWrap width="70%"><lt:Label res="res.label.forum.admin.move_board" key="move_board"/>      </TD>
    </TR>
    <TR class=row style="BACKGROUND-COLOR: #fafafa">
      <TD height="175" align="center" style="PADDING-LEFT: 10px"><table width="83%" border="0" align="center">
  <form name=form1 action="?op=move" method=post>
        <tr>
          <td height="28"><lt:Label res="res.label.forum.admin.move_board" key="from"/>
            <select name="fromCode" onChange="if(this.options[this.selectedIndex].value=='not'){alert(this.options[this.selectedIndex].text+' <lt:Label res="res.label.forum.admin.move_board" key="can_not_select"/>'); return false;}">
              <option value="not" selected><lt:Label res="res.label.forum.admin.move_board" key="sel_board"/></option>
              <%
				com.redmoon.forum.Directory directory = new com.redmoon.forum.Directory();
				com.redmoon.forum.Leaf lf = directory.getLeaf("root");
				com.redmoon.forum.DirectoryView dv = new com.redmoon.forum.DirectoryView(lf);
				dv.ShowDirectoryAsOptions(request, privilege, out, lf, lf.getLayer());
				%>
              </select>&nbsp;&nbsp;
          <lt:Label res="res.label.forum.admin.move_board" key="move_to"/>
            <select name="toCode" onChange="if(this.options[this.selectedIndex].value=='not'){alert(this.options[this.selectedIndex].text+' <lt:Label res="res.label.forum.admin.move_board" key="can_not_select"/>'); return false;}">
              <option value="not" selected><lt:Label res="res.label.forum.admin.move_board" key="sel_board"/></option>
              <%
				dv.ShowDirectoryAsOptions(request, privilege, out, lf, lf.getLayer());
				%>
              </select>            &nbsp;&nbsp;
            <input value="<lt:Label key="ok"/>" type="submit"></td>
          </tr>
        </form>
      </table></TD>
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