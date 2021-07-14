<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import ="com.redmoon.forum.plugin.*" %>
<%@ page import ="java.util.*" %>
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
request.setAttribute("skinPath", skinPath);	
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
<script src="inc/common.js"></script>
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-migrate-1.2.1.min.js"></script>
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
		$("#leftMenuMiddle").height($("body").height());
		
	});
	
	function changeMenuClass(obj){
		removeAllMenusClass()
		//给自己增加样式
		jQuery(obj).addClass('boxbgHover');
	}
	
	//删除所有一级菜单选中的样式
	function removeAllMenusClass(){
		jQuery('#menuMain1').children('div').each(function(i,v){
			if(jQuery(this).hasClass('boxbgHover')){
				jQuery(this).removeClass('boxbgHover');
			}
		})
	}
	
	function showChildrenMenu(obj,parentCode,hasLink) {
		changeMenuClass(obj);
		if(hasLink == "hasLink"){
			top.mainFrame.hideSecondLevelMenu();
			return;
		}
		try {
			top.mainFrame.showCurrentSecondLevelMenu(parentCode);
		} catch (e){}
	}
	
	function openOrCloseChildrenMenu(obj,parentCode){
		top.mainFrame.currentMenuShowOrHide(parentCode);
	}
	
	function hideClass(){
		top.mainFrame.judgeMenuShowOrHide();
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
			//jQuery(".submenuul").not("[id='leftSubMenu" + num + "']").hide();
			jQuery(".boxbg").not("[id='" + id + "']").children(".span-2").css('color','#606060');//将其他一级菜单的字体改成默认颜色
			$(".boxbg").not("[id='" + id + "']").children("span").children("img[id^='chainImg'][id!='chainImg"+num+"']").attr("src", "<%=skinPath%>/images/arrownormal.png");//修改其他一级菜单的箭头指向
			//jQuery("#menuFavorite > .submenuul").show();
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
		//refresh();
		//var allHeight = document.documentElement.clientHeight ;
		//document.getElementById("menuMain1").style.height = allHeight - 46 + "px";
		
		//获取光标
		//document.body.focus();  
		//禁止退格键 作用于Firefox、Opera   
	    document.onkeypress = banBackSpace;  
	    //禁止退格键 作用于IE、Chrome  
	    document.onkeydown = banBackSpace; 
	}

function openPage(obj, title, canRepeat, hideFocus, link, target) {
	if (isIE() && window.event.shiftKey) {
			 if (obj.target=="mainFrame") {
				window.open(link);
			 }
			removeAllMenusClass();
			return false;
	}
	if (target =="_blank") {
		if (link == null) {
			removeAllMenusClass();
			return false;
		}
		window.open(link);
		stopBubble(obj);
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
</head>
<body onload="init()" onunload="check()">
	<div class="v2main">
		<div id="leftMenuMiddle" class="leftsub">
			<div id="menuMain1" class="menuMain1">
				
<%
com.redmoon.oa.ui.menu.LeafChildrenCacheMgr lccm = new com.redmoon.oa.ui.menu.LeafChildrenCacheMgr(Leaf.CODE_ROOT);
Iterator ir = lccm.getChildren().iterator();
//得到有效的可显示的菜单数
int x=0;
boolean hasSystemMenu = false;
while (ir.hasNext()) {
	com.redmoon.oa.ui.menu.Leaf lf = (com.redmoon.oa.ui.menu.Leaf)ir.next();
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
	x++;
	
	if(lf.getCode().equals("super_m")){
		hasSystemMenu = true;
	}
}
ir = lccm.getChildren().iterator();
int k=2;
int menusCount = 0;
while (ir.hasNext()) {
	com.redmoon.oa.ui.menu.Leaf lf = (com.redmoon.oa.ui.menu.Leaf)ir.next();
	if(x>9){//如果x>9,循环显示7个，倒数第二个是“系统”菜单，倒数第一个是“更多”菜单；否则全部显示
		menusCount++;
		if(hasSystemMenu){
			if(k>8){
				break;
			}
			if(lf.getCode().equals("super_m")){
				continue;
			}
		}else{
			if(k>9){
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
//先显示一级菜单
//有链接，直接打开tab页
if (!lf.getLink(request).equals("")) {%>
	<div class="boxbg" id="leftMenu<%=k%>" isOpen=0 onmouseleave="hideClass();" onclick="removeAllMenusClass();openPage(this, '<%=lf.getName(request)%>','<%=lf.isCanRepeat()%>','true','<%=lf.getLink(request)%>','<%=lf.getTarget()%>')" onmouseenter="showChildrenMenu(this,'<%=lf.getCode()%>','hasLink')" >
<%//无链接，打开/关闭子菜单
}else {
%>
	<div class="boxbg" id="leftMenu<%=k%>" isOpen=0 onmouseenter="showChildrenMenu(this,'<%=lf.getCode()%>')" onmouseleave="hideClass();" onclick="openOrCloseChildrenMenu(this,'<%=lf.getCode()%>');">
<%} %>
		<p ><img src="<%=skinPath%>/icons/<%=lf.getIcon()%>" width="30" height="30" /></p><!-- 一级菜单图标 -->
		<p ><%=lf.getName(request)%></p><!-- 一级菜单名称 -->
	</div>
<%
k++;
}

if(x>9){//x>9,循环显示7个，倒数第二个是“系统”菜单，倒数第一个是“更多”菜单；
	if(hasSystemMenu){
		com.redmoon.oa.ui.menu.Leaf lf1 = new com.redmoon.oa.ui.menu.Leaf("super_m");
%>	
	<div class="boxbg" id="leftMenu<%=k%>" isOpen=0 onmouseenter="showChildrenMenu(this,'<%=lf1.getCode()%>')" onmouseleave="hideClass();" onclick="openOrCloseChildrenMenu(this,'<%=lf1.getCode()%>');">
		<p ><img src="<%=skinPath%>/icons/<%=lf1.getIcon()%>" width="30" height="30" /></p><!-- 一级菜单图标 -->
		<p ><%=lf1.getName(request)%></p><!-- 一级菜单名称 -->
	</div>
	<%} %>
	<div class="boxbg" id="leftMenu<%=++k%>" isOpen=0 onmouseenter="showChildrenMenu(this,'allOthers')" onmouseleave="hideClass();" onclick="openOrCloseChildrenMenu(this,'allOthers');">
		<p ><img src="images/desktop/icon_more.png" width="30" height="30" /></p><!-- 一级菜单图标 -->
		<p >更多</p><!-- 一级菜单名称 -->
	</div>
<%}%>	
			</div> 
		</div>
		<div id="leftMenuBottom" style="display:none"><img src="<%=skinPath%>/images/left_menu_bottom.png" /></div>
	</div>
    </div>
</body>
</html>
