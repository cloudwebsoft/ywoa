<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "sales.provider";
if (!privilege.isUserPrivValid(request, priv) && !privilege.isUserPrivValid(request, "sales"))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
String formCode = "sales_provider_name";

String querystr = "";

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

FormDb companyFd = new FormDb();
companyFd = companyFd.getFormDb("sales_provider_info");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=fd.getName()%>列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<%@ include file="../inc/nocache.jsp"%>
<script src="<%=Global.getRootPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_js.jsp"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../js/flexigrid.js"></script>
<script>
function selAllCheckBox(checkboxname){
	var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = true;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = true;
		}
	}
}

function clearAllCheckBox(checkboxname) {
	var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = false;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = false;
		}
	}
}

function sendSms() {
	var mobiles = getCheckboxValue("mobiles");
	if (mobiles=="") {
		jAlert("请选择人员！","提示");
		return;
	}
	window.location.href = "../message_oa/sms_send.jsp?mobile=" + mobiles;
}
</script>
</head>
<body>
<%@ include file="provider_contact_inc_menu_top.jsp"%>
<script>
o("menu1").className="current"; 
</script>
<%
String unitCode = privilege.getUserUnitCode(request);

String sql = "select id from " + fd.getTableNameByForm() + " where unit_code=" + StrUtil.sqlstr(unitCode);		

String query = ParamUtil.get(request, "query");
if (!query.equals(""))
	sql = query;
else
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
							if (cond.equals(""))
								cond += ff.getName() + "=" + StrUtil.sqlstr(ary[0]);
							else
								cond += " and " + ff.getName() + "=" + StrUtil.sqlstr(ary[0]);
						}
					}
					else {
						String orStr = "";
						for (int n=0; n<len; n++) {
							if (!ary[n].equals(""))
								orStr = SQLFilter.concat(orStr, "or", ff.getName() + "=" + StrUtil.sqlstr(ary[n]));
						}
						if (!orStr.equals(""))						
							cond = SQLFilter.concat(cond, "and", orStr);
					}
				}
			}
			else {
				if (name_cond.equals("0")) {
					if (!value.equals("")) {
						if (cond.equals("")){
							if("provider_company".equals(ff.getName())){
								cond += ff.getName() + " in (select id from form_table_sales_provider_info where provide_name like" + StrUtil.sqlstr("%" + value + "%") + ")";
							}else{
								cond += ff.getName() + " like " + StrUtil.sqlstr("%" + value + "%");
							}
						}
						else{
							if("provider_company".equals(ff.getName())){
								cond += ff.getName() + " in (select id from form_table_sales_provider_info where provide_name like" + StrUtil.sqlstr("%" + value + "%") + ")";
							}else{
								cond += " and " + ff.getName() + " like " + StrUtil.sqlstr("%" + value + "%");
							}
						}
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
	
	// out.print(sql);
	
	querystr = "query=" + StrUtil.UrlEncode(sql);	

		int pagesize = ParamUtil.getInt(request, "pagesize", 10);
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
			
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
    <th width="40">
      <input type="checkbox" onclick="if (this.checked) selAllCheckBox('mobiles'); else clearAllCheckBox('mobiles');" />
    </th>
    <th width="120">联系人</th>
    <th width="120">供应商单位</th>
    <th width="120">手机</th>
    <th width="120">QQ</th>
    <th width="120">电子邮件</th>
    <th width="120">操作</th>
  </tr>
  </thead>
  <%	
	  	int i = 0;
        FormDAO fdaoCompany = new FormDAO();
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long id = fdao.getId();
			String mob = fdao.getFieldValue("mobile");
			fdaoCompany = fdaoCompany.getFormDAO(StrUtil.toLong(fdao.getFieldValue("provider_company")), companyFd);
		%>
  <tr align="center">
    <td width="8%" align="center"><%if (!mob.equals("")) {%>
      <input type="checkbox" name="mobiles" value="<%=mob%>" />
    <%}%></td>
    <td width="22%" align="left"><a href="provider_contact_show.jsp?id=<%=id%>&amp;formCode=<%=formCode%>"><%=fdao.getFieldValue("linkman_name")%></a></td>
    <td width="16%"><a href="provider_contact_show.jsp?id=<%=id%>&amp;formCode=<%=formCode%>"><%=fdaoCompany.getFieldValue("provide_name")%></a></td>
    <td width="15%"><%=fdao.getFieldValue("mobile")%></td>
    <td width="13%"><%=fdao.getFieldValue("qq")%></td>
    <td width="12%"><%=fdao.getFieldValue("email")%></td>
    <td width="14%"><a href="provider_contact_edit.jsp?id=<%=id%>&amp;formCode=<%=StrUtil.UrlEncode(formCode)%>">编辑</a>&nbsp;&nbsp;<a onclick="jConfirm('您确定要删除么？','提示',function(r){if(!r){return false;}else{ window.location.href='../visual_del.jsp?id=<%=id%>&amp;formCode=<%=formCode%>&amp;privurl=<%=StrUtil.getUrl(request)%>'}})" style="cursor:pointer">删除</a>&nbsp;&nbsp;<%
if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
%>
      <a href="#" onclick="window.open('../message_oa/sms_send.jsp?mobile=<%=fdao.getFieldValue("mobile")%>')">短信</a>
      <%}%></td>
  </tr>
  <%}%>
</table>
<script>
	$(function(){
		flex = $("#grid").flexigrid
		(
			{
				buttons : [
				<%if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {%>
					{name: '群发短信', bclass: '', onpress : actions}
			  <%}%>
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
	window.location.href = "provider_contact_list.jsp?<%=querystr%>&pagesize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp){
		window.location.href = "provider_contact_list.jsp?<%=querystr%>&CPages=" + newp + "&pagesize=" + flex.getOptions().rp;
		}
}

function rpChange(pagesize) {
	window.location.href = "provider_contact_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pagesize=" + pagesize;
}

function onReload() {
	window.location.reload();
}

function actions(com, grid) {
	if (com=='全选') {
	  	selAllCheckBox('mobiles');
	} else if (com=='群发短信') {
		sendSms();
	}
}

</script>
</body>
</html>
