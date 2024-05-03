package com.samourai.wallet.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import io.reactivex.functions.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;

public class SystemUtil {
    private static final Logger log = LoggerFactory.getLogger(SystemUtil.class);
    public static Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    public static void createFile(File f) throws Exception {
        if (!f.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("Creating file " + f.getAbsolutePath());
            }
            try {
                f.createNewFile();
            } catch (Exception e) {
                throw new Exception("Unable to write file " + f.getAbsolutePath());
            }
        }
    }

    public static void safeWrite(File file, Consumer<File> callback) throws Exception {
        if (!file.exists()) {
            file.createNewFile();
        }
        FileLock fileLock = lockFile(file);

        File tempFile = null;
        try {
            try {
                // write to temp file (in same directory)
                tempFile = new File(file.getParent(), file.getName() + ".tmp");
                callback.accept(tempFile);
            } finally {
                // unlock before rename
                unlockFile(fileLock);
            }

            // rename
            Files.move(tempFile, file);
        } catch (Exception e) {
            log.error(
                    "safeWrite failed for "
                            + (tempFile != null ? tempFile.getAbsolutePath() : "null")
                            + " ->"
                            + file.getAbsolutePath());
            throw e;
        }
    }

    public static void safeWriteValue(final File file, final ObjectMapper mapper, final Object value)
            throws Exception {
        Consumer<File> callback = tempFile -> mapper.writeValue(tempFile, value);
        safeWrite(file, callback);
    }

    public static void safeWrite(final File file, final byte[] value)
            throws Exception {
        Consumer<File> callback = tempFile -> Files.write(value, tempFile);
        safeWrite(file, callback);
    }

    public static FileLock lockFile(File f) throws Exception {
        return lockFile(
                f,
                "Cannot lock file "
                        + f.getAbsolutePath()
                        + ". Make sure no other Whirlpool instance is running in same directory.");
    }

    public static FileLock lockFile(File f, String errorMsg) throws Exception {
        FileChannel channel = new RandomAccessFile(f, "rw").getChannel();
        FileLock fileLock = channel.tryLock(); // exclusive lock
        if (fileLock == null) {
            throw new Exception(errorMsg);
        }
        return fileLock; // success
    }

    public static void unlockFile(FileLock fileLock) throws Exception {
        fileLock.release();
        fileLock.channel().close();
    }

    public static String readFile(File file) throws Exception {
        return Files.toString(file, CHARSET_UTF8);
    }

    public static void mkDir(File f) throws Exception {
        if (!f.isDirectory()) {
            f.mkdir();
        }
    }
}
