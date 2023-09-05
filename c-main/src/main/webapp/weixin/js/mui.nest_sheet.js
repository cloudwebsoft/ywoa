(function ($, document, window, undefined) {
    $.ajaxSettings.beforeSend = function (xhr, setting) {
        jQuery.myloading();
    };
    $.ajaxSettings.complete = function (xhr, status) {
        jQuery.myloading("hide");
    }
    var self;
    var Form;
    $.NestSheet = $.Class.extend({
        init: function (element, options) {
            this.element = element,
                this.default = {
                    "formSelector": ".mui-input-group",
                    "ulSelector": ".mui-table-view"
                }
            this.options = $.extend(true, this.default, options);
            Form = new $.Form(this.element, this.options);
        },
        initField: function () {  // 待办流程初始化接口 - 发起流程初始化接口
            var self = this;
            var content = self.element;
            var opt = self.options;
            var isWx = opt.isWx;
            var datas = {
                "skey": opt.skey,
                "formCode": opt.formCode,
                "parentFormCode": opt.parentCode,
                "flowId": opt.flowId,
                "actionId": opt.actionId,
                "parentId": opt.parentId,
                "id": opt.id
            }

            $.get(AJAX_REQUEST_URL.NEST_SHEET_INIT, datas, function (data) {
                var res = data.res;
                if (res == "0") {
                    var fields = data.fields;
                    console.info(fields);
                    if (fields.length > 0) {
                        Form.initForms(opt.actionId, opt.flowId, fields, data.formCode);//初始化Form表单
                    }
                    var btnContent = '<div class="mui-button-row">';
                    btnContent += '	<button type="button"  class="mui-btn mui-btn-primary flow_submit" >提交</button>';
                    btnContent += '</div>'
                    var params;
                    if (opt.id == 0) {
                        //新增
                        params = '	<input type="hidden" name="cws_id" value="' + data.cws_id + '"/>';
                    } else {
                        params = '	<input type="hidden" name="id" value="' + opt.id + '"/>';
                    }

                    var formSelector = jQuery(".mui-input-group");
                    formSelector.append(params);
                    formSelector.append(btnContent);

                    if (data.viewJs) {
                        var s0 = document.createElement('script');
                        s0.text = data.viewJs;
                        document.body.appendChild(s0);
                    }
                }

                mui(".mui-button-row").on("tap", ".flow_submit", function () {
                    var isAdd = opt.id == 0 ? true : false;
                    var ajax_url = '';
                    var formData = new FormData($('#nest_form')[0]);
                    var url = "";

                    var parentPageType = getParentPageType();

                    if (isAdd) {
                        ajax_url = AJAX_REQUEST_URL.NEST_SHEET_ADD;
                        url = "?op=saveformvalue&parentId=" + opt.parentId + "&skey=" + opt.skey + "&flowId=" + opt.flowId + "&actionId=" + opt.actionId + "&cwsStatus=1";
                        url += "&formCode=" + opt.parentCode + "&formCodeRelated=" + opt.formCode + "&pageType=add&parentPageType=" + parentPageType;

                    } else {
                        ajax_url = AJAX_REQUEST_URL.NEST_SHEET_MODIFY;
                        url = "?op=saveformvalue&skey=" + opt.skey + "&actionId=" + opt.actionId + "&formCode=" + opt.parentCode + "&formCodeRelated=" + opt.formCode + "&id=" + opt.id;
                        url += "&parentId=" + data.cws_id + "&parentPageType=" + parentPageType;
                    }
                    jQuery.ajax(ajax_url + url, {
                        dataType: 'json',//服务器返回json格式数据
                        type: 'post',//HTTP请求类型
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
                            var res = data.res;
                            var msg = data.msg;
                            $.toast(msg);
                            var params = opt.urlParams;
                            // console.log("opt.urlParams=" + params);
                            if (res == "0") {
                                if (isWx == 1) {
                                    nestSheetJump("列表", "../macro/nest_sheet_select.jsp" + params + "&isWx=1", data.sums, opt.formCode);
                                } else {
                                    if (typeof (data.sums) == 'object') {
                                        var str = JSON.stringify(data.sums);
                                        if (/(iPhone|iPad|iPod|iOS)/i.test(navigator.userAgent)) {
                                            params += "&flow_nestsheet=" + encodeURI(str);
                                        } else if (/(Android)/i.test(navigator.userAgent)) {
                                            // 无用
                                            javascript:nestSheetInterface.calculateNestSheet(str);
                                        }
                                    }
                                    mui.openWindow({
                                        "url": "../macro/nest_sheet_select.jsp" + params
                                    })
                                }
                            }
                        },
                        error: function (xhr, type, errorThrown) {
                            //异常处理；
                            console.log("type=" + type);
                        }
                    });
                });
            }, "json");
        }
    })
})(mui, document, window)
