package com.cloudweb.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.entity.UserDesktopSetup;
import com.cloudweb.oa.mapper.UserDesktopSetupMapper;
import com.cloudweb.oa.service.IUserDesktopSetupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.utils.ConstUtil;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.person.UserDesktopSetupCache;
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
public class UserDesktopSetupServiceImpl extends ServiceImpl<UserDesktopSetupMapper, UserDesktopSetup> implements IUserDesktopSetupService {

    @Override
    public boolean delDesktopOfPortal(long portalId) {
        QueryWrapper<UserDesktopSetup> qw = new QueryWrapper<>();
        qw.eq("portal_id", portalId);
        return remove(qw);
    }

    @Override
    public boolean delOfUser(String userName) {
        QueryWrapper<UserDesktopSetup> qw = new QueryWrapper<>();
        qw.eq("user_name", userName);
        return remove(qw);
    }

    @Override
    public void initDesktopOfUser(long portalIdSys, long portalId, String userName) {
        QueryWrapper<UserDesktopSetup> qw = new QueryWrapper<>();
        qw.eq("portal_id", portalIdSys);
        List<UserDesktopSetup> list = list(qw);
        for (UserDesktopSetup userDesktopSetupSys : list) {
            int id = (int) SequenceManager.nextID(SequenceManager.OA_USER_DESKTOP_SETUP);

            UserDesktopSetup userDesktopSetup = new UserDesktopSetup();
            userDesktopSetup.setId(id);
            userDesktopSetup.setUserName(userName);
            userDesktopSetup.setTitle(userDesktopSetupSys.getTitle());
            userDesktopSetup.setModuleRows(userDesktopSetupSys.getModuleRows());
            userDesktopSetup.setModuleCode(userDesktopSetupSys.getModuleCode());
            userDesktopSetup.setModuleItem(userDesktopSetupSys.getModuleItem());
            userDesktopSetup.setIsSystem(ConstUtil.USER_DESKTOP_SETUP_SYSTEM_ID_NONE);
            userDesktopSetup.setTd(userDesktopSetupSys.getTd());
            userDesktopSetup.setOrderInTd(userDesktopSetupSys.getOrderInTd());
            userDesktopSetup.setWordCount(userDesktopSetupSys.getWordCount());
            userDesktopSetup.setPortalId(portalId);
            userDesktopSetup.setSystemId(userDesktopSetupSys.getSystemId());
            userDesktopSetup.setCanDelete(userDesktopSetupSys.getCanDelete());
            userDesktopSetup.setMetaData(userDesktopSetupSys.getMetaData());
            userDesktopSetup.setIcon(userDesktopSetupSys.getIcon());
            userDesktopSetup.insert();
        }

        UserDesktopSetupCache udsc = new UserDesktopSetupCache();
        udsc.refreshList();
    }
}
