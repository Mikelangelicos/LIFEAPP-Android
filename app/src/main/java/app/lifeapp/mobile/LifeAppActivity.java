package app.lifeapp.mobile;

import android.app.Dialog;
import android.app.NativeActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
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
            "<html>" +
            "<head>" +
            "<meta charset='utf-8'>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<style>" +
            "html,body{" +
            "margin:0;" +
            "height:100%;" +
            "background:#0b0d14;" +
            "color:white;" +
            "font-family:system-ui;" +
            "}" +
            "body{" +
            "display:grid;" +
            "place-items:center;" +
            "}" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div>Cargando ASK LIFE IA…</div>" +
            "<script>" +
            "fetch('" + ASK_UI_URL + "',{cache:'no-store'})" +
            ".then(function(r){return r.text();})" +
            ".then(function(html){" +
            "document.open();" +
            "document.write(html);" +
            "document.close();" +
            "})" +
            ".catch(function(){" +
            "document.body.innerHTML='<div>No se pudo cargar ASK LIFE IA.</div>';" +
            "});" +
            "</script>" +
            "</body>" +
            "</html>";

    private Button contextualButton;

    private PopupWindow askTabPopup;

    private Dialog askDialog;

    private WebView askWebView;

    @Override
    protected void onCreate(Bundle state) {

        super.onCreate(state);

        buildContextButton();

        /*
         * Dejamos que NativeActivity termine
         * primero de crear su Surface.
         */
        getWindow()
                .getDecorView()
                .postDelayed(
                        this::showAskLifeTab,
                        1200
                );
    }

    private void buildContextButton() {

        contextualButton =
                new Button(this);

        contextualButton.setText("⚙");

        contextualButton.setTextSize(
                22f
        );

        contextualButton.setVisibility(
                View.GONE
        );

        contextualButton.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                this,
                                LanguageSettingsActivity.class
                        )
                )
        );

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        dp(58),
                        dp(58),
                        Gravity.TOP | Gravity.END
                );

        params.setMargins(
                0,
                dp(18),
                dp(14),
                0
        );

        addContentView(
                contextualButton,
                params
        );
    }

    /**
     * Altura de la barra de navegación
     * del sistema Android.
     */
    private int getNavigationBarInset() {

        View decor =
                getWindow()
                        .getDecorView();

        WindowInsets insets =
                decor.getRootWindowInsets();

        if (insets == null) {
            return 0;
        }

        if (
                Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.R
        ) {

            return insets
                    .getInsets(
                            WindowInsets.Type.navigationBars()
                    )
                    .bottom;
        }

        return insets.getStableInsetBottom();
    }

    /**
     * Crea una ventana Android independiente
     * justo encima del tercer botón inferior.
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

        int height =
                decor.getHeight();

        if (
                width <= 0 ||
                height <= 0
        ) {

            decor.postDelayed(
                    this::showAskLifeTab,
                    400
            );

            return;
        }

        int navigationInset =
                getNavigationBarInset();

        /*
         * Cada pestaña ocupa 1/4
         * del ancho de LIFEAPP.
         */
        int tabWidth =
                width / 4;

        /*
         * Altura aproximada real
         * de la barra de LIFEAPP.
         */
        int tabHeight =
                Math.max(
                        dp(52),
                        Math.round(
                                height * 0.055f
                        )
                );

        /*
         * Borde inferior útil de LIFEAPP:
         * justo encima de la navegación Android.
         */
        int appBottom =
                height - navigationInset;

        /*
         * Posición TOP absoluta.
         *
         * Ya NO utilizamos Gravity.BOTTOM.
         */
        int tabTop =
                appBottom -
                tabHeight -
                dp(2);

        if (tabTop < 0) {
            tabTop = 0;
        }

        FrameLayout root =
                new FrameLayout(this);

        root.setClickable(true);

        root.setFocusable(false);

        /*
         * Fondo blanco ligeramente azulado,
         * igual a la barra inferior.
         */
        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                Color.rgb(
                        249,
                        251,
                        255
                )
        );

        background.setCornerRadius(
                dp(22)
        );

        root.setBackground(
                background
        );

        /*
         * Texto inequívoco.
         *
         * Si esto está bien colocado,
         * veremos ASK LIFE IA exactamente
         * encima del ASK LIFE antiguo.
         */
        TextView label =
                new TextView(this);

        label.setText(
                "✦\nASK LIFE IA"
        );

        label.setGravity(
                Gravity.CENTER
        );

        label.setTextSize(
                11f
        );

        label.setTextColor(
                Color.rgb(
                        43,
                        78,
                        145
                )
        );

        label.setClickable(false);

        root.addView(
                label,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        /*
         * El Popup completo es clicable.
         */
        root.setOnClickListener(v ->
                openAskLife()
        );

        askTabPopup =
                new PopupWindow(
                        root,
                        tabWidth,
                        tabHeight,
                        false
                );

        askTabPopup.setTouchable(
                true
        );

        askTabPopup.setFocusable(
                false
        );

        askTabPopup.setOutsideTouchable(
                false
        );

        askTabPopup.setClippingEnabled(
                false
        );

        askTabPopup.setBackgroundDrawable(
                new ColorDrawable(
                        Color.TRANSPARENT
                )
        );

        askTabPopup.setElevation(
                dp(20)
        );

        /*
         * Fundamental:
         *
         * coordenadas absolutas desde ARRIBA.
         *
         * X = tercer cuarto.
         * Y = justo encima de la navegación Android.
         */
        askTabPopup.showAtLocation(
                decor,
                Gravity.TOP | Gravity.START,
                width / 2,
                tabTop
        );
    }

    /**
     * Abre ASK LIFE IA real.
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

        askWebView.addJavascriptInterface(
                new AskLifeBridge(),
                "LifeApp"
        );

        askDialog.setContentView(
                askWebView
        );

        askDialog.setOnKeyListener(
                (dialog, keyCode, event) -> {

                    if (
                            keyCode ==
                                    KeyEvent.KEYCODE_BACK &&
                            event.getAction() ==
                                    KeyEvent.ACTION_UP
                    ) {

                        dialog.dismiss();

                        return true;
                    }

                    return false;
                }
        );

        askDialog.setOnDismissListener(
                dialog -> {

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
                                    300
                            );
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
                        700
                );
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

        if (askWebView != null) {

            askWebView.removeJavascriptInterface(
                    "LifeApp"
            );

            askWebView.destroy();
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
