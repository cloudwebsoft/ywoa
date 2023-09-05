import { isDevMode } from '/@/utils/env';

// System default cache time, in seconds 5天
// 会话存在临时缓存中，关闭浏览器就会消失
export const DEFAULT_CACHE_TIME = 60 * 60 * 24 * 5;

// aes encryption key
export const cacheCipher = {
  key: '_11111000001111@',
  iv: '@11111000001111_',
};

// Whether the system cache is encrypted using aes
export const enableStorageEncryption = !isDevMode();
