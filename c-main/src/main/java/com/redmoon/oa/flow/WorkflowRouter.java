package com.redmoon.oa.flow;

import bsh.EvalError;
import bsh.Interpreter;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.service.IDeptUserService;
import com.cloudweb.oa.service.IUserOfRoleService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.macroctl.*;
import com.redmoon.oa.flow.strategy.IStrategy;
import com.redmoon.oa.flow.strategy.StrategyMgr;
import com.redmoon.oa.flow.strategy.StrategyUnit;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.person.UserSetupDb;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.pvg.RoleMgr;
import com.redmoon.oa.util.BeanShellUtil;
import com.redmoon.oa.visual.Formula;
import com.redmoon.oa.visual.FormulaUtil;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class WorkflowRouter {

    /**
     * 匹配节点中的人员，如果只有一人，则自动匹配，当curAction被处理时，调用本方法
     * @param request HttpServletRequest
     * @param nextAction WorkflowActionDb
     * @param curAction WorkflowActionDb
     * @param isTest 是否为测试模式，用于设计器在设计时，显示节点上的用户
     * @param deptOfUserWithMultiDept 用户在flow_dispose.jsp中选择的部门
     * @return Vector
     */
    public Vector<UserDb> matchActionUser(HttpServletRequest request, WorkflowActionDb nextAction, WorkflowActionDb curAction, boolean isTest, String deptOfUserWithMultiDept) throws ErrMsgException, MatchUserException {
        Vector<UserDb> vt = new Vector<>();
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
        if (nextAction.getJobCode().equals(WorkflowActionDb.PRE_TYPE_USER_SELECT) || nextAction.getJobCode().equals(WorkflowActionDb.PRE_TYPE_USER_SELECT_IN_ADMIN_DEPT)) {
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
        WorkflowActionDb startAction = curAction.getStartAction(nextAction.getFlowId());
        if (nextAction.isRelateRoleToOrganization()) {
            if (nextAction.getRelateToAction().equals(WorkflowActionDb.RELATE_TO_ACTION_STARTER)) {
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
            else if (nextAction.getRelateToAction().equals(WorkflowActionDb.RELATE_TO_ACTION_DEPT)) {
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

        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());

        boolean isNeedCheckCurUserMultiDept = true;
        // 判断如果是否处于多个部门，有没有选择部门
        if (!isTest) {
            // 注释掉下行，是因为只要存在有兼职的情况，就需要去进一步判断是否需选择兼职所在部门
            // if (nextAction.isRelateRoleToOrganization()) {
            // 如果当前节点不是开始节点，且下一节点关联的是开始节点，则不需要判断是否选择了兼职部门
            if (curAction.getId()!=startAction.getId() && nextAction.getRelateToAction().equals(WorkflowActionDb.RELATE_TO_ACTION_STARTER)) {
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
                        if (deptOfUserWithMultiDept == null || "".equals(deptOfUserWithMultiDept)) {
                            throw new MatchUserException(); // 返回异常，以使得flow_dispose.jsp在try catch后显示多个部门供选择
                        }
                    }
                }
            }
            // }

            // 比较角色大小
            // 检查跳越模式，小于当前角色级别的节点需跳过，只比较节点上设置了单一角色的情况，如果设置了多个角色则无法跳过，遇到发散节点也不允许跳过
            if (wpd.getRoleRankMode() == WorkflowPredefineDb.ROLE_RANK_NEXT_LOWER_JUMP) {
                // 如果角色比较大小时允许跳过
                if (nextAction.getIgnoreType()!=WorkflowActionDb.IGNORE_TYPE_ROLE_COMPARE_NOT) {
                    Vector toV = nextAction.getLinkToActions();
                    LogUtil.getLog(getClass()).info("matchActionUser: getRoleRankMode=ROLE_RANK_NEXT_LOWER_JUMP" + " toV.size()=" + toV.size());
                    if (toV.size() == 1) {
                        LogUtil.getLog(getClass()).info("matchActionUser: compareRoleOfAction");
                        if (nextAction.compareRoleOfAction(nextAction, curAction)) {
                            LogUtil.getLog(getClass()).info("matchActionUser: compareRoleOfAction = true");
                            return vt; // 此时vt为空
                        }
                    }
                }
            }
        }

        if (nextAction.getNodeMode() == WorkflowActionDb.NODE_MODE_ROLE_SELECTED || nextAction.getNodeMode() == WorkflowActionDb.NODE_MODE_USER_SELECTED) {
            int nextActionFromCount = 0;
            Vector fromV = nextAction.getLinkFromActions();
            // 如果是汇聚节点
            if (fromV.size() > 1) {
                // 统计入度
                Iterator fromIr = fromV.iterator();
                while (fromIr.hasNext()) {
                    WorkflowActionDb fromAction = (WorkflowActionDb) fromIr.next();
                    if (fromAction.getStatus() != WorkflowActionDb.STATE_IGNORED) {
                        nextActionFromCount++;
                    }
                }

                // 如果节点的入度（不含来自被忽略的节点）大于1，而节点已经设置或者匹配好了人员，则不允许再次匹配用户，但如果是表单中选择的人员，则允许再次匹配用户
                if (nextActionFromCount > 1 &&
                        !"".equals(nextAction.getUserName()) && !nextAction.getJobCode().startsWith(WorkflowActionDb.PRE_TYPE_FIELD_USER)) {
                    String[] users = StrUtil.split(nextAction.getUserName(), ",");
                    for (String user : users) {
                        vt.addElement(um.getUserDb(user));
                    }
                    return vt;
                }
            }
            // 如果curAction是被打回的，则当非reMatchUser时，也不允许重新选择用户
            String op = ParamUtil.get(request, "op");
            if (!"reMatchUser".equals(op)) {
                if (curAction.getStatus() == WorkflowActionDb.STATE_RETURN) {
                    boolean canSelUserWhenReturned = "1".equals(WorkflowActionDb.getActionProperty(wpd, curAction.getInternalName(), "canSelUserWhenReturned"));
                    // 如果被退回再提交时不可以重新选择用户
                    if (!canSelUserWhenReturned) {
                        if (!nextAction.getUserName().equals("")) {
                            String[] users = StrUtil.split(nextAction.getUserName(), ",");
                            for (String user : users) {
                                vt.addElement(um.getUserDb(user));
                            }
                            return vt;
                        }
                    }
                }
            }
        }

        Logger.getLogger(getClass()).info("matchActionUser username=" + nextAction.getUserName() +
                " nodemode=" + nextAction.getNodeMode());

        // 用于匹配下一节点跟当前节点角色与部门相关联的情况中，当前节点用户数大于1时，当前处理用户应能够看见的关联用户，如：当前用户应只能看见自己的处长
        Vector myVt = new Vector();

        if (nextAction.getNodeMode() == WorkflowActionDb.NODE_MODE_ROLE || nextAction.getNodeMode() == WorkflowActionDb.NODE_MODE_ROLE_SELECTED) {
            // 如果为role型，检查role中是否只有一个用户，如果是则自动填充action中的用户，如果不是，则根据角色与组织机构相关联或者繁忙程度，自动分配
            String roleCodes = nextAction.getJobCode();
            String[] ary = StrUtil.split(roleCodes, ",");
            int aryrolelen = 0;
            if (ary != null) {
                aryrolelen = ary.length;
            }
            if (aryrolelen == 0) {
                return vt;
            }

            RoleMgr rm = new RoleMgr();

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
                    if (vu.size()==0 && isTest) {
                        return vt;
                    }
                    Logger.getLogger(getClass()).info("vu size=" + vu.size() + " deptOfUserWithMultiDept=" + deptOfUserWithMultiDept);

                    boolean isRelateActionMultiUser = false;
                    String[] aryUser = StrUtil.split(actionRelated.getUserName(), ",");
                    if (aryUser!=null && aryUser.length>1) {
                        isRelateActionMultiUser = true;
                    }

                    if (vu.size()==0) {
                        // 如果还没有保存草稿，且关联的是表单中的部门
                        if (wf.getStatus()==WorkflowDb.STATUS_NONE) {
                            if (nextAction.getRelateToAction().equals(WorkflowActionDb.RELATE_TO_ACTION_DEPT)) {
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
                        if (!nextAction.getRelateToAction().equals(WorkflowActionDb.RELATE_TO_ACTION_DEFAULT)) {
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
                                if (deptOfUserWithMultiDept==null || deptOfUserWithMultiDept.equals("")) {
                                    throw new ErrMsgException("请选择您所在的部门！"); // 提交时未选择部门
                                } else {
                                    dd = dd.getDeptDb(deptOfUserWithMultiDept);
                                }
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
                    // 只加入限定部门范围内的用户
                    if (!"".equals(dept)) {
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

                    // 限定部门表单域
                    String deptField = StrUtil.getNullStr(WorkflowActionDb.getActionProperty(wpd, nextAction.getInternalName(), "deptField"));
                    if (!"".equals(deptField)) {
                        String depts = "";
                        String op = ParamUtil.get(request, "op");
                        // 如果是在flow_dispose.jsp中重新选择了部门
                        if ("reMatchUser".equals(op)) {
                            String fieldName = ParamUtil.get(request, "fieldName");
                            if (deptField.equals(fieldName)) {
                                depts = ParamUtil.get(request, "fieldValue");
                            }
                        }
                        else {
                            Leaf lf = new Leaf();
                            lf = lf.getLeaf(wf.getTypeCode());
                            FormDAO fdao = new FormDAO();
                            FormDb fd = new FormDb();
                            fd = fd.getFormDb(lf.getFormCode());
                            fdao = fdao.getFormDAO(wf.getId(), fd);
                            depts = fdao.getFieldValue(deptField);
                        }

                        String[] arydept = StrUtil.split(depts, ",");
                        if (arydept!=null) {
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
                    }

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
                    if (dept.equals("") && rankCode.equals("") && "".equals(deptField)) {
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
            if (users.equals(WorkflowActionDb.PRE_TYPE_STARTER) || users.equals(WorkflowActionDb.PRE_TYPE_SELF)) {
                if (!isTest) {
                    // 填充为流程发起人员
                    UserDb ud = new UserDb();
                    ud = ud.getUserDb(startAction.getUserName());
                    vt.addElement(ud);
                }
            }
            else if (users.equals(WorkflowActionDb.PRE_TYPE_DEPT_MGR)) {
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
                        if (isFound) {
                            break;
                        }

                        if (dd.getParentCode().equals("-1")) {
                            break;
                        }
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
            else if (users.equals(WorkflowActionDb.PRE_TYPE_FORE_ACTION)) {
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
            else if (users.startsWith(WorkflowActionDb.PRE_TYPE_ACTION_USER)) {
                if (!isTest) {
                    // 所选节点上的用户
                    String iName = users.substring((WorkflowActionDb.PRE_TYPE_ACTION_USER + "_").length());

                    WorkflowActionDb w = nextAction.getWorkflowActionDbByInternalName(iName, curAction.getFlowId());
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
            else if (users.startsWith(WorkflowActionDb.PRE_TYPE_FIELD_USER)) {
                if (!isTest) {
                    // 表单中指定的用户
                    String fieldNames = users.substring((WorkflowActionDb.PRE_TYPE_FIELD_USER + "_").length());

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
                                if ("nest_table".equals(ff.getMacroType()) || "nest_sheet".equals(ff.getMacroType())) {
                                    isFound = true;
                                    String nestFormCode = ff.getDescription();
                                    try {
                                        String defaultVal = StrUtil.decodeJSON(ff.getDescription());
                                        JSONObject json = new JSONObject(defaultVal);
                                        nestFormCode = json.getString("destForm");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    FormDAO fdaoMain = new FormDAO();
                                    fdaoMain = fdaoMain.getFormDAO(wf.getId(), fd);

                                    FormDb nestfd = new FormDb();
                                    nestfd = nestfd.getFormDb(nestFormCode);
                                    String cwsId = String.valueOf(fdaoMain.getId());
                                    // 取得嵌套表中的数据
                                    String sql = "select id from " + nestfd.getTableNameByForm() + " where cws_id=" + StrUtil.sqlstr(cwsId);
                                    sql += " and cws_parent_form=" + StrUtil.sqlstr(ff.getFormCode());
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
                                        else {
                                            throw new ErrMsgException("嵌套表字段：" + fieldNames + "对应的用户：" + val + "不存在");
                                        }
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
                        if (fieldAry==null) {
                            throw new ErrMsgException("指定人员的表单域：" + fieldNames + "不存在！");
                        }

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
                            if (ary != null) {
                                len = ary.length;
                            }
                            if (len >= 1) {
                                for (int i = 0; i < len; i++) {
                                    UserDb user = um.getUserDb(ary[i]);
                                    if (user.isLoaded()) {
                                        vt.addElement(user);
                                    } else {
                                        throw new ErrMsgException(nextAction.getJobName() + "=" + ary[i] + " 不存在！");
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else if (users.startsWith(WorkflowActionDb.PRE_TYPE_PROJECT_ROLE)) {
                if (!com.redmoon.oa.kernel.License.getInstance().isPlatform()) {
                    throw new ErrMsgException("系统版本中无此功能！");
                }

                if (!isTest) {
                    // 所选节点上的用户
                    String prjRole = users.substring((WorkflowActionDb.PRE_TYPE_PROJECT_ROLE + "_").length());

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
            else if (users.startsWith(WorkflowActionDb.PRE_TYPE_NODE_SCRIPT)) {
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
                        e.printStackTrace();
                    }
                }
            }
            else if (nextAction.getJobCode().equals( // 我的领导
                    WorkflowActionDb.PRE_TYPE_MYLEADER)) {
                UserSetupDb usd = new UserSetupDb(pvg.getUser(request));
                String uName = usd.getMyleaders();
                if (uName!=null && !"".equals(uName)) {
                    String[] nameAry = StrUtil.split(uName, ",");
                    int aryLen = nameAry.length;
                    for (int k = 0; k < aryLen; k++) {
                        UserDb ud = um.getUserDb(nameAry[k]);
                        addUserDbToVector(vt, ud);
                    }
                }
            } else if (nextAction.getJobCode().equals(WorkflowActionDb.PRE_TYPE_MYSUBORDINATE)) {
                // 我的下属
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
            if (nextAction.getIgnoreType()==WorkflowActionDb.IGNORE_TYPE_USER_ACCESSED_BEFORE) {
                // 检查用户是否之前已经处理过流程，且可以跳过，则清空vt
                if (nextAction.isUserAccessedBefore(ud.getName(), nextAction.getId(), nextAction.getFlowId())) {
                    vt.removeAllElements();
                    isIgnored = true;
                }
            }

            Logger.getLogger(getClass()).info("Only one user matched name=" +
                    ud.getName() +
                    " realName=" + ud.getRealName());
            if (!isIgnored && !isTest) {
                WorkflowActionDb.setActionUserOnMatch(nextAction, vt);
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
                            WorkflowActionDb.setActionUserOnMatch(nextAction, v);
                        }
                        vt = v;
                    }
                }
            }
        }

        if (nextAction.isRelateRoleToOrganization() && nextAction.getRelateToAction().equals(WorkflowActionDb.RELATE_TO_ACTION_DEFAULT)) {
            if (curAction.getUserName().indexOf(",")!=-1) {
                return myVt;
            }
        }

        return vt;
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
        IUserOfRoleService userOfRoleService = SpringUtil.getBean(IUserOfRoleService.class);
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
                Logger.getLogger(getClass()).info("user=" + ud.getRealName() + " role=" + roles[i].getDesc() + " finding role=" + rd.getDesc());
                if (roles[i].getCode().equals(rd.getCode())) {
                    // Logger.getLogger(getClass()).info("rd name:" + rd.getDesc() + "" + ud.getRealName());
                    // 	判断用户在部门中是否拥有角色
                    boolean found = userOfRoleService.isRoleOfDept(dud.getUserName(), roles[i].getCode(), dud.getDeptCode());
                    if (!found) {
                        continue;
                    }

                    // 如果预设的职级不为空，则检查是否一致
                    if (!nextAction.getRankCode().equals("")) {
                        found = ud.getRankCode().equals(nextAction.getRankCode());
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
                                if (du.isUserOfDept(ud.getName(), arydept[m])) {
                                    found = true;
                                }
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
        IUserOfRoleService userOfRoleService = SpringUtil.getBean(IUserOfRoleService.class);
        DeptDb parentDept = dd.getDeptDb(dd.getParentCode());
        UserMgr um = new UserMgr();
        boolean re = false;
        if (parentDept != null) {
            Iterator brotherir = parentDept.getChildren().iterator();
            while (brotherir.hasNext()) {
                DeptDb brotherDept = (DeptDb) brotherir.next();
                // 跳过本部门
                if (brotherDept.getCode().equals(dd.getCode())) {
                    continue;
                }
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
                            // 	判断用户在部门中是否拥有角色
                            boolean found = userOfRoleService.isRoleOfDept(dud.getUserName(), roles[i].getCode(), dud.getDeptCode());
                            if (!found) {
                                continue;
                            }
                            // 如果预设的职级不为空，则检查是否一致
                            if (!nextAction.getRankCode().equals("")) {
                                found = ud.getRankCode().equals(nextAction.getRankCode());
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
        IUserOfRoleService userOfRoleService = SpringUtil.getBean(IUserOfRoleService.class);
        UserMgr um = new UserMgr();
        boolean re = false;
        // 取得上一级父节点
        DeptDb parentDept = dd.getDeptDb(dd.getParentCode());
        while (parentDept != null && parentDept.isLoaded()) {
            boolean found = false;
            // LogUtil.getLog(getClass()).info("parentDept=" + parentDept + " " + parentDept.getCode());
            // 遍历父节点下的所有人员，如果角色一致，则加入其中
            Iterator ir = du.list(parentDept.getCode()).iterator();
            while (ir.hasNext()) {
                du = (DeptUserDb) ir.next();
                // 取得用户的所有角色
                UserDb ud = um.getUserDb(du.getUserName());
                RoleDb[] roles = ud.getRoles();
                int rlen = roles.length;
                for (int i = 0; i < rlen; i++) {
                    // 用户的某个角色与预置的角色一致，职级也一致
                    if (roles[i].getCode().equals(rd.getCode())) {
                        // 	判断用户在部门中是否拥有角色
                        found = userOfRoleService.isRoleOfDept(du.getUserName(), roles[i].getCode(), du.getDeptCode());
                        if (!found) {
                            continue;
                        }
                        if (!nextAction.getRankCode().equals("")) {
                            found = ud.getRankCode().equals(nextAction.getRankCode());
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
                    if (isMe) {
                        break;
                    }
                }
            }
            // 如果找到的是本人，则继续往上级部门查找
            if (isMe) {
                vt.clear();
                re = false;
            }
        }

        if (!re) {
            return matchActionUserUp(nextAction, dd, rd, vt);
        }
        return false;
    }


    /**
     * 根据行文方向匹配用户
     * @param nextAction
     * @param curAction
     * @param dd 关联节点的用户所在部门
     * @param rd
     * @return
     */
    public Vector doMatchActionUser(WorkflowActionDb nextAction, WorkflowActionDb curAction, DeptDb dd, RoleDb rd) {
        String deptCode = dd.getCode(); // 记录dd，用于判断canUserAdminDept
        Vector vt = new Vector();
        int direction = nextAction.getDirection();
        // 上行文
        if (direction == WorkflowActionDb.DIRECTION_UP) {
            matchActionUserUp(nextAction, dd, rd, vt);
        } else if (direction == WorkflowActionDb.DIRECTION_MYDEPT) {
            // 在本部门中寻找
            matchActionUserMyDept(nextAction, dd, rd, vt);
        }
        else if (direction == WorkflowActionDb.DIRECTION_MYDEPT_UP) {
            // 本部门及上行
            matchActionUserMyDeptAndUp(nextAction, curAction, dd, rd, vt, false);
        }
        else if (direction == WorkflowActionDb.DIRECTION_MY_LEADER) {
            matchActionUserMyDeptAndUp(nextAction, curAction, dd, rd, vt, true);
        }
        else if (direction == WorkflowActionDb.DIRECTION_PARALLEL_MYDEPT) { // 本部门及平行文
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
        } else if (direction == WorkflowActionDb.DIRECTION_DOWN) {
            // 下行匹配
            doMatchDirectionDown(nextAction, dd, rd, vt);
        }

        if (nextAction.isRelateDeptManager()) {
            // 删除掉不能管理关联节点用户所在部门的人员
            Iterator ir = vt.iterator();
            while (ir.hasNext()) {
                UserDb user = (UserDb)ir.next();
                if (!com.redmoon.oa.pvg.Privilege.canUserAdminDept(user.getName(), deptCode)) {
                    ir.remove();
                }
            }
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
    public void doMatchDirectionDown(WorkflowActionDb nextAction, DeptDb curUserDept, RoleDb role, Vector vt) {
        DeptUserDb du = new DeptUserDb();
        IUserOfRoleService userOfRoleService = SpringUtil.getBean(IUserOfRoleService.class);
        UserMgr um = new UserMgr();
        // 下行文，在其孩子节点中寻找
        Iterator childir = curUserDept.getChildren().iterator();
        while (childir.hasNext()) {
            DeptDb childDept = (DeptDb) childir.next();

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
                        // 	判断用户在部门中是否拥有角色
                        boolean found = userOfRoleService.isRoleOfDept(dud.getUserName(), roles[i].getCode(), dud.getDeptCode());
                        if (!found) {
                            continue;
                        }
                        // 如果预设的职级不为空，则检查是否一致
                        if (!nextAction.getRankCode().equals("")) {
                            if (ud.getRankCode().equals(nextAction.getRankCode())) {
                                found = true;
                            } else {
                                found = false;
                            }
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
     * 将用户检查是否存在后加入Vector，避免重复加入
     * @param vt Vector
     * @param user UserDb
     */
    public static void addUserDbToVector(Vector<UserDb> vt, UserDb user) {
        for (UserDb ud : vt) {
            if (ud.getName().equals(user.getName())) {
                return;
            }
        }
        vt.addElement(user);
    }

    /**
     * 如果在后继节点的连接线上存在条件，则判别是否有符合条件的分支，如果有满足条件的，则自动运行，注意条件分支中应只有一个分支满足条件
     * 注意当存在不含条件的分支时，此分支是默认分支，异或发散时，而未设条件时就存在这种情况
     * 2012-7-11使支持多个分支同时满足条件 fgf
     * @return WorkflowLinkDb
     */
    public static Vector matchNextBranch(WorkflowActionDb wad, String userName, StringBuffer condBuf, long myActionId) throws ErrMsgException {
        int flowId = wad.getFlowId();
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
            LogUtil.getLog(WorkflowRouter.class).info("matchNextBranch:cond=" + cond);
            // 判别条件
            boolean isValid = false;
            // 如果条件类型为“无条件”或条件内容为空，则该分支线为默认条件
            if (("".equals(cond) || wld.getCondType().equals(WorkflowLinkDb.COND_TYPE_NONE)) &&
                    !wld.getCondType().equals(WorkflowLinkDb.COND_TYPE_COMB_COND)
                    && !wld.getCondType().equals(WorkflowLinkDb.COND_TYPE_MUST)) {
                defaultWld = wld;

                LogUtil.getLog(WorkflowRouter.class).info("matchNextBranch:title=" + wld.getTitle() + " " + wld.getCondDesc() + " " + wld.getCondType());

                blankCondBranchCount ++;
                // 如果title为空的分支线有两条以上，则表示没有默认条件，如果全部不满足条件，需手动选择分支
                if (blankCondBranchCount>=2) {
                    defaultWld = null;
                }
                continue;
            }
            else {
                if (wld.getCondType().equals(WorkflowLinkDb.COND_TYPE_NONE)) {
                    continue;
                }
                condBuf.append(wld.getCondDesc() + "  ");
                if (wld.getCondType().equals(WorkflowLinkDb.COND_TYPE_MUST)) {
                    isValid = true;
                }
                else if (wld.getCondType().equals(WorkflowLinkDb.COND_TYPE_FORM)) {
                    boolean isDefaultCondition = false;
                    if (cond.length()>1) { // 长度如果为1，有可能cond字符串只有=号字符
                        if (cond.indexOf(">=") != -1) {
                            int[] retAry = judgeCondition(cond, ">=", myActionId, flowId);
                            isValid = retAry[0] == 1;
                            isDefaultCondition = retAry[1] == 1;
                        } else if (cond.indexOf("<=") != -1) {
                            int[] retAry = judgeCondition(cond, "<=", myActionId, flowId);
                            isValid = retAry[0] == 1;
                            isDefaultCondition = retAry[1] == 1;
                        } else if (cond.indexOf("<>") != -1) {
                            int[] retAry = judgeCondition(cond, "<>", myActionId, flowId);
                            isValid = retAry[0] == 1;
                            isDefaultCondition = retAry[1] == 1;
                        } else if (cond.indexOf(">") != -1) {
                            int[] retAry = judgeCondition(cond, ">", myActionId, flowId);
                            isValid = retAry[0] == 1;
                            isDefaultCondition = retAry[1] == 1;
                        } else if (cond.indexOf("<") != -1) {
                            int[] retAry = judgeCondition(cond, "<", myActionId, flowId);
                            isValid = retAry[0] == 1;
                            isDefaultCondition = retAry[1] == 1;
                        } else if (cond.indexOf("=") != -1) {
                            int[] retAry = judgeCondition(cond, "=", myActionId, flowId);
                            isValid = retAry[0] == 1;
                            isDefaultCondition = retAry[1] == 1;

                            LogUtil.getLog(WorkflowRouter.class).info("retAry[0]=" + retAry[0] + " condBuf=" + condBuf);

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
                    // 检查用户所在的部门，其实此处的userName是当前处理用户，不是workflowaction中的用户，不存在多个，所以不需要split
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
                                if (!isValid) {
                                    continue;
                                }
                                break;
                            }
                        }
                    }
                }
                else if (wld.getCondType().equals(WorkflowLinkDb.COND_TYPE_DEPT_BELONG)) {
                    String[] depts = StrUtil.split(cond, ",");
                    int deptlen = depts.length;
                    // 检查用户所在的部门
                    String[] userAry = StrUtil.split(userName, ",");
                    if (userAry!=null) {
                        int len = userAry.length;
                        IDeptUserService deptUserService = SpringUtil.getBean(IDeptUserService.class);
                        for (int k = 0; k < len; k++) {
                            // 遍历条件中的部门
                            for (int m = 0; m < deptlen; m++) {
                                if (deptUserService.isUserBelongToDept(userAry[k], depts[m])) {
                                    isValid = true;
                                    break;
                                }
                            }
                            if (!isValid) {
                                continue;
                            }
                            break;
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
                            if (isValid) {
                                break;
                            }
                        }
                    }
                }
                else if (wld.getCondType().equals(WorkflowLinkDb.COND_TYPE_SCRIPT)) {
                    // 通过脚本判别下一分支节点
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

                    List<String> filedList = new ArrayList<>();
                    for (FormField ff : fd.getFields()) {
                        filedList.add(ff.getName());
                    }

                    /*
                     * LeafPriv lp = new LeafPriv(wld.getTypeCode()); if
                     * (!lp.canUserSee(privilege.getUser(request))) { throw new
                     * ErrMsgException("权限非法！"); }
                     */

                    // System.out.println(getClass() + " roleRankMode=" + roleRankMode);

                    String wld_linkProp = wpd.getLinkProp();
                    if (!"".equals(wld_linkProp)) {
                        SAXBuilder parser = new SAXBuilder();
                        org.jdom.Document doc;
                        try {
                            doc = parser.build(new InputSource(new StringReader(wld_linkProp)));

                            StringBuilder sb = new StringBuilder();

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

                                        if(null == firstBracket || "".equals(firstBracket)){
                                            firstBracket = "";
                                        }
                                        if(null == twoBracket || "".equals(twoBracket)){
                                            twoBracket = "";
                                        }
                                        if (name.equals(WorkflowPredefineDb.COMB_COND_TYPE_FIELD)) {
                                            FormField ff = fd.getFormField(fieldName);

                                            if (FormField.TYPE_MACRO.equals(ff.getType())) {
                                                if ("macro_formula_ctl".equals(ff.getMacroType())) {
                                                    String desc = ff.getDescription();
                                                    try {
                                                        JSONObject json = new JSONObject(desc);
                                                        String formulaCode = json.getString("code");
                                                        FormulaUtil formulaUtil = new FormulaUtil();
                                                        Formula formula = formulaUtil.getFormula(new JdbcTemplate(), formulaCode);
                                                        // 使字段类型为函数中所设的数据类型
                                                        ff.setFieldType(formula.getFieldType());
                                                    } catch (JSONException ex) {
                                                        ex.printStackTrace();
                                                    }
                                                }
                                            }
                                            boolean isTextOrBoolean = false; // 是否为字符型或布尔型
                                            if (ff.getFieldType() == FormField.FIELD_TYPE_TEXT ||
                                                    ff.getFieldType() == FormField.FIELD_TYPE_VARCHAR ||
                                                    ff.getFieldType() == FormField.FIELD_TYPE_BOOLEAN) {
                                                isTextOrBoolean = true;
                                            }
                                            if (isTextOrBoolean) {
                                                if ("=".equals(op)) {
                                                    sb.append(firstBracket);
                                                    sb.append("{$" + fieldName + "}");
                                                    sb.append(".equals(\"" + value + "\")");
                                                    sb.append(twoBracket);
                                                } else {
                                                    sb.append(firstBracket);
                                                    sb.append("!{$" + fieldName + "}");
                                                    sb.append(".equals(\"" + value + "\")");
                                                    sb.append(twoBracket);
                                                }
                                            } else {
                                                sb.append(firstBracket);
                                                sb.append("{$" + fieldName + "}");
                                                if ("=".equals(op)) {
                                                    op = "==";
                                                }
                                                if ("<>".equals(op)) {
                                                    op = "!=";
                                                }

                                                sb.append(op);
                                                sb.append(value);
                                                sb.append(twoBracket);
                                            }
                                        } else if (name.equals(WorkflowPredefineDb.COMB_COND_TYPE_PRIV_DEPT)) {
                                            sb.append(firstBracket);

                                            // 判断是否属于某部门
                                            if ("=>".equals(op)) {
                                                DeptUserDb deptUserDb = new DeptUserDb();
                                                IDeptUserService deptUserService = SpringUtil.getBean(IDeptUserService.class);
                                                boolean re = deptUserService.isUserBelongToDept(userName, value);
                                                sb.append("\"true\".equals(\"" + String.valueOf(re) + "\")");
                                            }
                                            else {
                                                DeptUserDb dud = new DeptUserDb();
                                                Vector<DeptDb> vDept = dud.getDeptsOfUser(userName);
                                                // 如果有多个部门
                                                if (vDept.size() > 1) {
                                                    sb.append("(");
                                                }

                                                for (int k = 0; k < vDept.size(); k++) {
                                                    DeptDb dd = (DeptDb) vDept.elementAt(k);

                                                    if (op.equals("=")) {
                                                        sb.append("\"" + dd.getCode() + "\"");
                                                    } else {
                                                        sb.append("!\"" + dd.getCode() + "\"");
                                                    }
                                                    sb.append(".equals");
                                                    sb.append("(\"" + value + "\")");

                                                    if (vDept.size() > 1 && k != vDept.size() - 1) {
                                                        if (op.equals("=")) {
                                                            sb.append(" || ");
                                                        } else {
                                                            sb.append(" && ");
                                                        }
                                                    }
                                                }

                                                if (vDept.size() > 1) {
                                                    sb.append(")");
                                                }
                                            }

                                            sb.append(twoBracket);
                                        } else if (name.equals(WorkflowPredefineDb.COMB_COND_TYPE_PRIV_ROLE)) {
                                            sb.append(firstBracket);

                                            UserDb user = new UserDb();
                                            user = user.getUserDb(userName);
                                            RoleDb[] ary = user.getRoles();

                                            // 如果有多个角色
                                            if (ary.length > 1) {
                                                // 去掉member
                                                RoleDb[] tmp = new RoleDb[ary.length - 1];
                                                int m = 0;
                                                for (int k = 0; k < ary.length; k++) {
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
                                                    sb.append(".equals");
                                                    sb.append("(\"" + value + "\")");
                                                } else if (op.equals("<>")) {
                                                    sb.append("!\"" + rd.getCode() + "\"");
                                                    sb.append(".equals");
                                                    sb.append("(\"" + value + "\")");
                                                } else {
                                                    // 比较角色的大小
                                                    int a = rd.getOrders();
                                                    RoleDb rdRight = new RoleDb();
                                                    rdRight = rdRight.getRoleDb(value);
                                                    if (rdRight.isLoaded()) {
                                                        int b = rdRight.getOrders();
                                                        sb.append(a + " " + op + " " + b);
                                                    } else {
                                                        throw new ErrMsgException("角色" + value + " 不存在，无法比较大小");
                                                    }
                                                }

                                                if (ary.length > 1 && k != ary.length - 1) {
                                                    if (op.equals("=")) {
                                                        sb.append(" || ");
                                                    } else {
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

                                    LogUtil.getLog(WorkflowRouter.class).info("script=" + script);
                                    if (p!=-1) {
                                        script = script.substring(0, p);
                                    }
                                    LogUtil.getLog(WorkflowRouter.class).info("script2=" + script);

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
                            e1.printStackTrace();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }

                if (isValid) {
                    LogUtil.getLog(WorkflowRouter.class).info("wld=" + wld.getTitle() + " condBuf=" + condBuf + " "); //  + StrUtil.trace(new Exception()));

                    v.addElement(wld);
                }
            }

        }
        if (defaultWld!=null) {
            // 如果默认条件不为空，且没有满足条件的分支，则走默认条件所在分支
            if (v.size()==0) {
                v.addElement(defaultWld);
            }
        }
        if (v.size()>1) {
            Comparator ct = new WorkflowLinkComparator();
            Collections.sort(v, ct);
        }
        return v;
    }


    /**
     * 判断分支线上的条件是否满足
     * @param cond String
     * @param token String
     * @return int[] int[0]表示是否满足条件 int[1]表示是否为默认条件
     */
    public static int[] judgeCondition(String cond, String token, long myActionId, int flowId) {
        // System.out.println(getClass() + " token=" + token);
        int[] ary = {0,0};
        String[] pairs = cond.split(token);
        String leftField = pairs[0].trim();
        String rightField = "";
        // pairs长度为1，表示条件右侧的值为空，如　用车人=
        if (pairs.length>1) {
            rightField = pairs[1].trim();
        }
        // 表达式的右侧如果为空表示默认条件，例：报销类型=
        if (rightField.equals("")) {
            ary[1] = 1;
            return ary;
        }

        // 取得流程表单中该域的值
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

        Logger.getLogger(WorkflowRouter.class).info("token=" + token);
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
            Logger.getLogger(WorkflowRouter.class).info("judgeCondition1:" + e.getMessage());
            if (token.equals("=") || token.equals("<>")) {
                ;
            } else {
                return ary;
            }
        }
        try {
            b = Double.parseDouble(rightField);
            isBDouble = true;
        }
        catch (Exception e) {
            Logger.getLogger(WorkflowRouter.class).info("judgeCondition2:" + e.getMessage());
            if (token.equals("=") || token.equals("<>")) {
                ;
            } else {
                return ary;
            }
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
            Logger.getLogger(WorkflowRouter.class).info("rightField=" + rightField + " value=" + value + " isADouble=" + isADouble + " isBDouble=" + isBDouble);

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
                            Logger.getLogger(WorkflowRouter.class).info("i=" + i + " value=" + value + " ary[0]=" + ary[0]);

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
        else {
            return ary;
        }
    }
}
