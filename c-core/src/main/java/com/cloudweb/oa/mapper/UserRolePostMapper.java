package com.cloudweb.oa.mapper;

import com.cloudweb.oa.entity.UserRolePost;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author fgf
 * @since 2022-02-19
 */
public interface UserRolePostMapper extends BaseMapper<UserRolePost> {

    List<UserRolePost> listBySql(@Param("sql") String sql);

}
