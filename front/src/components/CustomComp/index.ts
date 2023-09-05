import { defineAsyncComponent } from 'vue';
import { withInstall } from '/@/utils';
import selectUser from './src/selectUser/SelectUser.vue';
import selectAll from './src/selectAll/SelectAll.vue';
const selectUserInput = defineAsyncComponent(
  () => import('./src/selectUserInput/SelectUserInput.vue'),
);
import selectAllInput from './src/SelectAllInput/SelectAllInput.vue';

export const SelectUser = withInstall(selectUser);
export const SelectAll = withInstall(selectAll);
export const SelectUserInput = withInstall(selectUserInput);
export const SelectAllInput = withInstall(selectAllInput);
