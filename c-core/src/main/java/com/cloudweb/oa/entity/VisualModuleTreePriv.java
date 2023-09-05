package com.cloudweb.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author fgf
 * @since 2022-08-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "VisualModuleTreePriv对象", description = "树形权限实体类")
public class VisualModuleTreePriv extends Model<VisualModuleTreePriv> {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "基础数据树形的根节点编码，即基础数据编码")
    private String rootCode;

    @ApiModelProperty(value = "基础数据树形的节点编码")
    private String nodeCode;

    @ApiModelProperty(value = "权限拥有者的名称")
    private String name;

    @ApiModelProperty(value = "权限拥有者的类型 0用户组,1用户,2角色,3部门")
    private int privType;

    @ApiModelProperty(value = "浏览")
    private Integer privSee;

    @ApiModelProperty(value = "添加")
    private Integer privAdd;

    @ApiModelProperty(value = "修改")
    private Integer privEdit;

    @ApiModelProperty(value = "下载")
    private Integer privDownload;

    @ApiModelProperty(value = "删除")
    private Integer privDel;

    @ApiModelProperty(value = "导出")
    private Integer privExport;

    @ApiModelProperty(value = "导入")
    private Integer privImport;

    @ApiModelProperty(value = "生成")
    private Integer privExportWord;

    @ApiModelProperty(value = "管理")
    private Integer privManage;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
