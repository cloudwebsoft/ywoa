<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.android.Privilege"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="org.json.*"%>
<!DOCTYPE html>
<html>
	<head>
		<title>待办流程</title>
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="cache-control" content="no-cache">
		<meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
		<meta name="apple-mobile-web-app-capable" content="yes">
		<meta name="apple-mobile-web-app-status-bar-style" content="black">
		<meta content="telephone=no" name="format-detection" />
		<link rel="stylesheet" href="../css/mui.css">
		<link rel="stylesheet" href="../css/iconfont.css" />
		<link rel="stylesheet" href="../css/mui.picker.min.css">
		<link rel="stylesheet" href="../css/at_flow.css" />
		<link rel="stylesheet" href="../css/my_dialog.css" />
        
   		<link href="../../lte/css/bootstrap.min.css?v=3.3.6" rel="stylesheet">
        <link href="../../lte/css/font-awesome.css?v=4.4.0" rel="stylesheet">
        <link href="../../lte/css/animate.css" rel="stylesheet">
	    <link href="../../lte/css/style.css?v=4.1.0" rel="stylesheet">        
	</head>
	<style>
	body {
		font-size:17px;
		background-color:#efeff4;		
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
		margin-left:10px;
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
	<%
		String skey = ParamUtil.get(request, "skey");
		Privilege pvg = new Privilege();
		if (!pvg.auth(request)) {
			out.print(StrUtil.p_center("请登录"));
			return;
		}
		String userName = pvg.getUserName();
		UserDb ud = new UserDb();
		ud = ud.getUserDb(userName);
			
		int myActionId = ParamUtil.getInt(request, "myActionId", 0);
		String code = ParamUtil.get(request,"code"); // 流程类型
		int type = ParamUtil.getInt(request,"type",0);
		String title = ParamUtil.get(request,"title");
		int flowId = -1;
		long actionId = -1;
		if (myActionId!=0) {
			MyActionDb mad = new MyActionDb();
			mad = mad.getMyActionDb(myActionId);
			if (mad==null || !mad.isLoaded()) {
				out.print("待办记录已不存在！");
				return;
			}
			if (mad.getCheckStatus()==MyActionDb.CHECK_STATUS_CHECKED) {
				out.print("流程已处理！");
				return;				
			}
			actionId = mad.getActionId();
			flowId = (int)mad.getFlowId();
			WorkflowDb wf = new WorkflowDb();
			wf = wf.getWorkflowDb(flowId);
			code = wf.getTypeCode();
			// System.out.println(getClass() + " flowId=" + flowId + " code=" + code);
		}
		Leaf lf = new Leaf();
		lf = lf.getLeaf(code);
		if (lf==null && !lf.isLoaded()) {
			out.print("流程类型已不存在！");
			return;
		}
		String formCode = lf.getFormCode();
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);
		if (fd==null && !fd.isLoaded()) {
			out.print("表单类型已不存在！");
			return;
		}

		boolean isInit = myActionId==0; // 是否为发起流程
		
		WorkflowPredefineDb wpd = new WorkflowPredefineDb();
		wpd = wpd.getPredefineFlowOfFree(code);
		boolean isRecall = wpd.isRecall();
	%>	
		<div class="mui-content">
        	<%if (!isInit && !wpd.isLight()) {%>
            <div style="padding: 10px 10px;">
                <div id="segmentedControl" class="mui-segmented-control">
                    <a class="mui-control-item mui-active" href="#item1">
                    	待办
                    </a>
                    <a class="mui-control-item" href="#item2">
						过程
                    </a>
                </div>
            </div>
            <%}%>
            <div>
                <div id="item1" class="mui-control-content mui-active">     
                    <form id="free_flow_form" action="../../public/flow_dispose_free_do.jsp"  method="post" enctype="multipart/form-data">
                    </form>
                    <form class="mui-input-group" id="flow_form" >
                    </form>
                    <input type="file" id="captureFile" name="upload" accept="image/*"  />
                    <%
                    String dis = "";
                    if (!fd.isProgress()) {
                        dis = "display:none";
                    }
                    if (wpd.isReply() && !code.equals("at")) { %>
                    <div class="annex-group">
                        <div class="reply-form" style="display:none; margin-bottom:10px">
                        <div class="mui-input-row mui-input-range"s>
                            <label>进度<span id="progressLabel" style="margin-left:10px"></span></label>
                            <input id="progress" name="progress" type="range" min="0" max="100" onchange="$('#progressLabel').text(mui('#progress')[0].value)">
                        </div>
                        <div class="mui-input-row" data-code="content" data-isnull="false">
                            <label><span>回复</span><span style='color:red;'>*</span></label>
                            <div style="text-align:center">  
                            <textarea id="content" name="content" placeholder="请输入回复内容" style="width:96%; height:150px;"></textarea>
                            </div>
                        </div>			
                        <div class="mui-input-row mui-checkbox" data-code="isSecret">
                            <label><span>隐藏</span><span style='color:red;'>*</span></label>
                            <input type="checkbox" id="isSecret" name="isSecret" value="1"/>
                        </div>					
                        <div class="mui-button-row">
                            <button type="button" class="mui-btn mui-btn-primary mui-btn-outlined btn-ok">确定</button>
                        </div>	
                        </div>					
                    </div>
                    <%} %>                 
                </div>
                
                <%if (!isInit && !wpd.isLight()) {%>
				<div id="item2" class="mui-control-content">
                    <div id="vertical-timeline" class="vertical-container light-timeline">
    					<%
    					UserMgr um = new UserMgr();
						String sql = "select id from flow_my_action where flow_id=" + flowId + " order by receive_date asc";
						MyActionDb mad = new MyActionDb();
						Vector v = mad.list(sql);	
						Iterator ir = v.iterator();
						while (ir.hasNext()) {		
							mad = (MyActionDb)ir.next();
                            String userRealName;
                       		if (!mad.getProxyUserName().equals("")) {
								userRealName = um.getUserDb(mad.getProxyUserName()).getRealName();
							}
							else {
	                            UserDb user = um.getUserDb(mad.getUserName());
	                            userRealName = user.getRealName();							
							}
							
                        	WorkflowActionDb wad = new WorkflowActionDb();	
                        	wad = wad.getWorkflowActionDb((int)mad.getActionId());							
    					%>
                        <div class="vertical-timeline-block" style="margin-bottom:10px">
                            <div class="vertical-timeline-icon blue-bg">
                                <i class="fa fa-user"></i>
                            </div>
                            <div class="vertical-timeline-content">
                                <h3><%=StrUtil.getNullStr(wad.getTitle())%></h3>
                                <p>
									<%
									if (mad.getChecker().equals(UserDb.SYSTEM)) {
										String str = LocalUtil.LoadString(request,"res.flow.Flow","skipOverTime");
										out.print(str);
									}else{						
									%>
									<%=mad.getCheckStatusName()%>
									<%}
									if (mad.getCheckStatus()!=0 && mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_TRANSFER && mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {
										  if (mad.getResultValue()!=WorkflowActionDb.RESULT_VALUE_RETURN) {
											  if (mad.getSubMyActionId()==MyActionDb.SUB_MYACTION_ID_NONE)
											  	out.print("(" + WorkflowActionDb.getResultValueDesc(mad.getResultValue()) + ")");
										  }
									}
									%>
								</p>
			                    <%
					    		if (isRecall && mad.canRecall(userName)) {
								%>								
                                <a href="#" class="btn btn-sm btn-success btn-recall" myActionId="<%=mad.getId()%>">撤回</a>
                                <%} %>
                                <span class="vertical-date">
                                	<%=userRealName %> <br>
                                <small><%=DateUtil.format(mad.getCheckDate(), "MM-dd HH:mm")%></small>
                        		</span>
                            </div>
                        </div>
    					<%} %>
                    </div>     
				</div>      
				<%}%>
            </div>
		</div>
        <script>
			var appProp = {"type": "module", "isOnlyCamera": "<%=fd.isOnlyCamera()%>", "btnAddShow": 0, "btnBackUrl": ""};

			function callJS() {
				return appProp;
			}

			var iosCallJS = JSON.stringify(appProp);
			// var iosCallJS = '{ "type": "module", "isOnlyCamera": "<%=fd.isOnlyCamera()%>", "btnAddShow": 0, "btnBackUrl": "" }';

			function setIsOnlyCamera(isOnlyCamera) {
				appProp.isOnlyCamera = isOnlyCamera;
				iosCallJS = JSON.stringify(appProp);
			}

			function resetIsOnlyCamera() {
				appProp.isOnlyCamera = "<%=fd.isOnlyCamera()%>"; // 用以在图像宏控件拍完照后，恢复底部拍照按钮的设置
			}
        </script>
		<script type="text/javascript" src="../../inc/common.js"></script>
		<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
		<script src="../js/jq_mydialog.js"></script>
		<script type="text/javascript" src="../js/newPopup.js"></script>
		<script src="../js/macro/macro.js"></script>
		<script src="../js/mui.min.js"></script>
		<script src="../js/mui.picker.min.js"></script>
        
        <link rel="stylesheet" href="../css/photoswipe.css">
        <link rel="stylesheet" href="../css/photoswipe-default-skin/default-skin.css">	
        <script type="text/javascript" src="../js/photoswipe.js"></script>
        <script type="text/javascript" src="../js/photoswipe-ui-default.js"></script>
        <script type="text/javascript" src="../js/photoswipe-init-manual.js"></script>	
                
		<script type="text/javascript" src="../js/base/mui.form.js"></script>
		<script type="text/javascript" src="../js/mui.flow.wx.js"></script>
		<script type="text/javascript" charset="utf-8">
		var content = document.querySelector('.mui-content');
		var skey = '<%=skey%>';
		var myActionId = '<%=myActionId%>';
		var code = '<%=code%>';
		var title = '<%=title%>';
		var type = <%=type%>;
		var options = {
					"skey" : skey,
					"title":title,
					"myActionId":myActionId,
					"type":type,
					"code":code
				};
			
		<%
		// myActionId为0表示发起流程
		JSONObject extraData = new JSONObject();
		if (myActionId==0) {
			Enumeration paramNames = request.getParameterNames();
			while (paramNames.hasMoreElements()) {
				String paramName = (String) paramNames.nextElement();
				String[] paramValues = ParamUtil.getParameters(request, paramName); // 因为参数来自于url链接中，所以一定得通过ParamUtil.getParameters转换，否则会为乱码
				if (paramValues.length == 1) {
					String paramValue = paramValues[0];
					// 过滤掉formCode
					if (paramName.equals("myActionId") || paramName.equals("title") || paramName.equals("type") || paramName.equals("code") || paramName.equals("skey"))
						;
					else {
						 extraData.put(paramName, paramValue);
		            }
				}
			}	
		}
		%>		
		
		options.extraData = '<%=extraData%>';

		window.flow = new mui.Flow(content, options);
		window.flow.flowDisposeInit();
		        
		$(function() {
			$('.btn-ok').click(function() {
				var _tips = "";
				jQuery("div[data-isnull='false']").each(function(i){
					var _code = jQuery(this).data("code");
					var _val = jQuery("#"+_code).val();
					if(_val == undefined || _val == ""){
					   var _text = jQuery(this).find("span:first").text();
					   _tips += _text + " 不能为空<BR/>"
					}
				});
				if(_tips != null && _tips!= ""){
					mui.toast(_tips);
					return;
				}
				
				var progress = mui('#progress')[0].value;
				var isSecret = jQuery('#isSecret').is(":checked")?1:0;
				$.ajax({
					type: "post",
					url: "../../public/flow/addReply.do",
					data: "skey=<%=skey%>&content=" + jQuery('#content').val() + "&isSecret=" + isSecret + "&progress=" + progress + "&flowId=<%=flowId%>&actionId=<%=actionId%>",
					dataType: "html",
					contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
					beforeSend: function(XMLHttpRequest) {
					},
					success: function(data, status) {
						data = $.parseJSON(data);
						if (data.ret=="1") {
							var $ul = $('.reply-ul');
							$ul.show();
							var li = '<li class="mui-table-view-cell">';
							li += '<div class="reply-header">';
							li += '<span class="reply-name"><%=ud.getRealName() %></span>';
							li += '<span class="reply-progress">' + progress + '%</span>';
							li += '<span class="reply-date"><%=DateUtil.format(new Date(), "yyyy-MM-dd")%></span>';
							li += '</div>';
							li += '<div class="reply-content">' + $('#content').val() + '</div>';
							li += '</li>';
							$ul.append(li);
							
							$('#progressLabel').text(progress);
							$('#content').val('');
							// 不删除，使可以继续回复
							// $('.reply-form').remove();
					    }
					    mui.toast(data.msg);
					},
					error: function(XMLHttpRequest, textStatus) {
						alert(XMLHttpRequest.responseText);
					}
				});	
			});
		});
	</script>
	<script src="form_js/<%=lf.getFormCode()%>.jsp?flowId=<%=flowId%>&myActionId=<%=myActionId%>&skey=<%=skey %>&Fcode=<%=code %>&Ftype=<%=type %>&Ftitle=<%=title %>"></script>
	<jsp:include page="../inc/navbar.jsp">
		<jsp:param name="skey" value="<%=skey%>" />
		<jsp:param name="isBarBottomShow" value="false"/>
	</jsp:include>
	</body>
</html>
