var DEBUG = true;
var LIST_TYPE = {
    "FORM_FIELD": 1,
    "NEST_SHEET_SELECT": 2,
    "NEST_SHEET_SELECT_DELETE": 3,
    "NEST_SHEET_CHOOSE_SELECT": 4
};
var AJAX_REQUEST_URL = {
    "NEST_SHEET_INIT": "../../public/android/module/initNestSheet",
    "NEST_SHEET_LIST": "../../public/android/module/listNestSheet",
    "NEST_SHEET_ADD": "../../public/android/module/addNestSheet",
    "NEST_SHEET_MODIFY": "../../public/android/module/editNestSheet",
    "NEST_SHEET_DELETE": "../../public/android/module/delNestSheet",
    "MODULE_LIST": {
        "NEST_SEL_LIST": "../../public/android/module/listNestSel"
    },
    "MODULE_LIST_SEL": "../../public/android/module/moduleListSel",
    "MODULE_DEL": "../../public/android/module/del",
    "MODULE_ADD_INIT": "../../public/android/module/add",
    "MODULE_EDIT_INIT": "../../public/android/module/edit",
    "MODULE_CHILD_ADD_INIT": "../../public/android/module/addRelate",
    "MODULE_CHILD_EDIT_INIT": "../../public/android/module/editRelate",
    "MODULE_SHOW": "../../public/android/module/show",
    "MODULE_ADD_DO": "../../modular/create.do",
    "MODULE_EDIT_DO": "../../modular/update.do"
};
var OP = {
    "ADD": 1,
    "EDIT": 0
};
var MACRO = "macro";
var CHECKBOX = "checkbox";
var FLOW_TYPE_FREE = 1;
var SEARCH_FIELD_TYPE = {
    FIELD_TYPE_VARCHAR: 0,
    FIELD_TYPE_TEXT: 1,
    FIELD_TYPE_INT: 2,
    FIELD_TYPE_LONG: 3,
    FIELD_TYPE_BOOLEAN: 4,
    FIELD_TYPE_FLOAT: 5,
    FIELD_TYPE_DOUBLE: 6,
    FIELD_TYPE_DATE: 7,
    FIELD_TYPE_DATETIME: 8,
    FIELD_TYPE_PRICE: 9
}