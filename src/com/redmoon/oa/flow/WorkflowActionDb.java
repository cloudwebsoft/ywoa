package com.redmoon.oa.flow;

import bsh.EvalError;
import bsh.Interpreter;
import cn.js.fan.db.Conn;
import cn.js.fan.util.*;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import com.cloudwebsoft.framework.aop.Pointcut.MethodNamePointcut;
import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.aop.base.Advisor;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.Config;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.macroctl.*;
import com.redmoon.oa.flow.strategy.IStrategy;
import com.redmoon.oa.flow.strategy.StrategyMgr;
import com.redmoon.oa.flow.strategy.StrategyUnit;
import com.redmoon.oa.message.IMessage;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.message.MobileAfterAdvice;
import com.redmoon.oa.oacalendar.OACalendarDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.person.UserSetupDb;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.pvg.RoleMgr;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.util.BeanShellUtil;
import org.apache.log4j.Logger;
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
    final String INSERT = "insert into flow_action (id,flow_id,isStart,reason,status,title,userName,internal_name,office_color_index,userRealName,jobCode,jobName,proxyJobCode,proxyJobName,proxyUserName,proxyUserRealName,resultValue,fieldWrite,taskId,dept,flag,nodeMode,mydate,strategy,item1,item2,is_msg) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," + RESULT_VALUE_NOT_ACCESSED + ",?,?,?,?,?,?,?,?,?,?)";
    final String LOAD = "select id,flow_id,isStart,reason,status,title,userName,internal_name,office_color_index,userRealName,jobCode,jobName,proxyJobCode,proxyJobName,proxyUserName,proxyUserRealName,result,checkDate,resultValue,checkUserName,fieldWrite,taskId,dept,flag,nodeMode,strategy,item1,item2,plus,date_delayed,can_distribute,is_msg from flow_action where id=?";
    final String SAVE = "update flow_action set flow_id=?,isStart=?,reason=?,status=?,title=?,userName=?,office_color_index=?,userRealName=?,jobCode=?,jobName=?,proxyJobCode=?,proxyJobName=?,proxyUserName=?,proxyUserRealName=?,result=?,checkDate=?,resultValue=?,checkUserName=?,fieldWrite=?,taskId=?,dept=?,flag=?,nodeMode=?,strategy=?,item1=?,item2=?,plus=?,date_delayed=?,can_distribute=?,is_msg=? where id=?";
    private String userRealName = "-1";
    final String DELETE = "delete from flow_action where id=?";

    public static final int STATE_NOTDO = 0;
    public static final int STATE_IGNORED = 1;
    public static final int STATE_DOING = 2;
    public static final int STATE_RETURN = 3;
    public static final int STATE_FINISHED = 4;
    public static final int STATE_DISCARDED = -1;

    /**
     * 指派
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
    
    public static final int RESULT_VALUE_AGGREE = 1;
    public static final int RESULT_VALUE_DISAGGREE = -1;
    public static final int RESULT_VALUE_CONTINUE = 2; // 继续
    public static final int RESULT_VALUE_NOT_ACCESSED = -2; // 未处理
    public static final int RESULT_VALUE_RETURN = 3; // 返回
    public static final int RESULT_VALUE_TO_RETUNER = 4; // 直送
    public static final int RESULT_VALUE_READED = 5; // 审阅

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

    public static final int NODE_MODE_ROLE_SELECTED = 2; // 表示角色已被选择，人员被确定
    public static final int NODE_MODE_USER_SELECTED = 3; // 表示用户已被选择，人员被确定

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
        if (connname.equals(""))
            Logger.getLogger(getClass()).info("Directory:默认数据库名为空！");
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
        if (!beforeChangeStatusFree(request, wf, checkUserName, newstatus, reason, myActionId))
            return false;
        // Logger.getLogger(getClass()).info("changeStatus userName=" + getUserName() + " roleName=" + getJobName() + " status desc=" + getStatusName());

        MyActionDb mad = new MyActionDb();
        mad = mad.getMyActionDb(myActionId);
        // 置待处理动作通知中的处理时间及将其设置为已处理
        mad.setCheckDate(new java.util.Date());
        mad.setChecked(true);
        mad.setChecker(checkUserName);
        mad.setResultValue(WorkflowActionDb.RESULT_VALUE_AGGREE);

        Config cfg = new Config();
        if (cfg.get("flowExpireRelateOACalendar").equals("true")) {
            String flowExpireUnit = cfg.get("flowExpireUnit");
            OACalendarDb oad = new OACalendarDb();
            if (flowExpireUnit.equals("day")) {
                double d = oad.getWorkDayCountFromDb(mad.getReceiveDate(), new java.util.Date());
                double d2 = oad.getWorkDayCount(mad.getReceiveDate(), mad.getExpireDate());
                double performance = 0;

                if (d2!=0) {
                    String formula = StrUtil.getNullStr(cfg.get("flowPerformanceFormula"));
                    if (!formula.equals("")) {
                        formula = formula.replaceAll("a", NumberUtil.round(d, 2));
                        formula = formula.replaceAll("b", NumberUtil.round(d2, 2));
                        FormulaCalculator fc = new FormulaCalculator(formula);
                        performance = fc.getResult();
                    }
                    else
                        performance = d / d2;
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
                    if (!formula.equals("")) {
                        formula = formula.replaceAll("a", NumberUtil.round(d, 2));
                        formula = formula.replaceAll("b", NumberUtil.round(d2, 2));
                        FormulaCalculator fc = new FormulaCalculator(formula);
                        performance = fc.getResult();
                    }
                    else
                        performance = d / d2;
                }

                mad.setPerformance(performance);
            }
        }

        mad.save();

        mad.onChecked();

        if (newstatus == STATE_FINISHED) {
            // 加签处理，注意加签处理中如果是并签，其它加签人员没有处理完毕，则方法返回true，需return停止往下继续运行
            if (doPlus(request, wf, mad))
                return true;
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

        // Logger.getLogger(getClass()).info("changeStatus: userRealName=" + getUserRealName() + " jobName=" + getJobName() + " statusName=" + getStatusName());
        boolean re = save();
        if (re) {
            // 如果本action为start即开始节点，并且被打回，则置流程状态为未开始
            if (newstatus==STATE_RETURN && getIsStart()==1) {
                wf.setStatus(wf.STATUS_NOT_STARTED);
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
     * 当节点状态改变时更新myActoinDb
     * @param mad MyActionDb
     * @return boolean
     */
    public boolean changeMyActionDb(MyActionDb mad, String checkUserName) throws ErrMsgException {
        // 置待处理动作通知中的处理时间及将其设置为已处理
        mad.setCheckDate(new java.util.Date());
        mad.setChecked(true);
        mad.setChecker(checkUserName);

        Config cfg = new Config();
        if (cfg.get("flowExpireRelateOACalendar").equals("true")) {
            String flowExpireUnit = cfg.get("flowExpireUnit");
            OACalendarDb oad = new OACalendarDb();
            if (flowExpireUnit.equals("day")) {
                double d = oad.getWorkDayCountFromDb(mad.getReceiveDate(), new java.util.Date());
                double d2 = oad.getWorkDayCount(mad.getReceiveDate(), mad.getExpireDate());
                double performance = 0;
                if (d2!=0) {
                    String formula = StrUtil.getNullStr(cfg.get("flowPerformanceFormula"));
                    if (!formula.equals("")) {
                        formula = formula.replaceAll("a", NumberUtil.round(d, 2));
                        formula = formula.replaceAll("b", NumberUtil.round(d2, 2));
                        FormulaCalculator fc = new FormulaCalculator(formula);
                        performance = fc.getResult();
                    }
                    else
                        performance = d / d2;
                }
                mad.setPerformance(performance);
            }
            else {
                double d = oad.getWorkHourCount(mad.getReceiveDate(), new java.util.Date());

                double d2 =oad.getWorkHourCount(mad.getReceiveDate(), mad.getExpireDate());

                double performance = 0;
                if (d2!=0) {
                    String formula = StrUtil.getNullStr(cfg.get("flowPerformanceFormula"));
                    if (!formula.equals("")) {
                        formula = formula.replaceAll("a", NumberUtil.round(d, 2));
                        formula = formula.replaceAll("b", NumberUtil.round(d2, 2));
                        FormulaCalculator fc = new FormulaCalculator(formula);
                        performance = fc.getResult();
                    }
                    else
                        performance = d / d2;
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
            if (!plus.equals("")) {
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
                	if (ex1.getMessage().indexOf("internal") == -1) {
                		throw new ErrMsgException(ex1.getMessage());
                	}
                }
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
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
                    Iterator ir = v.iterator();
                    while (ir.hasNext()) {
                        MyActionDb mad2 = (MyActionDb) ir.next();
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
                    Vector v = mad.getPlusMyActionDbs(mad.getActionId());
                    LogUtil.getLog(getClass()).info("v.size()=" + v.size());
                    Iterator ir = v.iterator();
                    boolean isPlusFinished = true;
                    while (ir.hasNext()) {
                        MyActionDb mad2 = (MyActionDb) ir.next();
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
                Iterator ir = v.iterator();
                while (ir.hasNext()) {
                    MyActionDb mad2 = (MyActionDb) ir.next();
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
                    Iterator ir = v.iterator();
                    while (ir.hasNext()) {
                        MyActionDb mad2 = (MyActionDb) ir.next();
                        if (!mad2.getUserName().equals(mad.getUserName())) {
                            mad2.del();
                        }
                    }
                } else {
                    // 全部加签人员尚未处理完毕，则退出
                    Vector v = mad.getPlusMyActionDbs(mad.getActionId());
                    Iterator ir = v.iterator();
                    while (ir.hasNext()) {
                        MyActionDb mad2 = (MyActionDb) ir.next();
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
                    for (int i = 0; i < plusUsers.length; i++) {
                        MyActionDb newmad = wf.notifyUser(plusUsers[i], new java.util.Date(), mad.getId(), privAction, this,
                                                          WorkflowActionDb.STATE_PLUS, getFlowId());
                        wf.sendNotifyMsgAndEmail(request, newmad, sendmail);
                    }
                }
                return true;
            } else if (plusType == WorkflowActionDb.PLUS_TYPE_CONCURRENT) { // 并签
                // 如果其它加签人员没有处理完毕，则退出
                Vector v = mad.getPlusMyActionDbs(mad.getActionId());
                Iterator ir = v.iterator();
                while (ir.hasNext()) {
                    MyActionDb mad2 = (MyActionDb) ir.next();
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
	        if (!beforeChangeStatus(request, wf, checkUserName, newstatus, reason, myActionId))
	            return false;
	        // Logger.getLogger(getClass()).info("changeStatus userName=" + getUserName() + " roleName=" + getJobName() + " status desc=" + getStatusName());
        }
	        
        int oldStatus = status;

        // 节点不是将被激活时，更新myActionDb，如果将被激活，则是因为在afterChangeStatus中对下一节点调用了changeStatus
        if (newstatus!=WorkflowActionDb.STATE_DOING) {
            changeMyActionDb(mad, checkUserName);
        }

        if (newstatus == STATE_FINISHED) {
            // 加签处理，注意加签处理中如果是并签，其它加签人员没有处理完毕，则方法返回true，需return停止往下继续运行
            if (doPlus(request, wf, mad))
                return true;
        }

        // 根据节点策略，忽略相应的人员
        if (newstatus == STATE_FINISHED || newstatus == STATE_RETURN || newstatus==STATE_DISCARDED || newstatus==STATE_TRANSFERED) {
            WorkflowActionDb wa = new WorkflowActionDb();
            wa = wa.getWorkflowActionDb((int) mad.getActionId());
            String strategy = wa.getStrategy();
            Logger.getLogger(getClass()).info("afterChangeStatus:strategy=" + strategy);
            if (!strategy.equals("")) {
                StrategyMgr sm = new StrategyMgr();
                StrategyUnit su = sm.getStrategyUnit(strategy);
                Logger.getLogger(getClass()).info("afterChangeStatus:su=" + su);
                if (su != null) {
                    IStrategy ist = su.getIStrategy();
                    ist.onActionFinished(request, wf, mad);
                }
            }
        }

        Logger.getLogger(getClass()).info("changeStatus: newstatus=" + newstatus + " status=" + status);

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
            Logger.getLogger(getClass()).info("changeStatus: userName=" + userName);
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
                else
                    newstatus = status; // 恢复为以前的状态
                // Logger.getLogger(getClass()).info("changeStatus: isAllUserOfActionChecked=" + canFinish);
            }
            // Logger.getLogger(getClass()).info("changeStatus: canFinish=" + canFinish);
            if (canFinish) {
                setStatus(newstatus);
            }
        }
        // 如果是指派后返回给了原指派者，则两者是在同一节点上，返回后节点状态应为STATE_DOING
        else if (newstatus==STATE_NOTDO && status==STATE_RETURN) {
            if (returnIds==null || returnIds.length==0)
                throw new ErrMsgException("请选择将要返回的用户！");
            
            // 手机端发过来的，有时好象会带有换行符，所以要trim
            int returnId = Integer.parseInt(returnIds[0].trim());

            Logger.getLogger(getClass()).info("changeStatus: returnId=" + returnId + " id=" + id);

            if (returnId == id) {
            	// 不需要再赋值，因为已经是STATE_RETURN
            	// newstatus = STATE_RETURN;
            	// setStatus(newstatus);
            }
        }
        else
            setStatus(newstatus);

        Logger.getLogger(getClass()).info("changeStatus: userRealName=" + getUserRealName() + " jobName=" + getJobName() + " statusName=" + getStatusName());
        
        boolean re = save();

        Logger.getLogger(getClass()).info("changeStatus: re=" + re);

        if (re) {
            // 如果本action为start即开始节点，并且被打回，则置流程状态为未开始
            if (newstatus==STATE_RETURN && getIsStart()==1) {
                // 需重新获得wf，因为flowstring在上面的save()中被改变了，而wf不更新，继续往下作为afterChangeStatus参数传递，就会出现问题
                wf = wf.getWorkflowDb(wf.getId());
                wf.setStatus(WorkflowDb.STATUS_NOT_STARTED);
                wf.save();
            }

            // 清流程的缓存
            WorkflowCacheMgr wcm = new WorkflowCacheMgr();
            wcm.refreshList();

            Logger.getLogger(getClass()).info("changeStatus: newstatus=" + newstatus);

            // 流程状态改变后
            // 必须先更新flowstring
            wf = wf.getWorkflowDb(wf.getId());
            afterChangeStatus(request, wf, checkUserName, oldStatus, newstatus, myActionId);
        }
        return re;
    }

    public void setReturnIds(String[] ids) {
        this.returnIds = ids;
    }

    /**
     * 将用户检查是否存在后加入Vector，避免重复加入
     * @param vt Vector
     * @param user UserDb
     */
    public void addUserDbToVector(Vector vt, UserDb user) {
        Iterator ir = vt.iterator();
        while (ir.hasNext()) {
            UserDb ud = (UserDb)ir.next();
            if (ud.getName().equals(user.getName()))
                return;
        }
        vt.addElement(user);
    }

    /**
     * 判断分支线上的条件是否满足
     * @param cond String
     * @param token String
     * @return int[] int[0]表示是否满足条件 int[1]表示是否为默认条件
     */
    public int[] judgeCondition(String cond, String token, long myActionId) {
        // System.out.println(getClass() + " token=" + token);
        int[] ary = {0,0};
        String[] pairs = cond.split(token);
        String leftField = pairs[0].trim();
        String rightField = "";
        // pairs长度为1，表示条件右侧的值为空，如　用车人=
        if (pairs.length>1)
            rightField = pairs[1].trim();
        // 表达式的右侧如果为空表示默认条件，例：报销类型=
        if (rightField.equals("")) {
            ary[1] = 1;
            return ary;
        }

        // 取得流程表单中该域的值
        if (flowId == -1) {
        	MyActionDb mad = new MyActionDb(myActionId);
        	flowId = (int) mad.getFlowId();
        }
        WorkflowDb wfd = new WorkflowDb();
        wfd = wfd.getWorkflowDb(flowId);
        Leaf lf = new Leaf();
        lf = lf.getLeaf(wfd.getTypeCode());
        String formCode = lf.getFormCode();
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        FormDAO fdao = new FormDAO(flowId, fd);
        fdao.load();

        String value = "";
        Iterator ir = fdao.getFields().iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField)ir.next();
            if (ff.getName().equals(leftField)) {
                value = StrUtil.getNullStr(ff.getValue());
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                	if (ff.getMacroType().equals("macro_opinion") && myActionId != 0) {
                		OpinionCtl oc = new OpinionCtl();
                		value = oc.getOpinionProp(ff.getValue(), myActionId, "content");
                	} else if (ff.getMacroType().equals("macro_opinionex") && myActionId != 0) {
                		OpinionExCtl oec = new OpinionExCtl();
                		value = oec.getOpinionProp(ff.getValue(), myActionId, "content");
                	}
                }
                break;
            }
        }

        Logger.getLogger(getClass()).info("token=" + token);
        // Logger.getLogger(getClass()).info("leftField=" + leftField);
        // Logger.getLogger(getClass()).info("leftFieldvalue=" + value);

        double a=0, b=0;
        boolean isADouble = false;
        boolean isBDouble = false;

        try {
            a = Double.parseDouble(value);
            isADouble = true;
        }
        catch (Exception e) {
            Logger.getLogger(getClass()).info("judgeCondition1:" + e.getMessage());
            if (token.equals("=") || token.equals("<>"))
                ;
            else
                return ary;
        }
        try {
            b = Double.parseDouble(rightField);
            isBDouble = true;
        }
        catch (Exception e) {
            Logger.getLogger(getClass()).info("judgeCondition2:" + e.getMessage());
            if (token.equals("=") || token.equals("<>"))
                ;
            else
                return ary;
        }

        if (token.equals(">=")) {
            ary[0] = a>=b?1:0;
            return ary;
        }
        else if (token.equals(">")) {
            ary[0] = a>b?1:0;
            return ary;
        }
        else if (token.equals("=")) {
            Logger.getLogger(getClass()).info("rightField=" + rightField + " value=" + value + " isADouble=" + isADouble + " isBDouble=" + isBDouble);

            if (isADouble && isBDouble) {
                ary[0] = a==b?1:0;
                return ary;
            }
            else {
                String[] ary2 = StrUtil.split(rightField, ",");
                if (ary2!=null) {
                    int len = ary2.length;
                    for (int i = 0; i < len; i++) {
                        if (value.equals(ary2[i])) {
                            ary[0]=1;
                            Logger.getLogger(getClass()).info("i=" + i + " value=" + value + " ary[0]=" + ary[0]);

                            return ary;
                        }
                    }
                }
                return ary;
            }
        }
        else if (token.equals("<")) {
            ary[0] = a<b?1:0;
            return ary;
        }
        else if (token.equals("<=")) {
            ary[0] = a<=b?1:0;
            return ary;
        }
        else if (token.equals("<>")) {
            // System.out.println(getClass() + " rightField=" + rightField + " value=" + value);
            if (isADouble && isBDouble) {
                ary[0] = a!=b?1:0;
                return ary;
            }
            else {
                String[] ary2 = StrUtil.split(rightField, ",");
                if (ary2!=null) {
                    int len = ary2.length;
                    for (int i = 0; i < len; i++) {
                        if (value.equals(ary2[i])) {
                            return ary;
                        }
                    }
                }
                ary[0] = 1;
                return ary;
            }
        }
        else
            return ary;
    }

    /**
     * 如果在后继节点的连接线上存在条件，则判别是否有符合条件的分支，如果有满足条件的，则自动运行，注意条件分支中应只有一个分支满足条件
     * 注意当存在不含条件的分支时，此分支是默认分支，异或发散时，而未设条件时就存在这种情况
     * 2012-7-11使支持多个分支同时满足条件 fgf
     * @return WorkflowLinkDb
     */
    public Vector matchNextBranch(HttpServletRequest request, StringBuffer condBuf) throws ErrMsgException {
        com.redmoon.oa.pvg.Privilege privilege = new  com.redmoon.oa.pvg.Privilege();

        String userName = privilege.getUser(request);
        long myActionId = ParamUtil.getLong(request, "myActionId");
        return matchNextBranch(this, userName, condBuf, myActionId);
    }

    public Vector matchNextBranch(WorkflowActionDb wad, String userName, StringBuffer condBuf, long myActionId) throws ErrMsgException {
        Vector v = new Vector();
        WorkflowLinkDb wld = new WorkflowLinkDb();
        Iterator ir = wld.getToWorkflowLinks(wad).iterator();
        // 当分支上定义的标题为空时，该分支为默认分支，如果没有其它条件满足条件，则返回此默认分支
        WorkflowLinkDb defaultWld = null;
        int blankCondBranchCount = 0; // 条件为空的分支数
        while (ir.hasNext()) {
            wld = (WorkflowLinkDb)ir.next();
            // @task:是否该改为condType为-1
            String cond = wld.getTitle().trim(); // title为条件表达式，condDesc才是分支线上的标注
            LogUtil.getLog(getClass()).info("matchNextBranch:cond=" + cond);
            // 判别条件
            boolean isValid = false;
            // 如果条件类型为“无条件”或条件内容为空，则该分支线为默认条件
            if ((cond.equals("") || wld.getCondType().equals(WorkflowLinkDb.COND_TYPE_NONE)) && 
            		!wld.getCondType().equals(WorkflowLinkDb.COND_TYPE_COMB_COND)
            		 && !wld.getCondType().equals(WorkflowLinkDb.COND_TYPE_MUST)) {
                defaultWld = wld;

                LogUtil.getLog(getClass()).info("matchNextBranch:title=" + wld.getTitle() + " " + wld.getCondDesc() + " " + wld.getCondType());

                blankCondBranchCount ++;
                // 如果title为空的分支线有两条以上，则表示没有默认条件，如果全部不满足条件，需手动选择分支
                if (blankCondBranchCount>=2)
                    defaultWld = null;
                continue;
            }
            else {
                if (wld.getCondType().equals(WorkflowLinkDb.COND_TYPE_NONE))
                    continue;
                condBuf.append(wld.getCondDesc() + "  ");
                if (wld.getCondType().equals(WorkflowLinkDb.COND_TYPE_MUST)) {
                    isValid = true;
                }
                else if (wld.getCondType().equals(WorkflowLinkDb.COND_TYPE_FORM)) {
                    boolean isDefaultCondition = false;
                    if (cond.length()>1) { // 长度如果为1，有可能cond字符串只有=号字符
                        if (cond.indexOf(">=") != -1) {
                            int[] retAry = judgeCondition(cond, ">=", myActionId);
                            isValid = retAry[0] == 1;
                            isDefaultCondition = retAry[1] == 1;
                        } else if (cond.indexOf("<=") != -1) {
                            int[] retAry = judgeCondition(cond, "<=", myActionId);
                            isValid = retAry[0] == 1;
                            isDefaultCondition = retAry[1] == 1;
                        } else if (cond.indexOf("<>") != -1) {
                            int[] retAry = judgeCondition(cond, "<>", myActionId);
                            isValid = retAry[0] == 1;
                            isDefaultCondition = retAry[1] == 1;
                        } else if (cond.indexOf(">") != -1) {
                            int[] retAry = judgeCondition(cond, ">", myActionId);
                            isValid = retAry[0] == 1;
                            isDefaultCondition = retAry[1] == 1;
                        } else if (cond.indexOf("<") != -1) {
                            int[] retAry = judgeCondition(cond, "<", myActionId);
                            isValid = retAry[0] == 1;
                            isDefaultCondition = retAry[1] == 1;
                        } else if (cond.indexOf("=") != -1) {
                            int[] retAry = judgeCondition(cond, "=", myActionId);
                            isValid = retAry[0] == 1;
                            isDefaultCondition = retAry[1] == 1;

                            LogUtil.getLog(getClass()).info("retAry[0]=" + retAry[0] + " condBuf=" + condBuf);

                        } else {
                            isValid = false;
                        }
                    }
                    // 如果条件的判断符号后的值为空，则表示默认条件，即当其它条件都不满足时，使用此条件
                    // System.out.println(getClass() + " matchNextBranch:isDefaultCondition=" + isDefaultCondition);

                    if (isDefaultCondition) {
                        defaultWld = wld;
                    }
                }
                else if (wld.getCondType().equals(WorkflowLinkDb.COND_TYPE_DEPT)) {
                    String[] depts = StrUtil.split(cond, ",");
                    int deptlen = depts.length;
                    // 检查用户所在的部门
                    String[] userAry = StrUtil.split(userName, ",");
                    if (userAry!=null) {
                        int len = userAry.length;
                        DeptUserDb dud = new DeptUserDb();
                        for (int k=0; k<len; k++) {
                            Iterator dir = dud.getDeptsOfUser(userAry[k]).iterator();
                            // 遍历用户所属部门
                            while (dir.hasNext()) {
                                DeptDb dd = (DeptDb)dir.next();
                                // 遍历条件中的部门
                                for (int m=0; m<deptlen; m++) {
                                    if (depts[m].equals(dd.getCode())) {
                                        isValid = true;
                                        break;
                                    }
                                }
                                if (isValid)
                                    break;
                            }
                        }
                    }
                }
                else if (wld.getCondType().equals(WorkflowLinkDb.COND_TYPE_ROLE)) {
                    String[] roles = StrUtil.split(cond, ",");
                    int roleLen = roles.length;
                    // 检查用户所在的部门
                    String[] userAry = StrUtil.split(userName, ",");
                    if (userAry!=null) {
                        int len = userAry.length;
                        UserMgr um = new UserMgr();
                        for (int k=0; k<len; k++) {
                            UserDb user = um.getUserDb(userAry[k]);
                            // 遍历条件中的角色
                            for (int m = 0; m < roleLen; m++) {
                                if (user.isUserOfRole(roles[m])) {
                                    isValid = true;
                                    break;
                                }
                            }
                            if (isValid)
                                break;
                        }
                    }
                }
                else if (wld.getCondType().equals(WorkflowLinkDb.COND_TYPE_SCRIPT)) {
                    // 通过脚本判别下一分支节点
                    int flowId = getFlowId();
                    WorkflowDb wfd = new WorkflowDb();
                    wfd = wfd.getWorkflowDb(flowId);
                    Leaf lf = new Leaf();
                    lf = lf.getLeaf(wfd.getTypeCode());
                    FormDb fd = new FormDb();
                    fd = fd.getFormDb(lf.getFormCode());

                    FormDAO fdao = new FormDAO();
                    fdao = fdao.getFormDAO(flowId, fd);

                    BranchMatcher bm = new BranchMatcher(fd, fdao, userName);
                    isValid = bm.doMatch(cond);
                }
                else if (wld.getCondType().equals(WorkflowLinkDb.COND_TYPE_COMB_COND)) {
                    int flowId = getFlowId();
                    WorkflowDb wfd = new WorkflowDb();
                    wfd = wfd.getWorkflowDb(flowId);
                    
                	WorkflowPredefineDb wpd = new WorkflowPredefineDb();                    
                    wpd = wpd.getDefaultPredefineFlow(wfd.getTypeCode());
                    
                    Leaf lf = new Leaf();
                    lf = lf.getLeaf(wfd.getTypeCode());
                    FormDb fd = new FormDb();
                    fd = fd.getFormDb(lf.getFormCode());

                    FormDAO fdao = new FormDAO();
                    fdao = fdao.getFormDAO(flowId, fd);     
                    
                    List filedList = new ArrayList();
                   Iterator ir1 = fd.getFields().iterator();
                   while(ir1.hasNext()){
                	   FormField ff =  (FormField)ir1.next();
                	   filedList.add(ff.getName());
                   }

            		/*
            		 * LeafPriv lp = new LeafPriv(wld.getTypeCode()); if
            		 * (!lp.canUserSee(privilege.getUser(request))) { throw new
            		 * ErrMsgException("权限非法！"); }
            		 */

            		// System.out.println(getClass() + " roleRankMode=" + roleRankMode);

            		String wld_linkProp = wpd.getLinkProp();
					if (!wld_linkProp.equals("")) {
						SAXBuilder parser = new SAXBuilder();
						org.jdom.Document doc;
						try {
							doc = parser.build(new InputSource(new StringReader(wld_linkProp)));
							
							StringBuffer sb = new StringBuffer();
							
							Element root = doc.getRootElement();
							List<Element> vroot = root.getChildren();
							boolean formFlag = true;
							int i = 0;							
							if (vroot != null) {
								String lastLogical = "";
								for (Element e : vroot) {
									String from = e.getChildText("from");
									String to = e.getChildText("to");
									if (from.equals(wld.getFrom())
											&& to.equals(wld.getTo())) {
										String name = e.getChildText("name");
										String fieldName = e.getChildText("fieldName");
										String op = e.getChildText("operator");
										String logical = e.getChildText("logical");
										String value = e.getChildText("value");
										String firstBracket = e.getChildText("firstBracket");
										String twoBracket = e.getChildText("twoBracket");
										
										formFlag  = filedList.contains(fieldName);
										if(!formFlag){
											break;
										}
										
										if(null == firstBracket || firstBracket.equals("")){
											firstBracket = "";
										}
										if(null == twoBracket || twoBracket.equals("")){
											twoBracket = "";
										}
										if (name.equals(WorkflowPredefineDb.COMB_COND_TYPE_FIELD)) {
											FormField ff = fd.getFormField(fieldName);
											if (ff.getFieldType()==FormField.FIELD_TYPE_TEXT || 
													ff.getFieldType()==FormField.FIELD_TYPE_VARCHAR ||
													ff.getFieldType() == FormField.FIELD_TYPE_BOOLEAN) {
												if (op.equals("=")) {
													sb.append(firstBracket);
													sb.append("{$" + fieldName + "}");
													sb.append(".equals(\"" + value + "\")");
													sb.append(twoBracket);
												}
												else {
													sb.append(firstBracket);
													sb.append("!{$" + fieldName + "}");
													sb.append(".equals(\"" + value + "\")");	
													sb.append(twoBracket);
												}
											}
											else {
												sb.append(firstBracket);
												sb.append("{$" + fieldName + "}");	
												if(op.equals("=")){
													op = "==";
												}
												if(op.equals("<>")){
													
													op = "!=";
												}
												
												sb.append(op);																							
												sb.append(value);
												sb.append(twoBracket);
											}
										}
										else if (name.equals(WorkflowPredefineDb.COMB_COND_TYPE_PRIV_DEPT)) {
                                            sb.append(firstBracket);

                                            DeptUserDb dud = new DeptUserDb();
                                            Vector vDept = dud.getDeptsOfUser(userName);
                                            // 如果有多个部门
                                            if (vDept.size() > 1) {
                                                sb.append("(");
                                            }
                                            for (int k = 0; k < vDept.size(); k++) {
                                                DeptDb dd = (DeptDb) vDept.elementAt(k);

                                                sb.append("\"" + dd.getCode() + "\"");
                                                if (op.equals("=")) {
                                                    op = "==";
                                                } else {
                                                    op = "!=";
                                                }
                                                sb.append(op);
                                                sb.append("\"" + value + "\"");

                                                if (vDept.size()>1 && k != vDept.size()-1) {
                                                    if (op.equals("==")) {
                                                        sb.append(" || ");
                                                    }
                                                    else {
                                                        sb.append(" and ");
                                                    }
                                                }
                                            }
                                            if (vDept.size() > 1) {
                                                sb.append(")");
                                            }

                                            sb.append(twoBracket);
										}
										else if (name.equals(WorkflowPredefineDb.COMB_COND_TYPE_PRIV_ROLE)) {
											 sb.append(firstBracket);

                                            UserDb user = new UserDb();
                                            user = user.getUserDb(userName);
                                            RoleDb[] ary = user.getRoles();

                                            // 如果有多个角色
                                            if (ary.length > 1) {
                                                // 去掉member
                                                RoleDb[] tmp = new RoleDb[ary.length-1];
                                                int m = 0;
                                                for (int k=0; k<ary.length; k++) {
                                                    if (!ary[k].getCode().equals(RoleDb.CODE_MEMBER)) {
                                                        tmp[m] = ary[k];
                                                        m++;
                                                    }
                                                }
                                                ary = tmp;
                                                sb.append("(");
                                            }
                                            for (int k = 0; k < ary.length; k++) {
                                                RoleDb rd = ary[k];

                                                if (op.equals("=")) {
                                                    sb.append("\"" + rd.getCode() + "\"");
                                                    op = "==";
                                                    sb.append(op);
                                                    sb.append("\"" + value + "\"");
                                                } else if (op.equals("<>")) {
                                                    sb.append("\"" + rd.getCode() + "\"");
                                                    op = "!=";
                                                    sb.append(op);
                                                    sb.append("\"" + value + "\"");
                                                } else {
                                                    // 比较角色的大小
                                                    int a = rd.getOrders();
                                                    RoleDb rdRight = new RoleDb();
                                                    rdRight = rdRight.getRoleDb(value);
                                                    if (rdRight.isLoaded()) {
                                                        int b = rdRight.getOrders();
                                                        sb.append(a + " " + op + " " + b);
                                                    }
                                                    else {
                                                        throw new ErrMsgException("角色" + value + " 不存在，无法比较大小");
                                                    }
                                                }

                                                if (ary.length > 1 && k != ary.length-1) {
                                                    if (op.equals("==")) {
                                                        sb.append(" || ");
                                                    }
                                                    else {
                                                        sb.append(" && ");
                                                    }
                                                }
                                            }
                                            if (ary.length > 1) {
                                                sb.append(")");
                                            }

                                            sb.append(twoBracket);
										}
										
										// 去除最后一个逻辑判断
										// if ( i!=vroot.size()-1 ) {
											if(logical.equals("or")){
												logical = "||";
											}else{
												logical = "&&";
											}
										
											sb.append(" " + logical + " ");
											lastLogical = logical;
										// }
									}
																		
									i++;
								}
								String tempCond = sb.toString();
								//校验括弧对称性
								//boolean flag = checkComCond(tempCond);

								// 如果配置了条件
								if (!tempCond.equals("")) {
									String script = sb.toString();
									int p = script.lastIndexOf(" " + lastLogical + " ");
									
									LogUtil.getLog(getClass()).info("script=" + script);
									if (p!=-1) {
										script = script.substring(0, p);
									}
									LogUtil.getLog(getClass()).info("script2=" + script);
									
									//if (formFlag){
					                    BranchMatcher bm = new BranchMatcher(fd, fdao, userName);
					                    isValid = bm.doMatch(script);
									//}else{
										//isValid = false;
									//}
								}
								else {
									isValid = true;
								}
								
								if(!formFlag){
									isValid = false;
								}
								
							}							
						} catch (JDOMException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
                }
                
                if (isValid) {
                    LogUtil.getLog(getClass()).info("wld=" + wld.getTitle() + " condBuf=" + condBuf + " "); //  + StrUtil.trace(new Exception()));

                    v.addElement(wld);
                }
            }

        }
        if (defaultWld!=null) {
            // 如果默认条件不为空，且没有满足条件的分支，则走默认条件所在分支
            if (v.size()==0)
                v.addElement(defaultWld);
        }
        if (v.size()>1) {
            Comparator ct = new WorkflowLinkComparator();
            Collections.sort(v, ct);
        }
        return v;
    }

    /**
     * 在同级部门中寻找，已被matchActionUserParallel替代，因为aryrole参数会使得重复查找
     * @param dd DeptDb 当前寻找所处的节点，可能为当前节点，也可能为当前节点的父节点或其它
     * @param nextAction WorkflowActionDb
     * @param vt Vector 返回结果附加于vt上
     * @param aryrole String[] nextAction上定义的角色
     * @return boolean
     */
    public boolean matchWithBrotherNodeXXX(DeptDb dd, WorkflowActionDb nextAction,
                                        Vector vt, String[] aryrole) {
        DeptUserDb du = new DeptUserDb();
        int aryrolelen = 0;
        if (aryrole != null)
            aryrolelen = aryrole.length;
        if (aryrolelen == 0)
            return false;
        // 遍历动作上指定的角色
        RoleMgr rm = new RoleMgr();
        for (int k = 0; k < aryrolelen; k++) {
            RoleDb rd = rm.getRoleDb(aryrole[k]);
            boolean isFoundInBrotherDept = false;
            UserMgr um = new UserMgr();
            // 在同级的其它部门中寻找
            DeptDb parentDept = dd.getDeptDb(dd.
                                             getParentCode());
            if (parentDept != null) {
                Iterator brotherir = parentDept.getChildren().
                                     iterator();
                while (brotherir.hasNext()) {
                    DeptDb brotherDept = (DeptDb) brotherir.
                                         next();
                    // 跳过本部门
                    if (brotherDept.getCode().equals(dd.
                                                     getCode()))
                        continue;
                    Iterator duir = du.list(brotherDept.
                                            getCode()).
                                    iterator();
                    while (duir.hasNext()) {
                        DeptUserDb dud = (DeptUserDb) duir.
                                         next();
                        // 取得用户的所有角色
                        UserDb ud = um.getUserDb(dud.
                                                 getUserName());
                        RoleDb[] roles = ud.getRoles();
                        int rlen = roles.length;
                        for (int i = 0; i < rlen; i++) {
                            // 用户的某个角色与预置的角色一致，职级也一致
                            if (roles[i].getCode().equals(
                                    rd.
                                    getCode())) {
                                boolean found = true;
                                // 如果预设的职级不为空，则检查是否一致
                                if (!nextAction.getRankCode().
                                    equals("")) {
                                    if (ud.getRankCode().
                                        equals(
                                                nextAction.
                                                getRankCode()))
                                        found = true;
                                    else
                                        found = false;
                                }
                                if (found) {
                                    // 如果预设的部门不为空，则检查是否一致
                                    String dept = StrUtil.getNullStr(
                                            nextAction.
                                            getDept()).trim();
                                    if (!dept.equals("")) {
                                        found = false;
                                        String[] arydept =
                                                StrUtil.
                                                split(dept,
                                                      ",");
                                        int arydeptlen = 0;
                                        if (arydept != null)
                                            arydeptlen = arydept.length;
                                        for (int m = 0;
                                                     m <
                                                     arydeptlen;
                                                     m++) {
                                            // 用户属于预置的部门
                                            if (du.
                                                isUserOfDept(
                                                        ud.
                                                        getName(),
                                                        arydept[
                                                        m]))
                                                found = true;
                                        }
                                    }
                                }
                                if (found) {
                                    vt.addElement(ud);
                                    isFoundInBrotherDept = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (isFoundInBrotherDept)
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * 匹配节点中的人员，非测试模式
     * @param nextAction WorkflowActionDb
     * @param curAction WorkflowActionDb
     * @return Vector
     */
    public Vector matchActionUser(WorkflowActionDb nextAction, WorkflowActionDb curAction) throws ErrMsgException, MatchUserException {
        return matchActionUser(nextAction, curAction, false, null);
    }

    public Vector matchActionUser(WorkflowActionDb nextAction, WorkflowActionDb curAction, boolean isTest) throws ErrMsgException, MatchUserException {
        return matchActionUser(nextAction, curAction, isTest, null);
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
                    if (fieldAry == null)
                        continue;

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
                        if (ary != null)
                            len = ary.length;
                        if (len >= 1) {
                            for (int i = 0; i < len; i++) {
                                UserDb user = um.getUserDb(ary[i]);
                                if (user.isLoaded()) {
                                    StrUtil.concat(sb, ",", user.getRealName());
                                }
                                else
                                    throw new ErrMsgException(wa.getJobName() + "=" + ary[i] + " 不存在！");
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
     * 为向下兼容，原matchActionUser无request参数，fgf 20170310
     * @Description: 
     * @param nextAction
     * @param curAction
     * @param isTest
     * @param deptOfUserWithMultiDept
     * @return
     * @throws MatchUserException
     */
    public Vector matchActionUser(WorkflowActionDb nextAction, WorkflowActionDb curAction, boolean isTest, String deptOfUserWithMultiDept) throws ErrMsgException, MatchUserException {
    	return matchActionUser(null, nextAction, curAction, isTest, deptOfUserWithMultiDept);
    }    

    /**
     * 匹配节点中的人员，如果只有一人，则自动匹配，当curAction被处理时，调用本方法，注意要用nextAction.matchActionUser来调用
     * @param request HttpServletRequest
     * @param nextAction WorkflowActionDb
     * @param curAction WorkflowActionDb
     * @param isTest 是否为测试模式，用于设计器在设计时，显示节点上的用户
     * @param deptOfUserWithMultiDept 用户在flow_dispose.jsp中选择的部门
     * @return Vector
     */
    public Vector matchActionUser(HttpServletRequest request, WorkflowActionDb nextAction, WorkflowActionDb curAction, boolean isTest, String deptOfUserWithMultiDept) throws ErrMsgException, MatchUserException {
        Vector vt = new Vector();
        UserMgr um = new UserMgr();
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        String curUserName = "";
        if (request!=null) {
        	curUserName = pvg.getUser(request);
        }
        else {
        	// 如果request为null，则是来自WorkflowAutoDeliverJob线程中autoPassActionNoUserMatched的调用
        	// 在WorkflowAutoDeliverJob是已将被跳过的人员的值赋予给了curAction的userName
        	curUserName = curAction.getUserName();
        }
        
        // 取当前用户的MyActionDb，并记录所选择的兼职部门
        if (!"".equals(deptOfUserWithMultiDept) && !isTest) {
	        MyActionDb curMyActionDb = new MyActionDb();
	        curMyActionDb = curMyActionDb.getMyActionDbOfActionDoingByUser(curAction, curUserName);
	        if (curMyActionDb!=null) {
                curMyActionDb.setPartDept(deptOfUserWithMultiDept);
                curMyActionDb.save();
            }
        }

        // 如果是自选节点
        if (nextAction.getJobCode().equals(PRE_TYPE_USER_SELECT) || nextAction.getJobCode().equals(PRE_TYPE_USER_SELECT_IN_ADMIN_DEPT)) { 
        		// nextAction.getJobCode().equals(PRE_TYPE_MYLEADER) || nextAction.getJobCode().equals(PRE_TYPE_MYSUBORDINATE)) {
            /*
            String[] ary = StrUtil.split(nextAction.getUserName(), ",");
            int len = 0;
            if (ary!=null)
                len = ary.length;
            for (int i=0; i<len; i++) {
                vt.addElement(um.getUserDb(ary[i]));
            }
            */
            return vt;
        }

        Logger.getLogger(getClass()).info("matchActionUser: deptOfUserWithMultiDept=" + deptOfUserWithMultiDept + " isTest=" + isTest + " nextAction.isRelateRoleToOrganization()=" + nextAction.isRelateRoleToOrganization());

        WorkflowActionDb actionRelated = curAction; // 所关联的节点，上一节点或开始节点
        Vector vu = new Vector();
        DeptUserDb du = new DeptUserDb();
        // 关联发起人节点，相当于以发起人为上一节点，然后按行文方向匹配
        WorkflowActionDb startAction = getStartAction(nextAction.getFlowId());
        if (nextAction.isRelateRoleToOrganization()) {
	        if (nextAction.getRelateToAction().equals(RELATE_TO_ACTION_STARTER)) {
	            // 如果当前节点为发起节点
	            if (startAction.getId()==curAction.getId()) {
	                vu = du.getDeptsOfUser(curAction.getUserName());
	            }
	            else {
	            	actionRelated = startAction;
	            	
	                Logger.getLogger(getClass()).info("startAction.getDept()=" + startAction.getDept() + " startAction.getUserName()=" + startAction.getUserName());
	
                    // 如果开始节点上未保存单位（发起者无兼职）
	                // Logger.getLogger(getClass()).info("matchActionUser dept.name=" + ((DeptDb)du.getDeptsOfUser(startAction.getUserName()).elementAt(0)).getName());
	                MyActionDb madStarterActionDb = new MyActionDb();
                    madStarterActionDb = madStarterActionDb.getFirstMyActionDbOfFlow(curAction.getFlowId());
	                // 取得madStarterActionDb中所选的兼职部门
                    String partDept = madStarterActionDb.getPartDept();
	                if (!"".equals(partDept)) {
	                	DeptDb dd = new DeptDb();
	                	dd = dd.getDeptDb(partDept);
	                	vu.addElement(dd);
	                }
	                else {
	                	// 使3.0及以下版向下兼容
		                vu = du.getDeptsOfUser(startAction.getUserName());	                	
	                }
	            }
	        }
	        else if (nextAction.getRelateToAction().equals(RELATE_TO_ACTION_DEPT)) {
	        	WorkflowDb wf = new WorkflowDb();
	        	wf = wf.getWorkflowDb(curAction.getFlowId());
	        	Leaf lf = new Leaf();
	        	lf = lf.getLeaf(wf.getTypeCode());
	        	String formCode = lf.getFormCode();
	        	FormDb fd = new FormDb();
	        	fd = fd.getFormDb(formCode);
	        	FormDAO fdao = new FormDAO();
	        	fdao = fdao.getFormDAO(curAction.getFlowId(), fd);
		        MacroCtlMgr mm = new MacroCtlMgr();

	        	Iterator ir = fdao.getFields().iterator();
	        	while (ir.hasNext()) {
	        		FormField ff = (FormField)ir.next();
	        		if (ff.getType().equals(FormField.TYPE_MACRO)) {
		                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
		                if (mu.getIFormMacroCtl() instanceof DeptSelectCtl || mu.getIFormMacroCtl() instanceof DeptSelectWinCtl || mu.getIFormMacroCtl() instanceof MyDeptSelectCtl) {
		                    DeptDb dd = new DeptDb();
		                    dd = dd.getDeptDb(ff.getValue());
		                    if (dd!=null && dd.isLoaded()) {
		                    	vu.addElement(dd);
		                    }
		                	break;
		                }
	        		}
	        	}
	        }
	        else {
	        	// 这里如果curAction存在多用户，则vu的size为0
	            vu = du.getDeptsOfUser(curAction.getUserName());
	        }
        }
        else {
        	// 如果下一节点上指定的为部门管理員，則也要取用戶所在的部門
        	vu = du.getDeptsOfUser(curAction.getUserName());
        }
        
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(curAction.getFlowId());
        
        boolean isNeedCheckCurUserMultiDept = true;
        // 判断如果是否处于多个部门，有没有选择部门
        if (!isTest) {
            // 注释掉下行，是因为只要存在有兼职的情况，就需要去进一步判断是否需选择兼职所在部门
            // if (nextAction.isRelateRoleToOrganization()) {
                // 如果当前节点不是开始节点，且下一节点关联的是开始节点，则不需要判断是否选择了兼职部门
                if (curAction.getId()!=startAction.getId() && nextAction.getRelateToAction().equals(RELATE_TO_ACTION_STARTER)) {
                	isNeedCheckCurUserMultiDept = false;
                    Logger.getLogger(getClass()).info("matchActionUser vu.size=" + vu.size() + " deptOfUserWithMultiDept=" + deptOfUserWithMultiDept);
                }
                else {
                    Logger.getLogger(getClass()).info("matchActionUser vu.size()=" + vu.size() + " deptOfUserWithMultiDept2=" + deptOfUserWithMultiDept);
                    // 判断角色关联fgf 20140115，注意：当从子流程中返回父流程时注意不能设为角色关联
                    if (nextAction.isRelateRoleToOrganization()) {
	                    if (vu.size() > 1) {
	                        // 当处在flow_dispose.jsp页面时，deptOfUserWithMultiDept开始为null
	                        // if (deptOfUserWithMultiDept == null)
	                        // deptOfUserWithMultiDept应该不再可能为null，2012-08-28 fgf
	                    	// 如果未选择所兼职的部门，则抛出异常
	                        if (deptOfUserWithMultiDept == null || deptOfUserWithMultiDept.equals(""))
	                            throw new MatchUserException(); // 返回异常，以使得flow_dispose.jsp在try catch后显示多个部门供选择
	                    }
                    }
                }
            // }

            // 比较角色大小
            WorkflowPredefineDb wpd = new WorkflowPredefineDb();
            wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());

            // 检查跳越模式，小于当前角色级别的节点需跳过，只比较节点上设置了单一角色的情况，如果设置了多个角色则无法跳过，遇到发散节点也不允许跳过
            if (wpd.getRoleRankMode() == WorkflowPredefineDb.ROLE_RANK_NEXT_LOWER_JUMP) {
            	// 如果角色比较大小时允许跳过
            	if (nextAction.getIgnoreType()!=IGNORE_TYPE_ROLE_COMPARE_NOT) {
	                Vector toV = nextAction.getLinkToActions();
	                LogUtil.getLog(getClass()).info("matchActionUser: getRoleRankMode=ROLE_RANK_NEXT_LOWER_JUMP" + " toV.size()=" + toV.size());
	                if (toV.size() == 1) {
	                    LogUtil.getLog(getClass()).info("matchActionUser: compareRoleOfAction");
	                    if (compareRoleOfAction(nextAction, curAction)) {
	                        LogUtil.getLog(getClass()).info("matchActionUser: compareRoleOfAction = true");
	                        return vt; // 此时vt为空
	                    }
	                }
            	}
            }
        }

        if (nextAction.getNodeMode() == NODE_MODE_ROLE_SELECTED ||
        		nextAction.getNodeMode() == NODE_MODE_USER_SELECTED) {
            int nextActionFromCount = 0;
            Vector fromV = nextAction.getLinkFromActions();
            // 如果是汇聚节点
            if (fromV.size() > 1) {
                // 统计入度
                Iterator fromIr = fromV.iterator();
                while (fromIr.hasNext()) {
                    WorkflowActionDb fromAction = (WorkflowActionDb) fromIr.
                                                  next();
                    if (fromAction.getStatus() !=
                        WorkflowActionDb.STATE_IGNORED)
                        nextActionFromCount++;
                }

                // 如果节点的入度（不含来自被忽略的节点）大于1，而节点已经设置或者匹配好了人员，则不允许再次匹配用户，但如果是表单中选择的人员，则允许再次匹配用户
                if (nextActionFromCount > 1 &&
                    !nextAction.getUserName().equals("") && !nextAction.getJobCode().startsWith(PRE_TYPE_FIELD_USER)) {
                    String[] users = StrUtil.split(nextAction.getUserName(), ",");
                    int len = users.length;
                    for (int i=0; i<len; i++) { 
                        vt.addElement(um.getUserDb(users[i]));
                    }
                    return vt;
                }
            }
            // 如果curAction是被打回的，则也不允许多次选择用户
            if (curAction.getStatus() == WorkflowActionDb.STATE_RETURN) {
                if (!nextAction.getUserName().equals("")) {
                    String[] users = StrUtil.split(nextAction.getUserName(), ",");
                    int len = users.length;
                    for (int i=0; i<len; i++) {
                        vt.addElement(um.getUserDb(users[i]));
                    }
                    return vt;
                }
            }
        }

        Logger.getLogger(getClass()).info("matchActionUser username=" + nextAction.getUserName() +
                    " nodemode=" + nextAction.getNodeMode());

        // 用于匹配下一节点跟当前节点角色与部门相关联的情况中，当前节点用户数大于1时，当前处理用户应能够看见的关联用户，如：当前用户应只能看见自己的处长
        Vector myVt = new Vector();
        
        if (nextAction.getNodeMode() == NODE_MODE_ROLE ||
            nextAction.getNodeMode() == NODE_MODE_ROLE_SELECTED) {
            // 如果为role型，检查role中是否只有一个用户，如果是则自动填充action中的用户，如果不是，则根据角色与组织机构相关联或者繁忙程度，自动分配
            String roleCodes = nextAction.getJobCode();
            String[] ary = StrUtil.split(roleCodes, ",");
            int aryrolelen = 0;
            if (ary != null)
                aryrolelen = ary.length;
            if (aryrolelen == 0)
                return vt;

            Logger.getLogger(getClass()).info("matchActionUser aryrolelen=" + aryrolelen + " roleCodes=" + roleCodes + " rankCode=" +
                        rankCode + " dept=" + dept);

            RoleMgr rm = new RoleMgr();

            LogUtil.getLog(getClass()).info("matchActionUser: relateRoleToOrganization=" + relateRoleToOrganization);

            // 根据行文方向、在方向上第一个遇到的该角色，职级和部门范围联合选定用户，如果选不到，则根据关联的职级和部门范围列出用户
            if (nextAction.isRelateRoleToOrganization()) {
                // 遍历动作上指定的角色
                for (int k = 0; k < aryrolelen; k++) {
                    RoleDb rd = rm.getRoleDb(ary[k]);
                    /*
                    Vector v_user = rd.getAllUserOfRole();
                    // 下面的只有一个用户，就认为匹配成功，而在支持跳过的情况下，该用户可能根据行文方向并不存在，但因其它部门中有一个这样角色的用户，却错误匹配了
                    // 该角色中只有一个用户，则匹配成功
                    Logger.getLogger(getClass()).info("matchActionUser: v_user size=" + v_user.size());
                    if (v_user.size() == 1) {
                        UserDb ud = (UserDb) v_user.get(0);
                        vt.addElement(ud);
                        continue;
                    }
                    */
                    if (vu.size()==0 && isTest)
                        return vt;                    
                    Logger.getLogger(getClass()).info("vu size=" + vu.size() + " deptOfUserWithMultiDept=" + deptOfUserWithMultiDept);
                    
                    boolean isRelateActionMultiUser = false;
                   	String[] aryUser = StrUtil.split(actionRelated.getUserName(), ",");
                   	if (aryUser!=null && aryUser.length>1) {
                   		isRelateActionMultiUser = true;
                   	}
                   	
                    if (vu.size()==0) {
                    	// 如果还没有保存草稿，且关联的是表单中的部门
                    	if (wf.getStatus()==WorkflowDb.STATUS_NONE) {
                    		if (nextAction.getRelateToAction().equals(RELATE_TO_ACTION_DEPT)) {
                    			// return vt;
                                throw new ErrMsgException("开始节点至下一节点不支持关联到表单中的部门的方式！"); // 因为这时表单尚未保存                   			
                    		}
                    	}
                    	
                    	// 当节点上仅有单个用户时，如果没有部门，则报提示，如：用admin测试时
                    	if (!isRelateActionMultiUser) {
                        	throw new ErrMsgException(curAction.getUserRealName() + " 关联节点 " + curAction.getJobName() + "：" + curAction.getTitle() + " 节点中的用户尚未被分配部门，不支持关联到组织机构的自动匹配人员方式！");                    		
                    	}
                    	
                        // 如果节点未被忽略
                    	
                    	// 当关联上一节点时,因为允许多个用户，之前所取得的vu = du.getDeptsOfUser(curAction.getUserName())可能为空，即vu.size=0
                        if (!nextAction.getRelateToAction().equals(RELATE_TO_ACTION_DEFAULT)) {
                        	throw new ErrMsgException(curAction.getUserRealName() + " 当前节点 " + curAction.getJobName() + "：" + curAction.getTitle() + " 有多个用户或者节点中的用户尚未被分配部门，不支持关联到组织机构的自动匹配人员方式！");
                        }
                    }
            
                   	// 如果关联节点userName中只有1个用户
                    if (!isRelateActionMultiUser) {
	                    // 取得用户所在部门
	                    DeptDb dd = new DeptDb();
	                    if (isNeedCheckCurUserMultiDept) {
	    	                if (vu.size()>1) {
		                        // 当提交后
		                        if (deptOfUserWithMultiDept==null || deptOfUserWithMultiDept.equals(""))
		                            throw new ErrMsgException("请选择您所在的部门！"); // 提交时未选择部门
		                        else
		                            dd = dd.getDeptDb(deptOfUserWithMultiDept);	                    		
	                    	}
	    	                else if (vu.size()>0) {
	    	                	dd = (DeptDb) vu.get(0);
	    	                }
	                    }
	                    else {
	                    	if (vu.size()>0) {
	                    		dd = (DeptDb) vu.get(0);
	                    	}
	                    }
	                    vt.addAll(doMatchActionUser(nextAction, curAction, dd, rd));
                    }
                    else {
                        // fgf 20170316 如果是关联的上一节点，则应允许节点中存在多个人的情况，场景：地税意见建议流程中，选择部门管理员（可能多个）后至部门处长
                    	if (nextAction.isRelateRoleToOrganization()) {
                    		for (int i=0; i<aryUser.length; i++) {
                    			vu = du.getDeptsOfUser(aryUser[i]);
                    			
        	                    DeptDb dd = new DeptDb();
        	                    if (isNeedCheckCurUserMultiDept) {
	        	                    if (vu.size()>1) {
	        	                        // 当提交后
	        	                        if (deptOfUserWithMultiDept==null || deptOfUserWithMultiDept.equals(""))
	        	                            throw new ErrMsgException("请选择您所在的部门！"); // 提交时未选择部门
	        	                        else
	        	                            dd = dd.getDeptDb(deptOfUserWithMultiDept);
	        	                    }
	        	                    else {
	        	                    	if (vu.size()>0) {
	        	                    		dd = (DeptDb) vu.get(0);
	        	                    	}
	        	                    }
        	                    }
        	                    else {
        	                    	if (vu.size()>0) {
        	                    		dd = (DeptDb) vu.get(0);
        	                    	}
        	                    }
        	                    
        	                    Vector v = doMatchActionUser(nextAction, curAction, dd, rd);
        	                    vt.addAll(v);
        	                    
        	                    if (curUserName.equals(aryUser[i])) {
        	                    	myVt = v;
        	                    }
                    		}
                    	}
                    }
                }
            }
            else {
/*              Logger.getLogger(getClass()).info("aryrolelen23456=" + aryrolelen + " rankCod1e=" +
                            rankCode + " dept=" + dept + "333");*/

                // 如果没有自动匹配
                // 则根据限定的角色、职位、部门列出所有符合条件的用户
                for (int i = 0; i < aryrolelen; i++) {
                    RoleDb rd = rm.getRoleDb(ary[i]);
                    if (!rd.isLoaded()) {
                    	throw new ErrMsgException("角色不存在! 注意删除重建了角色后，需要重新在流程节点上指定角色！");
                    }
                    Vector v_user = rd.getAllUserOfRole();

                    // 根据关联的职级查找用户
                    String rankCode = nextAction.getRankCode();
                    if (!rankCode.equals("")) {
                        Iterator ir = v_user.iterator();
                        while (ir.hasNext()) {
                            UserDb ud = (UserDb) ir.next();
                            // 职级匹配
                            if (ud.getRankCode().equals(rankCode)) {
                                addUserDbToVector(vt, ud);
                            }
                        }
                    }

                    String dept = nextAction.getDept().trim();
                    // LogUtil.getLog(getClass()).info("matchActionUser: dept=" + dept + "--" + " jobName=" + nextAction.getJobName() + "。");
                    // 根据关联的部门查找用户
                    if (!dept.equals("")) {
                        String[] arydept = StrUtil.split(dept, ",");
                        int len1 = arydept.length;
                        Iterator ir = v_user.iterator();
                        while (ir.hasNext()) {
                            UserDb ud = (UserDb) ir.next();
                            for (int j = 0; j < len1; j++) {
                                if (du.isUserOfDept(ud.getName(), arydept[j])) {
                                    addUserDbToVector(vt, ud);
                                }
                            }
                        }
                    }

                    Logger.getLogger(getClass()).info("aryrolelen=" + aryrolelen + " rankCode=" +
                                rankCode + " dept=" + dept + "333");

                    // 过滤被选中的用户，使满足职级条件的用户同时满足部门条件
                    if (!dept.equals("") && !rankCode.equals("")) {
                        Vector vv = new Vector(); // 用以记录vt中将要被删除的UserDb
                        String[] arydept = StrUtil.split(dept, ",");
                        int len1 = arydept.length;
                        int size = vt.size();
                        for (int k = 0; k < size; k++) {
                            UserDb ud = (UserDb) vt.get(k);
                            for (int j = 0; j < len1; j++) {
                                if (!du.isUserOfDept(ud.getName(), arydept[j])) {
                                    vv.addElement(ud);
                                }
                            }
                        }
                        Iterator ir = vv.iterator();
                        while (ir.hasNext()) {
                            UserDb ud = (UserDb) ir.next();
                            vt.remove(ud);
                        }
                    }

                    // 如果既未设部门范围，也未设职级，则将角色中的所有人员列出
                    if (dept.equals("") && rankCode.equals("")) {
                        Iterator ir = v_user.iterator();
                        Logger.getLogger(getClass()).info("v_user.size=" + v_user.size());
                        while (ir.hasNext()) {
                            UserDb ud = (UserDb) ir.next();
                            Logger.getLogger(getClass()).info("user name=" + ud.getName() +
                                                              " realname=" + ud.getRealName());
                            addUserDbToVector(vt, ud);
                        }
                    }

                    // 根据行文方向列出所有的用户

                }
            }
        } else {
            // 用户型，则检查是否只有一个用户，如果是，则自动填充action中的用户，不是，则返回所有设定的人员
            String users = nextAction.getJobCode();
            Logger.getLogger(getClass()).info("matchActionUser: users=" + users);
            if (users.equals(PRE_TYPE_STARTER) || users.equals(PRE_TYPE_SELF)) {
                if (!isTest) {
                    // 填充为流程发起人员
                    UserDb ud = new UserDb();
                    ud = ud.getUserDb(startAction.getUserName());
                    vt.addElement(ud);
                }
            }
            else if (users.equals(PRE_TYPE_DEPT_MGR)) {
                // 找到沿组织机构树往上距离最近的部门管理员
                if (!isTest) {
                    // 取出关联节点用户所在的部门
                	// 如果用户所在的部门不存在
                	if (vu.size()==0) {
                        throw new ErrMsgException("请先安排所在部门！");       		
                	}
                    DeptDb dd = null;
                    if (vu.size()>1) {
                    	// 取所选的兼职部门
                    	if (deptOfUserWithMultiDept!=null && !"".equals(deptOfUserWithMultiDept)){
                    		dd = new DeptDb();
                    		dd = dd.getDeptDb(deptOfUserWithMultiDept);
                    	}
                    }
                    if (dd==null) {
                    	dd = (DeptDb)vu.elementAt(0); // 取用户所在的部门
                    }
                    String dCode = dd.getCode();

                    DeptDb parentDept = null;
                    String deptCode = dd.getCode();
                    do {
                        // 在本部门中寻找
                        boolean isFound = false;
                        Iterator ir = du.list(deptCode).iterator();
                        while (ir.hasNext()) {
                            DeptUserDb dud = (DeptUserDb) ir.next();
                            // 取得该本部门的管理员
                            if (com.redmoon.oa.pvg.Privilege.canUserAdminDept(dud.getUserName(), dCode)) {
                                vt.addElement(um.getUserDb(dud.getUserName()));
                                isFound = true;
                            }
                        }
                        if (isFound)
                            break;

                        if (dd.getParentCode().equals("-1"))
                        	break;
                        // 取出父部门
                        parentDept = dd.getDeptDb(dd.getParentCode());
                        if (parentDept == null)
                            break;

                        // 在兄弟节点中寻找
                        Iterator ir2 = parentDept.getChildren().iterator();
                        while (ir2.hasNext()) {
                            DeptDb dd2 = (DeptDb)ir2.next();
                            // 跳过本部门
                            if (dd2.getCode().equals(dd.getCode()))
                                continue;

                            ir = du.list(dd2.getCode()).iterator();
                            while (ir.hasNext()) {
                                DeptUserDb dud = (DeptUserDb) ir.next();
                                // 取得该本部门的管理员
                                if (com.redmoon.oa.pvg.Privilege.canUserAdminDept(dud.getUserName(), dCode)) {
                                    vt.addElement(um.getUserDb(dud.getUserName()));
                                    isFound = true;
                                }
                            }
                            if (isFound)
                                break;
                        }

                        if (isFound)
                            break;

                        if (parentDept.getCode().equals(DeptDb.ROOTCODE))
                            break;

                        // 往上寻找
                        deptCode = parentDept.getCode();
                        dd = parentDept;
                    }
                    while (true);
                }
            }
            else if (users.equals(PRE_TYPE_FORE_ACTION)) {
                if (!isTest) {
                    // 如果为预定义节点（上一节点处理人员）填充为其上一节点的处理人员,如果有多个节点，则将上一节点的处理人员全部置入
                    Vector v = curAction.getLinkFromActions();
                    Iterator ir = v.iterator();
                    while (ir.hasNext()) {
                        WorkflowActionDb w = (WorkflowActionDb) ir.next();
                        users = w.getUserName();
                        String[] ary = StrUtil.split(users, ",");
                        int len = 0;
                        if (ary != null)
                            len = ary.length;
                        if (len > 1) {
                            for (int i = 0; i < len; i++) {
                                vt.addElement(um.getUserDb(ary[i]));
                            }
                        } else {
                            vt.addElement(um.getUserDb(users));
                        }
                    }
                }
            }
            else if (users.startsWith(PRE_TYPE_ACTION_USER)) {
                if (!isTest) {
                    // 所选节点上的用户
                    String iName = users.substring((PRE_TYPE_ACTION_USER + "_").length());

                    WorkflowActionDb w = getWorkflowActionDbByInternalName(iName, flowId);
                    users = w.getUserName();
                    String[] ary = StrUtil.split(users, ",");
                    int len = 0;
                    if (ary != null)
                        len = ary.length;
                    if (len > 1) {
                        for (int i = 0; i < len; i++) {
                            vt.addElement(um.getUserDb(ary[i]));
                        }
                    } else {
                        vt.addElement(um.getUserDb(users));
                    }
                }
            }
            else if (users.startsWith(PRE_TYPE_FIELD_USER)) {
                if (!isTest) {
                    // 表单中指定的用户
                    String fieldNames = users.substring((PRE_TYPE_FIELD_USER + "_").length());

                    Leaf lf = new Leaf();
                    lf = lf.getLeaf(wf.getTypeCode());

                    FormDb fd = new FormDb();
                    fd = fd.getFormDb(lf.getFormCode());

                    if (fieldNames.startsWith("nest.")) {
                        fieldNames = fieldNames.substring("nest.".length());

                        // 取得嵌套表
                        Vector v = fd.getFields();
                        Iterator ir = v.iterator();
                        boolean isFound = false;
                        while (ir.hasNext()) {
                            FormField ff = (FormField) ir.next();
                            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                                // System.out.println(getClass() + " ff.getMacroType()=" + ff.getMacroType());
                                if (ff.getMacroType().equals("nest_table") || ff.getMacroType().equals("nest_sheet")) {
                                    isFound = true;
                                    String nestFormCode = ff.getDefaultValue();
                                    FormDb nestfd = new FormDb();
                                    nestfd = nestfd.getFormDb(nestFormCode);
                                    String cwsId = String.valueOf(wf.getId());
                                    // 取得嵌套表中的数据
                                    String sql = "select id from " + nestfd.getTableNameByForm() + " where cws_id=" + StrUtil.sqlstr(cwsId);
                                    sql += " order by cws_order";

                                    // out.print(sql);

                                    com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
                                    Vector vNest = fdao.list(nestFormCode, sql);
                                    Iterator irNest = vNest.iterator();
                                    while (irNest.hasNext()) {
                                        fdao = (com.redmoon.oa.visual.FormDAO)irNest.next();
                                        String val = fdao.getFieldValue(fieldNames);
                                        LogUtil.getLog(getClass()).info("matchActionUser val=" + val + fdao.getId() + " sql=" + sql);
                                        UserDb user = um.getUserDb(val);
                                        if (user.isLoaded()) {
                                            vt.addElement(user);
                                        }
                                        else
                                            throw new ErrMsgException("嵌套表字段：" + fieldNames + "对应的用户：" + val + "不存在");
                                    }
                                }
                            }
                        }
                        if (!isFound) {
                            throw new ErrMsgException("未找到嵌套表");
                        }
                    }
                    else {
                        String[] fieldAry = StrUtil.split(fieldNames, ",");
                        if (fieldAry==null)
                            throw new ErrMsgException("指定人员的表单域：" + fieldNames + "不存在！");
                        
                        // 判断是否当表单中的用户值改变时，重新匹配用户
                        String op = ParamUtil.get(request, "op");
                        boolean isReMatchUser = "reMatchUser".equals(op);
                        String reMatchField = "", reMatchValue = "";
                        if (isReMatchUser) {
                        	reMatchField = ParamUtil.get(request, "fieldName");
                        	reMatchValue = ParamUtil.get(request, "fieldValue");
                        }
                        
                        FormDAO fdao = new FormDAO();
                        fdao = fdao.getFormDAO(curAction.getFlowId(), fd);
                        
                        for (int k=0; k<fieldAry.length; k++) {
                            String fieldName = fieldAry[k];
                            
                            FormField ff = fd.getFormField(fieldName);
                            if (ff == null)
                                throw new ErrMsgException("指定人员的表单域：" + fieldName + "不存在！");
                            
                            String userNames = StrUtil.getNullStr(fdao.getFieldValue(fieldName));
                            if (isReMatchUser) {
                            	// 如果是因为该表单域的值有变化，而重新匹配用户
                            	if (reMatchField.equals(fieldName)) {
                            		userNames = reMatchValue;
                            	}
                            }
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

                            LogUtil.getLog(getClass()).info("matchActionUser: fieldName=" + fieldName + " users=" +
                                                            users +
                                                            " userNames=" + userNames + " ary=" + ary + " vt.size=" +
                                                            vt.size());

                            int len = 0;
                            if (ary != null)
                                len = ary.length;
                            if (len >= 1) {
                                for (int i = 0; i < len; i++) {
                                    UserDb user = um.getUserDb(ary[i]);
                                    if (user.isLoaded())
                                        vt.addElement(user);
                                    else
                                        throw new ErrMsgException(nextAction.getJobName() + "=" + ary[i] + " 不存在！");
                                }
                            }
                        }
                    }
                }
            }
            else if (users.startsWith(PRE_TYPE_PROJECT_ROLE)) {
            	if (com.redmoon.oa.kernel.License.getInstance().isEnterprise() || com.redmoon.oa.kernel.License.getInstance().isGroup() || com.redmoon.oa.kernel.License.getInstance().isPlatform())
            		;
            	else {
            		throw new ErrMsgException("系统版本中无此功能！");
            	}

                if (!isTest) {
                    // 所选节点上的用户
                    String prjRole = users.substring((PRE_TYPE_PROJECT_ROLE + "_").length());
                    
                    // 取得项目的ID
                    long projectId = wf.getProjectId();
                    if (projectId==-1) {
                    	throw new ErrMsgException("流程未与项目关联！");
                    }
                    
                    // 取得项目角色中的相应人员
                    com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
                    String formCode = "project_members";
                    String sql = "select id from " + FormDb.getTableName(formCode) + " where cws_id='" + projectId + "' and prj_role=" + StrUtil.sqlstr(prjRole);
                    Vector v = fdao.list(formCode, sql);
                    if (v.size()==0) {
                    	throw new ErrMsgException("项目角色" + prjRole + "中无对应人员！");
                    }
                    else {
	                    Iterator ir = v.iterator();
	                    while (ir.hasNext()) {
	                    	fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
	                    	String prjUser = fdao.getFieldValue("prj_user");
	                        vt.addElement(um.getUserDb(prjUser));                    	
	                    }
                    }
                }
            }
            else if (users.startsWith(PRE_TYPE_NODE_SCRIPT)) {
            	// 通过脚本选人
            	WorkflowPredefineDb wpd = new WorkflowPredefineDb();
            	wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
        		String script = WorkflowActionDb.getActionProperty(wpd, nextAction.getInternalName(), "nodeScript");
            	Leaf lf = new Leaf();
            	lf = lf.getLeaf(wf.getTypeCode());
				if (script != null) {
					Interpreter bsh = new Interpreter();
					try {
						StringBuffer sb = new StringBuffer();
						FormDAO fdao = new FormDAO();
						FormDb fd = new FormDb();
						fd = fd.getFormDb(lf.getFormCode());
						fdao = fdao.getFormDAO(wf.getId(), fd);

    					BeanShellUtil.setFieldsValue(fdao, sb);

						// 赋值给用户
						// sb.append("userName=\"" + privilege.getUser(request)
						// 		+ "\";");
						sb.append("int flowId=" + wf.getId() + ";");
						
						bsh.set("actionId", curAction);
						bsh.set("request", request);
						
						// bsh.set("fileUpload", fu);

						bsh.eval(BeanShellUtil.escape(sb.toString()));

						bsh.eval(script);
						Object obj = bsh.get("ret");
						if (obj != null) {
							Vector v = (Vector) obj;
							vt.addAll(v);
						}
					} catch (EvalError e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}            	
            }
            else if (nextAction.getJobCode().equals( // 我的领导
					WorkflowActionDb.PRE_TYPE_MYLEADER)) {
				UserSetupDb usd = new UserSetupDb(pvg.getUser(request));
				String uName = usd.getMyleaders();
				if (!uName.equals("")) {
					String[] nameAry = StrUtil.split(uName, ",");
					int aryLen = nameAry.length;
					for (int k = 0; k < aryLen; k++) {
						UserDb ud = um.getUserDb(nameAry[k]);
						addUserDbToVector(vt, ud);
					}
				}
			} else if (nextAction.getJobCode().equals( // 我的下属
					WorkflowActionDb.PRE_TYPE_MYSUBORDINATE)) {
				UserSetupDb usd = new UserSetupDb(pvg.getUser(request));
				Vector mySubordinates = usd.getMySubordinates();
				Iterator myit = mySubordinates.iterator();
				while (myit.hasNext()) {
					usd = (UserSetupDb) myit.next();
					if (usd == null || !usd.isLoaded()) {
						continue;
					}
					UserDb ud = um.getUserDb(usd.getUserName());
					addUserDbToVector(vt, ud);
				}
			}            
            else {
                String[] ary = StrUtil.split(users, ",");
                int len = 0;
                if (ary != null)
                    len = ary.length;
                if (len > 1) {
                    for (int i = 0; i < len; i++) {
                        vt.addElement(um.getUserDb(ary[i]));
                    }
                } else {
                    vt.addElement(um.getUserDb(users));
                }
            }
        }

        int roleRankMode = 1; // 1 - 小于等于角色级别的节点跳过

        // 如果只匹配到一个用户，则填充action中的用户
        if (vt.size() == 1) {
            UserDb ud = (UserDb) vt.get(0);
            boolean isIgnored = false;
            if (nextAction.getIgnoreType()==IGNORE_TYPE_USER_ACCESSED_BEFORE) {
                // 检查用户是否之前已经处理过流程，且可以跳过，则清空vt
                if (isUserAccessedBefore(ud.getName(), nextAction.getId(), nextAction.getFlowId())) {
                    vt.removeAllElements();
                    isIgnored = true;
                }
            }

            Logger.getLogger(getClass()).info("Only one user matched name=" +
                                              ud.getName() +
                                              " realName=" + ud.getRealName());
            if (!isIgnored && !isTest) {
                setActionUserOnMatch(nextAction, vt);
            }
        } else if (vt.size() > 1) { // 角色中的用户数或者预设的员工数目大于1
            // 如果定义了策略，则应用策略
            String strategy = nextAction.getStrategy();
            if (!strategy.equals("")) {
                StrategyMgr sm = new StrategyMgr();
                StrategyUnit su = sm.getStrategyUnit(strategy);
                // Logger.getLogger(getClass()).info("strategy strategy=" + strategy);
                if (su != null) {
                    IStrategy ist = su.getIStrategy();
                    Vector v = ist.selectUser(vt);
                    // 有些策略在处理前不选择人员，而在处理后，忽略掉其他的待处理人员
                    if (v!=null && v.size()>0) {
                        if (!isTest) {
                            setActionUserOnMatch(nextAction, v);
                        }
                        vt = v;
                    }
                }
            }
        }

        if (nextAction.isRelateRoleToOrganization() && nextAction.getRelateToAction().equals(RELATE_TO_ACTION_DEFAULT)) {
    		if (curAction.getUserName().indexOf(",")!=-1) {
    			return myVt;
    		}
        }    
        
       	return vt;
    }
    
    /**
     * 分配策略中的人員是否可以由前一節點人員選擇
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
    	// System.out.println(getClass() + " " + getTitle() + " " + strategy);
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
     * 匹配本部门 DIRECTION_MYDEPT
     * @param nextAction WorkflowActionDb
     * @param dd DeptDb
     * @param rd RoleDb
     * @param vt Vector
     * @return boolean
     */
    public boolean matchActionUserMyDept(WorkflowActionDb nextAction, DeptDb dd, RoleDb rd, Vector vt) {
        DeptUserDb du = new DeptUserDb();
        UserMgr um = new UserMgr();
        boolean re = false;
        // 在本部门中寻找
        Logger.getLogger(getClass()).info("mydept=" + dd.getName());
        Iterator ir = du.list(dd.getCode()).iterator();
        while (ir.hasNext()) {
            DeptUserDb dud = (DeptUserDb) ir.next();
            // 取得用户的所有角色
            UserDb ud = um.getUserDb(dud.getUserName());
            Logger.getLogger(getClass()).info("user=" + ud.getRealName());
            RoleDb[] roles = ud.getRoles();
            int rlen = roles.length;
            for (int i = 0; i < rlen; i++) {
                // 用户的某个角色与预置的角色一致，职级也一致
                Logger.getLogger(getClass()).info("user=" + ud.getRealName() + " role=" + roles[i].getDesc() +
                                                  " finding role=" + rd.getDesc());
                if (roles[i].getCode().equals(rd.getCode())) {
                    // Logger.getLogger(getClass()).info("rd name:" + rd.getDesc() + "" + ud.getRealName());
                    boolean found = true;
                    // 如果预设的职级不为空，则检查是否一致
                    if (!nextAction.getRankCode().equals("")) {
                        if (ud.getRankCode().equals(nextAction.getRankCode()))
                            found = true;
                        else
                            found = false;
                    }
                    if (found) {
                        // 如果预设的部门不为空，则检查是否一致
                        String dept = StrUtil.getNullStr(nextAction.getDept()).trim();
                        if (!dept.equals("")) {
                            found = false;
                            String[] arydept = StrUtil.split(dept, ",");
                            int arydeptlen = arydept.length;
                            for (int m = 0; m < arydeptlen; m++) {
                                // 用户属于预置的部门
                                if (du.isUserOfDept(ud.getName(), arydept[m]))
                                    found = true;
                            }
                        }
                    }
                    if (found) {
                        addUserDbToVector(vt, ud);
                        re = true;
                    }
                }
            }
        }
        return re;
    }
    
    /**
     * 在同级的其它部门中寻找
     * @Description: 
     * @param nextAction
     * @param dd
     * @param rd
     * @param vt
     * @return
     */
    public boolean matchActionUserParallel(WorkflowActionDb nextAction, DeptDb dd, RoleDb rd, Vector vt) {
        DeptUserDb du = new DeptUserDb();    	
        DeptDb parentDept = dd.getDeptDb(dd.getParentCode());
        UserMgr um = new UserMgr();
        boolean re = false;        
        if (parentDept != null) {
            Iterator brotherir = parentDept.getChildren().iterator();
            while (brotherir.hasNext()) {
                DeptDb brotherDept = (DeptDb) brotherir.next();
                // 跳过本部门
                if (brotherDept.getCode().equals(dd.getCode()))
                    continue;
                Iterator duir = du.list(brotherDept.getCode()).iterator();
                while (duir.hasNext()) {
                    DeptUserDb dud = (DeptUserDb) duir.next();
                    // 取得用户的所有角色
                    UserDb ud = um.getUserDb(dud.getUserName());
                    RoleDb[] roles = ud.getRoles();
                    int rlen = roles.length;
                    for (int i = 0; i < rlen; i++) {
                        // 用户的某个角色与预置的角色一致，职级也一致
                        if (roles[i].getCode().equals(rd.getCode())) {
                            boolean found = true;
                            // 如果预设的职级不为空，则检查是否一致
                            if (!nextAction.getRankCode().
                                    equals("")) {
                                if (ud.getRankCode().equals(
                                        nextAction.
                                        getRankCode()))
                                    found = true;
                                else
                                    found = false;
                            }
                            if (found) {
                                // 如果预设的部门不为空，则检查是否一致
                                String dept = StrUtil.getNullStr(nextAction.
                                        getDept()).trim();
                                if (!dept.equals("")) {
                                    found = false;
                                    String[] arydept = StrUtil.split(dept, ",");
                                    int arydeptlen = arydept.length;
                                    for (int m = 0; m < arydeptlen; m++) {
                                        // 用户属于预置的部门
                                        if (du.isUserOfDept(ud.getName(), arydept[m]))
                                            found = true;
                                    }
                                }
                            }
                            if (found) {
                                addUserDbToVector(vt, ud);
                                re = true;
                            }
                        }
                    }
                }
            }
        }    	
        return re;
    }

    /**
     * 匹配上行 DIRECTION_UP
     * @param nextAction WorkflowActionDb 待匹配节点
     * @param dd DeptDb
     * @param rd RoleDb
     * @param vt Vector
     * @return boolean
     */
    public boolean matchActionUserUp(WorkflowActionDb nextAction, DeptDb dd, RoleDb rd, Vector vt) {
        DeptUserDb du = new DeptUserDb();
        UserMgr um = new UserMgr();
        boolean re = false;
        // 取得上一级父节点
        DeptDb parentDept = dd.getDeptDb(dd.getParentCode());
        while (parentDept != null && parentDept.isLoaded()) {
            boolean found = false;        	
            // LogUtil.getLog(getClass()).info("parentDept=" + parentDept + " " + parentDept.getCode());
            // 遍历父节点下的所有人员，如果角色一致，则加入其中
            Iterator ir = du.list(parentDept.getCode()).
                          iterator();
            while (ir.hasNext()) {
                du = (DeptUserDb) ir.next();
                // 取得用户的所有角色
                UserDb ud = um.getUserDb(du.getUserName());
                RoleDb[] roles = ud.getRoles();
                int rlen = roles.length;
                for (int i = 0; i < rlen; i++) {
                    // 用户的某个角色与预置的角色一致，职级也一致
                    if (roles[i].getCode().equals(rd.getCode())) {
                        if (!nextAction.getRankCode().equals("")) {
                            if (ud.getRankCode().equals(nextAction.getRankCode()))
                                found = true;
                            else
                                found = false;
                        }else{
                        	found = true;
                        }
                        if (found) {
                            addUserDbToVector(vt, ud);
                            re = true;
                            break;
                        }
                    }
                }
            }
            if (found) {
            	break;
            }
            
            // 20161207 使支持根节点
            if (parentDept.getCode().equals(DeptDb.ROOTCODE)) {
            	break;
            }
            else {
            	// 取得再往上一个父节点
            	parentDept = parentDept.getDeptDb(parentDept.getParentCode());
            }
        }
        return re;
    }

    /**
     * 匹配本部门，匹配不到则上行，否则退出
     * @param nextAction WorkflowActionDb
     * @param dd DeptDb
     * @param rd RoleDb
     * @param vt Vector
     * @return boolean
     */
    public boolean matchActionUserMyDeptAndUp(WorkflowActionDb nextAction, WorkflowActionDb curAction, DeptDb dd, RoleDb rd, Vector vt, boolean isMyLeader) {
    	boolean re = matchActionUserMyDept(nextAction, dd, rd, vt);

    	// 如果是找领导模式
    	if (vt.size()>0 && isMyLeader) {
        	boolean isMe = false;
    		// 如果当前节点存在有多个处理人的话，应该不支持找领导模式
	    	String[] users = StrUtil.split(curAction.getUserName(), ",");
	        int len = 0;
	        // 为null表示matchActionUser未匹配到用户
	        if (users != null) {
	            len = users.length;
	            for (int n = 0; n < len; n++) {
	            	String curUserName = users[n];
	            	// 判断本部门中找到的是否为本人
	            	Iterator ir = vt.iterator();
	            	while (ir.hasNext()) {
	            		UserDb ud = (UserDb)ir.next();
	            		if (ud.getName().equals(curUserName)) {
	            			isMe = true;
	            			break;
	            		}
	            	}
	            	if (isMe)
	            		break;
	            }
	        }
	        // 如果找到的是本人，则继续往上级部门查找
	        if (isMe) {
	        	vt.clear();
	        	re = false;
	        }
    	}
    	
    	if (!re)
            return matchActionUserUp(nextAction, dd, rd, vt);
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

    public Vector doMatchActionUser(WorkflowActionDb nextAction, WorkflowActionDb curAction,
            DeptDb dd, RoleDb rd) {
    	Vector vt = new Vector();
        int direction = nextAction.getDirection();
        // 上行文
        if (direction == DIRECTION_UP) {
            matchActionUserUp(nextAction, dd, rd, vt);
        } else if (direction == DIRECTION_MYDEPT) {
            // 在本部门中寻找
            matchActionUserMyDept(nextAction, dd, rd, vt);
        }
        else if (direction == DIRECTION_MYDEPT_UP) {
            // 本部门及上行
            matchActionUserMyDeptAndUp(nextAction, curAction, dd, rd, vt, false);
        }
        else if (direction == DIRECTION_MY_LEADER) {
            matchActionUserMyDeptAndUp(nextAction, curAction, dd, rd, vt, true);
        }
        else if (direction == DIRECTION_PARALLEL_MYDEPT) { // 本部门及平行文
        	boolean re = matchActionUserMyDept(nextAction, dd, rd, vt);
        	if (!re) {
        		matchActionUserParallel(nextAction, dd, rd, vt);
        	}
        }
        else if (direction == WorkflowActionDb.DIRECTION_PARALLEL_MYDEPT_UP) {
        	// 在本部门及同级部门中寻找，找不到，则继续往上寻找，顺序先上级，而后其同级，再上级....
            while (dd != null && dd.isLoaded()) {
                // 遍历父节点下的所有人员，如果角色一致，则加入其中
                boolean re = matchActionUserMyDept(nextAction, dd, rd, vt);
                if (!re) {
                    // 遍历其它兄弟节点                                            	
                    re = matchActionUserParallel(nextAction, dd, rd, vt);
                }
                if (re) {
                	break;
                }
                // 取得再往上一个父节点
                dd = dd.getDeptDb(dd.getParentCode());
            }
        }
        else if (direction == WorkflowActionDb.DIRECTION_PARALLEL) { // 平行文
            // 在同级的其它部门中寻找
        	matchActionUserParallel(nextAction, dd, rd, vt);                        	
        } else if (direction == DIRECTION_DOWN) {
            // 下行匹配
            doMatchDirectionDown(nextAction, dd, rd, vt);
        }
        
        return vt;
    }

    /**
     * 下行匹配
     * @param nextAction
     * @param curUserDept
     * @param role 动作上指定的角色
     * @param vt 在vt中加入符合条件的用户
     */
    public void doMatchDirectionDown(WorkflowActionDb nextAction,
                                     DeptDb curUserDept, RoleDb role, Vector vt) {
        DeptUserDb du = new DeptUserDb();
        UserMgr um = new UserMgr();
        // 下行文，在其孩子节点中寻找
        Iterator childir = curUserDept.getChildren().iterator();
        while (childir.hasNext()) {
            DeptDb childDept = (DeptDb) childir.next();

            // Logger.getLogger(getClass()).info("doMatchDirectionDown:" + childDept.getName());

            // 遍历孩子节点部门中的所有人员
            Iterator duir = du.list(childDept.getCode()).iterator();
            while (duir.hasNext()) {
                DeptUserDb dud = (DeptUserDb) duir.next();
                // 取得用户的所有角色
                UserDb ud = um.getUserDb(dud.getUserName());

                RoleDb[] roles = ud.getRoles();
                int rlen = roles.length;
                for (int i = 0; i < rlen; i++) {
                    // 用户的某个角色与预置的角色一致，职级也一致
                    if (roles[i].getCode().equals(role.getCode())) {
                        boolean found = true;
                        // 如果预设的职级不为空，则检查是否一致
                        if (!nextAction.getRankCode().equals("")) {
                            if (ud.getRankCode().equals(nextAction.getRankCode()))
                                found = true;
                            else
                                found = false;
                        }
                        if (found) {
                            Logger.getLogger(getClass()).info(
                                    "doMatchDirectionDown:" +
                                    nextAction.getDept() + " ud.getName()=" +
                                    ud.getRealName());
                            // 如果预设的部门不为空，则检查是否一致
                            String dept = StrUtil.getNullStr(nextAction.getDept()).
                                          trim();
                            if (!dept.equals("")) {
                                found = false;
                                String[] arydept = StrUtil.split(dept, ",");
                                int arydeptlen = arydept.length;
                                for (int m = 0; m < arydeptlen; m++) {
                                    Logger.getLogger(getClass()).info(
                                            "doMatchDirectionDown:arydept[" + m +
                                            "]=" + arydept[m]);
                                    // 用户属于预置的部门
                                    if (du.isUserOfDept(ud.getName(), arydept[m])) {
                                        Logger.getLogger(getClass()).info(
                                                "doMatchDirectionDown:isUserOfDept=true");
                                        found = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (found) {
                            addUserDbToVector(vt, ud);
                        }
                    }
                }
            }
            doMatchDirectionDown(nextAction, childDept, role, vt);
        }
    }

    /**
     * 当匹配时，设置动作的处理人 20170316 fgf 废除
     * @param wad WorkflowActionDb
     */
    public void setActionUserOnMatchXXX(WorkflowActionDb wad, UserDb ud) throws ErrMsgException {
        wad.setUserName(ud.getName());
        wad.setUserRealName(ud.getRealName());
        // 为了便于用户在选错后再选，会出现多次匹配的问题
        if (wad.getNodeMode() == wad.NODE_MODE_ROLE ||
            wad.getNodeMode() ==
            WorkflowActionDb.NODE_MODE_ROLE_SELECTED)
            wad.setNodeMode(WorkflowActionDb.
                            NODE_MODE_ROLE_SELECTED);
        else
            wad.setNodeMode(WorkflowActionDb.
                            NODE_MODE_USER_SELECTED);
        wad.save();
    }

    /**
     * 置节点上的用户，用于当跳过节点匹配时
     * @param wad WorkflowActionDb
     * @param v Vector 用户UserDb
     */
    public void setActionUserOnMatch(WorkflowActionDb wad, Vector v) throws ErrMsgException {
        String uNames = "", uRealNames = "";
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            UserDb user = (UserDb)ir.next();
            if (!user.isLoaded()) {
            	continue;
            }
            String userRealName = user.getRealName();
            if (uNames.equals("")) {
                uNames = user.getName();
                uRealNames = userRealName;
            } else {
                uNames += "," + user.getName();
                uRealNames += "," + userRealName;
            }
        }

        wad.setUserName(uNames);
        wad.setUserRealName(uRealNames);
        // 为了便于用户在选错后再选，会出现多次匹配的问题
        if (wad.getNodeMode() == wad.NODE_MODE_ROLE ||
            wad.getNodeMode() ==
            WorkflowActionDb.NODE_MODE_ROLE_SELECTED)
            wad.setNodeMode(WorkflowActionDb.
                            NODE_MODE_ROLE_SELECTED);
        else
            wad.setNodeMode(WorkflowActionDb.
                            NODE_MODE_USER_SELECTED);
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
            if (flag.substring(6, 7).equals("1"))
                flagXorRadiate = true;
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
    		if (flag.substring(8, 9).equals("1"))
    			return true;
    		else
    			return false;
    	}
    	else
    		return true;
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
    		if (flag.substring(9, 10).equals("1"))
    			return true;
    		else
    			return false;
    	}
    	else
    		return true;
    }
    
    /**
     * 能否定稿，即接受修订
     * 20130720 fgf
     * @return
     */
    public boolean canReceiveRevise() {
    	if (flag.length()>=11) {
    		if (flag.substring(10, 11).equals("1"))
    			return true;
    		else
    			return false;
    	}
    	else
    		return true;
    }    
    
    /**
     * 能否删除附件
     * 20130720 fgf
     * @return
     */
    public boolean canDelAttachment() {
    	if (flag.length()>=6) {
    		if (flag.substring(5, 6).equals("1"))
    			return true;
    		else
    			return false;
    	}
    	else
    		return true;
    }     

    /**
     * 是否为异或聚合节点
     * @return boolean
     */
    public boolean isXorAggregate() {
        Vector vfrom = getLinkFromActions();
        boolean flagXorAggregate = false;
        if (flag.length() >= 8) {
            if (flag.substring(7, 8).equals("1"))
                flagXorAggregate = true;
        }

        if (flagXorAggregate && vfrom.size() > 1) {
            return true;
        }
        return false;
    }

    /**
     * 获取request的属性中存储的deptOfUserWithMultiDept
     * @param request HttpServletRequest
     * @return String
     */
    public String getDeptOfUserWithMultiDept(HttpServletRequest request) {
        String deptOfUserWithMultiDept = null;    	
    	if (request==null)
    		return deptOfUserWithMultiDept;
        // 在flow_dispose_do.jsp置request的workflowParams属性
        WorkflowParams wparam = (WorkflowParams) request.getAttribute(
                "workflowParams");
        if (wparam != null) {
            deptOfUserWithMultiDept = StrUtil.getNullStr(wparam.getFileUpload().
                                                         getFieldValue("deptOfUserWithMultiDept"));
        }
        return deptOfUserWithMultiDept;
    }
    
    /**
     * 当流程转交时，检查action中是否已选定用户，如果没有，则报错，或者系统自动选定（待扩展）
     * @return boolean
     */
    private String checkActionUserForMultiBranches(HttpServletRequest request, WorkflowActionDb nextwa, WorkflowActionDb curAction, boolean isNextActionSingle) throws ErrMsgException {
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(curAction.getFlowId());
        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
        // 如果返回后再交办时至返回者
        if (wpd.getReturnMode() == WorkflowPredefineDb.RETURN_MODE_TO_RETURNER && curAction.getStatus()==WorkflowActionDb.STATE_RETURN) {
            return "";
        }

        // 如果下一节点只有一个，且被忽略，并且节点上未选择用户
        if (nextwa.getStatus()==WorkflowActionDb.STATE_IGNORED && isNextActionSingle) {
            ;
        }

        // 如果下一节点未被忽略，且节点上未选择用户
        if (nextwa.getUserName().equals("") && nextwa.getStatus()!=WorkflowActionDb.STATE_IGNORED) {
            if (nextwa.getJobCode().equals(PRE_TYPE_USER_SELECT) || nextwa.getJobCode().equals(PRE_TYPE_USER_SELECT_IN_ADMIN_DEPT))
                return "请先选择下一节点上的办理用户！";
            else {
                // throw new ErrMsgException("请先选择角色编码为" + nextwa.getJobCode() +
                //                          "的节点上的用户！");
                // 如果节点上能够匹配到用户，则报异常，匹配不到，则有可能会自动跳过
                Vector vt = null;
                String errMsg = "";
                try {
                    String deptOfUserWithMultiDept = getDeptOfUserWithMultiDept(request);
                    vt = nextwa.matchActionUser(nextwa, curAction, false, deptOfUserWithMultiDept);
                }
                catch (MatchUserException e) {
                    ////////// 允许用户处于多个部门，流转时让其自行选择
                    return "请选择您所在的部门！";
                }
                catch (ErrMsgException ex1) {
                    // ex1.printStackTrace();
                    // 匹配时报异常，说明匹配有问题，比如：当关联组织机构时，节点中有多个人员，无法关联
                    // 或者当用户处于多个部门中，而没有在flow_dispose.jsp中选择部门便提交，此时deptOfUserWithMultiDept为空字符串(而不是null)，matchActionUser便会报异常
                    errMsg = ex1.getMessage();
                }
                if (!errMsg.equals("")) {
                    return "匹配用户出错：" + errMsg + "！";
                }

                LogUtil.getLog(getClass()).info("checkActionUser: getJobCode=" + nextwa.getJobCode() +
                                                " getJobName=" + nextwa.getJobName() + " getUserName=" +
                                                    nextwa.getUserName());

                if (nextwa.getJobCode().startsWith(PRE_TYPE_FIELD_USER)) {
                    // 在checkActionUser之前已经保存了表单，所以需在此再次匹配
                    if (vt.size()==0) {
                    	// 如果无用户跳过，或者用户之前处理过，则跳过
                    	if (nextwa.getIgnoreType() == IGNORE_TYPE_NOT)
                    		return "请填写" + nextwa.getJobName();
                    }
                    else {
                        String uNames = "";
                        String uRealNames = "";
                        Iterator ir = vt.iterator();
                        while (ir.hasNext()) {
                            UserDb user = (UserDb)ir.next();
                            if (uNames.equals("")) {
                                uNames = user.getName();
                                uRealNames = user.getRealName();
                            }
                            else {
                                uNames += "," + user.getName();
                                uRealNames += "," + user.getRealName();
                            }
                        }
                        nextwa.setUserName(uNames);
                        nextwa.setUserRealName(uRealNames);
                        nextwa.save();
                    }
                }
                else {
                    // 如果vt.size()==1，则表示匹配到了一个用户，则WorkflowActionDb中的用户会被填充
                    // 匹配到了多个用户，则说明在flow_dipose.jsp页面上没有选择用户
                    
                	// 如果当前节点为子流程，则当节点上存在多个人员时，nextwa.getUserName()可以为空
                	if (curAction.getKind() == WorkflowActionDb.KIND_SUB_FLOW) {
                	
                	}
                	else {
	                	if (vt != null && vt.size() != 0 && vt.size() != 1) {
	                        Iterator ir = vt.iterator();
	                        while (ir.hasNext()) {
	                            UserDb user = (UserDb) ir.next();
	                            LogUtil.getLog(getClass()).info("checkActionUser:" + user.getName() + "  " +
	                                                            user.getRealName());
	                        }
	                        return "请先选择用户！";
	                    }
                	}
                }
            }
        }
        return "";
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

        // System.out.println(getClass() + " " + nextwa.getTitle() + " nextwa.getUserName()=" + nextwa.getUserName() + " isNextActionSingle=" + isNextActionSingle);

        // 如果下一节点只有一个，且被忽略，并且节点上未选择用户
        if (nextwa.getStatus()==WorkflowActionDb.STATE_IGNORED && isNextActionSingle) {
            // System.out.println(getClass() + " " + nextwa.getTitle() + " nextwa.getJobName()=" + nextwa.getJobName() + " nextwa.getUserName()=" + nextwa.getUserName());
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

        // 如果下一节点未被忽略，且节点上未选择用户
        if (nextwa.getUserName().equals("") && nextwa.getStatus()!=WorkflowActionDb.STATE_IGNORED) {
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
                    vt = nextwa.matchActionUser(request, nextwa, curAction, false, deptOfUserWithMultiDept);
                }
                catch (MatchUserException e) {
                    ////////// 允许用户处于多个部门，流转时让其自行选择
                    throw new ErrMsgException("请选择您所在的部门！");
                }
                catch (ErrMsgException ex1) {
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
                    if (vt.size()==0) {
                    	// 如果无用户跳过，或者用户之前处理过，则跳过
                    	if (nextwa.getIgnoreType() == IGNORE_TYPE_NOT)
                    		throw new ErrMsgException("请填写" + nextwa.getJobName());
                    }
                    else {
                        String uNames = "";
                        String uRealNames = "";
                        Iterator ir = vt.iterator();
                        while (ir.hasNext()) {
                            UserDb user = (UserDb)ir.next();
                            if (uNames.equals("")) {
                                uNames = user.getName();
                                uRealNames = user.getRealName();
                            }
                            else {
                                uNames += "," + user.getName();
                                uRealNames += "," + user.getRealName();
                            }
                        }
                        nextwa.setUserName(uNames);
                        nextwa.setUserRealName(uRealNames);
                        nextwa.save();
                    }
                }
                else {
                    // 如果vt.size()==1，则表示匹配到了一个用户，则WorkflowActionDb中的用户会被填充
                    // 匹配到了多个用户，则说明在flow_dipose.jsp页面上没有选择用户
                    
                	// 如果当前节点为子流程，则当节点上存在多个人员时，nextwa.getUserName()可以为空
                	if (curAction.getKind() == WorkflowActionDb.KIND_SUB_FLOW) {
                	
                	}
                	else {
	                	if (vt != null && vt.size() != 0 && vt.size() != 1) {
	                        Iterator ir = vt.iterator();
	                        while (ir.hasNext()) {
	                            UserDb user = (UserDb) ir.next();
	                            LogUtil.getLog(getClass()).info("checkActionUser:" + user.getName() + "  " +
	                                                            user.getRealName());
	                        }
	                        if (nextwa.getIgnoreType() == IGNORE_TYPE_NOT) {
	                        	throw new ErrMsgException("请先选择用户！");
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
             if (vfrom.size()==0)
                 throw new ErrMsgException("本节点是开始结点，不能被返回！");

             if (returnIds==null || returnIds.length==0)
                 throw new ErrMsgException("请选择将要返回的用户！");
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
                     Logger.getLogger(getClass()).info("beforeChangeStatus:" + wapriv.getUserRealName());
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
            Logger.getLogger(getClass()).info("beforeChangeStatus: " + checkUserName + "finished!");
            // 如果本结点为发散结点，则置所有的未被跳过的to结点为DOING
            Vector v = getLinkToActions();
            boolean isNextActionSingle = v.size()==1;
            Iterator ir = v.iterator();
            if (isNextActionSingle) {
            	if (ir.hasNext()) {
	            	WorkflowActionDb wa = (WorkflowActionDb) ir.next();
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
	            	WorkflowActionDb wa = (WorkflowActionDb) ir.next();
	            	Vector tv = wa.getLinkToActions();
	            	if (tv.size() > 1) {
	            		isContersign = false;
	            		break;
	            	}
	            	if (internalName.equals("")) {
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
	                WorkflowActionDb wa = (WorkflowActionDb) ir.next();
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
	                Logger.getLogger(getClass()).info("beforeChangeStatus: begin checkActionUser!");
	                // 下一节点处理人为空且发散且不是会签
	                if (wa.getUserName().equals("") && !isContersign && !isNextActionSingle) {
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
		                		// 非条件分支的发散节点,有一个或多个分支可以走,均需要检查用户,至少有一条分支可以走即可
		                		/*
	                			String ret = checkActionUserForMultiBranches(request, wa, this, isNextActionSingle);
		                		if (ret.equals("")) {
		                			isUserChecked = true;
		                			break;
		                		} else {
		                			isUserChecked = true;
		                		}*/
	                		}
	                	}
	                } else {
	                    checkActionUser(request, wa, this, isNextActionSingle);
	                    if (!isContersign && !isNextActionSingle && !this.isXorRadiate()) {
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
            Vector vfrom = getLinkFromActions();
            if (vfrom.size()==0)
                throw new ErrMsgException("本节点是开始结点，不能被返回！");
            
            if (vfrom.size() == 1) {
            	// 如果当前节点有兄弟节点,则将其所有的兄弟节点都置为STATE_NOTDO
            	WorkflowActionDb previousAction = (WorkflowActionDb) vfrom.get(0);
            	Vector vto = previousAction.getLinkToActions();
            	for (int i = 0; i < vto.size(); i++) {
            		WorkflowActionDb SiblingAction = (WorkflowActionDb) vto.get(i);
            		if (this.id == SiblingAction.getId()) {
            			continue;
            		}
            		SiblingAction.setStatus(STATE_NOTDO);
            		SiblingAction.save();
            	}
            }

            if (returnIds==null || returnIds.length==0)
                throw new ErrMsgException("请选择将要返回的用户！");
            int len = returnIds.length;

            LogUtil.getLog(getClass()).info("beforeChangeStatus returnIds len=" + returnIds.length);
            // 返回时只能返回给一个节点，所以循环已无用
            for (int i = 0; i < len; i++) {
                // 手机端发过来的，有时好象会带有换行符，所以要trim
                int returnId = Integer.parseInt(returnIds[i].trim());
                // 找到上一结点
                WorkflowActionDb wapriv = getWorkflowActionDb(returnId);
                // 更改上一结点的状态，置其为被打回
                wapriv.setReason(reason);
                wapriv.setStatus(STATE_RETURN);
                wapriv.save();

                // 打回时，需使与被返回节点相连的分支线上正处理的其它节点变为未处理状态，已处理的节点不变维持原状
                Vector v = wapriv.getLinkToActions();
                Iterator ir = v.iterator();
                while (ir.hasNext()) {
                    WorkflowActionDb wad = (WorkflowActionDb)ir.next();
                    if (wad.getId()!=wapriv.getId() && wad.getStatus()==WorkflowActionDb.STATE_DOING) {
                        wad.setStatus(WorkflowActionDb.STATE_NOTDO);
                        wad.save();
                    }
                }

                // 通知用户办理
                String[] users = StrUtil.split(wapriv.getUserName(), ",");
                int userslen = (users == null ? 0 : users.length);
                for (int n = 0; n < userslen; n++) {
                    MyActionDb mad = wf.notifyUser(users[n],
                                  new java.util.Date(), myActionId, this,
                                  wapriv, STATE_RETURN,
                                  (long) wf.getId());
                    Logger.getLogger(getClass()).info("beforeChangeStatus:" + wapriv.getUserRealName());
                    // wapriv.changeStatus(request, wf, checkUserName,
                    //                    wapriv.STATE_RETURN, "", wapriv.getResult(),
                    //                    wapriv.getResultValue());
                    addTmpUserNameActived(mad);
                }
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
     * workflow_action:410.000000,98.000000,490.000000,138.000000,,179,0,新用户16,2,174,,2,;
     * @param str String
     * @param isCheck boolean
     */
    public boolean fromString(String str, boolean isCheck) throws ErrMsgException {
        if (str.startsWith("workflow_start")) {
            isStart = 1;
        }
        else if (str.startsWith("workflow_action"))
            isStart = 0;
        else
            return false;
        int p = str.indexOf(":");
        int q = str.indexOf(";");
        if (p==-1 || q==-1) {
            Logger.getLogger(getClass()).info("fromString:格式错误！");
            return false;
        }

        try {
            str = str.substring(p + 1, q);
            // 注意当a,b,,,这样的字符串在split时的长度只为2，后面的,号分隔的并不被计入，而当a,b,,,s时，长度为5
            String[] ary = str.split("\\,");
            if (ary.length < STRING_ARRAY_LENGTH) {
                Logger.getLogger(getClass()).info("fromString:数组长度小于" + STRING_ARRAY_LENGTH + "！");
                return false;
            }
            title = tran(ary[4]);
            internalName = ary[5];
            userName = tran(ary[7]); // 当节点上定义的为“用户”型处理者时，表示动作处理者，当节点处理完后，也是存储动作处理者，两种情况都可能有多个
            String strstatus = ary[11];
            // System.out.println(str);
            // System.out.println("WorkflowActionDb " + ary[15] + " status=" + strstatus);
            status = Integer.parseInt(strstatus);
            reason = tran(ary[12]);

            userRealName = tran(ary[13]); // 用户真实姓名
            jobCode = tran(ary[14]); // 角色编码
            jobName = tran(ary[15]); // 角色名称
            // System.out.println("WorkflowActionDb direction=" + ary[16]);
            direction = WorkflowActionDb.DIRECTION_UP; // 行文方向，字段proxyJobCode
            if (StrUtil.isNumeric(ary[16]))
                direction = Integer.parseInt(ary[16]);
            // System.out.println("jobCode=" + jobCode);
            // System.out.println("jobName=" + jobName);
            // System.out.println("userRealName=" + userRealName);
            // System.out.println("ary[20]=" + ary[20]);
            rankCode = tran(ary[17]); // 职级编码，字段proxyJobName
            rankName = tran(ary[18]); // 职级名称，字段proxyUserName
            String strRelateRoleToOrganization = tran(ary[19]); // 角色与组织机构(行文方向)、职级、部门相关联，字段proxyUserRealName
            if (strRelateRoleToOrganization.equals("1"))
                relateRoleToOrganization = true;
            else
                relateRoleToOrganization = false;
            result = tran(ary[20]);
            fieldWrite = tran(ary[21]);

            // System.out.println("WorkflowActionDb officeColorIndex=" + officeColorIndex);
            officeColorIndex = 6; // red
            if (StrUtil.isNumeric(ary[22]))
                officeColorIndex = Integer.parseInt(ary[22]);

            dept = tran(ary[23]); // 20060701
            flag = tran(ary[24]);
            nodeMode = 0;
            String strDeptMode = tran(ary[25]);
            if (StrUtil.isNumeric(strDeptMode))
                nodeMode = Integer.parseInt(strDeptMode);
            if (ary.length>=27)
                strategy = tran(ary[26]);
            if (ary.length>=28)
                item1 = tran(ary[27]); // 是否为结束型节点
            // 格式为 { relateToAction , ignoreType , kind , fieldHide , isDelayed , timeDelayedValue , timeDelayedUnit , canPrivUserModifyDelayDate, formView };
            if (ary.length>=29)
                item2 = tran(ary[28]);
            if (ary.length>=33) {
                String strIsMsg = tran(ary[32]);
                if ("1".equals(strIsMsg)) {
                    msg = true;
                }
                else {
                    msg = false;
                }
            }
            else {
                msg = true;
                LogUtil.getLog(getClass()).error("取是否发送消息提醒失败，客户端版本低，需安装1, 3, 0, 0以上版本！");
            }

            // Logger.getLogger(getClass()).info("fromString:dept=" + dept);

            // 检查action是否合法
            if (isCheck) {
                WorkflowActionChecker wack = new WorkflowActionChecker();
                if (!wack.check(this))
                    throw new ErrMsgException(wack.getErrMsg());
            }
        }
        catch (Exception e) {
            Logger.getLogger(getClass()).error("fromString:" + e.getMessage());
            e.printStackTrace();
            throw new ErrMsgException(e.getMessage());
         }
        return true;
    }

    public boolean create() {
        Conn conn = new Conn(connname);
        this.id = (int) SequenceManager.nextID(SequenceManager.OA_WORKFLOW_ACTION);
        try {
            PreparedStatement pstmt = conn.prepareStatement(INSERT);
            pstmt.setInt(1, id);
            pstmt.setInt(2, flowId);
            pstmt.setInt(3, isStart);
            if (reason==null || reason.equals(""))
                reason = " "; // 适应SQLSERVER
            pstmt.setString(4, reason);
            pstmt.setInt(5, status);
            pstmt.setString(6, title);
            pstmt.setString(7, userName);
            pstmt.setString(8, internalName);
            pstmt.setInt(9, officeColorIndex);
            pstmt.setString(10, userRealName);
            pstmt.setString(11, jobCode);
            pstmt.setString(12, jobName);
            // System.out.println("create jobName=" + jobName);
            pstmt.setString(13, "" + direction);
            pstmt.setString(14, rankCode);
            pstmt.setString(15, rankName);
            pstmt.setString(16, relateRoleToOrganization?"1":"0");
            if (fieldWrite==null || fieldWrite.equals(""))
                fieldWrite = " "; // 适应SQLSERVER
            pstmt.setString(17, fieldWrite);
            pstmt.setInt(18, taskId);
            if (dept==null || dept.equals(""))
                dept = " "; // 适应SQLSERVER
            pstmt.setString(19, dept);
            pstmt.setString(20, flag);
            pstmt.setInt(21, nodeMode);
            pstmt.setTimestamp(22, new Timestamp((new java.util.Date()).getTime()));
            pstmt.setString(23, strategy);
            pstmt.setString(24, item1);
            pstmt.setString(25, item2);
            pstmt.setInt(26, msg?1:0);
            int r = conn.executePreUpdate();
            if (r==1)
                return true;
        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("create:" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return false;
    }

    public String tranReverse(String str) {
        if (str==null)
            return "";
        str = str.replaceAll(":", "\\\\colon");
        str = str.replaceAll(";", "\\\\semicolon");
        str = str.replaceAll(",", "\\\\comma");
        str = str.replaceAll("\r\n","\\\\newline");
        return str;
    }

    public String tran(String str) {
        if (str==null)
            return "";
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
            // Based on the id in the object, get the message data from the database.
            Conn conn = new Conn(connname);
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                //select id,flow_id,isStart,reason,status,title,userName from flow_action where id=?";
                pstmt = conn.prepareStatement(LOAD);
                pstmt.setInt(1, id);
                rs = conn.executePreQuery();
                if (!rs.next()) {
                    Logger.getLogger(getClass()).error("流程动作id= " + id +
                                 " 在数据库中未找到.");
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
                Logger.getLogger(getClass()).error("load:" + e.getMessage());
            } finally {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
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
    		+ hide + "," + delayed + "," + timeDelayedValue + "," + timeDelayedUnit + "," + canPrivUserModifyDelayDate + "," + formView + "}";
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
            Logger.getLogger(getClass()).error("del:" + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
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
            Logger.getLogger(getClass()).error("onWorkflowDiscarded:" + e.getMessage());
            return false;
        } finally {
            if (rs!=null) {
                try {rs.close();} catch (Exception e) {}
                rs = null;
            }
            if (pstmt!=null) {
                try {pstmt.close();} catch (Exception e) {}
                pstmt = null;
            }

            if (conn != null) {
                conn.close();
                conn = null;
            }
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
            Logger.getLogger(getClass()).error("onWorkflowManualFinish:" + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    /**
     * 取得流程中的所有action节点
     * @return Vector
     */
    public Vector getActionsOfFlow(int flowId) {
        Vector v = new Vector();
        String sql =
                "select id from flow_action where flow_id=?";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flowId);
            rs = conn.executePreQuery();
            if (rs == null) {
                Logger.getLogger(getClass()).info("getActions:流程id= " + id + " 中的动作在数据库中未找到.");
            } else {
                WorkflowActionMgr wam = new WorkflowActionMgr();
                while (rs.next()) {
                    int actionid = rs.getInt(1);
                    v.addElement(wam.getWorkflowActionDb(actionid));
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("getActions:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }


    /**
     * 取得流程中的所有被延时的action节点
     * @return Vector
     */
    public Vector getActionsDelayedOfFlow(int flowId) {
        Vector v = new Vector();
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
                Logger.getLogger(getClass()).info("getActionsDelayedOfFlow:流程id= " + id + " 中的动作在数据库中未找到.");
            } else {
                WorkflowActionMgr wam = new WorkflowActionMgr();
                while (rs.next()) {
                    int actionid = rs.getInt(1);
                    v.addElement(wam.getWorkflowActionDb(actionid));
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("getActions:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }


    /**
     * 取得流程中的所有结束节点
     * @return Vector
     */
    public Vector getEndActionsOfFlow(int flowId) {
        Vector v = new Vector();
        String sql =
                "select id from flow_action where flow_id=? and item1='1'";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flowId);
            rs = conn.executePreQuery();
            if (rs == null) {
                Logger.getLogger(getClass()).error("getEndActionsOfFlow:流程id= " + id + " 中的动作在数据库中未找到.");
            } else {
                WorkflowActionMgr wam = new WorkflowActionMgr();
                while (rs.next()) {
                    int actionid = rs.getInt(1);
                    v.addElement(wam.getWorkflowActionDb(actionid));
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("getEndActionsOfFlow:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
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
            if (checkDate == null)
                pstmt.setDate(16, null);
            else
                pstmt.setTimestamp(16, new Timestamp(checkDate.getTime()));
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

            if (dateDelayed==null)
                pstmt.setDate(28, null);
            else
                pstmt.setTimestamp(28, new Timestamp(dateDelayed.getTime()));

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
            e.printStackTrace();
            Logger.getLogger(getClass()).error("save:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
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
        else
            return false;
    }

    /**
     * 获取本人处理完毕的动作节点 wad 的下一个被激活的动作节点，且该节点的处理者仍是本人（或者是由本人代理），只获取满足条件的第一个节点
     * @param userName String
     * @return MyActionDb
     */
    public MyActionDb getNextActionDoingWillBeCheckedByUserSelf(String userName) {
        MyActionDb mad = new MyActionDb();
        mad = mad.getMyActionDbOfFlowDoingByUser(flowId, userName);
        return mad;
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
                isDelayed = StrUtil.getNullStr(fu.getFieldValue("isDelayed")).equals("1");
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
       if (deptOfUserWithMultiDept!=null && !deptOfUserWithMultiDept.equals("")) {
           dept = deptOfUserWithMultiDept;
           save();
       }

       // @task:当从子流程回到父流程时，父流程节点中如果用户多于1个，则会被自动忽略，因此必须唯一
       String[] users = StrUtil.split(nextwa.getUserName(), ",");

        // 如果未匹配到用户，则自动跳过该节点
        if (users == null) {
            WorkflowActionDb nwa = autoPassActionNoUserMatched(request, myActionId, this, nextwa, deptOfUserWithMultiDept);
            // 如果连续存在未匹配到用户的节点，则继续跳过
            while (nwa != null) {
                nwa = autoPassActionNoUserMatched(request, myActionId, this, nwa, deptOfUserWithMultiDept);
            }
        } else {
            if (nextwa.getKind()==WorkflowActionDb.KIND_SUB_FLOW) {
                // 创建子流程
                createSubFlow(request, wf, nextwa, this, myActionId);
            }
            else {
            	if (nextwa.isDistribute()) {
            		// 如果是分发节点，则置该节点状态为允许分发，用于公文分发列表
            		nextwa.setCanDistribute(true);
            	}
                nextwa.setStatus(STATE_DOING);
                nextwa.save();

                // 通知用户办理
                int len = users.length;
                for (int n = 0; n < len; n++) {
                    MyActionDb mad = wf.notifyUser(users[n],
                                                   new java.util.Date(),
                                                   myActionId, this,
                                                   nextwa, STATE_DOING,
                                                   (long) wf.getId());
                    addTmpUserNameActived(mad);
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
        Vector vfrom = nextwa.getLinkFromActions();
        // 如果下一结点不是聚合结点，则向下转交
        boolean re = true;
        if (vfrom.size() > 1) {
            // 如果下一结点是聚合结点，则如果为异或聚合节点时置其为DOING，否则若其所有连接from结点的状态为FINISHED或者IGNORED，则置其为DOING
            boolean flagXorAggregate = false;
            if (nextwa.getFlag().length() >= 8) {
                if (nextwa.getFlag().substring(7, 8).equals("1"))
                    flagXorAggregate = true;
            }
            Logger.getLogger(getClass()).info("canDevliveToNextAction: flagXorAggregate=" + flagXorAggregate);
            if (!flagXorAggregate) {
                Iterator fromir = vfrom.iterator();
                    while (fromir.hasNext()) {
                    WorkflowActionDb wfa = (WorkflowActionDb) fromir.next();
                    Logger.getLogger(getClass()).info("wfa.getJobName()=" + wfa.getJobName() + " status=" +
                                                      wfa.getStatusName());
                    if (wfa.getStatus() == WorkflowActionDb.STATE_FINISHED ||
                        wfa.getStatus() == WorkflowActionDb.STATE_IGNORED) {
                        ;
                    } else {
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
                if (flowAction.equals("toRetuner") && wpd.getReturnMode()==WorkflowPredefineDb.RETURN_MODE_TO_RETURNER) {
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
                    int len = users.length;
                    for (int n = 0; n < len; n++) {
                        mad = wf.notifyUser(users[n],
                                            new java.util.Date(), myActionId, this,
                                            nextwa, STATE_DOING,
                                            (long) wf.getId());
                        addTmpUserNameActived(mad);
                    }

                    return true;
                }
            }

            Logger.getLogger(getClass()).info("afterChangeStatus:" + getUserName() + " finished!");

            // 检查所有结束节点是否都已完成
            boolean isDoFinished = false;
            if (wf.getStatus()==WorkflowDb.STATUS_FINISHED) {
                isDoFinished = true;
            }
            else {
                // 如果所有结束节点都已完成
                if (wf.checkEndActionsStatusFinished()) {
                    // 如果是允许变更，而且流程以前曾经结束过，则只有当本节点为结束节点时，才可以结束流程
                    WorkflowPredefineDb wfp = new WorkflowPredefineDb();
                    wfp = wfp.getDefaultPredefineFlow(wf.getTypeCode());
                    if (wfp.isReactive()) {
                        if (wf.getEndDate() != null) {
                            if (isEnd()) {
                                doWorkflowFinished(request, wf);
                                isDoFinished = true;
                            }
                        }
                    } else {
                        doWorkflowFinished(request, wf);
                        isDoFinished = true;
                    }
                }
            }

            // 如果本结点为发散结点
            Vector v = getLinkToActions();

            LogUtil.getLog(getClass()).info("afterChangeStatus: v.size=" + v.size());

            if (v.size() > 1) {
                Iterator ir = v.iterator();
                ArrayList<WorkflowActionDb> selectedActions = new ArrayList<WorkflowActionDb>();
                while (ir.hasNext()) {
                	WorkflowActionDb nextwa = (WorkflowActionDb) ir.next();
                    nextwa = new WorkflowActionDb(nextwa.getId());
                    if (!nextwa.getUserName().equals("")) {
                    	selectedActions.add(nextwa);
                    	continue;
                    }
                }
                ir = v.iterator();
                while (ir.hasNext()) {
                    WorkflowActionDb nextwa = (WorkflowActionDb) ir.next();
                    // 因为在处理一条分支的时候，可能会对分支的汇聚节点产生影响，所以不能通过缓存取
                    nextwa = new WorkflowActionDb(nextwa.getId());
                    // 如果下一节点没有被跳过且不是正处理状态（因为其它分支可能会激活汇聚节点）
                    // 只有当某分支中间出现跳过的情况时，才会出现聚合节点被激活的情况
                    // 详见文档：《20120106大亚流程问题(聚合节点).doc》

                    LogUtil.getLog(getClass()).info("afterChangeStatus: nextwa.getStatus()=" + nextwa.getStatus() + " title=" + nextwa.getTitle() + " userName=" + nextwa.getUserName());

                    if (nextwa.getStatus()!=WorkflowActionDb.STATE_IGNORED && nextwa.getStatus()!=WorkflowActionDb.STATE_DOING) {
                        boolean isFinished = canDevliveToNextAction(nextwa);
                        LogUtil.getLog(getClass()).info("afterChangeStatus: " + nextwa.getTitle() + " jobName=" + nextwa.getJobName() + " isFinished=" + isFinished);
                        if (isFinished) {
                        	boolean toIgnore = false;
                        	if (!nextwa.getUserName().equals("") || selectedActions.isEmpty()) {
                        		deliverToNextAction(request, wf, nextwa, myActionId, getDeptOfUserWithMultiDept(request));
                        	} 
                        	else if (nextwa.getIgnoreType() == WorkflowActionDb.IGNORE_TYPE_USER_ACCESSED_BEFORE && nextwa.getUserName().equals("")) {
                        		// 无用户或用户之前处理过则跳过，当用户之前处理过时，在mactchActionUser时，会过滤掉已处理过的用户，action中的userName仍为空
                        		deliverToNextAction(request, wf, nextwa, myActionId, getDeptOfUserWithMultiDept(request));                        		
                        	}
                        	else if (nextwa.getIgnoreType() == WorkflowActionDb.IGNORE_TYPE_NOT && nextwa.getUserName().equals("")) {
                            	toIgnore = true;
                            } else  {
                        		toIgnore = true;
                        	}
                            if (toIgnore) {
                            	/*
                            	 * 有问题,后期调整
                        		for (WorkflowActionDb selectedAction : selectedActions) {
        	                    	WorkflowMgr wfm = new WorkflowMgr();
        	                    	WorkflowActionDb endAction = wfm.getRelationOfTwoActions(nextwa, selectedAction);
        	                    	wfm.ignoreBranch(nextwa, endAction);
                            	}*/
                            }
                        }
                    }
                }
            } else if (v.size() == 1) { // 如果本结点不是发散结点
                WorkflowActionDb nextwa = (WorkflowActionDb) v.get(0);
                boolean isFinished = canDevliveToNextAction(nextwa);
                LogUtil.getLog(getClass()).info("afterChangeStatus: isFinished=" + isFinished);
                if (isFinished) {
                    deliverToNextAction(request, wf, nextwa, myActionId, getDeptOfUserWithMultiDept(request));
                }
            } else if (v.size() == 0) {
                // 无下一结点，如果检查更改流程中的各个节点的状态都为已完成，则置流程状态为已完成
            	if (!isDoFinished) {
            	    // 如果流程未结束
                    if (wf.getStatus()!=WorkflowDb.STATUS_FINISHED) {
                        // 检查流程中的节点是否都已完成
                        if (wf.checkStatusFinished()) {
                            LogUtil.getLog(getClass()).info("afterChangeStatus: checkStatusFinished=");

                            doWorkflowFinished(request, wf);
                        }
                    }
            	}
            }

            // 策略检查

        } else if (newstatus == STATE_DOING && oldStatus == STATE_DOING && !userName.equals("")) {
        	String[] userNames = userName.split(",");
        	if (userNames.length <= 1) {
        		return true;
        	}
        	// @task:如果当非下达模式时，如上一节点有多人处理，有人先选择了下一节点的用户，而上一节点中后来审批的人去掉了勾选的用户，则此处会产生垃圾数据
        	// 所以不如将状态CHECK_STATUS_WAITING_TO_DO废除，且不生成“等待处理”的待办记录，意义似乎不是太大
            Vector v = getLinkToActions();
            if (v.size() == 1) { // 如果本结点不是发散结点
                WorkflowActionDb nextwa = (WorkflowActionDb) v.get(0);
                if (nextwa.getUserName().equals("")) {
                	return true;
                }
                userNames = nextwa.getUserName().split(",");
                for (String user : userNames) {
                	if (MyActionDb.isActionExist(nextwa.getId(), user) != 0) {
                		continue;
                	}
    	            MyActionDb md = new MyActionDb();
    	            md.setFlowId(nextwa.getFlowId());
    	            md.setActionId(nextwa.getId());
    	            md.setPrivMyActionId(myActionId);
    	            md.setUserName(user);
    	            md.create();
    	            md.setCheckStatus(MyActionDb.CHECK_STATUS_WAITING_TO_DO);
    	            md.save();
                }
            }
        }
        return true;
    }

    /**
     * 加签功能加入前备份
     * @param request HttpServletRequest
     * @param wf WorkflowDb
     * @param checkUserName String
     * @param oldStatus int
     * @param newstatus int
     * @param myActionId long
     * @return boolean
     */
    public boolean afterChangeStatusXXX(HttpServletRequest request, WorkflowDb wf, String checkUserName,
                                     int oldStatus, int newstatus, long myActionId) throws ErrMsgException  {
        // 如果状态被置为已完成
        if (newstatus == STATE_FINISHED) {
            // 如果之前的状态为被返回
            if (oldStatus==WorkflowActionDb.STATE_RETURN) {
                // 激活打回的节点
                WorkflowPredefineDb wpd = new WorkflowPredefineDb();
                wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());

                String flowAction = StrUtil.getNullStr((String)request.getAttribute("flowAction"));

                // 如果返回后再交办时直接至返回者(直送)
                if (flowAction.equals("toRetuner") && wpd.getReturnMode()==WorkflowPredefineDb.RETURN_MODE_TO_RETURNER) {
                    MyActionDb mad = new MyActionDb();
                    mad = mad.getMyActionDb(myActionId);
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
                    int len = users.length;
                    for (int n = 0; n < len; n++) {
                        mad = wf.notifyUser(users[n],
                                            new java.util.Date(), myActionId, this,
                                            nextwa, STATE_DOING,
                                            (long) wf.getId());
                        addTmpUserNameActived(mad);
                    }

                    return true;
                }
            }

            Logger.getLogger(getClass()).info("afterChangeStatus:" + getUserName() + " finished!");
            // 检查所有结束节点是否都已完成
            if (wf.checkEndActionsStatusFinished()) {
                doWorkflowFinished(request, wf);
            }

            // 如果本结点为发散结点
            Vector v = getLinkToActions();
            if (v.size() > 1) {
                Iterator ir = v.iterator();
                while (ir.hasNext()) {
                    WorkflowActionDb nextwa = (WorkflowActionDb) ir.next();
                    // 如果下一节点没有被跳过
                    if (nextwa.getStatus()!= STATE_IGNORED) {
                        Vector vfrom = nextwa.getLinkFromActions();
                        // 如果下一结点不是聚合结点
                        if (vfrom.size() == 1) {
                           // 如果节点类别是子流程
                           if (nextwa.getKind()==WorkflowActionDb.KIND_SUB_FLOW) {
                               // 创建子流程
                               createSubFlow(request, wf, nextwa, this, myActionId);
                           }
                           else {
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
                                   WorkflowActionDb nwa = autoPassActionNoUserMatched(request, myActionId, this, nextwa, getDeptOfUserWithMultiDept(request));
                                   // 如果连续存在未匹配到用户的节点，则继续跳过
                                   while (nwa != null) {
                                       nwa = autoPassActionNoUserMatched(request, myActionId, this, nwa, getDeptOfUserWithMultiDept(request));
                                   }
                               }
                           }
                        }
                        // 如果下一结点是聚合结点，则如果为异步聚合节点时置其为DOING，否则若其所有连接from结点的状态为FINISHED或者IGNORED，则置其为DOING
                        else {
                            boolean isFinished = true;
                            boolean flagXorAggregate = false;
                            if (nextwa.getFlag().length() >= 8) {
                                if (nextwa.getFlag().substring(7, 8).equals("1"))
                                    flagXorAggregate = true;
                            }
                            Logger.getLogger(getClass()).info("flagXorAggregate=" + flagXorAggregate);
                            if (!flagXorAggregate) {
                                Iterator fromir = vfrom.iterator();
                                while (fromir.hasNext()) {
                                    WorkflowActionDb wfa = (WorkflowActionDb) fromir.next();
                                    Logger.getLogger(getClass()).info("wfa.getJobName()=" + wfa.getJobName() + " status=" + wfa.getStatusName());
                                    if (wfa.getStatus() == STATE_FINISHED ||
                                        wfa.getStatus() == STATE_IGNORED)
                                        ;
                                    else {
                                        isFinished = false;
                                        break;
                                    }
                                }
                            }
                            if (isFinished) {
                                // 匹配节点上的用户
                                /*
                                Vector vt = null;
                                try {
                                    String deptOfUserWithMultiDept = getDeptOfUserWithMultiDept(request);
                                    vt = nextwa.matchActionUser(nextwa, this, false, deptOfUserWithMultiDept);
                                } catch (ErrMsgException ex1) {
                                    ex1.printStackTrace();
                                }
                                catch (MatchUserException e) {
                                    e.printStackTrace();
                                    throw new ErrMsgException(e.getTypeDesc());
                                }

                                Logger.getLogger(getClass()).info("afterChangeStatus:vt.size()=" + vt.size());
                                */
                               // 如果节点类别是子流程

                               String[] users = StrUtil.split(nextwa.getUserName(), ",");

                               // 如果未匹配到用户，则自动跳过该节点
                               // if (vt.size() == 0) {
                               if (users == null) {
                                   WorkflowActionDb nwa = autoPassActionNoUserMatched(request, myActionId, this, nextwa, getDeptOfUserWithMultiDept(request));
                                   // 如果连续存在未匹配到用户的节点，则继续跳过
                                   while (nwa != null) {
                                       nwa = autoPassActionNoUserMatched(request, myActionId, this, nwa, getDeptOfUserWithMultiDept(request));
                                   }
                               } else {
                                   if (nextwa.getKind()==WorkflowActionDb.KIND_SUB_FLOW) {
                                       // 创建子流程
                                       createSubFlow(request, wf, nextwa, this, myActionId);
                                   }
                                   else {
                                       nextwa.setStatus(STATE_DOING);
                                       nextwa.save();

                                       // 通知用户办理
                                       /*
                                        String[] users = StrUtil.split(nextwa.getUserName(), ",");
                                                                            if (users == null) {
                                           throw new ErrMsgException("请选择节点" +
                                                   nextwa.getJobName() + "上的用户！");
                                                                            }
                                        */
                                       int len = users.length;
                                       for (int n = 0; n < len; n++) {
                                           MyActionDb mad = wf.notifyUser(users[n],
                                                                          new java.util.Date(),
                                                                          myActionId, this,
                                                                          nextwa, STATE_DOING,
                                                                          (long) wf.getId());
                                           // nextwa.changeStatus(request, wf, checkUserName,
                                           //                     nextwa.STATE_DOING, "", "",
                                           //                     nextwa.getResultValue());
                                           addTmpUserNameActived(mad);
                                       }
                                   }
                               }
                            }
                        }
                    }
                }
            } else if (v.size() == 1) { // 如果本结点不是发散结点
                WorkflowActionDb nextwa = (WorkflowActionDb) v.get(0);

                // 匹配节点上的用户，不能用matchActionUser，因为当“自选用户”时，matchActionUser返回为空，这样自选节点会被自动跳过
                /*
                Vector vt = null;
                try {
                    String deptOfUserWithMultiDept = getDeptOfUserWithMultiDept(request);
                    vt = nextwa.matchActionUser(nextwa, this, false, deptOfUserWithMultiDept);
                } catch (ErrMsgException ex1) {
                    ex1.printStackTrace();
                }
                catch (MatchUserException e) {
                    e.printStackTrace();
                    throw new ErrMsgException(e.getTypeDesc());
                }

                Logger.getLogger(getClass()).info("afterChangeStatus: v.size()=1 vt.size()=" + vt.size());
                */

               String[] users = StrUtil.split(nextwa.getUserName(), ",");

                // 如果未匹配到用户，则自动跳过该节点
                // if (vt.size()==0) {
                if (users==null) {
                    WorkflowActionDb nwa = autoPassActionNoUserMatched(request, myActionId, this, nextwa, getDeptOfUserWithMultiDept(request));
                    // 如果连续存在未匹配到用户的节点，则继续跳过
                    while (nwa!=null) {
                        nwa = autoPassActionNoUserMatched(request, myActionId, this, nwa, getDeptOfUserWithMultiDept(request));
                    }
                }
                else {
                    // Logger.getLogger(getClass()).info("afterChangeStatus:" + getJobName() + " 下一结点 " + nextwa.getJobName() + " 不是聚合结点");
                    // 如果下一结点不是聚合结点
                    Vector vfrom = nextwa.getLinkFromActions();
                    if (vfrom.size() == 1) {
                        // 如果节点类别是子流程
                        if (nextwa.getKind() == WorkflowActionDb.KIND_SUB_FLOW) {
                            // 创建子流程
                            LogUtil.getLog(getClass()).info("nextwa internalName=" + nextwa.getInternalName() + " this.getInternalName=" + this.getInternalName() + " myActionId" + myActionId);
                            createSubFlow(request, wf, nextwa, this, myActionId);
                        }
                        else {
                            // Logger.getLogger(getClass()).info(nextwa.getUserName() + " is not an aggrevate node.");
                            nextwa.changeStatus(request, wf, checkUserName,
                                                WorkflowActionDb.STATE_DOING,
                                                "", "", nextwa.getResultValue(),
                                                myActionId);
                            // 通知用户办理
                            // String[] users = StrUtil.split(nextwa.getUserName(),  ",");
                            int len = users.length;
                            for (int n = 0; n < len; n++) {
                                MyActionDb mad = wf.notifyUser(users[n],
                                                               new java.util.Date(), myActionId, this,
                                                               nextwa, STATE_DOING,
                                                               (long) wf.getId());
                                addTmpUserNameActived(mad);
                            }
                        }
                        // return true;
                    }
                    // 如果下一结点是聚合结点，则如果为异步节点时置其为DOING，否则其所有连接from结点的状态为FINISHED或者IGNORED，则置其为DOING
                    else {
                        boolean isFinished = true;
                        boolean flagXorAggregate = false;
                        if (nextwa.getFlag().length() >= 8) {
                            if (nextwa.getFlag().substring(7, 8).equals("1"))
                                flagXorAggregate = true;
                        }
                        // Logger.getLogger(getClass()).info("afterChangeStatus:flagXorAggregate=" + flagXorAggregate);
                        if (!flagXorAggregate) {
                            Iterator ir = vfrom.iterator();
                            while (ir.hasNext()) {
                                WorkflowActionDb wfa = (WorkflowActionDb) ir.
                                        next();
                                Logger.getLogger(getClass()).info(
                                        "afterChangeStatus: userRealName=" +
                                        wfa.getUserRealName() + " jobName=" +
                                        wfa.getJobName() + " status=" +
                                        wfa.getStatusName());
                                if (wfa.getStatus() == wfa.STATE_FINISHED ||
                                    wfa.getStatus() == wfa.STATE_IGNORED)
                                    ;
                                else {
                                    isFinished = false;
                                    break;
                                }
                            }
                        }
                        // Logger.getLogger(getClass()).info("afterChangeStatus:isFinished=" + isFinished);
                        if (isFinished) {
                            // 如果节点类别是子流程
                            if (nextwa.getKind() == WorkflowActionDb.KIND_SUB_FLOW) {
                                // 创建子流程
                                createSubFlow(request, wf, nextwa, this, myActionId);
                            }
                            else {
                                nextwa.setStatus(STATE_DOING);
                                nextwa.save();
                                // 通知用户办理
                                Logger.getLogger(getClass()).info(
                                        "afterChangeStatus:通知用户" +
                                        nextwa.getUserName() + "办理");
                                // String[] users = StrUtil.split(nextwa.getUserName(), ",");
                                int len = users.length;
                                for (int n = 0; n < len; n++) {
                                    MyActionDb mad = wf.notifyUser(users[n],
                                                                   new java.util.Date(), myActionId, this,
                                                                   nextwa, STATE_DOING,
                                                                   (long) wf.getId());
                                    // nextwa.changeStatus(request, wf, checkUserName,
                                    //                     nextwa.STATE_DOING, "", "",
                                    //                     nextwa.getResultValue());
                                    addTmpUserNameActived(mad);
                                }
                            }
                        }
                    }
                }
            } else if (v.size() == 0) {
                // 无下一结点，如果检查更改流程中的各个节点的状态都为已完成，则置流程状态为已完成
                // 检查流程中的节点是否都已完成
                if (wf.checkStatusFinished()) {
                    doWorkflowFinished(request, wf);
                }
            }
        }
        return true;
    }

    /**
     * 跳过未匹配到用户的节点，如果是发散节点，条件匹配成功则继续，否则则无法跳过，返回null
     * 如果在本方法中匹配到用户，则激活该节点
     * @param myActionId long 正在处理的MyActionDb的ID
     * @param myAction WorkflowActionDb 当前节点，对应myActionId处理的节点
     * @param nextActionToPass WorkflowActionDb 下一个将被跳过的节点
     * @return WorkflowActionDb curAction的下一节点如果匹配不到用户，则返回该节点，否则返回null
     */
    public WorkflowActionDb autoPassActionNoUserMatched(HttpServletRequest request, long myActionId, WorkflowActionDb myAction, WorkflowActionDb nextActionToPass, String deptOfUserWithMultiDept) throws ErrMsgException {
        // 如果不允许跳过
        if (nextActionToPass.getIgnoreType()==IGNORE_TYPE_NOT)
            return null;

        LogUtil.getLog(getClass()).info("autoPassActionNoUserMatched:" + myActionId);

        // 置待办记录对应的action的状态为被忽略
        nextActionToPass.setStatus(WorkflowActionDb.STATE_IGNORED);
        try {
            nextActionToPass.save();
        } catch (ErrMsgException ex3) {
            ex3.printStackTrace();
        }
                
        // 如果是结束节点，则在自动跳过时，使流程状态变为已完成
        if ("1".equals(nextActionToPass.getItem1())) {
        	// 這裏不需要再checkEndActionsStatusFinished，如果多起點的話，這樣會因爲被忽略的分支線上的結束節點，使得流程變爲結束狀態
        	WorkflowDb wf = new WorkflowDb();
        	wf = wf.getWorkflowDb(nextActionToPass.getFlowId());
	        doWorkflowFinished(request, wf);
        }

        // 取得后续节点，如果有多条分支，则退出
        // 如果是发散节点，带有条件分支，则如果跳过该节点时，匹配到了分支线（条件分支有且仅有一条符合条件，不会出现多条的情况），则
        Vector v = nextActionToPass.getLinkToActions();
        if (v.size() > 1) {
            // return null;
            LogUtil.getLog(getClass()).info("autoPassActionNoUserMatched:v.size=" + v.size());
            // 如果是异或发散节点，则先匹配分支
            // if (myAction.isXorRadiate()) {
            if (nextActionToPass.isXorRadiate()) {
                Vector vMatched = matchNextBranch(nextActionToPass, myAction.getUserName(),
                                                     new StringBuffer(), myActionId);
                // LogUtil.getLog(getClass()).info("autoPassActionNoUserMatched:wld=" + wld);
                if (vMatched.size()>0) {
                    WorkflowMgr wfm = new WorkflowMgr();
                    Iterator irMatched = vMatched.iterator();
                    while (irMatched.hasNext()) {
                        WorkflowLinkDb wld = (WorkflowLinkDb)irMatched.next();
                        // 置分支线
                        wfm.setXorRadiateNextBranch(nextActionToPass, wld.getTo());

                        WorkflowActionDb toBranchAct = wld.getToAction();
                        LogUtil.getLog(getClass()).info(
                                "autoPassActionNoUserMatched:toBranchAct=" +
                                toBranchAct.getTitle());
                        v.clear();
                        v.addElement(toBranchAct);
                        // 匹配到了分支线才允许跳过
                        // return vt;
                    }
                } else
                    return null;
            }
            else {

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

        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            WorkflowActionDb nextwa = (WorkflowActionDb) ir.next();

            // @task:待测，检查后续节点的入度，如果为非异或聚合，则检查聚合节点前面的节点是否均已办理完毕
            Vector vfrom = nextwa.getLinkFromActions();
            boolean isFinished = true;
            boolean flagXorAggregate = false;
            if (nextwa.getFlag().length() >= 8) {
                if (nextwa.getFlag().substring(7, 8).equals("1"))
                    flagXorAggregate = true;
            }

            Logger.getLogger(getClass()).info("autoPassActionNoUserMatched:flagXorAggregate=" + flagXorAggregate);

            if (!flagXorAggregate) {
                Iterator irFrom = vfrom.iterator();
                while (irFrom.hasNext()) {
                    WorkflowActionDb wfa = (WorkflowActionDb) irFrom.next();
                    if (wfa.getStatus() == WorkflowActionDb.STATE_FINISHED || wfa.getStatus() == WorkflowActionDb.STATE_IGNORED)
                        ;
                    else {
                        isFinished = false;
                        break;
                    }
                }
            }

            Logger.getLogger(getClass()).info("autoPassActionNoUserMatched:isFinished=" + isFinished);

            if (isFinished) {
                // 匹配节点上的用户
                // Vector vt = nextwa.matchActionUser(nextwa, nextActionToPass, false);
                
            	// 20161216 fgf 将nextwa.matchActionPassedNextUser改为matchActionUser
            	// Vector vt = nextwa.matchActionPassedNextUser(nextwa, myAction, deptOfUserWithMultiDept);
                Vector vt = null;
                try {
					vt = nextwa.matchActionUser(request, nextwa, myAction, false, deptOfUserWithMultiDept);
				} catch (MatchUserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new ErrMsgException(e.getMessage());
				}

                // 如果未匹配到用户，则返回
                // 如果同时激活多个后续节点（待跳过节点为发散节点），则如遇到某一个用户匹配不到的情况，则会继续向后匹配，而其它节点却不会被激活
                if (vt.size()==0)
                    return nextwa;
                
                if (vt.size() == 1) {
                	UserDb ud = (UserDb) vt.get(0);
                	if (ud.getName().equals(PRE_TYPE_USER_SELECT)) {
                		return nextwa;
                	}
                }

                // 如果有用户，则激活后续节点
                nextwa.setStatus(WorkflowActionDb.STATE_DOING);
                try {
                    nextwa.save();
                } catch (ErrMsgException ex) {
                    ex.printStackTrace();
                }

                String t = SkinUtil.LoadString(null, "res.module.flow", "msg_user_actived_title");
                String c = SkinUtil.LoadString(null, "res.module.flow", "msg_user_actived_content");

                WorkflowDb wf = new WorkflowDb();
                wf = wf.getWorkflowDb(nextActionToPass.getFlowId());

                t = t.replaceFirst("\\$flowTitle", wf.getTitle());
                c = c.replaceFirst("\\$flowTitle", wf.getTitle());
                c = c.replaceFirst("\\$fromUser", myAction.getUserRealName());
                String c_sms = c;

                c += WorkflowMgr.getFormAbstractTable(wf);

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

                Iterator ir2 = vt.iterator();
                while (ir2.hasNext()) {
                    UserDb user = (UserDb) ir2.next();
                    if (!user.isLoaded()) {
                    	continue;
                    }
                    MyActionDb nextMad = wf.notifyUser(user.getName(), new java.util.Date(),
                                  myActionId, nextActionToPass,
                                  nextwa,
                                  WorkflowActionDb.STATE_DOING,
                                  (long) wf.getId());
                    // 发送给后续节点通知处理短信
                    try {
                        MessageDb md = new MessageDb();
                        String action = "action=" +
                                        MessageDb.ACTION_FLOW_DISPOSE +
                                        "|myActionId=" + nextMad.getId();
                        if (imsg != null)
                            imsg.sendSysMsg(user.getName(), t, c_sms, action);
                        else {
                            md.sendSysMsg(user.getName(), t, c, action);
                        }

                        if (flowNotifyByEmail) {
                            com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();

                            UserMgr um = new UserMgr();
                            user = um.getUserDb(nextMad.getUserName());
                            if (!user.getEmail().equals("")) {
                                action = "userName=" + user.getName() + "|" + "myActionId=" + nextMad.getId();
                                action = cn.js.fan.security.ThreeDesUtil.
                                         encrypt2hex(
                                                 ssoCfg.getKey(), action);
                                String fc = c + "<BR />>>&nbsp;<a href='" +
                                        Global.getFullRootPath(request) +
                                        "/public/flow_dispose.jsp?action=" +
                                        action +
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
        return null;
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
            Logger.getLogger(getClass()).info("afterChangeStatus:" + getUserName() + " finished!");
            // 检查所有结束节点是否都已完成
            if (wf.checkEndActionsStatusFinished()) {
                wf.changeStatus(request, WorkflowDb.STATUS_FINISHED, this);
            }

            Vector v = getLinkToActions();
            // 如果本结点有后继节点
            if (v.size() > 0) {
                Iterator ir = v.iterator();
                while (ir.hasNext()) {
                    WorkflowActionDb nextwa = (WorkflowActionDb) ir.next();
                    // 判断能否转交至下一节点
                    if (!canDevliveToNextAction(nextwa))
                        continue;

                    nextwa.changeStatus(request, wf, checkUserName,
                                        WorkflowActionDb.STATE_DOING, "", "",
                                        nextwa.getResultValue(), myActionId);
                    // 通知用户办理
                    String[] users = StrUtil.split(nextwa.getUserName(),
                            ",");
                    int len = (users == null ? 0 : users.length);
                    for (int n = 0; n < len; n++) {
                        MyActionDb mad = wf.notifyUser(users[n],
                                      new java.util.Date(), myActionId, this,
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
     * 在本动作状态被置为FINISHED后激发，节点存在有ignore状态
     * @return boolean
     */
    /*
    public boolean afterChangeStatus(HttpServletRequest request, WorkflowDb wf, String checkUserName,
                                     int newstatus) throws ErrMsgException  {
        // 如果状态被置为已完成
        if (newstatus == STATE_FINISHED) {
            // Logger.getLogger(getClass()).info(getUserName() + "finished!");
            // 如果本结点为发散结点，则置所有的未被跳过的to结点为DOING
            Vector v = getLinkToActions();
            if (v.size() > 1) {
                Iterator ir = v.iterator();
                while (ir.hasNext()) {
                    WorkflowActionDb wa = (WorkflowActionDb) ir.next();
                    wa.changeStatus(request, wf, checkUserName, wa.STATE_DOING, "", "",
                                    wa.getResultValue());
                    addTmpUserNameActived(wa.getUserName());
                }
            } else if (v.size() == 1) {
                // 如果本结点不是发散结点
                WorkflowActionDb nextwa = (WorkflowActionDb) v.get(0);
                // Logger.getLogger(getClass()).info(nextwa.getUserName() + "is the next.");
                // 如果下一结点不是聚合结点，且未被跳过则置下一结点为DOING
                // 找到下一未被跳过结点
                int status = nextwa.getStatus();
                // 如果下一结点状态为被跳过，则继续往前寻找未被跳过结点
                while (nextwa.getStatus() == STATE_IGNORED) {
                    // 当nextwa的status为STATE_IGNORED时，结点不可能是发散结点
                    Vector vto = nextwa.getLinkToActions();
                    //if (vto.size() > 1)
                    //    break;
                    //else {
                    nextwa = (WorkflowActionDb) vto.get(0);
                    //}
                }
                // 如果下一结点不是聚合结点
                Vector vnext = nextwa.getLinkFromActoins();
                if (vnext.size() == 1) {
                    // Logger.getLogger(getClass()).info(nextwa.getUserName() + " is not an aggrevate node.");
                    nextwa.changeStatus(request, wf, checkUserName, nextwa.STATE_DOING,
                                        "", "", nextwa.getResultValue());
                    addTmpUserNameActived(nextwa.getUserName());
                    return true;
                }
                // 如果下一结点是聚合结点，则如果其所有连接from结点的状态为FINISHED或IGNORED，则置其为DOING
                if (vnext.size() > 1) {
                    boolean isFinished = true;
                    Iterator ir = vnext.iterator();
                    while (ir.hasNext()) {
                        WorkflowActionDb wfa = (WorkflowActionDb) ir.next();
                        if (wfa.getStatus() == wfa.STATE_FINISHED ||
                            wfa.getStatus() == wfa.STATE_IGNORED)
                            ;
                        else {
                            isFinished = false;
                            break;
                        }
                    }
                    if (isFinished) {
                        nextwa.changeStatus(request, wf, checkUserName,
                                            nextwa.STATE_DOING, "", "",
                                            nextwa.getResultValue());
                        addTmpUserNameActived(nextwa.getUserName());
                    }
                }
            } else if (v.size() == 0) {
                // 无下一结点，如果检查更改流程中的各个节点的状态都为已完成，则置流程状态为已完成
                WorkflowDb wfd = new WorkflowDb();
                wfd = wfd.getWorkflowDb(flowId);
                // 检查流程中的节点是否都已完成
                if (wf.checkStatusFinished()) {
                    wfd.changeStatus(wfd.STATUS_FINISHED, this);
                }
            }
        }
        return true;
    }
*/

    /**
     * 取得从本动作指向的所有动作，不包含被打回的动作，按节点上第一个角色的大小排序，从大至小
     * @return Vector
     */
    public Vector getLinkToActions() {
        Vector ret = new Vector();
        String sql = "select action_to from flow_link where action_from=? and flow_id=? and type<>" + WorkflowLinkDb.TYPE_RETURN;
        // Based on the id in the object, get the message data from the database.
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
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
            Logger.getLogger(getClass()).error("getLinkToActoins:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (ret.size()>1) {
            Comparator ct = new WorkflowActionComparator();
            Collections.sort(ret, ct);
        }
        return ret;
    }

    public Vector getLinkReturnActions() {
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);
        Leaf lf = new Leaf();
        lf = lf.getLeaf(wf.getTypeCode());

        Vector ret = new Vector();
        if (lf.getType() == Leaf.TYPE_FREE) {
            String sql =
                    "select id from flow_link where action_to=? and flow_id=?";
            LogUtil.getLog(getClass()).info("getLinkReturnActions:" + sql + " " + flowId + " " + internalName);
            Conn conn = new Conn(connname);
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, internalName);
                pstmt.setInt(2, flowId);
                rs = conn.executePreQuery();
                if (rs != null) {
                    while (rs.next()) {
                        WorkflowLinkDb wld = new WorkflowLinkDb();
                        wld = wld.getWorkflowLinkDb(rs.getInt(1));

                        ret.addElement(getWorkflowActionDbByInternalName(
                                wld.getFrom(), flowId));
                    }
                }
            } catch (SQLException e) {
                Logger.getLogger("getLinkReturnActions:" +
                        getClass()).error(e.getMessage());
            } finally {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
        } else {
            String sql =
                    "select id from flow_link where ((action_from=? and type=" +
                    WorkflowLinkDb.TYPE_RETURN + ") or (action_to=? and type=" +
                    WorkflowLinkDb.TYPE_BOTH + ")) and flow_id=?";
            Conn conn = new Conn(connname);
            PreparedStatement pstmt = null;
            ResultSet rs = null;
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
                        if (wld.getType() == wld.TYPE_BOTH)
                            ret.addElement(getWorkflowActionDbByInternalName(
                                    wld.getFrom(),
                                    flowId));
                        else
                            ret.addElement(getWorkflowActionDbByInternalName(
                                    wld.getTo(), flowId));
                    }
                }
            } catch (SQLException e) {
                Logger.getLogger("getLinkReturnActions:" +
                        getClass()).error(e.getMessage());
            } finally {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
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
            if (wa.getStatus()==STATE_FINISHED || wa.getStatus()==STATE_RETURN)
                count ++;
        }
        return count;
    }

    /**
     * 取得指向本动作的所有动作，不含打回动作连线的节点
     * @return Vector
     */
    public Vector getLinkFromActions() {
        Vector ret = new Vector();
        String sql = "select action_from from flow_link where action_to=? and flow_id=? and type<>" + WorkflowLinkDb.TYPE_RETURN;
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        // WorkflowActionMgr wfam = new WorkflowActionMgr();
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, internalName);
            pstmt.setInt(2, flowId);
            rs = conn.executePreQuery();
            if (rs!=null) {
                while (rs.next()) {
                    String from = rs.getString(1);
                    ret.addElement(getWorkflowActionDbByInternalName(from,
                            flowId));
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass()).error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
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
        PreparedStatement pstmt = null;
        ResultSet rs = null;
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
            Logger.getLogger(getClass()).error("getWorkflowActionDbByTitle:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return null;
    }
    public WorkflowActionDb getWorkflowActionDbByInternalName(String internalName, int flowid) {
        String sql = "select id from flow_action where flow_id=? and internal_name=?";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
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
            Logger.getLogger(getClass()).error("getWorkflowActionDbByInternalName:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
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
            Logger.getLogger(getClass()).error("listCheckedActionOfFlow:" + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    /**
     * 取得流程中的开始节点
     * @param flowId int 流程ID
     * @return WorkflowActionDb
     */
    public WorkflowActionDb getStartAction(int flowId) {
        Vector v = new Vector();
        String sql = "select id from flow_action where flow_id=? and isStart=1 and status<>" + STATE_IGNORED;
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // select id,flow_id,isStart,reason,status,title,userName from flow_action where id=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flowId);
            rs = conn.executePreQuery();
            if (rs.next()) {
                return getWorkflowActionDb(rs.getInt(1));
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("listCheckedActionOfFlow:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
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
            Logger.getLogger(getClass()).error("listCheckedActionOfFlow:" + e.getMessage());
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

    private Vector tmpUserNameActived;

    public Vector getTmpUserNameActived() {
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
        if (rds != null)
            len = rds.length;
        else
            return r;
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
        if (nextAction.getNodeMode() == nextAction.NODE_MODE_USER ||
            nextAction.getNodeMode() == nextAction.NODE_MODE_USER_SELECTED) {
            return false;
        }

        // 只处理当前节点只有一个用户，后继节点上只有一个角色的情况
        if (myAction.getJobCode().indexOf(",") == -1) {
            if (nextAction.getJobCode().indexOf(",") == -1) {
                RoleDb nextrd = new RoleDb();
                nextrd = nextrd.getRoleDb(nextAction.getJobCode());

                LogUtil.getLog(getClass()).info("compareRoleOfAction:" + nextAction.getJobName());

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
                        if (nextAction.getLinkToActions().size()==1)
                            return true;
                    }
                } else {
                    RoleDb currd = nextrd.getRoleDb(myAction.getJobCode());
                    // 本节点及下一节点是普通节点才能跳过
                    if (currd.getType() == RoleDb.TYPE_NORMAL &&
                        nextrd.getType() == RoleDb.TYPE_NORMAL) {
                        if (currd.getOrders() > nextrd.getOrders())
                            return true;
                    }
                }
            }
        }

        LogUtil.getLog(getClass()).info("compareRoleOfAction:" + nextAction.getJobName() + " false");

        return false;
    }

    /**
     * 20161216 此方法已弃用，用matchActionUser即可，没必要再多加一个方法
     * 匹配当跳越节点时，下一节点上满足条件的用户，注意当跳过多个节点时，只在本部门和上级部门两级中匹配人员
     * 用于autoPassActionNoUserMatched方法中
     * @task:需优化，当跳越至某节点，且该节点是关联至发起节点，应该按matchActioUser上行规则匹配，如果不行，则需明确跳过规则优先
     * 当下一节点上未设行文方向与组织机构关联时，直接取得符合条件的人员
     * 当下一节点上设置了关联时，则将myAction的本部门及上级部门加入至人员过滤条件中，因此至多只能跳过一级部门
     * @param nextAction WorkflowActionDb 下一节点
     * @param myAction WorkflowActionDb 跳越节点时的起点，注意匹配时可能会跳过多个节点，这里起点是指第一个被跳节点之前的那个节点
     * @param deptOfUserWithMultiDept  String 当处于多个部门时所选择的部门，如果为null，则表示当前处理人员不处于多个部门
     * @return Vector
     */
    public Vector matchActionPassedNextUser(WorkflowActionDb nextAction, WorkflowActionDb myAction, String deptOfUserWithMultiDept) throws ErrMsgException {
        Vector vt = new Vector();

        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(myAction.getFlowId());
        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());

        // System.out.println(getClass() + " wpd.getRoleRankMode()=" + wpd.getRoleRankMode() + " " + myAction.getJobCode() + " " + nextAction.getJobCode());

        // 判断是否需根据角色职级跳过
        if (wpd.getRoleRankMode() == WorkflowPredefineDb.ROLE_RANK_NEXT_LOWER_JUMP) {
        	// 如果角色比较大小时允许跳过
        	if (nextAction.getIgnoreType()!=IGNORE_TYPE_ROLE_COMPARE_NOT) {        	
	            // 检查nextAction是否为发散节点，如果是，则不允许跳过
	            // Vector toV = nextAction.getLinkToActions();
	            // 如果不是发散节点
	            // if (toV.size()==1) {
	            if (nextAction.getRelateToAction().equals(RELATE_TO_ACTION_STARTER)) {
	                WorkflowActionDb startAction = getStartAction(nextAction.getFlowId());
	                if (compareRoleOfAction(nextAction, startAction))
	                    return vt;
	            }
	            else {
	                if (compareRoleOfAction(nextAction, myAction))
	                    return vt;
	            }
	            // }
        	}
        }

        UserMgr um = new UserMgr();

        if (nextAction.getNodeMode() == WorkflowActionDb.NODE_MODE_ROLE_SELECTED ||
            nextAction.getNodeMode() == WorkflowActionDb.NODE_MODE_USER_SELECTED) {
            int nextActionFromCount = 0;
            Vector fromV = nextAction.getLinkFromActions();
            // 如果是汇聚节点
            if (fromV.size() > 1) {
                // 统计入度
                Iterator fromIr = fromV.iterator();
                while (fromIr.hasNext()) {
                    WorkflowActionDb fromAction = (WorkflowActionDb) fromIr.next();
                    if (fromAction.getStatus() != WorkflowActionDb.STATE_IGNORED)
                        nextActionFromCount++;
                }

                // 如果节点的入度（不含来自被忽略的节点）大于1，而节点已经设置或者匹配好了人员，则不允许多次选择用户
                if (nextActionFromCount > 1 && !nextAction.getUserName().equals("")) {
                    String[] users = StrUtil.split(nextAction.getUserName(), ",");
                    int len = users.length;
                    for (int i=0; i<len; i++) {
                        vt.addElement(um.getUserDb(users[i]));
                    }
                    return vt;
                }
            }

            // 在changeStatus中，myAction的状态已被更新，所以这里不会再遇到状态为STATE_RETURN的情况
            // @task:如果curAction是被打回的，则也不允许多次选择用户
            if (myAction.getStatus() == WorkflowActionDb.STATE_RETURN) {
                if (!nextAction.getUserName().equals("")) {
                    String[] users = StrUtil.split(nextAction.getUserName(), ",");
                    int len = users.length;
                    for (int i=0; i<len; i++) {
                        vt.addElement(um.getUserDb(users[i]));
                    }
                    return vt;
                }
            }
        }

        Logger.getLogger(getClass()).info("matchActionPassedNextUser username=" + nextAction.getUserName() +
                                          " nodemode=" + nextAction.getNodeMode());

        // 根据关联节点，取得curActionUserDept，取当前行文方向所关联节点的用户所在的部门
        DeptUserDb du = new DeptUserDb();
        // myAction.getUserName()中应为单个用户
        // 关联发起人节点
        String curActionUserDept = "";
        if (nextAction.getRelateToAction().equals(RELATE_TO_ACTION_STARTER)) {
            if (deptOfUserWithMultiDept == null || deptOfUserWithMultiDept.equals("")) {
                WorkflowActionDb startAction = getStartAction(nextAction.getFlowId());
                if (startAction.getDept().equals("")) {
                    // 如果开始节点上未保存单位（即发起者无兼职）
                    Vector vu = du.getDeptsOfUser(startAction.getUserName());
                    curActionUserDept = ((DeptDb) vu.get(0)).getCode();
                } else {
                    DeptDb dd = new DeptDb();
                    curActionUserDept = dd.getDeptDb(startAction.getDept()).getCode();
                }
            } else {
                // 如果有兼职
                curActionUserDept = deptOfUserWithMultiDept;
            }
        } else {
            if (deptOfUserWithMultiDept == null || deptOfUserWithMultiDept.equals("")) {
                Vector vu = du.getDeptsOfUser(myAction.getUserName());
                if (vu.size()>0) {
                	curActionUserDept = ((DeptDb) vu.get(0)).getCode();
                }
            } else {
                // 如果有兼职
                curActionUserDept = deptOfUserWithMultiDept;
            }
        }

        if (nextAction.getNodeMode() == NODE_MODE_ROLE ||
            nextAction.getNodeMode() == NODE_MODE_ROLE_SELECTED) {
            // 如果为role型，检查role中是否只有一个用户，如果是则自动填充action中的用户，如果不是，则根据角色与组织机构相关联或者繁忙程度，自动分配
            String roleCodes = nextAction.getJobCode();
            String[] ary = StrUtil.split(roleCodes, ",");
            int aryrolelen = 0;
            if (ary != null)
                aryrolelen = ary.length;
            if (aryrolelen == 0)
                return vt;

            RoleMgr rm = new RoleMgr();

            LogUtil.getLog(getClass()).info("matchActionPassedNextUser: relateRoleToOrganization=" + relateRoleToOrganization);

            // 根据行文方向、在方向上第一个遇到的该角色，职级和部门范围联合选定用户，如果选不到，则根据关联的职级和部门范围列出用户
            Logger.getLogger(getClass()).info("matchActionPassedNextUser: aryrolelen=" + aryrolelen +
                                              " rankCode1=" +
                                              rankCode + " dept=" + dept + " deptOfUserWithMultiDept=" + deptOfUserWithMultiDept);

            // 用部门curActionUserDept、上级及再上一级部门来限定范围
            String dCode = "";
            // 如果nextAction未设关联至组织机构，则不再根据部门范围过滤
            if (nextAction.isRelateRoleToOrganization()) {
                DeptDb dd = new DeptDb();
                dd = dd.getDeptDb(curActionUserDept);
                LogUtil.getLog(getClass()).info("matchActionPassedNextUser: curActionUserDept=" + curActionUserDept);
                
                dCode = curActionUserDept + "," + dd.getParentCode();
                LogUtil.getLog(getClass()).info("matchActionPassedNextUser: dCode=" + dCode);

                DeptDb ddParent = dd.getDeptDb(dd.getParentCode());
                if (ddParent.isLoaded() && !ddParent.getParentCode().equals("-1"))
                    dCode += "," + ddParent.getParentCode();
            }

            String dept = nextAction.getDept().trim();
            if (dept.equals(""))
                dept = dCode;
            else
                dept += "," + dCode;
            LogUtil.getLog(getClass()).info("matchActionPassedNextUser: dept=" + dept + " jobName=" + nextAction.getJobName());

            // 则根据限定的角色、职位、部门列出所有符合条件的用户
            for (int i = 0; i < aryrolelen; i++) {
                RoleDb rd = rm.getRoleDb(ary[i]);
                Vector v_user = rd.getAllUserOfRole();

                LogUtil.getLog(getClass()).info("matchActionPassedNextUser: v_user.size=" + v_user.size());

                // 根据关联的职级查找用户
                String rankCode = nextAction.getRankCode();
                if (!rankCode.equals("")) {
                    Iterator ir = v_user.iterator();
                    while (ir.hasNext()) {
                        UserDb ud = (UserDb) ir.next();
                        // 职级匹配
                        if (ud.getRankCode().equals(rankCode)) {
                            addUserDbToVector(vt, ud);
                        }
                    }
                }

                // 根据关联的部门查找用户
                // @task:这里应该优先匹配本部门，如果找不到，再去匹配上级部门，找到了则不再往上匹配
                if (!dept.equals("")) {
                    String[] arydept = StrUtil.split(dept, ",");
                    int len1 = arydept.length;
                    Iterator ir = v_user.iterator();
                    while (ir.hasNext()) {
                        UserDb ud = (UserDb) ir.next();
                        for (int j = 0; j < len1; j++) {
                            if (du.isUserOfDept(ud.getName(), arydept[j])) {
                                addUserDbToVector(vt, ud);
                            }
                        }
                    }
                }

                Logger.getLogger(getClass()).info("matchActionPassedNextUser: aryrolelen=" + aryrolelen +
                                                  " rankCode=" +
                                                  rankCode + " dept=" + dept + " vt.size()=" + vt.size());

                // 过滤被选中的用户，使满足职级条件的用户同时满足部门条件
                if (!dept.equals("") && !rankCode.equals("")) {
                    Vector vv = new Vector(); // 用以记录vt中将要被删除的UserDb
                    String[] arydept = StrUtil.split(dept, ",");
                    int len1 = arydept.length;
                    int size = vt.size();
                    for (int k = 0; k < size; k++) {
                        UserDb ud = (UserDb) vt.get(k);
                        for (int j = 0; j < len1; j++) {
                            if (!du.isUserOfDept(ud.getName(), arydept[j])) {
                                vv.addElement(ud);
                            }
                        }
                    }
                    Iterator ir = vv.iterator();
                    while (ir.hasNext()) {
                        UserDb ud = (UserDb) ir.next();
                        vt.remove(ud);
                    }
                }

                // 如果既未设部门范围，也未设职级，则将角色中的所有人员列出
                if (dept.equals("") && rankCode.equals("")) {
                    Iterator ir = v_user.iterator();
                    Logger.getLogger(getClass()).info("matchActionPassedNextUser: v_user.size=" + v_user.size());
                    while (ir.hasNext()) {
                        UserDb ud = (UserDb) ir.next();
                        Logger.getLogger(getClass()).info("matchActionPassedNextUser: user name=" + ud.getName() +
                                                          " realname=" + ud.getRealName());
                        addUserDbToVector(vt, ud);
                    }
                }
            }

            // @task:当节点上设了多个角色，vt有可能会产生重复的用户，需滤除


        } else {
            // 用户型，则检查是否只有一个用户，如果是，则自动填充action中的用户，不是，则返回所有设定的人员
            String users = nextAction.getUserName();
            if (users == null || users.equals("")) {
            	users = nextAction.getJobCode();
            }
            
            Logger.getLogger(getClass()).info("matchActionUser: users=" + users);
            if (users.equals(PRE_TYPE_STARTER) || users.equals(PRE_TYPE_SELF)) {
                    // 填充为流程发起人员
                    WorkflowDb wfd = new WorkflowDb();
                    wfd = wfd.getWorkflowDb(myAction.getFlowId());
                    WorkflowActionDb startAction = myAction.getWorkflowActionDb(wfd.getStartActionId());
                    UserDb ud = new UserDb();
                    ud = ud.getUserDb(startAction.getUserName());
                    vt.addElement(ud);
            }
            else if (users.equals(PRE_TYPE_DEPT_MGR)) {
                // 找到沿组织机构树往上距离最近的部门管理员
                // 取出关联节点用户所在的部门
                DeptDb parentDept = null; ;
                String deptCode = curActionUserDept;
                DeptDb dd = new DeptDb();
                dd = dd.getDeptDb(deptCode);
                do {
                    // 在本部门中寻找
                    boolean isFound = false;
                    Iterator ir = du.list(deptCode).iterator();
                    while (ir.hasNext()) {
                        DeptUserDb dud = (DeptUserDb) ir.next();
                        // 取得该本部门的管理员
                        if (com.redmoon.oa.pvg.Privilege.canUserAdminDept(dud.getUserName(), curActionUserDept)) {
                            vt.addElement(um.getUserDb(dud.getUserName()));
                            isFound = true;
                        }
                    }
                    if (isFound)
                        break;

                    // 取出父部门
                    parentDept = dd.getDeptDb(dd.getParentCode());
                    if (parentDept == null)
                        break;

                    // 在兄弟节点中寻找
                    Iterator ir2 = parentDept.getChildren().iterator();
                    while (ir2.hasNext()) {
                        DeptDb dd2 = (DeptDb) ir2.next();
                        // 跳过本部门
                        if (dd2.getCode().equals(dd.getCode()))
                            continue;

                        ir = du.list(dd2.getCode()).iterator();
                        while (ir.hasNext()) {
                            DeptUserDb dud = (DeptUserDb) ir.next();
                            // 取得该本部门的管理员
                            if (com.redmoon.oa.pvg.Privilege.canUserAdminDept(dud.getUserName(), curActionUserDept)) {
                                vt.addElement(um.getUserDb(dud.getUserName()));
                                isFound = true;
                            }
                        }
                        if (isFound)
                            break;
                    }

                    if (isFound)
                        break;

                    if (parentDept.getCode().equals(DeptDb.ROOTCODE))
                        break;

                    // 往上寻找
                    deptCode = parentDept.getCode();
                    dd = parentDept;
                } while (true);
            }
            else if (users.equals(PRE_TYPE_FORE_ACTION)) {
                // 如果为预定义节点（上一节点处理人员）填充为其上一节点的处理人员,如果有多个节点，则将上一节点的处理人员全部置入
                Vector v = myAction.getLinkFromActions();
                Iterator ir = v.iterator();
                while (ir.hasNext()) {
                    WorkflowActionDb w = (WorkflowActionDb) ir.next();
                    users = w.getUserName();
                    String[] ary = StrUtil.split(users, ",");
                    int len = 0;
                    if (ary != null)
                        len = ary.length;
                    if (len > 1) {
                        for (int i = 0; i < len; i++) {
                            vt.addElement(um.getUserDb(ary[i]));
                        }
                    } else {
                        vt.addElement(um.getUserDb(users));
                    }
                }
            }
            else if (users.startsWith(PRE_TYPE_ACTION_USER)) {
                // 如果为预定义节点（上一节点处理人员）填充为其上一节点的处理人员,如果有多个节点，则将上一节点的处理人员全部置入
                String iName = users.substring((PRE_TYPE_ACTION_USER + "_").length());
                WorkflowActionDb w = getWorkflowActionDbByInternalName(iName, flowId);
                users = w.getUserName();
                String[] ary = StrUtil.split(users, ",");
                int len = 0;
                if (ary != null)
                    len = ary.length;
                if (len > 1) {
                    for (int i = 0; i < len; i++) {
                        vt.addElement(um.getUserDb(ary[i]));
                    }
                } else {
                    vt.addElement(um.getUserDb(users));
                }
            }
            else if (users.startsWith(PRE_TYPE_PROJECT_ROLE)) {
            	if (com.redmoon.oa.kernel.License.getInstance().isEnterprise() || com.redmoon.oa.kernel.License.getInstance().isGroup() || com.redmoon.oa.kernel.License.getInstance().isPlatform())
            		;
            	else {
            		throw new ErrMsgException("系统版本中无此功能！");
            	}
            	
				// 所选节点上的用户
				String prjRole = users.substring((PRE_TYPE_PROJECT_ROLE + "_")
						.length());

				// 取得项目的ID
				long projectId = wf.getProjectId();
				if (projectId == -1) {
					throw new ErrMsgException("流程未与项目关联！");
				}
				else {
					// 取得项目角色中的相应人员
					com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
					String formCode = "project_members";
					String sql = "select id from " + FormDb.getTableName(formCode)
							+ " where cws_id='" + projectId + "' and prj_role="
							+ StrUtil.sqlstr(prjRole);
					Vector v = fdao.list(formCode, sql);
					if (v.size() == 0) {
						throw new ErrMsgException("项目角色" + prjRole + "中无对应人员！");
					} else {
						Iterator ir = v.iterator();
						while (ir.hasNext()) {
							fdao = (com.redmoon.oa.visual.FormDAO) ir.next();
							String prjUser = fdao.getFieldValue("prj_user");
							vt.addElement(um.getUserDb(prjUser));
						}
					}
				}
            }
            else if (users.startsWith(PRE_TYPE_NODE_SCRIPT)) {
            	// 通过脚本选人
        		String script = WorkflowActionDb.getActionProperty(wpd, nextAction.getInternalName(), "nodeScript");
            	Leaf lf = new Leaf();
            	lf = lf.getLeaf(wf.getTypeCode());
				if (script != null) {
					Interpreter bsh = new Interpreter();
					try {
						StringBuffer sb = new StringBuffer();
						FormDAO fdao = new FormDAO();
						FormDb fd = new FormDb();
						fd = fd.getFormDb(lf.getFormCode());
						fdao = fdao.getFormDAO(wf.getId(), fd);

    					BeanShellUtil.setFieldsValue(fdao, sb);

						// 赋值给用户
						// sb.append("userName=\"" + privilege.getUser(request)
						// 		+ "\";");
						sb.append("int flowId=" + wf.getId() + ";");

						// bsh.set("request", request);
						// bsh.set("fileUpload", fu);

						bsh.eval(BeanShellUtil.escape(sb.toString()));

						bsh.eval(script);
						Object obj = bsh.get("ret");
						if (obj != null) {
							Vector v = (Vector) obj;
							vt.addAll(v);
						}
					} catch (EvalError e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}            	
            }            
            else if (users.startsWith(PRE_TYPE_FIELD_USER)) {
                // 表单中指定的用户
                String fieldNames = users.substring((PRE_TYPE_FIELD_USER + "_").length());

                Leaf lf = new Leaf();
                lf = lf.getLeaf(wf.getTypeCode());

                FormDb fd = new FormDb();
                fd = fd.getFormDb(lf.getFormCode());

                if (fieldNames.startsWith("nest.")) {
                    fieldNames = fieldNames.substring("nest.".length());

                    // 取得嵌套表
                    Vector v = fd.getFields();
                    Iterator ir = v.iterator();
                    boolean isFound = false;
                    while (ir.hasNext()) {
                        FormField ff = (FormField) ir.next();
                        if (ff.getType().equals(FormField.TYPE_MACRO)) {
                            // System.out.println(getClass() + " ff.getMacroType()=" + ff.getMacroType());
                            if (ff.getMacroType().equals("nest_table") || ff.getMacroType().equals("nest_sheet")) {
                                isFound = true;
                                String nestFormCode = ff.getDefaultValue();
                                FormDb nestfd = new FormDb();
                                nestfd = nestfd.getFormDb(nestFormCode);
                                String cwsId = String.valueOf(wf.getId());
                                // 取得嵌套表中的数据
                                String sql = "select id from " + nestfd.getTableNameByForm() + " where cws_id=" + StrUtil.sqlstr(cwsId);
                                sql += " order by cws_order";

                                // out.print(sql);

                                com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
                                Vector vNest = fdao.list(nestFormCode, sql);
                                Iterator irNest = vNest.iterator();
                                while (irNest.hasNext()) {
                                    fdao = (com.redmoon.oa.visual.FormDAO)irNest.next();
                                    String val = fdao.getFieldValue(fieldNames);
                                    LogUtil.getLog(getClass()).info("matchActionPassedNextUser val=" + val + fdao.getId() + " sql=" + sql);
                                    UserDb user = um.getUserDb(val);
                                    if (user.isLoaded()) {
                                        vt.addElement(user);
                                    }
                                    else
                                        throw new ErrMsgException("嵌套表字段：" + fieldNames + "对应的用户：" + val + "不存在");
                                }
                            }
                        }
                    }
                    if (!isFound) {
                        throw new ErrMsgException("未找到嵌套表");
                    }
                }
                else {
                    FormDAO fdao = new FormDAO();
                    fdao = fdao.getFormDAO(wf.getId(), fd);

                    String[] fieldAry = StrUtil.split(fieldNames, ",");
                    if (fieldAry == null)
                        throw new ErrMsgException("指定人员的表单域：" + fieldNames + "不存在！");
                    for (int k = 0; k < fieldAry.length; k++) {
                        String fieldName = fieldAry[k];                        
                        
                        FormField ff = fd.getFormField(fieldName);
                        if (ff == null)
                            throw new ErrMsgException("指定人员的表单域：" + fieldName + "不存在！");
                        
                        String userNames = StrUtil.getNullStr(fdao.getFieldValue(fieldName));
                        if (!"".equals(userNames)) {
                            // 如果是部门宏控件，则取出部门中的全部用户
                            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                            	if (ff.getMacroType().equals("macro_my_dept_select")
                            			|| ff.getMacroType().equals("macro_dept_sel_win")
                            			|| ff.getMacroType().equals("macro_dept_select")
                            	) {
                            		userNames = "";
                            		DeptUserDb dud = new DeptUserDb();
                            		Iterator ir = dud.list(fdao.getFieldValue(fieldName)).iterator();
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
                        

                        userNames = userNames.replaceAll("，", ",");
                        String[] ary = StrUtil.split(userNames, ",");
                        int len = 0;
                        if (ary != null)
                            len = ary.length;

                        LogUtil.getLog(getClass()).info("matchActionPassedNextUser: users=" + users + " userNames=" +
                                                        userNames + " ary=" + ary + " vt.size=" + vt.size());

                        if (len >= 1) {
                            for (int i = 0; i < len; i++) {
                                UserDb user = um.getUserDb(ary[i]);
                                if (user.isLoaded())
                                    vt.addElement(user);
                                else
                                    throw new ErrMsgException(nextAction.getJobName() + "=" + ary[i] + " 不存在！");
                            }
                        }
                    }
                }
            }
            else {
                String[] ary = StrUtil.split(users, ",");
                int len = 0;
                if (ary != null)
                    len = ary.length;
                if (len > 1) {
                    for (int i = 0; i < len; i++) {
                        vt.addElement(um.getUserDb(ary[i]));
                    }
                } else {
                    vt.addElement(um.getUserDb(users));
                }
            }
        }

        Logger.getLogger(getClass()).info("matched nextAction.getJobName()=" + nextAction.getJobName() + " vt.size=" + vt.size());

        boolean isIgnored = false;

        // 如果只匹配到一个用户
        if (vt.size() == 1) {
            /*
            UserDb ud = (UserDb) vt.get(0);
            Logger.getLogger(getClass()).info("Only one user matched nextAction.getJobName()=" + nextAction.getJobName() + " name=" + ud.getName() +
                        " realName=" + ud.getRealName());
            setActionUserOnMatch(nextAction, ud);
            */

           UserDb ud = (UserDb) vt.get(0);
           if (nextAction.getIgnoreType() == IGNORE_TYPE_USER_ACCESSED_BEFORE) {
               // 检查用户是否之前已经处理过流程，且可以跳过，则清空vt
               if (isUserAccessedBefore(ud.getName(), nextAction.getId(), nextAction.getFlowId())) {
                   vt.removeAllElements();
                   isIgnored = true;
               }
           }
        } else if (vt.size() > 1) { // 角色中的用户数或者预设的员工数目大于1
            // 如果定义了策略，则应用策略
            String strategy = nextAction.getStrategy();
            if (!strategy.equals("")) {
                StrategyMgr sm = new StrategyMgr();
                StrategyUnit su = sm.getStrategyUnit(strategy);
                if (su != null) {
                    IStrategy ist = su.getIStrategy();
                    vt = ist.selectUser(vt);
                }
            }
        }

        // 置节点上的处理用户
        if (!isIgnored) {
            setActionUserOnMatch(nextAction, vt);
        }
        
        return vt;
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
        if (json==null)
            throw new ErrMsgException("节点属性格式错误！");

        String subFlowTypeCode = null;
        try {
            subFlowTypeCode = json.getString("subFlowTypeCode");
        } catch (JSONException ex) {
            ex.printStackTrace();
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
                        Global.getFullRootPath(request) +
                        "/public/flow_dispose.jsp?action=" + action +
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
                    		
                    		String sql = "select id from form_table_" + nestFormCodeFrom + " where cws_id=" + pfdao.getId();	
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
                    			fdaoNestTo.setCwsId("" + sfdao.getId());
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
            } catch (JSONException ex) {
                ex.printStackTrace();
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
                    		
                    		String sql = "select id from form_table_" + nestFormCodeFrom + " where cws_id=" + sfdao.getId();	
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
                    			fdaoNestTo.setCwsId("" + pfdao.getId());
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
                
            } catch (JSONException ex) {
                ex.printStackTrace();
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
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (JSONException ex) {
            /** @todo Handle this exception */
            ex.printStackTrace();
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
                    String prop = e.getChildText(property);
                    return prop;
                    // System.out.println("subFlowTypeCode = " + jobj.get("subFlowTypeCode"));
                    // System.out.println("toSubMap = " + jobj.get("toSubMap"));
                    // JSONObject json = jobj.getJSONObject("toSubMap");
                    // System.out.println("parentField = " + json.get("parentField"));
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (JDOMException ex) {
            ex.printStackTrace();
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

        if (wpd==null)
            return new Vector();

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
            ex.printStackTrace();
        }
        return v;
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
     * 格式：{relateToAction,ignoreType,kind,fieldHide,isDelayed,timeDelayedValue,timeDelayedUnit}
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
}
