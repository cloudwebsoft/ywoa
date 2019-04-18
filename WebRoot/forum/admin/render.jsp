<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.plugin.sweet.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<LINK href="default.css" type=text/css rel=stylesheet>
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.forum.admin.render" key="render"/></title>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String boardCode = ParamUtil.get(request, "boardCode").trim();
String renderCode = "";
com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();	
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "boardCode", boardCode, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

if (!boardCode.equals("")) {
	BoardRenderDb br = new BoardRenderDb();
	br = br.getBoardRenderDb(boardCode);
	if (br.isLoaded())
		renderCode = br.getRenderCode();
}

String op = ParamUtil.get(request, "op");
if (op.equals("setBoard")) {
	BoardRenderDb br = new BoardRenderDb();
	renderCode = ParamUtil.get(request, "renderCode");
	if (renderCode.equals("") || boardCode.equals("")) {
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "res.label.forum.admin.render", "board_render_empty")));
	}
	else {
		if (br.setBoardRender(boardCode, renderCode)) {
			out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
		}
		else
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_fail")));
	}
}
if (op.equals("del")) {
	BoardRenderDb br = new BoardRenderDb();
	br = br.getBoardRenderDb(boardCode);
	// renderCode = ParamUtil.get(request, "renderCode");
	if (br.del()) {
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
    <td height=20 align="left" class="thead"><lt:Label res="res.label.forum.admin.render" key="render"/></td>
  </tr>
  <tr> 
    <td height="142" valign="top"><br>
      <table width="86%"  border="0" align="center" cellpadding="0" cellspacing="1" bgcolor="#999999" class="tableframe_gray">
      <tr align="center">
        <td width="13%" height="24" bgcolor="#EFEBDE"><strong>
          <lt:Label res="res.label.forum.admin.entrance" key="code"/>
        </strong></td>
      <td width="23%" height="22" bgcolor="#EFEBDE"><strong>
        <lt:Label res="res.label.forum.admin.entrance" key="name"/>
      </strong></td>
        <td width="30%" bgcolor="#EFEBDE"><strong>
          <lt:Label res="res.label.forum.admin.render" key="render"/>
        </strong></td>
        <td width="34%" height="22" bgcolor="#EFEBDE"><strong>
          <lt:Label key="op"/>
        </strong></td>
      </tr>
<%
BoardRenderDb br = new BoardRenderDb();
Vector v = br.list();
Iterator ir = v.iterator();
Leaf leaf = new Leaf();
RenderMgr rm = new RenderMgr();
Vector vrender = rm.getAllRender();
while (ir.hasNext()) {
	BoardRenderDb sb = (BoardRenderDb)ir.next();
	leaf = leaf.getLeaf(sb.getBoardCode());
	if (leaf==null)
		leaf = new Leaf();	
	RenderUnit ru = rm.getRenderUnit(sb.getRenderCode());
%>
      <tr align="center">
        <td height="22" bgcolor="#FFFBFF"><%=leaf.getCode()%></td>
      <td height="22" bgcolor="#FFFBFF"><%=leaf.getName()%></td>
        <td bgcolor="#FFFBFF"><%=ru.getName(request)%></td>
        <td height="22" bgcolor="#FFFBFF"><a href="render.jsp?op=del&boardCode=<%=StrUtil.UrlEncode(sb.getBoardCode())%>&renderCode=<%=StrUtil.UrlEncode(sb.getRenderCode())%>"><lt:Label key="op_del"/></a></td>
      </tr>
<%}%>	  
    </table>
      <table width="98%"  border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
          <td height="22" align="center"><lt:Label res="res.label.forum.admin.render" key="default_render"/></td>
        </tr>
      </table>
      <br>
      <table width="86%"  border="0" align="center" cellpadding="0" cellspacing="0">
	  <form name=formsetrender action="?op=setBoard" method=post>
	     <tr>
          <td width="41%" align="right">
		  <select name="boardCode" onChange="if(this.options[this.selectedIndex].value=='no'){alert('<lt:Label res="res.label.forum.admin.entrance" key="error_sel_field"/>'); this.selectedIndex=0;} else window.location.href='render.jsp?boardCode='+this.options[this.selectedIndex].value">
            <option value="" selected><lt:Label res="res.label.forum.admin.render" key="sel_board"/></option>
            <%
				Directory dir = new Directory();
				leaf = dir.getLeaf(Leaf.CODE_ROOT);
				com.redmoon.forum.DirectoryView dv = new com.redmoon.forum.DirectoryView(leaf);
				dv.ShowDirectoryAsOptions(request, privilege, out, leaf, leaf.getLayer());			
			%>
          </select>
		  <script>
		  formsetrender.boardCode.value = "<%=boardCode%>";
		  </script>
          &nbsp;&nbsp;&nbsp;&nbsp;
	  		    </td>
				          <td width="21%" align="left">
			<select name="renderCode">
            <option value="" selected><lt:Label res="res.label.forum.admin.render" key="sel_render"/></option>
		  <%
		  ir = vrender.iterator();
		  while (ir.hasNext()) {
		  	RenderUnit ru = (RenderUnit)ir.next();
		  %>
		    <option value="<%=ru.getCode()%>"><%=ru.getName(request)%></option>
		  <%}%>
		  </select>
		  <script>
		  formsetrender.renderCode.value = "<%=renderCode%>";
		  </script>
						  </td>
						   <td width="38%" align="left"><input type=submit value="<lt:Label key="ok"/>"></td>
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
  