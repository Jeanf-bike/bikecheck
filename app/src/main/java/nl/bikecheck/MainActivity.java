package nl.bikecheck;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;

/**
 * BikeCheck: toont web/bikecheck.html (meegebakken in de app) schermvullend.
 * De pagina wordt geserveerd vanaf een https-adres, zodat locatie (voor het weer)
 * en opslag (foto's) gewoon werken.
 */
public class MainActivity extends Activity {

    private static final String HOST = "appassets.androidplatform.net";
    private static final String START_URL = "https://" + HOST + "/bikecheck.html";
    private static final int VERZOEK_LOCATIE = 1;
    private static final int VERZOEK_FOTO = 2;

    private WebView web;
    private ValueCallback<Uri[]> fotoCallback;
    private String locatieOrigin;
    private GeolocationPermissions.Callback locatieCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setGeolocationEnabled(true);
        s.setAllowFileAccess(false);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest req) {
                Uri u = req.getUrl();
                if (!HOST.equals(u.getHost())) return null; // weer-API e.d. gewoon via internet
                String pad = u.getPath() == null ? "" : u.getPath().replaceFirst("^/", "");
                try {
                    InputStream in = getAssets().open(pad);
                    return new WebResourceResponse(mimeType(pad), "UTF-8", in);
                } catch (IOException e) {
                    return new WebResourceResponse("text/plain", "UTF-8", 404, "Niet gevonden", null, null);
                }
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback cb) {
                if (heeftLocatieRecht()) {
                    cb.invoke(origin, true, false);
                } else {
                    locatieOrigin = origin;
                    locatieCallback = cb;
                    requestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, VERZOEK_LOCATIE);
                }
            }

            @Override
            public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (fotoCallback != null) fotoCallback.onReceiveValue(null);
                fotoCallback = cb;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                try {
                    startActivityForResult(Intent.createChooser(i, "Kies een foto"), VERZOEK_FOTO);
                } catch (Exception e) {
                    fotoCallback = null;
                    return false;
                }
                return true;
            }
        });

        if (savedInstanceState != null) web.restoreState(savedInstanceState);
        else web.loadUrl(START_URL);
    }

    private boolean heeftLocatieRecht() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        if (code == VERZOEK_LOCATIE && locatieCallback != null) {
            locatieCallback.invoke(locatieOrigin, heeftLocatieRecht(), false);
            locatieCallback = null;
        }
    }

    @Override
    protected void onActivityResult(int code, int result, Intent data) {
        if (code == VERZOEK_FOTO && fotoCallback != null) {
            Uri[] uris = null;
            if (result == RESULT_OK && data != null && data.getData() != null) {
                uris = new Uri[]{data.getData()};
            }
            fotoCallback.onReceiveValue(uris);
            fotoCallback = null;
        }
    }

    // Terugknop: van checklist naar overzicht; vanaf overzicht de app sluiten
    @Override
    public void onBackPressed() {
        web.evaluateJavascript(
                "(function(){var c=document.getElementById('check-scherm');" +
                "if(c&&!c.classList.contains('verborgen')){toonHoofd();return 'terug';}return 'sluit';})()",
                r -> { if (r == null || r.contains("sluit")) finish(); });
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    private static String mimeType(String pad) {
        if (pad.endsWith(".html")) return "text/html";
        if (pad.endsWith(".js")) return "application/javascript";
        if (pad.endsWith(".css")) return "text/css";
        if (pad.endsWith(".png")) return "image/png";
        if (pad.endsWith(".jpg") || pad.endsWith(".jpeg")) return "image/jpeg";
        if (pad.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }
}
