package com.cloudweb.oa.dingding;

import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.service.IDepartmentService;
import com.cloudweb.oa.service.IUserService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.dingding.Config;
import com.redmoon.dingding.domain.BaseDdObj;
import com.redmoon.dingding.domain.DdUser;
import com.redmoon.dingding.domain.UserInfoDto;
import com.redmoon.dingding.enums.Enum;
import com.redmoon.dingding.service.BaseService;
import com.redmoon.dingding.service.user.UserService;
import com.redmoon.dingding.service.user.dto.DdUserDto;
import com.redmoon.dingding.util.DdException;
import com.redmoon.dingding.util.HttpHelper;
import com.redmoon.oa.ui.SkinMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class DdUserService extends BaseService {

    @Autowired
    IUserService usersService;

    @Autowired
    IDepartmentService departmentService;

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
     * 免登錄方式获取用户id
     *
     * @param code
     * @return
     */
    public User getUserByAvoidLogin(String code) {
        User user = null;
        try {
            HttpHelper http = new HttpHelper("https://oapi.dingtalk.com/user/getuserinfo?code=" + code + "&");
            UserInfoDto userInfoDto = http.httpGet(UserInfoDto.class);
            if (userInfoDto != null) {
                String userId = userInfoDto.userid;
                Config _config = Config.getInstance();
                int isUserIdUse = _config.isUserIdUse();
                switch (isUserIdUse) {
                    case Enum.emBindAcc.emUserName:
                        user = usersService.getUser(userId);
                        break;
                    case Enum.emBindAcc.emEmail:
                        DdUser _ddUser = getUser(userId); //获得ddUser详细对象
                        if (_ddUser != null) {
                            String email = StrUtil.getNullStr(_ddUser.email);
                            if (!email.equals("")) {
                                user = usersService.getUserByEmail(email);
                            }
                        }
                        break;
                    case Enum.emBindAcc.emMobile:
                        DdUser ddUser = getUser(userId); //获得ddUser详细对象
                        if (ddUser != null) {
                            user = usersService.getUserByMobile(ddUser.mobile);
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
     * 创建用户
     * @param user
     * @return
     */
    public boolean createUser(User user) {
        boolean re = false;
        HttpHelper http = new HttpHelper(URL_USER_CREATE);
        DdUser ddUser = new DdUser();
        Config config = Config.getInstance();
        int isUserIdUse = config.isUserIdUse();
        String userId = "";
        switch (isUserIdUse) {
            case Enum.emBindAcc.emUserName:
                userId = user.getName();
                break;
            case Enum.emBindAcc.emEmail:
                userId = user.getEmail();
                break;
            case Enum.emBindAcc.emMobile:
                userId = user.getMobile();
                break;
            default:
                userId = user.getName();
                break;
        }

        user.setDingding(userId);
        user.updateById();

        ddUser.userid = userId;
        ddUser.name = user.getRealName();
        ddUser.mobile = user.getMobile();
        ddUser.email = user.getEmail();

        List<Integer> idList = new ArrayList<Integer>();
        List<Department> list = departmentService.getDeptsOfUser(user.getName());
        for (Department dept : list) {
            int id = Enum.ROOT_DEPT_ID;
            if(!dept.getCode().equals(ConstUtil.DEPT_ROOT)){
                id = dept.getId();
            }
            idList.add(id);
            log.info("createUser: " + user.getRealName() + " deptId=" + id);
        }

        ddUser.department = idList;
        try {
            http.httpPost(BaseDdObj.class, ddUser);
            re = true;
        } catch (DdException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return re;
    }

    /**
     * 删除用户
     *
     * @param userId
     * @return
     */
    public boolean delUser(String userId) {
        boolean re = false;
        try {
            HttpHelper http = new HttpHelper(URL_USER_DELETE + "userid=" + userId + "&");
            http.httpGet(BaseDdObj.class);
            re = true;
        } catch (DdException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        return re;
    }

    /**
     *更新部门用戶
     * @param user
     * @return
     */
    public  boolean updateUser(User user) {
        boolean re = false;
        HttpHelper _http = new HttpHelper(URL_USER_UPDATE);
        DdUser ddUser = new DdUser();
        Config config = Config.getInstance();
        int isUserIdUse = config.isUserIdUse();
        String userId = "";
        switch (isUserIdUse) {
            case Enum.emBindAcc.emUserName:
                userId = user.getName();
                break;
            case Enum.emBindAcc.emEmail:
                userId = user.getEmail();
                break;
            case Enum.emBindAcc.emMobile:
                userId = user.getMobile();
                break;
            default:
                userId = user.getName();
                break;

        }
        ddUser.userid = userId;
        ddUser.name = user.getRealName();
        ddUser.mobile = user.getMobile();
        ddUser.email = user.getEmail();

        List<Integer> idList = new ArrayList<>();
        List<Department> list = departmentService.getDeptsOfUser(user.getName());
        for (Department dept : list) {
            idList.add(dept.getId());
        }

        ddUser.department = idList;
        try {
            _http.httpPost(BaseDdObj.class, ddUser);
            re = true;
        } catch (DdException e) {
        }
        return re;
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
