package com.cloudweb.oa.mapper;

import com.cloudweb.oa.entity.Post;
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
 * @since 2022-02-15
 */
public interface PostMapper extends BaseMapper<Post> {

    List<Post> listBySql(@Param("sql") String sql);

    @Select("select MAX(orders) from post where dept_code=#{deptCode}")
    Integer getMaxOrdersByDeptCode(@Param("deptCode") String deptCode);
}
