package com.cloudweb.oa.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.util.WebUtils;

import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.notice.NoticeAttachmentDb;
import com.redmoon.oa.notice.NoticeDb;
import com.redmoon.oa.notice.NoticeMgr;
import com.redmoon.oa.notice.NoticeReplyDb;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

@Controller
@RequestMapping("/public/notice")
public class NoticeController {
	@Autowired  
	private HttpServletRequest request;
	
	@ResponseBody
	@RequestMapping(value = "/add", method = RequestMethod.POST, produces={"text/html;","application/json;charset=UTF-8;"})	
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
				// TODO Auto-generated catch block
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
	@RequestMapping(value = "/del", method = RequestMethod.GET, produces={"text/html;","application/json;charset=UTF-8;"})	
	public String del(long id, String skey) {
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

		// 判断能否删除
		NoticeMgr nm = new NoticeMgr();
        try {
			if (!nm.isNoticeManageable(request, id)) {
				json.put("ret", "0");
				json.put("msg", "权限非法！");	
				return json.toString();
			}
		} catch (ErrMsgException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}
	
	@ResponseBody
	@RequestMapping(value = "/edit", method = RequestMethod.POST, produces={"text/html;","application/json;charset=UTF-8;"})	
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
	@RequestMapping(value = "/delAtt", method = RequestMethod.GET, produces={"text/html;","application/json;charset=UTF-8;"})	
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
	@RequestMapping(value = "/addReply", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})	
	public String addReply(long id, String content, String skey) {
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

		NoticeReplyDb noticeReplyDb = new NoticeReplyDb();
		noticeReplyDb.setUsername(pvg.getUserName());
		noticeReplyDb.setNoticeid(id);
		noticeReplyDb.setContent(content);
		try {
			re = noticeReplyDb.save();
		} catch (ErrMsgException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ResKeyException e1) {
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}		
}
