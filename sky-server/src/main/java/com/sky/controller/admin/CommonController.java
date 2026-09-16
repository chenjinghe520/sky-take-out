package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) throws IOException {
        log.info("文件上传：{}",file);

        String originalFilename = file.getOriginalFilename();

        //获取文件后缀,例如 .jpg
        String suffix = originalFilename.substring(
                originalFilename.lastIndexOf(".")
        );

        //生成新文件名
        String fileName = UUID.randomUUID() + suffix;

        // 项目根目录 + sky-upload
        String uploadPath =
                System.getProperty("user.dir") + "/sky-upload/";

        File dir = new File(uploadPath);

        log.info("user.dir：{}", System.getProperty("user.dir"));
        //文件夹不存在就创建
        if(!dir.exists()){
            dir.mkdirs();
        }

        //最终保存位置
        File dest = new File(dir,fileName);

        //保存文件
        file.transferTo(dest);

        // 返回给前端的访问地址
        String url = "http://localhost:8080/files/" + fileName;

        return Result.success(url);
    }
}
