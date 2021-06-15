package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.OaNoticeAttach;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author fgf
 * @since 2020-01-01
 */
public interface IOaNoticeAttachService extends IService<OaNoticeAttach> {
    boolean create(OaNoticeAttach oaNotice);

    int del(long id);

    int delOfNotice(long noticeId);
}
