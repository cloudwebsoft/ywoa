package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.OaNoticeReply;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author fgf
 * @since 2020-01-01
 */
public interface IOaNoticeReplyService extends IService<OaNoticeReply> {

    List<OaNoticeReply> getReplyReadOrNot(long noticeId, int isReaded);

    List<OaNoticeReply> getReplyHasContent(long noticeId);

    List<OaNoticeReply> getReplies(long noticeId);

    boolean createBatch(List<OaNoticeReply> list);

    int del(long id);

    int delOfNotice(long noticeId);

    OaNoticeReply getOaNoticeReply(long noticeId, String userName);
}
