<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit">
    <Row
      class="h-8 flex justify-items-center items-center"
      v-for="(action, index) in toActions"
      :key="index"
    >
      <Col :span="6">
        <span style="color:{{ action.color }}">{{ action.title }}</span
        >：</Col
      >
      <Col :span="18">
        <div class="flex justify-start flex-wrap">
          <Checkbox
            :id="`XOR${action.id}`"
            name="XorActionSelected"
            :value="action.internalName"
            style="display: none"
            :checked="action.xorChecked"
            v-if="action.hasOwnProperty('XorActionSelected')"
          />
          <span v-for="(checker, indexChecker) in action.checkers" :key="indexChecker">
            <Checkbox
              :name="`WorkflowAction_${action.id}`"
              v-model:checked="checker.checked"
              :disabled="checker.disabled"
              @click="checkXOR(action.id, checker, index)"
              :value="checker.value"
            >
              {{ checker.realName }}
            </Checkbox>
          </span>
          <Button
            type="primary"
            size="small"
            class="ml-2"
            @click="selectUser(action, index, action.isBtnXor)"
            v-if="action.isBtnSelUser"
          >
            选择
          </Button>
          <span v-if="action.hasOwnProperty('expireHour')"
            >完成时间：{{ action.expireHour }}{{ action.expireUnit }}
          </span>
        </div>
      </Col>
    </Row>
    <Row
      class="flex items-center"
      v-if="matchJson.hasOwnProperty('isMatchUserException') && matchJson.isMultiDept"
    >
      <Col :span="4"> 请选择部门 </Col>
      <Col :span="20">
        <RadioGroup
          v-model:value="selectDeptCode"
          name="deptOfUserWithMultiDept"
          @change="onSelDept"
        >
          <Radio
            :value="dept.deptCode"
            v-for="(dept, indexDept) in matchJson.multiDepts"
            :key="indexDept"
          >
            {{ dept.deptName }}
          </Radio>
        </RadioGroup>
      </Col>
    </Row>
    <div class="mt-2">{{ rendResult }}</div>
    <SelectUser @register="registerModalUser" @success="handleCallBack" />
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, h } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { SelectUser } from '/@/components/CustomComp';
  import { Button, Row, Col, Checkbox, Radio } from 'ant-design-vue';
  import { getMatchBranchAndUser } from '/@/api/process/process';
  import { useModal } from '/@/components/Modal';
  export default defineComponent({
    name: 'MatchBranchAndUserModal',
    components: {
      BasicModal,
      SelectUser,
      Button,
      Row,
      Col,
      Checkbox,
      Radio,
      RadioGroup: Radio.Group,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      let isUpdate = ref(true);
      const { t } = useI18n();
      let matchJson = ref<any>({ toActions: [] });
      const toActions = ref<any>([]);
      let rendResult = ref('');
      let formRowData = ref<any>({});
      const selectDeptCode = ref('');
      const { createMessage, createConfirm } = useMessage();
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false, width: '45%' });
        rendResult.value = data.rendResult;
        formRowData.value = data.formRowData;
        handleMatchJson(data.record);
        selectDeptCode.value = '';
      });

      const getTitle = '请选择';

      //处理分支线人员
      function handleMatchJson(obj) {
        matchJson.value = obj.data || {};
        toActions.value = [];
        console.log('matchJson', unref(matchJson));
        if (matchJson.value.toActions && matchJson.value.toActions.length > 0) {
          matchJson.value.toActions.forEach((item) => {
            toActions.value.push({
              ...item,
              deptOfUserWithMultiDept: matchJson.value.deptOfUserWithMultiDept,
              errCode: matchJson.value.errCode,
              flagXorRadiate: matchJson.value.flagXorRadiate,
              hasCond: matchJson.value.hasCond,
              info: matchJson.value.info,
              /* isBtnSelUserShow: matchJson.isBtnSelUserShow, */
              isMatchUserException: matchJson.value.isMatchUserException,
              op: matchJson.value.op,
              checkers:
                item.checkers && item.checkers.length > 0
                  ? item.checkers.map((el) => {
                      let isDisabled = false;
                      if (el.hasOwnProperty('disabled')) {
                        isDisabled = el.disabled;
                      }
                      if (!el.hasOwnProperty('checked')) {
                        el.checked = false;
                      } else {
                        el.checked = !!el.checked; // el.checked可能为''
                      }
                      return {
                        ...el,
                        disabled: isDisabled,
                        value: el.userName,
                      };
                    })
                  : [] || [],
              xorChecked:
                item.checkers && item.checkers.length > 0
                  ? item.checkers.some((v) => (v.checked == true ? true : false))
                  : false,
            });
          });
        }
      }

      //设置支线是否勾选人员，如果选中则默认选中当前支线
      function checkXOR(actionId, checker, itemIndex) {
        if (!checker.hasOwnProperty('clickXor')) {
          return;
        }
        var xorObj = document.getElementById('XOR' + actionId);
        if (xorObj == null) return;

        // 如果用户原来未被勾选上，点击后则被勾选上了，置xor控件标志为checked
        if (!checker.checked) {
          // $(xorObj).prop('checked', true); // 无效
          toActions.value[itemIndex].xorChecked = true;
          return;
        }

        // 判断如果该action中所有的用户都未选，则置xor控件标志为checked=false
        var isAllUnchecked = true;
        $("input[name='" + 'WorkflowAction_' + actionId + "']").each(function () {
          var chked = $(this).prop('checked');
          if (chked) {
            isAllUnchecked = false;
            return;
          }
        });

        if (isAllUnchecked) {
          toActions.value[itemIndex].xorChecked = false;
        }
      }

      // -----------------------------------------------------选择部门开始-------------------------------------------------------------
      // 当存在兼职的情况，选择部门时
      function onSelDept() {
        var params = {
          op: 'matchAfterSelDept',
          actionId: unref(formRowData).actionId,
          myActionId: unref(formRowData).myActionId,
          deptOfUserWithMultiDept: unref(selectDeptCode),
        };
        getMatchBranchAndUser(params).then((res) => {
          if (res.code === 200) {
            handleMatchJson(res || {});
          } else {
            createMessage.warn(res.msg);
          }
        });
      }
      // -----------------------------------------------------选择部门结束-------------------------------------------------------------
      // -----------------------------------------------------选择人员开始-------------------------------------------------------------
      const [registerModalUser, { openModal }] = useModal();
      // 是否在表单中选择用户标志位
      let isSelUserInForm = ref(false);
      let currentIndex = ref(0);
      let currentActionId = ref(0);
      let currentIsBtnXor = false;
      function selectUser(record, index, isBtnXor) {
        isSelUserInForm.value = false;
        currentActionId.value = record.id;
        currentIndex.value = index;
        currentIsBtnXor = isBtnXor;
        openModal(true, {
          isUpdate: false,
        });
      }

      //人员组件选择后回调
      function handleCallBack(data) {
        let datas = data;
        datas.forEach((item) => {
          item.value = item.name;
          item.checked = true;
          item.id = currentActionId.value;
          item.type = 'checkbox';
          item.disabled = false;
          item.clickXor = true;
        });

        if (currentIsBtnXor) {
          toActions.value[currentIndex.value].xorChecked = true;
        }
        let newDats = ref<any>([]);
        let values = toActions.value[currentIndex.value].checkers.map((item) => item.value);
        datas.forEach((item) => {
          if (!values.includes(item.value)) {
            newDats.value.push(item);
          }
        });

        toActions.value[currentIndex.value].checkers = [
          ...toActions.value[currentIndex.value].checkers,
          ...newDats.value,
        ];
      }
      // -----------------------------------------------------选择人员结束-------------------------------------------------------------

      async function handleSubmit() {
        try {
          setModalProps({ confirmLoading: true });

          var hasUser = false;
          var hasBtnSelUser = false;
          toActions.value.forEach((item) => {
            if (item.checkers && item.checkers.length > 0) {
              item.checkers.forEach((el) => {
                if (el.checked) {
                  hasUser = true;
                }
              });
            }

            if (item.isBtnSelUser) {
              hasBtnSelUser = true;
            }
          });

          if (!hasUser) {
            // 20161116 fgf 如果下一节点未匹配到人，则可能需跳过，所以不必再次确认
            if (hasBtnSelUser) {
              createMessage.error('请选择用户');
              return;
            }

            // 未选择用户，确定要提交么
            createConfirm({
              iconType: 'info',
              title: () => h('span', t('common.prompt')),
              content: () => h('span', '未选择用户，确定要提交么？'),
              maskClosable: false,
              onOk: async () => {
                await closeModal();
                emit('success');
              },
            });
          } else {
            closeModal();
            emit('success', unref(toActions));
          }
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      return {
        registerModal,
        getTitle,
        handleSubmit,
        rendResult,
        toActions,
        matchJson,
        checkXOR,
        selectUser,
        handleCallBack,
        onSelDept,
        selectDeptCode,
        registerModalUser,
      };
    },
  });
</script>
