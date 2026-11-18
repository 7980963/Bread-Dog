package cc.doger.bread_dog;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

final class PackFiles {
    private static final Pattern PACK_NAME = Pattern.compile("[0-9]{2}_.+\\.zip", Pattern.CASE_INSENSITIVE);
    private static final int BUFFER_SIZE = 32 * 1024;
    private static final long MAX_BYTES = 256L * 1024 * 1024;
    private static final int MAX_ENTRIES = 10000;

    private PackFiles() {}

    static List<String> packs(String[] assets) {
        List<String> result = new ArrayList<>();
        if (assets != null) {
            for (String asset : assets) {
                if (PACK_NAME.matcher(asset).matches()) {
                    result.add(asset);
                }
            }
        }
        result.sort(String::compareTo);
        return result;
    }

    static String packLabel(String name) {
        return name.substring(name.indexOf('_') + 1, name.lastIndexOf('.'));
    }

    static String imageLabel(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    static List<File> images(File directory) throws IOException {
        List<File> result = new ArrayList<>();
        collectImages(directory, result);
        Collator collator = Collator.getInstance(Locale.CHINA);
        result.sort((left, right) -> collator.compare(left.getPath(), right.getPath()));
        return result;
    }

    private static void collectImages(File directory, List<File> result) throws IOException {
        File[] files = directory.listFiles();
        if (files == null) {
            throw new IOException("Cannot read image directory: " + directory);
        }
        for (File file : files) {
            checkInterrupted();
            if (file.getName().startsWith(".") || file.getName().equals("__MACOSX")) {
                continue;
            }
            if (file.isDirectory()) {
                collectImages(file, result);
            } else if (file.isFile() && isImage(file.getName())) {
                result.add(file);
            }
        }
    }

    private static boolean isImage(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".gif") || lower.endsWith(".webp") || lower.endsWith(".bmp");
    }

    static void extract(InputStream input, File directory) throws IOException {
        try (ZipArchiveInputStream zip = new ZipArchiveInputStream(input, "UTF-8", true)) {
            ensureDirectory(directory);
            String root = directory.getCanonicalPath() + File.separator;
            byte[] buffer = new byte[BUFFER_SIZE];
            long total = 0;
            int entries = 0;
            ZipArchiveEntry entry;
            while ((entry = zip.getNextZipEntry()) != null) {
                checkInterrupted();
                if (++entries > MAX_ENTRIES) {
                    throw new IOException("Too many archive entries");
                }
                File target = new File(directory, entry.getName());
                if (entry.getName().startsWith("/") || new File(entry.getName()).isAbsolute()
                        || !target.getCanonicalPath().startsWith(root) || entry.isUnixSymlink()
                        || entry.getName().contains("\\") || entry.getName().contains(":")) {
                    throw new IOException("Unsafe archive entry: " + entry.getName());
                }
                if (!zip.canReadEntryData(entry)) {
                    throw new IOException("Unsupported archive entry: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    ensureDirectory(target);
                    continue;
                }
                ensureDirectory(target.getParentFile());
                try (OutputStream output = new FileOutputStream(target)) {
                    int count;
                    while ((count = zip.read(buffer)) != -1) {
                        checkInterrupted();
                        total += count;
                        if (total > MAX_BYTES) {
                            throw new IOException("Archive exceeds extraction limit");
                        }
                        output.write(buffer, 0, count);
                    }
                }
            }
            if (entries == 0) {
                throw new IOException("Empty or invalid archive");
            }
        }
    }

    static void copy(InputStream input, OutputStream output) throws IOException {
        if (output == null) {
            throw new IOException("Document provider did not open an output stream");
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        int count;
        while ((count = input.read(buffer)) != -1) {
            checkInterrupted();
            output.write(buffer, 0, count);
        }
    }

    static void ensureDirectory(File directory) throws IOException {
        if (!directory.isDirectory() && !directory.mkdirs()) {
            throw new IOException("Cannot create directory: " + directory);
        }
    }

    static void deleteTree(File file) throws IOException {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children == null) {
                throw new IOException("Cannot read directory: " + file);
            }
            for (File child : children) {
                deleteTree(child);
            }
        }
        if (file.exists() && !file.delete()) {
            throw new IOException("Cannot delete: " + file);
        }
    }

    private static void checkInterrupted() throws InterruptedIOException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedIOException("File operation cancelled");
        }
    }
}
