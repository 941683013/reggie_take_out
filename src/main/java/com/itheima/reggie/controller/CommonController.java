package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    String path;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        String filename = file.getOriginalFilename();

        String suffix = filename.substring(filename.lastIndexOf("."));
        // 使用UUID生成一个新名字
        String newFileName = UUID.randomUUID() + suffix;
        String pathName = path + newFileName;

        // 如果目录不存在就创建一个目录
        File dir = new File(path);
        if(!dir.exists()) {
            dir.mkdir();
        }

        try {
            file.transferTo(new File(pathName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return R.success(newFileName);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        File file = new File(path + name);
        response.setContentType("image/ipeg");
        try {
            ServletOutputStream out = response.getOutputStream();
            FileInputStream inputStream = new FileInputStream(file);

            int len = 0;
            byte[] buf = new byte[1024];
            while((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
                out.flush();
            }
            out.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
