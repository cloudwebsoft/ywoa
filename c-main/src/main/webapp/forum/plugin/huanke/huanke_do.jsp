<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.plugin.huanke.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.setup.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%
Privilege privilege = new Privilege();
if (!privilege.isUserLogin(request)) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "err_not_login")));
	return;
}

String msgId = ParamUtil.get(request, "msgId");
if (msgId.equals("")) {
	out.print(SkinUtil.makeErrMsg(request, "缺少文章编号！"));
	return;
}	

String op = ParamUtil.get(request, "op");
if(op.equals("exchange")){	
	
	MsgMgr mm = new MsgMgr();
	MsgDb md = mm.getMsgDb(Long.parseLong(msgId));
	
	HuankeGoodsDb hgd = new HuankeGoodsDb();
	hgd = hgd.getHuankeGoodsDb(md.getRootid());
	
	hgd.setStatus(HuankeGoodsDb.HUANKE_GOOD_STATUS_EXCHANGED);
	hgd.setMsgId(Long.parseLong(msgId));
	
	if(hgd.save()){
		out.print(StrUtil.Alert_Redirect("物品交换成功！", "../../showtopic.jsp?rootid=" + md.getRootid()));
		return;
	}else{
		out.print(StrUtil.Alert_Back("物品交换失败！"));
		return;
	}	
}
%>