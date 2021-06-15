<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.base.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.security.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<LINK href="../../cms/default.css" type=text/css rel=stylesheet>
<script src="../../inc/common.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>
<lt:Label res="res.label.forum.admin.message_recommend" key="recommend_message"/>
</title>
</head>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
	if (!privilege.isMasterLogin(request)) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	String query = "";
	String op = ParamUtil.get(request, "op");
	if (op.equals("del")) {
		int id = ParamUtil.getInt(request, "id");
		MessageRecommendDb mrd = new MessageRecommendDb();
		mrd = mrd.getMessageRecommendDb(id);
		boolean re = mrd.del();
		if (re) {
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "message_recommend.jsp"));
			return;
		}
		else {
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
			return;
		}
	} else if(op.equals("search")) {
		String type = ParamUtil.get(request, "search_type");
		if(type.equals("id")) {
			int key = ParamUtil.getInt(request, "search_key", -1);
			if (key!=-1)
				query = " where msg_id=" + key;
		} else if(type.equals("title")) {
			String key = ParamUtil.get(request, "search_key");
			query = " where msg_id in (select id from sq_message where title like '%" + key + "%')";
		}
	}
	
	int pagesize = 20;
	Paginator paginator = new Paginator(request);
	int curpage = paginator.getCurPage();	
		
	MessageRecommendDb mrd = new MessageRecommendDb();
	String userName = privilege.getUser(request);
	String sql = "select id from " + mrd.getTable().getName();
	if(!query.equals("")) {
		sql += query;
	}
	sql += " order by submit_date desc";
	//System.out.println(sql);
	
	ListResult lr = mrd.listResult(sql, curpage, pagesize);
	
	paginator.init(lr.getTotal(), pagesize);
	// 设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if (totalpages==0) {
		curpage = 1;
		totalpages = 1;
	}
%>
<%@ include file="message_recommend_nav.jsp"%>
<script>
$("menu2").className="active";
</script>
<br>
<form action="message_recommend.jsp?op=search" method="post">
<table border='0' align="center" cellpadding='0' cellspacing='0'>
  <tr>
    <td valign="middle" width="105">
    	<select name="search_type">
    		<option value="id">按编号搜索</option>
            <option value="title">按标题搜索</option>
        </select>
    </td>
    <td valign="middle" width="170"><input name="search_key" type="text" /></td>
    <td valign="middle"><input type="submit" value="搜索" style="height:24px;font-size:12px" /></td>
  </tr>
</table>
</form>
<br>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0'>
  <tr>
    <td align="right"><%=paginator.getPageStatics(request)%></td>
  </tr>
</table>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='1' class="frame_gray">
  <tr>
    <td width="8%" align="center" class="thead"><lt:Label res="res.label.forum.admin.message_recommend" key="id"/></td>
    <td width="25%" align="center" class="thead"><lt:Label res="res.label.forum.admin.message_recommend" key="title"/></td>
    <td width="36%" align="center" class="thead"><lt:Label res="res.label.forum.admin.message_recommend" key="recommend_reason"/></td>
    <td width="10%" align="center" class="thead"><lt:Label res="res.label.forum.admin.message_recommend" key="recommend_user_name"/></td>
    <td width="16%" align="center" class="thead"><lt:Label res="res.label.forum.admin.message_recommend" key="recommend_date"/></td>
    <td width="5%" align="center" class="thead"><lt:Label res="res.label.forum.admin.message_recommend" key="op"/></td>
  </tr>
  <%
    Iterator ir = lr.getResult().iterator();
	while (ir.hasNext()) {
		mrd = (MessageRecommendDb)ir.next();
		MsgMgr mm = new MsgMgr();
		MsgDb md = mm.getMsgDb(mrd.getLong("msg_id"));
		UserMgr um = new UserMgr();
		UserDb ud = um.getUser(mrd.getString("user_name"));
		
	%>
  <tr>
    <td height="22" align="center"><%=mrd.getLong("msg_id")%></td>
    <td><a href="../showtopic_tree.jsp?showid=<%=md.getId()%>" target="_blank"><%=md.isLoaded()?DefaultRender.RenderFullTitle(request, md):""%></a></td>
    <td><%=mrd.getString("report_reason")%></td>
    <td align="center"><a href="../../userinfo.jsp?username=<%=StrUtil.UrlEncode(ud.getName())%>" target="_blank"><%=ud.getNick()%></a></td>
    <td align="center"><%=ForumSkin.formatDateTime(request, mrd.getDate("submit_date"))%></td>
    <td align="center">
      <a href="javascript: if (confirm('<%=SkinUtil.LoadString(request, "confirm_del")%>')) window.location.href='message_recommend.jsp?id=<%=mrd.getInt("id")%>&op=del'">
        <lt:Label res="res.label.cms.dir" key="del"/>
      </a>
    </td>
  </tr>
  <%}%>
</table>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0'>
  <tr>
    <td align="right">
<%
	String querystr = "";
	out.print(paginator.getCurPageBlock("message_recommend.jsp?"+querystr));
%>
	</td>
  </tr>
</table>
</body>
</html>
