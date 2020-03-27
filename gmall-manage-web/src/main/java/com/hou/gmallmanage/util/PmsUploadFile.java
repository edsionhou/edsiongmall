package com.hou.gmallmanage.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;

@Component
public class PmsUploadFile {
//    @Value("${imageurl.url}")   static修饰的变量无法使用 @Value
    private static  String url= "http://192.168.199.240:8888";


    public static  String uploadImage(MultipartFile multipartFile) {
        //配置fastdfs全局链接地址
        String path = PmsUploadFile.class.getResource("/tracker.conf").getPath();//获得配置文件的路径
        System.out.println(path);//E:/IDEA_work_space/gmall/gmall-manage-web/target/classes/tracker.conf
        try {
            ClientGlobal.init(path);  //读取 tracker.conf内容

            TrackerClient trackerClient = new TrackerClient();

            //获取一个trackerserver实例
            TrackerServer trackerServer = trackerClient.getTrackerServer();
            //通过tracker获得一个strorage链接客户端
            StorageClient storageClient = new StorageClient(trackerServer);


            byte[] bytes = multipartFile.getBytes();//获得上传的二进制对象
            String[] split = multipartFile.getOriginalFilename().split("\\.");
            System.out.println(Arrays.toString(split));
            String png = split[split.length-1];
            String[] uploadFile = storageClient.upload_file(bytes, png, null);//metalist 是元数据信息 没必要上传
            //居然上传到我的容器里了 /var/fdfs/data/00/00  而且我数据卷，映射目录都没找到，他娘的怎么传的 好神奇
            System.out.println(uploadFile);
            String s = url;
            for (int i = 0; i < uploadFile.length; i++) {
                s = s + "/" + uploadFile[i];
            }
            System.out.println("FAST---->>>>>   " + s);
            return s;
        }catch (Exception e){
            e.printStackTrace();
        }
       return null;
    }
}
