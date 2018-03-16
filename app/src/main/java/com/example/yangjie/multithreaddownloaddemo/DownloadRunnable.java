package com.example.yangjie.multithreaddownloaddemo;

import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by yangjie on 2018/3/15.
 */
public class DownloadRunnable implements Runnable{
    private final String TAG = "DownloadRunnable";
    private FileDownloader fileDownloader;
    private int threadId;
    private int blockSize; // 下载块 大小
    private String cookie;

    private int downloadSize = 0; //已下载的大小

    private int startPos = -1;
    private int endPos = -1;


    private boolean isFinish;
    private boolean isStart;

    public DownloadRunnable(FileDownloader fileDownloader, int threadId, int blockSize, String cookie) {
        this.fileDownloader = fileDownloader;
        this.threadId = threadId;
        this.blockSize = blockSize;
        int fileSize = fileDownloader.getFileSize(); // 文件大小

        this.cookie = cookie;

        startPos = blockSize * threadId;
        endPos = blockSize * (threadId+1) < fileSize ? blockSize * (threadId+1) : fileSize;
    }


    @Override
    public void run() {
        if (startPos >= endPos) {
            isFinish = true;
        } else {
            try {
                isStart = true;
                isFinish = false;

                // 下载
                HttpURLConnection conn = (HttpURLConnection)
                        new URL(fileDownloader.getDownloadUrl()).openConnection();
                conn.setConnectTimeout(FileDownloader.getConnectionTimeOut());
                conn.setRequestMethod("GET");

                //set accept file meta-data type
                conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg," +
                        " image/pjpeg, application/x-shockwave-flash, application/xaml+xml, " +
                        "application/vnd.ms-xpsdocument, application/x-ms-xbap, " +
                        "application/x-ms-application, application/vnd.ms-excel, " +
                        "application/vnd.ms-powerpoint, application/msword, */*");

                conn.setRequestProperty("Accept-Language", "zh-CN");
                conn.setRequestProperty("Referer", fileDownloader.getDownloadUrl());
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; " +
                        "Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; " +
                        ".NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Cookie", cookie);


                conn.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
                conn.connect();

                // 利用RandomAccessFile进行区间读写
                RandomAccessFile randomAccessFile = new RandomAccessFile(fileDownloader.getFileSavePath() +
                                                            File.separator + fileDownloader.getFileName(), "rwd");
                // 从startPos位置开始写入
                randomAccessFile.seek(startPos);

                InputStream is = conn.getInputStream();
                byte[] buffer = new byte[10240];
                int offset;
                while ((offset = is.read(buffer, 0, 10240)) != -1) {
                    randomAccessFile.write(buffer, 0, offset);
                    downloadSize += offset;
                    fileDownloader.appendDownloadSize(offset);
                }
                randomAccessFile.close();
                is.close();

                isFinish = true;
                isStart = false;

            } catch (IOException e) {
                downloadSize = -1;
                Log.e(TAG, "下载出错："+e.getMessage());
            }
        }
    }

    public boolean isFinish() {
        return isFinish;
    }

    public boolean isStart() {
        return isStart;
    }

    public int getDownloadSize() {
        return downloadSize;
    }

    public int getThreadId() {
        return threadId;
    }
}
