package com.cloudweb.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.entity.OaPortal;
import com.cloudweb.oa.entity.UserDesktopSetup;
import com.cloudweb.oa.entity.UserSetup;
import com.cloudweb.oa.mapper.OaPortalMapper;
import com.cloudweb.oa.service.IOaPortalService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.service.IUserDesktopSetupService;
import com.cloudweb.oa.service.IUserSetupService;
import com.cloudweb.oa.utils.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author fgf
 * @since 2020-02-12
 */
@Service
public class OaPortalServiceImpl extends ServiceImpl<OaPortalMapper, OaPortal> implements IOaPortalService {
    @Autowired
    IUserDesktopSetupService userDesktopSetupService;

    @Autowired
    IUserSetupService userSetupService;

    @Override
    public void delOfUser(String userName) {
        if (!userName.equals(ConstUtil.USER_SYSTEM)) {
            QueryWrapper<OaPortal> qw = new QueryWrapper<>();
            qw.eq("user_name", userName);
            List<OaPortal> list = list(qw);
            for (OaPortal oaPortal : list) {
                userDesktopSetupService.delDesktopOfPortal(oaPortal.getId());
                oaPortal.deleteById();
            }
        }
    }

    /**
     * 初始化某用户的门户
     *
     * @param userName String
     */
    @Override
    public void init(String userName) {
        // 删除用户的门户
        delOfUser(userName);

        // 创建门户
        if (!userName.equals(ConstUtil.USER_SYSTEM)) {
            QueryWrapper<OaPortal> qw = new QueryWrapper<>();
            qw.eq("user_name", ConstUtil.USER_SYSTEM);
            List<OaPortal> list = list(qw);
            for (OaPortal oaPortalSys : list) {
                OaPortal oaPortal = new OaPortal();
                oaPortal.setOrders(oaPortalSys.getOrders());
                oaPortal.setIsFixed(oaPortalSys.getIsFixed());
                oaPortal.setDepts(oaPortalSys.getDepts());
                oaPortal.setRoles(oaPortalSys.getRoles());
                oaPortal.setUserName(userName);
                oaPortal.setIcon(oaPortalSys.getIcon());
                oaPortal.setName(oaPortalSys.getName());
                oaPortal.setSystemId(oaPortalSys.getId());
                oaPortal.insert();

                long portalId = oaPortal.getId();

                userDesktopSetupService.initDesktopOfUser(oaPortalSys.getId(), portalId, userName);

                com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                String weatherCode = cfg.get("weatherCode");
                String clockCode = cfg.get("clockCode");
                String calendarCode = cfg.get("calendarCode");

                UserSetup userSetup = userSetupService.getUserSetup(userName);
                userSetup.setWeatherCode(weatherCode);
                userSetup.setClockCode(clockCode);
                userSetup.setCalendarCode(calendarCode);

                userSetupService.update(userSetup, new QueryWrapper<UserSetup>().eq("user_name", userName));
            }
        }
    }
}
