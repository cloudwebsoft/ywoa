package com.redmoon.oa.worklog.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import cn.js.fan.db.Conn;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.aop.Pointcut.MethodNamePointcut;
import com.cloudwebsoft.framework.aop.base.Advisor;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.message.IMessage;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.message.MobileAfterAdvice;
import com.redmoon.oa.oacalendar.OACalendarDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.prj.PrjConfig;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.worklog.MyWorkManageAction;
import com.redmoon.oa.worklog.WorkLogCache;
import com.redmoon.oa.worklog.WorkLogDb;
import com.redmoon.oa.worklog.WorkLogForModuleMgr;
import com.redmoon.oa.worklog.domain.WorkLog;
import com.redmoon.oa.worklog.domain.WorkLogAttach;
import com.redmoon.oa.worklog.domain.WorkLogExpand;


/**
 * 我的工作汇报Dao
 * @author jfy
 * @date Jul 9, 2015
 */
public class MyWorkManageDao{
	private Logger logger = Logger.getLogger(MyWorkManageDao.class.getName());
	private static MyWorkManageDao myWorkManageDao = null;
	int i = 0;
	private final static int PRAISE = 1;
	private final static int CANCEL_PRAISE = 0;
	private MyWorkManageDao(){
		
	}
	/**
	 * 单例模式
	 * @return
	 */
	public static MyWorkManageDao getInstance(){
		if (myWorkManageDao == null){
			myWorkManageDao = new MyWorkManageDao();
		}
		return myWorkManageDao;
	}
	/**
	 * 查询个人汇报信息
	 * @param wl 个人汇报信息初始化类
	 * @return
	 */
	public List<WorkLog> queryWorkLogInfos(WorkLog wl){
		List<WorkLog> list = new ArrayList<WorkLog>();
		String logType = wl.getType();
		
		String sql = "select * from work_log where username = " + SQLFilter.sqlstr(wl.getUserName());
		sql += " and log_type=" + logType ;

		if(wl.getBeginDate() != null && !"".equals(wl.getBeginDate())){
			sql += " and mydate >=" + SQLFilter.getDateStr(wl.getBeginDate(),DateUtil.DATE_TIME_FORMAT) ;
		}
		if (wl.getEndDate() != null && !"".equals(wl.getEndDate())){
			sql += " and mydate <=" + SQLFilter.getDateStr(wl.getEndDate(),DateUtil.DATE_TIME_FORMAT);
		}
		
		boolean isPrjOrTask = false;
		if (wl.getFormCode() != null && !"".equals(wl.getFormCode()) && wl.getPrjId() != 0 ){
			isPrjOrTask = true;
			// sql += " and id in (select workLog_id from visual_module_worklog where cws_id = "+wl.getPrjId()+" and form_code = "+ StrUtil.sqlstr(wl.getFormCode())+")";
			sql = " select l.* from work_log l,visual_module_worklog v where l.id=v.workLog_id and log_type=" + logType + " and v.cws_id = "+wl.getPrjId()+" and v.form_code = "+ StrUtil.sqlstr(wl.getFormCode());			
		}
		
		String condContent = wl.getCondContent();
		if (condContent != null && !"".equals(condContent)){
			sql += " and content like '%" + condContent + "%'";
		}
		
		if (logType.equals("0")){
			sql += " order by mydate desc";
		}else if(logType.equals("1") || logType.equals("2")){
			sql += " order by log_year, log_item desc";
		}
		
		if(!isPrjOrTask && wl.getCurPage() >= 1 && wl.getPageSize() > 0){
			sql += " limit "+ (wl.getCurPage()-1)*wl.getPageSize() + "," + wl.getCurPage()*wl.getPageSize();
		}
		JdbcTemplate jt = null;
		ResultIterator ri = null;
		ResultRecord rr = null;
		try {
			jt = new JdbcTemplate();
			ri = jt.executeQuery(sql);//获取主表信息
			while(ri.hasNext()){
				WorkLog wlTmp = new WorkLog();
				rr = (ResultRecord)ri.next();
				int id = rr.getInt("id");//主键
				wlTmp.setId(id);
				wlTmp.setContent(rr.getString("content"));
				wlTmp.setMyDate(DateUtil.parseDate(DateUtil.format((Date)rr.get("myDate"),DateUtil.DATE_TIME_FORMAT)));
				wlTmp.setPraiseCount(rr.getInt("praise_count"));
				wlTmp.setReviewCount(rr.getInt("review_count"));
				wlTmp.setLogItem(rr.getInt("log_item"));
				wlTmp.setLogYear(rr.getInt("log_year"));
				wlTmp.setWorkLogAttachs(getAttachsByWorkLogId(id));
				wlTmp.setWorkLogExpands(getExpandsByWorkLogId(id));
				wlTmp.setWorkLogPraises(getCommonExpands(id,Integer.parseInt(WorkLogExpand.PRAISE_TYPE)));
				list.add(wlTmp);
			}
		} catch (SQLException e) {
			logger.error("queryWorkLogInfos is error: " + e.getMessage());
		}catch (ParseException e) {
			logger.error("queryWorkLogInfos parseDate is error:" + e.getMessage());
		}
		
		return list;
	}
	
	/**
	 * 根据汇报ID获取所有的关联附件信息
	 * @param workLogId
	 * @return
	 */
	public List<WorkLogAttach> getAttachsByWorkLogId(int workLogId){
		List<WorkLogAttach> list = null;
		String sql = "select * from work_log_attach where workLogId=" + workLogId;
		JdbcTemplate jt = null;
		ResultIterator ri = null;
		ResultRecord rr = null;
		
		try {
			jt = new JdbcTemplate();
			ri = jt.executeQuery(sql);
			if (ri.size() > 0){
				list = new ArrayList<WorkLogAttach>();
			
				while(ri.hasNext()){
					WorkLogAttach wlaTmp = new WorkLogAttach();
					rr = (ResultRecord)ri.next();
					wlaTmp.setId(rr.getInt("id"));
					wlaTmp.setDiskName(rr.getString("diskname"));
					wlaTmp.setFileSize(rr.getInt("file_size"));
					wlaTmp.setName(rr.getString("name"));
					wlaTmp.setOrders(rr.getInt("orders"));
					wlaTmp.setVisualPath(rr.getString("visualpath"));
					list.add(wlaTmp);
				}
			}
		} catch (SQLException e) {
			logger.error("getAttachsByWorkLogId is error: " + e.getMessage());
		}
		return list;
		
	}
	/**
	 * 通用 日报扩展列表
	 * @Description: 
	 * @param workLogId
	 * @param type
	 * @return
	 */
	public List<WorkLogExpand> getCommonExpands(int workLogId,int type){
		List<WorkLogExpand> list = null;
		String sql = "select id,review,type,user_name,review_time from work_log_expand where work_log_id=" + workLogId + " and type = "+type+" order by review_time desc";
		JdbcTemplate jt = null;
		ResultIterator ri = null;
		ResultRecord rr = null;
		try {
			jt = new JdbcTemplate();
			ri = jt.executeQuery(sql);
			if (ri.size() > 0){
				list = new ArrayList<WorkLogExpand>();
				UserDb ud = null; 
				while(ri.hasNext()){
					WorkLogExpand wleTmp = new WorkLogExpand();
					rr = (ResultRecord)ri.next();
					wleTmp.setId(rr.getInt("id"));
					wleTmp.setReview(rr.getString("review"));
					try {
						wleTmp.setReviewTime(DateUtil.parseDateTime(DateUtil.format((Date)rr.get("review_time"),DateUtil.DATE_TIME_FORMAT)));
					} catch (ParseException e) {
						logger.error("getExpandsByWorkLogId parseDateTime is error:" + e.getMessage());
					}
					wleTmp.setType(rr.getString("type"));
					ud = new UserDb(rr.getString("user_name"));
					wleTmp.setUserName(ud.getRealName());
					wleTmp.setName(ud.getName());
					wleTmp.setWorkLogId(workLogId);
					list.add(wleTmp);
				}
			}
		} catch (SQLException e) {
			logger.error("getExpandsByWorkLogId is error:" + e.getMessage());
		}
		return list;
		
	}
	
	/**
	 * 根据汇报ID获取所有的扩展(评论、点赞)信息
	 * @param workLogId
	 * @return
	 */
	public List<WorkLogExpand> getExpandsByWorkLogId(int workLogId){
		List<WorkLogExpand> list = null;
		list = getCommonExpands(workLogId,Integer.parseInt(WorkLogExpand.REVIEW_TYPE));
		return list;
	}
	/**
	 * 根据传入日期获取汇报信息
	 * @return
	 */
	public WorkLog getWorkLogByDate(Date date, int type){
		WorkLog wl = null;
		String sql = "";
		JdbcTemplate jt = null;
		ResultIterator ri = null;
		ResultRecord rr = null;
		try {
			sql = "select * from work_log where log_type=" + type + " and myDate >= " + SQLFilter.getDateStr(DateUtil.format(date, DateUtil.DATE_FORMAT) + " 00:00:00", DateUtil.DATE_TIME_FORMAT);
			sql += " and myDate <= " + SQLFilter.getDateStr(DateUtil.format(date, DateUtil.DATE_FORMAT) + " 23:59:59", DateUtil.DATE_TIME_FORMAT);
			jt = new JdbcTemplate();
			ri = jt.executeQuery(sql);
			if (ri.size() > 0){
				 wl = new WorkLog();
				 while(ri.hasNext()){
					 rr = (ResultRecord)ri.next();
					 int id = rr.getInt("id");
					 wl.setContent(rr.getString("content"));
					 wl.setId(id);
					 wl.setMyDate(DateUtil.parseDate(DateUtil.format(rr.getDate("mydate"), DateUtil.DATE_TIME_FORMAT)));
					 wl.setReviewCount(rr.getInt("review_count"));
					 wl.setType(rr.getString("log_type"));
					 UserDb ud = new UserDb(rr.getString("userName"));
					 wl.setUserName(ud.getRealName());
					 wl.setWorkLogAttachs(getAttachsByWorkLogId(id));
					 wl.setWorkLogExpands(getExpandsByWorkLogId(id));
					 break;
				 }
				 
			}
			
		} catch (ParseException e) {
			logger.error("getWorkLogByDate is error:" + e.getMessage());
		}catch (SQLException e) {
			logger.error("getWorkLogByDate is error:" + e.getMessage());
		}
		return wl;
	}
	
	/**
	 * 根据传入ID获取汇报信息
	 * @return
	 */
	public WorkLog getWorkLogById(int id){
		WorkLog wl = null;
		String sql = "";
		JdbcTemplate jt = null;
		ResultIterator ri = null;
		ResultRecord rr = null;
		try {
			sql = "select * from work_log where id=" + id ;
			jt = new JdbcTemplate();
			ri = jt.executeQuery(sql);
			if (ri.size() > 0){
				 wl = new WorkLog();
				 while(ri.hasNext()){
					 rr = (ResultRecord)ri.next();
					 wl.setContent(rr.getString("content"));
					 wl.setId(id);
					 wl.setMyDate(DateUtil.parseDate(DateUtil.format(rr.getDate("mydate"), DateUtil.DATE_TIME_FORMAT)));
					 wl.setRealDate(rr.getDate("mydate"));
					 wl.setReviewCount(rr.getInt("review_count"));
					 wl.setPraiseCount(rr.getInt("praise_count"));
					 wl.setType(rr.getString("log_type"));
					 wl.setLogItem(rr.getInt("log_item"));
					 wl.setLogYear(rr.getInt("log_year"));
					 //UserDb ud = new UserDb(rr.getString("userName"));
					 wl.setUserName(rr.getString("userName"));
					 wl.setWorkLogAttachs(getAttachsByWorkLogId(id));
					 wl.setWorkLogExpands(getExpandsByWorkLogId(id));
					 wl.setWorkLogPraises(getCommonExpands(id,Integer.parseInt(WorkLogExpand.PRAISE_TYPE)));
					 break;
				 }
				 
			}
			
		} catch (ParseException e) {
			logger.error("getWorkLogByDate is error:" + e.getMessage());
		}catch (SQLException e) {
			logger.error("getWorkLogByDate is error:" + e.getMessage());
		}
		return wl;
	}
	/**
	 * 创建工作汇报
	 * @param wl
	 * @return
	 */
	public boolean createWorkLog(WorkLog wl){
		boolean flag = false;
		String sql = "";
		Conn conn = new Conn(Global.getDefaultDB());
		
		sql = "insert into work_log(id,userName,content,myDate,log_type,log_item,log_year) values (?,?,?,?,?,?,?)";;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, wl.getId());
	        ps.setString(2, wl.getUserName());
	        ps.setString(3, wl.getContent());
	        ps.setTimestamp(4, new Timestamp(DateUtil.parse(wl.getMyDate(), DateUtil.DATE_TIME_FORMAT).getTime()));
	        ps.setInt(5, Integer.valueOf(wl.getType()));
	        ps.setInt(6, wl.getLogItem());
	        ps.setInt(7, wl.getLogYear());
	        flag = conn.executePreUpdate()==1?true:false;
	        if (flag) {
	        	WorkLogCache wlc = new WorkLogCache();
	        	wlc.refreshList();

	        	// 更新项目或任务的进度及生成关联
	        	String code = StrUtil.getNullStr(wl.getFormCode());
	        	int prjId = wl.getPrjId();		        	
	        	int process = wl.getProcess();
	        	if(code.equals(PrjConfig.CODE_PRJ)){
	        		WorkLogForModuleMgr wm = new WorkLogForModuleMgr();
	        		wm.updatePrj(code,wl.getId(),prjId,process,wl.getType(), true);
	        	}else if(code.equals(PrjConfig.CODE_TASK)){
	        		WorkLogForModuleMgr wm = new WorkLogForModuleMgr();
	        		wm.updatePrjTask(code,wl.getId(),prjId,process,wl.getType(), true);
	        	}
	        }
		} catch (SQLException e) {
			logger.error("createWorkLog is error:" + e.getMessage());
		}finally{
			if (ps != null){
				try {
					ps.close();
				} catch (SQLException e) {
					logger.error("createWorkLog is error:" + e.getMessage());
				}
				ps = null;
			}
			if (conn!=null) {
                conn.close();
                conn = null;
            }			
		}
        return flag;
	}
	/**
	 * 插入附件信息
	 * @param wla
	 * @return
	 */
	public boolean createWorkLogAttach(WorkLogAttach wla){
		boolean flag = false;
		String sql = "";
		Conn conn = new Conn(Global.getDefaultDB());
		PreparedStatement ps = null;
		try {
			sql = "insert into work_log_attach(id,workLogId, name, diskname, visualpath, orders, file_size) values(?,?,?,?,?,?,?)";
			
			ps = conn.prepareStatement(sql);
			ps.setInt(1, wla.getId());
			ps.setInt(2, wla.getWorkLogId());
			ps.setString(3, wla.getName());
			ps.setString(4, wla.getDiskName());
			ps.setString(5, wla.getVisualPath());
			ps.setInt(6, wla.getOrders());
			ps.setLong(7, wla.getFileSize());
	        flag = conn.executePreUpdate()==1?true:false;
		} catch (SQLException e) {
			logger.error("createWorkLogAttach is error:" + e.getMessage());
		}finally{
			if (conn!=null) {
                conn.close();
                conn = null;
            }
			if (ps != null){
				try {
					ps.close();
				} catch (SQLException e) {
					logger.error("createWorkLogAttach is error:" + e.getMessage());
				}
				ps = null;
			}
		}
        return flag;
	}
	/**
	 * 修改后保存
	 * @return
	 */
	public boolean saveMyWorkLog(WorkLog wl){
		boolean flag = false;
		String sql = "";
		Conn conn = new Conn(Global.getDefaultDB());
		PreparedStatement ps = null;
		try {
			sql = "update work_log set content=? where id=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, wl.getContent());
			ps.setInt(2, wl.getId());
	        flag = conn.executePreUpdate()==1?true:false;
	        if (flag) {
	        	WorkLogCache wlc = new WorkLogCache();
	        	wlc.refreshList();
	        	
	        	// 更新项目或任务的进度及生成关联
	        	String code = StrUtil.getNullStr(wl.getFormCode());
	        	int prjId = wl.getPrjId();		        	
	        	int process = wl.getProcess();
	        	if(code.equals(PrjConfig.CODE_PRJ)){
	        		WorkLogForModuleMgr wm = new WorkLogForModuleMgr();
	        		wm.updatePrj(code,wl.getId(),prjId,process,wl.getType(), true);
	        	}else if(code.equals(PrjConfig.CODE_TASK)){
	        		WorkLogForModuleMgr wm = new WorkLogForModuleMgr();
	        		wm.updatePrjTask(code,wl.getId(),prjId,process,wl.getType(), true);
	        	}	        	
	        }
		} catch (SQLException e) {
			logger.error("saveMyWorkLog is error:" + e.getMessage());
		}finally{
			if (ps != null){
				try {
					ps.close();
				} catch (SQLException e) {
					logger.error("saveMyWorkLog is error:" + e.getMessage());
				}
				ps = null;
			}
			if (conn!=null) {
                conn.close();
                conn = null;
            }			
		}
        return flag;
	}
	
	/**
	 * 删除附件
	 * @param attachId
	 * @return
	 */
	public boolean delAttach(int attachId){
		boolean flag = false;
		String sql = "";
		Conn conn = new Conn(Global.getDefaultDB());
		PreparedStatement ps = null;
		try {
			sql = "delete from work_log_attach where id=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, attachId);
	        flag = conn.executePreUpdate()==1?true:false;
		} catch (SQLException e) {
			logger.error("delAttach is error:" + e.getMessage());
		}finally{
			if (ps != null){
				try {
					ps.close();
				} catch (SQLException e) {
					logger.error("delAttach is error:" + e.getMessage());
				}
				ps = null;
			}			
			if (conn!=null) {
                conn.close();
                conn = null;
            }
		}
        return flag;
	}
	
	/**
	 * 点赞
	 * @param workLogId
	 * @return
	 */
	public boolean savePraise(int workLogId){
		boolean flag = false;
		String sql = "";
		Conn conn = new Conn(Global.getDefaultDB());
		PreparedStatement ps = null;
		try {
			sql = "update work_log set praise_count= (praise_count+1) where id=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, workLogId);
	        flag = conn.executePreUpdate()==1?true:false;
		} catch (SQLException e) {
			logger.error("savePraise is error:" + e.getMessage());
		}finally{
			if (ps != null){
				try {
					ps.close();
				} catch (SQLException e) {
					logger.error("savePraise is error:" + e.getMessage());
				}
				ps = null;
			}
			if (conn!=null) {
                conn.close();
                conn = null;
            }			
		}
        return flag;
	}
	/**
	 * lzm 点赞或者 取消点赞
	 * @Description: 
	 * @param wle
	 * @param praiseType
	 * @return
	 */
	public boolean savePraiseOrCancel(WorkLogExpand wle ,int praiseType){
		boolean flag = false;
		switch (praiseType) {
		case PRAISE:
			flag = addCommonWorkLogExpand(wle, Integer.parseInt(WorkLogExpand.PRAISE_TYPE));
			break;
		case CANCEL_PRAISE:
			flag = cancelPraise(wle);
			break;
		default:
			flag = addCommonWorkLogExpand(wle, Integer.parseInt(WorkLogExpand.PRAISE_TYPE));
			break;
		}
		return flag;
	}

	/**
	 * 取消点赞 lzm 
	 * @Description: 
	 * @param wle
	 * @return
	 */
	private boolean cancelPraise(WorkLogExpand wle){
		boolean flag = false;
		String sql = "";
		Conn conn = new Conn(Global.getDefaultDB());
		PreparedStatement ps = null;
		try {
			//点赞 
	        sql = "delete from work_log_expand where user_name = ? and work_log_id = ? and type = "+WorkLogExpand.PRAISE_TYPE;
	        ps = conn.prepareStatement(sql);
	        ps.setString(1, wle.getUserName());
	        ps.setInt(2, wle.getWorkLogId());
	        flag = conn.executePreUpdate()==1?true:false;
	        if(flag){
				//更改点赞数量
				sql = "update work_log set praise_count= (praise_count-1) where id=?";
				ps = conn.prepareStatement(sql);
				ps.setInt(1,wle.getWorkLogId());
		        flag = conn.executePreUpdate()==1?true:false;
	        }
	        
		} catch (SQLException e) {
			logger.error("cancelPraise is error:" + e.getMessage());
		}finally{
			if (conn!=null) {
                conn.close();
                conn = null;
            }
			if (ps != null){
				try {
					ps.close();
				} catch (SQLException e) {
					logger.error("cancelPraise is error:" + e.getMessage());
				}
				ps = null;
			}
		}
        return flag;
		
		
	}
	/**
	 * 罗珠敏 通用 工作汇报扩展表基础类 新增
	 * @Description: 
	 * @param wle
	 * @param type
	 * @return
	 */
	private boolean addCommonWorkLogExpand(WorkLogExpand wle,int type){
		String updateSql = "update work_log set praise_count= (praise_count+1) where id=?";
	
		//0 代表 评论 
		switch (type) {
		case 0:
			updateSql ="update work_log set review_count= (review_count+1) where id=?";
			
			break;
		default:
			break;
		}
		boolean flag = false;
		String sql = "";
		Conn conn = new Conn(Global.getDefaultDB());
		PreparedStatement ps = null;
		try {
			//插入评论/点赞内容
	        sql = "insert into work_log_expand(id,work_log_id, user_name, review, review_time, type) values(?,?,?,?,?,?) ";
	        ps = conn.prepareStatement(sql);
	        ps.setInt(1, wle.getId());
			ps.setInt(2, wle.getWorkLogId());
			ps.setString(3, wle.getUserName());
			ps.setString(4, wle.getReview());
			ps.setTimestamp(5, new Timestamp(DateUtil.parse(wle.getReviewTime(), DateUtil.DATE_TIME_FORMAT).getTime()));
			ps.setInt(6,type );
	        flag = conn.executePreUpdate()==1?true:false;
	        if(flag){
				//更新评论或点赞数
				ps = conn.prepareStatement(updateSql);
				ps.setInt(1, wle.getWorkLogId());
		        flag = conn.executePreUpdate()==1?true:false;
		        
		        // 发送消息
		        IMessage imsg = null;
		        ProxyFactory proxyFactory = new ProxyFactory("com.redmoon.oa.message.MessageDb");
		        Advisor adv = new Advisor();
		        MobileAfterAdvice mba = new MobileAfterAdvice();
		        adv.setAdvice(mba);
		        adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
		        proxyFactory.addAdvisor(adv);
		        imsg = (IMessage) proxyFactory.getProxy();
		        
		        try {
		        	WorkLogDb wld = new WorkLogDb();
		        	wld = wld.getWorkLogDb(wle.getWorkLogId());
		        	if (!wld.getUserName().equals(wle.getUserName())) {
		        		UserDb ud = new UserDb();
		        		ud = ud.getUserDb(wle.getUserName());
		        		Config cfg = new Config();
		        		String t = cfg.get("workLogRemindTitle");
		        		t = StrUtil.format(t, new Object[]{ud.getRealName(), StrUtil.getAbstract(null, wle.getReview(), 15, "")});
		        		imsg.sendSysMsg(wld.getUserName(), t, wle.getReview(), MessageDb.ACTION_WORKLOG, "", String.valueOf(wld.getId()));
		        	}
				} catch (ErrMsgException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
		} catch (SQLException e) {
			logger.error("addReview is error:" + e.getMessage());
		}finally{
			if (conn!=null) {
                conn.close();
                conn = null;
            }
		}
		
		return flag;
	}
	/**
	 * 新增评论
	 * @param wle
	 * @return
	 */
	public boolean addReview(WorkLogExpand wle){
		boolean flag = false;
		flag = addCommonWorkLogExpand(wle,Integer.parseInt(WorkLogExpand.REVIEW_TYPE));
        return flag;
	}
	/**
	 * 获取传入日期多少个工作日之前的所有日期
	 * 
	 * @param fromDate
	 *            Date
	 * @param dayNums
	 *            int
	 * @return List<Date>
	 * @throws ErrMsgException
	 */
	public List<Date> getWorkDaysFromDb(java.util.Date date,
			int dayNums) {
		if (date == null || dayNums == 0)
			return null;
		List<Date> list = null;
		String sql = "select oa_date from oa_calendar"
				+ " where date_type="
				+ OACalendarDb.DATE_TYPE_WORK
				+ " and oa_date<"
				+ SQLFilter.getDateStr(DateUtil.format(DateUtil.addDate(date,
						1), "yyyy-MM-dd"), "yyyy-MM-dd")
				+ " order by oa_date desc limit " + dayNums;
		JdbcTemplate jt = null;
		try {
			jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(sql);
			list = new ArrayList<Date>();
			while (ri.hasNext()){
				ResultRecord rr = (ResultRecord) ri.next();
				list.add(rr.getDate("oa_date"));
			}
			return list;
			
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}finally{
			if(jt != null){
				jt.close();
			}
		}
		return null;
	}
	/**
	 * 获取周报详情
	 * @param wl
	 * @return
	 */
	public List<WorkLog> queryWeekLogInfos(WorkLog wl){
		List<WorkLog> list = null;
		String sql;
		if (wl.getFormCode() != null && !"".equals(wl.getFormCode()) && wl.getPrjId() != 0 ){
			// sql += " and id in (select workLog_id from visual_module_worklog where cws_id = "+wl.getPrjId()+" and form_code = "+ StrUtil.sqlstr(wl.getFormCode())+")";
			sql = " select l.* from work_log l,visual_module_worklog v where l.id=v.workLog_id and v.cws_id = "+wl.getPrjId()+" and v.form_code = "+ StrUtil.sqlstr(wl.getFormCode());			
		}
		else {
			sql = "select * from work_log l where username = " + SQLFilter.sqlstr(wl.getUserName());
		}
		
		sql += " and l.log_type=" + wl.getType() ;
		String condContent = wl.getCondContent();
		if (condContent != null && !"".equals(condContent)){
			sql += " and l.content like '%" + condContent + "%'";
		}
		if(wl.getLogItem() != 0){
			sql += " and l.log_item =" +  wl.getLogItem();
		}
		if (wl.getLogYear() != 0){
			sql += " and l.log_year =" + wl.getLogYear();
		}		
		sql += " order by l.mydate desc";

		JdbcTemplate jt = null;
		ResultIterator ri = null;
		ResultRecord rr = null;
		try {
			jt = new JdbcTemplate();
			ri = jt.executeQuery(sql);//获取主表信息
			list = new ArrayList<WorkLog>();
			while(ri.hasNext()){
				WorkLog wlTmp = new WorkLog();
				rr = (ResultRecord)ri.next();
				int id = rr.getInt("id");//主键
				wlTmp.setId(id);
				wlTmp.setContent(rr.getString("content"));
				wlTmp.setLogItem(rr.getInt("log_item"));
				wlTmp.setLogYear(rr.getInt("log_year"));
				wlTmp.setPraiseCount(rr.getInt("praise_count"));
				wlTmp.setReviewCount(rr.getInt("review_count"));
				wlTmp.setWorkLogAttachs(getAttachsByWorkLogId(id));
				wlTmp.setWorkLogExpands(getExpandsByWorkLogId(id));
				wlTmp.setWorkLogPraises(getCommonExpands(id,Integer.parseInt(WorkLogExpand.PRAISE_TYPE)));
				list.add(wlTmp);
				
			}
		} catch (SQLException e) {
			logger.error("queryWorkLogInfos is error: " + e.getMessage());
		}finally{
			if (jt != null){
				jt.close();
				jt = null;
			}
		}
		
		return list;
		
	}
	
	/**
	 * 根据日期取得日报的ID
	 * @Description: 
	 * @param userName
	 * @param date
	 * @return
	 */
	public long getWorkLogId(String userName, String date, String logType) {
		String sql = "select id from work_log where userName = " + SQLFilter.sqlstr(userName) + " and myDate=" + SQLFilter.getDateStr(date, "yyyy-MM-dd HH:mm:ss");
		sql += " and log_type=" + logType + " order by mydate desc";

		JdbcTemplate jt = null;
		ResultIterator ri = null;
		try {
			jt = new JdbcTemplate();
			ri = jt.executeQuery(sql);//获取主表信息
			if(ri.hasNext()){
				ResultRecord rr = (ResultRecord)ri.next();
				return rr.getLong(1);
			}
		} catch (SQLException e) {
			logger.error("isTheDateExit is error: " + e.getMessage());
		}
		return -1;
	}
	
	/**
	 * 判断当前日期是否已存在日报
	 * @param wl
	 * @return
	 */
	public boolean isTheDateExit(String userName , String date){
		return getWorkLogId(userName, date, MyWorkManageAction.TYPE_DAY)>0;
	}
}
