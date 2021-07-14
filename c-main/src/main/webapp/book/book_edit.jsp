<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.book.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "book.all";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("modify")) {
	BookMgr bm = new BookMgr();
	boolean re = false;
	try {
		re = bm.modify(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re)
		out.print(StrUtil.Alert("操作成功！"));
}

int id = ParamUtil.getInt(request, "id");
BookDb btd = new BookDb();
BookTypeDb btdb = new BookTypeDb();
btd = btd.getBookDb(id);
int typeId = btd.getTypeId();
String pubDate = DateUtil.format(btd.getPubDate(), "yyyy-MM-dd");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>图书类别编辑</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
</head>
<body>
<%@ include file="book_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<div class="spacerH"></div>
<table class="tabStyle_1 percent80">
  <form action="book_do.jsp?op=modify" name="form1" method="post">
    <tr>
      <td colspan="4" class="tabStyle_1_title">修改图书</td>
    </tr>
    <tr>
      <td width="68">书名：</td>
      <td width="171"><input name="bookName" type="text" id="bookName" value="<%=btd.getBookName()%>" size="30" maxlength="100">
	  <font color=red>*</font>
      <input type=hidden name="id" value="<%=btd.getId()%>"></td>
      <td width="68">图书编号：</td>
      <td><input type="text"  name="bookNum" id="bookNum" value="<%=btd.getBookNum()%>" maxlength="110">
	  <font color=red>*</font></td>
    </tr>
    <tr>
      <td>图书类别：</td>
      <td><%
	  String opts = "";
	  Iterator ir = btdb.list().iterator();
	  while (ir.hasNext()) {
	  	 btdb = (BookTypeDb)ir.next();
	  	 opts += "<option value='" + btdb.getId() + "'>" + btdb.getName() + "</option>";
	  }
	  %>
          <select name="typeId" id="typeId" >
            <option selected>-----请选择-----</option>
            <%=opts%>
          </select>
        <font color=red>*</font>
		<script>
		form1.typeId.value = "<%=btdb.getId()%>";
		</script>
		</td>
      <td>图书归属：</td>
      <td width="183"><select name="deptCode">
        <%
	DeptMgr dm = new DeptMgr();
	DeptDb lf = dm.getDeptDb(DeptDb.ROOTCODE);
	DeptView dv = new DeptView(lf);
	dv.ShowDeptAsOptions(out, lf, lf.getLayer()); 
 %>
      </select>
		<script>
		form1.deptCode.value = "<%=btd.getDeptCode()%>";
		</script>
	  </td>
    </tr>
    <tr>
      <td>作者： </td>
      <td><input name="author" type="text" id="author" value="<%=btd.getAuthor()%>" size="30" maxlength="100"></td>
      <td>价格：(￥)</td>
      <td><input type="text" name="price" id="price" maxlength="100" value="<%=btd.getPrice()%>">
          <font color=red>*</font></td>
    </tr>
    <tr>
      <td>出版社：</td>
      <td><input name="pubHouse" type="text" id="pubHouse" value="<%=btd.getPubHouse()%>" size="30" maxlength="100"></td>
      <td>出版日期： </td>
      <td><input name="pubDate" type="text" id="pubDate"value="<%=pubDate%>" readOnly>
    </tr>
    <tr>
      <td align="left" nowrap>内容简介：</td>
      <td colspan="3"><textarea  name="brief"  id="brief" style="width:98%" rows="8" ><%=btd.getBrief()%></textarea></td>
    </tr>
    <tr>
      <td colspan="4" align="center"><input class="btn" name="submit2" type="submit"  value="确定" >
        &nbsp;
        <input class="btn" name="reset" type="reset"  value="重置" >      </td>
    </tr>
  </form>
</table>
</body>
<script>
$(function() {
	$('#pubDate').datetimepicker({
     	lang:'ch',
     	timepicker:false,
     	format:'Y-m-d'
	});
});

</script>
</html>
