package com.cloudweb.oa.service;

import cn.js.fan.util.ErrMsgException;
import com.cloudweb.oa.entity.PostUser;
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
public interface IPostUserService extends IService<PostUser> {

    List<PostUser> listByPostId(int postId);

    PostUser getPostUserByUserName(String userName);

    boolean delByPostId(int postId);

    PostUser getPostUser(String userName, int postId);

    boolean update(int postId, String[] aryUsers) throws ErrMsgException;

    List<PostUser> listByUserName(String userName);

    boolean create(String userName, int postId);

    boolean delByUserName(String userName);
}
