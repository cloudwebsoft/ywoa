package com.cloudweb.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.cache.UserSetupCache;
import com.cloudweb.oa.dingding.DdUserService;
import com.cloudweb.oa.entity.Role;
import com.cloudweb.oa.entity.UserSetup;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.mapper.UserSetupMapper;
import com.cloudweb.oa.visual.PersonBasicService;
import com.cloudweb.oa.service.IDeptUserService;
import com.cloudweb.oa.service.IRoleService;
import com.cloudweb.oa.service.IUserSetupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.weixin.WxUserService;
import com.redmoon.oa.account.AccountDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sso.SyncUtil;
import com.redmoon.oa.ui.menu.WallpaperDb;
import com.redmoon.weixin.Config;
import com.redmoon.weixin.mgr.WXUserMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.redmoon.dingding.enums.Enum;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author fgf
 * @since 2020-02-10
 */
@Slf4j
@Service
public class UserSetupServiceImpl extends ServiceImpl<UserSetupMapper, UserSetup> implements IUserSetupService {
    @Autowired
    IDeptUserService deptUserService;

    @Autowired
    WxUserService wxUserService;

    @Autowired
    DdUserService ddUserService;

    @Autowired
    PersonBasicService personBasicService;

    @Autowired
    UserSetupMapper userSetupMapper;

    @Autowired
    IRoleService roleService;

    @Autowired
    UserSetupCache userSetupCache;

    @Override
    public UserSetup getUserSetup(String userName) {
        QueryWrapper<UserSetup> qw = new QueryWrapper<>();
        qw.eq("user_name", userName);
        return getOne(qw);
    }

    @Override
    public boolean create(String userName) {
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        int messageToMaxUser = cfg.getInt("message_to_max_user");
        int messageUserMaxCount = cfg.getInt("message_user_max_count");

        Role role = roleService.getRole(ConstUtil.ROLE_MEMBER);
        long diskSpaceAllowed = role.getDiskQuota();
        long msgSpaceAllowed = role.getMsgSpaceQuota();
        if (diskSpaceAllowed == -1) {
            diskSpaceAllowed = 0;
        }
        if (msgSpaceAllowed == -1) {
            msgSpaceAllowed = 0;
        }

        UserSetup userSetup = new UserSetup();
        userSetup.setUserName(userName);
        userSetup.setMessageToDept("");
        userSetup.setMessageToUsergroup("");
        userSetup.setMessageToUserrole("");
        userSetup.setMessageToMaxUser(messageToMaxUser);
        userSetup.setMessageUserMaxCount(messageUserMaxCount);
        userSetup.setSkinCode("");
        userSetup.setMsgSpaceAllowed(msgSpaceAllowed);
        userSetup.setLastMsgNotifyTime(LocalDateTime.now());
        userSetup.setMsgSpaceAllowed(diskSpaceAllowed);
        return userSetup.insert();
    }

    @Override
    public boolean updateByUserName(UserSetup userSetup) {
        QueryWrapper<UserSetup> qw = new QueryWrapper<>();
        qw.eq("user_name", userSetup.getUserName());
        boolean re = userSetup.update(qw);
        if (re) {
            userSetupCache.refreshSave(userSetup.getUserName());
        }
        return re;
    }

    /**
     * 当创建用户及恢复启用时调用
     * @param user
     * @param newDeptCode 恢复启用时为null
     * @param operator
     */
    @Override
    public void onUserCreated(User user, String newDeptCode, String operator) {
        personBasicService.updatePersonbasic(user, newDeptCode, operator, 0);

        com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
        if (ssoCfg.getBooleanProperty("isUse")) {
            SyncUtil su = new SyncUtil();
            su.userSync(user.getName(), newDeptCode, SyncUtil.CREATE, new Privilege().getUser());
        }

        // 同步到微信企业号
        com.redmoon.weixin.Config weixinCfg = Config.getInstance();
        if (weixinCfg.getBooleanProperty("isUse") && !weixinCfg.getBooleanProperty("isSyncWxToOA")) {
            wxUserService.createWxUser(user);
        }
        // 同步至钉钉
        com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
        if (dingdingCfg.isUseDingDing() && !dingdingCfg.getBooleanProperty("isSyncDingDingToOA")) {
            ddUserService.createUser(user);
        }
    }

    @Override
    public void onUserUpdated(User user, String newDeptCode, String operator) {
        personBasicService.updatePersonbasic(user, newDeptCode, operator, 1);

        // 与Spark同步
        com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
        if (ssoCfg.getBooleanProperty("isUse")) {
            SyncUtil su = new SyncUtil();
            su.userSync(user.getName(), newDeptCode, SyncUtil.MODIFY, new Privilege().getUser());
        }

        //同步到微信企业号
        com.redmoon.weixin.Config weixinCfg = Config.getInstance();
        if (weixinCfg.getBooleanProperty("isUse") && !weixinCfg.getBooleanProperty("isSyncWxToOA")) {
            wxUserService.updateWxUser(user);
        }
        com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
        if (dingdingCfg.isUseDingDing() && !dingdingCfg.getBooleanProperty("isSyncDingDingToOA")) {
            ddUserService.updateUser(user);
        }
    }

    @Override
    public void onUserDeleted(User user, String operator) {
        personBasicService.updatePersonbasic(user, null, operator, -1);

        // 从spark中删除
        com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
        if (ssoCfg.getBooleanProperty("isUse")) {
            SyncUtil su = new SyncUtil();
            String deptCode = "";
            su.userSync(user.getName(), deptCode, SyncUtil.DEL, SpringUtil.getUserName());
        }

        // 同步到微信企业号
        com.redmoon.weixin.Config weixinCfg = Config.getInstance();
        if (weixinCfg.getBooleanProperty("isUse") && !weixinCfg.getBooleanProperty("isSyncWxToOA")) {
            String userId = user.getName();
            if (weixinCfg.isUserIdUseEmail()) {
                userId = user.getWeixin();
            } else if (weixinCfg.isUserIdUseAccount()) {
                // 使用工号登录
                AccountDb accountDb = new AccountDb();
                accountDb = accountDb.getUserAccount(userId);
                userId = accountDb.getName();
            } else if (weixinCfg.isUserIdUseMobile()) {
                userId = user.getWeixin();
            }

            WXUserMgr wxUserMgr = new WXUserMgr();
            wxUserMgr.deleteUser(userId);
        }

        com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
        if (dingdingCfg.isUseDingDing() && !dingdingCfg.getBooleanProperty("isSyncDingDingToOA")) {
            String userId;
            int useIdUse = dingdingCfg.isUserIdUse();
            switch (useIdUse) {
                case Enum.emBindAcc.emEmail:
                    userId = user.getEmail();
                    break;
                case Enum.emBindAcc.emMobile:
                    userId = user.getMobile();
                    break;
                case Enum.emBindAcc.emUserName:
                    userId = user.getName();
                    break;
                default:
                    userId = user.getName();
            }
            ddUserService.delUser(userId);
        }
    }

    /**
     * 取得用户头像
     * 注意此方法不能返回空值，否则在lte/index.html中经th:src="@{'/' + ${portrait}}显示时，会致访问 oa/，导致session丢失，点击菜单上任何链接，均进入了登录页
     * @param user
     * @return
     */
    @Override
    public String getPortrait(User user) {
        String str = "";
        if (user.getPhoto()!=null && !"".equals(user.getPhoto())) {
            str = "showImg.do?path=" + user.getPhoto();
        } else {
            if (!user.getGender()) {
                str += "showImgInJar.do?path=static/images/man.png";
            } else {
                str += "showImgInJar.do?path=static/images/woman.png";
            }
        }
        return str;
    }

    /**
     * 取得用户头像，用于前端
     * @param user
     * @return
     */
    @Override
    public String getPortraitForFront(User user) {
        if (user.getPhoto()!=null && !"".equals(user.getPhoto())) {
            return user.getPhoto();
        }
        else {
            return "";
        }
    }

    /**
     * 取得用户的下属
     * @param userName
     * @return
     */
    @Override
    public List<String> getMySubordinates(String userName) {
        return userSetupMapper.getMySubordinates(userName);
    }

    @Override
    public boolean setMyleaders(String userName, String leaders) {
        UserSetup userSetup = getUserSetup(userName);
        userSetup.setMyleaders(leaders);
        boolean re = updateByUserName(userSetup);
        if (re) {
            userSetupCache.refreshSave(userSetup.getUserName());
        }
        return re;
    }

    @Override
    public boolean del(String userName) {
        // 删除UserSetup
        QueryWrapper<UserSetup> qw = new QueryWrapper<>();
        qw.eq("user_name", userName);
        boolean re = remove(qw);
        if (re) {
            userSetupCache.refreshDel(userName);
        }
        return re;
    }

    @Override
    public String getWallpaperPath(String userName) {
        // 如果wallpaper以#号开头，则表示为用户自己上传的壁纸
        UserSetup userSetup = getUserSetup(userName);
        String wallpaper = userSetup.getWallpaper();
        if ("".equals(wallpaper)) {
            return "images/wallpaper/default.jpg";
        } else if (wallpaper.startsWith("#")) {
            WallpaperDb wd = new WallpaperDb();
            String imgPath = wd.getImgPath(userName);
            if (imgPath!=null) {
                imgPath = "upfile/wallpaper/" + imgPath;
                return imgPath;
            }
            else {
                return "images/wallpaper/default.jpg";
            }
        }
        else {
            return "images/wallpaper/" + wallpaper;
        }
    }

    @Override
    public boolean clearToken(String token) {
        QueryWrapper<UserSetup> qw = new QueryWrapper<>();
        qw.eq("token", token);
        UserSetup userSetup = getOne(qw, false);
        boolean re = false;
        if (userSetup != null) {
            qw = new QueryWrapper<>();
            qw.eq("user_name", userSetup.getUserName());
            userSetup.setToken("");
            userSetup.setClient(ConstUtil.CLIENT_NONE);
            re = userSetup.update(qw);
            if (re) {
                userSetupCache.refreshSave(userSetup.getUserName());
            }
        }
        return re;
    }
}
