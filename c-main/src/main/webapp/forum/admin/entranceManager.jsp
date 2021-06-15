<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.entrance.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<LINK href="default.css" type=text/css rel=stylesheet>
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.forum.admin.entrance" key="plugin_manage"/></title>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	String boardCode = ParamUtil.get(request, "boardCode");
	if (!boardCode.equals("")) {
		String entranceCode = ManagerEntrance.CODE;
		BoardEntranceDb be = new BoardEntranceDb();
		be.setBoardCode(boardCode);
		be.setEntranceCode(entranceCode);
		
		if (be.create()) {
			out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
		}
		else
			out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_fail")));
	}
}
if (op.equals("del")) {
	String boardCode = ParamUtil.get(request, "boardCode");
	String entranceCode = ManagerEntrance.CODE;
	BoardEntranceDb be = new BoardEntranceDb();
	be = be.getBoardEntranceDb(boardCode, entranceCode);
	if (be.del()) {
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
	}
	else
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_fail")));
}
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head"><lt:Label res="res.label.forum.admin.entrance" key="plugin_manage"/></td>
  </tr>
</table>
<br>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead"> <lt:Label res="res.label.forum.admin.entrance" key="plugin_entrance"/> 
      - <%=SkinUtil.LoadString(request, "res.config.entrance", ManagerEntrance.CODE)%></td>
  </tr>
  <tr> 
    <td valign="top"><br>
      <table width="86%"  border="0" align="center" cellpadding="0" cellspacing="0" bgcolor="#FFFBFF" class="tableframe_gray">
      <tr align="center">
        <td width="13%" height="22"><lt:Label res="res.label.forum.admin.entrance" key="board_code"/></td>
      <td width="23%" height="22"><lt:Label res="res.label.forum.admin.entrance" key="board_name"/></td>
        <td width="34%" height="22"><lt:Label key="op"/></td>
      </tr>
<%
BoardEntranceDb br = new BoardEntranceDb();
Vector v = br.list(ManagerEntrance.CODE);
Iterator ir = v.iterator();
Leaf leaf = new Leaf();
while (ir.hasNext()) {
	BoardEntranceDb sb = (BoardEntranceDb)ir.next();
	leaf = leaf.getLeaf(sb.getBoardCode());
%>
      <tr align="center">
        <td height="22"><%=leaf.getCode()%></td>
      <td height="22"><%=leaf.getName()%></td>
        <td height="22"><a href="?op=del&boardCode=<%=StrUtil.UrlEncode(leaf.getCode())%>&entranceCode=<%=StrUtil.UrlEncode(sb.getEntranceCode())%>"><lt:Label key="op_del"/></a></td>
      </tr>
<%}%>	  
    </table>
      <br>
      <table width="86%"  border="0" align="center" cellpadding="0" cellspacing="0">
	  <form name=form1 action="?op=add" method=post>
	          <tr>
          <td width="47%" align="right">
		  <select name="boardCode" onChange="if(this.options[this.selectedIndex].value=='no'){alert('<lt:Label res="res.label.forum.admin.entrance" key="error_sel_field"/>'); this.selectedIndex=0;}">
            <option value="" selected><lt:Label res="res.label.forum.admin.entrance" key="sel_board"/></option>
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
						   <td width="49%" align="left"><input type=submit value="<lt:Label res="res.label.forum.admin.entrance" key="add_board"/>"></td>
	          </tr></form>
      </table>
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
  