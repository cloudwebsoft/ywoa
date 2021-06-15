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
 * @since 2020-02-02
 */
@Data
@TableName(value="user_group")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Group extends Model<Group> {

    private static final long serialVersionUID = 1L;

    private String code;

    private String description;

    @TableField("isSystem")
    private Boolean isSystem;

    private Boolean isDept;

    private Integer isIncludeSubDept;

    private String deptCode;

    private String unitCode;

    private String kind;

    @Override
    protected Serializable pkVal() {
        return this.code;
    }

}
