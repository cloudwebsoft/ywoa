<%@ page contentType="text/html; charset=gb2312"%>
<%@ page import="java.io.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.oa.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import="jxl.*"%>
<%@ page import="jxl.write.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
String priv="admin.flow.query";
if (!privilege.isUserPrivValid(request, priv)){
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

long id = ParamUtil.getLong(request, "id", -1);
if (id==-1) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "err_id")));
	return;
}

FormQueryReportRender fqrr = new FormQueryReportRender();

Vector[] ret = fqrr.export(request, id);
if (ret==null) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, "结果集为空！"));
	return;
}

FormQueryReportDb fqrd = new FormQueryReportDb();
fqrd = (FormQueryReportDb)fqrd.getQObjectDb(new Long(id));
			
response.setContentType("application/vnd.ms-excel");
response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode(fqrd.getString("title")) + ".xls");  
            
OutputStream os = response.getOutputStream();

try {
	
	File file = new File(Global.realPath + "visual/template/blank.xls");
	Workbook wb = Workbook.getWorkbook(file);

	// 打开一个文件的副本，并且指定数据写回到原文件
	WritableWorkbook wwb = Workbook.createWorkbook(os, wb);
	WritableSheet ws = wwb.getSheet(0);

	Vector vTitle = ret[0];
	Iterator ir = vTitle.iterator();
	int i = 0;
	while (ir.hasNext()) {
		String title = (String)ir.next();
		Label a = new Label(i, 0, title);
		ws.addCell(a);
		i++;
	}

	Vector vValue = ret[1];
	ir = vValue.iterator();

	int j = 1;
	int k = 0;
	
	while (ir.hasNext()) {
		String[] ary = (String[])ir.next();
		for (i=0; i<ary.length; i++) {
			String value = ary[i];
			Label a = new Label(i, j, value);
			ws.addCell(a);
		}
			
		j++;
	}
	wwb.write();
	wwb.close();
	wb.close();
} catch (Exception e) {
	out.println(e.toString());
}
finally {
	os.close();
}

out.clear();
out = pageContext.pushBody();
%>