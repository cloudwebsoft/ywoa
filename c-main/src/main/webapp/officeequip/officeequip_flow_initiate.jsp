<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.util.file.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="java.io.*"%>
<%@page import="com.redmoon.oa.officeequip.OfficeOpDb"%>
<%@page import="com.redmoon.oa.basic.TreeSelectDb"%>
<%@page import="com.redmoon.oa.person.UserDb"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil" />
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege" />
<%
String qStr = request.getQueryString();
if (qStr!=null) {
	if (!cn.js.fan.security.AntiXSS.antiXSS(qStr).equals(qStr)) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "参数非法！"));
		return;
	}
}
String priv = "read";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request,
			cn.js.fan.web.SkinUtil.LoadString(request,
					"pvg_invalid")));
	return;
}
%>
<body background="" leftmargin="0" topmargin="5" marginwidth="0" marginheight="0">
	<%
		String userName = privilege.getUser(request);
		int equipId = ParamUtil.getInt(request, "id");
		String typeCode = "ypghwp";

		Leaf lf = new Leaf();
		lf = lf.getLeaf(typeCode);

		if (lf == null) {
			out.print(StrUtil.Alert_Back("流程不存在"));
			return;
		}

		if (lf.getType() == Leaf.TYPE_NONE) {
			out.print(StrUtil.Alert_Back("流程不存在"));
			return;
		}

		WorkflowMgr wm = new WorkflowMgr();
		long startActionId = -1;

		long projectId = ParamUtil.getLong(request, "projectId", -1);
		int level = ParamUtil.getInt(request, "level",
				WorkflowDb.LEVEL_NORMAL);
		
		OfficeOpDb ooDb = new OfficeOpDb(equipId);
		int flowStatus = ooDb.checkIsFlowing("ghwpex");

		if (flowStatus == OfficeOpDb.RETURN_FLOWNONE) {
			try {
				startActionId = wm.initWorkflow(userName, typeCode, "归还办公用品", projectId,
						level);

				MyActionDb mad = new MyActionDb();
				mad = mad.getMyActionDb(startActionId);

				FormDb fd = new FormDb();
				fd = fd.getFormDb("ghwpex");
				FormDAO fdao = new FormDAO();
				fdao = fdao.getFormDAO((int) mad.getFlowId(), fd);
				fdao.setFieldValue("equip_id", "" + equipId);
				fdao.setFieldValue("officeCode", ooDb.getOfficeCode());
				TreeSelectDb tsDb = new TreeSelectDb(ooDb.getOfficeCode());
				fdao.setFieldValue("officeName", tsDb.getName());
				fdao.setFieldValue("borrowCount", "" + ooDb.getCount());
				fdao.setFieldValue("person", "" + ooDb.getPerson());
				UserDb ud = new UserDb(ooDb.getPerson());
				fdao.setFieldValue("personReal", "" + ud.getRealName());
				fdao.setFieldValue("opDate", "" + DateUtil.format(ooDb.getOpDate(), "yyyy年MM月dd日"));
				fdao.save();

				response
						.sendRedirect("../flow_dispose.jsp?myActionId="
								+ startActionId);
			} catch (ErrMsgException e) {
				out.print(fchar.Alert_Back(e.getMessage()));
				e.printStackTrace();
			}
		} else if (flowStatus == OfficeOpDb.RETURN_FLOWING) {
			out.print(StrUtil.Alert("归还流程已发起！"));
		} else if (flowStatus == OfficeOpDb.RETURN_FLOWED) {
			out.print(StrUtil.Alert("办公用品已归还！"));
		}
	%>
</body>