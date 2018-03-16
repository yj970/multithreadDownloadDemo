package com.example.yangjie.multithreaddownloaddemo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yangjie on 2018/3/15.
 */
public class FileDownloader {
    private final String TAG = "FileDownloader";
    private String fileName; // 文件名
    private int fileSize = 0; // 文件大小
    private String fileSavePath; // 手机存储路径
    private String downloadUrl; // 文件下载地址
    private int threadNum = 1; // 下载线程数量
    private final static int CONNECTION_TIME_OUT = 10 * 1000; // 连接超时时间

    private DownloadRunnable[] downloadRunnables; // runnable数组

    private int currentDownloadSize = 0; // 当前下载的大小
    private String cookie;

    public FileDownloader() {
    }

    // 准备数据
    public void prepare(String downloadUrl, String fileSavePath, int threadNum, String cookie, String fileName) {
        currentDownloadSize = 0;
        // 对下载路径、存储路径、下载线程数量赋值
        this.downloadUrl = downloadUrl;
        this.fileSavePath = fileSavePath;
        this.fileName = fileName;
        this.cookie = cookie;
        if (threadNum > 0) {
            this.threadNum = threadNum;
        }
        // 获取文件信息
        requestFileInfo(downloadUrl);
        // 执行完上述步骤，fileName，fileSize，fileSavePath，downloadUr，threadNum都已经有了

        // 根据 文件大小、下载线程数量确定 "下载块"block的大小
        int blockSize = fileSize % this.threadNum == 0 ?
                fileSize / this.threadNum : fileSize / this.threadNum + 1;
        // 根据下载线程数量，初始化DownloadRunnable数组
        downloadRunnables = new DownloadRunnable[threadNum];
        // 根据数组大小, 循环初始化DownloadRunnable数组内的对象
        for (int i = 0; i < downloadRunnables.length; i++) {
            downloadRunnables[i] = new DownloadRunnable(this, i, blockSize, cookie);
        }

        // 上述步骤完成后，多线程下载所需要的数据已准备完成
    }

    // 获取 文件大小(fileSize)和 文件名字(fileName)
    private void requestFileInfo(String downloadUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection)
                    new URL(downloadUrl).openConnection();
            connection.setConnectTimeout(CONNECTION_TIME_OUT);
            connection.setRequestMethod("GET");

            //set accept file meta-data type
            connection.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg," +
                    " image/pjpeg, application/x-shockwave-flash, application/xaml+xml, " +
                    "application/vnd.ms-xpsdocument, application/x-ms-xbap, " +
                    "application/x-ms-application, application/vnd.ms-excel, " +
                    "application/vnd.ms-powerpoint, application/msword, */*");

            connection.setRequestProperty("Accept-Language", "zh-CN");
            connection.setRequestProperty("Referer", downloadUrl);
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; " +
                    "Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; " +
                    ".NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            connection.setRequestProperty("Cookie", cookie);

//            connection.setRequestProperty("Connection", "Keep-Alive");

            connection.connect();

            if (connection.getResponseCode() == 200) {
                fileSize = connection.getContentLength();
                if (fileSize <= 0) {
                    throw new RuntimeException(TAG + " Unknown file size");
                }

//                fileName = getFilename(connection); 构造参数已获取
            } else {
                throw new RuntimeException(TAG + " Server Response Code is "
                        + connection.getResponseCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 获取 文件名(fileName)
    private String getFilename(HttpURLConnection connection) {
        String filename = downloadUrl != null ?
                downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1) : null;
        if (filename == null || "".equals(filename.trim())) {//如果获取不到文件名称
            for (int i = 0; ; i++) {
                String mine = connection.getHeaderField(i);
                if (mine == null) break;
                if ("content-disposition".equals(connection.getHeaderFieldKey(i).toLowerCase())) {
                    Matcher m = Pattern.compile(".*filename=(.*)").
                            matcher(mine.toLowerCase());
                    if (m.find()) return m.group(1);
                }
            }
            filename = UUID.randomUUID() + ".tmp";//默认取一个文件名
        }
        return filename;
    }


    public int getFileSize() {
        return fileSize;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public static int getConnectionTimeOut() {
        return CONNECTION_TIME_OUT;
    }

    public String getFileSavePath() {
        return fileSavePath;
    }

    public String getFileName() {
        return fileName;
    }

    public DownloadRunnable[] getDownloadRunnables() {
        return downloadRunnables;
    }

    public void start(FileDownloadListener fileDownloadListener) {
        boolean isFinish = false;
        while (!isFinish) {
            isFinish = checkFinish();
            int progress = (int) (currentDownloadSize / (float)fileSize * 100);
            fileDownloadListener.onProgress(progress);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        fileDownloadListener.onSuccess();
    }

    public synchronized void appendDownloadSize(int size) {
        currentDownloadSize += size;
    }

    /**
     * 是否下载完成
     * @return
     */
    private boolean checkFinish() {
        if (downloadRunnables != null && downloadRunnables.length > 0) {
            for (DownloadRunnable runnable : downloadRunnables) {
                if (!runnable.isFinish()) {
                    return  false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 是否下载失败
     */
    public boolean isDownloadFailure(){
        if (downloadRunnables != null && downloadRunnables.length > 0) {
            for (DownloadRunnable runnable : downloadRunnables) {
                if (runnable.getDownloadSize() == -1) {
                    return true;
                }
            }
        }
        return false;
    }

    public interface FileDownloadListener {
        void onSuccess();
        void onFailure(String errorMessage);
        void onProgress(int progress);
    }
}
