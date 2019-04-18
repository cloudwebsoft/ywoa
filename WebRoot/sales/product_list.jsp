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
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "sales.product";
if (!privilege.isUserPrivValid(request, priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
String formCode = "sales_product_info";

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
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
	<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
	<style>
		.search-form input,select {
			vertical-align:middle;
		}
		.search-form input:not([type="radio"]):not([type="button"]) {
			width: 80px;
			line-height: 20px; /*否则输入框的文字会偏下*/
		}
	</style>
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<%@ include file="../inc/nocache.jsp"%>
<script src="<%=Global.getRootPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_js.jsp"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
</head>
<body>
<%@ include file="product_inc_menu_top.jsp"%>
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
	
	// out.print(sql);
	
	querystr = "query=" + StrUtil.UrlEncode(sql);
%>

  <table id="searchTable" width="100%" border="0" cellpadding="0" cellspacing="0">
      <!--<tr>
        <td width="29%">供应商：</td>
        <td nowrap="nowrap" width="19%"><select name="provider_cond">
          <option value="1" selected="selected">等于</option>
        </select></td>
        <td width="52%"><input name="provider" size="20" /></td>
      </tr>
      --><tr>
        <td align="center">
        <form id="form2" name="form2" class="search-form" action="?op=search" method="post">
        &nbsp;产品名称：
        <select name="product_name_cond">
          <option value="1">等于</option>
          <option value="0" selected="selected">包含</option>
        </select>&nbsp;&nbsp;&nbsp;<input type="text" name="product_name" size="20" /><!--
      <tr>
        <td width="29%">产品类别：</td>
        <td nowrap="nowrap" width="19%"><select name="product_mode_cond">
          <option value="0" selected="selected">包含</option>
          <option value="1">等于</option>
        </select></td>
        <td width="52%"><input name="product_mode" size="20" /></td>
      </tr>
      -->&nbsp;<input class="tSearch"  type="submit" value="查  询" name="submit" />
      </form></td>
      </tr>
  </table>

<%
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
    <th width="120" style="cursor:pointer">产品名称</th>
    <th width="120" style="cursor:pointer">售价</th>
    <th width="120" style="cursor:pointer">类别</th>
    <th width="120" style="cursor:pointer">操作</th>
  </tr>
  </thead>
  <%	
	  	int i = 0;
		TreeSelectMgr tsm = new TreeSelectMgr();
		
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			TreeSelectDb tsd = tsm.getTreeSelectDb(fdao.getFieldValue("product_mode"));
			i++;
			long id = fdao.getId();
		%>
  <tr align="center">
    <td width="26%" align="left"><a href="product_show.jsp?id=<%=id%>&amp;formCode=<%=formCode%>"><%=fdao.getFieldValue("product_name")%></a></td>
    <td width="20%"><%=fdao.getFieldValue("standard_price")%></td>
    <td width="21%"><%=tsd.getName()%></td>
    <td width="17%"><a href="product_edit.jsp?id=<%=id%>&amp;formCode=<%=StrUtil.UrlEncode(formCode)%>">编辑</a>&nbsp;&nbsp;<a onclick=" jConfirm('您确定要删除吗？','提示',function(r){ if(!r){return false;}else{ window.location.href='../visual_del.jsp?id=<%=id%>&amp;formCode=<%=formCode%>&amp;privurl=<%=StrUtil.getUrl(request)%>'}}) " style="cursor:pointer">删除</a>&nbsp;&nbsp; </td>
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
				{name: '条件', bclass: '', type: 'include', id: 'searchTable'}
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
	window.location.href = "product_list.jsp?<%=querystr%>&pagesize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp){
		window.location.href = "product_list.jsp?<%=querystr%>&CPages=" + newp + "&pagesize=" + flex.getOptions().rp;
		}
}

function rpChange(pageSize) {
	window.location.href = "product_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pagesize=" + pageSize;
}

function onReload() {
	window.location.reload();
}
</script>
</body>
</html>
