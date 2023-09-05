package com.cloudweb.oa.mapper;

import com.cloudweb.oa.entity.OaNoticeReply;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author fgf
 * @since 2020-01-01
 */
@Mapper
public interface OaNoticeReplyMapper extends BaseMapper<OaNoticeReply> {

    /*@Select("select * from oa_notice_reply where notice_id = #{noticeId} and user_name = #{userName}")
    List<OaNoticeReply> selectIsReaded(@Param("noticeId") long noticeId, @Param("userName") String userName);*/

    /*@Select("select * from oa_notice_reply where notice_id = #{noticeId} and is_readed = #{isReaded}")
    List<OaNoticeReply> getReplyReadOrNot(@Param("noticeId") long noticeId, @Param("isReaded") int isReaded);*/

    List<OaNoticeReply> getReplyReadOrNot(@Param("noticeId") long noticeId, @Param("isReaded") int isReaded);

    List<OaNoticeReply> getReplyHasContent(@Param("noticeId") long noticeId);
}
