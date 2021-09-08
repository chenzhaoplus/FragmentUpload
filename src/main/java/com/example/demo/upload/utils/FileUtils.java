package com.example.demo.upload.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Objects;

/**
 * @author fr
 * @date 2019年11月18日, 0018 19:55
 */
@Slf4j
public class FileUtils {

    public static String file2Base64(File file) {
        if (file == null) {
            return null;
        }
        String base64 = null;
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            byte[] buff = new byte[fin.available()];
            fin.read(buff);
            base64 = Base64.encodeBase64String(buff);
        } catch (FileNotFoundException e) {
            log.error("[文件转base64失败], error = {}", e.getMessage());
        } catch (IOException e) {
            log.error("[文件转base64失败], error = {}", e.getMessage());
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return base64;
    }

    /**
     * 将inputStream转化为file
     *
     * @param is
     * @param file 要输出的文件目录
     */
    public static void inputStream2File(InputStream is, File file) throws IOException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            int len = 0;
            byte[] buffer = new byte[8192];

            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } finally {
            os.close();
            is.close();
        }
    }


    public static String instream2Base64(InputStream fis) {
        String base64 = null;
        try (InputStream fin = fis) {
            byte[] buff = new byte[fin.available()];
            fin.read(buff);
            base64 = Base64.encodeBase64String(buff);
        } catch (FileNotFoundException e) {
            log.error("[文件转base64失败], error = {}", e.getMessage());
        } catch (IOException e) {
            log.error("[文件转base64失败], error = {}", e.getMessage());
        }
        return base64;
    }

    public static File base64ToFile(String base64) {
        if (base64 == null || "".equals(base64)) {
            return null;
        }
        byte[] buff = Base64.decodeBase64(base64);
        File file = null;
        FileOutputStream fout = null;
        try {
            file = File.createTempFile("tmp", ".jpg");
            fout = new FileOutputStream(file);
            fout.write(buff);
        } catch (IOException e) {
            log.warn("[base64转文件失败], error = {}", e.getMessage());
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    public static InputStream baseToInputStream(String base64) {
        ByteArrayInputStream stream = null;
        try {
            byte[] bytes = Base64.decodeBase64(base64);
            stream = new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            log.warn("[base64转文件读取流失败], error = {}", e.getMessage());
        }
        return stream;
    }

    public static void main(String[] args) {
        String path = "D:\\face.jpg";
        File file = new File(path);
        String base64 = file2Base64(file);
        System.out.println(base64);

        File file1 = base64ToFile(base64);
        System.out.println(file1.getAbsolutePath());
    }

    public static String fileNameWithTime(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return fileName;
        }

        StringBuilder sb = new StringBuilder();
        long time = System.currentTimeMillis();
        int index = fileName.indexOf(".");
        if (index != -1) {
            return sb.append(fileName.substring(0, index))
                    .append(StrUtil.UNDERLINE)
                    .append(time)
                    .append(fileName.substring(index, fileName.length()))
                    .toString();
        }

        return sb.append(fileName)
                .append(StrUtil.UNDERLINE)
                .append(time)
                .toString();
    }

    public static void writeFile(HttpServletResponse resp, InputStream is, String fileName) {
        if (Objects.isNull(is)) {
            return;
        }
        Validate.isTrue(StringUtils.isNotBlank(fileName), "文件名称不能为空");

        resp.setContentType("application/octet-stream");
        resp.setHeader("Content-Disposition", "attachment;fileName=" + fileName);

        try (OutputStream out = resp.getOutputStream()) {
            int len = 0;
            byte[] b = IOUtils.toByteArray(is);
            IOUtils.write(b, out);
            out.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public static String getRootByPath(String path) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        String[] dirs = path.split(StrUtil.SLASH);
        if (Objects.isNull(dirs) || dirs.length == 0) {
            return null;
        }
        for (String d : dirs) {
            if (StringUtils.isNotBlank(d)) {
                return d;
            }
        }
        return null;
    }

    public static String exceptRootByPath(String path) {
        String bucketName = getRootByPath(path);
        if (StringUtils.isBlank(bucketName)) {
            return null;
        }
        if (path.startsWith(StrUtil.SLASH)) {
            path = path.substring(1);
        }
        return path.replaceFirst(bucketName, "");
    }

}
