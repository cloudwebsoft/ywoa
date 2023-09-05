<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@page import="com.redmoon.oa.android.Privilege" %>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr" %>
<%@page import="com.redmoon.oa.person.*" %>
<%@page import="com.redmoon.oa.notice.*" %>
<%@page import="cn.js.fan.util.*" %>
<%
    Privilege pvg = new Privilege();
    pvg.auth(request);
    String skey = pvg.getSkey();
    String userName = pvg.getUserName();
    boolean isUniWebview = ParamUtil.getBoolean(request, "isUniWebview", false);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>通知公告-添加</title>
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1, user-scalable=no"/>
    <meta name="format-detection" content="telephone=no,email=no,adress=no">
    <link rel="stylesheet" href="../css/mui.css">
    <link rel="stylesheet" href="../css/iconfont.css"/>
    <link rel="stylesheet" type="text/css" href="../css/mui.picker.min.css"/>
    <link href="../css/mui.indexedlist.css" rel="stylesheet"/>
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

        .mui-input-row label {
            color: #000;
        }
    </style>
</head>
<body>
<header class="mui-bar mui-bar-nav">
    <a class="mui-action-back mui-icon mui-icon-left-nav mui-pull-left"></a>
    <h1 class="mui-title">通知公告</h1>
</header>
<div class="mui-content">
    <form id="formNotice" action="../../public/notice/add.do" class="mui-input-group" enctype="multipart/form-data">
        <div class="mui-input-row" data-code="title" data-isnull="false">
            <label><span>标题</span><span style='color:red;'>*</span></label>
            <input type="text" id="title" name="title" class="mui-input-clear"/>
        </div>
        <div class="mui-input-row mui-checkbox">
            <label><span>可否回复</span></label>
            <input type="checkbox" id="is_reply" name="isReply" value="1" class="mui-input-clear"/>
        </div>
        <div class="mui-input-row mui-checkbox">
            <label><span>强制回复</span></label>
            <input type="checkbox" id="isForcedResponse" name="isForcedResponse" value="1" class="mui-input-clear"/>
        </div>
        <div class="mui-input-row">
            <label><span>人员</span></label>
            <button name="persons" type="button" class="mui-btn mui-btn-primary choose_user_btn mui-input-clear"
                    style="margin-right:10px">选择
            </button>
            <button id="btnAll" type="button" class="mui-btn mui-btn-primary mui-input-clear" style="margin-right:10px">
                全部
            </button>
        </div>
        <div class="mui-input-row next_user_div">
            <label>全部用户</label>
        </div>
        <div class="mui-input-row" data-code="beginDate" data-isnull="false">
            <label><span>开始时间</span><span style='color:red;'>*</span></label>
            <input type="text" id="beginDate" name="beginDate" value="<%=DateUtil.format(new Date(), "yyyy-MM-dd") %>"
                   class="input-icon" readonly/>
            <a class="date_btn"><span class="iconfont icon-rili"></span></a>
        </div>
        <div class="mui-input-row">
            <label><span>结束时间</span></label>
            <input type="text" id="endDate" name="endDate" class="input-icon" readonly/>
            <a class="date_btn"><span class="iconfont icon-rili"></span></a>
        </div>
        <div class="mui-input-row" data-code="content" data-isnull="false">
            <label><span>内容</span><span style='color:red;'>*</span></label>
            <div style="text-align:center">
                <textarea id="content" name="content" placeholder="请输入内容" style="height:150px;"></textarea>
                <%
                    com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
                %>
                <input id="isall" name="isall" value="<%=privilege.isUserPrivValid(request,"notice")==true?2:1 %>"
                       type="hidden"/>
            </div>
        </div>
        <%if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) { %>
        <div class="mui-input-row mui-checkbox" data-code="isToMobile" data-isnull="false">
            <label><span>短信</span></label>
            <input id="isToMobile" name="isToMobile" type="checkbox" checked value="1"/>
        </div>
        <%} %>
        <div class="mui-button-row">
            <button type="button" class="mui-btn mui-btn-primary mui-btn-outlined capture-btn">照片</button>
            <button type="button" style="margin-left:5px;" class="mui-btn mui-btn-primary mui-btn-outlined btn-ok">确定
            </button>
        </div>
        <input name="skey" type="hidden" value="<%=skey %>"/>
    </form>
    <input type="file" id="captureFile" name="upload" accept="image/*">

    <script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jq_mydialog.js"></script>
    <script type="text/javascript" src="../js/newPopup.js"></script>
    <script src="../js/macro/macro.js"></script>
    <script src="../js/mui.min.js"></script>
    <script src="../js/mui.picker.min.js"></script>
    <script type="text/javascript" src="../js/config.js"></script>
    <script type="text/javascript" src="../js/base/mui.form.js"></script>

    <script type="text/javascript" src="../js/visual/module_list.js"></script>
    <script type="text/javascript">
        var isUniWebview = <%=isUniWebview%>;

        if(!mui.os.plus || isUniWebview) {
            // 必须删除，而不能是隐藏，否则mui-bar-nav ~ mui-content中的padding-top会使得位置下移
            $('.mui-bar').remove();
        }

        mui.init({
            keyEventBind: {
                backbutton: !isUniWebview //关闭back按键监听
            }
        });

        $(function () {
            mui.init({
                swipeBack: true //启用右滑关闭功能
            });

            var data = formatDateTime(new Date);
            $('#mydate').val(data);

            $('#btnAll').click(function () {
                $('.next_user_div').html('<label>全部用户</label>');
            });

            $(".mui-content").on('tap', '.att_del', function (event) {
                var elem = this;
                var li = elem.parentNode.parentNode;
                var fId = li.getAttribute("fId");//判断是否是新增
                var btnArray = ['确认', '取消'];
                mui.confirm('确认删除该条记录？', '提示', btnArray, function (e) {
                    setTimeout(function () {
                        $.swipeoutClose(li);
                    }, 0);
                    if (e.index == 0) {
                        if (fId != "0") {
                            mui.get("../../public/notice/delAtt.do", {"skey": skey, "id": fId}, function (data) {
                                var res = data.res;
                                var msg = data.msg;
                                if (res == "0") {
                                    $.toast("删除成功!");
                                    li.parentNode.removeChild(li);
                                    var _pLen = jQuery(".att_li").length;
                                    if (_pLen == 0) {
                                        jQuery(".att_ul").remove();
                                    }
                                } else {
                                    $.toast(msg);
                                }
                            }, "json");
                        } else {
                            li.parentNode.removeChild(li);
                            var _index = jQuery(li).index() - 1;
                            blob_arr.splice(_index, 1);
                            var _pLen = jQuery(".att_li").length;
                            if (_pLen == 0) {
                                jQuery(".att_ul").remove();
                            }
                        }
                    }
                });
            });

            // 与macro.js联用
            mui(".mui-input-group").on("tap", ".capture-btn", function () {
                var cap = jQuery("#captureFile").get(0);
                cap.click();
            });

            // 日期选择控件
            $('.mui-input-group').on("tap", ".date_btn", function () {
                var id = this.getAttribute('id');
                var par = this.parentNode;
                var time_input = par.querySelector(".input-icon");
                var picker = new mui.DtPicker({"type": "date"});
                picker.show(function (rs) {
                    jQuery(time_input).val(rs.value);
                    picker.dispose();
                });
            });

            $(".mui-input-group").on("tap", ".choose_user_btn", function () {
                var checkedValues = [];
                jQuery(".next_user_ck").each(function (i) {
                    checkedValues.push(jQuery(this).val());
                })
                var chooseUser = checkedValues.join(",");
                openChooseUser(chooseUser, false);
            });
        });

        $(function () {
            $('.btn-ok').click(function () {
                // 用于发送通知的界面
                if ($("input[name='persons']").length > 0) {
                    $('#isall').val('<%=NoticeDb.IS_ALL_SEL_USER%>');
                }
                else {
                    $('#isall').val('<%=privilege.isUserPrivValid(request,"notice")==true?NoticeDb.IS_ALL_WHOLE:NoticeDb.IS_ALL_DEPT%>');
                }

                var _tips = "";
                jQuery("div[data-isnull='false']").each(function (i) {
                    var _code = jQuery(this).data("code");
                    var _val = jQuery("#" + _code).val();
                    if (_val == undefined || _val == "") {
                        var _text = jQuery(this).find("span:first").text();
                        _tips += _text + " 不能为空\n"
                    }
                });
                if (_tips != null && _tips != "") {
                    mui.toast(_tips);
                    return;
                }

                var formData = new FormData($('#formNotice')[0]);
                console.log("blob_arr.length=" + blob_arr.length);
                for (i = 0; i < blob_arr.length; i++) {
                    var _blobObj = blob_arr[i];
                    formData.append('upload', _blobObj.blob, _blobObj.fname);
                }

                jQuery.ajax("../../public/notice/add.do", {
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
                        mui.toast(data.msg);
                        if (data.ret == "1") {
                            window.location.href = "notice_list.jsp?skey=<%=skey%>";
                        }
                    },
                    error: function (xhr, type, errorThrown) {
                        console.log(type);
                    }
                });
            });
        });

        function callJS() {
            return {"btnAddShow": 0, "btnBackUrl": ""};
        }

        var iosCallJS = '{ "btnAddShow":0, "btnBackUrl":"" }';
    </script>
</div>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=pvg.getSkey()%>" />
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>