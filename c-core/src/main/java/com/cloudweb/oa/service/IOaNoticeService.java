package com.cloudweb.oa.service;

import cn.js.fan.db.ListResult;
import com.cloudweb.oa.entity.OaNotice;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudweb.oa.vo.OaNoticeVO;
import com.redmoon.oa.pvg.Privilege;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2019-12-29
 */
public interface IOaNoticeService extends IService<OaNotice> {
    String getSqlList(Privilege privilege, boolean isNoticeAll, String fromDate, String toDate, boolean isSearch, String what, String cond, String orderBy, String sort);

    ListResult listResult(String userName, String sql, int curPage, int pageSize);

    /**
     * 带附件
     * @param id
     * @return
     */
    OaNotice getNoticeById(long id);

    boolean create(OaNotice oaNotice);

    boolean createNoticeReply(OaNotice oaNotice, String receiver, boolean isToMobile);

    int delBatch(String ids);

    List<OaNotice> selectMyNoticeOnDesktop(String userName, int count);

    List<OaNotice> listImportant(String userName);

    boolean isUserReaded(long id, String userName);

}
