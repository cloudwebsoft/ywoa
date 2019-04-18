package com.redmoon.oa.android.xinge;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserSetupDb;
import com.tencent.xinge.Message;
import com.tencent.xinge.MessageIOS;
import com.tencent.xinge.XingeApp;


 /**
 * @Description: 手机端消息推送类，分android和ios两种情况分别处理。
 * @author: lichao
 * @Date: 2015-7-24上午11:22:45
 */
public class SendNotice {
	private static int CLIENT_NOT_USE = 0;     		//没有使用过手机app
	private static int CLIENT_ANDROID = 1;     		//安卓
	private static int CLIENT_IOS = 2;         		//苹果
	
	private static int TYPE_FLOW = 1;          		//代办流程
	private static int TYPE_NOTICE = 2;        		//系统通知公告
	private static int TYPE_CHECK = 3;         		//注册审批，仅admin
	private static int TYPE_CHECK_PASS = 4;			//审批结果，通过
	private static int TYPE_CHECK_NOT_PASS = 5;    	//审批结果，不通过
	private static int TYPE_FLOW_END = 6;          	//流程结束
	private static int TYPE_NOTICE_OTHER = 7;       //系统通知公告之外的消息
	
	private int client;
	private String token = "";
	
	private long access_id_android;
	private String secret_key_android;
	
	private long access_id_ios;
	private String secret_key_ios;

    public SendNotice() {
		com.redmoon.oa.Config config = new com.redmoon.oa.Config();
		
		access_id_android = Long.parseLong(config.get("access_id_android"));
		secret_key_android = config.get("secret_key_android");
		access_id_ios = Long.parseLong(config.get("access_id_ios"));
		secret_key_ios = config.get("secret_key_ios");
    }

	//通过token推送消息到单个手机,receiver为一个name
	public JSONObject PushNoticeSingleByToken(String receiver, String title, String content, int id) {	
		if("".equals(receiver)){
			return null;
		}

		UserSetupDb ub = new UserSetupDb();
		ub = ub.getUserSetupDb(receiver);
		client = ub.getClient();
		token = StrUtil.getNullString(ub.getToken());
		
		if (client == CLIENT_NOT_USE || "".equals(token)){
			return null;
		}

		JSONObject jReturn = new JSONObject();
		Map<String, Object> custom = new HashMap<String, Object>();

		MessageDb mb = new MessageDb(id);
		String action = StrUtil.getNullString(mb.getAction());
		
		if (!"".equals(action) && action.contains("myActionId=")) { 
			String myactionId = action.substring(action.indexOf("myActionId=") + "myActionId=".length(), action.length());

			MyActionDb mab = new MyActionDb();
			mab = mab.getMyActionDb(Long.parseLong(myactionId));
			long flowId = mab.getFlowId();
			
			WorkflowDb wb = new WorkflowDb();
			wb = wb.getWorkflowDb(Integer.parseInt(String.valueOf(flowId)));

			String flowName = wb.getTitle();

			String typeCode = wb.getTypeCode();
			Leaf lf = new Leaf();
			lf = lf.getLeaf(typeCode);
			int flowType = lf.getType();

			String uName = mab.getUserName();
			UserDb usb = new UserDb(uName);
			String realName = usb.getRealName();
			
			custom.put("type", TYPE_FLOW);
			custom.put("id", myactionId);
			custom.put("flowType", flowType);
			custom.put("flowName", flowName);
			custom.put("realName", realName);
		}else if (!"".equals(action) && action.contains("flowId=")) { 
			String flowId = action.substring(action.indexOf("flowId=") + "flowId=".length(), action.length());

			WorkflowDb wb = new WorkflowDb();
			wb = wb.getWorkflowDb(Integer.parseInt(flowId));

			String flowName = wb.getTitle();

			custom.put("type", TYPE_FLOW_END);
			custom.put("id", flowId);
			custom.put("flowName", flowName);
		}else if (!"".equals(action) && action.contains("examine=")) {
			String name = action.substring(action.indexOf("name=") + "name=".length(), action.length());
			String result = action.substring(action.indexOf("examine=") + "examine=".length(), action.indexOf("|"));

			int resultInt = Integer.parseInt(result);
			
			//0 等待审核    1 审核通过   2审核不通过
			if(resultInt==0){
				UserDb usb = new UserDb();
				usb = usb.getUserDb(name);
				
				custom.put("type", TYPE_CHECK);
				custom.put("id", name);
				custom.put("mobile", usb.getMobile());
				custom.put("realName", usb.getRealName());
			}else if(resultInt==1){
				custom.put("type", TYPE_CHECK_PASS);
				custom.put("id", name);
			}else if(resultInt==2){
				custom.put("type", TYPE_CHECK_NOT_PASS);
				custom.put("id", name);
			}
		}else if (!"".equals(action) && action.contains("noticeId=")) {
			String noticeId = action.substring("noticeId=".length());
			
			custom.put("type", TYPE_NOTICE);
			custom.put("id",noticeId);       //oa_message 表 id
			custom.put("title",title);
		}else if ("".equals(action)) {
			custom.put("type", TYPE_NOTICE_OTHER);
			custom.put("id",id);             //oa_message 表id
			custom.put("title",title);
		}
		
		if (client == CLIENT_ANDROID) {
			XingeApp xinge = new XingeApp(access_id_android, secret_key_android);
			
			Message message = new Message();
			message.setTitle(title);
			message.setType(Message.TYPE_MESSAGE);
			message.setExpireTime(86400);
			message.setCustom(custom);

			jReturn = xinge.pushSingleDevice(token, message);
		} else if (client == CLIENT_IOS) {
			XingeApp xinge = new XingeApp(access_id_ios, secret_key_ios);
			
			MessageIOS messageIOS = new MessageIOS();
			messageIOS.setExpireTime(86400);
			messageIOS.setAlert(title);
			messageIOS.setBadge(1);
			messageIOS.setSound("beep.wav");
			messageIOS.setCustom(custom);
			
			jReturn = xinge.pushSingleDevice(token, messageIOS, XingeApp.IOSENV_PROD);
		}
		
		
		LogUtil.getLog(getClass()).info("***********" + (client==1?"android":"ios") + "**********");
		LogUtil.getLog(getClass()).info("receiver:" + receiver);
		LogUtil.getLog(getClass()).info("PushNoticeSingleByToken_title:" + title +", " +jReturn);
		
		//System.out.println("***********" + (client==1?"ios":"android") + "**********");
		//System.out.println("receiver:" + receiver);
		//System.out.println("PushNoticeSingleByToken_title:" + title +", " +jReturn);

		return jReturn;
	}
	
	//通过token推送消息到单个手机,receiver为admin
	public JSONObject PushNoticeToAdmin(String receiver, String title, String content) {	
		if("".equals(receiver)){
			return null;
		}
		
		UserSetupDb ub = new UserSetupDb();
		ub = ub.getUserSetupDb(receiver);
		client = ub.getClient();
		token = StrUtil.getNullString(ub.getToken());
		
		if (client == CLIENT_NOT_USE || "".equals(token)){
			return null;
		}
		
		JSONObject jReturn = new JSONObject();
		
		if (client == CLIENT_ANDROID) {
			jReturn = XingeApp.pushTokenAndroid(access_id_ios, secret_key_ios, title, content, token);
		} else if (client == CLIENT_IOS) {
			jReturn = XingeApp.pushTokenIos(access_id_android, secret_key_android, content, token,	0);
		}
		
		LogUtil.getLog(getClass()).info("PushNoticeToAdmin:" + receiver);
		LogUtil.getLog(getClass()).info("PushNoticeToAdmin_title:" + title +", " +jReturn);
		
		//System.out.println("PushNoticeToAdmin:" + receiver);
		//System.out.println("PushNoticeToAdmin_title:" + title +", " +jReturn);

		return jReturn;
	}
	
	//通过token推送消息到多个手机,receiver为name字符串
	//此方法暂时弃用
	public JSONObject PushNoticeManyByToken(String receiverAll, String title, String content, int id) {
		JSONObject jReturn = new JSONObject();
		
		if("".equals(receiverAll)){
			return null;
		}

		UserSetupDb ub = new UserSetupDb();

		String[] ary = receiverAll.split(",");
		for (int n = 0; n < ary.length; n++) {
			ub = ub.getUserSetupDb(ary[n]);
			
			if(ub!=null){
				client = ub.getClient();
				token = StrUtil.getNullString(ub.getToken());
			}

			if (client == CLIENT_NOT_USE || "".equals(token)) {
				continue;
			}

			Map<String, Object> custom = new HashMap<String, Object>();

			MessageDb mb = new MessageDb(id);
			String action = StrUtil.getNullString(mb.getAction());
			String myactionId = "";
			String name = "";

			if (!"".equals(action) && action.contains("myActionId=")) {
				myactionId = action.substring(action.indexOf("myActionId=")	+ "myActionId=".length(), action.length());

				MyActionDb mab = new MyActionDb();
				mab = mab.getMyActionDb(Long.parseLong(myactionId));
				long flowId = mab.getFlowId();

				WorkflowDb wb = new WorkflowDb();
				wb = wb.getWorkflowDb(Integer.parseInt(String.valueOf(flowId)));

				String flowName = wb.getTitle();

				String typeCode = wb.getTypeCode();
				Leaf lf = new Leaf();
				lf = lf.getLeaf(typeCode);
				int flowType = lf.getType();

				String uName = mab.getUserName();
				UserDb usb = new UserDb(uName);
				String realName = usb.getRealName();

				custom.put("type", TYPE_FLOW);
				custom.put("id", myactionId);
				custom.put("flowType", flowType);
				custom.put("flowName", flowName);
				custom.put("realName", realName);
			} else if (!"".equals(action) && action.contains("name=")) {
				name = action.substring(action.indexOf("name=")	+ "name=".length(), action.length());

				UserDb usb = new UserDb();
				usb = usb.getUserDb(name);

				custom.put("type", TYPE_CHECK);
				custom.put("id", name);
				custom.put("mobile", usb.getMobile());
				custom.put("realName", usb.getRealName());
			} else if ("".equals(action)) {
				custom.put("type", TYPE_NOTICE);
				custom.put("id", id);
				custom.put("title", title);
			}

			if (client == CLIENT_ANDROID) {
				XingeApp xinge = new XingeApp(access_id_android, secret_key_android);

				Message message = new Message();
				message.setTitle(title);
				message.setType(Message.TYPE_MESSAGE);
				message.setExpireTime(86400);
				message.setCustom(custom);

				jReturn = xinge.pushSingleDevice(token, message);
			} else if (client == CLIENT_IOS) {
				XingeApp xinge = new XingeApp(access_id_ios, secret_key_ios);

				MessageIOS messageIOS = new MessageIOS();
				messageIOS.setExpireTime(86400);
				messageIOS.setAlert(title);
				messageIOS.setBadge(1);
				messageIOS.setSound("beep.wav");
				messageIOS.setCustom(custom);

				jReturn = xinge.pushSingleDevice(token, messageIOS,	0);
			}
			
			System.out.println("PushNoticeManyByToken_jReturn:" + jReturn);
		}

		return jReturn;
	}
}
