<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@ page import="com.redmoon.oa.android.Privilege" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="com.redmoon.oa.workplan.WorkPlanTaskDb" %>
<%@ page import="com.redmoon.oa.workplan.WorkPlanAnnexDb" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%
    Privilege pvg = new Privilege();
    if (!pvg.auth(request)) {
        out.print(StrUtil.p_center("请登录"));
        return;
    }
    String skey = pvg.getSkey();
    String userName = pvg.getUserName();
    UserDb ud = new UserDb();
    ud = ud.getUserDb(userName);

    long taskId = ParamUtil.getLong(request, "taskId", -1);
    WorkPlanTaskDb wptd = new WorkPlanTaskDb();
    wptd = (WorkPlanTaskDb) wptd.getQObjectDb(new Long(taskId));

    boolean isWorkPlanManager = false;
    com.redmoon.oa.workplan.Privilege pvgWorkplan = new com.redmoon.oa.workplan.Privilege();
    int workplanId = wptd.getInt("work_plan_id");
    if (pvgWorkplan.canUserManageWorkPlan(request, workplanId))
        isWorkPlanManager = true;

    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    int workplan_annex_day_add_limit = cfg.getInt("workplan_annex_day_add_limit");
    int workplan_annex_day_edit_limit = cfg.getInt("workplan_annex_day_edit_limit");
%>
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1, user-scalable=no"/>
    <meta charset="utf-8">
    <title>任务日报</title>
    <meta name="format-detection" content="telephone=no,email=no,adress=no">
    <link rel="stylesheet" href="../css/mui.css">
    <link rel="stylesheet" href="../calendar/css/reset.css"/>
    <link rel="stylesheet" type="text/css" href="../calendar/css/simple-calendar.css">
    <link rel="stylesheet" href="../calendar/css/calendar.css"/>
    <link rel="stylesheet" href="../css/my_dialog.css"/>
    <style>
        #captureFile {
            display: none;
        }
    </style>
    <script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="../js/mui.min.js"></script>
    <script type="text/javascript" src="../calendar/js/simple-calendar.js"></script>
    <script type="text/javascript" src="../calendar/js/hammer-2.0.8-min.js"></script>
    <script type="text/javascript" src="../js/macro/macro.js"></script>
    <script type="text/javascript" src="../js/newPopup.js"></script>
    <script type="text/javascript" src="../js/jq_mydialog.js"></script>
    <link rel="stylesheet" href="../css/photoswipe.css">
    <link rel="stylesheet" href="../css/photoswipe-default-skin/default-skin.css">
    <script type="text/javascript" src="../js/photoswipe.js"></script>
    <script type="text/javascript" src="../js/photoswipe-ui-default.js"></script>
    <script type="text/javascript" src="../js/photoswipe-init.js"></script>
</head>
<body>
<div class="mui-content">
    <div style="padding: 10px 10px;">
        <div id="segmentedControl" class="mui-segmented-control">
            <a class="mui-control-item task-detail">
                任务详情
            </a>
            <a class="mui-control-item mui-active annex-day">
                任务日报
            </a>
        </div>
    </div>
    <script>
        $(function () {
            mui('#segmentedControl').on('tap', '.task-detail', function () {
                mui.openWindow({
                    "url": "workplan_task_show.jsp?id=<%=taskId%>"
                })
            });
        })
    </script>
    <div class="inner">
        <div id='calendar' class="sc-calendar">
            <div class="sc-header">
                <div class="sc-title">
                    <div class="year">&nbsp;<span class="sc-select-year" name=""></span>年</div>
                    <div class="month">
                        <div class="arrow sc-mleft"></div>
                        <div class="monthdiv">
                            <span class="sc-select-month" name=""></span>
                        </div>
                        <div class="arrow sc-mright"></div>
                    </div>
                </div>
                <div class="sc-week"></div>
            </div>
            <div class="sc-body">
                <div class="sc-days"></div>
            </div>
        </div>
        <div class="announcement">
            <ul class="matter">
            </ul>
        </div>
    </div>
</div>

<pre id="boxAnnex" style="display: none">
	<ul class="reply-ul">
        <form id="formAnnex" method="post" enctype="multipart/form-data">
		<div class="annex-group">
		<div class="reply-form" style="margin-bottom:10px">
		<div class="mui-input-row mui-input-range">
			<label>进度<span id="progressLabel" style="margin-left:10px"><%=wptd.getInt("progress")%></span></label>
			<input id="progress" name="progress" type="range" min="0" max="100" value="<%=wptd.getInt("progress")%>" onchange="$('#progressLabel').text(mui('#progress')[0].value)">
		</div>
		<div class="mui-input-row" data-code="content" data-isnull="false">
			<label><span>汇报</span><span style='color:red;'>*</span></label>
			<div style="text-align:center">
			<textarea id="content" name="content" placeholder="请输入汇报内容" style="width:96%; height:150px;"></textarea>
			</div>
		</div>
		<div class="mui-button-row">
            <button type="button" class="mui-btn mui-btn-primary mui-btn-outlined capture-btn">照片</button>
			<button type="button" class="mui-btn mui-btn-primary mui-btn-outlined btn-ok">确定</button>
		</div>
		</div>
		</div>
            <input type="hidden" name="skey" value="<%=skey%>"/>
            <input type="hidden" name="taskId" value="<%=taskId%>"/>
            <input type="hidden" name="annexType" value="<%=WorkPlanAnnexDb.TYPE_NORMAL%>"/>
        </form>
	</ul>
</pre>
<input type="file" id="captureFile" name="upload" accept="image/*">
<script type="text/javascript">
    var myCalendar = new SimpleCalendar('#calendar');
    var addAnnexHtml = "";
    $(function () {
        addAnnexHtml = $('#boxAnnex').html();
        $('#boxAnnex').html('');

        var year = $('.sc-select-year').text();
        var monthCH = $('.sc-select-month').text();
        var month = SimpleCalendar.prototype.languageData.months_CH.indexOf(monthCH) + 1;
        loadMark(year, month);

        $(".sc-mleft").click(function () {
            myCalendar.subMonth();
            var year = $('.sc-select-year').text();
            var monthCH = $('.sc-select-month').text();
            var month = SimpleCalendar.prototype.languageData.months_CH.indexOf(monthCH) + 1;

            loadMark(year, month);
        })
        $(".sc-mright").click(function () {
            myCalendar.addMonth();
            var year = $('.sc-select-year').text();
            var monthCH = $('.sc-select-month').text();
            var month = SimpleCalendar.prototype.languageData.months_CH.indexOf(monthCH) + 1;

            loadMark(year, month);
        })
    });

    // 滑动切换
    var myElement = document.getElementById('calendar');
    var hammer = new Hammer(myElement);
    hammer.on("swipeleft", function (ev) {
        myCalendar.addMonth();

        var year = $('.sc-select-year').text();
        var monthCH = $('.sc-select-month').text();
        var month = SimpleCalendar.prototype.languageData.months_CH.indexOf(monthCH) + 1;
        loadMark(year, month);
        console.log("month=" + month);
    });
    hammer.on("swiperight", function (ev) {
        myCalendar.subMonth();
        var year = $('.sc-select-year').text();
        var monthCH = $('.sc-select-month').text();
        var month = SimpleCalendar.prototype.languageData.months_CH.indexOf(monthCH) + 1;
        loadMark(year, month);
        console.log("month=" + month);
    });

    var mark;

    function loadMark(y, m, isShowToday) {
        $.ajax({
            url: "../../public/workplan/listAnnexes.do",
            async: false,
            type: "post",
            data: {
                skey: "<%=skey%>",
                year: y,
                month: m,
                taskId: "<%=taskId%>"
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                mark = data.result;
                myCalendar._defaultOptions.mark = mark;
                myCalendar.update(m, y);
                // 显示当天的活动在初始化mark之后
                // 初始化今天的活动
                if (isShowToday == undefined || isShowToday) {
                    announceList($('.sc-today'));
                }
            },
            complete: function (XMLHttpRequest, status) {
            },
            error: function (XMLHttpRequest, textStatus) {
            }
        });
    }

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

    // 有标记的日期点击事件
    $('#calendar').on("click", '.sc-selected', function () {
        announceList($(this));
    });

    // 显示选择日期当天的活动
    function announceList(v) {
        if (v.children().hasClass('sc-mark-show')) {
            var year = $('.sc-select-year').text();
            var monthCH = $('.sc-select-month').text();
            var day = v.children()[1].innerText;
            var month = SimpleCalendar.prototype.languageData.months_CH.indexOf(monthCH) + 1;
            showAnnexes(year, month, day);
        } else {
            var year = $('.sc-select-year').text();
            var monthCH = $('.sc-select-month').text();

            var day = v.children()[1].innerText;
            var month = SimpleCalendar.prototype.languageData.months_CH.indexOf(monthCH) + 1;
            var myMonth = month;

            var m = '' + month;
            if (m.length < 2) {
                m = "0" + m;
            }
            if (day.length < 2) {
                day = "0" + day;
            }
            var date = year + '-' + m + '-' + day;

            var d = new Date(date.replace(/-/g, "/"));
            var days = dateDiff(new Date(), d);
            var workplan_annex_day_add_limit = <%=workplan_annex_day_add_limit%>;
            // 如果早于当天，或超出天数大于规定值，则不允许添加
            if (days < 0 || (days >= 0 && days > workplan_annex_day_add_limit)) {
                $('.matter').html('');
                return;
            }

            <%
                // 是否通过流程汇报
                String flowType = StrUtil.getNullStr(wptd.getString("report_flow_type"));
                if (!flowType.equals("") && !flowType.equals("''")) {
            %>
            var strLi = '<li style="padding-top:20px;"><div style="text-align: center"><p>';
            strLi += "<button class=\"mui-btn\" onclick=\"window.location.href='<%=request.getContextPath()%>/weixin/flow/flow_dispose.jsp?type=2&taskId=<%=taskId%>&projectId=<%=workplanId%>&addDate=" + date + "&code=<%=flowType%>&skey=<%=skey%>'\">汇报</button>";
            strLi += '</p></div></li>';
            $('.matter').html(strLi);
            return;
            <%
                }
            %>

            var matterHtml = '';
            matterHtml += '<li class="announceItem"><div><p class="announceContent">' + addAnnexHtml + '</p></div></li>';
            $('.matter').html(matterHtml);

            // 在表单中加入当前选择的日期
            $('#formAnnex').append('<input name="addDate" type="hidden" value="' + date + '"/>');
            $('#formAnnex').append('<input name="annexYear" type="hidden" value="' + year + '"/>');
            $('#formAnnex').append('<input name="annexItem" type="hidden" value="-1"/>');

            // 与macro.js联用
            mui(".mui-button-row").on("tap", ".capture-btn", function () {
                var cap = jQuery("#captureFile").get(0);
                cap.click();
            });

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

                var progress = mui('#progress')[0].value;

                var formData = new FormData($('#formAnnex')[0]);
                for (i = 0; i < blob_arr.length; i++) {
                    var _blobObj = blob_arr[i];
                    formData.append('upload', _blobObj.blob, _blobObj.fname);
                }
                jQuery.ajax({
                    type: "post",
                    data: formData,
                    url: "../../public/workplan/addAnnex.do",
                    dataType: "html",
                    processData: false,
                    contentType: false,
                    beforeSend: function (XMLHttpRequest) {
                        jQuery.myloading();
                    },
                    complete: function (XMLHttpRequest, status) {
                        jQuery.myloading("hide");
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        if (data.ret == "1") {
                            $('.reply-ul').remove();
                            // 重新加载本月的汇报
                            var isShowToday = false;
                            loadMark(year, month, isShowToday);
                            showAnnexes(year, myMonth, day);
                        }
                        mui.toast(data.msg);
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        alert(XMLHttpRequest.responseText);
                    }
                });
            });
        }
    }

    function showAnnexes(year, month, day) {
        var date = year + '-' + month + '-' + day;
        var content = mark[date];
        if (content == null) {
            content = "";
        }

        // 判断是否可修改
        var canEidt = false;
        var m = '' + month;
        if (m.length < 2) {
            m = "0" + m;
        }
        if (day.length < 2) {
            day = "0" + day;
        }
        date = year + '-' + m + '-' + day;
        var d = new Date(date.replace(/-/g, "/"));
        var days = dateDiff(new Date(), d);
        var workplan_annex_day_edit_limit = <%=workplan_annex_day_edit_limit%>;
        // 如果相差天数大于规定值，则不允许添加
        if (days >= 0 && days <= workplan_annex_day_edit_limit) {
            canEidt = true;
        }

        var matterHtml = '';
        for (var i = 0; i < content.length; i++) {
            var id = content[i].id;
            var checkStatus = content[i].checkStatus;
            var imgPath = getImgPath(checkStatus);
            var userName = content[i].userName;
            var strEdit, strDel;
            var isWorkPlanManager = <%=isWorkPlanManager%>;
            var me = "<%=userName%>";
            // 如果是计划负责人或者是本人
            if (isWorkPlanManager || me == userName) {
                if (canEidt) {
                    strEdit = '<a class="mui-btn mui-btn-grey btn-edit" data-id="' + id + '">编辑</a>';
                }
                if (isWorkPlanManager) {
                    strDel = '<a class="mui-btn mui-btn-yellow btn-del" data-id="' + id + '">删除</a>';
                }
            } else {
                strEdit = "";
                strDel = "";
            }
            matterHtml += '<li class="announceItem mui-table-view-cell" id="item' + id + '" oldProgress=' + content[i].oldProgress + ' progress=' + content[i].progress + '>'
                + '<div class="mui-slider-right mui-disabled">'
                + strEdit
                + strDel
                + '</div>'
                + '<div class="mui-slider-handle">'
                + '<div><div class="fl announceImg">'
                + '<img src="' + imgPath + '"></div>'
                + '<p class="announceContent">' + content[i].content + '</p>'
                + '</div><div class="announceTime">' + content[i].realName + '&nbsp;&nbsp;原进度' + content[i].oldProgress + '%&nbsp;&nbsp;现进度' + content[i].progress + '%</div>'
                + '</div>'
                + '</li>';

            <%
            String vpath = cfg.get("file_workplan");
            String attachmentBasePath = request.getContextPath() + "/" + vpath + "/";
            %>
            var atts = content[i].attachments;
            for (var k in atts) {
                var att = atts[k];
                matterHtml += '<li class="mui-table-view-cell att-li" id="liAtt' + att.id + '">';
                if (canEidt) {
                    matterHtml += ' <div class="mui-slider-right mui-disabled"><a class="mui-btn mui-btn-red btn-att-del" data-attid="' + att.id + '">删除</a></div>';
                }
                matterHtml += '     <div class="mui-slider-handle">';
                matterHtml += '         <a class="attFile" link="<%=attachmentBasePath%>' + att.visualPath + '/' + att.diskName + '" target="_blank">';
                matterHtml += '         <img class="mui-media-object mui-pull-left" src="../images/file/' + att.icon + '"/>';
                matterHtml += '         <div class="mui-media-body">';
                matterHtml += att.name;
                matterHtml += '         </div>';
                matterHtml += '         </a>';
                matterHtml += '    </div>';
                matterHtml += '</li>';
            }
        }
        $('.matter').html(matterHtml);

        $('.btn-att-del').click(function () {
            var attId = $(this).data("attid");
            var btnArray = ['否', '是'];
            mui.confirm('您确定要删除附件么？', '提示', btnArray, function (e) {
                if (e.index == 1) {
                    $.ajax({
                        type: "post",
                        data: {
                            attId: attId,
                            skey: "<%=skey%>"
                        },
                        url: "../../public/workplan/delAnnexAttachment.do",
                        dataType: "html",
                        beforeSend: function (XMLHttpRequest) {
                            jQuery.myloading();
                        },
                        complete: function (XMLHttpRequest, status) {
                            jQuery.myloading("hide");
                        },
                        success: function (data, status) {
                            data = $.parseJSON(data);
                            if (data.ret == "1") {
                                $('#liAtt' + attId).remove();
                            }
                            mui.toast(data.msg);
                        },
                        error: function (XMLHttpRequest, textStatus) {
                            alert(XMLHttpRequest.responseText);
                        }
                    });
                }
            });
        })

        $(".mui-content").on("tap", ".attFile", function () {
            var url = jQuery(this).attr("link");
            var p = url.lastIndexOf(".");
            var ext = url.substring(p + 1);
            if (ext == "jpg" || ext == "jpeg" || ext == "png" || ext == "gif" || ext == "bmp") {
                // 与选项卡标签不兼容，被覆盖了，故还是用mui.openWindow显示
                // showImg(url);
                mui.openWindow({
                    "url": "../../public/img_show.jsp?path=" + encodeURI(url)
                })
            } else {
                mui.openWindow({
                    "url": "../../public/img_show.jsp?path=" + encodeURI(url)
                })
            }
        })

        $('.btn-edit').click(function () {
            var id = $(this).data('id');
            var $item = $('#item' + id);

            var content = $item.find('.announceContent').html();
            var oldProgress = $item.attr('oldProgress');
            var progress = $item.attr('progress');

            var matterHtml = '';
            matterHtml += '<li class="announceItem"><div><p class="announceContent">' + addAnnexHtml + '</p></div></li>';
            $('.matter').html(matterHtml);

            $('#content').val(content);
            mui('#progress')[0].value = progress;
            $('#progressLabel').text(progress);

            // 在表单中加入当前选择的日期
            $('#formAnnex').append('<input name="oldProgress" type="hidden" value="' + oldProgress + '"/>');
            $('#formAnnex').append('<input name="id" type="hidden" value="' + id + '"/>');

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

                var progress = mui('#progress')[0].value;
                var formData = new FormData($('#formAnnex')[0]);
                for (i = 0; i < blob_arr.length; i++) {
                    var _blobObj = blob_arr[i];
                    formData.append('upload', _blobObj.blob, _blobObj.fname);
                }
                jQuery.ajax({
                    type: "post",
                    data: formData,
                    url: "../../public/workplan/editAnnex.do",
                    dataType: "html",
                    processData: false,
                    contentType: false,
                    beforeSend: function (XMLHttpRequest) {
                        jQuery.myloading();
                    },
                    complete: function (XMLHttpRequest, status) {
                        jQuery.myloading("hide");
                    },
                    success: function (data, status) {
                        // console.log(data);
                        data = $.parseJSON(data);
                        if (data.ret == "1") {
                            var checkStatus = data.checkStatus;
                            var imgPath = getImgPath(checkStatus);

                            var strDel;
                            var isWorkPlanManager = <%=isWorkPlanManager%>;
                            if (isWorkPlanManager) {
                                strDel = '<a class="mui-btn mui-btn-yellow btn-del" data-id="' + id + '">删除</a>';
                            } else {
                                strDel = "";
                            }

                            var li = '<li class="announceItem mui-table-view-cell" id="item' + data.id + '">'
                                + '<div class="mui-slider-right mui-disabled">'
                                + '<a class="mui-btn mui-btn-grey btn-edit" data-id="' + data.id + '">编辑</a>'
                                + strDel
                                + '</div>'
                                + '<div class="mui-slider-handle">'
                                + '<div><div class="fl announceImg">'
                                + '<img src="' + imgPath + '"></div>'
                                + '<p class="announceContent">' + $('#content').val() + '</p>'
                                + '</div><div class="announceTime"><%=ud.getRealName()%>&nbsp;&nbsp;原进度<%=wptd.getInt("progress")%>%&nbsp;&nbsp;现进度' + progress + '%</div>'
                                + '</div>'
                                + '</li>';
                            $('.reply-ul').remove();
                            $('.matter').append(li);
                        }
                        mui.toast(data.msg);

                        $('.mui-btn').click(function () {
                            var id = $(this).data("id");
                            var btnArray = ['否', '是'];
                            mui.confirm('您确定要删除么？', '提示', btnArray, function (e) {
                                if (e.index == 1) {
                                    $.ajax({
                                        type: "post",
                                        data: {
                                            id: id,
                                            skey: "<%=skey%>"
                                        },
                                        url: "../../public/workplan/delAnnex.do",
                                        dataType: "html",
                                        beforeSend: function (XMLHttpRequest) {
                                            jQuery.myloading();
                                        },
                                        complete: function (XMLHttpRequest, status) {
                                            jQuery.myloading("hide");
                                        },
                                        success: function (data, status) {
                                            data = $.parseJSON(data);
                                            if (data.ret == "1") {
                                                var isShowToday = false;
                                                loadMark(year, month, isShowToday); // 重新加载汇报
                                                $('.announceItem').remove();
                                            }
                                            mui.toast(data.msg);
                                        },
                                        error: function (XMLHttpRequest, textStatus) {
                                            alert(XMLHttpRequest.responseText);
                                        }
                                    });
                                }
                            });
                        })
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        alert(XMLHttpRequest.responseText);
                    }
                });
            });
        });

        $('.btn-del').click(function () {
            var id = $(this).data("id");
            var btnArray = ['否', '是'];
            mui.confirm('您确定要删除么？', '提示', btnArray, function (e) {
                if (e.index == 1) {
                    $.ajax({
                        type: "post",
                        data: {
                            id: id,
                            skey: "<%=skey%>"
                        },
                        url: "../../public/workplan/delAnnex.do",
                        dataType: "html",
                        beforeSend: function (XMLHttpRequest) {
                            jQuery.myloading();
                        },
                        complete: function (XMLHttpRequest, status) {
                            jQuery.myloading("hide");
                        },
                        success: function (data, status) {
                            data = $.parseJSON(data);
                            if (data.ret == "1") {
                                var isShowToday = false;
                                loadMark(year, month, isShowToday); // 重新加载汇报
                                $('#item' + id).remove();
                            }
                            mui.toast(data.msg);
                        },
                        error: function (XMLHttpRequest, textStatus) {
                            alert(XMLHttpRequest.responseText);
                        }
                    });
                }
            });
        })
    }

    // 对Date的扩展，将 Date 转化为指定格式的String
    // 月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符，
    // 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字)
    // 例子：
    // (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423
    // (new Date()).Format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18
    Date.prototype.format = function(fmt) {
        var o = {
            "M+" : this.getMonth()+1,                 //月份
            "d+" : this.getDate(),                    //日
            "h+" : this.getHours(),                   //小时
            "m+" : this.getMinutes(),                 //分
            "s+" : this.getSeconds(),                 //秒
            "q+" : Math.floor((this.getMonth()+3)/3), //季度
            "S"  : this.getMilliseconds()             //毫秒
        };
        if(/(y+)/.test(fmt))
            fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));
        for(var k in o)
            if(new RegExp("("+ k +")").test(fmt))
                fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
        return fmt;
    }

    function dateDiff(d1, d2) {
        // 注意要计算相隔天数，下列两行计算的是相差天数，如果两个时间点相差不足24时，会被计为相差0天
        // var times = d1.getTime() - d2.getTime();
        // return parseInt(times / (1000 * 60 * 60 * 24));
        var sd1 = d1.format("yyyy-MM-dd");
        var sd2 = d2.format("yyyy-MM-dd");
        var aDate, oDate1, oDate2;
        aDate = sd1.split("-");
        oDate1 = new Date(aDate[1] + '-' + aDate[2] + '-' + aDate[0]); //转换为12-18-2002格式  
        aDate = sd2.split("-");
        oDate2 = new Date(aDate[1] + '-' + aDate[2] + '-' + aDate[0]);
        return parseInt((oDate1.getTime() - oDate2.getTime()) / 1000 / 60 / 60 /24) //把相差的毫秒数转换为天数  
    }

    function getImgPath(checkStatus) {
        var imgPath;
        switch (checkStatus) {
            case 0:
                imgPath = "../../images/check_wait.png";
                break;
            case 1:
                imgPath = "../../images/check_pass.png";
                break;
            case 2:
                imgPath = "../../images/check_unpass.png";
                break;
            default:
                imgPath = "../../images/fileicon/txt.gif";
        }
        return imgPath;
    }

    var iosCallJS = '{ "btnAddShow":0, "isOnlyCamera":"true", "btnAddUrl":"", "btnBackUrl":"weixin/workplan/workplan_show.jsp?id=<%=workplanId%>" }';

    function callJS() {
        return {"btnAddShow": 0, "isOnlyCamera": "true", "btnAddUrl": "", "btnBackUrl": "weixin/workplan/workplan_show.jsp?id=<%=workplanId%>"};
    }

</script>

<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=skey%>"/>
    <jsp:param name="isBarBtnAddShow" value="true"/>
    <jsp:param name="barBtnAddUrl" value="calendar_add.jsp"/>
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>