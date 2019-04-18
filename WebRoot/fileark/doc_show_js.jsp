<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "com.redmoon.oa.kaoqin.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.robot.*"%>
<%@ page import = "java.net.*"%>
<%@ page import = "java.util.*"%>
<%
/*
- 功能描述：查看文章时载入的脚本
- 访问规则：从doc_show.jsp中通过include script访问
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：
==================
- 修改者：
- 修改时间：
- 修改原因: 
- 修改点:
*/
int id = ParamUtil.getInt(request, "id", -1);
Privilege pvg = new Privilege();
String userName = pvg.getUser(request);
String op = ParamUtil.get(request, "op");
if ("view".equals(op)) {
	com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();	
	
	// 每查看1次消耗3个积分
	ScoreRule sr = RobotUtil.getScoreRule("painting_view");
	int scorePerView = sr.getValue();
	
	// 如果消耗为0，则不记录
	if (scorePerView != 0) {
		FormDAO fdao = RobotUtil.getSign(userName);
		int score = StrUtil.toInt(fdao.getFieldValue("score"), 0) + scorePerView;
		int score_remained = StrUtil.toInt(fdao.getFieldValue("score_remained"), 0) + scorePerView;
		fdao.setFieldValue("score", String.valueOf(score));
		fdao.setFieldValue("score_remained", String.valueOf(score_remained));
		fdao.save();
		
		RobotUtil.logScoreDetail(userName, sr.getCode(), scorePerView, "", "", "");
	}
	
	String desKey = ssoCfg.get("key");
	String timeStamp = DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss");
	String visitKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(desKey, timeStamp + "|guest");
	JSONObject json = new JSONObject();
	json.put("ret", "1");
	json.put("visitKey", visitKey);
	out.print(json.toString());
	return;
}

// 每查看1次消耗3个积分
ScoreRule sr = RobotUtil.getScoreRule("painting_view");
int scorePerView = sr.getValue();
String formCode = "qqgroup_sign";

int scoreToUse = scorePerView; // 将消耗的分值
int scoreRemained = 0;

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

String errMsg = "";
boolean canView = true;
FormDAO fdao = RobotUtil.getSign(userName);
if (fdao!=null) {
    scoreRemained = StrUtil.toInt(fdao.getFieldValue("score_remained"), 0);
    if (scoreRemained < scoreToUse) {
        canView = false;
        errMsg = "积分不足：当前剩余积分为" + scoreRemained + "，需要消耗积分" + scoreToUse;
    }
}
else {
    canView = false;
    errMsg = "您没有积分记录，不能下载！";
}

String info = "您当前剩余积分" + scoreRemained + "，查看需消耗积分" + scoreToUse;
%>

function show() {
	<%if (!canView) {%>
	jAlert('<%=errMsg %>', '提示');
	return;
	<%} %>

	jConfirm('<%=info %>，您确定要浏览么？', '提示', function(r) {
		if (r) {
			$.ajax({
				type: "post",
				url: "<%=request.getContextPath()%>/fileark/doc_show_js.jsp",
				data: {
					op: "view"
				},
				dataType: "html",
				beforeSend: function(XMLHttpRequest) {
				},
				success: function(data, status) {
					data = $.parseJSON(data);
					if (data.ret=="1") {
						window.open('http://114.55.91.128/gallery/waterfall.jsp?visitKey=' + data.visitKey);
					}
					else {
						jAlert(data.msg, "提示");
					}
				},
				complete: function(XMLHttpRequest, status){
				},
				error: function(XMLHttpRequest, textStatus){
					// 请求出错处理
					alert(XMLHttpRequest.responseText);
				}
			});
		}
	});
}
