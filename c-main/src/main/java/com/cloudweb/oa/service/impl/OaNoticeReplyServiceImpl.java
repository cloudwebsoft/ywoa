package com.cloudweb.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.OaNoticeReply;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.mapper.OaNoticeReplyMapper;
import com.cloudweb.oa.service.IOaNoticeReplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudwebsoft.framework.util.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author fgf
 * @since 2020-01-01
 */
@Service
public class OaNoticeReplyServiceImpl extends ServiceImpl<OaNoticeReplyMapper, OaNoticeReply> implements IOaNoticeReplyService {

    @Autowired
    OaNoticeReplyMapper oaNoticeReplyMapper;

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    private UserCache userCache;

    /**
     * 取得已读和未读的列表
     * @param noticeId
     * @param isReaded
     * @return
     */
    @Override
    public List<OaNoticeReply> getReplyReadOrNot(long noticeId, int isReaded) {
        // 通过下列方式并不能取得 BaseResultMap 中的 association
        /*QueryWrapper<OaNoticeReply> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id")
                .eq("notice_id", noticeId)
                .eq("is_readed", isReaded ? 1 : 0);
        return oaNoticeReplyMapper.selectList(queryWrapper);*/
        return oaNoticeReplyMapper.getReplyReadOrNot(noticeId, isReaded);
    }

    @Override
    public List<OaNoticeReply> getReplyHasContent(long noticeId) {
        /*QueryWrapper<OaNoticeReply> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id")
                .eq("notice_id", noticeId)
                .isNotNull("content")
                .ne("content", "");
        return oaNoticeReplyMapper.selectList(queryWrapper);*/

        return oaNoticeReplyMapper.getReplyHasContent(noticeId);
    }

    @Override
    public List<OaNoticeReply> getReplies(long noticeId) {
        QueryWrapper<OaNoticeReply> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id")
                .eq("notice_id", noticeId);
        return oaNoticeReplyMapper.selectList(queryWrapper);
    }

    /**
     * 批量操作，创建回复
     * @param list
     * @return
     */
    @Override
    public boolean createBatch(List<OaNoticeReply> list) {
        SqlSession session = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
        OaNoticeReplyMapper mapper = session.getMapper(OaNoticeReplyMapper.class);
        try {
            for (int i = 0; i < list.size(); i++) {
                mapper.insert(list.get(i));
                if (i % 1000 == 999 || i == list.size() - 1) {
                    session.commit();
                    session.clearCache();
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
            session.rollback();
            return false;
        } finally {
            session.close();
        }
        return true;
    }

    @Override
    public int del(long id) {
        return oaNoticeReplyMapper.deleteById(id);
    }

    @Override
    public int delOfNotice(long noticeId) {
        int n = 0;
        QueryWrapper<OaNoticeReply> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id")
                .eq("notice_id", noticeId);
        List<OaNoticeReply> list = oaNoticeReplyMapper.selectList(queryWrapper);
        Iterator<OaNoticeReply> ir = list.iterator();
        while (ir.hasNext()) {
            OaNoticeReply oaNoticeReply = ir.next();
            n += del(oaNoticeReply.getId());
        }
        return n;
    }

    @Override
    public OaNoticeReply getOaNoticeReply(long noticeId, String userName) {
        QueryWrapper<OaNoticeReply> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("notice_id", noticeId)
                .eq("user_name", userName);
        OaNoticeReply oaNoticeReply = oaNoticeReplyMapper.selectOne(queryWrapper);
        if (oaNoticeReply != null) {
            User user = userCache.getUser(userName);
            oaNoticeReply.setUser(user);
        }
        return oaNoticeReply;
    }

}
