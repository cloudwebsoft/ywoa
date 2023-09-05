package com.redmoon.dingding.service.user;

import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.dingding.Config;
import com.redmoon.dingding.domain.BaseDdObj;
import com.redmoon.dingding.domain.DdUser;
import com.redmoon.dingding.domain.UserInfoDto;
import com.redmoon.dingding.enums.Enum;
import com.redmoon.dingding.service.BaseService;
import com.redmoon.dingding.service.user.dto.DdUserDto;
import com.redmoon.dingding.util.HttpHelper;
import com.redmoon.dingding.util.DdException;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.sys.DebugUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class UserService extends BaseService {
    /**
     * 获得用户详情
     *
     * @param userId
     * @return
     */
    public DdUser getUser(String userId) {
        DdUser _user = null;
        try {
            HttpHelper _http = new HttpHelper(URL_USER_GET + "userid=" + userId + "&");
            _user = _http.httpGet(DdUser.class);
        } catch (DdException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        return _user;
    }

    /**
     * 免登录方式获取用户id
     *
     * @param code
     * @return
     */
    public UserDb getUserByAvoidLogin(String code) {
        UserDb user = null;
        try {
            HttpHelper http = new HttpHelper("https://oapi.dingtalk.com/user/getuserinfo?code=" + code + "&");
            UserInfoDto userInfoDto = http.httpGet(UserInfoDto.class);
            if (userInfoDto != null) {
                DebugUtil.i(getClass(), "getUserByAvoidLogin", userInfoDto.toString());
                String userId = userInfoDto.userid;
                Config config = Config.getInstance();
                int isUserIdUse = config.isUserIdUse();
                switch (isUserIdUse) {
                    case Enum.emBindAcc.emUserName:
                        user = new UserDb(userId);
                        break;
                    case Enum.emBindAcc.emEmail:
                        DdUser ddUser = getUser(userId); //获得ddUser详细对象
                        if (ddUser != null) {
                            String email = StrUtil.getNullStr(ddUser.email);
                            if (!"".equals(email)) {
                                user = new UserDb();
                                user = user.getUserDbByEmail(email);
                            }
                        }
                        break;
                    case Enum.emBindAcc.emMobile:
                        DdUser ddUser2 = getUser(userId); //获得ddUser详细对象
                        if (ddUser2 != null) {
                            String _mobile = ddUser2.mobile;
                            user = new UserDb();
                            user = user.getUserDbByMobile(_mobile);
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (DdException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return user;
    }

    /**
     * 删除用户
     *
     * @param userId
     * @return
     */
    public boolean delUser(String userId) {
        boolean _flag = false;
        try {
            HttpHelper _http = new HttpHelper(URL_USER_DELETE + "userid=" + userId + "&");
            _http.httpGet(BaseDdObj.class);
            _flag = true;
        } catch (DdException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        return _flag;
    }

    /**
     * 创建用户
     * @param userDb
     * @return
     */
    public boolean createUser(UserDb userDb) {
        boolean _flag = false;
        HttpHelper _http = new HttpHelper(URL_USER_CREATE);
        DdUser _ddUser = new DdUser();
        Config config = Config.getInstance();
        int isUserIdUse = config.isUserIdUse();
        String userId = "";
        switch (isUserIdUse) {
            case Enum.emBindAcc.emUserName:
                userId = userDb.getName();
                break;
            case Enum.emBindAcc.emEmail:
                userId = userDb.getEmail();
                break;
            case Enum.emBindAcc.emMobile:
                userId = userDb.getMobile();
                break;
            default:
                userId = userDb.getName();
                break;
        }

        userDb.setDingding(userId);
        userDb.save();

        _ddUser.userid = userId;
        _ddUser.name = userDb.getRealName();
        _ddUser.mobile = userDb.getMobile();
        _ddUser.email = userDb.getEmail();
        DeptUserDb dud = new DeptUserDb();
        List<Integer> _list = new ArrayList<Integer>();
        Vector<DeptDb> vector = dud.getDeptsOfUser(userDb.getName());
        Iterator<DeptDb> it = vector.iterator();
        while (it.hasNext()) {
            DeptDb dd = it.next();
            int id = Enum.ROOT_DEPT_ID;
            if(!dd.getCode().equals(DeptDb.ROOTCODE)){
                id = dd.getId();
            }
            _list.add(id);
            DebugUtil.log(getClass(), "createUser", userDb.getRealName() + " deptId=" + id);
        }
        _ddUser.department = _list;
        try {
            _http.httpPost(BaseDdObj.class, _ddUser);
            _flag = true;
        } catch (DdException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return _flag;
    }

    /**
     *更新部門用戶
     * @param userDb
     * @return
     */
    public  boolean updateUser(UserDb userDb) {
        boolean _flag = false;
        HttpHelper _http = new HttpHelper(URL_USER_UPDATE);
        DdUser _ddUser = new DdUser();
        Config config = Config.getInstance();
        int isUserIdUse = config.isUserIdUse();
        String userId = "";
        switch (isUserIdUse) {
            case Enum.emBindAcc.emUserName:
                userId = userDb.getName();
                break;
            case Enum.emBindAcc.emEmail:
                userId = userDb.getEmail();
                break;
            case Enum.emBindAcc.emMobile:
                userId = userDb.getMobile();
                break;
            default:
                userId = userDb.getName();
                break;

        }
        _ddUser.userid = userId;
        _ddUser.name = userDb.getRealName();
        _ddUser.mobile = userDb.getMobile();
        _ddUser.email = userDb.getEmail();
        DeptUserDb dud = new DeptUserDb();
        List<Integer> _list = new ArrayList<Integer>();
        Vector<DeptDb> vector = dud.getDeptsOfUser(userDb.getName());
        Iterator<DeptDb> it = vector.iterator();
        while (it.hasNext()) {
            DeptDb dd = it.next();
            _list.add(dd.getId());
        }
        _ddUser.department = _list;
        try {
            _http.httpPost(BaseDdObj.class, _ddUser);
            _flag = true;
        } catch (DdException e) {
        }
        return _flag;
    }

    /**
     * 部门下用户
     * @param department_id
     * @return
     */
    public List<DdUser> usersByDept(int department_id){
        HttpHelper _http = new HttpHelper(URL_USER_LIST+"department_id="+department_id+"&");
        List<DdUser> _list = null;
        try {
            DdUserDto ddUserDto = _http.httpGet(DdUserDto.class);
            if(ddUserDto!=null){
                _list = ddUserDto.userlist;
            }
        } catch (DdException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return _list;
    }



}
