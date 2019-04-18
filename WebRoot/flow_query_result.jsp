<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作流查询结果</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="inc/common.js"></script>
<script type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/flexigrid.js"></script>
</head>
<body>
<%
String typeCode = ParamUtil.get(request, "typeCode");
Leaf lf = new Leaf();
lf = lf.getLeaf(typeCode);

String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
if (strcurpage.equals(""))
	strcurpage = "1";
if (!StrUtil.isNumeric(strcurpage)) {
	out.print(StrUtil.makeErrMsg("标识非法！"));
	return;
}

if (lf.getType()==lf.TYPE_NONE) {
	// out.print(SkinUtil.makeInfo(request, "请选择流程！"));
	response.sendRedirect("flow/flow_list.jsp");
	return;
}

int pagesize = ParamUtil.getInt(request, "pageSize", 20);
int curpage = Integer.parseInt(strcurpage);

String op = ParamUtil.get(request, "op");
FormDb fd = new FormDb();
fd = fd.getFormDb(lf.getFormCode());

if (!fd.isLoaded()) {
	out.print(SkinUtil.makeInfo(request, "流程表单尚未定义！"));
	return;
}

MacroCtlMgr mm = new MacroCtlMgr();

com.redmoon.oa.sso.Config cfg = new com.redmoon.oa.sso.Config();
		
String sql = "select id from flow where status<>" + WorkflowDb.STATUS_NONE + " order by id desc";
String query = ParamUtil.get(request, "query");
if (!query.equals("")) { // 分页传过来的sql
	sql = cn.js.fan.security.ThreeDesUtil.decrypthexstr(cfg.get("key"), query);
}
else {
	if (op.equals("queryFlow")) {
		String title = ParamUtil.get(request, "title");
		int title_cond = ParamUtil.getInt(request, "title_cond", 0);
		String fDate = ParamUtil.get(request, "fromDate");
		String tDate = ParamUtil.get(request, "toDate");
		String fEndDate = ParamUtil.get(request, "fromBeginDate");
		String tEndDate = ParamUtil.get(request, "toEndDate");
		int status = ParamUtil.getInt(request, "status", 1000);
		/*
		// 未限定用户只能查看自己参与的流程
		sql = "select id from flow where type_code=" + StrUtil.sqlstr(typeCode);
		if (!title.equals("")) {
			if (title_cond==0) {
				sql += " and title like " + StrUtil.sqlstr("%" + title + "%");
			}
			else if (title_cond==1) {
				sql += " and title=" + StrUtil.sqlstr(title);
			}
		}
		if (!fDate.equals("")) {
			sql += " and BEGIN_DATE>=" + StrUtil.sqlstr(fDate);
		}
		if (!tDate.equals("")) {
			sql += " and BEGIN_DATE<=" + StrUtil.sqlstr(tDate);
		}
		if (!fEndDate.equals("")) {
			sql += " and END_DATE>=" + StrUtil.sqlstr(fEndDate);
		}
		if (!tEndDate.equals("")) {
			sql += " and END_DATE<=" + StrUtil.sqlstr(tEndDate);
		}
		if (status!=1000) {
			sql += " and status=" + status;
		}
		*/

		// 限定用户只能查看自己参与的
		sql = "select distinct f.id from flow f, flow_my_action a where f.id=a.flow_id and f.status<>" + WorkflowDb.STATUS_NONE + " and a.user_name=" + StrUtil.sqlstr(privilege.getUser(request)) + " and f.type_code=" + StrUtil.sqlstr(typeCode);
		if (!title.equals("")) {
			if (title_cond==0) {
				sql += " and f.title like " + StrUtil.sqlstr("%" + title + "%");
			}
			else if (title_cond==1) {
				sql += " and f.title=" + StrUtil.sqlstr(title);
			}
		}
		if (!fDate.equals("")) {
			sql += " and f.BEGIN_DATE>=" + StrUtil.sqlstr(fDate);
		}
		if (!tDate.equals("")) {
			sql += " and f.BEGIN_DATE<=" + StrUtil.sqlstr(tDate);
		}
		if (!fEndDate.equals("")) {
			sql += " and f.END_DATE>=" + StrUtil.sqlstr(fEndDate);
		}
		if (!tEndDate.equals("")) {
			sql += " and f.END_DATE<=" + StrUtil.sqlstr(tEndDate);
		}
		if (status!=1000) {
			sql += " and f.status=" + status;
		}
	}
	else if (op.equals("queryForm")) {

		Iterator ir = fd.getFields().iterator();
		/*
		// 未限定用户只能查看自己参与的流程
		sql = "select flowId from " + fd.getTableNameByForm() + " where flowTypeCode=" + StrUtil.sqlstr(typeCode);
		while (ir.hasNext()) {
			FormField ff = (FormField)ir.next();
			String value = ParamUtil.get(request, ff.getName());
			String name_cond = ParamUtil.get(request, ff.getName() + "_cond");
			if (ff.getType().equals(ff.TYPE_DATE) || ff.getType().equals(ff.TYPE_DATE_TIME)) {
				String fDate = ParamUtil.get(request, ff.getName() + "FromDate");
				String tDate = ParamUtil.get(request, ff.getName() + "ToDate");
				if (!fDate.equals(""))
					sql += " and " + ff.getName() + ">=" + StrUtil.sqlstr(fDate);
				if (!tDate.equals(""))
					sql += " and " + ff.getName() + "<=" + StrUtil.sqlstr(tDate);
			}
			else {
				if (name_cond.equals("0")) {
					if (!value.equals("")) {
						sql += " and " + ff.getName() + " like " + StrUtil.sqlstr("%" + value + "%");
					}
				}
				else if (name_cond.equals("1")) {
					if (!value.equals("")) {
						sql += " and " + ff.getName() + "=" + StrUtil.sqlstr(value);
					}
				}
			}
		}
		*/
		
		// 限定用户只能查看自己参与的
		sql = "select distinct f.flowId from " + fd.getTableNameByForm() + " f, flow_my_action a where f.flowId=a.flow_id and a.user_name=" + StrUtil.sqlstr(privilege.getUser(request)) + " and f.flowTypeCode=" + StrUtil.sqlstr(typeCode);
		while (ir.hasNext()) {
			FormField ff = (FormField)ir.next();
			String value = ParamUtil.get(request, ff.getName());
			String name_cond = ParamUtil.get(request, ff.getName() + "_cond");
			if (ff.getType().equals(ff.TYPE_DATE) || ff.getType().equals(ff.TYPE_DATE_TIME)) {
				String fDate = ParamUtil.get(request, ff.getName() + "FromDate");
				String tDate = ParamUtil.get(request, ff.getName() + "ToDate");
				if (!fDate.equals(""))
					sql += " and f." + ff.getName() + ">=" + StrUtil.sqlstr(fDate);
				if (!tDate.equals(""))
					sql += " and f." + ff.getName() + "<=" + StrUtil.sqlstr(tDate);
			}
			else if(ff.getMacroType().equals("serach_helper")){
			   value = value.replaceAll("，", ",");
			   String[] ary = StrUtil.split(value, ",");
			   if(ary==null){
			      ary = StrUtil.split(value, " ");
			   }
			   if(ary!=null){
			      String str = "";
			      for(String s : ary){
				    s = s.trim();
				    if(str.equals("")) {
				    	str = " f." + ff.getName() + " like " + StrUtil.sqlstr("%" + s + "%");
					}else{
						str += " or f." + ff.getName() + " like " + StrUtil.sqlstr("%" + s + "%");
					}			
				  }
				  sql += " and (" + str + ")";
			   }	   
			}			
			else {
				if (name_cond.equals("0")) {
					if (!value.equals("")) {
						sql += " and f." + ff.getName() + " like " + StrUtil.sqlstr("%" + value + "%");
					}
				}
				else if (name_cond.equals("1")) {
					if (!value.equals("")) {
						sql += " and f." + ff.getName() + "=" + StrUtil.sqlstr(value);
					}
				}
				else {
					if (!value.equals("")) {
						sql += " and f." + ff.getName() + name_cond + value;
					}				
				}
			}
		}		
	}
	
	sql += " order by a.receive_date desc";	
}

// out.print(sql + "<BR>");

WorkflowDb wf = new WorkflowDb();
ListResult lr = wf.listResult(sql, curpage, pagesize);
int total = lr.getTotal();
Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}

Vector v = lr.getResult();
Iterator ir = null;
if (v!=null)
	ir = v.iterator();
%>
<table id="grid">
  <thead>
    <tr>
      <th width="48">ID</th>
      <th width="144"><lt:Label res="res.flow.Flow" key="tit"/></th>
<%	  
	int fieldTdCount = 0;
	Iterator irField = fd.getFields().iterator();
	while (irField.hasNext()) {
		FormField ff = (FormField)irField.next();		
		if (ff.isCanList()) {
			fieldTdCount++;
		%>
		<th width="79"><%=ff.getTitle()%></th>
		<%
		}
	}	  
%>
      <th width="132"><lt:Label res="res.flow.Flow" key="fqTime"/></th>
      <th width="119"><lt:Label res="res.flow.Flow" key="organ"/></th>
      <th width="83"><lt:Label res="res.flow.Flow" key="state"/></th>
      <th width="140"><lt:Label res="res.flow.Flow" key="manage"/></th>
    </tr>
  </thead>
  <tbody>
    <%
UserMgr um = new UserMgr();
FormDAO fdao = new FormDAO();
while (ir.hasNext()) {
 	WorkflowDb wfd = (WorkflowDb)ir.next(); 
	%>
    <tr>
      <td><%=wfd.getId()%></td>
      <td><a href="flow_modify.jsp?flowId=<%=wfd.getId()%>" title="<%=wfd.getTitle()%>"><%=StrUtil.getLeft(wfd.getTitle(), 36)%></a></td>
<%	  
	irField = fd.getFields().iterator();
	while (irField.hasNext()) {
		FormField ff = (FormField)irField.next();
		if (ff.isCanList()) {
			fdao = fdao.getFormDAO(wfd.getId(), fd);
		%>
			<td>
	    <%
			if (ff.getType().equals(FormField.TYPE_MACRO)) {
				MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
				if (mu != null) {
					out.print(StrUtil.getAbstract(request, mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(ff.getName())), 100));
				}
			}
			else {%>		
				<%=StrUtil.getAbstract(request, fdao.getFieldValue(ff.getName()), 100)%>
  <%}%>		</td>
		<%
		}
	}	  
%>
	  <td align="center"><%=DateUtil.format(wfd.getMydate(), "yy-MM-dd HH:mm:ss")%> </td>
      <td><%=um.getUserDb(wfd.getUserName()).getRealName()%></td>
      <td class="<%=WorkflowDb.getStatusClass(wfd.getStatus())%>"><%=wfd.getStatusDesc()%></td>
      <td align="center"><a href="flow_modify.jsp?flowId=<%=wfd.getId()%>"><lt:Label res="res.flow.Flow" key="showModify"/></a></td>
    </tr>
<%}
String[] sumFields = ParamUtil.getParameters(request, "sumField");
if (sumFields!=null && !sumFields[0].equals("")) {
	int p = sql.indexOf(" from ");
	String s1 = sql.substring(0, p);
	String s2 = sql.substring(p);
	
	String fields = "";
	for (int i=0; i<sumFields.length; i++) {
		if (fields.equals(""))
			fields = "sum(f." + sumFields[i] + ")";
		else
			fields += ",sum(f." + sumFields[i] + ")";
	}
	
	sql = "select " + fields + s2;
	// System.out.println(getClass() + " " + sql);
%>
    <tr>
      <td colspan="2" style="line-height:1.5">
      <%
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = jt.executeQuery(sql);
		if (ri.hasNext()) {
			ResultRecord rr = (ResultRecord)ri.next();
			
			for (int i=0; i<sumFields.length; i++) {
				FormField field = fd.getFormField(sumFields[i]);
				if (field.getFieldType()==FormField.FIELD_TYPE_INT 
					|| field.getFieldType()==FormField.FIELD_TYPE_LONG) {
					out.print(field.getTitle() + "&nbsp;合计：" + rr.getInt(i+1) + "<BR />");
				}
				else
					out.print(field.getTitle() + "&nbsp;合计：" + rr.getDouble(i+1) + "<BR />");
			}
		}
	  %>
      </td>
    </tr>
<%
}
%>
  </tbody>
</table>
<%
	String sql3des = "";
	if (query.equals(""))
		sql3des = cn.js.fan.security.ThreeDesUtil.encrypt2hex(cfg.get("key"), sql);
	else
		sql3des = query;
	String querystr = "op="+op + "&typeCode=" + StrUtil.UrlEncode(typeCode) + "&query=" + StrUtil.UrlEncode(sql3des);
%>
</body>
<script>
var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "flow_query_result.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "flow_query_result.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "flow_query_result.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

flex = $("#grid").flexigrid
(
	{
	buttons : [
		{name: '<lt:Label res="res.flow.Flow" key="query"/>', bclass: 'query', onpress : action},
		{name: '<lt:Label res="res.flow.Flow" key="export"/>', bclass: 'export', onpress : action}
		],
	/*
	searchitems : [
		{display: 'ISO', name : 'iso'},
		{display: 'Name', name : 'name', isdefault: true}
		],
	sortname: "iso",
	sortorder: "asc",
	*/
	url: false,
	usepager: true,
	checkbox : false,
	page: <%=curpage%>,
	total: <%=total%>,
	useRp: true,
	rp: <%=pagesize%>,
	
	//title: "通知",
	singleSelect: true,
	resizable: false,
	showTableToggleBtn: true,
	showToggleBtn: true,
	
	onChangeSort: changeSort,
	
	onChangePage: changePage,
	onRpChange: rpChange,
	onReload: onReload,
	/*
	onRowDblclick: rowDbClick,
	onColSwitch: colSwitch,
	onColResize: colResize,
	onToggleCol: toggleCol,
	*/
	autoHeight: true,
	width: document.documentElement.clientWidth,
	height: document.documentElement.clientHeight - 84
	}
);

function action(com, grid) {
	if (com=='<lt:Label res="res.flow.Flow" key="export"/>')	{
		window.location.href = "flow/flow_query_result_export.jsp?typeCode=<%=typeCode%>&query=<%=sql3des%>";
	}
	else if (com=='<lt:Label res="res.flow.Flow" key="query"/>') {
		window.location.href = "flow_query.jsp?dirCode=<%=typeCode%>";
	}
	
}
</script>
</html>