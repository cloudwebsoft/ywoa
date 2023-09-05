package com.cloudweb.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author fgf
 * @since 2022-02-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Post extends Model<Post> {

    private static final long serialVersionUID=1L;

    @TableId(value = "id")
    private Integer id;

    /**
     * 职位名称
     */
    private String name;

    /**
     * 单位
     */
    private String unitCode;

    /**
     * 描述
     */
    private String description;

    /**
     * 排序号
     */
    private Integer orders;

    /**
     * 所属部门
     */
    private String deptCode;

    /**
     * 限制人数
     */
    private Integer numLimited;

    /**
     * 是否限制
     */
    private Boolean limited;

    /**
     * 是否互斥
     */
    private Boolean excluded;

    private LocalDateTime createDate;

    private String creator;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    private Boolean status;

}
