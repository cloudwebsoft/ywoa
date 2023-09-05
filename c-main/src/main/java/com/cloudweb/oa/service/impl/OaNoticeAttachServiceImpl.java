package com.cloudweb.oa.service.impl;

import cn.js.fan.web.Global;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.api.IObsService;
import com.cloudweb.oa.entity.OaNoticeAttach;
import com.cloudweb.oa.mapper.OaNoticeAttachMapper;
import com.cloudweb.oa.service.IFileService;
import com.cloudweb.oa.service.IOaNoticeAttachService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.utils.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fgf
 * @since 2020-01-01
 */
@Service
public class OaNoticeAttachServiceImpl extends ServiceImpl<OaNoticeAttachMapper, OaNoticeAttach> implements IOaNoticeAttachService {

    @Autowired
    OaNoticeAttachMapper oaNoticeAttachMapper;

    @Autowired
    IFileService fileService;

    @Override
    public boolean create(OaNoticeAttach oaNoticeAttach) {
        return oaNoticeAttachMapper.insert(oaNoticeAttach)==1;
    }

    @Override
    public int del(long id) {
        OaNoticeAttach oaNoticeAttach = oaNoticeAttachMapper.selectById(id);
        // 删除文件
        fileService.del(oaNoticeAttach.getVisualPath(), oaNoticeAttach.getDiskName());

        // 删除记录
        return oaNoticeAttachMapper.deleteById(id);
    }

    @Override
    public int delOfNotice(long noticeId) {
        int n = 0;
        QueryWrapper<OaNoticeAttach> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id")
                .eq("notice_id", noticeId);
        List<OaNoticeAttach> list = oaNoticeAttachMapper.selectList(queryWrapper);
        for (OaNoticeAttach oaNoticeAttach : list) {
            n += del(oaNoticeAttach.getId());
        }
        return n;
    }

}
