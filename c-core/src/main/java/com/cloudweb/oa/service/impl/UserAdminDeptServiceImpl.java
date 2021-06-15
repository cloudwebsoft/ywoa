package com.cloudweb.oa.service.impl;

import cn.js.fan.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.annotation.SysLog;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.UserAdminDept;
import com.cloudweb.oa.enums.LogLevel;
import com.cloudweb.oa.enums.LogType;
import com.cloudweb.oa.mapper.UserAdminDeptMapper;
import com.cloudweb.oa.service.IUserAdminDeptService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.ws.Action;
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
public class UserAdminDeptServiceImpl extends ServiceImpl<UserAdminDeptMapper, UserAdminDept> implements IUserAdminDeptService {

    @Autowired
    UserCache userCache;

    @Override
    public List<UserAdminDept> listByUserName(String userName) {
        QueryWrapper<UserAdminDept> qw = new QueryWrapper<>();
        qw.eq("userName", userName);
        return list(qw);
    }

    public boolean delOfUser(String userName) {
        QueryWrapper<UserAdminDept> qw = new QueryWrapper<>();
        qw.eq("userName", userName);
        return remove(qw);
    }

    @Override
    @SysLog(type = LogType.AUTHORIZE, action = "授权给用户${userName}管理部门", remark="${deptCodes}", debug = true, level = LogLevel.NORMAL)
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public boolean setUserAdminDept(String userName, String deptCodes) {
        boolean re = true;
        delOfUser(userName);
        String[] depts = StrUtil.split(deptCodes, ",");
        if (depts!=null) {
            for (String deptCode : depts) {
                UserAdminDept userAdminDept = new UserAdminDept();
                userAdminDept.setUserName(userName);
                userAdminDept.setDeptCode(deptCode);
                re = userAdminDept.insert();
            }
        }
        userCache.refreshAdminDepts(userName);
        return re;
    }

}
