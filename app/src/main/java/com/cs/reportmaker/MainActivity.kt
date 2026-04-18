package com.cs.reportmaker

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var myWebView: WebView
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var pendingBackupData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myWebView = findViewById(R.id.webview)

        val settings = myWebView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true

        // --- NEW LOGIC: ALWAYS USE CACHE BY DEFAULT ---
        // This stops the app from updating automatically every time it opens
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

        myWebView.webViewClient = WebViewClient()
        myWebView.addJavascriptInterface(WebAppInterface(), "AndroidBridge")

        myWebView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@MainActivity.filePathCallback = filePathCallback
                val intent = fileChooserParams?.createIntent()
                startActivityForResult(intent!!, 1)
                return true
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (myWebView.canGoBack()) myWebView.goBack() else finish()
            }
        })

        // REPLACE WITH YOUR GITHUB URL
        myWebView.loadUrl("https://allmodeai.github.io/Report-m-app-updates/")
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    inner class WebAppInterface {

        // --- NEW: THE MANUAL UPDATE TRIGGER ---
        @JavascriptInterface
        fun forceUpdate() {
            runOnUiThread {
                if (isNetworkAvailable()) {
                    Toast.makeText(this@MainActivity, "Downloading updates...", Toast.LENGTH_SHORT).show()
                    // Clear the old saved version and reload from the internet
                    myWebView.clearCache(true)
                    myWebView.reload()
                } else {
                    Toast.makeText(this@MainActivity, "No internet! Cannot update.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        @JavascriptInterface
        fun printPDF() {
            runOnUiThread {
                val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = myWebView.createPrintDocumentAdapter("StudentReport")
                printManager.print("Student_Report", printAdapter, PrintAttributes.Builder().build())
            }
        }

        @JavascriptInterface
        fun saveBackup(jsonData: String, filename: String) {
            pendingBackupData = jsonData
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
                putExtra(Intent.EXTRA_TITLE, filename)
            }
            startActivityForResult(intent, 2)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            val results = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
            filePathCallback?.onReceiveValue(results)
            filePathCallback = null
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        pendingBackupData?.let { outputStream.write(it.toByteArray()) }
                    }
                    Toast.makeText(this, "Backup Saved!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error saving backup", Toast.LENGTH_SHORT).show()
                }
            }
            pendingBackupData = null
        }
    }
}