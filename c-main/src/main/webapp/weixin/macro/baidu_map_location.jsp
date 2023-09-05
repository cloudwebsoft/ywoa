<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="com.redmoon.oa.android.Privilege"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="cn.js.fan.util.StrUtil"%>
<%@page import="com.redmoon.oa.flow.WorkflowDb"%>
<%@page import="com.redmoon.oa.flow.Leaf"%>
<!DOCTYPE HTML>
<html>
	<head>
		<title>位置</title>
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="cache-control" content="no-cache">
		<meta name="viewport"
			content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
		<meta name="apple-mobile-web-app-capable" content="yes">
		<meta name="apple-mobile-web-app-status-bar-style" content="black">
		<meta content="telephone=no" name="format-detection" />
		<link rel="stylesheet" href="../css/mui.css">
		<link rel="stylesheet" href="../css/iconfont.css" />
		<link rel="stylesheet" type="text/css"
			href="../css/mui.picker.min.css" />
		<link rel="stylesheet" href="../css/my_dialog.css" />
		<style>
			html, body, .mui-content {
				height: 100%;
			}
		</style>
	</head>

	<body>
		<header class="mui-bar mui-bar-nav">
		
		<h1 class="mui-title">
			位置
		</h1>
		<a id='comp' class="mui-btn mui-btn-link mui-pull-right mui-btn-blue ">完成</a>
		</header>
		<div class="mui-content">
			<div id="container" style="width:100%;height: 90%;margin:0 auto;" >
			</div>
			<span id="s_lo" style="margin:10px"></span>
		</div>
<%--		<script type="text/javascript" src="http://api.map.baidu.com/api?v=3.0&ak=3dd31b657f333528cc8b581937fd066a"></script>--%>
		<script type="text/javascript" src="http://api.map.baidu.com/getscript?v=3.0&ak=3dd31b657f333528cc8b581937fd066a"></script>
		<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
		<script src="../js/mui.min.js"></script>
		<script src="../js/macro/open_window_macro.js"></script>
		<% 
		String code = ParamUtil.get(request,"code");
		String val = ParamUtil.get(request,"val");
		%>
		<script type="text/javascript">
		var lat;
   		var lon;
   		var address;
   		var code = '<%=code%>';
   		var val = '<%=val%>';
   		
   		if(val == undefined || val == ""){
   			$("#comp").show();
			
			// 进行浏览器定位
			var geolocation = new BMap.Geolocation();
			geolocation.getCurrentPosition(function(r){
				// 定位成功事件
				if(this.getStatus() == BMAP_STATUS_SUCCESS){
					//alert('您的位置：'+r.point.lng+','+r.point.lat);
					var point = new BMap.Point(r.point.lng, +r.point.lat);
					lat = +r.point.lat;
					lon = r.point.lng;
					showMap(lat, lon);
				}     
			},{enableHighAccuracy: true})
			/*
			// 该方法定位不准确，有偏移
   			if(navigator.geolocation)
   		    {	
   		        navigator.geolocation.getCurrentPosition(function (p) {
   		            lat = p.coords.latitude//纬度
   		            lon = p.coords.longitude;
   		         	// 百度地图API功能
   					showMap(lat,lon);
   					
   		        }, function (error) {//错误信息
   		        	mui.toast("定位失败");
   		        }
   		        );
   		    }
			*/
   		}else{
   			$("#comp").hide();
   			var _arr= new Array(); // 定义一数组
   			_arr = val.split(","); // 字符分割
   			if(_arr.length == 3){
				lon = _arr[0];
				lat = _arr[1];
				showMap(lat,lon);
   	 	   	}
   	   	}

	    mui(".mui-bar").on("tap","#comp",function(){
	    	doneLocation(code,lat,lon,address);
		});
		function showMap(latitude,longitude){
			var map = new BMap.Map("container");
			var geoc = new BMap.Geocoder();   
			var point = new BMap.Point(longitude,latitude);
			geoc.getLocation(point, function(rs){
				var addComp = rs.addressComponents;
				address = addComp.province+ addComp.city + addComp.district  + addComp.street + addComp.streetNumber;
				$("#s_lo").html("当前位置："+address);
			});  
			map.enableScrollWheelZoom(true);
			map.centerAndZoom(point, 25);
			var marker = new BMap.Marker(point);// 创建标注
			map.addOverlay(marker);             // 将标注添加到地图中
			marker.enableDragging();           // 不可拖
		}
			
	</script>
	</body>
</html>
