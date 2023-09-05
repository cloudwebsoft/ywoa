package com.cloudweb.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

/**
 * <p>
 *
 * </p>
 *
 * @author fgf
 * @since 2020-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Component
public class OaNoticeAttach extends Model<OaNoticeAttach> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long noticeId;

    private String name;

    @TableField("diskname")
    private String diskName;

    @TableField("visualpath")
    private String visualPath;

    private Integer orders;

    @TableField("downloadCount")
    private Integer downloadCount;

    private LocalDateTime uploadDate;

    private Long fileSize;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @TableField(exist=false)
    private boolean image;
}
