package com.ruoxu.update;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.orhanobut.logger.Logger;
import com.ruoxu.update.util.DownloadTask;

import java.io.File;

public class UpdateService extends Service {


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MSG_DOWNLOAD_DONE:
                    UpdateAgent.getInstance().updateFinish(getApplicationContext());
                    stopSelf();
                    break;
                case Constants.MSG_DOWNLOAD_CANCEL:
                    UpdateAgent.getInstance().updateCancel();
                    stopSelf();
                    break;
                case Constants.MSG_UPDATE_PROGRESS:
                    UpdateAgent.getInstance().updateNotificationPorgress((Integer) msg.obj);
                    break;
            }
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i("Service onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String apkName = intent.getStringExtra("apkName");
        String url = intent.getStringExtra("downloadUrl");
        File apkFile = new File(Constants.save_path,apkName);
        Logger.i("start download");

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            DownloadTask downloadTask = new DownloadTask(url,apkFile);
            downloadTask.download(new DownloadTask.Callback() {
                @Override
                public <T> void event(int what, T msg) {
                    switch (what) {
                        case DownloadTask.Callback.MSG_START:

                            break;
                        case DownloadTask.Callback.MSG_UPDATE:
                            int progress = (Integer)msg;

                            handler.removeMessages(Constants.MSG_UPDATE_PROGRESS);
                            Message message = handler.obtainMessage(Constants.MSG_UPDATE_PROGRESS, progress);
                            message.sendToTarget();

                            break;
                        case DownloadTask.Callback.MSG_END:
                            // 更新结束,更新通知栏并且结束更新服务
                            handler.sendEmptyMessage(Constants.MSG_DOWNLOAD_DONE);
                            break;
                        case DownloadTask.Callback.MSG_ERROR:
                            handler.sendEmptyMessage(Constants.MSG_DOWNLOAD_CANCEL);
                            break;

                    }
                }
            });


        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



}