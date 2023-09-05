package com.cloudweb.oa.mapper;

import com.cloudweb.oa.entity.PostUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author fgf
 * @since 2022-02-19
 */
public interface PostUserMapper extends BaseMapper<PostUser> {

    @Select("select * from post_user where post_id=${postId}")
    List<PostUser> listByPostId(@Param("postId") Integer postId);
}
