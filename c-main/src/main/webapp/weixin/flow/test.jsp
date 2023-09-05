<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.android.Privilege"%>
<%@ page import="cn.js.fan.util.ParamUtil"%>
<%@ page import="org.json.*"%>
<%@ page import="com.redmoon.oa.dept.DeptDb" %>
<!DOCTYPE>
<html>
<head>
<title>待办流程</title>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta name="viewport"
			content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<meta content="telephone=no" name="format-detection" />
<link rel="stylesheet" href="../css/mui.css">
<link rel="stylesheet" href="../css/iconfont.css" />
<link rel="stylesheet" type="text/css"
			href="../css/mui.picker.min.css" />
<link href="../css/mui.indexedlist.css" rel="stylesheet" />
<link rel="stylesheet" href="../css/at_flow.css" />
<link rel="stylesheet" href="../css/my_dialog.css" />
</head>
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
</style>

<body>
<div class="mui-content">
  <%
  DeptDb dd = new DeptDb();
  dd = dd.getDeptDb("0001");
  dd.setName("市场部3001");
  dd.setId(750091106);
  dd.save();
  %>
  <form id="free_flow_form" action="../../public/flow_dispose_free_do.jsp" method="post" enctype="multipart/form-data">
  </form>
  <form class="mui-input-group submitFlow" id="flow_form">
    <div class="mui-input-row">
      <label style="color:#000;width:100%;font-weight:bold">名片申请</label>
    </div>
    <div class="mui-input-row mui-select" id="row_bm" data-code="bm" data-isnull="true">
      <label><span style="color:#000">部门</span></label>
      <select name="bm" id="bm">
        <option value="root" disabled="">全部</option>
        <option value="0001" selecteddisabled="">总经理</option>
        <option value="0002" disabled="">副总经理</option>
        <option value="0003" disabled="">行政部</option>
        <option value="0004" disabled="">市场部</option>
        <option value="0005" disabled="">财务部</option>
      </select>
    </div>
    <div class="mui-input-row" id="row_cz" data-code="cz" data-isnull="true">
      <label><span style="color:#000">传真</span></label>
      <input type="text" name="cz" id="cz" value="" class="" readonly="">
    </div>
    <div class="mui-input-row" id="row_sj" data-code="sj" data-isnull="true">
      <label><span style="color:#000">手机</span></label>
      <input type="text" name="sj" id="sj" value="1" class="" readonly="">
    </div>
    <div class="mui-input-row" id="row_yx" data-code="yx" data-isnull="true">
      <label><span style="color:#000">邮箱</span></label>
      <input type="text" name="yx" id="yx" value="1" class="" readonly="">
    </div>
    <div class="mui-input-row" id="row_zw" data-code="zw" data-isnull="true">
      <label><span style="color:#000">职务</span></label>
      <input type="text" name="zw" id="zw" value="1" class="" readonly="">
    </div>
    <div class="mui-input-row" id="row_spr" data-code="spr" data-isnull="true">
      <label><span style="color:#000">审批人</span></label>
      <div class="opinionContent">
        <h5 class="content_h5"></h5>
        <div><span class="name mui-h6">管理员</span><span class="date mui-h6">2017-11-15 07:42</span> </div>
      </div>
    </div>
    <div class="mui-input-row" id="row_sqr" data-code="sqr" data-isnull="true">
      <label><span style="color:#000">申请人</span></label>
      <input type="hidden" name="sqr" id="sqr" value="admin">
      <input type="text" value="管理员" readonly="readonly">
    </div>
    <div class="mui-input-row" id="row_gddh" data-code="gddh" data-isnull="true">
      <label><span style="color:#000">固定电话</span></label>
      <input type="text" name="gddh" id="gddh" value="" class="" readonly="">
    </div>
    <div class="mui-input-row" id="row_qzlq" data-code="qzlq" data-isnull="true">
      <label><span style="color:#000">签字领取</span></label>
      <input type="text" name="qzlq" id="qzlq" value="" class="signInput " readonly="">
    </div>
    <div class="mui-input-row" id="row_sqrq" data-code="sqrq" data-isnull="true">
      <label><span style="color:#000">申请日期</span></label>
      <input type="text" name="sqrq" id="sqrq" kind="=&quot;DATE&quot;" class="input-icon" value="2017-11-15" readonly="readonly">
    </div>
    <div class="mui-input-row" id="row_xzqr" data-code="xzqr" data-isnull="true">
      <label><span style="color:#000">行政确认</span></label>
      <div class="opinionContent">
        <h5 class="content_h5"></h5>
        <div><span class="name mui-h6">管理员</span><span class="date mui-h6">2017-11-15 07:42</span> </div>
      </div>
    </div>
    <div class="mui-input-row" id="row_yssl" data-code="yssl" data-isnull="true">
      <label><span style="color:#000">印刷数量</span></label>
      <input type="text" name="yssl" id="yssl" value="" class="" readonly="">
    </div>
    <div class="mui-input-row" id="row_myflow" data-code="myflow" data-isnull="true">
      <label><span style="color:#000">我的流程</span></label>
    </div>
    <div class="mui-button-row">
      <button type="button" class="mui-btn mui-btn-primary mui-btn-outlined flow_submit" islight="false">提交</button>
      <button style="margin-left:5px;" type="button" class="mui-btn mui-btn-primary mui-btn-outlined capture_btn">照片</button>
      <button style="margin-left:5px;" type="button" class="mui-btn mui-btn-primary mui-btn-outlined capture_btn" onclick="clickprompt()">扫描</button>
    </div>
    <script>
    function clickprompt(){
    	// 调用prompt（）
    	var result=prompt("js://webview?action=qrcode&param=mynameisfgf");
    	// alert("demo " + result);
	}
    </script>
    <input type="hidden" name="expireHours" value="0">
    <input type="hidden" name="isToMobile" value="true">
    <input type="hidden" id="flowId" name="flowId" value="188">
    <input type="hidden" id="myActionId" name="myActionId" value="211">
    <input type="hidden" name="isUseMsg" value="true">
    <input type="hidden" name="cws_lontitude" value="">
    <input type="hidden" name="cws_latitude" value="">
    <input type="hidden" name="cws_address" value="">
    <input type="hidden" name="actionId" value="223">
    <input type="hidden" id="skey" name="skey" value="9d46250122c009134f9bfe303dded9868775f555d862c173">
    <input type="hidden" name="orders" value="1">
    <input type="hidden" name="op" id="op" value="finish">
    <input type="hidden" name="cwsWorkflowTitle" value="名片申请">
    
  </form>
  <input type="file" id="captureFile" name="upload" accept="image/*">
</div>
</body>
</html>
