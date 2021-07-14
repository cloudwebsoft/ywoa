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
	
	
	
	String sql = "select id,sender,subject,mydate,is_readed,email_addr,msg_level from email where email_addr=" + StrUtil.sqlstr(email) + " and msg_type=" + MailMsgDb.TYPE_INBOX+" order by mydate desc";

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
	  			int msgLevel = rr.getInt("msg_level");
	  			if(sendTime == null){
	  				sendTime = new Date();
	  			}
	  			
	  			SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				String str ="<tr id=\""+rr.getInt("id")+"\" lang=\""+rr.getInt("is_readed")+"\">"
				+"<td><div class=\"cth\" style=\"width:22px\"><input type=\"checkbox\" value=\""+rr.getInt("id")+"\"></input></div></td>";
				
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
				
				str += "<td><div style=\"width:220px;text-align:left;\">"+sender+"</div></td><td><div style=\"width:500px;text-align:left;\">"+subject+"</a></div></td><td><div style=\"width:180px;text-align:left;\">"+sd.format(sendTime)+"</div></td></tr>";	 
					
	  			strToghter += str;
	  		}
	  		out.print(strToghter);
	  	%>
		
		

