package com.cs.reportmaker
import android.webkit.WebChromeClient
import android.net.Uri
import android.webkit.ValueCallback
import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import android.webkit.JavascriptInterface
import android.widget.Toast
import android.print.PrintAttributes
import android.print.PrintManager
import android.content.Context
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    lateinit var myWebView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Option 1: Create WebView directly via code (Simplest if it's a 1-page app)
        myWebView = WebView(this)
        setContentView(myWebView)

        // Option 2: If you have a WebView defined inside your activity_main.xml layout
        // setContentView(R.layout.activity_main)
        // myWebView = findViewById(R.id.webview) // Make sure 'webview' matches the ID in your XML

        // Enable JavaScript and DOM Storage (DOM Storage is required for your app's caching to work!)
        myWebView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
        // THIS IS THE MISSING PIECE FOR YOUR "LOAD" BUTTON
        myWebView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
            }
        }

        // Connect the Bridge to the HTML
        // This links the Java class we made to the "AndroidBridge" name in your HTML
        myWebView.addJavascriptInterface(AndroidBridge(this), "AndroidBridge")

        // Load the HTML file from your assets folder
        // (Make sure to change "report_maker.html" to "index.html" if you used that name instead!)
        myWebView.loadUrl("file:///android_asset/index.html")
    }
}
// This is the "Bridge" class that talks to your HTML
class AndroidBridge(private val context: Context) {

    @JavascriptInterface
    fun saveBackup(data: String, fileName: String) {
        try {
            // Defines the folder: Android/data/com.cs.reportmaker/files/Documents/
            val file = File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS), fileName)
            FileOutputStream(file).use { output ->
                output.write(data.toByteArray())
            }
            // Show a small popup to confirm the save
            Toast.makeText(context, "Saved to: " + file.absolutePath, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving file: " + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun printPDF() {
        val activity = context as MainActivity
        activity.runOnUiThread {
            // We use the activity's myWebView property directly
            val webView = activity.myWebView

            val printManager = activity.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val printAdapter = webView.createPrintDocumentAdapter("Report_Job")

            printManager.print("Report_Document", printAdapter, PrintAttributes.Builder().build())
        }
    }