package com.luoyang.location_h5_vs_android

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup.LayoutParams
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.luoyang.location_h5_vs_android.ui.theme.Location_h5_vs_androidTheme


class MainActivity : ComponentActivity() {
    private val webChromeClient = object : WebChromeClient() {
        override fun onGeolocationPermissionsShowPrompt(
            origin: String?,
            callback: GeolocationPermissions.Callback?
        ) {
            super.onGeolocationPermissionsShowPrompt(origin, callback)
            callback!!.invoke(origin, true, false)
        }
    }

    private val url = "file:///android_asset/location-h5.html"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            permissionRequest()
            Location_h5_vs_androidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LWebView(
                        url = url,
                        webChromeClient = webChromeClient,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@SuppressLint("JavascriptInterface", "SetJavaScriptEnabled")
@Composable
fun LWebView(
    url: String,
    webChromeClient: WebChromeClient,
    modifier: Modifier,
) {
    AndroidView(factory = { context ->
        WebView.setWebContentsDebuggingEnabled(true)
        WebView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            settings.javaScriptEnabled = true
            settings.setGeolocationEnabled(true)
            settings.domStorageEnabled = true
            setWebChromeClient(webChromeClient)
            val webAppInterface = WebAppInterface(context, this)
            addJavascriptInterface(webAppInterface, webAppInterface.name)
            loadUrl(url)
        }
    }, modifier = modifier)
}

@Preview
@Composable
fun LWebViewPreview() {
    Location_h5_vs_androidTheme {
        LWebView(
            url = "file:///android_asset/location-h5.html",
            webChromeClient = object : WebChromeClient() {
                override fun onGeolocationPermissionsShowPrompt(
                    origin: String?,
                    callback: GeolocationPermissions.Callback?
                ) {
                    super.onGeolocationPermissionsShowPrompt(origin, callback)
                    callback!!.invoke(origin, true, false)
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun permissionRequest() {
    // 基于 LocalComposition 获取 Context
    val context = LocalContext.current

    // 基于 LocalLifecycleOwner 获取 Lifecycle
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // 定义需要动态获取的 Permission 类型
    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { result ->
            val fineLocationGranted: Boolean = result.getOrDefault(
                Manifest.permission.ACCESS_FINE_LOCATION, false
            )
            val coarseLocationGranted: Boolean = result.getOrDefault(
                Manifest.permission.ACCESS_COARSE_LOCATION, false
            )
            if (fineLocationGranted != null && fineLocationGranted) {
                // Precise location access granted.
            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                // Only approximate location access granted.
            } else {
                // No location access granted.
            }
        }
    )
    val lifecycleEventObserver = remember {
        LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                for (permission in permissions) {
                    if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                        launcher.launch(permissions)
                        return@LifecycleEventObserver
                    }
                }
            }
        }
    }
    // 当 Lifecycle 或者 LifecycleObserver 变化时注册回调，注意 onDispose 中的注销处理避免泄露
    DisposableEffect(lifecycle, lifecycleEventObserver) {
        lifecycle.addObserver(lifecycleEventObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleEventObserver)
        }
    }

}