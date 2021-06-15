<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.Conn"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.entrance.*"%>
<%@ page import="com.redmoon.forum.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<html><head>
<meta http-equiv="pragma" content="no-cache">
<link rel="stylesheet" href="../../common.css">
<LINK href="default.css" type=text/css rel=stylesheet>
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>插件管理</title>
<script language="JavaScript">
<!--

//-->
</script>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="cn.js.fan.module.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "forum.plugin"))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	String boardCode = ParamUtil.get(request, "boardCode");
	if (!boardCode.equals("")) {
		String entranceCode = MasterEntrance.CODE;
		BoardEntranceDb be = new BoardEntranceDb();
		be.setBoardCode(boardCode);
		be.setEntranceCode(entranceCode);
		
		if (be.create()) {
			out.print(StrUtil.Alert("添加成功！"));
		}
		else
			out.print(StrUtil.Alert("添加失败，请检查版块是否已被加入！"));
	}
}
if (op.equals("del")) {
	String boardCode = ParamUtil.get(request, "boardCode");
	String entranceCode = MasterEntrance.CODE;
	BoardEntranceDb be = new BoardEntranceDb();
	be = be.getBoardEntranceDb(boardCode, entranceCode);
	if (be.del()) {
		out.print(StrUtil.Alert("删除成功！"));
	}
	else
		out.print(StrUtil.Alert("删除失败！"));
}
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head">管理插件-管理员进入方式插件</td>
  </tr>
</table>
<br>
<table width="98%" height="227" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead">管理 - 管理员进入方式插件 </td>
  </tr>
  <tr> 
    <td valign="top"><br>
      <table width="86%"  border="0" align="center" cellpadding="0" cellspacing="0" bgcolor="#FFFBFF" class="tableframe_gray">
      <tr align="center">
        <td width="13%" height="22">版面编码</td>
      <td width="23%" height="22">版面名称</td>
        <td width="30%">进入方式插件</td>
        <td width="34%" height="22">操作</td>
      </tr>
<%
BoardEntranceDb br = new BoardEntranceDb();
Vector v = br.list(MasterEntrance.CODE);
Iterator ir = v.iterator();
Leaf leaf = new Leaf();
while (ir.hasNext()) {
	BoardEntranceDb sb = (BoardEntranceDb)ir.next();
	leaf = leaf.getLeaf(sb.getBoardCode());
%>
      <tr align="center">
        <td height="22"><%=leaf.getCode()%></td>
      <td height="22"><%=leaf.getName()%></td>
        <td>管理员准入</td>
        <td height="22"><a href="?op=del&boardCode=<%=StrUtil.UrlEncode(leaf.getCode())%>&entranceCode=<%=StrUtil.UrlEncode(sb.getEntranceCode())%>">删除</a></td>
      </tr>
<%}%>	  
    </table>
      <br>
      <table width="86%"  border="0" align="center" cellpadding="0" cellspacing="0">
	  <form name=form1 action="?op=add" method=post>
	          <tr>
          <td width="47%" align="right">
		  <select name="boardCode" onChange="if(this.options[this.selectedIndex].value=='no'){alert('您选择的是区域，请选择版块！'); this.selectedIndex=0;}">
            <option value="" selected>请选择版块</option>
            <%
LeafChildrenCacheMgr dlcm = new LeafChildrenCacheMgr("root");
java.util.Vector vt = dlcm.getChildren();
ir = vt.iterator();
while (ir.hasNext()) {
	leaf = (Leaf) ir.next();
	String parentCode = leaf.getCode();
%>
            <option style="BACKGROUND-COLOR: #f8f8f8" value="no">╋ <%=leaf.getName()%></option>
<%
	LeafChildrenCacheMgr dl = new LeafChildrenCacheMgr(parentCode);
	v = dl.getChildren();
	Iterator ir1 = v.iterator();
	while (ir1.hasNext()) {
		Leaf lf = (Leaf) ir1.next();
%>
            <option style="BACKGROUND-COLOR: #eeeeee" value="<%=lf.getCode()%>">　├『<%=lf.getName()%>』</option>
		<%if (lf.getChildCount()>0) {
			Vector vch = lf.getChildren();
			Iterator irch = vch.iterator();
			while (irch.hasNext()) {
				Leaf chlf = (Leaf)irch.next();
		%>			
            <option style="BACKGROUND-COLOR: #eeeeee" value="<%=chlf.getCode()%>">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;　├『<%=chlf.getName()%>』</option>
		<%
			}
		}%>
    <%}
}%>
          </select>
          &nbsp;&nbsp;&nbsp;&nbsp;
	  		    </td>
				          <td width="4%" align="left">
			    </td>
						   <td width="49%" align="left"><input type=submit value="添加使用本进入方式插件的版块"></td>
	          </tr></form>
      </table></td>
  </tr>
</table>
</td> </tr>             
      </table>                                        
       </td>                                        
     </tr>                                        
 </table>                                        
                               
</body>                                        
</html>                            
  