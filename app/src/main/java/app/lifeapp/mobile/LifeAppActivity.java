package app.lifeapp.mobile;

import android.app.Dialog;
import android.app.NativeActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
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

    /*
     * IMPORTANTE:
     *
     * Ya NO contiene un segundo botón.
     *
     * Es únicamente una capa transparente colocada
     * encima del botón ASK LIFE original.
     *
     * Dentro solo sustituimos visualmente el texto.
     */
    private PopupWindow askHomePopup;

    /*
     * Oculta la antigua pestaña pequeña ASK LIFE
     * del menú inferior.
     */
    private PopupWindow askBottomBlocker;

    private Dialog askDialog;

    private WebView askWebView;

    private boolean isHomeSection = true;

    @Override
    protected void onCreate(Bundle state) {

        super.onCreate(state);

        buildContextButton();

        getWindow()
                .getDecorView()
                .post(() -> {

                    applyNativeContentInsets();

                    showAskControls();
                });
    }

    private void buildContextButton() {

        contextualButton =
                new Button(this);

        contextualButton.setText("⚙");

        contextualButton.setTextSize(22f);

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

    private int getStatusBarInset() {

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
                            WindowInsets.Type.statusBars()
                    )
                    .top;
        }

        return insets.getStableInsetTop();
    }

    private void applyNativeContentInsets() {

        View content =
                findViewById(
                        android.R.id.content
                );

        if (content == null) {
            return;
        }

        int top =
                getStatusBarInset();

        int bottom =
                getNavigationBarInset();

        if (
                content.getPaddingTop() != top ||
                content.getPaddingBottom() != bottom
        ) {

            content.setPadding(
                    0,
                    top,
                    0,
                    bottom
            );
        }
    }

    private void showAskControls() {

        applyNativeContentInsets();

        if (isFinishing()) {
            return;
        }

        if (
                askDialog != null &&
                askDialog.isShowing()
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
                    this::showAskControls,
                    120
            );

            return;
        }

        hideBottomAskTab(
                decor,
                width,
                height
        );

        if (isHomeSection) {

            showAskLifeOriginalReplacement(
                    decor,
                    width,
                    height
            );

        } else {

            hideAskLifeOriginalReplacement();
        }
    }

    /**
     * NO crea otro botón.
     *
     * Usa el botón ASK LIFE original como base.
     *
     * Esta capa:
     *
     * 1. deja visibles la forma, sombra y degradado originales;
     * 2. tapa únicamente el texto ASK LIFE;
     * 3. escribe ASK LIFE IA;
     * 4. captura el toque para abrir nuestra IA.
     */
    private void showAskLifeOriginalReplacement(
            View decor,
            int width,
            int height
    ) {

        if (
                askHomePopup != null &&
                askHomePopup.isShowing()
        ) {
            return;
        }

        /*
         * Medidas aproximadas del botón ORIGINAL.
         *
         * No dibujamos nada alrededor.
         */
        int buttonWidth =
                Math.round(
                        width * 0.74f
                );

        int buttonHeight =
                Math.max(
                        dp(54),
                        Math.round(
                                height * 0.038f
                        )
                );

        int left =
                Math.round(
                        width * 0.13f
                );

        int navigationInset =
                getNavigationBarInset();

        int bottomMenuHeight =
                Math.max(
                        dp(52),
                        Math.round(
                                height * 0.055f
                        )
                );

        int appBottom =
                height -
                navigationInset;

        /*
         * Posición del ASK LIFE original.
         */
        int top =
                appBottom -
                bottomMenuHeight -
                buttonHeight -
                dp(40);

        /*
         * Capa TOTALMENTE TRANSPARENTE.
         *
         * Ya no hay rectángulo blanco.
         * Ya no hay segundo botón.
         */
        FrameLayout touchLayer =
                new FrameLayout(this);

        touchLayer.setBackgroundColor(
                Color.TRANSPARENT
        );

        touchLayer.setClickable(true);

        touchLayer.setFocusable(false);

        touchLayer.setOnClickListener(v ->
                openAskLife()
        );

        /*
         * Únicamente sustituimos la zona central
         * donde aparece el texto ASK LIFE.
         *
         * Como el botón original tiene un degradado
         * horizontal, reproducimos ese mismo degradado
         * solamente detrás del texto.
         *
         * No tiene esquinas redondeadas.
         * Por tanto NO parece otro botón.
         */
        TextView newLabel =
                new TextView(this);

        newLabel.setText(
                "ASK LIFE IA"
        );

        newLabel.setGravity(
                Gravity.CENTER
        );

        newLabel.setTextColor(
                Color.WHITE
        );

        newLabel.setTextSize(
                22f
        );

        newLabel.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        /*
         * Misma transición horizontal del botón.
         */
        GradientDrawable textBackground =
                new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[] {
                                Color.rgb(
                                        35,
                                        169,
                                        245
                                ),
                                Color.rgb(
                                        108,
                                        82,
                                        226
                                ),
                                Color.rgb(
                                        239,
                                        44,
                                        155
                                )
                        }
                );

        /*
         * SIN radio.
         *
         * Es solamente una banda integrada
         * dentro del botón original.
         */
        textBackground.setCornerRadius(0);

        newLabel.setBackground(
                textBackground
        );

        /*
         * Esta banda tapa el antiguo ASK LIFE
         * pero deja completamente intactos:
         *
         * - bordes
         * - forma
         * - sombra
         * - posición
         *
         * del botón original.
         */
        FrameLayout.LayoutParams labelParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        dp(34),
                        Gravity.CENTER
                );

        touchLayer.addView(
                newLabel,
                labelParams
        );

        /*
         * El TextView también abre ASK LIFE IA
         * por si el toque cae directamente sobre él.
         */
        newLabel.setClickable(true);

        newLabel.setOnClickListener(v ->
                openAskLife()
        );

        askHomePopup =
                new PopupWindow(
                        touchLayer,
                        buttonWidth,
                        buttonHeight,
                        false
                );

        askHomePopup.setTouchable(
                true
        );

        askHomePopup.setFocusable(
                false
        );

        askHomePopup.setOutsideTouchable(
                false
        );

        askHomePopup.setClippingEnabled(
                false
        );

        askHomePopup.setBackgroundDrawable(
                new ColorDrawable(
                        Color.TRANSPARENT
                )
        );

        /*
         * CERO elevación.
         *
         * La sombra que vemos es la del botón
         * original de LIFEAPP.
         */
        askHomePopup.setElevation(0);

        askHomePopup.showAtLocation(
                decor,
                Gravity.TOP | Gravity.START,
                left,
                top
        );
    }

    private void hideAskLifeOriginalReplacement() {

        if (
                askHomePopup != null &&
                askHomePopup.isShowing()
        ) {

            askHomePopup.dismiss();
        }

        askHomePopup =
                null;
    }

    /**
     * Oculta únicamente la antigua pestaña
     * pequeña ASK LIFE de la barra inferior.
     */
    private void hideBottomAskTab(
            View decor,
            int width,
            int height
    ) {

        if (
                askBottomBlocker != null &&
                askBottomBlocker.isShowing()
        ) {
            return;
        }

        int navigationInset =
                getNavigationBarInset();

        int tabWidth =
                width / 4;

        int tabHeight =
                Math.max(
                        dp(52),
                        Math.round(
                                height * 0.055f
                        )
                );

        int appBottom =
                height -
                navigationInset;

        int tabTop =
                appBottom -
                tabHeight -
                dp(2);

        if (tabTop < 0) {
            tabTop = 0;
        }

        FrameLayout blank =
                new FrameLayout(this);

        blank.setClickable(true);

        blank.setFocusable(false);

        blank.setBackgroundColor(
                Color.rgb(
                        249,
                        251,
                        255
                )
        );

        blank.setOnClickListener(v -> {
            // ASK LIFE inferior antiguo desactivado.
        });

        askBottomBlocker =
                new PopupWindow(
                        blank,
                        tabWidth,
                        tabHeight,
                        false
                );

        askBottomBlocker.setTouchable(true);

        askBottomBlocker.setFocusable(false);

        askBottomBlocker.setOutsideTouchable(false);

        askBottomBlocker.setClippingEnabled(false);

        askBottomBlocker.setBackgroundDrawable(
                new ColorDrawable(
                        Color.TRANSPARENT
                )
        );

        askBottomBlocker.setElevation(
                dp(10)
        );

        askBottomBlocker.showAtLocation(
                decor,
                Gravity.TOP | Gravity.START,
                width / 2,
                tabTop
        );
    }

    @Override
    public boolean dispatchTouchEvent(
            MotionEvent event
    ) {

        if (
                event.getAction() ==
                MotionEvent.ACTION_UP &&
                (
                        askDialog == null ||
                        !askDialog.isShowing()
                )
        ) {

            View decor =
                    getWindow()
                            .getDecorView();

            int width =
                    decor.getWidth();

            int height =
                    decor.getHeight();

            if (
                    width > 0 &&
                    height > 0
            ) {

                int navigationInset =
                        getNavigationBarInset();

                int tabHeight =
                        Math.max(
                                dp(52),
                                Math.round(
                                        height * 0.055f
                                )
                        );

                int appBottom =
                        height -
                        navigationInset;

                int tabTop =
                        appBottom -
                        tabHeight -
                        dp(2);

                float x =
                        event.getX();

                float y =
                        event.getY();

                if (
                        y >= tabTop &&
                        y <= appBottom
                ) {

                    /*
                     * INICIO
                     */
                    if (
                            x <
                            width * 0.25f
                    ) {

                        isHomeSection =
                                true;

                        decor.postDelayed(
                                this::showAskControls,
                                80
                        );

                    /*
                     * EXPLORAR
                     */
                    } else if (
                            x <
                            width * 0.50f
                    ) {

                        isHomeSection =
                                false;

                        hideAskLifeOriginalReplacement();

                    /*
                     * PERFIL
                     */
                    } else if (
                            x >=
                            width * 0.75f
                    ) {

                        isHomeSection =
                                false;

                        hideAskLifeOriginalReplacement();
                    }
                }
            }
        }

        return super.dispatchTouchEvent(
                event
        );
    }

    /**
     * Solo se usa al destruir completamente
     * la Activity.
     */
    private void dismissAskControlsCompletely() {

        hideAskLifeOriginalReplacement();

        if (
                askBottomBlocker != null &&
                askBottomBlocker.isShowing()
        ) {

            askBottomBlocker.dismiss();
        }

        askBottomBlocker =
                null;
    }

    /**
     * Abre ASK LIFE IA.
     *
     * NO retiramos la capa del botón.
     *
     * El diálogo se muestra encima y,
     * cuando se cierra, el botón ya estaba ahí.
     *
     * Por tanto no existe ningún instante
     * en el que reaparezca ASK LIFE.
     */
    private void openAskLife() {

        if (
                askDialog != null &&
                askDialog.isShowing()
        ) {
            return;
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

        settings.setJavaScriptEnabled(true);

        settings.setDomStorageEnabled(true);

        settings.setAllowFileAccess(false);

        settings.setAllowContentAccess(false);

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

                        askWebView =
                                null;
                    }

                    isHomeSection =
                            true;

                    getWindow()
                            .getDecorView()
                            .post(
                                    this::showAskControls
                            );
                }
        );

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
        }

        askDialog.show();

        window =
                askDialog.getWindow();

        if (window != null) {

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
                .post(() -> {

                    applyNativeContentInsets();

                    showAskControls();
                });
    }

    @Override
    public void onWindowFocusChanged(
            boolean hasFocus
    ) {

        super.onWindowFocusChanged(
                hasFocus
        );

        if (hasFocus) {

            getWindow()
                    .getDecorView()
                    .post(() -> {

                        applyNativeContentInsets();

                        showAskControls();
                    });
        }
    }

    @Override
    protected void onDestroy() {

        dismissAskControlsCompletely();

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