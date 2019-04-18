<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="com.redmoon.oa.contract.ContractMgr"%>
<%@page import="cn.js.fan.security.ThreeDesUtil"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "sales.contract";
if (!privilege.isUserPrivValid(request, priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
String formCode = "sales_contract";

String querystr = "";

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=fd.getName()%>列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%@ include file="../inc/nocache.jsp"%>
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script src="<%=Global.getRootPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_js.jsp"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../js/flexigrid.js"></script>
</head>
<body>
<%@ include file="contract_inc_menu_top.jsp"%>
<script>
o("menu1").className="current"; 
</script>
<%
String unitCode = privilege.getUserUnitCode(request);

String sql = "select id from " + fd.getTableNameByForm() + " where unit_code=" + StrUtil.sqlstr(unitCode);		

String query = ParamUtil.get(request, "query");
com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();

if (!query.equals("")){
	sql = ThreeDesUtil.decrypthexstr(cfg.getKey(), query);
}else{
	if (op.equals("search")) {
		Iterator ir = fd.getFields().iterator();
		String cond = "";
		while (ir.hasNext()) {
			FormField ff = (FormField)ir.next();
			String value = ParamUtil.get(request, ff.getName());
			String name_cond = ParamUtil.get(request, ff.getName() + "_cond");
			if (ff.getType().equals(ff.TYPE_DATE) || ff.getType().equals(ff.TYPE_DATE_TIME)) {
				if (name_cond.equals("0")) {
					// 时间段
					String fDate = ParamUtil.get(request, ff.getName() + "FromDate");
					String tDate = ParamUtil.get(request, ff.getName() + "ToDate");
					
					try {
						com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, ff.getName(), fDate, getClass().getName());
					}
					catch (ErrMsgException e) {
						out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
						return;
					}
					try {
						com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, ff.getName(), tDate, getClass().getName());
					}
					catch (ErrMsgException e) {
						out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
						return;
					}
					
					if (!fDate.equals("")) {
						if (cond.equals(""))
							cond += ff.getName() + ">=" + StrUtil.sqlstr(fDate);
						else
							cond += " and " + ff.getName() + ">=" + StrUtil.sqlstr(fDate);
					}
					if (!tDate.equals("")) {
						if (cond.equals(""))
							cond += ff.getName() + "<=" + StrUtil.sqlstr(tDate);
						else
							cond += " and " + ff.getName() + "<=" + StrUtil.sqlstr(tDate);
					}
				}
				else {
					// 时间点
					String d = ParamUtil.get(request, ff.getName());
					try {
						com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, ff.getName(), d, getClass().getName());
					}
					catch (ErrMsgException e) {
						out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
						return;
					}
					
					if (!d.equals("")) {
						cond = SQLFilter.concat(cond, "and", ff.getName() + "=" + StrUtil.sqlstr(d));
					}
				}
			}
			else if (ff.getType().equals(FormField.TYPE_SELECT)) {
				String[] ary = ParamUtil.getParameters(request, ff.getName());
				if (ary!=null) {
					int len = ary.length;
					if (len==1) {
						if (!ary[0].equals("")) {
							try {
								com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, ff.getName(), ary[0], getClass().getName());
							}
							catch (ErrMsgException e) {
								out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
								return;
							}
							
							if (cond.equals(""))
								cond += ff.getName() + "=" + StrUtil.sqlstr(ary[0]);
							else
								cond += " and " + ff.getName() + "=" + StrUtil.sqlstr(ary[0]);
						}
					}
					else {
						String orStr = "";
						for (int n=0; n<len; n++) {
							try {
								com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, ff.getName(), ary[n], getClass().getName());
							}
							catch (ErrMsgException e) {
								out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
								return;
							}
														
							if (!ary[n].equals("")) {
								orStr = SQLFilter.concat(orStr, "or", ff.getName() + "=" + StrUtil.sqlstr(ary[n]));
							}
						}
						if (!orStr.equals(""))						
							cond = SQLFilter.concat(cond, "and", orStr);
					}
				}
			}
			else {
				try {
					com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, ff.getName(), value, getClass().getName());
				}
				catch (ErrMsgException e) {
					out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
					return;
				}
				
				if (name_cond.equals("0")) {
					if (!value.equals("")) {
						if (cond.equals(""))
							cond += ff.getName() + " like " + StrUtil.sqlstr("%" + value + "%");
						else
							cond += " and " + ff.getName() + " like " + StrUtil.sqlstr("%" + value + "%");
					}
				}
				else if (name_cond.equals("1")) {
					if (!value.equals("")) {
						if (cond.equals(""))
							cond += ff.getName() + "=" + StrUtil.sqlstr(value);
						else
							cond += " and " + ff.getName() + "=" + StrUtil.sqlstr(value);
					}
				}
			}
		}
		if (!cond.equals(""))
			sql = sql + " and " + cond;		
	}
	
	sql += " order by id desc";
}
querystr = "query=" + ThreeDesUtil.encrypt2hex(cfg.getKey(), sql);
// out.print(sql);
		int pagesize = ParamUtil.getInt(request, "pagesize", 10);
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
			
		String code = "contract_type";
		SelectMgr sm = new SelectMgr();
		
		FormDAO fdao = new FormDAO();
		
		ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
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
%>

<table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
	<thead>
  <tr align="center">
    <th width="120">编码</th>
    <th width="120">名称</th>
    <th width="120">类型</th>
    <th width="120">客户</th>
    <th width="120">操作</th>
  </tr>
  </thead>
  <%	
		FormDb fdCustomer = new FormDb();
		fdCustomer = fdCustomer.getFormDb("sales_customer");
  		ContractMgr cmr = new ContractMgr();
	  	int i = 0;
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long id = fdao.getId();
			
			com.redmoon.oa.visual.FormDAO fdaoCustomer = new com.redmoon.oa.visual.FormDAO(StrUtil.toInt(fdao.getFieldValue("customer")), fdCustomer);			
		%>
  <tr align="center">
    <td width="18%"><a href="contract_show.jsp?id=<%=id%>&amp;formCode=<%=formCode%>"><%=fdao.getFieldValue("contact_no")%></a></td>
    <td width="28%"><%=fdao.getFieldValue("contact_name")%></td>
    <td width="21%">
	<%
		String optName = "";
		SelectDb sd = sm.getSelect(code);
		if (sd.getType() == SelectDb.TYPE_LIST) {
			SelectOptionDb sod = new SelectOptionDb();
			optName = sod.getOptionName(code, fdao.getFieldValue("contract_type"));
		} else {
			TreeSelectDb tsd = new TreeSelectDb();
			tsd = tsd.getTreeSelectDb(fdao.getFieldValue("contract_type"));
			optName = tsd.getName();
		}
		out.print(optName);		
	%>	
	</td>
    <td width="16%"><%=fdaoCustomer.getFieldValue("customer")%></td>
    <td width="17%">
    <%if(cmr.getFlowId(id)>0){ %><a href='../flow_modify.jsp?flowId=<%=cmr.getFlowId(id) %>'>流程</a><%} %>&nbsp;
    <a href="contract_edit.jsp?id=<%=id%>&amp;formCode=<%=StrUtil.UrlEncode(formCode)%>">编辑</a>&nbsp;&nbsp;<a onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return false;}else{ window.location.href='../visual_del.jsp?id=<%=id%>&amp;formCode=<%=formCode%>&amp;privurl=<%=StrUtil.getUrl(request)%>'}}) " style="cursor:pointer">删除</a>&nbsp;&nbsp; </td>
  </tr>
  <%
		}
%>
</table>
<script>
	$(function(){
		flex = $("#grid").flexigrid
		(
			{
				buttons : [
							],
			/*
			searchitems : [
				{display: 'ISO', name : 'iso'},
				{display: 'Name', name : 'name', isdefault: true}
				],
			*/
			url: false,
			usepager: true,
			checkbox : false,
			page: <%=curpage%>,
			total: <%=total%>,
			useRp: true,
			rp: <%=pagesize%>,
			
			// title: "通知",
			singleSelect: true,
			resizable: false,
			showTableToggleBtn: true,
			showToggleBtn: false,
			
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
});

function changeSort(sortname, sortorder) {
	window.location.href = "contract_list.jsp?<%=querystr%>&pagesize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp){
		window.location.href = "contract_list.jsp?<%=querystr%>&CPages=" + newp + "&pagesize=" + flex.getOptions().rp;
		}
}

function rpChange(pagesize) {
	window.location.href = "contract_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pagesize=" + pagesize;
}

function onReload() {
	window.location.reload();
}
</script>
</body>
</html>
