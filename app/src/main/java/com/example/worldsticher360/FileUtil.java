package com.example.worldsticher360;
import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_PICTURES;

//class taken from https://github.com/YarikSOffice/OpenCV-Playground
public class FileUtil {
    private final Context context;

    public FileUtil(Context context) {
        this.context = context;
    }

    public List<File> urisToFiles(List<Uri> uris) throws IOException {
        List<File> files = new ArrayList<>(uris.size());
        for (Uri uri : uris) {
            File file = createTempFile(requireTemporaryDirectory());
            writeUriToFile(uri, file);
            files.add(file);
        }
        return files;
    }

    public File createResultFile() throws IOException {
        File pictures = context.getExternalFilesDir(DIRECTORY_PICTURES);
        if (pictures == null) {
            throw new IOException("External storage not available");
        }
        return createTempFile(new File(pictures, RESULT_DIRECTORY_NAME));
    }

    private File createTempFile(File root) throws IOException {
        root.mkdirs(); // make sure that the directory exists
        String date = new SimpleDateFormat(DATE_FORMAT_TEMPLATE, Locale.getDefault()).format(new Date());
        String filePrefix = IMAGE_NAME_TEMPLATE.replace("%s", date);
        return File.createTempFile(filePrefix, JPG_EXTENSION, root);
    }

    private void writeUriToFile(Uri target, File destination) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(target);
        if (inputStream == null) {
            throw new IOException("Failed to open input stream for URI: " + target);
        }
        try (FileOutputStream outputStream = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    private File requireTemporaryDirectory() {
        // don't need read/write permission for this directory starting from android 19
        File pictures = context.getExternalFilesDir(DIRECTORY_PICTURES);
        if (pictures == null) {
            throw new RuntimeException("External storage not available");
        }
        return new File(pictures, TEMPORARY_DIRECTORY_NAME);
    }

    // there is no built-in function for deleting folders
    private void remove(File file) {
        if (file.isDirectory()) {
            File[] entries = file.listFiles();
            if (entries != null) {
                for (File entry : entries) {
                    remove(entry);
                }
            }
        }
        file.delete();
    }

    public static final String TEMPORARY_DIRECTORY_NAME = "Temporary";
    public static final String RESULT_DIRECTORY_NAME = "Results";
    public static final String DATE_FORMAT_TEMPLATE = "yyyyMMdd_HHmmss";
    public static final String IMAGE_NAME_TEMPLATE = "IMG%s_";
    public static final String JPG_EXTENSION = ".jpg";
}
