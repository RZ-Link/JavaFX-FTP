package org.example.demo.view.ftp;

import lombok.Data;

@Data
public class FileVO {
    private String fileName;
    private String fileSize;
    private String fileLastModifiedTime;
    private Boolean isDirectory;
}
