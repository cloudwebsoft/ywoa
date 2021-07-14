<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.entrance.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<link rel="stylesheet" href="../../common.css">
<LINK href="default.css" type=text/css rel=stylesheet>
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.forum.admin.entrance" key="plugin_manage"/></title>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	String boardCode = ParamUtil.get(request, "boardCode");
	if (!boardCode.equals("")) {
		String entranceCode = VIPUserGroupEntrance.CODE;
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
	String entranceCode = VIPUserGroupEntrance.CODE;
	BoardEntranceDb be = new BoardEntranceDb();
	be = be.getBoardEntranceDb(boardCode, entranceCode);
	if (be.del()) {
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
	}
	else
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_fail")));
}
if (op.equals("delUserGroup")) {
	String groupCode = ParamUtil.get(request, "groupCode");
	VIPUserGroupDb be = new VIPUserGroupDb();
	be = be.getVIPUserGroupDb(groupCode);
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
<table width="98%" height="227" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead">
      <lt:Label res="res.label.forum.admin.entrance" key="plugin_entrance"/>
    -&nbsp;<%=SkinUtil.LoadString(request, "res.config.entrance", VIPUserGroupEntrance.CODE)%></td>
  </tr>
  <tr> 
    <td valign="top"><br>
      <table width="86%"  border="0" align="center" cellpadding="0" cellspacing="0" bgcolor="#FFFBFF" class="tableframe_gray">
      <tr align="center">
        <td width="23%" height="22"><lt:Label res="res.label.forum.admin.entrance" key="board_code"/></td>
        <td width="34%" height="22"><lt:Label key="op"/></td>
      </tr>
<%
BoardEntranceDb br = new BoardEntranceDb();
Vector v = br.list(VIPUserGroupEntrance.CODE);
Iterator ir = v.iterator();
Leaf leaf = new Leaf();
while (ir.hasNext()) {
	BoardEntranceDb sb = (BoardEntranceDb)ir.next();
	leaf = leaf.getLeaf(sb.getBoardCode());
%>
      <tr align="center">
        <td height="22"><%=leaf.getName()%></td>
        <td height="22"><a href="?op=del&boardCode=<%=StrUtil.UrlEncode(leaf.getCode())%>&entranceCode=<%=StrUtil.UrlEncode(sb.getEntranceCode())%>"><lt:Label key="op_del"/></a></td>
      </tr>
<%}%>	  
    </table>
      <br>
      <table width="86%"  border="0" align="center" cellpadding="0" cellspacing="0">
	  <form name=form1 action="?op=add" method=post>
	          <tr>
          <td width="38%" align="right">
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
&nbsp;&nbsp;&nbsp;&nbsp;	  		    </td>
				          <td width="62%" align="left">			    <input type=submit value="<lt:Label res="res.label.forum.admin.entrance" key="add_board"/>">
&nbsp;&nbsp;<input type="button" onClick="window.location.href='VIPUserGroup_add.jsp'" value="<lt:Label res="res.label.forum.admin.entrance" key="add_user_group"/>"></td>
		      </tr></form>
      </table>
      <%
int pagesize = 10;
Paginator paginator = new Paginator(request);

VIPUserGroupDb vtu = new VIPUserGroupDb();
String sql = "select groupCode from " + vtu.getTableName() + " order by beginDate desc";	

int total = vtu.getObjectCount(sql);
paginator.init(total, pagesize);
int curpage = paginator.getCurPage();
//设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}
%>
      <table width="86%" height="24" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
          <td align="right"><div><%=paginator.getPageStatics(request)%></div></td>
        </tr>
      </table>
      <table width="86%"  border="0" align="center" cellpadding="0" cellspacing="1" bgcolor="#666666" class="tableframe_gray">
        <tr align="center" bgcolor="#F1EDF3">
          <td width="8%" height="22"><lt:Label res="res.label.forum.admin.entrance" key="user_group"/></td>
          <td width="12%" height="22"><lt:Label res="res.label.forum.admin.entrance" key="begin_date"/></td>
          <td width="18%" height="22"><lt:Label res="res.label.forum.admin.entrance" key="end_date"/></td>
          <td width="21%"><lt:Label res="res.label.forum.admin.entrance" key="status"/></td>
          <td width="14%"><lt:Label key="op"/></td>
        </tr>
        <%
Vector vth = vtu.list(sql, (curpage-1)*pagesize, curpage*pagesize-1);

Iterator irt = vth.iterator();
int i = 0;
UserGroupMgr ugm = new UserGroupMgr();
while (irt.hasNext()) {
	vtu = (VIPUserGroupDb)irt.next();
	UserGroupDb ugd = ugm.getUserGroupDb(vtu.getGroupCode());
	i++;
%>
        <form id="form<%=i%>" name="form<%=i%>" action="?op=modify" method="post">
          <tr align="center">
            <td height="22" bgcolor="#FFFFFF"><%=ugd.getDesc()%> </td>
            <td height="22" bgcolor="#FFFFFF"><%=com.redmoon.forum.ForumSkin.formatDate(request, vtu.getBeginDate())%></td>
            <td height="22" bgcolor="#FFFFFF"><%=com.redmoon.forum.ForumSkin.formatDate(request, vtu.getEndDate())%></td>
            <td bgcolor="#FFFFFF"><%=vtu.isValid()?SkinUtil.LoadString(request, "res.label.forum.admin.entrance", "status_use"):SkinUtil.LoadString(request, "res.label.forum.admin.entrance", "status_not_use")%></td>
            <td height="22" bgcolor="#FFFFFF"><a href="VIPUserGroup_edit.jsp?groupCode=<%=StrUtil.UrlEncode(vtu.getGroupCode())%>"><lt:Label key="op_edit"/></a>&nbsp;&nbsp;<a href="#" onClick="if (confirm('<lt:Label key="confirm_del"/>')) window.location.href='?op=delUserGroup&groupCode=<%=StrUtil.UrlEncode(vtu.getGroupCode())%>'"><lt:Label key="op_del"/></a></td>
          </tr>
        </form>
        <%}%>
      </table>
      <table width="86%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
        <tr>
          <td height="23"><div align="right">
              <%
	String querystr = "op=" + op;
    out.print(paginator.getCurPageBlock("?"+querystr));
%>
          </div></td>
        </tr>
      </table></td>
      </tr>
      </table>
      </td>
      </tr>
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
  