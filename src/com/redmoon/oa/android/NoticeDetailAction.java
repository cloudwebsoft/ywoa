package com.redmoon.oa.android;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.opensymphony.xwork2.ActionSupport;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.notice.NoticeAttachmentDb;
import com.redmoon.oa.notice.NoticeDb;
import com.redmoon.oa.notice.NoticeReplyDb;
import com.redmoon.oa.notice.NoticeReplyMgr;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;

import cn.js.fan.util.*;

public class NoticeDetailAction extends ActionSupport {
	private String result = "";
	private String skey = "";
	private final static int  READ_NOTICE = 1;
	private final static int  NO_READ_NOTICE = 0;
	private int id;

	public int getId() {
		return id;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	public String execute() {
		
		JSONObject json = new JSONObject();
		com.redmoon.oa.android.Privilege privilege = new com.redmoon.oa.android.Privilege();
		boolean re = privilege.Auth(getSkey());
		try {
			if (re) {
				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			}
			NoticeDb ntd = new NoticeDb();
			ntd = ntd.getNoticeDb(id);
			String userName = privilege.getUserName(skey);
			String usersKnow = ntd.getUsersKnow();
			if (usersKnow.equals("")) {
				usersKnow = privilege.getUserName(skey);
			} else {
				// 检查用户是否已被记录
				String[] ary = usersKnow.split(",");
				boolean isFound = false;
				int len = ary.length;
			
				if (ary != null && len > 0) {
					for (String name : ary) {
						if (userName.equals(name)) {
							isFound = true;
							break;
						}
					}
				}
				if (!isFound) {
					usersKnow += "," + userName;
				}
			}
			ntd.setUsersKnow(usersKnow);
			NoticeReplyDb nrdb = new NoticeReplyDb();
			NoticeReplyMgr replyMgr = new NoticeReplyMgr();
			if(!replyMgr.readStatusByReply(id, privilege.getUserName(skey))){
				nrdb.setIsReaded("1");
				nrdb.setReadTime(new java.util.Date());
				nrdb.setNoticeid((long) id);
				nrdb.setUsername(privilege.getUserName(skey));
				nrdb.saveStatus();
			}
			boolean flag = ntd.save();
			//更新oa_message表中的通知公告 状态位的问题
			MessageDb messageDb = new MessageDb();
			messageDb.setCommonUserReaded(userName, (long)id , MessageDb.MESSAGE_SYSTEM_NOTICE_TYPE);
			UserDb user = new UserDb();
			HttpServletRequest request = ServletActionContext.getRequest();
			if (flag) {
				json.put("res", "0");
				json.put("msg", "操作成功");
				json.put("id", String.valueOf(getId()));
				json.put("title", ntd.getTitle());
				json.put("sender", user.getUserDb(ntd.getUserName())
						.getRealName());
				json.put("content", StringEscapeUtils.unescapeHtml(StrUtil.getAbstract(request, ntd
						.getContent(), 20000, "")));
				json.put("expirydate", DateUtil.format(ntd.getEndDate(),
						"yyyy-MM-dd HH:mm"));
				json.put("createdate", DateUtil.format(ntd.getCreateDate(),
						"yyyy-MM-dd HH:mm"));
				json.put("isBold", "" + ntd.isBold());
				// 文件柜文件
				JSONArray files = new JSONArray();
				Iterator riAtt = ntd.getAttachs().iterator();
				String downPath = "";
				while (riAtt.hasNext()) {
					NoticeAttachmentDb nad = (NoticeAttachmentDb) riAtt.next();
					JSONObject file = new JSONObject();
					file.put("name", nad.getName());
					downPath = "public/android/notice_getfile.jsp?attId="
							+ nad.getId();
					file.put("url", downPath);
					files.put(file);
				}
				json.put("files", files);
				Privilege prv = new Privilege();
				json.put("canSeeUsers", false);
				JSONArray vusers = new JSONArray();
				json.put("vusers", vusers);
				/*if(ntd.getIsShow()== 1){
					if (prv.isUserPrivValid(request, "notice") || prv.isUserPrivValid(request, "notice.dept")) {
						// 已查看人数
						json.put("canSeeUsers", true);
						JSONArray vusers = new JSONArray();
						NoticeReplyDb npd = new NoticeReplyDb();
						Vector vu = npd.getNoticeReadOrNot((long)id,READ_NOTICE);
						Iterator riVuser = vu.iterator();
						while (riVuser.hasNext()) {
							String userName1 = (String) riVuser.next();
							JSONObject vuser = new JSONObject();
							UserDb userDb = new UserDb(userName1);
							vuser.put("id", String.valueOf(userDb.getId()));
							vuser.put("username", userDb.getRealName());
							vusers.put(vuser);
						}
						json.put("vusers", vusers);

						// 未查看人数
						JSONArray nusers = new JSONArray();
						vu = npd.getNoticeReadOrNot((long)id,NO_READ_NOTICE);
						Iterator riNuser = vu.iterator();
						while (riNuser.hasNext()) {
							String userName1 = (String) riNuser.next();
							user = new UserDb(userName1);
							JSONObject nuser = new JSONObject();
							nuser.put("id", String.valueOf(user.getId()));
							nuser.put("username", user.getRealName());
							nusers.put(nuser);
						}
						json.put("nusers", nusers);
					}
				}*/
			
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(NoticeEditAction.class).error(e.getMessage());
		}
		setResult(json.toString());
		return "SUCCESS";
	}
}
