package com.cloudweb.oa.utils;

import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.entity.UserSetup;
import com.cloudweb.oa.service.IUserSetupService;
import com.cloudwebsoft.framework.util.HttpFileUpTool;
import com.cloudwebsoft.framework.util.LogUtil;
import com.getui.push.v2.sdk.ApiHelper;
import com.getui.push.v2.sdk.GtApiConfiguration;
import com.getui.push.v2.sdk.api.PushApi;
import com.getui.push.v2.sdk.common.ApiResult;
import com.getui.push.v2.sdk.dto.CommonEnum;
import com.getui.push.v2.sdk.dto.req.Audience;
import com.getui.push.v2.sdk.dto.req.Settings;
import com.getui.push.v2.sdk.dto.req.Strategy;
import com.getui.push.v2.sdk.dto.req.message.PushChannel;
import com.getui.push.v2.sdk.dto.req.message.PushDTO;
import com.getui.push.v2.sdk.dto.req.message.PushMessage;
import com.getui.push.v2.sdk.dto.req.message.android.AndroidDTO;
import com.getui.push.v2.sdk.dto.req.message.android.GTNotification;
import com.getui.push.v2.sdk.dto.req.message.android.ThirdNotification;
import com.getui.push.v2.sdk.dto.req.message.android.Ups;
import com.getui.push.v2.sdk.dto.req.message.ios.Alert;
import com.getui.push.v2.sdk.dto.req.message.ios.Aps;
import com.getui.push.v2.sdk.dto.req.message.ios.IosDTO;
import com.redmoon.oa.Config;
import com.redmoon.oa.android.xinge.SendNotice;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserSetupDb;
import com.redmoon.oa.sys.DebugUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class GtPushUtil {

    private static PushApi pushApi;
    private static GtPushUtil gtPushUtil;
    private static Object initLock = new Object();

    public GtPushUtil() {
        pushApi = getPushApi();
    }

    public static GtPushUtil getInstance() {
        if (gtPushUtil == null) {
            synchronized (initLock) {
                gtPushUtil = new GtPushUtil();
            }
        }
        return gtPushUtil;
    }

    public PushApi getPushApi() {
        Config cfg = Config.getInstance();
        boolean gtIsEnabled = cfg.getBooleanProperty("gtIsEnabled");
        if (gtIsEnabled) {
            String gtAppId = cfg.get("gtAppId");
            String gtAppKey = cfg.get("gtAppKey");
            String gtMasterSecret = cfg.get("gtMasterSecret");
            if ("".equals(gtAppId) || "".equals(gtAppKey) || "".equals(gtMasterSecret)) {
                System.err.println("个推初始化参数不能为空");
                return null;
            }

            GtApiConfiguration apiConfiguration = new GtApiConfiguration();
            // 填写应用配置
            apiConfiguration.setAppId(cfg.get("gtAppId"));
            apiConfiguration.setAppKey(cfg.get("gtAppKey"));
            apiConfiguration.setMasterSecret(cfg.get("gtMasterSecret"));
            // 接口调用前缀，请查看文档: 接口调用规范 -> 接口前缀, 可不填写appId
            apiConfiguration.setDomain("https://restapi.getui.com/v2/");
            // 实例化ApiHelper对象，用于创建接口对象
            ApiHelper apiHelper = ApiHelper.build(apiConfiguration);
            // 创建对象，建议复用。目前有PushApi、StatisticApi、UserApi
            return apiHelper.creatApi(PushApi.class);
        }
        else {
            LogUtil.getLog(getClass()).info("个推未启用");
            return null;
        }
    }

    public void push(String userName, String title, String content, int id) {
        if ("".equals(userName)) {
            return;
        }

        // 过滤掉content中的html代码
        content = StrUtil.getAbstract(null, content, 100, "", false);

        IUserSetupService userSetupService = SpringUtil.getBean(IUserSetupService.class);
        UserSetup userSetup = userSetupService.getUserSetup(userName);
        if (userSetup == null) {
            return;
        }

        if (null == userSetup.getCid()) {
            DebugUtil.e(GtPushUtil.class, userName, "cid is null");
            return;
        }

        MessageDb md = new MessageDb();
        md = (MessageDb) md.getMessageDb(id);
        push(userSetup.getCid(), title, content, md);
    }

    public JSONObject getPayload(String title, MessageDb md) {
        // 注意在android手机客户端custom中不能有int型，否则小米会过滤掉，H5+中个推是否有此同样问题？
        JSONObject custom = new JSONObject();

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

            custom.put("type", String.valueOf(SendNotice.TYPE_FLOW));
            custom.put("id", String.valueOf(myactionId));
            custom.put("flowType", String.valueOf(flowType));
            custom.put("flowName", flowName);
            custom.put("realName", realName);
            custom.put("title", flowName);
        } else if (!"".equals(action) && action.contains("flowId=")) {
            String flowId = action.substring(action.indexOf("flowId=") + "flowId=".length(), action.length());

            WorkflowDb wb = new WorkflowDb();
            wb = wb.getWorkflowDb(Integer.parseInt(flowId));

            String flowName = wb.getTitle();

            custom.put("type", String.valueOf(SendNotice.TYPE_FLOW_END));
            custom.put("id", String.valueOf(flowId));
            custom.put("flowName", flowName);
            custom.put("title", flowName);
        } else if (!"".equals(action) && action.contains("examine=")) {
            String name = action.substring(action.indexOf("name=") + "name=".length(), action.length());
            String result = action.substring(action.indexOf("examine=") + "examine=".length(), action.indexOf("|"));

            int resultInt = Integer.parseInt(result);

            //0 等待审核    1 审核通过   2审核不通过
            if (resultInt == 0) {
                UserDb usb = new UserDb();
                usb = usb.getUserDb(name);

                custom.put("type", String.valueOf(SendNotice.TYPE_CHECK));
                custom.put("id", name);
                custom.put("mobile", usb.getMobile());
                custom.put("realName", usb.getRealName());
            } else if (resultInt == 1) {
                custom.put("type", String.valueOf(SendNotice.TYPE_CHECK_PASS));
                custom.put("id", name);
            } else if (resultInt == 2) {
                custom.put("type", String.valueOf(SendNotice.TYPE_CHECK_NOT_PASS));
                custom.put("id", name);
            }
        } else if (!"".equals(action) && action.contains("noticeId=")) {
            String noticeId = action.substring("noticeId=".length());
            custom.put("type", String.valueOf(SendNotice.TYPE_NOTICE));
            custom.put("id", String.valueOf(noticeId));
            custom.put("title", title);
        } else if (!"".equals(action) && action.contains("send_id=")) {
            custom.put("type", String.valueOf(SendNotice.TYPE_NOTICE_OTHER));
            custom.put("id", String.valueOf(md.getId()));
            custom.put("title", title);
        } else if ("".equals(action)) {
            custom.put("type", String.valueOf(SendNotice.TYPE_NOTICE_OTHER));
            custom.put("id", String.valueOf(md.getId()));             //oa_message 表id
            custom.put("title", title);
        }
        return custom;
    }

    /**
     * 推送安卓消息通知
     * @param cid
     * @param title
     * @param body
     * @param md
     */
    public void pushAndroid(String cid, String title, String body, MessageDb md) {
        // 根据cid进行单推
        PushDTO<Audience> pushDTO = new PushDTO<Audience>();
        // 设置推送参数
        pushDTO.setRequestId(System.currentTimeMillis() + "");
        PushMessage pushMessage = new PushMessage();
        pushDTO.setPushMessage(pushMessage);
        GTNotification notification = new GTNotification();
        pushMessage.setNotification(notification);

        notification.setTitle(title);
        notification.setBody(body);

        // 打开应用内特定页面
        notification.setClickType(CommonEnum.ClickTypeEnum.TYPE_INTENT.type);

        JSONObject custom = getPayload(title, md);
        notification.setIntent("intent:#Intent;action=android.intent.action.oppopush;launchFlags=0x14000000;component=com.redmoon.xiaocai/io.dcloud.PandoraEntry;S.UP-OL-SU=true;S.title=" + title + ";S.content=" + body + ";S.payload=" + custom.toString() + ";end");

        // 打开url
        // notification.setClickType("url");
        // notification.setUrl(url); // "https://www.getui.com"

        // 设置接收人信息
        Audience audience = new Audience();
        pushDTO.setAudience(audience);
        audience.addCid(cid);

        // 进行cid单推
        ApiResult<Map<String, Map<String, String>>> apiResult = pushApi.pushToSingleByCid(pushDTO);
        if (apiResult.isSuccess()) {
            // success
            DebugUtil.i(getClass(), "push", apiResult.getData().toString());
        } else {
            // failed
            DebugUtil.e(getClass(), "", "push:" + apiResult.getCode() + ", msg: " + apiResult.getMsg());
        }
    }

    public void push(String cid, String title, String body, MessageDb md) {
        //根据cid进行单推
        PushDTO<Audience> pushDTO = new PushDTO<Audience>();
        // 设置推送参数
        pushDTO.setRequestId(System.currentTimeMillis() + "");//requestid需要每次变化唯一
        //配置推送条件
        // 1: 表示该消息在用户在线时推送个推通道，用户离线时推送厂商通道;
        // 2: 表示该消息只通过厂商通道策略下发，不考虑用户是否在线;
        // 3: 表示该消息只通过个推通道下发，不考虑用户是否在线；
        // 4: 表示该消息优先从厂商通道下发，若消息内容在厂商通道代发失败后会从个推通道下发。
        Strategy strategy=new Strategy();
        strategy.setDef(1);
        Settings settings=new Settings();
        settings.setStrategy(strategy);
        pushDTO.setSettings(settings);
        settings.setTtl(3600000);//消息有效期，走厂商消息需要设置该值
        //推送苹果离线通知标题内容
        Alert alert=new Alert();
        alert.setTitle(title);
        alert.setBody(body);
        Aps aps = new Aps();
        //1表示静默推送(无通知栏消息)，静默推送时不需要填写其他参数。
        //苹果建议1小时最多推送3条静默消息
        aps.setContentAvailable(0);
        aps.setSound("default");
        aps.setAlert(alert);
        IosDTO iosDTO = new IosDTO();
        iosDTO.setAps(aps);
        iosDTO.setType("notify");
        //ios透传paload在这里也要传
        // iosDTO.setPayload("{\"ams_Type\":\"101\",\"pageParams\":{\"CAB_ID\": \"fbcc51cd-f291-4b06-8c7a-9ed67e884351\",\"CAB_BasicID\": \"1017A71000000009YWIX\",\"currentActive\": \"0\"}}");
        JSONObject custom = getPayload(title, md);
        iosDTO.setPayload(custom.toString());

        PushChannel pushChannel = new PushChannel();
        pushChannel.setIos(iosDTO);

        //安卓离线厂商通道推送消息体
        // PushChannel pushChannel = new PushChannel();
        AndroidDTO androidDTO = new AndroidDTO();
        Ups ups = new Ups();
        ThirdNotification notification1 = new ThirdNotification();;
        ups.setNotification(notification1);
        notification1.setTitle(title);
        notification1.setBody(body);
        notification1.setClickType("intent");
        // 示例，需将io.dcloud.HBuilder改为自己的包名
        // notification1.setIntent("intent:#Intent;launchFlags=0x04000000;action=android.intent.action.oppopush;component=io.dcloud.HBuilder/io.dcloud.PandoraEntry;S.UP-OL-SU=true;S.title=测试标题;S.content=测试内容;S.payload=test;end");
        notification1.setIntent("intent:#Intent;action=android.intent.action.oppopush;launchFlags=0x14000000;component=com.redmoon.xiaocai/io.dcloud.PandoraEntry;S.UP-OL-SU=true;S.title=" + title + ";S.content=" + body + ";S.payload=" + custom.toString() + ";end");

        //各厂商自有功能单项设置
        //ups.addOption("HW", "/message/android/notification/badge/class", "io.dcloud.PandoraEntry ");
        //ups.addOption("HW", "/message/android/notification/badge/add_num", 1);
        //ups.addOption("HW", "/message/android/notification/importance", "HIGH");
        //ups.addOption("VV","classification",1);
        androidDTO.setUps(ups);
        pushChannel.setAndroid(androidDTO);

        pushDTO.setPushChannel(pushChannel);

        // PushMessage在线走个推通道才会起作用的消息体
        PushMessage pushMessage = new PushMessage();
        pushDTO.setPushMessage(pushMessage);
        pushMessage.setTransmission(" {title:\"" + title + "\",content:\"" + body + "\",payload:\"" + custom.toString() + "\"}");
        // 设置接收人信息
        Audience audience = new Audience();
        pushDTO.setAudience(audience);
        audience.addCid(cid);

        // 进行cid单推，个推偶尔会报NullPointerException，故需try catch
        try {
            ApiResult<Map<String, Map<String, String>>> apiResult = pushApi.pushToSingleByCid(pushDTO);
            if (apiResult.isSuccess()) {
                // success
                DebugUtil.i(getClass(), "push success", "cid:" + cid + " userName:" + md.getReceiver() + "code:" + apiResult.getCode() + ", msg: " + apiResult.getMsg());
            } else {
                // failed
                DebugUtil.i(getClass(), "push failed", "cid:" + cid + " userName:" + md.getReceiver() + " code:" + apiResult.getCode() + ", msg: " + apiResult.getMsg());
            }
        }
        catch (Exception e) {
            DebugUtil.e(getClass(), "gt push error", e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        }
    }

}
