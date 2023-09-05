<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.map.*"%>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>地图</title>
<style type="text/css">
html{height:100%}
body{height:100%;margin:0px;padding:0px}
#container{height:100%}
</style>
<script type="text/javascript" src="http://api.map.baidu.com/api?v=3.0&ak=3dd31b657f333528cc8b581937fd066a"></script>
<script type="text/javascript" src="../../inc/common.js"></script>
<script src="../../js/jquery-1.9.1.min.js"></script>
<script src="../../js/jquery-migrate-1.2.1.min.js"></script>
</head>
<body>
<jsp:useBean id="cfg" scope="page" class="cn.js.fan.web.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
long id = ParamUtil.getLong(request, "id", -1);
if (id==-1) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id")));
	return;
}

LocationDb ld = new LocationDb();
ld = ld.getLocationDb(id);
String lontitude = String.valueOf(ld.getDouble("lontitude"));
String latitude = String.valueOf(ld.getDouble("latitude"));

String client = StrUtil.getNullStr(ld.getString("client"));
%>
<span id="content" style="display:none"><%=ld.getString("address")%>
<br>
时间：<%=DateUtil.format(ld.getDate("create_date"), "yyyy-MM-dd HH:mm:ss")%></span>

<div id="container"></div>
<script type="text/javascript">
var latitude = <%=latitude%>;
var lontitude = <%=lontitude%>;
var map = new BMap.Map("container");          // 创建地图实例
var point = new BMap.Point(lontitude, latitude);  // 创建点坐标
map.centerAndZoom(point, 15);                 // 初始化地图，设置中心点坐标和地图级别

map.addControl(new BMap.NavigationControl());
map.addControl(new BMap.ScaleControl());
map.addControl(new BMap.OverviewMapControl());
// map.addControl(new BMap.MapTypeControl());    
// map.setCurrentCity("北京"); // 仅当设置城市信息时，MapTypeControl的切换功能才能可用

<%if (client.equals("ios")) {%>

  // http://developer.baidu.com/map/changeposition.htm 百度转换接口文档
  var PositionUrl = "http://api.map.baidu.com/geoconv/v1/?";
  function changePosition(){
	  var str  = "coords=" + lontitude + "," + latitude + "&from=1&to=5";
	  var url = PositionUrl + str;
	  

	  var script = document.createElement('script');

	  script.src = url + '&ak=3dd31b657f333528cc8b581937fd066a&callback=dealResult';
	  document.getElementsByTagName("head")[0].appendChild(script);

  }
  function dealResult(msg){				
	  if(msg.status != 0){
		  alert("坐标转换失败！");
		  return;
	  }
	  
	  lontitude = msg.result[0].x;
	  latitude = msg.result[0].y;
	  point = new BMap.Point(lontitude, latitude);  // 创建点坐标

	  map.centerAndZoom(point, 15);                 // 初始化地图，设置中心点坐标和地图级别


	var marker = new BMap.Marker(point);        // 创建标注    
	map.addOverlay(marker);                     // 将标注添加到地图中
	marker.addEventListener("click", function(){    
		var opts = {  
		 width: 250,     // 信息窗口宽度  
		 height: 100,     // 信息窗口高度  
		 title: "附近位置："  // 信息窗口标题 
		}  
		var content = o("content").innerHTML;
		var infoWindow = new BMap.InfoWindow(content, opts);  // 创建信息窗口对象  
		// map.openInfoWindow(infoWindow, map.getCenter());      // 打开信息窗口  
		map.openInfoWindow(infoWindow, point);      // 打开信息窗口  
	});


  }
  
  // 不能用$() $(document).ready，因为不起作用
  window.onload = function() {
	  changePosition();
  }


<%}else{%>
	var marker = new BMap.Marker(point);        // 创建标注    
	map.addOverlay(marker);                     // 将标注添加到地图中
	marker.addEventListener("click", function(){    
		var opts = {  
		 width: 250,     // 信息窗口宽度  
		 height: 100,     // 信息窗口高度  
		 title: "附近位置："  // 信息窗口标题 
		}  
		var content = o("content").innerHTML;
		var infoWindow = new BMap.InfoWindow(content, opts);  // 创建信息窗口对象  
		// map.openInfoWindow(infoWindow, map.getCenter());      // 打开信息窗口  
		map.openInfoWindow(infoWindow, point);      // 打开信息窗口  
	});
<%}%>
</script>
</body>
</html>