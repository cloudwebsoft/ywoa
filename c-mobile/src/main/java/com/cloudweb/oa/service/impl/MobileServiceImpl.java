package com.cloudweb.oa.service.impl;

import cn.js.fan.security.ThreeDesUtil;
import com.cloudweb.oa.service.IMobileService;
import com.cloudweb.oa.utils.ConstUtil;
import com.redmoon.oa.android.CloudConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Service
public class MobileServiceImpl implements IMobileService {

    @Override
    public String generateSkey(String userName) {
        Date now = new Date();
        String skey = userName + "|" + "OA" + "|" + now.getTime();
        CloudConfig cloudConfig = CloudConfig.getInstance();
        String key = cloudConfig.getProperty("key");
        return ThreeDesUtil.encrypt2hex(key, skey);
    }

    @Override
    public String getUserNameBySkey(String skey) {
        String userName = "";
        if (StringUtils.isNotEmpty(skey)) {
            CloudConfig cloudConfig = CloudConfig.getInstance();
            String text = ThreeDesUtil.decrypthexstr(cloudConfig.getProperty("key"), skey);
            int index = text.indexOf("|");
            if (index == -1) {
                return null;
            }
            userName = text.substring(0, index);
        }
        return userName;
    }

    @Override
    public String getSkey(HttpServletRequest request) {
        String skey = request.getHeader(ConstUtil.SKEY);
        if (skey == null) {
            skey = request.getParameter("skey");
        }
        return skey;
    }
}
