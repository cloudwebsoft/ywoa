package com.cloudweb.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.entity.PostExcluded;
import com.cloudweb.oa.mapper.PostExcludedMapper;
import com.cloudweb.oa.service.IPostExcludedService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.utils.SpringUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class PostExcludedServiceImpl extends ServiceImpl<PostExcludedMapper, PostExcluded> implements IPostExcludedService {

    /**
     * 取出与职位互斥
     * @param postId
     * @return
     */
    @Override
    public List<Integer> listByPostId(int postId) {
        List<Integer> list = new ArrayList<>();
        QueryWrapper<PostExcluded> qw = new QueryWrapper<>();
        qw.eq("post_id", postId);
        // .or().eq("post_id_excluded", postId);
        List<PostExcluded> listTmp = list(qw);
        for (PostExcluded postExcluded : listTmp) {
            list.add(postExcluded.getPostIdExcluded());
        }
        qw = new QueryWrapper<>();
        qw.eq("post_id_excluded", postId);
        listTmp = list(qw);
        for (PostExcluded postExcluded : listTmp) {
            if (!list.contains(postExcluded.getPostIdExcluded())) {
                list.add(postExcluded.getPostId());
            }
        }
        return list;
    }

    @Override
    public boolean removeExcluded(int postId) {
        QueryWrapper<PostExcluded> qw = new QueryWrapper<>();
        qw.eq("post_id", postId).or().eq("post_id_excluded", postId);
        return remove(qw);
    }

    @Override
    public boolean create(int postId, int postIdExcluded) {
        PostExcluded postExcluded = new PostExcluded();
        postExcluded.setPostId(postId);
        postExcluded.setPostIdExcluded(postIdExcluded);
        postExcluded.setCreateDate(LocalDateTime.now());
        postExcluded.setCreator(SpringUtil.getUserName());
        return postExcluded.insert();
    }
}
