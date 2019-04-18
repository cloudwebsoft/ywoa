package com.redmoon.oa.android.work;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.redmoon.oa.dept.DeptMgr;

import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.pvg.RoleMgr;
import com.redmoon.oa.worklog.WorkLogDb;
import com.redmoon.oa.worklog.domain.WorkLogExpand;
import com.redmoon.oa.worklog.service.MyWorkManageServices;
import com.redmoon.oa.worklog.service.impl.MyWorkManageServicesImpl;


/**
 * @Description: 
 * @author: 
 * @Date: 2015-12-14下午03:04:31
 */
public class LeaderViewWorkAction extends BaseAction {
	private String[] roles = {"14652002886951448023","14684596921825871948","14684665397117416079"};//董事长,公司高管,中层管理人员
	private String[] roles_m = {"14684596921825871948","14684665397117416079","14684666596624090847"};//被管理的角色 公司高管，中层管理人员,基层管理人员
	private String skey;
	private int type;
	private int pagenum = 1;
	private int pagesize = 15;
	private String op;
	private String what;
	

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
			JSONObject jRes = leaderViewWorkByType(request);
			jReturn.put(RESULT, jRes);
			
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.error(LeaderViewWorkAction.class.getName()+"==="+e.getMessage());
		}
		
		
		
		
	}
	/**
	 * 领导查看下属日报 月报 周报 
	 * 罗珠敏
	 * @Description: 
	 * @param request
	 * @param type
	 * @return
	 */
	  public JSONObject leaderViewWorkByType(HttpServletRequest request){
		  	boolean  isContain = false; //是否包含权限管理
		    JSONObject jRes = new JSONObject();
	    	DeptMgr deptMgr = new DeptMgr();
	    	Privilege mPriv = new Privilege();
	    	HashMap<String,String> roleMap = initRolesHashMap(roles, roles_m);
	    	String myName = mPriv.getUserName(skey);
	    	UserDb myUserDb = new UserDb(myName);
	    	List<String> users = deptMgr.getUserAdminDeptsUser(request); //用户  管理所有用户
	    	StringBuilder userSb = new StringBuilder();
	    	if(myUserDb != null && myUserDb.isLoaded()){ 
	    		RoleDb[] roles = myUserDb.getRoles();//获得当前用户的roles
	    		if(roles != null && roles.length>0){
	    			for(RoleDb roleDb :roles){
	    				String roleCode = roleDb.getCode();//获得roleCode 判断是不是  “董事长 ”...角色
	    				if(roleMap.containsKey(roleCode)){
	    					isContain = true;
	    					String role_m = roleMap.get(roleCode);//获得董事长 管理的角色
	    					if(users != null && users.size()>0){
	    						for(String username:users){
	    							UserDb userDb = new UserDb(username);
	    							if(userDb != null && userDb.isLoaded()){
	    								RoleDb[] roleDbs = userDb.getRoles();//循环用户  管理的用户 ，并获得 用户的角色  看是否包含 被管理的角色
	    								if(roleDbs != null && roleDbs.length>0){
	    									for(RoleDb sRoleDb:roleDbs){
	    										String sRoleCode = sRoleDb.getCode();
	    										if(sRoleCode.equals(role_m)){//用户角色是否包含被管理的角色
	    											if(userSb.toString().equals("")){
	    								    			userSb.append(StrUtil.sqlstr(username));
	    								    		}else{
	    								    			userSb.append(",").append(StrUtil.sqlstr(username));
	    								    		}
	    										}
	    									}
	    								}
	    							}
	    						}
	    					}
	    				}
	    				
	    			}
	    		}
	    	
	    	}
	    	if(!isContain){
	    		for(String username:users){
		    		if(userSb.toString().equals("")){
		    			userSb.append(StrUtil.sqlstr(username));
		    		}else{
		    			userSb.append(",").append(StrUtil.sqlstr(username));
		    		}
		    	}
	    	}
	    	System.out.println(userSb.toString());
	    	
	    
	    	
			try {
				if(userSb.toString().trim().equals("")){
		    		jRes.put(RETURNCODE, RESULT_NO_DATA);
		    	}else{
		    		StringBuilder sqlSb = new StringBuilder();
			    	sqlSb.append("select w.id,userName,content,myDate,appraise,log_type,log_item,log_year,itemType,review_count,praise_count from work_log w,users u where w.userName = u.name and userName in (").append(userSb).append(")")
		    		.append(" and log_type = ").append(type);
			    	if(op != null && !op.equals("")){
			    		if(op.equals(SEARCH)){
			    			sqlSb.append(" and u.realName like '%").append(what).append("%'");
			    		}
			    	}
			    	if(type == WorkLogDb.TYPE_WEEK || type == WorkLogDb.TYPE_MONTH){
			    		
			    		sqlSb.append("  order by log_year desc ,log_item desc ");
			    		
			    	}else{
			    		
			    		sqlSb.append("  order by myDate desc ");
			    	}
			    	WorkLogDb wld = new WorkLogDb();
			    	wld.objectCache.refreshList(); //清空缓存  评论数量未插入缓存
					ListResult lr;
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
							wlds.put("id",String.valueOf(wld.getId()));
							wlds.put("myDate",DateUtil.parseDate(DateUtil.format(wld.getMyDate(), DateUtil.DATE_TIME_FORMAT)));
							wlds.put("logType", type);
							int logItem = wld.getLogItem();
							wlds.put("logItem", logItem);
							int logYear = wld.getLogYear();
							wlds.put("logYear", logYear);
							String beginDate = "";
							String endDate = "";
							if(type == WorkLogDb.TYPE_WEEK){
								 beginDate = DateUtil.format(DateUtil.getFirstDayOfWeek(logYear, logItem), DateUtil.DATE_FORMAT);
								 endDate = DateUtil.format(DateUtil.getLastDayOfWeek(logYear, logItem), DateUtil.DATE_FORMAT);
							}
							wlds.put("beginDate", beginDate);
							wlds.put("endDate", endDate);
							UserDb userDb = new UserDb(wld.getUserName());
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
						jRes.put("total", total);
						jRes.put(DATAS, arrays);
					}else{
						jRes.put(RETURNCODE, RESULT_NO_DATA);
					}
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
	  
	  private HashMap<String,String> initRolesHashMap (String[] role,String[] role_m){
		  HashMap<String, String> roleMap = new HashMap<String, String>();
		  if(role != null && role.length>0){
			 for (int i = 0; i < role.length; i++) {
				 roleMap.put(role[0], role_m[0]);
				
			}
		  }
		  return roleMap;
		  
	  }

	
	

	
}
