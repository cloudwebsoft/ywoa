<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@ page import="com.redmoon.oa.android.Privilege" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.workplan.WorkPlanDb" %>
<%@ page import="com.redmoon.oa.workplan.WorkPlanAnnexDb" %>
<%@ page import="com.redmoon.oa.workplan.WorkPlanTaskDb" %>
<%@ page import="com.redmoon.oa.workplan.WorkPlanAnnexAttachment" %>
<%
    Privilege pvg = new Privilege();
    if (!pvg.auth(request)) {
        out.print(StrUtil.p_center("请登录"));
        return;
    }
    String skey = pvg.getSkey();
    int id = ParamUtil.getInt(request, "id", -1);
    WorkPlanDb wpd = new WorkPlanDb();
    wpd = wpd.getWorkPlanDb(id);
    if (!wpd.isLoaded()) {
        out.print(StrUtil.p_center("计划不存在"));
        return;
    }
    int year = ParamUtil.getInt(request, "year", -1);
    int month = ParamUtil.getInt(request, "month", -1);
    int annexType = ParamUtil.getInt(request, "annexType", -1);
    long annexId = ParamUtil.getLong(request, "annexId", -1);
    WorkPlanAnnexDb wpad = new WorkPlanAnnexDb();
    wpad = (WorkPlanAnnexDb)wpad.getQObjectDb(annexId);
    if (wpad==null) {
        out.print(StrUtil.p_center("汇报不存在"));
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1, user-scalable=no"/>
    <meta charset="utf-8">
    <meta name="format-detection" content="telephone=no,email=no,adress=no">
    <title>修改汇报</title>
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
        #captureFile {
            display: none;
        }
        .mui-input-row label {
            color: #000;
        }
    </style>
</head>
<body>
<div class="mui-content">
    <form id="formAdd" class="mui-input-group" enctype="multipart/form-data">
        <div class="mui-input-row">
            <label style="width: 100%; text-align: center">
            <%=wpd.getTitle()%>&nbsp;
                <%
                    if (annexType == WorkPlanAnnexDb.TYPE_WEEK) {
                %>
                周报
                <%
                } else {
                %>
                月报
                <%
                    }

                %>
            </label>
        </div>
        <div class="mui-input-row">
            <label><span>原进度</span></label>
            <label><%=wpad.getInt("old_progress")%>%</label>
        </div>
        <div class="mui-input-row mui-input-range">
            <label>现进度<span id="progressLabel" style="margin-left:10px"><%=wpad.getInt("progress")%></span></label>
            <input id="progress" name="progress" type="range" min="0" max="100" value="<%=wpad.getInt("progress")%>" onchange="$('#progressLabel').text(mui('#progress')[0].value)">
        </div>
        <div class="mui-input-row" data-code="content" data-isnull="false">
            <label><span>内容</span><span style='color:red;'>*</span></label>
            <div style="text-align:center">
                <textarea id="content" name="content" placeholder="请输入内容" style="height:150px;"><%=wpad.getString("content")%></textarea>
            </div>
        </div>
        <div class="mui-button-row">
            <button type="button" class="mui-btn mui-btn-primary mui-btn-outlined capture-btn">照片</button>
            <button type="button" style="margin-left:5px;" class="mui-btn mui-btn-primary mui-btn-outlined btn-ok">确定
            </button>
        </div>
        <%
            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            String vpath = cfg.get("file_workplan");
            String attachmentBasePath = request.getContextPath() + "/" + vpath + "/";
            WorkPlanAnnexAttachment wpaa = new WorkPlanAnnexAttachment();
            Vector v = wpaa.getAttachments(wpad.getLong("id"));
            if (v.size() > 0) {
        %>
        <ul class="mui-table-view mui-table-view-chevron att_ul">
            <li class="mui-table-view-cell mui-media ">附件：</li>
            <%
                Iterator ir = v.iterator();
                while (ir.hasNext()) {
                    wpaa = (WorkPlanAnnexAttachment) ir.next();
            %>
            <li class="mui-table-view-cell mui-media" fId="<%=wpaa.getId() %>">
                <div class="mui-slider-right mui-disabled">
                    <a class="mui-btn mui-btn-red att_del">删除</a>
                </div>
                <div class="mui-slider-handle">
                    <a class="attFile" href="<%=attachmentBasePath + wpaa.getVisualPath() + "/" + wpaa.getDiskName() %>" target="_blank">
                        <img class="mui-media-object mui-pull-left"
                             src="../images/file/<%=com.redmoon.oa.android.tools.Tools.getIcon(StrUtil.getFileExt(wpaa.getDiskName())) %>"/>
                        <div class="mui-media-body">
                            <%=wpaa.getName() %>
                        </div>
                    </a>
                </div>
            </li>
            <%
                }
            %>
        </ul>
        <%
            }
        %>
        <input name="skey" type="hidden" value="<%=skey %>"/>
        <input name="id" type="hidden" value="<%=wpad.getLong("id")%>"/>
        <input name="addDate" type="hidden" value="<%=DateUtil.format(new Date(), "yyyy-MM-dd")%>"/>
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
    <link rel="stylesheet" href="../css/photoswipe.css">
    <link rel="stylesheet" href="../css/photoswipe-default-skin/default-skin.css">
    <script type="text/javascript" src="../js/photoswipe.js"></script>
    <script type="text/javascript" src="../js/photoswipe-ui-default.js"></script>
    <script type="text/javascript" src="../js/photoswipe-init.js"></script>
    <script type="text/javascript">
        $(function () {
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
        });

        $(function () {
            $('.btn-ok').click(function () {
                var _tips = "";
                jQuery("div[data-isnull='false']").each(function (i) {
                    var _code = jQuery(this).data("code");
                    var _val = jQuery("#" + _code).val();
                    if (_val == undefined || _val == "") {
                        var _text = jQuery(this).find("span:first").text();
                        _tips += _text + " 不能为空<BR/>"
                    }
                });
                if (_tips != null && _tips != "") {
                    mui.toast(_tips);
                    return;
                }

                var formData = new FormData($('#formAdd')[0]);
                console.log("blob_arr.length=" + blob_arr.length);
                for (i = 0; i < blob_arr.length; i++) {
                    var _blobObj = blob_arr[i];
                    formData.append('upload', _blobObj.blob, _blobObj.fname);
                }

                jQuery.ajax("../../public/workplan/editAnnex.do", {
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
                            <%
                            if (annexType==WorkPlanAnnexDb.TYPE_WEEK) {
                                %>
                            mui.openWindow({
                                "url": "workplan_annex_list_week.jsp?id=<%=id%>&skey=<%=skey%>&year=<%=year%>&month=<%=month%>"
                            });
                                <%
                            }
                            else {
                                %>
                            mui.openWindow({
                               "url": "workplan_annex_list_month.jsp?id=<%=id%>&skey=<%=skey%>&year=<%=year%>&month=<%=month%>"
                            });
                                <%
                            }
                            %>
                        }
                    },
                    error: function (xhr, type, errorThrown) {
                        console.log(type);
                    }
                });
            });
        });

        $(".mui-content").on("tap", ".attFile", function () {
            var url = jQuery(this).attr("href");
            var p = url.lastIndexOf(".");
            var ext = url.substring(p+1);
            if (ext == "jpg" || ext == "jpeg" || ext == "png" || ext == "gif" || ext == "bmp") {
                showImg(url);
            }
            else {
                mui.openWindow({
                    "url": "../../public/img_show.jsp?path=" + encodeURI(url)
                })
            }
        })

        function showImg(path) {
            var openPhotoSwipe = function () {
                var pswpElement = document.querySelectorAll('.pswp')[0];
                var items = [{
                    src: "../../public/img_show.jsp?path=" + encodeURI(path),
                    w: 964,
                    h: 1024
                }
                ];
                // define options (if needed)
                var options = {
                    // history & focus options are disabled on CodePen
                    history: false,
                    focus: false,
                    showAnimationDuration: 0,
                    hideAnimationDuration: 0
                };
                var gallery = new PhotoSwipe(pswpElement, PhotoSwipeUI_Default, items, options);
                gallery.init();
            };
            openPhotoSwipe();
        }

        $(".mui-content").on('tap', '.att_del', function (event) {
            var elem = this;
            var li = elem.parentNode.parentNode;
            var fId = li.getAttribute("fId");//判断是否是新增
            var btnArray = ['取消', '确认'];
            mui.confirm('确认删除该条记录？', '提示', btnArray, function (e) {
                if (e.index == 1) {
                    if (fId != "0") {
                        mui.get("../../public/workplan/delAnnexAttachment.do", {
                            "skey": "<%=skey%>",
                            "attId": fId
                        }, function (data) {
                            var ret = data.ret;
                            var msg = data.msg;
                            mui.toast(msg);
                            if (ret == "1") {
                                li.parentNode.removeChild(li);
                                var _pLen = jQuery(".att-li").length;
                                if (_pLen == 0) {
                                    jQuery(".att_ul").remove();
                                }
                            }
                        }, "json");
                    } else {
                        li.parentNode.removeChild(li);
                        var _index = jQuery(li).index() - 1;
                        blob_arr.splice(_index, 1);
                        var _pLen = jQuery(".att-li").length;
                        if (_pLen == 0) {
                            jQuery(".att_ul").remove();
                        }
                    }
                }
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