package com.redmoon.weixin.mgr;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.organization.DeptTreeAction;
import com.redmoon.weixin.config.Constant;
import com.redmoon.weixin.util.HttpUtil;
import net.sf.json.JSONObject;

import cn.js.fan.util.ErrMsgException;

import com.redmoon.oa.account.AccountDb;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.dept.DeptUserMgr;
import com.redmoon.oa.person.UserDb;
import com.redmoon.weixin.Config;
import com.redmoon.weixin.bean.WxDept;
import com.redmoon.weixin.bean.WxUser;
import org.json.JSONArray;
import org.json.JSONException;

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
        WXMenuMgr wxMenuMgr = new WXMenuMgr();
        wxMenuMgr.batchDeleteMenu();// 批量删除
        wxMenuMgr.batchCreateMenu();// 批量新增菜单按钮
        Config config = Config.getInstance();
        boolean isUserIdUseEmail = config.isUserIdUseEmail();
        boolean isUserIdUseAccount = config.isUserIdUseAccount();
        boolean isUserIdUseMobile = config.isUserIdUseMobile();

        WXUserMgr wum = new WXUserMgr();
        String accessToken = wum.getTokenContacts();

        WXDeptMgr wxDeptMgr = new WXDeptMgr();
        WXUserMgr wxUserMgr = new WXUserMgr();
        DeptDb dd = new DeptDb();
        String sql = "select code from department where is_hide=0 order by orders asc";
        Vector dv = dd.list(sql);
        Iterator dit = dv.iterator();
        while (dit.hasNext()) {
            DeptDb dept = (DeptDb) dit.next();
            if (!dept.getCode().equals(DeptDb.ROOTCODE)) {
                // 加部门
                wxDeptMgr.deleteWxDept(dept.getId()); // 删除部门
                wxDeptMgr.createWxDept(dept);// 创建部门
            }

            String deptCode = dept.getCode();
            DeptUserDb dud = new DeptUserDb();
            Vector duv = dud.list(deptCode);
            Iterator uit = duv.iterator();
            //加用户
            while (uit.hasNext()) {
                DeptUserDb du = (DeptUserDb) uit.next();
                if (du.getUserName().equals("admin")) {
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
                boolean isUserExist = true;
                // wxUserMgr.deleteUser(userId);// 删除用户会删除管理员权限
                String url = Constant.GET_USER_INFO + accessToken + "&userid=" + userId;
                String userInfoAll = HttpUtil.MethodGet(url);
                try {
                    org.json.JSONObject json = new org.json.JSONObject(userInfoAll);
                    if (json.getInt("errcode")!=0) {
                        isUserExist = false;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (!isUserExist) {
                    wxUserMgr.createWxUser(userDb); // 创建用户
                }
                else {
                    wxUserMgr.updateWxUser(userDb);
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
                                    if (_deptSb == null || _deptSb.toString().equals(""))
                                        _deptSb.append(id);
                                    else
                                        _deptSb.append(",").append(id);
                                }
                                String _mobile = _jsonUser.getString("mobile");
                                String _email = _jsonUser.getString("email");
                                int _gender = _jsonUser.getInt("gender");
                                String _telephone = _jsonUser.getString("telephone");
                                _userMap.put("UserID", _userid);
                                _userMap.put("Name", _name);
                                _userMap.put("Mobile", _mobile);
                                _userMap.put("Email", _email);
                                _userMap.put("Gender", Integer.toString(_gender));
                                _userMap.put("Telephone", _telephone);
                                _userMap.put("Department", _deptSb.toString());
                                _userDeptByWx.createUser(_userMap);
                            }
                        }
                    }
                } catch (JSONException e) {
                    LogUtil.getLog(WeixinDo.class).info("微信企业号获取部门列表异常" + e.getMessage());
                } catch (ErrMsgException e) {
                    LogUtil.getLog(WeixinDo.class).info("微信企业号获取部门列表异常" + e.getMessage());
                }
            }

        }
    }

}
