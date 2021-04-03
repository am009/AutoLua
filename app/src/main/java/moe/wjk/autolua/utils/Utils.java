package moe.wjk.autolua.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import moe.wjk.autolua.MyAccessibilityService;

public class Utils {

    private static final String TAG = "Utils";
    public static final String CODE_FILENAME = "code.lua";
    public static final String ERR_LOG_FILENAME = "error.log";

    static public void startService(Context context, Class<?> cls, String action) {
        Intent reportIntent = new Intent(context, cls);
        reportIntent.setAction(action);
        context.startService(reportIntent);
    }

    public static boolean saveFile(Context context, String filename, byte[] content) {
        return saveFile(context, filename, content, false);
    }

    public static String readFile(Context context, String filename) {
        FileInputStream fis;
        try {
            fis = context.openFileInput(filename);
        } catch (FileNotFoundException e) {
            Toast.makeText(context, "File: " + filename + " not found", Toast.LENGTH_LONG).show();
            return null;
        }
        InputStreamReader inputStreamReader =
                new InputStreamReader(fis, StandardCharsets.UTF_8);

        StringBuilder stringBuilder = new StringBuilder();
        String contents;
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {
            Toast.makeText(context, "IOException", Toast.LENGTH_LONG).show();
            Log.w(TAG, "readFile: IOException", e);
        } finally {
            contents = stringBuilder.toString();
        }
        return contents;
    }

    public static boolean saveFile(Context context, String filename, byte[] content, boolean isAppend) {
        int flag = Context.MODE_PRIVATE;
        if (isAppend) {
            flag |= Context.MODE_APPEND;
        }
        try (FileOutputStream fos = context.openFileOutput(filename, flag)) {
            fos.write(content);
        } catch (IOException e) {
            Toast.makeText(context, "Save Failed", Toast.LENGTH_LONG).show();
            Log.w(TAG, "saveFile: IOException", e);
            return false;
        }
        return true;
    }

    public static void sleep_ms(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteFile(Context context, String filename) {
        return context.deleteFile(filename);
    }

    public static boolean emptyFile(Context context, String filename) throws IOException {
        File f =new File(context.getFilesDir() + File.separator +  filename);
        if (f.exists()) {
            if(!f.delete()) {
                return false;
            }
        }
        return f.createNewFile();
    }

    public static String getTimeStamp() {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM ,DateFormat.MEDIUM);
        Date currentTime = Calendar.getInstance().getTime();
        return df.format(currentTime);
    }

}
