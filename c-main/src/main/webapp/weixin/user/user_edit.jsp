<%@page language="java" contentType="text/html;charset=utf-8" %>
<%@page import="cn.js.fan.util.StrUtil" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.DateUtil" %>
<%
    Privilege pvg = new Privilege();
    if (!pvg.auth(request)) {
        out.print(StrUtil.p_center("请登录"));
        return;
    }
    String skey = pvg.getSkey();
    String userName = pvg.getUserName();
    UserDb user = new UserDb();
    user = user.getUserDb(userName);
    String realName = user.getRealName();
    String mobile = user.getMobile();
    String birthday = DateUtil.format(user.getBirthday(), "yyyy-MM-dd");
    int gender = user.getGender();
    String weixin = StrUtil.getNullStr(user.getWeixin());
    String qq = user.getQQ();
    String address = user.getAddress();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>我的信息</title>
    <meta name="viewport" content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no"/>
    <link href="../css/mui.css" rel="stylesheet"/>
    <link rel="stylesheet" href="../css/iconfont.css"/>
    <link rel="stylesheet" type="text/css" href="../css/mui.picker.min.css"/>
    <link rel="stylesheet" href="../css/my_dialog.css"/>
    <style>
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

        .mui-input-row label {
            width: 30%;
            color: #000;
        }

        .mui-input-row label ~ input,
        .mui-input-row label ~ select,
        .mui-input-row label ~ textarea {
            width: 70%;
        }

        .mui-input-row input, select {
            height: 47px !important;
			padding-left: 5px !important;
        }
        #captureFile {
            display: none;
        }
		.photo {
			vertical-align: middle;
			width: 29px;
			margin-top: 10px;
		}
    </style>
</head>
<body>
<header class="mui-bar mui-bar-nav">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <h1 class="mui-title">我的信息</h1>
</header>
<div class="mui-content">
    <form class="mui-input-group" enctype="multipart/form-data">
        <div class="mui-input-row" data-code="realName" data-isnull="false">
            <label>姓名</label>
            <input id="realName" name="realName" value="<%=realName%>" class="mui-input-clear mui-input input-icon" placeholder="请输入姓名">
            <a class="capture-btn">
				<%
                    if (!"".equals(user.getPhoto())) {
                %>
             	<img class="photo" src="<%=request.getContextPath()%>/showImg.do?path=<%=user.getPhoto() %>"/>
				  <%
                  } else {
                      if (user.getGender() == 0) {
                  %>
                <img class="photo" src="<%=request.getContextPath()%>/images/man.png"/>
                <%
                } else {
                %>
                <img class="photo" src="<%=request.getContextPath()%>/images/woman.png"/>
                <%
                        }
                    }
                %>
			</a>
        </div>
        <div class="mui-input-row">
            <label>性别</label>
            <select id="gender" name="gender">
                <option value="0">男</option>
                <option value="1">女</option>
            </select>
        </div>
        <div class="mui-input-row" data-code="mobile" data-isnull="false">
            <label>手机</label>
            <input id="mobile" name="mobile" value="<%=mobile%>" class="mui-input-clear mui-input" placeholder="请输入手机号">
        </div>
        <div class="mui-input-row">
            <label>部门</label>

        </div>
        <div class="mui-input-row">
            <label>角色</label>

        </div>
        <div class="mui-input-row" data-code="birthday" data-isnull="false">
            <label>出生日期</label>
            <input type="text" id="birthday" name="birthday" value="<%=birthday%>" class="input-icon" readonly="">
            <a class="date_btn"><span class="iconfont icon-rili"></span></a>
        </div>
        <div class="mui-input-row">
            <label>微信</label>
            <input id="weixin" name="weixin" value="<%=weixin%>" class="mui-input-clear mui-input" placeholder="请输入微信号">
        </div>
        <div class="mui-input-row">
            <label>QQ</label>
            <input id="qq" name="qq" value="<%=qq%>" class="mui-input-clear mui-input" placeholder="请输入QQ号">
        </div>
        <div class="mui-input-row">
            <label>地址</label>
            <input id="address" name="address" value="<%=address%>" class="mui-input-clear mui-input" placeholder="请输入地址">
        </div>
        <input type="file" id="captureFile" name="upload" accept="image/*">
        <input name="skey" value="<%=skey%>" type="hidden"/>
    </form>
    <div class="mui-content-padded">
        <button id='btnSubmit' class="mui-btn mui-btn-block mui-btn-primary btn-ok">提交</button>
    </div>
</div>
<script src="../js/mui.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/mui.picker.min.js"></script>
<script src="../js/jq_mydialog.js"></script>
<script src="../js/macro/macro.js"></script>
<script>
    if (!mui.os.plus) {
        // 必须删除，而不能是隐藏，否则mui-bar-nav ~ mui-content中的padding-top会使得位置下移
        $('.mui-bar').remove();
    }

    (function ($, doc) {
        mui.init();

        jQuery('#gender').val('<%=gender%>');

        // 日期选择控件
        mui('.mui-input-group').on("tap", ".date_btn", function () {
            var id = this.getAttribute('id');
            var par = this.parentNode;
            var time_input = par.querySelector(".input-icon");
            var picker = new mui.DtPicker({"type": "date"});
            picker.show(function (rs) {
                jQuery(time_input).val(rs.value);
                picker.dispose();
            });
        });

        // 与macro.js联用
        mui(".mui-input-group").on("tap", ".capture-btn", function () {
            var cap = jQuery("#captureFile").get(0);
            cap.click();
        });

        jQuery("#captureFile").change(function(e) {
            var fileList = $(this)[0].files;
            showImgList(fileList);
        });

        function showImgList(fileList) {
            if (fileList[0]) {
                jQuery('.photo')[0].src = window.URL.createObjectURL(fileList[0]);
            }
        }
    }(mui, document));

    mui.plusReady(function() {
        mui('.mui-content-padded').on("tap", ".btn-ok", function () {
            var _tips = "";
            jQuery("div[data-isnull='false']").each(function (i) {
                var _code = jQuery(this).data("code");
                var _val = jQuery("#" + _code).val();
                if (_val == undefined || _val == "") {
                    var _text = jQuery(this).find("label:first").text();
                    _tips += _text + " 不能为空\n"
                }
            });
            if (_tips != null && _tips != "") {
                mui.toast(_tips);
                return;
            }

            var formData = new FormData($('.mui-input-group')[0]);
            // console.log("blob_arr.length=" + blob_arr.length);
            for (i = 0; i < blob_arr.length; i++) {
                var _blobObj = blob_arr[i];
                formData.append('upload', _blobObj.blob, _blobObj.fname);
            }

            jQuery.ajax("../../public/android/i/modifyPersonInfor.do", {
                dataType: 'json',// 服务器返回json格式数据
                type: 'post',// HTTP请求类型
                data: formData,
                processData: false,
                contentType: false,
                beforeSend: function (XMLHttpRequest) {
                    jQuery.myloading();
                },
                complete: function (XMLHttpRequest, status) {
                    jQuery.myloading("hide");
                },
                success: function (data) {
                    if (data.res == 0) {
                        mui.toast('修改成功');
                        // 置photo
                        var stateText = plus.storage.getItem('$state') || "{}";
                        var state = JSON.parse(stateText);
                        state.photo = data.photo;
                        // console.log('data.photo=' + data.photo);
                        plus.storage.setItem('$state', JSON.stringify(state));

                        // 发送onPhotoChange事件至me.html，以更新头像
                        var self = plus.webview.currentWebview();
                        var meView = plus.webview.getWebviewById(self.meViewId); // HBuilder
                        mui.fire(meView, 'onPhotoChange', {});
                    }
                    else {
                        if (null != data.msg && '' != data.msg) {
                            mui.toast(data.msg);
                        }
                        else {
                            mui.toast('修改失败');
                        }
                    }
                },
                error: function (xhr, type, errorThrown) {
                    console.log(type);
                }
            });
        });
    });
</script>
</body>

</html>