<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="com.redmoon.forum.plugin.*" %>
<%@ page import="java.util.*" %>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.ui.menu.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.message.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.pvg.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String userName = privilege.getUser(request);
String skincode = UserSet.getSkin(request);
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();
skinPath = "skin/bluethink_0";
request.setAttribute("skinPath", skinPath);	
String mm = skin.getLeftMenuTopBtn();
String[] m = mm.split(",");
String num1="",num2="",num3="",num4="",num5="",num6="",num7="",num8="",num9="";
for(int i=0;i<m.length;i++) {
 num1 = m[0];
 num2 = m[1];
 num3 = m[2];
 num4 = m[3];
 num5 = m[4];
 num6 = m[5];
 num7 = m[6];
 num8 = m[7];
 num9 = m[8];
}
String item = ParamUtil.get(request, "item").equals("") ? "all" : ParamUtil.get(request, "item");

UserMgr um = new UserMgr();

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
<title>left</title>
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css" />
<link href="<%=skinPath%>/main.css" rel="stylesheet" type="text/css" />
<script language="JScript.Encode" src="js/browinfo.js"></script>				
<script language="JScript.Encode" src="js/rtxint.js"></script>
<script src="inc/common.js"></script>
<script src="js/jquery.js"></script>
<script src="forum/inc/main.js"></script>
<script>

	//阻止事件冒泡
	function stopBubble(e) {
	   if (e && e.stopPropagation)
		   e.stopPropagation()
	   else
		   window.event.cancelBubble=true
	}
	
	function onClickDoc(e) {
		var obj=isIE()? event.srcElement : e.target
		if (isIE() && event.shiftKey) {
			if (obj.tagName=="A") {
				// if (obj.target=="mainFrame") {
					window.open(obj.getAttribute("link"));
				// }
				return false;
			}
		}
		if (obj.tagName=="A") {
			if (obj.target=="_blank") {
				if (obj.getAttribute("link") == null) {
					return false;
				}
				window.open(obj.getAttribute("link"));
				stopBubble(e);
				return false;	
			}
			else if (obj.target=="_top") {
				window.top.location.href = obj.getAttribute("link");
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

	var isReady = false;
	
	$(document).ready(function() {
		isReady = true;
		
		// 某些IE8下面不起作用fgf 20140114，改用jquery
		// document.onclick=onClickDoc;
		
		$(document).click(function(e) {
			//onClickDoc(e);
			return false;
		});	
		
	});
	
	function storeMenu(menuCode, flag) {
		if (!isReady)
			return false;
		
		var url = "ui/store_menu_ajax.jsp";
		jQuery.post(
			url,
			{
				menu_code : menuCode,
				user_name : "<%=userName%>"
			},
			function(){
				if(flag) {
					refreshMostRecentlyUsedMenu();
				}
			}
		);
	}
	
	function refreshMostRecentlyUsedMenu() {
		jQuery.ajax({
			url: "ui/most_recently_used_menu_list.jsp",
			data: "user_name=<%=userName%>",
			cache: false,
			success: function(html) {
				jQuery("#leftSubMenu0").html(html);
			}
		});
	}
	
	function setDivBackground(divId) {
		jQuery("#"+divId).toggleClass("selbg2");
	}
	
	function setMenuTop(divId,width,background,color) {
		document.getElementById(divId).style.width = width;
		document.getElementById(divId).style.backgroundImage = "url(" + background + ")";
		// document.getElementById(divId).style.color = color;
	}
	
	function setDivDisplay(divId,display) {
		if (o(divId))
			o(divId).style.display = display;
	}
	
	function setMenu(current) {
		switch(current) {
		case 1:
			setDivDisplay('menuMain1','');
			setDivDisplay('menuMain2','none');
			setDivDisplay('menuMain3','none');
			
			break;
		case 3:
			setDivDisplay('menuMain1','none');
			setDivDisplay('menuMain2','none');
			setDivDisplay('menuMain3','');
			break;
		}
	}
	
	function findObj(theObj, theDoc) {
		var p, i, foundObj;
		if(!theDoc) theDoc = document;
		if( (p = theObj.indexOf("?")) > 0 && parent.frames.length) {
			theDoc = parent.frames[theObj.substring(p+1)].document;
			theObj = theObj.substring(0,p);
		}
		if(!(foundObj = theDoc[theObj]) && theDoc.all) {
			foundObj = theDoc.all[theObj];
		}
		for (i=0; !foundObj && i < theDoc.forms.length; i++) { 
			foundObj = theDoc.forms[i][theObj];
		}
		for(i=0; !foundObj && theDoc.layers && i<theDoc.layers.length; i++) {
			foundObj = findObj(theObj,theDoc.layers[i].document);
		}
		if(!foundObj && document.getElementById) {
			foundObj = document.getElementById(theObj);
		}
		return foundObj;
	}
	
	function displayLeftSubMenu() {
		
		var args = displayLeftSubMenu.arguments;
		var num = args[0];
		var id = "leftMenu" + num;
		leftMenuObj = o(id);
		if (leftMenuObj==null) {
			return;
		}
		leftSubMenuObj = o("leftSubMenu" + num);
		leftSubMenuLineObj = o("leftSubMenuLine" + num);
		
		chainImgObj = o("chainImg" + num);
		if (leftMenuObj.getAttribute("isOpen")==1) {
			jQuery("#" + id+" .span-2").css('color','#606060');//一级菜单关闭时，字体改成默认颜色
			leftSubMenuObj.style.display = "none";
			if(leftSubMenuLineObj!=null) leftSubMenuLineObj.style.display = "none";
			leftMenuObj.setAttribute("isOpen", 0);
			if (chainImgObj)
			{
				var imgSrc = chainImgObj.src;
				if (imgSrc.indexOf("<%=skinPath%>/images/arrowpull.png") > 0)
				{
					chainImgObj.src = "<%=skinPath%>/images/arrownormal.png";
				}
				else
				{
					chainImgObj.src = "<%=skinPath%>/images/openico.jpg";
				}
			}
		}
		else
		{
			jQuery("#" + id+" .span-2").css('color','#f45c00');//一级菜单打开时，字体改成醒目颜色
			if (leftSubMenuObj!=null)
				leftSubMenuObj.style.display = "";
			if(leftSubMenuLineObj!=null)
				leftSubMenuLineObj.style.display = "";
			leftMenuObj.setAttribute("isOpen", 1);
			if (chainImgObj)
			{
				var imgSrc = chainImgObj.src;
				if (imgSrc.indexOf("<%=skinPath%>/images/arrownormal.png") > 0)
				{
					chainImgObj.src = "<%=skinPath%>/images/arrowpull.png";
				}
				else
				{
					chainImgObj.src = "<%=skinPath%>/images/putawayico.jpg";
				}
			}
		}
		if(jQuery("#" + id).hasClass("boxbg")) {
			jQuery(".boxbg").not("[id='" + id + "']").attr("isOpen", "0");
			jQuery(".submenuul").not("[id='leftSubMenu" + num + "']").hide();
			jQuery(".boxbg").not("[id='" + id + "']").children(".span-2").css('color','#606060');//将其他一级菜单的字体改成默认颜色
			$(".boxbg").not("[id='" + id + "']").children("span").children("img[id^='chainImg'][id!='chainImg"+num+"']").attr("src", "<%=skinPath%>/images/arrownormal.png");//修改其他一级菜单的箭头指向
			//jQuery("#menuFavorite > .submenuul").show();
		}
		
	}

	var currentMenuItem = -1;

	function selectMenuItem() {
		var args = selectMenuItem.arguments;
		var num = args[0];
	
		var menuItemObj;
		
		menuItemObj = findObj("menuItem" + currentMenuItem);
		if(menuItemObj != null) {
			menuItemObj.isSelected = 0;
			menuItemObj.className='menuItem';
		}
		
		menuItemObj = findObj("menuItem" + num);
		if(menuItemObj != null) {
			menuItemObj.isSelected = 1;
			menuItemObj.className='menuItemActived';
		}
		currentMenuItem = num;
	}

	function outMenuItem() {
		var args = outMenuItem.arguments;
		var num = args[0];
	
		menuItemObj = findObj("menuItem" + num);
		if(menuItemObj != null) {
			if(menuItemObj.isSelected == 0) {
				menuItemObj.className='menuItem';
			}
		}
	}
	
	function hoverMenuItem() {
		var args = hoverMenuItem.arguments;
		var num = args[0];

		menuItemObj = findObj("menuItem" + num);
		if(menuItemObj != null) {
			if(menuItemObj.isSelected == 0) {
				menuItemObj.className='menuItemActive';
			}
		}
	}

	var isOpen = false;
	
	function checkall(){
		var leftMenuObj,leftSubMenuObj,leftSubMenuLineObj,chainImgObj;
		var islayer2 = false;
		for (i=2; i<100; i++) 
		{
			leftMenuObj = findObj("leftMenu"+i);
			if (leftMenuObj==null)
				continue;
			leftSubMenuObj = findObj("leftSubMenu"+i);
			if (leftSubMenuObj==null)
				continue;
			leftSubMenuLineObj = findObj("leftSubMenuLine" + i);
			chainImgObj = findObj("chainImg" + i);
	
			if (isOpen)
			{
				leftSubMenuObj.style.display = "none";
				if(leftSubMenuLineObj!=null)leftSubMenuLineObj.style.display = "none";
				if (chainImgObj.src == "<%=skinPath%>/images/arrowpull.png")
				{
					chainImgObj.src = "<%=skinPath%>/images/arrownormal.png";
				}
				else
				{
					chainImgObj.src = "<%=skinPath%>/images/openico.jpg";
				}
				leftMenuObj.isOpen = 0;
			}
			else
			{
				leftSubMenuObj.style.display = "";
				if(leftSubMenuLineObj!=null)leftSubMenuLineObj.style.display = "";
				if (chainImgObj)
				{
					if (chainImgObj.src == "<%=skinPath%>/images/arrownormal.png")
					{
						chainImgObj.src = "<%=skinPath%>/images/arrowpull.png";
					}
					else
					{
						chainImgObj.src = "<%=skinPath%>/images/putawayico.jpg";
					}
				}
				leftMenuObj.isOpen = 1;
			}
		}
		isOpen = !isOpen;
		
	}
	
	function check(){
		var openones = "";
		var array = jQuery("div[id^='leftMenu'][isOpen=1]");
		for(i=0; i<array.length; i++) {
			if (openones=="") {	
				openones = array[i].id.substring(8);
			} else {
				openones += ("|" + array[i].id.substring(8));
			}
		}
		
		var expdate = new Date();
		var expday = 60;
		expdate.setTime(expdate.getTime() +  (24 * 60 * 60 * 1000 * expday));
		document.cookie="oa_left_menu"+"="+openones+" ;expires="+expdate.toGMTString();
	}
	
	function get_cookie(Name) {
		var search = Name + "="
		var returnvalue = "";
		if (document.cookie.length > 0) {
			offset = document.cookie.indexOf(search)
			// if cookie exists
			if (offset != -1) { 
				offset += search.length
				// set index of beginning of value
				end = document.cookie.indexOf(";", offset);
				// set index of end of cookie value
				if (end == -1) end = document.cookie.length;
				returnvalue=unescape(document.cookie.substring(offset, end))
			}
		}
		return returnvalue;
	}
	
	function init() {
		if (get_cookie("oa_left_menu") != ''){
			var openresults=get_cookie("oa_left_menu").split("|");
			for (i=0; i<openresults.length; i++){
				displayLeftSubMenu(openresults[i]);
			}
		}
		refresh();
		var allHeight = document.documentElement.clientHeight ;
		document.getElementById("menuMain1").style.height = allHeight - 46 + "px";
	}
	
	//加载在线用户
	function doBiz(response){
		var rsp = response.responseText.trim();
		if (rsp!="no") {
			o("menuOnline").innerHTML = rsp;
		}
	}
	
	var errFunc = function(response) {
		// alert('Error ' + response.status + ' - ' + response.statusText);
		// alert(response.responseText);
	}
	
	//刷新在线用户
	function refreshOnlineUser() {
		var str = "";
		var myAjax = new cwAjax.Request( 
			"ajax_online.jsp",
			{
				method:"post",
				parameters:str,
				onComplete:doBiz,
				onError:errFunc
			}
		);
	}
	
	function refresh(){
		<%
		
		String refreshOnlineInterval = cfg.get("refreshOnlineInterval");
		%>
		refreshOnlineUser();
		timeoutid = window.setTimeout("refresh()", <%=refreshOnlineInterval%> * 1000); // 每隔N秒钟刷新一次
	}
	
//设置菜单选中后背景色
function setBackGround(id)
{
	jQuery("#"+id).addClass("selbg");
}
//在线用户中显示消息
function showDiv(obj)
{
	jQuery("#"+obj).show(); 
}
//在线用户中隐藏消息
function hideDiv(obj)
{
	jQuery("#"+obj).hide(); 
}

function openPage(obj, title, canRepeat, hideFocus, link, target) {
		if (isIE() && event.shiftKey) {
				// if (obj.target=="mainFrame") {
					window.open(link);
				// }
				return false;
		}
		if (target =="_blank") {
			if (link == null) {
				return false;
			}
			window.open(link);
			stopBubble(obj);
			return false;	
		}
		else if (target=="_top") {
			window.top.location.href = link;
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
			return false;
		}
	
	}
	
	
function changeLeft(index) {
	try	{
		if (index == 0)	{
			jQuery("#menuTab").addClass("selected-1");
			jQuery("#online").removeClass("selected-2");
			
			jQuery("#menuIcon").attr("src", "<%=skinPath%>/images/selico-1.png");
			jQuery("#onlineIcon").attr("src", "<%=skinPath%>/images/norico-2.png");
			setMenu(1);
		}
		else {
			jQuery("#menuTab").removeClass("selected-1");
			jQuery("#online").addClass("selected-2");
			
			jQuery("#menuIcon").attr("src", "<%=skinPath%>/images/norico-1.png");
			jQuery("#onlineIcon").attr("src", "<%=skinPath%>/images/selico-2.png");
			setMenu(3);
		}
	}
	catch(e) {
	}
}

jQuery.ready(function(){
	
})
</script>
</head>
<body onload="init()" onunload="check()">
<%
boolean isRTXUsed = cfg.get("isRTXUsed").equals("true");
%>
	<div id="left" class="v2main">
		<div class="menutab" id="tab">
	       <ul>
	          <li class="selected-1" onclick="changeLeft(0);" id="menuTab"><a href="#"><img id="menuIcon" src="<%=skinPath%>/images/selico-1.png" width="15" height="18" /> 菜单</a></li>
	          <li id="online" onclick="changeLeft(1);"><a href="#"><img id="onlineIcon" src="<%=skinPath%>/images/norico-2.png" width="18" height="17" /> 在线</a></li>
	       </ul>
		</div>
		<div id="leftMenuMiddle" class="leftsub">
			<div id="menuMain1" class="menuMain1">
				
<%
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
	LeafChildrenCacheMgr lccm2 = new LeafChildrenCacheMgr(lf.getCode());
	Vector v2 = lccm2.getChildren();
%>
<%if (!lf.getLink(request).equals("")) {%>
	<div class="boxbg" id="leftMenu<%=k%>" isOpen=0 onclick="storeMenu('<%=lf.getCode()%>', true);openPage(this, '<%=lf.getName(request)%>','<%=lf.isCanRepeat()%>','true','<%=lf.getLink(request)%>','<%=lf.getTarget()%>')" onmouseover="setDivBackground('leftMenu<%=k%>')" onmouseout="setDivBackground('leftMenu<%=k%>')">
<%} 
else {%>
	<div class="boxbg" id="leftMenu<%=k%>" isOpen=0 onclick="<%=v2.size()>0?"displayLeftSubMenu(" + k + ")":""%>" onmouseover="setDivBackground('leftMenu<%=k%>')" onmouseout="setDivBackground('leftMenu<%=k%>')">
<%} %>
		<span class="span-1"><img src="<%=skinPath%>/icons/<%=lf.getIcon()%>" width="30" height="30" /></span>
		
		<%if (!lf.getLink(request).equals("")) {%>
			<span class="span-2"><%=lf.getName(request)%></span><span><img src="<%=skinPath%>/images/arrownormal.png" width="11" height="11" id="chainImg<%=k %>"/></span>
			<%}else{%>
			<span class="span-2"><%=lf.getName(request)%></span><span><img src="<%=skinPath%>/images/arrownormal.png" width="11" height="11" id="chainImg<%=k %>"/></span>
			<%}%>
	</div>
	<div class="menuLineLevel1"></div>
	<%
	if (v2.size()>0) {%>
	<ul class="submenuul" id="leftSubMenu<%=k%>" style="display:none">
	<%
	k++;
	Iterator ir2 = v2.iterator();
	while (ir2.hasNext()) {
		Leaf lf2 = (Leaf)ir2.next();
		if (!lf2.canUserSee(request) || !lf2.isUse())
			continue;
		LeafChildrenCacheMgr lccm3 = new LeafChildrenCacheMgr(lf2.getCode());
		Vector v3 = lccm3.getChildren();
		if (v3.size()==0) {
	%>
			<li id="menuItem<%=x%>" isSelected=0 onclick="selectMenuItem(<%=x%>);setBackGround('menuItem<%=x%>');" onmouseover="setDivBackground('menuItem<%=x%>')" onmouseout="setDivBackground('menuItem<%=x++%>')">
			 <div class="listbox" onclick="storeMenu('<%=lf2.getCode()%>', true);openPage(this, '<%=lf2.getName(request)%>','<%=lf2.isCanRepeat()%>','true','<%=lf2.getLink(request)%>','<%=lf2.getTarget()%>')">
			 	<span class="new_f-1">■</span><%=lf2.getName(request)%>
			</div>
			</li>
			
		<%}else{%>
			<li>
			<div class="listbox" id="leftMenu<%=k%>" isSelected=0 onclick="displayLeftSubMenu(<%=k%>)" onmouseover="setDivBackground('leftMenu<%=k%>')" onmouseout="setDivBackground('leftMenu<%=k%>')"><img src="<%=skinPath%>/images/openico.jpg" width="15" height="15" class="chainImg" id="chainImg<%=k%>"/>&nbsp;<span><%=lf2.getName(request)%></span>
			</div>
			
			<ul  id="leftSubMenu<%=k%>" style="display:none">
			<%
			k++;
			Iterator ir3 = v3.iterator();
			while (ir3.hasNext()) {
				Leaf lf3 = (Leaf)ir3.next();
				if (!lf3.canUserSee(request) || !lf3.isUse())
					continue;
			%>
				<li class="3level" id="menuItem<%=x%>" onclick="selectMenuItem(<%=x%>);setBackGround('menuItem<%=x%>');" isSelected=0 onmouseover="setDivBackground('menuItem<%=x%>')" onmouseout="setDivBackground('menuItem<%=x++%>')">
				<div class="listbox1" onclick="storeMenu('<%=lf3.getCode()%>', true);openPage(this, '<%=lf3.getName(request)%>','<%=lf3.isCanRepeat()%>','true','<%=lf3.getLink(request)%>','<%=lf3.getTarget()%>')">
					<%=lf3.getName(request)%>
				</div>
				</li>
				
			<%}%>
			</ul>
			</li>
		<%}%>
	<%}%>
	</ul>
	<%}
	else
		k++;
	%>
<%}%>	

				<!--<div> <div class="boxbg" id="leftMenu103" isOpen=0 onclick="javascript:window.open('help/frame.html','_blank')" onmouseover="setDivBackground('leftMenu103')" onmouseout="setDivBackground('leftMenu103')"><span class="span-1"><img src="<%=skinPath%>/images/ico_help.png" width="30" height="30" /></span><span>帮助</span></div></div>-->
			</div> 

			<div id="menuMain3" style="display:none;overflow:auto;" class="onlineCss">
			<div id="menuOnline">
<%
	DeptMgr dm = new DeptMgr();
	DeptUserDb du = new DeptUserDb();

	OnlineUserDb oud = new OnlineUserDb();
	Iterator iOud = oud.list().iterator();
	while (iOud.hasNext()) {
		oud = (OnlineUserDb)iOud.next();
		UserDb user = um.getUserDb(oud.getName());
		
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
		 
		String mstr = "<a target='mainFrame' href='message_oa/send.jsp?receiver=" + StrUtil.UrlEncode(oud.getName()) + "'>" + "消息" + "</a>";
%>
		<div>
<%
	if (isRTXUsed) {
%>
		<img align="absbottom" width=16 height=16 
		<%
			if (user.getGender() == 0)
			{
		%>
		
		src="images/man.png" 
		<%
			}
			else
			{
		%>
			src="images/woman.png" 
		
		<%
			}
		%>
		onload="RAP('<%=oud.getName()%>');" />
<%
	}
%>
	&nbsp;<%=deptName%>&nbsp;<a title="登录时间：<%=DateUtil.format(oud.getLogTime(), "yyyy-MM-dd HH:mm:ss")%>" onClick="addTab('<%=user.getRealName()%>', 'user_info.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>')" target="mainFrame"><%=user.getRealName()%></a>&nbsp;&nbsp;<a href="message_oa/message_ext/send.jsp?receiver=<%=StrUtil.UrlEncode(oud.getName())%>" target="mainFrame" style="display:none">消息</a>
		</div>
<%
	}
%>
				</div>
			</div>
		</div>
		<div id="leftMenuBottom" style="display:none"><img src="<%=skinPath%>/images/left_menu_bottom.png" /></div>
	</div>
    
    
    </div>
</body>
</html>
