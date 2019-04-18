<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="java.util.Calendar" %>
<%@ page import="cn.js.fan.db.Paginator"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="org.json.*"%>
<%@page import="com.opensymphony.xwork2.Action"%>
<%@page import="java.sql.SQLException"%>
<%@page import="com.redmoon.clouddisk.Config"%>

<!-- <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"> -->
<html>
  <head>
    
    <title>云盘</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<link type="text/css" rel="stylesheet" href="clouddisk.css"/>
	<script src="../inc/common.js"></script>
	<script src="../js/jquery.js"></script>
	<script src="../js/jquery1.7.2.min.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-showLoading/jquery.showLoading.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />  
	<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" type="text/css" media="screen" /> 
	

	<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
		 <% 
		//if (!privilege.isUserLogin(request)) {
		//	out.print("对不起，请先登录！");
		//	 return;
		//}
		String priv="read";
		boolean flag = true;
		if (!privilege.isUserPrivValid(request,priv)) {
			out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
		String userName = privilege.getUser(request);
		String root_code = ParamUtil.get(request, "root_code");
		if (root_code.equals("")) {
			 //root_code = "root";
			 root_code = userName;
		}
		Leaf leaf = new Leaf(root_code); 
		if (leaf==null || !leaf.isLoaded()) {
			Leaf leafUser = new Leaf();
			leafUser.AddUser(root_code);
			leaf =leafUser.getLeaf(root_code);
			RoleTemplateMgr roleTemplateMgr = new RoleTemplateMgr();
			try {
				flag = roleTemplateMgr.copyDirsAndAttToNewUser(root_code);
				if(!flag){
					out.print(StrUtil.jAlert("角色模板初始化失败！","提示"));
				}
			}
			catch (ErrMsgException e1) {
				out.print(StrUtil.jAlert("角色模板初始化失败！","提示"));
			} 
		}
		
		// 如果启用了客户端侧边栏的html则初始化html
		Config cfg = Config.getInstance();
		if (cfg.getBooleanProperty("is_openSideHTML")) {
			SideBarMgr sbMgr = new SideBarMgr();
			sbMgr.initialization(userName);
		}
		
		int pageNo = ParamUtil.getInt(request, "page_no", 1);
		String initPage = "";
		switch (pageNo) {
		case 1:
			initPage = "clouddisk_list.jsp";
			break;
		case 2:
			initPage = "clouddisk_myshare.jsp";
			break;
		case 3:
			initPage = "clouddisk_cooperate.jsp";
			break;
		case 4:
			initPage = "clouddisk_recycler.jsp";
			break;
		case 5:
			initPage = "clouddisk_pubilc_share.jsp";
			break;
		case 6:
			initPage = "clouddisk_network_neighborhood.jsp";
			break;
		default:
			initPage = "";
			break;
		}
		
	 %>
	<script>
	var window_height = document.documentElement.clientHeight - 65;
	var pageNo = <%=pageNo%>;
	var initPage = '<%=initPage%>';
	var expdate = new Date();
	var expday = 60;
	expdate.setTime(expdate.getTime() +  (24 * 60 * 60 * 1000 * expday));
	$(function(){
	//调整页面宽度
		//获得云盘某人界面值
		var netdiskDefaultStatus =  unescape(get_cookie("netdiskDefaultStatus"));
		if( pageNo == 1 || pageNo == 0){
			 var srcInfo = 'images/clouddisk/cloudDisk_2.gif';
			$(".cloudDisk").find("img").attr("src",srcInfo);
			 $('.cloudDisk').css({"background":"#dfe6eb","color":"#2d69b0"}).parent().siblings().find("div").css({"background":"#eff3f6","color":"#888888"});
			if(netdiskDefaultStatus == ""){
				document.cookie="netdiskDefaultStatus"+"="+escape(1)+";expires="+expdate.toGMTString();//1代表平铺界面
				initPage = 'clouddisk_tiled.jsp';
			}else if ( netdiskDefaultStatus == 1 ){ //1代表平铺
				initPage = 'clouddisk_tiled.jsp';
			}else if ( netdiskDefaultStatus == 0 ){//代表列表界面
				initPage = 'clouddisk_list.jsp'
			}
		}
		$("#Left").css({"height":window_height+"px"});
		$("#center_right").attr("src",initPage);	
		var wR=(document.all.Center.offsetWidth-181)+"px";
		$('.Right').css("width",wR);
		$('dd').css("width",wR);
		function wRight(){
		  var w=(document.all.Center.offsetWidth-181)+"px";
		  $('.Right').css("width",w);
		  $('dd').css("width",w);
		
		}
		window.onresize=wRight;
		
		});
		
		function clouddisk(){
			var netdiskDefaultStatus =  unescape(get_cookie("netdiskDefaultStatus"));
			
			if( pageNo == 1 || pageNo == 0){
				if(netdiskDefaultStatus == ""){
					document.cookie="netdiskDefaultStatus"+"="+escape(1)+";expires="+expdate.toGMTString();//1代表平铺界面
					initPage = 'clouddisk_tiled.jsp';
				}else if ( netdiskDefaultStatus == 1 ){ //1代表平铺
					initPage = 'clouddisk_tiled.jsp';
				}else if ( netdiskDefaultStatus == 0 ){//代表列表界面
					initPage = 'clouddisk_list.jsp'
				}
			}
			$("#center_right").attr("src",initPage);
		}
		function cooperation(){
			document.getElementById("center_right").src = "clouddisk_cooperate.jsp";
		}
		function recycler(){
			document.getElementById("center_right").src = "clouddisk_recycler.jsp";
		}

		function share(){
			document.getElementById("center_right").src = "clouddisk_myshare.jsp";
		}
		function publicShare(){
			document.getElementById("center_right").src = "clouddisk_pubilc_share.jsp";
		}
		function sidebarSetup(){
			document.getElementById("center_right").src = "clouddisk_sidebar_setup.jsp?userName=<%=StrUtil.UrlEncode(userName) %>";
		}
		function onlineNetwork(){
			document.getElementById("center_right").src = "clouddisk_network_neighborhood.jsp";
		}
	  	var now=new Date();
		var yy=now.getYear();
		if (!isIE() || isIE9 || isIE10 || isIE11)
			yy = 1900 + yy;
		var MM=now.getMonth()+1;
		var dd=now.getDate();
		var DD=now.getDay();
		var x = new Array("星期日","星期一","星期二","星期三","星期四","星期五","星期六");
		var date = yy+"年"+MM+"月"+dd+"日"+"  "+x[DD]+"  ";
		function refreshCalendarClock(){
			var now=new Date();
			var hh=now.getHours();
			var mm=now.getMinutes();
			var ss=now.getTime()%60000;
			ss=(ss-(ss%1000))/1000;
			if(hh<10)hh="0"+hh;
			if(mm<10)mm="0"+mm;
			if(ss<10)ss="0"+ss;
			// $("tdTime").innerHTML=date+hh+":"+mm+":"+ss;
			$("#spanDate").html(date);
			$("#spanTime").html(hh+":"+mm+":"+ss);
		}
		
		function init() {
			refreshSpace();
			window.setInterval("refreshSpace()",3000);	
		}

		function refreshSpace() {
			$.ajax({
					type:"post",
					url:"clouddisk_list_do.jsp",
					data:{"op":"space"},
					success:function(data,status){
						data = $.parseJSON(data);
						if(data.ret == "1"){ 
							$("#diskspace").html(data.msg);
						}
					},
					error:function(XMLHttpRequest, textStatus){
						//alert(XMLHttpRequest.responseText);
					}
				});
		}
</script>

  </head>
  
 <body id="loading" onload="init()" scroll="no" >

<div style="width:100%; margin:auto;min-width:1000px"  >
  <div id="Top" class="Top">
    <div class="logo"></div>
    <div class="infoDetail">&nbsp; 
    		<span class="name">您好：<%
    		UserDb ud = new UserDb();
			ud = ud.getUserDb(userName);
      		out.print(ud.getRealName()); 
      		%>
      		</span>
      		
			容量：
    		<span id='diskspace'>
    		</span>
    		<%if (!com.redmoon.oa.kernel.License.getInstance().isOem() && cfg.getBooleanProperty("is_openSideDiy")) { %>
      		<span id="sideBar">
      			<a onclick="sidebarSetup()"><img src="images/settings-25.png" alt="自定义侧边栏" title="自定义侧边栏" /></a>
      		</span>
      		<%} %>
    </div> 
  </div>
  
  <div id="Center" class="Center">
    <div id="Left" class="Left">
      <a ><div class="cloudDisk" onclick="clouddisk();" >
         <div style="text-align:center; ">
         	<%if (pageNo == 1) { %>
        		<img style="border:0" src="images/clouddisk/cloudDisk_2.gif" />
         	<%} else { %>
         		<img style="border:0" src="images/clouddisk/cloudDisk_1.gif" />
         	<%} %>
		   <p>我的云盘</p> 
         </div>
      </div></a>
      <a ><div class="shared">
        <div style="text-align:center; " onclick="share()">
        	<%if (pageNo == 2) { %>
          		<img style="border:0" src="images/clouddisk/shared_2.gif" />
         	<%} else { %>
          		<img style="border:0" src="images/clouddisk/shared_1.gif" />
         	<%} %>
		  <p>我发起的协作</p> 
        </div>
      </div></a>
     <a><div class="cooperate" onclick="cooperation()">
        <div style="text-align:center; ">
        	<%if (pageNo == 3) { %>
          		<img style="border:0" src="images/clouddisk/cooperate_2.gif" />
         	<%} else { %>
         		<img style="border:0" src="images/clouddisk/cooperate_1.gif" />
         	<%} %>
		  <p>我参与的协作</p> 
        </div>
      </div></a>
      
       <a ><div class="source">
        <div style="text-align:center; " onclick="publicShare()">
        	<%if (pageNo == 5) { %>
          		<img style="border:0" src="images/clouddisk/source_2.gif" />
         	<%} else { %>
          		<img style="border:0" src="images/clouddisk/source_1.gif" />
         	<%} %>
		  <p>资源库</p> 
        </div>
      </div></a>
      <a><div class="network" onclick="onlineNetwork();">
        <div style="text-align:center; ">
        	<%if (pageNo == 6) { %>
          		<img style="border:0" src="images/clouddisk/network_2.gif" />
         	<%} else { %>
         		<img style="border:0" src="images/clouddisk/network_1.gif" />
         	<%} %>
		  <p>网上邻居</p> 
        </div>
      </div>
      </a>
      <a><div class="recycle" onclick="recycler();">      
         <div style="text-align:center; ">
         	<%if (pageNo == 4) { %>
          		<img style="border:0" src="images/clouddisk/recycle_2.gif" />
         	<%} else { %>
          		<img style="border:0" src="images/clouddisk/recycle_1.gif" />
         	<%} %>
		 <p>回收站</p>
        </div>
      </div></a>
      

      <script>
        $(function(){
		  $(".Left a").click(
		   
		    function(){
			  var a=$(this).find("div").attr("class");
			  var src = 'images/clouddisk/'+a+'_2.gif';
			  $(this).find("div div img").attr("src",src);
			  $(".Left a").each(function(index, element) {	
			  	var srcClass = $(this).find("div").attr("class");
			  	if(srcClass != a){
					 var srcInfo = 'images/clouddisk/'+ srcClass + '_1.gif';
				     $(this).find("div div img").attr("src",srcInfo);
				}
			  }
             );
		      $(this).find("div").css({"background":"#dfe6eb","color":"#2d69b0"}).parent().siblings().find("div").css({"background":"#eff3f6","color":"#888888"});
			})
		    
		})
		
		
      </script>
      
    </div>
    <div style="float:left; width:88.9%">
	  	 <iframe id="center_right" name="center_right"  frameborder="0" border="0" width="100%" style="overflow-y:hidden; min-width:1000px;height:605px;float:left">
		</iframe>
	</div>
	
  </div>
  

</div>
</body>
</html>
