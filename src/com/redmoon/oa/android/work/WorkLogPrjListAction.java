package com.redmoon.oa.android.work;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.android.base.BaseAction;
import com.redmoon.oa.android.sales.SalesContractListAction;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.prj.PrjConfig;
import com.redmoon.oa.visual.ModuleSetupDb;
import com.redmoon.oa.worklog.WorkLogDb;
import com.redmoon.oa.worklog.WorkLogForModuleMgr;
import com.redmoon.oa.worklog.domain.WorkLogExpand;
import com.redmoon.oa.worklog.service.MyWorkManageServices;
import com.redmoon.oa.worklog.service.impl.MyWorkManageServicesImpl;


/**
 * @Description: 
 * @author: 
 * @Date: 2015-12-14下午03:04:31
 */
public class WorkLogPrjListAction extends BaseAction {
	private String skey;
	private int pagenum = 1;
	private int pagesize = 15;
	private int workLogType;
	private int prjTaskType;
	private int ptId;
	private String op;
	private String what;
	private String cond;
	
	/**
	 * @return the workLogType
	 */
	public int getWorkLogType() {
		return workLogType;
	}
	/**
	 * @param workLogType the workLogType to set
	 */
	public void setWorkLogType(int workLogType) {
		this.workLogType = workLogType;
	}
	/**
	 * @return the prjTaskType
	 */
	public int getPrjTaskType() {
		return prjTaskType;
	}
	/**
	 * @param prjTaskType the prjTaskType to set
	 */
	public void setPrjTaskType(int prjTaskType) {
		this.prjTaskType = prjTaskType;
	}
	public String getSkey() {
		return skey;
	}
	public void setSkey(String skey) {
		this.skey = skey;
	}
	public int getPagenum() {
		return pagenum;
	}
	public void setPagenum(int pagenum) {
		this.pagenum = pagenum;
	}
	public int getPagesize() {
		return pagesize;
	}
	public void setPagesize(int pagesize) {
		this.pagesize = pagesize;
	}
	
	public int getPtId() {
		return ptId;
	}
	public void setPtId(int ptId) {
		this.ptId = ptId;
	}
	public String getOp() {
		return op;
	}
	public void setOp(String op) {
		this.op = op;
	}
	public String getWhat() {
		return what;
	}
	public void setWhat(String what) {
		this.what = what;
	}
	public String getCond() {
		return cond;
	}
	public void setCond(String cond) {
		this.cond = cond;
	}
	/**
	 * @Description: 
	 */
	@Override
	public void executeAction() {
		// TODO Auto-generated method stub
		super.executeAction();
		Privilege mPriv = new Privilege();
		boolean re = mPriv.Auth(getSkey());
		HttpServletRequest request = ServletActionContext.getRequest();
		
		try {
			jReturn.put(RES, RESULT_SUCCESS);
			if(re){
				jResult.put(RETURNCODE, RESULT_TIME_OUT);
				jReturn.put(RESULT, jResult);
				return;
			}
			mPriv.doLogin(request, skey);
			JSONObject jRes = queryList(request);
			jReturn.put(RESULT, jRes);
			
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.error(LeaderViewWorkAction.class.getName()+"==="+e.getMessage());
		}
	}
	
	public JSONObject queryList(HttpServletRequest request){
		
		JSONObject jRes = new JSONObject();
    	Privilege mPriv = new Privilege();
    	try {
	    		StringBuilder sqlSb = new StringBuilder();
	    		sqlSb.append("select l.id,userName,content,myDate,review_count,log_type,review_count from work_log l,visual_module_worklog w,users u  where l.userName = u.name and l.id = w.workLog_id");
	    		sqlSb.append(" and log_type =").append(String.valueOf(workLogType));
	    		String formCode = PrjConfig.CODE_PRJ;
	    		if(prjTaskType == WOKR_lOG_TASK_TYPE){
	    			formCode = PrjConfig.CODE_TASK;
	    		}
	    		sqlSb.append(" and form_code = ").append(StrUtil.sqlstr(formCode)).append(" and cws_id = ").append(ptId);
		    	if(op != null && !op.equals("")){
		    		if(op.equals(SEARCH)){
		    			sqlSb.append(" and "+cond+" like '%").append(what).append("%'");
		    		}
		    	}
		    	sqlSb.append("  order by myDate desc ");
		    	ModuleSetupDb msd = new ModuleSetupDb();
		    	WorkLogForModuleMgr wlfm = new WorkLogForModuleMgr();
		    	String userName = mPriv.getUserName(skey);
	    		msd = msd.getModuleSetupDb(formCode);
				int is_workLog = msd.getInt("is_workLog");
				if (msd==null) {
					jRes.put(RETURNCODE,RESULT_MODULE_ERROR);//表单不存在
				}
				int progress = getCurProgress(ptId,prjTaskType);
				if(is_workLog == 1 && wlfm.isReport(userName,formCode,ptId) && progress<100){
					jRes.put("canInsertWorkLog", true);
				}else{
					jRes.put("canInsertWorkLog", false);
				}
		    	WorkLogDb wld = new WorkLogDb();
		    	wld.objectCache.refreshList(); //清空缓存  评论数量未插入缓存
				ListResult lr;
				wld.objectCache.refreshList(); //清空缓存  评论数量未插入缓存
				lr = wld.listResult(sqlSb.toString(), pagenum, pagesize);
				int total = lr.getTotal();
				if(total>0){
					JSONArray arrays = new JSONArray();
					jRes.put(RETURNCODE, RESULT_SUCCESS);
					jRes.put("total", total);
					Vector v = lr.getResult();
					Iterator ir = null;
					if (v!=null)
						ir = v.iterator();		
					while (ir!=null && ir.hasNext()) {
						wld = (WorkLogDb)ir.next();
						JSONObject wlds = new JSONObject(); 
						String name = wld.getUserName();
						UserDb userDb = new UserDb(name);
						wlds.put("userName", userDb.getRealName());
						wlds.put("id",String.valueOf(wld.getId()));
						wlds.put("myDate",DateUtil.parseDate(DateUtil.format(wld.getMyDate(), DateUtil.DATE_TIME_FORMAT)));
						wlds.put("logType", workLogType);
						int logItem = wld.getLogItem();
						wlds.put("logItem", logItem);
						int logYear = wld.getLogYear();
						String beginDate = "";
						String endDate = "";
						if(workLogType == WorkLogDb.TYPE_WEEK){
							 beginDate = DateUtil.format(DateUtil.getFirstDayOfWeek(logYear, logItem), DateUtil.DATE_FORMAT);
							 endDate = DateUtil.format(DateUtil.getLastDayOfWeek(logYear, logItem), DateUtil.DATE_FORMAT);
						}
						wlds.put("beginDate", beginDate);
						wlds.put("endDate", endDate);
						wlds.put("userName",userDb.getRealName() );
						wlds.put("reviewCount", wld.getReviewCount());
						int appraise = wld.getPraiseCount();
					    if(appraise >0){
					    	MyWorkManageServices mwms = MyWorkManageServicesImpl.getInstance();
					    	List<WorkLogExpand> list = mwms.getCommonExpands(wld.getId(), Integer.parseInt(WorkLogExpand.PRAISE_TYPE));
					    	boolean isPraise = false;
					    	Privilege privilege = new Privilege();
					    	String uname = privilege.getUserName(skey);
					    	if(list != null && list.size()>0){
					    		for(WorkLogExpand wle:list){
					    			if(wle.getName().equals(uname)){
					    				isPraise = true;
					    				break;
					    			}
					    		}
					    		
					    	}
					    	wlds.put("isUserPraise", isPraise);
					    	
					    }else{
					    	wlds.put("isUserPraise", false);
					    }
						wlds.put("appraiseCount",appraise);
						wlds.put("content", StrUtil.getAbstract(request, wld.getContent(), 20000, "\r\n"));				
						arrays.put(wlds);
					}
				
					jRes.put("progress", progress);
					jRes.put(DATAS, arrays);
				}else{
					jRes.put(RETURNCODE, RESULT_NO_DATA);
				}
			
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			logger.error(LeaderViewWorkAction.class.getName()+"==="+e.getMessage());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.error(LeaderViewWorkAction.class.getName()+"==="+e.getMessage());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			logger.error(LeaderViewWorkAction.class.getName()+"==="+e.getMessage());
		} 
		
    	
    	return jRes;
    	
	}
	
	public int getCurProgress(int prjTaskId,int prjTaskType ){
		String sql = "";
		if(prjTaskType== WOKR_lOG_PRJ_TYPE){
			sql = "select prj_progress from form_table_prj where id = "+prjTaskId;
		}else{
			sql = "select task_progress from form_table_prj_task where id = "+prjTaskId;	
    	}
		
		JdbcTemplate jt = null;
		int sum = 0;
		try {
			jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(sql);
			while (ri.hasNext()) {
				ResultRecord record = (ResultRecord) ri.next();
				sum = record.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(SalesContractListAction.class).error(
					"ReakSumByContact SQLException:" + e.getMessage());
		}
		return sum;
	}
	
}
