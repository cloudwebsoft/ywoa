<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.book.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@page import="cn.js.fan.web.Global"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>图书查询</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
<script language="javascript">
function getcheckbox(checkboxname){
	var checkboxboxs = document.all.item(checkboxname);
	var CheckboxValue = '';
	if (checkboxboxs!=null)
	{
		if (checkboxboxs.length==null) {
			if (checkboxboxs.checked) {
				return checkboxboxs.value;
			}
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			if (checkboxboxs[i].type=="checkbox" && checkboxboxs[i].checked)
			{
				if (CheckboxValue==''){
					CheckboxValue += checkboxboxs[i].value;
				}
				else{
					CheckboxValue += ","+ checkboxboxs[i].value;
				}
			}
		}
		//return checkboxboxs.value
	}
	return CheckboxValue;
}
</script>
<script src="../inc/common.js"></script>
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

function deSelAllCheckBox(checkboxname) {
  var checkboxboxs = document.getElementsByName(checkboxname);
  if (checkboxboxs!=null)
  {
	  if (checkboxboxs.length==null) {
	  checkboxboxs.checked = false;
	  }
	  for (i=0; i<checkboxboxs.length; i++)
	  {
		  checkboxboxs[i].checked = false;
	  }
  }
}
</script>
<body>
<%@ include file="book_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<div class="spacerH"></div>
<%
	String priv = "read";
	if(!privilege.isUserPrivValid(request, priv)) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	String myname = privilege.getUser(request);
	String myUnitCode = privilege.getUserUnitCode(request);
	
	String unitCode = ParamUtil.get(request, "unitCode");
	
	String sql = "select id from book b where b.unit_code=" + StrUtil.sqlstr(myUnitCode);
	String querystr = "";
	String op = ParamUtil.get(request, "op");
	String cond = "";
	String department=ParamUtil.get(request,"department");
	String bookName=ParamUtil.get(request,"bookName");
	String strTypeId=ParamUtil.get(request,"typeId");
	
	try {
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "typeId", strTypeId, getClass().getName());
	}
	catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
	
	String author=ParamUtil.get(request,"author");
	String bookNum=ParamUtil.get(request,"bookNum");
	String pubHouse=ParamUtil.get(request,"pubHouse");
	String keepSite=ParamUtil.get(request,"keepSite");
	if (op.equals("search")) {
		sql = "select id from book b where 1=1";
		if(!department.equals("")) {
			cond = " and b.department like " + StrUtil.sqlstr("%" + department + "%");
		}
		if(!bookName.equals("")) {
			cond += " and  b.bookName like " + StrUtil.sqlstr("%" + bookName + "%");
		}
		if (!strTypeId.equals("")) {
			cond += " and b.typeId=" + StrUtil.sqlstr(strTypeId);
		}
		if(!author.equals("")) {
			cond += " and b.author like " + StrUtil.sqlstr("%" + author + "%");
		}
		if(!bookNum.equals("")) {
			cond += " and b.bookNum =" + StrUtil.sqlstr(bookNum); 
		}
		if(!pubHouse.equals("")) {
			cond += " and b.pubHouse like " + StrUtil.sqlstr("%" + pubHouse + "%");
		}
		if(!keepSite.equals("")) {
			cond += " and b.keepSite like " + StrUtil.sqlstr("%" + keepSite + "%");
		}
		if (!unitCode.equals(""))
			cond += " and b.unit_code=" + StrUtil.sqlstr(unitCode);;
		sql += cond;
		querystr += "op=" +op + "&department=" + StrUtil.UrlEncode(department) + "&bookName=" + StrUtil.UrlEncode(bookName)+ "&typeId=" + strTypeId+ "&author=" + StrUtil.UrlEncode(author)+"&bookNum=" + StrUtil.UrlEncode(bookNum) +"&pubHouse=" + StrUtil.UrlEncode(pubHouse) +"&keepSite=" + StrUtil.UrlEncode(keepSite) + "&unitCode=" + StrUtil.UrlEncode(unitCode);
	}
	else
		unitCode = myUnitCode;
				
	int pagesize = 10;
	Paginator paginator = new Paginator(request);
	int curpage = paginator.getCurPage();
		
	BookDb bd = new BookDb();
	
	ListResult lr = bd.listResult(sql, curpage, pagesize);
	int total = lr.getTotal();
	Vector v = lr.getResult();
	Iterator ir = null;
	if(v!=null)
		ir = v.iterator();
	paginator.init(total, pagesize);
	// 设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if(totalpages==0) {
		curpage = 1;
		totalpages = 1;
	}
%>
<table align="center" class="percent98">
<form action="book_list.jsp" name="formSearch" method="get">
<tr>
  <td colspan="2" align="center"><%
		if (com.redmoon.oa.kernel.License.getInstance().isGroup()) {
        %>
        单位
        <select id="unitCode" name="unitCode">
		  <%if (myUnitCode.equals(DeptDb.ROOTCODE)) {%>
          <option value="">- 不限 -</option>
          <%}%>        
          <%
            Iterator irUnit = privilege.getUserAdminUnits(request).iterator();
            while (irUnit.hasNext()) {
              DeptDb dd = (DeptDb)irUnit.next();
            %>
          <option value="<%=dd.getCode()%>"><%=dd.getName()%></option>
          <%}%>
        </select>
        <script>
		o("unitCode").value = "<%=unitCode%>";
		</script>
        <%}%>
        <input name="op" type="hidden" value="search" />
        类别
<%
	  BookTypeDb btd = new BookTypeDb();
	  String opts = "";
	  Iterator irtype = btd.list().iterator();
	  while (irtype.hasNext()) {
	  	 btd = (BookTypeDb)irtype.next();
	  	 opts += "<option value='" + btd.getId() + "'>" + btd.getName() + "</option>";
	  }
	  %>
    <select name="typeId" id="typId" >   
      <option value="">全部</option>
      <%=opts%>
      </select>
      <script>
	  o("typeId").value = "<%=strTypeId%>";
	  </script>
    名称    
    <input name="bookName" type="text" id="bookName" value="<%=bookName%>" size="10" maxlength="100">
    编号    
    <input name="bookNum" type="text" id="bookNum" value="<%=bookNum%>" size="10" maxlength="100" >
    作者    <input name="author" type="text" id="author" value="<%=author%>" size="10" maxlength="100">
    出版社    <input name="pubHouse" type="text" id="pubHouse"value="<%=pubHouse%>" size="10" maxlength="100">    <input name="submit" type="submit" class="btn"  value="查询" ></td>
</tr>
</form>
</table>
<table width="98%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b></td>
  </tr>
</table>
<table align="center" class="tabStyle_1 percent98">
	<form id="form1" name=form1 action="book_borrow.jsp" method="post">
    <tr>
      <td width="5%" align="center" class="tabStyle_1_title"><input id="checkbox" name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" /></td>
      <td width="15%" class="tabStyle_1_title">图书类别</td>
      <td width="15%" class="tabStyle_1_title">图书名称</td>
      <td width="15%" class="tabStyle_1_title">图书编号</td>
      <td width="15%" class="tabStyle_1_title">作者</td>
      <td width="15%" class="tabStyle_1_title">出版社</td>
      <td width="10%" class="tabStyle_1_title">借出</td>
      <td width="10%" class="tabStyle_1_title">操作</td>
    </tr>
<%	
	int i = 0;
	BookTypeDb btdb = new BookTypeDb();
	while (ir!=null && ir.hasNext()) {
		bd = (BookDb)ir.next();
		i++;
		int id = bd.getId();
		int typeId = bd.getTypeId();
		btd = btdb.getBookTypeDb(typeId);
		DeptDb dd = new DeptDb();
		dd = dd.getDeptDb(bd.getDeptCode());
	
		String deptName = "";
		if(dd!=null && dd.isLoaded())deptName = dd.getName();
		String borrowState="";
		String checkbox;
		if(bd!=null && !bd.getBorrowState()) {
			borrowState="否";
			checkbox = ""; 
		} else {
			borrowState="是";   
			checkbox= "disabled";
		}
%>
    <tr>
      <td align="center"><input name="ids" type="checkbox" value="<%=bd.getId()%>" <%=checkbox%>></td>
      <td><%=btd.getName()%></td>
      <td><a href="javascript:;" class="STYLE2" onClick="addTab('<%=bd.getBookName() %>','<%=Global.getFullRootPath(request) %>/book/book_show.jsp?id=<%=bd.getId()%>')"><%=bd.getBookName()%></a></td>
      <td><%=bd.getBookNum()%></td>
      <td><%=bd.getAuthor()%></td>
      <td><%=bd.getPubHouse()%></td>
      <td><%=borrowState%></td>
      <td align="center"><a href="book_edit.jsp?id=<%=bd.getId()%>">编辑</a>&nbsp;&nbsp;<a href="book_do.jsp?op=del&id=<%=bd.getId()%>"><%if(!bd.getBorrowState())out.print("删除");%></a></td>
    </tr>
<%
	}
%>
	</form>
</table>
<table width="98%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td align="right">
<%
	out.print(paginator.getCurPageBlock("book_list.jsp?"+querystr));
%>
	</td>
  </tr>
  </tr>
  
  <tr>
    <td align="center">
      <%if (privilege.isUserPrivValid(request, "book.all")) {%>
      <input name="button" type="submit" class="btn"  value="借书 " onClick="form1.submit()">
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
      <input type="button" class="btn" onClick="window.location.href='book_return_list.jsp'" value="还书 ">
      <%}%>
      <span style="display:none">
      &nbsp;&nbsp;&nbsp;&nbsp;
      <input name="button2" type="button" class="btn" onClick="window.location.href='book_query.jsp'" value="查询 "></span></td>
  </tr>
</table>
</body>
</html>
