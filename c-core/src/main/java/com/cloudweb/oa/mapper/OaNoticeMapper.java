package com.cloudweb.oa.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudweb.oa.entity.OaNotice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author fgf
 * @since 2019-12-29
 */
@Mapper
public interface OaNoticeMapper extends BaseMapper<OaNotice> {

    // @Select("SELECT * FROM oauth_organization WHERE id < #{m.id} ORDER BY `id` DESC")
    // List<OaNotice> selectpage(Map<String,Object> m, Page<OaNotice> page);

    List<OaNotice> selectNoticeList(@Param("sql") String sql);

    OaNotice selectByIdWithAtt(long id);

    List<OaNotice> selectMyNoticeOnDesktop(@Param("userName") String userName, @Param("curDate") LocalDate curDate);

    List<OaNotice> listImportant(@Param("userName") String userName, @Param("curDate") LocalDate curDate);
}
