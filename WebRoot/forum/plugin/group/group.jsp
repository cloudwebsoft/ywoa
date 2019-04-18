<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.plugin.group.*"%>
<%@ page import="com.redmoon.forum.plugin.group.photo.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.module.nav.*"%>
<%
	long id = ParamUtil.getLong(request, "id", -1);
	if (id==-1) {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "err_id")));	
		return;
	}
	GroupDb gd = new GroupDb();
	gd = (GroupDb) gd.getQObjectDb(new Long(id));
	if (gd == null) {
		return;
	}
	
	if (gd.getInt("is_public")==0) {
		if (!GroupPrivilege.isMember(request, id)) {
			out.print(SkinUtil.makeErrMsg(request, "只有圈内成员才能访问!"));
			return;			
		}
	}
	
	String skinPath = GroupSkin.getSkin(gd.getString("skin_code")).getPath();

	UserMgr um = new UserMgr();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<title><%=gd.getString("name")%>-<%=Global.AppName%>
		</title>
		<link
			href="<%=GroupSkin.getSkin(gd.getString("skin_code")).getPath()%>/css.css"
			rel="stylesheet" type="text/css" />
	</head>
	<body>
		<%@ include file="group_header.jsp"%>
		<div class="content xw">
			<%@ include file="group_left.jsp"%>
			<div class="rw">
				<div class="rw11">
					<div class="topic block">
						<div class="title">
							<div class="cName l">
								圈子话题
							</div>
							<div class="btn r">
								<a target="_blank" id="hpl_postTopic" title="只有本圈成员才能发表话题"
									href="manager/frame.jsp?op=addTopic&id=<%=id%>"><img
										title="只有本圈成员才能发表话题"
										src="<%=skinPath%>/addTopic_<%=SkinUtil.getLocale(request)%>.gif"
										style="border-width:0px;" alt="" /> </a>
							</div>
						</div>
						<div class="txt">
							<div class="countAndSort">
								<div class="l">
									共有
									<%=gd.getInt("msg_count")%>
									<span></span> 个圈子话题，
									<%=gd.getInt("photo_count")%>
									<span></span> 个相片
								</div>
							</div>
							<table class="topicList">
								<thead>
									<tr>
										<th class="name" style="width: 333px">
											话题
										</th>
										<th class="postPerson">
											发布者
										</th>
										<th class="count">
											回复
										</th>
									</tr>
								</thead>
								<tbody>
									<%
										GroupThreadDb gtd = new GroupThreadDb();
										String sql = gtd.getListThreadSql(id, "reply_date");
										QObjectBlockIterator obi = gtd.getQObjects(sql, "" + id, 0, 10);
										long msgId = -1;
										MsgMgr mm = new MsgMgr();
										MsgDb md = null;
										while (obi.hasNext()) {
											gtd = (GroupThreadDb) obi.next();
											md = mm.getMsgDb(gtd.getLong("msg_id"));
											UserDb user = um.getUser(md.getName());
									%>
									<tr>
										<td class="name" style="width: 333px">
											<a
												href="../../<%=ForumPage.getShowTopicPage(request, md.getId())%>"
												target="_blank"> <%=StrUtil.toHtml(md.getTitle())%> </a>
											<br />
											发布时间:
											<%=ForumSkin.formatDateTime(request, gtd
								.getDate("add_date"))%>
										</td>
										<td class="postPerson greenLink">
											<a
												href="../../../userinfo.jsp?username=<%=StrUtil.UrlEncode(user.getName())%>"
												target="_blank"> <%=user.getNick()%> </a>
										</td>
										<td class="count">
											<%=md.getRecount()%>
											人
										</td>
									</tr>
									<%
									}
									%>
								</tbody>
							</table>
							<div class="more" style="text-align: right;">
								<a href="group_thread.jsp?id=<%=id%>">更多...</a>
							</div>
						</div>
					</div>
					<div class="photo block yspace">
						<div class="title">
							<div class="cName l">
								圈子相册
							</div>
							<div class="btn r">
								<input type="image" name="img_UplodPic" id="img_UplodPic"
									src="<%=skinPath%>/uploadImg_<%=SkinUtil.getLocale(request)%>.gif"
									alt="只有本圈成员才能上传照片"
									onclick="window.open('manager/frame.jsp?op=addPhoto&id=<%=id%>')"
									style="border-width:0px;" />
							</div>
						</div>
						<div class="txt" style="padding-left: 5px;">
							<span id="photoList" class="photoCatalog"> <%
 	PhotoDb pd = new PhotoDb();
 	sql = GroupSQLBuilder.getListGroupPhotoSql(id);
 	ObjectBlockIterator oi = pd.getObjects(sql, "" + id, 0, 8);
 	while (oi.hasNext()) {
 		pd = (PhotoDb) oi.next();
 %> <span>
									<div class="alumList" style="margin: 0 5px;">
										<p class="name">
											<strong><a href='<%=pd.getPhotoUrl(request)%>'
												target="_blank"> <%=StrUtil.toHtml(pd.getTitle())%> </a> </strong>
										</p>
										<p class="imgsrcname1">
											<%
											if (!pd.getImage().equals("")) {
											%>
											<a href='<%=pd.getPhotoUrl(request)%>' target="_blank"><img
													src='<%=pd.getPhotoUrl(request)%>' alt='<%=pd.getTitle()%>'
													width="120" />
											</a>
											<%
											}
											%>
										</p>
									</div> </span> <%
 }
 %> </span>
							<div class="hackbox">
							</div>
							<div class="more" style="text-align: right;">
								<a href="group_photo.jsp?id=<%=id%>">更多...</a>
							</div>
						</div>
					</div>
				</div>
				<div class="rw12">
					<div class="leftBlock1">
						<div>
							<input type="image" name="doJoin" id="doJoin" title=""
								src="<%=skinPath%>/join_<%=SkinUtil.getLocale(request)%>.gif"
								onclick="window.location.href='group_apply.jsp?id=<%=gd.getLong("id")%>'"
								style="border-width:0px;padding-bottom: 8px;" />
							<input type="image" name="doFavorite" id="doFavorite" title=""
								src="<%=skinPath%>/favarite_<%=SkinUtil.getLocale(request)%>.gif"
								onclick="window.external.addFavorite('<%=Global.getRootPath()%>/forum/plugin/group/group.jsp?id=<%=id%>', '<%=gd.getString("name")%>')"
								style="border-width:0px;padding-bottom: 8px;" />
						</div>
						<div class="title">
							<div class="cName">
								圈子公告
							</div>
						</div>
						<div class="txt note">
							<%=StrUtil.toHtml(gd.getString("notice"))%>
						</div>
					</div>
					<div class="party leftBlock1">
						<div class="title">
							<div class="cName">
								圈子活动
							</div>
						</div>
						<div class="txt">
							<span id="partyList" class="partyList"> <%
 	GroupActivityDb ga = new GroupActivityDb();
 	sql = ga.getTable().getSql("activityOfGroup") + id;
 	obi = ga.getQObjects(sql, 0, 5);
 	while (obi.hasNext()) {
 		ga = (GroupActivityDb) obi.next();
 		md = mm.getMsgDb(ga.getLong("msg_id"));
 %> <span id="newActiveList"> <a target="_blank"
									href="../../<%=ForumPage.getShowTopicPage(request, md.getId())%>"><%=StrUtil.toHtml(md.getTitle())%>
								</a> </span> <%
 }
 %> </span>
							<p style="text-align:right; padding-top:6px;">
								<a href="group_activity.jsp?id=<%=id%>">更多..</a>
							</p>
						</div>
					</div>
					<div class="leftBlock1 yspace">
						<div class="title">
							<div class="cName">
								友情链接
							</div>
						</div>
						<div class="txt note">
							<ul class="adAutoServer">
								<%
									LinkDb ld = new LinkDb();
									String listsql = ld.getListSql("group", "" + id);
									int totalLink = ld.getObjectCount(listsql, LinkDb
											.getVisualGroupName("group", "" + id));
									com.cloudwebsoft.framework.base.ObjectBlockIterator irlink = ld
											.getObjects(listsql, LinkDb.getVisualGroupName("group", ""
											+ id), 0, totalLink);
									int m = 0;
									while (irlink.hasNext()) {
										ld = (LinkDb) irlink.next();
								%>
								<li class="adAutoServerImg">
									<a target="_blank" href="<%=ld.getUrl()%>"
										title="<%=StrUtil.toHtml(ld.getTitle())%>"> <%
 if (!ld.getImage().equals("")) {
 %> <img src="<%=ld.getImageUrl(request)%>" border="0" width="80" alt="" />
										<%
										} else {
										%> <%=ld.getTitle()%> <%
 }
 %> </a>
									<p class="hackbox"></p>
								</li>
								<%
								}
								%>
							</ul>
							<p class="hackbox"></p>
						</div>
					</div>
				</div>
			</div>
		</div>
		<%@ include file="group_footer.jsp"%>
	</body>
</html>
