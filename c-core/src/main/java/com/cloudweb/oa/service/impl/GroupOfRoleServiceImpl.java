package com.cloudweb.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.entity.GroupOfRole;
import com.cloudweb.oa.mapper.GroupOfRoleMapper;
import com.cloudweb.oa.service.IGroupOfRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fgf
 * @since 2020-02-17
 */
@Service
public class GroupOfRoleServiceImpl extends ServiceImpl<GroupOfRoleMapper, GroupOfRole> implements IGroupOfRoleService {

    @Override
    public List<GroupOfRole> listByGroupCode(String groupCode) {
        QueryWrapper<GroupOfRole> qw = new QueryWrapper<>();
        qw.eq("userGroupCode", groupCode);
        return list(qw);
    }

    @Override
    public List<GroupOfRole> listByRoleCode(String roleCode) {
        QueryWrapper<GroupOfRole> qw = new QueryWrapper<>();
        qw.eq("roleCode", roleCode);
        return list(qw);
    }

    @Override
    public boolean setGroupOfRole(String groupCode, String[] roleCode) {
        QueryWrapper<GroupOfRole> qw = new QueryWrapper<>();
        qw.eq("userGroupCode", groupCode);
        remove(qw);

        if (roleCode==null) {
            return true;
        }

        for (String code : roleCode) {
            GroupOfRole groupOfRole = new GroupOfRole();
            groupOfRole.setRoleCode(code);
            groupOfRole.setUserGroupCode(groupCode);
            groupOfRole.insert();
        }
        return true;
    }

}
