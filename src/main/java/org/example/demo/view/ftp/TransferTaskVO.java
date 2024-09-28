package org.example.demo.view.ftp;

import lombok.Data;

@Data
public class TransferTaskVO {

    public static final String UPLOAD = "UPLOAD";
    public static final String DOWNLOAD = "DOWNLOAD";

    public static final Long WAITING = 1L;
    public static final Long RUNNING = 2L;
    public static final Long SUCCESS = 3L;
    public static final Long FAIL = 4L;

    private String fileName;
    private String type;
    private Long fileByteCount;
    private Long bytesTransferred;
    private String localFilePath;
    private String remoteFilePath;
    private Long status;

}
