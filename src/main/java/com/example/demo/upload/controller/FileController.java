package com.example.demo.upload.controller;

import com.example.demo.upload.entity.FileDTO;
import com.example.demo.upload.service.FileService;
import com.example.demo.upload.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 分片上传 参考：
 * https://www.jb51.net/article/190808.htm
 * <p>
 * 访问路径：
 * http://localhost:8000/file/show
 */
@Controller
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Autowired
    FileService fileService;

    public static final String BUSINESS_NAME = "普通分片上传";

    // 设置图片上传路径
    @Value("${file.basepath}")
    private String basePath;

    @RequestMapping("/show")
    public String show() {
        return "file";
    }

    /**
     * 上传
     *
     * @param file
     * @param suffix
     * @param shardIndex
     * @param shardSize
     * @param shardTotal
     * @param size
     * @param key
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @RequestMapping("/upload")
    @ResponseBody
    public String upload(MultipartFile file,
                         String suffix,
                         Long shardIndex,
                         Long shardSize,
                         Long shardTotal,
                         String originName,
                         Long size,
                         String key
    ) throws IOException, InterruptedException {
        log.info("上传文件开始");
        //设置图片新的名字
        String fileName = new StringBuffer().append(key).append(".").append(suffix).toString(); // course\6sfSqfOwzmik4A4icMYuUe.mp4
        //这个是分片的名字
        String localfileName = new StringBuffer(fileName)
                .append(".")
                .append(shardIndex)
                .toString(); // course\6sfSqfOwzmik4A4icMYuUe.mp4.1

        // 以绝对路径保存重名命后的图片
        File targeFile = new File(basePath, localfileName);
        //上传这个图片
        file.transferTo(targeFile);
        //数据库持久化这个数据
        FileDTO file1 = new FileDTO();
        file1.setPath(basePath + localfileName);
        file1.setSuffix(suffix);
        file1.setOriginName(originName);
        file1.setSize(size);
        file1.setShardIndex(shardIndex);
        file1.setShardSize(shardSize);
        file1.setShardTotal(shardTotal);
        file1.setFileKey(key);
        //插入到数据库中
        //保存的时候 去处理一下 这个逻辑
        fileService.save(file1);
        //判断当前是不是最后一个分页 如果不是就继续等待其他分页  合并分页
        if (shardIndex.equals(shardTotal)) {
            file1.setPath(basePath + fileName);
            this.mergeShards(file1);
        }
        return "上传成功";
    }

    @RequestMapping("/check")
    @ResponseBody
    public Result check(String key) {
        List<FileDTO> check = fileService.check(key);
        //如果这个key存在的话 那么就获取上一个分片去继续上传
        if (check.size() != 0) {
            return Result.ok("查询成功", check.get(0));
        }
        return Result.fail("查询失败,可以添加");
    }


    /**
     * @author fengxinglie
     * 合并分页
     */
    private void mergeShards(FileDTO fileDTO) throws InterruptedException {
        boolean toFile = shardsToFile(fileDTO);

        //告诉java虚拟机去回收垃圾 至于什么时候回收  这个取决于 虚拟机的决定
        System.gc();
        //等待100毫秒 等待垃圾回收去 回收完垃圾
        Thread.sleep(100);

        if(toFile){
            deleteShards(fileDTO);
        }
    }

    private boolean shardsToFile(FileDTO fileDTO) {
        log.info("分片合并开始");
        File newFile = new File(basePath + fileDTO.getOriginName());
        byte[] buffer = new byte[10 * 1024 * 1024];
        int len;
        try (FileOutputStream fos = new FileOutputStream(newFile, true)) {
            for (int i = 0; i < fileDTO.getShardTotal(); i++) {
                try (FileInputStream fis = new FileInputStream(new File(fileDTO.getPath() + "." + (i + 1)))) {
                    while ((len = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
        } catch (IOException e) {
            log.error("分片合并异常, error = {}", e);
            if (newFile.exists()) {
                newFile.delete();
            }
            return false;
        }
        log.info("分片结束了");
        return newFile.exists();
    }

    private void deleteShards(FileDTO fileDTO) {
        log.info("删除分片开始");
        for (int i = 0; i < fileDTO.getShardTotal(); i++) {
            String filePath = fileDTO.getPath() + "." + (i + 1);
            File file = new File(filePath);
            if (file.exists()) {
                boolean result = file.delete();
                log.info("删除{}，{}", filePath, result ? "成功" : "失败");
            }
        }
        log.info("删除分片结束");
    }

}
