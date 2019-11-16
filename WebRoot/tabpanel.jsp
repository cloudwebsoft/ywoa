<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.message.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.menu.*"%>
<%@ page import ="com.redmoon.forum.plugin.*" %>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.pvg.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String skincode = UserSet.getSkin(request);
String mainTitle = ParamUtil.get(request, "mainTitle");
String mainPage = ParamUtil.get(request, "mainPage");
String item = ParamUtil.get(request, "item").equals("") ? "all" : ParamUtil.get(request, "item");
if (mainTitle.equals(""))
	mainTitle = "桌面";
	
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "mainTitle", mainTitle, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "mainPage", mainPage, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;	
}
	
if (mainPage.equals("")) {
	UserSetupDb usd = new UserSetupDb();
	usd = usd.getUserSetupDb(privilege.getUser(request));
	if (usd.getUiMode()==UserSetupDb.UI_MODE_FASHION) {
		mainPage = "desktop_frame.jsp";
	} else {
		mainPage = "desktop.jsp";
	}
}
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String firstUseDate = cfg.get("firstUseDate");
if (firstUseDate != null && firstUseDate.equals(""))
{
    cfg.put("firstUseDate",DateUtil.format(new java.util.Date(),"yyyy-MM-dd"));
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="renderer" content="ie-stand" />
<meta http-equiv="X-UA-Compatible"content="IE=9; IE=8; IE=7; IE=EDGE" />
<%@ include file="inc/nocache.jsp" %>
<title>选项卡</title>
<style>
html, body {
	width : 100%;
	height : 100%;
	padding : 0;
	margin : 0;
	overflow: hidden;
}

.transparent {
	filter:alpha(opacity=92); /* for IE4 - IE7 */
	-ms-filter: "progid:DXImageTransform.Microsoft.Alpha(Opacity=90)"; /* IE8 */
	-moz-opacity:0.9;
	-khtml-opacity: 0.9;
	opacity: 0.9;
}
</style>
<link href="<%=SkinMgr.getSkinPath(request)%>/css.css" rel="stylesheet" type="text/css"/>
<link href="<%=SkinMgr.getSkinPath(request)%>/TabPanel.css" rel="stylesheet" type="text/css"/>
<link href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" rel="stylesheet" type="text/css"/>
<link rel="stylesheet" type="text/css" href="js/flexdropdown/flexdropdown.css" />
<link rel="stylesheet" type="text/css" href="skin/common/sub_menu.css" />
<script type="text/javascript" src="inc/common.js"></script>
<script type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/jquery-ui/jquery-ui.js"></script>
<script type="text/javascript" src="js/jquery.form.js"></script>
<script type="text/javascript" src="js/tabpanel/TabPanel.js"></script>
<script type="text/javascript" src="js/tabpanel/Fader.js"></script>
<script type="text/javascript" src="js/tabpanel/Math.uuid.js"></script>
<script type="text/javascript" src="js/jquery.toaster.js"></script>
<script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script type="text/javascript">
var tabpanel;
jQuery(document).ready(function(){
	tabpanel = new TabPanel({
		//renderTo:'tab',
		widthResizable:true,
		heightResizable:true,
		autoResizable:true,
		//width:document.documentElement.clientWidth,
		width:'100%',
		height:document.documentElement.clientHeight,
		active : 0,
		items : [{
			id:'toolbarPlugin',
			title:'<%=mainTitle%>',
			//html:'<iframe id="ifrmMain" src="./toolbar.html" width="100%" frameborder="0" onload="this.height=100;"></iframe>',
			// html:'<iframe name="ifrmMain" src="desktop.jsp" width="100%" frameborder="0" onload="this.height = document.body.clientHeight-28"></iframe>',			
			html:'<iframe name="ifrmMain" src="<%=mainPage%>" width="100%" frameborder="0" height="100%"></iframe>',			
			closable: true
		}]
	});
	
	jQuery('#msgForm').submit(function() {
		var options = {
			success:	showResponse,  // post-submit callback 
			error: 		showError,
			dataType:  	'html'   // 'xml', 'script', or 'json' (expected server response type)  表单为multipart/form-data即上传文件时，json无法解析
		};
		$(this).ajaxSubmit(options);
		return false;
	});	
});

function addTab(tabTitle, url, id){
    if (tabpanel.tabs)
    {
		var tabs = tabpanel.tabs;
		if (tabs.length>=10 && !id) {
			// 自动关闭第一个选项卡
			tabpanel.kill(1);
			// alert("选项卡不能超出10个，请关闭多余的选项卡！");
			// return;
		}
	}
	
	// tabpanel.addTab({title:'无限添加', html:'<iframe src="" width="100%" height="100%" frameborder="0"></iframe>'});
	if (!id){
		tabpanel.addTab({title:tabTitle, html:'<iframe name="ifrmMain" src=' + url + ' width="100%" height="100%" frameborder="0"></iframe>'});
	}else{ // 存在相同ID，则激活
		tabpanel.addTab({id:id, title:tabTitle, html:'<iframe name="ifrmMain" src=' + url + ' width="100%" height="100%" frameborder="0"></iframe>'});	
	}
	
	var allWindows = top.mainFrame.document.getElementsByName('ifrmMain');
	for(var i = 0;i < allWindows.length; i++) { 
		var myDocument = allWindows[i].document || allWindows[i].contentDocument;
		allWindows[i].onload = function(){
			//获取光标
		    //myDocument.body.focus();
			//禁止后退键 作用于Firefox、Opera  
			try{
				myDocument.onkeypress=top.mainFrame.banBackSpace;  
				//禁止后退键  作用于IE、Chrome  
				myDocument.onkeydown=top.mainFrame.banBackSpace;
			}catch(err){
			
			}
		}
	}
}

function removeIntrTab(position){
	tabpanel.kill(position);
}

function setActiveTabTitle(title) {
	tabpanel.setActiveTabTitle(title);
}

function getActiveTab() {
	return tabpanel.getActiveTab();
}

// 重新载入
function reloadTab(title) {
	tabpanel.reloadTab(title);
}

function reloadTabById(tabId) {
	tabpanel.reloadTabById(tabId);
}

function closeTabById(tabId) {
	var tabs = tabpanel.tabs;
	for (var i=0; i<tabs.length; i++) {
		if (tabs[i].id==tabId) {
			tabpanel.kill(tabId);
		}
	}
}

function getAndShowDesktopTab() {
	var tabs = tabpanel.tabs;
	for (var i=0; i<tabs.length; i++) {
		if (tabs[i].title.text()=="桌面") {
			tabpanel.show(tabs[i].id, false);
			return tabs[i];
		}
	}
	return null;
}

function showTab(pos, notExecuteMoveSee) {
	tabpanel.show(pos, notExecuteMoveSee);
}

function closeTab(tabName) {
	var tabs = tabpanel.tabs;
	for (var i=0; i<tabs.length; i++) {
		if (tabs[i].title.text()==tabName) {
			tabpanel.kill(tabs[i].id);
		}
	}	
}

function showResponse(data) {
	jQuery("#dialog").dialog("close");
}

function showError(pRequest, pStatus, pErrorText) {
	alert('pStatus='+pStatus+'\r\n\r\n'+'pErrorText='+pErrorText);
}

function exitOa()
{
    //jConfirm('您确定要退出么？',"提示",function(r){
    //	if(!r){
    //		return;
    //	}else{
    //   		window.top.location.href = "exit_oa.jsp?skincode=<%=skincode %>";
    //   }
    //});

    if (!confirm("您确定要退出么？")) {
		return;
	}
    window.top.location.href = "exit_oa.jsp?skincode=<%=skincode %>";
}
</script>
<script type="text/javascript" src="js/flexdropdown/flexdropdown.js">

/***********************************************
* Flex Level Drop Down Menu- (c) Dynamic Drive DHTML code library (www.dynamicdrive.com)
* This notice MUST stay intact for legal use
* Visit Dynamic Drive at http://www.dynamicdrive.com/ for this script and 100s more
***********************************************/
</script>
<script>
$(document).ready(function(){
	var offsettop=jQuery("#menuTarget").offset().top;
	var offsetleft=jQuery("#menuTarget").offset().left; 
	jQuery("#menuTarget").css({position: "absolute",'top':offsettop - jQuery('#menuTarget').height()});
	// jQuery("#menuTarget").css({position: "absolute",'top':300});
	
	$(".startMenuItem").bind("click", function(e) {
		onClickMenu(e);
	});
	var firstUseDate = "<%=firstUseDate%>";
    if (firstUseDate == "" && <%=privilege.isUserPrivValid(request,"admin")%>)
    {
      //addTab("操作引导", "admin/introduction.jsp","747765919");
    }
    
    jQuery("#submenu-wrap").height(jQuery("#tabpanelBody").height()-23);
    
    //获取光标
	//document.body.focus();  
	//禁止退格键 作用于Firefox、Opera   
    document.onkeypress = banBackSpace;  
    //禁止退格键 作用于IE、Chrome  
    document.onkeydown = banBackSpace; 
});

function showMenu() {
	var $=jQuery;
	var $target = $('#menuTarget');
	var $flexmenu = $('#flexmenu1');
	flexdropdownmenu.positionul($, $flexmenu, null, $target);
	flexdropdownmenu.showbox($, $target, $flexmenu, null);
}

function hideMenu() {
	$('.jqflexmenu').find('ul').andSelf().hide()
}

function onClickMenu(e) {
	var obj = e.target
	if (e.shiftKey) {
		if (obj.tagName=="A") {
			// if (obj.target=="mainFrame") {
				window.open(obj.getAttribute("link"));
			// }
			return false;
		}
	}
	if (obj.tagName=="A") {
		if (obj.getAttribute("link")=="") {
			e.preventDefault();			
			return false;
		}
		if (obj.target=="_blank") {
			window.open(obj.getAttribute("link"));
			e.preventDefault();			
			return false;
		}
		else if (obj.target=="_top") {
			window.top.location.href = obj.getAttribute("link");
			e.preventDefault();			
			return false;
		}
		else {
			if (!top.mainFrame.tabpanel)
				return false;
			if (!top.mainFrame.tabpanel.tabs)
				return false;
			if (typeof(top.mainFrame.addTab)!="function") {
				if (typeof(top.mainFrame.addTab)!="object")
					return false;
			}

			// 检查是否已有同样的title的选项卡
			var isAdded = false;
			var tabs = top.mainFrame.tabpanel.tabs;
			var tabsId;
			for (var i=0; i<tabs.length; i++) {
				if (tabs[i].title.text()==obj.innerText) {
					isAdded = true;
					tabsId = tabs[i].id;
					break;
				}
			}		

			if (!isAdded) {
				top.mainFrame.addTab(obj.innerHTML, obj.getAttribute("link"));
			}
			else {
				var isActive = false;
				
				if (obj.getAttribute("canRepeat")) {
					if (obj.getAttribute("canRepeat")=="false")
						isActive = true;
				}					
												
				if (isActive) {
					// 如果不允许重复，则激活
					top.mainFrame.addTab(obj.innerText, obj.href, tabsId);
					
					var position = top.mainFrame.tabpanel.getTabPosision(tabsId);
					var iframes = top.mainFrame.tabpanel.tabs[position].content.find('iframe');
					iframes[0].src = iframes[0].src;	//刷新，注意不能用iframes[0].location.reload()，这样会失去tab
				}
				else {
					top.mainFrame.addTab(obj.innerText, obj.getAttribute("link"));				
				}
			}
			return false;
		}
	}
}
</script>
</head>
<body id="tabpanelBody">
<div class="onlineList" style="border:1px #aeaeae solid;width:125px;height:625px;overflow-y:auto;position:absolute;display:none;z-index:9999;background:#fff;" onblur="closeOnline()">
<%DeptMgr dm = new DeptMgr();
	DeptUserDb du = new DeptUserDb();
	UserMgr um = new UserMgr();
	OnlineUserDb oud = new OnlineUserDb();
	Iterator iOud = oud.list().iterator();
	while (iOud.hasNext()) {
		oud = (OnlineUserDb)iOud.next();
		UserDb user = um.getUserDb(oud.getName());
		com.redmoon.forum.person.UserDb ud = new com.redmoon.forum.person.UserDb();
		ud = ud.getUser(user.getName());
		String deptName = "";
		Iterator ir2 = du.getDeptsOfUser(user.getName()).iterator();
		if (ir2.hasNext()) {
			DeptDb dd = (DeptDb)ir2.next();
			/*
			if (!dd.getParentCode().equals(DeptDb.ROOTCODE)) {
				deptName = dm.getDeptDb(dd.getParentCode()).getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dd.getName();
			}
			else
			*/
			deptName = dd.getName();
		}	
		 
		String mstr = "<a target='mainFrame' href='message_oa/message_frame.jsp?op=send&receiver=" + StrUtil.UrlEncode(oud.getName()) + "'>" + "消息" + "</a>";
%>
<div style="margin:5px 0 5px 5px;">

		<img align="absbottom" width=30 height=30 
		<%
			String imgSrc = "";
		    if (StrUtil.getNullStr(user.getPhoto()).equals("")) {
		    	imgSrc = "forum/images/face/";
		    	if (StrUtil.getNullStr(user.getPicture()).equals("")) {
		    		if (StrUtil.getNullStr(ud.getMyface()).equals("")) {
		    			String realPic = StrUtil.getNullStr(ud.getRealPic());
		    			if (realPic.toLowerCase().endsWith(".gif") || 
		    					realPic.toLowerCase().endsWith(".png") ||
		    					realPic.toLowerCase().endsWith(".jpg") ||
		    					realPic.toLowerCase().endsWith(".bmp")) {
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
		src = "<%=imgSrc %>"/>

	：&nbsp;<a title="登录时间：<%=DateUtil.format(oud.getLogTime(), "yyyy-MM-dd HH:mm:ss")%>" onclick="addTab('<%=user.getRealName()%>', 'user_info.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>')" target="mainFrame" style="cursor:pointer"><%=user.getRealName()%></a>&nbsp;&nbsp;<a href="message_oa/message_frame.jsp?op=send&receiver=<%=StrUtil.UrlEncode(oud.getName())%>" target="mainFrame" style="display:none">消息</a>
		</div>
<%
	}
%>
</div>
<div>
<a id="menuTarget" href="#" data-flexmenu="flexmenu1" style="position:absolute; top:0px; left:0px"><span style="display:none">下拉菜单</span></a>
<%
if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();
request.setAttribute("skinPath", skinPath);	

int x=0;

com.redmoon.oa.ui.menu.LeafChildrenCacheMgr lccm = new com.redmoon.oa.ui.menu.LeafChildrenCacheMgr(Leaf.CODE_ROOT);
Iterator ir = lccm.getChildren().iterator();
int k=2;
%>
<ul id="flexmenu1" class="flexdropdownmenu">
<%
while (ir.hasNext()) {
	com.redmoon.oa.ui.menu.Leaf lf = (com.redmoon.oa.ui.menu.Leaf)ir.next();
	if (!lf.canUserSee(request) || lf.getCode().equals(Leaf.CODE_BOTTOM))
		continue;
	LeafChildrenCacheMgr lccm2 = new LeafChildrenCacheMgr(lf.getCode());
	Vector v2 = lccm2.getChildren();
%>
    <li>
        <a class="startMenuItem" href="javascript:void(0);" link="<%=lf.getLink(request)%>" hidefocus="true" canRepeat="<%=lf.isCanRepeat()%>" target="<%=lf.getTarget()%>"><%=lf.getName()%></a>
		<%
        if (v2.size()>0) {%>
        <ul>
        <%
        k++;
        Iterator ir2 = v2.iterator();
        while (ir2.hasNext()) {
            Leaf lf2 = (Leaf)ir2.next();
            if (!lf2.canUserSee(request))
                continue;
            LeafChildrenCacheMgr lccm3 = new LeafChildrenCacheMgr(lf2.getCode());
            Vector v3 = lccm3.getChildren();
        %>
            <li>
                <a class="startMenuItem" href="javascript:void(0);" link="<%=lf2.getLink(request)%>" hidefocus="true" canRepeat="<%=lf2.isCanRepeat()%>" target="<%=lf2.getTarget()%>"><%=lf2.getName()%></a>
				<%if (v3.size()>0) {%>
                    <ul>
                    <%
                    k++;
                    Iterator ir3 = v3.iterator();
                    while (ir3.hasNext()) {
                        Leaf lf3 = (Leaf)ir3.next();
                        if (!lf3.canUserSee(request))
                            continue;
                    %>
                        <li>
                            <a class="startMenuItem" href="javascript:void(0);" link="<%=lf3.getLink(request)%>" hidefocus="true" canRepeat="<%=lf3.isCanRepeat()%>" target="<%=lf3.getTarget()%>"><%=lf3.getName()%></a>
                        </li>
                    <%}%>
                    </ul>
                <%}%>
            </li>            
        <%}%>
        </ul>
        <%}
        else
            k++;
        %>
  </li>
<%}%>
</ul>
</div>
<!--
<div id="tab"></div>
-->
<object NAME='player' classid=clsid:22d6f312-b0f6-11d0-94ab-0080c74c7e95 width=350 height=70 style="position:absolute; left:-300px; top:-300px;">
  <param name=showstatusbar value=1>
  <param name=filename value='<%=request.getContextPath()%>/message/msg.wav'>
  <param name="AUTOSTART" value="false" />
  <embed src='<%=request.getContextPath()%>/message/msg.wav'> </embed>
</object>

<div id="dialog" title="消息" style="display:none" >
<form id="msgForm" action="message_oa/iknow.jsp" method="post">
<div id="msgDiv"></div>
</form>
<div align="center" style="margin-top:10px">
<a href="javascript:IKnow()">我知道了</a>&nbsp;&nbsp;&nbsp;&nbsp;<a href="#" onclick="$('#dialog').dialog('close')">&nbsp;关闭窗口(<span id="sec"></span>)</a>
</div>
</div>
<!-- 快捷菜单 -->
	<div id="quickMenu" onmouseout="hide()" style="display:none">
    	<iframe src="admin/entry_pop.jsp"    class="entryFrame" >
    	</iframe>
    	</div>
<!-- 左侧菜单子级菜单 -->   
<div id="submenu-wrap" class="submenu-wrap" style="display:none;" onmouseleave="removeAllMenusClass();">
<%
com.redmoon.oa.ui.menu.LeafChildrenCacheMgr lccm1 = new com.redmoon.oa.ui.menu.LeafChildrenCacheMgr(Leaf.CODE_ROOT);
Iterator ir1 = lccm1.getChildren().iterator();
//得到有效的可显示的菜单数
int firstMenuNum=0;
boolean hasSystemMenu = false;
while (ir1.hasNext()) {
	com.redmoon.oa.ui.menu.Leaf lf = (com.redmoon.oa.ui.menu.Leaf)ir1.next();
	//无菜单项
	if(!item.equals("all")){
		if (!item.equals(lf.getCode())){
			continue;
		}
	}
	//判断的菜单是否可见；是否头部菜单；是否启用
	if (!lf.canUserSee(request) || lf.getCode().equals(Leaf.CODE_BOTTOM) || !lf.isUse()){
		continue;
	}
	firstMenuNum++;
	if(lf.getCode().equals("super_m")){
		hasSystemMenu = true;
	}
}
ir1 = lccm1.getChildren().iterator();
int n=2;
int menusCount = 0;
while (ir1.hasNext()) {
	com.redmoon.oa.ui.menu.Leaf lf = (com.redmoon.oa.ui.menu.Leaf)ir1.next();
	if(firstMenuNum>9){//如果x>9,循环显示7个，倒数第二个是“系统”菜单，倒数第一个是“更多”菜单；否则全部显示
		menusCount++;
		if(hasSystemMenu){
			if(n>8){
				break;
			}
			if(lf.getCode().equals("super_m")){
				continue;
			}
		}else{
			if(n>9){
				break;
			}
		}
	}
	
	//无菜单项
	if(!item.equals("all")){
		if (!item.equals(lf.getCode())){
			continue;
		}
	}
	//判断的菜单是否可见；是否头部菜单；是否启用
	if (!lf.canUserSee(request) || lf.getCode().equals(Leaf.CODE_BOTTOM) || !lf.isUse()){
		continue;
	}
//判断当前一级菜单是否有链接,只是点击的事件不一样
//无链接，打开/关闭子菜单
if (lf.getLink(request).equals("")) {%>
	<div id="<%=lf.getCode()%>" style="display:none;">
	<%
		LeafChildrenCacheMgr lccm1_1 = new LeafChildrenCacheMgr(lf.getCode());	
		Vector v1_1 = lccm1_1.getChildren();
		Iterator ir1_1 = v1_1.iterator();
		int flagNum=0;
		while (ir1_1.hasNext()) {
			flagNum++;
			Leaf lf1_1 = (Leaf)ir1_1.next();
			//判断二级菜单是否可见；是否启用
			if (!lf1_1.canUserSee(request) || !lf1_1.isUse()){
				continue;
			}
			
			//获取当前二级菜单的三级菜单（整个系统最多只有三级菜单）
			LeafChildrenCacheMgr lccm1_2 = new LeafChildrenCacheMgr(lf1_1.getCode());
			Vector v1_2 = lccm1_2.getChildren();
			int menuNum = 0;
			%>
			<div class='submenu-wrap-box' id="<%=lf1_1.getCode() %><%=flagNum %>">
			<%
			if (v1_2.size() != 0) {//有三级菜单
				%>
				<div class='submenu-wrap-box-title'><div><%=lf1_1.getName(request) %></div></div>
				<div class='submenu-wrap-thirdmenu'>
				<%
				Iterator ir1_2 = v1_2.iterator();
				
				while (ir1_2.hasNext()) {
					Leaf lf1_2 = (Leaf)ir1_2.next();
					//判断三级菜单是否可见；是否启用
					if (!lf1_2.canUserSee(request) || !lf1_2.isUse()){
						continue;
					}%>
					<div class='submenu-wrap-box-div'>
						<a href='javascript:;' onclick="removeAllMenusClass();openPage(this, '<%=lf1_2.getName(request)%>','<%=lf1_2.isCanRepeat() %>','true','<%=lf1_2.getLink(request)%>','<%=lf1_2.getTarget() %>')"><%=lf1_2.getName(request)%></a>
					</div>
					<%
					menuNum++;
				}
				%>
				</div>
				</div>
				<script>
					if("<%=menuNum%>" == "0"){
						$("#<%=lf1_1.getCode() %><%=flagNum %>").hide();
					}else{
						$("#<%=lf1_1.getCode() %><%=flagNum %>").after($("<hr/>"));
					}
				</script>
				<%
			}else{%>
				<div class='submenu-wrap-box-title'>
					<div class='submenu-wrap-box-div'>
						<a href='javascript:;' onclick="removeAllMenusClass();openPage(this, '<%=lf1_1.getName(request)%>','<%=lf1_1.isCanRepeat() %>','true','<%=lf1_1.getLink(request)%>','<%=lf1_1.getTarget() %>')"><%=lf1_1.getName(request)%></a>
					</div>
				</div>
				</div><hr/>
			<%
			}
		}
	%>
	</div>
<%} 
n++;
}

if(firstMenuNum > 9){//x>9,循环显示7个，倒数第二个是“系统”菜单，倒数第一个是“更多”菜单；
	if(hasSystemMenu){
		com.redmoon.oa.ui.menu.Leaf lf1 = new com.redmoon.oa.ui.menu.Leaf("super_m");
%>	
	<div id="<%=lf1.getCode()%>" style="display:none;">
		<%
		LeafChildrenCacheMgr lccm1_1 = new LeafChildrenCacheMgr(lf1.getCode());	
		Vector v1_1 = lccm1_1.getChildren();
		Iterator ir1_1 = v1_1.iterator();
		int flagNum1=0;
		while (ir1_1.hasNext()) {
			flagNum1++;
			Leaf lf1_1 = (Leaf)ir1_1.next();
			//判断二级菜单是否可见；是否启用
			if (!lf1_1.canUserSee(request) || !lf1_1.isUse()){
				continue;
			}
			
			//获取当前二级菜单的三级菜单（整个系统最多只有三级菜单）
			LeafChildrenCacheMgr lccm1_2 = new LeafChildrenCacheMgr(lf1_1.getCode());
			Vector v1_2 = lccm1_2.getChildren();
			int menuNum1 = 0;
			%>
			<div class='submenu-wrap-box' id="<%=lf1_1.getCode() %><%=flagNum1 %>">
			<%
			if (v1_2.size() != 0) {//有三级菜单
				%>
				<div class='submenu-wrap-box-title'><div><%=lf1_1.getName(request) %></div></div>
				<div class='submenu-wrap-thirdmenu'>
				<%
				Iterator ir1_2 = v1_2.iterator();
				while (ir1_2.hasNext()) {
					Leaf lf1_2 = (Leaf)ir1_2.next();
					//判断三级菜单是否可见；是否启用
					if (!lf1_2.canUserSee(request) || !lf1_2.isUse()){
						continue;
					}%>
					<div class='submenu-wrap-box-div'>
						<a href='javascript:;' onclick="removeAllMenusClass();openPage(this, '<%=lf1_2.getName(request)%>','<%=lf1_2.isCanRepeat() %>','true','<%=lf1_2.getLink(request)%>','<%=lf1_2.getTarget() %>')"><%=lf1_2.getName(request)%></a>
					</div>
					<%
					menuNum1++;
				}
				%>
				</div>
				</div>
				<script>
					if("<%=menuNum1%>" == "0"){
						$("#<%=lf1_1.getCode() %><%=flagNum1 %>").hide();
					}else{
						$("#<%=lf1_1.getCode() %><%=flagNum1 %>").after($("<hr/>"));
					}
				</script>
				<%
			}else{%>
				<div class='submenu-wrap-box-title'>
					<div class='submenu-wrap-box-div'>
						<a href='javascript:;' onclick="removeAllMenusClass();openPage(this, '<%=lf1_1.getName(request)%>','<%=lf1_1.isCanRepeat() %>','true','<%=lf1_1.getLink(request)%>','<%=lf1_1.getTarget() %>')"><%=lf1_1.getName(request)%></a>
					</div>
				</div>
				</div><hr/>
			<%
			}
		}
	%>
	</div>
	<%} %>
	<div id="allOthers" style="display:none;">
		<%
		com.redmoon.oa.ui.menu.LeafChildrenCacheMgr lccm1_4 = new com.redmoon.oa.ui.menu.LeafChildrenCacheMgr(Leaf.CODE_ROOT);
		Iterator ir1_4 = lccm1_4.getChildren().iterator();
		int currentIndex = 0;
		while (ir1_4.hasNext()) {
			com.redmoon.oa.ui.menu.Leaf lf = (com.redmoon.oa.ui.menu.Leaf)ir1_4.next();
			if(lf.getCode().equals("super_m")){
				++currentIndex;
				continue;
			}
			if(++currentIndex >= menusCount){
				//判断一级菜单是否可见；是否头部菜单；是否启用
				if (!lf.canUserSee(request) || lf.getCode().equals(Leaf.CODE_BOTTOM) || !lf.isUse()){
					continue;
				}
				//判断一级菜单是否有链接
				if (!lf.getLink(request).equals("")) {//有链接，直接打开tab页
				%>
					<div class='submenu-wrap-box' >
						<div class='submenu-wrap-box-title'>
							<div class='submenu-wrap-box-div'>
								<a href='javascript:;' onclick="removeAllMenusClass();openPage(this, '<%=lf.getName(request)%>','<%=lf.isCanRepeat()%>','true','<%=lf.getLink(request)%>','<%=lf.getTarget()%>')"><%=lf.getName(request)%></a>
							</div>
						</div>
					</div>
					<hr/>
				<%
				}else{//无链接，显示二级菜单
					LeafChildrenCacheMgr lccm4 = new LeafChildrenCacheMgr(lf.getCode());
					Iterator ir4 = lccm4.getChildren().iterator();
					int flagNum2=0;
					while (ir4.hasNext()) {
						flagNum2++;
						Leaf lf4 = (Leaf)ir4.next();
						//判断二级菜单是否可见；是否启用
						if (!lf4.canUserSee(request) || !lf4.isUse()){
							continue;
						}
						//获取当前二级菜单的三级菜单（整个系统最多只有三级菜单）
						LeafChildrenCacheMgr lccm5 = new LeafChildrenCacheMgr(lf4.getCode());
						Vector v5 = lccm5.getChildren();
						if (v5.size() != 0) {//有三级菜单
						%>
							<div class='submenu-wrap-box' id="<%=lf.getCode() %><%=flagNum2 %>">
							<div class='submenu-wrap-box-title'><div><%=lf.getName(request)%> / <%=lf4.getName(request)%></div></div>
							<div class='submenu-wrap-thirdmenu'>
						<%
							Iterator ir5 = v5.iterator();
							int menuNum2 = 0;
							while (ir5.hasNext()) {
								Leaf lf5 = (Leaf)ir5.next();
								//判断三级菜单是否可见；是否启用
								if (!lf5.canUserSee(request) || !lf5.isUse()){
									continue;
								}
								%>
								<div class='submenu-wrap-box-div'>
									<a href='javascript:;' onclick="removeAllMenusClass();openPage(this, '<%=lf5.getName(request)%>','<%=lf5.isCanRepeat()%>','true','<%=lf5.getLink(request)%>','<%=lf5.getTarget()%>')"><%=lf5.getName(request)%></a>
								</div>
								<%
								menuNum2++;
							}
							%>
							</div>
							</div>
							<script>
								if("<%=menuNum2%>" == "0"){
									$("#<%=lf.getCode() %><%=flagNum2 %>").hide();
								}else{
									$("#<%=lf.getCode() %><%=flagNum2 %>").after($("<hr/>"));
								}
							</script>
							<%
						}else{//没有三级菜单
							%>
							<div class='submenu-wrap-box'>
							<div class='submenu-wrap-box-title'>
							<div><%=lf.getName(request) %> / </div><div class='submenu-wrap-box-div'><a href='javascript:;' onclick="removeAllMenusClass();openPage(this, '<%=lf4.getName(request)%>','<%=lf4.isCanRepeat()%>','true','<%=lf4.getLink(request)%>','<%=lf4.getTarget()%>')"><%=lf4.getName(request)%></a></div>
							</div>
							</div><hr/>
							<%
						}
						
					}
				}
				
			}
		}
		%>	
	</div>
<%}%>	

</div>
</body>
<script type="text/JavaScript">
function online(){
	$(".onlineList").css({"display":""});
}
function closeOnline(){
	$(".onlineList").css({"display":"none"});
	window.top.topFrame.type2();
}
function IKnow() {
	jQuery("#msgForm").submit();
}
/*
function reinitIframe(){
	var iframe = document.getElementById("ifrmMain");
	try{
		var bHeight = document.body.scrollHeight;
		var dHeight = document.documentElement.scrollHeight;
		var height = Math.max(bHeight, dHeight);
		iframe.height = document.body.clientHeight - 28;
	}catch (ex){}
}
*/
/*
function reinitIframe(){
	var iframes = document.getElementsByName("ifrmMain");
	for (i=0; i<iframes.length; i++) {
		try{
		var cw = document.documentElement.clientWidth;
		var sw = document.documentElement.scrollWidth;
		var w = Math.max(cw, sw);
		
		//window.status = document.documentElement.scrollWidth ;
		
		//iframes[i].width = w - 2; // IE6的bug，宽度包含了边框
		//iframes[i].height = document.documentElement.clientHeight - 32;
			
		tabpanel.resize();
			
		}catch (ex){}
	}
}
window.setInterval("reinitIframe()", 200);
*/
function showmsg(id) {
	addTab("消息", "<%=request.getContextPath()%>/message_oa/message_ext/sys_showmsg.jsp?id=" + id);
	jQuery("#dialog").dialog("close");
}

function tickout(secs) {
	sec.innerText = secs;
	if (--secs > 0) {
	  	setTimeout('tickout(' +secs + ')', 1000);
	}
	else {
		jQuery("#dialog").dialog("close");
	}
}

function notifyMsg(msgs) {
	// 取得当前active状态的tab中的iframe中处于焦点状态的元素
	var $focused = null;
	if (top.mainFrame) {
		if (top.mainFrame.tabpanel) {
			var tabpan = top.mainFrame.tabpanel;
			var position = tabpan.getTabPosision(tabpan.getActiveIndex());
			var iframes = tabpan.tabs[position].content.find('iframe');
			// console.log($(iframes[0]).contents().find(':focus')[0]);
			$focused = $(iframes[0]).contents().find(':focus');
		}
	}
	
	jQuery("#msgDiv").html(msgs);
	jQuery("#dialog").dialog({
		dialogClass:'transparent',
		height: 150,
		width: 300, 
		draggable: true,
		position: ['right', 'bottom'],
		autoOpen: true,
		show: {
			effect: 'slide',
			direction: 'down',
			duration: 1000
		}
	});
	
	tickout(15);
	
	// 使当前活动状态的选项卡中，因为弹出窗口而失去焦点的元素，重新获得焦点
	if ($focused) {
		$focused.focus();
	}
	
	<%
	UserSetupDb usd = new UserSetupDb();
	usd = usd.getUserSetupDb(privilege.getUser(request));
	if (usd.isMessageSoundPlay()) {
	%>
	player.play();
	<%}%>	
}
function show()
{
	jQuery("#quickMenu").show();
}
function hide()
{
	jQuery("#quickMenu").hide();
}


 function removeHide()
 {	
	try
	{
 		jQuery("#quickMenu").attr("onmouseout","");
 	}
 	catch(e)
 	{
 		
 	}
 }
 function addHide()
 {
 	jQuery("#quickMenu").attr("onmouseout","hide()");
 }
 
 //删除所有一级菜单选中的样式,并隐藏子级菜单层
function removeAllMenusClass(){
	try {
		top.leftFrame.removeAllMenusClass();
	}
	catch (e) {}
	hideSecondLevelMenu();
}
 
function openPage(obj, title, canRepeat, hideFocus, link, target) {
	if (isIE() && event.shiftKey) {
			// if (obj.target=="mainFrame") {
				window.open(link);
			// }
			removeAllMenusClass();
			return false;
	}
	if (target =="_blank") {
		if (link == null) {
			removeAllMenusClass();
			return false;
		}
		window.open(link);
		removeAllMenusClass();
		return false;	
	}
	else if (target=="_top") {
		window.top.location.href = link;
		removeAllMenusClass();
		return false;
	}
	else {
		if (!top.mainFrame.tabpanel){
			removeAllMenusClass();
			return false;
		}
		if (!top.mainFrame.tabpanel.tabs){
			removeAllMenusClass();
			return false;
		}
		if (typeof(top.mainFrame.addTab)!="function") {
			if (typeof(top.mainFrame.addTab)!="object"){
				removeAllMenusClass();
				return false;
			}
		}

		// 检查是否已有同样的title的选项卡
		var isAdded = false;
		var tabs = top.mainFrame.tabpanel.tabs;
		var tabsId;
		for (var i=0; i<tabs.length; i++) {
			if (tabs[i].title.text()==title) {
				isAdded = true;
				tabsId = tabs[i].id;
				break;
			}
		}		

		if (!isAdded) {
			top.mainFrame.addTab(title, link);
		}
		else {
			var isActive = false;
			
			
			if (canRepeat=="false")
				isActive = true;
							
											
			if (isActive) {
				// 如果不允许重复，则激活
				top.mainFrame.addTab(title, link, tabsId);
				
				var position = top.mainFrame.tabpanel.getTabPosision(tabsId);
				var iframes = top.mainFrame.tabpanel.tabs[position].content.find('iframe');
				iframes[0].src = iframes[0].src;	//刷新，注意不能用iframes[0].location.reload()，这样会失去tab
			}
			else {
				top.mainFrame.addTab(title, link);				
			}
		}
		removeAllMenusClass();
		return false;
	}
}

function getSecondLevelMenuToaster(){
	if($("#toaster").length <= 0){
		$.toaster({priority : 'info', message : '子菜单加载失败！' });
	}
}

//判断二级菜单显示/隐藏
function judgeMenuShowOrHide(){
	if(document.getElementById("submenu-wrap").style.display == "none"){
		removeAllMenusClass();
	}
}

//当前二级菜单显示/隐藏切换
function currentMenuShowOrHide(code){
	if(document.getElementById("submenu-wrap").style.display == ""){//如果显示就隐藏
		hideSecondLevelMenu();
	}else{//如果隐藏就显示
		showCurrentSecondLevelMenu(code);
	}
}

//显示当前二级菜单
function showCurrentSecondLevelMenu(code){
	if(jQuery("#"+code).children('div').length == 0){
		hideSecondLevelMenu();
	}else{
		showSecondLevelMenu();
		hideOtherSecondLevelMenu(code);
		document.getElementById(code).style.display = "";
	}
}
//隐藏所有其他二级菜单
function hideOtherSecondLevelMenu(code){
	jQuery('#submenu-wrap').children('div').each(function(i,v){
		if(document.getElementById(code) != this){
			this.style.display = "none";
		}
	})
}
//显示二级菜单层
function showSecondLevelMenu(){
	document.getElementById("submenu-wrap").style.display = "";
}
//隐藏二级菜单层
function hideSecondLevelMenu(){
	document.getElementById("submenu-wrap").style.display = "none";
}

//解决页面按了退格键，回到登陆页bug
function banBackSpace(e){
    var ev = e || window.event;//获取event对象
    var obj = ev.target || ev.srcElement;//获取事件源
    var t = obj.type || obj.getAttribute('type');//获取事件源类型
    //获取作为判断条件的事件类型
    var vReadOnly = obj.readOnly;
    var vDisabled = obj.disabled;
    //处理undefined值情况
    vReadOnly = (vReadOnly == undefined) ? false: vReadOnly;
    vDisabled = (vDisabled == undefined) ? true: vDisabled;
    //当敲Backspace键时，事件源类型为密码或单行、多行文本的，
    //并且readOnly属性为true或disabled属性为true的，则退格键失效
    var flag1= ev.keyCode == 8 && (t=="password"|| t=="text"|| t=="textarea")&& (vReadOnly==true|| vDisabled==true);
    //当敲Backspace键时，事件源类型非密码或单行、多行文本的，则退格键失效
    var flag2= ev.keyCode == 8 && t != "password"&& t != "text"&& t != "textarea";
    //判断
    if(flag2 || flag1) return false;
}
</script>
</html>