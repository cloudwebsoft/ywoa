package com.cloudweb.oa.service.impl;

import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.entity.Post;
import com.cloudweb.oa.entity.PostUser;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.mapper.PostMapper;
import com.cloudweb.oa.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.sys.DebugUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fgf
 * @since 2022-02-15
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements IPostService {

    @Autowired
    I18nUtil i18nUtil;

    @Autowired
    PostMapper postMapper;

    @Autowired
    IPostUserService postUserService;

    @Autowired
    IPostExcludedService postExcludedService;

    @Autowired
    IUserAuthorityService userAuthorityService;

    @Autowired
    UserCache userCache;

    @Autowired
    IDepartmentService departmentService;

    @Override
    public boolean isExist(String deptCode, String name) {
        // 检查同一部门下是否存在有重名的岗位
        String sql = "select * from post where dept_code=" + StrUtil.sqlstr(deptCode) + " and name=" + StrUtil.sqlstr(name);
        return postMapper.listBySql(sql).size() > 0;
    }

    @Override
    public boolean create(String deptCode, String name, String description, Integer id, Boolean status) {
        Post post = new Post();
        post.setCreateDate(DateUtil.toLocalDateTime(new Date()));
        post.setName(name);
        post.setDescription(description);
        post.setDeptCode(deptCode);
        post.setExcluded(false);
        post.setLimited(false);
        post.setCreator(SpringUtil.getUserName());
        // 为便于与其它系统集成，ID改为非自增长型
        if (id == null) {
            post.setId((int) SequenceManager.nextID(SequenceManager.OA_POST));
        }
        else {
            post.setId(id);
        }
        Department department = departmentService.getDepartment(deptCode);
        if (department == null) {
            DebugUtil.e(getClass(), "create", "部门：" + deptCode + "不存在");
            return false;
        }
        else {
            String unitCode = departmentService.getUnitOfDept(department).getCode();
            post.setUnitCode(unitCode);
        }

        Integer maxOrders = getMaxOrdersByDeptCode(deptCode);
        int orders = maxOrders != null ? maxOrders + 1 : 0;
        post.setOrders(orders);
        return post.insert();
    }

    @Override
    public List<Post> list(String op, String deptCode, String name) {
        String sql;
        if (ConstUtil.DEPT_ROOT.equals(deptCode) || "".equals(deptCode)) {
            sql = "select * from post where 1=1";
        }
        else {
            sql = "select * from post where dept_code=" + StrUtil.sqlstr(deptCode);
        }
        if ("search".equals(op)) {
            if (!"".equals(name)) {
                sql += " and name like " + StrUtil.sqlstr("%" + name + "%");
            }
        }
        sql += " order by orders desc";
        return postMapper.listBySql(sql);
    }

    @Override
    public List<Post> listByUnitCode(String unitCode) {
        String sql = "select * from post";
        if (!ConstUtil.DEPT_ROOT.equals(unitCode)) {
            sql += " where unit_code=" + StrUtil.sqlstr(unitCode);
        }
        return postMapper.listBySql(sql);
    }

    @Override
    public Integer getMaxOrdersByDeptCode(String deptCode) {
        return postMapper.getMaxOrdersByDeptCode(deptCode);
    }

    /**
     * 根据名称取得实例
     * @param name
     * @return
     */
    @Override
    public Post getPostByName(String name) {
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq("name", name);
        return getOne(qw, false);
    }

    @Override
    public boolean del(int id) {
        // 删除职位中的人员
        postUserService.delByPostId(id);

        // 删除互斥职位记录
        postExcludedService.removeExcluded(id);

        return removeById(id);
    }

    @Override
    public boolean update(Post post, String[] postsExcluded) {
        // 清空互斥的职位
        postExcludedService.removeExcluded(post.getId());
        if (postsExcluded != null) {
            // 添加互斥的职位
            for (String strId : postsExcluded) {
                int pId = StrUtil.toInt(strId, -1);
                postExcludedService.create(post.getId(), pId);
            }
        }

        Post oldPost = getById(post.getId());

        boolean re = post.updateById();

        // 判断职位的状态是否有改变
        if (!oldPost.getStatus().equals(post.getStatus())) {
            // 刷新职位下人员的权限
            List<PostUser> list = postUserService.listByPostId(post.getId());
            for (PostUser postUser : list) {
                // 刷新用户所拥有的权限及角色
                userAuthorityService.refreshUserAuthority(postUser.getUserName());
                userCache.refreshRoles(postUser.getUserName());
            }
        }
        return re;
    }
}
