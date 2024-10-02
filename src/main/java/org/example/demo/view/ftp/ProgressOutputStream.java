package org.example.demo.view.ftp;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

public class ProgressOutputStream extends FilterOutputStream {

    private Long totalBytesWrite;
    private Consumer<Long> action;

    public ProgressOutputStream(OutputStream outputStream, Consumer<Long> action) {
        super(outputStream);
        this.totalBytesWrite = 0L;
        this.action = action;
    }

    @Override
    public void write(int b) throws IOException {
        super.write(b);
        totalBytesWrite += 1;
        action.accept(totalBytesWrite);
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
    }
}
