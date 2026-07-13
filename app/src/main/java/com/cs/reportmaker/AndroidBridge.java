package com.cs.reportmaker;
import android.content.Context;
import android.os.Environment;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AndroidBridge {
    Context mContext;

    // Instantiate the interface and set the context
    public AndroidBridge(Context c) {
        mContext = c;
    }

    // This annotation allows your HTML JavaScript to trigger this Android Java function
    @JavascriptInterface
    public void saveBackup(String jsonData, String fileName) {
        try {
            // Target the App's dedicated Documents folder (Bypasses Android 11+ permission issues)
            File directory = mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Create the file target
            File file = new File(directory, fileName);

            // The 'false' parameter here is the magic trick: It tells Android to OVERWRITE, not append.
            FileWriter writer = new FileWriter(file, false);
            writer.write(jsonData);
            writer.flush();
            writer.close();

            // Show a small popup on the Android phone confirming the save
            Toast.makeText(mContext, "Saved: " + fileName, Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Save Failed", Toast.LENGTH_SHORT).show();
        }
    }

    @JavascriptInterface
    public void printPDF() {
        // Add your Android PDF printing logic here if you have it implemented
        Toast.makeText(mContext, "Printing...", Toast.LENGTH_SHORT).show();
    }
}