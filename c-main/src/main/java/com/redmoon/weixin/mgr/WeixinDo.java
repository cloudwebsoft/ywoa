package com.redmoon.weixin.mgr;

import cn.js.fan.util.ErrMsgException;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.service.IDepartmentService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.account.AccountDb;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.weixin.Config;
import com.redmoon.weixin.config.Constant;
import com.redmoon.weixin.util.HttpUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * @Description:
 * @author:
 * @Date: 2016-8-26上午11:20:11
 */
public class WeixinDo {

    /**
     * 一键同步OA部门人员至微信端
     */
    public void syncDeptUsers() {
        /*
        WXMenuMgr wxMenuMgr = new WXMenuMgr();
        wxMenuMgr.batchDeleteMenu();// 批量删除
        wxMenuMgr.batchCreateMenu();// 批量新增菜单按钮
        */
        Config config = Config.getInstance();
        boolean isUserIdUseEmail = config.isUserIdUseEmail();
        boolean isUserIdUseAccount = config.isUserIdUseAccount();
        boolean isUserIdUseMobile = config.isUserIdUseMobile();

        WXDeptMgr wxDeptMgr = new WXDeptMgr();
        WXUserMgr wxUserMgr = new WXUserMgr();

        // 获取所有的部门成员
        JSONArray aryListId = wxDeptMgr.wxUserListId();
        // 获取全量的部门id
        JSONArray aryDeptListId = wxDeptMgr.wxDeptListId();

        IDepartmentService departmentService = SpringUtil.getBean(IDepartmentService.class);
        List<Department> list = departmentService.getDeptsNotHide();
        for (Department dept : list) {
            if (!dept.getCode().equals(DeptDb.ROOTCODE)) {
                // 判断用户是否存在，如存在，则更新
                boolean isDeptExist = false;
                for (int i=0; i<aryDeptListId.length(); i++) {
                    try {
                        JSONObject obj = aryDeptListId.getJSONObject(i);
                        if (dept.getId() == obj.getInt("id")) {
                            isDeptExist = true;
                        }
                    } catch (JSONException e) {
                        LogUtil.getLog(getClass()).error(e);
                    }
                }
                // 不能直接删除部门，因为不允许删除有成员的部门
                // wxDeptMgr.deleteWxDept(dept.getId());
                // 加部门
                if (!isDeptExist) {
                    int errCode = wxDeptMgr.createWxDept(dept);// 创建部门
                    if (errCode != 0) {
                        DebugUtil.e(getClass(), "syncDeptUsers createWxDept errCode", String.valueOf(errCode));
                        // throw new ErrMsgException(wxDeptMgr.getErrMsg());
                    }
                }
            }

            String deptCode = dept.getCode();
            DeptUserDb dud = new DeptUserDb();
            Vector<DeptUserDb> duv = dud.list(deptCode);
            //加用户
            for (DeptUserDb du : duv) {
                if ("admin".equals(du.getUserName())) {
                    continue;
                }
                String userId = du.getUserName();
                UserDb userDb = new UserDb();
                userDb = userDb.getUserDb(du.getUserName());
                if (isUserIdUseAccount) {
                    AccountDb accountDb = new AccountDb();
                    accountDb = accountDb.getUserAccount(userId);
                    userId = accountDb.getName();
                } else if (isUserIdUseEmail) {
                    userId = userDb.getEmail();
                } else if (isUserIdUseMobile) {
                    userId = userDb.getMobile();
                }

                // 判断用户是否存在，如存在，则更新
                boolean isUserExist = false;
                for (int i=0; i<aryListId.length(); i++) {
                    try {
                        JSONObject obj = aryListId.getJSONObject(i);
                        if (userId.equals(obj.getString("userid"))) {
                            isUserExist = true;
                        }
                    } catch (JSONException e) {
                        LogUtil.getLog(getClass()).error(e);
                    }
                }

                /*
                以下方面自2022.8.20已停用
                // wxUserMgr.deleteUser(userId);// 删除用户会删除管理员权限
                String url = Constant.GET_USER_INFO + accessToken + "&userid=" + userId;
                String userInfoAll = HttpUtil.MethodGet(url);
                try {
                    org.json.JSONObject json = new org.json.JSONObject(userInfoAll);
                    if (json.getInt("errcode") != 0) {
                        isUserExist = false;
                    }
                } catch (JSONException e) {
                    LogUtil.getLog(getClass()).error(e);
                }*/
                int errorCode = 0;
                if (!isUserExist) {
                    errorCode = wxUserMgr.createWxUser(userDb); // 创建用户
                } else {
                    errorCode = wxUserMgr.updateWxUser(userDb);
                }
                if (errorCode != 0) {
                    // 60129,"errmsg":"missing mobile or email"
                    if (errorCode == 60129) {
                        throw new ErrMsgException(userDb.getRealName() + ": 个人信息中没有手机号或邮箱");
                    } else {
                        throw new ErrMsgException(userDb.getRealName() + ": " + wxUserMgr.getErrMsg());
                    }
                }
            }
        }
    }

    /**
     * 获取微信用户名密码至OA
     */
    public void syncWxDeptUserToOA() {
        WXDeptMgr _wxDeptMgr = new WXDeptMgr();
        WXUserMgr _wxUserMgr = new WXUserMgr();
        UserDeptByWXMgr _userDeptByWx = new UserDeptByWXMgr();
        JSONArray _jsonDeptArr = _wxDeptMgr.wxDeptList();
        if (_jsonDeptArr != null && _jsonDeptArr.length() > 0) {
            for (int i = 0; i < _jsonDeptArr.length(); i++) {
                try {
                    org.json.JSONObject _jsonDept = _jsonDeptArr.getJSONObject(i);
                    HashMap<String, String> _deptMap = new HashMap<String, String>();
                    int _id = _jsonDept.getInt("id");
                    _deptMap.put("Id", Integer.toString(_id));
                    _deptMap.put("ParentId", Integer.toString(_jsonDept.getInt("parentid")));
                    _deptMap.put("Name", _jsonDept.getString("name"));
                    boolean _res = _userDeptByWx.createDept(_deptMap);
                    if (_res) {
                        JSONArray _jsonUserArr = _wxUserMgr.wxDeptUserList(_id);//批量获取部门下用户
                        if (_jsonUserArr != null && _jsonUserArr.length() > 0) {
                            for (int j = 0; j < _jsonUserArr.length(); j++) {
                                org.json.JSONObject _jsonUser = _jsonUserArr.getJSONObject(j);
                                HashMap<String, String> _userMap = new HashMap<String, String>();
                                String _userid = _jsonUser.getString("userid");
                                String _name = _jsonUser.getString("name");
                                JSONArray _arr = _jsonUser.getJSONArray("department");
                                StringBuilder _deptSb = new StringBuilder();
                                for (int k = 0; k < _arr.length(); k++) {
                                    int id = _arr.getInt(k);
                                    if (_deptSb == null || _deptSb.toString().equals("")) {
                                        _deptSb.append(id);
                                    } else {
                                        _deptSb.append(",").append(id);
                                    }
                                }
                                String mobile = _jsonUser.getString("mobile");
                                String _email = _jsonUser.getString("email");
                                int _gender = _jsonUser.getInt("gender");
                                String _telephone = _jsonUser.getString("telephone");
                                _userMap.put("UserID", _userid);
                                _userMap.put("Name", _name);
                                _userMap.put("Mobile", mobile);
                                _userMap.put("Email", _email);
                                _userMap.put("Gender", Integer.toString(_gender));
                                _userMap.put("Telephone", _telephone);
                                _userMap.put("Department", _deptSb.toString());
                                _userDeptByWx.createUser(_userMap);
                            }
                        }
                    }
                } catch (JSONException | ErrMsgException e) {
                    LogUtil.getLog(WeixinDo.class).info("微信企业号获取部门列表异常" + e.getMessage());
                    LogUtil.getLog(getClass()).error(e);
                }
            }

        }
    }

}
