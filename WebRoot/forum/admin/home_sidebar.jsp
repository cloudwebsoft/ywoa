<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.Conn"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="org.jdom.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<LINK href="../../cms/default.css" type=text/css rel=stylesheet>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>首页管理</title>
<style>
.btn {
border:1pt solid #636563;font-size:9pt; LINE-HEIGHT: normal;HEIGHT: 18px;
}
.style1 {font-size: 14px;
	font-weight: bold;
}
</style>
<script language="JavaScript">
<!--
function openWin(url,width,height){
	var newwin = window.open(url,"_blank","scrollbars=yes,resizable=yes,toolbar=no,location=no,directories=no,status=no,menubar=no,top=50,left=120,width="+width+",height="+height);
}

function formDefine_onsubmit() {
	var oEditor = FCKeditorAPI.GetInstance('FCKeditor') ;
	var htmlcode = oEditor.GetXHTML(true);
	formDefine.content.value = htmlcode;
}
//-->
</script>
<script src="../../inc/common.js"></script>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

Home home = Home.getInstance();

String op = ParamUtil.get(request, "op");

%>
<DIV id="tabBar">
  <div class="tabs">
    <ul>
      <li id="menu1"><a href="home.jsp">首页元素</a></li>
      <li id="menu2"><a href="home_sidebar.jsp">边栏配置</a></li>
    </ul>
  </div>
</DIV>
<script>
$("menu2").className="active"; 
</script>
<%
String opts = "";
Element root = home.getRoot();
Element blockItems = root.getChild("sideBar").getChild("blockItems");
Iterator ir = blockItems.getChildren().iterator();
while (ir.hasNext()) {
	Element e = (Element)ir.next();
	opts += "<option value='" + e.getText() + "'>" + home.getDesc(request, e.getText()) + "</option>";
}
opts = "<option value='none'>无</option>" + opts;

if (op.equals("addBlock")) {
	int count = ParamUtil.getInt(request, "count", 10);
	String block1 = ParamUtil.get(request, "block1");
	String block2 = ParamUtil.get(request, "block2");
	String block3 = ParamUtil.get(request, "block3");
	String isDisplay = ParamUtil.get(request, "isDisplay");
	if (isDisplay.equals(""))
		isDisplay = "false";
	String isNumber = ParamUtil.get(request, "isNumber");
	if (isNumber.equals(""))
		isNumber = "false";
	
    Element block = new Element("block");
    block.setAttribute(new Attribute("count", ""+count));
    block.setAttribute(new Attribute("isDisplay", isDisplay));
    block.setText(block1 + "," + block2 + "," + block3);
	block.setAttribute(new Attribute("isNumber", isNumber));

    root.getChild("sideBar").getChild("blocks").addContent(block);
	home.writemodify();
	out.print(StrUtil.Alert_Redirect("操作成功！", "home_sidebar.jsp"));
	return;
}
else if (op.equals("setBlock")) {
	int count = ParamUtil.getInt(request, "count", 10);
	String block1 = ParamUtil.get(request, "block1");
	String block2 = ParamUtil.get(request, "block2");
	String block3 = ParamUtil.get(request, "block3");
	int order = ParamUtil.getInt(request, "order");
	String isDisplay = ParamUtil.get(request, "isDisplay");
	if (isDisplay.equals(""))
		isDisplay = "false";
	String isNumber = ParamUtil.get(request, "isNumber");
	if (isNumber.equals(""))
		isNumber = "false";		
	
	ir = root.getChild("sideBar").getChild("blocks").getChildren().iterator();
	int k = 0;
	while (ir.hasNext()) {
		Element block = (Element)ir.next();
		if (k==order) {
			block.setAttribute(new Attribute("count", ""+count));
		    block.setAttribute(new Attribute("isDisplay", isDisplay));	
			block.setText(block1 + "," + block2 + "," + block3);
			block.setAttribute(new Attribute("isNumber", isNumber));
			
			break;
		}
		k++;
	}
	home.writemodify();
	out.print(StrUtil.Alert_Redirect("操作成功！", "home_sidebar.jsp"));
	return;
}
else if (op.equals("delBlock")) {
	int order = ParamUtil.getInt(request, "order");
	ir = root.getChild("sideBar").getChild("blocks").getChildren().iterator();
	int k = 0;
	Element block = null;
	while (ir.hasNext()) {
		block = (Element)ir.next();
		if (k==order) {
			break;
		}
		k++;
	}
	root.getChild("sideBar").getChild("blocks").removeContent(block);
	home.writemodify();
	out.print(StrUtil.Alert_Redirect("操作成功！", "home_sidebar.jsp"));
	return;
}
else if (op.equals("setUserDefine")) {
	String content = ParamUtil.get(request, "content");
	home.setProperty("sideBar.userDefine", content);
	out.print(StrUtil.Alert_Redirect("操作成功！", "home_sidebar.jsp"));
	return;
}
%>
<br>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead">管理边栏</td>
  </tr>
  <tr> 
    <td valign="top"><br>
      <table width="98%" align="center" cellspacing="0" class="frame_gray">
        <tr>
          <td height="22" align="center" class="thead">区块管理</td>
        </tr>
	  <%
	  List list = root.getChild("sideBar").getChild("blocks").getChildren();
	  if (list!=null) {
	  	ir = list.iterator();
		int k = 0;
		while (ir.hasNext()) {
			Element e = (Element)ir.next();
	  %>
      <form id=form<%=k%> name=form<%=k%> action="home_sidebar.jsp?op=setBlock" method=post>
        <tr>
          <td height="22" align="left">
		  <%
		  String blks = e.getText();
		  String count = e.getAttribute("count").getValue();
		  String isDisplay = e.getAttribute("isDisplay").getValue();
		  String isNumber = e.getAttribute("isNumber").getValue();
		  String[] ary = StrUtil.split(blks, ",");
		  %>
		  <select name="block1">
              <%=opts%>
          </select>
		  <select name="block2">
			<%=opts%>
		  </select>
		  <select name="block3">
			<%=opts%>
		  </select>
		  <input name="order" type="hidden" value="<%=k%>" />
		  <input name="isDisplay" value="true" type="checkbox" <%=isDisplay.equals("true")?"checked":""%> />是否显示
		  <input name="isNumber" value="true" type="checkbox" <%=isNumber.equals("true")?"checked":""%> />
		是否带序号&nbsp;条数：
          <input name="count" value="<%=count%>" />
		<script>
		form<%=k%>.block1.value="<%=ary[0]%>";
		form<%=k%>.block2.value="<%=ary[1]%>";
		form<%=k%>.block3.value="<%=ary[2]%>";
		</script>
		<input name="submit2" type="submit" class="btn" value="确 定">
		<input name="submit22" type="button" class="btn" value="删 除" onclick="if (confirm('您确定要删除么？')) window.location.href='home_sidebar.jsp?op=delBlock&order=<%=k%>' "></td>
        </tr>
		</form>
		<%
			k++;
			}
		}%>
      </table>
      <br>
      <form id=form1 name=form1 action="home_sidebar.jsp?op=addBlock" method=post>
      <table width="98%" align="center" cellspacing="0" class="frame_gray">
          <tr>
            <td height="22" align="center" class="thead">添加区块</td>
          </tr>
          <tr>
            <td height="22" align="left">
			  <select name="block1">
			<%=opts%>
			</select>
			<select name="block2">
			<%=opts%>
			</select>
			<select name="block3">
			<%=opts%>
			</select>&nbsp;
			<input name="isDisplay" value="true" type="checkbox" checked />是否显示
			<input name="isNumber" value="true" type="checkbox" checked />
是否带序号&nbsp;条数：
<input name="count" value="10" />
			<input name="submit" type="submit" class="btn" value="确 定">			</td>
          </tr>
      </table>
	  </form>
      <br>
	  
      <form id=formDefine name=formDefine action="home_sidebar.jsp?op=setUserDefine" method=post onSubmit="return formDefine_onsubmit()">
      <table width="73%" align="center" class="frame_gray">
        <tr>
          <td height="22" class="thead">
		  其它
            <input name="content" type="hidden" value="">
          </td>
        </tr>
        <tr>
          <td height="22">
<pre id="divUserDefine" name="divUserDefine" style="display:none">
<%=home.getProperty("sideBar.userDefine")%>
</pre>
<script type="text/javascript" src="../../FCKeditor/fckeditor.js"></script>
<script type="text/javascript">
<!--
var oFCKeditor = new FCKeditor("FCKeditor");
oFCKeditor.BasePath = '../../FCKeditor/';
oFCKeditor.Config['CustomConfigurationsPath'] = "<%=request.getContextPath()%>/FCKeditor/fckconfig_cws_forum.jsp";
oFCKeditor.ToolbarSet = 'Simple'; // 'Basic' ;
oFCKeditor.Width = "100%";
oFCKeditor.Height = 150 ;
oFCKeditor.Value = divUserDefine.innerHTML;

// 解决自动首尾加<p></p>的问题
// oFCKeditor.Config["EnterMode"] = 'br' ;     // p | div | br (回车)
// oFCKeditor.Config["ShiftEnterMode"] = 'p' ; // p | div | br(shift+enter)
oFCKeditor.Config["FormatSource"]=false;
oFCKeditor.Create() ;
//-->
        </script>
          </td>
        </tr>
        <tr>
          <td height="22" align="center"><input name="submit23" type="submit" style="border:1pt solid #636563;font-size:9pt; LINE-HEIGHT: normal;HEIGHT: 18px;" value="确 定"></td>
        </tr>
      </table>
      </form>
      <br>
      <br>
      <br>
    <br></td>
  </tr>
</table>
</td> </tr>             
      </table>                                        
       </td>                                        
     </tr>                                        
 </table>                                        
</body>                                        
</html>                            
  