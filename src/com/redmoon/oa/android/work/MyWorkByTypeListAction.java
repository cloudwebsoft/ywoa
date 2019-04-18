package com.redmoon.oa.android.work;

import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.android.base.BaseAction;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.worklog.WorkLogDb;
import com.redmoon.oa.worklog.domain.WorkLogExpand;
import com.redmoon.oa.worklog.service.MyWorkManageServices;
import com.redmoon.oa.worklog.service.impl.MyWorkManageServicesImpl;

/**
 * @Description: 
 * @author: 
 * @Date: 2015-12-15下午01:45:59
 */
public class MyWorkByTypeListAction extends BaseAction {
	private String skey;
	private int type;
	private int pagenum = 1;
	private int pagesize = 15;
	private String op = "";
	private String what = "";	
	
	/**
	 * @return the pagenum
	 */
	public int getPagenum() {
		return pagenum;
	}
	/**
	 * @param pagenum the pagenum to set
	 */
	public void setPagenum(int pagenum) {
		this.pagenum = pagenum;
	}
	/**
	 * @return the pagesize
	 */
	public int getPagesize() {
		return pagesize;
	}
	/**
	 * @param pagesize the pagesize to set
	 */
	public void setPagesize(int pagesize) {
		this.pagesize = pagesize;
	}
	/**
	 * @return the op
	 */
	public String getOp() {
		return op;
	}
	/**
	 * @param op the op to set
	 */
	public void setOp(String op) {
		this.op = op;
	}
	/**
	 * @return the what
	 */
	public String getWhat() {
		return what;
	}
	/**
	 * @param what the what to set
	 */
	public void setWhat(String what) {
		this.what = what;
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
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}
	
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
			String userName = mPriv.getUserName(getSkey()) ;
			String sql = "select id from work_log where userName="+StrUtil.sqlstr(userName)+" and log_type=" + type;
			if (op.equals("search")) {
				sql +=" and content like " + StrUtil.sqlstr("%" + getWhat() + "%");
			}
			sql +=" order by myDate desc";
			WorkLogDb wld = new WorkLogDb();
			wld.objectCache.refreshList(); //清空缓存  评论数量未插入缓存
			ListResult lr = wld.listResult(sql, pagenum, pagesize);
			int total = lr.getTotal();
			if(total>0){
				JSONArray arrays = new JSONArray();
				jResult.put(RETURNCODE, RESULT_SUCCESS);
				jResult.put("total", total);
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
					wlds.put("logType", type);
					int logItem = wld.getLogItem();
					wlds.put("logItem", logItem);
					int logYear = wld.getLogYear();
					String beginDate = "";
					String endDate = "";
					if(type == WorkLogDb.TYPE_WEEK){
						 beginDate = DateUtil.format(DateUtil.getFirstDayOfWeek(logYear, logItem), DateUtil.DATE_FORMAT);
						 endDate = DateUtil.format(DateUtil.getLastDayOfWeek(logYear, logItem), DateUtil.DATE_FORMAT);
					}
					wlds.put("beginDate", beginDate);
					wlds.put("endDate", endDate);
					wlds.put("content", StrUtil.getAbstract(request, wld.getContent(), 20000, "\r\n"));				
					wlds.put("reviewCount", wld.getReviewCount());
					wlds.put("logYear", logYear);
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
					arrays.put(wlds);	
				}
				
			
				jResult.put(DATAS, arrays);
				
			}else{
				jResult.put(RETURNCODE, RESULT_NO_DATA);
			}
			jReturn.put(RESULT, jResult);
			
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.error(MyWorkByTypeListAction.class.getName()+":"+e.getMessage());
		} catch (ErrMsgException e) {
			logger.error(MyWorkByTypeListAction.class.getName()+":"+e.getMessage());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			logger.error(MyWorkByTypeListAction.class.getName()+":"+e.getMessage());
		}
		
		
		
		
	} 

}
