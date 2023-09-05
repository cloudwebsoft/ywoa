package com.cloudweb.oa.api;

public interface IBasicDataService {
    boolean delTreeSelect(String code);

    boolean initTreeSelect(String rootCode, String rootName);
}
