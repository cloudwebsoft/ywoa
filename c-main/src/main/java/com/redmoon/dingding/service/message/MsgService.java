package com.redmoon.dingding.service.message;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.dingding.Config;
import com.redmoon.dingding.domain.*;
import com.redmoon.dingding.enums.Enum;
import com.redmoon.dingding.service.BaseService;
import com.redmoon.dingding.service.department.DepartmentService;
import com.redmoon.dingding.service.user.UserService;
import com.redmoon.dingding.util.DdException;
import com.redmoon.dingding.util.HttpHelper;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.person.UserDb;

import java.util.List;

public class MsgService extends BaseService {
    public static enum MESSAGE_TYPE_ENMU {
        NOTICE, SYSMSG, MSG, FLOW
    }

    private boolean isUseDingDing;
    private int isUserIdUse;
    private String flowAgentId;

    public MsgService() {
        Config cfg = Config.getInstance();
        isUseDingDing = cfg.isUseDingDing();
        isUserIdUse = cfg.isUserIdUse();
        flowAgentId = cfg.getProperty("flowAgentId");
    }

    /**
     * 发送消息
     *
     * @param receiver
     * @param content
     * @param action
     * @param type
     * @return
     */
    public String sendMsg(String receiver, String content, String action, MESSAGE_TYPE_ENMU type) {
        if (type != MESSAGE_TYPE_ENMU.FLOW) {
            return "";
        }
        String taskId = "";
        try {
            if (isUseDingDing) {
                if (type == MESSAGE_TYPE_ENMU.FLOW) {
                    String[] ary = StrUtil.split(action, "\\|");
                    if (ary != null) {
                        String actionType = ary[0];
                        if ("action=flow_dispose".equals(actionType)) {
                            String[] arr = StrUtil.split(ary[1], "=");
                            if ("myActionId".equals(arr[0])) {
                                long myActionId = StrUtil.toLong(arr[1], -1);
                                if (myActionId != -1) {
                                    MyActionDb mad = new MyActionDb();
                                    mad = mad.getMyActionDb(myActionId);
                                    com.redmoon.oa.android.Privilege pvg = new com.redmoon.oa.android.Privilege();
                                    String skey = pvg.getSkey(receiver);
                                    boolean isSSL = Global.getInternetFlag().equals(Global.INTERNET_FLAG_SECURE);

                                    String serverDomain = Global.server;
                                    String serverPort = Global.port;

                                    String rootPath;
                                    String virtualPath = Global.virtualPath;
                                    if (!virtualPath.equals("")) {
                                        if (isSSL) {
                                            rootPath = "https://" + serverDomain + ":" + serverPort + "/" + Global.virtualPath; // "http://www.zjrj.cn";
                                        } else {
                                            if (serverPort.equals("80"))
                                                rootPath = "http://" + serverDomain + "/" + Global.virtualPath; // "http://www.zjrj.cn";
                                            else {
                                                rootPath = "http://" + serverDomain + ":" + serverPort + "/"
                                                        + Global.virtualPath; // "http://www.zjrj.cn";
                                            }
                                        }
                                    } else {
                                        if (isSSL) {
                                            rootPath = "https://" + serverDomain + ":" + serverPort;
                                        } else {
                                            if (serverPort.equals("80"))
                                                rootPath = "http://" + serverDomain;
                                            else
                                                rootPath = "http://" + serverDomain + ":" + serverPort; // "http://www.zjrj.cn";
                                        }
                                    }

                                    String link = rootPath + "/weixin/flow/flow_dispose.jsp?skey=" + skey + "&flowId=" + mad.getFlowId() + "&myActionId=" + myActionId;
                                    content = "<a href='" + link + "'>" + content + "</a>";
                                }
                            }
                        }
                    }
                }

                String _DingUserId = getDdUserId(receiver);
                if (!_DingUserId.equals("")) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.agent_id = flowAgentId;
                    sendMessage.dept_id_list = null;
                    DdMessage ddMessage = new DdMessage();
                    ddMessage.msgtype = "text";
                    TextMessageType textMessageType = new TextMessageType();
                    textMessageType.content = content;
                    ddMessage.text = textMessageType;
                    sendMessage.msg = ddMessage;
                    sendMessage.to_all_user = false;
                    sendMessage.userid_list = _DingUserId;
                    HttpHelper _http = new HttpHelper(URL_ASYNC_URL);

                    MsgTaskDto _dto = _http.httpPost(MsgTaskDto.class, sendMessage);
                    if (_dto != null) {
                        taskId = _dto.task_id;
                    }
                }
            }

        } catch (DdException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return taskId;
    }

    private String getDdUserId(String receiver) {
        String userId = "";
        UserDb _userDb = new UserDb(receiver);
        if (_userDb != null && _userDb.isLoaded()) {
            userId = StrUtil.getNullStr(_userDb.getDingding());
            if (userId.equals("")) {
                switch (isUserIdUse) {
                    case Enum.emBindAcc.emUserName:
                        userId = _userDb.getName();
                        break;
                    case Enum.emBindAcc.emEmail:
                        userId = _userDb.getEmail();
                        break;
                    case Enum.emBindAcc.emMobile:
                        userId = _userDb.getMobile();
                        break;
                }
            }
/*            UserService userService = new UserService();
            DdUser ddUser = userService.getUser(userId);
            // 未同步用户所以无法得知userid
            if (ddUser == null) {
                DepartmentService _deptService = new DepartmentService();
                //获取系统中所有部门
                List<DdDepartment> _list = _deptService.allDepartments();
                for(DdDepartment dd:_list){
                    List<DdUser> users = userService.usersByDept(dd.id);
                    if (users != null && users.size() > 0) {
                        for (DdUser user : users) {
                            if (user.mobile.equals(userId)) {
                                userId = user.userid;
                                break;
                            }
                        }
                    }
                }

            }*/
        }
        return userId;
    }


}
