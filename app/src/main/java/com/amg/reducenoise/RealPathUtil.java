package com.amg.reducenoise;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/* loaded from: classes.dex */
public class RealPathUtil {
    public static String getRealPath(Context context, Uri fileUri) {
        return getRealPathFromURI_API19(context, fileUri);
    }

    public static String getRealPathFromURI_API19(final Context context, final Uri uri) {
        String str = "";
        Uri uri2 = null;
        if ((Build.VERSION.SDK_INT >= 19) && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                String[] split = DocumentsContract.getDocumentId(uri).split(":");
                if ("primary".equalsIgnoreCase(split[0])) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                return "";
            } else if (isDownloadsDocument(uri)) {
                String documentId = DocumentsContract.getDocumentId(uri);
                if (documentId != null && documentId.startsWith("raw:/")) {
                    return Uri.parse(documentId).getPath();
                }
                String[] strArr = {"content://downloads/public_downloads", "content://downloads/my_downloads"};
                for (int i = 0; i < 2; i++) {
                    str = getDataColumn(context, ContentUris.withAppendedId(Uri.parse(strArr[i]), Long.valueOf(documentId).longValue()), null, null);
                    if (!TextUtils.isEmpty(str)) {
                        return str;
                    }
                }
                return str;
            } else if (!isMediaDocument(uri)) {
                return "content".equalsIgnoreCase(uri.getScheme()) ? getDataColumn(context, uri, null, null) : "";
            } else {
                String[] split2 = DocumentsContract.getDocumentId(uri).split(":");
                String str2 = split2[0];
                if ("image".equals(str2)) {
                    uri2 = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(str2)) {
                    uri2 = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(str2)) {
                    uri2 = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                return getDataColumn(context, uri2, "_id=?", new String[]{split2[1]});
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        } else {
            return "file".equalsIgnoreCase(uri.getScheme()) ? uri.getPath() : "";
        }
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        try {
            Cursor query = context.getContentResolver().query(uri, new String[]{"_data"}, selection, selectionArgs, null);
            if (query != null) {
                try {
                    if (query.moveToFirst()) {
                        String string = query.getString(query.getColumnIndexOrThrow("_data"));
                        if (query != null) {
                            query.close();
                        }
                        return string;
                    }
                } catch (Throwable th) {
                    th = th;
                    cursor = query;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
            if (query != null) {
                query.close();
            }
        } catch (Throwable th2) {

        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static String getFileName(String filePath) {
        String[] split = filePath.split("/");
        try {
            return split[split.length - 1];
        } catch (Exception unused) {
            return "";
        }
    }

    public static String newFilePath(String suffix, String fileName, String folder) {
        String[] split = fileName.split("\\.");
        String format = split[split.length-1];
        return folder + (fileName.substring(0, (fileName.length() - split[split.length - 1].length()) - 1) + suffix + "." + format);
    }

    public static String newFilePath(String suffix, String fileName, String folder,String ext) {
        String[] split = fileName.split("\\.");
        return folder + (fileName.substring(0, (fileName.length() - split[split.length - 1].length()) - 1) + suffix + "." + ext);
    }

    public static String copyFileToInternal(Context context, Uri fileUri) {
        if (Build.VERSION.SDK_INT >= 26) {
            Cursor query = context.getContentResolver().query(fileUri, new String[]{"_display_name", "_size"}, null, null);
            query.moveToFirst();
            String string = query.getString(query.getColumnIndex("_display_name"));
            query.getLong(query.getColumnIndex("_size"));
            File file = new File(context.getFilesDir() + "/" + string);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                InputStream openInputStream = context.getContentResolver().openInputStream(fileUri);
                byte[] bArr = new byte[1024];
                while (true) {
                    int read = openInputStream.read(bArr);
                    if (read != -1) {
                        fileOutputStream.write(bArr, 0, read);
                    } else {
                        openInputStream.close();
                        fileOutputStream.close();
                        return file.getPath();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
