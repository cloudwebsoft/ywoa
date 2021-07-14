<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.plugin.group.*"%>
<%@ page import="com.redmoon.forum.plugin.group.photo.*"%>
<%
	long id = ParamUtil.getLong(request, "id");
	GroupDb gd = new GroupDb();
	gd = (GroupDb) gd.getQObjectDb(new Long(id));
	if (gd == null) {
		return;
	}

	UserMgr um = new UserMgr();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3c.org/TR/1999/REC-html401-19991224/loose.dtd">
<HTML xmlns="http://www.w3.org/1999/xhtml"><HEAD id=Head1>
<TITLE><%=gd.getString("name")%> - <%=Global.AppName%></TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<LINK href="<%=GroupSkin.getSkin(gd.getString("skin_code")).getPath()%>/css.css" type=text/css 
rel=stylesheet>
<META content="MSHTML 6.00.2900.3132" name=GENERATOR></HEAD>
<BODY>
<%@ include file="group_header.jsp"%>
<DIV class="content xw">
<%@ include file="group_left.jsp"%>
<DIV class=rw>
<DIV class="subPhoto block">
<DIV class=title>
<DIV class="cName l">圈子相册</DIV>
<DIV class="btn r"><IMG onclick="window.open('manager/frame.jsp?op=addPhoto&id=<%=id%>')" 
style="BORDER-TOP-WIDTH: 0px; BORDER-LEFT-WIDTH: 0px; BORDER-BOTTOM-WIDTH: 0px; BORDER-RIGHT-WIDTH: 0px" 
src="<%=GroupSkin.getSkin(gd.getString("skin_code")).getPath()%>/uploadImg_<%=SkinUtil.getLocale(request)%>.gif"></DIV></DIV>
<DIV class=txt>
<DIV class=countAndSort>
<DIV class=l><A href="http://q.35.cn/mygroup/photoCatalog-129.html">圈子相册</A> 
&gt; 共<%=gd.getInt("photo_count")%>张</DIV>
<DIV class=r>排序方式： <A id=LinkButton1 
href="javascript:__doPostBack('LinkButton1','')">按最新</A> <A id=LinkButton2 
href="javascript:__doPostBack('LinkButton2','')">按人气</A> </DIV></DIV>
<DIV class=imgList1>
<TABLE class=picListAll id=DataList1 style="BORDER-COLLAPSE: collapse" 
cellSpacing=0 border=0>
  <TBODY>
<%
	int pagesize = 8;
	
 	PhotoDb pd = new PhotoDb();
 	String sql = GroupSQLBuilder.getListGroupPhotoSql(id);
    long total = pd.getObjectCount(sql, "" + id);
	
	Paginator paginator = new Paginator(request, total, pagesize);
	int curpage = paginator.getCurPage();

 	ObjectBlockIterator oi = pd.getObjects(sql, "" + id, (curpage - 1) * pagesize, curpage * pagesize);
	int row = 0, col = 0;
 	while (oi.hasNext()) {
 		pd = (PhotoDb) oi.next();
		if (row==0) {
			out.print("<tr>");
			row = 1;
		}
		UserDb user = um.getUser(pd.getUserName());
%>  
    <TD>
      <P class=name 
      style="PADDING-RIGHT: 0px; PADDING-LEFT: 0px; PADDING-BOTTOM: 6px; PADDING-TOP: 0px"><STRONG><A 
      href="<%=pd.getPhotoUrl(request)%>" target="_blank"><%=StrUtil.toHtml(pd.getTitle())%></A></STRONG></P>
      <P class=imgsrcname style="BACKGROUND: #fff"><A 
      href="<%=pd.getPhotoUrl(request)%>" target="_blank"><IMG alt=<%=StrUtil.toHtml(pd.getTitle())%> 
      src="<%=pd.getPhotoUrl(request)%>"
      width=120></A></P>
      <P class="create blackLink" 
      style="FONT-SIZE: 12px; COLOR: #666; LINE-HEIGHT: 1.5em"><A 
      href="<%=request.getContextPath()%>/userinfo.jsp?username=<%=user.getName()%>"><%=user.getNick()%> </A>上传</P>
      <P class=blackLink style="FONT-SIZE: 12px; PADDING-TOP: 5px"></P></TD>
<%
		col ++;
		if (col==4) {
			out.print("</tr>");
			row = 0;
			col = 0;
		}
	}
	if (row==1) {
		out.print("</tr>");
	}
%>
  </TBODY>
  </TABLE>
</DIV>
<DIV class=more>
<DIV id=AspNetPager1 style="WIDTH: 100%">
<TABLE cellSpacing=0 cellPadding=0 width="100%" border=0>
  <TBODY>
  <TR>
    <TD style="WIDTH: 40%" vAlign=bottom noWrap align=left></TD>
    <TD class="" style="WIDTH: 60%" vAlign=bottom noWrap align=notset><%
	String querystr = "id=" + id;
	out.print(paginator.getPageBlock(request, "group_photo.jsp?" + querystr));
%></TD>
  </TR></TBODY></TABLE></DIV></DIV></DIV></DIV>
</DIV></DIV>
<%@ include file="group_footer.jsp"%>
</BODY></HTML>
