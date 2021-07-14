package com.redmoon.oa.worklog.service.impl;

import java.util.Date;
import java.util.List;

import com.redmoon.oa.worklog.dao.MyWorkManageDao;
import com.redmoon.oa.worklog.domain.WorkLog;
import com.redmoon.oa.worklog.domain.WorkLogAttach;
import com.redmoon.oa.worklog.domain.WorkLogExpand;
import com.redmoon.oa.worklog.service.MyWorkManageServices;
/**
 * 我的工作汇报接口实现类
 * @author jfy
 * @date Jul 9, 2015
 */
public class MyWorkManageServicesImpl implements MyWorkManageServices {
	
	private static MyWorkManageServicesImpl myWorkManageServicesImpl = null;
	
	private MyWorkManageServicesImpl(){
		
	}
	
	public static MyWorkManageServicesImpl getInstance(){
		if (myWorkManageServicesImpl == null){
			myWorkManageServicesImpl = new MyWorkManageServicesImpl();
		}
		return myWorkManageServicesImpl;
	}
	/**
	 * 获取个人汇报信息
	 * 
	 */
	public List<WorkLog> queryWorkLogInfos(WorkLog wl) {
		MyWorkManageDao mwmd = MyWorkManageDao.getInstance();
		return mwmd.queryWorkLogInfos(wl);
	}
	/**
	 * 根据日期判断该日是否已写汇报
	 */
	@Override
	public boolean isPrepared(Date date, int type) {
		boolean flag = false;
		
		MyWorkManageDao mwmd = MyWorkManageDao.getInstance();
		WorkLog wl = mwmd.getWorkLogByDate(date, type);
		if (wl != null){
			flag = true;
		}
		return flag;
	}

	@Override
	public boolean createMyWorkAttach(WorkLogAttach wla) {
		MyWorkManageDao mwmd = MyWorkManageDao.getInstance();
		return mwmd.createWorkLogAttach(wla);
	}

	@Override
	public boolean createMyWorkExpand(WorkLogExpand wle) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean createMyWorkLog(WorkLog wl) {
		MyWorkManageDao mwmd = MyWorkManageDao.getInstance();
		return mwmd.createWorkLog(wl);
	}

	@Override
	public boolean delAttach(int attachId) {
		MyWorkManageDao mwmd = MyWorkManageDao.getInstance();
		return mwmd.delAttach(attachId);
	}

	@Override
	public boolean saveMyWorkLog(WorkLog wl) {
		MyWorkManageDao mwmd = MyWorkManageDao.getInstance();
		return mwmd.saveMyWorkLog(wl);
	}

	@Override
	public List<WorkLogAttach> getWorkLogAttachesByWorkLogId(int workLogId) {
		MyWorkManageDao mwmd = MyWorkManageDao.getInstance();
		return mwmd.getAttachsByWorkLogId(workLogId);
	}
	
	public WorkLog getWorkLogInfoById(int id){
		MyWorkManageDao mwmd = MyWorkManageDao.getInstance();
		return mwmd.getWorkLogById(id);
	}
	
	public boolean savePraise(int workLogId){
		MyWorkManageDao mwmd = MyWorkManageDao.getInstance();
		return mwmd.savePraise(workLogId);
	}
	
	public boolean addReview(WorkLogExpand wle){
		MyWorkManageDao mwmd = MyWorkManageDao.getInstance();
		return mwmd.addReview(wle);
	}
	public List<Date> getWorkDaysFromDb(java.util.Date date,
			int dayNums) {
		MyWorkManageDao mwmd = MyWorkManageDao.getInstance();
		return mwmd.getWorkDaysFromDb(date, dayNums);
	}
	
	public List<WorkLog> queryWeekLogInfos(WorkLog wl){
		MyWorkManageDao mwmd = MyWorkManageDao.getInstance();
		return mwmd.queryWeekLogInfos(wl);
	}

	public boolean isDateExit(String userName,String date) {
		MyWorkManageDao mwmd = MyWorkManageDao.getInstance();
		return mwmd.isTheDateExit(userName, date);
	}


	@Override
	public boolean savePraiseOrCancel(WorkLogExpand wle, int praiseType) {
		// TODO Auto-generated method stub
		MyWorkManageDao mwmd = MyWorkManageDao.getInstance();
		return mwmd.savePraiseOrCancel(wle, praiseType);
	}

	/**
	 * @Description: 
	 * @param workLogId
	 * @param type
	 * @return
	 */
	@Override
	public List<WorkLogExpand> getCommonExpands(int workLogId, int type) {
		MyWorkManageDao mwmd = MyWorkManageDao.getInstance();
		return mwmd.getCommonExpands(workLogId, type);
	}

	
}
