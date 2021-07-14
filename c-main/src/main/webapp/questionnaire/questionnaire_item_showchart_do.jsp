<%@ page contentType="text/html;charset=utf-8" language="java" import="java.sql.*" errorPage="" %><%@page import="net.sf.json.*"%><%@ page import="cn.js.fan.util.*"%><%@ page import="java.util.*"%><%@ page import="com.redmoon.oa.questionnaire.*"%> <%@ page import="java.lang.Exception"%> <%@ page import="com.redmoon.oa.questionnaire.QuestionnaireFormDb"%> <%@ page import="java.math.BigDecimal"%> <%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate"%> <%@ page import="cn.js.fan.db.ResultIterator"%> <%@ page import="cn.js.fan.db.ResultRecord"%> <%@ page import="com.redmoon.oa.ui.*"%><%
JSONObject json = new JSONObject();
int formId = ParamUtil.getInt(request,"form_id");
int itemId = ParamUtil.getInt(request,"item_id");
QuestionnaireFormDb qfd = new QuestionnaireFormDb();
qfd = qfd.getQuestionnaireFormDb(formId);
QuestionnaireFormItemDb qfid = new QuestionnaireFormItemDb();
qfid = qfid.getQuestionnaireFormItemDb(itemId);
String formName = "";
String itemName = "";
if (qfd != null && qfd.isLoaded()) {
	formName = StrUtil.getNullStr(qfd.getFormName ());
}
if (qfid !=null && qfid.isLoaded()) {
	itemName = qfid.getItemName();
}
JdbcTemplate jdbc = null;
Object[][] attr = null;
// oa_questionnaire_form_subitem 某题的选项，oa_questionnaire_item 用户的答案
String sql = "select name,count(b.item_value) total"
	+ " from oa_questionnaire_form_subitem a,oa_questionnaire_item b"
	+ " where a.id=b.item_id and b.item_id=" + itemId
	+ " group by b.item_value";
	try {
	jdbc = new JdbcTemplate();
	ResultIterator ri = jdbc.executeQuery(sql);
	String name = "";
	int count = 0;
	attr = new Object[ri.size()][];
	int row = 0;
	while(ri.hasNext()) {
		ResultRecord  rr = (ResultRecord)ri.next();
		count = rr.getInt("total");
		name = StrUtil.getNullStr(rr.getString("name"));
		attr[row] = new Object[2];
		attr[row][0] = name + ":" + count + "票";
		attr[row][1] = count;
		row++;
	}
} catch (Exception e) {
} finally{
	jdbc.close();
}
json.put("itemName", itemName);
json.put("attr", attr);
json.put("name", formName + ":" + itemName);
out.print(json.toString());
return;
%>