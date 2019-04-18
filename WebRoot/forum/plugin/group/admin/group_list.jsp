<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.plugin.group.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<LINK href="../../../admin/default.css" type=text/css rel=stylesheet>
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>插件管理</title>
<style>
TABLE {
	BORDER-TOP: 0px; BORDER-LEFT: 0px; BORDER-BOTTOM: 1px;
}
TD {
	BORDER-RIGHT: 0px; BORDER-TOP: 0px;
}
</style>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String code = GroupUnit.code;
PluginMgr pm = new PluginMgr();
PluginUnit pu = pm.getPluginUnit(code);
String op = ParamUtil.get(request, "op");

if (op.equals("del")) {
	long groupId = ParamUtil.getLong(request, "groupId");
	GroupDb gd = new GroupDb();
	gd = (GroupDb)gd.getQObjectDb(new Long(groupId));
	boolean re = false;
	try {
		re = gd.del(new JdbcTemplate());
		if (re) {
			out.print(StrUtil.Alert_Redirect("操作成功!", "group_list.jsp"));
			return;
		}
	}
	catch (ResKeyException e) {
		out.print(StrUtil.Alert_Back(e.getMessage(request)));
		return;
	}
}

if (op.equals("modify")) {
	long groupId = ParamUtil.getLong(request, "groupId");
	GroupDb gd = new GroupDb();
	gd = (GroupDb)gd.getQObjectDb(new Long(groupId));
	int point = ParamUtil.getInt(request, "recommand_point", 0);
	String color = ParamUtil.get(request, "color");
	int is_open = ParamUtil.getInt(request, "is_open");
	int is_bold = ParamUtil.getInt(request, "is_bold");
	gd.set("recommand_point", new Integer(point));
	gd.set("is_open", new Integer(is_open));
	gd.set("is_bold", new Integer(is_bold));
	gd.set("color", color);
	boolean re = false;
	try {
		re = gd.save();
		if (re) {
			out.print(StrUtil.Alert_Redirect("操作成功!", "group_list.jsp"));
			return;
		}
	}
	catch (ResKeyException e) {
		out.print(StrUtil.Alert_Back(e.getMessage(request)));
		return;
	}
}
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head">管理插件-<%=GroupSkin.LoadString(request, "name")%></td>
  </tr>
</table>
<br>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead">管理 - <%=GroupSkin.LoadString(request, "name")%></td>
  </tr>
  <tr> 
    <td valign="top"><table width="82%"  border="0" align="center">
      
      <tr>
        <td height="34" align="center"><form action="group_list.jsp?op=search" method="post">
          <img src="../../../../images/arrow.gif" width="5" height="7">&nbsp;<a href="catalog_frame.jsp">朋友圈分类</a>&nbsp;&nbsp;<img src="../../../../images/arrow.gif" width="5" height="7">&nbsp;&nbsp;<a href="config_m.jsp">朋友圈配置</a>&nbsp;&nbsp;&nbsp;<img src="../../../../images/arrow.gif" width="5" height="7">&nbsp;<a href="group_list.jsp">朋友圈管理</a>&nbsp;&nbsp;&nbsp;&nbsp;
          <input name="what"><input type="submit" value="搜索">
		&nbsp;&nbsp;<a href="group_list.jsp?listType=member">成员排行</a>&nbsp;&nbsp;&nbsp;<a href="group_list.jsp?listType=topic">话题排行</a>
        </form></td>
      </tr>
    </table>
      <br>
      <%
		int pagesize = 10;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();

		String catalogCode = "";
		GroupDb gd = new GroupDb();
		String sql = GroupSQLBuilder.getListGroupSql(request);
		long total = gd.getQObjectCount(sql, catalogCode);
		paginator.init(total, pagesize);
		String listType = ParamUtil.get(request, "listType");
		String what = ParamUtil.get(request, "what");
		String querystr = "listType=" + listType + "&op=" + op + "&what=" + StrUtil.UrlEncode(what);
		UserMgr um = new UserMgr();
	  %>
      <table width="95%" border="0" align="center" class="p9">
        <tr>
          <td align="right"><%=paginator.getPageStatics(request)%>&nbsp;</td>
        </tr>
      </table>
      <table width="98%" style="border:solid 1px" border="1" align="center" cellpadding="3" cellspacing="0" bordercolor="#CCCCCC">
        <tr class="td_title">
          <td width="15%" align="center" class="thead">LOGO</td>
          <td width="32%" align="center" class="thead">名称</td>
          <td width="10%" align="center" class="thead">创建者</td>
          <td width="9%" align="center" class="thead">话题</td>
          <td width="8%" align="center" class="thead">相片</td>
          <td width="8%" align="center" class="thead">成员</td>
          <td width="9%" align="center" class="thead">状态</td>
          <td width="9%" align="center" class="thead"> 操作</td>
        </tr>
        <%		
		QObjectBlockIterator qi = gd.getQObjects(sql, catalogCode, (curpage-1)*pagesize, curpage*pagesize);
		int k=100;
		while (qi.hasNext()) {
			gd = (GroupDb)qi.next();
			k++;
		%>
		<form name="form<%=k%>" action="group_list.jsp?op=modify&groupId=<%=gd.getLong("id")%>" method="post">
        <tr>
          <td height="53" rowspan="2" align="center"><img src="<%=gd.getLogoUrl(request)%>" width=75 height=50></td>
          <td><img src="../images/group.gif">&nbsp;
		  <%
		  String clrName = gd.getString("name");
		  String color = StrUtil.getNullStr(gd.getString("color"));
		  if (!color.equals(""))
		  	clrName = "<font color=" + color + ">" + clrName + "</font>";
		  if (gd.getInt("is_bold")==1)
		  	clrName = "<strong>" + clrName + "</strong>";
		  %>
		  <a href="../group.jsp?id=<%=gd.getLong("id")%>" target="_blank">
		  <%=clrName%></a>
		  </td>
          <td align="center"><%=um.getUser(gd.getString("creator")).getNick()%></td>
          <td align="center"><%=gd.getInt("msg_count")%></td>
          <td align="center"><%=gd.getInt("photo_count")%></td>
          <td align="center"><%=gd.getInt("user_count")%></td>
          <td align="center">
		  <select name="is_open">
		  <option value="1">开启</option>
		  <option value="0">关闭</option>
		  </select>
		  </td>
          <td align="center"><a href="javascript:if (window.confirm('<%=cn.js.fan.web.SkinUtil.LoadString(request, "confirm_del")%>')) window.location.href='group_list.jsp?op=del&groupId=<%=gd.getLong("id")%>'">删除</a></td>
        </tr>
        <tr>
          <td colspan="7"><span style="float:right">创建日期：<%=ForumSkin.formatDate(request, gd.getDate("create_date"))%></span>
		  介绍：
		  <textarea cols="60" rows="3"><%=gd.getString("description")%></textarea>
		  &nbsp;</td>
        </tr>
        <tr>
          <td align="center">&nbsp;</td>
          <td colspan="7">推荐指数：<input name="recommand_point" value="<%=gd.getInt("recommand_point")%>">
          标题颜色：
            <select name="color" style="width:50px">
              <option value="" style="COLOR: black" selected>
              <lt:Label res="res.label.forum.manager" key="clear_color"/>
              </option>
              <option style="BACKGROUND: #000088" value="#000088"></option>
              <option style="BACKGROUND: #0000ff" value="#0000ff"></option>
              <option style="BACKGROUND: #008800" value="#008800"></option>
              <option style="BACKGROUND: #008888" value="#008888"></option>
              <option style="BACKGROUND: #0088ff" value="#0088ff"></option>
              <option style="BACKGROUND: #00a010" value="#00a010"></option>
              <option style="BACKGROUND: #1100ff" value="#1100ff"></option>
              <option style="BACKGROUND: #111111" value="#111111"></option>
              <option style="BACKGROUND: #333333" value="#333333"></option>
              <option style="BACKGROUND: #50b000" value="#50b000"></option>
              <option style="BACKGROUND: #880000" value="#880000"></option>
              <option style="BACKGROUND: #8800ff" value="#8800ff"></option>
              <option style="BACKGROUND: #888800" value="#888800"></option>
              <option style="BACKGROUND: #888888" value="#888888"></option>
              <option style="BACKGROUND: #8888ff" value="#8888ff"></option>
              <option style="BACKGROUND: #aa00cc" value="#aa00cc"></option>
              <option style="BACKGROUND: #aaaa00" value="#aaaa00"></option>
              <option style="BACKGROUND: #ccaa00" value="#ccaa00"></option>
              <option style="BACKGROUND: #ff0000" value="#ff0000"></option>
              <option style="BACKGROUND: #ff0088" value="#ff0088"></option>
              <option style="BACKGROUND: #ff00ff" value="#ff00ff"></option>
              <option style="BACKGROUND: #ff8800" value="#ff8800"></option>
              <option style="BACKGROUND: #ff0005" value="#ff0005"></option>
              <option style="BACKGROUND: #ff88ff" value="#ff88ff"></option>
              <option style="BACKGROUND: #ee0005" value="#ee0005"></option>
              <option style="BACKGROUND: #ee01ff" value="#ee01ff"></option>
              <option style="BACKGROUND: #3388aa" value="#3388aa"></option>
              <option style="BACKGROUND: #000000" value="#000000"></option>
            </select>
		  <select name="is_bold">
		  <option value="0">不加粗</option>
		  <option value="1">加粗</option>
		  </select>			
          <input name="submit" type="submit" value="确定"><input name="groupId" value="<%=gd.getLong("id")%>" type="hidden">
		  <script>
		  form<%=k%>.color.value = "<%=StrUtil.getNullStr(gd.getString("color"))%>";
		  form<%=k%>.is_open.value = "<%=gd.getInt("is_open")%>";
		  form<%=k%>.is_bold.value = "<%=gd.getInt("is_bold")%>";
		  </script>
		  </td>
        </tr>
		</form>
        <%}%>
      </table>
      <table width="98%" align="center">
        <tr><td align="right">
        <%
		out.print(paginator.getPageBlock(request, "group_list.jsp?" + querystr, "up"));
	  %>
      </td>
        </tr></table>
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
  