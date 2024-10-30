package org.example.demo.view.ftp;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * https://commons.apache.org/proper/commons-net/examples/ftp/FTPClientExample.java
 */
public class FTPService {

    /**
     * 创建FTP连接
     *
     * @param hostname 主机
     * @param port     端口
     * @param user     用户名
     * @param password 密码
     * @return FTPClient
     */
    public static FTPClient connectAndLogin(String hostname, Integer port, String user, String password) {
        try {
            FTPClient ftpClient = new FTPClient();
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.connect(hostname, port);
            boolean done = ftpClient.login(user, password);
            if (!done) {
                return null;
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            return ftpClient;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 断开FTP连接
     *
     * @param ftpClient FTPClient
     */
    public static void logoutAndDisconnect(FTPClient ftpClient) {
        try {
            if (ftpClient == null) {
                return;
            }
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除远程文件
     *
     * @param ftpClient      FTPClient
     * @param remoteFilePath 远程文件路径，例如/home/root/vsftpd-3.0.5.tar.gz
     * @return boolean
     */
    public static boolean deleteFile(FTPClient ftpClient, String remoteFilePath) {
        try {
            if (ftpClient == null) {
                return false;
            }
            return ftpClient.deleteFile(remoteFilePath);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 创建远程目录
     *
     * @param ftpClient           FTPClient
     * @param remoteDirectoryPath 远程目录路径，例如/home/root
     * @return boolean
     */
    public static boolean makeDirectory(FTPClient ftpClient, String remoteDirectoryPath) {
        try {
            if (ftpClient == null) {
                return false;
            }
            String[] parts = remoteDirectoryPath.split("/");
            String path = "";
            for (String part : parts) {
                if (StrUtil.isBlank(part)) {
                    continue;
                }
                path += "/" + part;
                if (!ftpClient.changeWorkingDirectory(path)) {
                    boolean done = ftpClient.makeDirectory(path);
                    if (!done) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 上传本地文件
     *
     * @param ftpClient      FTPClient
     * @param localFilePath  本地文件路径，例如D:\vsftpd-3.0.5.tar.gz或者/home/root/vsftpd-3.0.5.tar.gz
     * @param remoteFilePath 远程文件路径，例如/home/root/vsftpd-3.0.5.tar.gz
     * @param action         更新action
     * @return boolean
     */
    public static boolean storeFile(FTPClient ftpClient, String localFilePath, String remoteFilePath, Consumer<Long> action) {
        try {
            if (ftpClient == null) {
                return false;
            }
            var inputStream = new ProgressInputStream(FileUtil.getInputStream(localFilePath), action);
            boolean done = ftpClient.storeFile(remoteFilePath, inputStream);
            inputStream.close();
            return done;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 下载远程文件
     *
     * @param ftpClient      FTPClient
     * @param localFilePath  本地文件路径，例如D:\vsftpd-3.0.5.tar.gz或者/home/root/vsftpd-3.0.5.tar.gz
     * @param remoteFilePath 远程文件路径，例如/home/root/vsftpd-3.0.5.tar.gz
     * @param action         更新action
     * @return boolean
     */
    public static boolean retrieveFile(FTPClient ftpClient, String localFilePath, String remoteFilePath, Consumer<Long> action) {
        try {
            if (ftpClient == null) {
                return false;
            }
            var outputStream = new ProgressOutputStream(FileUtil.getOutputStream(localFilePath), action);
            boolean done = ftpClient.retrieveFile(remoteFilePath, outputStream);
            outputStream.close();
            return done;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 读取远程目录
     *
     * @param ftpClient           FTPClient
     * @param remoteDirectoryPath 远程目录路径，例如/home/root
     * @return boolean
     */
    public static FTPFile[] listFiles(FTPClient ftpClient, String remoteDirectoryPath) {
        try {
            if (ftpClient == null) {
                return null;
            }
            FTPFile[] files = ftpClient.listFiles(remoteDirectoryPath);
            for (FTPFile file : files) {
                String path = remoteDirectoryPath.endsWith("/") ? remoteDirectoryPath + file.getName() : remoteDirectoryPath + "/" + file.getName();
                file.setTimestamp(ftpClient.mdtmCalendar(path));
            }
            return files;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 删除远程目录
     *
     * @param ftpClient           FTPClient
     * @param remoteDirectoryPath 远程目录路径，例如/home/root
     * @return boolean
     */
    public static boolean deleteDirectory(FTPClient ftpClient, String remoteDirectoryPath) {
        try {
            if (ftpClient == null) {
                return false;
            }
            FTPFile[] files = ftpClient.listFiles(remoteDirectoryPath);
            for (FTPFile file : files) {
                String pathName = remoteDirectoryPath + "/" + file.getName();

                if (file.isDirectory()) {
                    boolean done = deleteDirectory(ftpClient, pathName);
                    if (!done) {
                        return false;
                    }
                } else {
                    boolean done = ftpClient.deleteFile(pathName);
                    if (!done) {
                        return false;
                    }
                }
            }
            return ftpClient.removeDirectory(remoteDirectoryPath);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 递归遍历远程目录
     *
     * @param ftpClient           FTPClient
     * @param remoteDirectoryPath 远程目录路径，例如/home/root
     * @return List<Pair < FTPFile, String>>
     */
    public static List<Pair<FTPFile, String>> loopFiles(FTPClient ftpClient, String remoteDirectoryPath) {
        try {
            if (ftpClient == null) {
                return null;
            }
            List<Pair<FTPFile, String>> result = new ArrayList<>();
            for (FTPFile file : ftpClient.listFiles(remoteDirectoryPath)) {
                String pathName = remoteDirectoryPath + "/" + file.getName();

                if (file.isDirectory()) {
                    var temp = loopFiles(ftpClient, pathName);
                    if (temp != null) {
                        result.addAll(temp);
                    }
                } else {
                    result.add(Pair.of(file, pathName));
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
