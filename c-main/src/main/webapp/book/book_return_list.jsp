<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.book.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "book.all";
if (!privilege.isUserPrivValid(request, priv)) {
	%>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />	
	<%	
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>图书归还</title>
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
<script src="../inc/common.js"></script>
<body>
<%@ include file="book_inc_menu_top.jsp"%>
<script>
o("menu3").className="current";
</script>
<div class="spacerH"></div>
<%
String sql;
String myname = privilege.getUser(request);
String myUnitCode = privilege.getUserUnitCode(request);

String unitCode = ParamUtil.get(request, "unitCode");

sql = "select id from book where borrowState='1' and unit_code=" + StrUtil.sqlstr(myUnitCode);
String querystr="";
//System.out.println("book_return_list.jsp: " + sql);
//if (op.equals("mine"))
	//sql = "select id from work_plan where author=" + StrUtil.sqlstr(privilege.getUser(request));

int pagesize = 10;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
	
BookDb bd = new BookDb();

ListResult lr = bd.listResult(sql, curpage, pagesize);
long total = lr.getTotal();
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
<table width="98%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b></td>
  </tr>
</table>
<table align="center" class="tabStyle_1 percent98">
<form action="book_return.jsp" method="post" name="form1" id="form1">
    <tr>
      <td width="5%" align="center" class="tabStyle_1_title"><input id="checkbox" name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" /></td>
      <td width="35%" class="tabStyle_1_title">图书名称</td>
      <td width="30%" class="tabStyle_1_title">借阅人</td>
      <td width="15%" class="tabStyle_1_title">借阅时间</td>
      <td width="15%" class="tabStyle_1_title">预计归还时间</td>
    </tr>
  <%	
	  	int i = 0;
		BookTypeDb btdb = new BookTypeDb();
		UserMgr um = new UserMgr();
		while (ir!=null && ir.hasNext()) {
			bd = (BookDb)ir.next();
			i++;
			int id = bd.getId();
			int typeId = bd.getTypeId();
			BookTypeDb btd = btdb.getBookTypeDb(typeId);
			DeptDb dd = new DeptDb();
			dd = dd.getDeptDb(bd.getDeptCode());
			// out.print("deptCode=" + bd.getDeptCode());
			String deptName = "";
			if (dd!=null && dd.isLoaded())
				deptName = dd.getName();
			String borrowState="";
			String  checkbox;
			if (bd!=null && !bd.getBorrowState()) {
				borrowState="未借出"; 
			}
			else {
			 	borrowState="已借出";   
			}
		%>
    <tr>
      <td align="center"><input name="ids" type="checkbox" value="<%=bd.getId()%>" /></td>
      <td><a href="javascript:;" class="STYLE2" onClick="window.open('book_show.jsp?id=<%=bd.getId()%>','','height=181, width=400, top=200,left=400, toolbar=no, menubar=no, scrollbars=no, resizable=no,location=no,status=no')"><%=bd.getBookName()%></a></td>
      <td><%
	  if (!bd.getBorrowPerson().equals("")) {
		  UserDb ud = um.getUserDb(bd.getBorrowPerson());
		  %>
          <%=ud.getRealName()%>
      <%}%>    
      </td>
      <td><%=DateUtil.format(bd.getBeginDate(), "yyyy-MM-dd")%></td>
      <td><%=DateUtil.format(bd.getEndDate(), "yyyy-MM-dd")%> </td>
    </tr>
  <%}%>
</form>
</table>
<table width="98%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td align="right">
<%
	out.print(paginator.getCurPageBlock("?"+querystr));
%>
	</td>
  </tr>
</table>
<table width="98%" border="0" cellspacing="0" cellpadding="0">
  <tr> 
    <td align="center"><input name="button" type="submit" class="btn"  value="还  书 " onClick="form1.submit()"></td>
  </tr>
</table>

</body>
</html>
