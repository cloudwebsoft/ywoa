package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.Post;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2022-02-15
 */
public interface IPostService extends IService<Post> {

    boolean isExist(String deptCode, String name);

    boolean create(String deptCode, String name, String description, Integer id, Boolean status);

    List<Post> list(String op, String deptCode, String name);

    Integer getMaxOrdersByDeptCode(String deptCode);

    List<Post> listByUnitCode(String unitCode);

    Post getPostByName(String name);

    boolean del(int id);

    boolean update(Post post, String[] postsExcluded);
}
