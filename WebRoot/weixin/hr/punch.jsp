<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.attendance.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.android.Privilege" %>
<%@ page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<!doctype html>
<html>
<head>
    <meta charset="utf-8">
    <title>打卡</title>
    <%@ include file="../../inc/nocache.jsp" %>
    <meta name="viewport" content="width=device-width,initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no"/>
    <style>
        .btn-box {
            width: 130px;
            height: 130px;
            padding: 0px;
            margin: 0px auto;
        }

        ul {
            list-style: none;
            padding: 0px;
            margin: 0px;
        }

        li {
            font: 14px/10px Arial, Verdana, sans-serif;
            color: #000;
            width: 120px;
            height: 120px;
            padding: 5px;
            margin: 0px auto;
            -webkit-border-radius: 75px;
            -moz-border-radius: 75px;
            border-radius: 75px;
            box-shadow: 1px 1px 1px #666;
        }

        li#btn {
            background-color: #FF9933;
        }

        li a {
            color: #000;
            text-decoration: none;
            display: block;
            width: 120px;
            height: 75px;
            text-align: center;
            padding: 45px 0 0 0;
            margin: 0px;
            -webkit-border-radius: 60px;
            -moz-border-radius: 60px;
            border-radius: 60px;
            font-size: 14px;
        }

        li#btn a {
            background-color: #ffffff;
            /*
            立体内陷效果
            -o-box-shadow: 1px 10px 5px #B04600 inset;
            -ms-box-shadow: 1px 10px 5px #B04600 inset;
            -webkit-box-shadow: 1px 10px 5px #B04600 inset;
            box-shadow: 1px 10px 5px #B04600 inset
            */
        }

        li a:hover, li a:focus, li a:active {
            width: 120px;
            height: 85px;
            padding: 35px 0 0 0;
            margin: 0px;
            -webkit-border-radius: 60px;
            -moz-border-radius: 60px;
            border-radius: 60px;
        }

        li a:hover, li a:focus, li a:active {
            -webkit-animation-name: bounce;
            -webkit-animation-duration: 1s;
            -webkit-animation-iteration-count: 4;
            -webkit-animation-direction: alternate;
        }

        .locBox {
            width: 80%;
            text-align: center;
            margin: 80px auto;
        }
    </style>
    <script type="text/javascript" src="https://api.map.baidu.com/api?v=2.0&ak=3dd31b657f333528cc8b581937fd066a"></script>
    <script src="../../inc/common.js"></script>
    <script src="../../js/jquery-1.9.1.min.js"></script>
    <script src="../../js/jquery-migrate-1.2.1.min.js"></script>
    <link rel="stylesheet" href="../../js/toastr/toastr.min.css">
    <script src="../../js/toastr/toastr.min.js"></script>
</head>
<body>
<%
    String code = ParamUtil.get(request, "code");
    Privilege pvg = new Privilege();
    pvg.auth(request);

    String userName = pvg.getUserName();
    UserDb user = new UserDb();
    user = user.getUserDb(userName);

    String skey = pvg.getSkey();

    Date dt = new Date();
    Object[] ary = ShiftScheduleMgr.getShiftDAO(userName, dt);
    FormDAO fdao = (FormDAO) ary[0];
    String location = "", radius = "";
    double lngLoc = 0, latLoc = 0;
    boolean isAbnormal = false;
    boolean hasLocation = false;

    out.print("<div>打卡人：" + user.getRealName() + "</div>");

    if (fdao != null) {
        out.print("<div>班次：" + fdao.getFieldValue("name") + "</div>");
        location = fdao.getFieldValue("location");
        String[] aryLoc = StrUtil.split(location, ",");
        if (aryLoc != null) {
            hasLocation = true;
            lngLoc = StrUtil.toDouble(aryLoc[0]);
            latLoc = StrUtil.toDouble(aryLoc[1]);
            location = aryLoc[2];
        }
        radius = fdao.getFieldValue("radius");
        isAbnormal = fdao.getFieldValue("is_abnormal").equals("1");
    }

    if ("".equals(radius)) {
        radius = "200";
    }
%>
<div class="locBox">
    <div><img src="../../images/location_mark.png" style="width:32px"/></div>
    <%if (fdao == null) { %>
    <div>当前没有排班记录！</div>
    <%} else {%>
    <div><%=location %>
    </div>
    <div>有效距离<%=radius %>米</div>
    <div id="msgBox"></div>
    <%} %>
</div>
<%
    String dtStr = DateUtil.format(new java.util.Date(), "MM-dd");
    String timeStr = DateUtil.format(new java.util.Date(), "HH:mm:ss");
    int[] aryPunch = new int[2];
    aryPunch[0] = -1;
    aryPunch[1] = AttendanceMgr.NORMAL;
    String typeDesc;
    if (fdao != null) {
        aryPunch = AttendanceMgr.getPunchType(userName, new java.util.Date());
        if (aryPunch[0] == -1) {
            typeDesc = "非打卡时间";
        } else {
            // 如果已打卡
            if (AttendanceMgr.isPunched(userName, aryPunch[0])) {
                typeDesc = AttendanceMgr.getPunchTypeDesc(aryPunch[0]) + "已打卡";
                aryPunch[0] = -1;
            } else {
                typeDesc = AttendanceMgr.getPunchTypeDesc(aryPunch[0]) + AttendanceMgr.getPunchStatusDesc(aryPunch[1]) + "打卡";
            }
        }
        if (aryPunch[0] == -1) {
%>
<script>
    $(function () {
        $('#btn').css("background-color", "#ccc");
    });
</script>
<%
        }
        if (!hasLocation) {
            aryPunch[0] = -1;
            typeDesc = "未设置打卡地点";
        }
    } else {
        typeDesc = "无需打卡";
    }
%>
<div class="btn-box">
    <ul>
        <li id="btn">
            <a href="javascript:;" onclick="punch(<%=aryPunch[0] %>)">
        <span style="line-height:1.5">
        	<%if (aryPunch[0] != -1) { %>
			<span><%=dtStr %></span>
			&nbsp;
			<span id="spanTime"> <%=timeStr %> </span> <br/>
			<%} %>
        	<%=typeDesc %>
        </span>
            </a>
        </li>
    </ul>
</div>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=pvg.getSkey()%>" />
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
<script>
    var type = <%=aryPunch[0]%>;
    var isLocationAbnormal = false;
    function punch(type) {
        if (type == -1) {
            toastr.warning("不能打卡！");
            return;
        }
        toastr.options = {
            closeButton: false,
            debug: false,
            progressBar: false,
            positionClass: "toast-bottom-center",
            onclick: null,
            showDuration: "300",
            hideDuration: "1000",
            timeOut: "5000",
            extendedTimeOut: "1000",
            showEasing: "swing",
            hideEasing: "linear",
            showMethod: "fadeIn",
            hideMethod: "fadeOut"
        };
        if (isLocationAbnormal && !<%=isAbnormal%>) {
            toastr.warning("位置异常，不能打卡！");
            return;
        }
        var geolocation = new BMap.Geolocation();
        geolocation.getCurrentPosition(function (r) {
            // 定位成功事件
            if (this.getStatus() == BMAP_STATUS_SUCCESS) {
                // console.log('您的位置：'+r.point.lng+','+r.point.lat);
                var point = new BMap.Point(r.point.lng, +r.point.lat);
                lat = +r.point.lat;
                lng = r.point.lng;

                var addr = r.address;
                var address = addr.district + addr.street;
                if (addr.streetNumber) {
                    address += addr.streetNumber;
                }

                $.ajax({
                    type: "post",
                    url: "../../attendance/punch.do",
                    data: {
                        "userName": "<%=userName%>",
                        "type": type,
                        "lng": lng,
                        "lat": lat,
                        "address": address
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        if (data.ret == 1) {
                            // 如果迟到或者早退，则说明理由
                            var url = "punch_result.jsp?skey=<%=skey%>&result=" + data.result + "&min=" + data.min + "&isLocationAbnormal=" + isLocationAbnormal;
                            url += "&address=" + encodeURI(address) + "&id=" + data.id;
                            window.location.href = url;
                        } else {
                            toastr.info(data.msg);
                        }
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        alert(XMLHttpRequest.responseText);
                    }
                });
            }
        }, {enableHighAccuracy: true});
    }

    $(function () {
        var sint = window.setInterval(function () {
            var d = new Date();
            var str = d.getHours() + ":" + d.getMinutes() + ":";
            var s = d.getSeconds();
            if (s < 10) {
                s = "0" + s;
            }
            str += s;
            $('#spanTime').html(str);
        }, 1000);

        // 如果不能打卡，则不再定位，否则在IOS上会使得点击APP左上角按钮时，要退N次才能退回主界面
        if (type == -1) {
            return;
        }
/* H5原生方法，转换为百度坐标
        navigator.geolocation.getCurrentPosition((position) => {
            let lat = position.coords.latitude;
            let lng = position.coords.longitude;
            console.log('lng1=' + lng + ' lat1=' + lat);
            const pointBak = new BMap.Point(lng, lat);
            const convertor = new BMap.Convertor();
            convertor.translate([pointBak], 1, 5,function(resPoint) {
                if(resPoint && resPoint.points && resPoint.points.length>0){
                    lng = resPoint.points[0].lng;
                    lat = resPoint.points[0].lat;
                }
                console.log('lng=' + lng + ' lat=' + lat);
                /!*                const point = new BMap.Point(lng, lat);
                                const geo = new BMap.Geocoder();
                                geo.getLocation(point, (res) => {

                                });*!/

                $.ajax({
                    type: "post",
                    url: "../../attendance/getDistance.do",
                    data: {
                        "lngLoc":<%=lngLoc%>,
                        "latLoc":<%=latLoc%>,
                        "lng": lng,
                        "lat": lat
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        if (data.ret == 1) {
                            var r = data.distance;
                            // console.log("distance=" + r);
                            if (r > <%=radius%>) {
                                isLocationAbnormal = true;
                                $('#msgBox').html("您不在打卡范围内，距离" + parseInt(r) + "米");
                            }
                            else {
                                $('#msgBox').html("您已在打卡范围内，距离" + parseInt(r) + "米");
                            }
                        } else {
                            alert("获取距离失败！");
                        }
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        alert(XMLHttpRequest.responseText);
                    }
                });
            });
        });

return;*/
        var geolocation = new BMap.Geolocation();
        geolocation.getCurrentPosition(function (r) {
            // 定位成功事件
            if (this.getStatus() == BMAP_STATUS_SUCCESS) {
                console.log('您的位置：'+r.point.lng+','+r.point.lat + " 打卡位置：<%=lngLoc%>,<%=latLoc%>");
                var point = new BMap.Point(r.point.lng, +r.point.lat);
                lat = +r.point.lat;
                lng = r.point.lng;

                $.ajax({
                    type: "post",
                    url: "../../attendance/getDistance.do",
                    data: {
                        "lngLoc":<%=lngLoc%>,
                        "latLoc":<%=latLoc%>,
                        "lng": lng,
                        "lat": lat
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        if (data.ret == 1) {
                            var r = data.distance;
                            // console.log("distance=" + r);
                            if (r > <%=radius%>) {
                                isLocationAbnormal = true;
                                $('#msgBox').html("您不在打卡范围内，距离" + parseInt(r) + "米");
                            }
                            else {
                                $('#msgBox').html("您已在打卡范围内，距离" + parseInt(r) + "米");
                            }
                        } else {
                            alert("获取距离失败！");
                        }
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        alert(XMLHttpRequest.responseText);
                    }
                });
            }
            else {
                alert('failed ' + this.getStatus());
            }
        }, {enableHighAccuracy: true})
    });

    // 强制后退时刷新
    $(document).ready(function(){
        var pagNum=performance.navigation.type;
        if(pagNum==2){
            document.location.reload();
        }
    });

    function callJS() {
        return {"btnAddShow": 0, "btnBackUrl": ""};
    }
    var iosCallJS = '{ "btnAddShow":0, "btnBackUrl":"" }';
</script>
</html>