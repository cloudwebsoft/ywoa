import ObsClient from 'esdk-obs-browserjs/src/obs';

export const bucketName = 'obs';
export const server = 'http://180.1.1.1:9099'; // 你的endPoint  http://10.180.35.2  http://180.101.236.32:8088
// 创建ObsClient实例
export const obsClient = new ObsClient({
  access_key_id: '', // 你的ak
  secret_access_key: '', // 你的sk
  server,
});

//判断是否存在桶

export const isHasBucket = async () => {
  await obsClient.headBucket(
    {
      Bucket: bucketName,
    },
    function (err: any, result: any) {
      if (err) {
        console.error('Error-->' + err);
      } else {
        if (result.CommonMsg.Status < 300) {
          console.log('Bucket exists');
          return true;
        } else if (result.CommonMsg.Status === 404) {
          console.log('Bucket does not exist');
          return false;
        }
      }
    },
  );
};

//上传文件
export const putObject = async (
  file: any,
  key: string,
  progressCallback: (transferredAmount: number, totalAmount: number, totalSeconds: number) => void,
  callback: (result) => void,
) => {
  await obsClient.putObject(
    {
      Bucket: bucketName,
      SourceFile: file,
      Key: key,
      ProgressCallback: progressCallback,
    },
    function (err: any, result: any) {
      if (err) {
        console.error('Error-->' + err);
      } else {
        callback(result);
        if (result.CommonMsg.Status < 300) {
          // console.log('RequestId-->' + result.InterfaceResult.RequestId);
          // console.log('ETag-->' + result.InterfaceResult.ETag);
          // console.log('VersionId-->' + result.InterfaceResult.VersionId);
          // console.log('StorageClass-->' + result.InterfaceResult.StorageClass);
        } else {
          console.log('Code-->' + result.CommonMsg.Code);
          console.log('Message-->' + result.CommonMsg.Message);
          return { ETag: {} };
        }
      }
    },
  );
};

//获取桶内对象
export const getBucketList = (callback) => {
  obsClient.listObjects(
    {
      Bucket: bucketName,
    },
    (err, result: any) => {
      if (err) {
        console.error('Error-->' + err);
      } else {
        if (result.CommonMsg.Status < 300) {
          console.log('RequestId-->' + result.InterfaceResult.RequestId);
          callback(result.InterfaceResult.Contents);
        } else {
          console.log('Code-->' + result.CommonMsg.Code);
          console.log('Message-->' + result.CommonMsg.Message);
        }
      }
    },
  );
};

//下载文件
export const getObject = async (
  key: any,
  callback: (result) => {},
  progressCallback: (transferredAmount: number, totalAmount: number, totalSeconds: number) => void,
) => {
  await obsClient.getObject(
    {
      Bucket: bucketName,
      Key: key,
      //   Range: "bytes=0-10",
      SaveByType: 'file',
      ProgressCallback: progressCallback,
    },
    function (err: any, result: any) {
      console.log('getObject-result', result);
      if (err) {
        console.error('Error-->' + err);
      } else {
        if (result.CommonMsg.Status < 300) {
          callback(result.InterfaceResult);
          // console.log('RequestId-->' + result.InterfaceResult.RequestId);
          // console.log('ETag-->' + result.InterfaceResult.ETag);
          // console.log('VersionId-->' + result.InterfaceResult.VersionId);
          // console.log('ContentLength-->' + result.InterfaceResult.ContentLength);
          // console.log('DeleteMarker-->' + result.InterfaceResult.DeleteMarker);
          // console.log('LastModified-->' + result.InterfaceResult.LastModified);
          // console.log('StorageClass-->' + result.InterfaceResult.StorageClass);
          // console.log('Content-->' + result.InterfaceResult.Content);
          // console.log('Metadata-->' + JSON.stringify(result.InterfaceResult.Metadata));
        } else {
          console.log('Code-->' + result.CommonMsg.Code);
          console.log('Message-->' + result.CommonMsg.Message);
        }
      }
    },
  );
};

//删除文件
export const deleteObject = async (key: any, callback: (result) => {}) => {
  await obsClient.deleteObject(
    {
      Bucket: bucketName,
      Key: key,
    },
    function (err: any, result: any) {
      if (err) {
        console.error('Error-->' + err);
      } else {
        callback(result);
        if (result.CommonMsg.Status < 300) {
          console.log('RequestId-->' + result.InterfaceResult.RequestId);
          console.log('VersionId-->' + result.InterfaceResult.VersionId);
        } else {
          console.log('Code-->' + result.CommonMsg.Code);
          console.log('Message-->' + result.CommonMsg.Message);
        }
      }
    },
  );
};
