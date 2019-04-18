<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %><%@page import="cn.js.fan.util.ParamUtil"%><%@page import="com.redmoon.oa.notice.NoticeDb"%><%@page import="net.sf.json.JSONObject"%><%@page import="cn.js.fan.util.DateUtil"%><%@page import="com.redmoon.oa.person.UserDb"%><%@page import="com.redmoon.oa.pvg.Privilege"%><%@page import="com.redmoon.oa.android.NoticeReadFlagAction"%><%@page import="com.redmoon.oa.notice.NoticeReplyDb"%><%@page import="java.util.Vector"%><%@page import="java.util.Iterator"%>
<%@page import="com.redmoon.weixin.mgr.NoticeDoMgr"%>
<%@page import="com.redmoon.weixin.mgr.FlowDoMgr"%>
<%@page import="cn.js.fan.util.*"%><% 
JSONObject json = new JSONObject();
String op = ParamUtil.get(request,"op");
long notice_id = ParamUtil.getLong(request,"notice_id",0);
if(op.equals("detail")){
	NoticeDb noticeDb = new NoticeDb();
	noticeDb = noticeDb.getNoticeDb(notice_id);
	if(noticeDb !=null && noticeDb.isLoaded()){
		int is_forced_res = noticeDb.getIs_forced_response();
		Privilege privilege = new Privilege();
		boolean isReplyExist = true;
		String uName = privilege.getUser(request);
		NoticeReplyDb nnrd = new NoticeReplyDb();
		nnrd.setUsername(uName);
		nnrd.setNoticeid(notice_id);
		nnrd = nnrd.getReply();
		if(nnrd == null) {
			nnrd = new NoticeReplyDb();
			isReplyExist = false;
		}
		String content = StrUtil.getNullStr(nnrd.getContent());
		String name = StrUtil.getNullStr(nnrd.getUsername());
		boolean isReaded = "1".equals(nnrd.getIsReaded());
		// 当前用户尚未回复内容  // 当前用户不是 通知发布者
		boolean isNotReply = uName.equals(name) && (content.equals("")); // && (!uName.equals(nd.getUserName()));
		if (isReplyExist && !isReaded && is_forced_res == 0){
			java.util.Date rDate = new java.util.Date();
			NoticeReplyDb nrdb = new NoticeReplyDb();
			nrdb.setIsReaded("1");
			nrdb.setReadTime(rDate);
			nrdb.setNoticeid((long) notice_id);
			nrdb.setUsername(uName);
			nrdb.saveStatus();
		}	
	
		JSONObject data = new JSONObject();
		data.put("title",noticeDb.getTitle());
		data.put("content",noticeDb.getContent());
		data.put("createData",DateUtil.format(noticeDb.getCreateDate(),DateUtil.DATE_FORMAT));
		data.put("userRealName",new UserDb(noticeDb.getUserName()).getRealName());
		int isShow = noticeDb.getIsShow();
		if(isShow == 1){
			NoticeReplyDb noticeReplyDb = new NoticeReplyDb();
			if(privilege.isUserPrivValid(request,"notice") || privilege.isUserPrivValid(request,"notice.dept")){
				Vector knowsVec = noticeReplyDb.getNoticeReadOrNot(notice_id,1);//已读
				Vector unKnowsVec = noticeReplyDb.getNoticeReadOrNot(notice_id,0);//未读
				//knowsSb已查看用户
				StringBuilder knowsSb = new StringBuilder();
				//unKnowsSb未查看用户
				StringBuilder unKnowsSb = new StringBuilder();
				if(knowsVec!=null && knowsVec.size()>0){
					Iterator knowIt = knowsVec.iterator();
					while(knowIt.hasNext()){
						String username = (String)knowIt.next();
						UserDb userDb = new UserDb(username);
						if(knowsSb.toString().equals("")){
							knowsSb.append(userDb.getRealName());
						}else{
							knowsSb.append(",").append(userDb.getRealName());
						}
					}
				}
				if(unKnowsVec!=null && unKnowsVec.size()>0){
					Iterator unKnowIt = unKnowsVec.iterator();
					while(unKnowIt.hasNext()){
						String username = (String)unKnowIt.next();
						UserDb userDb = new UserDb(username);
						if(unKnowsSb.toString().equals("")){
							unKnowsSb.append(userDb.getRealName());
						}else{
							unKnowsSb.append(",").append(userDb.getRealName());
						}
					}
				}
				data.put("knows",knowsSb.toString());
				data.put("unKnows",unKnowsSb.toString());
			}
		}
		json.put("data",data);
		json.put("res",0);
	}else{
		json.put("res",-1);
	}
}else if(op.equals("list")){
	NoticeDoMgr noticeDoMgr = new NoticeDoMgr();
	int curPage = ParamUtil.getInt(request,"curPage",1);
	String userId = ParamUtil.get(request,"userId");
	session.setAttribute("OA_NAME",userId);
	Privilege privilege = new Privilege();
	boolean isNoticeAll = privilege.isUserPrivValid(request, "notice");
	boolean isNoticeMgr = privilege.isUserPrivValid(request, "notice.dept");
	json = noticeDoMgr.noticeList(userId,curPage,isNoticeAll,isNoticeMgr);
}
out.print(json.toString());
%>