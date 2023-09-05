package com.cloudweb.oa.vo;

import lombok.Data;

@Data
public class RoleVO {

    private String code;

    private String description;

    private Boolean isSystem;

    private Integer orders;

    private Long diskQuota;

    private String unitCode;

    private Long msgSpaceQuota;

    private String rankCode;

    private Integer roleType;

    /**
     * 是否可管理本部门  1 是 0 否
     */
    private String isDeptManager;

    private String unitName;

    private String diskQuotaDesc;

    private String msgSpaceQuotaDesc;

    private String userRealNames;

    /**
     * 用于用户组中显示是否属于该角色
     */
    private boolean checked;

    private String kindName;

    boolean status;

    int userCount = 0;
}
