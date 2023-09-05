package com.cloudweb.oa.mapper;

import com.cloudweb.oa.entity.VisualModuleTreePriv;
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
 * @since 2022-08-20
 */
public interface VisualModuleTreePrivMapper extends BaseMapper<VisualModuleTreePriv> {
    @Select("select * from visual_module_tree_priv where root_code=#{rootCode} and node_code=#{nodeCode}")
    List<VisualModuleTreePriv> list(@Param("rootCode") String rootCode, @Param("nodeCode") String nodeCode);

    List<VisualModuleTreePriv> selectTreePrivList(@Param("sql") String sql);
}
