package com.cloudweb.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.Group;
import com.cloudweb.oa.entity.OaSlideMenuGroup;
import com.cloudweb.oa.entity.Role;
import com.cloudweb.oa.entity.VisualModuleTreePriv;
import com.cloudweb.oa.mapper.VisualModuleTreePrivMapper;
import com.cloudweb.oa.service.IDeptUserService;
import com.cloudweb.oa.service.IVisualModuleTreePrivService;
import com.cloudweb.oa.utils.SpringUtil;
import com.github.pagehelper.PageHelper;
import com.redmoon.oa.basic.TreeSelectDb;
import com.redmoon.oa.fileark.Leaf;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.PrivDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.pvg.UserGroupDb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author fgf
 * @since 2022-08-20
 */
@Service
public class VisualModuleTreePrivServiceImpl extends ServiceImpl<VisualModuleTreePrivMapper, VisualModuleTreePriv> implements IVisualModuleTreePrivService {

    @Autowired
    UserCache userCache;

    @Autowired
    VisualModuleTreePrivMapper visualModuleTreePrivMapper;

    @Override
    public List<VisualModuleTreePriv> list(String rootCode, String nodeCode, int pageSize, int curPage) {
        PageHelper.startPage(curPage, pageSize); // 分页查询
        /*String sql = "select * from visual_module_tree_priv where root_code=" + StrUtil.sqlstr(rootCode) + " and node_code=" + StrUtil.sqlstr(nodeCode);
        return visualModuleTreePrivMapper.selectTreePrivList(sql);*/
        return visualModuleTreePrivMapper.list(rootCode, nodeCode);
    }

    @Override
    public List<VisualModuleTreePriv> list(String rootCode, String nodeCode) {
        return visualModuleTreePrivMapper.list(rootCode, nodeCode);
    }

    public VisualModuleTreePriv get(long id) {
        return getById(id);
    }

}
