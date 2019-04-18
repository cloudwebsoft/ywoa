package com.redmoon.oa.android.work;


import java.util.Date;
import java.util.List;


import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.struts2.ServletActionContext;
import org.json.JSONException;
import cn.js.fan.util.DateUtil;


import com.redmoon.oa.android.base.BaseAction;
import com.redmoon.oa.db.SequenceManager;

import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.worklog.domain.WorkLog;
import com.redmoon.oa.worklog.domain.WorkLogExpand;
import com.redmoon.oa.worklog.service.MyWorkManageServices;
import com.redmoon.oa.worklog.service.impl.MyWorkManageServicesImpl;

/**
 * @Description: 
 * @author: 
 * @Date: 2015-12-15下午04:41:52
 */
public class WorkPraiseOrCancelAction extends BaseAction{
	private int  praiseType;//点赞类型  
	private String skey;//用户名
	private int workId;

	/**
	 * @return the praiseType
	 */
	public int getPraiseType() {
		return praiseType;
	}
	/**
	 * @param praiseType the praiseType to set
	 */
	public void setPraiseType(int praiseType) {
		this.praiseType = praiseType;
	}
	/**
	 * @return the skey
	 */
	public String getSkey() {
		return skey;
	}
	/**
	 * @param skey the skey to set
	 */
	public void setSkey(String skey) {
		this.skey = skey;
	}
	/**
	 * @return the workId
	 */
	public int getWorkId() {
		return workId;
	}
	/**
	 * @param workId the workId to set
	 */
	public void setWorkId(int workId) {
		this.workId = workId;
	}
	
	@Override
	public void executeAction() {
		// TODO Auto-generated method stub
		super.executeAction();
		com.redmoon.oa.android.Privilege mPriv = new com.redmoon.oa.android.Privilege();
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
			String userName = mPriv.getUserName(getSkey()) ;
			UserDb userDb = new UserDb(userName);
			int id = (int) SequenceManager.nextID(SequenceManager.OA_WORK_LOG_EXPAND);
			MyWorkManageServices mwms = MyWorkManageServicesImpl.getInstance();
			WorkLogExpand wle = new WorkLogExpand();
			wle.setId(id);
			wle.setReview("");
			wle.setUserName(userName);
			wle.setReviewTime(DateUtil.format(new Date(), DateUtil.DATE_TIME_FORMAT));
			wle.setWorkLogId(workId);
			
			if (mwms.savePraiseOrCancel(wle, praiseType)){
				jResult.put(RETURNCODE, RESULT_SUCCESS); //评论成功
				WorkLog wl = mwms.getWorkLogInfoById(workId);
				int praiseCount = wl.getPraiseCount();
				List<WorkLogExpand> praises = wl.getWorkLogPraises();
				StringBuilder praiseSb = new StringBuilder();
				if(praises != null && praises.size()>0){
					for(WorkLogExpand workLogExpand:praises){
						if(praiseSb.toString().equals("")){
							praiseSb.append(workLogExpand.getName());
						}else{
							praiseSb.append(",").append(workLogExpand.getName());
						}
					}
				
				}
				jResult.put("praiseUser", praiseSb.toString());
				jResult.put("praiseCount", praiseCount);
				
			}else{
				jResult.put(RETURNCODE, RESULT_INSERT_FAIL);
			}
			jReturn.put(RESULT, jResult);
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.error(MyWorkByTypeListAction.class.getName()+":"+e.getMessage());
		}
		
		
		
		
	} 

	
}
