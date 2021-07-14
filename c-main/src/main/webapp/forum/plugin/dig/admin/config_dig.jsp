<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="org.jdom.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.dig.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<%@ include file="../../../inc/nocache.jsp" %>
<meta http-equiv="expires" content="wed, 26 Feb 1997 08:21:57 GMT">
<title></title>
<LINK href="../../../admin/default.css" type=text/css rel=stylesheet>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
body {
	margin-left: 0px;
	margin-top: 0px;
}
-->
</style><body  bgcolor="#FFFFFF">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

DigConfig dc = DigConfig.getInstance();

String op = ParamUtil.get(request, "op");
if (op.equals("digSelf")) {
	String canDigSelf = ParamUtil.get(request, "canDigSelf");
	dc.setProperty("canDigSelf", canDigSelf);
	out.print(StrUtil.Alert_Redirect("操作成功!", "config_dig.jsp"));
	return;
}

if (op.equals("dig")) {
	String pay = ParamUtil.get(request, "pay");
	String reward = ParamUtil.get(request, "reward");
	String scoreCode = ParamUtil.get(request, "scoreCode");
	
	if (StrUtil.isDouble(pay) && StrUtil.isDouble(reward))
		;
	else {
        out.print(StrUtil.Alert_Back("消耗和积分必须为整数或者小数!"));
		return;		
	}
	dc.setProperty("dig", "type", scoreCode, "pay", pay);
	dc.setProperty("dig", "type", scoreCode, "reward", reward);
	dc.cfg = null;
	out.print(StrUtil.Alert_Redirect("操作成功!", "config_dig.jsp"));
	return;
}

if (op.equals("undig")) {
	String pay = ParamUtil.get(request, "pay");
	String reward = ParamUtil.get(request, "reward");
	if (StrUtil.isDouble(pay) && StrUtil.isDouble(reward))
		;
	else {
        out.print(StrUtil.Alert_Back("消耗和积分必须为整数或者小数!"));
		return;		
	}	
	String scoreCode = ParamUtil.get(request, "scoreCode");
	dc.setProperty("undig", "type", scoreCode, "pay", pay);
	dc.setProperty("undig", "type", scoreCode, "reward", reward);
	dc.cfg = null;	
	out.print(StrUtil.Alert_Redirect("操作成功!", "config_dig.jsp"));
	return;
}

if (op.equals("delDig")) {
	String scoreCode = ParamUtil.get(request, "scoreCode");
  	Element root = dc.getRoot();
	Iterator ir = root.getChild("dig").getChildren().iterator();
	while (ir.hasNext()) {
        Element child = (Element) ir.next();
        String type = child.getAttributeValue("type");
        if (type.equals(scoreCode)) {
            root.getChild("dig").removeContent(child);
            dc.writemodify();
            break;
        }	
	}
	out.print(StrUtil.Alert_Redirect("操作成功!", "config_dig.jsp"));
	return;	
}

if (op.equals("delUndig")) {
	String scoreCode = ParamUtil.get(request, "scoreCode");
  	Element root = dc.getRoot();
	Iterator ir = root.getChild("undig").getChildren().iterator();
	while (ir.hasNext()) {
        Element child = (Element) ir.next();
        String type = child.getAttributeValue("type");
        if (type.equals(scoreCode)) {
            root.getChild("undig").removeContent(child);
            dc.writemodify();
            break;
        }	
	}
	
	out.print(StrUtil.Alert_Redirect("操作成功!", "config_dig.jsp"));
	return;	
}

if (op.equals("digAdd")) {
	String scoreCode = ParamUtil.get(request, "scoreCode");
  	Element root = dc.getRoot();	
	Iterator ir = root.getChild("dig").getChildren().iterator();
	while (ir.hasNext()) {
        Element child = (Element) ir.next();
        String type = child.getAttributeValue("type");
        if (type.equals(scoreCode)) {
            out.print(StrUtil.Alert_Back("该积分种类已存在!"));
			return;
        }	
	}
	
	String pay = ParamUtil.get(request, "pay");
	String reward = ParamUtil.get(request, "reward");
	if (StrUtil.isDouble(pay) && StrUtil.isDouble(reward))
		;
	else {
        out.print(StrUtil.Alert_Back("消耗和积分必须为整数或者小数!"));
		return;		
	}	

    Element escore = new Element("score");
    escore.setAttribute(new Attribute("type", scoreCode));
    Element epay = new Element("pay");
    epay.setText(pay);
    escore.addContent(epay);
    Element ereward = new Element("reward");
    ereward.setText(reward);
    escore.addContent(ereward);

    root.getChild("dig").addContent(escore);
	dc.writemodify();
		
	out.print(StrUtil.Alert_Redirect("操作成功！", "config_dig.jsp"));
	return;
}

if (op.equals("undigAdd")) {
	String scoreCode = ParamUtil.get(request, "scoreCode");
  	Element root = dc.getRoot();
	Iterator ir = root.getChild("undig").getChildren().iterator();
	while (ir.hasNext()) {
        Element child = (Element) ir.next();
        String type = child.getAttributeValue("type");
        if (type.equals(scoreCode)) {
            out.print(StrUtil.Alert_Back("该积分种类已存在!"));
			return;
        }	
	}	
	String pay = ParamUtil.get(request, "pay");
	String reward = ParamUtil.get(request, "reward");
	if (StrUtil.isDouble(pay) && StrUtil.isDouble(reward))
		;
	else {
        out.print(StrUtil.Alert_Back("消耗和积分必须为整数或者小数!"));
		return;		
	}
    Element escore = new Element("score");
    escore.setAttribute(new Attribute("type", scoreCode));
    Element epay = new Element("pay");
    epay.setText(pay);
    escore.addContent(epay);
    Element ereward = new Element("reward");
    ereward.setText(reward);
    escore.addContent(ereward);

    root.getChild("undig").addContent(escore);
	dc.writemodify();
		
	out.print(StrUtil.Alert_Redirect("操作成功！", "config_dig.jsp"));
	return;
}

if (op.equals("digRecordSecretLevel")) {
	int digRecordSecretLevel = ParamUtil.getInt(request, "digRecordSecretLevel");
	dc.setProperty("digRecordSecretLevel", "" + digRecordSecretLevel);
	out.print(StrUtil.Alert_Redirect("操作成功!", "config_dig.jsp"));
	return;
}

if (op.equals("digRecordShowInTopicCount")) {
	int digRecordShowInTopicCount = ParamUtil.getInt(request, "digRecordShowInTopicCount");
	dc.setProperty("digRecordShowInTopicCount", "" + digRecordShowInTopicCount);
	out.print(StrUtil.Alert_Redirect("操作成功!", "config_dig.jsp"));
	return;
}
%>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
  <TBODY>
    <TR>
      <TD class=head>掘客配置</TD>
    </TR>
  </TBODY>
</TABLE>
<br>
<table width="80%" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe" style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" >
  <tr style="BACKGROUND-COLOR: #fafafa"> 
    <td width="100%" valign="top" bgcolor="#FFFFFF"><table width="100%" border="0" cellpadding="3" cellspacing="0">
          <tr>
            <td width="19%" class="thead">种类</td>
            <td width="26%" class="thead"><img src="../../../admin/images/tl.gif" align="absMiddle" width="10" height="15">挖掘一次消耗</td>
            <td width="55%" class="thead"><img src="../../../admin/images/tl.gif" align="absMiddle" width="10" height="15">得分</td>
          </tr>
<%
  ScoreMgr sm = new ScoreMgr();		  
  Element root = dc.getRoot();
  Iterator ir = root.getChild("dig").getChildren("score").iterator();
  int k = 0;
  while (ir.hasNext()) {
  	Element e = (Element)ir.next();
	String type = e.getAttributeValue("type");
	ScoreUnit su = sm.getScoreUnit(type);
	String pay = e.getChildText("pay");
	String reward = e.getChildText("reward");
	k++;
%>
	<form name="formDig<%=k%>" action="config_dig.jsp?op=dig" method="post">
            <tr><td><%=su.getName(request)%><input name="scoreCode" value="<%=type%>" type="hidden"></td>
            <td><input name="pay" value="<%=pay%>"></td>
            <td><input name="reward" value="<%=reward%>">
              &nbsp;
              <input name="submit" type="submit" value="确定">
              <input name="submit3" type="button" value="删除" onClick="window.location.href='config_dig.jsp?op=delDig&scoreCode=<%=type%>'"></td></tr>
	</form>
            <%
  }
%>      
<form name="formDigAdd" action="config_dig.jsp?op=digAdd" method="post">
            <tr>
              <td><%
Iterator iterator = sm.getAllScore().iterator();
%>
                <select name="scoreCode">
                  <%	  
while (iterator.hasNext()) {
      ScoreUnit su = (ScoreUnit) iterator.next();
      if (su.isExchange()) {
%>
                  <option value="<%=su.getCode()%>"><%=su.getName(request)%></option>
                  <%}
}%>
                </select></td>
              <td><input name="pay" id="pay"></td>
              <td><input name="reward" id="reward">
              &nbsp;
<input name="submit4" type="submit" value="添加"></td>
            </tr>
			</form>
			</table>
    </td>
  </tr>
</table> 
<br>
<table width="80%" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe" style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" >
  <tr style="BACKGROUND-COLOR: #fafafa">
    <td width="100%" valign="top" bgcolor="#FFFFFF"><table width="100%" border="0" cellpadding="3" cellspacing="0">
      <tr>
        <td width="19%" class="thead">种类</td>
        <td width="26%" class="thead"><img src="../../../admin/images/tl.gif" align="absMiddle" width="10" height="15">埋贴一次消耗</td>
        <td width="55%" class="thead"><img src="../../../admin/images/tl.gif" align="absMiddle" width="10" height="15">得分</td>
      </tr>
      <%
  ir = root.getChild("undig").getChildren("score").iterator();
  while (ir.hasNext()) {
  	Element e = (Element)ir.next();
	String type = e.getAttributeValue("type");
	ScoreUnit su = sm.getScoreUnit(type);
	String pay = e.getChildText("pay");
	String reward = e.getChildText("reward");
	k++;
%>
	<form name="formDig<%=k%>" action="config_dig.jsp?op=undig" method="post">
      <tr>
        <td><%=su.getName(request)%>
          <input name="scoreCode" value="<%=type%>" type="hidden"></td>
        <td><input name="pay" value="<%=pay%>"></td>
        <td><input name="reward" value="<%=reward%>">
          &nbsp;
          <input name="submit2" type="submit" value="确定">
          <input name="submit32" type="button" value="删除" onClick="window.location.href='config_dig.jsp?op=delUndig&scoreCode=<%=type%>'"></td>
      </tr>
	</form>
      <%
  }
%>
<form name="formUndigigAdd" action="config_dig.jsp?op=undigAdd" method="post">
            <tr>
              <td><%
iterator = sm.getAllScore().iterator();
%>
                <select name="scoreCode">
                  <%	  
while (iterator.hasNext()) {
      ScoreUnit su = (ScoreUnit) iterator.next();
      if (su.isExchange()) {
%>
                  <option value="<%=su.getCode()%>"><%=su.getName(request)%></option>
                  <%}
}%>
                </select></td>
              <td><input name="pay" id="pay"></td>
              <td><input name="reward" id="reward">
              &nbsp;
<input name="submit4" type="submit" value="添加"></td>
            </tr>
			</form>
    </table></td>
  </tr>
</table>
<br>
<table width="80%" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe" style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" >
  <tr style="BACKGROUND-COLOR: #fafafa">
    <td width="100%" valign="top" bgcolor="#FFFFFF"><table width="100%" border="0" cellpadding="3" cellspacing="0">
      <tr>
        <td colspan="3" class="thead">其它</td>
        </tr>
		<form name="formDigSelf" action="config_dig.jsp?op=digSelf" method="post">
      <tr>
        <td width="32%">是否允许挖或埋自己的贴子</td>
        <td width="36%">
		<select name="canDigSelf">
		<option value="true">是</option>
		<option value="false">否</option>
		</select>
		<script>
		formDigSelf.canDigSelf.value = "<%=dc.getProperty("canDigSelf")%>";
		</script>
		&nbsp;&nbsp;</td>
        <td width="32%"><input name="submit5" type="submit" value="确定"></td>
      </tr>
		</form>
		<form name="form_digRecordSecretLevel" action="config_dig.jsp?op=digRecordSecretLevel" method="post">
      <tr>
        <td>可以查看挖掘记录的用户</td>
        <td>
		<select name="digRecordSecretLevel">
		<option value="0">公众可见</option>
		<option value="1">楼主可见</option>
		<option value="2">版主可见</option>
		</select>
		<script>
		form_digRecordSecretLevel.digRecordSecretLevel.value = "<%=dc.getProperty("digRecordSecretLevel")%>";
		</script></td>
        <td><input name="submit52" type="submit" value="确定"></td>
      </tr>
		</form>
		<form name="form_digRecordShowInTopicCount" action="config_dig.jsp?op=digRecordShowInTopicCount" method="post">
      <tr>
        <td>贴子中显示最新挖掘记录条数</td>
        <td>
		<input name="digRecordShowInTopicCount">
		<script>
		form_digRecordShowInTopicCount.digRecordShowInTopicCount.value = "<%=dc.getProperty("digRecordShowInTopicCount")%>";
		</script>		
		</td>
        <td><input name="submit53" type="submit" value="确定"></td>
      </tr>
		</form>
    </table></td>
  </tr>
</table>
<p><br>
  <br>
</p>
<p>
</p>
</body>                                        
</html>                            
  