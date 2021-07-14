package com.cloudweb.oa.controller;


import cn.js.fan.db.ListResult;
import cn.js.fan.util.*;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import com.cloudweb.oa.entity.OaNotice;
import com.cloudweb.oa.entity.OaNoticeAttach;
import com.cloudweb.oa.entity.OaNoticeReply;
import com.cloudweb.oa.service.IOaNoticeAttachService;
import com.cloudweb.oa.service.IOaNoticeReplyService;
import com.cloudweb.oa.service.IOaNoticeService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.vo.OaNoticeVO;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.notice.NoticeAttachmentDb;
import com.redmoon.oa.notice.NoticeDb;
import com.redmoon.oa.notice.NoticeReplyDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.ui.SkinMgr;
import org.apache.commons.lang3.StringUtils;
import org.dozer.DozerBeanMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 *
 * </p>
 *
 * @author fgf
 * @since 2019-12-29
 */
@Controller
@RequestMapping("/notice")
public class OaNoticeController {
    @Autowired
    HttpServletRequest request;
    @Autowired
    IOaNoticeService oaNoticeService;
    @Autowired
    IOaNoticeReplyService oaNoticeReplyService;
    @Autowired
    DozerBeanMapper dozerBeanMapper;

    @Autowired
    IOaNoticeAttachService oaNoticeAttachService;

    @Autowired
    I18nUtil i18nUtil;

    @RequestMapping("/list")
    public String list(Model model) {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        String priv = "read";
        if (!pvg.isUserPrivValid(request, priv)) {
            model.addAttribute("info", SkinUtil.LoadString(request, "pvg_invalid"));
            return "error";
        }

        String userName = pvg.getUser(request);

        boolean isNoticeAll = pvg.isUserPrivValid(request, "notice");
        boolean isNoticeMgr = pvg.isUserPrivValid(request, "notice.dept");
        boolean isNoticeAdd = pvg.isUserPrivValid(request, "notice.add");

        int pageNum = ParamUtil.getInt(request, "pageNum", 1);
        int pageSize = ParamUtil.getInt(request, "pageSize", 20);
        String op = ParamUtil.get(request, "op");

        String fromDate = ParamUtil.get(request, "fromDate");
        String toDate = ParamUtil.get(request, "toDate");

        String what = ParamUtil.get(request, "what");
        String cond = ParamUtil.get(request, "cond");
        if ("".equals(cond)) {
            cond = "title";
        }

        boolean isSearch = op.equals("search");

        String orderBy = ParamUtil.get(request, "orderBy");
        if (orderBy.equals("")) {
            orderBy = "id";
        }
        String sort = ParamUtil.get(request, "sort");
        if (sort.equals("")) {
            sort = "desc";
        }

        String querystr = "op=" + op + "&what=" + StrUtil.UrlEncode(what) + "&fromDate=" + fromDate + "&toDate=" + toDate + "&cond=" + cond;

        String sql = oaNoticeService.getSqlList(pvg, isNoticeAll, fromDate, toDate, isSearch, what, cond, orderBy, sort);
        ListResult lr = oaNoticeService.listResult(userName, sql, pageNum, pageSize);

        model.addAttribute("result", lr.getResult());

        model.addAttribute("skinPath", SkinMgr.getSkinPath(request));
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("what", what);
        model.addAttribute("cond", cond);
        model.addAttribute("op", op);
        model.addAttribute("orderBy", orderBy);
        model.addAttribute("total", lr.getTotal());
        model.addAttribute("pageNum", pageNum);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("cond", cond);

        model.addAttribute("isNoticeAll", isNoticeAll);
        model.addAttribute("isNoticeMgr", isNoticeMgr);
        model.addAttribute("isNoticeAdd", isNoticeAdd);
        model.addAttribute("querystr", querystr);

        return "notice/notice_list";
    }

    @PreAuthorize("hasAnyAuthority('notice','notice.dept','admin', 'notice.add')")
    @RequestMapping("/add")
    public String add(Model model) {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();

        String userName = SpringUtil.getUserName();

        String depts = "";
        DeptUserDb du = new DeptUserDb();
        java.util.Iterator ir = du.getDeptsOfUser(userName).iterator();
        depts = "";
        while (ir.hasNext()) {
            DeptDb dd = (DeptDb) ir.next();

            if (depts.equals("")) {
                depts = dd.getCode();
            } else {
                depts += "," + dd.getCode();
            }

            // 加入子部门
            Vector v = new Vector();
            try {
                dd.getAllChild(v, dd);
            } catch (ErrMsgException e) {
                e.printStackTrace();
            }
            Iterator ir2 = v.iterator();
            while (ir2.hasNext()) {
                DeptDb dd2 = (DeptDb) ir2.next();
                if (("," + depts + ",").indexOf("," + dd2.getCode() + ",") == -1) {
                    depts += "," + dd2.getCode();
                }
            }
        }

        model.addAttribute("depts", depts);
        model.addAttribute("userName", userName);
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request));

        model.addAttribute("isUseSMS", com.redmoon.oa.sms.SMSFactory.isUseSMS());
        String myUnitCode = pvg.getUserUnitCode(request);
        model.addAttribute("myUnitCode", myUnitCode);

        boolean isNoticeAll = pvg.isUserPrivValid(request, "notice");
        boolean isNoticeMgr = pvg.isUserPrivValid(request, "notice.dept");
        model.addAttribute("isNoticeAll", isNoticeAll);
        model.addAttribute("isNoticeMgr", isNoticeMgr);

        boolean isAdmin = pvg.isUserPrivValid(request, "admin");
        model.addAttribute("isAdmin", isAdmin);

        return "notice/notice_add";
    }

    /**
     * 删除
     *
     * @return
     */
    @PreAuthorize("hasAnyAuthority('notice','notice.dept','admin', 'notice.add')")
    @ResponseBody
    @RequestMapping(value = "/del", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String del(String ids, Model model) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();

        String[] ary = StrUtil.split(ids, ",");
        if (ary != null) {
            String myUnitCode = pvg.getUserUnitCode(request);
            for (int i = 0; i < ary.length; i++) {
                long id = StrUtil.toLong(ary[i], -1);
                OaNotice oaNotice = oaNoticeService.getNoticeById(id);
                boolean isUserPrivValid = false;
                if (pvg.isUserPrivValid(request, "notice")) {
                    isUserPrivValid = true;
                } else {
                    if (oaNotice.getIsDeptNotice() && pvg.isUserPrivValid(request, "notice.dept")) {
                        if (myUnitCode.equals(oaNotice.getUnitCode())) {
                            isUserPrivValid = true;
                        }
                    }
                }
                if (!isUserPrivValid) {
                    if (pvg.isUserPrivValid(request, "notice.add")) {
                        // 判断用户是否为发布者
                        if (oaNotice.getUserName().equals(pvg.getUser(request))) {
                            isUserPrivValid = true;
                        }
                    }
                    if (!isUserPrivValid) {
                        json.put("ret", "0");
                        json.put("msg", i18nUtil.get("pvg_invalid"));
                        return json.toString();
                    }
                }
            }
        } else {
            json.put("ret", "0");
            json.put("msg", "请选择记录！");
            return json.toString();
        }

        boolean re = oaNoticeService.delBatch(ids) > 0;
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    /**
     * 批量删除
     *
     * @return
     */
    @PreAuthorize("hasAnyAuthority('notice','notice.dept','admin','notice.add')")
    @ResponseBody
    @RequestMapping(value = "/delAtt", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String delAtt(long noticeId, long attId, Model model) {
        JSONObject json = new JSONObject();
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();

        String myUnitCode = pvg.getUserUnitCode(request);
        OaNotice oaNotice = oaNoticeService.getNoticeById(noticeId);
        boolean isUserPrivValid = false;
        if (pvg.isUserPrivValid(request, "notice")) {
            isUserPrivValid = true;
        } else {
            if (oaNotice.getIsDeptNotice() && pvg.isUserPrivValid(request, "notice.dept")) {
                if (myUnitCode.equals(oaNotice.getUnitCode())) {
                    isUserPrivValid = true;
                }
            }
        }
        if (!isUserPrivValid) {
            model.addAttribute("info", SkinUtil.LoadString(request, "pvg_invalid"));
            return "error";
        }

        boolean re = true;
        try {
            oaNoticeAttachService.del(attId);
            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @RequestMapping("/show")
    public String show(long id, Model model) {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        String priv = "read";
        if (!pvg.isUserPrivValid(request, priv)) {
            model.addAttribute("info", SkinUtil.LoadString(request, "pvg_invalid"));
            return "error";
        }

        String userName = pvg.getUser(request);

        MessageDb messageDb = new MessageDb();
        messageDb.setCommonUserReaded(pvg.getUser(request), id, MessageDb.MESSAGE_SYSTEM_NOTICE_TYPE);

        OaNotice oaNotice = oaNoticeService.getNoticeById(id);
        // 转换为VO
        OaNoticeVO oaNoticeVO = dozerBeanMapper.map(oaNotice, OaNoticeVO.class);
        // 判断是否读过
        OaNoticeReply myOaNoticeReply = oaNoticeReplyService.getOaNoticeReply(oaNotice.getId(), userName);

        boolean isReaded = true, isReplied = true;
        if (myOaNoticeReply != null) {
            isReaded = "1".equals(myOaNoticeReply.getIsReaded());
            isReplied = StringUtils.isEmpty(myOaNoticeReply.getContent());
        }
        oaNoticeVO.setReaded(isReaded);
        oaNoticeVO.setNotReplied(!isReplied);

        // 遍历附件，判断是否为图片
        List<OaNoticeAttach> attList = oaNoticeVO.getOaNoticeAttList();
        Iterator<OaNoticeAttach> ir = attList.iterator();
        while (ir.hasNext()) {
            OaNoticeAttach att = ir.next();
            String ext = StrUtil.getFileExt(att.getDiskname());
            boolean isImage = ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("gif") ||
                    ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("bmp");
            att.setImage(isImage);
        }

        // 取得已查看的用户数
        UserDb user = new UserDb();
        List<OaNoticeReply> readedList = oaNoticeReplyService.getReplyReadOrNot(id, true);
        List<OaNoticeReply> notReadedList = oaNoticeReplyService.getReplyReadOrNot(id, false);
        List<OaNoticeReply> replyList = oaNoticeReplyService.getReplyHasContent(id);
/*
        // 遍历回复，置realName
        Iterator<OaNoticeReply> irReply = readedList.iterator();
        while (irReply.hasNext()) {
            OaNoticeReply oaNoticeReply = irReply.next();
            user = user.getUserDb(oaNoticeReply.getUserName());
            oaNoticeReply.setRealName(user.getRealName());
        }

        irReply = notReadedList.iterator();
        while (irReply.hasNext()) {
            OaNoticeReply oaNoticeReply = irReply.next();
            user = user.getUserDb(oaNoticeReply.getUserName());
            oaNoticeReply.setRealName(user.getRealName());
        }

        irReply = replyList.iterator();
        while (irReply.hasNext()) {
            OaNoticeReply oaNoticeReply = irReply.next();
            user = user.getUserDb(oaNoticeReply.getUserName());
            oaNoticeReply.setRealName(user.getRealName());
        }*/

        model.addAttribute("readedList", readedList);
        model.addAttribute("notReadedList", notReadedList);
        model.addAttribute("replyList", replyList);

        boolean isNoticeAll = pvg.isUserPrivValid(request, "notice");
        boolean isNoticeMgr = pvg.isUserPrivValid(request, "notice.dept");

        model.addAttribute("notice", oaNoticeVO);
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request));
        model.addAttribute("myUserName", userName);
        model.addAttribute("isNoticeAll", isNoticeAll);
        model.addAttribute("isNoticeMgr", isNoticeMgr);

        boolean canReplay = false;
        if (myOaNoticeReply != null) {
            /*user = user.getUserDb(myOaNoticeReply.getUserName());
            myOaNoticeReply.setRealName(user.getRealName());*/

            if ("0".equals(myOaNoticeReply.getIsReaded())) {
                myOaNoticeReply.setIsReaded("1");
                myOaNoticeReply.updateById();
            }

            if (StringUtils.isEmpty(myOaNoticeReply.getContent())) {
                canReplay = true;
            }
            model.addAttribute("myReply", myOaNoticeReply);
        }
        model.addAttribute("canReply", canReplay);

        return "notice/notice_show";
    }

    @PreAuthorize("hasAnyAuthority('notice','notice.dept','admin', 'notice.add')")
    @RequestMapping("/edit")
    public String edit(long id, Model model) {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        String myUnitCode = pvg.getUserUnitCode(request);
        OaNotice oaNotice = oaNoticeService.getNoticeById(id);

        boolean isUserPrivValid = false;
        if (pvg.isUserPrivValid(request, "notice")) {
            isUserPrivValid = true;
        } else {
            if (oaNotice.getIsDeptNotice() && pvg.isUserPrivValid(request, "notice.dept")) {
                if (myUnitCode.equals(oaNotice.getUnitCode())) {
                    isUserPrivValid = true;
                }
            }
        }
        if (!isUserPrivValid) {
            if (pvg.isUserPrivValid(request, "notice.add")) {
                // 判断用户是否为发布者
                if (oaNotice.getUserName().equals(pvg.getUser(request))) {
                    isUserPrivValid = true;
                }
            }
            if (!isUserPrivValid) {
                model.addAttribute("info", SkinUtil.LoadString(request, "pvg_invalid"));
                return "error";
            }
        }

        String userName = pvg.getUser(request);

        MessageDb messageDb = new MessageDb();
        messageDb.setCommonUserReaded(pvg.getUser(request), id, MessageDb.MESSAGE_SYSTEM_NOTICE_TYPE);

        // 转换为VO
        OaNoticeVO oaNoticeVO = dozerBeanMapper.map(oaNotice, OaNoticeVO.class);
        // 判断是否读过
        /*OaNoticeReply myOaNoticeReply = oaNoticeReplyService.getOaNoticeReply(oaNotice.getId(), userName);
        oaNoticeVO.setReaded("1".equals(myOaNoticeReply.getIsReaded()));
        oaNoticeVO.setNotReplied(StringUtils.isEmpty(myOaNoticeReply.getContent()));*/

        UserDb user = new UserDb();
        user = user.getUserDb(oaNotice.getUserName());
        oaNoticeVO.setRealName(user.getRealName());

        boolean isNoticeAll = pvg.isUserPrivValid(request, "notice");
        boolean isNoticeMgr = pvg.isUserPrivValid(request, "notice.dept");

        // 遍历回复，置realName
        if (oaNotice.getIsAll() == 0) {
            StringBuffer sbRealNames = new StringBuffer();
            List<OaNoticeReply> replyList = oaNoticeReplyService.getReplies(id);
            Iterator<OaNoticeReply> irReaply = replyList.iterator();
            while (irReaply.hasNext()) {
                OaNoticeReply oaNoticeReply = irReaply.next();
                user = user.getUserDb(oaNoticeReply.getUserName());
                StrUtil.concat(sbRealNames, "，", user.getRealName());
            }
            model.addAttribute("realNames", sbRealNames);
        }

        model.addAttribute("notice", oaNoticeVO);
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request));
        model.addAttribute("myUserName", userName);
        model.addAttribute("isNoticeAll", isNoticeAll);
        model.addAttribute("isNoticeMgr", isNoticeMgr);
        model.addAttribute("myUnitCode", myUnitCode);

        return "notice/notice_edit";
    }

    @PreAuthorize("hasAnyAuthority('notice','notice.dept','admin', 'notice.add')")
    @ResponseBody
    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String create(
            HttpServletRequest request
            /*@RequestParam String title,
                         @RequestParam String content,
                         @RequestParam(defaultValue="0") int isReply,
                         @RequestParam(defaultValue="0") int isForcedResponse,
                         @RequestParam(defaultValue="0") int isall,
                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate beginDate,
                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                         @RequestParam(defaultValue="0") int isToMobile,
                         String receiver*/
            // @RequestParam(value = "att1", required = false) MultipartFile[] files
    ) {
        JSONObject json = new JSONObject();
        // StandardServletMultipartResolver multipartResolver = new StandardServletMultipartResolver();
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        if (!multipartResolver.isMultipart(request)) {
            try {
                json.put("ret", "0");
                json.put("msg", "上传格式非法！");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }

        MultipartHttpServletRequest multiRequest = multipartResolver.resolveMultipart(request);

        String title = ParamUtil.get(multiRequest, "title");
        String content = ParamUtil.get(multiRequest, "content");
        int isReply = ParamUtil.getInt(multiRequest, "isReply", 0);
        int isForcedResponse = ParamUtil.getInt(multiRequest, "isForcedResponse", 0);
        int isall = ParamUtil.getInt(multiRequest, "isall", 0);

        LocalDate beginDate = DateUtil.parseLocalDate(ParamUtil.get(multiRequest, "beginDate"), "yyyy-MM-dd");
        LocalDate endDate = DateUtil.parseLocalDate(ParamUtil.get(multiRequest, "endDate"), "yyyy-MM-dd");

        int isToMobile = ParamUtil.getInt(multiRequest, "isToMobile", 0);
        String receiver = ParamUtil.get(multiRequest, "receiver");
        int level = ParamUtil.getInt(multiRequest, "level", 0);

        String userName = SpringUtil.getUserName();
        DeptUserDb dud = new DeptUserDb();
        String unitCode = dud.getUnitOfUser(userName).getCode();
        try {
            OaNotice oaNotice = new OaNotice();
            oaNotice.setUserName(userName);
            oaNotice.setUnitCode(unitCode);
            oaNotice.setTitle(title);
            oaNotice.setContent(content);
            oaNotice.setIsForcedResponse(isForcedResponse);
            oaNotice.setIsReply(isReply);
            oaNotice.setIsAll(isall);
            oaNotice.setBeginDate(beginDate);
            oaNotice.setEndDate(endDate);
            oaNotice.setCreateDate(LocalDateTime.now());
            oaNotice.setNoticeLevel(level);
            boolean re = oaNoticeService.create(oaNotice);

            if (re) {
                oaNoticeService.createNoticeReply(oaNotice, receiver, isToMobile == 1);

                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(Calendar.YEAR));
                String month = "" + (cal.get(Calendar.MONTH) + 1);
                String vpath = ConstUtil.NOTICE_ATT_BASE_PATH + "/" + year + "/" + month + "/";

                int i = 0;
                MultipartFile[] files = null;
                files = new MultipartFile[multiRequest.getFileMap().size()];
                Iterator<String> names = multiRequest.getFileNames();
                while (names.hasNext()) {
                    MultipartFile file = multiRequest.getFile(names.next().toString());
                    if (file != null) {
                        files[i] = file;
                        i++;
                    }
                }

                if (files != null && files.length > 0) {
                    // 循环获取file数组中得文件
                    for (i = 0; i < files.length; i++) {
                        MultipartFile file = files[i];
                        if (!file.isEmpty()) {
                            byte[] bytes = file.getBytes();
                            String name = file.getOriginalFilename(); // 带有完整路径
                            int p = name.lastIndexOf(File.separator);
                            if (p != -1) {
                                name = name.substring(p + 1);
                            }
                            // name = file.getName(); // 取的是表单域的名称
                            String ext = StrUtil.getFileExt(name);
                            String diskName = FileUpload.getRandName() + "." + ext;
                            String filePath = Global.getRealPath() + "/" + vpath + "/" + diskName;

                            File f = new File(filePath);
                            if (!f.getParentFile().exists()) {
                                f.getParentFile().mkdirs();
                            }
                            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(filePath));
                            stream.write(bytes);
                            stream.close();

                            OaNoticeAttach oaNoticeAttach = new OaNoticeAttach();
                            oaNoticeAttach.setVisualpath(vpath);
                            oaNoticeAttach.setNoticeId(oaNotice.getId());
                            oaNoticeAttach.setName(name);
                            oaNoticeAttach.setDiskname(diskName);
                            oaNoticeAttach.setOrders(i);
                            oaNoticeAttach.setFileSize(file.getSize());
                            oaNoticeAttach.setUploadDate(LocalDateTime.now());
                            oaNoticeAttachService.create(oaNoticeAttach);
                        }
                    }
                }
            }

            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            try {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            try {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return json.toString();
    }

    @PreAuthorize("hasAnyAuthority('notice','notice.dept','admin', 'notice.add')")
    @ResponseBody
    @RequestMapping(value = "/save", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String save() {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();

        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        if (!multipartResolver.isMultipart(request)) {
            json.put("ret", "0");
            json.put("msg", "上传格式非法！");
            return json.toString();
        }

        MultipartHttpServletRequest multiRequest = multipartResolver.resolveMultipart(request);

        long id = ParamUtil.getLong(multiRequest, "id", -1);

        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        String myUnitCode = pvg.getUserUnitCode(request);
        OaNotice oaNotice = oaNoticeService.getNoticeById(id);

        boolean isUserPrivValid = false;
        if (pvg.isUserPrivValid(request, "notice")) {
            isUserPrivValid = true;
        } else {
            if (oaNotice.getIsDeptNotice() && pvg.isUserPrivValid(request, "notice.dept")) {
                if (myUnitCode.equals(oaNotice.getUnitCode())) {
                    isUserPrivValid = true;
                }
            }
        }
        if (!isUserPrivValid) {
            if (pvg.isUserPrivValid(request, "notice.add")) {
                // 判断用户是否为发布者
                if (oaNotice.getUserName().equals(pvg.getUser(request))) {
                    isUserPrivValid = true;
                }
            }
            if (!isUserPrivValid) {
                json.put("ret", "0");
                json.put("msg", i18nUtil.get("pvg_invalid"));
                return json.toString();
            }
        }

        String title = ParamUtil.get(multiRequest, "title");
        String content = ParamUtil.get(multiRequest, "content");
        int isReply = ParamUtil.getInt(multiRequest, "isReply", 0);
        int isForcedResponse = ParamUtil.getInt(multiRequest, "isForcedResponse", 0);
        int isall = ParamUtil.getInt(multiRequest, "isall", 0);
        String color = ParamUtil.get(multiRequest, "color");

        LocalDate beginDate = DateUtil.parseLocalDate(ParamUtil.get(multiRequest, "beginDate"), "yyyy-MM-dd");
        LocalDate endDate = DateUtil.parseLocalDate(ParamUtil.get(multiRequest, "endDate"), "yyyy-MM-dd");

        String userName = pvg.getUser(request);
        DeptUserDb dud = new DeptUserDb();
        String unitCode = dud.getUnitOfUser(userName).getCode();
        try {
            oaNotice.setUserName(userName);
            oaNotice.setUnitCode(unitCode);
            oaNotice.setTitle(title);
            oaNotice.setContent(content);
            oaNotice.setIsForcedResponse(isForcedResponse);
            oaNotice.setIsReply(isReply);
            oaNotice.setIsAll(isall);
            oaNotice.setBeginDate(beginDate);
            oaNotice.setEndDate(endDate);
            oaNotice.setColor(color);
            boolean re = oaNoticeService.updateById(oaNotice);

            if (re) {
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(Calendar.YEAR));
                String month = "" + (cal.get(Calendar.MONTH) + 1);
                String vpath = ConstUtil.NOTICE_ATT_BASE_PATH + "/" + year + "/" + month + "/";

                Iterator<String> names = multiRequest.getFileNames();
                int i = 0;
                while (names.hasNext()) {
                    MultipartFile file = multiRequest.getFile(names.next().toString());
                    if (file != null && !file.isEmpty()) {
                        String name = file.getOriginalFilename();
                        String ext = StrUtil.getFileExt(name);
                        String diskName = FileUpload.getRandName() + "." + ext;
                        String filePath = Global.getRealPath() + "/" + vpath + "/" + diskName;

                        OaNoticeAttach oaNoticeAttach = new OaNoticeAttach();
                        oaNoticeAttach.setVisualpath(vpath);
                        oaNoticeAttach.setNoticeId(oaNotice.getId());
                        oaNoticeAttach.setName(name);
                        oaNoticeAttach.setDiskname(diskName);
                        oaNoticeAttach.setOrders(i);
                        oaNoticeAttach.setFileSize(file.getSize());
                        oaNoticeAttach.setUploadDate(LocalDateTime.now());
                        oaNoticeAttachService.create(oaNoticeAttach);

                        File f = new File(filePath);
                        if (!f.getParentFile().exists()) {
                            f.getParentFile().mkdirs();
                        }
                        file.transferTo(f);
                    }
                }
            }

            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (FileNotFoundException e) {
            json.put("ret", 0);
            json.put("msg", e.getMessage());
        } catch (IOException e) {
            json.put("ret", 0);
            json.put("msg", e.getMessage());
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/addReply", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String addReply() {
        Privilege privilege = new Privilege();
        String userName = privilege.getUser(request);
        String content = ParamUtil.get(request,"content");
        long noticeId = ParamUtil.getLong(request,"noticeId",0);
        NoticeReplyDb noticeReplyDb = new NoticeReplyDb();
        noticeReplyDb.setUsername(userName);
        noticeReplyDb.setNoticeid(noticeId);
        noticeReplyDb.setContent(content);
        boolean flag = false;
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        try {
            flag = noticeReplyDb.save();
        } catch (ErrMsgException e) {
            e.printStackTrace();
            json.put("msg", e.getMessage());
        } catch (ResKeyException e) {
            e.printStackTrace();
            json.put("msg", e.getMessage());
        }

        if(flag){
            java.util.Date rDate = new java.util.Date();
            NoticeDb noticeDb = new NoticeDb();
            noticeDb = noticeDb.getNoticeDb(noticeId);
            if(noticeDb.getIs_forced_response() == 1){
                NoticeReplyDb nrdb = new NoticeReplyDb();
                nrdb.setIsReaded("1");
                nrdb.setReadTime(rDate);
                nrdb.setNoticeid(noticeId);
                nrdb.setUsername(userName);
                nrdb.saveStatus();
            }
            json.put("res",0);
        }else{
            json.put("res",1);
        }
        return json.toString();
    }

    @RequestMapping("/getfile")
    public void getfile(HttpServletResponse response) throws IOException, ErrMsgException, JSONException {
        Privilege privilege = new Privilege();
        if (!privilege.isUserPrivValid(request, "read")) {
            throw new ErrMsgException(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
        }

        long noticeId = ParamUtil.getLong(request, "noticeId");
        long attId = ParamUtil.getLong(request, "attachId");
        NoticeAttachmentDb att = new NoticeAttachmentDb(attId);

        if (!att.isLoaded()) {
            throw new ErrMsgException("文件不存在");
        }

        // 用下句会使IE在本窗口中打开文件
        // response.setContentType(MIMEMap.get(StrUtil.getFileExt(att.getDiskName())));
        // 使客户端直接下载，上句会使IE在本窗口中打开文件，下句也一样
        response.setContentType("application/octet-stream");
        response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode(att.getName()));

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(Global.realPath+att.getVisualPath()+"/"+att.getDiskName()));
            bos = new BufferedOutputStream(response.getOutputStream());

            byte[] buff = new byte[2048];
            int bytesRead;

            while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff,0,bytesRead);
            }
        } catch(final IOException e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
    }
}