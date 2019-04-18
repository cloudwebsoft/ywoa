<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../../../inc/inc.jsp" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.Conn"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.miniplugin.index.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<head>
<meta http-equiv="pragma" content="no-cache">
<LINK href="../../../admin/default.css" type=text/css rel=stylesheet>
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>插件管理</title>
<style>
.btn {
border:1pt solid #636563;font-size:9pt; LINE-HEIGHT: normal;HEIGHT: 18px;
}
</style>
<script language="JavaScript">
<!--
function openWin(url,width,height){
	var newwin = window.open(url,"_blank","scrollbars=yes,resizable=yes,toolbar=no,location=no,directories=no,status=no,menubar=no,top=50,left=120,width="+width+",height="+height);
}
var urlObj;
function SelectImage(urlObject){
	urlObj = urlObject;
	openWin("../../../admin/media_frame.jsp?action=selectImage", 800, 600);
}
function SetUrl(visualPath) {
	urlObj.value = visualPath;
}

function openSelTopicWin(idsObj) {
	curObj = idsObj;
	openWin("../../../topic_m.jsp?action=sel", 800, 600);	
}

function selTopic(ids) {
	// 检查在notices中是否已包含了ids中的id，避免重复加入
	var ary = ids.split(",");
	var ntc = curObj.value;
	var ary2 = ntc.split(",");
	for (var i=0; i<ary.length; i++) {
		var founded = false;
		for (var j=0; j<ary2.length; j++) {
			if (ary[i]==ary2[j]) {
				founded = true;
				break;
			}
		}
		if (!founded) {
			if (ntc=="")
				ntc += ary[i];
			else
				ntc += "," + ary[i];
		}
	}
	curObj.value = ntc;
}

function delId(idsForm, id) {
	var ntc = idsForm.value.value;
	var ary = ntc.split(",");
	var ary2 = new Array();
	var k = 0;
	for (var i=0; i<ary.length; i++) {
		if (ary[i]==id) {
			continue;
		}
		else {
			ary2[k] = ary[i];
			k++;
		}
	}
	ntc = "";
	for (i=0; i<ary2.length; i++) {
		if (ntc=="")
			ntc += ary2[i];
		else
			ntc += "," + ary2[i];
	}
	idsForm.value.value = ntc;
			
	idsForm.submit();
}

function up(idsForm, id) {
	var ntc = idsForm.value.value;
	var ary = ntc.split(",");
	for (var i=0; i<ary.length; i++) {
		if (ary[i]==id) {
			// 往上移动的节点不是第一个节点
			if (i!=0) {
				var tmp = ary[i-1];
				ary[i-1] = ary[i];
				ary[i] = tmp;
			}
			else
				return;
			break;
		}
	}
	ntc = "";
	for (i=0; i<ary.length; i++) {
		if (ntc=="")
			ntc += ary[i];
		else
			ntc += "," + ary[i];
	}
	idsForm.value.value = ntc;
	idsForm.submit();
}

function down(idsForm, id) {
	var ntc = idsForm.value.value;
	var ary = ntc.split(",");
	for (var i=0; i<ary.length; i++) {
		if (ary[i]==id) {
			// 往上移动的节点不是第一个节点
			if (i!=ary.length-1) {
				var tmp = ary[i+1];
				ary[i+1] = ary[i];
				ary[i] = tmp;
			}
			else
				return;
			break;
		}
	}
	ntc = "";
	for (i=0; i<ary.length; i++) {
		if (ntc=="")
			ntc += ary[i];
		else
			ntc += "," + ary[i];
	}
	idsForm.value.value = ntc;
	idsForm.submit();
}
//-->
</script>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

Index home = Index.getInstance();

String op = ParamUtil.get(request, "op");
if (op.equals("setUserDefine")) {
	String content = ParamUtil.get(request, "content");
	home.setProperty("userDefine.content", content);
	
	out.print(StrUtil.Alert_Redirect("操作成功！", "manager.jsp"));
}

if (op.equals("setBoards")) {
	for (int i=1; i<=8; i++) {
		String block = ParamUtil.get(request, "block" + i);
		home.setProperty("blocks", "id", "" + i, block);	
	}	
	out.print(StrUtil.Alert_Redirect("操作成功！", "manager.jsp"));
	return;
}
else if (op.equals("setBlockIds")) {
	String ids = ParamUtil.get(request, "ids");
	String value = ParamUtil.get(request, "value");
	home.setProperty("blocks", "id", ids, value);	
	out.print(StrUtil.Alert_Redirect("操作成功！", "manager.jsp"));
	return;
}
else if (op.equals("setBlockName")) {
	String id = ParamUtil.get(request, "id");
	String name = ParamUtil.get(request, "name");
	home.setProperty("blocks", "id", "name" + id, name);
	out.print(StrUtil.Alert_Redirect("操作成功！", "manager.jsp"));
	return;
}
else if (op.equals("setFlashImages")) {
	for (int i=1; i<=5; i++) {
		String url = ParamUtil.get(request, "url" + i);
		String link = ParamUtil.get(request, "link" + i);
		String text = ParamUtil.get(request, "text" + i);
		home.setProperty("flash", "id", "" + i, "url", url);	
		home.setProperty("flash", "id", "" + i, "link", link);	
		home.setProperty("flash", "id", "" + i, "text", text);	
	}	
	out.print(StrUtil.Alert_Redirect("操作成功！", "manager.jsp"));
}%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head">管理迷你插件</td>
  </tr>
</table>
<br>
<table width="98%" height="227" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead">管理首页新贴-精华-置顶</td>
  </tr>
  <tr> 
    <td valign="top"><br>
    <br>
      <table width="73%" align="center" class="frame_gray">
        <form id=form2 name=form2 action="?op=setUserDefine" method=post>
          <tr>
            <td height="22" class="thead"><strong><a name="focus">自定义</a></strong></td>
          </tr>
          <tr>
            <td height="22">摘要：
<pre id="divAbstract" name="divAbstract" style="display:none">
<%=home.getProperty("userDefine.content")%>
</pre>
<script type="text/javascript" src="../../../../FCKeditor/fckeditor.js"></script>
<script type="text/javascript">
<!--
var oFCKeditor = new FCKeditor('content') ;
oFCKeditor.BasePath = '../../../../FCKeditor/';
oFCKeditor.Config['CustomConfigurationsPath'] = '<%=request.getContextPath()%>/FCKeditor/fckconfig_cws_forum.jsp' ;
oFCKeditor.ToolbarSet = 'Simple'; // 'Basic' ;
oFCKeditor.Width = 550 ;
oFCKeditor.Height = 150 ;
oFCKeditor.Value = divAbstract.innerHTML;

oFCKeditor.Config["LinkBrowser"]=false;//文件
oFCKeditor.Config["ImageBrowser"]=true;
oFCKeditor.Config["FlashBrowser"]=true;

oFCKeditor.Config["LinkUpload"]=false;
oFCKeditor.Config["ImageUpload"]=false;
oFCKeditor.Config["FlashUpload"]=false;

oFCKeditor.Create() ;
//-->
</script></td>
          </tr>
          <tr>
            <td height="22" align="center"><input name="submit2" type="submit" style="border:1pt solid #636563;font-size:9pt; LINE-HEIGHT: normal;HEIGHT: 18px;" value="确 定"></td>
          </tr>
        </form>
      </table>
      <br>
      <table width="73%" align="center" class="frame_gray">
        <form id=form3 name=form3 action="?op=setBoards" method=post>
          <tr>
            <td height="22" colspan="4" class="thead"><strong><a name="boards">区块设置</a></strong></td>
          </tr>
          <tr>
            <td height="22" colspan="2">区块2设置
              <select name="block2">
				<option value="newtopic" selected>最新贴子</option>
				<option value="newelite">最新精华</option>
				<option value="newtop">最新置顶</option>				  
				<option value="custom">自定义</option>				  
              </select>
            <script>
			  form3.block2.value = "<%=home.getProperty("blocks", "id", "" + 2)%>";
			    </script></td>
            <td width="34%">区块3设置
              <select name="block3">
				<option value="newtopic">最新贴子</option>
				<option value="newelite" selected>最新精华</option>
				<option value="newtop">最新置顶</option>				  
				<option value="custom">自定义</option>				  
              </select>
              <script>
			  form3.block3.value = "<%=home.getProperty("blocks", "id", "" + 3)%>";
			  </script></td>
            <td width="33%">区块4设置
              <select name="block4">
				<option value="newtopic">最新贴子</option>
				<option value="newelite">最新精华</option>
				<option value="newtop" selected>最新置顶</option>				  
				<option value="custom">自定义</option>				  
              </select>
              <script>
			  form3.block4.value = "<%=home.getProperty("blocks", "id", "" + 4)%>";
			  </script></td>
          </tr>
          <tr>
            <td height="22" colspan="4" align="center"><input name="submit3" type="submit" style="border:1pt solid #636563;font-size:9pt; LINE-HEIGHT: normal;HEIGHT: 18px;" value=" 确 定 ">
            <br>
            (区块共分四格，第一格为Flash图片变换，其后依次为第二、三、四格)</td>
          </tr>
        </form>
      </table>
      <br>
      <table width="73%" align="center" cellspacing="0" class="frame_gray">
        <form id=form22 name=form22 action="?op=setBlockIds" method=post>
          <tr>
            <td height="22" class="thead"><strong><a name="hot">区块2</a></strong>&nbsp;( 编号之间用，分隔 )
			<input name="ids" value="ids2" type="hidden">			</td>
          </tr>
          <tr>
            <td height="22">
				<input type=text value="<%=StrUtil.getNullString(home.getProperty("blocks", "id", "ids" + 2))%>" name="value" size=60>
                <input name="button2" type="button" class="btn" onClick="openSelTopicWin(form22.value)" value="选 择">
            <input name="submit2" type="submit" class="btn" value="确 定"></td>
          </tr>
          <tr>
            <td height="22"><%
			MsgMgr mm = new MsgMgr();
			MsgDb md = null;
			String[] v = StrUtil.split(home.getProperty("blocks", "id", "ids" + 2), ",");
			int hotlen = 0;
			if (v!=null)
				hotlen = v.length;
			if (hotlen==0)
				out.print("无！");
			else {
				for (int k=0; k<hotlen; k++) {
					long n = StrUtil.toInt(v[k], -1);
					if (n==-1)
						continue;
					md = mm.getMsgDb(n);
					if (md.isLoaded()) {
						String color = StrUtil.getNullString(md.getColor());
						if (color.equals("")) {%>
						<%=md.getId()%>&nbsp;<img src="../../../admin/images/arrow.gif">&nbsp;<a target="_blank" href="<%=request.getContextPath()%>/forum/showtopic.jsp?rootid=<%=md.getId()%>"><%=md.getTitle()%></a>
						<%}else{%>
						<%=md.getId()%>&nbsp;<img src="../../../admin/images/arrow.gif">&nbsp;<a target="_blank" href="<%=request.getContextPath()%>/forum/showtopic.jsp?rootid=<%=md.getId()%>"><font color="<%=color%>"><%=md.getTitle()%></font></a>
						<%}%>
					&nbsp;&nbsp;[<a onClick="return confirm('您确定要删除么？')" href="javascript:delId(form22, '<%=md.getId()%>')">
					<lt:Label key="op_del"/>
					</a>]
					<%if (k!=0) {%>
					&nbsp;&nbsp;[<a href="javascript:up(form22, '<%=md.getId()%>')">
					<lt:Label res="res.label.forum.admin.ad_topic_bottom" key="up"/>
					</a>]
					<%}%>
					<%if (k!=hotlen-1) {%>
					&nbsp;&nbsp;[<a href="javascript:down(form22, '<%=md.getId()%>')">
					<lt:Label res="res.label.forum.admin.ad_topic_bottom" key="down"/>
					</a>]
					<%}%>
			<br>
			<%	}
				else {%>
			<%=v[k]%>&nbsp;<font color=red><img src="../../../admin/images/arrow.gif">&nbsp;贴子不存在</font> &nbsp;&nbsp;[<a onClick="return confirm('您确定要删除么？')" href="javascript:delId(form22, '<%=v[k]%>')">
			<lt:Label key="op_del"/>
			</a>]<BR>
			<%}
				}
			}%>
			</td>
          </tr>
        </form>
        <form action="?op=setBlockName" method=post>
          <tr>
            <td height="22">
			<input name="id" value="2" type="hidden">
			名称&nbsp;
			<input name="name" value="<%=StrUtil.getNullString(home.getProperty("blocks", "id", "name2"))%>">
			<input name="submit23" type="submit" class="btn" value="确 定"></td>
          </tr>
		</form>
      </table>
      <br>
      <table width="73%" align="center" cellspacing="0" class="frame_gray">
        <form id=form33 name=form33 action="?op=setBlockIds" method=post>
          <tr>
            <td height="22" class="thead"><strong><a name="hot">区块3</a></strong>&nbsp;( 编号之间用，分隔 )
              <input name="ids" value="ids3" type="hidden">
            </td>
          </tr>
          <tr>
            <td height="22"><input type=text value="<%=StrUtil.getNullString(home.getProperty("blocks", "id", "ids" + 3))%>" name="value" size=60>
                <input name="button22" type="button" class="btn" onClick="openSelTopicWin(form33.value)" value="选 择">
                <input name="submit22" type="submit" class="btn" value="确 定"></td>
          </tr>
          <tr>
            <td height="22"><%
			v = StrUtil.split(home.getProperty("blocks", "id", "ids" + 3), ",");
			hotlen = 0;
			if (v!=null)
				hotlen = v.length;
			if (hotlen==0)
				out.print("无！");
			else {
				for (int k=0; k<hotlen; k++) {
					long n = StrUtil.toInt(v[k], -1);
					if (n==-1)
						continue;
					md = mm.getMsgDb(n);
					if (md.isLoaded()) {
						String color = StrUtil.getNullString(md.getColor());
						if (color.equals("")) {%>
                <%=md.getId()%>&nbsp;<img src="../../../admin/images/arrow.gif">&nbsp;<a target="_blank" href="<%=request.getContextPath()%>/forum/showtopic.jsp?rootid=<%=md.getId()%>"><%=md.getTitle()%></a>
                <%}else{%>
                <%=md.getId()%>&nbsp;<img src="../../../admin/images/arrow.gif">&nbsp;<a target="_blank" href="<%=request.getContextPath()%>/forum/showtopic.jsp?rootid=<%=md.getId()%>"><font color="<%=color%>"><%=md.getTitle()%></font></a>
                <%}%>
              &nbsp;&nbsp;[<a onClick="return confirm('您确定要删除么？')" href="javascript:delId(form33, '<%=md.getId()%>')">
                <lt:Label key="op_del"/>
                </a>]
              <%if (k!=0) {%>
              &nbsp;&nbsp;[<a href="javascript:up(form33, '<%=md.getId()%>')">
                <lt:Label res="res.label.forum.admin.ad_topic_bottom" key="up"/>
                </a>]
              <%}%>
              <%if (k!=hotlen-1) {%>
              &nbsp;&nbsp;[<a href="javascript:down(form33, '<%=md.getId()%>')">
                <lt:Label res="res.label.forum.admin.ad_topic_bottom" key="down"/>
                </a>]
              <%}%>
              <br>
              <%	}
				else {%>
              <%=v[k]%>&nbsp;<font color=red><img src="../../../admin/images/arrow.gif">&nbsp;贴子不存在</font> &nbsp;&nbsp;[<a onClick="return confirm('您确定要删除么？')" href="javascript:delId(form33, '<%=v[k]%>')">
                <lt:Label key="op_del"/>
                </a>]<BR>
              <%}
				}
			}%>
            </td>
          </tr>
        </form>
        <form action="?op=setBlockName" method=post>
          <tr>
            <td height="22">
			<input name="id" value="3" type="hidden">
			名称&nbsp;
			<input name="name" value="<%=StrUtil.getNullString(home.getProperty("blocks", "id", "name3"))%>">
			<input name="submit23" type="submit" class="btn" value="确 定"></td>
          </tr>
		</form>		
      </table>
      <br>
      <table width="73%" align="center" cellspacing="0" class="frame_gray">
        <form id=form44 name=form44 action="?op=setBlockIds" method=post>
          <tr>
            <td height="22" class="thead"><strong><a name="hot">区块4</a></strong>&nbsp;( 编号之间用，分隔 )
              <input name="ids" value="ids4" type="hidden">
            </td>
          </tr>
          <tr>
            <td height="22"><input type=text value="<%=StrUtil.getNullString(home.getProperty("blocks", "id", "ids" + 4))%>" name="value" size=60>
                <input name="button222" type="button" class="btn" onClick="openSelTopicWin(form44.value)" value="选 择">
                <input name="submit222" type="submit" class="btn" value="确 定"></td>
          </tr>
          <tr>
            <td height="22"><%
			v = StrUtil.split(home.getProperty("blocks", "id", "ids" + 4), ",");
			hotlen = 0;
			if (v!=null)
				hotlen = v.length;
			if (hotlen==0)
				out.print("无！");
			else {
				for (int k=0; k<hotlen; k++) {
					long n = StrUtil.toInt(v[k], -1);
					if (n==-1)
						continue;
					md = mm.getMsgDb(n);
					if (md.isLoaded()) {
						String color = StrUtil.getNullString(md.getColor());
						if (color.equals("")) {%>
                <%=md.getId()%>&nbsp;<img src="../../../admin/images/arrow.gif">&nbsp;<a target="_blank" href="<%=request.getContextPath()%>/forum/showtopic.jsp?rootid=<%=md.getId()%>"><%=md.getTitle()%></a>
                <%}else{%>
                <%=md.getId()%>&nbsp;<img src="../../../admin/images/arrow.gif">&nbsp;<a target="_blank" href="<%=request.getContextPath()%>/forum/showtopic.jsp?rootid=<%=md.getId()%>"><font color="<%=color%>"><%=md.getTitle()%></font></a>
                <%}%>
              &nbsp;&nbsp;[<a onClick="return confirm('您确定要删除么？')" href="javascript:delId(form44, '<%=md.getId()%>')">
                <lt:Label key="op_del"/>
                </a>]
              <%if (k!=0) {%>
              &nbsp;&nbsp;[<a href="javascript:up(form44, '<%=md.getId()%>')">
                <lt:Label res="res.label.forum.admin.ad_topic_bottom" key="up"/>
                </a>]
              <%}%>
              <%if (k!=hotlen-1) {%>
              &nbsp;&nbsp;[<a href="javascript:down(form44, '<%=md.getId()%>')">
                <lt:Label res="res.label.forum.admin.ad_topic_bottom" key="down"/>
                </a>]
              <%}%>
              <br>
              <%	}
				else {%>
              <%=v[k]%>&nbsp;<font color=red><img src="../../../admin/images/arrow.gif">&nbsp;贴子不存在</font> &nbsp;&nbsp;[<a onClick="return confirm('您确定要删除么？')" href="javascript:delId(form44, '<%=v[k]%>')">
                <lt:Label key="op_del"/>
                </a>]<BR>
              <%}
				}
			}%>
            </td>
          </tr>
        </form>
        <form action="?op=setBlockName" method=post>
          <tr>
            <td height="22">
			<input name="id" value="4" type="hidden">
			名称&nbsp;
			<input name="name" value="<%=StrUtil.getNullString(home.getProperty("blocks", "id", "name4"))%>">
			<input name="submit23" type="submit" class="btn" value="确 定"></td>
          </tr>
		</form>		
      </table>
      <br>
      <table width="73%" align="center" class="frame_gray">
        <form id=form4 name=form4 action="?op=setFlashImages" method=post>
          <tr>
            <td height="22" class="thead"><strong><a name="flash">Flash图片设置</a></strong></td>
            <td height="22" class="thead"><strong>地址</strong></td>
            <td height="22" class="thead"><strong>链接</strong></td>
            <td height="22" class="thead"><strong>文字</strong></td>
          </tr>
          <tr>
            <td height="22">图片1              </td>
            <td><input name="url1" value="<%=StrUtil.getNullStr(home.getProperty("flash", "id", "1", "url"))%>">
            <input name="button" type="button" onclick="SelectImage(form4.url1)" value="选择" /></td>
            <td><input name="link1" value="<%=StrUtil.getNullStr(home.getProperty("flash", "id", "1", "link"))%>"></td>
            <td><input name="text1" value="<%=StrUtil.getNullStr(home.getProperty("flash", "id", "1", "text"))%>"></td>
          </tr>
          <tr>
            <td height="22">图片2              </td>
            <td><input name="url2" value="<%=StrUtil.getNullStr(home.getProperty("flash", "id", "2", "url"))%>">
            <input name="button3" type="button" onclick="SelectImage(form4.url2)" value="选择" /></td>
            <td><input name="link2" value="<%=StrUtil.getNullStr(home.getProperty("flash", "id", "2", "link"))%>"></td>
            <td><input name="text2" value="<%=StrUtil.getNullStr(home.getProperty("flash", "id", "2", "text"))%>"></td>
          </tr>
          <tr>
            <td height="22">图片3              </td>
            <td><input name="url3" value="<%=StrUtil.getNullStr(home.getProperty("flash", "id", "3", "url"))%>">
            <input name="button4" type="button" onclick="SelectImage(form4.url3)" value="选择" /></td>
            <td><input name="link3" value="<%=StrUtil.getNullStr(home.getProperty("flash", "id", "3", "link"))%>"></td>
            <td><input name="text3" value="<%=StrUtil.getNullStr(home.getProperty("flash", "id", "3", "text"))%>"></td>
          </tr>
          <tr>
            <td height="22">图片4              </td>
            <td><input name="url4" value="<%=StrUtil.getNullStr(home.getProperty("flash", "id", "4", "url"))%>">
            <input name="button5" type="button" onclick="SelectImage(form4.url4)" value="选择" /></td>
            <td><input name="link4" value="<%=StrUtil.getNullStr(home.getProperty("flash", "id", "4", "link"))%>"></td>
            <td><input name="text4" value="<%=StrUtil.getNullStr(home.getProperty("flash", "id", "4", "text"))%>"></td>
          </tr>
          <tr>
            <td width="17%" height="22">图片5  			  </td>
            <td width="32%"><input name="url5" value="<%=StrUtil.getNullStr(home.getProperty("flash", "id", "5", "url"))%>">
            <input name="button6" type="button" onclick="SelectImage(form4.url5)" value="选择" /></td>
            <td width="28%"><input name="link5" value="<%=StrUtil.getNullStr(home.getProperty("flash", "id", "5", "link"))%>"></td>
            <td width="23%"><input name="text5" value="<%=StrUtil.getNullStr(home.getProperty("flash", "id", "5", "text"))%>"></td>
          </tr>
          <tr>
            <td height="22" colspan="4" align="center"><input name="submit32" type="submit" style="border:1pt solid #636563;font-size:9pt; LINE-HEIGHT: normal;HEIGHT: 18px;" value=" 确 定 "></td>
          </tr>
        </form>
      </table>
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
  