<%@ page language="java" import="com.redmoon.oa.android.Privilege" pageEncoding="utf-8" %>
<%@ page import="com.redmoon.dingding.service.auth.AuthService" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>应用</title>
    <script type="text/javascript">
        var _config = <%= AuthService.getConfig(request) %>;
        console.log(_config);
    </script>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="viewport"
          content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta content="telephone=no" name="format-detection"/>
    <link rel="stylesheet" href="../weixin/css/mui.css">
    <link rel="stylesheet" href="../weixin/css/my_dialog.css"/>
    <script type="text/javascript" src="../weixin/js/jquery-1.9.1.min.js"></script>
    <script src="http://g.alicdn.com/dingding/dingtalk-jsapi/2.3.0/dingtalk.open.js"></script>
</head>
<body>
<script>
/*    $(function() {
        dd.getAuthCode({
                success:(res)=>{
                dd.alert({content: res.authCode})
            },
                fail: (err)=>{
                dd.alert({content: JSON.stringify(err)})
            }
        });
    })*/

    dd.config({
        agentId : _config.agentid,
        corpId : _config.corpId,
        timeStamp : _config.timeStamp,
        nonceStr : _config.nonceStr,
        signature : _config.signature,
        jsApiList : [ 'runtime.info', 'biz.contact.choose',
            'device.notification.confirm', 'device.notification.alert',
            'device.notification.prompt', 'biz.ding.post',
            'biz.util.openLink' ]
    });

    dd.ready(function() {
        dd.runtime.permission.requestAuthCode({
            corpId : _config.corpId,
            onSuccess : function(info) {
                $.ajax({
                    url : '../public/dingding/loginByCode?code=' + info.code ,
                    type : 'POST',
                    success : function(res, status, xhr) {
                        var data = res.data;
                        window.location.href = "../weixin/message/msg_new_list.jsp?skey=" + data.skey;
                    },
                    error : function(xhr, errorType, error) {
                        alert(errorType + ', ' + error);
                    }
                });

            },
            onFail : function(err) {
                alert('fail: ' + JSON.stringify(err));
            }
        });
    })

    dd.error(function(error){
        /**
         {
               errorMessage:"错误信息",// errorMessage 信息会展示出钉钉服务端生成签名使用的参数，请和您生成签名的参数作对比，找出错误的参数
               errorCode: "错误码"
            }
         **/
        alert('dd error: ' + JSON.stringify(error));
    });
</script>
</body>
</html>
