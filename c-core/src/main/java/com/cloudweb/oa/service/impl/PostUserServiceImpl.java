package com.cloudweb.oa.service.impl;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.Post;
import com.cloudweb.oa.entity.PostUser;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.mapper.PostUserMapper;
import com.cloudweb.oa.service.IPostExcludedService;
import com.cloudweb.oa.service.IPostService;
import com.cloudweb.oa.service.IPostUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.service.IUserAuthorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author fgf
 * @since 2022-02-19
 */
@Service
public class PostUserServiceImpl extends ServiceImpl<PostUserMapper, PostUser> implements IPostUserService {

    @Autowired
    PostUserMapper postUserMapper;

    @Autowired
    IPostService postService;

    @Autowired
    IPostExcludedService postExcludedService;

    @Autowired
    IUserAuthorityService userAuthorityService;

    @Autowired
    UserCache userCache;

    @Override
    public List<PostUser> listByPostId(int postId) {
        return postUserMapper.listByPostId(postId);
    }

    @Override
    public List<PostUser> listByUserName(String userName) {
        QueryWrapper<PostUser> qw = new QueryWrapper<>();
        qw.eq("user_name", userName).orderByDesc("orders");
        return list(qw);
    }

    @Override
    public boolean delByUserName(String userName) {
        QueryWrapper<PostUser> qw = new QueryWrapper<>();
        qw.eq("user_name", userName);
        return remove(qw);
    }

    @Override
    public PostUser getPostUserByUserName(String userName) {
        QueryWrapper<PostUser> qw = new QueryWrapper<>();
        qw.eq("user_name", userName).orderByDesc("orders");
        return getOne(qw, false);
    }

    @Override
    public PostUser getPostUser(String userName, int postId) {
        QueryWrapper<PostUser> qw = new QueryWrapper<>();
        qw.eq("user_name", userName);
        qw.eq("post_id", postId);
        return getOne(qw, false);
    }

    @Override
    public boolean create(String userName, int postId) {
        PostUser postUser = new PostUser();
        postUser.setUserName(userName);
        postUser.setPostId(postId);
        return postUser.insert();
    }

    @Override
    public boolean delByPostId(int postId) {
        List<String> userNameList = new ArrayList<>();
        List<PostUser> postUserList = listByPostId(postId);
        for (PostUser postUser : postUserList) {
            userNameList.add(postUser.getUserName());
        }

        QueryWrapper<PostUser> qw = new QueryWrapper<>();
        qw.eq("post_id", postId);
        boolean re = remove(qw);
        if (re) {
            for (String userName : userNameList) {
                // 刷新用户所拥有的权限及角色
                userAuthorityService.refreshUserAuthority(userName);
                userCache.refreshRoles(userName);
            }
        }
        return re;
    }

    @Override
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public boolean update(int postId, String[] aryUsers) throws ErrMsgException {
        // 检查人数限制
        Post post = postService.getById(postId);
        if (post.getLimited()) {
            if (aryUsers.length > post.getNumLimited()) {
                throw new ErrMsgException("职位人数不能大于" + post.getNumLimited());
            }
        }

        List<String> userNameList = new ArrayList<>();
        List<PostUser> postUserList = listByPostId(postId);
        for (PostUser postUser : postUserList) {
            userNameList.add(postUser.getUserName());
        }

        // 清空
        QueryWrapper<PostUser> qw = new QueryWrapper<>();
        qw.eq("post_id", postId);
        remove(qw);

        boolean isExclusive = post.getExcluded();
        List<Integer> listExcluded = null;
        if (isExclusive) {
            listExcluded = postExcludedService.listByPostId(postId);
        }

        int orders = 0;
        for (String userName : aryUsers) {
            // 将与其相斥的职位中的用户记录删除
            if (isExclusive) {
                if (listExcluded != null) {
                    StringBuilder sb = new StringBuilder();
                    // 如果当前加入职位的用户在互斥职位中，则将其从互斥职位中清除
                    for (int pId : listExcluded) {
                        PostUser postUser = getPostUser(userName, pId);
                        if (postUser != null) {
                            // postUser.deleteById();

                            // 改为如果互斥职位中存在记录，则抛出异常，而不是直接删除
                            Post postEx = postService.getById(pId);
                            sb.append("存在互斥职位：").append(postEx.getName()).append("\r\n");
                        }
                    }
                    if (sb.length() > 0) {
                        throw new ErrMsgException(sb.toString());
                    }
                }
            }

            PostUser postUser = new PostUser();
            postUser.setPostId(postId);
            postUser.setUserName(userName);
            postUser.setOrders(orders);
            postUser.insert();
            orders ++;

            if (!userNameList.contains(userName)) {
                userNameList.add(userName);
            }
        }

        for (String userName : userNameList) {
            // 刷新用户所拥有的权限及角色
            userAuthorityService.refreshUserAuthority(userName);
            userCache.refreshRoles(userName);
        }
        return true;
    }
}
