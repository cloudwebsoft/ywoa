package com.cloudweb.oa.mapper;

import com.cloudweb.oa.entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author fgf
 * @since 2020-01-23
 */
public interface RoleMapper extends BaseMapper<Role> {

    List<Role> listBySql(@Param("sql") String sql);
}
