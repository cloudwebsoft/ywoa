package com.cloudweb.oa.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.SQLFilter;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.person.UserDb;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.notice.NoticeAttachmentDb;
import com.redmoon.oa.notice.NoticeDb;
import com.redmoon.oa.notice.NoticeMgr;
import com.redmoon.oa.notice.NoticeReplyDb;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

@Controller
@RequestMapping
public class NoticeController {
	@Autowired  
	private HttpServletRequest request;

	@ResponseBody
	@RequestMapping(value = "/public/notice/add", method = RequestMethod.POST, produces={"text/html;","application/json;charset=UTF-8;"})
	public String add(@RequestParam String title, @RequestParam String content, 
			@RequestParam(defaultValue="0") int isReply, 
			@RequestParam(defaultValue="0") int isForcedResponse, 
			@RequestParam(defaultValue="0") int isall, 
			@RequestParam String beginDate, 
			String endDate, 
			@RequestParam String skey,
			@RequestParam(defaultValue="0") int isToMobile,
			@RequestParam(value = "upload", required = false) MultipartFile[] files,
			String persons) {
		Privilege pvg = new Privilege();
		boolean re = pvg.auth(request);
		JSONObject json = new JSONObject();
		if (!re) { 
			try {
				json.put("ret", "0");
				json.put("msg", "权限非法！");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return json.toString();
		}

		String userName = pvg.getUserName();
        DeptUserDb dud = new DeptUserDb();
        String unitCode = dud.getUnitOfUser(userName).getCode();
		java.util.Date bd = DateUtil.parse(beginDate, "yyyy-MM-dd");
		java.util.Date ed = DateUtil.parse(endDate, "yyyy-MM-dd");
		try {
			NoticeDb nd = new NoticeDb();
			nd.setUserName(userName);
			nd.setUnitCode(unitCode);
			nd.setTitle(title);
			nd.setContent(content);
			nd.setIs_forced_response(isForcedResponse);
			nd.setIs_reply(isReply);
			nd.setIsall(isall);
			nd.setUserList(persons);
			nd.setBeginDate(bd);
			nd.setEndDate(ed);
			re = nd.create();
	        if(re) {
	        	NoticeMgr nm = new NoticeMgr();
	        	re = nm.createNoticeReply(nd, isToMobile==1);
	    		Calendar cal = Calendar.getInstance();
	    		String year = "" + (cal.get(Calendar.YEAR));
	    		String month = "" + (cal.get(Calendar.MONTH) + 1);
	    		String vpath = "upfile/notice/" + year + "/" + month + "/";
	    		NoticeAttachmentDb nad = new NoticeAttachmentDb();
	    		
	    		// 得到的multipartRequest为null
                // MultipartHttpServletRequest multipartRequest = WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class);	                
/*                MultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());
                MultipartHttpServletRequest multipartRequest = resolver.resolveMultipart(request);		    		
                List<MultipartFile> files = multipartRequest.getFiles("upload");*/	
	            // System.out.println(getClass() + " size=" + files.size());
				if(files!=null && files.length>0){  
		            // 循环获取file数组中得文件  
		            for(int i = 0;i<files.length;i++){  
		                MultipartFile file = files[i];  
		                if (!file.isEmpty()) {
		                    byte[] bytes = file.getBytes();
		                    String name = file.getOriginalFilename();
		                    String ext = StrUtil.getFileExt(name);
		                    String diskName = FileUpload.getRandName() + "." + ext;
			    			String filePath = Global.getRealPath() + "/" + vpath + "/" + diskName;

			                File f = new File(filePath);
			                if(!f.getParentFile().exists()){
			                	f.getParentFile().mkdirs();
			                }
			                BufferedOutputStream stream =
			                        new BufferedOutputStream(new FileOutputStream(filePath));
			                stream.write(bytes);
			                stream.close();

			    			nad.setVisualPath(vpath);
			    			nad.setNoticeId(nd.getId());
			    			nad.setName(name);
			    			nad.setDiskName(diskName);
			    			nad.setOrders(i);
			    			nad.setSize(file.getSize());
			    			re = nad.create();
		                }
		            }  
		        }  	   
            }
			
			if (re) { 
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			}
			else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			try {
				json.put("ret", 0);
				json.put("msg", e.getMessage());				
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}				
		} catch (IOException e) {
			try {
				json.put("ret", 0);
				json.put("msg", e.getMessage());				
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}				
		}

		return json.toString();				
	}	
	
	@ResponseBody
	@RequestMapping(value = "/public/notice/del", method = RequestMethod.GET, produces={"text/html;","application/json;charset=UTF-8;"})
	public String del(long id, String skey) {
		Privilege pvg = new Privilege();
		boolean re = pvg.auth(request);
		JSONObject json = new JSONObject();
		if (!re) { 
			try {
				json.put("ret", "0");
				json.put("msg", "权限非法！");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return json.toString();
		}

		// 判断能否删除
		NoticeMgr nm = new NoticeMgr();
        try {
			if (!nm.isNoticeManageable(request, id)) {
				json.put("ret", "0");
				json.put("msg", "权限非法！");	
				return json.toString();
			}
		} catch (ErrMsgException e1) {
			e1.printStackTrace();
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
        
        NoticeDb nd = new NoticeDb();
        nd = nd.getNoticeDb(id);
        re = nd.del();
		try {
			if (re) { 
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			}
			else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	@ResponseBody
	@RequestMapping(value = "/public/notice/edit", method = RequestMethod.POST, produces={"text/html;","application/json;charset=UTF-8;"})
	public String edit(@RequestParam String title, @RequestParam String content,
		@RequestParam(defaultValue="0") int isReply,
		@RequestParam(defaultValue="0") int isForcedResponse, 
		@RequestParam String beginDate, 
		long id,
		String endDate, 
		@RequestParam String skey,
		@RequestParam(value = "upload", required = false) MultipartFile[] files) {
		JSONObject json = new JSONObject();
		Privilege pvg = new Privilege();
		boolean re = pvg.auth(request);
		if (!re) { 
			try {
				json.put("ret", "0");
				json.put("msg", "权限非法！");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return json.toString();
		}
		NoticeDb nd = new NoticeDb();
		nd = nd.getNoticeDb(id);
		nd.setTitle(title);
		nd.setContent(content);
		nd.setBeginDate(DateUtil.parse(beginDate, "yyyy-MM-dd"));
		nd.setEndDate(DateUtil.parse(endDate, "yyyy-MM-dd"));
		nd.setIs_reply(isReply);
		nd.setIs_forced_response(isForcedResponse);
		re = nd.save();
		try {
			if (re) { 
				NoticeMgr nm = new NoticeMgr();
				nm.changeNoticeStatus(id);
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			}
			else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}	
	
	
	@ResponseBody
	@RequestMapping(value = "/public/notice/delAtt", method = RequestMethod.GET, produces={"text/html;","application/json;charset=UTF-8;"})
	public String delAtt(long attId, String skey) {
		Privilege pvg = new Privilege();
		boolean re = pvg.auth(request);
		JSONObject json = new JSONObject();
		if (!re) { 
			try {
				json.put("ret", "0");
				json.put("msg", "权限非法！");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return json.toString();
		}
		
		NoticeDb nda = new NoticeDb();
		NoticeAttachmentDb nad = new NoticeAttachmentDb(attId);
		nda = nda.getNoticeDb(nad.getNoticeId());
		re = nda.delAttach(attId);

		try {
			if (re) { 
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			}
			else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}	
	
	@ResponseBody
	@RequestMapping(value = "/public/notice/addReply", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})
	public String addReply(long id, String content, String skey) {
		Privilege pvg = new Privilege();
		boolean re = pvg.auth(request);
		JSONObject json = new JSONObject();
		if (!re) { 
			try {
				json.put("ret", "0");
				json.put("msg", "权限非法！");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return json.toString();
		}

		NoticeReplyDb noticeReplyDb = new NoticeReplyDb();
		noticeReplyDb.setUsername(pvg.getUserName());
		noticeReplyDb.setNoticeid(id);
		noticeReplyDb.setContent(content);
		try {
			re = noticeReplyDb.save();
		} catch (ErrMsgException e1) {
			e1.printStackTrace();
		} catch (ResKeyException e1) {
			e1.printStackTrace();
		}		
		if(re){
			java.util.Date rDate = new java.util.Date();
			NoticeDb noticeDb = new NoticeDb();
			noticeDb = noticeDb.getNoticeDb(id);
			if(noticeDb.getIs_forced_response() == 1){
				NoticeReplyDb nrdb = new NoticeReplyDb();
				nrdb.setIsReaded("1");
				nrdb.setReadTime(rDate);
				nrdb.setNoticeid(id);
				nrdb.setUsername(pvg.getUserName());
				nrdb.saveStatus();
			}
		}
		try {
			if (re) { 
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			}
			else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	@ResponseBody
	@RequestMapping(value = "/public/notice/list", method = RequestMethod.POST, produces={"text/html;","application/json;charset=UTF-8;"})
	public String list(String skey, String op, String cond, String what, int pagenum, int pagesize) {
		Privilege privilege = new Privilege();
		boolean re = privilege.auth(request);
		JSONObject json = new JSONObject();
		if (!re) {
			try {
				json.put("ret", "0");
				json.put("msg", "权限非法！");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return json.toString();
		}

		try {
			privilege.doLogin(request, skey);
			boolean canAdd = false;

			com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();

			boolean isNoticeAll = pvg.isUserPrivValid(request, "notice");
			boolean isNoticeMgr = pvg.isUserPrivValid(request, "notice.dept");
			if (isNoticeAll|| isNoticeMgr) {
				canAdd = true;
			}
			String userName = privilege.getUserName(skey);
			String unitCode = privilege.getUserUnitCode(skey);
			String curDay = DateUtil.format(new java.util.Date(), "yyyy-MM-dd");
			String sql = "";
			if(isNoticeAll) {
				sql = "select id from oa_notice o where 1=1";
			}else if(isNoticeMgr){
				sql = "select o.id from oa_notice o,oa_notice_reply r where o.id = r.notice_id and r.user_name = "+StrUtil.sqlstr(userName)+" and o.unit_code =" +StrUtil.sqlstr(unitCode);
			}else{
				sql = "select o.id from oa_notice o,oa_notice_reply r where o.id = r.notice_id and r.user_name = "+StrUtil.sqlstr(userName)+" and o.begin_date<=" + SQLFilter.getDateStr(curDay, "yyyy-MM-dd") + " and (o.end_date is null or o.end_date>=" + SQLFilter.getDateStr(curDay, "yyyy-MM-dd") + ")";
			}
			if (op.equals("search")) {
				sql += " and o." + cond + " like "
						+ StrUtil.sqlstr("%" + what + "%") + "";
			}
			sql += " order by id desc ";

			NoticeDb nd = new NoticeDb();
			ListResult lr = nd.listResult(sql, pagenum, pagesize);
			Vector vt = lr.getResult();
			Iterator ri = vt.iterator();
			json.put("res", "0");
			json.put("msg", "操作成功");
			json.put("total", String.valueOf(lr.getTotal()));
			json.put("canAdd", "" + canAdd);
			JSONObject result = new JSONObject();
			result.put("count", String.valueOf(pagesize));
			JSONArray notices = new JSONArray();
			UserDb user = new UserDb();
			while (ri.hasNext()) {
				NoticeDb rr = (NoticeDb) ri.next();
				JSONObject notice = new JSONObject();
				notice.put("id", String.valueOf(rr.getId()));
				notice.put("title", rr.getTitle());
				notice.put("sender", user.getUserDb(rr.getUserName())
						.getRealName());
				notice.put("expirydate", DateUtil.format(rr.getEndDate(),
						"yyyy-MM-dd HH:mm"));
				notice.put("createdate",DateUtil.parseDate(DateUtil.format(rr.getCreateDate(), DateUtil.DATE_TIME_FORMAT)));
				notices.put(notice);
			}

			result.put("notices", notices);
			json.put("result", result);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ErrMsgException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	@ResponseBody
	@RequestMapping(value = "/public/notice/getNotice", method = RequestMethod.GET, produces={"text/html;","application/json;charset=UTF-8;"})
	public String getNotice(long id, String skey) {
		Privilege pvg = new Privilege();
		boolean re = pvg.auth(request);
		JSONObject json = new JSONObject();
		if (!re) {
			try {
				json.put("ret", "0");
				json.put("msg", "权限非法！");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return json.toString();
		}

		try {
			NoticeDb noticeDb = new NoticeDb();
			noticeDb = noticeDb.getNoticeDb(id);
			if(noticeDb !=null && noticeDb.isLoaded()){
				int is_forced_res = noticeDb.getIs_forced_response();
				Privilege privilege = new Privilege();
				boolean isReplyExist = true;
				String uName = privilege.getUserName(skey);
				NoticeReplyDb nnrd = new NoticeReplyDb();
				nnrd.setUsername(uName);
				nnrd.setNoticeid(id);
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
					nrdb.setNoticeid(id);
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
					com.redmoon.oa.pvg.Privilege privilege1 = new com.redmoon.oa.pvg.Privilege();
					if(privilege1.isUserPrivValid(request,"notice") || privilege1.isUserPrivValid(request,"notice.dept")){
						Vector knowsVec = noticeReplyDb.getNoticeReadOrNot(id,1);//已读
						Vector unKnowsVec = noticeReplyDb.getNoticeReadOrNot(id,0);//未读
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
			if (re) {
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			}
			else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
}
