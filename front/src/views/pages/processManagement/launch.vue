<template>
  <div class="bg-white">
    <div class="p-5">
      <Select
        v-model:value="selectCode"
        show-search
        placeholder="请选择流程类型"
        style="width: 200px"
        :options="allChildren"
        :field-names="{ label: 'name', value: 'code' }"
        :filter-option="filterOption"
        allow-clear
      />
      <Button type="primary" @click="fastLaunch" class="ml-3">快速发起</Button>
    </div>
    <Card v-for="(item, index) in dataSource" :key="index" :bordered="false">
      <template #title> <Icon icon="clarity:tags-solid" />{{ item['name'] }} </template>
      <div class="flex flex-wrap">
        <div v-for="(el, chil) in item['children'] || []" :key="chil" class="w-1/8">
          <Spin :spinning="el['loading']">
            <div
              class="p-5 flex justify-center flex-col cursor-pointer outline-white"
              @click="flowInitClick(el)"
            >
              <div class="flex justify-center">
                <SvgIcon size="30" :name="el['icon']" v-if="el['icon'] && el['icon'].length > 0" />
              </div>
              <div class="mt-2 flex justify-center">{{ el['name'] }}</div>
            </div>
          </Spin>
        </div>
      </div>
    </Card>
    <ProcessDrawer @register="registerDrawer" @success="handleSuccess" @show-view="handleView" />
    <ProcessShowDrawer @register="registerViewDrawer" @success="handleSuccess" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, onMounted, ref, unref } from 'vue';
  import { Card, Spin, Select, Button } from 'ant-design-vue';
  import { getDirTree, getFlowInit } from '/@/api/process/process';
  import ProcessDrawer from './processDrawer.vue';
  import { useDrawer } from '/@/components/Drawer';
  import { SvgIcon, Icon } from '/@/components/Icon';
  import { useMessage } from '/@/hooks/web/useMessage';
  import ProcessShowDrawer from './processShowDrawer.vue';
  import { useRouter } from 'vue-router';
  import { useGo } from '/@/hooks/web/usePage';

  export default defineComponent({
    components: { Card, Spin, ProcessDrawer, SvgIcon, Icon, Select, Button, ProcessShowDrawer },
    setup() {
      const dataSource = ref([]);
      const allChildren = ref<any>([]);
      const selectCode = ref('');
      const { createMessage } = useMessage();

      onMounted(() => {
        init();
      });
      const init = () => {
        selectCode.value = '';
        fetch();
      };
      function fetch() {
        allChildren.value = [];
        getDirTree().then((res) => {
          let data = res;
          if (data && data.length > 0 && data[0].children && data[0].children.length > 0) {
            dataSource.value = data[0].children;
            dataSource.value.forEach((item: any) => {
              if (item['children'] && item['children'].length > 0)
                item['children'] = item['children'].map((el) => {
                  allChildren.value.push(el);
                  return { ...el, loading: false };
                });
            });
          }
        });
      }

      //初始化流程myActionId
      const flowInitClick = async (currentRecord: object) => {
        currentRecord['loading'] = true;
        await beginLaunch(currentRecord['code']);
        currentRecord['loading'] = false;
      };

      const { currentRoute } = useRouter();

      const go = useGo();
      //发起流程
      const beginLaunch = (typeCode: string) => {
        return new Promise((resolve) => {
          let params = { typeCode: typeCode };
          console.log('beginLaunch currentRoute.value.query', currentRoute.value.query);
          // 如果不是从菜单进入
          if (JSON.stringify(currentRoute.value.query) !== '{}') {
            let query = currentRoute.value.query;
            params = { ...params, ...query };
          }
          console.log('beginLaunch params', params);
          getFlowInit(params)
            .then((res) => {
              let myActionId = res.myActionId || '';
              let type = res.type || 2; // 1为自由流程 2为固定流程
              if (myActionId) {
                // openDrawer(true, {
                //   myActionId: myActionId,
                //   type: type,
                // });

                if (type === 2) {
                  go({
                    path: '/processHandle',
                    query: {
                      myActionId: myActionId,
                      type: type,
                      isTab: true,
                    },
                  });
                } else {
                  go({
                    path: '/processHandleFree',
                    query: {
                      myActionId: myActionId,
                      type: type,
                      isTab: true,
                    },
                  });
                }
              }
            })
            .finally(() => {
              resolve(true);
            });
        });
      };

      const filterOption = (input: string, option: any) => {
        return option.name.toLowerCase().indexOf(input.toLowerCase()) >= 0;
      };

      //快速发起按钮
      const fastLaunch = () => {
        if (!unref(selectCode)) {
          createMessage.warning('请选择流程类型');
          return;
        }
        beginLaunch(unref(selectCode));
      };

      const [registerDrawer, { openDrawer }] = useDrawer();
      function handleSuccess() {}

      const [registerViewDrawer, { openDrawer: openViewDrawer }] = useDrawer();
      //查看详情
      function handleView(record: object) {
        // 延时打开详情页面，否则当处理流程结束，如果允许撤回，将跳转至详情页时，而此时SQL控件中setInterval还在运行，会致使其ajax被调用，呈现为可编辑状态
        setTimeout(() => {
          openViewDrawer(true, {
            flowId: record['f.id'],
          });
        }, 500);
      }
      return {
        dataSource,
        flowInitClick,
        registerDrawer,
        handleSuccess,
        allChildren,
        filterOption,
        selectCode,
        fastLaunch,
        registerViewDrawer,
        handleView,
      };
    },
  });
</script>
<style lang="less" scoped>
  ::v-deep .ant-card-body {
    padding: 0;
  }
  // .ant-card{
  //   margin-bottom: ;
  // }
</style>
