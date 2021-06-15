package com.redmoon.oa.notice;

import com.cloudwebsoft.framework.db.JdbcTemplate;

public class NoticeAttachmentMgr {
    public boolean delAttachment(long noticeId){
    	String sql = "delete from oa_notice_attach where notice_id=?";
    	JdbcTemplate jt = new JdbcTemplate();
    	try {
			return jt.executeUpdate(sql, new Object[]{noticeId})>0?true:false;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
    	return false;
    }
}