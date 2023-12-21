package com.amg.reducenoise;

import java.io.File;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.Date;

/* loaded from: classes.dex */
public class MediaFile {
    private File file;
    private String lastModification;
    private String name;
    private String size;

    private MediaType type;

    public MediaFile(File file, MediaType type) {
        this.name = file.getName();
        this.file = file;
        this.lastModification = convertToDate(file.lastModified());
        this.size = convertToSize(file.length());
        this.type = type;
    }

    private String convertToSize(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        StringCharacterIterator stringCharacterIterator = new StringCharacterIterator("KMGTPE");
        while (true) {
            if (bytes > -999950 && bytes < 999950) {
                return String.format("%.1f %cB", Double.valueOf(bytes / 1000.0d), Character.valueOf(stringCharacterIterator.current()));
            }
            bytes /= 1000;
            stringCharacterIterator.next();
        }
    }

    private String convertToDate(long lastModified) {
        return new SimpleDateFormat("HH:mm, dd MMM yyyy").format(new Date(lastModified));
    }

    public String getLastModification() {
        return this.lastModification;
    }

    public String getSize() {
        return this.size;
    }

    public String getName() {
        return this.name;
    }

    public File getFile() {
        return this.file;
    }

    public MediaType getType() {
        return type;
    }
}
