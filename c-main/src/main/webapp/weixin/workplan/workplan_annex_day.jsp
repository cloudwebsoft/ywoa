<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="com.redmoon.oa.android.Privilege"%>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr"%>
<%@page import="com.redmoon.oa.person.UserDb"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@ page import="com.redmoon.oa.workplan.WorkPlanTaskDb" %>
<%@ page import="cn.js.fan.util.DateUtil" %>
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

	long id = ParamUtil.getLong(request, "id", -1);

	boolean isWorkPlanManager = false;
	com.redmoon.oa.workplan.Privilege pvgWorkplan = new com.redmoon.oa.workplan.Privilege();
	if (pvgWorkplan.canUserManageWorkPlan(request, (int)id))
		isWorkPlanManager = true;

	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
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
		<div id="segmentedControl" class="mui-segmented-control mui-segmented-control-inverted">
			<a class="mui-control-item workplan-task">
				任务
			</a>
			<a class="mui-control-item workplan-detail">
				详情
			</a>
			<a class="mui-control-item mui-active annex-day">
				日报
			</a>
			<a class="mui-control-item annex-week" href="#item4">
				周报
			</a>
			<a class="mui-control-item" href="#item5">
				月报
			</a>
		</div>
		<script>
			$(function() {
				mui('#segmentedControl').on('tap', '.workplan-task', function () {
					mui.openWindow({
						"url": "workplan_show.jsp?id=<%=id%>"
					})
				});
				mui('#segmentedControl').on('tap', '.workplan-detail', function () {
					mui.openWindow({
						"url": "workplan_show.jsp?id=<%=id%>&action=detail"
					})
				});
				mui('#segmentedControl').on('tap', '.annex-week', function () {
					mui.openWindow({
						"url": "workplan_annex_list_week.jsp?id=<%=id%>"
					})
				});
			})
		</script>
	</div>
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
<script type="text/javascript">
	var myCalendar = new SimpleCalendar('#calendar');
	$(function() {
		var year = $('.sc-select-year').text();
		var monthCH = $('.sc-select-month').text();
		var month = SimpleCalendar.prototype.languageData.months_CH.indexOf(monthCH)+1;
		loadMark(year, month);
		
		$(".sc-mleft").click(function(){
		   myCalendar.subMonth();
		   var year = $('.sc-select-year').text();
		   var monthCH = $('.sc-select-month').text();
		   var month = SimpleCalendar.prototype.languageData.months_CH.indexOf(monthCH)+1;
		   
		   loadMark(year, month);
	    })
		$(".sc-mright").click(function(){
			myCalendar.addMonth();
			var year = $('.sc-select-year').text();
			var monthCH = $('.sc-select-month').text();
			var month = SimpleCalendar.prototype.languageData.months_CH.indexOf(monthCH)+1;
			
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
			url: "../../public/workplan/listAnnexesOfWorkplan.do",
            async: false,
			type: "post",
			data: {
				skey: "<%=skey%>",
				year: y,
				month: m,
				id: "<%=id%>"
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest) {
			},
			success: function(data, status) {
				data = $.parseJSON(data);
				mark = data.result;
				myCalendar._defaultOptions.mark = mark;
				myCalendar.update(m, y);
				// 显示当天的活动在初始化mark之后
				// 初始化今天的活动
				if (isShowToday==undefined || isShowToday) {
					announceList($('.sc-today'));
				}
			},
			complete: function(XMLHttpRequest, status){
			},
			error: function(XMLHttpRequest, textStatus){
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
	$('#calendar').on("click", '.sc-selected', function() {
		announceList($(this));
	});
	
	// 显示选择日期当天的活动
	function announceList(v) {
		if(v.children().hasClass('sc-mark-show')) {
			var year = $('.sc-select-year').text();
			var monthCH = $('.sc-select-month').text();
			var day = v.children()[1].innerText;
			var month = SimpleCalendar.prototype.languageData.months_CH.indexOf(monthCH)+1;
            showAnnexes(year, month, day);
		}
	}

	function showAnnexes(year, month, day) {
        var date = year + '-' + month + '-' + day;
        var content = mark[date];
        if (content==null) {
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

        var matterHtml='';
        for (var i = 0; i < content.length; i++) {
            var id = content[i].id;
            var checkStatus = content[i].checkStatus;
            var imgPath = getImgPath(checkStatus);
            var userName = content[i].userName;
            matterHtml += '<li class="announceItem mui-table-view-cell" id="item' + id + '" oldProgress=' + content[i].oldProgress + ' progress=' + content[i].progress + '>'
                + '<div class="mui-slider-right mui-disabled">'
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
            for(var k in atts) {
                var att = atts[k];
                matterHtml += '<li class="mui-table-view-cell att-li" id="liAtt' + att.id + '">';
                if (canEidt) {
                    matterHtml += ' <div class="mui-slider-right mui-disabled"><a class="mui-btn mui-btn-red btn-att-del" data-attid="' + att.id + '">删除</a></div>';
                }
                matterHtml += '     <div class="mui-slider-handle">';
                matterHtml += '         <a class="attFile" link="<%=attachmentBasePath%>' + att.visualPath + '/' + att.diskName + '" target="_blank">';
                matterHtml += '         <img class="mui-media-object mui-pull-left" src="../images/file/' + att.icon + '"/>';
                matterHtml += '         <div class="mui-media-body">';
                matterHtml +=               att.name;
                matterHtml += '         </div>';
                matterHtml += '         </a>';
                matterHtml += '    </div>';
                matterHtml += '</li>';
            }
        }
        $('.matter').html(matterHtml);

        $(".mui-content").on("tap", ".attFile", function () {
            var url = jQuery(this).attr("link");
            var p = url.lastIndexOf(".");
            var ext = url.substring(p + 1);
            if (ext == "jpg" || ext == "jpeg" || ext == "png" || ext == "gif" || ext == "bmp") {
                // 与选项卡标签不兼容，被覆盖了，故还是用mui.openWindow显示
                // showImg(url);
                mui.openWindow({
                    "url": url
                })
            }
            else {
                mui.openWindow({
                    "url": url
                })
            }
        })
    }

	function dateDiff(d1, d2) {
		var times = d1.getTime() - d2.getTime();
		return parseInt(times / (1000 * 60 * 60 * 24));
	}

	function getImgPath(checkStatus) {
		var imgPath;
		switch(checkStatus) {
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

	var iosCallJS = '{ "btnAddShow":0, "isOnlyCamera":"true", "btnAddUrl":"" }';
	function callJS(){
	  return { "btnAddShow":0, "isOnlyCamera":"true", "btnAddUrl":"" };
	}
</script>

<jsp:include page="../inc/navbar.jsp">
	<jsp:param name="skey" value="<%=skey%>" />
	<jsp:param name="isBarBtnAddShow" value="true" />
	<jsp:param name="barBtnAddUrl" value="calendar_add.jsp" />
	<jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>