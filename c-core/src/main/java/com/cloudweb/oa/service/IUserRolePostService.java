package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.Post;
import com.cloudweb.oa.entity.UserRolePost;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2022-02-19
 */
public interface IUserRolePostService extends IService<UserRolePost> {
    boolean create(String roleCode, int postId);

    boolean update(String roleCode, String[] ary);

    boolean del(String[] ary);

    List<UserRolePost> listByPostId(int postId);

    boolean delByRoleCode(String roleCode);

    List<UserRolePost> listRolePost(String op, String roleCode, String name);
}
