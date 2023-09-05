package com.redmoon.oa.flow;

import cn.js.fan.db.Conn;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.NumberUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import com.cloudweb.oa.api.IWorkflowHelper;
import com.cloudweb.oa.api.IWorkflowScriptUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.aop.Pointcut.MethodNamePointcut;
import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.aop.base.Advisor;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.Config;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.flow.strategy.IStrategy;
import com.redmoon.oa.flow.strategy.StrategyMgr;
import com.redmoon.oa.flow.strategy.StrategyUnit;
import com.redmoon.oa.message.IMessage;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.message.MobileAfterAdvice;
import com.redmoon.oa.oacalendar.OACalendarDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.sys.DebugUtil;
import org.apache.commons.lang3.StringUtils;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * workflow_action:410.000000,98.000000,490.000000,138.000000,,179,0,新用户16,2,174,,0,;
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class WorkflowActionDb implements Serializable {
    String connname = "";

    public static final int RESULT_VALUE_AGGREE = 1;
    public static final int RESULT_VALUE_DISAGGREE = -1;
    public static final int RESULT_VALUE_CONTINUE = 2; // 继续
    public static final int RESULT_VALUE_NOT_ACCESSED = -2; // 未处理
    public static final int RESULT_VALUE_RETURN = 3; // 返回
    public static final int RESULT_VALUE_TO_RETUNER = 4; // 直送
    public static final int RESULT_VALUE_READED = 5; // 审阅

    static final String INSERT = "insert into flow_action (id,flow_id,isStart,reason,status,title,userName,internal_name,office_color_index,userRealName,jobCode,jobName,proxyJobCode,proxyJobName,proxyUserName,proxyUserRealName,resultValue,fieldWrite,taskId,dept,flag,nodeMode,mydate,strategy,item1,item2,is_msg) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," + RESULT_VALUE_NOT_ACCESSED + ",?,?,?,?,?,?,?,?,?,?)";
    final String LOAD = "select id,flow_id,isStart,reason,status,title,userName,internal_name,office_color_index,userRealName,jobCode,jobName,proxyJobCode,proxyJobName,proxyUserName,proxyUserRealName,result,checkDate,resultValue,checkUserName,fieldWrite,taskId,dept,flag,nodeMode,strategy,item1,item2,plus,date_delayed,can_distribute,is_msg from flow_action where id=?";
    final String SAVE = "update flow_action set flow_id=?,isStart=?,reason=?,status=?,title=?,userName=?,office_color_index=?,userRealName=?,jobCode=?,jobName=?,proxyJobCode=?,proxyJobName=?,proxyUserName=?,proxyUserRealName=?,result=?,checkDate=?,resultValue=?,checkUserName=?,fieldWrite=?,taskId=?,dept=?,flag=?,nodeMode=?,strategy=?,item1=?,item2=?,plus=?,date_delayed=?,can_distribute=?,is_msg=? where id=?";
    private String userRealName = "-1";
    final String DELETE = "delete from flow_action where id=?";

    /**
     * 未处理
     */
    public static final int STATE_NOTDO = 0;
    /**
     * 忽略/跳过
     */
    public static final int STATE_IGNORED = 1;
    /**
     * 正在办理
     */
    public static final int STATE_DOING = 2;
    /**
     * 被退回
     */
    public static final int STATE_RETURN = 3;
    /**
     * 已处理
     */
    public static final int STATE_FINISHED = 4;
    /**
     * 已放弃
     */
    public static final int STATE_DISCARDED = -1;

    /**
     * 转办
     */
    public static final int STATE_TRANSFERED = 5;
    /**
     * 移交
     */
    public static final int STATE_HANDOVER = 6;

    /**
     * 加签，仅用于MyActionDb的actionStatus(到达时状态)
     */
    public static final int STATE_PLUS = 7;

    /**
     * 从挂起恢复
     */
    public static final int STATE_SUSPEND_OVER = 8;

    /**
     * 被延迟
     */
    public static final int STATE_DELAYED = 9;

    /**
     * 自选用户
     */
    public static final String PRE_TYPE_USER_SELECT = "$userSelect";
    
    /**
     * 自选用户（有权限管理的部门）
     */
    public static final String PRE_TYPE_USER_SELECT_IN_ADMIN_DEPT = "$userSelectInAdminDept";
    
    /**
     * 发起人
     */
    public static final String PRE_TYPE_STARTER = "$starter";
    /**
     * 往前两个节点
     */
    public static final String PRE_TYPE_FORE_ACTION = "$foreAction";
    /**
     * 本人（发起流程）
     */
    public static final String PRE_TYPE_SELF = "$self";
    /**
     * 我的领导
     */
    public static final String PRE_TYPE_MYLEADER = "$myleader";
    /**
     * 我的下属
     */
    public static final String PRE_TYPE_MYSUBORDINATE = "$mysubordinate";
    /**
     * 部门管理员
     */
    public static final String PRE_TYPE_DEPT_MGR = "$deptManager";
    /**
     * 某个节点上的处理人员
     */
    public static final String PRE_TYPE_ACTION_USER = "$action";
    /**
     * 表单中指定的人员
     */
    public static final String PRE_TYPE_FIELD_USER = "$field";
    
    public static final String PRE_TYPE_PROJECT_ROLE = "$projectRole";
    
    public static final String PRE_TYPE_NODE_SCRIPT = "$nodeScript";

    public static final int STRING_ARRAY_LENGTH = 26; // 20060706 至,,,...,,dept,A_END的数组长度

    public static final int NODE_MODE_ROLE = 0; // 表示userName中记录的是role
    public static final int NODE_MODE_USER = 1; // 表示userName中记录的是user
    public static final int NODE_MODE_POST = 4; // 表示userName中记录的是职位

    public static final int NODE_MODE_ROLE_SELECTED = 2; // 表示角色已被选择，人员被确定
    public static final int NODE_MODE_USER_SELECTED = 3; // 表示用户已被选择，人员被确定
    public static final int NODE_MODE_POST_SELECTED = 5; // 表示职位已被选择，人员被确定

    public static final int DIRECTION_DOWN = 0;
    public static final int DIRECTION_PARALLEL_MYDEPT =1; // 本部门及平行部门
    public static final int DIRECTION_UP = 2;
    public static final int DIRECTION_MYDEPT = 3; // 方向为本部门
    public static final int DIRECTION_PARALLEL = 4; // 平行
    public static final int DIRECTION_PARALLEL_MYDEPT_UP = 5; // 本部门、平行，如果找不到，则继续往上

    /**
     * 先本部门，然后上行
     */
    public static final int DIRECTION_MYDEPT_UP = 6;
    
    /**
     * 找上级领导模式，按照先本部门后上行的方式，如果本人角色是指定的角色，则继续往上级部门寻找
     */
    public static final int DIRECTION_MY_LEADER = 7;    

    private int resultValue = RESULT_VALUE_NOT_ACCESSED;

    /**
     * 动作类型：处理
     */
    public static final int KIND_ACCESS = 0;
    /**
     * 动作类型：审阅
     */
    public static final int KIND_READ = 1;

    /**
     * 子流程
     */
    public static final int KIND_SUB_FLOW = 2;

    /**
     * 行文方向关联，默认为default，表示上一节点
     */
    public static final String RELATE_TO_ACTION_DEFAULT = "default";
    /**
     * 关联发起人
     */
    public static final String RELATE_TO_ACTION_STARTER = "starter";
    /**
     * 关联表单中的按字段排序的第一个部门字段
     */
    public static final String RELATE_TO_ACTION_DEPT = "dept";

    /**
     * 跳过方式，默认为0，表示无用户时跳过
     */
    public static final int IGNORE_TYPE_DEFAULT = 0;
    /**
     * 跳过方式，无用户时不允许跳过
     */
    public static final int IGNORE_TYPE_NOT = 1;
    
    /**
     * 跳过方式，无用户或用户之前处理过则跳过
     */
    public static final int IGNORE_TYPE_USER_ACCESSED_BEFORE = 2;
    
    /**
     * 角色比较大小时，不允许跳过 20161124 fgf
     */
    public static final int IGNORE_TYPE_ROLE_COMPARE_NOT = 3;    

    /**
     * 前加签
     */
    public static final int PLUS_TYPE_BEFORE = 0;
    /**
     * 后加签
     */
    public static final int PLUS_TYPE_AFTER = 1;
    /**
     * 并签
     */
    public static final int PLUS_TYPE_CONCURRENT = 2;

    /**
     * 加签时顺序处理
     */
    public static final int PLUS_MODE_ORDER = 0;
    /**
     * 加签时有一人处理完毕，即往下流转
     */
    public static final int PLUS_MODE_ONE = 1;
    /**
     * 加签时全部加签人员都处理完毕才往下流转
     */
    public static final int PLUS_MODE_ALL = 2;
    
    /**
     * 分支允许多选
     */
    public static final int BRANCH_MODE_MULTI = 1;
    
    /**
     * 分支只能单选
     */
    public static final int BRANCH_MODE_SINGLE = 0;

    private String[] returnIds = null; // 用于打回时记录打回的action的id

    public boolean isRelateDeptManager() {
        return relateDeptManager;
    }

    public void setRelateDeptManager(boolean relateDeptManager) {
        this.relateDeptManager = relateDeptManager;
    }

    private boolean relateDeptManager = false;

    public WorkflowActionDb() {
        init();
    }

    public WorkflowActionDb(int id) {
        init();
        this.id = id;
        load();
    }

    public String getResultValueDesc() {
        return getResultValueDesc(resultValue);
    }

    /**
     * 取得resultValue的描述
     * @param resultValue int
     * @return String
     */
    public static String getResultValueDesc(int resultValue) {
        String str = "";
        WorkflowConfig wfcfg = WorkflowConfig.getInstance();
        switch(resultValue) {
        case RESULT_VALUE_AGGREE: str = wfcfg.getProperty("RESULT_VALUE_AGGREE"); break;
        case RESULT_VALUE_DISAGGREE: str = wfcfg.getProperty("RESULT_VALUE_DISAGGREE"); break;
        case RESULT_VALUE_CONTINUE: str = wfcfg.getProperty("RESULT_VALUE_CONTINUE"); break;
        case RESULT_VALUE_NOT_ACCESSED: str = wfcfg.getProperty("RESULT_VALUE_NOT_ACCESSED"); break;
        case RESULT_VALUE_RETURN: str = wfcfg.getProperty("RESULT_VALUE_RETURN"); break;
        case RESULT_VALUE_TO_RETUNER: str = wfcfg.getProperty("RESULT_VALUE_TO_RETUNER"); break;
        case RESULT_VALUE_READED: str = wfcfg.getProperty("RESULT_VALUE_READED"); break;
        }
        return str;
    }

    public void init() {
        flowId = -1;
        isStart = 0;
        connname = Global.getDefaultDB();
        if (connname.equals("")) {
            LogUtil.getLog(getClass()).info("Directory:默认数据库名为空！");
        }
    }

    public void setFlowId(int flowId) {
        this.flowId = flowId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean changeStatusFree(HttpServletRequest request, WorkflowDb wf, String checkUserName, int newstatus, String reason, String result, int resultValue, long myActionId) throws ErrMsgException {
        initTmpUserNameActived();
        
        // 状态改变前，处理打回动作
        if (!beforeChangeStatusFree(request, wf, checkUserName, newstatus, reason, myActionId)) {
            return false;
        }
        // LogUtil.getLog(getClass()).info("changeStatus userName=" + getUserName() + " roleName=" + getJobName() + " status desc=" + getStatusName());

        MyActionDb mad = new MyActionDb();
        mad = mad.getMyActionDb(myActionId);
        // 置待处理动作通知中的处理时间及将其设置为已处理
        mad.setCheckDate(new java.util.Date());
        mad.setChecked(true);
        mad.setChecker(checkUserName);
        mad.setResultValue(WorkflowActionDb.RESULT_VALUE_AGGREE);

        Config cfg = new Config();
        if ("true".equals(cfg.get("flowExpireRelateOACalendar"))) {
            String flowExpireUnit = cfg.get("flowExpireUnit");
            OACalendarDb oad = new OACalendarDb();
            if ("day".equals(flowExpireUnit)) {
                double d = oad.getWorkDayCountFromDb(mad.getReceiveDate(), new java.util.Date());
                double d2 = oad.getWorkDayCount(mad.getReceiveDate(), mad.getExpireDate());
                double performance = 0;

                if (d2!=0) {
                    String formula = StrUtil.getNullStr(cfg.get("flowPerformanceFormula"));
                    if (!"".equals(formula)) {
                        formula = formula.replaceAll("a", NumberUtil.round(d, 2));
                        formula = formula.replaceAll("b", NumberUtil.round(d2, 2));
                        FormulaCalculator fc = new FormulaCalculator(formula);
                        performance = fc.getResult();
                    }
                    else {
                        performance = d / d2;
                    }
                }
                mad.setPerformance(performance);
            }
            else {
                // 完成所用时间
                double d = oad.getWorkHourCount(mad.getReceiveDate(), new java.util.Date());
                // 流程到期时间
                double d2 = oad.getWorkHourCount(mad.getReceiveDate(), mad.getExpireDate());

                double performance = 0;
                if (d2!=0) {
                    String formula = StrUtil.getNullStr(cfg.get("flowPerformanceFormula"));
                    if (!"".equals(formula)) {
                        formula = formula.replaceAll("a", NumberUtil.round(d, 2));
                        formula = formula.replaceAll("b", NumberUtil.round(d2, 2));
                        FormulaCalculator fc = new FormulaCalculator(formula);
                        performance = fc.getResult();
                    }
                    else {
                        performance = d / d2;
                    }
                }

                mad.setPerformance(performance);
            }
        }

        mad.save();

        mad.onChecked();

        if (newstatus == STATE_FINISHED) {
            // 加签处理，注意加签处理中如果是并签，其它加签人员没有处理完毕，则方法返回true，需return停止往下继续运行
            if (doPlus(request, wf, mad)) {
                return true;
            }
        }

        // 如果更改动作的状态为完成，则置审批时间
        if (newstatus == STATE_FINISHED) {
            setCheckDate(new java.util.Date());
            setCheckUserName(checkUserName);
            // 用于在流程图中节点上显示时间
            setResult(DateUtil.format(new java.util.Date(),
                                      "yyyy-MM-dd HH:mm"));
            setResultValue(resultValue);
            setCheckDate(new java.util.Date());
        }

        setStatus(newstatus);

        // LogUtil.getLog(getClass()).info("changeStatus: userRealName=" + getUserRealName() + " jobName=" + getJobName() + " statusName=" + getStatusName());
        boolean re = save();
        if (re) {
            // 如果本action为start即开始节点，并且被打回，则置流程状态为未开始
            if (newstatus==STATE_RETURN && getIsStart()==1) {
                wf.setStatus(WorkflowDb.STATUS_NOT_STARTED);
                wf.save();
            }

            // 清流程的缓存
            WorkflowCacheMgr wcm = new WorkflowCacheMgr();
            wcm.refreshList();

            // 流程状态改变后
            afterChangeStatusFree(request, wf, checkUserName, newstatus, myActionId);
        }
        return re;
    }

    /**
     * 当流程节点状态改变时更新对应的MyActoinDb
     * @param mad MyActionDb
     * @return boolean
     */
    public boolean changeMyActionDb(MyActionDb mad, String checkUserName) throws ErrMsgException {
        // 置待处理动作通知中的处理时间及将其设置为已处理
        mad.setCheckDate(new java.util.Date());
        mad.setChecked(true);
        mad.setChecker(checkUserName);

        Config cfg = new Config();
        if ("true".equals(cfg.get("flowExpireRelateOACalendar"))) {
            String flowExpireUnit = cfg.get("flowExpireUnit");
            OACalendarDb oad = new OACalendarDb();
            if ("day".equals(flowExpireUnit)) {
                double d = oad.getWorkDayCountFromDb(mad.getReceiveDate(), new java.util.Date());
                double d2 = oad.getWorkDayCount(mad.getReceiveDate(), mad.getExpireDate());
                double performance = 0;
                if (d2!=0) {
                    String formula = StrUtil.getNullStr(cfg.get("flowPerformanceFormula"));
                    if (!"".equals(formula)) {
                        formula = formula.replaceAll("a", NumberUtil.round(d, 2));
                        formula = formula.replaceAll("b", NumberUtil.round(d2, 2));
                        FormulaCalculator fc = new FormulaCalculator(formula);
                        performance = fc.getResult();
                    }
                    else {
                        performance = d / d2;
                    }
                }
                mad.setPerformance(performance);
            }
            else {
                double d = oad.getWorkHourCount(mad.getReceiveDate(), new java.util.Date());

                double d2 =oad.getWorkHourCount(mad.getReceiveDate(), mad.getExpireDate());

                double performance = 0;
                if (d2!=0) {
                    String formula = StrUtil.getNullStr(cfg.get("flowPerformanceFormula"));
                    if (!"".equals(formula)) {
                        formula = formula.replaceAll("a", NumberUtil.round(d, 2));
                        formula = formula.replaceAll("b", NumberUtil.round(d2, 2));
                        FormulaCalculator fc = new FormulaCalculator(formula);
                        performance = fc.getResult();
                    }
                    else {
                        performance = d / d2;
                    }
                }
                mad.setPerformance(performance);
            }
        }

        boolean re = mad.save();
        if (re) {
            mad.onChecked();
        }
        return re;
    }

    /**
     * 加签处理
     * @param request HttpServletRequest
     * @param wf WorkflowDb
     * @param mad MyActionDb
     * @return boolean 如果为false，则表示接着需退出 changeStatus 方法
     */
    public boolean doPlus(HttpServletRequest request, WorkflowDb wf, MyActionDb mad) throws ErrMsgException {
        // 加签处理
        JSONObject plusJson = null;
        int plusType = -1;
        int plusMode = PLUS_MODE_ORDER;
        String[] plusUsers = null;
        String from = null;
        String internal = null;
        try {
            if (!"".equals(plus)) {
                plusJson = new JSONObject(plus);
                try {
                    plusType = plusJson.getInt("type");
                    plusMode = plusJson.getInt("mode");
                    plusUsers = StrUtil.split(plusJson.getString("users"), ",");
                    // 向下兼容，兼容1.3版
                    if (plusJson.has("from")) {
                        from = plusJson.getString("from");
                    }
                    else {
                        from = "";
                    }
                    if (plusJson.has("internal")) {
                        internal = plusJson.getString("internal");
                    }
                    else {
                        internal = "";
                    }
                } catch (JSONException ex1) {
                	if (!ex1.getMessage().contains("internal")) {
                		throw new ErrMsgException(ex1.getMessage());
                	}
                }
            }
        } catch (JSONException ex) {
            LogUtil.getLog(getClass()).error(ex);
        }
        // 检查加签人员是否已处理完毕，加签人员应不能打回操作
        if (plusUsers == null) {
            return false;
        }
        
        String curUser = new com.redmoon.oa.pvg.Privilege().getUser(request);

        boolean flag = false;
        WorkflowActionDb wa = new WorkflowActionDb((int) mad.getActionId());
        // 当前节点不是加签节点且internalname和plusjson中的internalname一致且当前用户和plusjson中的from一致,且为后加签或并签
        if (mad.getActionStatus() != STATE_PLUS && 
        		wa.getInternalName().equals(internal) && 
        		curUser.equals(from) && 
        		(plusType == WorkflowActionDb.PLUS_TYPE_AFTER || 
        				plusType == WorkflowActionDb.PLUS_TYPE_CONCURRENT)) {
        	flag = true;
        }
        
        if (!flag) {
	        // 当前节点不是加签节点
	        if (mad.getActionStatus() != STATE_PLUS) {
	        	return false;
	        }
	        
	        // 如果当前用户不在加签用户中也不加签
	        for (String user : plusUsers) {
	        	if (user.equals(curUser)) {
	        		flag = true;
	        		break;
	        	}
	        }
	        
	        if (!flag) {
	        	return false;
	        }
        }

        cn.js.fan.mail.SendMail sendmail = WorkflowDb.getSendMail();

        long privMyActionId = mad.getPrivMyActionId();
        MyActionDb privMyActionDb = null;
        WorkflowActionDb privAction = null;
        if (privMyActionId!=-1) {
            privMyActionDb = mad.getMyActionDb(privMyActionId);
            privAction = getWorkflowActionDb((int) privMyActionDb.getActionId());
        }

        // 判断是如果为加签MyActionDb动作
        if (mad.getActionStatus() == STATE_PLUS) {
            // 如果为前加签
            if (plusType == WorkflowActionDb.PLUS_TYPE_BEFORE) {
                // 如果为顺序加签
                if (plusMode == PLUS_MODE_ORDER) {
                    // 检查是否为最后一名加签处理人员，如果是，则给原处理者发待办通知
                    if (mad.getUserName().equals(plusUsers[plusUsers.length - 1])) {
                        MyActionDb newmad = wf.notifyUser(from, new java.util.Date(), mad.getId(),
                                                          privAction, this,
                                                          WorkflowActionDb.STATE_DOING, getFlowId());

                        wf.sendNotifyMsgAndEmail(request, newmad, sendmail);

                    } else {
                        // 不是，则给下一名处理者发待办通知
                        for (int i = 0; i < plusUsers.length; i++) {
                            if (plusUsers[i].equals(mad.getUserName())) {
                                MyActionDb newmad = wf.notifyUser(plusUsers[i + 1], new java.util.Date(), mad.getId(),
                                                                  privAction, this,
                                                                  WorkflowActionDb.STATE_PLUS, getFlowId());
                                wf.sendNotifyMsgAndEmail(request, newmad, sendmail);

                                break;
                            }
                        }
                    }
                } else if (plusMode == PLUS_MODE_ONE) { // 如果有一人处理完毕即往下流转
                    // 删除其它的加签MyActionDb
                    Vector v = mad.getPlusMyActionDbs(mad.getActionId());
                    for (Object o : v) {
                        MyActionDb mad2 = (MyActionDb) o;
                        if (!mad2.getUserName().equals(mad.getUserName())) {
                            mad2.del();
                        }
                    }

                    // 给原处理者发待办通知
                    MyActionDb newmad = wf.notifyUser(from, new java.util.Date(),
                                                      mad.getId(),
                                                      privAction, this,
                                                      WorkflowActionDb.STATE_DOING, getFlowId());
                    wf.sendNotifyMsgAndEmail(request, newmad, sendmail);

                } else {
                    // 全部加签人员处理完毕，则给原处理者发待办通知
                    boolean isPlusFinished = true;
                    Vector v = mad.getPlusMyActionDbs(mad.getActionId());
                    for (Object o : v) {
                        MyActionDb mad2 = (MyActionDb) o;
                        LogUtil.getLog(getClass()).info("mad2.getCheckStatus()=" + mad2.getCheckStatus());

                        if (mad2.getCheckStatus() == MyActionDb.CHECK_STATUS_NOT) {
                            isPlusFinished = false;
                            break;
                        }
                    }
                    if (isPlusFinished) {
                        // 给原处理者发待办通知
                        MyActionDb newmad = wf.notifyUser(from, new java.util.Date(), mad.getId(),
                                                          privAction, this,
                                                          WorkflowActionDb.STATE_DOING, getFlowId());
                        wf.sendNotifyMsgAndEmail(request, newmad, sendmail);
                    }
                }
                // 前加签运行到此即退出，不再进行后续处理
                return true;
            } else if (plusType == PLUS_TYPE_CONCURRENT) { // 并签，如果没有全部处理完毕，则退出，并签时，不分审批方式，即无mode
                // 如果该节点上的原处理人员未处理
                if (!privMyActionDb.isChecked()) {
                    return true;
                }

                Vector v = mad.getPlusMyActionDbs(mad.getActionId());
                for (Object o : v) {
                    MyActionDb mad2 = (MyActionDb) o;
                    if (mad2.getCheckStatus() == MyActionDb.CHECK_STATUS_NOT) {
                        return true;
                    }
                }
            } else { // 后加签
                if (plusMode == PLUS_MODE_ORDER) {
                    // 如果不是最后一个加签用户，则给下一个加签人员发待办通知，同时退出
                    if (!mad.getUserName().equals(plusUsers[plusUsers.length - 1])) {
                        for (int i = 0; i < plusUsers.length; i++) {
                            if (plusUsers[i].equals(mad.getUserName())) {
                                MyActionDb newmad = wf.notifyUser(plusUsers[i + 1], new java.util.Date(), mad.getId(),
                                                                  privAction, this,
                                                                  WorkflowActionDb.STATE_PLUS, getFlowId());
                                wf.sendNotifyMsgAndEmail(request, newmad, sendmail);
                                return true;
                            }
                        }
                    }
                } else if (plusMode == PLUS_MODE_ONE) { // 如果有一人处理完毕即继续流转
                    // 删除其它的加签MyActionDb
                    Vector v = mad.getPlusMyActionDbs(mad.getActionId());
                    for (Object o : v) {
                        MyActionDb mad2 = (MyActionDb) o;
                        if (!mad2.getUserName().equals(mad.getUserName())) {
                            mad2.del();
                        }
                    }
                } else {
                    // 全部加签人员尚未处理完毕，则退出
                    Vector v = mad.getPlusMyActionDbs(mad.getActionId());
                    for (Object o : v) {
                        MyActionDb mad2 = (MyActionDb) o;
                        if (mad2.getCheckStatus() == MyActionDb.CHECK_STATUS_NOT) {
                            return true;
                        }
                    }
                }
            }
        } else { // 如果是本节点初始用户处理完毕
            // 后加签，通知待办用户
            if (plusType == WorkflowActionDb.PLUS_TYPE_AFTER) {
                if (plusMode == PLUS_MODE_ORDER) {
                    MyActionDb newmad = wf.notifyUser(plusUsers[0], new java.util.Date(), mad.getId(),
                                                      privAction, this,
                                                      WorkflowActionDb.STATE_PLUS, getFlowId());
                    wf.sendNotifyMsgAndEmail(request, newmad, sendmail);
                } else {
                    for (String plusUser : plusUsers) {
                        MyActionDb newmad = wf.notifyUser(plusUser, new Date(), mad.getId(), privAction, this,
                                WorkflowActionDb.STATE_PLUS, getFlowId());
                        wf.sendNotifyMsgAndEmail(request, newmad, sendmail);
                    }
                }
                return true;
            } else if (plusType == WorkflowActionDb.PLUS_TYPE_CONCURRENT) { // 并签
                // 如果其它加签人员没有处理完毕，则退出
                Vector v = mad.getPlusMyActionDbs(mad.getActionId());
                for (Object o : v) {
                    MyActionDb mad2 = (MyActionDb) o;
                    if (mad2.getCheckStatus() == MyActionDb.CHECK_STATUS_NOT) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 改变动作状态
     * @param reason String 被打回原因，只有打回时才需要此参数
     * @return boolean
     */
    public boolean changeStatus(HttpServletRequest request, WorkflowDb wf, String checkUserName, int newstatus, String reason, String result, int resultValue, long myActionId) throws ErrMsgException {
        // 重置tmpUserNameActive
        initTmpUserNameActived();

        MyActionDb mad = new MyActionDb();
        mad = mad.getMyActionDb(myActionId);

        // 加签不会产生任何可能的改变
        if (mad.getActionStatus() != STATE_PLUS) {
	        // 状态改变前，处理打回动作
	        if (!beforeChangeStatus(request, wf, checkUserName, newstatus, reason, myActionId)) {
                return false;
            }
        }
	        
        int oldStatus = status;

        // 节点不是将被激活时，更新待办记录MyActionDb中的处理状态等，如果将被激活，则是因为在afterChangeStatus中对下一节点调用了changeStatus
        if (newstatus != STATE_DOING) {
            changeMyActionDb(mad, checkUserName);
        }

        if (newstatus == STATE_FINISHED) {
            // 加签处理，注意加签处理中如果是并签，其它加签人员没有处理完毕，则方法返回true，需return停止往下继续运行
            if (doPlus(request, wf, mad)) {
                return true;
            }
        }

        // 根据节点策略，忽略相应的人员
        if (newstatus == STATE_FINISHED || newstatus == STATE_RETURN || newstatus==STATE_DISCARDED || newstatus==STATE_TRANSFERED) {
            WorkflowActionDb wa = new WorkflowActionDb();
            wa = wa.getWorkflowActionDb((int) mad.getActionId());
            String strategy = wa.getStrategy();
            LogUtil.getLog(getClass()).info("changeStatus:strategy=" + strategy);
            if (!"".equals(strategy)) {
                StrategyMgr sm = new StrategyMgr();
                StrategyUnit su = sm.getStrategyUnit(strategy);
                LogUtil.getLog(getClass()).info("changeStatus:su=" + su);
                if (su != null) {
                    IStrategy ist = su.getIStrategy();
                    ist.onActionFinished(request, wf, mad);
                }
            }
        }
        
        // 如果更改动作的状态为完成，则置审批时间
        if (newstatus == STATE_FINISHED) {
            setCheckDate(new java.util.Date());
            setCheckUserName(checkUserName);
            // 用于在流程图中节点上显示时间
            setResult(DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm"));
            setResultValue(resultValue);
            setCheckDate(new java.util.Date());

            // 检查当节点上的被选人员有多人时，判断每个人是否都已处理完毕
            String[] users = StrUtil.split(userName, ",");
            LogUtil.getLog(getClass()).info("changeStatus: userName=" + userName);
            int len = (users == null ? 0 : users.length);
            boolean canFinish = false;
            // len==1的情况不能考虑，因为可能有加签，此时userName只有1个用户，而实际上还有加签的待办记录存在
            if (len==1) {
                canFinish = true;
            }
            else {
                if (mad.isAllUserOfActionChecked()) {
                    canFinish = true;
                }
                else {
                    newstatus = status; // 恢复为以前的状态
                }
                LogUtil.getLog(getClass()).info("changeStatus: isAllUserOfActionChecked=" + canFinish);
            }
            LogUtil.getLog(getClass()).info("changeStatus: canFinish=" + canFinish);
            if (canFinish) {
                setStatus(newstatus);
            }
        }
        // 如果是转办后返回给了原转办者，则两者是在同一节点上，返回后节点状态应为STATE_DOING
        else if (newstatus==STATE_NOTDO && status==STATE_RETURN) {
            if (returnIds==null || returnIds.length==0) {
                throw new ErrMsgException("请选择将要返回的用户！");
            }
            
            // 手机端发过来的，有时好象会带有换行符，所以要trim
            /*int returnId = Integer.parseInt(returnIds[0].trim());
            LogUtil.getLog(getClass()).info("changeStatus: returnId=" + returnId + " id=" + id);
            if (returnId == id) {
            	// 不需要再赋值，因为已经是STATE_RETURN
            	 newstatus = STATE_RETURN;
            	 setStatus(newstatus);
            }*/
        }
        else {
            // 如果是退回操作
            if (newstatus == STATE_NOTDO && status == STATE_DOING) {
                // 如果是异步退回
                if (isXorReturn()) {
                    // 如果节点上还有其他人员的正在办理的待办记录，则不更改节点正在办理的状态
                    if (mad.getOthersOfActionDoing().size() == 0) {
                        setStatus(newstatus);
                    }
                }
                else {
                    setStatus(newstatus);
                }
            }
            else {
                setStatus(newstatus);
            }
        }

        LogUtil.getLog(getClass()).info("changeStatus: userRealName=" + getUserRealName() + " jobName=" + getJobName() + " statusName=" + getStatusName());
        
        boolean re = save();
        LogUtil.getLog(getClass()).info("changeStatus: re=" + re);
        if (re) {
            // 如果本节点为即开始节点，并且被打回，则置流程状态为未开始
            if (newstatus==STATE_RETURN && getIsStart()==1) {
                // 需重新获得wf，因为flowstring在上面的save()中被改变了，而wf不更新，继续往下作为afterChangeStatus参数传递，就会出现问题
                wf = wf.getWorkflowDb(wf.getId());
                wf.setStatus(WorkflowDb.STATUS_NOT_STARTED);
                wf.save();
            }

            // 清流程的缓存
            WorkflowCacheMgr wcm = new WorkflowCacheMgr();
            wcm.refreshList();

            LogUtil.getLog(getClass()).info("changeStatus: newstatus=" + newstatus);

            // 流程状态改变后
            wf = wf.getWorkflowDb(wf.getId());
            afterChangeStatus(request, wf, checkUserName, oldStatus, newstatus, myActionId);
        }
        return re;
    }

    public void setReturnIds(String[] ids) {
        this.returnIds = ids;
    }

    /**
     * 手机端提交流程时，重新对节点上的用户进行匹配，用于当节点上为表单中指定的字段时
     * @param request
     * @param fd
     * @param wa
     */
    public static void reMatchUserOnMobileFinish(HttpServletRequest request, FormDb fd, WorkflowActionDb wa) throws ErrMsgException {
        // 判断是否来自手机端
        WorkflowParams wp = (WorkflowParams)request.getAttribute("workflowParams");
        FileUpload fu = wp.getFileUpload();
        UserMgr um = new UserMgr();

        Vector vto = wa.getLinkToActions();
        Iterator toir = vto.iterator();
        while (toir.hasNext()) {
            WorkflowActionDb towa = (WorkflowActionDb) toir.next();
            String jobCode = towa.getJobCode();
            if (jobCode.startsWith(WorkflowActionDb.PRE_TYPE_FIELD_USER)) {
                String fieldNames = jobCode.substring((WorkflowActionDb.PRE_TYPE_FIELD_USER + "_").length());
                if (!fieldNames.startsWith("nest.")) {
                    String[] fieldAry = StrUtil.split(fieldNames, ",");
                    if (fieldAry == null) {
                        continue;
                    }

/*                    FormDAO fdao = new FormDAO();
                    fdao = fdao.getFormDAO(wa.getFlowId(), fd);*/

                    for (int k=0; k<fieldAry.length; k++) {
                        String fieldName = fieldAry[k];

                        FormField ff = fd.getFormField(fieldName);
                        if (ff == null)
                            throw new ErrMsgException("指定人员的表单域：" + fieldName + "不存在！");

                        String userNames = StrUtil.getNullStr(fu.getFieldValue(fieldName));
                        if (!"".equals(userNames)) {
                            // 如果是部门宏控件，则取出部门中的全部用户
                            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                                if (ff.getMacroType().equals("macro_my_dept_select")
                                        || ff.getMacroType().equals("macro_dept_sel_win")
                                        || ff.getMacroType().equals("macro_dept_select")
                                        ) {
                                    DeptUserDb dud = new DeptUserDb();
                                    Iterator ir = dud.list(userNames).iterator();
                                    userNames = "";
                                    while (ir.hasNext()) {
                                        dud = (DeptUserDb)ir.next();
                                        if ("".equals(userNames)) {
                                            userNames = dud.getUserName();
                                        }
                                        else {
                                            userNames += "," + dud.getUserName();
                                        }
                                    }
                                }
                            }
                        }

                        // users的值可能为宏
                        userNames = userNames.replaceAll("，", ",");
                        String[] ary = StrUtil.split(userNames, ",");
                        StringBuffer sb = new StringBuffer();
                        int len = 0;
                        if (ary != null) {
                            len = ary.length;
                        }
                        if (len >= 1) {
                            for (int i = 0; i < len; i++) {
                                UserDb user = um.getUserDb(ary[i]);
                                if (user.isLoaded()) {
                                    StrUtil.concat(sb, ",", user.getRealName());
                                }
                                else {
                                    throw new ErrMsgException(wa.getJobName() + "=" + ary[i] + " 不存在！");
                                }
                            }
                        }

                        wa.setUserName(userNames);
                        wa.setUserRealName(sb.toString());
                        wa.save();
                    }
                }
            }
        }
    }

    /**
     * 分配策略中的人員是否可以由前一节点人員选择
     * @return
     */
    public boolean isStrategySelectable() {
    	String strategy = getStrategy();
        if (!strategy.equals("")) {
            StrategyMgr sm = new StrategyMgr();
            StrategyUnit su = sm.getStrategyUnit(strategy);
            if (su != null) {
                IStrategy ist = su.getIStrategy();
                return ist.isSelectable();
            }
        }
        return true;
    }
    
    /**
     * 分配策略中的人员默认是否为选中状态
     * @return
     */
    public boolean isStrategySelected() {
    	String strategy = getStrategy();
        if (!strategy.equals("")) {
            StrategyMgr sm = new StrategyMgr();
            StrategyUnit su = sm.getStrategyUnit(strategy);
            if (su != null) {
                IStrategy ist = su.getIStrategy();
                return ist.isSelected();
            }
        }
        return false;
    } 

    /**
     * 是否为下达模式，如果是，则自选用户时，不能勾选其他人已选的下一节点的用户
     * 仅用于“自选用户”的情况，且不能与存在分支异或发散时联用
     * @return
     */
    public boolean isStrategyGoDown() {
    	String strategy = getStrategy();
        if (!strategy.equals("")) {
            StrategyMgr sm = new StrategyMgr();
            StrategyUnit su = sm.getStrategyUnit(strategy);
            if (su != null) {
                IStrategy ist = su.getIStrategy();
                return ist.isGoDown();
            }
        }
        return false;
    } 


    /**
     * 判断用户处理actionId对应节点之前有没有处理过其它节点，注意返回不算在内
     * @param userName String
     * @param flowId long
     * @return boolean
     */
    public boolean isUserAccessedBefore(String userName, long actionId, long flowId) {
        return MyActionDb.isUserAccessedBefore(userName, actionId, flowId);
        /*
        String sql = "select id from flow_my_action where flow_id=" + flowId + " and user_name=" + StrUtil.sqlstr(userName) + " and is_checked<>" + MyActionDb.CHECK_STATUS_RETURN + " order by receive_date desc";
        MyActionDb mad = new MyActionDb();
        Vector v = mad.list(sql);
        if (v.size()>0)
            return true;
        else
            return false;
        */
    }

    /**
     * 置节点上的用户，用于当跳过节点或批量处理匹配时
     * @param wad WorkflowActionDb
     * @param v Vector 用户UserDb
     */
    public static void setActionUserOnMatch(WorkflowActionDb wad, Vector<UserDb> v) throws ErrMsgException {
        StringBuilder uNames = new StringBuilder();
        StringBuilder uRealNames = new StringBuilder();
        for (UserDb user : v) {
            if (!user.isLoaded()) {
                continue;
            }
            String userRealName = user.getRealName();
            if ("".equals(uNames.toString())) {
                uNames = new StringBuilder(user.getName());
                uRealNames = new StringBuilder(userRealName);
            } else {
                uNames.append(",").append(user.getName());
                uRealNames.append(",").append(userRealName);
            }
        }

        wad.setUserName(uNames.toString());
        wad.setUserRealName(uRealNames.toString());
        // 为了便于用户在选错后再选，会出现多次匹配的问题
        if (wad.getNodeMode() == NODE_MODE_ROLE || wad.getNodeMode() == WorkflowActionDb.NODE_MODE_ROLE_SELECTED) {
            wad.setNodeMode(WorkflowActionDb.NODE_MODE_ROLE_SELECTED);
        }
        else if (wad.getNodeMode() == NODE_MODE_POST || wad.getNodeMode() == WorkflowActionDb.NODE_MODE_POST_SELECTED) {
            wad.setNodeMode(WorkflowActionDb.NODE_MODE_POST_SELECTED);
        }
        else {
            wad.setNodeMode(WorkflowActionDb.NODE_MODE_USER_SELECTED);
        }
        wad.save();
    }

    /**
     * 是否为异或发散节点
     * @return boolean
     */
    public boolean isXorRadiate() {
        Vector vto = getLinkToActions();
        boolean flagXorRadiate = false;
        if (flag.length() >= 7) {
            if (flag.substring(6, 7).equals("1")) {
                flagXorRadiate = true;
            }
        }

        if (flagXorRadiate && vto.size() > 1) {
            return true;
        }
        return false;
    }
    
    /**
     * 能否编辑附件
     * 20130720 fgf
     * @return
     */
    public boolean canDecline() {
    	if (flag.length()>=9) {
            return flag.substring(8, 9).equals("1");
    	} else {
            return true;
        }
    }

    /**
     * 能否放弃
     * @return
     */
    public boolean canDiscard() {
        return flag.length() >= 3 && "1".equals(flag.substring(2, 3));
    }
    
    /**
     * 能否公文分发
     * @return
     */
    public boolean isDistribute() {
    	if (flag.length()>=1) {
    		if (flag.substring(0, 1).equals("1"))
    			return true;
    		else
    			return false;
    	}
    	else
    		return false;
    }
    
    /**
     * 能否公文存档
     * @return
     */
    public boolean isArchiveGov() {
    	if (flag.length()>=5) {
    		if (flag.substring(4, 5).equals("3"))
    			return true;
    		else
    			return false;
    	}
    	else
    		return false;    	
    }
    
    /**
     * 能否手工存至文件柜
     * @return
     */
    public boolean isArchiveManual() {
    	if (flag.length()>=5) {
    		if (flag.substring(4, 5).equals("1"))
    			return true;
    		else
    			return false;
    	}
    	else
    		return false;    	
    }    
    
    /**
     * 能否编辑附件
     * 20130720 fgf
     * @return
     */
    public boolean canEditAttachment() {
    	if (flag.length()>=10) {
            return flag.substring(9, 10).equals("1");
    	} else {
            return true;
        }
    }
    
    /**
     * 能否套红
     * 20130720 fgf
     * @return
     */
    public boolean canReceiveRevise() {
    	if (flag.length()>=11) {
            return "1".equals(flag.substring(10, 11));
    	}
    	else {
            return false;
        }
    }

    /**
     * 能否盖章
     * @return
     */
    public boolean canSeal() {
        if (flag.length()>=15) {
            return "1".equals(flag.substring(14, 15));
        } else {
            return false;
        }
    }

    /**
     * 能否删除附件
     * 20130720 fgf
     * @return
     */
    public boolean canDelAttachment() {
    	if (flag.length()>=6) {
            return flag.substring(5, 6).equals("1");
    	}
    	else {
            return true;
        }
    }     

    /**
     * 是否为异或聚合节点
     * @return boolean
     */
    public boolean isXorAggregate() {
        Vector<WorkflowActionDb> vfrom = getLinkFromActions();
        boolean flagXorAggregate = false;
        if (flag.length() >= 8) {
            if ("1".equals(flag.substring(7, 8))) {
                flagXorAggregate = true;
            }
        }

        return flagXorAggregate && vfrom.size() > 1;
    }

    /**
     * 获取request的属性中存储的deptOfUserWithMultiDept
     * @param request HttpServletRequest
     * @return String
     */
    public String getDeptOfUserWithMultiDept(HttpServletRequest request) {
        String deptOfUserWithMultiDept = null;    	
    	if (request==null) {
            return deptOfUserWithMultiDept;
        }
        // 在finishAction.do置request的workflowParams属性
        WorkflowParams wparam = (WorkflowParams) request.getAttribute("workflowParams");
        if (wparam != null) {
            deptOfUserWithMultiDept = StrUtil.getNullStr(wparam.getFileUpload().getFieldValue("deptOfUserWithMultiDept"));
        }
        return deptOfUserWithMultiDept;
    }

    /**
     * 当流程转交时，检查action中是否已选定用户，如果没有，则报错，或者系统自动选定（待扩展）
     * @return boolean
     */
    public boolean checkActionUser(HttpServletRequest request, WorkflowActionDb nextwa, WorkflowActionDb curAction, boolean isNextActionSingle) throws ErrMsgException {
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(curAction.getFlowId());
        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
        // 如果返回后再交办时至返回者
        if (wpd.getReturnMode() == WorkflowPredefineDb.RETURN_MODE_TO_RETURNER && curAction.getStatus()==WorkflowActionDb.STATE_RETURN) {
            return true;
        }

        /*
        if (nextwa.getNodeMode()==nextwa.NODE_MODE_ROLE_SELECTED || nextwa.getNodeMode()==nextwa.NODE_MODE_USER_SELECTED) {
            if (!nextwa.getUserName().equals("")) {
                return true;
            }
        }
        */

        // 如果下一节点只有一个，且被忽略，并且节点上未选择用户
        if (nextwa.getStatus()==WorkflowActionDb.STATE_IGNORED && isNextActionSingle) {
            /* 如果用户为空，有可能是因为节点被跳过，在没有跳过功能前，这里是为了防止没有选择用户
            if (nextwa.getUserName().equals("")) {
                if (nextwa.getJobCode().equals(PRE_TYPE_USER_SELECT))
                    throw new ErrMsgException("请选择节点 " + nextwa.getJobName() + " " + nextwa.getTitle() + " 上的办理用户！");
                else {
                    throw new ErrMsgException("请选择节点 " + nextwa.getJobName() + " " + nextwa.getTitle() + " 中角色编码为" + nextwa.getJobCode() +
                                              "的用户！");
                }
            }
            */
        }

        LogUtil.getLog(getClass()).info("checkActionUser: getJobCode=" + nextwa.getJobCode() + " getJobName=" + nextwa.getJobName() + " getUserName=" + nextwa.getUserName());

        // 如果节点上未选择用户
        if (StrUtil.isEmpty(nextwa.getUserName())) {
            // 如果未被忽略，或者是虽然被忽略，但下一节点为异步聚合，则需检查是否选择了用户
            if (nextwa.getStatus()!=WorkflowActionDb.STATE_IGNORED || (nextwa.getStatus()==WorkflowActionDb.STATE_IGNORED && nextwa.isXorAggregate())) {
                if (nextwa.getJobCode().equals(PRE_TYPE_USER_SELECT) || nextwa.getJobCode().equals(PRE_TYPE_USER_SELECT_IN_ADMIN_DEPT)) {
                    if (nextwa.getIgnoreType() == IGNORE_TYPE_NOT) {
                        throw new ErrMsgException("请先选择下一节点上的办理用户！");
                    }
                } else {
                    // throw new ErrMsgException("请先选择角色编码为" + nextwa.getJobCode() +
                    //                          "的节点上的用户！");
                    // 如果节点上能够匹配到用户，则报异常，匹配不到，则有可能会自动跳过
                    Vector vt = null;
                    String errMsg = "";
                    try {
                        String deptOfUserWithMultiDept = getDeptOfUserWithMultiDept(request);
                        WorkflowRouter workflowRouter = new WorkflowRouter();
                        vt = workflowRouter.matchActionUser(request, nextwa, curAction, false, deptOfUserWithMultiDept);
                    } catch (MatchUserException e) {
                        ////////// 允许用户处于多个部门，流转时让其自行选择
                        throw new ErrMsgException("请选择您所在的部门！");
                    } catch (ErrMsgException ex1) {
                        // ex1.printStackTrace();
                        // 匹配时报异常，说明匹配有问题，比如：当关联组织机构时，节点中有多个人员，无法关联
                        // 或者当用户处于多个部门中，而没有在flow_dispose.jsp中选择部门便提交，此时deptOfUserWithMultiDept为空字符串(而不是null)，matchActionUser便会报异常
                        errMsg = ex1.getMessage();
                    }
                    if (!errMsg.equals("")) {
                        throw new ErrMsgException("匹配用户出错：" + errMsg + "！");
                    }

                    LogUtil.getLog(getClass()).info("checkActionUser: getJobCode=" + nextwa.getJobCode() +
                            " getJobName=" + nextwa.getJobName() + " getUserName=" +
                            nextwa.getUserName());

                    if (nextwa.getJobCode().startsWith(PRE_TYPE_FIELD_USER)) {
                        // 在checkActionUser之前已经保存了表单，所以需在此再次匹配
                        if (vt.size() == 0) {
                            // 如果无用户跳过，或者用户之前处理过，则跳过
                            if (nextwa.getIgnoreType() == IGNORE_TYPE_NOT) {
                                throw new ErrMsgException("请填写" + nextwa.getJobName());
                            }
                        } else {
                            String uNames = "";
                            String uRealNames = "";
                            Iterator ir = vt.iterator();
                            while (ir.hasNext()) {
                                UserDb user = (UserDb) ir.next();
                                if (uNames.equals("")) {
                                    uNames = user.getName();
                                    uRealNames = user.getRealName();
                                } else {
                                    uNames += "," + user.getName();
                                    uRealNames += "," + user.getRealName();
                                }
                            }
                            nextwa.setUserName(uNames);
                            nextwa.setUserRealName(uRealNames);
                            nextwa.save();
                        }
                    } else {
                        // 如果vt.size()==1，则表示匹配到了一个用户，则WorkflowActionDb中的用户会被填充
                        // 匹配到了多个用户，则说明在flow_dipose.jsp页面上没有选择用户

                        // 如果当前节点为子流程，则当节点上存在多个人员时，nextwa.getUserName()可以为空
                        if (curAction.getKind() == WorkflowActionDb.KIND_SUB_FLOW) {

                        } else {
                            if (vt != null && vt.size() != 0 && vt.size() != 1) {
	                        /*Iterator ir = vt.iterator();
	                        while (ir.hasNext()) {
	                            UserDb user = (UserDb) ir.next();
	                            LogUtil.getLog(getClass()).info("checkActionUser:" + user.getName() + "  " +
	                                                            user.getRealName());
	                        }*/
                                if (nextwa.getIgnoreType() == IGNORE_TYPE_NOT) {
                                    // 如果nextwa中无用户，而此方法中matchActionUser又得到了用户，说明在flow_dipose.jsp页面上没有选择用户
                                    throw new ErrMsgException("请先选择用户！");
                                }
                            } else {
                                // 如果vt的size为0
                                if (vt.size() == 0) {
                                    if (nextwa.getIgnoreType() == IGNORE_TYPE_NOT) {
                                        // 检查限定部门表单域是否未选择
                                        String deptField = StrUtil.getNullStr(WorkflowActionDb.getActionProperty(wpd, nextwa.getInternalName(), "deptField"));
                                        if (!"".equals(deptField)) {
                                            Leaf lf = new Leaf();
                                            lf = lf.getLeaf(wf.getTypeCode());
                                            FormDb fd = new FormDb();
                                            fd = fd.getFormDb(lf.getFormCode());
                                            FormDAO fdao = new FormDAO();
                                            fdao = fdao.getFormDAO(flowId, fd);
                                            String deptFieldVal = StrUtil.getNullStr(fdao.getFieldValue(deptField));
                                            if ("".equals(deptFieldVal)) {
                                                throw new ErrMsgException("请选择" + fdao.getFormField(deptField).getTitle());
                                            }
                                        }
                                        throw new ErrMsgException("请选择处理用户！");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return true;
    }

    /**
      * 处理打回动作及检查下一节点是否合法
      * @param wf WorkflowDb
      * @param checkUserName String
      * @param newstatus int
      * @param reason String
      * @return boolean
      */
     public boolean beforeChangeStatusFree(HttpServletRequest request, WorkflowDb wf, String checkUserName, int newstatus, String reason, long myActionId) throws ErrMsgException  {
         // 打回操作
         if ((status==STATE_DOING && newstatus == STATE_NOTDO) || (status==STATE_RETURN && newstatus==STATE_NOTDO)) {
             // 如果本结点是聚合结点则不能被打回
             Vector vfrom = getLinkFromActions();
             if (vfrom.size()==0) {
                 throw new ErrMsgException("本节点是开始结点，不能被返回！");
             }

             if (returnIds==null || returnIds.length==0) {
                 throw new ErrMsgException("请选择将要返回的用户！");
             }
             int len = returnIds.length;
             for (int i = 0; i < len; i++) {
                 int returnId = Integer.parseInt(returnIds[i]);
                 // 找到上一结点
                 WorkflowActionDb wapriv = getWorkflowActionDb(returnId);
                 // 更改上一结点的状态，置其为被打回
                 wapriv.setReason(reason);
                 wapriv.setStatus(STATE_RETURN);
                 wapriv.save();
                 // 通知用户办理
                 String[] users = StrUtil.split(wapriv.getUserName(), ",");
                 int userslen = users.length;
                 for (int n = 0; n < userslen; n++) {
                     MyActionDb mad = wf.notifyUser(users[n],
                                   new java.util.Date(), myActionId, this,
                                   wapriv, STATE_RETURN,
                                   (long) wf.getId());
                     LogUtil.getLog(getClass()).info("beforeChangeStatus:" + wapriv.getUserRealName());
                     addTmpUserNameActived(mad);
                 }
             }
         }
         return true;
    }

    public boolean beforeChangeStatus(HttpServletRequest request, WorkflowDb wf, String checkUserName, int newstatus, String reason, long myActionId) throws ErrMsgException  {
        // 检查下一节点是否为 $userSelect
        LogUtil.getLog(getClass()).info("beforeChangeStatus: newstatus=" + newstatus);
        if (newstatus == STATE_FINISHED) {
            LogUtil.getLog(getClass()).info("beforeChangeStatus: " + checkUserName + " finished!");
            // 如果本结点为发散结点，则置所有的未被跳过的to结点为DOING
            Vector<WorkflowActionDb> v = getLinkToActions();
            boolean isNextActionSingle = v.size()==1;
            Iterator<WorkflowActionDb> ir = v.iterator();
            if (isNextActionSingle) {
            	if (ir.hasNext()) {
	            	WorkflowActionDb wa = ir.next();
	            	if (wa.getIgnoreType() == IGNORE_TYPE_NOT && (!wa.getJobCode().startsWith("$") && wa.getUserName().equals(""))) {
	            		throw new ErrMsgException("请选择用户！");
	            	}
	            	checkActionUser(request, wa, this, isNextActionSingle);
            	}
            } else {
	            String internalName = "";
	            boolean isContersign = true;
	            // 判断是否为会签,会签就是同一个节点发散至多人再汇聚至同一个节点
	            while (ir.hasNext()) {
	            	WorkflowActionDb wa = ir.next();
	            	Vector<WorkflowActionDb> tv = wa.getLinkToActions();
	            	if (tv.size() > 1) {
	            		isContersign = false;
	            		break;
	            	}
	            	if ("".equals(internalName)) {
	            		internalName = wa.getInternalName();
	            	} else {
	            		if (!internalName.equals(wa.getInternalName())) {
	            			isContersign = false;
	                		break;
	            		}
	            	}
	            }
	            
	            boolean isUserChecked = true;
	            ir = v.iterator();
	            while (ir.hasNext()) {
	                WorkflowActionDb wa = ir.next();
	                if (v.size()==1) {
	                    // 如果没有分支线，且节点为被忽略状态，节点是聚合节点，则临时置其状态为未处理，以便于在checkAction的时候能够对其进行处理
	                    // 如：当前节点用户有兼职，则在flow_dispose.jsp中matchActionUser的时候，先抛出异常让其选部门，然后提交时，下一节点还未匹配到用户，会此处如果不置为未处理状态，就会被跳过
	                    // 置成未处理状态后，在checkActionUser中还会再被matchActionUser
	                    // 考虑如果改为在流程节点中，只要用户处于多个部门，在flow_dispose.jsp上就需先去选择处在哪个部门，也不合理，因为选了部门后，还是要post到服务器端，再matchActionUser一次，因为matchActionUser是与部门相关的
	                    if (wa.getStatus()==WorkflowActionDb.STATE_IGNORED) {
	                        if (wa.getLinkFromActions().size()>1) {
	                            wa.setStatus(WorkflowActionDb.STATE_NOTDO);
	                        }
	                    }
	                }
	
	                // 检查myAction中的用户是否合法
	                LogUtil.getLog(getClass()).info("beforeChangeStatus: begin checkActionUser!");
	                // 下一节点处理人为空且发散且不是会签
	                if ("".equals(wa.getUserName()) && !isContersign) {
	                	// 如果当前节点为条件分支节点且下一节点未跳过,则判断用户
	                	if (this.isXorRadiate()) {
	                		if (wa.getIgnoreType() != IGNORE_TYPE_NOT) {
	                			isUserChecked = true;
	                		} else {
		                		if (wa.getStatus() != WorkflowActionDb.STATE_IGNORED) {
		                			checkActionUser(request, wa, this, isNextActionSingle);
		                		}
	                		}
	                	} else {
	                		if (wa.getIgnoreType() == IGNORE_TYPE_NOT) {
	                			isUserChecked = false;
	                		} else {
	                			isUserChecked = true;
	                			break;
	                		}
	                	}
	                } else {
	                    checkActionUser(request, wa, this, isNextActionSingle);
	                    if (!isContersign && !this.isXorRadiate()) {
	                    	isUserChecked = true;
	            			break;
	                    }
	                }
	            }
	            // 非条件分支的发散节点,全部用户检查失败
	            if (!isUserChecked) {
	            	throw new ErrMsgException("请选择后续节点的用户！");
	            }
            }
        }
        
        // 打回或连续打回操作
        if ((status==STATE_FINISHED && newstatus==STATE_NOTDO) // 已完成状态变为未处理状态（变更时返回）
                || (status==STATE_DOING && newstatus == STATE_NOTDO)
                || (status==STATE_RETURN && newstatus==STATE_NOTDO)) {
            // 如果本结点是聚合结点则不能被打回
            Vector<WorkflowActionDb> vfrom = getLinkFromActions();
            if (vfrom.size()==0) {
                throw new ErrMsgException("本节点是开始结点，不能进行返回操作！");
            }

            if (returnIds==null || returnIds.length==0) {
                throw new ErrMsgException("请选择将要返回的用户！");
            }

            // 如果未设为异步退回
            if (!isXorReturn()) {
                // 如果节点不是聚合节点且当前节点有兄弟节点,则将其所有的兄弟节点都置为STATE_NOTDO
                if (vfrom.size() == 1) {
                    WorkflowActionDb previousAction = vfrom.get(0);
                    Vector<WorkflowActionDb> vto = previousAction.getLinkToActions();
                    for (WorkflowActionDb siblingAction : vto) {
                        if (this.id == siblingAction.getId()) {
                            continue;
                        }
                        siblingAction.setStatus(STATE_NOTDO);
                        siblingAction.save();
                    }
                }
            }

            MyActionDb mad = new MyActionDb();
            LogUtil.getLog(getClass()).info("beforeChangeStatus returnIds len=" + returnIds.length);
            // 已退定为返回时只能返回给一个节点，所以此处的循环其实已无用
            for (String s : returnIds) {
                // 手机端发过来的，有时好象会带有换行符，所以要trim
                int returnId = Integer.parseInt(s.trim());
                // 找到被打回的节点
                WorkflowActionDb waPriv = getWorkflowActionDb(returnId);
                // 更改被打回节点的状态，置其为被打回
                waPriv.setReason(reason);
                waPriv.setStatus(STATE_RETURN);
                waPriv.save();

                // 如果未设为异步退回
                if (!isXorReturn()) {
                    // 打回时，需使与被返回节点相连的分支线上正处理的其它节点变为未处理状态，已处理的节点不变维持原状
                    Vector<WorkflowActionDb> v = waPriv.getLinkToActions();
                    for (WorkflowActionDb wad : v) {
                        if (wad.getId() != waPriv.getId() && wad.getStatus() == WorkflowActionDb.STATE_DOING) {
                            wad.setStatus(WorkflowActionDb.STATE_NOTDO);
                            wad.save();
                        }
                    }
                }

                // 通知用户办理
                // 20221206 此处应取得实际办理者，因为可能存在如：只需其中一人办理 的情况
                List<MyActionDb> listChecked = mad.listByActionRealyChecked(waPriv.getId());
                for (MyActionDb myActionDb : listChecked) {
                    if (!StrUtil.isEmpty(myActionDb.getProxyUserName())) {
                        mad = wf.notifyUser(myActionDb.getProxyUserName(),
                                new Date(), myActionId, this,
                                waPriv, STATE_RETURN,
                                wf.getId());
                    } else {
                        mad = wf.notifyUser(myActionDb.getUserName(),
                                new Date(), myActionId, this,
                                waPriv, STATE_RETURN,
                                wf.getId());
                    }
                    LogUtil.getLog(getClass()).info("beforeChangeStatus:" + waPriv.getUserRealName());
                    addTmpUserNameActived(mad);
                }

                /*String[] users = StrUtil.split(waPriv.getUserName(), ",");
                int userslen = (users == null ? 0 : users.length);
                for (int n = 0; n < userslen; n++) {
                    MyActionDb mad = wf.notifyUser(users[n],
                            new java.util.Date(), myActionId, this,
                            waPriv, STATE_RETURN,
                            wf.getId());
                    Logger.getLogger(getClass()).info("beforeChangeStatus:" + waPriv.getUserRealName());
                    addTmpUserNameActived(mad);
                }*/
            }
        }
        return true;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIsStart(int isStart) {
        this.isStart = isStart;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public void setOfficeColorIndex(int officeColorIndex) {
        this.officeColorIndex = officeColorIndex;
    }

    public void setUserRealName(String userRealName) {
        this.userRealName = userRealName;
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setRankCode(String rankCode) {
        this.rankCode = rankCode;
    }

    public void setRelateRoleToOrganization(boolean relateRoleToOrganization) {
        this.relateRoleToOrganization = relateRoleToOrganization;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setCheckDate(java.util.Date checkDate) {
        this.checkDate = checkDate;
    }

    public void setResultValue(int resultValue) {
        this.resultValue = resultValue;
    }

    public void setCheckUserName(String checkUserName) {
        this.checkUserName = checkUserName;
    }

    public void setFieldWrite(String fieldWrite) {
        this.fieldWrite = fieldWrite;
    }
    
    public void setFieldHide(String fieldHide) {
    	this.fieldHide = fieldHide;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public void setNodeMode(int nodeMode) {
        this.nodeMode = nodeMode;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void setRankName(String rankName) {
        this.rankName = rankName;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public void setItem1(String item1) {
        this.item1 = item1;
    }

    public void setItem2(String item2) {
        this.item2 = item2;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }

    public void setPlus(String plus) {
        this.plus = plus;
    }

    public void setDateDelayed(java.util.Date dateDelayed) {
        this.dateDelayed = dateDelayed;
    }

    public void setTimeDelayedValue(int timeDelayedValue) {
        this.timeDelayedValue = timeDelayedValue;
    }

    public void setTimeDelayedUnit(int timeDelayedUnit) {
        this.timeDelayedUnit = timeDelayedUnit;
    }

    public void setDelayed(boolean delayed) {
        this.delayed = delayed;
    }

    public void setCanPrivUserModifyDelayDate(boolean canPrivUserModifyDelayDate) {
        this.canPrivUserModifyDelayDate = canPrivUserModifyDelayDate;
    }

    public int getFlowId() {
        return flowId;
    }

    public String getUserName() {
        return userName;
    }

    public String getReason() {
        return reason;
    }

    public int getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public int getIsStart() {
        return isStart;
    }

    public int getId() {
        return id;
    }

    public String getInternalName() {
        return internalName;
    }

    public int getOfficeColorIndex() {
        return officeColorIndex;
    }

    public String getUserRealName() {
        return userRealName;
    }

    public String getJobCode() {
        return jobCode;
    }

    public String getJobName() {
        return jobName;
    }

    public int getDirection() {
        return direction;
    }

    public String getRankCode() {
        return rankCode;
    }

    public boolean isRelateRoleToOrganization() {
        return relateRoleToOrganization;
    }

    public String getResult() {
        return result;
    }

    public java.util.Date getCheckDate() {
        return checkDate;
    }

    public int getResultValue() {
        return resultValue;
    }

    public String getCheckUserName() {
        return checkUserName;
    }

    public String getFieldWrite() {
        return fieldWrite;
    }

    public int getTaskId() {
        return taskId;
    }

    /**
     * @param str String
     * @param isCheck boolean
     */
    public boolean fromString(String str, boolean isCheck) throws ErrMsgException {
        IWorkflowHelper workflowHelper = SpringUtil.getBean(IWorkflowHelper.class);
        return workflowHelper.fromString(this, str, isCheck);
    }

    public void createAddBatch(PreparedStatement pstmt) {
        this.id = (int) SequenceManager.nextID(SequenceManager.OA_WORKFLOW_ACTION);
        try {
            pstmt.setInt(1, id);
            pstmt.setInt(2, flowId);
            pstmt.setInt(3, isStart);
            if (reason==null || "".equals(reason)) {
                reason = " "; // 适应SQLSERVER
            }
            pstmt.setString(4, reason);
            pstmt.setInt(5, status);
            pstmt.setString(6, title);
            pstmt.setString(7, userName);
            pstmt.setString(8, internalName);
            pstmt.setInt(9, officeColorIndex);
            pstmt.setString(10, userRealName);
            pstmt.setString(11, jobCode);
            pstmt.setString(12, jobName);
            pstmt.setString(13, "" + direction);
            pstmt.setString(14, rankCode);
            pstmt.setString(15, rankName);
            pstmt.setString(16, relateRoleToOrganization?"1":"0");
            if (fieldWrite==null || "".equals(fieldWrite)) {
                fieldWrite = " "; // 适应SQLSERVER
            }
            pstmt.setString(17, fieldWrite);
            pstmt.setInt(18, taskId);
            if (dept==null || "".equals(dept)) {
                dept = " "; // 适应SQLSERVER
            }
            pstmt.setString(19, dept);
            pstmt.setString(20, flag);
            pstmt.setInt(21, nodeMode);
            pstmt.setTimestamp(22, new Timestamp((new java.util.Date()).getTime()));
            pstmt.setString(23, strategy);
            pstmt.setString(24, item1);
            pstmt.setString(25, item2);
            pstmt.setInt(26, msg?1:0);
            pstmt.addBatch();
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }

    public boolean create() {
        Conn conn = new Conn(connname);
        this.id = (int) SequenceManager.nextID(SequenceManager.OA_WORKFLOW_ACTION);
        try {
            PreparedStatement pstmt = conn.prepareStatement(INSERT);
            pstmt.setInt(1, id);
            pstmt.setInt(2, flowId);
            pstmt.setInt(3, isStart);
            if (reason==null || "".equals(reason)) {
                reason = " "; // 适应SQLSERVER
            }
            pstmt.setString(4, reason);
            pstmt.setInt(5, status);
            pstmt.setString(6, title);
            pstmt.setString(7, userName);
            pstmt.setString(8, internalName);
            pstmt.setInt(9, officeColorIndex);
            pstmt.setString(10, userRealName);
            pstmt.setString(11, jobCode);
            pstmt.setString(12, jobName);
            pstmt.setString(13, "" + direction);
            pstmt.setString(14, rankCode);
            pstmt.setString(15, rankName);
            pstmt.setString(16, relateRoleToOrganization?"1":"0");
            if (fieldWrite==null || "".equals(fieldWrite)) {
                fieldWrite = " "; // 适应SQLSERVER
            }
            pstmt.setString(17, fieldWrite);
            pstmt.setInt(18, taskId);
            if (dept==null || "".equals(dept)) {
                dept = " "; // 适应SQLSERVER
            }
            pstmt.setString(19, dept);
            pstmt.setString(20, flag);
            pstmt.setInt(21, nodeMode);
            pstmt.setTimestamp(22, new Timestamp((new java.util.Date()).getTime()));
            pstmt.setString(23, strategy);
            pstmt.setString(24, item1);
            pstmt.setString(25, item2);
            pstmt.setInt(26, msg?1:0);
            int r = conn.executePreUpdate();
            if (r==1) {
                return true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return false;
    }

    public String tranReverse(String str) {
        if (StringUtils.isEmpty(str)) {
            return "";
        }
        str = str.replaceAll(":", "\\\\colon");
        str = str.replaceAll(";", "\\\\semicolon");
        str = str.replaceAll(",", "\\\\comma");
        str = str.replaceAll("\r\n","\\\\newline");
        return str;
    }

    /**
     * 因为org.json.JSONObject.toString会自动去掉转义字符\，如：\comma会变为comma，故将其变为#comma，等toString后再变回来
     * @param str
     * @return
     */
    public String tranReverseForFlowJson(String str) {
        if (StringUtils.isEmpty(str)) {
            return "";
        }
        // DebugUtil.i(getClass(), "tranReverse", str);
        str = str.replaceAll(":", "#colon");
        str = str.replaceAll(";", "#semicolon");
        str = str.replaceAll(",", "#comma");
        str = str.replaceAll("\r\n","#newline");
        // DebugUtil.i(getClass(), "tranReverse after", str);
        return str;
    }

    public String tranForFlowJson(String str) {
        if (StringUtils.isEmpty(str)) {
            return "";
        }
        str = str.replaceAll("#colon", "\\\\colon");
        str = str.replaceAll("#semicolon", "\\\\semicolon");
        str = str.replaceAll("#comma", "\\\\comma");
        str = str.replaceAll("#newline","\\\\newline");
        return str;
    }

    public static String tran(String str) {
        if (str==null) {
            return "";
        }
        str = str.replaceAll("\\\\colon", ":");
        str = str.replaceAll("\\\\semicolon", ";");
        str = str.replaceAll("\\\\comma", ",");
        str = str.replaceAll("\\\\newline","\r\n");
        return str;
    }

    public WorkflowActionDb getWorkflowActionDb(int id) {
        WorkflowActionCacheMgr wacm = new WorkflowActionCacheMgr();
        return wacm.getWorkflowActionDb(id);
    }

    public void load() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt;
        ResultSet rs;
        try {
            pstmt = conn.prepareStatement(LOAD);
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            if (!rs.next()) {
                LogUtil.getLog(getClass()).error("流程动作id= " + id + " 在数据库中未找到.");
                LogUtil.getLog(getClass()).trace(new Exception());
            } else {
                this.id = rs.getInt(1);
                this.flowId = rs.getInt(2);
                this.isStart = rs.getInt(3);
                this.reason = StrUtil.getNullString(rs.getString(4));
                this.status = rs.getInt(5);
                this.title = StrUtil.getNullStr(rs.getString(6));
                this.userName = StrUtil.getNullString(rs.getString(7));
                this.internalName = rs.getString(8);
                this.officeColorIndex = rs.getInt(9);
                this.userRealName = StrUtil.getNullString(rs.getString(10));
                this.jobCode = StrUtil.getNullString(rs.getString(11)); // 存放预定义的角色或者用户
                this.jobName = StrUtil.getNullString(rs.getString(12));
                this.direction = rs.getInt(13); // proxyJobCode
                this.rankCode = StrUtil.getNullString(rs.getString(14));   // proxyJobName
                this.rankName = StrUtil.getNullString(rs.getString(15));
                this.relateRoleToOrganization = StrUtil.getNullString(rs.getString(16)).equals("1");
                this.result = StrUtil.getNullString(rs.getString(17));
                this.checkDate = rs.getTimestamp(18);
                this.resultValue = rs.getInt(19);
                this.checkUserName = StrUtil.getNullString(rs.getString(20));
                this.fieldWrite = rs.getString(21);
                this.taskId = rs.getInt(22);
                this.dept = StrUtil.getNullString(rs.getString(23)).trim();
                this.flag = StrUtil.getNullString(rs.getString(24));
                this.nodeMode = rs.getInt(25);
                this.strategy = StrUtil.getNullStr(rs.getString(26));
                this.item1 = StrUtil.getNullString(rs.getString(27));
                item2 = StrUtil.getNullStr(rs.getString(28));

                plus = StrUtil.getNullStr(rs.getString(29));

                // parse item2 {relateToAction}
                parseItem2(item2);

                dateDelayed = rs.getTimestamp(30);

                canDistribute = rs.getInt(31)==1;
                msg = rs.getInt(32)==1;
                loaded = true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
    }

    /**
     * 解析item2，{relateToAction,ignoreType,kind,fieldHide,...}
     * @param item2 String
     */
    public void parseItem2(String item2) {
        if (item2.length()>2) {
            // 去掉{}
            String str = item2.substring(1, item2.length()-1);
            String[] ary = StrUtil.split(str, ",");
            if (ary.length>=1) {
                relateToAction = ary[0];
            }
            if (ary.length>=2) {
                ignoreType = StrUtil.toInt(ary[1], IGNORE_TYPE_DEFAULT);
            }
            if (ary.length>=3) {
                kind = StrUtil.toInt(ary[2], KIND_ACCESS);
            }
            if (ary.length>=4) {
                fieldHide = ary[3].replaceAll("\\|", ",");
            }
            if (ary.length>=5) {
                delayed = StrUtil.toInt(ary[4], 0)!=0;
            }
            if (ary.length>=6) {
                timeDelayedValue = StrUtil.toInt(ary[5], 0);
            }
            if (ary.length>=7) {
                timeDelayedUnit = StrUtil.toInt(ary[6], TIME_UNIT_DAY);
            }
            if (ary.length>=8) {
                canPrivUserModifyDelayDate = StrUtil.toInt(ary[7], 0)==1;
            }
            if (ary.length>=9) {
            	formView = StrUtil.toInt(ary[8], VIEW_DEFAULT);
            }
            if (ary.length>=10) {
                // 关联部门管理
                relateDeptManager = StrUtil.toInt(ary[9], 0) == 1;
            }
        }
    }
    
    /**
     * 生成item2
     * @Description: 
     * @return
     */
    public String generateItem2() {
    	String hide = fieldHide.replaceAll(",", "|");
    	return "{" + relateToAction + "," + ignoreType + "," + kind + ","
    		+ hide + "," + delayed + "," + timeDelayedValue + "," + timeDelayedUnit + "," + (canPrivUserModifyDelayDate?1:0) + "," + formView + "," + (relateDeptManager?1:0) + "}";
    }

    public boolean del() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(DELETE);
            pstmt.setInt(1, id);
            int r = pstmt.executeUpdate();
            if (r==1) {
                // 更新缓存
                WorkflowActionCacheMgr wac = new WorkflowActionCacheMgr();
                wac.refreshDel(id);
                return true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del:" + e.getMessage());
            return false;
        } finally {
            conn.close();
        }
        return false;
    }

    /**
     * 当流程被放弃时，置正处理和被打回的节点状态为已放弃
     * @return boolean
     */
    public boolean onWorkflowDiscarded(int flowId) throws ErrMsgException  {
        String sql = "select id from flow_action where ( status=" + WorkflowActionDb.STATE_DOING + " or status=" + WorkflowActionDb.STATE_RETURN + ") and flow_id=?";;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            // 置流程中正在处理状态的节点的状态为已放弃
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flowId);
            rs = conn.executePreQuery();
            if (rs!=null) {
                while (rs.next()) {
                    WorkflowActionDb wad = getWorkflowActionDb(rs.getInt(1));
                    wad.setStatus(STATE_DISCARDED);
                    wad.save();
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("onWorkflowDiscarded:" + e.getMessage());
            return false;
        } finally {
            conn.close();
        }
        return true;
    }

    /**
     * 当流程被手工结束时
     * @param flowId int
     * @return boolean
     */
    public boolean onWorkflowManualFinished(int flowId) throws ErrMsgException  {
        String sql = "select id from flow_action where ( status=" + WorkflowActionDb.STATE_DOING + " or status=" + WorkflowActionDb.STATE_RETURN + ") and flow_id=?";;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            // 置流程中正在处理状态的节点的状态为已结束
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flowId);
            rs = conn.executePreQuery();
            if (rs!=null) {
                while (rs.next()) {
                    WorkflowActionDb wad = getWorkflowActionDb(rs.getInt(1));
                    wad.setStatus(STATE_FINISHED);
                    wad.save();
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("onWorkflowManualFinish:" + e.getMessage());
            return false;
        } finally {
            conn.close();
        }
        return true;
    }

    /**
     * 取得流程中的所有action节点
     * @return Vector
     */
    public Vector<WorkflowActionDb> getActionsOfFlow(int flowId) {
        Vector<WorkflowActionDb> v = new Vector<>();
        String sql = "select id from flow_action where flow_id=? order by id asc";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt;
        ResultSet rs;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flowId);
            rs = conn.executePreQuery();
            if (rs == null) {
                LogUtil.getLog(getClass()).info("getActions:流程id= " + id + " 中的动作在数据库中未找到.");
            } else {
                WorkflowActionMgr wam = new WorkflowActionMgr();
                while (rs.next()) {
                    int actionId = rs.getInt(1);
                    v.addElement(wam.getWorkflowActionDb(actionId));
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getActions:" + e.getMessage());
        } finally {
            conn.close();
        }
        return v;
    }

    /**
     * 取得流程中的所有已结束的action节点
     * @return Vector
     */
    public List<WorkflowActionDb> getActionsFinishedOfFlow(int flowId) {
        List<WorkflowActionDb> list = new ArrayList<>();
        WorkflowActionMgr wam = new WorkflowActionMgr();
        String sql = "select id from flow_action where flow_id=? and status=" + STATE_FINISHED + " order by id desc";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt;
        ResultSet rs;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flowId);
            rs = conn.executePreQuery();
            while (rs.next()) {
                int actionId = rs.getInt(1);
                list.add(wam.getWorkflowActionDb(actionId));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getActions:" + e.getMessage());
        } finally {
            conn.close();
        }
        return list;
    }

    /**
     * 取得流程中的所有被延时的action节点
     * @return Vector
     */
    public Vector<WorkflowActionDb> getActionsDelayedOfFlow(int flowId) {
        Vector<WorkflowActionDb> v = new Vector<>();
        String sql =
                "select id from flow_action where flow_id=? and status=?";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flowId);
            pstmt.setInt(2, STATE_DELAYED);
            rs = conn.executePreQuery();
            if (rs == null) {
                LogUtil.getLog(getClass()).info("getActionsDelayedOfFlow:流程id= " + id + " 中的动作在数据库中未找到.");
            } else {
                WorkflowActionMgr wam = new WorkflowActionMgr();
                while (rs.next()) {
                    int actionid = rs.getInt(1);
                    v.addElement(wam.getWorkflowActionDb(actionid));
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getActions:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return v;
    }


    /**
     * 取得流程中的所有结束节点
     * @return Vector
     */
    public Vector<WorkflowActionDb> getEndActionsOfFlow(int flowId) {
        Vector<WorkflowActionDb> v = new Vector<>();
        String sql = "select id from flow_action where flow_id=? and item1='1'";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flowId);
            rs = conn.executePreQuery();
            if (rs == null) {
                LogUtil.getLog(getClass()).error("getEndActionsOfFlow:流程id= " + id + " 中的动作在数据库中未找到.");
            } else {
                WorkflowActionMgr wam = new WorkflowActionMgr();
                while (rs.next()) {
                    int actionid = rs.getInt(1);
                    v.addElement(wam.getWorkflowActionDb(actionid));
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getEndActionsOfFlow:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return v;
    }

    public boolean saveOnlyToDb() throws ErrMsgException {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(SAVE);
            pstmt.setInt(1, flowId);
            pstmt.setInt(2, isStart);
            pstmt.setString(3, reason);
            pstmt.setInt(4, status);
            pstmt.setString(5, title);
            pstmt.setString(6, userName);
            pstmt.setInt(7, officeColorIndex);
            pstmt.setString(8, userRealName);
            pstmt.setString(9, jobCode);
            pstmt.setString(10, jobName);
            pstmt.setInt(11, direction);
            pstmt.setString(12, rankCode);
            pstmt.setString(13, rankName);
            pstmt.setString(14, relateRoleToOrganization ? "1" : "0");
            pstmt.setString(15, result);
            if (checkDate == null) {
                pstmt.setDate(16, null);
            } else {
                pstmt.setTimestamp(16, new Timestamp(checkDate.getTime()));
            }
            pstmt.setInt(17, resultValue);
            pstmt.setString(18, checkUserName);
            pstmt.setString(19, fieldWrite);
            pstmt.setInt(20, taskId);
            pstmt.setString(21, dept);
            pstmt.setString(22, flag);
            pstmt.setInt(23, nodeMode);
            pstmt.setString(24, strategy);
            pstmt.setString(25, item1);
            pstmt.setString(26, item2);

            // parse item2 {relateToAction,ignoreType,kind,fieldHide}
            parseItem2(item2);

            pstmt.setString(27, plus);

            if (dateDelayed==null) {
                pstmt.setDate(28, null);
            } else {
                pstmt.setTimestamp(28, new Timestamp(dateDelayed.getTime()));
            }

            pstmt.setInt(29, canDistribute?1:0);
            pstmt.setInt(30, msg?1:0);
            pstmt.setInt(31, id);
            boolean re = pstmt.executeUpdate()==1;
            if (re) {
                // 更新缓存
                WorkflowActionCacheMgr wac = new WorkflowActionCacheMgr();
                wac.refreshSave(id);
            }
            return re;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error("save:" + e.getMessage());
        } finally {
            conn.close();
        }
        return false;
    }

    public boolean save() throws ErrMsgException {
        if (saveOnlyToDb()) {
            // 当设置代理、...后需重新生成一下flowString
            WorkflowDb wfd = new WorkflowDb();
            wfd = wfd.getWorkflowDb(flowId);
            LogUtil.getLog(getClass()).info("WorkflowActionDb.java save: renewWorkflowString flowId=" + flowId);
            wfd.renewWorkflowString(this, true);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * 获取本人处理完毕的动作节点 wad 的下一个被激活的动作节点，且该节点的处理者仍是本人（或者是由本人代理），只获取满足条件的第一个节点
     * @param userName String
     * @return MyActionDb
     */
    public MyActionDb getNextActionDoingWillBeCheckedByUserSelf(String userName) {
        MyActionDb mad = new MyActionDb();
        return mad.getMyActionDbOfFlowDoingByUser(flowId, userName);
    }

    /**
     * 当流程结束之前，判断是否为子流程，如果是，则更新父流程
     * @param request HttpServletRequest
     * @param wf WorkflowDb
     */
    public void doWorkflowFinished(HttpServletRequest request, WorkflowDb wf) throws ErrMsgException {
        // 判断流程状态是否为已结束
        // 因为在afterChangeStatus会根据结束型节点和节点是否全部处理完毕来先后判断并处理流程是否结束，所有有可能会重复调用doWorkflowFinished
        if (wf.getStatus()!=WorkflowDb.STATUS_FINISHED) {
            // 如果该流程为子流程，则更改节点状态
            if (wf.getParentActionId() != WorkflowDb.PARENT_ACTION_ID_NONE) {
                LogUtil.getLog(getClass()).info("afterChangeStatus: wf.getParentActionId()=" + wf.getParentActionId());
                WorkflowActionDb pwad = new WorkflowActionDb();
                pwad = pwad.getWorkflowActionDb((int) wf.getParentActionId());
                WorkflowDb pwf = wf.getWorkflowDb(pwad.getFlowId());

                MyActionDb pmad = new MyActionDb();
                pmad = pmad.getMyActionDbOfActionDoingByUser(pwad, wf.getUserName());

                LogUtil.getLog(getClass()).info("doWorkflowFinished: pmad=" + pmad);

                // 映射字段
                WorkflowPredefineDb wpd = new WorkflowPredefineDb();
                wpd = wpd.getDefaultPredefineFlow(pwf.getTypeCode());
                LogUtil.getLog(getClass()).info("doWorkflowFinished:pwad.getInternalName()=" + pwad.getInternalName());
                LogUtil.getLog(getClass()).info("doWorkflowFinished:props=" + wpd.getProps());
                JSONObject json = getProps(wpd, pwad.getInternalName());
                doSubToParentMap(pwf, wf, json);

                pwad.changeStatus(request, pwf, checkUserName, STATE_FINISHED, "", "",
                                  WorkflowActionDb.RESULT_VALUE_AGGREE,
                                  pmad.getId());
            }
            // 调用脚本
            // Leaf lf = new Leaf();
            // lf = lf.getLeaf(wf.getTypeCode());
            // NetUtil.gather(request, "utf-8", Global.getFullRootPath(request) + "/flow/form_js_" + lf.getFormCode() + ".jsp?op=flowFinished&flowId=" + wf.getId());
        }
        
        LogUtil.getLog(getClass()).info("doWorkflowFinished: flowId=" + wf.getId());
        
        wf.changeStatus(request, WorkflowDb.STATUS_FINISHED, this);
    }

    /**
     * 转交给下一节点
     * @param request HttpServletRequest
     * @param wf WorkflowDb
     * @param nextwa WorkflowActionDb
     * @param myActionId long
     * @param deptOfUserWithMultiDept String
     */
    public void deliverToNextAction(HttpServletRequest request, WorkflowDb wf, WorkflowActionDb nextwa, long myActionId, String deptOfUserWithMultiDept) throws
            ErrMsgException {
        // 如果下一节点被延时
        if (nextwa.isDelayed()) {
            boolean isDelayed = true;
            int timeDelayedValue = nextwa.getTimeDelayedValue();
            int timeUnit = nextwa.getTimeDelayedUnit();
            // 如果允许前一节用户修改到期时间，只支持上一节点出度为1的情况
            if (nextwa.isCanPrivUserModifyDelayDate()) {
                WorkflowParams wparam = (WorkflowParams) request.getAttribute("workflowParams");
                FileUpload fu = wparam.getFileUpload();
                isDelayed = "1".equals(StrUtil.getNullStr(fu.getFieldValue("isDelayed")));
                if (isDelayed) {
                    timeDelayedValue = StrUtil.toInt(fu.getFieldValue("timeDelayedValue"), 0);
                    if (timeDelayedValue==0) {
                        isDelayed = false;
                    }
                    timeUnit = StrUtil.toInt(fu.getFieldValue("timeDelayedUnit"));
                }
            }

            if (isDelayed) {
                nextwa.setStatus(STATE_DELAYED);
                java.util.Date d = null;
                if (timeUnit == TIME_UNIT_WORKDAY) {
                    d = OACalendarDb.addWorkDay(timeDelayedValue);
                } else if (timeUnit == TIME_UNIT_WORKHOUR) {
                    d = OACalendarDb.addWorkHour(timeDelayedValue);
                } else if (timeUnit == TIME_UNIT_DAY) {
                    d = DateUtil.addDate(new java.util.Date(), timeDelayedValue);
                } else if (timeUnit == TIME_UNIT_HOUR) {
                    d = DateUtil.addHourDate(new java.util.Date(), timeDelayedValue);
                }

                nextwa.setDateDelayed(d);
                nextwa.save();
                return;
            }
        }

        /*

        // 如果节点类别是子流程
        if (nextwa.getKind() == WorkflowActionDb.KIND_SUB_FLOW) {
            // 创建子流程
            createSubFlow(request, wf, nextwa, this, myActionId);
        } else {
            // 此时nextwa中，已经过flow_dipose.jsp页面匹配matchActionUser，如果匹配到，nextwa中的userName将会存在用户
            nextwa.changeStatus(request, wf, checkUserName,
                                WorkflowActionDb.STATE_DOING, "", "",
                                nextwa.getResultValue(), myActionId);
            // 通知用户办理
            String[] users = StrUtil.split(nextwa.getUserName(), ",");
            int len = 0;
            // 为null表示matchActionUser未匹配到用户
            if (users != null) {
                len = users.length;
                for (int n = 0; n < len; n++) {
                    MyActionDb mad = wf.notifyUser(users[n],
                                                   new java.util.Date(), myActionId, this,
                                                   nextwa, STATE_DOING,
                                                   (long) wf.getId());
                    addTmpUserNameActived(mad);
                }
            } else {
                // 自动跳过未匹配到用户的节点
                WorkflowActionDb nwa = autoPassActionNoUserMatched(myActionId, this, nextwa);
                // 如果连续存在未匹配到用户的节点，则继续跳过
                while (nwa != null) {
                    nwa = autoPassActionNoUserMatched(myActionId, this, nwa);
                }
            }
        }
        */

       // 保存有兼职时所选择的部门，比如：当关联开始节点时，需保存开始节点人员有兼职时，所选择的部门
       if (deptOfUserWithMultiDept!=null && !"".equals(deptOfUserWithMultiDept)) {
           dept = deptOfUserWithMultiDept;
           save();
       }

       // @task:当从子流程回到父流程时，父流程节点中如果用户多于1个，则会被自动忽略，因此必须唯一
       String[] users = StrUtil.split(nextwa.getUserName(), ",");

        // 如果未匹配到用户，则自动跳过该节点
        if (users == null) {
            WorkflowActionDb nextActionToActive = nextwa;
            WorkflowActionDb nwa = autoPassActionNoUserMatched(request, myActionId, this, nextwa, deptOfUserWithMultiDept, false);
            // 如果连续存在未匹配到用户的节点，则继续跳过
            while (nwa != null) {
                nextActionToActive = nwa;
                nwa = autoPassActionNoUserMatched(request, myActionId, this, nwa, deptOfUserWithMultiDept, false);
            }

            if (nwa == null) {
                // 运行激活事件
                IWorkflowScriptUtil workflowScriptUtil = SpringUtil.getBean(IWorkflowScriptUtil.class);
                workflowScriptUtil.runActiveScript(request, wf, myActionId, nextActionToActive, false);
            }
        } else {
            if (nextwa.getKind()==WorkflowActionDb.KIND_SUB_FLOW) {
                // 创建子流程
                createSubFlow(request, wf, nextwa, this, myActionId);
            }
            else {
                int oldStatus = nextwa.getStatus();
                if (oldStatus!=STATE_DOING) {
                    if (nextwa.isDistribute()) {
                        // 如果是分发节点，则置该节点状态为允许分发，用于公文分发列表
                        nextwa.setCanDistribute(true);
                    }
                    nextwa.setStatus(STATE_DOING);
                    nextwa.save();

                    // 运行激活事件
                    IWorkflowScriptUtil workflowScriptUtil = SpringUtil.getBean(IWorkflowScriptUtil.class);
                    workflowScriptUtil.runActiveScript(request, wf, myActionId, nextwa, false);
                }

                MyActionDb myActionDb = new MyActionDb();
                myActionDb = myActionDb.getMyActionDb(myActionId);
                MyActionDb privMyActionDb = myActionDb.getMyActionDb(myActionDb.getPrivMyActionId());

                WorkflowActionDb curAction = new WorkflowActionDb();
                curAction = curAction.getWorkflowActionDb((int)myActionDb.getActionId());

                // 如果下一节点为异步提交模式
                // if (nextwa.isXorFinish()) {
                // 20220812 改为如果本节点为异步提交模式
                if (curAction.isXorFinish()) {
                    // 如果当前待办记录是被下一节点的处理者退回，则下一节点只生成退回者的待办记录
                    if (privMyActionDb.getCheckStatus() == MyActionDb.CHECK_STATUS_RETURN && privMyActionDb.getActionId()==nextwa.getId()) {
                        MyActionDb mad = wf.notifyUser(privMyActionDb.getUserName(),
                                new Date(),
                                myActionId, this,
                                nextwa, STATE_DOING,
                                wf.getId());
                        addTmpUserNameActived(mad);
                    }
                    else {
                        // 20220718改为取当前用户所选的下一节点上的人员（原来取的是节点上匹配到的所有人员数组users)，注意users中为节点上已匹配到的全部人员，前者为后者的子集
                        // 当角色关联且上一节点存在多个用户，会匹配到多人，但跟当前用户相关联的只有其中的一部分
                        // 如：角色关联，下行，当前节点为副局长，下一节点为处长，按原来的实现逻辑，当某一副局长提交后，所有处长都会收到待办记录
                        WorkflowParams workflowParams = (WorkflowParams)request.getAttribute("workflowParams");
                        FileUpload fu = workflowParams.getFileUpload();
                        String[] aryUsers = fu.getFieldValues("WorkflowAction_" + nextwa.getId());
                        // List<String> curSelUserList = Arrays.asList(aryUsers);
                        for (String user : aryUsers) {
                            // 下一节点为异步提交时，如果该节点上的用户已经有过待办记录，则不再重复生成，如发散分支上的其他节点
                            boolean canNotify = true;
                            MyActionDb nextUserMyActionDb = MyActionDb.getMyActionDbByAction(flowId, user, nextwa.getId());
                            if (nextUserMyActionDb != null) {
                                canNotify = false;
                            }

                            if (canNotify) {
                                MyActionDb mad = wf.notifyUser(user,
                                        new Date(),
                                        myActionId, this,
                                        nextwa, STATE_DOING,
                                        wf.getId());
                                addTmpUserNameActived(mad);
                            }
                            else {
                                // 注释掉，因为异步提交不可能出现这样的情况
                                // 如果当前用户选择了该用户，判断下一节点上的待办如果不是当前用户提交的，则修改待办记录中的priv_myaction_id为当前用户的待办记录
                                /*if (curSelUserList.contains(user)) {
                                    if (nextUserMyActionDb.getPrivMyActionId() != myActionId) {
                                        nextUserMyActionDb.setPrivMyActionId(myActionId);
                                        nextUserMyActionDb.save();
                                    }
                                }*/
                            }
                        }
                    }
                }
                else {
                    // 通知用户办理
                    for (String user : users) {
                        // 如果下一节点原来的状态为处理中，且下一节点上存在此用户的未处理的待办记录，则跳过，以免生成重复的待办记录
                        boolean canNotify = true;
                        if (oldStatus == STATE_DOING && MyActionDb.isNotifyNotCheckedExistOnAction(flowId, user, nextwa.getId())) {
                            canNotify = false;
                        }

                        if (canNotify) {
                            MyActionDb mad = wf.notifyUser(user,
                                    new Date(),
                                    myActionId, this,
                                    nextwa, STATE_DOING,
                                    wf.getId());
                            addTmpUserNameActived(mad);
                        }
                    }
                }
            }
        }
    }

    /**
     * 判断能否转交至下个节点
     * @param nextwa WorkflowActionDb
     * @return boolean
     */
    public boolean canDevliveToNextAction(WorkflowActionDb nextwa) {
    	// 20161025 fgf 解决多重聚合的问题
        Vector<WorkflowActionDb> vfrom = nextwa.getLinkFromActions();

        // 如果下一结点不是聚合结点，则向下转交
        boolean re = true;
        if (vfrom.size() > 1) {
            // 如果下一结点是聚合结点，则如果为异或聚合节点时置其为DOING，否则若其所有连接from结点的状态为FINISHED或者IGNORED，则置其为DOING
            boolean flagXorAggregate = false;
            if (nextwa.getFlag().length() >= 8) {
                if ("1".equals(nextwa.getFlag().substring(7, 8))) {
                    flagXorAggregate = true;
                }
            }
            LogUtil.getLog(getClass()).info("canDevliveToNextAction: flagXorAggregate=" + flagXorAggregate);
            if (!flagXorAggregate) {
                for (Object o : vfrom) {
                    WorkflowActionDb wfa = (WorkflowActionDb) o;
                    LogUtil.getLog(getClass()).info("wfa.getJobName()=" + wfa.getJobName() + " status=" +
                            wfa.getStatusName());
                    if (!(wfa.getStatus() == WorkflowActionDb.STATE_FINISHED ||
                            wfa.getStatus() == WorkflowActionDb.STATE_IGNORED)) {
                        re = false;
                        break;
                    }
                }
            }
        }
        else {
            // 不能将nextwa.getStatus()!=nextwa.STATE_IGNORED && nextwa.getStatus()!=WorkflowActionDb.STATE_DOING置于canDevliveToNextAction中
            // 因为在afterChangeStatus中当v.size()==1时，如果后续节点为被忽略，则仍然需转交过去
        }
        return re;
    }

    public boolean afterChangeStatus(HttpServletRequest request, WorkflowDb wf, String checkUserName,
                                     int oldStatus, int newstatus, long myActionId) throws ErrMsgException  {
        // 如果状态被置为已完成
        if (newstatus == STATE_FINISHED) {
            MyActionDb mad = new MyActionDb();
            mad = mad.getMyActionDb(myActionId);

            // 如果之前的状态为被返回
            if (oldStatus==WorkflowActionDb.STATE_RETURN) {
                // 激活打回的节点
                WorkflowPredefineDb wpd = new WorkflowPredefineDb();
                wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());

                String flowAction = StrUtil.getNullStr((String)request.getAttribute("flowAction"));

                // 如果返回后再交办时直接至返回者(直送)
                if ("toRetuner".equals(flowAction) && wpd.getReturnMode()==WorkflowPredefineDb.RETURN_MODE_TO_RETURNER) {
                    // LogUtil.getLog(getClass()).info("afterChangeStatus mad id=" + mad.getId() + " resultValue=" + WorkflowActionDb.RESULT_VALUE_TO_RETUNER);
                    mad.setResultValue(WorkflowActionDb.RESULT_VALUE_TO_RETUNER);// 直送
                    mad.save();

                    MyActionDb madPre = mad.getMyActionDb(mad.getPrivMyActionId());
                    WorkflowActionDb nextwa = new WorkflowActionDb();
                    nextwa = nextwa.getWorkflowActionDb((int)madPre.getActionId());

                    nextwa.changeStatus(request, wf, checkUserName,
                                        WorkflowActionDb.STATE_DOING,
                                        "", "", nextwa.getResultValue(),
                                        myActionId);
                    // 通知用户办理
                    String[] users = StrUtil.split(nextwa.getUserName(), ",");
                    for (String user : users) {
                        mad = wf.notifyUser(user,
                                new Date(), myActionId, this,
                                nextwa, STATE_DOING,
                                wf.getId());
                        addTmpUserNameActived(mad);
                    }

                    return true;
                }
            }

            LogUtil.getLog(getClass()).info("afterChangeStatus:" + getUserName() + " finished!");

            // 检查所有结束节点是否都已完成
            boolean isDoFinished = false;
            // 如果流程已结束（可能为变更流程）
            if (wf.getStatus()==WorkflowDb.STATUS_FINISHED) {
                isDoFinished = true;
            }
            else {
                // 如果所有结束节点都已完成
                if (wf.checkEndActionsStatusFinished()) {
                    // 如果是允许变更，而且流程以前曾经结束过，则只有当本节点为结束节点时，才可以结束流程，以免因结束流程而造成结束事件运行多次
                    WorkflowPredefineDb wfp = new WorkflowPredefineDb();
                    wfp = wfp.getDefaultPredefineFlow(wf.getTypeCode());
                    if (wfp.isReactive() && wf.getEndDate() != null) {
                        if (isEnd()) {
                            doWorkflowFinished(request, wf);
                            isDoFinished = true;
                        }
                    } else {
                        doWorkflowFinished(request, wf);
                        isDoFinished = true;
                    }
                }
            }

            // 如果本结点为发散结点
            Vector<WorkflowActionDb> v = getLinkToActions();

            LogUtil.getLog(getClass()).info("afterChangeStatus: v.size=" + v.size() + " isDoFinished=" + isDoFinished);

            if (v.size() > 1) {
                ArrayList<WorkflowActionDb> selectedActions = new ArrayList<WorkflowActionDb>();
                Iterator<WorkflowActionDb> ir = v.iterator();
                while (ir.hasNext()) {
                	WorkflowActionDb nextwa = ir.next();
                    nextwa = new WorkflowActionDb(nextwa.getId());
                    if (!"".equals(nextwa.getUserName())) {
                    	selectedActions.add(nextwa);
                    	continue;
                    }
                }
                ir = v.iterator();
                while (ir.hasNext()) {
                    WorkflowActionDb nextwa = ir.next();
                    // 因为在处理一条分支的时候，可能会对分支的汇聚节点产生影响，所以不能通过缓存取
                    nextwa = new WorkflowActionDb(nextwa.getId());
                    // 如果下一节点没有被跳过且不是正处理状态（因为其它分支可能会激活汇聚节点）
                    // 只有当某分支中间出现跳过的情况时，才会出现聚合节点被激活的情况
                    // 详见文档：《20120106大亚流程问题(聚合节点).doc》

                    LogUtil.getLog(getClass()).info("afterChangeStatus: nextwa.getStatus()=" + nextwa.getStatus() + " title=" + nextwa.getTitle() + " userName=" + nextwa.getUserName());

                    boolean canDelive = true;
                    if (nextwa.getStatus() == WorkflowActionDb.STATE_DOING) {
                        canDelive = false;
                    } else if (nextwa.getStatus() == WorkflowActionDb.STATE_IGNORED) {
                        // 20221201 如果分支被忽略，但nextwa为聚合节点，那么也可以走该分支，否则流程将走不下去，如果不是聚合节点，则不能再往下走
                        Vector<WorkflowActionDb> vFrom = nextwa.getLinkFromActions();
                        if (vFrom.size() > 1) {
                            // 20220112如果当前待办记录中的action有条件分支，说明nextwa所在分支不满足条件，则不能往下走，详见：2022011201
                            WorkflowActionDb curAction = nextwa.getWorkflowActionDb((int)mad.getActionId());
                            if (curAction.isXorRadiate()) {
                                canDelive = false;
                            }
                        }
                        else if (vFrom.size() == 1) {
                            canDelive = false;
                        }
                    }
                    if (canDelive) {
                        boolean isFinished = canDevliveToNextAction(nextwa);
                        LogUtil.getLog(getClass()).info("afterChangeStatus: " + nextwa.getTitle() + " jobName=" + nextwa.getJobName() + " isFinished=" + isFinished);
                        if (isFinished) {
                        	boolean toIgnore = false;
                        	if (!"".equals(nextwa.getUserName()) || selectedActions.isEmpty()) {
                        		deliverToNextAction(request, wf, nextwa, myActionId, getDeptOfUserWithMultiDept(request));
                        	} 
                        	else if (nextwa.getIgnoreType() == WorkflowActionDb.IGNORE_TYPE_USER_ACCESSED_BEFORE && "".equals(nextwa.getUserName())) {
                        		// 无用户或用户之前处理过则跳过，当用户之前处理过时，在mactchActionUser时，会过滤掉已处理过的用户，action中的userName仍为空
                        		deliverToNextAction(request, wf, nextwa, myActionId, getDeptOfUserWithMultiDept(request));                        		
                        	}
                        	else if (nextwa.getIgnoreType() == WorkflowActionDb.IGNORE_TYPE_NOT && "".equals(nextwa.getUserName())) {
                            	toIgnore = true;
                            } else  {
                        		toIgnore = true;
                        	}
                            if (toIgnore) {
                                WorkflowMgr wfm = new WorkflowMgr();
                            	/*
                            	 * 有问题,后期调整
                        		for (WorkflowActionDb selectedAction : selectedActions) {
        	                    	WorkflowActionDb endAction = wfm.getRelationOfTwoActions(nextwa, selectedAction);
        	                    	wfm.ignoreBranch(nextwa, endAction);
                            	}*/
                                // 20220606 忽略当前分支
                                wfm.ignoreBranch(nextwa, null);
                            }
                        }
                    }
                    else {
                        if (nextwa.getStatus() == WorkflowActionDb.STATE_DOING) {
                            // 20220812 如果本节点是异步提交
                            if (isXorFinish()) {
                                deliverToNextAction(request, wf, nextwa, myActionId, getDeptOfUserWithMultiDept(request));
                            }
                        }
                    }
                }
            } else if (v.size() == 1) { // 如果本结点不是发散结点
                WorkflowActionDb nextwa = v.get(0);
                boolean isFinished = canDevliveToNextAction(nextwa);
                LogUtil.getLog(getClass()).info("afterChangeStatus: isFinished=" + isFinished);
                if (isFinished) {
                    deliverToNextAction(request, wf, nextwa, myActionId, getDeptOfUserWithMultiDept(request));
                    // 20220720判断流程是否未结束，因为如果本节点为辅助角色且为结束节点，而其它节点都已处理完毕，那么在autoPassActionNoUserMatched跳过本节点时，
                    // 已经调用过了doWorkflowFinished，所以此处需加判断，当流程未结束时才可以去检查流程是否需要作结束处理
                    wf = wf.getWorkflowDb(wf.getId()); // 因为在deliverToNextAction的autoPassActionNoUserMatched可能改变了流程状态，所以这儿需重新获取
                    if (wf.getStatus()!=WorkflowDb.STATUS_FINISHED) {
                        // 20220112 将deliverToNextAction中自动跳过时检查流程是否节点已全部处理完毕移至此处
                        if (wf.checkStatusFinished()) {
                            doWorkflowFinished(request, wf);
                        }
                    }
                }
            } else {
                // 无下一结点，如果检查更改流程中的各个节点的状态都为已完成，则置流程状态为已完成
            	if (!isDoFinished) {
            	    // 如果流程未结束
                    if (wf.getStatus()!=WorkflowDb.STATUS_FINISHED) {
                        // 检查流程中的节点是否都已完成
                        if (wf.checkStatusFinished()) {
                            LogUtil.getLog(getClass()).info("afterChangeStatus: checkStatusFinished=true");
                            doWorkflowFinished(request, wf);
                        }
                    }
            	}
            }
        } else if (newstatus == STATE_DOING && oldStatus == STATE_DOING && !"".equals(userName)) {
            // 节点状态仍为办理中，即节点中的用户们并未都处理完毕
        	String[] userNames = userName.split(",");
        	if (userNames.length <= 1) {
        		return true;
        	}

            // 异步提交后，后续节点只要是没被忽略的，都提交，即便该节点的状态为正在处理中
            Vector<WorkflowActionDb> v = getLinkToActions();
            if (isXorFinish()) {
                // 如果本结点不是发散结点
                // 20220812 改为支持发散节点
                // if (v.size() == 1) {
                    for (WorkflowActionDb nextwa : v) {
                        if ("".equals(nextwa.getUserName())) {
                            continue;
                        }
                        if (nextwa.getStatus() != WorkflowActionDb.STATE_IGNORED) {
                            deliverToNextAction(request, wf, nextwa, myActionId, getDeptOfUserWithMultiDept(request));
                        }
                    }
                // }
            }
            else {
                // @task:如果当非下达模式时，如上一节点有多人处理，有人先选择了下一节点的用户，而上一节点中后来审批的人去掉了勾选的用户，则此处会产生垃圾数据
                // 所以不如将状态CHECK_STATUS_WAITING_TO_DO废除，且不生成“等待处理”的待办记录，意义似乎不是太大
                if (v.size() == 1) { // 如果本结点不是发散结点
                    WorkflowActionDb nextwa = v.get(0);
                    if ("".equals(nextwa.getUserName())) {
                        return true;
                    }
                    userNames = nextwa.getUserName().split(",");
                    for (String userName : userNames) {
                        if (MyActionDb.isActionWaitExist(nextwa.getId(), userName) != 0) {
                            continue;
                        }
                        MyActionDb md = new MyActionDb();
                        md.setFlowId(nextwa.getFlowId());
                        md.setActionId(nextwa.getId());
                        md.setPrivMyActionId(myActionId);
                        md.setUserName(userName);
                        md.setCheckStatus(MyActionDb.CHECK_STATUS_WAITING_TO_DO);
                        md.create();
                    }
                }
            }
        }
        return true;
    }

    /**
     * 节点是否为异步提交
     * @return
     */
    public boolean isXorFinish() {
        // 是否异步提交
        boolean flagXorFinish = false;
        if (flag.length() >= 13) {
            if ("1".equals(flag.substring(12, 13))) {
                flagXorFinish = true;
            }
        }
        return flagXorFinish;
    }

    /**
     * 是否异步退回
     * @return
     */
    public boolean isXorReturn() {
        boolean flagXorReturn = false;
        if (flag.length()>=14) {
            if ("1".equals(flag.subSequence(13, 14))) {
                flagXorReturn = true;
            }
        }
        return flagXorReturn;
    }

    /**
     * 跳过未匹配到用户的节点，如果是条件分支，则匹配成功即继续，否则则无法跳过，返回null
     * 如果在本方法中匹配到用户，则激活该节点
     * 注意如果发散节点被跳过，则当其不是条件分支时，匹配到人员的分支线都会被激活，如果多个分支线上没有匹配到人员，则只有最后被处理的分支线才能接着被跳过
     * @param myActionId long 正在处理的MyActionDb的ID
     * @param myAction WorkflowActionDb 当前节点，对应myActionId处理的节点
     * @param nextActionToPass WorkflowActionDb 下一个将被跳过的节点
     * @param isJob boolean 是否来自于调度执行
     * @return WorkflowActionDb curAction的下一节点如果匹配不到用户，则返回该节点，否则返回null
     */
    public WorkflowActionDb autoPassActionNoUserMatched(HttpServletRequest request, long myActionId, WorkflowActionDb myAction, WorkflowActionDb nextActionToPass, String deptOfUserWithMultiDept, boolean isJob) throws ErrMsgException {
        // 如果不允许跳过
        if (nextActionToPass.getIgnoreType()==IGNORE_TYPE_NOT) {
            LogUtil.getLog(getClass()).info("autoPassActionNoUserMatched:" + nextActionToPass.getJobName() + " 不允许被跳过");
            return null;
        }

        LogUtil.getLog(getClass()).info("autoPassActionNoUserMatched:" + myActionId);

        // 置待办记录对应的action的状态为被忽略
        nextActionToPass.setStatus(WorkflowActionDb.STATE_IGNORED);
        try {
            nextActionToPass.save();
        } catch (ErrMsgException ex3) {
            ex3.printStackTrace();
        }

        // 如果流程未结束，当前被跳过节点是结束节点（如：辅助角色），或者流程所有节点已结束，则使流程状态变为已完成
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(nextActionToPass.getFlowId());
        boolean isNeedFinish = false;
        if (wf.getStatus() != WorkflowDb.STATUS_FINISHED) {
            if (nextActionToPass.isEnd()) {
                // 这里不需要再checkEndActionsStatusFinished，如果多起點的話，這樣會因爲被忽略的分支線上的結束节点，使得流程变为結束狀態
                isNeedFinish = true;
            }
            else {
                // 检查流程中的节点是否都已完成
                // 20220112 注释掉，因为后面可能会连续跳过（或者多起点时），但当前检查所有节点可能都已完成或忽略，故将下面的代码移至afterChangeStatus中当v.size()==1时
                /*if (wf.checkStatusFinished()) {
                    isNeedFinish = true;
                }*/
            }
        }
        if (isNeedFinish) {
            doWorkflowFinished(request, wf);
        }

        // 如果是发散节点，带有条件分支，则如果跳过该节点时，匹配到了分支线（条件分支有且仅有一条符合条件，不会出现多条的情况）
        Vector<WorkflowActionDb> v = nextActionToPass.getLinkToActions();
        if (v.size() > 1) {
            LogUtil.getLog(getClass()).info("autoPassActionNoUserMatched:v.size=" + v.size());
            // 如果是异或发散节点，则先匹配分支
            // if (myAction.isXorRadiate()) {
            if (nextActionToPass.isXorRadiate()) {
                Vector<WorkflowLinkDb> vMatched = WorkflowRouter.matchNextBranch(nextActionToPass, myAction.getUserName(), new StringBuffer(), myActionId);
                // LogUtil.getLog(getClass()).info("autoPassActionNoUserMatched:wld=" + wld);
                if (vMatched.size()>0) {
                    WorkflowMgr wfm = new WorkflowMgr();
                    for (WorkflowLinkDb wld : vMatched) {
                        // 置分支线
                        wfm.setXorRadiateNextBranch(nextActionToPass, wld.getTo());

                        WorkflowActionDb toBranchAct = wld.getToAction();
                        LogUtil.getLog(getClass()).info(
                                "autoPassActionNoUserMatched:toBranchAct=" +
                                        toBranchAct.getTitle());
                        v.clear(); // TODO: 不应该清除????? 如果清除那只能保留匹配到的最后一个分支线
                        v.addElement(toBranchAct);
                        // 匹配到了分支线才允许跳过
                        // return vt;
                    }
                } else {
                    return null;
                }
            }
        }

        IMessage imsg = null;
        ProxyFactory proxyFactory = new ProxyFactory("com.redmoon.oa.message.MessageDb");
        Advisor adv = new Advisor();
        MobileAfterAdvice mba = new MobileAfterAdvice();
        adv.setAdvice(mba);
        adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
        proxyFactory.addAdvisor(adv);
        imsg = (IMessage) proxyFactory.getProxy();

        // 20220705 增加，用于记录将要被跳过的节点，注意只能记录一个分支，@TODO 将来需扩展能支持多个分支
        WorkflowActionDb nextWaToPass = null;

        for (WorkflowActionDb nextwa : v) {
            // 检查后续节点的入度，如果为非异或聚合，则检查聚合节点前面的节点是否均已办理完毕
            Vector<WorkflowActionDb> vfrom = nextwa.getLinkFromActions();
            boolean isFinished = true;
            boolean flagXorAggregate = false;
            if (nextwa.getFlag().length() >= 8) {
                if (nextwa.getFlag().substring(7, 8).equals("1")) {
                    flagXorAggregate = true;
                }
            }

            LogUtil.getLog(getClass()).info("autoPassActionNoUserMatched:flagXorAggregate=" + flagXorAggregate);

            if (!flagXorAggregate) {
                for (WorkflowActionDb wfa : vfrom) {
                    if (wfa.getStatus() == WorkflowActionDb.STATE_FINISHED || wfa.getStatus() == WorkflowActionDb.STATE_IGNORED) {
                        ;
                    } else {
                        isFinished = false;
                        break;
                    }
                }
            }

            LogUtil.getLog(getClass()).info("autoPassActionNoUserMatched:isFinished=" + isFinished);

            if (isFinished) {
                // 匹配节点上的用户
                Vector<UserDb> vt = null;
                try {
                    WorkflowRouter workflowRouter = new WorkflowRouter();
                    vt = workflowRouter.matchActionUser(request, nextwa, myAction, false, deptOfUserWithMultiDept);
                } catch (MatchUserException e) {
                    LogUtil.getLog(getClass()).error(e);
                    throw new ErrMsgException(e.getMessage());
                }

                // 如果未匹配到用户，则继续处理下一节点
                // 如果同时激活多个后续节点（待跳过节点为发散节点），则如遇到某一个用户匹配不到的情况，则会继续向后匹配，而其它节点却不会被激活
                if (vt.size() == 0) {
                    nextWaToPass = nextwa;
                    continue;
                }

                if (vt.size() == 1) {
                    UserDb ud = vt.get(0);
                    if (ud.getName().equals(PRE_TYPE_USER_SELECT)) {
                        return nextwa;
                    }
                }

                // 如果有用户，则激活后续节点
                nextwa.setStatus(WorkflowActionDb.STATE_DOING);
                try {
                    nextwa.save();
                } catch (ErrMsgException ex) {
                    LogUtil.getLog(getClass()).error(ex);
                }

                String t = SkinUtil.LoadString(null, "res.module.flow", "msg_user_actived_title");
                String c = SkinUtil.LoadString(null, "res.module.flow", "msg_user_actived_content");
                t = t.replaceFirst("\\$flowTitle", wf.getTitle());
                c = c.replaceFirst("\\$flowTitle", wf.getTitle());
                c = c.replaceFirst("\\$fromUser", myAction.getUserRealName());
                String c_sms = c;
                if (!isJob) {
                    // WorkflowAutoDeliverJob中调用本方法时，SpringUtil.getRequest会报错
                    c += WorkflowMgr.getFormAbstractTable(wf);
                }

                Config cfg = new Config();
                boolean flowNotifyByEmail = cfg.getBooleanProperty("flowNotifyByEmail");
                cn.js.fan.mail.SendMail sendmail = new cn.js.fan.mail.SendMail();
                String senderName = StrUtil.GBToUnicode(Global.AppName);
                senderName += "<" + Global.getEmail() + ">";
                if (flowNotifyByEmail) {
                    String mailserver = Global.getSmtpServer();
                    int smtp_port = Global.getSmtpPort();
                    String name = Global.getSmtpUser();
                    String pwd_raw = Global.getSmtpPwd();
                    try {
                        sendmail.initSession(mailserver, smtp_port, name, pwd_raw);
                    } catch (Exception ex) {
                        LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
                    }
                }

                for (UserDb user : vt) {
                    if (!user.isLoaded()) {
                        continue;
                    }
                    MyActionDb nextMad = wf.notifyUser(user.getName(), new Date(),
                            myActionId, nextActionToPass,
                            nextwa,
                            WorkflowActionDb.STATE_DOING,
                            wf.getId());
                    // 发送给后续节点通知处理短信
                    try {
                        MessageDb md = new MessageDb();
                        String action = "action=" +
                                MessageDb.ACTION_FLOW_DISPOSE +
                                "|myActionId=" + nextMad.getId();
                        if (imsg != null) {
                            imsg.sendSysMsg(user.getName(), t, c_sms, action);
                        } else {
                            md.sendSysMsg(user.getName(), t, c, action);
                        }

                        if (flowNotifyByEmail) {
                            com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();

                            UserMgr um = new UserMgr();
                            user = um.getUserDb(nextMad.getUserName());
                            if (!"".equals(user.getEmail())) {
                                action = "userName=" + user.getName() + "|" + "myActionId=" + nextMad.getId();
                                action = cn.js.fan.security.ThreeDesUtil.
                                        encrypt2hex(
                                                ssoCfg.getKey(), action);
                                String fc = c + "<BR />>>&nbsp;<a href='" +
                                        WorkflowUtil.getJumpUrl(WorkflowUtil.OP_FLOW_PROCESS, action) +
                                        "' target='_blank'>请点击此处办理</a>";
                                sendmail.initMsg(user.getEmail(),
                                        senderName, t, fc, true);
                                sendmail.send();
                                sendmail.clear();
                            }
                        }
                    } catch (ErrMsgException ex2) {
                        ex2.printStackTrace();
                    }
                }
            }
        }
        return nextWaToPass;
    }

    /**
     * 在本动作状态被置为FINISHED后激发
     * @param request HttpServletRequest
     * @param wf WorkflowDb
     * @param checkUserName String
     * @param newstatus int
     * @param myActionId long
     * @return boolean
     */
    public boolean afterChangeStatusFree(HttpServletRequest request, WorkflowDb wf, String checkUserName,
                                     int newstatus, long myActionId) throws ErrMsgException  {
        // 如果状态被置为已完成
        if (newstatus == STATE_FINISHED) {
            LogUtil.getLog(getClass()).info("afterChangeStatus:" + getUserName() + " finished!");
            // 检查所有结束节点是否都已完成
            if (wf.checkEndActionsStatusFinished()) {
                wf.changeStatus(request, WorkflowDb.STATUS_FINISHED, this);
            }

            Vector<WorkflowActionDb> v = getLinkToActions();
            // 如果本结点有后继节点
            if (v.size() > 0) {
                for (WorkflowActionDb o : v) {
                    WorkflowActionDb nextwa = o;
                    // 判断能否转交至下一节点
                    if (!canDevliveToNextAction(nextwa)) {
                        continue;
                    }

                    nextwa.changeStatus(request, wf, checkUserName,
                            WorkflowActionDb.STATE_DOING, "", "",
                            nextwa.getResultValue(), myActionId);
                    // 通知用户办理
                    String[] users = StrUtil.split(nextwa.getUserName(), ",");
                    int len = (users == null ? 0 : users.length);
                    for (int n = 0; n < len; n++) {
                        MyActionDb mad = wf.notifyUser(users[n],
                                new Date(), myActionId, this,
                                nextwa, STATE_DOING,
                                (long) wf.getId());
                        addTmpUserNameActived(mad);
                    }
                }
            } else if (v.size() == 0) {
                // 无下一结点，如果检查更改流程中的各个节点的状态都为已完成，则置流程状态为已完成
                // 检查流程中的节点是否都已完成
                if (wf.checkStatusFinished()) {
                    wf.changeStatus(request, WorkflowDb.STATUS_FINISHED, this);
                }
            }
        }
        return true;
    }

    /**
     * 取得从本动作指向的所有动作，不包含被打回的动作，按节点上第一个角色的大小排序，从大至小
     * @return Vector
     */
    public Vector<WorkflowActionDb> getLinkToActions() {
        Vector<WorkflowActionDb> ret = new Vector();
        String sql = "select action_to from flow_link where action_from=? and flow_id=? and type<>" + WorkflowLinkDb.TYPE_RETURN;
        Conn conn = new Conn(connname);
        PreparedStatement pstmt;
        ResultSet rs;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, internalName);
            pstmt.setInt(2, flowId);
            rs = conn.executePreQuery();
            if (rs!=null) {
                while (rs.next()) {
                    String to = rs.getString(1);
                    ret.addElement(getWorkflowActionDbByInternalName(to, flowId));
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getLinkToActoins:" + e.getMessage());
        } finally {
            conn.close();
        }
        if (ret.size()>1) {
            Comparator ct = new WorkflowActionComparator();
            Collections.sort(ret, ct);
        }
        return ret;
    }

    public Vector<WorkflowActionDb> getLinkReturnActions() {
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);
        Leaf lf = new Leaf();
        lf = lf.getLeaf(wf.getTypeCode());

        Vector<WorkflowActionDb> ret = new Vector<>();
        if (lf.getType() == Leaf.TYPE_FREE) {
            String sql = "select id from flow_link where action_to=? and flow_id=?";
            LogUtil.getLog(getClass()).info("getLinkReturnActions:" + sql + " " + flowId + " " + internalName);
            Conn conn = new Conn(connname);
            PreparedStatement pstmt;
            ResultSet rs;
            try {
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, internalName);
                pstmt.setInt(2, flowId);
                rs = conn.executePreQuery();
                if (rs != null) {
                    while (rs.next()) {
                        WorkflowLinkDb wld = new WorkflowLinkDb();
                        wld = wld.getWorkflowLinkDb(rs.getInt(1));
                        ret.addElement(getWorkflowActionDbByInternalName(wld.getFrom(), flowId));
                    }
                }
            } catch (SQLException e) {
                LogUtil.getLog(getClass()).error(e.getMessage());
                LogUtil.getLog(getClass()).error(e);
            } finally {
                conn.close();
            }
        } else {
            String sql =
                    "select id from flow_link where ((action_from=? and type=" +
                    WorkflowLinkDb.TYPE_RETURN + ") or (action_to=? and type=" +
                    WorkflowLinkDb.TYPE_BOTH + ")) and flow_id=?";
            Conn conn = new Conn(connname);
            PreparedStatement pstmt;
            ResultSet rs;
            try {
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, internalName);
                pstmt.setString(2, internalName);
                pstmt.setInt(3, flowId);
                rs = conn.executePreQuery();
                if (rs != null) {
                    while (rs.next()) {
                        WorkflowLinkDb wld = new WorkflowLinkDb();
                        wld = wld.getWorkflowLinkDb(rs.getInt(1));
                        if (wld.getType() == WorkflowLinkDb.TYPE_BOTH) {
                            ret.addElement(getWorkflowActionDbByInternalName(
                                    wld.getFrom(),
                                    flowId));
                        } else {
                            ret.addElement(getWorkflowActionDbByInternalName(
                                    wld.getTo(), flowId));
                        }
                    }
                }
            } catch (SQLException e) {
                LogUtil.getLog(getClass()).error(e.getMessage());
                LogUtil.getLog(getClass()).error(e);
            } finally {
                conn.close();
            }
        }
        return ret;
    }

    /**
     * 动作的前继节点是否已经有处理过的，即是否有已处理和被打回的节点，要有两个以上的节点才
     * @return boolean
     */
    public int linkedFromActionsAccessedCount() {
        int count = 0;
        Iterator ir = getLinkFromActions().iterator();
        while (ir.hasNext()) {
            WorkflowActionDb wa = (WorkflowActionDb)ir.next();
            if (wa.getStatus()==STATE_FINISHED || wa.getStatus()==STATE_RETURN) {
                count ++;
            }
        }
        return count;
    }

    /**
     * 取得指向本动作的所有动作，不含打回动作连线的节点
     * @return Vector
     */
    public Vector<WorkflowActionDb> getLinkFromActions() {
        Vector<WorkflowActionDb> ret = new Vector();
        String sql = "select action_from from flow_link where action_to=? and flow_id=? and type<>" + WorkflowLinkDb.TYPE_RETURN;
        Conn conn = new Conn(connname);
        PreparedStatement pstmt;
        ResultSet rs;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, internalName);
            pstmt.setInt(2, flowId);
            rs = conn.executePreQuery();
            while (rs.next()) {
                String from = rs.getString(1);
                ret.addElement(getWorkflowActionDbByInternalName(from, flowId));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return ret;
    }
    
    /**
     * 用于任务的日报回写时根据标题取WorkflowActionDb
     * @Description: 
     * @param title
     * @param flowid
     * @return
     */
    public WorkflowActionDb getWorkflowActionDbByTitle(String title, int flowid) {
        String sql = "select id from flow_action where flow_id=? and title=?";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt;
        ResultSet rs;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flowid);
            pstmt.setString(2, title);
            rs = conn.executePreQuery();
            if (!rs.next()) {
                return null;
            } else {
                int id = rs.getInt(1);
                return getWorkflowActionDb(id);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getWorkflowActionDbByTitle:" + e.getMessage());
        } finally {
            conn.close();
        }
        return null;
    }

    public WorkflowActionDb getWorkflowActionDbByInternalName(String internalName, int flowid) {
        String sql = "select id from flow_action where flow_id=? and internal_name=?";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt;
        ResultSet rs;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flowid);
            pstmt.setString(2, internalName);
            rs = conn.executePreQuery();
            if (!rs.next()) {
                return null;
            } else {
                int id = rs.getInt(1);
                return getWorkflowActionDb(id);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getWorkflowActionDbByInternalName:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return null;
    }

    private int flowId;
    private String userName;
    private String reason;
    private int status;
    private String title;
    public int isStart;
    private int id;
    private String internalName = "-1";

    public static String getTimeUnitDesc(int timeUnit) {
        String r = "";
        switch( timeUnit ) {
        case TIME_UNIT_DAY:
            r = "天";
            break;
        case TIME_UNIT_HOUR:
            r = "小时";
            break;
        case TIME_UNIT_WORKDAY:
            r = "工作日";
            break;
        case TIME_UNIT_WORKHOUR:
            r = "工作小时";
            break;
        default:
            r = "天";
        }
        return r;
    }

    public static String getStatusName(int status) {
        String r;
        WorkflowConfig wfcfg = WorkflowConfig.getInstance();
        switch( status) {
          case STATE_NOTDO: r = wfcfg.getProperty("STATE_NOTDO"); break;
          case STATE_IGNORED: r = wfcfg.getProperty("STATE_IGNORED"); break;
          case STATE_DOING: r = wfcfg.getProperty("STATE_DOING"); break;
          case STATE_RETURN: r = wfcfg.getProperty("STATE_RETURN"); break;
          case STATE_FINISHED: r = wfcfg.getProperty("STATE_FINISHED"); break;
          case STATE_DISCARDED: r = wfcfg.getProperty("STATE_DISCARDED"); break;
          case STATE_TRANSFERED: r = wfcfg.getProperty("STATE_TRANSFERED"); break;
          case STATE_HANDOVER: r = wfcfg.getProperty("STATE_HANDOVER"); break;
          case STATE_PLUS: r = wfcfg.getProperty("STATE_PLUS"); break;
          case STATE_SUSPEND_OVER: r = wfcfg.getProperty("STATE_SUSPEND_OVER");break;
          case STATE_DELAYED: r = wfcfg.getProperty("STATE_DELAYED");break;
          default: r = wfcfg.getProperty("STATE_UNKNOWN");
        }
        return r;
    }

    /**
     * 取得显示CSS样式
     * @param status int
     * @return String
     */
    public static String getStatusClass(int status) {
        String r;
        switch( status) {
          case STATE_NOTDO: r = "STATE_NOTDO"; break;
          case STATE_IGNORED: r = "STATE_IGNORED"; break;
          case STATE_DOING: r = "STATE_DOING"; break;
          case STATE_RETURN: r = "STATE_RETURN"; break;
          case STATE_FINISHED: r = "STATE_FINISHED"; break;
          case STATE_DISCARDED: r = "STATE_DISCARDED"; break;
          case STATE_TRANSFERED: r = "STATE_TRANSFERED"; break;
          case STATE_HANDOVER: r = "STATE_HANDOVER"; break;
          case STATE_PLUS: r = "STATE_PLUS"; break;
          default: r = "STATE_UNKNOWN";
        }
        return r;
    }

    public String getStatusName() {
        return getStatusName(status);
    }

    public Vector listActionsOfFlow(int flowId) {
        Vector v = new Vector();
        String sql = "select id from flow_action where flow_id=? order by mydate asc";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // select id,flow_id,isStart,reason,status,title,userName from flow_action where id=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flowId);
            rs = conn.executePreQuery();
            while (rs.next()) {
                v.addElement(getWorkflowActionDb(rs.getInt(1)));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("listActionsOfFlow:" + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            conn.close();
            conn = null;
        }
        return v;
    }

    /**
     * 取得流程中的开始节点
     * @param flowId int 流程ID
     * @return WorkflowActionDb
     */
    public WorkflowActionDb getStartAction(int flowId) {
        Vector<WorkflowActionDb> v = new Vector<>();
        // 如果有多个未被忽略的，则可能是误做了多个开始节点，比如：用在了串签中，所以在SQL语句中加了根据status倒排，未处理的伪开始节点状态为0，肯定排在后面
        String sql = "select id from flow_action where flow_id=? and isStart=1 and status<>" + STATE_IGNORED + " order by status desc";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt;
        ResultSet rs;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flowId);
            rs = conn.executePreQuery();
            if (rs.next()) {
                return getWorkflowActionDb(rs.getInt(1));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("listCheckedActionOfFlow:" + e.getMessage());
        } finally {
            conn.close();
        }
        return null;
    }

    public Vector listCheckedActionOfFlow(int flowId) {
        Vector v = new Vector();
        // String sql = "select id from flow_action where flow_id=? and status=" + STATE_FINISHED + " and isStart=0 order by checkDate asc";
        String sql = "select id from flow_action where flow_id=? and status=" + STATE_FINISHED + " order by checkDate asc";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // select id,flow_id,isStart,reason,status,title,userName from flow_action where id=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flowId);
            rs = conn.executePreQuery();
            while (rs.next()) {
                v.addElement(getWorkflowActionDb(rs.getInt(1)));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("listCheckedActionOfFlow:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    public String getFieldHide() {
        return fieldHide;
    }

    private int officeColorIndex = 1;
    private String jobCode;
    private String jobName;
    private int direction;
    private String rankCode;
    private boolean relateRoleToOrganization = true;
    private String result;
    private java.util.Date checkDate;
    /**
     * 处理人
     */
    private String checkUserName;
    private String fieldWrite;
    private String fieldHide;
    private int taskId = NOTASK;

    public static final int NOTASK = -1;

    private Vector<MyActionDb> tmpUserNameActived;

    public Vector<MyActionDb> getTmpUserNameActived() {
        return tmpUserNameActived;
    }

    public String getDept() {
        return dept;
    }

    public String getFlag() {
        return flag;
    }

    public int getNodeMode() {
        return nodeMode;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public String getRankName() {
        return rankName;
    }

    public String getStrategy() {
        return strategy;
    }

    public String getItem1() {
        return item1;
    }

    public String getItem2() {
        return item2;
    }

    public String getRelateToAction() {
        return relateToAction;
    }

    public int getIgnoreType() {
        return ignoreType;
    }

    public int getKind() {
        return kind;
    }

    public String getPlus() {
        return plus;
    }

    public java.util.Date getDateDelayed() {
        return dateDelayed;
    }

    public int getTimeDelayedValue() {
        return timeDelayedValue;
    }

    public int getTimeDelayedUnit() {
        return timeDelayedUnit;
    }

    public boolean isDelayed() {
        return delayed;
    }

    public boolean isCanPrivUserModifyDelayDate() {
        return canPrivUserModifyDelayDate;
    }


    /**
     * 往关联用户名集合中增加用户
     * @param mad MyActionDb
     */
    public void addTmpUserNameActived(MyActionDb mad) {
        tmpUserNameActived.addElement(mad);
    }

    /**
     * 初始化动作本次操作（更改状态时）中所关联的用户名集合
     */
    public void initTmpUserNameActived() {
        tmpUserNameActived = null;
        tmpUserNameActived = new Vector();
    }

    /**
     * 限定处理人员所在部门，当节点处理完毕后，如果当前处理人员有兼职，则记录其所选部门
     */
    private String dept;
    private String flag;
    private int nodeMode;
    private boolean loaded = false;
    private String rankName;
    private String strategy;

    /**
     * 是否为结束节点
     */
    private String item1;

    public boolean isEnd() {
        return "1".equals(item1);
    }

    /**
     * 比较用户与下一节点角色的大小，序号越大角色越大
     * @param curActionUser UserDb
     * @param nextActionRole RoleDb
     * @return int 0表示等于 1表示大于 2表示角色相同 -2表示出错 -1表示小于
     */
    public int compareUserRole(UserDb curActionUser, RoleDb nextActionRole) {
        RoleDb[] rds = curActionUser.getRoles();
        int len = 0;
        int r = -2;
        if (rds != null) {
            len = rds.length;
        } else {
            return r;
        }
        r = -1;

        for (int i = 0; i < len; i++) {
            // 如果用户的角色与下一节点角色相同
            if (rds[i].getCode().equals(nextActionRole.getCode())) {
                r = 2;
                return r;
            }
        }

        for (int i = 0; i < len; i++) {
            if (rds[i].getOrders() > nextActionRole.getOrders()) {
                r = 1;
                break;
            } else if (rds[i].getOrders() == nextActionRole.getOrders()) {
                r = 0;
            } else {
                if (r != 0)
                    r = -1;
            }
        }
        return r;
    }

    /**
     * 判断两个节点的角色大小
     * @param nextAction WorkflowActionDb 下一节点
     * @param myAction WorkflowActionDb 跳越节点时的起始节点
     * @return boolean true 表示myAction大于nextAction
     */
    public boolean compareRoleOfAction(WorkflowActionDb nextAction, WorkflowActionDb myAction) {
        // 下一个节点如果为用户型节点则不参与比较大小，本节点此时用户已确定，所以无论后续节点如何，本节点都可以参与角色的比较大小
        if (nextAction.getNodeMode() == WorkflowActionDb.NODE_MODE_USER ||
            nextAction.getNodeMode() == WorkflowActionDb.NODE_MODE_USER_SELECTED) {
            return false;
        }

        // 只处理当前节点只有一个用户，后继节点上只有一个角色的情况
        if (myAction.getJobCode().indexOf(",") == -1) {
            if (nextAction.getJobCode().indexOf(",") == -1) {
                RoleDb nextrd = new RoleDb();
                nextrd = nextrd.getRoleDb(nextAction.getJobCode());

                // LogUtil.getLog(getClass()).info("compareRoleOfAction:" + nextAction.getJobName());

                // 如果是发起节点
                if (myAction.getIsStart() == 1 &&
                    nextrd.getType() == RoleDb.TYPE_NORMAL) {
                    // LogUtil.getLog(getClass()).info("compareRoleOfAction:" + nextAction.getJobName() + " nextrd.getType()=" + nextrd.getType());

                    UserDb user = new UserDb();
                    user = user.getUserDb(myAction.getUserName());
                    // 如果user的角色大于nextrd，或者user拥有角色nextrd，则跳过
                    if (compareUserRole(user, nextrd) == 1) {
                        return true;
                    }
                    // 如果角色是一样的，则不一定能跳过，这样可能会带来问题，如本人发起，则跳过几步后，再到本人手里如果继续跳，则可能带来问题
                    // 如丹投OA，本人发起后，跳过中间一人，再到本人时，存在分支线，而分支线上可能仅是异或发散，而无条件，不能自动跳过本人
                    if (compareUserRole(user, nextrd) == 2) {
                        // 如果后继节点的出度为1，不存在分支线则才允许比较大小，以免跳过后，因分支线上可能仅为异或，产生问题
                        if (nextAction.getLinkToActions().size()==1) {
                            return true;
                        }
                    }
                } else {
                    RoleDb currd = nextrd.getRoleDb(myAction.getJobCode());
                    // 本节点及下一节点是普通节点才能跳过
                    if (currd.getType() == RoleDb.TYPE_NORMAL &&
                        nextrd.getType() == RoleDb.TYPE_NORMAL) {
                        if (currd.getOrders() > nextrd.getOrders()) {
                            return true;
                        }
                    }
                }
            }
        }

        LogUtil.getLog(getClass()).info("compareRoleOfAction:" + nextAction.getJobName() + " false");

        return false;
    }

    /**
     * 创建子流程
     * @param wf WorkflowDb
     * @param nextwa WorkflowActionDb
     */
    public void createSubFlow(HttpServletRequest request, WorkflowDb wf, WorkflowActionDb nextwa, WorkflowActionDb curwa, long curMyActionId) throws ErrMsgException {
        if (nextwa.getUserName().indexOf(",")!=-1) {
            throw new ErrMsgException("子流程节点只能选一个发起用户！");
        }

        WorkflowMgr wm = new WorkflowMgr();

        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());

        LogUtil.getLog(getClass()).info("createSubFlow:nextwa.getInternalName()=" + nextwa.getInternalName());
        LogUtil.getLog(getClass()).info("createSubFlow:props=" + wpd.getProps());

        JSONObject json = getProps(wpd, nextwa.getInternalName());
        if (json==null) {
            throw new ErrMsgException("节点属性格式错误！");
        }

        String subFlowTypeCode = null;
        try {
            subFlowTypeCode = json.getString("subFlowTypeCode");
        } catch (JSONException ex) {
            LogUtil.getLog(getClass()).error(ex);
            throw new ErrMsgException(ex.getMessage());
        }

        Leaf lf = new Leaf();
        lf = lf.getLeaf(subFlowTypeCode);

        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        // String subFlowTitle = WorkflowMgr.makeTitle(request, pvg, lf, false);
        String subFlowTitle = WorkflowMgr.makeTitle(request, pvg, lf, true);
        
        // 初始化子流程
        long subMyActionId = wm.initWorkflow(nextwa.getUserName(), subFlowTypeCode, subFlowTitle, -1, WorkflowDb.LEVEL_NORMAL, nextwa.getId());

        // 发送待办通知
        wf.notifyUser(nextwa.getUserName(), new java.util.Date(), curMyActionId, curwa, nextwa, STATE_DOING, (long) wf.getId(), subMyActionId);
        
        MyActionDb submad = new MyActionDb();
        submad = submad.getMyActionDb(subMyActionId);
        WorkflowDb subwf = wf.getWorkflowDb((int) submad.getFlowId());
        // 将子流程置为已开始状态，否则待办流程中不会出现
        subwf.setBeginDate(new java.util.Date());
        subwf.setStatus(WorkflowDb.STATUS_STARTED);
        subwf.save();

        // 映射表单域
        doParentToSubMap(wf, subwf, json);

        // 发送短消息、短信、邮件
        boolean isUseMsg = true;
        boolean isToMobile = SMSFactory.isUseSms;
        Config cfg = new Config();
        boolean flowNotifyByEmail = cfg.getBooleanProperty("flowNotifyByEmail");
        cn.js.fan.mail.SendMail sendmail = new cn.js.fan.mail.SendMail();
        String senderName = StrUtil.GBToUnicode(Global.AppName);
        senderName += "<" + Global.getEmail() + ">";
        if (flowNotifyByEmail) {
            String mailserver = Global.getSmtpServer();
            int smtp_port = Global.getSmtpPort();
            String name = Global.getSmtpUser();
            String pwd_raw = Global.getSmtpPwd();
            try {
                sendmail.initSession(mailserver, smtp_port, name,
                                     pwd_raw);
            } catch (Exception ex) {
                LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
            }
        }
        com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
        String t = SkinUtil.LoadString(request,
                                       "res.module.flow",
                                       "msg_user_actived_title");
        String c = SkinUtil.LoadString(request,
                                       "res.module.flow",
                                       "msg_user_actived_content");
        String tail = WorkflowMgr.getFormAbstractTable(subwf);

        MessageDb md = new MessageDb();
        UserMgr um = new UserMgr();
        t = t.replaceFirst("\\$flowTitle", subwf.getTitle());
        String fc = c.replaceFirst("\\$flowTitle", subwf.getTitle());
        fc = fc.replaceFirst("\\$fromUser", curwa.getUserRealName());
        UserDb user = um.getUserDb(submad.getUserName());
        if (isToMobile) {
            IMsgUtil imu = SMSFactory.getMsgUtil();
            if (imu != null) {
                imu.send(user, fc, MessageDb.SENDER_SYSTEM);
            }
        }
        fc += tail;
        if (isUseMsg) {
            // 发送信息
            String action = "action=" + MessageDb.ACTION_FLOW_DISPOSE + "|myActionId=" + submad.getId();
            md.sendSysMsg(submad.getUserName(), t, fc, action);
        }
        if (flowNotifyByEmail) {
            if (!user.getEmail().equals("")) {
                String action = "userName=" + user.getName() + "|" +
                                "myActionId=" + submad.getId();
                action = cn.js.fan.security.ThreeDesUtil.encrypt2hex(
                        ssoCfg.getKey(), action);
                fc += "<BR />>>&nbsp;<a href='" +
                        WorkflowUtil.getJumpUrl(WorkflowUtil.OP_FLOW_PROCESS, action) +
                        "' target='_blank'>请点击此处办理</a>";
                sendmail.initMsg(user.getEmail(),
                                 senderName,
                                 t, fc, true);
                sendmail.send();
                sendmail.clear();
            }
        }

        // 更新下一节点处理状态
        nextwa.setStatus(STATE_DOING);
        nextwa.save();
    }

    /**
     * 映射父流程的表单域至子流程
     * @param parentWf WorkflowDb
     * @param subWf WorkflowDb
     * @param props JSONObject
     */
    public boolean doParentToSubMap(WorkflowDb parentWf, WorkflowDb subWf, JSONObject props) throws
            ErrMsgException {
        JSONArray ary = null;
        try {
            ary = props.getJSONArray("parentToSubMap");
        } catch (JSONException ex1) {
            ex1.printStackTrace();
            throw new ErrMsgException(ex1.getMessage());
        }

        FormDAO pfdao = new FormDAO();

        Leaf lf = new Leaf();
        lf = lf.getLeaf(parentWf.getTypeCode());
        String formCode = lf.getFormCode();
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        pfdao = pfdao.getFormDAO(parentWf.getId(), fd);

        MacroCtlMgr mm = new MacroCtlMgr();
        FormDb subfd = new FormDb();
        Leaf sublf = lf.getLeaf(subWf.getTypeCode());
        subfd = subfd.getFormDb(sublf.getFormCode());
        FormDAO sfdao = new FormDAO();
        sfdao = sfdao.getFormDAO(subWf.getId(), subfd);

        for (int i=0; i<ary.length(); i++) {
            JSONObject j = null;
            try {
                j = ary.getJSONObject(i);
                String pfield = (String) j.get("parentField");
                String jfield = (String) j.get("subField");
                
                // 判断是否为嵌套表格2
                FormField pff = pfdao.getFormField(pfield);
                FormField sff = sfdao.getFormField(jfield);
                boolean isNestField = false;
                if (pff.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(pff.getMacroType());
                    if (mu!=null) {
                        if (mu.getNestType() != MacroCtlUnit.NEST_TYPE_NONE) {
                        	isNestField = true;
                        	
                    		// 为了向下兼容
                    		String nestFormCodeFrom = pff.getDescription(); // 父流程嵌套表的表单编码
                    		try {
                    			// 20131123 fgf 添加
                    			String defaultVal = StrUtil.decodeJSON(pff.getDescription());
                    			JSONObject json = new JSONObject(defaultVal);
                    			nestFormCodeFrom = json.getString("destForm");
                    		} catch (JSONException e) {
                    			LogUtil.getLog(getClass()).info(nestFormCodeFrom + " is old version before 20131123. ff.getDefaultValueRaw()=" + pff.getDefaultValueRaw());
                    		}
                    		String nestFormCodeTo = sff.getDescription(); // 父流程嵌套表的表单编码
                    		try {
                    			// 20131123 fgf 添加
                    			String defaultVal = StrUtil.decodeJSON(sff.getDescription());
                    			JSONObject json = new JSONObject(defaultVal);
                    			nestFormCodeTo = json.getString("destForm");
                    		} catch (JSONException e) {
                    			LogUtil.getLog(getClass()).info(nestFormCodeTo + " is old version before 20131123. ff.getDefaultValueRaw()=" + pff.getDefaultValueRaw());
                    		}                    		
                    		
                    		FormDb nestFdTo = new FormDb();
                    		nestFdTo = nestFdTo.getFormDb(nestFormCodeTo);
                    		com.redmoon.oa.visual.FormDAO fdaoNestTo = new com.redmoon.oa.visual.FormDAO(nestFdTo);
                    		
                    		String sql = "select id from ft_" + nestFormCodeFrom + " where cws_id='" + pfdao.getId() + "'";
                    		com.redmoon.oa.visual.FormDAO fdaoFrom = new com.redmoon.oa.visual.FormDAO();
                    		Iterator ir = fdaoFrom.list(nestFormCodeFrom, sql).iterator();
                    		while (ir.hasNext()) {
                    			fdaoFrom = (com.redmoon.oa.visual.FormDAO)ir.next();
                    			
                    			// 全部字段
                    			Iterator irTo = nestFdTo.getFields().iterator();
                    			while (irTo.hasNext()) {
                    				FormField ffTo = (FormField)irTo.next();   
                    				// 从源表单中找到目的表中相同名称的字段
                    				FormField ffFrom = fdaoFrom.getFormField(ffTo.getName());
                    				if (ffFrom!=null) {
                    					fdaoNestTo.setFieldValue(ffTo.getName(), ffFrom.getValue());
                    				}                    				
                    			}
                    			
                    			fdaoNestTo.setFlowId(subWf.getId());	
                    			fdaoNestTo.setCwsId(String.valueOf(sfdao.getId()));
                    			fdaoNestTo.setCreator(fdaoFrom.getCreator());
                    			fdaoNestTo.setUnitCode(sfdao.getUnitCode());
                    			fdaoNestTo.setCwsQuoteId((int)fdaoFrom.getId());
                    			fdaoNestTo.setCwsParentForm(sfdao.getFormDb().getCode());
                    			fdaoNestTo.create();
                    		}                        	
                        }
                    }
                }
                if (!isNestField) {
                    sfdao.setFieldValue(jfield, pfdao.getFieldValue(pfield));
                }
            } catch (JSONException | SQLException ex) {
                LogUtil.getLog(getClass()).error(ex);
            }
        }
        return sfdao.save();
    }

    public boolean doSubToParentMap(WorkflowDb parentWf, WorkflowDb subWf, JSONObject props) throws
            ErrMsgException {
        JSONArray ary = null;
        try {
            ary = props.getJSONArray("subToParentMap");
        } catch (JSONException ex1) {
            ex1.printStackTrace();
            throw new ErrMsgException(ex1.getMessage());
        }

        FormDAO pfdao = new FormDAO();

        Leaf lf = new Leaf();
        lf = lf.getLeaf(parentWf.getTypeCode());
        String formCode = lf.getFormCode();
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        pfdao = pfdao.getFormDAO(parentWf.getId(), fd);

        MacroCtlMgr mm = new MacroCtlMgr();

        FormDb subfd = new FormDb();
        Leaf sublf = lf.getLeaf(subWf.getTypeCode());
        subfd = subfd.getFormDb(sublf.getFormCode());
        FormDAO sfdao = new FormDAO();
        sfdao = sfdao.getFormDAO(subWf.getId(), subfd);

        for (int i=0; i<ary.length(); i++) {
            JSONObject j = null;
            try {
                j = ary.getJSONObject(i);
                String pfield = (String) j.get("parentField");
                String sfield = (String) j.get("subField");
                
                // 判断是否为嵌套表格2
                FormField pff = pfdao.getFormField(pfield);
                FormField sff = sfdao.getFormField(sfield);
                boolean isNestField = false;
                if (pff.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(pff.getMacroType());
                    if (mu!=null) {
                        if (mu.getNestType() != MacroCtlUnit.NEST_TYPE_NONE) {
                        	isNestField = true;
                        	
                    		// 为了向下兼容
                    		String nestFormCodeTo = pff.getDescription(); // 父流程嵌套表的表单编码
                    		try {
                    			// 20131123 fgf 添加
                    			String defaultVal = StrUtil.decodeJSON(pff.getDescription());
                    			JSONObject json = new JSONObject(defaultVal);
                    			nestFormCodeTo = json.getString("destForm");
                    		} catch (JSONException e) {
                    			LogUtil.getLog(getClass()).info(nestFormCodeTo + " is old version before 20131123. ff.getDefaultValueRaw()=" + pff.getDefaultValueRaw());
                    		}
                    		String nestFormCodeFrom = sff.getDescription(); // 父流程嵌套表的表单编码
                    		try {
                    			// 20131123 fgf 添加
                    			String defaultVal = StrUtil.decodeJSON(sff.getDescription());
                    			JSONObject json = new JSONObject(defaultVal);
                    			nestFormCodeFrom = json.getString("destForm");
                    		} catch (JSONException e) {
                    			LogUtil.getLog(getClass()).info(nestFormCodeFrom + " is old version before 20131123. ff.getDefaultValueRaw()=" + pff.getDefaultValueRaw());
                    		}                    		
                    		
                    		FormDb nestFdTo = new FormDb();
                    		nestFdTo = nestFdTo.getFormDb(nestFormCodeTo);
                    		com.redmoon.oa.visual.FormDAO fdaoNestTo = new com.redmoon.oa.visual.FormDAO(nestFdTo);
                    		
                    		String sql = "select id from ft_" + nestFormCodeFrom + " where cws_id='" + sfdao.getId() + "'";
                    		com.redmoon.oa.visual.FormDAO fdaoFrom = new com.redmoon.oa.visual.FormDAO();
                    		Iterator ir = fdaoFrom.list(nestFormCodeFrom, sql).iterator();
                    		while (ir.hasNext()) {
                    			fdaoFrom = (com.redmoon.oa.visual.FormDAO)ir.next();
                    			
                    			// 全部字段
                    			Iterator irTo = nestFdTo.getFields().iterator();
                    			while (irTo.hasNext()) {
                    				FormField ffTo = (FormField)irTo.next();
                    				FormField ffFrom = fdaoFrom.getFormField(ffTo.getName());
                    				if (ffFrom!=null) {
                    					fdaoNestTo.setFieldValue(ffTo.getName(), ffFrom.getValue());
                    				}
                    			}
                    			
                    			fdaoNestTo.setFlowId(parentWf.getId());	
                    			fdaoNestTo.setCwsId(String.valueOf(pfdao.getId()));
                    			fdaoNestTo.setCreator(fdaoFrom.getCreator());
                    			fdaoNestTo.setUnitCode(pfdao.getUnitCode());
                    			fdaoNestTo.setCwsQuoteId((int)fdaoFrom.getId());
                    			fdaoNestTo.setCwsParentForm(pfdao.getFormDb().getCode());
                    			fdaoNestTo.create();
                    		}                        	
                        }
                    }
                }
                if (!isNestField) {
                    pfdao.setFieldValue(pfield, sfdao.getFieldValue(sfield));
                }            
                
            } catch (JSONException | SQLException ex) {
                LogUtil.getLog(getClass()).error(ex);
            }
        }
        return pfdao.save();
    }

    /**
     * 取得预定义的节点附加属性(子流程映射关系)
     * @param wpd WorkflowPredefineDb
     * @param internalName String
     * @return JSONObject
     */
    public JSONObject getProps(WorkflowPredefineDb wpd, String internalName) {
        // WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        // wpd = wpd.getDefaultPredefineFlow(flowTypeCode);

        try {
            SAXBuilder parser = new SAXBuilder();

            org.jdom.Document doc = parser.build(new InputSource(new StringReader(wpd.getProps())));

            Element root = doc.getRootElement();
            Iterator ir = root.getChildren().iterator();
            while (ir.hasNext()) {
                Element e = (Element) ir.next();
                LogUtil.getLog(getClass()).info("getProps: internalName=" + e.getAttribute("internalName").getValue());
                if (e.getAttribute("internalName").getValue().equals(internalName)) {
                    String prop = e.getChildText("property");
                    JSONObject json = new JSONObject(prop);
                    return json;
                }
            }
        } catch (IOException | JDOMException | JSONException ex) {
            LogUtil.getLog(getClass()).error(ex);
        }
        return null;
    }
    
    /**
     * 取得节点上的属性，节点上的属性有property(子流程)、redirect(节点处理完后重定向的JSP页面)、脚本选人及是否启用模块过滤
     * @param wpd
     * @param internalName
     * @param property
     * @return
     */
    public static String getActionProperty(WorkflowPredefineDb wpd, String internalName, String property) {
        // WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        // wpd = wpd.getDefaultPredefineFlow(flowTypeCode);

        try {
            SAXBuilder parser = new SAXBuilder();

            org.jdom.Document doc = parser.build(new InputSource(new StringReader(wpd.getProps())));

            Element root = doc.getRootElement();
            Iterator ir = root.getChildren().iterator();
            while (ir.hasNext()) {
                Element e = (Element) ir.next();
                // LogUtil.getLog("WorkflowActionDb").info("getActionProperty: internalName=" + e.getAttribute("internalName").getValue());
                if (e.getAttribute("internalName").getValue().equals(internalName)) {
                    return e.getChildText(property);
                }
            }
        } catch (IOException ex) {
            LogUtil.getLog(WorkflowActionDb.class).error(ex);
        } catch (JDOMException ex) {
            // LogUtil.getLog(getClass()).error(ex);
            DebugUtil.i(WorkflowActionDb.class, "getActionProperty", ex.getMessage());
        }
        return null;
    }    

    /**
     * 取得流程中各节点的绩效，并按从大至小排序
     * @param flowTypeCode String
     * @param year int
     * @param month int
     * @return isCompare boolean
     */
    public static Vector getActionsPerformance(String flowTypeCode, int year, int month, boolean isCompare) {
        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(flowTypeCode);

        if (wpd==null) {
            return new Vector();
        }

        MyActionDb mad = new MyActionDb();

        java.util.Date d1 = DateUtil.getDate(year, month, 1);
        java.util.Date d2 = DateUtil.addMonthDate(d1, 1);
        Vector v = new Vector();
        WorkflowDb wf = new WorkflowDb();
        try {
            v = wf.getActionsFromString(wpd.getFlowString());
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                WorkflowActionDb wa = (WorkflowActionDb)ir.next();
                wa.setAveragePerformance(mad.getActionAvgPerformance(wa.getInternalName(), d1, d2));
            }

            if (isCompare) {
                Comparator ct = new WorkflowActionComparator();
                Collections.sort(v, ct);
            }
        } catch (ErrMsgException ex) {
            LogUtil.getLog(WorkflowActionDb.class).error(ex);
        }
        return v;
    }

    public boolean ignoreActions(WorkflowDb wf, List<WorkflowActionDb> actionsToIngore) {
        if (actionsToIngore.size() == 0) {
            return false;
        }
        String sql = "update flow_action set status=" + STATE_IGNORED + " where id=";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            WorkflowActionCacheMgr wacm = new WorkflowActionCacheMgr();
            for (WorkflowActionDb wad : actionsToIngore) {
                jt.addBatch(sql + wad.getId());
                wacm.refreshSave(wad.getId());
                wf.renewWorkflowString(wad, false);
            }
            jt.executeBatch();
            wf.setRenewed(true);
            wf.save();
            LogUtil.getLog(getClass()).info("ignoreActions: renewWorkflowString flowId=" + wf.getId());
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        return true;
    }

    public double getAveragePerformance() {
        return averagePerformance;
    }

    public void setAveragePerformance(double averagePerformance) {
        this.averagePerformance = averagePerformance;
    }

    public void setCanDistribute(boolean canDistribute) {
		this.canDistribute = canDistribute;
	}

	public boolean isCanDistribute() {
		return canDistribute;
	}

	public void setFormView(int formView) {
		this.formView = formView;
	}

	public int getFormView() {
		return formView;
	}

	/**
     * 格式为 { relateToAction , ignoreType , kind , fieldHide , isDelayed , timeDelayedValue , timeDelayedUnit , canPrivUserModifyDelayDate, formView, relateDeptManager };
     */
    private String item2;
    private String relateToAction = RELATE_TO_ACTION_DEFAULT;
    private int ignoreType = IGNORE_TYPE_DEFAULT;
    private String plus;
    private int kind = KIND_ACCESS;

    /**
     * 是否延迟
     */
    private boolean delayed = false;

    /**
     * 通过timeDelayed、timeUnit计算所得的时间
     */
    private java.util.Date dateDelayed;
    /**
     * 延迟时间值
     */
    private int timeDelayedValue;
    /**
     * 前一用户能否修改延迟时间
     */
    private boolean canPrivUserModifyDelayDate = false;
    /**
     * 延迟时间单位
     */
    private int timeDelayedUnit;

    /**
     * 延迟时间单位-天
     */
    public static final int TIME_UNIT_DAY = 0;
    /**
     * 延迟时间单位-小时
     */
    public static final int TIME_UNIT_HOUR = 1;
    /**
     * 延迟时间单位-工作日
     */
    public static final int TIME_UNIT_WORKDAY = 2;
    /**
     * 延迟时间单位-工作小时
     */
    public static final int TIME_UNIT_WORKHOUR = 3;

    /**
     * 节点上的平均绩效，仅用于排序
     */
    private double averagePerformance = 0.0;
    
    /**
     * 能否分发
     */
    private boolean canDistribute = false;

    /**
     * 默认视图
     */
    public static final int VIEW_DEFAULT = 0;

    /**
     * 视图
     */
    private int formView = VIEW_DEFAULT;

    public boolean isMsg() {
        return msg;
    }

    public void setMsg(boolean msg) {
        this.msg = msg;
    }

    /**
     * 流程提交时，是否发送消息提醒
     */
    private boolean msg = true;

    public static String getINSERT() {
        return INSERT;
    }
}
