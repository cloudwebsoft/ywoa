<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../../../inc/inc.jsp" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.Conn"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.plugin.refer.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.Leaf"%>
<%@ page import="com.redmoon.forum.LeafChildrenCacheMgr"%>
<%@ page import="org.jdom.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<link rel="stylesheet" href="../../../common.css">
<LINK href="../../../admin/default.css" type=text/css rel=stylesheet>
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>插件管理</title>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String code = "refer";
PluginMgr pm = new PluginMgr();
PluginUnit pu = pm.getPluginUnit(code);
String op = ParamUtil.get(request, "op");
if (op.equals("addBoard")) {
	BoardDb sb = new BoardDb();
	String boardCode = ParamUtil.get(request, "boardCode");
	ReferSkin sv = new ReferSkin();
	if (!boardCode.equals("")) {
		if (sb.create(code, boardCode, ""))
			out.print(StrUtil.Alert(sv.LoadString(request, "addBoardSucceed")));
		else
			out.print(StrUtil.Alert(sv.LoadString(request, "addBoardFail")));
	}
}

if (op.equals("del")) {
	BoardDb sb = new BoardDb();
	String boardCode = ParamUtil.get(request, "boardCode");
	sb = sb.getBoardDb(code, boardCode);
	ReferSkin sv = new ReferSkin();
	if (!boardCode.equals("")) {
		if (sb.del())
			out.print(StrUtil.Alert(sv.LoadString(request, "delBoardSucceed")));
		else
			out.print(StrUtil.Alert_Back(sv.LoadString(request, "delBoardFail")));
	}
}
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head">管理插件-<%=ReferSkin.LoadString(request, "name")%></td>
  </tr>
</table>
<br>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead">管理 - <%=ReferSkin.LoadString(request, "name")%></td>
  </tr>
  <tr>
    <td valign="top"><br>
      <table width="86%"  border="0" align="center" cellpadding="0" cellspacing="1" bgcolor="#999999" class="tableframe_gray">
      <tr align="center">
        <td width="19%" height="24" bgcolor="#EFEBDE">版面编码</td>
      <td width="35%" height="22" bgcolor="#EFEBDE">版面名称</td>
        <td width="46%" height="22" bgcolor="#EFEBDE">操作</td>
      </tr>
<%
BoardDb sb1 = new BoardDb();
Vector v = sb1.list("select pluginCode, boardCode from " + sb1.getTableName() + " where pluginCode=" + StrUtil.sqlstr(code));
Iterator ir = v.iterator();
Leaf leaf = null;
com.redmoon.forum.Directory dir = new com.redmoon.forum.Directory();
while (ir.hasNext()) {
	BoardDb sb = (BoardDb)ir.next();
	leaf = dir.getLeaf(sb.getBoardCode());
%>
      <tr align="center">
        <td height="22" bgcolor="#FFFBFF"><%=leaf==null?"该版块已不存在":leaf.getCode()%></td>
      <td height="22" bgcolor="#FFFBFF"><%=leaf==null?"":leaf.getName()%></td>
        <td height="22" bgcolor="#FFFBFF"><a href="boardProp.jsp?code=<%=StrUtil.UrlEncode(code)%>&boardCode=<%=StrUtil.UrlEncode(leaf==null?"":leaf.getCode())%>">管理</a>&nbsp;&nbsp;&nbsp;<a href="manager.jsp?op=del&boardCode=<%=StrUtil.UrlEncode(sb.getBoardCode())%>">删除</a></td>
      </tr>
<%}%>
    </table>
      <br>
      <table width="86%"  border="0" align="center" cellpadding="0" cellspacing="0">
	  <form action="?op=addBoard" method=post>
        <tr>
          <td width="56%" align="right">
		  <select name="boardCode" onChange="if(this.options[this.selectedIndex].value=='not'){alert('您选择的是区域，请选择版块！'); this.selectedIndex=0;}">
            <option value="" selected>请选择版块</option>
			<%
				leaf = dir.getLeaf(Leaf.CODE_ROOT);
				com.redmoon.forum.DirectoryView dv = new com.redmoon.forum.DirectoryView(leaf);
				dv.ShowDirectoryAsOptions(request, privilege, out, leaf, leaf.getLayer(), true);
			%>
          </select>
          &nbsp;&nbsp;&nbsp;&nbsp;</td>
        <td width="44%" align="left"><input type=submit value="设为支持<%=ReferSkin.LoadString(request, "name")%>功能"></td>
        </tr></form>
      </table>
	  <br>
      <br></td>
  </tr>
</table>
</td> </tr>             
      </table>                                        
       </td>                                        
     </tr>                                        
 </table>                                        
                               
</body>                                        
</html>                            
  