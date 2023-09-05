<template>
  <div>
    <ul>
      <li v-for="(item, index) in dataSource" :key="index">
        <a @click="handleGetObject(item.Key)">
          Key--{{ item.Key }} Etag--{{ item.Etag }} Size--{{ item.Size }}
        </a>
        <a-button @click="handleDelete(item.Key)">删除</a-button></li
      >
    </ul>
    <a-button type="primary" @click="handleSearch">查询</a-button>
    <!-- <a-button @click="open">上传文件</a-button> -->
    <!-- <div>
      <BasicTree title="树" :clickRowToExpand="false" :treeData="dData" treeKey="value">
        <template #title="{ title, key }">
          <Dropdown :trigger="['contextmenu']">
            <span>{{ title }}</span>
            <template #overlay>
              <Menu @click="({ key: menuKey }) => onContextMenuClick(treeKey, menuKey)">
                <MenuItem key="1">1st menu item</MenuItem>
                <MenuItem key="2">2nd menu item</MenuItem>
                <MenuItem key="3">3rd menu item</MenuItem>
              </Menu>
            </template>
          </Dropdown>
        </template>
      </BasicTree>
      <div> </div>
    </div> -->
    <!-- 
    <input ref="input" type="file" @change="handleFiles" /> -->
  </div>
</template>
<script lang="ts" setup>
  import { onMounted, ref } from 'vue';
  import { isHasBucket, getBucketList, getObject, deleteObject } from '/@/utils/obsUtil.ts';
  import { BasicTree } from '/@/components/Tree';
  import { Dropdown, Menu } from 'ant-design-vue';
  const MenuItem = Menu.Item;
  import { useFileDialog } from '/@/hooks/web/useFileModal';
  const { files, open, reset, onChange } = useFileDialog();

  onChange((files) => {
    console.log('files', files);
    /** do something with files */
  });

  const dData = ref([
    {
      title: '新增1',
      children: [
        {
          title: 'key1',
          value: '11111',
        },
        {
          title: 'key2',
          value: '22222',
        },
        {
          title: 'key3',
          value: '33333',
        },
      ],
    },
    {
      title: '新增2',
      children: [
        {
          title: 'key1',
          value: '11111',
        },
        {
          title: 'key2',
          value: '22222',
        },
        {
          title: 'key3',
          value: '33333',
        },
      ],
    },
  ]);
  const onContextMenuClick = (treeKey: string, menuKey: string | number) => {
    console.log(`treeKey: ${treeKey}, menuKey: ${menuKey}`);
  };
  const dataSource = ref([]);
  onMounted(() => {
    // isHasBucket();
    handleSearch();
  });
  const handleSearch = () => {
    getBucketList((res) => {
      dataSource.value = res;
      console.log('dataSource', dataSource.value);
    });
  };
  const handleDelete = (key) => {
    console.log('key', key);
    deleteObject(
      key,
      (res) => {
        handleSearch();
      },
      (transferredAmount: number, totalAmount: number, totalSeconds: number) => {
        // 获取上传平均速率（KB/S）
        console.log('平均速率（KB/S）', (transferredAmount * 1.0) / totalSeconds / 1024);
        // 获取上传进度百分比
        console.log('进度百分比', (transferredAmount * 100.0) / totalAmount);
      },
    );
  };

  const handleGetObject = (key) => {
    getObject(key, (record) => {
      console.log('下载', record);
      window.open(record?.Content?.SignedUrl);
    });
  };

  function handleFiles(event: any) {
    var f = event.target.files[0];
    console.log('f', f);
    //   obsClient.
    //   obsClient.putObject({
    //     Bucket: 'xxx', // 桶名
    //     Key: this.path + 'test01.jpg', // 路径 + 文件名
    //     SourceFile:f
    //   }, function (err, result) {
    //     if (err) {
    //       console.error('Error-->' + err)
    //     } else {
    //       console.log('Status-->' + result.CommonMsg.Status)
    //     }
    //   })
    //   };
  }
</script>
