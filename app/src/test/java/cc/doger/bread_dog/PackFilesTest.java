package cc.doger.bread_dog;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;

public class PackFilesTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test public void discoversOnlyNamedZipPacksInOrder() {
        assertEquals(Arrays.asList("01_first.zip", "02_name_with_underscores.ZIP"),
                PackFiles.packs(new String[]{"notes.txt", "02_name_with_underscores.ZIP",
                        "1_wrong.zip", "01_first.zip", "03_empty", "04_.zip"}));
        assertEquals(Collections.emptyList(), PackFiles.packs(null));
        assertEquals("name_with_underscores", PackFiles.packLabel("02_name_with_underscores.ZIP"));
    }

    @Test public void extractsNestedUnicodeImageAndClosesInput() throws Exception {
        String name = "nested/\u8868\u60c5.png";
        byte[] data = "image bytes".getBytes(StandardCharsets.UTF_8);
        TrackingInput input = new TrackingInput(zip(name, data));
        File folder = temporary.newFolder();
        PackFiles.extract(input, folder);
        assertTrue(input.closed);
        assertArrayEquals(data, Files.readAllBytes(new File(folder, name).toPath()));
        assertEquals(Collections.singletonList(new File(folder, name)), PackFiles.images(folder));
    }

    @Test public void rejectsTraversalAndClosesInputOnFailure() throws Exception {
        File folder = temporary.newFolder("pack");
        TrackingInput input = new TrackingInput(zip("../escaped.png", new byte[]{1}));
        assertThrows(IOException.class, () -> PackFiles.extract(input, folder));
        assertTrue(input.closed);
        assertFalse(new File(temporary.getRoot(), "escaped.png").exists());
    }

    @Test public void rejectsWindowsTraversalAndAbsolutePaths() throws Exception {
        for (String name : Arrays.asList("..\\escaped.png", "C:/escaped.png", "/escaped.png", "../pack-other/image.png")) {
            File folder = temporary.newFolder();
            assertThrows(name, IOException.class, () -> PackFiles.extract(
                    new ByteArrayInputStream(zip(name, new byte[]{1})), folder));
        }
    }

    @Test public void rejectsInvalidEmptyAndTruncatedArchives() throws Exception {
        byte[] valid = zip("image.png", new byte[1024]);
        for (byte[] bytes : Arrays.asList(new byte[0], new byte[]{1, 2, 3, 4}, Arrays.copyOf(valid, 20))) {
            File folder = temporary.newFolder();
            assertThrows(IOException.class, () -> PackFiles.extract(new ByteArrayInputStream(bytes), folder));
        }
    }

    @Test public void filtersMetadataDirectoriesAndUnsupportedFiles() throws Exception {
        File folder = temporary.newFolder();
        Files.write(new File(folder, "a.PNG").toPath(), new byte[]{1});
        Files.write(new File(folder, "b.gif").toPath(), new byte[]{2});
        Files.write(new File(folder, "README").toPath(), new byte[]{3});
        Files.write(new File(folder, ".hidden.png").toPath(), new byte[]{4});
        File metadata = new File(folder, "__MACOSX");
        PackFiles.ensureDirectory(metadata);
        Files.write(new File(metadata, "junk.png").toPath(), new byte[]{5});
        List<File> files = PackFiles.images(folder);
        assertEquals(Arrays.asList(new File(folder, "a.PNG"), new File(folder, "b.gif")), files);
        assertEquals("README", PackFiles.imageLabel(new File("README")));
        assertEquals("a", PackFiles.imageLabel(new File("a.PNG")));
    }

    @Test public void emptyDirectoryIsEmptyAndMissingDirectoryFails() throws Exception {
        assertTrue(PackFiles.images(temporary.newFolder()).isEmpty());
        assertThrows(IOException.class, () -> PackFiles.images(new File(temporary.getRoot(), "missing")));
    }

    @Test public void copyUsesBoundedBuffer() throws Exception {
        byte[] data = new byte[2 * 1024 * 1024];
        ByteArrayInputStream input = new ByteArrayInputStream(data) {
            @Override public synchronized int read(byte[] buffer, int offset, int length) {
                assertTrue("Copy buffer exceeds 32 KiB", length <= 32 * 1024);
                return super.read(buffer, offset, length);
            }
        };
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PackFiles.copy(input, output);
        assertArrayEquals(data, output.toByteArray());
    }

    @Test public void copyReportsMissingAndFailingOutput() {
        assertThrows(IOException.class, () -> PackFiles.copy(new ByteArrayInputStream(new byte[]{1}), null));
        OutputStream failing = new OutputStream() {
            @Override public void write(int value) throws IOException {
                throw new IOException("Disk full");
            }
        };
        assertThrows(IOException.class, () -> PackFiles.copy(new ByteArrayInputStream(new byte[]{1}), failing));
    }

    @Test public void copyHonorsCancellation() {
        try {
            Thread.currentThread().interrupt();
            assertThrows(IOException.class, () -> PackFiles.copy(
                    new ByteArrayInputStream(new byte[]{1}), new ByteArrayOutputStream()));
        } finally {
            Thread.interrupted();
        }
    }

    private static byte[] zip(String name, byte[] data) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
            zip.putNextEntry(new ZipEntry(name));
            zip.write(data);
            zip.closeEntry();
        }
        return bytes.toByteArray();
    }

    private static final class TrackingInput extends ByteArrayInputStream {
        boolean closed;

        TrackingInput(byte[] bytes) {
            super(bytes);
        }

        @Override public void close() throws IOException {
            closed = true;
            super.close();
        }
    }
}
