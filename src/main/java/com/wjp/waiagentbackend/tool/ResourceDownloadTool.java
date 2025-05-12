package com.wjp.waiagentbackend.tool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.wjp.waiagentbackend.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

/**
 * 资源下载
 * @author wjp
 */
public class ResourceDownloadTool {

    @Tool(description = "Download a resource from a given URL")
    public String downloadResource(
            @ToolParam(description = "URL of the resource to download") String url,
            @ToolParam(description = "Name of the file to save the download resource") String fileName) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/download";
        String filePath = fileDir + "/" + fileName;

        try {
            FileUtil.mkdir(fileDir);
            HttpUtil.downloadFile(url, new File(filePath));
            return "Resource downloaded successfully to " + filePath;
        } catch (Exception e) {
            return "Error downloading resource: " + e.getMessage();
        }
    }

}
