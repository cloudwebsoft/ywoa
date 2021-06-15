package com.cloudweb.oa.service;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.FormDAO;
import org.springframework.stereotype.Service;

public interface FormArchiveService {

    IFormDAO create(String userName, String code, String content);

    IFormDAO getCurFormArchiveRaw(String formCode);

    IFormDAO getCurFormArchive(String formCode);

    IFormDAO getCurFormArchiveOrInit(String formCode);

    boolean isUsedByFlow(long formArchiveId);

    void onFormUpdate(FormDb formDb) throws ErrMsgException;
}
