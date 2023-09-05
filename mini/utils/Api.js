// let baseUrl = 'https://dev.xiaocaicloud.com:9443/oa';
// let baseUrl = 'http://192.168.0.100:8085/oa'
let baseUrl = 'http://localhost:8085/oa';
// let baseUrl = 'http://demo.yimihome.com:8085/oa';
// let baseUrl = 'http://192.168.3.84:8085/oa'

let appBaseUrl = uni.getStorageSync("appBaseUrl");
if (appBaseUrl) {
	baseUrl = appBaseUrl;
}

export const Api = {
	baseUrl: baseUrl,
	// urlDownload: baseUrl + "/",
	// urlViewPDF: baseUrl + "/",
	// urlViewImg: baseUrl + "/",
	// urlViewVideo: baseUrl + "/",
}

export function setApiBaseUrl(url) {
	Api.baseUrl = url;
}
