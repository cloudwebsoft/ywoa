export enum FormField {
  TYPE_TEXTFIELD = 'text',
  TYPE_TEXTAREA = 'textarea',
  TYPE_SELECT = 'select',
  TYPE_LIST = 'list',
  TYPE_CHECKBOX = 'checkbox',
  TYPE_RADIO = 'radio',
  TYPE_DATE = 'DATE',
  TYPE_DATE_TIME = 'DATE_TIME',
  TYPE_MACRO = 'macro',

  TYPE_CALCULATOR = 'CALCULATOR',
  TYPE_SQL = 'SQL',
  TYPE_BUTTON = 'BUTTON',

  MACRO_NOT = '0',

  FORMAT_DATE = 'yyyy-MM-dd',
  FORMAT_DATE_TIME = 'yyyy-MM-dd HH:mm:ss',

  FIELD_TYPE_VARCHAR = 0,
  FIELD_TYPE_TEXT = 1,
  FIELD_TYPE_INT = 2,
  FIELD_TYPE_LONG = 3,
  FIELD_TYPE_BOOLEAN = 4,
  FIELD_TYPE_FLOAT = 5,
  FIELD_TYPE_DOUBLE = 6,
  FIELD_TYPE_DATE = 7,
  FIELD_TYPE_DATETIME = 8,
  FIELD_TYPE_PRICE = 9, // 价格型，00.00

  /**
   * 不隐藏
   */
  HIDE_NONE = 0,
  /**
   * 流程中编辑和显示时及模块显示、添加、编辑时均隐藏
   */
  HIDE_ALWAYS = 1,
  /**
   * 流程及模块中仅编辑时隐藏
   */
  HIDE_EDIT = 2,

  /**
   * 单行文本框字段的默认长度
   */
  TEXT_FIELD_DEFAULT_LENGTH = 100,

  DATE_CURRENT = 'CURRENT',

  /**
   * 不唯一，默认
   */
  UNIQUE_NONE = 0,
  /**
   * 全局唯一
   */
  UNIQUE_GLOBAL = 1,
  /**
   * 嵌套表（或关联模块）唯一
   */
  UNIQUE_NEST = 2,

  /**
   * 格式化：千分位
   */
  FORMAT_THOUSANDTH = '0',

  /**
   * 格式化：百分比，如0.01将会被转换为1%
   */
  FORMAT_PERCENTAGE = '1',

  /**
   * 模糊查询，包含
   */
  COND_TYPE_FUZZY = '0',

  /**
   * 准确查询，等于
   */
  COND_TYPE_NORMAL = '1',

  /**
   * 一段范围，用于数值型的表单域
   */
  COND_TYPE_SCOPE = '2',

  /**
   * 下拉菜单，可以勾选多个
   */
  COND_TYPE_MULTI = '3',

  /**
   * 两个模块之间没有关联关系
   */
  IS_NOT_RELATED = 'isNotRelated',

  /**
   * 不限，不含临时记录
   */
  CWS_STATUS_NOT_LIMITED = 10000,
  /**
   * 为空
   */
  IS_EMPTY = '=空',
  /**
   * 不为空
   */
  IS_NOT_EMPTY = '<>空',
}
