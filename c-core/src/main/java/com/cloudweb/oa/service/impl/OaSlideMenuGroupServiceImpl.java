package com.cloudweb.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.entity.OaSlideMenu;
import com.cloudweb.oa.entity.OaSlideMenuGroup;
import com.cloudweb.oa.mapper.OaSlideMenuGroupMapper;
import com.cloudweb.oa.service.IOaSlideMenuGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.service.IOaSlideMenuService;
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
public class OaSlideMenuGroupServiceImpl extends ServiceImpl<OaSlideMenuGroupMapper, OaSlideMenuGroup> implements IOaSlideMenuGroupService {

    @Autowired
    IOaSlideMenuService oaSlideMenuService;

    @Override
    public boolean delOfUser(String userName) {
        QueryWrapper<OaSlideMenuGroup> qw = new QueryWrapper<>();
        qw.eq("user_name", userName);
        return remove(qw);
    }

    /**
     * 初始化某用户的滑动菜单
     *
     * @param userName String
     */
    @Override
    public void init(String userName) {
        // 删除用户的菜单组
        delOfUser(userName);

        QueryWrapper<OaSlideMenuGroup> qw = new QueryWrapper<>();
        qw.eq("user_name", ConstUtil.USER_SYSTEM);
        List<OaSlideMenuGroup> list = list(qw);
        for (OaSlideMenuGroup oaSlideMenuGroupSys : list) {
            OaSlideMenuGroup oaSlideMenuGroup = new OaSlideMenuGroup();
            oaSlideMenuGroup.setOrders(oaSlideMenuGroupSys.getOrders());
            oaSlideMenuGroup.setUserName(userName);
            oaSlideMenuGroup.setName(oaSlideMenuGroupSys.getName());
            oaSlideMenuGroup.insert();

            long groupId = oaSlideMenuGroup.getId();

            QueryWrapper<OaSlideMenu> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("group_id", oaSlideMenuGroupSys.getId());
            List<OaSlideMenu> listMenu = oaSlideMenuService.list(queryWrapper);
            for (OaSlideMenu oaSlideMenuSys : listMenu) {
                OaSlideMenu oaSlideMenu = new OaSlideMenu();
                oaSlideMenu.setCode(oaSlideMenuSys.getCode());
                oaSlideMenu.setOrders(oaSlideMenuSys.getOrders());
                oaSlideMenu.setGroupId(groupId);
                oaSlideMenu.insert();
            }
        }
    }
}
