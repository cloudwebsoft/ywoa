<%@ page contentType="text/html;charset=utf-8" %>
<%@ include file="../inc/nocache.jsp"%>
<%@ page import="com.redmoon.oa.message.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="Msg" scope="page" class="com.redmoon.oa.message.MessageMgr"/>
<%
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String name = privilege.getUser(request);
MessageDb md = new MessageDb();
int pagesize = 10;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

String sql = "select id from oa_message where sender="+StrUtil.sqlstr(name)+" and box="+ MessageDb.DRAFT +" and is_sender_dustbin=0 order by rq desc";

int total = md.getObjectCount(sql);
paginator.init(total, pagesize);
//设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
  curpage = 1;
  totalpages = 1;
}

int id,type;
String title="",sender="",receiver="",rq="";
String bg = "";
int i = 0;
Iterator ir = md.list(sql, (curpage-1)*pagesize, curpage*pagesize-1).iterator();

//信息删除
String op = ParamUtil.get(request, "op");
int box = ParamUtil.getInt(request, "box", -1);
JSONObject json = new JSONObject();
boolean isSuccess = false;
if (op.equals("del")) {
	try {
		isSuccess = Msg.delMsgBySenderDustbin(request);
	}
	catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "消息删除失败："+e.getMessage()));
		return;
	}
	String querystr = "CPages=" + curpage + "&pagesize=" + pagesize;
	if(isSuccess){
		json.put("ret","1");
		json.put("url",querystr);
		json.put("msg","删除成功!");
	}else{
		json.put("ret","0");
		json.put("url","");
		json.put("msg","删除失败!");
	}
	out.print(json);
	return;
}


%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>消息中心</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/message/message.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
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
</head>
<body>
<div class="message_content">
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="message_tdStyle_1">草稿箱</td>
    </tr>
  </tbody>
</table>
 <table width="98%" align="center"">
 	<tr>
      <td class="message_btnbox" colspan="2" >
	      	<!-- <img src="../skin/bluethink/images/message/message_back.png" onclick="location.href='javascript:history.go(-1)'" /> -->
      		<img src="../skin/bluethink/images/message/message_furbish.png" onclick="window.location.reload()" />
      		<img src="../skin/bluethink/images/message/message_close.png"   onclick="doDel()"/>
	      	<img src="../skin/bluethink/images/message/message_transmit.png"   onclick="doTransmit()"/>
      	</td>
    </tr>
  <tr>
 </table>
<table width="98%" border="0" cellpadding="0" cellspacing="0" align="center">
  <tr>
    <td align="right" class="grey5">共 <b><%=paginator.getTotal() %></b> 条　每页<b><%=paginator.getPageSize() %></b> 条　<b><%=curpage %>/<%=totalpages %></b></td>
  </tr>
</table>
<form name="form1" method="post" action="msg_do.jsp">
  <table width="98%" align="center" class="message_tabStyle_1">
    <tr class="message_tabStyle_1_tr">
      <td width="4%" align="center" ><input name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" /></td>
      <td  width="28%" align="center">标题</td>
      <td  width="15%" align="center">收件人</td>
      <td  width="17%" align="center">创建时间</td>
      <td  width="7%" align="center">定时</td>
      <td  width="18%" align="center">定时发送时间</td>
      <td width="11%"" align="center">操作<input type="hidden" name="box" value="<%=MessageDb.DRAFT%>" /></td>
    </tr>
    <%
UserMgr um = new UserMgr();	
while (ir.hasNext()) {
 	      md = (MessageDb)ir.next(); 
		  i++;
		  id = md.getId();
		  title = md.getTitle();
		  String title1 = md.getTitle();
		  title = md.getTitle();
		  if(title.length()>30){
		  	 title = title.substring(0,30)+"...";
		  }
		  sender = md.getSender();
		  receiver = md.getReceiver();
		  rq = md.getRq();
		  type = md.getType();
		  int isSent = md.getIsSent();
		  
		  String receiversAll = md.getReceiversAll();
		  String[] ary = StrUtil.split(receiversAll, ",");
		  String realNames = "";
		  for (int k=0; k<ary.length; k++) {
		  	UserDb user = um.getUserDb(ary[k]);
			if (!user.isLoaded())
				continue;
		  	if (realNames.equals(""))
				realNames = user.getRealName();
			else
				realNames += "," + user.getRealName();
		  }		  
		 %>
    <tr>
      <td align="center" class="message_line"><input type="checkbox" name="ids" value="<%=id%>"></td>
      <td class="message_line"><a href="draft_edit.jsp?id=<%=id%>" title="<%=title1 %>"><%=title%></a></td>
      <td align="center" class="message_line"><%=realNames%></td>
      <td align="center" class="message_line">
	  <%=rq%>
	  </td>
      <td align="center" class="message_line"><%=isSent==0?"是":"否"%></td>
      <td align="center" class="message_line"><%=isSent==0?md.getSendTime():"-"%></td>
      <td align="center" class="message_line"><a href="draft_edit.jsp?id=<%=id%>">编辑</a></td>
    </tr>
    <%}%>
  </table>
</form>
<% if(paginator.getTotal()>0){ %>
  <table width="98%" border="0" cellspacing="0" cellpadding="0" align="center">
  <tr>
    <td align="right">
        <%
	  String querystr = "";
 	  out.print(paginator.getCurPageBlock("listdraft.jsp?"+querystr));
	  %>
</td>
  </tr>
</table>
<%}%>
</div>
</body>

<script>
	function doDel(){
		var ids = getCheckboxValue("ids");
		if(ids == ''){
			jAlert("请选择消息!","提示");
			return;
		}
		jConfirm("您确定要删除么？","提示",function(r){
			if(!r){return;}
			else{
				$.ajax({
					type: "post",
					url: "listdraft.jsp",
					data : {
						op: "del",
						ids: ids
			        },
					dataType: "html",
					beforeSend: function(XMLHttpRequest){
						//ShowLoading();
					},
					success: function(data, status){
						data = $.parseJSON(data);
						jAlert(data.msg,"提示");
						if(data.ret == "1"){
							window.location.href = "listdraft.jsp?"+data.url;
						}
					},
					complete: function(XMLHttpRequest, status){
						//HideLoading();
					},
					error: function(XMLHttpRequest, textStatus){
						// 请求出错处理
						jAlert(XMLHttpRequest.responseText,"提示");
					}
				});
			}
		})
	}
	function doTransmit(){
		var id = getCheckboxValue("ids");
		if(id.indexOf(",")>=0){
			jAlert("只能单个转发","提示");
			return;
		}
		if(id == ''){
			jAlert("请选择消息!","提示");
			return;
		}
		parent.rightFrame.location.href='transmit.jsp?id='+id;
}
</script>
</html>
