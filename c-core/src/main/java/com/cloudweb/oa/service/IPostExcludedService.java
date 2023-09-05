package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.PostExcluded;
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
public interface IPostExcludedService extends IService<PostExcluded> {

    List<Integer> listByPostId(int postId);

    boolean removeExcluded(int postId);

    boolean create(int postId, int postIdExcluded);

}
