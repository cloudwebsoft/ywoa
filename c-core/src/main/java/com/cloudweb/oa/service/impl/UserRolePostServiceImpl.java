package com.cloudweb.oa.service.impl;

import cn.js.fan.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.cache.RoleCache;
import com.cloudweb.oa.cache.UserAuthorityCache;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.Post;
import com.cloudweb.oa.entity.PostUser;
import com.cloudweb.oa.entity.UserRoleDepartment;
import com.cloudweb.oa.entity.UserRolePost;
import com.cloudweb.oa.mapper.UserRolePostMapper;
import com.cloudweb.oa.service.IPostUserService;
import com.cloudweb.oa.service.IUserAuthorityService;
import com.cloudweb.oa.service.IUserOfRoleService;
import com.cloudweb.oa.service.IUserRolePostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.utils.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fgf
 * @since 2022-02-19
 */
@Service
public class UserRolePostServiceImpl extends ServiceImpl<UserRolePostMapper, UserRolePost> implements IUserRolePostService {

    @Autowired
    UserRolePostMapper userRolePostMapper;

    @Autowired
    IPostUserService postUserService;

    @Autowired
    IUserAuthorityService userAuthorityService;

    @Autowired
    UserCache userCache;

    @Autowired
    IUserOfRoleService userOfRoleService;

    @Autowired
    UserAuthorityCache userAuthorityCache;

    @Autowired
    RoleCache roleCache;

    /**
     * 创建
     * @param roleCode
     * @param postId
     * @return
     */
    @Override
    public boolean create(String roleCode, int postId) {
        UserRolePost userRolePost = new UserRolePost();
        userRolePost.setPostId(postId);
        userRolePost.setRoleCode(roleCode);
        userRolePost.setCreator(SpringUtil.getUserName());
        userRolePost.setCreateDate(LocalDateTime.now());
        return userRolePost.insert();
    }

    @Override
    public boolean delByRoleCode(String roleCode) {
        // 删除原有的记录
        QueryWrapper<UserRolePost> qw = new QueryWrapper<>();
        qw.eq("role_code", roleCode);

        /*List<String> userNameList = new ArrayList<>();
        // 取角色下的所有职位
        List<UserRolePost> list = list(qw);
        for (UserRolePost userRolePost : list) {
            // 取职位下的人员
            List<PostUser> postUserList = postUserService.listByPostId(userRolePost.getPostId());
            for (PostUser postUser : postUserList) {
                if (!userNameList.contains(postUser.getUserName())) {
                    userNameList.add(postUser.getUserName());
                }
            }
        }*/

        // 删除角色下的所有职位
        boolean re = remove(qw);
        if (re) {
            // 刷新用户所拥有的权限及角色，改为在RoleSerivceImpl.del中刷新
            /*for (String userName : userNameList) {
                userAuthorityService.refreshUserAuthority(userName);
                userCache.refreshRoles(userName);
            }*/
        }
        return re;
    }

    @Override
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public boolean update(String roleCode, String[] ary) {
        // 删除原有的记录
        QueryWrapper<UserRolePost> qw = new QueryWrapper<>();
        qw.eq("role_code", roleCode);

        List<String> userNameList = new ArrayList<>();
        // 取角色下的原来所有职位
        List<UserRolePost> list = list(qw);

        List<String> newIdList = Arrays.asList(ary);
        for (UserRolePost userRolePost : list) {
            // 判断原职位是否在新选取职位中，如果不在则删除人员的角色
            boolean isToDel = false;
            if (!newIdList.contains(String.valueOf(userRolePost.getPostId()))) {
                isToDel = true;
            }

            // 取职位下的人员
            List<PostUser> postUserList = postUserService.listByPostId(userRolePost.getPostId());
            for (PostUser postUser : postUserList) {
                if (!userNameList.contains(postUser.getUserName())) {
                    if (isToDel) {
                        // 删除人员所属的角色
                        userOfRoleService.delOfUser(postUser.getUserName());
                    }
                    userNameList.add(postUser.getUserName());
                }
            }
        }

        // 删除角色下的所有职位
        remove(qw);

        // 添加职位记录
        if (ary != null) {
            for (String strId : ary) {
                int id = StrUtil.toInt(strId, -1);
                UserRolePost userRolePost = new UserRolePost();
                userRolePost.setRoleCode(roleCode);
                userRolePost.setPostId(id);
                userRolePost.setCreateDate(LocalDateTime.now());
                userRolePost.setCreator(SpringUtil.getUserName());
                userRolePost.insert();

                // 取职位下的人员
                List<PostUser> postUserList = postUserService.listByPostId(userRolePost.getPostId());
                for (PostUser postUser : postUserList) {
                    // 置人员角色
                    List listOfRole = userOfRoleService.listByUserName(postUser.getUserName());
                    if (listOfRole.size() == 0) {
                        userOfRoleService.create(postUser.getUserName(), roleCode);
                    }

                    if (!userNameList.contains(postUser.getUserName())) {
                        userNameList.add(postUser.getUserName());
                    }
                }
            }
        }

        // 刷新用户所拥有的权限及角色
        for (String userName : userNameList) {
            userAuthorityService.refreshUserAuthority(userName);
            userAuthorityCache.refreshUserAuthorities(userName);
            userCache.refreshRoles(userName);
        }
        roleCache.refreshAll();
        return true;
    }

    @Override
    public List<UserRolePost> listByPostId(int postId) {
        QueryWrapper<UserRolePost> qw = new QueryWrapper<>();
        qw.eq("post_id", postId);
        return list(qw);
    }

    @Override
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public boolean del(String[] ary) {
        // 添加记录
        if (ary != null) {
            List<String> userNameList = new ArrayList<>();
            for (String strId : ary) {
                int id = StrUtil.toInt(strId, -1);
                UserRolePost userRolePost = getById(id);
                // 取职位下的人员
                List<PostUser> postUserList = postUserService.listByPostId(userRolePost.getPostId());
                for (PostUser postUser : postUserList) {
                    if (!userNameList.contains(postUser.getUserName())) {
                        userNameList.add(postUser.getUserName());
                    }
                }
                removeById(id);
            }

            // 刷新用户所拥有的权限及角色
            for (String userName : userNameList) {
                userAuthorityService.refreshUserAuthority(userName);
                userAuthorityCache.refreshUserAuthorities(userName);
                userCache.refreshRoles(userName);
            }
            roleCache.refreshAll();
        }
        return true;
    }

    @Override
    public List<UserRolePost> listRolePost(String op, String roleCode, String name) {
        String sql = "select r.* from post p, user_role_post r where r.role_code=" + StrUtil.sqlstr(roleCode) + " and r.post_id=p.id";
        if ("search".equals(op)) {
            if (!"".equals(name)) {
                sql += " and p.name like " + StrUtil.sqlstr("%" + name + "%");
            }
        }
        sql += " order by orders desc";
        return userRolePostMapper.listBySql(sql);
    }
}
