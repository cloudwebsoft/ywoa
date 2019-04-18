<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import = "com.redmoon.forum.plugin.*" %>
<%@ page import = "java.util.*" %>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.ui.menu.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.message.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.pvg.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String appName = cfg.get("enterprise");
String refresh_message = cfg.get("refresh_message");

String username = privilege.getUser(request);

UserMgr um = new UserMgr();
UserDb user = um.getUserDb(username);
if (user==null || !user.isLoaded()) {
	out.print(StrUtil.Alert_Back("该用户已不存在！"));
	return;
}

String mainTitle = ParamUtil.get(request, "mainTitle");
String mainPage = ParamUtil.get(request, "mainPage");
if ("".equals(mainTitle)) {
	mainTitle = "桌面";
	mainPage = "../desktop.jsp";
}
else {
	mainPage = "../" + mainPage;
}
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="renderer" content="webkit">
    <title><%=appName%></title>

    <!--[if lt IE 9]>
    <meta http-equiv="refresh" content="0;ie.html" />
    <![endif]-->

	<link href="../images/favicon.ico" rel="shortcut icon" />    
	<link href="css/bootstrap.min.css?v=3.3.6" rel="stylesheet">
    <link href="css/font-awesome.min.css?v=4.4.0" rel="stylesheet">
    <link href="css/animate.css" rel="stylesheet">
    <link href="css/style.css?v=4.1.0" rel="stylesheet">
	<script src="../inc/common.js"></script>
    <script src="js/jquery.min.js?v=2.1.4"></script>	
	<script src='<%=request.getContextPath()%>/dwr/interface/MessageDb.js'></script>
	<script src='<%=request.getContextPath()%>/dwr/engine.js'></script>
	<script src='<%=request.getContextPath()%>/dwr/util.js'></script>	
</head>
<body class="fixed-sidebar full-height-layout gray-bg" style="overflow:hidden">
    <div id="wrapper">
<%
String navDis = "";
if (!privilege.isUserPrivValid(request, "admin") && privilege.isUserPrivValid(request, "menu.main.forbid")) {
	navDis = "display:none";
	%>
	<script>
	$(function() {
		$('#page-wrapper').css("margin-left", "0px");
	});
	</script>
	<%
}
%>    
        <!--左侧导航开始-->
        <nav class="navbar-default navbar-static-side" style="<%=navDis %>" role="navigation">
            <div class="nav-close"><i class="fa fa-times-circle"></i>
            </div>
            <div class="sidebar-collapse">
                <ul class="nav" id="side-menu">
                    <li class="nav-header">
                        <div class="dropdown profile-element">
                            <span>
                            <img class="img-circle" src="<%=request.getContextPath()%>/<%=UserSetupMgr.getPortrait(user)%>" />                          
                            </span>
                            <a data-toggle="dropdown" class="dropdown-toggle" href="#">
                                <span class="clear">
                               <span class="block m-t-xs"><strong class="font-bold"><%=user.getRealName()%></strong></span>
                                <span class="text-muted text-xs block">个人信息<b class="caret"></b></span>
                                </span>
                            </a>
                            <ul class="dropdown-menu animated fadeInRight m-t-xs">
                                <li><a class="J_menuItem" href="../user/user_edit.jsp">个人资料</a>
                                </li>
                                <li><a class="J_menuItem" href="../message_oa/message_frame.jsp">内部邮箱</a>
                                </li>
                                <li class="divider"></li>
                                <li><a href="../exit_oa.jsp">安全退出</a>
                                </li>
                            </ul>
                        </div>
                        <div class="logo-element"><img src="../images/oa_logo.png"/>
                        </div>
                    </li>
					<%
					String item = ParamUtil.get(request, "item").equals("") ? "all" : ParamUtil.get(request, "item");
                    int x=0;
                    com.redmoon.oa.ui.menu.LeafChildrenCacheMgr lccm = new com.redmoon.oa.ui.menu.LeafChildrenCacheMgr(Leaf.CODE_ROOT);
                    Iterator ir = lccm.getChildren().iterator();
                    int k=2;
                    while (ir.hasNext()) {
                        com.redmoon.oa.ui.menu.Leaf lf = (com.redmoon.oa.ui.menu.Leaf)ir.next();
                        if(!item.equals("all")){
                            if (!item.equals(lf.getCode()))
                                continue;
                        }
                        if (!lf.canUserSee(request) || lf.getCode().equals(Leaf.CODE_BOTTOM) || !lf.isUse())
                            continue;
                        String faIcon = "fa-columns";
                        if (!"".equals(lf.getFontIcon())) {
                        	faIcon = lf.getFontIcon();
                        }
                        LeafChildrenCacheMgr lccm2 = new LeafChildrenCacheMgr(lf.getCode());
                        Vector v2 = lccm2.getChildren();
                    %>
                    	<li>
                    		<%
                    		String link = lf.getLink(request);
                    		if (!link.equals("")) { %>
                        	<a class="J_menuItem" href="../<%=link %>"><i class="fa <%=faIcon %>"></i> <span class="nav-label"><%=lf.getName(request)%></span></a>
                        	<%} else {%>
                    	    <a href="#">
                            	<i class="fa <%=faIcon %>"></i>
                            	<span class="nav-label"><%=lf.getName(request)%></span>
                            	<%if (v2.size()>0) { %>
                            	<span class="fa arrow"></span>
                            	<%} %>
                        	</a>
                        	<%}%>
                        	<%
                        	if (v2.size()>0) { 
                        	%>
	                        <ul class="nav nav-second-level">
							<%                        	
		                        Iterator ir2 = v2.iterator();
		                        while (ir2.hasNext()) {
		                            Leaf lf2 = (Leaf)ir2.next();
		                            if (!lf2.canUserSee(request) || !lf2.isUse())
		                                continue;                        	
                        	%>
	                            <li>
	                            <%
	                            String link2 = lf2.getLink(request);
	                            LeafChildrenCacheMgr lccm3 = new LeafChildrenCacheMgr(lf2.getCode());
	                            Vector v3 = lccm3.getChildren();	 
	                            if (v3.size()==0) {                       
	                            %>
	                                <a class="J_menuItem" href="../<%=link2 %>"><%=lf2.getName(request) %></a>
								<%} else {%>
	                                <a href="#"><%=lf2.getName(request) %> <span class="fa arrow"></span></a>
	                                <ul class="nav nav-third-level">
	                                <%
	                                Iterator ir3 = v3.iterator();
	                                while (ir3.hasNext()) {
	                                    Leaf lf3 = (Leaf)ir3.next();
	                                    if (!lf3.canUserSee(request) || !lf3.isUse())
	                                        continue;	                                
	                                %>
	                                    <li><a class="J_menuItem" href="../<%=lf3.getLink(request) %>"><%=lf3.getName(request) %></a>
	                                    </li>
	                                <%} %>
	                                </ul>
	                            <%} %>
	                            </li>	                            
	                        <%	}%>
	                        </ul>	                        
	                        <%}%>                 	
                        </li>
                    <%}%>
                </ul>
            </div>
        </nav>
        <!--左侧导航结束-->
        <!--右侧部分开始-->
        <div id="page-wrapper" class="gray-bg dashbard-1">
            <div class="row border-bottom">
                <nav class="navbar navbar-static-top" role="navigation" style="margin-bottom: 0">
                    <div class="navbar-header"><a class="navbar-minimalize minimalize-styl-2 btn btn-primary " href="#"><i class="fa fa-bars"></i> </a>
                        <form role="search" class="navbar-form-custom" method="post" action="search_results.html">
                            <div class="form-group">
                            	<%=appName %>
                            </div>
                        </form>
                    </div>
                    <ul class="nav navbar-top-links navbar-right">
						<%
                            PortalDb pd = new PortalDb();
                            String sql = "select id from " + pd.getTable().getName() + " where (user_name='system' or user_name=?) order by is_fixed desc, orders asc";
				    		boolean isDefaultDeskForbid = false;
				    		if (!privilege.isUserPrivValid(request, "admin") && privilege.isUserPrivValid(request, "desk.default.forbid")) {
				    			isDefaultDeskForbid = true;
				            	sql = "select id from " + pd.getTable().getName() + " where (user_name='system' or user_name=?) and orders<>1 order by is_fixed desc, orders asc";    		
							}                               
                            int desktopIndex = 1;
                            Iterator pdir = pd.list(sql, new Object[]{privilege.getUser(request)}).iterator();
                            while (pdir.hasNext()) {
                                pd = (PortalDb)pdir.next();
                                boolean canSee = true;
                                if (pd.getString("user_name").equals("system")) {
                                    canSee = pd.canUserSee(privilege.getUser(request));
                                }
                                String desktopName = pd.getString("name");
                                String desktopUrl = "../desktop.jsp";
                                // 如果是排在第1位的门户，则其页面为desktop.jsp，无需带参数，以免重复打开
                                if (isDefaultDeskForbid && desktopIndex==1) {
                                	desktopName = "桌面";
                                }
                                else {
                                	if (desktopIndex!=1) {
                                		desktopUrl += "?portalId=" + pd.getLong("id");
                                	}
                                	else {
                                		desktopName = "桌面";
                                	}
                                }
                                desktopIndex ++;
                                if (canSee) {
                        %>
                        		<li class="hidden-xs">
                                <a href="<%=desktopUrl %>" class="J_menuItem" data-index="0">
                                <i class="fa fa-tv"></i>
                                <%=desktopName%>
                                </a>
                            	</li>      
                        <%
                                }
                        }%>    
                        <li class="dropdown" title="消息">
                            <a class="dropdown-toggle count-info" style="padding-left:5px; padding-right:5px" data-toggle="dropdown" href="#">
                                <i class="fa fa-envelope"></i><span class="label label-warning"></span>
                            </a>
                            <ul class="dropdown-menu dropdown-messages">
                            </ul>
                        </li>
                        <li class="hidden-xs" title="控制面板">
                            <a href="../user/control_panel.jsp" style="padding-left:5px; padding-right:5px" class="J_menuItem" data-index="0" data-name="控制面板"><i class="fa fa-gear fa-cog"></i></a>
                        </li>
                        <li class="hidden-xs" title="刷新">
                            <a href="javascript:;" style="padding-left:5px; padding-right:5px" onclick="refreshTabFrame()"><i class="fa fa-refresh fa-cog"></i></a>
                        </li>                        
                        <li class="dropdown hidden-xs">
                            <a class="right-sidebar-toggle" aria-expanded="false">
                                <i class="fa fa-tasks"></i> 主题
                            </a>
                        </li>
                    </ul>
                </nav>
            </div>
            <div class="row content-tabs">
                <button class="roll-nav roll-left J_tabLeft"><i class="fa fa-backward"></i>
                </button>
                <nav class="page-tabs J_menuTabs">
                    <div class="page-tabs-content">
                        <a href="javascript:;" class="active J_menuTab" data-id="<%=mainPage%>"><%=mainTitle %></a>
                    </div>
                </nav>
                <button class="roll-nav roll-right J_tabRight"><i class="fa fa-forward"></i>
                </button>
                <div class="btn-group roll-nav roll-right">
                    <button class="dropdown J_tabClose" data-toggle="dropdown">关闭操作<span class="caret"></span>
                    </button>
                    <ul role="menu" class="dropdown-menu dropdown-menu-right">
                        <li class="J_tabShowActive"><a>定位当前选项卡</a>
                        </li>
                        <li class="divider"></li>
                        <li class="J_tabCloseAll"><a>关闭全部选项卡</a>
                        </li>
                        <li class="J_tabCloseOther"><a>关闭其他选项卡</a>
                        </li>
                    </ul>
                </div>
                <a href="../exit_oa.jsp" class="roll-nav roll-right J_tabExit"><i class="fa fa fa-sign-out"></i> 退出</a>
            </div>
            <div class="row J_mainContent" id="content-main">
                <iframe class="J_iframe" name="iframe0" width="100%" height="100%" src="<%=mainPage %>" frameborder="0" data-id="<%=mainPage %>" seamless></iframe>
            </div>
            <div class="footer">
                <div class="pull-right">&copy; 2014-2015 <a href="http://www.yimihome.com/" target="_blank">cloud web soft</a>
                </div>
            </div>
        </div>
        <!--右侧部分结束-->
        <!--右侧边栏开始-->
        <div id="right-sidebar">
            <div class="sidebar-container">

                <ul class="nav nav-tabs navs-3">
                    <li class="active">
                        <a data-toggle="tab" href="#tab-1">
                            <i class="fa fa-gear"></i> 主题
                        </a>
                    </li>
                </ul>

                <div class="tab-content">
                    <div id="tab-1" class="tab-pane active">
                        <div class="sidebar-title">
                            <h3> <i class="fa fa-comments-o"></i> 主题设置</h3>
                            <small><i class="fa fa-tim"></i> 请选择和预览主题的布局和样式，这些设置会被保存在本地，下次打开的时候会直接应用这些设置。</small>
                        </div>
                        <div class="skin-setttings">
                            <div class="title">主题设置</div>
                            <%if (!privilege.isUserPrivValid(request, "menu.main.forbid")) {%>
                            <div class="setings-item">
                                <span>收起左侧菜单</span>
                                <div class="switch">
                                    <div class="onoffswitch">
                                        <input type="checkbox" name="collapsemenu" class="onoffswitch-checkbox" id="collapsemenu">
                                        <label class="onoffswitch-label" for="collapsemenu">
                                            <span class="onoffswitch-inner"></span>
                                            <span class="onoffswitch-switch"></span>
                                        </label>
                                    </div>
                                </div>
                            </div>
                            <div class="setings-item">
                                <span>固定顶部</span>
                                <div class="switch">
                                    <div class="onoffswitch">
                                        <input type="checkbox" name="fixednavbar" class="onoffswitch-checkbox" id="fixednavbar">
                                        <label class="onoffswitch-label" for="fixednavbar">
                                            <span class="onoffswitch-inner"></span>
                                            <span class="onoffswitch-switch"></span>
                                        </label>
                                    </div>
                                </div>
                            </div>
                            <%} %>
                            <div class="setings-item">
                                <span>
                        		固定宽度
                    			</span>
                                <div class="switch">
                                    <div class="onoffswitch">
                                        <input type="checkbox" name="boxedlayout" class="onoffswitch-checkbox" id="boxedlayout">
                                        <label class="onoffswitch-label" for="boxedlayout">
                                            <span class="onoffswitch-inner"></span>
                                            <span class="onoffswitch-switch"></span>
                                        </label>
                                    </div>
                                </div>
                            </div>
                            <div class="title">皮肤选择</div>
                            <div class="setings-item default-skin nb">
                                <span class="skin-name ">
                         <a href="#" class="s-skin-0">
                             默认皮肤
                         </a>
                    </span>
                            </div>
                            <div class="setings-item blue-skin nb">
                                <span class="skin-name ">
                        <a href="#" class="s-skin-1">
                            蓝色主题
                        </a>
                    </span>
                            </div>
                            <div class="setings-item yellow-skin nb">
                                <span class="skin-name ">
                        <a href="#" class="s-skin-3">
                            黄色/紫色主题
                        </a>
                    </span>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
        </div>
        <!--右侧边栏结束-->
    </div>

    <!-- 全局js -->
    <script src="js/bootstrap.min.js?v=3.3.6"></script>
    <script src="js/plugins/metisMenu/jquery.metisMenu.js"></script>
    <script src="js/plugins/slimscroll/jquery.slimscroll.min.js"></script>
    <script src="js/plugins/layer/layer.min.js"></script>

    <!-- 自定义js -->
    <script src="js/hplus.js?v=4.1.0"></script>
    <script type="text/javascript" src="js/contabs.js"></script>

    <!-- 第三方插件 -->
    <script src="js/plugins/pace/pace.min.js"></script>

    <!-- Gritter -->
    <link href="js/plugins/gritter/jquery.gritter.css" rel="stylesheet">
    <script src="js/plugins/gritter/jquery.gritter.min.js"></script>

</body>
<script>
	function handler(msg) {
		// alert("您与服务器的连接已断开，请刷新页面尝试重新连接！");
	}
	DWREngine.setErrorHandler(handler);
	DWREngine.setTimeout(2000);
	
	var userName = "<%=privilege.getUser(request)%>";
	var refresh_message = <%=refresh_message%>;
	
	function refreshMsg() {
		getNewMsg(userName);
		timeoutid = window.setTimeout("refreshMsg()", refresh_message * 1000); // 每隔N秒钟刷新一次
	}
	
	function getNewMsg(userName) {
	  try {
	  	MessageDb.getNewMsgsOfUser(showMsgWin, userName);
	  }
	  catch (e) {
	  	alert(e);
	  }
	}
	  
	var unique_id;
	
	function showMsgWin(msg) {
	  var str = "";
	  if (msg.length>0) {
	  	  var i = 0;
		  for (var data in msg) {
		  	i++;
			var id = msg[data].id
			var title = msg[data].title;
			var sender = msg[data].sender;
			var senderPortrait = msg[data].senderPortrait;
			var type = msg[data].type;
			
			if (type==<%=MessageDb.TYPE_SYSTEM%>) {
	        str += "<div class=\"dropdown-messages-box\" onclick=\"addTab('" + title + "', '<%=request.getContextPath()%>/message_oa/message_ext/sys_showmsg.jsp?id=" + id + "')\">";
			}
			else {
	        str += "<div class=\"dropdown-messages-box\" onclick=\"addTab('" + title + "', '<%=request.getContextPath()%>/message_oa/message_frame.jsp?id=" + id + "')\">";
	        }
	        str += '     <a href="profile.html" class="pull-left">';
	        str += '         <img alt="image" class="img-circle" src="../' + senderPortrait + '">';
	        str += '     </a>';
	        str += "     <div class=\"media-body\">";
	        str += '         <small class="pull-right"></small>';
	        str += '         <strong>' + msg[data].senderRealName + '</strong> ' + title;
	        str += '         <br>';
	        str += '         ' + msg[data].summary + '<br>';	        
	        str += '         <small class="text-muted">' + msg[data].rq.substring(5) + '</small>';
	        str += '     </div>';
	        str += '</div>';				
	        str += '<li class="divider"></li>';
			
			// 最多取5条
			if (i>=5)
				break;
		  }
		}
		else {
	        str += '<li>';
	        str += '    <div class="text-center link-block">';
	        str += '            暂无新消息';
	        str += '    </div>';
	        str += '</li>';				
			str += '<li class="divider"></li>';
		}
		
        str += '<li>';
        str += '    <div class="text-center link-block">';
        str += "        <a class=\"J_menuItem\" onclick=\"addTab('邮件', '<%=request.getContextPath()%>/message_oa/message_frame.jsp')\">";
        str += '            <i class="fa fa-envelope"></i> <strong> 查看所有邮件</strong>';
        str += '        </a>';
        str += '    </div>';
        str += '</li>';
		str += '<li class="divider"></li>';		
        str += '<li>';
        str += '    <div class="text-center link-block">';
        str += "        <a class=\"J_menuItem\" onclick=\"addTab('消息', '<%=request.getContextPath()%>/message_oa/message_ext/sys_message.jsp')\">";
        str += '            <i class="fa fa-bell"></i> <strong> 查看所有消息</strong>';
        str += '        </a>';
        str += '    </div>';
        str += '</li>';	        
	  	$('.dropdown-messages').html(str);
	  			
		if (i>0) {
			$('.label-warning').html(i);
			
            unique_id = $.gritter.add({
                title: '您有' + i + '条未读信息',
                text: "请前往<a href=\"javascript:;\" onclick=\"addTab('邮件', '<%=request.getContextPath()%>/message_oa/message_frame.jsp')\" class=\"text-warning\">收件箱</a>或者<a href=\"javascript:;\" onclick=\"addTab('邮件', '<%=request.getContextPath()%>/message_oa/message_ext/sys_message.jsp')\" class=\"text-warning\">消息中心</a>查看信息&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"setAllReaded()\" class=\"text-warning\">我知道了</a>",
                time: 10000
            });			
		}

	}	
	
	function setAllReaded() {
		$.ajax({
			url: "../public/message/setAllReaded.do",
			type: "post",
			data: {
				
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest) {
			},
			success: function(data, status) {
				$('.label-warning').html('');			
				$.gritter.remove(unique_id, { 
					fade: true,
					speed: 'fast'
				});
			},
			complete: function(XMLHttpRequest, status){
			},
			error: function(XMLHttpRequest, textStatus){
			}
		});			
	}
	
	$(function() {
		refreshMsg();	
	});	
</script>
</html>
