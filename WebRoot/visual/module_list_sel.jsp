<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.base.IFormMacroCtl"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "java.util.regex.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@ page import = "com.redmoon.oa.util.RequestUtil"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
/*
- 功能描述：表单域选择框
- 访问规则：如果传入了条件conds，则从父窗口中取值，传入sql语句中的{$fieldName}或{@fieldName}其中@表示like条件
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：
==================
- 修改者：fgf
- 修改时间：2013.09.15
- 修改原因：
- 修改点：

- 修改者：fgf
- 修改时间：2016.02.17
- 修改原因：
增加{$cwsCurUser}表示当前用户
可以在条件中增加cws_status字段
- 修改点：
*/
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");

String moduleCode = ParamUtil.get(request, "formCode");
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(moduleCode);

FormDb fd = new FormDb();
fd = fd.getFormDb(msd.getString("form_code"));
if (!fd.isLoaded()) {
	out.print(StrUtil.Alert_Back("表单不存在！"));
	return;
}

ModulePrivDb mpd = new ModulePrivDb(moduleCode);
if (!mpd.canUserSee(privilege.getUser(request))) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

// 用于传过滤条件
request.setAttribute(ModuleUtil.MODULE_SETUP, msd);

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "id";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String byFieldName = ParamUtil.get(request, "byFieldName");
String showFieldName = ParamUtil.get(request, "showFieldName");

String openerFormCode = ParamUtil.get(request, "openerFormCode");
String openerFieldName = ParamUtil.get(request, "openerFieldName");

int mode = 1; // 默认选择窗体

FormDb openerFd = new FormDb();
openerFd = openerFd.getFormDb(openerFormCode);
FormField openerField = openerFd.getFormField(openerFieldName);
JSONObject json = null;
JSONArray mapAry = new JSONArray();
String filter = "";
try {
	// System.out.println(getClass() + " openerField.getDescription()=" + openerField.getDescription());
	String desc = ModuleFieldSelectCtl.formatJSONStr(openerField.getDescription());
	json = new JSONObject(desc);
	filter = com.redmoon.oa.visual.ModuleUtil.decodeFilter(json.getString("filter"));
	/*
	String sourceFormCode = json.getString("sourceFormCode");
	String byFieldName = json.getString("idField");
	String showFieldName = json.getString("showField");
	*/
	mapAry = (JSONArray)json.get("maps");
	
	if (json.has("mode")) {
		mode = json.getInt("mode");
	}	
} catch (JSONException e) {
	// TODO Auto-generated catch block
	// "json 格式非法";
	e.printStackTrace();
}

String querystr = "";

int pagesize = 20;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

// 过滤条件
String conds = filter; // ParamUtil.get(request, "filter");
if (conds.equals("none"))
	conds = "";

String action = ParamUtil.get(request, "action");

querystr = "op=" + op + "&action=" + action + "&formCode=" + moduleCode + "&orderBy=" + orderBy + "&sort=" + sort + "&filter=" + StrUtil.UrlEncode(conds) + "&byFieldName=" + StrUtil.UrlEncode(byFieldName) + "&showFieldName=" + StrUtil.UrlEncode(showFieldName) + "&openerFormCode=" + openerFormCode + "&openerFieldName=" + openerFieldName;

String querystrForSort = "op=" + op + "&formCode=" + moduleCode + "&filter=" + StrUtil.UrlEncode(conds) + "&byFieldName=" + StrUtil.UrlEncode(byFieldName) + "&showFieldName=" + StrUtil.UrlEncode(showFieldName) + "&openerFormCode=" + openerFormCode + "&openerFieldName=" + openerFieldName;

// String sql = "select distinct form.id from " + fd.getTableNameByForm() + " form, flow f ";
String sql = "select t1.id from " + fd.getTableNameByForm() + " t1";

String urlStrFilter = "";

// 如果在宏控件中定义了条件conds，则解析条件，并从父窗口中取表单域的值，再作为参数重定向回本页面传入sql条件参数中
if (!"".equals(conds)) {
	Pattern p = Pattern.compile(
			"\\{\\$([A-Z0-9a-z-_@\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
			Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	boolean isFound = false;
	Matcher m = p.matcher(conds);
	// out.print(conds + "<BR>");
	// 如果尚未取值，则从其父窗口中取值，然后重定向回来赋值给条件中的{$fieldName}
	if (!action.equals("afterGetClientValue")) {
		StringBuffer urlStrBuf = new StringBuffer();
		if (op.equals("search")) {
				Iterator ir = fd.getFields().iterator();
				while (ir.hasNext()) {
					FormField ff = (FormField)ir.next();
					String value = ParamUtil.get(request, ff.getName());
					String name_cond = ParamUtil.get(request, ff.getName() + "_cond");
					if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {
						urlStrBuf = StrUtil.concat(urlStrBuf, "&", ff.getName() + "_cond=" + name_cond);
						if (name_cond.equals("0")) {
							// 时间段
							String fDate = ParamUtil.get(request, ff.getName() + "FromDate");
							String tDate = ParamUtil.get(request, ff.getName() + "ToDate");
							if (!fDate.equals("")) {
								urlStrBuf = StrUtil.concat(urlStrBuf, "&", ff.getName() + "FromDate=" + fDate);
							}
							if (!tDate.equals("")) {
								urlStrBuf = StrUtil.concat(urlStrBuf, "&", ff.getName() + "ToDate=" + fDate);
							}
						}
						else {
							// 时间点
							String d = ParamUtil.get(request, ff.getName());
							if (!d.equals("")) {
								urlStrBuf = StrUtil.concat(urlStrBuf, "&", ff.getName() + "=" + StrUtil.UrlEncode(d));
							}
						}
					}			
					else if (ff.getType().equals(FormField.TYPE_SELECT)) {
						String[] ary = ParamUtil.getParameters(request, ff.getName());
						if (ary!=null) {
							int len = ary.length;
							for (int n=0; n<len; n++) {
								if (!ary[n].equals("")) {
									urlStrBuf = StrUtil.concat(urlStrBuf, "&", ff.getName() + "=" + StrUtil.UrlEncode(ary[n]));
								}
							}
						}
					}			
					else {
						if (!value.equals("")) {
							urlStrBuf = StrUtil.concat(urlStrBuf, "&", ff.getName() + "=" + StrUtil.UrlEncode(value));
							urlStrBuf = StrUtil.concat(urlStrBuf, "&", ff.getName() + "_cond=" + StrUtil.UrlEncode(name_cond));
						}
					}
				}
				
				int cws_flag = ParamUtil.getInt(request, "cws_flag", -1);			
				String cws_flag_cond = ParamUtil.get(request, "cws_flag_cond");
				StrUtil.concat(urlStrBuf, "&", "cws_flag_cond=" + cws_flag_cond);
				StrUtil.concat(urlStrBuf, "&", "cws_flag=" + cws_flag);
				int cws_status = ParamUtil.getInt(request, "cws_status", -20000);						
				String cws_status_cond = ParamUtil.get(request, "cws_status_cond");			
				StrUtil.concat(urlStrBuf, "&", "cws_status=" + cws_status);
				StrUtil.concat(urlStrBuf, "&", "cws_status_cond=" + cws_status_cond);				
			}	
	%>
		<script>
        var condStr = "";
        </script>
        <%
        while (m.find()) {
            String fieldName = m.group(1);
			
            if (fieldName.equals("cwsCurUser") || fieldName.equals("curUser") 
            	|| fieldName.equals("curUserDept") || fieldName.equals("curUserRole") || fieldName.equals("admin.dept") || fieldName.equals("parentId")) {
				isFound = true;
            	continue;
            }
			// 当条件为包含时，fieldName以@开头
			if (fieldName.startsWith("@"))
				fieldName = fieldName.substring(1);			
			
            %>
            <script>
			if (window.opener.o("<%=fieldName%>")==null) {
				alert("条件<%=fieldName%>不存在！");
			}
			else {
				if (condStr=="")
					condStr = "<%=fieldName%>=" + encodeURI(window.opener.o("<%=fieldName%>").value);
				else
					condStr += "&<%=fieldName%>=" + encodeURI(window.opener.o("<%=fieldName%>").value);
			}
            </script>
            <%
            isFound = true;
        }
        
        if (isFound) {
            %>
            <script>
            window.location.href = "module_list_sel.jsp?action=afterGetClientValue&<%=querystr%>&<%=urlStrBuf%>&CPages=<%=curpage%>&" + condStr;
            </script>
            <%
            return;
        }
        else {
			String[] ary = ModuleUtil.parseFilter(request, msd.getString("form_code"), conds);
			if (ary[0]!=null) {
				conds = ary[0];
			}
        }
	}
	else {
		String[] ary = ModuleUtil.parseFilter(request, msd.getString("form_code"), conds);
		if (ary[0]!=null) {
			// conds = ary[0];
			urlStrFilter = ary[1];
		}
	}
}

// 表单域选择控件中可能有传过来的filter，所以还不能用SQLBuiler中的getModuleListSqlAndUrlStr方法
String urlStr = "";
String query = ParamUtil.get(request, "query");
if (!query.equals("")) {
	sql = query;
}
else {
	op = "search";
	String[] ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
	sql = ary[0];
	urlStr = ary[1];	    
}

if ("".equals(urlStr)) {
	urlStr = urlStrFilter;
}
else {
	urlStr += "&" + urlStrFilter;
}

if (!urlStr.equals("")) {
	querystr += "&" + urlStr;
	querystrForSort += "&" + urlStr;
}

// out.println("sql = " + sql);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><%=fd.getName()%>列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<style>
.condSpan {
	display:inline-block;
	float:left;
	text-align: left;
	width:330px;
	height:32px;
}
.condBtnSearch {
	display:inline-block;
	float:left;
}
</style>
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script>
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "module_list_sel.jsp?op=<%=op%>&formCode=<%=msd.getString("form_code")%>&orderBy=" + orderBy + "&sort=" + sort + "&<%=querystrForSort%>";
}

function sel(id,sth) {
	if (<%=mode%>==1) {
		window.opener.setIntpuObjValue(id,sth);
	}
	else {
		// var obj = window.opener.o("<%=openerFieldName%>");
		// alert("sth" + sth);
	    window.opener.$("#<%=openerFieldName%>").empty().append("<option id='" + id + "' value='" + id +"'>"+sth+"</option>").trigger('change');
	}
	window.close();
}
</script>
</head>
<body>
<%@ include file="module_sel_inc_menu_top.jsp"%>
<script>
o("menu1").className="current"; 
</script>
<div class="spacerH"></div>
<table id="searchTable" width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
  <tr>
    <td height="23" align="center">
    <form id="searchForm" action="module_list_sel.jsp" method="get">
    &nbsp;
    <%
	boolean isShowUnitCode = false;
	/*
    Vector vt = privilege.getUserAdminUnits(request);
	if (vt.size()>0) {
		isShowUnitCode = true;
		if (vt.size()==1) {
			DeptDb dd = (DeptDb)vt.elementAt(0);
			// 只有一个单位，且是本单位节点
			if (dd.getCode().equals(privilege.getUserUnitCode(request))) {
				isShowUnitCode = false;
			}
		}
	}
	*/
	
	String myUnitCode = privilege.getUserUnitCode(request);
	DeptDb dd = new DeptDb();
	dd = dd.getDeptDb(myUnitCode);
	
	Vector vtUnit = new Vector();
	vtUnit.addElement(dd);
		
	// 向下找两级单位
    DeptChildrenCache dl = new DeptChildrenCache(dd.getCode());
    java.util.Vector vt = dl.getDirList();	
	Iterator irDept = vt.iterator();
	while (irDept.hasNext()) {
		dd = (DeptDb)irDept.next();
		if (dd.getType()==DeptDb.TYPE_UNIT) {
			vtUnit.addElement(dd);
			DeptChildrenCache dl2 = new DeptChildrenCache(dd.getCode());
			Iterator ir2 = dl2.getDirList().iterator();
			while (ir2.hasNext()) {
				dd = (DeptDb)ir2.next();
				if (dd.getType()==DeptDb.TYPE_UNIT) {
					vtUnit.addElement(dd);
				}
				
				DeptChildrenCache dl3 = new DeptChildrenCache(dd.getCode());
				Iterator ir3 = dl3.getDirList().iterator();
				while (ir3.hasNext()) {
					dd = (DeptDb)ir3.next();
					if (dd.getType()==DeptDb.TYPE_UNIT) {
						vtUnit.addElement(dd);
					}
				}				
			}
			
		}
	}

String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
String[] btnNames = StrUtil.split(btnName, ",");
String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
String[] btnScripts = StrUtil.split(btnScript, "#");
String btnBclass = StrUtil.getNullStr(msd.getString("btn_bclass"));
String[] btnBclasses = StrUtil.split(btnBclass, ",");

ArrayList<String> list = new ArrayList<String>();
MacroCtlMgr mm = new MacroCtlMgr();
FormMgr fm = new FormMgr();
int len = 0;
boolean isQuery = false;
if (btnNames!=null) {
	len = btnNames.length;
	for (int i=0; i<len; i++) {
		if (btnScripts[i].startsWith("{")) {
			// System.out.println(getClass() + " " + btnScripts[i]);
			Map<String, String> checkboxGroupMap = new HashMap<String, String>();			
			JSONObject jsonBtn = new JSONObject(btnScripts[i]);
			if (((String)jsonBtn.get("btnType")).equals("queryFields")) {
				isQuery = true;
				String title = "";
				String condFields = (String)jsonBtn.get("fields");
				String[] fieldAry = StrUtil.split(condFields, ",");
				Iterator irKey = jsonBtn.keys();
				for (int j=0; j<fieldAry.length; j++) {
					String fieldName = fieldAry[j];
					FormField ff = fd.getFormField(fieldName);

					String condType = (String)jsonBtn.get(fieldName);
					String queryValue = ParamUtil.get(request, fieldName);

					if ("cws_status".equals(fieldName)) {
						title = "状态";
					}
					else if ("cws_flag".equals(fieldName)) {
						title = "冲抵状态";
					}
					else {
						if (fieldName.startsWith("main:")) { // 关联的主表
							 String[] aryField = StrUtil.split(fieldName, ":");			
							 String field = fieldName.substring(5);
							 if (aryField.length==3) {
							  	FormDb mainFormDb = fm.getFormDb(aryField[1]);
							  	ff = mainFormDb.getFormField(aryField[2]);
								if (ff==null) {
									out.print(fieldName + "不存在");
									continue;
								}							  	
							  	title = ff.getTitle();
							 }
							 else {
							  	title = field + " 不存在";
							 }
						}
						else if (fieldName.startsWith("other")) { // 映射的字段，多重映射不支持
							 String[] aryField = StrUtil.split(fieldName, ":");
							 if (aryField.length<5) {
							 	title = "<font color='red'>格式非法</font>";
							 }
							 else {
								FormDb otherFormDb = fm.getFormDb(aryField[2]);
								ff = otherFormDb.getFormField(aryField[4]);
								if (ff==null) {
									out.print(fieldName + "不存在");
									continue;
								}								
								title = ff.getTitle();
							 }
						}
						else {
							// ff = fd.getFormField(fieldName);
							if (ff==null) {
								out.print(fieldName + "不存在");
								continue;
							}
							title = ff.getTitle();
							// 用于给convertToHTMLCtlForQuery辅助传值
							// ff.setCondType(condType);			
							if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {			
								String desc = StrUtil.getNullStr(ff.getDescription());	
								if (!"".equals(desc)) {
									title = desc;
								}
								else {		
									title = ff.getTitle();
								}
								String chkGroup = StrUtil.getNullStr(ff.getDescription());
								if (!"".equals(chkGroup)) {
									if (!checkboxGroupMap.containsKey(chkGroup)) {
										checkboxGroupMap.put(chkGroup, "");
									}
									else {
										continue;
									}
								}								
							}
							else {
								title = ff.getTitle();
							}												
						}
					}							
					%>
                    <span class="condSpan">
        			<%=title%>
					<%
					if ("cws_status".equals(fieldName)) {
                        String nameCond = ParamUtil.get(request, fieldName + "_cond");
						if ("".equals(nameCond)) {
							nameCond = condType;
						}
						int queryValueCwsStatus = ParamUtil.getInt(request, "cws_status", -20000);						
						%>
				          <select name="<%=fieldName%>_cond" style="display:none">
				            <option value="=" selected="selected">等于</option>
				          </select>						
                          <select name='<%=fieldName%>'>
                          <option value='<%=SQLBuilder.CWS_STATUS_NOT_LIMITED%>'>不限</option>
                          <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DRAFT%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DRAFT)%></option>
                          <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_NOT%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_NOT)%></option>
                          <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DONE%>' selected><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DONE)%></option>
                          <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_REFUSED%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_REFUSED)%></option>
                          <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DISCARD%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DISCARD)%></option>
                          </select>                          
						<script>
						$(function() {
							o("<%=fieldName%>_cond").value = "<%=nameCond%>";
							<%if (queryValueCwsStatus!=-20000) {%>
							o("<%=fieldName%>").value = "<%=queryValueCwsStatus%>";
							<%}	else {%>
							o("<%=fieldName%>").value = "<%=msd.getInt("cws_status")%>";
							<%}%>
						});
						</script>							
						<%
					}
					else if ("cws_flag".equals(fieldName)) {
                        String nameCond = ParamUtil.get(request, fieldName + "_cond");
						if ("".equals(nameCond)) {
							nameCond = condType;
						}
						int queryValueCwsFlag = ParamUtil.getInt(request, "cws_flag", -1);						
						%>
				          <select name="<%=fieldName%>_cond" style="display:none">
				            <option value="=" selected="selected">等于</option>
				          </select>						
                          <select name='<%=fieldName%>'>
                          <option value='-1'>不限</option>
                          <option value='0'>否</option>
                          <option value='1'>是</option>
                          </select>
						<script>
						$(function() {
							o("<%=fieldName%>_cond").value = "<%=nameCond%>";
							o("<%=fieldName%>").value = "<%=queryValueCwsFlag%>";
						});
						</script>							
						<%					
					}        			
               		else if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {
               			%>
						<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />
               			<%
						if (condType.equals("0")) {
							String fDate = ParamUtil.get(request, ff.getName() + "FromDate");
							String tDate  = ParamUtil.get(request, ff.getName() + "ToDate");
							list.add(ff.getName() + "FromDate");
							list.add(ff.getName() + "ToDate");
							%>
                             	 大于
                              <input id="<%=ff.getName()%>FromDate" name="<%=ff.getName()%>FromDate" size="15" style="width:80px" value = "<%=fDate%>" />
                              	小于
                              <input id="<%=ff.getName()%>ToDate" name="<%=ff.getName()%>ToDate" size="15" style="width:80px" value = "<%=tDate%>" />
	  <%
						}
						else {
							list.add(ff.getName());
							%>
                              <input id="<%=ff.getName()%>" name="<%=ff.getName()%>" size="15" value = "<%=queryValue%>" />
                              <!-- <img style="CURSOR: hand" onClick="SelectDate('<%=ff.getName()%>', 'yyyy-MM-dd')" src="<%=request.getContextPath()%>/images/form/calendar.gif" align="absmiddle" border="0" width="26" height="26" /> -->
							  <!-- <script>
							  $(document).ready(function() {							  
                              o("<%=fieldName%>").value = "<%=queryValue%>";
							  });
                              </script> -->						
							<%
						}
					} else if(ff.getType().equals(FormField.TYPE_MACRO)) {
						MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
						if (mu!=null) {
							String queryValueRealShow = ParamUtil.get(request, fieldName + "_realshow");
							out.print(mu.getIFormMacroCtl().convertToHTMLCtlForQuery(request, ff));
							%>
							<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />
							<script>
							$(document).ready(function() {
							o("<%=fieldName%>").value = "<%=queryValue%>";
							try {
                          	  o("<%=fieldName%>_realshow").value = "<%=queryValueRealShow%>";
                            } catch (e) {}
							});
							</script>						
						<%
						}
					}
					else if (ff.getFieldType()==FormField.FIELD_TYPE_INT || ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE || ff.getFieldType()==FormField.FIELD_TYPE_FLOAT || ff.getFieldType()==FormField.FIELD_TYPE_LONG || ff.getFieldType()==FormField.FIELD_TYPE_PRICE) {
						String nameCond = ParamUtil.get(request, fieldName + "_cond");
						if ("".equals(nameCond)) {
							nameCond = condType;
						}
						%>
				          <select name="<%=fieldName%>_cond">
				            <option value="=" selected="selected">等于</option>
				            <option value=">">></option>
				            <option value="&lt;"><</option>
				            <option value=">=">>=</option></option>
				            <option value="&lt;="><=</option>
				          </select>						
	                      <input name="<%=fieldName%>" size="5" />
						<script>
						$(document).ready(function() {
							o("<%=fieldName%>_cond").value = "<%=nameCond%>";
							o("<%=fieldName%>").value = "<%=queryValue%>";
						});
						</script>						
						<%
					}
					else {
						boolean isSpecial = false;
						if (condType.equals(SQLBuilder.COND_TYPE_NORMAL)) {
							if (ff.getType().equals(FormField.TYPE_SELECT)) {
								isSpecial = true;
								%>
								<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />
								<select id="<%=fieldName %>" name="<%=fieldName %>">
								<%=FormParser.getOptionsOfSelect(fd, ff) %>
								</select>
								<script>
								$(document).ready(function() {
									o("<%=fieldName%>").value = "<%=queryValue%>";
								});
								</script>							
								<%
							}
							else if (ff.getType().equals(FormField.TYPE_RADIO)) {
								isSpecial = true;
								%>
								<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />								
								<%
								String[][] aryRadio = FormParser.getOptionsArrayOfRadio(fd, ff);
								for (int k=0; k<aryRadio.length; k++) {
									String val = aryRadio[k][0];
									String text = aryRadio[k][1];
								%>
									<input type="radio" id="<%=fieldName %>" name="<%=fieldName %>" value="<%=val %>"/><%=text %>
								<%									
								}
							}
							else if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {
								isSpecial = true;
								%>
								<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />								
								<%
								String[][] aryChk = FormParser.getOptionsArrayOfCheckbox(fd, ff);
								for (int k=0; k<aryChk.length; k++) {
									String val = aryChk[k][0];
									String fName = aryChk[k][1];
									String text = aryChk[k][2];
									queryValue = ParamUtil.get(request, fName);
								%>
									<input type="checkbox" id="<%=fName %>" name="<%=fName %>" value="<%=val %>" style="<%=aryChk.length>1?"width:20px":""%>"/>
									<script>
									$(function() {
										o('<%=fName%>').checked = <%=queryValue.equals(val)?"true":"false"%>;
									})
									</script>
									<%if (aryChk.length>1) { %>
									<%=text %>
									<%} %>
								<%									
								}
							}							
						}
						if (!isSpecial) {
						%>
							<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />
		                    <input name="<%=fieldName%>" size="5" />
							<script>
							$(document).ready(function() {
							o("<%=fieldName%>").value = "<%=queryValue%>";
							});
							</script>
						<%
						}
					}
					%>
					</span>
					<%
				}
			}
		}
	}
	
	if (isQuery) {
	%>			
        <input type="hidden" name="op" value="search" />
        <input type="hidden" name="formCode" value="<%=moduleCode%>" />
        
        <input type="hidden" name="orderBy" value="<%=orderBy %>" />
        <input type="hidden" name="sort" value="<%=sort %>" />
        
        <input type="hidden" name="filter" value="<%=conds %>" />
        <input type="hidden" name="byFieldName" value="<%=byFieldName %>" />
        <input type="hidden" name="showFieldName" value="<%=showFieldName %>" />
        <input type="hidden" name="openerFormCode" value="<%=openerFormCode %>" />
        <input type="hidden" name="openerFieldName" value="<%=openerFieldName %>" />
        
        <input class="tSearch condBtnSearch" type="submit" value="搜索" />
	<%
	}
}
%>
        </form>
    </td>
  </tr>
</table>
<%
com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();

ListResult lr = fdao.listResult(msd.getString("form_code"), sql, curpage, pagesize);
int total = lr.getTotal();
Vector v = lr.getResult();
Iterator ir = null;
if (v!=null)
	ir = v.iterator();
paginator.init(total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}

String listField = StrUtil.getNullStr(msd.getString("list_field"));
String[] fields = StrUtil.split(listField, ",");
String listFieldWidth = StrUtil.getNullStr(msd.getString("list_field_width"));
String[] fieldsWidth = StrUtil.split(listFieldWidth, ",");
String listFieldOrder = StrUtil.getNullStr(msd.getString("list_field_order"));
String[] fieldsOrder = StrUtil.split(listFieldOrder, ",");

%>
<table class="percent98 p9" width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td align="left"><input type="button" class="btn" value="清空" onclick="window.opener.setIntpuObjValue('', ' ');window.close()" style="margin-bottom:5px" /> </td><td align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b></td>
  </tr>
</table>
<table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="1">
  <tr align="center">
<%
len = 0;
if (fields!=null)
	len = fields.length;
for (int i=0; i<len; i++) {
	String fieldName = fields[i];
	if ("colOperate".equals(fieldName)) {
		continue;
	}

	String title;
	if (fieldName.startsWith("main:")) {
		String[] subFields = StrUtil.split(fieldName, ":");
		if (subFields.length == 3) {
			FormDb subfd = new FormDb(subFields[1]);
			title = subfd.getFieldTitle(subFields[2]);
		}
	} else if (fieldName.startsWith("other:")) {
		String[] otherFields = StrUtil.split(fieldName, ":");
		if (otherFields.length == 5) {
			FormDb otherFormDb = new FormDb(otherFields[2]);
			title = otherFormDb.getFieldTitle(otherFields[4]);
		}
	}
	if (fieldName.equals("cws_creator")) {
		title = "创建者";
	}
	else if (fieldName.equals("cws_progress")) {
		title = "进度";
	}
	else if (fieldName.equals("ID")) {
		title = "ID";
	}	
	else if (fieldName.equals("cws_status")) {
		title = "状态";
	}	
	else if (fieldName.equals("cws_flag")) {
		title = "冲抵状态";
	}	
	else {
		title = fd.getFieldTitle(fieldName);
	}	
	
	String doSort = "doSort('" + fieldName + "')";
	if (fieldName.startsWith("main:") || fieldName.startsWith("other:")) {
		doSort = "";
	}
%>
    <td class="tabStyle_1_title" width="<%=fieldsWidth[i]%>" style="cursor:hand" onClick="<%=doSort%>">
	<%=title%>
	<%if (orderBy.equals(fieldName)) {
		if (sort.equals("asc")) 
			out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
		else
			out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
	}%>	
	</td>
<%}%>
    <td class="tabStyle_1_title" title="按时间排序" style="cursor:hand" onClick="doSort('id')">操作
      	<%
      	if (orderBy.equals("id")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}
		%>
    </td>
  </tr>
  <%	
	  	int k = 0;
		UserMgr um = new UserMgr();
		while (ir!=null && ir.hasNext()) {
			fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
			RequestUtil.setFormDAO(request, fdao);
			k++;
			long id = fdao.getId();
		%>
  <tr align="center" class="highlight">
<%
	String showValue = "";
	boolean isShowFieldFound = false;
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
		if ("colOperate".equals(fieldName)) {
			continue;
		}
	%>	
		<td align="left">
		<a href="module_show.jsp?parentId=<%=id%>&id=<%=id%>&code=<%=moduleCode%>&formCode=<%=msd.getString("form_code")%>&isShowNav=0" target="_blank">		
		<%
		if (fieldName.startsWith("main:")) {
			String[] subFields = fieldName.split(":");
			if (subFields.length == 3) {
				FormDb subfd = new FormDb(subFields[1]);
				com.redmoon.oa.visual.FormDAO subfdao = new com.redmoon.oa.visual.FormDAO(subfd);
				FormField subff = subfd.getFormField(subFields[2]);
				String subsql = "select id from " + subfdao.getTableName() + " where cws_id=" + id + " order by cws_order";
				StringBuilder sb = new StringBuilder();
				JdbcTemplate jt = new JdbcTemplate();
				try {
					ResultIterator ri = jt.executeQuery(subsql);
					while (ri.hasNext()) {
						ResultRecord rr = (ResultRecord) ri.next();
						int subid = rr.getInt(1);
						subfdao = new com.redmoon.oa.visual.FormDAO(subid, subfd);
						String subFieldValue = subfdao.getFieldValue(subFields[2]);
						if (subff != null && subff.getType().equals(FormField.TYPE_MACRO)) {
							MacroCtlUnit mu = mm.getMacroCtlUnit(subff.getMacroType());
							if (mu != null) {
								subFieldValue = mu.getIFormMacroCtl().converToHtml(request, subff, subFieldValue);
							}
						}
						sb.append("<span>").append(subFieldValue).append("</span>").append(ri.hasNext() ? "</br>" : "");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				out.print(sb.toString());
			}
		} else if (fieldName.startsWith("other:")) {
			out.print(StrUtil.getNullStr(com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName)));
		}
		else if (fieldName.equals("cws_creator")) {
			String realName = "";
			if (fdao.getCreator()!=null) {
			UserDb user = um.getUserDb(fdao.getCreator());
			if (user!=null)
				realName = user.getRealName();
			}
			out.print(realName);
		}		
		else if (fieldName.equals("cws_progress")) {
			out.print(fdao.getCwsProgress());
		} 
		else if (fieldName.equals("ID")) {
			out.print(fdao.getId());
		}		
		else if (fieldName.equals("cws_status")) {
			out.print(com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus()));
		}				
		else if (fieldName.equals("cws_flag")) {
			out.print(fdao.getCwsFlag());
		}
		else {
			FormField ff = fdao.getFormField(fieldName);			
			if (ff!=null) {
				String tempValue = "";
				if (ff.getType().equals(FormField.TYPE_MACRO)) {
					MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
					if (mu != null) {
						tempValue = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
					}
				} else {
					tempValue = FuncUtil.renderFieldValue(fdao, ff);
				}
				out.print(tempValue);
				if(!isShowFieldFound && ff.getName().equals(showFieldName)) {
					isShowFieldFound = true;
					showValue = tempValue;
				}
			}else {
				%>
				<%=fieldName%>不存在
				<%
			}
		}
		%>
		</a>
        </td>
	<%}%>
	<td width="50px">
	<%
	String byValue = "";
	if (byFieldName.equals("id")) {
		byValue = "" + id;
	}
	else {
		byValue = fdao.getFieldValue(byFieldName);
	}
	// @task:id在设置宏控件时，还不能被配置为被显示
	if (showFieldName.equals("id")) {//yonghu bdyhxz
		showValue = "" + id;
		isShowFieldFound = true;
	}
	
	if (!isShowFieldFound) {
		FormField ff = fd.getFormField(showFieldName);
		if (ff.getType().equals(FormField.TYPE_MACRO)) {
			MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
			if (mu != null) {
				showValue = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(showFieldName));
			}
		} else {
			showValue = fdao.getFieldValue(showFieldName);
		}		
	}
	
	ParamChecker pck = new ParamChecker(request);
	String funs = "";
	for (int i=0; i<mapAry.length(); i++) {
		json = (JSONObject)mapAry.get(i);
		String destF = (String)json.get("destField");	// 父页面
		String sourceF = (String)json.get("sourceField");	// module_list_sel.jsp页面
		Vector vector = openerFd.getFields();
		Iterator it = vector.iterator();
		FormField tempFf = null;
		while (it.hasNext()) {
			tempFf = (FormField) it.next();
			if (tempFf.getName().equals(destF)) {
				break;
			}
		}
		boolean isMacro = false;
		// setValue为module_list_sel.jsp页面中所选择的值
		String setValue = fdao.getFieldValue(sourceF);
		String checkJs = com.redmoon.oa.visual.FormUtil.getCheckFieldJS(pck, tempFf);		
		// 如果这个值将被赋值至父页面中的一个宏控件中的时候，则需要将父页面中的宏控件用convertToHTMLCtl重新替换赋值，需要注意的是宏控件传入参数中FormField需要用setValue赋值
		if (tempFf != null && tempFf.getType().equals(FormField.TYPE_MACRO)) {
			tempFf.setValue(setValue);
			isMacro = true;
			// setValue = mm.getMacroCtlUnit(tempFf.getMacroType()).getIFormMacroCtl().convertToHTMLCtl(request, tempFf).replaceAll("\\'", "\\\\'").replaceAll("\"", "&quot;");
			setValue = mm.getMacroCtlUnit(tempFf.getMacroType()).getIFormMacroCtl().convertToHTMLCtl(request, tempFf);
		}
		// 增加辅助表单域，以免算式中出现引号问题
		%>
		<textarea id="helper<%=k%>_<%=i%>" style="display:none"><%=setValue %></textarea>
		<textarea id="helperSource<%=k%>_<%=i%>" style="display:none"><%=fdao.getFieldValue(sourceF) %></textarea>	
		<textarea id="helperJs<%=k%>_<%=i%>" style="display:none"><%=checkJs%></textarea>	
		<%
		// System.out.println(getClass() + " " + destF + "-" + setValue);
		funs += "setOpenerFieldValue('" + destF + "', o('helper" + k + "_" + i + "').value," + isMacro + ", o('helperSource" + k + "_" + i + "').value, o('helperJs" + k + "_" + i + "').value);";
	}
	// 替换掉单引号，防止JS错误
	String temp = showValue.replaceAll("'","\\\\'");
	%>
	<a href="javascript:sel('<%=byValue%>', '<%=temp%>');<%=funs%>">选择</a></td>
  </tr>
  <%
  }
%>
</table>
<table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="percent98">
  <tr>
    <td width="50%" height="23" align="left">&nbsp;</td>
    <td width="50%" align="right"><%
		out.print(paginator.getCurPageBlock("module_list_sel.jsp?"+querystr));
	%></td>
  </tr>
</table>
</body>
<script>
function initCalendar() {
	<%for (String ffname : list) {%>
	$('#<%=ffname%>').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d'
    });
    <%}%>
}

$(function() {
	initCalendar();
});

function setOpenerFieldValue(openerField, val, isMacro, sourceValue, checkJs) {
    if(isMacro){
    	window.opener.replaceValue(openerField, val, sourceValue, checkJs);  	
    }else{
		window.opener.o(openerField).value = val;
	}
}
</script>
</html>
