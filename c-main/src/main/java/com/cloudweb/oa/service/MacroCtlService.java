package com.cloudweb.oa.service;

import com.cloudweb.oa.api.*;

public interface MacroCtlService {

    INestTableCtl getNestTableCtl();

    INestSheetCtl getNestSheetCtl();

    ISQLCtl getSQLCtl();

    IBasicSelectCtl getBasicSelectCtl();

    IModuleFieldSelectCtl getModuleFieldSelectCtl();
}
