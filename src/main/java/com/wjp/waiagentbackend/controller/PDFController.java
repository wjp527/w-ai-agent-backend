package com.wjp.waiagentbackend.controller;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.wjp.waiagentbackend.app.LoveApp;
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
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件接口
 *
 * @author <a href="https://github.com/liwjp">程序员鱼皮</a>
 * @from <a href="https://wjp.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/pdf")
@Slf4j
public class PDFController {

//    @Resource
//    private UserService userService;

    @Resource
    private LoveApp loveApp;

    @Resource
    private CosManager cosManager;

    private String testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        return answer;
    }

    /**
     * 文件上传
     *
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(UploadFileRequest uploadFileRequest, String message, HttpServletRequest request) {
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 测试 PDF 生成
        // "生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单"
        String result =  testMessage(message);

        System.out.println("结果: " + result);

        // 正则匹配 ![](D:\\
        // 优化后的正则表达式（适配中文路径和特殊符号）
        String regex = "D:\\\\[^\\s()'\"]+?\\.pdf";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(result);

        // 提取结果
        if (matcher.find()) {
            String filePath = matcher.group()
                    .replaceAll("\\\\+", "\\\\")  // 标准化反斜杠
                    .replaceAll("[‘’”“]", "");    // 去除中文引号

            System.out.println("提取路径：" + filePath);

            // 我该刚刚保存到本地的PDF中，存入到cos中
            File file = new File(filePath);

            //        User loginUser = userService.getLoginUser(request);
            pattern = Pattern.compile(".*[\\\\/]([^\\\\/]+?)(?=\\.\\w+$)");
            matcher = pattern.matcher(filePath);


            String fileName = file.getName(); // 直接提取完整文件名
            // 文件目录：根据业务、用户来划分
            String uuid = RandomStringUtils.randomAlphanumeric(8);
            String filename = uuid + "-" + fileName;
            String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), "π", filename);

            try {
                // 上传文件
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

        return null;
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
