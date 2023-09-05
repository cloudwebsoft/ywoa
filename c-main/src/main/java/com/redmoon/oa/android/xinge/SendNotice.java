package com.redmoon.oa.android.xinge;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.redmoon.oa.sys.DebugUtil;
import com.tencent.xinge.XingeAppSimple;
import com.tencent.xinge.bean.*;
import com.tencent.xinge.bean.ios.Alert;
import com.tencent.xinge.bean.ios.Aps;
import com.tencent.xinge.push.app.PushAppRequest;
import org.json.JSONException;
import org.json.JSONObject;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserSetupDb;
import com.tencent.xinge.XingeApp;


 /**
 * @Description: 手机端消息推送类，分android和ios两种情况分别处理。
 * @author: lichao
 * @Date: 2015-7-24上午11:22:45
 */
public class SendNotice {
	 public static final int CLIENT_NOT_USE = 0;            //没有使用手机app
	 public static final int CLIENT_ANDROID = 1;            //安卓
	 public static final int CLIENT_IOS = 2;                //苹果

	 public static final int TYPE_FLOW = 1;                //代办流程
	 public static final int TYPE_NOTICE = 2;                //系统通知公告
	 public static final int TYPE_CHECK = 3;                //注册审批，仅admin
	 public static final int TYPE_CHECK_PASS = 4;            //审批结果，通过
	 public static final int TYPE_CHECK_NOT_PASS = 5;        //审批结果，不通过
	 public static final int TYPE_FLOW_END = 6;            //流程结束
	 public static final int TYPE_NOTICE_OTHER = 7;       //系统通知公告之外的消息
	
	private int client;
	private String token = "";
	
	private String access_id_android;
	private String secret_key_android;
	
	private String access_id_ios;
	private String secret_key_ios;

	private String XingeDomainUrl;

    public SendNotice() {
		com.redmoon.oa.Config config = new com.redmoon.oa.Config();
		
		access_id_android = config.get("access_id_android");
		secret_key_android = config.get("secret_key_android");
		access_id_ios = config.get("access_id_ios");
		secret_key_ios = config.get("secret_key_ios");

		XingeDomainUrl = config.get("XingeDomainUrl");
    }

	//通过token推送消息到单个手机,receiver为一个name
	public JSONObject PushNoticeSingleByToken(String receiver, String title, String content, int id) {	
		if("".equals(receiver)) {
			return null;
		}

		// 过滤掉content中的html代码
		content = StrUtil.getAbstract(null, content, 100, "", false);

		UserSetupDb ub = new UserSetupDb();
		ub = ub.getUserSetupDb(receiver);
		client = ub.getClient();
		token = StrUtil.getNullString(ub.getToken());

		if (client == CLIENT_NOT_USE || "".equals(token)){
			return null;
		}

		JSONObject jReturn = new JSONObject();
		// 注意custom中不能有int型，否则小米会过滤掉
		Map<String, Object> custom = new HashMap<String, Object>();

		MessageDb md = new MessageDb();
		md = (MessageDb) md.getMessageDb(id);
		String action = StrUtil.getNullString(md.getAction());
		
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

			// action.setIntent("xgscheme://com.xg.push/flow_dispose?id=" + myactionId + "&flowType=" + flowType + "&flowName=" + flowName + "&realName=" + realName + "&title=" + flowName);

			custom.put("type", String.valueOf(TYPE_FLOW));
			custom.put("id", String.valueOf(myactionId));
			custom.put("flowType", String.valueOf(flowType));
			custom.put("flowName", flowName);
			custom.put("realName", realName);
			custom.put("title", flowName);
		}else if (!"".equals(action) && action.contains("flowId=")) { 
			String flowId = action.substring(action.indexOf("flowId=") + "flowId=".length(), action.length());

			WorkflowDb wb = new WorkflowDb();
			wb = wb.getWorkflowDb(Integer.parseInt(flowId));

			String flowName = wb.getTitle();

			custom.put("type", String.valueOf(TYPE_FLOW_END));
			custom.put("id", String.valueOf(flowId));
			custom.put("flowName", flowName);
			custom.put("title", flowName);
		}else if (!"".equals(action) && action.contains("examine=")) {
			String name = action.substring(action.indexOf("name=") + "name=".length(), action.length());
			String result = action.substring(action.indexOf("examine=") + "examine=".length(), action.indexOf("|"));

			int resultInt = Integer.parseInt(result);
			
			//0 等待审核    1 审核通过   2审核不通过
			if(resultInt==0){
				UserDb usb = new UserDb();
				usb = usb.getUserDb(name);
				
				custom.put("type", String.valueOf(TYPE_CHECK));
				custom.put("id", name);
				custom.put("mobile", usb.getMobile());
				custom.put("realName", usb.getRealName());
			}else if(resultInt==1){
				custom.put("type", String.valueOf(TYPE_CHECK_PASS));
				custom.put("id", name);
			}else if(resultInt==2){
				custom.put("type", String.valueOf(TYPE_CHECK_NOT_PASS));
				custom.put("id", name);
			}
		}else if (!"".equals(action) && action.contains("noticeId=")) {
			String noticeId = action.substring("noticeId=".length());
			custom.put("type", String.valueOf(TYPE_NOTICE));
			custom.put("id", String.valueOf(noticeId));
			custom.put("title",title);
		}
		else if (!"".equals(action) && action.contains("send_id=")) {
			 custom.put("type", String.valueOf(TYPE_NOTICE_OTHER));
			 custom.put("id", String.valueOf(id));
			 custom.put("title",title);
		 }
		else if ("".equals(action)) {
			custom.put("type", String.valueOf(TYPE_NOTICE_OTHER));
			custom.put("id", String.valueOf(id));             //oa_message 表id
			custom.put("title",title);
		}
		
		if (client == CLIENT_ANDROID) {
			/*XingeApp xinge = new XingeApp(access_id_android, secret_key_android);
			Message message = new Message();
			message.setTitle(title);
			message.setType(Message.TYPE_MESSAGE);
			message.setExpireTime(86400);
			message.setCustom(custom);
			jReturn = xinge.pushSingleDevice(token, message);*/

			// 对于华为通道，覆盖消息时携带自定义参数需要使用 intent 方式，如使用 custom_content 方式携带自定义参数，接口层会进行拦截
			MessageAndroid messageAndroid = new MessageAndroid();
			// custom_content 参数需要序列化为 json string，上面一行为JSON.toString的结果，下面一行为直接custom.toString的结果
			// {"id":"457","flowType":"1","title":"@admin   1123123213","realName":"管理员","type":"1","flowName":"@admin   1123123213"}
			// {id=457, flowType=1, title=@admin   1123123213, realName=管理员, type=1, flowName=@admin   1123123213}
			// DebugUtil.i(getClass(), "PushNoticeSingleByToken", JSON.toJSONString(custom) + "--" + custom.toString());
			messageAndroid.setCustom_content(JSON.toJSONString(custom)); // custom.toString());
			Message message = new Message();
			message.setTitle(title);
			message.setContent(content);
			message.setAndroid(messageAndroid);

			XingeApp xingeApp = new XingeApp.Builder()
					.appId(access_id_android)
					.secretKey(secret_key_android)
					// .domainUrl("https://api.tpns.tencent.com/") // 广州接入点
					.domainUrl(XingeDomainUrl) // "https://api.tpns.sh.tencent.com/" 上海接入点
        			.build();

			PushAppRequest pushAppRequest = new PushAppRequest();
			pushAppRequest.setMessage(message);
			pushAppRequest.setPlatform(Platform.android);
			// nofity为发送通知，而message为发送消息
			// pushAppRequest.setMessage_type(MessageType.notify);
			pushAppRequest.setMessage_type(MessageType.message);
			pushAppRequest.setAudience_type(AudienceType.token_list);
			ArrayList<String> list = new ArrayList<String>();
			list.add(token);
			pushAppRequest.setToken_list(list);
			jReturn = xingeApp.pushApp(pushAppRequest);
		} else if (client == CLIENT_IOS) {
			/*XingeApp xinge = new XingeApp(access_id_ios, secret_key_ios);
			MessageIOS messageIOS = new MessageIOS();
			messageIOS.setExpireTime(86400);
			messageIOS.setAlert(title);
			messageIOS.setBadge(1);
			messageIOS.setSound("beep.wav");
			messageIOS.setCustom(custom);
			jReturn = xinge.pushSingleDevice(token, messageIOS, XingeApp.IOSENV_PROD);*/

			MessageIOS messageIOS = new MessageIOS();
			Aps aps = new Aps();
			Alert alert = new Alert();
			alert.setTitle(title);
			alert.setBody(content);
			aps.setAlert(alert);
			aps.setBadge_type(1);
			messageIOS.setAps(aps);
			messageIOS.setCustom(custom.toString());

			Message message = new Message();
			message.setTitle(title);
			message.setContent(content);
			message.setIos(messageIOS);

			XingeApp xingeApp = new XingeApp.Builder()
					.appId(access_id_ios)
					.secretKey(secret_key_ios)
					.domainUrl("https://api.tpns.sh.tencent.com/") // 上海接入点
					.build();

			PushAppRequest pushAppRequest = new PushAppRequest();
			pushAppRequest.setMessage(message);
			pushAppRequest.setPlatform(Platform.ios);
			// 用户指定推送环境，仅限 iOS 平台推送使用
			pushAppRequest.setEnvironment(Environment.product);
			pushAppRequest.setMessage_type(MessageType.message);
			pushAppRequest.setAudience_type(AudienceType.token_list);
			ArrayList<String> list = new ArrayList<String>();
			list.add(token);
			pushAppRequest.setToken_list(list);

			jReturn = xingeApp.pushApp(pushAppRequest);
		}

		if(null != jReturn) {
			try {
				if(null != jReturn.getString("ret_code") && !jReturn.get("ret_code").equals(200) && !jReturn.get("ret_code").equals(0)) {
					DebugUtil.e(getClass(), "PushNoticeSingleByToken", (client==1?"android":"ios") + ", receiver:" + receiver + ", title:" + title +", " + jReturn.toString());
				}
			} catch (JSONException e) {
				LogUtil.getLog(getClass()).error(e);
			}
		}
		DebugUtil.i(getClass(), "PushNoticeSingleByToken", (client==1?"android":"ios") + ", receiver:" + receiver + ", title:" + title +", " + jReturn);

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
			// jReturn = XingeApp.pushTokenAndroid(access_id_ios, secret_key_ios, title, content, token);
		} else if (client == CLIENT_IOS) {
			// jReturn = XingeApp.pushTokenIos(access_id_android, secret_key_android, content, token, 0);
		}
		
		LogUtil.getLog(getClass()).info("PushNoticeToAdmin:" + receiver);
		LogUtil.getLog(getClass()).info("PushNoticeToAdmin_title:" + title +", " +jReturn);
		return jReturn;
	}
	
	//通过token推送消息到多个手机,receiver为name字符串
	//此方法暂时弃用
	/*public JSONObject PushNoticeManyByToken(String receiverAll, String title, String content, int id) {
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
		}
		return jReturn;
	}*/
}
