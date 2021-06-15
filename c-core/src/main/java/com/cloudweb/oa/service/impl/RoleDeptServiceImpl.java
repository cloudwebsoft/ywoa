package com.cloudweb.oa.service.impl;

import cn.js.fan.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.annotation.SysLog;
import com.cloudweb.oa.entity.RoleDept;
import com.cloudweb.oa.enums.LogLevel;
import com.cloudweb.oa.enums.LogType;
import com.cloudweb.oa.mapper.RoleDeptMapper;
import com.cloudweb.oa.service.IRoleDeptService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fgf
 * @since 2020-02-22
 */
@Service
public class RoleDeptServiceImpl extends ServiceImpl<RoleDeptMapper, RoleDept> implements IRoleDeptService {

    @Override
    public List<RoleDept> listByRoleCode(String roleCode) {
        QueryWrapper<RoleDept> qw = new QueryWrapper<>();
        qw.eq("roleCode", roleCode);
        return list(qw);
    }

    @Override
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    @SysLog(type = LogType.AUTHORIZE, action = "授权角色${roleDesc}管理部门${deptNames}", remark="授权角色${roleDesc}管理部门${deptNames}", debug = true, level = LogLevel.NORMAL)
    public boolean setRoleDepts(String roleCode, String roleDesc, String deptCodes, String deptNames) {
        String[] depts = StrUtil.split(deptCodes, ",");
        QueryWrapper<RoleDept> qw = new QueryWrapper<>();
        qw.eq("roleCode", roleCode);
        remove(qw);

        boolean re = true;
        if (depts!=null) {
            for (String deptCode : depts) {
                RoleDept roleDept = new RoleDept();
                roleDept.setDeptCode(deptCode);
                roleDept.setRoleCode(roleCode);
                re = roleDept.insert();
            }
        }

        return re;
    }

    @Override
    public boolean delByRoleCode(String roleCode) {
        QueryWrapper<RoleDept> qw = new QueryWrapper<>();
        qw.eq("roleCode", roleCode);
        return remove(qw);
    }
}
