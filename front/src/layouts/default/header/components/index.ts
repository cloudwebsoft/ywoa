import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
import FullScreen from './FullScreen.vue';
import BigScreen from './BigScreen.vue';
import UploadFileView from './uploadFileView/index.vue';
import UploadFileSuspension from './uploadFileView/uploadFileSuspension.vue';

export const UserDropDown = createAsyncComponent(() => import('./user-dropdown/index.vue'), {
  loading: true,
});

export const LayoutBreadcrumb = createAsyncComponent(() => import('./Breadcrumb.vue'));

export const Notify = createAsyncComponent(() => import('./notify/index.vue'));

export const ErrorAction = createAsyncComponent(() => import('./ErrorAction.vue'));

export { FullScreen, BigScreen, UploadFileView, UploadFileSuspension };
