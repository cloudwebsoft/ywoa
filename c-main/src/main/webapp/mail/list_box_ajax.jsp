<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.emailpop3.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="java.text.SimpleDateFormat"%>
<jsp:useBean id="privilege" scope="page"
	class="com.redmoon.oa.pvg.Privilege" />
<%
	if (!privilege.isUserLogin(request)) {
		out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	String myname = privilege.getUser(request);
	String email = ParamUtil.get(request, "email");
	
	String what = java.net.URLDecoder.decode(request.getParameter("what"),"utf-8");
	String senderWhat = java.net.URLDecoder.decode(request.getParameter("senderWhat"),"utf-8");
	String receiverWhat = java.net.URLDecoder.decode(request.getParameter("receiverWhat"),"utf-8");
	String subjectWhat = java.net.URLDecoder.decode(request.getParameter("subjectWhat"),"utf-8");
	String contentWhat = java.net.URLDecoder.decode(request.getParameter("contentWhat"),"utf-8");
	String fileNameWhat = java.net.URLDecoder.decode(request.getParameter("fileNameWhat"),"utf-8");
	
	/**String what = ParamUtil.get(request, "what");
	String senderWhat = ParamUtil.get(request,"senderWhat");
	String receiverWhat = ParamUtil.get(request,"receiverWhat");
	String subjectWhat = ParamUtil.get(request,"subjectWhat");
	String contentWhat = ParamUtil.get(request,"contentWhat");
	String fileNameWhat = ParamUtil.get(request,"fileNameWhat");*/
	int subMneu = ParamUtil.getInt(request,"subMenu",-1);
	int subMenuButton = ParamUtil.getInt(request,"subMenuButton",-1);
	
	String sql = "select distinct a.id,a.sender,a.subject,a.mydate,a.is_readed,a.email_addr,a.msg_level from email a left join email_attach b on a.id = b.emailId where  a.email_addr=" + StrUtil.sqlstr(email) + " and a.msg_type=" + MailMsgDb.TYPE_SENDED;

	if(!what.equals("") && !what.equals("搜索邮件")){
		sql += " and (a.sender like " + StrUtil.sqlstr("%" + what + "%")+" or a.receiver like "+ StrUtil.sqlstr("%" + what + "%")+" or a.subject like "+ StrUtil.sqlstr("%" + what + "%")+
		" or a.content like "+ StrUtil.sqlstr("%" + what + "%")+" or b.name like "+ StrUtil.sqlstr("%" + what + "%");
	}
	
	if(!senderWhat.equals("")){
		sql += " and (a.sender like " + StrUtil.sqlstr("%" + senderWhat + "%");
	}
	if(!receiverWhat.equals("")){
		sql += " and (a.receiver like " + StrUtil.sqlstr("%" + receiverWhat + "%");
	}
	if(!subjectWhat.equals("")){
		sql += " and (a.subject like " + StrUtil.sqlstr("%" + subjectWhat + "%");
	}
	if(!contentWhat.equals("")){
		sql += " and (a.content like " + StrUtil.sqlstr("%" + contentWhat + "%");
	}
	if(!fileNameWhat.equals("")){
		sql += " and (b.name like " + StrUtil.sqlstr("%" + fileNameWhat + "%");
	}
	
	if((!what.equals("") && !what.equals("搜索邮件")) || !senderWhat.equals("") || !receiverWhat.equals("") || !subjectWhat.equals("") || !contentWhat.equals("") || !fileNameWhat.equals("")){
		sql += ") ";
	}
	
	sql += " order by a.mydate desc";
	String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
	int pagesize = ParamUtil.getInt(request, "pageSize", 20);
	if (strcurpage.equals(""))
		strcurpage = "1";
	if (!StrUtil.isNumeric(strcurpage)) {
		out.print(SkinUtil.makeErrMsg(request, "标识非法！"));
		return;
	}
	
	int curpage = Integer.parseInt(strcurpage);
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = jt.executeQuery(sql, Integer.parseInt(strcurpage), pagesize);
	ResultRecord rr = null;

	long total = jt.getTotal();
	Paginator paginator = new Paginator(request, total, pagesize);
	//设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if (totalpages==0) {
		curpage = 1;
		totalpages = 1;
	}
	
%>
	
	
		<%
			String strToghter = "";
		
	  		while(ri.hasNext()){
	  			rr = (ResultRecord)ri.next(); 
	  			String sender = rr.getString("sender");
	  			String subject = rr.getString("subject");
	  			Date sendTime = rr.getDate("mydate");
	  			SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	  			int msgLevel = rr.getInt("msg_level");
	  			
	  			if(subject.equals("")){
	  				subject = "(无主题)";
	  			}
	  			
	  			String str ="<tr id=\""+rr.getInt("id")+"\" lang=\""+rr.getInt("is_readed")+"\">";
	  						str += "<td><div class=\"cth\" style=\"width:22px\"><input type=\"checkbox\" value=\""+rr.getInt("id")+"\"></input></div></td>";
	  						
	  						if(rr.getInt("is_readed") == 1){
	  							str += "<td><div style=\"width:30px;text-align:left;\" title=\"已读\"><img src=\"images/readed.png\"/>";
	  							if(msgLevel == 1){
										str+= "<div title=\"紧急\" style=\"display:inline\"><font color =\"red\">!</font></div>";
									}
	  							str += "</div></td>";
	  						}else{
	  							str += "<td><div style=\"width:30px;text-align:left;\" title=\"未读\"><img src=\"images/no-readed.png\"/>";
	  							if(msgLevel == 1){
										str+= "<div title=\"紧急\" style=\"display:inline\"><font color =\"red\">!</font></div>";
									}
	  							
	  							str += "</div></td>";
	  						}
	  						
	  						str +="<td><div style=\"width:220px;text-align:left;\"><a href=\"mail_show.jsp?id="+rr.getInt("id")+"&emailAddr="+rr.getString("email_addr")+"&subMenu="+subMneu+"&subMenuButton="+subMenuButton+"&box="+MailMsgDb.TYPE_SENDED+"\">"+sender+"</a></div></td>";
	  						str +="<td><div style=\"width:500px;text-align:left;\"><a href=\"mail_show.jsp?id="+rr.getInt("id")+"&emailAddr="+rr.getString("email_addr")+"&subMenu="+subMneu+"&subMenuButton="+subMenuButton+"&box="+MailMsgDb.TYPE_SENDED+"\">"+subject+"</a></div></td>";
	  						str +="<td><div style=\"width:180px;text-align:left;\">"+sd.format(sendTime)+"</div><input type=\"hidden\" name=\"hiddenTotal\" id=\"hiddenTotal\" value=\""+total+"\"/></td></tr>";	  

	  			strToghter += str;
	  		}	
	  		out.print(strToghter);
	  		
	  	%>
		
		

