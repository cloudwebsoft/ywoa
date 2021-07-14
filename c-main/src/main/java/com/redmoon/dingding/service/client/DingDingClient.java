package com.redmoon.dingding.service.client;

import cn.js.fan.util.ErrMsgException;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.service.IDepartmentService;
import com.cloudweb.oa.utils.SpringUtil;
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
            for (DdDepartment dept : _list) {
                DeptDb _parentDept = null;
                if (dept.parentid == Enum.ROOT_DEPT_ID) {
                    _parentDept = new DeptDb(DeptDb.ROOTCODE);
                } else {
                    _parentDept = new DeptDb(dept.parentid);
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
                    dingDUserByDeptToOA(dept.id,lf.getCode());
                }
            }
        } catch (ErrMsgException | ValidateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 同步对应部门下的人员进入OA系统
     * @param dId
     * @param deptCode
     * @throws ErrMsgException
     */
    private void dingDUserByDeptToOA(int dId,String deptCode) throws ErrMsgException {
        UserService _userService = new UserService();
        List<DdUser> list = _userService.usersByDept(dId);
        if (list != null && list.size() > 0) {
            for (DdUser user : list) {
                UserDb _userDb = new UserDb(user.userid);
                if (_userDb == null || !_userDb.isLoaded()) {
                    _userDb.create(user.userid, user.name, Enum.INIT_PWD, user.mobile, deptCode);//新增用户
                    List<Integer> _Departments = user.department;
                    if (_Departments != null && _Departments.size() > 0) {
                        for (int deptId : _Departments) {
                            DeptDb deptDb = null;
                            deptDb = deptId == 1 ? new DeptDb(DeptDb.ROOTCODE) : new DeptDb(deptId);
                            if (deptDb != null && deptDb.isLoaded()) {
                                DeptUserDb _deptUserDb = new DeptUserDb();
                                _deptUserDb.create(deptDb.getCode(), user.userid, "");
                            }
                        }
                    }
                }
            }
        }
    }

    private void OAUserByDeptToDd(String deptCode) {
        UserService _userService = new UserService();
        DeptUserDb dud = new DeptUserDb();
        Vector duv = dud.list(deptCode);
        Iterator uit = duv.iterator();
        // 再加用户
        while (uit.hasNext()) {
            DeptUserDb du = (DeptUserDb) uit.next();
            if (du.getUserName().equals("admin")) {
                continue;
            }
            String userId = du.getUserName();
            UserDb _userDb = new UserDb(userId);
            if (_userDb != null && _userDb.isLoaded()) {
                _userService.createUser(_userDb);
            }
        }
    }

    /**
     * 同步OA部门和用户至钉钉
     */
    public void syncOAtoDingDing(){
        try {
            // 同步根部门下面的人员
            OAUserByDeptToDd(DeptDb.ROOTCODE);
            DepartmentService departmentService = new DepartmentService();
            DeptDb dd = new DeptDb(DeptDb.ROOTCODE);
            Vector vt = new Vector();
            dd.getAllChild(vt,dd);
            Iterator dit = vt.iterator();
            while (dit.hasNext()) {
                DeptDb dept = (DeptDb) dit.next();
                String _dCode = dept.getCode();
                String _name = dept.getName();
                int _parentId = Enum.ROOT_DEPT_ID;
                DeptDb pDept = new DeptDb(dept.getParentCode());
                if(!pDept.getCode().equals(DeptDb.ROOTCODE)){
                    _parentId = pDept.getId();
                }
                departmentService.delDept(dept.getId());
                int _id = departmentService.addDept(_name, _parentId, dept.getOrders());
                if(_id != -1){
                    dept.setId(_id);
                    dept.save();
                    OAUserByDeptToDd(_dCode);
                }
            }
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }
    }

    /**
     * 同步钉钉userId至用户表中dingding
     */
    public static void batchUserAddDingDing() {
        UserService userService = new UserService();
        DepartmentService _deptService = new DepartmentService();

        Config cfg = Config.getInstance();
        int isUserIdUse = cfg.getIntProperty("isUserIdUse");
        // 获取系统中所有部门
        List<DdDepartment> _list = _deptService.allDepartments();
        for(DdDepartment dd:_list){
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
