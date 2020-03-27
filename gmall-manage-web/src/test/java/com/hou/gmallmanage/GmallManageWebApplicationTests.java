package com.hou.gmallmanage;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.io.IOException;

@SpringBootTest
public class GmallManageWebApplicationTests {
//    @Value("${imageurl}")
    @Value("111")
    public String url;



    @Test
   public  void contextLoads() throws IOException, MyException {
        //配置fastdfs全局链接地址
        String path = GmallManageWebApplicationTests.class.getResource("/tracker.conf").getPath();//获得配置文件的路径
        System.out.println(path);//E:/IDEA_work_space/gmall/gmall-manage-web/target/classes/tracker.conf
        ClientGlobal.init(path);  //读取 tracker.conf内容
        TrackerClient trackerClient = new TrackerClient();

        //获取一个trackerserver实例
        TrackerServer trackerServer = trackerClient.getTrackerServer();
        //通过tracker获得一个strorage链接客户端
        StorageClient storageClient = new StorageClient(trackerServer);


        String[] uploadFile = storageClient.upload_file("C:/Users/ym/Desktop/docker.PNG", "PNG", null);//metalist 是元数据信息 没必要上传
        //居然上传到我的容器里了 /var/fdfs/data/00/00  而且我数据卷，映射目录都没找到，他娘的怎么传的 好神奇
        String s = "";
        for (int i = 0; i < uploadFile.length; i++) {
            s = s+"/"+uploadFile[i];
        }
        System.out.println("FAST---->>>>>   "+s);


    }
    @Test
    public void test2(){
        System.out.println(url);
    }

}
