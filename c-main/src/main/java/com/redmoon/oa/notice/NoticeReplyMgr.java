package com.redmoon.oa.notice;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;

public class NoticeReplyMgr {
	
    FileUpload fileUpload;
    
    String noticeId;
    String isShow;

    public FileUpload doUpload(ServletContext application,
                               HttpServletRequest request) throws
            ErrMsgException {
        fileUpload = new FileUpload();
        fileUpload.setMaxFileSize(Global.FileSize); 

        int ret = 0;
        try {
            ret = fileUpload.doUpload(application, request);
            if (ret != FileUpload.RET_SUCCESS) {
                throw new ErrMsgException("ret=" + ret + " " +
                                          fileUpload.getErrMessage());
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("doUpload:" + e.getMessage());
        }
        return fileUpload;
    }
	
    
    
    public boolean saveReply(ServletContext application,
            HttpServletRequest request) throws ErrMsgException, ResKeyException {
    	
        Privilege pvg = new Privilege();
        if (!pvg.isUserLogin(request))
            throw new ErrMsgException("请先登录!");
        doUpload(application, request);
        String content = StrUtil.getNullStr(fileUpload.getFieldValue("content"));
        
        int id = ParamUtil.getInt(request, "noticeid",-1);
        String username = ParamUtil.get(request, "uName");
       
        NoticeReplyDb nrd = new NoticeReplyDb();
        nrd.setNoticeid(id);
        nrd.setUsername(username);
        nrd.setContent(content);
        nrd.setReplyTime(new Date());
        return nrd.save();
    }
    
    public boolean canReplay(long noticeId,String uName){
    	String sql = "select id from oa_notice_reply where notice_id=? and user_name=? and content is null";
    	JdbcTemplate jt = new JdbcTemplate();
    	ResultIterator ri = null;
    	try {
			ri = jt.executeQuery(sql, new Object[]{noticeId,uName});
			if(ri.hasNext()){
				return true;
			}
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error(e);
		}
    	return false;
    }

    /**
     * 查看用户通知的阅读状态
     * @param noticeId
     * @param userName
     * @return
     */
    public boolean readStatusByReply(long noticeId,String userName){
    	String sql ="select is_readed from oa_notice_reply where notice_id ="+noticeId+" and user_name = "+StrUtil.sqlstr(userName);
    	boolean flag = true;
    	JdbcTemplate jt = null;
    	jt = new JdbcTemplate();
    	ResultIterator ri = null;
    	try {
			ri = jt.executeQuery(sql);
			while(ri.hasNext()){
				ResultRecord rr = (ResultRecord)ri.next();
				int read_status = rr.getInt("is_readed");
				if(read_status == 0){
					flag = false;
				}
			}
		} catch (SQLException e) {
			flag = false;
			LogUtil.getLog(getClass()).error(e.getMessage());
		}
		return flag;
    }
    public boolean delReply(long noticeId){
    	String sql = "delete from oa_notice_reply where notice_id=?";
    	JdbcTemplate jt = new JdbcTemplate();
    	try {
			return jt.executeUpdate(sql, new Object[]{noticeId}) > 0;
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error(e);
		}
    	return false;
    }
    
    public String getUserStr(long nid){
    	String userStr = "";
    	String userName = "";
    	String sql = "select user_name from oa_notice_reply where notice_id=?";
    	JdbcTemplate jt = new JdbcTemplate();
    	ResultIterator ri = null;
    	ResultRecord rd = null;
    	try {
			ri = jt.executeQuery(sql, new Object[]{nid});
			while(ri.hasNext()){
				rd = (ResultRecord)ri.next();
				userName = rd.getString(1);
				if (userName.equals(UserDb.SYSTEM)) {
					continue;
				}
				UserDb user = new UserDb(userName);
				if (user != null && user.isLoaded()) {
					userStr += (userStr.equals("") ? "" : ",")
							+ user.getRealName();
				}
			}
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error(e);
		}
    	return userStr;
    }
}