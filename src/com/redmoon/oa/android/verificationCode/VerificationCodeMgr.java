package com.redmoon.oa.android.verificationCode;

import org.json.JSONException;
import org.json.JSONObject;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.sms.SMSFactory;

import cn.js.fan.db.*;
import cn.js.fan.util.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description: 短信验证码业务逻辑类
 * @author: lichao
 * @Date: 2015-8-6下午02:43:08
 */
public class VerificationCodeMgr {
	private static int VALID_VERIFI = 0;       //有效验证码
	private static int EXPIRE_VERIFI = 3;      //验证码过期
	private static int ERROR_VERIFI = 4;       //验证码错误
	
	//是否是新手机号来验证
	public boolean isNew(String name){
		boolean res = true;
		String sql = "select id from verification_code_record where name=?";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		
		try{
			ri = jt.executeQuery(sql,new Object[]{name});
			if(ri.hasNext()){
				res = false;
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			jt.close();
		}
		
		return res;
	}
	
	//新建一条验证记录
	public JSONObject create(String name, String enterpriseNo) throws JSONException, ClassNotFoundException, ErrMsgException {
		boolean re = false;		
		JSONObject jot = new JSONObject();
		
		VerificationCodeConfig vg = new VerificationCodeConfig();
		String expireSecond = vg.getExpireSecond();
		
		VerificationCodeDb vb = new VerificationCodeDb();
		
		String verificationCode = makeVerificationCode();
		Date createTime = new java.util.Date();
		
        long time = createTime.getTime()+ Integer.parseInt(expireSecond)*1000; //毫秒数，乘以1000
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String expireTime = sdf.format(time);

		try {
			re = vb.create(new JdbcTemplate(), new Object[] { name, verificationCode, createTime, expireTime , enterpriseNo});
		} catch (ResKeyException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			jot.put("result", -1);
			jot.put("verificationCode", "");
			return jot;
		}
		
		if(re){
			jot.put("result", 0);
			jot.put("verificationCode",verificationCode);
			
			//发送验证码短信
			re = SMSFactory.getMsgUtil().send(name,"【" + com.redmoon.oa.Config.getInstance().get("enterprise") + "】验证码：" + verificationCode + "，请您尽快完成登录。如非本人操作，请忽略此短信。","");
			
			if(!re){
				jot.put("result", -1);
				jot.put("verificationCode", "");
				return jot;
			}else{
				System.out.println("-----手机"+name +",【" + com.redmoon.oa.Config.getInstance().get("enterprise") + "】验证码:" + verificationCode);
			}
		}

		return jot;
	}
	
	//生产随机4位验证码
	public static String  makeVerificationCode() throws NumberFormatException ,IndexOutOfBoundsException,IllegalArgumentException{
		String code = "";

		VerificationCodeConfig vg = new VerificationCodeConfig();
		String verificationCodeStr = vg.getVerificationCodeStr();
		String verificationCodeNum = vg.getVerificationCodeNum();
		
	    Random random = new Random(); 
	    for (int i = 1; i <= Integer.parseInt(verificationCodeNum); i++) { 
	    	char rand = verificationCodeStr.charAt(random.nextInt(verificationCodeStr.length())); 
	    	code += rand; 
	     } 

		return code;
	}
	
	//生成新验证码
	public JSONObject setNewVerificationCode(String name, String enterpriseNo) throws ResKeyException, ErrMsgException, JSONException{
		boolean re = false;
		JSONObject jot = new JSONObject();
		
		VerificationCodeConfig vg = new VerificationCodeConfig();
		String expireSecond = vg.getExpireSecond();
		
		String sql = "update verification_code_record set verificationCode = ? ,createTime=? ,expireTime=? where name=?";
		JdbcTemplate jt = new JdbcTemplate();
		
		Date createTime = new java.util.Date();
        long time=createTime.getTime()+ Integer.parseInt(expireSecond)*1000; //毫秒数，乘以1000
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String expireTime = sdf.format(time);
		String verificationCode = makeVerificationCode();
		
		int n =-1;
		try{
			n = jt.executeUpdate(sql,new Object[]{verificationCode, createTime , expireTime ,name});
			if(n==1){
				re = true;
			}
		}catch(Exception e){
			e.printStackTrace();
			jot.put("result", -1);
			jot.put("verificationCode", "");
			return jot;
		}
		
		if(re){
			jot.put("result", 0);
			jot.put("verificationCode",verificationCode);
			
			//发送验证码短信
			re = SMSFactory.getMsgUtil().send(name,"【" + com.redmoon.oa.Config.getInstance().get("enterprise") + "】验证码：" + verificationCode + "，请您尽快完成登录。如非本人操作，请忽略此短信。","");
			
			if(!re){
				jot.put("result", -1);
				jot.put("verificationCode", "");
				return jot;
			}else{
				System.out.println("-----手机"+name +",【" + com.redmoon.oa.Config.getInstance().get("enterprise") + "】验证码:" + verificationCode);
			}
		}
		
		return jot;
	}
	
	//检查验证码时效性
	public int checkVerificationCodeValid(String name ,String verificationCode){
		int re = VALID_VERIFI; //验证码有效
		
		VerificationCodeMgr  vr = new VerificationCodeMgr();
		if(vr.isNew(name)){
			re = ERROR_VERIFI; //验证码错误
			return re;
		}

		String sql = "select verificationCode from verification_code_record where name='"+name+"' and now() < expireTime ";
		String OldVerificationCode ="";
		
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		ResultRecord rd = null;

		try{
			ri = jt.executeQuery(sql);

			if(ri.hasNext()){
				rd = (ResultRecord)ri.next();
				OldVerificationCode = rd.getString("verificationCode");
				if(!verificationCode.trim().equals(OldVerificationCode.trim())){
					re = ERROR_VERIFI; //验证码错误
				}
			}else{
				re = EXPIRE_VERIFI;  //验证码过期
			}
		}catch(Exception e){
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			e.printStackTrace();
		}finally {
			jt.close();
		}
		
		return re;
	}
}
