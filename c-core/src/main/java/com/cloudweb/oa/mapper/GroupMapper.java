package com.cloudweb.oa.mapper;

import com.cloudweb.oa.entity.Group;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author fgf
 * @since 2020-02-02
 */
public interface GroupMapper extends BaseMapper<Group> {

    List<Group> listBySql(@Param("sql") String sql);

}
