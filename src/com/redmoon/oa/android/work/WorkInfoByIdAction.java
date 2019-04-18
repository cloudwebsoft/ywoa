package com.redmoon.oa.android.work;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.android.base.BaseAction;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.worklog.WorkLogDb;
import com.redmoon.oa.worklog.domain.WorkLog;
import com.redmoon.oa.worklog.domain.WorkLogAttach;
import com.redmoon.oa.worklog.domain.WorkLogExpand;
import com.redmoon.oa.worklog.service.MyWorkManageServices;
import com.redmoon.oa.worklog.service.impl.MyWorkManageServicesImpl;

/**
 * @Description: 
 * @author: 
 * @Date: 2015-12-15下午04:41:52
 */
public class WorkInfoByIdAction extends BaseAction{
	private String skey;//用户名
	private int id;
	
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
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
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
			if(id != 0){
				MyWorkManageServices mwms = MyWorkManageServicesImpl.getInstance();
				WorkLog workLog = mwms.getWorkLogInfoById(id);
				org.json.JSONObject obj = new org.json.JSONObject();
				obj.put("id",id);
			    obj.put("myDate", workLog.getMyDate());
			    obj.put("reviewCount",workLog.getReviewCount());
			    obj.put("content", StrUtil.getAbstract(request,workLog.getContent(), 20000, "\r\n"));
			    int logType = Integer.parseInt(workLog.getType());
			    obj.put("logType", logType);
				int logItem = workLog.getLogItem();
				obj.put("logItem", logItem);
				int logYear = workLog.getLogYear();
				obj.put("logYear",logYear);
				String beginDate = "";
				String endDate = "";
				if(logType == WorkLogDb.TYPE_WEEK){
					 beginDate = DateUtil.format(DateUtil.getFirstDayOfWeek(logYear, logItem), DateUtil.DATE_FORMAT);
					 endDate = DateUtil.format(DateUtil.getLastDayOfWeek(logYear, logItem), DateUtil.DATE_FORMAT);
				}
				obj.put("beginDate", beginDate);
				obj.put("endDate", endDate);
				UserDb userDb = new UserDb(workLog.getUserName());
				obj.put("userName",userDb.getRealName() );
				obj.put("reviewCount", workLog.getReviewCount());
				List<WorkLogAttach> list_attach = mwms.getWorkLogAttachesByWorkLogId(id);
				JSONArray attachs = new JSONArray();
				//附件列表
				if(list_attach != null && list_attach.size()>0){
					for(WorkLogAttach workLogAttach:list_attach){
						JSONObject attachsObj = new JSONObject();
						attachsObj.put("id", workLogAttach.getId());
						attachsObj.put("name", workLogAttach.getName());
						attachsObj.put("downloadUrl",workLogAttach.getDiskName());   // 是否用IO流合适
						attachs.put(attachsObj);
					}
					obj.put("attachs", attachs);
				}
				List<WorkLogExpand> list =  workLog.getWorkLogExpands();
				JSONArray reviews = new JSONArray();
				//评论列表
				if(list != null && list.size()>0){
					for(WorkLogExpand workLogExpand:list){
						JSONObject reviewObj = new JSONObject();
						reviewObj.put("id", workLogExpand.getId());
						reviewObj.put("reviewTime", workLogExpand.getReviewTime());
						reviewObj.put("reviewContent",StrUtil.getAbstract(request,workLogExpand.getReview(), 20000, "\r\n") );
						reviewObj.put("userName",workLogExpand.getUserName());
						reviews.put(reviewObj);
					}
					obj.put("reviews", reviews);
				}
				List<WorkLogExpand> praisesList = workLog.getWorkLogPraises();
				JSONArray praises = new JSONArray();
				if(praisesList != null && praisesList.size()>0){
					for(WorkLogExpand workLogExpand:praisesList){
						JSONObject praisesObj = new JSONObject();
						praisesObj.put("id", workLogExpand.getId());
						praisesObj.put("reviewTime", workLogExpand.getReviewTime());
						praisesObj.put("userName",workLogExpand.getUserName());
						praises.put(praisesObj);
					}
					obj.put("workLogPraises", praises);
				}
				
				
				
				jResult.put(DATA, obj);
				jResult.put(RETURNCODE, RESULT_SUCCESS);
			}else{
				jResult.put(RETURNCODE, RESULT_ID_NULL);
				
			}
			jReturn.put(RESULT, jResult);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.error(MyWorkByTypeListAction.class.getName()+":"+e.getMessage());
		}
		
	} 

	
}
