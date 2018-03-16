package com.example.yangjie.multithreaddownloaddemo;

import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        DownloadManager downloadManager = DownloadManager.getInstance(this);
        /**downloadUrl : 下载地址
         * savePath : 存储路径（文件夹）
         * threadNum:下载线程数量
         * cookie : 信息
         * fileName : 文件名（用于保存）
         */
        downloadManager.download(downloadUrl, savePath, threadNum, cookie, fileName, new FileDownloader.FileDownloadListener(){
            @Override
            public void onSuccess() {
                Log.d("MyTAG", "成功");
                // 文件的存储地址是 FILE_PATH, savePath+ File.separator + fileName
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d("MyTAG", "失败");
            }

            @Override
            public void onProgress(int progress) {
                Log.d("MyTAG", progress+"");
            }
        });


        // 说明： 2018年03月16日11:28:52
        // 本demo经测试可以使用，不过存在缺陷，还有很多地方需要优化
        // todo ： 线程池管理？断点续传（数据库）？

        // 在项目上测试，多线程下载速度还不如单次下载的速度快
        // 要使用多线程下载，服务端也需要做一定的修改，需要返回contentLength和range，即是返回总长度和分段的字节（block）
        // demo学习于https://github.com/onlynight/MultiThreadDownloader

    }
}
