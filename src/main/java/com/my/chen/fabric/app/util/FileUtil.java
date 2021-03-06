package com.my.chen.fabric.app.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
public class FileUtil {

    private static final int BUFFER_SIZE = 2 * 1024;

    /**
     * 解压指定zip文件
     *
     * @param unZipFile 压缩文件的路径
     * @param destFile  解压到的目录
     */
    private static void unZip(String unZipFile, String destFile, boolean policy) {
        FileOutputStream fileOut;
        File file;
        InputStream inputStream;

        try {
            // 生成一个zip的文件
            ZipFile zipFile = new ZipFile(unZipFile);
            Enumeration enumeration = zipFile.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) enumeration.nextElement();
                if(entry.getName().contains(".yaml") && policy){
                    file = new File(destFile + File.separator + "policy.yaml");
                }else {
                    file = new File(destFile + File.separator + entry.getName());
                }
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    // 如果指定文件的目录不存在,则创建之.
                    File parent = file.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    // 获取出该压缩实体的输入流
                    inputStream = zipFile.getInputStream(entry);

                    fileOut = new FileOutputStream(file);
                    int length = 0;
                    // 将实体写到本地文件中去
                    byte[] buffer = new byte[BUFFER_SIZE];
                    while ((length = inputStream.read(buffer)) > 0) {
                        fileOut.write(buffer, 0, length);
                    }
                    fileOut.close();
                    inputStream.close();
                }
            }
            zipFile.close();
        } catch (IOException e) {
            log.error("Error", e);
        }
    }

    public static void unZipAndSave(MultipartFile file, String parentPath, String childrenPath) throws IOException {
        String fileName = file.getOriginalFilename();
        File dest = new File(parentPath + File.separator + fileName);
        File childrenFile = new File(childrenPath);
        if (childrenFile.exists()) {
            deleteFiles(childrenPath);
        }
        childrenFile.mkdirs();

        //缓存临时文件
        file.transferTo(dest);
        unZip(String.format("%s" + File.separator + "%s", parentPath, fileName), parentPath, false);
        dest.delete();
    }

    public static void chaincodeUnzipAndSave(MultipartFile file, String parentPath, String childrenPath) throws IOException {
        String fileName = file.getOriginalFilename();
        File dest = new File(parentPath + File.separator + fileName);
        File childrenFile = new File(childrenPath);
        if (childrenFile.exists()) {
            deleteFiles(childrenPath);
        }
        childrenFile.mkdirs();

        //缓存临时文件
        file.transferTo(dest);
        String str1 = dest.getAbsolutePath();
        String destFileFolder = str1.substring(0, str1.lastIndexOf("."));

        checkPolicyYaml(String.format("%s" + File.separator + "%s", parentPath, fileName));

        unZip(String.format("%s" + File.separator + "%s", parentPath, fileName), destFileFolder, true);
        dest.delete();
    }


    private static void checkPolicyYaml(String unZipFile) throws IOException {
        // check hash policy.yaml
        ZipFile zipFile = new ZipFile(unZipFile);
        Enumeration enumeration = zipFile.entries();
        while (enumeration.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) enumeration.nextElement();
            if (entry.getName().contains(".yaml")) {
                return;
            }
        }
        throw new RuntimeException("chain code file must has policy yaml");
    }



    /**
     * 通过递归得到某一路径下所有的目录及其文件并删除所有文件
     *
     * @param filePath 文件夹路径
     */
    public static void deleteFiles(String filePath) {
        File root = new File(filePath);
        if (!root.exists()) {
            return;
        }
        File[] files = root.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                deleteFiles(file.getAbsolutePath());// 递归调用
                file.delete();
                log.debug(String.format("显示%s下所有子目录及其文件", file.getAbsolutePath()));
            } else {
                file.delete();
                log.debug(String.format("显示%s下所有子目录%s====文件名：%s", filePath, file.getAbsolutePath(), file.getName()));
            }
        }
    }

    public static void copyDirectory(String oldPath, String newPath) throws IOException {
        if(!newPath.trim().equals(oldPath.trim())){
            File newPathFile = new File(newPath);
            File oldPathFile = new File(oldPath);

            if(oldPathFile.exists()){
                if(newPathFile.exists()){
                    deleteFiles(newPath);
                }
                newPathFile.mkdirs();
                FileUtils.copyDirectory(oldPathFile, newPathFile);
                deleteFiles(oldPath);
                oldPathFile.delete();
            }
        }
    }



    public static void main(String[] args) throws IOException {
        File file1 =  new File("E:\\home\\web-fabric\\tt\\Org1");
        File file2 = new File("E:\\home\\web-fabric\\tt\\Org2");

        String[] list = null;
        if(file1.exists()){
            list = file1.list();
        }

        if(!file2.exists()){
            file2.mkdirs();
        }

        if(!file1.getName().equals(file2.getName())){
            FileUtils.copyDirectory(file1, file2);
        }

    }

}
