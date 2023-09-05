package com.cloudweb.oa.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author fgf
 * @since 2020-01-09
 */
@TableName(value="users")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class User extends Model<User> {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String name;

    private String pwd;

    @TableField("pwdRaw")
    private String pwdRaw;

    @TableField("regDate")
    private LocalDateTime regDate;

    @TableField("realName")
    private String realName;

    /**
     * 论坛中所用的图片
     */
    private String picture;

    private String email;

    private Boolean gender;

    @TableField("QQ")
    private String qq;

    private LocalDate birthday;

    @TableField("IDCard")
    private String IDCard;

    /**
     * 0 未婚
     */
    @TableField("isMarriaged")
    private Boolean isMarriaged;

    private String state;

    private String city;

    private String address;

    @TableField("postCode")
    private String postCode;

    private String phone;

    private String mobile;

    private String hobbies;

    @TableField("lastTime")
    private LocalDateTime lastTime;

    /**
     * 1 有效 0 禁止
     */
    @TableField("isValid")
    private Integer isValid;

    @TableField("emailName")
    private String emailName;

    @TableField("MSN")
    private String msn;

    /**
     * null -- no proxy  代理者的职位编码
     */
    private String proxy;

    @TableField("proxyBeginDate")
    private LocalDateTime proxyBeginDate;

    @TableField("proxyEndDate")
    private LocalDateTime proxyEndDate;

    @TableField("diskSpaceAllowed")
    private Long diskSpaceAllowed;

    @TableField("diskSpaceUsed")
    private Long diskSpaceUsed;

    @TableField("rankCode")
    private String rankCode;

    private Float onlineTime;

    /**
     * RTX
     */
    private String uin;

    private String unitCode;

    private String personNo;

    private String userType;

    /**
     * 职务
     */
    private String duty;

    /**
     * 政治面貌
     */
    private String party;

    /**
     * 简历
     */
    private String resume;

    private String photo;

    /**
     * 0：未审批 1：通过 2：未通过 
     */
    @TableField("isPass")
    private Integer isPass;

    @TableField("entryDate")
    private LocalDate entryDate;

    private String weixin;

    private String dingding;

    private Integer orders;

    private LocalDateTime lastLogin;

    private String openId;

    private String unionId;

    private String loginName;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @TableField(exist=false)
    private List<UserOfRole> userRoleList;

}
