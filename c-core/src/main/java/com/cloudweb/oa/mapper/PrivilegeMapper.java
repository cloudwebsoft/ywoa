package com.cloudweb.oa.mapper;

import com.cloudweb.oa.entity.Privilege;
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
public interface PrivilegeMapper extends BaseMapper<Privilege> {

    List<Privilege> listBySql(@Param("sql") String sql);
}
