package com.cs.reportmaker
import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var myWebView: WebView

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

        // Connect the Bridge to the HTML
        // This links the Java class we made to the "AndroidBridge" name in your HTML
        myWebView.addJavascriptInterface(AndroidBridge(this), "AndroidBridge")

        // Load the HTML file from your assets folder
        // (Make sure to change "report_maker.html" to "index.html" if you used that name instead!)
        myWebView.loadUrl("file:///android_asset/index.html")
    }
}