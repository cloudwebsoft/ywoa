<%@ page contentType="text/html; charset=utf-8"%><%@ page import="com.redmoon.oa.person.*"%><%@ page import="com.redmoon.oa.dept.*"%><%@ page import="com.redmoon.oa.flow.*"%><%@ page import="java.io.*"%><%@ page import="jxl.*"%><%@ page import="jxl.write.*"%><%@ page import="com.cloudwebsoft.framework.db.*"%><%@ page import="cn.js.fan.db.*"%><%@ page import="java.util.*"%><%@ page import="cn.js.fan.web.*"%><%@ page import="cn.js.fan.util.*"%><%@ page import="cn.js.fan.security.*"%><%@ page import="jxl.*"%><%@ page import="jxl.write.*"%>
<%@page import="com.redmoon.oa.flow.macroctl.MacroCtlUnit"%>
<%@page import="com.redmoon.oa.flow.macroctl.MacroCtlMgr"%>
<%@ page import="com.redmoon.oa.util.RequestUtil" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudweb.oa.utils.ConfigUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String typeCode = ParamUtil.get(request, "typeCode");
com.redmoon.oa.flow.Leaf lf = new com.redmoon.oa.flow.Leaf();
lf = lf.getLeaf(typeCode);
if (lf==null) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "typeCode=" + typeCode + " 流程节点不存在！"));
	return;
}

String fields = ParamUtil.get(request, "fields");
if (!fields.equals(lf.getExportColProps())) {
	lf.setExportColProps(fields);
	lf.update();
}

com.redmoon.oa.sso.Config cfg = new com.redmoon.oa.sso.Config();
String query = ParamUtil.get(request, "query");
String sql = cn.js.fan.security.ThreeDesUtil.decrypthexstr(cfg.get("key"), query);

FormDb fd = new FormDb();
fd = fd.getFormDb(lf.getFormCode());

String[] fieldsSelected = StrUtil.split(fields, ",");
if (fieldsSelected==null) {
	fieldsSelected = new String[0];
}

WorkflowDb wf = new WorkflowDb();
Vector v = wf.list(sql);

response.setContentType("application/vnd.ms-excel");
response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode("查询结果导出.xls"));  

OutputStream os = response.getOutputStream();

try {
	// File file = new File(Global.getAppPath(request) + "flow/blank.xls");
	/*File file = new File(Global.getRealPath() + "flow/blank.xls");
	Workbook wb = Workbook.getWorkbook(file);*/
	ConfigUtil configUtil = SpringUtil.getBean(ConfigUtil.class);
	InputStream inputStream = configUtil.getFile("templ/blank.xls");
	Workbook wb = Workbook.getWorkbook(inputStream);

	WorkbookSettings settings = new WorkbookSettings ();  
	settings.setWriteAccess(null);  

	// 打开一个文件的副本，并且指定数据写回到原文件
	WritableWorkbook wwb = Workbook.createWorkbook(os, wb, settings);
	WritableSheet ws = wwb.getSheet(0);

	Iterator ir = v.iterator();
	UserMgr um = new UserMgr();
	FormDAO fdao = new FormDAO();
	DeptMgr dm = new DeptMgr();

	int j = 0;
	Label t = new Label(0, j, "ID");
	ws.addCell(t);
	t = new Label(1, j, "标题");
	ws.addCell(t);
	int k = 2;

	for (int m=0; m<fieldsSelected.length; m++) {
		FormField ff = fd.getFormField(fieldsSelected[m]);
		t = new Label(k, j, ff.getTitle());
		ws.addCell(t);
		k++;
	}
	
	t = new Label(k, j, "发起时间");
	ws.addCell(t);
	k++;
	t = new Label(k, j, "发起人");
	ws.addCell(t);
	k++;
	t = new Label(k, j, "状态");
	ws.addCell(t);
	k++;
	t = new Label(k, j, "当前办理");
	ws.addCell(t);
	k++;
	
	MyActionDb mad = new MyActionDb();
	j = 1;
	MacroCtlMgr mm = new MacroCtlMgr();
	while (ir.hasNext()) {
		WorkflowDb wfd = (WorkflowDb)ir.next();
		
		fdao = fdao.getFormDAO(wfd.getId(), fd);
		
		// 置SQL宏控件中需要用到的fdao
		RequestUtil.setFormDAO(request, fdao);
		
		t = new Label(0, j, String.valueOf(wfd.getId()));
		ws.addCell(t);
		
		t = new Label(1, j, wfd.getTitle());
		ws.addCell(t);

		k = 2;
		for (int m=0; m<fieldsSelected.length; m++) {
			FormField ff = fd.getFormField(fieldsSelected[m]);
			String str = fdao.getFieldValue(ff.getName());
			
			if (ff.getType().equals(FormField.TYPE_MACRO)) {
				MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
				if (mu != null && !mu.getCode().equals("macro_raty")) {
					str = StrUtil.getAbstract(request, mu.getIFormMacroCtl().converToHtml(request, ff, str), 1000, "");
				}
			}
			t = new Label(k, j, str);
			ws.addCell(t);
			k++;
		}		

		t = new Label(k, j, DateUtil.format(wfd.getBeginDate(), "yy-MM-dd HH:mm:ss"));
		ws.addCell(t);
		k++;
		
		String userName = StrUtil.getNullStr(wfd.getUserName());
		String realName = "";
		if (!"".equals(userName)) {
			realName = um.getUserDb(userName).getRealName();
		}
		t = new Label(k, j, realName);
		ws.addCell(t);
		k++;
		t = new Label(k, j, wfd.getStatusDesc());
		ws.addCell(t);
		k++;
		
		String val = "";
		Iterator ir2 = mad.getMyActionDbDoingOfFlow(wfd.getId()).iterator();
		while (ir2.hasNext()) {
			MyActionDb madCur = (MyActionDb) ir2.next();
			if (!val.equals("")) {
				val += "、";
			}
			val += um.getUserDb(madCur.getUserName()).getRealName();
		}
		t = new Label(k, j, val);
		ws.addCell(t);
		k++;
		
		j++;
	}	
	
	wwb.write();
	wwb.close();
	wb.close();	
}
catch (Exception e) {
	e.printStackTrace();
}
finally {
	os.close();
	out.clear();
	out = pageContext.pushBody();
}
%>