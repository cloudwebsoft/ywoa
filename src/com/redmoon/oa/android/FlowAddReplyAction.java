package com.redmoon.oa.android;


import java.util.Date;

import org.json.*;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowAnnexDb;
import com.redmoon.oa.flow.WorkflowAnnexMgr;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.person.UserDb;


 /**
 * @Description: 流程添加附言
 * @author: lichao
 * @Date: 2015-9-18上午10:41:40
 */
public class FlowAddReplyAction {
	private static int RES_SUCCESS = 0;                      //成功
	private static int RES_FAIL = -1;                        //失败
	private static int RES_EXPIRED = -2;                     //SKEY过期

	private String skey = "";
	private int flow_id ;
	private int myActionId ;
	private String content = "";
	private int is_secret ;
	private String result = "";
	
	private int progress = 0;
	
	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	public int getFlow_id() {
		return flow_id;
	}

	public void setFlow_id(int flowId) {
		flow_id = flowId;
	}

	public int getMyActionId() {
		return myActionId;
	}

	public void setMyActionId(int myActionId) {
		this.myActionId = myActionId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getIs_secret() {
		return is_secret;
	}

	public void setIs_secret(int isSecret) {
		is_secret = isSecret;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String execute() {
		boolean flag = true;
		JSONObject jReturn = new JSONObject();
		JSONObject jResult = new JSONObject();
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(skey);

		if(re){
			try {
				jReturn.put("res",RES_EXPIRED);
				jResult.put("returnCode", "");
				jReturn.put("result", jResult);
				
				setResult(jReturn.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		WorkflowAnnexMgr workflowAnnexMgr = new WorkflowAnnexMgr();
		String name = privilege.getUserName(skey);
		try {
			String sql = "insert into flow_annex (id,flow_id,content,user_name,reply_name,add_date,action_id,is_secret) values(?,?,?,?,?,?,?,?)";
			long id = SequenceManager.nextID(SequenceManager.OA_FLOW_ANNEX);
			if(myActionId != 0){
				//@流程
				MyActionDb mad = new MyActionDb(myActionId);
				long flow_id = mad.getFlowId();
				long action_id = mad.getActionId();
				String reply_name = mad.getUserName();
				re = workflowAnnexMgr.create(sql, new Object[]{id,flow_id,content,name,reply_name,new Date(),action_id,is_secret});
			}else{
				//普通流程
				sql = "insert into flow_annex (id,flow_id,content,user_name,reply_name,add_date,action_id,is_secret,parent_id,progress) values(?,?,?,?,?,?,?,?,?,?)";
				re = workflowAnnexMgr.create(sql, new Object[]{id,flow_id,content,name,name,new Date(),0,0,-1,progress});
				
				if (re) {
					WorkflowDb wf = new WorkflowDb(flow_id);
					
					// 写入进度
					Leaf lf = new Leaf();
					lf = lf.getLeaf(wf.getTypeCode());
					String formCode = lf.getFormCode();
					FormDb fd = new FormDb();
					fd = fd.getFormDb(formCode);
					// 进度为0的时候不更新
					if (fd.isProgress() && progress>0) {
						com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
						fdao = fdao.getFormDAO(flow_id, fd);
						fdao.setCwsProgress(progress);
						fdao.save();
					}					
				}
			}
			if(re){
				jReturn.put("res", RES_SUCCESS);
				jResult.put("returnCode", RES_SUCCESS);
				UserDb userDb = new UserDb(name);
				jResult.put("annexName", userDb.getRealName());
				jReturn.put("result", jResult);
			}			
		} catch (JSONException e) {
			flag = false;
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (Exception e) {
			flag = false;
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally{
			if(!flag){
				try {
					jReturn.put("res", RES_FAIL);
					jResult.put("returnCode", "");
					jReturn.put("result", jResult);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		
		setResult(jReturn.toString());
		return "SUCCESS";
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int getProgress() {
		return progress;
	}
}
