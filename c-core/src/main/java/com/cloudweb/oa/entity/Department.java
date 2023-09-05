package com.cloudweb.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

/**
 * <p>
 *
 * </p>
 *
 * @author fgf
 * @since 2020-01-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Department extends Model<Department> {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "{dept.code.null}")
    private String code;

    @NotEmpty(message = "{dept.name.null}")
    private String name;

    private String description;

    @NotEmpty(message = "{dept.parentCode.null}")
    @TableField("parentCode")
    private String parentCode;

    @TableField("rootCode")
    private String rootCode;

    private Integer orders;

    @TableField("childCount")
    private Integer childCount;

    @TableField("addDate")
    private LocalDateTime addDate;

    private Integer deptType;

    private Integer layer;

    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    private Integer isShow;

    @Length(min = 0, max = 45, message = "{dept.shortName.tooLong}")
    private String shortName;

    private Integer isGroup;

    private Integer isHide;

    @TableField(exist = false)
    private List<Department> children;

    @Override
    protected Serializable pkVal() {
        return this.code;
    }

}
