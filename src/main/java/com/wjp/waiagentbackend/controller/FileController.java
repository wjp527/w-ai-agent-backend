package com.wjp.waiagentbackend.controller;

import cn.hutool.core.io.FileUtil;

import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.wjp.waiagentbackend.common.BaseResponse;
import com.wjp.waiagentbackend.common.ErrorCode;
import com.wjp.waiagentbackend.common.ResultUtils;
import com.wjp.waiagentbackend.constant.FileConstant;
import com.wjp.waiagentbackend.exception.BusinessException;
import com.wjp.waiagentbackend.manager.CosManager;
import com.wjp.waiagentbackend.model.dto.file.UploadFileRequest;
import com.wjp.waiagentbackend.model.enums.FileUploadBizEnum;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * 文件接口
 *
 * @author <a href="https://github.com/liwjp">程序员鱼皮</a>
 * @from <a href="https://wjp.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

//    @Resource
//    private UserService userService;

    @Resource
    private CosManager cosManager;

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                           UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        validFile(multipartFile, fileUploadBizEnum);
//        User loginUser = userService.getLoginUser(request);
        // 文件目录：根据业务、用户来划分
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), "π", filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            // 返回可访问地址
            return ResultUtils.success(FileConstant.COS_HOST + filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long THREE_M = 3 * 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > THREE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 3M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }

    /**
     * 测试下载文件
     * @param filepath 文件路径
     * @param response HTTP响应
     * @throws IOException
     */
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        // 获取文件
        COSObjectInputStream cosObjectInput = null;
        // 创建 GetObjectRequest 对象，设置存储桶名称、ObjectKey
        COSObject cosObject = cosManager.getObject(filepath);
        // 获取 COSObject 的输入流
        cosObjectInput = cosObject.getObjectContent();
        try {
            // 处理下载的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            // 流式响应
            // application/octet-stream: 二进制流数据。对于大多数浏览器，这种类型会触发下载行为
            response.setContentType("application/octet-stream;charset=UTF-8");
            // 设置HTTP响应的文件名
            // setHeader(): 用于设置HTTP响应头部字段
            // Content-Disposition:
            // - HTTP头字段，用于指定响应的呈现方式
            // - attachment: 响应一个附件，客户端会下载文件
            // - filename= : 制定文件的下载名称
            //   - filepath: 是下载到客户端显示的名称
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);

            // 写入响应
            response.getOutputStream().write(bytes);
            // 刷新缓冲区
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            // ✨关闭流
            if(cosObjectInput != null) {
                cosObjectInput.close();
            }
        }

    }
}
