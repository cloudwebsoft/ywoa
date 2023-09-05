package com.cloudweb.oa.service;

import javax.servlet.http.HttpServletRequest;

public interface IMobileService {
    String generateSkey(String userName);

    String getUserNameBySkey(String skey);

    String getSkey(HttpServletRequest request);
}
