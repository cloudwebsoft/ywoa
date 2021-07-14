<%@ page contentType="text/html;charset=gb2312" %>
<%@ page import="com.redmoon.oa.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<!DOCTYPE html>
<html>
<head>
    <title>待办流程</title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection"/>
    <link rel="stylesheet" href="css/mui.css">
    <link rel="stylesheet" href="css/iconfont.css"/>
    <link rel="stylesheet" href="css/mui.picker.min.css">
    <link rel="stylesheet" href="css/at_flow.css"/>
    <link rel="stylesheet" href="css/my_dialog.css"/>

    <link href="../lte/css/bootstrap.min.css?v=3.3.6" rel="stylesheet">
    <link href="../lte/css/font-awesome.css?v=4.4.0" rel="stylesheet">
    <link href="../lte/css/animate.css" rel="stylesheet">
    <link href="../lte/css/style.css?v=4.1.0" rel="stylesheet">
</head>
<style>
    body {
        font-size: 17px;
        background-color: #efeff4;
    }

    .mui-input-row .input-icon {
        width: 50%;
        float: left;
    }

    .mui-input-row a {
        margin-right: 10px;
        float: right;
        text-align: left;
        line-height: 1.5;
    }

    .div_opinion {
        text-align: left;
    }

    .opinionContent {
        margin: 10px;
        width: 65%;
        float: right;
        font-weight: normal;
    }

    .opinionContent div {
        text-align: right;
    }

    .opinionContent div span {
        padding: 10px;
    }

    .opinionContent .content_h5 {
        color: #000;
        font-size: 17px;
    }

    #captureFile {
        display: none;
    }

    .reply-date {
        margin-left: 10px;
    }

    .reply-header {
        color: #666;
    }

    .reply-content {
        margin: 20px 0px 10px 0px;
        color: #666;
    }

    .reply-progress {
        margin: 0px 10px;
    }
</style>
<body>
<script>
    function cameraCallback(imageData) {
        var img = createImageWithBase64(imageData);
        document.getElementById("cameraWrapper").appendChild(img);
    }
    function photolibraryCallback(imageData) {
        var img = createImageWithBase64(imageData);
        document.getElementById("photolibraryWrapper").appendChild(img);
    }

    function albumCallback(imageData) {
        var img = createImageWithBase64(imageData);
        document.getElementById("albumWrapper").appendChild(img);
    }

    function createImageWithBase64ForIOS(imageData) {
        var img = new Image();
        img.src = "data:image/jpeg;base64," + imageData;
        img.style.width = "50px";
        img.style.height = "50px";
        return img;
    }

</script>
<p style="text-align:center;padding:20px;">
<ul>
    <li class="mui-button-row">
        <input id="btnCapure" type="button" value="拍照按钮" class="capture_btn"/>
    </li>
    <li class="mui-button-row">
        <input id="captureFile" name="upload" type="file" accept="image/*" value="拍照"/>
        拍照123
    </li>
    1
</ul>
<a href="js-call://camera/cameraCallback">拍照</a>
<a href="js-call://photolibrary/photolibraryCallback">图库</a>
<a href="js-call://album/albumCallback">相册</a>
</p>

<fieldset>
    <legend>拍照</legend>
    <div id="cameraWrapper">
    </div>
</fieldset>

<fieldset>
    <legend>图库</legend>
    <div id="photolibraryWrapper">
    </div>
</fieldset>

<fieldset>
    <legend>相册</legend>
    <div id="albumWrapper">
    </div>
</fieldset>

1、multiple="multiple" capture="camera"
安卓表现为 可拍照可图库 ios 只可拍照
2、multiple="multiple" 无capture
安卓表现为 只可拍照 ios可拍照可图库
3、无multiple capture="camera"
安卓表现为 只可图库 ios只可拍照
4、无multiple 无capture
安卓ios 都可拍照可图库
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
<script src="js/macro/macro.js"></script>
<script src="js/mui.min.js"></script>
<script src="js/mui.picker.min.js"></script>
<script type="text/javascript" src="js/base/mui.form.js"></script>
<script type="text/javascript" src="js/mui.flow.wx.js"></script>

</body>
<script>
    document.getElementById("btnCapure").onclick = function () {
        document.getElementById('captureFile').click();
    }

    // IOS中mui无法唤起document.getElementById('captureFile').click()事件
    mui(".mui-button-row").on("tap", ".capture_btn", function () {
        captureFieldName = jQuery(this).attr("captureFieldName");
        // var cap = jQuery("#captureFile").get(0);
        // cap.click();
        // alert(cap.name + " value=" + cap.value)
        // jQuery("#captureFile").click();

        document.getElementById('captureFile').click();

        if (true) return;

        if (/(iPhone|iPad|iPod|iOS)/i.test(navigator.userAgent)) {
            isIos = true;
            if (!jQuery('#btnIosCapture').get(0)) {
                jQuery('body').append('<a id="btnIosCapture" href="js-call://camera/cameraCallback">拍照</a>')
            }
            jQuery('#btnIosCapture').get(0).click();
        } else if (/(Android)/i.test(navigator.userAgent)) {
            var cap = jQuery("#captureFile").get(0);
            cap.click();
        } else {

        }
    });

</script>
</html>