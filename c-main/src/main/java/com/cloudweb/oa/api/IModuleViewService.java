package com.cloudweb.oa.api;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import com.redmoon.oa.visual.ModuleSetupDb;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description:
 * @author:
 * @Date: 2018-1-12上午10:56:00
 */
public interface IModuleViewService {

    boolean add(HttpServletRequest request, String code) throws ErrMsgException;

    boolean del(HttpServletRequest request, String code) throws ErrMsgException;

    boolean modify(HttpServletRequest request, String code) throws ErrMsgException;

    boolean addTag(HttpServletRequest request, String code) throws ErrMsgException;

    boolean delTag(HttpServletRequest request, String code) throws ErrMsgException;

    /**
     * 修改导航标签
     *
     * @param request
     * @param code
     * @return
     * @throws ErrMsgException
     */
    boolean modifyTag(HttpServletRequest request, String code) throws ErrMsgException;

    /**
     * 用于远程调用
     *
     * @param request
     * @param code
     * @return
     * @throws ErrMsgException
     */
    boolean addCond(HttpServletRequest request, String code) throws ErrMsgException;

    boolean setFilter(HttpServletRequest request, String code) throws ErrMsgException;

    boolean setPromptIcon(HttpServletRequest request, String code) throws ErrMsgException;

    boolean setCols(HttpServletRequest request, String code) throws ErrMsgException;

    boolean addBtn(HttpServletRequest request, String code) throws ErrMsgException;

    boolean addBtnBatch(HttpServletRequest request, String code) throws ErrMsgException;

    boolean addBtnFlow(HttpServletRequest request, String code) throws ErrMsgException;

    boolean addBtnModule(HttpServletRequest request, String code) throws ErrMsgException;

    boolean delBtn(HttpServletRequest request, String code) throws ErrMsgException;

    /**
     * 修改按钮及查询
     *
     * @param request
     * @param code
     * @return
     * @throws ErrMsgException
     */
    boolean modifyBtn(HttpServletRequest request, String code) throws ErrMsgException;

    boolean saveBtn(ModuleSetupDb msd, com.alibaba.fastjson.JSONObject result) throws ResKeyException;

    /**
     * 增加操作列链接
     *
     * @param request
     * @param code
     * @return
     * @throws ErrMsgException
     */
    boolean addLink(HttpServletRequest request, String code) throws ErrMsgException;

    boolean modifyLink(HttpServletRequest request, String code) throws ErrMsgException;

    boolean saveLink(ModuleSetupDb msd, com.alibaba.fastjson.JSONObject result) throws ResKeyException;

    boolean saveCol(ModuleSetupDb msd, com.alibaba.fastjson.JSONObject result) throws ResKeyException;

    boolean delLink(HttpServletRequest request, String code) throws ErrMsgException;

    boolean setUse(HttpServletRequest request, String code) throws ErrMsgException;
}

