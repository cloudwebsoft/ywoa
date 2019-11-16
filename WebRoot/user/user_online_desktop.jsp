<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.worklog.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    /*
    if (!privilege.isUserLogin(request)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
    
        return;
    }
    */
    
    int id = ParamUtil.getInt(request, "id");
    UserDesktopSetupDb udsd = new UserDesktopSetupDb();
    udsd = udsd.getUserDesktopSetupDb(id);
    int count = udsd.getCount();
    String kind = ParamUtil.get(request, "kind"); // 是否来自于lte界面
%>
<div id="drag_<%=id%>" dragTitle="<%=udsd.getTitle()%>" count="<%=udsd.getCount()%>"
     wordCount="<%=udsd.getWordCount()%>" class="portlet drag_div bor ibox">
    <div id="drag_<%=id%>_h" class="box ibox-title">
        <!-- <span class="titletxt"><img src="<%=SkinMgr.getSkinPath(request)%>/images/titletype.png" width="8" height="12" /><a href="user/user_online_list.jsp"><%=udsd.getTitle()%></a></span>-->
        <!-- <div class="opbut-1"> <img onclick="mini('<%=udsd.getId()%>')" title="最小化" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/minimization.png" align="absmiddle" width="19" height="19"/></div>-->
        <!-- <div class="opbut-2"><img onclick="mod('<%=udsd.getId()%>')" title="修改显示方式" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/configure.png" align="absmiddle" width="19" height="19"/></div>-->
        <!-- <div class="opbut-3"><img onclick="clo('<%=udsd.getId()%>')" title="关闭" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/close.png" align="absmiddle" width="19" height="19"/></div>-->
        <%
            if ("lte".equals(kind)) {
        %>
        <h5>
            <i class="fa <%=udsd.getIcon()%>"></i>&nbsp;&nbsp;<a
                href="<%=request.getContextPath()%>/user/user_online_list.jsp"><%=udsd.getTitle()%>
        </a>
        </h5>
        <%
        } else {
        %>
        <div class="titleimg">
            <!--<img src="images/desktop/notepaper.png" width="40" height="40" />-->
            <i class="fa <%=udsd.getIcon()%>"></i>
            &nbsp;&nbsp;
        </div>
        <div class="titletxt">&nbsp;&nbsp;<a
                href="<%=request.getContextPath()%>/user/user_online_list.jsp"><%=udsd.getTitle()%>
        </a></div>
        <%
            }
        %>
    </div>
    <div id="drag_<%=udsd.getId()%>_c" class="portlet_content ibox-content">
        <%
            String sql = "select name from users where isValid=1 and name <> 'system' order by online_time desc";
            UserDb user = new UserDb();
            
            String op = ParamUtil.get(request, "op");

// out.print(sql);
            
            UserLevelDb uld = new UserLevelDb();
            ListResult lr = user.listResult(sql, 1, count);
            Vector v = lr.getResult();
            Iterator ir = null;
            if (v != null) {
                ir = v.iterator();
            }
            
            int k = 0;
            com.redmoon.forum.person.UserDb ud = new com.redmoon.forum.person.UserDb();
            if (ir.hasNext()) {
        %>
        <div style="padding-top:5px;">
            <ul>
                <%
                    while (ir.hasNext()) {
                        user = (UserDb) ir.next();
                        k++;
                        
                        if (user.getName().equalsIgnoreCase("system")) {
                            continue;
                        }
                        String onlineImg = StrUtil.getNullStr(uld.getUserLevelDbByLevel(user.getOnlineTime()).getLevelPicPath());
                        
                        ud = ud.getUser(user.getName());
                        String myface = StrUtil.getNullStr(ud.getMyface());
                        String realPic = ud.getRealPic();
                        
                        String imgSrc = "";
                        if (StrUtil.getNullStr(user.getPhoto()).equals("")) {
                            imgSrc = "forum/images/face/";
                            if (StrUtil.getNullStr(user.getPicture()).equals("")) {
                                if (myface.equals("")) {
                                    if (realPic != null && (realPic.toLowerCase().endsWith(".gif") ||
                                            realPic.toLowerCase().endsWith(".png") ||
                                            realPic.toLowerCase().endsWith(".jpg") ||
                                            realPic.toLowerCase().endsWith(".bmp"))) {
                                        imgSrc = imgSrc + realPic;
                                    } else {
                                        imgSrc = imgSrc + "face.gif";
                                    }
                                } else {
                                    imgSrc = ud.getMyfaceUrl(request);
                                }
                            } else {
                                if (user.getPicture().toLowerCase().endsWith(".gif") ||
                                        user.getPicture().toLowerCase().endsWith(".png") ||
                                        user.getPicture().toLowerCase().endsWith(".jpg") ||
                                        user.getPicture().toLowerCase().endsWith(".bmp")) {
                                    imgSrc = imgSrc + user.getPicture();
                                } else {
                                    imgSrc = imgSrc + "face.gif";
                                }
                            }
                        } else {
                            imgSrc = user.getPhoto();
                        }
                %>
                <li style="float:left;width:100%;height:32px;line-height:32px;">
        	<span style="display:block;float:left;width:70%">
                <img src="<%=imgSrc%>" width=30 height=30> 
        		<a title="<%=NumberUtil.round(user.getOnlineTime(), 1)%>小时" target="_blank"
                   href="user_info.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>"><%=user.getRealName()%></a>&nbsp;&nbsp;
        	</span>
                    <span style="padding-top:1px;">
		        <%if (!"".equals(onlineImg)) { %>
		        <img src="<%=request.getContextPath()%>/<%=onlineImg%>"/>
		        <%}%>
	        </span>
                </li>
                <%}%>
            </ul>
        </div>
        <%} else {%>
        <div class='no_content'><img title='暂无在线人数' src='images/desktop/no_content.jpg'></div>
        <%}%>
    </div>
</div>