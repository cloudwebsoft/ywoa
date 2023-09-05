package com.cloudweb.oa.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserVO {

    private Integer id;

    @NotEmpty(message = "{user.name.notempty}")
    @Length(max = 20, message = "{user.name.length}")
    private String name;

    private String pwd;

    private String pwdRaw;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime regDate;

    @NotEmpty(message = "{user.realName.empty}")
    private String realName;

    /**
     * 论坛中所用的图片
     */
    private String picture;

    private String email;

    private Boolean gender;

    private String qq;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    private String IDCard;

    /**
     * 0 未婚
     */
    private Boolean isMarriaged;

    private String state;

    private String city;

    private String address;

    private String postCode;

    private String phone;

    private String mobile;

    private String hobbies;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastTime;

    /**
     * 1 有效 0 禁止
     */
    private Integer isValid;

    private String emailName;

    /**
     * 短号码
     */
    private String msn;

    /**
     * null -- no proxy  代理者的职位编码
     */
    private String proxy;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime proxyBeginDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime proxyEndDate;

    private Long diskSpaceAllowed;

    private Long diskSpaceUsed;

    private String rankCode;

    private Float onlineTime;

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

    private MultipartFile photo; // 不能是String，否则会报错

    /**
     * 0：未审批 1：通过 2：未通过
     */
    private Integer isPass;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate entryDate;

    private String weixin;

    private String dingding;

    private Integer orders;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLogin;

    private String leaderCode;

    private String deptCode;

    private String account;

    private String Password;

    private String Password2;

    private Long msgSpaceAllowed;

    private String loginName;
}
