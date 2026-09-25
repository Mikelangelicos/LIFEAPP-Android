package app.lifeapp.mobile;

import android.app.Dialog;
import android.app.NativeActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class LifeAppActivity extends NativeActivity {

    private static final String ASK_UI_URL =
            "https://rtggrizfrkplropltwdc.supabase.co/functions/v1/ask-life-web";

    private static final String ASK_BASE_URL =
            "https://rtggrizfrkplropltwdc.supabase.co/";

    private static final String ASK_LOADER =
            "<!doctype html>" +
            "<html><head>" +
            "<meta charset='utf-8'>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<style>" +
            "html,body{margin:0;height:100%;background:#0b0d14;color:white;" +
            "font-family:system-ui}" +
            "body{display:grid;place-items:center}" +
            "</style>" +
            "</head><body>" +
            "<div>Cargando ASK LIFE IA…</div>" +
            "<script>" +
            "fetch('" + ASK_UI_URL + "',{cache:'no-store'})" +
            ".then(function(r){return r.text();})" +
            ".then(function(h){" +
            "document.open();" +
            "document.write(h);" +
            "document.close();" +
            "})" +
            ".catch(function(){" +
            "document.body.innerHTML='<div>No se pudo cargar ASK LIFE IA.</div>';" +
            "});" +
            "</script>" +
            "</body></html>";

    private Button contextualButton;

    private PopupWindow askTabPopup;

    private Dialog askDialog;

    private WebView askWebView;

    @Override
    protected void onCreate(Bundle state) {

        super.onCreate(state);

        contextualButton = new Button(this);

        contextualButton.setText("⚙");
        contextualButton.setTextSize(22f);
        contextualButton.setVisibility(View.GONE);

        contextualButton.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                this,
                                LanguageSettingsActivity.class
                        )
                )
        );

        FrameLayout.LayoutParams buttonParams =
                new FrameLayout.LayoutParams(
                        dp(58),
                        dp(58),
                        Gravity.TOP | Gravity.END
                );

        buttonParams.setMargins(
                0,
                dp(18),
                dp(14),
                0
        );

        addContentView(
                contextualButton,
                buttonParams
        );

        /*
         * Esperamos a que la interfaz nativa
         * tenga ya sus dimensiones.
         */
        getWindow()
                .getDecorView()
                .postDelayed(
                        this::showAskLifeTab,
                        700
                );
    }

    /**
     * Coloca una ventana independiente encima
     * del tercer botón de LIFEAPP.
     */
    private void showAskLifeTab() {

        if (isFinishing()) {
            return;
        }

        if (
                askDialog != null &&
                askDialog.isShowing()
        ) {
            return;
        }

        if (
                askTabPopup != null &&
                askTabPopup.isShowing()
        ) {
            return;
        }

        View decor =
                getWindow()
                        .getDecorView();

        int width =
                decor.getWidth();

        if (width <= 0) {

            decor.postDelayed(
                    this::showAskLifeTab,
                    300
            );

            return;
        }

        int tabWidth =
                width / 4;

        FrameLayout root =
                new FrameLayout(this);

        root.setBackgroundColor(
                Color.TRANSPARENT
        );

        root.setClickable(true);
        root.setFocusable(false);

        /*
         * Texto que sustituye visualmente
         * al ASK LIFE antiguo.
         */
        TextView label =
                new TextView(this);

        label.setText(
                "ASK LIFE IA"
        );

        label.setTextSize(
                11f
        );

        label.setGravity(
                Gravity.CENTER
        );

        label.setTextColor(
                Color.rgb(
                        55,
                        82,
                        140
                )
        );

        label.setBackgroundColor(
                Color.rgb(
                        251,
                        252,
                        255
                )
        );

        FrameLayout.LayoutParams labelParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        dp(27),
                        Gravity.BOTTOM
                );

        labelParams.bottomMargin =
                dp(3);

        root.addView(
                label,
                labelParams
        );

        /*
         * Toda la zona del tercer botón
         * abre ASK LIFE IA.
         */
        root.setOnClickListener(v ->
                openAskLife()
        );

        askTabPopup =
                new PopupWindow(
                        root,
                        tabWidth,
                        dp(86),
                        false
                );

        askTabPopup.setBackgroundDrawable(
                new ColorDrawable(
                        Color.TRANSPARENT
                )
        );

        askTabPopup.setTouchable(
                true
        );

        askTabPopup.setOutsideTouchable(
                false
        );

        askTabPopup.setClippingEnabled(
                false
        );

        askTabPopup.setElevation(
                dp(10)
        );

        /*
         * Los otros botones de LIFEAPP
         * siguen funcionando normalmente.
         */
        if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.Q
        ) {

            askTabPopup.setTouchModal(
                    false
            );
        }

        /*
         * El tercer botón comienza justo
         * en la mitad del ancho.
         */
        askTabPopup.showAtLocation(
                decor,
                Gravity.BOTTOM | Gravity.START,
                width / 2,
                0
        );
    }

    /**
     * Abre ASK LIFE IA en una ventana
     * completamente independiente.
     */
    private void openAskLife() {

        if (
                askTabPopup != null &&
                askTabPopup.isShowing()
        ) {

            askTabPopup.dismiss();
        }

        askDialog =
                new Dialog(
                        this,
                        android.R.style.Theme_DeviceDefault_NoActionBar
                );

        askWebView =
                new WebView(this);

        askWebView.setBackgroundColor(
                Color.rgb(
                        11,
                        13,
                        20
                )
        );

        askWebView.setWebViewClient(
                new WebViewClient()
        );

        WebSettings settings =
                askWebView.getSettings();

        settings.setJavaScriptEnabled(
                true
        );

        settings.setDomStorageEnabled(
                true
        );

        settings.setAllowFileAccess(
                false
        );

        settings.setAllowContentAccess(
                false
        );

        /*
         * Permite al botón X de ASK LIFE IA
         * volver a LIFEAPP.
         */
        askWebView.addJavascriptInterface(
                new AskLifeBridge(),
                "LifeApp"
        );

        askDialog.setContentView(
                askWebView
        );

        askDialog.setOnDismissListener(dialog -> {

            if (askWebView != null) {

                askWebView.removeJavascriptInterface(
                        "LifeApp"
                );

                askWebView.destroy();

                askWebView = null;
            }

            getWindow()
                    .getDecorView()
                    .postDelayed(
                            this::showAskLifeTab,
                            200
                    );
        });

        /*
         * Botón Atrás:
         * vuelve a LIFEAPP.
         */
        askDialog.setOnKeyListener(
                (dialog, keyCode, event) -> {

                    if (
                            keyCode == KeyEvent.KEYCODE_BACK &&
                            event.getAction() ==
                                    KeyEvent.ACTION_UP
                    ) {

                        dialog.dismiss();

                        return true;
                    }

                    return false;
                }
        );

        askDialog.show();

        Window window =
                askDialog.getWindow();

        if (window != null) {

            window.setBackgroundDrawable(
                    new ColorDrawable(
                            Color.rgb(
                                    11,
                                    13,
                                    20
                            )
                    )
            );

            window.setStatusBarColor(
                    Color.rgb(
                            11,
                            13,
                            20
                    )
            );

            window.setNavigationBarColor(
                    Color.rgb(
                            11,
                            13,
                            20
                    )
            );

            window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
            );
        }

        /*
         * Cargador local:
         * obtiene desde Supabase la interfaz
         * actual de ASK LIFE IA.
         */
        askWebView.loadDataWithBaseURL(
                ASK_BASE_URL,
                ASK_LOADER,
                "text/html",
                "UTF-8",
                null
        );
    }

    private final class AskLifeBridge {

        @JavascriptInterface
        public void closeAskLife() {

            runOnUiThread(() -> {

                if (
                        askDialog != null &&
                        askDialog.isShowing()
                ) {

                    askDialog.dismiss();
                }
            });
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        getWindow()
                .getDecorView()
                .postDelayed(
                        this::showAskLifeTab,
                        300
                );
    }

    @Override
    protected void onPause() {

        if (
                askTabPopup != null &&
                askTabPopup.isShowing()
        ) {

            askTabPopup.dismiss();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if (
                askTabPopup != null &&
                askTabPopup.isShowing()
        ) {

            askTabPopup.dismiss();
        }

        if (
                askDialog != null &&
                askDialog.isShowing()
        ) {

            askDialog.dismiss();
        }

        super.onDestroy();
    }

    private int dp(int value) {

        return Math.round(
                value *
                getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}
