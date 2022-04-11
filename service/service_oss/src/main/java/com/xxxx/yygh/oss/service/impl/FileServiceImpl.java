package com.xxxx.yygh.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.xxxx.yygh.oss.service.FileService;
import com.xxxx.yygh.oss.utils.ConstantOssPropertiesUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author v
 */
@Service
public class FileServiceImpl implements FileService {

    //文件上传

    @Override
    public String upload(MultipartFile file) {

        String endpoint = ConstantOssPropertiesUtils.ENDPOINT;
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = ConstantOssPropertiesUtils.ACCESS_KEY_ID;
        String accessKeySecret = ConstantOssPropertiesUtils.SECRET;
        String bucketName = ConstantOssPropertiesUtils.BUCKET;

        InputStream inputStream = null;
        OSS ossClient = null;
        //生成随机id值，，使用uuid，添加到文件名称里
        String uuid = UUID.randomUUID().toString().replaceAll("-","");
        //按照当前日期创建文件夹，上传到文件夹里
        //2022/02/08 01.jpg
        String timeUrl = new DateTime().toString("yyyy/MM/dd");

        //文件路径名称
        String fileName = timeUrl+"/"+uuid+file.getOriginalFilename();
        try {
            // 创建OSSClient实例
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            inputStream = file.getInputStream();

            ossClient.putObject(bucketName,fileName,inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }


        // 关闭OSSClient。
        ossClient.shutdown();
        //https://yygh-2-8.oss-cn-guangzhou.aliyuncs.com/c273e283f8e0faf71c356fddfe0fb6d9.jpeg

        String url = "http://"+bucketName+"."+endpoint+"/"+ fileName;
        return url;
    }
}
