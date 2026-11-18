package cc.doger.bread_dog;

import android.content.Context;
import android.content.pm.PackageManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

final class EmoticonRepository {
    private final Context context;

    EmoticonRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    // Publish only complete packs; existing files remain valid for shares and pending exports.
    private synchronized static List<File> load(Context context, String assetName) throws IOException {
        long installedAt;
        try {
            installedAt = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).lastUpdateTime;
        } catch (PackageManager.NameNotFoundException e) {
            throw new IOException("Cannot identify installed assets", e);
        }
        File root = new File(context.getCacheDir(), "img/" + BuildConfig.VERSION_CODE + "-" + installedAt);
        PackFiles.ensureDirectory(root);
        File destination = new File(root, assetName);
        if (!destination.getCanonicalPath().startsWith(root.getCanonicalPath() + File.separator)) {
            throw new IOException("Invalid pack name");
        }
        if (new File(destination, ".complete").isFile()) {
            List<File> cached = PackFiles.images(destination);
            if (!cached.isEmpty()) {
                return cached;
            }
        }

        File staging = Files.createTempDirectory(root.toPath(), "unpack-").toFile();
        try {
            try (InputStream input = context.getAssets().open(assetName)) {
                PackFiles.extract(input, staging);
            }
            if (PackFiles.images(staging).isEmpty()) {
                throw new IOException("No supported images in pack");
            }
            if (!new File(staging, ".complete").createNewFile()) {
                throw new IOException("Cannot mark pack complete");
            }
            PackFiles.deleteTree(destination);
            if (!staging.renameTo(destination)) {
                throw new IOException("Cannot publish extracted pack");
            }
            return PackFiles.images(destination);
        } finally {
            PackFiles.deleteTree(staging);
        }
    }

    List<File> load(String assetName) throws IOException {
        return load(context, assetName);
    }
}
