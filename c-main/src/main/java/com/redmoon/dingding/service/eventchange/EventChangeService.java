package com.redmoon.dingding.service.eventchange;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.service.IDepartmentService;
import com.cloudweb.oa.service.IDeptUserService;
import com.cloudweb.oa.service.IUserService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.dingding.Config;
import com.redmoon.dingding.domain.BaseDdObj;
import com.redmoon.dingding.domain.DdUser;
import com.redmoon.dingding.enums.Enum;
import com.redmoon.dingding.service.BaseService;
import com.redmoon.dingding.service.department.DepartmentService;
import com.redmoon.dingding.service.department.dto.DdDeptDetailDto;
import com.redmoon.dingding.service.eventchange.dto.EventChangeDto;
import com.redmoon.dingding.service.eventchange.dto.RegCallbackDto;
import com.redmoon.dingding.service.user.UserService;
import com.redmoon.dingding.util.DdException;
import com.redmoon.dingding.util.HttpHelper;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.organization.DeptTreeAction;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;

import java.util.List;

public class EventChangeService extends BaseService {
    /**
     * 注册事件更新
     * user_add_org : 通讯录用户增加
     * user_modify_org : 通讯录用户更改
     * user_leave_org : 通讯录用户离职
     * org_admin_add ：通讯录用户被设为管理员
     * org_admin_remove ：通讯录用户被取消设置管理员
     * org_dept_create ： 通讯录企业部门创建
     * org_dept_modify ： 通讯录企业部门修改
     * org_dept_remove ： 通讯录企业部门删除
     *
     * @return
     */
    public static boolean registerEventChange() {
        boolean _flag = false;
        try {
            Config _config = Config.getInstance();
            RegCallbackDto regCallbackDto = new RegCallbackDto();
            regCallbackDto.aes_key = _config.getAesKey();
            regCallbackDto.token = _config.getToken();
            regCallbackDto.url = _config.getEventChangeReviceURL();
            regCallbackDto.call_back_tag = new String[]{"user_add_org", "user_modify_org", "org_dept_create", "org_dept_modify", "org_dept_remove", "user_leave_org"};
            HttpHelper _httpPost = new HttpHelper(REGISTER_CALL_BACK);
            _httpPost.httpPost(BaseDdObj.class, regCallbackDto);
            _flag = true;
        } catch (DdException e) {
            LogUtil.getLog(EventChangeService.class).error(e);
        }
        return _flag;
    }

    /**
     * 查询注册回调事件
     * @return
     */
    public static String getEventCallBack() {
        String _result = "";
        HttpHelper _http = new HttpHelper(GET_CALL_BACK);
        try {
            _result = _http.httpGet();
        } catch (DdException e) {
        }
        return _result;
    }

    /**
     * 删除回调事件
     * @return
     */
    public static boolean deleteEventCallBack(){
        boolean _flag = true;
        HttpHelper _http = new HttpHelper(DELETE_CALL_BACK);
        try {
            _http.httpGet(BaseDdObj.class);
        } catch (DdException e) {
            LogUtil.getLog(EventChangeService.class).error(e);
        }
        return _flag;
    }

    public static void disposeEventChange(String plainText) {
        com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
        if(!dingdingCfg.isUseDingDing()) {
            return;
        }
        if (!dingdingCfg.getBooleanProperty("isSyncDingDingToOA")) {
            return;
        }
        try {
            DepartmentService _departmentService = new DepartmentService();
            UserService _userService = new UserService();
            EventChangeDto _eventChangeDto = JSONObject.parseObject(plainText, EventChangeDto.class);
            String _eventType = _eventChangeDto.EventType;
            /*user_add_org : 通讯录用户增加
            user_modify_org : 通讯录用户更改
            user_leave_org : 通讯录用户离职
            org_admin_add ：通讯录用户被设为管理员
            org_admin_remove ：通讯录用户被取消设置管理员
            org_dept_create ： 通讯录企业部门创建
            org_dept_modify ： 通讯录企业部门修改
            org_dept_remove ： 通讯录企业部门删除*/
            if (_eventType.equals("user_add_org")) {
                String[] userIds = _eventChangeDto.UserId;
                if (userIds != null && userIds.length > 0) {
                    for (String userId : userIds) {
                        DdUser ddUser = _userService.getUser(userId);
                        UserDb _userDb = new UserDb(userId);
                        if (_userDb != null && _userDb.isLoaded()) {
                            return;
                        }
                        //创建用户
                        _userDb = new UserDb();
                        _userDb.create(ddUser.userid, ddUser.name, Enum.INIT_PWD, ddUser.mobile, DeptDb.ROOTCODE);//新增用户
                        //更新邮件
                        String _email = StrUtil.getNullStr(ddUser.email);
                        if (!_email.equals("")) {
                            _userDb.setEmail(_email);
                            _userDb.setDingding(ddUser.userid);
                            _userDb.save();
                        }
                        //同步部门
                        List<Integer> _Departments = ddUser.department;
                        if (_Departments != null && _Departments.size() > 0) {
                            for (Integer deptId : _Departments) {
                                DeptDb deptDb = null;
                                deptDb = deptId == 1 ? new DeptDb(DeptDb.ROOTCODE) : new DeptDb(deptId);
                                if (deptDb != null && deptDb.isLoaded()) {
                                    DeptUserDb _deptUserDb = new DeptUserDb();
                                    _deptUserDb.create(deptDb.getCode(), userId, "");
                                }
                            }
                        }
                    }
                }
            } else if (_eventType.equals("user_modify_org")) {
                String[] userIds = _eventChangeDto.UserId;
                if (userIds != null && userIds.length > 0) {
                    for (String userId : userIds) {
                        DdUser ddUser = _userService.getUser(userId);
                        UserDb _userDb = new UserDb(userId);
                        if (_userDb != null && _userDb.isLoaded()) {
                            _userDb.setEmail(ddUser.email);
                            _userDb.setRealName(ddUser.name);
                            _userDb.save();

                            IDeptUserService deptUserService = SpringUtil.getBean(IDeptUserService.class);
                            deptUserService.delOfUser(userId);

                            //同步部门
                            List<Integer> _Departments = ddUser.department;
                            if (_Departments != null && _Departments.size() > 0) {
                                for (Integer deptId : _Departments) {
                                    DeptDb deptDb = null;
                                    deptDb = deptId == 1 ? new DeptDb(DeptDb.ROOTCODE) : new DeptDb(deptId);
                                    if (deptDb != null && deptDb.isLoaded()) {
                                        DeptUserDb _deptUserDb = new DeptUserDb();
                                        _deptUserDb.create(deptDb.getCode(), userId, "");
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (_eventType.equals("user_leave_org")) {
                String[] userIds = _eventChangeDto.UserId;
                IUserService userService = SpringUtil.getBean(IUserService.class);
                if (userIds != null && userIds.length > 0) {
                    userService.delUsers(userIds);
                }
            } else if (_eventType.equals("org_dept_create")) {
                Integer[] deptIds = _eventChangeDto.DeptId;
                if (deptIds != null && deptIds.length > 0) {
                    for (Integer _Id : deptIds) { //新建的部門id
                        DeptDb deptDb = new DeptDb(_Id);
                        if (deptDb != null && deptDb.isLoaded()) {
                            return;
                        }
                        DdDeptDetailDto _deptDtail = _departmentService.getDept(_Id);
                        int _parentId = _deptDtail.parentid;
                        String _name = _deptDtail.name;
                        DeptDb _pDept = null;
                        if (_parentId == Enum.ROOT_DEPT_ID) {
                            _pDept = new DeptDb(DeptDb.ROOTCODE);
                        } else
                            _pDept = new DeptDb(_parentId);
                        if (_pDept != null && _pDept.isLoaded()) {
                            DeptTreeAction _deptTreeAction = new DeptTreeAction();
                            _deptTreeAction.generateNewNodeCode(_pDept.getCode());
                            String _newCode = _deptTreeAction.getNewNodeCode();

                            Department lf = new Department();
                            lf.setId(_Id);
                            lf.setName(_name);
                            lf.setCode(_newCode);
                            lf.setParentCode(_pDept.getCode());
                            lf.setDescription("");
                            lf.setDeptType(1);
                            lf.setIsShow(1);
                            lf.setShortName(_name);
                            lf.setIsGroup(0);
                            lf.setIsHide(0);
                            IDepartmentService departmentService = SpringUtil.getBean(IDepartmentService.class);
                            departmentService.create(lf);
                        }
                    }
                }
            } else if (_eventType.equals("org_dept_modify")) {
                Integer[] deptIds = _eventChangeDto.DeptId;
                if (deptIds != null && deptIds.length > 0) {
                    for (Integer _id : deptIds) {
                        DdDeptDetailDto _deptDtail = _departmentService.getDept(_id);
                        String _name = _deptDtail.name;
                        int _parentId = _deptDtail.parentid;

                        IDepartmentService departmentService = SpringUtil.getBean(IDepartmentService.class);
                        Department department = departmentService.getById(_id);

                        DeptDb _pDept = null;
                        if (department != null) {
                            department.setName(_name);
                            departmentService.updateByCode(department);
                            if (_parentId == Enum.ROOT_DEPT_ID) {
                                _pDept = new DeptDb(DeptDb.ROOTCODE);
                            } else {
                                _pDept = new DeptDb(_parentId);
                            }
                            if (_pDept != null && _pDept.isLoaded()) {
                                departmentService.setNewParent(department, _pDept.getCode());
                            }
                        }
                    }
                }
            } else if (_eventType.equals("org_dept_remove")) {
                Integer[] deptIds = _eventChangeDto.DeptId;
                if (deptIds != null && deptIds.length > 0) {
                    IDepartmentService departmentService = SpringUtil.getBean(IDepartmentService.class);
                    for (Integer id : deptIds) {
                        Department department = departmentService.getById(id);
                        departmentService.delWithChildren(department.getCode(), true);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(EventChangeService.class).error(e);
        }
    }
}
