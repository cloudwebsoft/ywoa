package com.cloudweb.oa.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Data
// @ConfigurationProperties(prefix = "sys")
@RefreshScope
@Configuration
public class SysProperties {

    @Value("${sys.web.rootPath}")
    private String rootPath;

    @Value("${sys.web.uploadPath}")
    private String uploadPath;

    @Value("${sys.web.publicPath}")
    private String publicPath;

    @Value("${sys.web.msg.fetchDays}")
    private int msgFetchDays;

    @Value("${mq.isOpen}")
    private boolean mqOpen;

    @Value("${sys.cache.enabled}")
    private boolean cache;

    @Value("${sys.cache.type}")
    private String cacheType;

    @Value("${sys.cache.redis.cluster}")
    private boolean redisCluster;

    @Value("${sys.cache.redis.host}")
    private String redisHost;

    @Value("${sys.cache.redis.port}")
    private String redisPort;

    @Value("${sys.cache.redis.password}")
    private String redisPassword;

    @Value("${sys.cache.redis.maxTotal:8}")
    private int redisMaxTotal;

    @Value("${sys.cache.redis.maxIdle:8}")
    private int redisMaxIdle;

    @Value("${sys.cache.redis.minIdle:0}")
    private int redisMinIdle;

    @Value("${sys.cache.redis.maxWaitMillis:-1}")
    private int redisMaxWaitMillis;

    @Value("${sys.cache.redis.db:0}")
    private int redisDb;

    @Value("${sys.web.frontPath}")
    private String frontPath;

    @Value("${sys.web.domainName}")
    private String domainName;

    @Value("${report.type}")
    private String reportType;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${sys.id}")
    private String id;

    @Value("${sys.showId}")
    private boolean showId;

    @Value("${sys.user.mobile.required}")
    private boolean userMobileRequired;

    @Value("${sys.user.email.required}")
    private boolean userEmailRequired;

    @Value("${sys.front.tabTitle.maxLen}")
    private int tabTitleMaxLen;
	
    @Value("${sys.export.excel.async}")
    private boolean exportExcelAsync;	

    @Value("${sys.obj.store.enabled}")
    private boolean objStoreEnabled;

    @Value("${sys.obj.store.type}")
    private String objStoreType;

    @Value("${sys.obj.store.front}")
    private boolean objStoreFront;

    @Value("${sys.obj.store.endPoint}")
    private String objStoreEndPoint;

    @Value("${sys.obj.store.accessKeyId}")
    private String objStoreAccessKeyId;

    @Value("${sys.obj.store.secretAccessKey}")
    private String objStoreSecretAccessKey;

    @Value("${sys.obj.store.bucketName}")
    private String objStoreBucketName;

    @Value("${sys.obj.store.reserveLocalFile}")
    private boolean objStoreReserveLocalFile;

    @Value("${sys.obj.store.appId}")
    private String objStoreAppId;

    @Value("${sys.obj.store.region}")
    private String objStoreRegion;

    @Value("${sys.front.upload.panel}")
    private boolean uploadPanel;

    @Value("${sys.front.upload.panel.btn.suspension}")
    private boolean uploadPanelBtnSuspension;
}
