<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.cloudwebsoft.framework.base.*" %>
<%@ page import="com.redmoon.forum.music.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>目录管理</title>
<LINK href="default.css" type=text/css rel=stylesheet>
<script>
function form1_onsubmit() {
	// form1.root_code.value = window.parent.dirmainFrame.getRootCode();
}
</script>
</head>
<body>
<%
String code = ParamUtil.get(request, "code");
String name = ParamUtil.get(request, "name");
String description = ParamUtil.get(request, "description");
String op = ParamUtil.get(request, "op");
int type = 0;
MusicDirMgr dir = new MusicDirMgr();
MusicDirDb leaf = dir.getMusicDirDb(code);
name = leaf.getName();
description = leaf.getDescription();
type = leaf.getType();

String parent_code = leaf.getParentCode();
String parent_name = "";
MusicDirDb pisdd = dir.getMusicDirDb(parent_code);
if (pisdd!=null)
	parent_name = leaf.getName();
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="head">修改 <%=name%></td>
    </tr>
  </tbody>
</table><br>
<TABLE 
style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" 
cellSpacing=0 cellPadding=3 width="95%" align=center>
  <TBODY>
    <TR>
      <TD class=thead style="PADDING-LEFT: 10px" noWrap width="70%">目录修改</TD>
    </TR>
    <TR class=row style="BACKGROUND-COLOR: #fafafa">
      <TD align="center" style="PADDING-LEFT: 10px"><table class="frame_gray" width="48%" border="0" cellpadding="0" cellspacing="1">
        <tr>
          <td align="center"><table width="100%">
            <form name="form1" method="post" action="music_left.jsp?op=modify" target="leftFileFrame" onClick="return form1_onsubmit()">
              <tr>
                <td width="24%" align="center">&nbsp;</td>
                <td width="76%" align="left">名称
                    <input name="name" value="<%=name%>">
                    <input type="hidden" name="code" value="<%=code%>" <%=op.equals("modify")?"readonly":""%>>
                    <input type=hidden name=parent_code value="<%=parent_code%>">
                    <input type=hidden name=type value=1></td>
              </tr>
              <tr>
                <td align="center">&nbsp;</td>
                <td align="left">
                  <script>
				    var bcode = "<%=leaf.getCode()%>";
			        </script>
                  <%if (code.equals(leaf.ROOTCODE)) {%>
<input type=hidden name="parentCode" value="<%=leaf.getParentCode()%>">
<%}else{%>
父结点
<select name="parentCode">
  <%
									MusicDirDb rootlf = leaf.getMusicDirDb(MusicDirDb.ROOTCODE);
									MusicDirView dv = new MusicDirView(rootlf);
									dv.ShowDirAsOptions(out, rootlf, rootlf.getLayer());
					%>
</select>
<script>
					form1.parentCode.value = "<%=leaf.getParentCode()%>";
					</script>
<%}%></td>
              </tr>
              <tr>
                <td colspan="2" align="center"><input name="Submit" type="submit" class="singleboarder" value="提交">
&nbsp;&nbsp;
<input name="Submit" type="reset" class="singleboarder" value="重置">
&nbsp; 
<%if (!code.equals(leaf.ROOTCODE)) {%>
<input type="button" value="删除" onClick="del()">
&nbsp;
<input type="button" value="上移" onClick="move('up')">
&nbsp;
<input type="button" value="下移" onClick="move('down')">
<%}%></td>
                </tr>
            </form>
          </table></td>
        </tr>
      </table>
      </TD>
    </TR>
    <TR>
      <TD class=tfoot align=right><DIV align=right> </DIV></TD>
    </TR>
  </TBODY>
</TABLE>
</body>
<script>
function del() {
	if (confirm("您确定要删除吗？目录下的所有文件也将会一起被删除！")) {
		window.parent.leftFileFrame.location.href = "img_left.jsp?op=del&code=" + "<%=StrUtil.UrlEncode(code)%>";
	}
}

function move(fx) {
	window.parent.leftFileFrame.location.href = "img_left.jsp?op=move&direction=" + fx + "&code=" + "<%=StrUtil.UrlEncode(code)%>";
}
</script>
</html>
