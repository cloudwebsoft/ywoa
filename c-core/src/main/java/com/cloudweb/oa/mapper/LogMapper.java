package com.cloudweb.oa.mapper;

import com.cloudweb.oa.entity.Log;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author
 * @since 2020-02-15
 */
public interface LogMapper extends BaseMapper<Log> {

    List<Log> listBySql(@Param("sql") String sql);

}
