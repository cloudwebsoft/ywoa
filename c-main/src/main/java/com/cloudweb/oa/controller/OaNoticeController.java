package com.cloudweb.oa.controller;


import cn.js.fan.db.ListResult;
import cn.js.fan.util.*;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.entity.OaNotice;
import com.cloudweb.oa.entity.OaNoticeAttach;
import com.cloudweb.oa.entity.OaNoticeReply;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.service.IFileService;
import com.cloudweb.oa.service.IOaNoticeAttachService;
import com.cloudweb.oa.service.IOaNoticeReplyService;
import com.cloudweb.oa.service.IOaNoticeService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.vo.OaNoticeVO;
import com.cloudweb.oa.vo.Result;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.Config;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.notice.NoticeAttachmentDb;
import com.redmoon.oa.notice.NoticeDb;
import com.redmoon.oa.notice.NoticeReplyDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.dozer.DozerBeanMapper;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
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

    @Autowired
    private IFileService fileService;

    @Autowired
    AuthUtil authUtil;

//    @RequestMapping("/listPage")
//    public String listPage(Model model) {
//        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
//        String userName = pvg.getUser(request);
//        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
//        boolean isNoticeAll = pvg.isUserPrivValid(request, "notice");
//        boolean isNoticeMgr = pvg.isUserPrivValid(request, "notice.dept");
//        boolean isNoticeAdd = pvg.isUserPrivValid(request, "notice.add");
//        model.addAttribute("isNoticeAll", isNoticeAll);
//        model.addAttribute("isNoticeMgr", isNoticeMgr);
//        model.addAttribute("isNoticeAdd", isNoticeAdd);
//        return "th/notice/notice_list";
//    }
    @ApiOperation(value = "列表", notes = "列表", httpMethod = "GET")
    // @PreAuthorize("hasAnyAuthority('notice', 'notice.dept', 'admin', 'notice.add')")
    @RequestMapping(value = "/list", produces={"text/html;charset=UTF-8;","application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> list() {
        int pageNum = ParamUtil.getInt(request, "pageNum", 1);
        int pageSize = ParamUtil.getInt(request, "pageSize", 20);
        String op = ParamUtil.get(request, "op");
        boolean isSearch = "search".equals(op);

        String fromDate = ParamUtil.get(request, "fromDate");
        String toDate = ParamUtil.get(request, "toDate");

        String what = ParamUtil.get(request, "what");
        String cond = ParamUtil.get(request, "cond");
        if ("".equals(cond)) {
            cond = "title";
        }

        String orderBy = ParamUtil.get(request, "orderBy");
        if ("".equals(orderBy)) {
            orderBy = "id";
        }
        String sort = ParamUtil.get(request, "sort");
        if ("".equals(sort)) {
            sort = "desc";
        }
        Privilege pvg = new Privilege();
        boolean isNoticeAll = pvg.isUserPrivValid(request, "notice");
        String userName = pvg.getUser(request);

        String sql = oaNoticeService.getSqlList(pvg, isNoticeAll, fromDate, toDate, isSearch, what, cond, orderBy, sort);
        ListResult lr = oaNoticeService.listResult(userName, sql, pageNum, pageSize);
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        json.put("list", lr.getResult());
        json.put("total", lr.getTotal());
        json.put("page", pageNum);
        return new Result<>(json);
    }

    @ApiOperation(value = "重要通知列表", notes = "重要通知列表", httpMethod = "POST")
    @RequestMapping(value = "/listImportant", produces={"application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> listImportant() {
        return new Result<>(oaNoticeService.listImportant(authUtil.getUserName()));
    }

    @ApiOperation(value = "新增", notes = "新增", httpMethod = "GET")
    @PreAuthorize("hasAnyAuthority('notice','notice.dept','admin', 'notice.add')")
    @RequestMapping("/add")
    @ResponseBody
    public Result<Object> add() {
        com.alibaba.fastjson.JSONObject object = new com.alibaba.fastjson.JSONObject();

        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        String userName = SpringUtil.getUserName();
        DeptUserDb du = new DeptUserDb();
        Vector<DeptDb> vector = du.getDeptsOfUser(userName);

        object.put("depts", vector);
        object.put("userName", userName);

        object.put("isUseSMS", com.redmoon.oa.sms.SMSFactory.isUseSMS());
        String myUnitCode = pvg.getUserUnitCode(request);
        object.put("myUnitCode", myUnitCode);

        boolean isNoticeAll = pvg.isUserPrivValid(request, "notice");
        boolean isNoticeMgr = pvg.isUserPrivValid(request, "notice.dept");
        object.put("isNoticeAll", isNoticeAll);
        object.put("isNoticeMgr", isNoticeMgr);

        boolean isAdmin = pvg.isUserPrivValid(request, "admin");
        object.put("isAdmin", isAdmin);

        object.put("curDate", DateUtil.format(new Date(), "yyyy-MM-dd"));

        return new Result<>(object);
    }

    /**
     * 删除
     *
     * @return
     */
    @ApiOperation(value = "删除", notes = "删除", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "ids", dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('notice','notice.dept','admin', 'notice.add')")
    @ResponseBody
    @RequestMapping(value = "/del", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> del(String ids) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();

        String[] ary = StrUtil.split(ids, ",");
        if (ary != null) {
            String myUnitCode = pvg.getUserUnitCode(request);
            for (String s : ary) {
                long id = StrUtil.toLong(s, -1);
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
                }
            }
        }

        boolean re = oaNoticeService.delBatch(ids) > 0;

        return new Result<>(re);
    }

    /**
     * 批量删除
     *
     * @return
     */
    @ApiOperation(value = "批量删除", notes = "批量删除", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "noticeId", value = "信息id", dataType = "long"),
            @ApiImplicitParam(name = "attId", value = "文件id", dataType = "long"),
    })
    @PreAuthorize("hasAnyAuthority('notice','notice.dept','admin','notice.add')")
    @ResponseBody
    @RequestMapping(value = "/delAtt", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> delAtt(long noticeId, long attId) {
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
            return new Result<>(false);
        }

        oaNoticeAttachService.del(attId);
        return new Result<>();
    }

    @ApiOperation(value = "列表页", notes = "列表页", httpMethod = "POST")
    @RequestMapping(value="/listPage", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> listPage() {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        boolean canManageNotice = false;
        if (pvg.isUserPrivValid(request, "notice")) {
            canManageNotice = true;
        } else if (pvg.isUserPrivValid(request, "notice.dept")) {
            canManageNotice = true;
        }
        JSONObject json = new JSONObject();
        json.put("canManage", canManageNotice);
        return new Result<>(json);
    }

    @ApiOperation(value = "显示详情", notes = "显示详情", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "long"),
    })
    @RequestMapping("/show")
    @ResponseBody
    public Result<Object> showPage(long id) {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        JSONObject jsonObject = new JSONObject();
        String priv = "read";
        if (!pvg.isUserPrivValid(request, priv)) {
            return new Result<>(false);
        }

        String userName = pvg.getUser(request);
        jsonObject.put("myUserName", userName);

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
        for (OaNoticeAttach att : attList) {
            String ext = StrUtil.getFileExt(att.getDiskName());
            boolean isImage = ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("gif") ||
                    ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("bmp");
            att.setImage(isImage);
        }
        jsonObject.put("attList", attList);

        // 取得已查看的用户数
        List<OaNoticeReply> readedList = oaNoticeReplyService.getReplyReadOrNot(id, 1);
        List<OaNoticeReply> notReadedList = oaNoticeReplyService.getReplyReadOrNot(id, 0);

        jsonObject.put("readedList", readedList);
        jsonObject.put("notReadedList", notReadedList);

        /*List<OaNoticeReply> replyList = oaNoticeReplyService.getReplyHasContent(id);
        jsonObject.put("replyList", replyList);*/

        boolean isNoticeAll = pvg.isUserPrivValid(request, "notice");
        boolean isNoticeMgr = pvg.isUserPrivValid(request, "notice.dept");

        jsonObject.put("notice", oaNoticeVO);
        jsonObject.put("myUserName", userName);
        jsonObject.put("isNoticeAll", isNoticeAll);
        jsonObject.put("isNoticeMgr", isNoticeMgr);

        boolean canReply = false;
        if (myOaNoticeReply != null) {
            /*user = user.getUserDb(myOaNoticeReply.getUserName());
            myOaNoticeReply.setRealName(user.getRealName());*/

            if ("0".equals(myOaNoticeReply.getIsReaded())) {
                myOaNoticeReply.setIsReaded("1");
                myOaNoticeReply.setReadTime(DateUtil.toLocalDateTime(new Date()));
                myOaNoticeReply.updateById();
            }

            if (StringUtils.isEmpty(myOaNoticeReply.getContent())) {
                canReply = true;
            }
            jsonObject.put("myReply", myOaNoticeReply);
        }
        jsonObject.put("canReply", canReply);

        return new Result<>(jsonObject);
    }

    @ApiOperation(value = "修改获取对象", notes = "修改获取对象", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "long"),
    })
    @PreAuthorize("hasAnyAuthority('notice','notice.dept','admin', 'notice.add')")
    @RequestMapping("/edit")
    @ResponseBody
    public Result<Object> edit(long id) {
        JSONObject object = new JSONObject();
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
                return new Result<>(false);
            }
        }

        String userName = pvg.getUser(request);

        MessageDb messageDb = new MessageDb();
        messageDb.setCommonUserReaded(pvg.getUser(request), id, MessageDb.MESSAGE_SYSTEM_NOTICE_TYPE);

        // 转换为VO
        OaNoticeVO oaNoticeVO = dozerBeanMapper.map(oaNotice, OaNoticeVO.class);
        if(oaNoticeVO.getIsDeptNotice()){
            oaNoticeVO.setKind("部门通知");
        }else{
            oaNoticeVO.setKind("公共通知");
        }

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
            StringBuffer sbNames = new StringBuffer();
            List<OaNoticeReply> replyList = oaNoticeReplyService.getReplies(id);
            for (OaNoticeReply oaNoticeReply : replyList) {
                user = user.getUserDb(oaNoticeReply.getUserName());
                StrUtil.concat(sbRealNames, "，", user.getRealName());
                StrUtil.concat(sbNames, "，", user.getName());
            }
            object.put("realNames", sbRealNames);
            object.put("names", sbNames);
        }

        object.put("notice", oaNoticeVO);
        object.put("myUserName", userName);
        object.put("isNoticeAll", isNoticeAll);
        object.put("isNoticeMgr", isNoticeMgr);
        object.put("myUnitCode", myUnitCode);

        return new Result<>(object);
    }

    @ApiOperation(value = "创建", notes = "创建", httpMethod = "POST")
    @PreAuthorize("hasAnyAuthority('notice','notice.dept','admin', 'notice.add')")
    @ResponseBody
    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> create(
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
        // StandardServletMultipartResolver multipartResolver = new StandardServletMultipartResolver();
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        if (!multipartResolver.isMultipart(request)) {
            return new Result<>(false);
        }

        MultipartHttpServletRequest multiRequest = multipartResolver.resolveMultipart(request);

        String title = ParamUtil.get(multiRequest, "title");
        String content = ParamUtil.get(multiRequest, "content");
        int isReply = ParamUtil.getInt(multiRequest, "isReply", 0);
        int isForcedResponse = ParamUtil.getInt(multiRequest, "isForcedResponse", 0);
        int isall = ParamUtil.getInt(multiRequest, "isAll", ConstUtil.NOTICE_IS_SEL_USER);
        String color = ParamUtil.get(multiRequest, "color");
        int isBold = ParamUtil.getInt(multiRequest, "isBold", 0);

        LocalDate beginDate = DateUtil.parseLocalDate(ParamUtil.get(multiRequest, "beginDate"), "yyyy-MM-dd");
        LocalDate endDate = DateUtil.parseLocalDate(ParamUtil.get(multiRequest, "endDate"), "yyyy-MM-dd");

        int isToMobile = ParamUtil.getInt(multiRequest, "isToMobile", 0);
        String receiver = ParamUtil.get(multiRequest, "receiver");
        int level = ParamUtil.getInt(multiRequest, "level", 0);

        String userName = SpringUtil.getUserName();
        DeptUserDb dud = new DeptUserDb();
        String unitCode = dud.getUnitOfUser(userName).getCode();
        try {
            String[] extAry = Config.getInstance().get("shortMsgFileExt").split(",");
            List<String> exts = java.util.Arrays.asList(extAry);

            int i = 0;
            MultipartFile[] files = new MultipartFile[multiRequest.getFileMap().size()];
            Iterator<String> names = multiRequest.getFileNames();
            while (names.hasNext()) {
                MultipartFile file = multiRequest.getFile(names.next().toString());
                if (file != null) {
                    files[i] = file;
                    String ext = StrUtil.getFileExt(file.getOriginalFilename());
                    if (!exts.contains(ext)) {
                        return new Result<>(false,"文件格式: " + ext + " 非法");
                    }
                    i++;
                }
            }

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
            oaNotice.setColor(color);
            oaNotice.setIsBold(isBold);
            boolean re = oaNoticeService.create(oaNotice);

            if (re) {
                oaNoticeService.createNoticeReply(oaNotice, receiver, isToMobile == 1);

                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(Calendar.YEAR));
                String month = "" + (cal.get(Calendar.MONTH) + 1);
                String vpath = ConstUtil.NOTICE_ATT_BASE_PATH + "/" + year + "/" + month;

                if (files.length > 0) {
                    // 循环获取file数组中得文件
                    for (i = 0; i < files.length; i++) {
                        MultipartFile file = files[i];
                        if (!file.isEmpty()) {
                            String name = file.getOriginalFilename(); // 带有完整路径
                            int p = name.lastIndexOf(File.separator);
                            if (p != -1) {
                                name = name.substring(p + 1);
                            }
                            // name = file.getName(); // 取的是表单域的名称
                            String ext = StrUtil.getFileExt(name);
                            String diskName = FileUpload.getRandName() + "." + ext;

                            fileService.write(file, vpath, diskName);

                            OaNoticeAttach oaNoticeAttach = new OaNoticeAttach();
                            oaNoticeAttach.setVisualPath(vpath);
                            oaNoticeAttach.setNoticeId(oaNotice.getId());
                            oaNoticeAttach.setName(name);
                            oaNoticeAttach.setDiskName(diskName);
                            oaNoticeAttach.setOrders(i);
                            oaNoticeAttach.setFileSize(file.getSize());
                            oaNoticeAttach.setUploadDate(LocalDateTime.now());
                            oaNoticeAttachService.create(oaNoticeAttach);
                        }
                    }
                }
            }

//            if (re) {
//                json.put("ret", "1");
//                json.put("msg", "操作成功！");
//            } else {
//                json.put("ret", "0");
//                json.put("msg", "操作失败！");
//            }
        } catch (IOException e) {
//            try {
//                json.put("ret", 0);
//                json.put("msg", e.getMessage());
//            } catch (JSONException e1) {
//                e1.printStackTrace();
//            }
        }
        return new Result<>();
    }

    @ApiOperation(value = "保存获取对象", notes = "保存获取对象", httpMethod = "POST")
    @PreAuthorize("hasAnyAuthority('notice','notice.dept','admin', 'notice.add')")
    @ResponseBody
    @RequestMapping(value = "/save", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> save() {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();

        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        if (!multipartResolver.isMultipart(request)) {
            return new Result<>(false);
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
                return new Result<>(false);
            }
        }

        String title = ParamUtil.get(multiRequest, "title");
        String content = ParamUtil.get(multiRequest, "content");
        int isReply = ParamUtil.getInt(multiRequest, "isReply", 0);
        int isForcedResponse = ParamUtil.getInt(multiRequest, "isForcedResponse", 0);
        int isall = ParamUtil.getInt(multiRequest, "isall", 0);
        String color = ParamUtil.get(multiRequest, "color");
        int isShow = ParamUtil.getInt(multiRequest, "isShow", 0);
        int level = ParamUtil.getInt(multiRequest, "level", 0);
        int isBold = ParamUtil.getInt(multiRequest, "isBold", 0);

        LocalDate beginDate = DateUtil.parseLocalDate(ParamUtil.get(multiRequest, "beginDate"), "yyyy-MM-dd");
        LocalDate endDate = DateUtil.parseLocalDate(ParamUtil.get(multiRequest, "endDate"), "yyyy-MM-dd");

        String userName = pvg.getUser(request);
        DeptUserDb dud = new DeptUserDb();
        String unitCode = dud.getUnitOfUser(userName).getCode();
        try {
            String[] extAry = Config.getInstance().get("shortMsgFileExt").split(",");
            List<String> exts = java.util.Arrays.asList(extAry);

            Iterator<String> names = multiRequest.getFileNames();
            while (names.hasNext()) {
                MultipartFile file = multiRequest.getFile(names.next());
                if (file != null && !file.isEmpty()) {
                    String ext = StrUtil.getFileExt(file.getOriginalFilename());
                    if (!exts.contains(ext)) {
                        return new Result<>(false,"文件格式: " + ext + " 非法");
                    }
                }
            }

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
            oaNotice.setIsShow(isShow==1);
            oaNotice.setNoticeLevel(level);
            oaNotice.setIsBold(isBold);
            boolean re = oaNoticeService.updateById(oaNotice);

            if (re) {
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(Calendar.YEAR));
                String month = "" + (cal.get(Calendar.MONTH) + 1);
                String vpath = ConstUtil.NOTICE_ATT_BASE_PATH + "/" + year + "/" + month;

                names = multiRequest.getFileNames();
                int i = 0;
                while (names.hasNext()) {
                    MultipartFile file = multiRequest.getFile(names.next());
                    if (file != null && !file.isEmpty()) {
                        String name = file.getOriginalFilename();
                        String ext = StrUtil.getFileExt(name);
                        String diskName = FileUpload.getRandName() + "." + ext;

                        fileService.write(file, vpath, diskName);

                        OaNoticeAttach oaNoticeAttach = new OaNoticeAttach();
                        oaNoticeAttach.setVisualPath(vpath);
                        oaNoticeAttach.setNoticeId(oaNotice.getId());
                        oaNoticeAttach.setName(name);
                        oaNoticeAttach.setDiskName(diskName);
                        oaNoticeAttach.setOrders(i);
                        oaNoticeAttach.setFileSize(file.getSize());
                        oaNoticeAttach.setUploadDate(LocalDateTime.now());
                        oaNoticeAttachService.create(oaNoticeAttach);
                    }
                }
            }

            if (!re) {
                return new Result<>(false);
            }
        } catch (IOException e) {
            json.put("ret", 0);
            json.put("msg", e.getMessage());
        }
        return new Result<>(json);
    }

    @RequestMapping("/getFile")
    public void getFile(HttpServletResponse response, @RequestParam(required = true) Long attachId) throws IOException, ErrMsgException {
        NoticeAttachmentDb att = new NoticeAttachmentDb(attachId);
        if (!att.isLoaded()) {
            throw new ErrMsgException("文件不存在");
        }

        fileService.download(response, att.getName(), att.getVisualPath(), att.getDiskName());
    }
}