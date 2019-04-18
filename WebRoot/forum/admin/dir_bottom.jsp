<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.forum.*" %>
<%@ page import="com.redmoon.forum.plugin2.*" %>
<%@ page import="com.redmoon.forum.ui.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title></title>
<LINK href="../common.css" type=text/css rel=stylesheet>
<LINK href="default.css" type=text/css rel=stylesheet>
<script src="../../inc/common.js"></script>
<script>
function form1_onsubmit() {
	if (o("code").value=="") {
		alert("编码不能为空！");
		o("code").focus();
		return false;
	}
	if (o("name").value=="") {
		alert("名称不能为空！");
		o("name").focus();
		return false;
	}
}
</script>
</head>
<body>
<jsp:useBean id="dir" scope="page" class="com.redmoon.forum.Directory"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String parent_code = ParamUtil.get(request, "parent_code");
if (parent_code.equals(""))
	parent_code = "root";
String parent_name = ParamUtil.get(request, "parent_name");
String code = ParamUtil.get(request, "code");
String name = ParamUtil.get(request, "name");
String description = ParamUtil.get(request, "description");

String op = ParamUtil.get(request, "op");
boolean isHome = false;
int type = 0;
if (op.equals(""))
	op = "AddChild";
Leaf leaf = null;
if (op.equals("modifydo")) {
	boolean re = true;
	try {
		re = dir.update(application, request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re)
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "res.label.forum.admin.dir_bottom", "op_success_need_refresh")));
}
String action = "dir_top.jsp";
String target = "dirmainFrame";
if (op.equals("modify") || op.equals("modifydo")) {
	leaf = dir.getLeaf(code);
	Leaf parentLeaf = leaf.getLeaf(leaf.getParentCode());
	parent_name = parentLeaf.getName();
	name = leaf.getName();
	description = leaf.getDescription();
	type = leaf.getType();
	isHome = leaf.getIsHome();
	op = "modifydo";
	action = "dir_bottom.jsp";
	target = "_self";
}
%>
<TABLE 
style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" 
cellSpacing=0 cellPadding=3 width="95%" align=center>
  <!-- Table Head Start-->
  <TBODY>
    <TR>
      <TD class=thead style="PADDING-LEFT: 10px" noWrap width="70%"><lt:Label res="res.label.forum.admin.dir_bottom" key="dir_add_or_del"/></TD>
    </TR>
    <TR class=row style="BACKGROUND-COLOR: #fafafa">
      <TD align="center" style="PADDING-LEFT: 10px"><table class="frame_gray" width="88%" border="0" cellpadding="0" cellspacing="1">
        <tr>
          <td align="center"><table width="98%">
            <form name="form1" method="post" action="<%=action%>?op=<%=op%>&code=<%=code%>" target="<%=target%>" enctype="MULTIPART/FORM-DATA" onSubmit="return form1_onsubmit()">
              <tr>
                <td width="82" rowspan="8" align="left" valign="top"><br>
                  父结点：<br>
                    <font color=blue><%=parent_name.equals("")?SkinUtil.LoadString(request, "res.label.forum.admin.dir_bottom", "dir_root"):parent_name%></font>
                    <br>
                    <%
				if (op.equals("modify") || op.equals("modifydo")) {
					if (leaf.getLogo()!=null && !leaf.getLogo().equals(""))
						out.print("<img src='../images/board_logo/" + leaf.getLogo() + "'>");
				}
				%></td>
                <td width="63" align="right"> <lt:Label res="res.label.forum.admin.dir_bottom" key="code"/></td>
              <td width="624" align="left"><input name="code" value="<%=code%>" <%=(op.equals("modify") || op.equals("modifydo"))?"readonly":""%>></td>
              </tr>
              <tr>
                <td width="63" align="right"><lt:Label res="res.label.forum.admin.dir_bottom" key="name"/></td>
              <td align="left"><input name="name" value="<%=StrUtil.HtmlEncode(name)%>">
                <select name="delMode">
                  <option value="<%=Leaf.DEL_DUSTBIN%>">
                    <lt:Label res="res.label.forum.admin.dir_bottom" key="del_dustbin"/>
                  </option>
                  <option value="<%=Leaf.DEL_FOREVER%>">
                    <lt:Label res="res.label.forum.admin.dir_bottom" key="del_forever"/>
                  </option>
                </select>
				  <script>
				  <%if (op.equals("modify") || op.equals("modifydo")) {%>
				  form1.delMode.value = "<%=leaf.getDelMode()%>"
				  <%}%>
				  </script>				</td>
              </tr>
              <tr>
                <td width="63" align="right"><lt:Label res="res.label.forum.admin.dir_bottom" key="desc"/></td>
              <td align="left"><textarea name="description" cols="52" rows="3"><%=description%></textarea>
                <input type=hidden name=parent_code value="<%=parent_code%>"></td>
              </tr>
              <tr>
                <td width="63" align="right">
				<script>
					// var root_code = window.parent.dirmainFrame.getRootCode();				  
					// document.write("<input type=hidden name=root_code value='" + root_code + "'");
				  </script>				  </td>
              <td align="left"><%
				String disabled = "";
				if (op.equals("modify") && leaf.getType()>=1) {
					disabled = "disabled"; %>
                  <input type=hidden name="type" value="<%=leaf.getType()%>">
                <%}%>
<lt:Label res="res.label.forum.admin.dir_bottom" key="type"/>
<select name="type" <%=disabled%>>
  <option value="0"><lt:Label res="res.label.forum.admin.dir_bottom" key="field"/></option>
  <option value="1" <%=!parent_code.equals("root")?"selected":""%>><lt:Label res="res.label.forum.admin.dir_bottom" key="board"/></option>
</select>
				  <script>
				  <%if (op.equals("modify") || op.equals("modifydo")) {%>
				  form1.type.value = "<%=type%>"
				  <%}%>
				  </script>
<%
ThemeMgr tmmgr = new ThemeMgr();
Vector v = tmmgr.getAllTheme();
Iterator ir = v.iterator();
String options = "";
while (ir.hasNext()) {
	Theme tm = (Theme) ir.next();
	options += "<option value='" + tm.getCode() + "'>" + tm.getName() + "</option>";
}

String theme = "";
if (leaf!=null)
	theme = StrUtil.getNullString(leaf.getTheme());
// if (theme.equals(""))
// 	theme = "default";
%> 
<lt:Label res="res.label.forum.admin.dir_bottom" key="topic"/>				  
<select name="theme">
<option value=""><lt:Label res="res.label.forum.admin.dir_bottom" key="none"/></option>
<%=options%>
</select>
<%
SkinMgr skmgr = new SkinMgr();
v = skmgr.getAllSkin();
ir = v.iterator();
String skinoptions = "";
while (ir.hasNext()) {
	Skin skin = (Skin) ir.next();
	skinoptions += "<option value='" + skin.getCode() + "'>" + skin.getName() + "</option>";
}

String skin = "";
if (leaf!=null)
	skin = StrUtil.getNullString(leaf.getSkin());
// if (skin.equals(""))
// 	skin = "default";
%>
<lt:Label res="res.label.forum.admin.dir_bottom" key="skin"/>				  
<select name="skin">
<option value=""><lt:Label res="res.label.forum.admin.dir_bottom" key="none"/></option>
<%=skinoptions%>
</select>
				  <script>
				  <%if (op.equals("modify") || op.equals("modifydo")) {%>
				  form1.theme.value = "<%=theme%>"
				  form1.skin.value = "<%=skin%>"
				  <%}%>				  
				  </script>				<%if (op.equals("modifydo")) {%>
                  <input type="checkbox" name="isBold" value="true" <%=leaf.isBold()?"checked":""%> >
                  <%}else{%>
                  <input type="checkbox" name="isBold" value="true">
                  <%}%>
                  <lt:Label res="res.label.forum.admin.dir_bottom" key="isBold"/></td>
              </tr>
              <tr>
                <td width="63" align="right">&nbsp;</td>
              <td align="left">
			    <%if (op.equals("modifydo")) {%>
                <input type="checkbox" name="isHome" value="true" <%=isHome?"checked":""%> >
                <%}else{%>
                <input type="checkbox" name="isHome" value="true" checked>
                <%}%>
				<lt:Label res="res.label.forum.admin.dir_bottom" key="confirm_home"/>
				<select name="isLocked">
				<option value=0><lt:Label res="res.label.forum.admin.dir_bottom" key="open"/></option>
				<option value=1><lt:Label res="res.label.forum.admin.dir_bottom" key="lock"/></option>
				</select>
				<%if (op.equals("modifydo")) {%>
				<script>
				form1.isLocked.value = "<%=leaf.isLocked()?1:0%>";
				</script>
				<%}%>
				<select name="color">
                  <option value="" style="COLOR: black" selected><lt:Label res="res.label.forum.admin.dir_bottom" key="clear_color"/></option>
                  <option style="BACKGROUND: #000088" value="#000088"></option>
                  <option style="BACKGROUND: #0000ff" value="#0000ff"></option>
                  <option style="BACKGROUND: #008800" value="#008800"></option>
                  <option style="BACKGROUND: #008888" value="#008888"></option>
                  <option style="BACKGROUND: #0088ff" value="#0088ff"></option>
                  <option style="BACKGROUND: #00a010" value="#00a010"></option>
                  <option style="BACKGROUND: #1100ff" value="#1100ff"></option>
                  <option style="BACKGROUND: #111111" value="#111111"></option>
                  <option style="BACKGROUND: #333333" value="#333333"></option>
                  <option style="BACKGROUND: #50b000" value="#50b000"></option>
                  <option style="BACKGROUND: #880000" value="#880000"></option>
                  <option style="BACKGROUND: #8800ff" value="#8800ff"></option>
                  <option style="BACKGROUND: #888800" value="#888800"></option>
                  <option style="BACKGROUND: #888888" value="#888888"></option>
                  <option style="BACKGROUND: #8888ff" value="#8888ff"></option>
                  <option style="BACKGROUND: #aa00cc" value="#aa00cc"></option>
                  <option style="BACKGROUND: #aaaa00" value="#aaaa00"></option>
                  <option style="BACKGROUND: #ccaa00" value="#ccaa00"></option>
                  <option style="BACKGROUND: #ff0000" value="#ff0000"></option>
                  <option style="BACKGROUND: #ff0088" value="#ff0088"></option>
                  <option style="BACKGROUND: #ff00ff" value="#ff00ff"></option>
                  <option style="BACKGROUND: #ff8800" value="#ff8800"></option>
                  <option style="BACKGROUND: #ff0005" value="#ff0005"></option>
                  <option style="BACKGROUND: #ff88ff" value="#ff88ff"></option>
                  <option style="BACKGROUND: #ee0005" value="#ee0005"></option>
                  <option style="BACKGROUND: #ee01ff" value="#ee01ff"></option>
                  <option style="BACKGROUND: #3388aa" value="#3388aa"></option>
                  <option style="BACKGROUND: #000000" value="#000000"></option>
                </select>
				<select name="checkMsg">
				<option value="<%=Leaf.CHECK_NOT%>"><lt:Label res="res.label.forum.admin.dir_bottom" key="chk_not"/></option>
				<option value="<%=Leaf.CHECK_TOPIC%>"><lt:Label res="res.label.forum.admin.dir_bottom" key="chk_topic"/></option>
				<option value="<%=Leaf.CHECK_TOPIC_REPLY%>"><lt:Label res="res.label.forum.admin.dir_bottom" key="chk_topic_reply"/></option>
				</select>
				<select name="displayStyle">
				<option value="<%=Leaf.DISPLAY_STYLE_VERTICAL%>"><lt:Label res="res.label.forum.admin.dir_bottom" key="display_style_vertical"/></option>
				<option value="<%=Leaf.DISPALY_STYLE_HORIZON%>"><lt:Label res="res.label.forum.admin.dir_bottom" key="display_style_horizon"/></option>
				</select>
				<%if (op.equals("modifydo")) {%>
				<script>
				form1.color.value = "<%=leaf.getColor()%>";
				form1.checkMsg.value = "<%=leaf.getCheckMsg()%>";
				form1.displayStyle.value = "<%=leaf.getDisplayStyle()%>";
				</script>
				<%}%></td>
              </tr>
              <tr>
                <td width="63" align="right">&nbsp;</td>
                <td align="left"><span class="unnamed2">
                  <%if (op.equals("modifydo")) {%>
						<script>
						  var bcode = "<%=leaf.getCode()%>";
						</script>
						&nbsp;<lt:Label res="res.label.forum.admin.dir_bottom" key="dir_parent"/>：
						<select name="parentCode">
						<%
								Leaf rootlf = leaf.getLeaf("root");
								DirectoryView dv = new DirectoryView(rootlf);
								dv.ShowDirectoryAsOptionsWithCode(out, rootlf, rootlf.getLayer());
						%>
						</select>
						<script>
						form1.parentCode.value = "<%=leaf.getParentCode()%>";
						</script>
				  <%}%>
				  <lt:Label res="res.label.forum.admin.dir_bottom" key="add_mode"/>
                  <select name="webeditAllowType">
				  <option value="<%=Leaf.WEBEDIT_ALLOW_TYPE_UBB_NORMAL%>"><lt:Label res="res.label.forum.admin.dir_bottom" key="normal_ubb"/></option>
<%
if (com.redmoon.forum.Config.getInstance().getBooleanProperty("forum.isWebeditTopicEnabled")) {
%>				  
				  <option value="<%=Leaf.WEBEDIT_ALLOW_TYPE_UBB_NORMAL_REDMOON%>"><lt:Label res="res.label.forum.admin.dir_bottom" key="normal_ubb_senior"/></option>
				  <option value="<%=Leaf.WEBEDIT_ALLOW_TYPE_REDMOON_FIRST%>"><lt:Label res="res.label.forum.admin.dir_bottom" key="default_senior"/></option>
<%}%>				  
				  </select>
                  <%if (op.equals("modifydo")) {%>
				  <script>
				  form1.webeditAllowType.value = "<%=leaf.getWebeditAllowType()%>";
				  </script>
				  <%}%>
					</span></td>
              </tr>
              <tr>
                <td align="center">&nbsp;</td>
                <td align="left">LOGO
                  <input type="file" name="filename">
                  <%		
				  String codes = "";
				  String names = "";
				  if (op.equals("modify") || op.equals("modifydo")){
				  		String logo = StrUtil.getNullString(leaf.getLogo());
						if (!logo.equals("")) {%>
                  <input type=checkbox name=dellogo value=1>
<lt:Label res="res.label.forum.admin.dir_bottom" key="del_logo"/>
<%}
						String[] ary = StrUtil.split(leaf.getPlugin2Code(), ",");
						  if (ary!=null) {
							int len = ary.length;
							Plugin2Mgr pm = new Plugin2Mgr();
							Plugin2Unit pu = null;
							for (int i=0; i<len; i++) {
								if (codes.equals("")) {
									codes = ary[i];
									pu = pm.getPlugin2Unit(ary[i]);
									if (pu!=null)
										names = pu.getName(request);
								}
								else {
									codes += "," + ary[i];
									pu = pm.getPlugin2Unit(ary[i]);
									if (pu!=null)
										names += "," + pu.getName(request);
								}
							}
						  }						
				  }%>
<br>
<lt:Label res="res.label.forum.admin.dir_bottom" key="plugin"/>：<input name="plugin2Code" type="hidden" value="<%=leaf!=null?codes:""%>">
<input name="plugin2CodeName" value="<%=leaf==null?"":names%>" readonly size="50">
&nbsp;<input type="button" onClick="openSelWin()" value="<lt:Label res="res.label.forum.admin.dir_bottom" key="select"/>"></td>
              </tr>
              <tr>
                <td colspan="2" align="center"><input name="Submit" type="submit" class="singleboarder" value="<lt:Label key="commit"/>">
                  &nbsp;&nbsp;&nbsp;
                  <input name="Submit" type="reset" class="singleboarder" value="<lt:Label key="reset"/>"></td>
              </tr>
              <tr>
                <td align="left" valign="top">&nbsp;</td>
                <td colspan="2" align="center">&nbsp;				</td>
              </tr>
            </form>
          </table></td>
        </tr>
      </table>
      </TD>
    </TR>
    <!-- Table Body End -->
    <!-- Table Foot -->
    <TR>
      <TD class=tfoot align=right><DIV align=right> </DIV></TD>
    </TR>
    <!-- Table Foot -->
  </TBODY>
</TABLE>
</body>
<script>
function openWin(url,width,height)
{
  var newwin = window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,top=50,left=120,width="+width+",height="+height);
}

function getPlugin2Code() {
	return form1.plugin2Code.value;
}

function openSelWin() {
	openWin("sel_plugin2.jsp", 480, 320);
}

function setPlugin2Code(ret) {
	if (ret==null)
		return;
	form1.plugin2CodeName.value = "";
	form1.plugin2Code.value = "";
	for (var i=0; i<ret.length; i++) {
		if (form1.plugin2CodeName.value=="") {
			form1.plugin2Code.value += ret[i][0];
			form1.plugin2CodeName.value += ret[i][1];
		}
		else {
			form1.plugin2Code.value += "," + ret[i][0];
			form1.plugin2CodeName.value += "," + ret[i][1];
		}
	}
}
</script>
</html>
