package com.example.yangjie.multithreaddownloaddemo;

import android.content.Context;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by yangjie on 2018/3/15.
 */
public class DownloadManager {
    // 单例
    public static DownloadManager downloadManager;
    // ？？？什么鬼（最大线程数？）
    private static int PARALLEL_DOWNLOAD_SIZE = 6;
    private ArrayList<FileDownloader> fileDownloaders;
    private Context context;
    // 线程池
    private Executor downloadExecutor;

    public static DownloadManager getInstance(Context context) {
        if (downloadManager == null) {
            downloadManager = new DownloadManager(context);
        }
        return downloadManager;
    }

    public DownloadManager(Context context) {
        this.context = context;
        // 初始化线程池
        downloadExecutor = Executors.newFixedThreadPool(PARALLEL_DOWNLOAD_SIZE);
        fileDownloaders = new ArrayList<>();
    }

    /**
     * @param downloadUrl 下载地址
     * @param savePath 存储路径
     * @param threadNum 下载线程数量
     * @param fileName
     * @param fileDownloadListener 下载监听
     */
    public void download(String downloadUrl, String savePath, int threadNum, String cookie, String fileName, FileDownloader.FileDownloadListener fileDownloadListener) {
       // 这里还可以优化 todo


        // 新建一个下载器
        FileDownloader fileDownloader = new FileDownloader();
        fileDownloaders.add(fileDownloader);
        startDownload(fileDownloader, downloadUrl, savePath, threadNum, cookie, fileName, fileDownloadListener);
    }

    // synchronized 放这个干嘛？？？
    private synchronized void startDownload(final FileDownloader fileDownloader, final String downloadUrl, final String savePath, final int threadNum, final String cookie, final String fileName, final FileDownloader.FileDownloadListener fileDownloadListener) {
        downloadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                // 准备数据
                fileDownloader.prepare(downloadUrl, savePath, threadNum, cookie, fileName);
                // 开始执行下载
                if (fileDownloader.getDownloadRunnables() != null) {
                    for (DownloadRunnable downloadRunnable : fileDownloader.getDownloadRunnables()) {
                        downloadExecutor.execute(downloadRunnable);
                    }
                }
                // 设置监听
                fileDownloader.start(fileDownloadListener);
            }
        });
    }
}
