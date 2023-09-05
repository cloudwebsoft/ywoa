<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="100%"
    :showOkBtn="true"
    :cancelText="'关闭'"
    :destroyOnClose="true"
    @close="closeCurrentDrawer"
    @ok="handleOk"
  >
    <template #title>
      <div class="flex justify-between items-center">
        <div>{{ getTitle }}</div>
        <div class="flex">
          <div class="mr-2">
            <Popover title="配置" trigger="click">
              <template #content>
                <Form class="w-100" :label-col="labelCol" :wrapper-col="wrapperCol">
                  <FormItem label="屏宽类型">
                    <RadioGroup v-model:value="layoutRecord.widthType" button-style="solid">
                      <template v-for="item in widthTypeOptions" :key="`${item.value}`">
                        <RadioButton :value="item.value">
                          {{ item.label }}
                        </RadioButton>
                      </template>
                    </RadioGroup>
                  </FormItem>
                  <FormItem label="屏宽">
                    <InputNumber
                      size="small"
                      class="w-20"
                      :max="layoutRecord.widthType == 2 ? 100 : 999999"
                      :min="layoutRecord.widthType == 2 ? 30 : 300"
                      v-model:value="layoutRecord.screenWidth"
                    />
                  </FormItem>
                  <FormItem label="列长">
                    <InputNumber
                      size="small"
                      class="w-20"
                      :max="999"
                      :min="6"
                      v-model:value="layoutRecord.colSpans"
                    />
                  </FormItem>
                  <FormItem label="背景图片">
                    <TreeSelect
                      size="small"
                      class="w-20"
                      :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                      placeholder="请选择背景图片"
                      allow-clear
                      show-search
                      :tree-data="imgeLists"
                      treeNodeFilterProp="title"
                      v-model:value="layoutRecord.backgroundImge"
                      :getPopupContainer="() => document.body"
                    />
                  </FormItem>
                  <FormItem label="背景色">
                    <ColorPicker v-model:value="layoutRecord.backgroundColor" :isWidget="false" />
                  </FormItem>
                </Form>
              </template>
              <Icon icon="ant-design:setting-outlined" class="mr-2 cursor-pointer" />
            </Popover>
          </div>
          <a-button type="primary" size="small" @click="handleAdd">新增</a-button>
        </div>
      </div>
    </template>
    <div
      :style="{
        backgroundColor: layoutRecord.backgroundColor,
        backgroundImge: layoutRecord.backgroundImge
          ? `url(${layoutRecord.backgroundImge})`
          : 'none',
        backgroundRepeat: layoutRecord.backgroundImge ? 'no-repeat' : 'none',
        backgroundSize: layoutRecord.backgroundImge ? '100% 100%' : 'none',
        width: '100%',
        overflow: 'auto',
      }"
      class="h-full"
    >
      <div
        :style="{
          width:
            layoutRecord.widthType == 1
              ? `${layoutRecord.screenWidth}px`
              : layoutRecord.widthType == 2
              ? `${layoutRecord.screenWidth}%`
              : '100%',
        }"
        class="h-full"
      >
        <grid-layout
          v-model:layout="layoutRecord.layoutList"
          :col-num="column"
          :row-height="30"
          :is-draggable="true"
          :is-resizable="true"
          :is-mirrored="false"
          :vertical-compact="true"
          :margin="[10, 10]"
          :use-css-transforms="true"
        >
          <grid-item
            v-for="(item, index) in layoutRecord.layoutList"
            :key="item.i"
            drag-allow-from=".toolbox"
            :i="item.i"
            :x="item.x"
            :y="item.y"
            :w="item.w"
            :h="item.h"
          >
            <div style="display: flex; flex-direction: column; height: 100%">
              <div class="flex justify-between">
                <div
                  class="toolbox bg-green-400 pt-1 flex justify-left w-1/3"
                  style="height: 30px"
                  @dblclick="handleItem(item)"
                >
                  <span class="mb-2 ml-2"
                    ><Icon :icon="item?.item?.icon" :size="18" class="mr-2"
                  /></span>
                  <span :title="item?.item?.title">{{ item?.item?.title || '请设置' }}</span>
                </div>
                <div class="mb-2 flex justify-between w-1/3 bg-white p-1">
                  <div>条数：{{ item?.item?.rowCount }}</div>
                  <div>
                    <Popover title="配置" placement="leftTop">
                      <template #content>
                        <Form>
                          <FormItem label="是否内嵌">
                            <Switch
                              v-model:checked="item.item.embedded"
                              checked-children="是"
                              un-checked-children="否"
                            />
                          </FormItem>
                          <template v-if="item.item?.embedded">
                            <FormItem label="内嵌操作">
                              <a-button
                                type="primary"
                                size="small"
                                @click="addChildItem(item, index)"
                              >
                                新增
                              </a-button>
                            </FormItem>
                            <FormItem label="内嵌列长">
                              <InputNumber
                                size="small"
                                class="w-20"
                                v-model:value="item.item.childColumn"
                              />
                            </FormItem>
                          </template>
                        </Form>
                      </template>
                      <Icon icon="ant-design:setting-outlined" class="mr-2 cursor-pointer" />
                    </Popover>

                    <Icon
                      icon="ant-design:edit-outlined"
                      class="mr-2 cursor-pointer"
                      @click="handleItem(item)"
                      v-if="!item.item?.embedded"
                    />
                    <Icon
                      icon="ant-design:close-outlined"
                      @click="delDrag(item, index)"
                      class="cursor-pointer"
                    />
                  </div>
                </div>
              </div>
              <div
                style="height: 100%; text-align: left; background-color: #eee"
                class="cursor-pointer overflow-hidden"
              >
                <div v-if="item.item?.embedded" class="h-full overflow-auto">
                  <EmbeddedGridLayout
                    :childColumn="item?.item?.childColumn"
                    v-model:children="item.item.children"
                    :imgeLists="imgeLists"
                    :key="`children${item.i}`"
                    :parentI="item.i"
                  />
                </div>
              </div>
            </div>
          </grid-item>
        </grid-layout>
      </div>
    </div>
    <PortalManageDesignModal
      @register="registerPortalManageDesignModal"
      @success="handlePortalManageDesignModalCallBack"
    />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, h, onMounted, provide } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { GridItem, GridLayout } from '/@/components/GridLayout';
  import { useModal } from '/@/components/Modal';
  import { getSetup, getUpdateSetup } from '/@/api/system/system';
  import { useMessage } from '/@/hooks/web/useMessage';
  import Icon from '/@/components/Icon/index';
  import { InputNumber, Popover, Form, Switch, TreeSelect, Radio } from 'ant-design-vue';
  import PortalManageDesignModal from './modules/PortalManageDesignModal.vue';
  import EmbeddedGridLayout from './modules/EmbeddedGridLayout.vue';
  import { ColorPicker } from '/@/components/ColorPicker';
  import { getLsdList } from '/@/api/process/process';
  import { isObject, isArray } from '/@/utils/is';
  import { cloneDeep } from 'lodash-es';
  export default defineComponent({
    name: 'ProcessDrawer',
    components: {
      BasicDrawer,
      GridLayout,
      GridItem,
      PortalManageDesignModal,
      Icon,
      InputNumber,
      Popover,
      EmbeddedGridLayout,
      Form,
      FormItem: Form.Item,
      Switch,
      Radio,
      RadioGroup: Radio.Group,
      RadioButton: Radio.Button,
      TreeSelect,
      ColorPicker,
    },
    emits: ['register', 'success'],
    setup(_, { emit }) {
      // provide(eventBusKey); //需要外部调用方法时候使用
      const { createMessage, createConfirm } = useMessage();
      const defaultRecord = ref({
        //宽度类型 默认百分比
        widthType: '2',
        screenWidth: 100,
        layoutList: [],
        colSpans: 12,
        backgroundColor: '#f0f2f5', //屏幕背景色
        backgroundImge: '', //屏幕背景图片
      });
      const layoutRecord = ref(cloneDeep(defaultRecord.value));
      const widthTypeOptions = ref([
        {
          label: '像素',
          value: '1',
        },
        {
          label: '百分比',
          value: '2',
        },
      ]);
      const column = ref(12);
      const getTitle = '设计';
      const layoutItem = ref({
        x: 0,
        y: 0,
        w: 4,
        h: 8,
        i: '0',
        static: false,
        minH: 2,
        item: {
          icon: '',
          title: '',
          showTitle: true, //是否显示标题
          titleAlign: 'left', //标题位置
          titleSize: '16', //标题大小
          titleColor: '#000000', //标题颜色
          titleBackgroundImge: '', //标题背景图
          boxBorder: '#efefef', //边框颜色
          showHorn: '0', //是否显示四个角 默认不显示
          hornColor: '#02a6b5', //四个角颜色
          boxBackgroundImge: '', //卡片背景
          imgeCenter: '0', //图片是否居中，默认不居中 铺满
          cardHeadBorderBottomColor: '#f9f9f9', //标题底部线条
          boxBackgroundColor: '#ffffff', //卡片背景色
          type: '',
          rowCount: 6,
          embedded: false, //是否内嵌
          childColumn: 12, //内嵌列长
          children: [],
          meta: {
            dirCode: '',
            typeCode: '',
            chartId: '',
            formCode: '',
            carouselPictureId: '',
            leftField: '',
            rightField: '',
            isShowDirImage: '1',
            cardId: '',
            showSearch: '0',
            showToolbar: '0',
            showOpCol: '0',
            pagination: '0',
          },
        },
      });
      //const layoutList = ref<Recordable[]>([]),
      const rowNumber = ref(9);
      const list = ref<Recordable[]>([]);
      for (let i = 0; i < rowNumber.value; i++) {
        list.value.push({
          ...cloneDeep(layoutItem.value),
          x: i % 3 == 1 ? 0 : i % 3 == 2 ? 4 : 8,
          y: i >= 0 && i < 3 ? 0 : i >= 3 && i < 6 ? 1 : 2,
        });
      }

      const mainId = ref('');
      //初始化抽屉
      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner((data) => {
        setDrawerProps({ confirmLoading: false });
        mainId.value = data?.record?.id;
        layoutRecord.value = cloneDeep(defaultRecord.value);
        fetch();
      });

      const fetch = () => {
        getSetup({ id: mainId.value }).then((res) => {
          if (res?.setup && isObject(JSON.parse(res?.setup))) {
            layoutRecord.value = JSON.parse(res.setup);
          } else if (res?.setup && isArray(JSON.parse(res?.setup))) {
            layoutRecord.value = {
              widthType: '2',
              screenWidth: 100,
              layoutList: JSON.parse(res?.setup),
              colSpans: 12,
              backgroundColor: '#ffffff', //屏幕背景色
              backgroundImge: '', //屏幕背景图片
            };
          }

          // 清洗旧数据，初始化边框色
          if (layoutRecord.value.layoutList && layoutRecord.value.layoutList.length) {
            layoutRecord.value.layoutList.forEach((node: any) => {
              // node.item.boxBorder = node.item.boxBorder ? node.item.boxBorder : '#f0f0f0';
              // node.item.cardHeadBorderBottomColor = node.item.cardHeadBorderBottomColor
              //   ? node.item.cardHeadBorderBottomColor
              //   : '#f0f0f0';

              node.item.boxBorder = '#f0f0f0';
              node.item.cardHeadBorderBottomColor = '#f0f0f0';
              node.item.boxBackgroundColor = '#ffffff';
            });
            layoutRecord.value.backgroundColor = '#f0f2f5';
          }
          if (layoutRecord.value.layoutList.length == 0) {
            layoutRecord.value.layoutList = list.value;
          }

          console.log('layoutList==>', layoutRecord.value.layoutList);
        });
      };

      //成功回调
      function handleSuccess() {
        emit('success');
      }

      //关闭抽屉
      function closeCurrentDrawer() {
        layoutRecord.value = cloneDeep(defaultRecord.value);
        // emit('success');
      }

      const handleOk = () => {
        console.log('layoutList', layoutRecord.value.layoutList);
        setDrawerProps({ confirmLoading: true });
        getUpdateSetup({ id: mainId.value, setup: JSON.stringify(layoutRecord.value) })
          .then((res) => {
            console.log('res', res);
          })
          .finally(() => {
            setDrawerProps({ confirmLoading: false });
            closeDrawer();
            emit('success');
          });
      };

      const [registerPortalManageDesignModal, { openModal }] = useModal();

      const handleItem = (record: Recordable) => {
        openModal(true, { isUpdate: true, record, imgeLists: imgeLists.value });
      };

      const handlePortalManageDesignModalCallBack = (record: Recordable) => {
        console.log('record==>', record);
        layoutRecord.value.layoutList.forEach((item) => {
          if (item.i == record.id) {
            item = Object.assign(item, record);
          }
        });
        console.log('layoutList==>', layoutRecord.value.layoutList);
      };

      const delDrag = (record: Recordable, index) => {
        console.log(record);

        createConfirm({
          iconType: 'warning',
          title: () => h('span', '确定删除吗？'),
          onOk: async () => {
            layoutRecord.value.layoutList = layoutRecord.value.layoutList.filter(
              (_, i) => i != index,
            );
            layoutRecord.value.layoutList.forEach((item, i) => {
              item.i = i;
            });
          },
        });
      };

      const handleAdd = () => {
        let i = layoutRecord.value.layoutList.length;
        let item: Recordable = cloneDeep(layoutItem.value);

        if (i != 0) {
          const arr = layoutRecord.value.layoutList || [];
          const maxY = arr.length > 0 ? Math.max(...arr.map((res) => res.y)) : 0;
          // const arr = JSON.parse(JSON.stringify(layoutList));
          // item = { ...arr[i - 1] };
          console.log('item==>', item);
          item.x = 0;
          item.y = maxY - 0 + 1;
          item.i = i;
          item.minH = 1;
          item.children = [];
        }
        console.log('item==>', item);
        layoutRecord.value.layoutList.push(item);
        console.log('layoutListlayoutListlayoutList==>', layoutRecord.value.layoutList);
      };

      //新增子级个数
      const addChildItem = (record: Record, index: Number) => {
        console.log('record', record, index);

        if (!record.item.children) {
          record.item.children = [];
        }
        let i = record.item.children.length;
        let item: Recordable = {
          ...cloneDeep(layoutItem.value),
          w: 6,
          h: 2,
        };

        if (i != 0) {
          const arr =
            record.item.children && isArray(record.item.children)
              ? JSON.parse(JSON.stringify(record.item.children))
              : [];
          const maxY = arr.length > 0 ? Math.max(...arr.map((res) => res.y)) : 0;
          console.log('maxY', maxY);
          item.x = 0;
          item.y = maxY - 0 + 1;
          item.i = String(i);
        }
        console.log('item==>', item);
        record.item.children.push(item);
        console.log('record.item.children==>', record.item.children);
        console.log('layoutList==>', layoutRecord.value.layoutList);
      };

      const imgeLists = ref([]);
      const getLsdListRes = async () => {
        const res = await getLsdList();
        console.log('Ress', res);
        if (res && res.length > 0) {
          res.forEach((item) => {
            let obj = {};
            obj['value'] = item.id;
            obj['title'] = item.name;
            obj['disabled'] = true;
            obj['children'] = [];
            if (item.images && item.images.length > 0) {
              obj['children'] = item.images.map((el) => {
                return {
                  value: el.path,
                  title: el.name,
                };
              });
              imgeLists.value.push(obj);
            }
          });
          console.log('imgeLists.value', imgeLists.value);
        }
      };
      onMounted(() => {
        getLsdListRes();
      });
      return {
        registerDrawer,
        handleOk,
        closeDrawer,
        closeCurrentDrawer,
        handleSuccess,
        getTitle,
        handleItem,
        registerPortalManageDesignModal,
        handlePortalManageDesignModalCallBack,
        delDrag,
        handleAdd,
        column,
        addChildItem,
        widthTypeOptions,
        layoutRecord,
        labelCol: { span: 6 },
        wrapperCol: { span: 16 },
        imgeLists,
      };
    },
  });
</script>
