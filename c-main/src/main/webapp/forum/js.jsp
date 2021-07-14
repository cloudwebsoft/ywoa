<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="cn.js.fan.base.*"%>
<%
/*
参数：
1、论坛版面编码（为空表示所有版面）　boardcode
2、帖子id
3、取出图片的数量 picnum
4、图片的宽度（默认150）picw
5、图片的高度（默认150）pich
6、图片的行数 picx
7、图片的列数 picy
8、是否显示标题（＝n为不显示 ＝y为显示，默认不显示）pictitle
9、标题或内容字数（默认20）len
10、标题位置（＝top在图片的顶部显示、＝left在图片的左侧显示、＝right在图片的右侧显示、bottom在图片的底部显示）pos
11、是否显示内容（＝n为不显示 ＝y为显示）content
12、显示内容的位置（＝top在图片的顶部显示、＝left在图片的左侧显示、＝right在图片的右侧显示、＝bottom在图片的底部显示）pos
13、显示贴子从第start条至第end条，当start>=0时，显示贴子的列表
14、显示博客文章，isblog=y
15、显示最热门博客文章，ishot=y
16、显示某一类的博客文章，blogDirCode=类别编码
17、显示精华贴子,iselite=y
18、显示版块的贴子数，var=topiccount，显示最新用户，var=newuser,若type=online，则表示在线用户
19、显示日期，isdate=y，日期格式 dateformat=yyyy-MM-dd
例：
显示编号为1000的贴子的图片，图片行数为2，列数为3，图片显示数量为5，宽度和高度为150，显示标题，标题长度为10
<script src="forum/js.jsp?op=topic&id=1000&picx=2&picy=3&picnum=5&picw=150&pich=150&pictitle=y&len=10"></script>

取出版块编码为sqzw的版块中从0至9条贴子
<script src="forum/js.jsp?op=list&boardcode=sqzw&start=0&end=10"></script>

根据用户的id显示用户信息
<script src="forum/js.jsp?op=user&uid=userId"></script>

显示版块贴子数
<script src="forum/js.jsp?var=topiccount&boardcode=sqzw"></script>

显示论坛公告
<script src="forum/js.jsp?var=forum.notice"></script>

显示焦点热贴
<script src="forum/js.jsp?var=forum.hot"></script>

显示贴子及其回复，id为贴子编号
<script src="forum/js.jsp?op=reply&id=1000"></script>
*/

MsgDb md = new MsgDb();
String boardcode = ParamUtil.get(request, "boardcode");
long id = ParamUtil.getLong(request, "id", -1);
String var = ParamUtil.get(request, "var");
int start = ParamUtil.getInt(request, "start", 0);
int len = ParamUtil.getInt(request, "len", 30);
int row = ParamUtil.getInt(request, "row", 10);
String kind = ParamUtil.get(request, "kind");
String op = ParamUtil.get(request, "op");
if (op.equals("list")) {
	int end = ParamUtil.getInt(request, "end", 10);
	boolean isblog = ParamUtil.get(request, "isblog").equals("y");
	boolean ishot = ParamUtil.get(request, "ishot").equals("y");
	boolean iselite = ParamUtil.get(request, "iselite").equals("y");
	boolean isdate = ParamUtil.get(request, "isdate").equals("y");
	String dateformat = ParamUtil.get(request, "dateformat");
	if (dateformat.equals(""))
		dateformat = "yyyy-MM-dd";
	String blogDirCode = ParamUtil.get(request, "blogDirCode");
			
	String sql = "";
	
	if (!isblog) {
		if (!boardcode.equals("")) {
			if (iselite) {
				sql = "select id from sq_thread where boardcode="+StrUtil.sqlstr(boardcode)+" and iselite=1 and check_status=" + MsgDb.CHECK_STATUS_PASS + " and msg_level<=" + MsgDb.LEVEL_TOP_BOARD + " ORDER BY msg_level desc,redate desc";
			}
			else if (ishot)
				sql = "select id from sq_thread where boardcode="+StrUtil.sqlstr(boardcode)+" and check_status=" + MsgDb.CHECK_STATUS_PASS + " and msg_level<=" + MsgDb.LEVEL_TOP_BOARD + " ORDER BY msg_level desc,hit desc";
			else
				sql = "select id from sq_thread where boardcode="+StrUtil.sqlstr(boardcode)+" and check_status=" + MsgDb.CHECK_STATUS_PASS + " and msg_level<=" + MsgDb.LEVEL_TOP_BOARD + " ORDER BY msg_level desc,redate desc";
		}
		else {
			if (iselite) {
				sql = "select id from sq_thread where iselite=1 and check_status=" + MsgDb.CHECK_STATUS_PASS + " and msg_level<=" + MsgDb.LEVEL_TOP_BOARD + " ORDER BY msg_level desc,redate desc";
			}
			else if (ishot)
				sql = "select id from sq_thread where check_status=" + MsgDb.CHECK_STATUS_PASS + " and msg_level<=" + MsgDb.LEVEL_TOP_BOARD + " ORDER BY msg_level desc,hit desc";
			else
				sql = "select id from sq_thread where check_status=" + MsgDb.CHECK_STATUS_PASS + " and msg_level<=" + MsgDb.LEVEL_TOP_BOARD + " ORDER BY msg_level desc,redate desc";
		}
	}
	else {
		if (!ishot)
			sql = "select id from sq_thread where isblog=1 and check_status=" + MsgDb.CHECK_STATUS_PASS + " ORDER BY redate desc";
		else
			sql = "select id from sq_thread where isblog=1 and check_status=" + MsgDb.CHECK_STATUS_PASS + " ORDER BY hit desc, redate desc";
		if (!blogDirCode.equals(""))
			sql = "select id from sq_thread where isBlog=1 and check_status=" + MsgDb.CHECK_STATUS_PASS + " and blog_dir_code=" + StrUtil.sqlstr(blogDirCode) + " order by lydate desc";
	}
	ThreadBlockIterator irmsg = md.getThreads(sql, boardcode, start, end);
	while (irmsg.hasNext()) {
		md = (MsgDb)irmsg.next();
		if (!md.isLoaded())
			continue;
		if (isdate) {%>
			document.write("<span style='float:right'><%=DateUtil.format(md.getAddDate(), dateformat)%>&nbsp;</span>");
	<%	}
		if (!isblog) {
	%>
			document.write('<li><a href="<%=Global.getRootPath()%>/forum/showtopic.jsp?rootid=<%=md.getId()%>" title="<%=StrUtil.toHtml(md.getTitle())%>"><%=StrUtil.getLeft(DefaultRender.RenderFullTitle(request, md), len)%></a>' + '</li>');
	<%	}
		else {%>
			document.write('<li><a href="<%=Global.getRootPath()%>/blog/showblog.jsp?rootid=<%=md.getId()%>" title="<%=StrUtil.toHtml(md.getTitle())%>"><%=StrUtil.getLeft(DefaultRender.RenderFullTitle(request, md), len)%></a>' + '</li>');
		<%}
	}%>
<%
}
else if (op.equals("topic")) {
	md = md.getMsgDb(id);
	if (!md.isLoaded()) {%>
		document.write("id=" + id + " is not exist");
	<%}else{
		// 显示图片
		com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
	
		int picnum = ParamUtil.getInt(request, "picnum", 1);
		String picw = ParamUtil.get(request, "picw");
		String pich = ParamUtil.get(request, "pich");
		if (picw.equals(""))
			picw = "150";
		if (pich.equals(""))
			pich = "150";
		int w = StrUtil.toInt(picw, -1);
		if (w!=-1) {
			w += 5; // DIV比图片宽5个像素
			picw = "" + w;
		}

		int picx = ParamUtil.getInt(request, "picx", 1);
		int picy = ParamUtil.getInt(request, "picy", 0);
		
		boolean isShowTitle = ParamUtil.get(request, "pictitle").equals("y");
		boolean isShowContent = ParamUtil.get(request, "piccontent").equals("y");
		String pos = ParamUtil.get(request, "pos");
		if (isShowTitle || isShowContent) {
			if (pos.equals(""))
				pos = "bottom"; 
		}
		
		String target = ParamUtil.get(request, "target");
		Vector v = md.getAttachments();
		Iterator ir = v.iterator();
		int pnum = 1;
		int currow=0, curcol=0;
		out.println("document.write('<table>');");
		while (ir.hasNext()) {
			Attachment att = (Attachment)ir.next();
			String fname = att.getDiskName();
			String ext = StrUtil.getFileExt(fname);
			if (ext.equalsIgnoreCase("gif") || ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("bmp")) {
				if (curcol==0)
					out.println("document.write('<tr><td>');");
				String tlink = "<a target='" + target + "' href='forum/showtopic.jsp?rootid=" + id + "'>" + StrUtil.getLeft(DefaultRender.RenderFullTitle(request, md), len) + "</a>";
				String clink = "<a target='" + target + "' href='forum/showtopic.jsp?rootid=" + id + "'>" + StrUtil.toHtml(StrUtil.getLeft(md.getContent(), len)) + "</a>";
				%>
				document.write("<div align=center style='width:<%=picw%>px;height:<%=pich%>px;float:left'>");
				<%
				if (isShowTitle) {
					if (pos.equals("top"))
						out.println("document.write(\"" + tlink + "<BR>\");");
					else if (pos.equals("left"))
						out.println("document.write(\"'" + tlink + "\");");
				}
				else if (isShowContent) {
					if (pos.equals("top"))
						out.println("document.write(\"" + clink + "<BR>\");");
					else if (pos.equals("left"));
						out.println("document.write(\"" + clink + "\");");
				}
				String wh = "";
				if (!picw.equals(""))
					wh += "width='" + picw + "'";
				if (!pich.equals(""))
					wh += " height='" + pich + "'";
				out.println("document.write(\"<img target='" + target + "' href='forum/showtopic.jsp?rootid=" + id + "' border=0 src='" + cfg.getAttachmentPath() + "/" + att.getVisualPath() + "/" + att.getDiskName() + "' " + wh + ">\");");
				if (isShowTitle) {
					if (pos.equals("bottom"))
						out.println("document.write(\"<BR>" + tlink + "\");");
					else if (pos.equals("right"))
						out.println("document.write(\"" + tlink + "\");");
				}
				else if (isShowContent) {
					if (pos.equals("bottom"))
						out.println("document.write(\"<BR>" + clink + "\");");
					else if (pos.equals("right"));
						out.println("document.write(\"'" + clink + "\");");
				}
				
				out.println("document.write('</div>');");
				
				curcol ++;
				
				// System.out.println(getClass() + " curcol=" + curcol + " picy=" + picy);
				
				if (curcol>=picy) {
					out.println("document.write('</td></tr>');");
					curcol = 0;
					currow ++;
					if (currow>=picx)
						break;
				}
			}
			pnum ++;
			if (pnum>picnum)
				break;
		}
		if (curcol!=picy)
			out.println("document.write('</td></tr>');");
			
		out.println("document.write('</table>');");
	}%>
<%}
else if (op.equals("reply")) {
	start = ParamUtil.getInt(request, "start", 0);
	md = md.getMsgDb(id);
	if (!md.isLoaded()) {
		return;
	}
	String sql = "select id from sq_message where rootid=" + id +
                  " and check_status=" + MsgDb.CHECK_STATUS_PASS +
                  " ORDER BY lydate asc";
	String dateformat = ParamUtil.get(request, "dateformat");
	if (dateformat.equals(""))
		dateformat = "yyyy-MM-dd";
	int end = ParamUtil.getInt(request, "end", 10);
	boolean isdate = ParamUtil.get(request, "isdate").equals("y");

    MsgBlockIterator irmsg = md.getMsgs(sql, md.getboardcode(), id, start, end);
%>
	document.write("<ul>");
<%		
	while (irmsg.hasNext()) {
		md = (MsgDb)irmsg.next();
%>
		document.write("<li>");
<%		
		if (isdate) {%>
			document.write("<span style='float:right'><%=DateUtil.format(md.getAddDate(), dateformat)%>&nbsp;</span>");
	<%	}
	%>
		document.write('<a href="<%=Global.getRootPath()%>/forum/showtopic_tree.jsp?rootid=<%=id%>&showid=<%=md.getId()%>" title="<%=StrUtil.toHtml(md.getTitle())%>"><%=StrUtil.getLeft(md.getTitle(), len)%></a>' + '</li>');
<%
	}
%>
	document.write("</ul>");
<%		
} else if (op.equals("user")) {
	String userId = ParamUtil.get(request, "uid");
	UserDb user = new UserDb();
	user = user.getUser(userId);
	String myface = StrUtil.getNullStr(user.getMyface());
	%>
	document.write("<ul>");
	<%
	if (myface.equals("")) {%>
	document.write("<li><img src=\"<%=request.getContextPath()%>/images/face/<%=user.getRealPic().equals("") ? "face.gif" : user.getRealPic()%>\"></li>");
	<%}else{%>
	document.write("<li><img src=\"<%=user.getMyfaceUrl(request)%>\"></li>");
	<%}%>
	document.write("<li><%=user.getNick()%></li>");
	document.write("<li>发贴数：<%=user.getAddCount()%></li>");
	document.write("<li>等级：<%=user.getLevelDesc()%></li>");
	document.write("<li>经验：<%=user.getExperience()%></li>");
	document.write("<li>信用：<%=user.getCredit()%></li>");
	document.write("<li>金币：<%=user.getGold()%></li>");
	document.write("</ul>");
<%} else if (!var.equals("")) {
	// 显示版块的贴子数
	if (var.equals("topiccount")) {
		if (!boardcode.equals("")) {
			String sql = SQLBuilder.getListtopicSql(request, response, out, boardcode, "", "", 0);
			MsgDb msgdb = new MsgDb();
			int total = msgdb.getThreadsCount(sql, boardcode);
			out.println("document.write('" + total + "');");
		}
	}
	else if (var.equalsIgnoreCase("newuser")) {
		String type = ParamUtil.get(request, "type");
        String orderBy;
        if (type.equals("online"))
            orderBy = "online_time";
        else {
            orderBy = "RegDate";
        }
        String target = ParamUtil.get(request, "target");

        String more = ParamUtil.get(request, "more");
        if (more.equalsIgnoreCase("y")) {
            if (type.equals("online")) {
				out.println("document.write('" + request.getContextPath() + "/forum/stats.jsp?type=online" + "');");				
            }
            else {
				out.println("document.write('" + request.getContextPath() + "/listmember.jsp" + "');");				
            }
			return ;
        }

        UserDb ud = new UserDb();

        if (!target.equals(""))
            target = "target=\"" + target + "\"";

        StringBuffer str = new StringBuffer();
        str.append("<ul>");

        ObjectBlockIterator oi = ud.listUserRank(orderBy, row);
        String rootPath = request.getContextPath();
        while (oi.hasNext()) {
            ud = (UserDb) oi.next();
            str.append("<li>");
            str.append("<a " + target + " href='" + rootPath + "/userinfo.jsp?username=" + StrUtil.UrlEncode(ud.getName()) + "'>" + ud.getNick() + "</a>");
            str.append("</li>");
        }
        str.append("</ul>");	
		out.println("document.write(\"" + str + "\");");
	}
	else if (var.equalsIgnoreCase("forum.notice")) {
		// 论坛公告
        StringBuffer str = new StringBuffer();
        ForumDb fd = ForumDb.getInstance();
        Vector v = fd.getAllNotice();
        Iterator ir = v.iterator();
        String rootPath = Global.getRootPath();
        if (request != null)
            rootPath = request.getContextPath();
        while (ir.hasNext()) {
            md = (MsgDb) ir.next();
            str.append("<a href='" + rootPath +
                    "/forum/showtopic.jsp?rootid=" +
                    md.getId() + "' target='_blank'>");
            if (!md.getColor().equals(""))
                str.append("<font color='" + md.getColor() + "'>");
            if (md.isBold())
                str.append("<b>");
            str.append(md.getTitle());
            if (md.isBold())
                str.append("</b>");
            if (!md.getColor().equals(""))
                str.append("</font>");
            str.append(" [" + DateUtil.format(md.getAddDate(), "yyyy-MM-dd") +
                    "]</a>&nbsp;&nbsp;&nbsp;");
        }
		out.print("document.write(\"" + str + "\");");	
	}
	else if (var.equalsIgnoreCase("forum.hot")) {
		// 焦点热贴
        StringBuffer str = new StringBuffer();
        MsgMgr mm = new MsgMgr();
        com.redmoon.forum.ui.Home home = com.redmoon.forum.ui.Home.getInstance();
        int[] v = home.getHotIds();
        int hotlen = v.length;
        if (hotlen != 0) {
            boolean isDateShow = false;
            String dateformat = "";
			boolean isdate = ParamUtil.get(request, "isdate").equals("y");
            if (isdate) {
				dateformat = ParamUtil.get(request, "dateformat");
				if (dateformat.equals(""))
					dateformat = "yyyy-MM-dd";
            }

            String rootPath = Global.getRootPath();
            if (request != null)
                rootPath = request.getContextPath();

            str.append("<ul>");

            for (int k = 0; k < hotlen; k++) {
                md = mm.getMsgDb(v[k]);
                if (md.isLoaded()) {
                    str.append("<li><a href='" + rootPath +
                            "/forum/showtopic.jsp?rootid=" + md.getId() +
                            "' title='" + md.getTitle() + "'>");
                    if (!md.getColor().equals(""))
                        str.append("<font color='" + md.getColor() + "'>");
                    if (md.isBold())
                        str.append("<b>");
                    str.append(md.getTitle());
                    if (md.isBold())
                        str.append("</b>");
                    if (!md.getColor().equals(""))
                        str.append("</font>");
                    str.append("</a>&nbsp;");
                    if (isDateShow) {
                        str.append(" [" +
                                DateUtil.format(md.getAddDate(), dateformat) +
                                "]");
                    }
                    str.append("</li>");
                }
            }
        }
        str.append("</ul>");
		out.print("document.write(\"" + str + "\");");		
	}
}
%>