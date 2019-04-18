<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="ltpos" %>
<div class="position">
<%
String pos_boardcode = ParamUtil.get(request, "boardcode");
if (!pos_boardcode.equals("")) {
	Leaf pos_curleaf = new Leaf();
	pos_curleaf = pos_curleaf.getLeaf(pos_boardcode);
	String pos_op = ParamUtil.get(request, "op");
%>
	<span style="float:right">
	<b>&raquo;</b>&nbsp;
	<%
	if (com.redmoon.forum.Privilege.isUserLogin(request)) {
		UserDb user = new UserDb();
		user = user.getUser(com.redmoon.forum.Privilege.getUser(request));
	%>
		<a href="../usercenter.jsp" style="color:#427396"><%=user.getNick()%></a>,&nbsp;
		<ltpos:Label res="res.label.forum.inc.position" key="welcome"/>
		<%=ForumSkin.formatDate(request, user.getLastTime())%>&nbsp;&nbsp;
	<%}else{%>
		<font color="#333333"><ltpos:Label res="res.label.forum.inc.position" key="guest"/></font>&nbsp;&nbsp;&nbsp;&nbsp;<a href="<%=request.getContextPath()%>/door.jsp?privurl=<%=StrUtil.getUrl(request)%>" style="color:#008dd9">[<ltpos:Label res="res.label.forum.inc.position" key="login"/>]</a> | <a style="color:#008dd9; margin-right:10px" href="<%=request.getContextPath()%>/regist.jsp">[<ltpos:Label res="res.label.forum.inc.position" key="regist"/>]</a>
	<%}%>
	</span>
	&nbsp;<img src="<%=request.getContextPath()%>/forum/images/userinfo.gif" width="9" height="9">
	<span id="pos_root" name="pos_root" style="display:none"><%=pos_curleaf.getMenuString(request, pos_curleaf.getLeaf(Leaf.CODE_ROOT))%></span>
	<a  style="color:#008dd5;"><ltpos:Label res="res.label.forum.inc.position" key="cur_position"/></a>&nbsp;<a href="<%=request.getContextPath()%>/forum/index.jsp" style="color:#008dd5" onmouseover="showmenu(event, document.getElementById('pos_root').innerHTML, 0)">
	<ltpos:Label res="res.label.forum.inc.position" key="forum_home"/></a>&nbsp;&nbsp;<B>&raquo;</B>&nbsp;<%
		com.redmoon.forum.Privilege pvgPos = new com.redmoon.forum.Privilege();
		String pCode = pos_curleaf.getParentCode();
		String plink = "";
		while (!pCode.equals(Leaf.CODE_ROOT)) {
			Leaf pleaf = pos_curleaf.getLeaf(pCode);
			// 防止当parentCode出错时，陷入死循环
			if (pleaf==null || !pleaf.isLoaded())
				break;
		%><span id="posspan_<%=pleaf.getCode()%>" name="posspan_<%=pleaf.getCode()%>" style="display:none;color:#427396""><%=pos_curleaf.getMenuString(request, pleaf)%></span>
		<%
			if (pleaf.getType()==pleaf.TYPE_BOARD && pleaf.isDisplay(request, pvgPos)) {
				plink = "<a style='color:#008dd5' href='" + request.getContextPath() + "/forum/" + ForumPage.getListTopicPage(request, pCode) + "' onmousemove=\"showmenu(event, document.getElementById('posspan_" + pleaf.getCode() + "').innerHTML, 0)\">" + pleaf.getName() + "</a>&nbsp;&nbsp;<B>&raquo;</B>&nbsp;" + plink;
			}
			else {
				if (pleaf.getCode().equals(Leaf.CODE_ROOT)) {
					plink = "<a style='color:#008dd5' href='" + request.getContextPath() + "/forum/index.jsp?boardField=" + StrUtil.UrlEncode(pCode) + "' onmousemove=\"showmenu(event, document.getElementById('posspan_" + pleaf.getCode() + "'.innerHTML, 0)\">" + pleaf.getName() + "</a>&nbsp;&nbsp;<B>&raquo;</B>&nbsp;" + plink;
				}
				else {
					plink = "<a style='color:#008dd5' href='" + request.getContextPath() + "/forum/" + ForumPage.getListTopicPage(request, pCode) + "' onmousemove=\"showmenu(event, document.getElementById('posspan_" + pleaf.getCode() + "').innerHTML, 0)\">" + pleaf.getName() + "</a>&nbsp;&nbsp;<B>&raquo;</B>&nbsp;" + plink;
				}
			}
			pCode = pleaf.getParentCode();
	   }
	  %>
	  <%=plink%>&nbsp;<a style="color:#008dd5" href="<%=request.getContextPath()%>/forum/<%=ForumPage.getListTopicPage(request, pos_boardcode)%>"><%=pos_curleaf.getName()%></a>
	  <%if (pos_op.equals("showelite")) {%>
	  <ltpos:Label res="res.label.forum.inc.position" key="elite_field"/>
	  <%}%>
<%
} else if (!com.redmoon.forum.Privilege.isUserLogin(request)) { 
	String posprivurl = request.getRequestURL() + "?" + request.getQueryString();
%>
	<form action="<%=cn.js.fan.web.Global.getRootPath()%>/login.jsp" method=post>
	&nbsp;<img src="<%=cn.js.fan.web.Global.getRootPath()%>/forum/images/userinfo.gif" width="9" height="9">&nbsp;<ltpos:Label res="res.label.forum.inc.position" key="user_name"/>
	<input maxlength=15 size=10 name="name">
	<ltpos:Label res="res.label.forum.inc.position" key="pwd"/>
	<input type=password maxlength=15 size=10 name="pwd">
	<%
	com.redmoon.forum.Config cfg_pos = com.redmoon.forum.Config.getInstance();
	if (cfg_pos.getBooleanProperty("forum.loginUseValidateCode")) {
	%>
	<ltpos:Label res="res.label.forum.inc.position" key="validate_code"/>
	<input name="validateCode" type="text" size="1">
	<img src='<%=request.getContextPath()%>/validatecode.jsp' border="0" align="absmiddle" style="cursor:hand" onclick="this.src='<%=request.getContextPath()%>/validatecode.jsp'" alt="<ltpos:Label res="res.label.forum.index" key="refresh_validatecode"/>" />
	<%}%>
	<select name=covered>
	  <option value=0 selected type='checkbox' checked><ltpos:Label res="res.label.forum.inc.position" key="not_cover"/></option>
	  <option value=1><ltpos:Label res="res.label.forum.inc.position" key="cover"/></option>
	</select>
	<input type='submit' name='Submit' value='<ltpos:Label res="res.label.forum.inc.position" key="commit"/>' class=singleboarder>
	<input type="hidden" name="privurl" value="<%=posprivurl%>">
	</form>
<%}else{
	UserDb user = new UserDb();
	user = user.getUser(com.redmoon.forum.Privilege.getUser(request));
%>
	&nbsp;<img src="<%=cn.js.fan.web.Global.getRootPath()%>/forum/images/userinfo.gif" width="9" height="9">&nbsp;<ltpos:Label res="res.label.forum.inc.position" key="user_name"/>		<%=user.getNick()%>&nbsp;&nbsp;<ltpos:Label res="res.label.forum.inc.position" key="welcome"/><%=user.getLevelDesc()%>&nbsp;&nbsp;<ltpos:Label res="res.label.forum.inc.position" key="exprience"/><%=user.getExperience()%>&nbsp;&nbsp;<ltpos:Label res="res.label.forum.inc.position" key="credit"/><%=user.getCredit()%>&nbsp;&nbsp;
	<%
	ScoreMgr sm_pos = new ScoreMgr();
	ScoreUnit su_pos = sm_pos.getScoreUnit("gold");
	out.print(su_pos.getName());
	%>：<%=user.getGold()%>		
<%}%>
</div>