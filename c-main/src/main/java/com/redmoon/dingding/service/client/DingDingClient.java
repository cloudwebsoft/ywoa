package com.redmoon.dingding.service.client;

import cn.js.fan.util.ErrMsgException;
import com.alibaba.fastjson.JSON;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.service.IDepartmentService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.dingding.Config;
import com.redmoon.dingding.domain.DdDepartment;
import com.redmoon.dingding.domain.DdUser;
import com.redmoon.dingding.enums.Enum;
import com.redmoon.dingding.service.department.DepartmentService;
import com.redmoon.dingding.service.eventchange.EventChangeService;
import com.redmoon.dingding.service.user.UserService;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.organization.DeptTreeAction;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.sys.DebugUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class DingDingClient {
    /**
     * 获取钉钉用户名密码至OA
     */
    public void syncDingDingToOA() {
        try {
            DepartmentService _deptService = new DepartmentService();
            //同步根目录下人员
            dingDUserByDeptToOA(Enum.ROOT_DEPT_ID,DeptDb.ROOTCODE);
            //获取系统中所有部门
            List<DdDepartment> _list = _deptService.allDepartments();
            DebugUtil.i(getClass(), "syncDingDingToOA", JSON.toJSONString(_list));

            int c = 0;
            while (_list.size() > 0) {
                Iterator<DdDepartment> ir = _list.iterator();
                while (ir.hasNext()) {
                    DdDepartment dept = ir.next();
                    // for (DdDepartment dept : _list) {
                    DebugUtil.i(getClass(), "syncDingDingToOA", dept.id + "," + dept.name + "," + dept.parentid + "," + dept.order);
                    DeptDb _parentDept = null;
                    if (dept.parentid == Enum.ROOT_DEPT_ID) {
                        _parentDept = new DeptDb(DeptDb.ROOTCODE);
                    } else {
                        _parentDept = new DeptDb(dept.parentid);
                        // 如果父节点不存在，则继续处理
                        if (!_parentDept.isLoaded()) {
                            continue;
                        }
                    }
                    DeptTreeAction _deptTreeAction = new DeptTreeAction();
                    _deptTreeAction.generateNewNodeCode(_parentDept.getCode());
                    String _newCode = _deptTreeAction.getNewNodeCode();

                    Department lf = new Department();
                    lf.setId(dept.id);
                    lf.setName(dept.name);
                    lf.setCode(_newCode);
                    lf.setParentCode(_parentDept.getCode());
                    lf.setDescription("");
                    lf.setDeptType(1);
                    lf.setIsShow(1);
                    lf.setShortName(dept.name);
                    lf.setIsGroup(0);
                    lf.setIsHide(0);
                    IDepartmentService departmentService = SpringUtil.getBean(IDepartmentService.class);
                    boolean flag = departmentService.create(lf);
                    if (flag) {
                        dingDUserByDeptToOA(dept.id, lf.getCode());
                    }
                    // 处理完一个就从列表中删除一个
                    ir.remove();
                }
                c++;
                // 防止死循环
                // if (c > 1000) {
                //     break;
                // }
            }
        } catch (ErrMsgException | ValidateException e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }

    /**
     * 同步对应部门下的人员进入OA系统
     * @param dId
     * @param deptCode
     * @throws ErrMsgException
     */
    private void dingDUserByDeptToOA(int dId,String deptCode) throws ErrMsgException {
        Config config = Config.getInstance();
        int isUserIdUse = config.isUserIdUse();

        UserService _userService = new UserService();
        List<DdUser> list = _userService.usersByDept(dId);
        if (list != null && list.size() > 0) {
            for (DdUser user : list) {
                String userId;
                switch (isUserIdUse) {
                    case Enum.emBindAcc.emUserName:
                        userId = user.userid;
                        break;
                    case Enum.emBindAcc.emEmail:
                        userId = user.email;
                        break;
                    case Enum.emBindAcc.emMobile:
                        userId = user.mobile;
                        break;
                    default:
                        userId = user.userid;
                        break;
                }

                UserDb userDb = new UserDb(userId);
                if (!userDb.isLoaded()) {
                    userDb.create(userId, user.name, Enum.INIT_PWD, user.mobile, deptCode);//新增用户
                    List<Integer> Departments = user.department;
                    if (Departments != null && Departments.size() > 0) {
                        for (int deptId : Departments) {
                            DeptDb deptDb = deptId == 1 ? new DeptDb(DeptDb.ROOTCODE) : new DeptDb(deptId);
                            if (deptDb.isLoaded()) {
                                DeptUserDb deptUserDb = new DeptUserDb();
                                deptUserDb.create(deptDb.getCode(), userId, "");
                            }
                        }
                    }
                }
            }
        }
    }

    private void OAUserByDeptToDd(String deptCode) {
        UserService userService = new UserService();
        DeptUserDb dud = new DeptUserDb();
        UserDb userDb = new UserDb();
        Vector<DeptUserDb> duv = dud.list(deptCode);
        // 加用户
        for (DeptUserDb du : duv) {
            if ("admin".equals(du.getUserName())) {
                continue;
            }
            String userId = du.getUserName();
            userDb = userDb.getUserDb(userId);
            if (userDb.isLoaded()) {
                userService.createUser(userDb);
            }
        }
    }

    /**
     * 同步OA部门和用户至钉钉
     */
    public void syncOAtoDingDing(){
        DepartmentService deptService = new DepartmentService();
        List<DdDepartment> allDepts = deptService.allDepartments();

        // 同步根部门下面的人员
        OAUserByDeptToDd(DeptDb.ROOTCODE);
        DeptDb pDept = new DeptDb();
        DepartmentService departmentService = new DepartmentService();
        DeptDb dd = new DeptDb(DeptDb.ROOTCODE);
        Vector vt = new Vector();
        dd.getAllChild(vt, dd);
        Iterator dit = vt.iterator();
        while (dit.hasNext()) {
            DeptDb dept = (DeptDb) dit.next();
            String _dCode = dept.getCode();
            String _name = dept.getName();
            int parentId = Enum.ROOT_DEPT_ID;
            pDept = pDept.getDeptDb(dept.getParentCode());
            if (!pDept.getCode().equals(DeptDb.ROOTCODE)) {
                parentId = pDept.getId();
            }

            boolean isFound = false;
            for (DdDepartment ddDepartment : allDepts) {
                if (ddDepartment.id == dept.getId()) {
                    isFound = true;
                    break;
                }
            }
            // 不能直接删除，可能会报：error code: 60003, error message: 部门不存在
            // departmentService.delDept(dept.getId());
            if (!isFound) {
                int id = departmentService.addDept(_name, parentId, dept.getOrders());
                DebugUtil.i(getClass(), "syncOAtoDingDing addDept id", String.valueOf(id));
                if (id != -1) {
                    dept.setId(id);
                    dept.save();
                    OAUserByDeptToDd(_dCode);
                }
            }
        }
    }

    /**
     * 同步钉钉userId至用户表中dingding
     */
    public static void batchUserAddDingDing() {
        UserService userService = new UserService();
        DepartmentService deptService = new DepartmentService();

        Config cfg = Config.getInstance();
        int isUserIdUse = cfg.getIntProperty("isUserIdUse");
        // 获取系统中所有部门
        List<DdDepartment> list = deptService.allDepartments();
        for(DdDepartment dd:list){
            List<DdUser> users = userService.usersByDept(dd.id);
            if (users != null && users.size() > 0) {
                for (DdUser user : users) {
                    UserDb userDb = new UserDb();
                    if (isUserIdUse==3) {
                        userDb = userDb.getUserDbByMobile(user.mobile);
                    }
                    else if (isUserIdUse==2) {
                        userDb = userDb.getUserDbByEmail(user.email);
                    }
                    if(userDb!=null && userDb.isLoaded()){
                        userDb.setDingding(user.userid);
                        userDb.save();
                    }
                }
            }
        }
    }

}
