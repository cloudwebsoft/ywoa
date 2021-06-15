<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.map.*" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>地图</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <style type="text/css">
        html {
            height: 100%
        }

        body {
            height: 100%;
            margin: 0px;
            padding: 0px;
            background-color: #efefef
        }

        <%
        String mode = ParamUtil.get(request, "mode");
        if ("show".equals(mode)) {
        %>
        #container {
            height: 100%;
            width: 100%;
            float: left
        }

        #info {
            height: 100%;
            width: 0%;
            float: left;
            padding: 5px;
            overflow-x: scroll;
            display: none
        }

        <%
        }
        else {
        %>
        #container {
            height: 100%;
            width: 70%;
            float: left
        }

        #info {
            height: 100%;
            width: 28%;
            float: left;
            padding: 5px;
            overflow-x: scroll
        }

        <%}%>
        #infoTitle {
            height: 30px;
            font-weight: bold;
        }
    </style>
    <script type="text/javascript" src="https://api.map.baidu.com/api?v=1.5&ak=3dd31b657f333528cc8b581937fd066a"></script>
    <script type="text/javascript" src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script type="text/javascript" src="../js/jquery.toaster.flow.js"></script>
</head>
<body>
<jsp:useBean id="cfg" scope="page" class="cn.js.fan.web.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "read")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String maps = ParamUtil.get(request, "locationMaps");
    String[] mapArr = maps.split(",");
    String lng = "";
    String lat = "";
    String address = "";

    if (mapArr != null && mapArr.length == 3) {
        lng = mapArr[0];
        lat = mapArr[1];
        address = mapArr[2];
    }
%>
<div id="container"></div>
<div id="info">
    <div id="infoTitle">请点击地图选择位置</div>
    <div id="infoAddr">
        <input id="addr" name="addr" value="<%=address%>"/>
        <input type="button" value="选择" onclick="doSel()"/>
        <input type="hidden" name="lng" value="<%=lng%>"/>
        <input type="hidden" name="lat" value="<%=lat%>"/>
    </div>
    <div id="address">
    </div>
</div>
<script type="text/javascript">
    $(function () {
        var map = new BMap.Map("container");          // 创建地图实例
        var lng = 116.331398;
        var lat = 39.897445;
        <%
        if(mapArr!=null && mapArr.length  == 3) {
        %>
        lng = <%=lng%>;
        lat = <%=lat%>;
        <%
        }
        %>
        var point = new BMap.Point(lng, lat);  // 创建点坐标
        map.centerAndZoom(point, 16);          // 初始化地图，设置中心点坐标和地图级别
        <%
        if(mapArr!=null && mapArr.length  == 3) {
        %>
        map.addOverlay(new BMap.Marker(point));
        map.centerAndZoom(point, 16);
        <%}%>

        map.addControl(new BMap.NavigationControl());
        map.addControl(new BMap.ScaleControl());
        map.addControl(new BMap.OverviewMapControl()); // 缩略地图
        map.addControl(new BMap.GeolocationControl()); // 定位
        // map.addControl(new BMap.MapTypeControl());
        // map.setCurrentCity("北京"); // 仅当设置城市信息时，MapTypeControl的切换功能才能可用

        // 开启滚轮缩放地图
        map.enableScrollWheelZoom();

        <%if(mapArr==null || mapArr.length != 3) {%>
        // 进行浏览器定位
        var geolocation = new BMap.Geolocation();
        geolocation.getCurrentPosition(function (r) {
            // 定位成功事件
            if (this.getStatus() == BMAP_STATUS_SUCCESS) {
                //alert('您的位置：'+r.point.lng+','+r.point.lat);
                var point = new BMap.Point(r.point.lng, +r.point.lat);
                map.centerAndZoom(point, 16);
                map.addOverlay(new BMap.Marker(point));
            }
        }, {enableHighAccuracy: true})
        <%}%>

        <%if (!"show".equals(mode)) {%>
        // addEventListener--添加事件监听函数
        // click--点击事件获取经纬度
        map.addEventListener("click", function (e) {
            // prompt("鼠标单击地方的经纬度为：",e.point.lng + "," + e.point.lat);
            map.clearOverlays();
            map.addOverlay(new BMap.Marker(e.point));
            displayPOI(e.point);
        });
        <%}%>

        $.toaster({priority : 'info', message : "请在地图上点选位置" });
    })

    var mOption = {
        poiRadius: 500,           // 半径为1000米内的POI,默认100米
        numPois: 12               // 列举出50个POI,默认10个
    }

    var myGeo = new BMap.Geocoder();        //创建地址解析实例
    function displayPOI(mPoint) {
        // map.addOverlay(new BMap.Circle(mPoint,500));        //添加一个圆形覆盖物
        $('#address').html("");
        myGeo.getLocation(mPoint,
            function mCallback(rs) {
                var allPois = rs.surroundingPois;       //获取全部POI（该点半径为100米内有6个POI点）
                for (i = 0; i < allPois.length; ++i) {
                    document.getElementById("address").innerHTML += "<p style='font-size:12px;'><a href='javascript:;' onclick=\"sel(" + allPois[i].point.lng + "," + allPois[i].point.lat + ",'" + allPois[i].title + "')\">" + (i + 1) + "、" + allPois[i].title + "，地址:" + allPois[i].address + "</a></p>";
                    // map.addOverlay(new BMap.Marker(allPois[i].point));
                }
            }, mOption
        );
    }

    function sel(lng, lat, addr) {
        o("lng").value = lng;
        o("lat").value = lat;
        o("addr").value = addr;
    }

    function doSel() {
        window.opener.setIntpuObjValue(o("lng").value + "," + o("lat").value + "," + o("addr").value, o("addr").value);
        window.close();
    }
</script>
</body>
</html>