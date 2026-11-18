package cc.doger.bread_dog;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class Utils {
    private Utils() {}

    static Intent createDocumentIntent(Context context, File file) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".FileProvider", file);
        String type = context.getContentResolver().getType(uri);
        return new Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType(type == null ? "image/*" : type)
                .putExtra(Intent.EXTRA_TITLE, file.getName());
    }

    static void saveToUri(Context context, File source, Uri destination) throws IOException {
        try (FileInputStream input = new FileInputStream(source);
             OutputStream output = context.getContentResolver().openOutputStream(destination, "wt")) {
            PackFiles.copy(input, output);
        }
    }

    public static void share(Context context, File file, int type) {
        try {
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".FileProvider", file);
            String mime = context.getContentResolver().getType(uri);
            Intent intent = new Intent(Intent.ACTION_SEND)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .setType(mime == null ? "image/*" : mime)
                    .putExtra(Intent.EXTRA_STREAM, uri);
            intent.setClipData(ClipData.newRawUri(file.getName(), uri));
            switch (type) {
                case 1:
                    intent.setPackage("com.tencent.mobileqq");
                    break;
                case 2:
                    intent.setPackage("com.tencent.mm");
                    break;
                case 3:
                    intent.setPackage("com.tencent.tim");
                    break;
                default:
                    break;
            }
            if (type == 0) {
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.str_share)));
            } else {
                context.startActivity(intent);
            }
        } catch (RuntimeException e) {
            Toast.makeText(context, R.string.share_failed, Toast.LENGTH_LONG).show();
        }
    }
}
