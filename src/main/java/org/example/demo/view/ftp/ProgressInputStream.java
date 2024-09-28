package org.example.demo.view.ftp;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class ProgressInputStream extends FilterInputStream {

    private Long totalBytesRead;
    private Consumer<Long> action;

    public ProgressInputStream(InputStream inputStream, Consumer<Long> action) {
        super(inputStream);
        this.totalBytesRead = 0L;
        this.action = action;
    }

    @Override
    public int read() throws IOException {
        int read = super.read();
        if (read != -1) {
            totalBytesRead += 1;
            action.accept(totalBytesRead);
        }
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = super.read(b, off, len);
        if (read != -1) {
            totalBytesRead += read;
            action.accept(totalBytesRead);
        }
        return read;
    }
}
