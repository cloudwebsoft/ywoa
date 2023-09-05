package com.cloudweb.oa.service.impl;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.api.IMsgProducer;
import com.cloudweb.oa.entity.OaNotice;
import com.cloudweb.oa.entity.OaNoticeReply;
import com.cloudweb.oa.mapper.OaNoticeMapper;
import com.cloudweb.oa.service.IOaNoticeAttachService;
import com.cloudweb.oa.service.IOaNoticeReplyService;
import com.cloudweb.oa.service.IOaNoticeService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.SysProperties;
import com.cloudweb.oa.vo.OaNoticeVO;
import com.cloudwebsoft.framework.util.LogUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.redmoon.oa.Config;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import org.apache.commons.lang3.StringUtils;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fgf
 * @since 2019-12-29
 */
@Service
public class OaNoticeServiceImpl extends ServiceImpl<OaNoticeMapper, OaNotice> implements IOaNoticeService {
    @Autowired
    OaNoticeMapper oaNoticeMapper;
    @Autowired
    DozerBeanMapper dozerBeanMapper;
    @Autowired
    IOaNoticeReplyService oaNoticeReplyService;
    @Autowired
    IOaNoticeAttachService oaNoticeAttachService;

    @Autowired
    HttpServletRequest request;

    @Autowired
    SysProperties sysProperties;

    /**
     *
     * @param privilege
     * @param isNoticeAll 是否通知管理员
     * @param fromDate
     * @param toDate
     * @param isSearch
     * @param what
     * @param cond
     * @param orderBy
     * @param sort
     * @return
     */
    @Override
    public String getSqlList(Privilege privilege, boolean isNoticeAll, String fromDate, String toDate, boolean isSearch, String what, String cond, String orderBy, String sort) {
        String strCurDay = DateUtil.format(new java.util.Date(), "yyyy-MM-dd");
        String userName = SpringUtil.getUserName();

        String sql = "select * from oa_notice where 1=1";
        if (!privilege.isUserPrivValid(request, "admin") && !isNoticeAll) {
            sql += " and begin_date<=" + SQLFilter.getDateStr(strCurDay, "yyyy-MM-dd") + " and (end_date is null or end_date>=" + SQLFilter.getDateStr(strCurDay, "yyyy-MM-dd") + ")";
        }
        if (isSearch) {
            if (!"".equals(what)) {
                sql += " and " + cond + " like '%" + what + "%'";
            }

            if (!fromDate.equals("") && !toDate.equals("")) {
                java.util.Date d = DateUtil.parse(toDate, "yyyy-MM-dd");
                d = DateUtil.addDate(d, 1);
                String toDate2 = DateUtil.format(d, "yyyy-MM-dd");
                sql += " and (create_date>=" + SQLFilter.getDateStr(fromDate, "yyyy-MM-dd") + " and create_date<" + SQLFilter.getDateStr(toDate2, "yyyy-MM-dd") + ")";
            } else if (!fromDate.equals("")) {
                sql += " and create_date>=" + SQLFilter.getDateStr(fromDate, "yyyy-MM-dd");
            } else if (fromDate.equals("") && !toDate.equals("")) {
                sql += " and create_date<=" + SQLFilter.getDateStr(toDate, "yyyy-MM-dd");
            }
        }

        if (!privilege.isUserPrivValid(request, "admin") && !isNoticeAll) {
            sql += " and ((id in (select notice_id from oa_notice_reply where user_name = " + StrUtil.sqlstr(userName) + ")) or user_name=" + StrUtil.sqlstr(userName) + ")";
        }

        sql += " order by " + orderBy + " " + sort;
        return sql;
    }

    @Override
    @Transactional
    public ListResult listResult(String userName, String sql, int curPage, int pageSize) {
        PageHelper.startPage(curPage, pageSize); // 分页查询
        List<OaNotice> list = oaNoticeMapper.selectNoticeList(sql);
        PageInfo<OaNotice> pageInfo = new PageInfo<>(list);

        ListResult lr = new ListResult();
        Vector<OaNoticeVO> v = new Vector<>();

        // 逐条遍历list判断是否读过
        for (OaNotice oaNotice : list) {
            OaNoticeVO oaNoticeVO = dozerBeanMapper.map(oaNotice, OaNoticeVO.class);

            // 判断是否读过
            boolean isReaded = true;
            OaNoticeReply oaNoticeReply = oaNoticeReplyService.getOaNoticeReply(oaNotice.getId(), userName);
            if (oaNotice.getIsBold() == 1 || (oaNoticeReply != null && "0".equals(oaNoticeReply.getIsReaded()))) {
                isReaded = false;
            }
            oaNoticeVO.setReaded(isReaded);

            if (oaNoticeVO.getIsDeptNotice()) {
                oaNoticeVO.setKind("部门通知");
            } else {
                oaNoticeVO.setKind("公共通知");
            }

            // 置是否为新通知
            oaNoticeVO.setFresh(!isReaded && (oaNotice.getEndDate() == null || !oaNotice.getEndDate().isBefore(LocalDate.now())));

//            String caption = oaNotice.getTitle();
//            LocalDate now = LocalDate.now();
//            if (oaNotice.getEndDate() != null && oaNotice.getEndDate().isBefore(now)) {
//                caption = "<span style=\"color:#cccccc\">" + caption + "</span>";
//            } else {
//                if (!StringUtils.isEmpty(oaNotice.getColor())) {
//                    caption = "<span style=\"color:" + oaNotice.getColor() + "\">" + caption + "</span>";
//                }
//                if (!isReaded) {
//                    caption = "<b>" + caption + "</b>";
//                }
//            }
//            oaNoticeVO.setCaption(caption);
            v.add(oaNoticeVO);
        }

        lr.setResult(v);
        lr.setTotal(pageInfo.getTotal());

        return lr;
    }

    @Override
    public int delBatch(String ids) {
        int n = 0;
        String[] idTemp = ids.split(",");
        for (String id : idTemp) {
            long noticeId = StrUtil.toLong(id, -1);
            // 删除回复
            oaNoticeReplyService.delOfNotice(noticeId);

            // 删除附件
            oaNoticeAttachService.delOfNotice(noticeId);

            n += oaNoticeMapper.deleteById(id);
        }
        return n;
    }

    @Override
    public List<OaNotice> selectMyNoticeOnDesktop(String userName, int count) {
        PageHelper.startPage(1, count); // 分页查询
        return oaNoticeMapper.selectMyNoticeOnDesktop(userName, LocalDate.now());
    }

    @Override
    public OaNotice getNoticeById(long id) {
        return oaNoticeMapper.selectByIdWithAtt(id);
    }

    @Override
    public boolean isUserReaded(long id, String userName) {
        OaNoticeReply oaNoticeReply = oaNoticeReplyService.getOaNoticeReply(id, userName);
        return "1".equals(oaNoticeReply.getIsReaded());
    }

    @Override
    public boolean create(OaNotice oaNotice) {
        int num = oaNoticeMapper.insert(oaNotice);
        return num==1;
    }

    @Override
    public List<OaNotice> listImportant(String userName) {
        return oaNoticeMapper.listImportant(userName, LocalDate.now());
    }

    @Override
    public boolean createNoticeReply(OaNotice oaNotice, String receiver, boolean isToMobile) {
        String[] userNames = null;

        if (ConstUtil.NOTICE_IS_SEL_USER == oaNotice.getIsAll()) { // 选择特定用户的情况
            userNames = StrUtil.split(receiver, ",");
        } else if (ConstUtil.NOTICE_IS_DEPT == oaNotice.getIsAll()) { // 部门管理员选择全部人员 取出部门下全部用户,含子部门用户
            String userName = oaNotice.getUserName();
            DeptUserDb dud = new DeptUserDb();
            Vector<DeptDb> v1 = dud.getDeptsOfUser(userName); // 取得用户所在部门的deptcode
            if (v1.size() > 0) {
                List<String> list = new ArrayList<String>();
                for (Object o : v1) {
                    DeptDb deptDb = (DeptDb) o;
                    Vector<UserDb> v = dud.getAllUsersOfUnit(deptDb.getCode());
                    for (UserDb user : v) {
                        list.add(user.getName());
                    }
                }

                userNames = new String[list.size()];
                list.toArray(userNames);
            }
        } else if (ConstUtil.NOTICE_IS_ALL == oaNotice.getIsAll()) { // 系统管理员选择全部人员
            List<String> list = new ArrayList<>();
            DeptUserDb dud = new DeptUserDb();
            Vector<UserDb> v = dud.getAllUsersOfUnit(DeptDb.ROOTCODE);
            for (UserDb user : v) {
                list.add(user.getName());
            }

            userNames = new String[list.size()];
            list.toArray(userNames);
        } else {
            return false;
        }

        if (userNames == null || userNames.length == 0) {
            return false;
        }

        List<OaNoticeReply> list = new ArrayList<>();
        for (String userName : userNames) {
            OaNoticeReply reply = new OaNoticeReply();
            reply.setNoticeId(oaNotice.getId());
            reply.setUserName(userName);
            list.add(reply);
        }
        boolean re = oaNoticeReplyService.createBatch(list);

        if (re) {
            MessageDb mdb = new MessageDb();
            boolean mqIsOpen = sysProperties.isMqOpen();

            String txt = StrUtil.getAbstract(null, oaNotice.getContent(), 380, "", false) + "......";
            try {
                if (mqIsOpen) {
                    IMsgProducer msgProducer = SpringUtil.getBean(IMsgProducer.class);
                    msgProducer.sendSysMsg(userNames, "请注意查看：通知公告 " + oaNotice.getTitle(), txt, MessageDb.ACTION_NOTICE, "", String.valueOf(oaNotice.getId()));
                } else {
                    mdb.sendSysMsgNotice(oaNotice.getId(), userNames, "请注意查看：通知公告 " + oaNotice.getTitle(), txt);
                }

                if (isToMobile) {
                    if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
                        if (mqIsOpen) {
                            IMsgProducer msgProducer = SpringUtil.getBean(IMsgProducer.class);
                            msgProducer.sendSmsBatch(userNames, txt, oaNotice.getUserName());
                        }
                        else {
                            IMsgUtil imu = SMSFactory.getMsgUtil();
                            imu.sendBatch(userNames, txt, oaNotice.getUserName());
                        }
                    }
                }
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error(e);
                re = false;
            }
        }
        return re;
    }
}
