<%@ page contentType="text/html;charset=utf-8"%><%@ page import="cn.js.fan.util.*"%><%@ page import="cn.js.fan.web.*"%><%@ page import="java.util.*"%><%@ page import="com.redmoon.forum.ad.*"%><%@ page import="com.redmoon.forum.security.*"%><%@ page import="com.redmoon.forum.*"%><%@ page import="com.redmoon.forum.Leaf"%><%@ taglib uri="/WEB-INF/tlds/AdTag.tld" prefix="ad_header"%><%
String hUrl = StrUtil.getUrl(request);
// 进入info.jsp页面时不检测刷新
if (hUrl.indexOf("/info.jsp")==-1) {
	if (!ActionMonitor.canVisit(request, response)) {
		String fastStr = SkinUtil.LoadString(request, "res.label.forum.inc.header", "visit_too_fast");
		fastStr = StrUtil.format(fastStr, new Object[] {new Integer(com.redmoon.forum.Config.getInstance().getVisitInterval()/1000)});
		out.print(SkinUtil.makeErrMsg(request, fastStr));
		return;
	}
}

if (ForumDb.getInstance().getStatus()==ForumDb.STATUS_STOP) {
	// 防止进入info.jsp页面时反复redirect
	if (hUrl.indexOf("/info.jsp")==-1) {
		response.sendRedirect(request.getContextPath() + "/info.jsp?info=" + cn.js.fan.util.StrUtil.UrlEncode(ForumDb.getInstance().getReason()));
		return;
	}
}

if (!com.redmoon.forum.Privilege.isUserLogin(request)) {
	// 防止进入info.jsp页面时反复redirect
	if (hUrl.indexOf("/info.jsp")==-1) {
		if (!ForumDb.getInstance().canGuestEnterIntoForum()) {
			response.sendRedirect(request.getContextPath() + "/info.jsp?op=login&info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "info_please_login")));
			return;
		}
	}
}
%>
<script language="JavaScript">
function hopenWin(url,width,height) {
  var newwin = window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,top=50,left=120,width="+width+",height="+height);
}
</script>
<%
com.redmoon.forum.security.IPMonitor ipm = new com.redmoon.forum.security.IPMonitor();
String rootpath = request.getContextPath(); // 当运用于hopenWin中时，rootpath会为空字符串
if (!ipm.isIPCanVisit(request.getRemoteAddr())) {
	// 防止进入info.jsp页面时反复redirect
	if (hUrl.indexOf("/info.jsp")==-1) {
		response.sendRedirect(rootpath + "/info.jsp?info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "res.label.forum.inc.header", "ipvisitlist")));
		return;
	}
}

TimeConfig tc = new TimeConfig();
if (tc.isVisitForbidden(request)) {
	if (hUrl.indexOf("/info.jsp")==-1) {
		String str = SkinUtil.LoadString(request, "res.label.forum.inc.header", "time_visit_forbid");
		str = StrUtil.format(str, new Object[] {tc.getProperty("forbidVisitTime1"), tc.getProperty("forbidVisitTime2")});
		response.sendRedirect(rootpath + "/info.jsp?info=" + StrUtil.UrlEncode(str));
		return;
	}
}

// String rootpath = Global.getRootPath();	// "http://" + Global.server + ":" + Global.port + "/" + Global.virtualPath; // "http://www.zjrj.cn";
String hboardcode,hboardname;
hboardcode = ParamUtil.get(request, "boardcode");
hboardname = ParamUtil.get(request, "boardname");
if (hboardcode==null)
	hboardcode = "";
if (hboardname == null)
	hboardname = "";
hboardname = cn.js.fan.util.StrUtil.UrlEncode(hboardname);

String hSessionBoardCode = UserSession.getBoardCode(request);
com.redmoon.forum.ui.ThemeMgr thm = new com.redmoon.forum.ui.ThemeMgr();
com.redmoon.forum.ui.Theme hTheme = thm.getTheme("default");
if (!hSessionBoardCode.equals("")) {
	Leaf hleaf = new Leaf();
	hleaf = hleaf.getLeaf(hSessionBoardCode);
	String theme = "";
	if (hleaf!=null)
		theme = hleaf.getTheme();
	if (theme==null || theme.equals(""))
		;
	else {
		com.redmoon.forum.ui.Theme t = thm.getTheme(theme);
		if (t!=null) {
			hTheme = t;
		}
	}
}

String skinPathHeader = com.redmoon.forum.ui.SkinMgr.getSkinPath(request);
%>
<script language="JavaScript" src="<%=rootpath%>/forum/inc/main.js"></script>

<div id="header">
  <div id="banner">
    <div id="logo"><img src="<%=request.getContextPath()%>/forum/<%=skinPathHeader%>/images/logo.gif" /></div>
    <!-- <div id="adBanner"><ad_header:AdTag type="<%=AdDb.TYPE_HEADER%>" boardCode="<%=hSessionBoardCode%>"></ad_header:AdTag></div> -->
  </div>
  <div id="topNav">
  	<span id="navLeft">
		<div class="suckertreemenu">
		<ul id="treemenu">
		<%
		com.redmoon.forum.ui.menu.LeafChildrenCacheMgr dlcmHe = new com.redmoon.forum.ui.menu.LeafChildrenCacheMgr("root");
		java.util.Vector vtHe = dlcmHe.getChildren();
		Iterator irHe = vtHe.iterator();
		int headCt = 0;
		int navpos = vtHe.size() - 2;
		int lastpos = vtHe.size() - 1;
		while (irHe.hasNext()) {
			com.redmoon.forum.ui.menu.Leaf menuleaf = (com.redmoon.forum.ui.menu.Leaf) irHe.next();
			out.print("<li>");
			String menuItem = "";
			if (menuleaf.getType()==com.redmoon.forum.ui.menu.Leaf.TYPE_LINK) {
		%>
				<a target="<%=menuleaf.getTarget()%>" href="<%=menuleaf.getLink(request)%>" style="width:<%=menuleaf.getWidth()%>px;"><%=menuleaf.getName(request)%></a>
		<%	}
			else {
				menuItem = com.redmoon.forum.ui.menu.PresetLeaf.getMenuItem(request, menuleaf);
				out.print(menuItem);
			}
			com.redmoon.forum.ui.menu.LeafChildrenCacheMgr dl = new com.redmoon.forum.ui.menu.LeafChildrenCacheMgr(menuleaf.getCode());
			java.util.Vector headv = dl.getChildren();
			Iterator headir1 = headv.iterator();
			if (headv.size()>0)
				out.print("<ul>");
			while (headir1.hasNext()) {
				com.redmoon.forum.ui.menu.Leaf menulf = (com.redmoon.forum.ui.menu.Leaf) headir1.next();
		%>
				<li><a target="<%=menulf.getTarget()%>" href="<%=menulf.getLink(request)%>" style="width:<%=menulf.getWidth()%>px"><%=menulf.getName(request)%></a>
				<%
				com.redmoon.forum.ui.menu.LeafChildrenCacheMgr dl2 = new com.redmoon.forum.ui.menu.LeafChildrenCacheMgr(menulf.getCode());
				java.util.Vector v2 = dl2.getChildren();
				Iterator ir2 = v2.iterator();
				if (v2.size()>0)
					out.print("<ul>");
				while (ir2.hasNext()) {
					com.redmoon.forum.ui.menu.Leaf menulf2 = (com.redmoon.forum.ui.menu.Leaf) ir2.next();
					%>
					<li><a target="<%=menulf2.getTarget()%>" href="<%=menulf2.getLink(request)%>" style="width:<%=menulf2.getWidth()%>px"><%=menulf2.getName(request)%></a></li>
					<%
				}
				if (v2.size()>0)
					out.print("</ul>");
				out.print("</li>");
			}
			if (headv.size()>0)
				out.print("</ul>");
			
			out.print("</li>");
			
			// 最后一项不写分隔线
			if (headCt!=lastpos) {
				if (menuleaf.getType()==com.redmoon.forum.ui.menu.Leaf.TYPE_PRESET && menuItem.equals(""))
					;
				else
					out.print("<li class='menuDiv'></li>");
			}
			
			// 在倒数第二项写插件的菜单项
			if (headCt==navpos) {
				com.redmoon.forum.plugin.PluginMgr headerPm = new com.redmoon.forum.plugin.PluginMgr();
				Iterator irHeaderPlugin = headerPm.getAllPlugin().iterator();
				while (irHeaderPlugin.hasNext()) {
					com.redmoon.forum.plugin.PluginUnit pu = (com.redmoon.forum.plugin.PluginUnit)irHeaderPlugin.next();
					com.redmoon.forum.plugin.base.IPluginUI ipu = pu.getUI(request, response, out);
					com.redmoon.forum.plugin.base.IPluginViewCommon pvc = ipu.getViewCommon();
					if (pvc!=null) {
						String hnavstr = pvc.render(com.redmoon.forum.plugin.base.IPluginViewCommon.POS_NAV_BAR);
						if (!hnavstr.equals("")) {
							out.print("<li>" + hnavstr + "</li>");
							out.print("<li class='menuDiv'></li>");
						}
					}
				}	
			}
			
			headCt++;
		}
		%>
        </ul>
       </div>
  	</span>
	<span id="navRight"><!--<a href="index.jsp?isSideBar=y">右栏</a>--></span>
</div>
<script language="javascript" src="<%=request.getContextPath()%>/forum/inc/forum_common.js"></script>
<iframe width=0 height=0 src="" id="hiddenframe" style="display:none"></iframe>
</div>
<div class=menuskin id=popmenu onMouseOver="clearhidemenu();highlightmenu(event,'on')" onmouseout="highlightmenu(event,'off');dynamichide(event)" style="Z-index:100"></div>
