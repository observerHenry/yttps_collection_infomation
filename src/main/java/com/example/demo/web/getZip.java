package com.example.demo.web;

import com.example.demo.model.util.Result;
import com.example.demo.model.util.ResultGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * @program:yttps_collection_infomation
 * @description:将文件压缩
 * @author: Mr.Henry
 * @create:2019/7/1 9:56
 */
@RestController
@RequestMapping("zip")
public class getZip {

    private static final int BUFFER_SIZE = 2 * 1024;

    @Value("${excel_path}")
    private String path;

    @Value("${zip_create_path}")
    private String createPathZip;

    /**
     * @param sourceFile       源文件
     * @param zos              zip输出流
     * @param name             压缩后名称
     * @param KeepDirStructure 是否保留原来的目录结构,true:保留目录结构;
     *                         false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws Exception
     */
    private static void compress(File sourceFile, ZipOutputStream zos, String name, boolean KeepDirStructure) throws Exception {
        byte[] buf = new byte[BUFFER_SIZE];
        if (sourceFile.isFile()) {
            zos.putNextEntry(new ZipEntry(name));
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf)) != -1) {
                zos.write(buf, 0, len);
            }
            zos.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                zos.putNextEntry(new ZipEntry(name + "/"));
                zos.closeEntry();
            } else {
                for (File file : listFiles) {
                    if (KeepDirStructure) {
                        compress(file, zos, name + "/" + file.getName(), KeepDirStructure);
                    } else {
                        compress(file, zos, file.getName(), KeepDirStructure);
                    }
                }
            }
        }
    }
    public static void toZip(List<File> srcFiles , OutputStream out)throws RuntimeException {
        long start = System.currentTimeMillis();
        ZipOutputStream zos = null ;
        try{
            zos = new ZipOutputStream(out);
            for (File srcFile : srcFiles) {
                if (srcFile.isFile()){
                    byte[] buf = new byte[BUFFER_SIZE];
                    zos.putNextEntry(new ZipEntry(srcFile.getName()));
                    int len;
                    FileInputStream in = new FileInputStream(srcFile);
                    while ((len = in.read(buf)) != -1){
                        zos.write(buf, 0, len);
                    }
                    zos.closeEntry();
                    in.close();
                }else{
                    compress(srcFile,zos,srcFile.getName(),true);
                }

            }
            long end = System.currentTimeMillis();
            System.out.println("压缩完成，耗时：" + (end - start) +" ms");
        }catch (Exception e){
            throw new RuntimeException("zip error from getZip",e);
        }finally {
            if(zos != null){
                try {
                    zos.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
@PostMapping("/create")
    public Result getTip(){
        List<File> fileList = new ArrayList<>();
        File file=null;
        if (!new File(path).exists()){
            return ResultGenerator.genFailResult("没有可压缩文件");
        }
        fileList.add(new File(path+"/img"));
        fileList.add(new File(path+"/staffInformationList.xlsx"));
        try {
            file= new File(createPathZip);
            FileOutputStream fos2 = new FileOutputStream(file);
            toZip(fileList, fos2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ResultGenerator.genSuccessResult(file.getName());
    }
}
