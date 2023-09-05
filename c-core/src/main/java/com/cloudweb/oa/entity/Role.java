package com.cloudweb.oa.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author fgf
 * @since 2020-01-23
 */
@Data
@TableName(value="user_role")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Role extends Model<Role> {

    private static final long serialVersionUID = 1L;

    private String code;

    private String description;

    @TableField("isSystem")
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

    private String kind;

    @Override
    protected Serializable pkVal() {
        return this.code;
    }

    private Boolean status;

    private Integer id;
}
