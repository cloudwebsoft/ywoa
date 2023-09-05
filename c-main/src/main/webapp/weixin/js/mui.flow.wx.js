(function ($, window, document, undefined) {
    $.ajaxSettings.beforeSend = function (xhr, setting) {
        jQuery.myloading();
    };
    // 设置全局complete
    $.ajaxSettings.complete = function (xhr, status) {
        jQuery.myloading("hide");
    }
    var w = window;
    var self;
    var FATTEND_DETAIL_AJAX_URL = "../../public/android/flow/modify?";// 流程详情
    var _FATTEND_DETAIL_AJAX_URL = "public/android/flow/modify?";// 流程详情，用于 5+ App

    var FLOW_INIT_AJAX_URL = "../../public/android/flow/init?";// 流程发起详情界面
    var _FLOW_INIT_AJAX_URL = "public/android/flow/init?";// 流程发起详情界面，用于 5+ App

    var FLOW_DISPOSE_AJAX_URL = "../../public/android/flow/dispose?"// 流程处理
    var _FLOW_DISPOSE_AJAX_URL = "public/android/flow/dispose?"// 流程处理

    // 20210706改为finishAction
    // var FREE_FLOW_DISPOSE_AJAX_URL= "../../public/flow_dispose_free_do.jsp";// 自由流程处理
    // var _FREE_FLOW_DISPOSE_AJAX_URL= "public/flow_dispose_free_do.jsp";// IOS自由流程处理

    var FREE_FLOW_DISPOSE_AJAX_URL = "../../public/android/flow/finishActionFree";// 自由流程处理
    var _FREE_FLOW_DISPOSE_AJAX_URL = "public/android/flow/finishActionFree";// IOS自由流程处理

    // var PRESET_FLOW_DISPOSE_AJAX_URL= "../../public/flow_dispose_do.jsp";// 预定流程处理
    // var _PRESET_FLOW_DISPOSE_AJAX_URL= "public/flow_dispose_do.jsp";// IOS预定流程处理

    var PRESET_FLOW_DISPOSE_AJAX_URL = "../../public/android/flow/finishAction";// 预定流程处理
    var _PRESET_FLOW_DISPOSE_AJAX_URL = "public/android/flow/finishAction";// IOS预定流程处理

    var FLOW_RETURN_AJAX_URL = "../../public/android/flow/getreturn";
    var _FLOW_RETURN_AJAX_URL = "public/android/flow/getreturn";

    var FLOW_MULTI_DEPT = "../../public/android/flow/multiDept";
    var _FLOW_MULTI_DEPT = "public/android/flow/multiDept";

    var Form;
    $.Flow = $.Class.extend({
        init: function (element, options) {
            this.element = element,
                this.default = {
                    "formSelector": ".mui-input-group",
                    "ulSelector": ".mui-table-view"
                }
            this.options = $.extend(true, this.default, options);
            Form = new $.Form(this.element, this.options);
        },
        flowAttendDetail: function () {
            var self = this;
            var skey = self.options.skey;
            var flowId = self.options.flowId;
            var ul = jQuery(self.options.ulSelector);
            var datas = {"skey": skey, "flowId": flowId}

            var url = FATTEND_DETAIL_AJAX_URL;
            if (mui.os.plus && mui.os.ios) {
                // 将FLOW_INIT_AJAX_URL路径改为完整的路径，否则ios中5+app会因为spring security不允许url中包括../而致无法访问
                var rootPath = this.getContextPath();
                var p = rootPath.indexOf('/weixin');
                if (p != -1) {
                    rootPath = rootPath.substring(0, p);
                }
                url = rootPath + "/" + _FATTEND_DETAIL_AJAX_URL;
            }
            mui.post(url, datas, function (data) {
                console.log('data', data);
                Form.initFlowDetail(data);

                if (data.viewJs) {
                    var s0 = document.createElement('script');
                    s0.text = data.viewJs;
                    document.body.appendChild(s0);
                }
            }, "json");
        },
        getContextPath: function () {
            var strFullPath = document.location.href;
            var strPath = document.location.pathname;
            var pos = strFullPath.indexOf(strPath);
            var prePath = strFullPath.substring(0, pos);
            var postPath = strPath.substring(0, strPath.substr(1).indexOf('/') + 1);
            return (prePath + postPath);
        },
        flowDisposeInit: function () {  // 待办流程初始化接口 - 发起流程初始化接口
            var self = this;
            var content = self.element;
            var url = FLOW_DISPOSE_AJAX_URL;
            if (mui.os.plus && mui.os.ios) {
                // 将FLOW_INIT_AJAX_URL路径改为完整的路径，否则ios中5+app会因为spring security不允许url中包括../而致无法访问
                var rootPath = this.getContextPath();
                var p = rootPath.indexOf('/weixin');
                if (p != -1) {
                    rootPath = rootPath.substring(0, p);
                }
                url = rootPath + "/" + _FLOW_DISPOSE_AJAX_URL;
            }
            var skey = self.options.skey;
            var formCode = self.options.formCode;
            // console.log('skey=' + skey);
            var flagXorRadiate = false;
            var datas;
            if (myActionId == 0) {
                datas = {
                    "skey": skey,
                    "title": self.options.title,
                    "code": self.options.code,
                    "type": self.options.type
                }
                var extraData = self.options.extraData;
                extraData = $.parseJSON(extraData);
                // 合并
                datas = $.extend({}, datas, extraData);
                url = FLOW_INIT_AJAX_URL;
                if (mui.os.plus && mui.os.ios) {
                    // 将FLOW_INIT_AJAX_URL路径改为完整的路径，否则ios中5+app会因为spring security不允许url中包括../而致无法访问
                    var rootPath = this.getContextPath();
                    var p = rootPath.indexOf('/weixin');
                    if (p != -1) {
                        rootPath = rootPath.substring(0, p);
                    }
                    url = rootPath + "/" + _FLOW_INIT_AJAX_URL;
                    console.log('url', url);
                }
            } else {
                datas = {"skey": skey, "myActionId": myActionId};
                var extraData = self.options.extraData;
                extraData = $.parseJSON(extraData);
                datas = $.extend({}, datas, extraData);
            }

            $.post(url, datas, function (data) {
                var res = data.res;
                var myAId = -1;
                if (res != '0') {
                    mui.toast(data.msg);
                    return;
                } else {
                    var actionId = data.actionId;
                    var flowId = data.flowId;
                    var annexs = data.result.annexs;
                    var fields = data.result.fields;
                    var hasAttach = data.hasAttach;
                    var isFlowStarted = data.isFlowStarted;

                    var isLight = data.isLight;
                    var btnContent = self.flowDisposeBtn(data);
                    flagXorRadiate = data.flagXorRadiate;

                    myAId = data.myActionId;
                    var cwsWorkflowTitle = data.cwsWorkflowTitle;
                    var commonParams = self.flowDisposeCommonParam(flowId, myAId, actionId, skey, cwsWorkflowTitle);

                    var isPlusBefore = data.isPlusBefore;
                    var isMyPlus = data.isMyPlus;
                    var plusDesc = data.plusDesc;

                    var isShowNextUsers = data.isShowNextUsers;

                    if (isLight) {
                        // @输入框
                        var at_input_box = '';
                        at_input_box += '<div data-isnull="false" data-code="cwsWorkflowResult"><span style="display:none">内容</span><textarea type="text" name="cwsWorkflowResult" id="cwsWorkflowResult" class="at_textarea" placeholder="说点什么吧~"/></div>';
                        at_input_box += commonParams;
                        at_input_box += '<div class="at_user_div clearfix" isLight="true">';
                        at_input_box += '<a><span class="iconfont icon-at"></span><span>提醒谁看</span>';
                        at_input_box += '<div class="userDiv"></div>';
                        at_input_box += '</a>';
                        at_input_box += '</div>';
                        jQuery("#free_flow_form").append(at_input_box);
                        jQuery("#free_flow_form").addClass("submitFlow");
                        var annex = data.lightDetail; // 评论
                        jQuery(".mui-content").append(Form.initAtStep(annex));

                        jQuery(".mui-content").append(btnContent);
                    } else {
                        var title = data.cwsWorkflowTitle;// 标题
                        var c_t = '<div class="mui-input-row" style="display: none">';
                        // c_t += '<label style="color:#000;width:100%;font-weight:bold">'+title+'</label>';
                        c_t += '<input type="text" name="cwsWorkflowTitle" value="' + title + '"/>';
                        c_t += '</div>';
                        jQuery(".mui-input-group").append(c_t);
                        if (fields.length > 0) {
                            Form.initForms(actionId, flowId, fields, formCode, data); // 初始化Form表单
                        }

                        // 赋予计算控件字段的值，allSums，因为有可能在手机端嵌套表中添加了记录，sum(...)计算成功了，但是主表并未保存或提交，那么计算控件中的值仍为0
                        if (data.allSums) {
                            var allSums = data.allSums;
                            console.log('allSums', allSums);
                            for (var k = 0; k < allSums.length; k++) {
                                var json = allSums[k];
                                calByNestSheet(json['sums'], json['formCode']);
                            }
                        }

                        if ("files" in data.result) {
                            var _files = data.result.files;
                            if (_files.length > 0) {
                                var _ul = Form.flowInitFiles(_files);
                                jQuery(".mui-input-group").append(_ul);
                            }
                        }

                        // 照片区域
                        if (hasAttach) {
                            jQuery(".mui-input-group").append("<ul class='mui-input-row img-area'><li class='img-box' style='border:0px'><img class='capture_btn' src='../../images/camera.png'></li></ul>");
                        }

                        if ("users" in data.result) {
                            var users = data.result.users;
                            if (users.length > 0) {
                                var _flowNextUsers = self.flowNextUsers(flagXorRadiate, users);
                                jQuery(".mui-input-group").append(_flowNextUsers);
                                if (!isShowNextUsers) {
                                    jQuery('.user-area').hide();
                                }
                            }
                        }

                        if (data.isFree) {
                            var _flowNextUsers = self.flowNextUsersFree();
                            jQuery(".mui-input-group").append(_flowNextUsers);
                        }

                        if ("multiDepts" in data.result) {
                            var multiDepts = data.result.multiDepts;
                            if (multiDepts.length > 0) {
                                self.chooseMultiDepts(multiDepts);
                            }
                        }

                        // 加签描述区域
                        var dis = "";
                        if (!(isMyPlus && !isPlusBefore)) {
                            dis = "style='display:none'";
                        }
                        jQuery(".mui-input-group").append('<div id="plusDescBox" class="mui-input-row" ' + dis + '>' + plusDesc + '<span id="btnDelPlus">✕</span></div>');

                        if ("isReply" in data) {
                            if (data.isReply) {
                                var annexGroup = '<ul class="mui-table-view reply-ul">';
                                var annexes = data.result.annexs;
                                $.each(annexes, function (index, item) {
                                    annexGroup += '<li class="mui-table-view-cell">';
                                    annexGroup += '	<div class="reply-header">';
                                    annexGroup += '		<span class="reply-name">' + item.annexUser + '</span>';
                                    // console.log("data.isProgress=" + data.isProgress);
                                    if (data.isProgress) {
                                        annexGroup += '	<span class="reply-progress">' + item.progress + '%</span>';
                                    }
                                    annexGroup += '		<span class="reply-date">' + item.add_date + '</span>';
                                    annexGroup += '	</div>';
                                    annexGroup += '	<div class="reply-content">' + item.content + '</div>';
                                    annexGroup += '</li>';
                                });
                                annexGroup += "</ul>";
                                jQuery('.reply-form').show();
                                jQuery('#progressLabel').text(data.progress);
                                jQuery('#progress').val(data.progress);
                                jQuery(".annex-group").append(annexGroup);
                            } else {
                                // 以免出现回复不能为空
                                jQuery('.reply-form').remove();
                            }
                        } else {
                            // 以免出现回复不能为空
                            jQuery('.reply-form').remove();
                        }

                        var formSelector = jQuery(".mui-input-group");
                        formSelector.append(btnContent);
                        formSelector.append(commonParams);
                        formSelector.addClass("submitFlow");

                        if (data.viewJs) {
                            var s0 = document.createElement('script');
                            s0.text = data.viewJs;
                            document.body.appendChild(s0);
                        }
                        formSelector.append('<input id="deptOfUserWithMultiDept" name="deptOfUserWithMultiDept" type="hidden" />');
                    }
                }
                mui(".mui-button-row").on("tap", ".back_submit", function () {
                    self.flowBackServer();
                });
                mui(".mui-button-row").on("tap", ".refuse_submit", function () {
                    var btnArray = ['否', '是'];
                    mui.confirm('您确定要拒绝么？', '提示', btnArray, function (e) {
                        if (e.index == 1) {
                            jQuery("#op").val("manualFinish");
                            self.flowSendServer();
                        }
                    });
                });
                // 提交按钮绑定事件
                mui(".mui-button-row").on("tap", ".flow_submit", function () {
                    var btnArray = ['是', '否'];
                    mui.confirm('您确定要提交么？', '提示', btnArray, function (e) {
                        if (e.index == 0) {
                            // 如果flagXorRadiate为false，有可能是条件分支，但却不带条件，所以此处仍需将所选人员对应的分支线传至服务器端，以便于手工选择
                            jQuery('.cls-XorNextActionInternalNames').remove(); // 清空XorNextActionInternalNames，因为之前的提交可能会不成功，但会生成此隐藏域
                            var _ckChecked = jQuery(".next_user_ck:checked");
                            if (_ckChecked.length > 0) {
                                _ckChecked.each(function (i) {
                                    var _curCk = jQuery(this);
                                    var _internalname = _curCk.data("internalname");
                                    // 当存在分支，且分支为自选用户时，分支前的checkbox是没有internalname的
                                    if (_internalname != null) {
                                        // 如果已存在，则不生成
                                        if (!jQuery('name[XorNextActionInternalNames=' + _internalname + ']')[0]) {
                                            jQuery("#flow_form").append('<input type="hidden" class="cls-XorNextActionInternalNames" name="XorNextActionInternalNames" value="' + _internalname + '" />');
                                        }
                                    }
                                })
                            }

                            jQuery("#op").val("finish");
                            self.flowSendServer();
                        }
                    });
                });
                // 保存草稿
                mui(".mui-button-row").on("tap", ".flow_draft", function () {
                    jQuery("#op").val("saveformvalue");
                    self.flowSendServer();
                });
                // 加签
                mui(".mui-button-row").on("tap", ".flow_plus", function () {
                    self.openPlusDlg(myAId, jQuery('.flow_plus').attr('isFlowStarted'));
                });
                mui("#plusDescBox").on("tap", "#btnDelPlus", function () {
                    var btnArray = ['是', '否'];
                    mui.confirm('您确定要取消加签么？', '提示', btnArray, function (e) {
                        if (e.index == 0) {
                            $.ajax({
                                type: "post",
                                url: "../../flow/delPlus",
                                data: {
                                    actionId: actionId,
                                },
                                dataType: "json",
                                success: function (res, status) {
                                    console.log(res);
                                    mui.toast(res.msg);
                                    if (res.code == 200) {
                                        jQuery('#plusDescBox').hide();
                                        jQuery('.flow_plus').show();
                                    }
                                },
                                error: function (XMLHttpRequest, textStatus) {
                                    alert(XMLHttpRequest.responseText);
                                }
                            });
                        }
                    });
                });

                // 删除按钮绑定事件
                mui(".mui-button-row").on("tap", ".del_btn", function () {
                    var btnArray = ['否', '是'];
                    mui.confirm('您确定要删除么？', '提示', btnArray, function (e) {
                        if (e.index == 1) {
                            jQuery("#op").val("del");
                            self.flowSendServer();
                        }
                    });
                });
                mui(".mui-button-row").on("tap", ".finish_btn", function () {
                    var btnArray = ['否', '是'];
                    mui.confirm('您确定要结束么？', '提示', btnArray, function (e) {
                        if (e.index == 1) {
                            jQuery("#op").val("manualFinishAgree");
                            self.flowSendServer();
                        }
                    });
                });

                self.bindDateEvent();
            }, "json");
        },
        // 打开加签窗对话框
        openPlusDlg: function (myActionId, isFlowStarted) {
            console.log('openPlusDlg isFlowStarted', isFlowStarted);
            var strHtml = '<ul class="mui-table-view">';
            strHtml += '<li class="mui-table-view-cell mui-left">';
            strHtml += '加签类型';
            strHtml += '</li>';
            // 注意isFlowStarted为字符串型
            if (isFlowStarted == 'true') {
                strHtml += '<li class="mui-table-view-cell mui-left mui-radio">';
                strHtml += '<input type="radio" name="plusType" value="0" onclick="window.flow.onClickPlusType()" />前加签';
                strHtml += '</li>';
            }
            strHtml += '<li class="mui-table-view-cell mui-left mui-radio">';
            strHtml += '<input type="radio" name="plusType" value="1" onclick="window.flow.onClickPlusType()" />后加签';
            strHtml += '</li>';
            strHtml += '<li class="mui-table-view-cell mui-left mui-radio">';
            strHtml += '<input type="radio" name="plusType" value="2" onclick="window.flow.onClickPlusType()" />并签';
            strHtml += '</li>';
            strHtml += '<li class="mui-table-view-cell mui-left plus-mode">';
            strHtml += '审批方式';
            strHtml += '</li>';
            strHtml += '<li class="mui-table-view-cell mui-left mui-radio plus-mode">';
            strHtml += '<input type="radio" name="plusMode" value="0" />顺序审批';
            strHtml += '</li>';
            strHtml += '<li class="mui-table-view-cell mui-left mui-radio plus-mode">';
            strHtml += '<input type="radio" name="plusMode" value="1" />只需其中一人处理';
            strHtml += '</li>';
            strHtml += '<li class="mui-table-view-cell mui-left mui-radio plus-mode">';
            strHtml += '<input type="radio" name="plusMode" value="2" />全部审批';
            strHtml += '</li>';
            strHtml += '</ul>';
            strHtml += '<div class="mui-button-row" style="margin:10px;">';
            strHtml += '<button class="mui-btn mui-btn-primary" type="button" onclick="selectUserWinForPlus(getRadioValue(\'plusType\'), getRadioValue(\'plusMode\'), ' + myActionId + ');">选择人员</button>';
            strHtml += '</div>';
            var pop = new Popup({contentType: 2, isReloadOnClose: false, width: 340, height: 300});
            pop.setContent("contentHtml", strHtml);
            pop.setContent("title", "加签");
            pop.build();
            pop.show();
            jQuery('#dialogBox').css('background-color', '#fff');
        },
        onClickPlusType: function() {
            var plusType = getRadioValue('plusType');
            if (plusType == 2) {
                jQuery('.plus-mode').hide();
            } else {
                jQuery('.plus-mode').show();
            }
        },
        onSelUserForPlus: function (userNames, realNames, plusType, plusMode, myActionId) {
            var self = this;
            $.ajax({
                type: "post",
                url: "../../flow/plus",
                data: {
                    users: userNames,
                    myActionId: myActionId,
                    type: plusType,
                    mode: plusMode,
                },
                dataType: "json",
                success: function (res, status) {
                    var data = res.data;
                    plusDesc = data.plusDesc;
                    if (data.ret == 0) {
                        mui.toast(data.msg);
                    } else {
                        mui.toast(data.msg);
                        // 如果是前加签，则直接返回
                        if (plusType == 0) {
                            if (self.options.isUniWebview) {
                                wx.miniProgram.getEnv(function (res) {
                                    console.log("当前环境：" + JSON.stringify(res));
                                    /*if (res.miniprogram) {
                                    }*/
                                });
                                wx.miniProgram.postMessage({
                                    data: {
                                        res: 0,
                                        url: ""
                                    },
                                });
                                uni.navigateBack();
                            } else {
                                mui.back();
                            }
                        } else {
                            jQuery('#plusDescBox').html(plusDesc + '<span id="btnDelPlus">✕</span>').show();
                            jQuery('.flow_plus').hide();
                        }
                    }
                },
                error: function (XMLHttpRequest, textStatus) {
                    alert(XMLHttpRequest.responseText);
                }
            });
        },
        flowBackServer: function () {
            var self = this;
            var _data = {
                "skey": jQuery("#skey").val(),
                "myActionId": jQuery("#myActionId").val(),
                "flowId": jQuery("#flowId").val()
            };
            var url = FLOW_RETURN_AJAX_URL;
            if (mui.os.plus && mui.os.ios) {
                // 将FLOW_INIT_AJAX_URL路径改为完整的路径，否则ios中5+app会因为spring security不允许url中包括../而致无法访问
                var rootPath = this.getContextPath();
                var p = rootPath.indexOf('/weixin');
                if (p != -1) {
                    rootPath = rootPath.substring(0, p);
                }
                url = rootPath + "/" + _FLOW_RETURN_AJAX_URL;
            }
            jQuery.ajax(url, {
                dataType: 'json',// 服务器返回json格式数据
                type: 'post',// HTTP请求类型
                data: _data,
                beforeSend: function (XMLHttpRequest) {
                    jQuery.myloading();
                },
                complete: function (XMLHttpRequest, status) {
                    jQuery.myloading("hide");
                },
                success: function (data) {
                    var res = data.res;
                    if (res == "0") {
                        if ("users" in data) {
                            var users = data.users;
                            if (users.length > 0) {
                                self.backUsers(users);
                            }
                        }
                    } else {
                        var msg = data.msg;
                        $.toast(msg);
                    }
                },
                error: function (xhr, type, errorThrown) {
                    // 异常处理；
                    console.log(type);
                }
            });
        },
        flowSendServer: function () {
            var self = this;
            // console.log("jQuery('#op').val()=" + jQuery('#op').val());
            // 防止保存草稿、退回、拒绝时报“回复 不能为空”
            if (jQuery('#op').val() != "saveformvalue" && jQuery('#op').val() != "return" && jQuery('#op').val() != "manualFinish") {
                var _tips = "";

                jQuery("div[data-fieldtype='2'],div[data-fieldtype='3'],div[data-fieldtype='5'],div[data-fieldtype='6'],div[data-fieldtype='9']").each(function (i) {
                    var _code = jQuery(this).data("code");
                    var _val = jQuery("#" + _code).val();

                    if (_val != '' && !isNumeric(_val)) {
                        var _text = jQuery(this).find("span:first").text();
                        _tips += _text + " 须为数字！\n"
                    }
                });

                jQuery("div[data-isnull='false']").each(function (i) {
                    // 如果是嵌套表格，则不检查是否必填，由后台检查
                    if (jQuery(this).find('.nestSheetSelect')[0]) {
                        return false;
                    }
                    // 如果是图像宏控件，则不检查是否必填，由后台检查
                    if (jQuery(this).find('.capture_btn')[0]) {
                        return false;
                    }
                    var _code = jQuery(this).data("code");
                    var _val = jQuery("#" + _code).val();

                    // 防止当提交时报“回复 不能为空”
                    if (jQuery('#op').val() == "finish" || jQuery('#op').val() == "del") {
                        if (_code == "content") {
                            // 通过判断其父节点的class是否为reply-form，确定是否为回复
                            if (jQuery(this).parent().attr("class") == "reply-form") {
                                return;
                            }
                        }
                    }

                    if (_val == undefined || _val == "") {
                        var _text = jQuery(this).find("span:first").text();
                        // console.log("_code=" + _code + " " + _text + " 不能为空！");
                        _tips += _text + " 不能为空！\n"
                    }
                });

                if (_tips != null && _tips != "") {
                    $.toast(_tips);
                    return;
                }
            }

            var isLight = jQuery(".flow_submit").attr("isLight");
            var isFree = jQuery(".flow_submit").attr("isFree");
            if (jQuery('#op').val() != "saveformvalue" && (isFree == 'true' || isLight == 'true')) {
                if (!jQuery("input[name='nextUsers']")[0]) {
                    var btnArray = ['否', '是'];
                    mui.confirm('您还没有选择下一步的用户，确定办理完毕了么？', '提示', btnArray, function (e) {
                        if (e.index == 1) {
                            self.flowSendServerPost();
                        }
                    });
                } else {
                    self.flowSendServerPost();
                }
            } else {
                self.flowSendServerPost();
            }
        },
        flowSendServerPost: function () {
            var self = this;
            var isLight = jQuery(".flow_submit").attr("isLight");
            var isFree = jQuery(".flow_submit").attr("isFree");
            var formData;
            var ajax_url = PRESET_FLOW_DISPOSE_AJAX_URL;
            if (mui.os.plus && mui.os.ios) {
                // 将FLOW_INIT_AJAX_URL路径改为完整的路径，否则ios中5+app会因为spring security不允许url中包括../而致无法访问
                var rootPath = this.getContextPath();
                var p = rootPath.indexOf('/weixin');
                if (p != -1) {
                    rootPath = rootPath.substring(0, p);
                }
                url = rootPath + "/" + _PRESET_FLOW_DISPOSE_AJAX_URL;
            }

            if (isLight == 'true') {
                ajax_url = FREE_FLOW_DISPOSE_AJAX_URL;
                if (mui.os.plus && mui.os.ios) {
                    // 将FLOW_INIT_AJAX_URL路径改为完整的路径，否则ios中5+app会因为spring security不允许url中包括../而致无法访问
                    var rootPath = this.getContextPath();
                    var p = rootPath.indexOf('/weixin');
                    if (p != -1) {
                        rootPath = rootPath.substring(0, p);
                    }
                    url = rootPath + "/" + _FREE_FLOW_DISPOSE_AJAX_URL;
                }
                formData = new FormData($('#free_flow_form')[0]);
            } else if (isFree == 'true') {
                ajax_url = FREE_FLOW_DISPOSE_AJAX_URL;
                if (mui.os.plus && mui.os.ios) {
                    // 将FLOW_INIT_AJAX_URL路径改为完整的路径，否则ios中5+app会因为spring security不允许url中包括../而致无法访问
                    var rootPath = this.getContextPath();
                    var p = rootPath.indexOf('/weixin');
                    if (p != -1) {
                        rootPath = rootPath.substring(0, p);
                    }
                    url = rootPath + "/" + _FREE_FLOW_DISPOSE_AJAX_URL;
                }
                formData = new FormData($('#flow_form')[0]);
            } else {
                formData = new FormData($('#flow_form')[0]);
            }
            for (i = 0; i < blob_arr.length; i++) {
                var _blobObj = blob_arr[i];
                var field = "upload";
                if (_blobObj.field) {
                    field = _blobObj.field; // 图像宏控件的
                }
                formData.append(field, _blobObj.blob, _blobObj.fname);
            }
            // console.info(formData);
            jQuery.ajax(ajax_url, {
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
                    console.log('data', data);
                    var res = data.res;
                    if (res == "0") {
                        var nextMyActionId = data.nextMyActionId;
                        var open_url = '';
                        var title = '操作成功!'
                        var isBack = true;
                        // 20220523手机端不宜继续处理下一步，因为处理完以后，会回到之前的处理页面，看起来就象没退出
                        if (false && "nextMyActionId" in data && nextMyActionId != '') {
                            isBack = false;
                            title = '操作成功！请点击确定，继续处理下一步！';
                            open_url = "../flow/flow_dispose.jsp?skey=" + skey + "&myActionId=" + nextMyActionId + "&isUniWebview=" + self.options.isUniWebview;
                        } else {
                            // 如果打开待办流程页面，点击后退按钮，会回退至流程处理dispose页面，故采用返回的方式
                            // open_url = "../flow/flow_doing_or_return.jsp?skey="+skey;
                        }

                        mui.alert(title, '提示', function () {
                            if (isBack) {
                                console.log('isBack', isBack);
                                if (self.options.isUniWebview) {
                                    console.log('wx.miniProgram', wx.miniProgram);
                                    wx.miniProgram.getEnv(function (res) {
                                        console.log("当前环境：" + JSON.stringify(res));
                                        /*if (res.miniprogram) {
                                        }*/
                                    });
                                    wx.miniProgram.postMessage({
                                        data: {
                                            res: 0,
                                            url: ""
                                        },
                                    });
                                    uni.navigateBack();
                                } else {
                                    mui.back();
                                }
                            } else {
                                mui.openWindow({
                                    "url": open_url
                                })
                            }
                        });
                    } else if (res == "3") {
                        var users = data.users;
                        if (users.length == 0) {
                            $.toast("没有满足条件的分支或人员");
                        } else {
                            self.conditionBranch(users);
                        }
                    } else {
                        var msg = data.msg;
                        $.toast(msg);
                    }
                },
                error: function (xhr, type, errorThrown) {
                    $.toast(type);
                    console.log(type);
                }
            });
        },
        // 流程提交通用参数
        flowDisposeCommonParam: function (flowId, myActionId, actionId, skey, cwsWorkflowTitle) {
            var params = '<input type="hidden" name="expireHours" value="0"/>';
            params += '	<input type="hidden" name="isToMobile" value="true"/>';
            params += '	<input type="hidden" id="flowId" name="flowId" value="' + flowId + '"/>';
            params += '	<input type="hidden" id="myActionId" name="myActionId" value="' + myActionId + '"/>';
            params += '	<input type="hidden" name="isUseMsg" value="true"/>';
            params += '	<input type="hidden" name="cws_lontitude" value=""/>';
            params += '	<input type="hidden" name="cws_latitude" value=""/>';
            params += '	<input type="hidden" name="cws_address" value=""/>';
            params += '	<input type="hidden" name="actionId" value="' + actionId + '"/>';
            params += '	<input type="hidden" id="skey" name="skey" value="' + skey + '"/>';
            params += '	<input type="hidden" name="orders" value="1"/>';
            params += '	<input type="hidden" name="op" id="op" value="finish"/>';
            // params +='<input type="hidden" name="cwsWorkflowTitle" value="'+ cwsWorkflowTitle+'" />'
            params += '	<input type="hidden" name="cwsWorkflowResult" id="cwsWorkflowResult" />';
            return params;
        },
        flowDisposeBtn: function (data) {
            var isLight = data.isLight;
            var canDecline = data.canDecline;
            var canReturn = data.canReturn;
            var hasAttach = data.hasAttach;
            var canDel = data.canDel;
            var canFinishAgree = data.canFinishAgree;
            var btnContent = '<div class="mui-button-row">';
            var isFree = data.isFree;
            var isBtnSaveShow = data.isBtnSaveShow;
            var btnAgreeName = data.btnAgreeName ? data.btnAgreeName : '提交';
            var isActionKindRead = data.isActionKindRead;
            if (isActionKindRead) {
                btnAgreeName = data.btnReadName;
            }
            var btnRefuseName = data.btnRefuseName;
            var canPlus = data.canPlus;
            var isFlowStarted = data.isFlowStarted;

            if (!btnRefuseName) {
                btnRefuseName = '拒绝';
            }
            var btnReturnName = data.btnReturnName;
            if (!btnReturnName) {
                btnReturnName = '退回';
            }
            var isFlowReturnWithRemark = data.isFlowReturnWithRemark;
            if (!isFlowReturnWithRemark) {
                isFlowReturnWithRemark = false;
            }
            if (!isLight && isBtnSaveShow) {
                btnContent += '<button type="button" class="mui-btn mui-btn-primary mui-btn-outlined flow_draft">保存</button>';
            }
            if (canPlus) {
                btnContent += ' <button type="button" style="margin-left:5px;" isFlowStarted="' + isFlowStarted + '" class="mui-btn mui-btn-primary mui-btn-outlined flow_plus">加签</button>';
            }
            btnContent += '	<button style="margin-left:5px;" type="button" class="mui-btn mui-btn-primary mui-btn-outlined flow_submit" isFree="' + isFree + '" isLight=' + isLight + '>' + btnAgreeName + '</button>';
            if (canDel) {
                btnContent += '	<button style="margin-left:5px;" type="button" class="mui-btn mui-btn-primary mui-btn-outlined del_btn">删除</button>';
            }
            /*if (hasAttach) {
                btnContent += '	<button style="margin-left:5px;" type="button" captureFieldName="upload" class="mui-btn mui-btn-primary mui-btn-outlined capture_btn">照片</button>';
            }*/
            if (!isLight && canDecline == 'true') {
                btnContent += '	<button style="margin-left:5px;" type="button" class="mui-btn mui-btn-primary mui-btn-outlined refuse_submit">' + btnRefuseName + '</button>';
            }
            if (!isLight && canReturn == 'true') {
                btnContent += '	<button style="margin-left:5px;" type="button" class="mui-btn mui-btn-primary mui-btn-outlined back_submit" isFlowReturnWithRemark="' + isFlowReturnWithRemark + '">' + btnReturnName + '</button>';
            }
            if (canFinishAgree) {
                btnContent += '	<button style="margin-left:5px;" type="button" class="mui-btn mui-btn-primary mui-btn-outlined finish_btn">结束</button>';
            }
            btnContent += '</div>';
            return btnContent;
        },
        chooseMultiDepts: function (multiDepts) {
            var self = this;
            var deptContent = '<div class="mui-input-row dept_title"><label>请选择你所在部门:</label></div>';
            deptContent += '<div class="mui-row multi_dept_div">';
            $.each(multiDepts, function (index, item) {
                var _name = item.name;
                var _code = item.code;
                deptContent += '<span class="mui-checkbox mui-left user_ck_span " style="float: left;">';
                deptContent += '<label style="line-height: 45px;">';
                deptContent += _name;
                deptContent += '</lable>';
                deptContent += '<input name="multi_dept_ck" value="' + _code + ' "  type="checkbox"  class="multi_dept_ck" />'
                deptContent += '</span>';
            });
            deptContent += '</div>';
            jQuery(".mui-input-group").append(deptContent);
            mui(".mui-input-group").on("tap", ".multi_dept_ck", function () {
                var _value = jQuery(this).val();
                self.getUsersByDepts(_value);
            });
        },
        getUsersByDepts: function (deptCode) {
            var self = this;
            var _data = {
                "skey": jQuery("#skey").val(),
                "myActionId": jQuery("#myActionId").val(),
                "deptCode": deptCode
            };
            var url = FLOW_MULTI_DEPT;
            if (mui.os.plus && mui.os.ios) {
                // 将FLOW_INIT_AJAX_URL路径改为完整的路径，否则ios中5+app会因为spring security不允许url中包括../而致无法访问
                var rootPath = this.getContextPath();
                var p = rootPath.indexOf('/weixin');
                if (p != -1) {
                    rootPath = rootPath.substring(0, p);
                }
                url = rootPath + "/" + _FLOW_MULTI_DEPT;
            }
            jQuery.ajax(url, {
                dataType: 'json',// 服务器返回json格式数据
                type: 'post',// HTTP请求类型
                data: _data,
                beforeSend: function (XMLHttpRequest) {
                    jQuery.myloading();
                },
                complete: function (XMLHttpRequest, status) {
                    jQuery.myloading("hide");
                },
                success: function (data) {
                    var res = data.res;
                    if (res == "0") {
                        jQuery('#deptOfUserWithMultiDept').val(deptCode);
                        if ("users" in data.result) {
                            var users = data.result.users;
                            if (users.length > 0) {
                                jQuery(".multi_dept_div").remove();
                                jQuery(".dept_title").remove();
                                var _users = self.flowNextUsers('false', users);
                                jQuery(".mui-button-row").before(_users);
                            }
                        }
                    }
                },
                error: function (xhr, type, errorThrown) {
                    // 异常处理；
                    console.log(type);
                }
            });
        },
        flowNextUsers: function (flagXorRadiate, users) {
            // users
            var userContent = "";
            if (flagXorRadiate == 'false') {
                var isUserSelect = false; // 是否为自选用户
                userContent = '<div class="mui-input-row user-area"><label>下一步用户:</label></div>';
                $.each(users, function (index, item) {
                    var actionTitle = item.actionTitle;
                    var actionUserName = item.actionUserName;
                    var actionUserRealName = item.actionUserRealName;
                    // 自选用户时，服务器端返回不带actionUserName及actionUserRealName
                    if (actionUserName == null) {
                        actionUserName = "";
                        actionUserRealName = "";
                    }
                    var value = item.value;
                    var name = item.name;
                    var realName = item.realName;
                    var roleName = item.roleName;
                    var isSelectable = item.isSelectable === 'true';
                    var isSelected = item.isSelected === 'true';
                    var isGoDown = item.isGoDown === 'true';
                    var canSelUser = item.canSelUser === 'true';
                    // console.info( item.canSelUser);
                    var disabled = isSelectable ? "" : "disabled";
                    // 如果根据策略需选中
                    var checked = isSelectable ? "" : "checked";
                    if (isSelected) {
                        checked = "checked";
                    }
                    // 如果根据策略为下达
                    var internalname = item.internalname;
                    if (actionUserName == "$userSelect" || value == "$userSelect") {
                        isUserSelect = true;
                        // XorNextActionInternalNames已改为在提交时通过被选中人员的data-internalname获取
                        // userContent += "<input type='hidden' name='XorNextActionInternalNames' value='"+internalname+"' />";

                        checked = "checked";
                        userContent += '<div id="next_user' + internalname + '" class="mui-row next_user_div user-area">';

                        userContent += '<div class="user_ck_span user-area" style="margin-left:15px">';
                        userContent += '<label style="line-height: 45px;">';
                        userContent += actionTitle + ":" + realName;
                        userContent += '</lable>';
                        userContent += '</div>';

                        /*var userNameAry = actionUserName.split(",");
                        var realNameAry = actionUserRealName.split(",");
                        // 列出其他人已选择的用户
                        for (var i = 0; i < userNameAry.length; i++) {
                            value = userNameAry[i];
                            realName = realNameAry[i];
                            userContent += '<span class="mui-checkbox mui-left user_ck_span user-area" style="float:left;">';
                            userContent += '<label style="line-height: 45px;">';
                            userContent += actionTitle + ":" + realName;
                            userContent += '</lable>';
                            var style = "";
                            if (isGoDown) {
                                userContent += '<input type="checkbox" checked disabled />';
                                style = " style='display:none' ";
                            }
                            userContent += '<input name="' + name + '" value="' + value + ' " ' + disabled + style + ' type="checkbox" ' + checked + ' class="next_user_ck" />'

                            userContent += '</span>';
                        }*/
                        userContent += '</div>';
                        if (isUserSelect) {
                            userContent += '<div class="mui-row user-area">';
                            userContent += '<button type="button" name="' + name + '" class="mui-btn mui-btn-primary choose_user_btn" isGoDown="' + isGoDown + '" internalName="' + internalname + '" style="margin: 10px;float:right;" >选择用户</button>';
                            userContent += '</div>';
                        }
                    } else {
                        userContent += '<div id="next_user' + internalname + '" class="mui-row next_user_div user-area">';
                        userContent += '<span class="mui-checkbox mui-left user_ck_span" style="float:left;">';
                        userContent += '<label style="line-height: 45px;">';
                        userContent += actionTitle + ":" + realName;
                        userContent += '</lable>';
                        userContent += '<input data-internalname="' + internalname + '" name="' + name + '" value="' + value + ' " ' + disabled + ' type="checkbox" ' + checked + ' class="next_user_ck" />'
                        userContent += '</span>';
                        userContent += '</div>';
                    }
                });
            }
            return userContent;
        },
        flowNextUsersFree: function () {
            var at_input_box = '';
            at_input_box += '<div class="at_user_div clearfix" isFree="true">';
            at_input_box += '<a><span class="iconfont"></span><span>选择用户</span>';
            at_input_box += '<div class="userDiv"></div>';
            at_input_box += '</a>';
            at_input_box += '</div>';
            return at_input_box;
        },
        conditionBranch: function (users) {
            var self = this;
            var isUserSelect = false;
            var strHtml = '<ul class="mui-table-view condition-view">';
            $.each(users, function (index, item) {
                strHtml += '<li class="mui-table-view-cell mui-checkbox mui-left">';
                if (!item.isSelectable) {
                    strHtml += '<input data-internalname = "' + item.internalname + '" style="display:none" checked  data-name = "' + item.name + '" type="checkbox" value="' + item.value + '" class="return_ck"/>';
                } else {
                    var isOnlyOneUser = users.length == 1;
                    if (isOnlyOneUser) {
                        strHtml += '<input type="checkbox" checked disabled style="top:0" />';
                        strHtml += '<input data-internalname = "' + item.internalname + '" data-name= "' + item.name + '" checked style="display:none" type="checkbox" value="' + item.value + '" class="return_ck"/>';
                    } else {
                        strHtml += '<input data-internalname = "' + item.internalname + '" data-name= "' + item.name + '" type="checkbox" style="top:0" value="' + item.value + '" class="return_ck"/>';
                    }
                }

                strHtml += item.actionTitle + "：" + item.realName;

                strHtml += '</li>';

                // 当条件中有自选用户
                if (item.value == '$userSelect') {
                    isUserSelect = true;
                    strHtml += '<li class="mui-table-view-cell mui-checkbox mui-left"><span class="mui-input-group">';
                    strHtml += '<button type="button" data-name = "' + item.name + '" class="mui-btn mui-btn-primary choose_user_btn" internalName="' + item.internalname + '" style="margin: 10px;float:right;" >选择用户</button>';
                    strHtml += '</span></li>';
                }
            });
            strHtml += '</ul>';
            // 如果是自选用户，则不显示确定按钮，以免误操作
            if (!isUserSelect) {
                strHtml += '<div class="mui-button-row" style="margin-top:10px;">';
                strHtml += '<button class="mui-btn mui-btn-primary" id="return-confirm-btn" type="button" onclick="return false;">确认</button>';
                strHtml += '</div>';
            }
            var pop = new Popup({contentType: 2, isReloadOnClose: false, width: 340, height: 300});
            pop.setContent("contentHtml", strHtml);
            pop.setContent("title", "下一步处理人");
            pop.build();
            pop.show();

            // 当条件中有自选用户
            $(".condition-view").on("tap", ".choose_user_btn", function () {
                // szzz的iphone上选择用户界面会变为一个页面，而非一个iframe弹窗
                pop.close();

                var checkedValues = [];
                /*jQuery(".next_user_ck").each(function(i) {
                    checkedValues.push(jQuery.trim(jQuery(this).val()));
                });*/
                var internalName = jQuery(this).attr('internalName');
                var chooseUser = checkedValues.join(",");
                var isCondition = true;
                var workflowActionIdStr = jQuery(this).data('name');
                openChooseUser(chooseUser, false, false, internalName, isCondition, workflowActionIdStr);
            });

            // 使”下一步处理人"弹出对话框的背景色为白色
            jQuery('#dialogBox').css('background-color', '#fff');
            mui(".mui-button-row").on("tap", "#return-confirm-btn", function () {
                var _ckChecked = jQuery(".return_ck:checked");
                var _checkLen = _ckChecked.length;
                pop.close();
                if (_checkLen > 0) {
                    jQuery("#flow_form").append('<input type="hidden" name="isAfterSaveformvalueBeforeXorCondSelect" value="true" />');
                    _ckChecked.each(function (i) {
                        var _curCk = jQuery(this);
                        var _internalname = _curCk.data("internalname");
                        var _actionName = _curCk.data("name");
                        var _val = _curCk.val();
                        jQuery("#flow_form").append('<input type="hidden" name="XorNextActionInternalNames" value="' + _internalname + '" />');
                        jQuery("#flow_form").append('<input type="hidden" name="' + _actionName + '" value="' + _val + '" />');
                    })
                    self.flowSendServer();
                }
            });
        },
        onSelUserForCondition: function (userNames, internalName, actionName) {
            // 当条件中有自选用户时调用
            var self = this;
            jQuery("#flow_form").append('<input type="hidden" name="isAfterSaveformvalueBeforeXorCondSelect" value="true" />');
            jQuery("#flow_form").append('<input type="hidden" name="XorNextActionInternalNames" value="' + internalName + '" />');
            jQuery("#flow_form").append('<input type="hidden" name="' + actionName + '" value="' + userNames + '" />');
            self.flowSendServer();
        },
        backUsers: function (users) {
            var self = this;
            var strHtml = '<ul class="mui-table-view">';
            $.each(users, function (index, item) {
                strHtml += '<li class="mui-table-view-cell mui-radio mui-left">';
                strHtml += '<input type="radio" style="top:0" value="' + item.id + '" name="returnUsers" class="return_ck"/>';
                strHtml += item.actionTitle + ":" + item.name;
                strHtml += '</li>';
            });
            strHtml += '</ul>';
            var isFlowReturnWithRemark = jQuery(".back_submit").attr("isFlowReturnWithRemark") == "true";
            strHtml += '<div class="mui-input-row">';
            strHtml += '<label>留言</label>';
            strHtml += '</div>';
            strHtml += '<div style="text-align: center"><textarea id="returnRemark" style="border: 1px solid #eee; width:90%"></textarea></div>';
            strHtml += '<div class="mui-button-row" style="margin-top:10px;">';
            strHtml += '<button class="mui-btn mui-btn-primary" id="return-back-confirm-btn" type="button" onclick="return false;">确认</button>';
            strHtml += '</div>';
            var pop = new Popup({contentType: 2, isReloadOnClose: false, width: 340, height: 300});
            pop.setContent("contentHtml", strHtml);
            pop.setContent("title", "请选择要返回的人员");
            pop.build();
            pop.show();
            // 使弹出对话框的背景色为白色
            jQuery('#dialogBox').css('background-color', '#fff');
            mui(".mui-button-row").on("tap", "#return-back-confirm-btn", function () {
                var _ckChecked = jQuery(".return_ck:checked");
                var _checkLen = _ckChecked.length;
                if (_checkLen == 0) {
                    mui.toast('请选择人员');
                    return;
                }
                if (isFlowReturnWithRemark) {
                    if (jQuery('#returnRemark').val() == '') {
                        mui.toast('请填写留言');
                        return;
                    }
                }
                jQuery('#cwsWorkflowResult').val(jQuery('#returnRemark').val());
                pop.close();

                if (_checkLen > 0) {
                    // 如果进度的回复未填，会导致需填写后重新再次取返回用户，有可能会导致returnId重复，所以此处需清除
                    jQuery("input[name='returnId']").remove();

                    _ckChecked.each(function (i) {
                        var _curCk = jQuery(this);
                        var _val = _curCk.val();
                        jQuery("#flow_form").append('<input type="hidden" name="returnId" value="' + _val + '" />');
                    })
                    jQuery('#op').val('return');
                    self.flowSendServer();
                }
            });
        },
        bindDateEvent: function () {
            var self = this;
            var content = self.element;
            // 选择用户
            $(".mui-input-group").on("tap", ".choose_user_btn", function () {
                var checkedValues = [];
                jQuery(".next_user_ck").each(function (i) {
                    checkedValues.push(jQuery.trim(jQuery(this).val()));
                });
                var internalName = jQuery(this).attr('internalName');
                var chooseUser = checkedValues.join(",");
                openChooseUser(chooseUser, false, false, internalName);
            });
            Form.bindFileDel();

            // iphone只能用原生的方式来绑定事件
            if (/(iPhone|iPad|iPod|iOS)/i.test(navigator.userAgent)) {
                var btnCapture = $('.capture_btn')[0];
                if (btnCapture) {
                    btnCapture.onclick = function () {
                        captureFieldName = jQuery(btnCapture).attr("captureFieldName");
                        // 置图像宏控件是否只允许拍照
                        if (jQuery(btnCapture).attr("isOnlyCamera")) {
                            setIsOnlyCamera(jQuery(btnCapture).attr("isOnlyCamera"));
                        } else {
                            // 恢复默认设置
                            resetIsOnlyCamera();
                        }
                        // 如果只允许拍照
                        if (appProp.isOnlyCamera == "true") {
                            jQuery("#captureFile").attr('capture', 'camera');
                        }
                        var cap = jQuery("#captureFile").get(0);
                        cap.click();
                        // 会出错，因为页面中可能含有多个captureFile
                        // document.getElementById('captureFile').click();
                    }
                }
            } else {
                mui(".mui-button-row, .mui-input-row").on("tap", ".capture_btn", function () {
                    captureFieldName = jQuery(this).attr("captureFieldName");
                    // 置图像宏控件是否只允许拍照
                    if (jQuery(this).attr("isOnlyCamera")) {
                        setIsOnlyCamera(jQuery(this).attr("isOnlyCamera"));
                    } else {
                        // 恢复默认设置
                        resetIsOnlyCamera();
                    }

                    if (appProp.isOnlyCamera == "true") {
                        jQuery("#captureFile").attr('capture', 'camera');
                    }
                    var cap = jQuery("#captureFile").get(0);
                    cap.click();
                });
            }

            // 自由流程、@流程选择用户
            $("form").on("tap", ".at_user_div", function () {
                var checkedValues = [];
                jQuery(".free_next_user_ck").each(function (i) {
                    checkedValues.push(jQuery(this).val());
                })
                var chooseUser = checkedValues.join(",");
                var isLight = jQuery(".at_user_div").attr("isLight") == "true";
                var isFree = jQuery(".at_user_div").attr("isFree") == "true";
                openChooseUser(chooseUser, isLight, isFree);
            });
        }
    })
})(mui, window, document)
