<template>
  <PageWrapper dense contentFullHeight fixedHeight contentClass="flex">
    <div class="w-full h-full bg-gay-300 flex">
      <SmartTreeView
        :code="code"
        @select="handleSelect"
        :defaultNodeCode="treeNodeCode"
        @set-module-code="setModuleCode"
        class="w-1/4 xl:w-1/5"
      />
      <div class="w-3/4 xl:w-4/5 h-full">
        <div v-show="!isShowEmpty" class="h-full mt-1">
          <SmartModule
            :start="5"
            v-model:moduleCode="moduleCode"
            v-model:treeNodeCode="treeNodeCode"
            :isTreeView="true"
          />
        </div>
        <div class="flex flex-col justify-center bg-white m-4 h-48/50" v-show="isShowEmpty">
          <Empty>
            <template #description>
              <span> 请选择类型 </span>
            </template>
          </Empty>
        </div>
      </div>
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, onMounted, ref, unref } from 'vue';
  import { useRouter } from 'vue-router';
  import {} from '@ant-design/icons-vue';
  import { Empty } from 'ant-design-vue';

  import { PageWrapper } from '/@/components/Page';
  import SmartTreeView from './modules/smartTreeView.vue';
  import SmartModule from './smartModule.vue';
  import { getIsManagerOfNode } from '/@/api/module/module';

  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'smartModuleTreeView',
    components: {
      PageWrapper,
      SmartTreeView,
      SmartModule,
      Empty,
    },
    setup() {
      // 基础数据编码
      const code = ref('');
      // 模块编码
      const moduleCode = ref('');
      const { currentRoute } = useRouter();
      const query = ref<any>({});
      const treeNodeCode = ref('');
      const isShowEmpty = ref(true);

      onMounted(() => {
        query.value = currentRoute.value.query;
        if (query.value.treeBasicCode) {
          // 来自于模块树形视图
          code.value = query.value.treeBasicCode;
          setModuleCode('');
        } else {
          // 来自于菜单
          const paths = currentRoute.value.path.split('/');
          code.value = paths[paths.length - 1];
        }
      });

      const setModuleCode = (curCode: string) => {
        if (curCode) {
          moduleCode.value = curCode;
        }

        if (query.value.treeBasicCode) {
          // 来自于模块树形视图
          moduleCode.value = query.value.moduleCode;
          treeNodeCode.value = query.value.treeNodeCode;
        } else {
          // 来自于菜单，使模块中展现根目录的内容
          treeNodeCode.value = unref(code);
        }

        // 判断是否为根节点，获取是否有管理根节点的权限，如果没有则显示Empty
        if (treeNodeCode.value == unref(code)) {
          getIsManagerOfNode({ code: unref(code) }).then((res) => {
            isShowEmpty.value = !res.canManage;
          });
        } else {
          isShowEmpty.value = false;
        }
      };

      const handleSelect = (key: string, trc: any) => {
        const { dataRef } = trc.node;
        treeNodeCode.value = key;
        if (dataRef.linkTo.type === 'module') {
          moduleCode.value = dataRef.linkTo.moduleCode;
        } else {
          moduleCode.value = '';
        }
        if (treeNodeCode.value != code.value) {
          isShowEmpty.value = false;
        }

        // getModuleTreeNodePriv({ nodeCode: treeNodeCode.value }).then((res) => {
        //   console.log('getModuleTreeNodePriv res', res);
        //   nodePriv.value = res;
        // });
      };
      return {
        handleSelect,
        moduleCode,
        code,
        treeNodeCode,
        setModuleCode,
        isShowEmpty,
      };
    },
  });
</script>
<style scoped></style>
