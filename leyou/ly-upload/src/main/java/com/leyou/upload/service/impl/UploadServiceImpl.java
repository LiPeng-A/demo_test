package com.leyou.upload.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.upload.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class UploadServiceImpl implements UploadService {

    private static final List<String> ALLOW_TYPE= Arrays.asList("image/jpeg","image/png","image/bmp");

    @Override
    public String uploadImage(MultipartFile file) {
        try {
            //校验文件类型
            String type = file.getContentType();
            if(!ALLOW_TYPE.contains(type))
            {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            //校验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if(image==null)
            {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            //1.保存文件到本地
            File dest = new File("F:\\upload",file.getOriginalFilename());
            file.transferTo(dest);
            //2.返回路径
            return "http://image.leyou.com/"+file.getOriginalFilename();
        } catch (IOException e) {
            //上传失败，记录日志
            log.error("上传失败",e);
            throw  new LyException(ExceptionEnum.UPLOAD_ERROR);
        }

    }
}
