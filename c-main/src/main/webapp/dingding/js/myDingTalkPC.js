$(function(){
    DingTalkPC.config({
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
    DingTalkPC.ready(function() {
        DingTalkPC.runtime.permission.requestAuthCode({
            corpId : _config.corpId,
            onSuccess : function(info) {
                $.ajax({
                    url : '../do/dingding_do.jsp?code=' + info.code ,
                    type : 'GET',
                    success : function(data, status, xhr) {
                        //alert(data);
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
    });
    DingTalkPC.error(function(err) {
        alert('dd error: ' + JSON.stringify(err));
    });
})