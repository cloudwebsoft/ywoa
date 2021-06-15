<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="java.util.*"%>
<%@page import="com.redmoon.forum.person.UserDb"%>
<%
	// 防跨站点请求伪造
	String callingPage = request.getHeader("Referer");
	if (callingPage == null
			|| callingPage.indexOf(request.getServerName()) != -1) {
	} else {
		Privilege privilege = new Privilege();
		com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "CSRF ajax_online.jsp");
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "op_invalid")));
		return;
	}

	OnlineUserDb oud = new OnlineUserDb();
	Vector v = oud.list();
	String op = ParamUtil.get(request, "op");
	if (op.equals("count")) {
		out.print(v.size());
		return;
	}
%>
		<%@ include file="inc/nocache.jsp"%>
		<%
			DeptMgr dm = new DeptMgr();
			DeptUserDb du = new DeptUserDb();

			UserMgr um = new UserMgr();
			Iterator ir = v.iterator();
			int i = 0;
			while (ir.hasNext()) {
				oud = (OnlineUserDb) ir.next();
				com.redmoon.oa.person.UserDb user = um.getUserDb(oud.getName());

				String deptName = "";
				Iterator ir2 = du.getDeptsOfUser(user.getName()).iterator();
				if (ir2.hasNext()) {
					DeptDb dd = (DeptDb) ir2.next();
					/*
					if (!dd.getParentCode().equals(DeptDb.ROOTCODE)) {
						deptName = dm.getDeptDb(dd.getParentCode()).getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dd.getName();
					}
					else
					*/
					deptName = dd.getName();
				}
				i++;
		%>
		<!-- <div onMouseOut="hideDiv('<%=i%>')" onMouseOver="showDiv('<%=i%>')" style="line-height:40px;"> -->
			<div style="line-height:40px;">
				<%
					if (!"".equals(user.getPhoto())) {
				%>
				<img width=32 height=32 src="<%=request.getContextPath()%>/img_show.jsp?path=<%=user.getPhoto()%>"/>
				<%
					} else {
						if (user.getGender() == 0) {
						%>
						<img width=32 src="<%=request.getContextPath()%>/images/man.png"/>
						<%
						} else {
						%>
						<img width=32 src="<%=request.getContextPath()%>/images/woman.png"/>
						<%
						}
					}
				%>
			&nbsp;<%=deptName%>：&nbsp;
			<a title="登录时间：<%=DateUtil.format(oud.getLogTime(), "yyyy-MM-dd HH:mm:ss")%>" href="javascript:void(0);"
				onClick="addTab('<%=user.getRealName()%>', 'user_info.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>')"
				target="mainFrame" ><%=user.getRealName()%></a>&nbsp;&nbsp;
			<span id="<%=i%>" style="display:none">
                <a	href="javascript:void(0);" target="mainFrame">
                <img title="消息" src="<%=SkinMgr.getSkinPath(request)%>/images/message.png" onClick="addTab('消息', '<%=request.getContextPath()%>/message_oa/send.jsp?receiver=<%=StrUtil.UrlEncode(oud.getName())%>')" />
                </a>
			</span>
		</div>
		<%
			}
		%>
