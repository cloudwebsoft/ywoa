package com.redmoon.oa.android.work;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.StrUtil;

import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.android.base.BaseAction;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptMgr;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.dept.DeptView;
import com.redmoon.oa.person.UserDb;


/**
 * @Description: 
 * @author: 
 * @Date: 2015-12-14下午03:04:31
 */
public class OrgBySuperviseAction extends BaseAction {
	private String skey;
	private String deptCode = "root";
	
	/**
	 * @return the deptCode
	 */
	public String getDeptCode() {
		return deptCode;
	}

	/**
	 * @param deptCode the deptCode to set
	 */
	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
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
			UserDb ud = new UserDb(mPriv.getUserName(skey));
			String[] admindepts = ud.getAdminDepts();
			if(admindepts!=null && admindepts.length > 0){
				JSONObject jRes = getAdminDeptAndUsersBySupervise(request,deptCode);
				jReturn.put(RESULT, jRes);
			}else{
				JSONObject jRes = commonUsersByUn(request);
				jReturn.put(RESULT, jRes);
			}
		
			
			
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.error(OrgBySuperviseAction.class.getName()+"==="+e.getMessage());
		}
		
		
		
		
	}
	
	
	/**
	 * 根据权限获得用户管理的部门
	 * @Description: 
	 * @param request
	 * @param deptCode
	 * @return
	 */
	public JSONObject getAdminDeptAndUsersBySupervise(HttpServletRequest request,String deptCode){
		int i = 1;
		DeptDb dd = new DeptDb(deptCode);
		DeptView deptView = new DeptView(dd);
		JSONObject json = new JSONObject();//result内容
		JSONArray jsonArr = new JSONArray(); //json数组  
		DeptMgr dir = new DeptMgr();
		Vector vec = new Vector();
		DeptUserDb deptUserDb = new DeptUserDb();
		try {
			com.redmoon.oa.pvg.Privilege mPriv = new com.redmoon.oa.pvg.Privilege();
			Vector v = mPriv.getUserAdminDepts(request);
			
			Iterator it = v.iterator();
			while (it.hasNext()) {
				DeptDb d = (DeptDb) it.next();
				vec.add(d.getCode());
				Vector v1 = new Vector();
				v1 = d.getAllChild(v1, d);
				Iterator it1 = v1.iterator();
				while (it1.hasNext()) {
					DeptDb dd1 = (DeptDb) it1.next();
					vec.add(dd1.getCode());
				}
			}
			ArrayList<String> list = new ArrayList<String>();
			deptView.getJsonByDept(dir, DeptDb.ROOTCODE, vec, list);
			DeptDb deptDb = new DeptDb(deptCode);
			if (mPriv.canAdminDept(request, deptDb.getCode())) {
				//部门下的所有用户
				Vector deptUsersVec = deptUserDb.list(deptDb.getCode());
				if(deptUsersVec != null && deptUsersVec.size()>0){
					Iterator deptUsersIt = deptUsersVec.iterator();
					while(deptUsersIt.hasNext()){
						DeptUserDb dUserDb = (DeptUserDb)deptUsersIt.next();
						String userName = dUserDb.getUserName();
						UserDb userDb = new UserDb(userName);
						JSONObject jsonUser = new JSONObject();
						jsonUser.put("id", i++);
						jsonUser.put("dCode",deptDb.getCode());
						jsonUser.put("dName", deptDb.getName());
						jsonUser.put("userName",userDb.getName());
						jsonUser.put("realName", userDb.getRealName());
						jsonUser.put("type",BaseAction.USER);
						jsonArr.put(jsonUser);
					}
				}
		
			}
			Vector childVec = deptDb.getChildren();
			Iterator chilIt = childVec.iterator();
			while (chilIt.hasNext()) {
				DeptDb childDept = (DeptDb) chilIt.next();
				if(list != null){
					for(String code:list){
						if(code.equals(childDept.getCode())){
							JSONObject jsonDept = new JSONObject();
							jsonDept.put("id", i++);
							jsonDept.put("dCode",childDept.getCode());
							jsonDept.put("dName", childDept.getName());
							jsonDept.put("type",BaseAction.DEPT);
							jsonArr.put(jsonDept);
						}
					}
				}
			}
			if(jsonArr.length()>0){
				json.put(BaseAction.RETURNCODE,BaseAction.RESULT_SUCCESS);
				json.put(BaseAction.DATAS, jsonArr);
			}else{
				json.put(BaseAction.RETURNCODE,BaseAction.RESULT_NO_DATA);
			}
			
			json.put("parentCode", dd.getParentCode());
			json.put("deptName", dd.getName());	
		}catch (Exception e) {
			try {
				json.put(BaseAction.RETURNCODE,BaseAction.RESULT_SERVER_ERROR);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				
			}
		}	
		return json;
		
	}
	
	
	 public JSONObject  commonUsersByUn(HttpServletRequest request) throws JSONException{
		 	int i = 1;
		 	JSONObject json = new JSONObject();//result内容
			JSONArray jsonArr = new JSONArray(); //json数组  
	    	List<String> deptCodes = new ArrayList<String>();
	    	StringBuilder userNameSb = new StringBuilder();
	    	Vector vec = new Vector();
	    	com.redmoon.oa.pvg.Privilege priv = new com.redmoon.oa.pvg.Privilege();
	    	String userName = priv.getUser(request);
	    	//获得用户所在部门  考虑多用户情况
	    	DeptUserDb dud = new DeptUserDb(userName);
	    	vec = dud.getDeptsOfUser(userName);
	    	Iterator deptIt = vec.iterator();
	    	while(deptIt.hasNext()){
	    		DeptDb deptDb = (DeptDb)deptIt.next();
	    		deptCodes.add(deptDb.getCode());//获得用户所在部门
	    	}
	    	if(deptCodes!=null && deptCodes.size()>0){
	    			for(String deptCode:deptCodes){
	    				Vector  userVec = dud.list(deptCode); //获得部门下所有用户
	    				Iterator userIt = userVec.iterator();
	    				while(userIt.hasNext()){
	    					DeptUserDb dudChild = (DeptUserDb)userIt.next();
	    					String un = dudChild.getUserName();
	    					UserDb ud = new UserDb(un);
	    					String[] admindepts = ud.getAdminDepts();
	    					if(admindepts.length == 0){
	    						String dCode = dudChild.getDeptCode();
	    						DeptDb dd = new DeptDb(dCode);
	    						UserDb userDb = new UserDb(un);
	    						JSONObject jsonUser = new JSONObject();
	    						jsonUser.put("id", i++);
	    						jsonUser.put("dCode",dd.getCode());
	    						jsonUser.put("dName", dd.getName());
	    						jsonUser.put("userName",userDb.getName());
	    						jsonUser.put("realName", userDb.getRealName());
	    						jsonUser.put("type",BaseAction.USER);
	    						jsonArr.put(jsonUser);
	        					
	    					}
	    				}
	    				
	    			}
	    	}
	    	if(jsonArr.length()>0){
				json.put(BaseAction.RETURNCODE,BaseAction.RESULT_SUCCESS);
				json.put(BaseAction.DATAS, jsonArr);
			}else{
				json.put(BaseAction.RETURNCODE,BaseAction.RESULT_NO_DATA);
			}
	    	json.put("parentCode", "-1");
			json.put("parentName", "");
			
	    	return json;
	    	
	    }

	
}
