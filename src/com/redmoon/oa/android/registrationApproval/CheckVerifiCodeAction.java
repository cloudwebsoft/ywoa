package com.redmoon.oa.android.registrationApproval;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.Conn;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.android.tools.Des3;
import com.redmoon.oa.android.verificationCode.VerificationCodeMgr;
import com.redmoon.oa.android.xinge.SendNotice;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.verificationCode.HttpClientVerificationCode;


/**
 * @author lichao
 * 注册数据验证处理接口
 */
public class CheckVerifiCodeAction {
	private static int RETURNCODE_REGIST_SUCCESS = 0;          //注册成功
	private static int RETURNCODE_DUPLICATE_REALNAME = 1;      //姓名重复
	private static int RETURNCODE_REGISTED_NAME = 2;           //手机号已经注册
	private static int RETURNCODE_EXPIRE_VERIFI = 3;	       //验证码已过期
	private static int RETURNCODE_ERROR_VERIFI = 4;		       //验证码错误
	
	private static int TYPE_LOCAL = 0 ;                        //短信发送方，本地
	private static int TYPE_YIMIHOME = 1 ;                     //短信发送方，云端
	
	private String realname = "";
	private String mobile = "";
	private String password = "";
	private String verificationCode = "";
	private int type;
	private String result = "";
	
	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public String getPassword() {
		return password;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
	
	public String execute() {
		JSONObject jSend = new JSONObject();
		JSONObject jReturn = new JSONObject(); 
		JSONObject jResult = new JSONObject();

		//先验证验证码是否过期 ，根据type区分是本地发送的验证码 ，还是云端
		try {
			int result = 0; //验证码有效

			if (type == TYPE_YIMIHOME) {
				Config cg = new Config();
				String yimihomeURL = cg.get("yimihome_url");
				
				HttpClientVerificationCode he = new HttpClientVerificationCode();

				jSend.put("name",getMobile());
				jSend.put("verificationCode",getVerificationCode());
				
				jReturn = he.checkVerificationCode(yimihomeURL + "/httpClientServer/httpclient_server_check_verification_code.jsp",	jSend);

				result = jReturn.getInt("result");
				jReturn.remove("remark");
			} else if (type == TYPE_LOCAL) {
				VerificationCodeMgr vr = new VerificationCodeMgr();
				result = vr.checkVerificationCodeValid(getMobile(),	getVerificationCode());
			}

			if (result == RETURNCODE_EXPIRE_VERIFI) {
				jReturn.put("res", 0);
				jReturn.put("msg", "验证码已过期");
				jResult.put("returnCode", RETURNCODE_EXPIRE_VERIFI);
				jReturn.put("result", jResult);

				setResult(jReturn.toString());
				return "SUCCESS";
			} else if (result == RETURNCODE_ERROR_VERIFI) {
				jReturn.put("res", 0);
				jReturn.put("msg", "验证码错误");
				jResult.put("returnCode", RETURNCODE_ERROR_VERIFI);
				jReturn.put("result", jResult);

				setResult(jReturn.toString());
				return "SUCCESS";
			}
		} catch (JSONException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (IOException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		
		// 再验证手机号和姓名是否注册过
		String sql = "select mobile from users where mobile=?";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;

		try {
			ri = jt.executeQuery(sql, new Object[] { getMobile() });
			if (ri.hasNext()) {
				jReturn.put("res",0);
				jReturn.put("msg","手机号已经注册过");
				jResult.put("returnCode", RETURNCODE_REGISTED_NAME);
				jReturn.put("result", jResult);
				
				setResult(jReturn.toString());
				return "SUCCESS";
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			jt.close();
		}
		
		sql = "select name from users where name=?";
		jt = new JdbcTemplate();
		ri = null;
		
		boolean flag = false; 
		String reName = getRealname();
		try {
			ri = jt.executeQuery(sql, new Object[] { getRealname() });
			if (ri.hasNext()) {
				flag = true;
				ResultIterator ror = null;
				for(int n=2; n<=20; n++){
					reName = reName + String.valueOf(n);
					ror = jt.executeQuery(sql, new Object[] { reName });
					if(!ror.hasNext()){
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			jt.close();
		}
		
		String decrypPassWord = "";
		
		try {
			decrypPassWord = Des3.decode(password);
		} catch (Exception e) {
			e.printStackTrace();
		}

		int isPass =0;
		boolean re = false;

		UserDb ub = new UserDb();
		re = ub.create(reName, getRealname(), decrypPassWord, getMobile(), "", isPass);

		try {
			if(re){
				if(flag){
					jReturn.put("res",0);
					jReturn.put("msg","注册成功");
					jResult.put("returnCode", RETURNCODE_DUPLICATE_REALNAME);
					jResult.put("returnName", reName);
					jReturn.put("result", jResult);
				}else{
					jReturn.put("res",0);
					jReturn.put("msg","注册成功");
					jResult.put("returnCode", RETURNCODE_REGIST_SUCCESS);
					jReturn.put("result", jResult);
				}
				
				//注册成功后，向oa_message表中插入待审批消息
		        String QUERY_CREATE ="insert into oa_message (id,title,content,sender,receiver,type,ip,rq,box,receivers_all,is_sent,send_time,receipt_state,msg_level,action,action_type,action_sub_type) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				
		        Conn conn = null;
		        try {
		            conn = new Conn(Global.getDefaultDB());
		            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
		            
		            int id = (int) SequenceManager.nextID(SequenceManager.OA_MESSAGE);
		            ps.setInt(1, id);
		            ps.setString(2, "手机端注册用户审核");
		            ps.setString(3, "请审核手机端注册用户:"+ getRealname()+"。");
		            ps.setString(4, "系统");
		            ps.setString(5, "admin");
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
		            ps.setString(15, "examine=0|name="+reName);
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
			}else{
				jReturn.put("res",-1);
				jReturn.put("msg","注册失败");
				jResult.put("returnCode", "");
				jReturn.put("result", jResult);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		
		setResult(jReturn.toString());
		return "SUCCESS";
	}	
}
