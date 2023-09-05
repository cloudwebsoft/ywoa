import { putObject, getObject } from './obsUtil';
import { useUploadFileStore } from '/@/store/modules/uploadFile';
import type { AppRouteRecordRaw } from '/@/router/types';
import { useFileDialog } from '/@/hooks/web/useFileModal';
import { buildUUID } from './uuid';
import { useUserStore } from '/@/store/modules/user';
import { string } from 'vue-types';
import { useMessage } from '/@/hooks/web/useMessage';

const uploadFileStore = useUploadFileStore();
const userStore = useUserStore();
const { createMessage } = useMessage();

export interface UploadFileParams {
  formName: string | undefined; //表单名
  fieldValue: string; //上传成功后文件名称
  fileName: string; //文件名
  progress: number; //进度
  file: any;
  key: string;
  title: string;
  fullPath: string | undefined; // 路由，匹配选项卡及高亮突出显示当前选项卡的名称
  filePath: string | undefined; // 上传至obs的路径
  fieldName: string | undefined;
  formCode: string | undefined;
  pageType: string | undefined;
  mainId: number;
  fieldTitle: string;
}

// 单文件 Or 多文件

export class uploadFile {
  public formName: string | undefined;
  public fieldValue: string;
  public fileName: string;
  public progress: number;
  public key: string;
  public title: string;
  public file: any;
  public level: number;
  public fullPath: string | undefined;
  public filePath: string | undefined;
  public fieldName: string | undefined;
  public formCode: string | undefined;
  public pageType: string | undefined;
  public mainId: number | undefined;
  public fieldTitle: string | undefined;

  constructor(opt: Partial<UploadFileParams> = {}) {
    const {
      formName,
      file,
      fullPath,
      filePath,
      fieldName,
      formCode,
      pageType,
      mainId,
      fieldTitle,
    } = opt;
    this.formName = formName;
    this.file = file;
    this.fieldValue = '';
    this.fileName = file?.name;
    this.title = file?.name;
    this.key = file?.name;
    this.level = 2;
    this.fullPath = fullPath;
    this.progress = 0;
    this.filePath = filePath;
    this.fieldName = fieldName;
    this.formCode = formCode;
    this.pageType = pageType;
    this.mainId = mainId;
    this.fieldTitle = fieldTitle;

    // this.sum();
    this.putObject();
  }
  public getOptions() {
    return {
      formName: this.formName,
      fieldValue: this.fieldValue,
      fileName: this.fileName,
      progress: this.progress,
      level: this.level,
      file: this.file,
      title: this.title,
      fieldName: this.fieldName,
      formCode: this.formCode,
      pageType: this.pageType,
      mainId: this.mainId,
      filePath: this.filePath,
      fieldTitle: this.fieldTitle,
    };
  }

  // public sum = () => {
  //   const set = setInterval(() => {
  //     if (this.progress == 100) {
  //       clearInterval(set);
  //     } else {
  //       this.progress += 2;
  //     }
  //     const data = this.getOptions();
  //     finishRefresh(data);
  //   }, 1000);
  // };
  public putObject = async () => {
    if (this.file) {
      this.fieldValue =
        `${this.filePath}/${buildUUID(20)}.` +
        this.file.name.substring(this.file.name.lastIndexOf('.') + 1);

      await putObject(this.file, this.fieldValue, this.setProgress, this.putObjectCallback);
    }
  };
  public putObjectCallback = (res) => {
    // 上传结束回调
    console.log('上传结束', res);
    const newWindow = (window as any) || undefined;
    const data = this.getOptions();
    console.log('putObjectCallback', data);
    if (newWindow && typeof newWindow?.putUploadFinish == 'function') {
      newWindow.putUploadFinish(data);
    }
  };

  private setProgress = (
    transferredAmount: number,
    totalAmount: number,
    totalSeconds: number,
  ): void => {
    console.log('callback', { transferredAmount, totalAmount, totalSeconds });
    // 获取上传平均速率（KB/S）
    console.log('平均速率（KB/S）', (transferredAmount * 1.0) / totalSeconds / 1024);
    // 获取上传进度百分比
    console.log('进度百分比', (transferredAmount * 100.0) / totalAmount);
    const prog = parseInt((transferredAmount * 100.0) / totalAmount);

    if (prog == this.progress) return;

    this.progress = prog;
    const data = this.getOptions();
    finishRefresh(data);
  };
}

export const finishRefresh = (data) => {
  console.log('每次结束调用', data);
  // 刷新uploadFileTreeList
  uploadFileStore.setUploadFileTreeList(uploadFileStore.getUploadFileTreeList);

  // 刷新文件宏控件的进度条
  const newWindow = (window as any) || undefined;
  if (newWindow && typeof newWindow?.putUploadProgress == 'function') {
    newWindow.putUploadProgress(data);
  } else {
    console.error('finishRefresh: function putUploadProgress is not found');
  }
};

export interface UploadFileObjectParams {
  files: File[] | undefined;
  formName: string;
  route: AppRouteRecordRaw;
  multiple?: boolean;
  fieldName: string;
  filePath: string;
  formCode: string;
  pageType: string;
  mainId: number;
  fieldTitle: string;
}

//upload file
export const uploadFileObject = (opt: Partial<UploadFileObjectParams> = {}) => {
  const { files, formName, route, fieldName, filePath, formCode, pageType, mainId, fieldTitle } =
    opt;
  console.log('上传文件传入参数', { files, formName, route, fieldName, filePath });

  // uploadFileStore
  if (!files) {
    return;
  }

  const serverInfo = userStore.getServerInfo;
  if (serverInfo.isUploadPanel) {
    // 显示悬浮进度栏
    uploadFileStore.setIsSelectFileAfter(true);
  }

  //multiple  false is one, true is more
  for (let i = 0; i < files.length; i++) {
    const file = files[i];
    const obj = new uploadFile({
      formName,
      file,
      fullPath: route?.fullPath,
      fieldName,
      filePath,
      formCode,
      pageType,
      mainId,
      fieldTitle,
    });
    const fileTreeList = uploadFileStore.getUploadFileTreeList;
    const findIndex = fileTreeList.findIndex(
      (item) => item.title === (route?.query?.titleName || route?.meta?.title),
    );
    console.log('findIndex', findIndex);
    if (findIndex != -1) {
      fileTreeList[findIndex].children.push(obj);
    } else {
      const newObj = {
        title: route?.query?.titleName || route?.meta?.title,
        key: route?.query?.titleName || route?.meta?.title,
        path: route?.path,
        level: 1,
        fullPath: route?.fullPath,
        children: [obj],
      };
      fileTreeList.push(newObj);
    }
    console.log('fileTreeList', fileTreeList);
    uploadFileStore.setUploadFileTreeList(fileTreeList);
  }
};

export const isUploadFinished = (tabName) => {
  const fileTreeList = uploadFileStore.getUploadFileTreeList;
  const findIndex = fileTreeList.findIndex((item) => item.title === tabName);
  console.log('findIndex', findIndex);
  if (findIndex != -1) {
    const hasNotFinished = fileTreeList[findIndex].children.some((item) => item.progress < 100);
    return !hasNotFinished;
  } else {
    console.error(tabName + ' is not exit in fileTreeList');
    return true;
  }
};

// 删除UploadFileTreeList中表单域fieldName对应的节点和进度条
export const deleteByField = (fieldName) => {
  let treeList = uploadFileStore.getUploadFileTreeList;
  console.log('deleteByField treeList', treeList, 'fieldName', fieldName);
  treeList.forEach((item) => {
    item.children = item.children.filter((item) => item.fieldName != fieldName);
  });
  treeList = treeList.filter((item) => item.children.length > 0);
  uploadFileStore.setUploadFileTreeList(treeList);
};

export const deleteByUid = (uid) => {
  let treeList = uploadFileStore.getUploadFileTreeList;
  console.log('deleteByUid treeList', treeList, 'uid', uid);
  treeList.forEach((item) => {
    item.children = item.children.filter((item) => item.file.uid && item.file.uid != uid);
  });
  treeList = treeList.filter((item) => item.children.length > 0);
  uploadFileStore.setUploadFileTreeList(treeList);
};

export interface SelectFilesParams {
  formName: string;
  fieldName: string;
  route: AppRouteRecordRaw;
  multiple: boolean;
  filePath: string;
  formCode: string;
  pageType: string;
  mainId: number;
  fieldTitle: string;
  accept: string;
  validExt: string[];
  maxFileSize: number;
}

//page import this method
export const selectFiles = (opt: Partial<SelectFilesParams> = {}, callback: (files) => {}) => {
  // uploadFileStore.setUploadFileParams(opt);
  const {
    formName,
    route,
    multiple = false,
    fieldName,
    filePath,
    formCode,
    pageType,
    mainId,
    fieldTitle,
    accept,
    validExt,
    maxFileSize = -1,
  } = opt;

  //init open files
  const { files, open, reset, onChange } = useFileDialog({ multiple: multiple, accept: accept });
  open();
  //上传文件处
  onChange((files: any) => {
    console.log('selectFiles files', files);
    let isExtValid = true;
    let isSizeValid = true;
    console.log('maxFileSize', maxFileSize * 1024);

    for (let i = 0; i < files.length; i++) {
      const file = files[i];
      const fileName = file.name;
      const ext = fileName.substring(fileName.lastIndexOf('.') + 1);
      if (validExt && validExt.length > 0) {
        isExtValid = validExt.some((item) => item == ext);
        if (!isExtValid) {
          createMessage.warn('文件: ' + file.name + ' 类型非法');
          return;
        }
      }
      if (maxFileSize != -1) {
        console.log('files.size', file.size);
        if (file.size > maxFileSize * 1024) {
          isSizeValid = false;
          if (!isSizeValid) {
            createMessage.warn('文件: ' + file.name + ' 大小超过了 ' + maxFileSize / 1024 + 'M');
            return;
          }
        }
      }
    }

    // 隐藏上传文件链接，防止网卡时反复点击可以上传
    if (typeof hideUploadLink == 'function') {
      hideUploadLink(formName, fieldName);
    }

    // const { formName, route } = uploadFileStore.getUploadFileParams;
    uploadFileObjectFunc({
      files,
      formName,
      route,
      fieldName,
      filePath,
      formCode,
      pageType,
      mainId,
      fieldTitle,
    });
    callback(files);
  });
};

export const uploadFileObjectFunc = (opt = {}) => {
  uploadFileObject({
    ...opt,
  });
};

export const getObjectByKey = (key: string) => {
  if (!key) return;
  getObject(
    key,
    (res): any => {
      console.log('下载结束回调', res);
      const { SignedUrl } = res?.Content;
      window.open(SignedUrl);
    },
    (res) => {
      console.log('下载进度', res);
    },
  );
};
