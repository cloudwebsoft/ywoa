package com.redmoon.oa.worklog.service;

import java.util.Date;
import java.util.List;

import com.redmoon.oa.worklog.domain.WorkLog;
import com.redmoon.oa.worklog.domain.WorkLogAttach;
import com.redmoon.oa.worklog.domain.WorkLogExpand;

/**
 * 我的工作汇报接口
 * @author jfy
 * @date Jul 9, 2015
 */
public interface MyWorkManageServices {
	/**
	 * 获取汇报信息
	 * @param wl
	 * @return
	 */
	List<WorkLog> queryWorkLogInfos(WorkLog wl);
	/**
	 * 校验传入时间是否已写汇报
	 * @param date
	 * @return
	 */
	boolean isPrepared(Date date, int type);
	/**
	 * 创建主表记录
	 * @param wl
	 * @return
	 */
	boolean createMyWorkLog(WorkLog wl);
	/**
	 * 创建附件信息
	 * @param wla
	 * @return
	 */
	boolean createMyWorkAttach(WorkLogAttach wla);
	/**
	 * 创建评论点赞信息
	 * @param wle
	 * @return
	 */
	boolean createMyWorkExpand(WorkLogExpand wle);
	/**
	 * 修改后保存汇报信息
	 * @param wl
	 * @return
	 */
	boolean saveMyWorkLog(WorkLog wl);
	/**
	 * 删除附件
	 * @param attachId
	 * @return
	 */
	boolean delAttach(int attachId);
	/**
	 * 根据worklogid获取附件信息
	 * @param workLogId
	 * @return
	 */
	List<WorkLogAttach> getWorkLogAttachesByWorkLogId(int workLogId);
	/**
	 * 根据ID获取回报信息
	 * @param id
	 * @return
	 */
	WorkLog getWorkLogInfoById(int id);
	/**
	 * 点赞
	 * @param workLogId
	 * @return
	 */
	boolean savePraise(int workLogId);
	/**
	 * 新增评论
	 * @param wle
	 * @return
	 */
	boolean addReview(WorkLogExpand wle);
	/**
	 * 获取传入日期多少个工作日之前的所有日期
	 * @param date
	 * @param dayNums
	 * @return
	 */
	List<Date> getWorkDaysFromDb(java.util.Date date,
			int dayNums);
	/**
	 * 获取周报信息
	 * @param wl
	 * @return
	 */
	List<WorkLog> queryWeekLogInfos(WorkLog wl);
	/**
	 * 判断当前日期下是否已存在
	 * @param date
	 * @return
	 */
	boolean isDateExit(String userName , String date);
	
	/**
	 * 点赞或者取消点赞
	 * @Description: 
	 * @param wle
	 * @param praiseType
	 * @return
	 */
	boolean savePraiseOrCancel(WorkLogExpand wle ,int praiseType);
	/**
	 * 获得日报 扩展属性的列表
	 * @Description: 
	 * @param workLogId
	 * @param type
	 * @return
	 */
	List<WorkLogExpand> getCommonExpands(int workLogId,int type);
	
}
