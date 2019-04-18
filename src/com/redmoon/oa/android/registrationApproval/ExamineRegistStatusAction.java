package com.redmoon.oa.android.registrationApproval;


import java.sql.PreparedStatement;
import java.sql.Timestamp;

import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.Conn;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.Global;

import com.redmoon.oa.android.xinge.SendNotice;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;


/**
 * @author lichao
 * 注册审批接口
 */
public class ExamineRegistStatusAction {
	private static int RETURNCODE_PASS = 1;              //审核通过
	private static int RETURNCODE_NOT_PASS= 2;           //审核不通过
	
	private String name = "";
	private String deptCode = "";
	private int isPass ;
	private String result = "";
	
	public String getDeptCode() {
		return deptCode;
	}

	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}

	public int getIsPass() {
		return isPass;
	}

	public void setIsPass(int isPass) {
		this.isPass = isPass;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String execute() {
		JSONObject jReturn = new JSONObject();
		JSONObject jResult = new JSONObject();
		
		UserDb ud = new UserDb(name);
		
		ud.setIsPass(isPass);
		boolean re = ud.save();

		try {
			if (re) {
				String result="";
				
				jReturn.put("res", 0);
				if (getIsPass() == RETURNCODE_PASS) {
					DeptUserDb dub = new DeptUserDb();
					dub.create(getDeptCode(), ud.getName(), "");
					
					jReturn.put("msg", "审核通过");
					jResult.put("returnCode", RETURNCODE_PASS);
					
					result="审核通过";
				} else if (getIsPass() == RETURNCODE_NOT_PASS) {
					if(ud.del()){
						jReturn.put("msg", "审核不通过");
						jResult.put("returnCode", RETURNCODE_NOT_PASS);
					}
					
					result="审核不通过";
				}
				
				//审批后，向oa_message表中插入审批结果消息
		        String QUERY_CREATE ="insert into oa_message (id,title,content,sender,receiver,type,ip,rq,box,receivers_all,is_sent,send_time,receipt_state,msg_level,action,action_type,action_sub_type) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				
		        Conn conn = null;
		        try {
		            conn = new Conn(Global.getDefaultDB());
		            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
		            
		            int id = (int) SequenceManager.nextID(SequenceManager.OA_MESSAGE);
		            ps.setInt(1, id);
		            ps.setString(2, "手机端注册用户审核结果");
		            ps.setString(3, "手机端注册用户" + ud.getRealName() + "审批结果：" + result + "。");
		            ps.setString(4, "系统");
		            ps.setString(5, ud.getName());
		            ps.setInt(6, 10);
		            ps.setString(7, "");
		            java.util.Date curDate = new java.util.Date();
		            ps.setTimestamp(8, new Timestamp(curDate.getTime()));
		            ps.setInt(9, 0);
		            ps.setString(10, null);
		            ps.setInt(11, 1);
		            ps.setTimestamp(12, new Timestamp(curDate.getTime()));
		            ps.setInt(13, 0);
		            ps.setInt(14, 0);
		            ps.setString(15, "examine="+getIsPass()+"|name="+ud.getName());
					ps.setString(16, null);
					ps.setString(17, null);            
					
		            re = conn.executePreUpdate() == 1 ? true : false;
		            
			        //add by lichao 手机端消息推送
		            if(re){
		        		MessageDb md = new MessageDb(id);
		        		
				        SendNotice se = new SendNotice();
				        se.PushNoticeSingleByToken(md.getReceiver(), md.getTitle(), md.getContent(), id);
		            }
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (conn != null) {
						conn.close();
						conn = null;
					}
				} 
				
				jReturn.put("result", jResult);
			} else {
				jReturn.put("res", -1);
				jReturn.put("msg", "审核错误");
				jResult.put("returnCode", "");
				jReturn.put("result", jResult);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ErrMsgException e) {
			e.printStackTrace();
		}

		setResult(jReturn.toString());
		return "SUCCESS";
	}
}
