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
#container{height:100%; width:50%; float:left}
#photo{height:100%; width:48%; float:left; padding:10px}
</style>
<script type="text/javascript" src="http://api.map.baidu.com/api?v=1.5&ak=3dd31b657f333528cc8b581937fd066a"></script>
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<!--v1.5-->
</head>
<body>
<jsp:useBean id="cfg" scope="page" class="cn.js.fan.web.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	if (!privilege.isUserPrivValid(request, "read")) {
	    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	
	String maps= ParamUtil.get(request,"locationMaps");
	if(maps == null || maps.trim().equals("")){
		 out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_maps")));
		 return;
	}
	
	String[] mapArr = maps.split(",");
	double lontitude = 0;
	double latitude = 0;
	String address = "";
	
	if(mapArr!=null && mapArr.length  == 3){
		lontitude = Double.parseDouble(mapArr[0]);
		latitude = Double.parseDouble(mapArr[1]);
		address = mapArr[2];
	}
%>

<div id="container" style="width:80%;height: 80%;margin:0 auto;" >
</div>
<script type="text/javascript">
	// 百度地图API功能
	var map = new BMap.Map("container");
	var lontitude = <%=lontitude%>;
	var latitude = <%=latitude%>
	var address ='<%=address%>';
	var point = new BMap.Point(lontitude,latitude);
	map.enableScrollWheelZoom(true);
	map.centerAndZoom(point, 18);
	var marker = new BMap.Marker(point);  // 创建标注
	map.addOverlay(marker);              // 将标注添加到地图中
	var label = new BMap.Label(address,{offset:new BMap.Size(20,-10)});
	marker.setLabel(label);
</script>
</body>
</html>