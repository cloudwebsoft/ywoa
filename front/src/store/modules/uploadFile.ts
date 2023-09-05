import { defineStore } from 'pinia';

import { isArray } from '/@/utils/is';

interface UploadFile {
  uploadFileList: Recordable[];
  uploadFileTreeList: Recordable[];
  uploadFileParams: Recordable;
  isSelectFileAfter: boolean;
}

export const useUploadFileStore = defineStore({
  id: 'app-uploadFile',
  state: (): UploadFile => ({
    uploadFileList: [],
    uploadFileTreeList: [],
    uploadFileParams: {},
    isSelectFileAfter: false,
  }),
  getters: {
    getUploadFileList(): Recordable[] {
      return this.uploadFileList;
    },
    getUploadFileTreeList(): Recordable[] {
      return this.uploadFileTreeList;
    },
    getUploadFileParams(): Recordable {
      return this.uploadFileParams;
    },
    getIsSelectFileAfter(): Boolean {
      return this.isSelectFileAfter;
    },
  },
  actions: {
    setUploadFileTreeList(info: Recordable[]) {
      this.uploadFileTreeList = [...info];
      if (isArray(this.uploadFileTreeList)) {
        this.uploadFileList = [];
        this.uploadFileTreeList.forEach((item) => {
          if (isArray(item.children)) {
            item.children.forEach((el) => {
              this.uploadFileList.push(el);
            });
          }
        });
      } else {
        this.uploadFileList = [];
      }
    },
    resetUploadFileTreeList() {
      this.uploadFileTreeList = [];
      this.resetUploadFileList();
    },
    resetUploadFileList() {
      this.uploadFileList = [];
    },
    setUploadFileParams(params = {}) {
      this.uploadFileParams = { ...params };
    },
    setIsSelectFileAfter(bool = false) {
      this.isSelectFileAfter = bool;
      console.log('this.isSelectFileAfter', this.isSelectFileAfter);
    },
  },
});
