package org.example.demo.view.ftp;

import lombok.Data;

@Data
public class FileVO {
    private String fileName;
    private String filePath;
    private Long fileByteCount;
    private String fileDisplaySize;
    private String fileLastModifiedTime;
    private Boolean isDirectory;
}
