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
     * Botón grande ASK LIFE IA.
     */
    private PopupWindow askHomePopup;

    /*
     * Capa que oculta y bloquea
     * la antigua pestaña inferior ASK LIFE.
     */
    private PopupWindow askBottomBlocker;

    private Dialog askDialog;

    private WebView askWebView;

    @Override
    protected void onCreate(Bundle state) {

        super.onCreate(state);

        buildContextButton();

        /*
         * Esperamos a que la interfaz nativa
         * ya esté completamente dibujada.
         */
        getWindow()
                .getDecorView()
                .postDelayed(
                        this::showAskControls,
                        1000
                );
    }

    private void buildContextButton() {

        contextualButton =
                new Button(this);

        contextualButton.setText(
                "⚙"
        );

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
     * Altura de la navegación inferior
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
     * Crea los dos elementos necesarios:
     *
     * 1. botón grande ASK LIFE IA
     * 2. tapa invisible de la antigua pestaña inferior
     */
    private void showAskControls() {

        if (isFinishing()) {
            return;
        }

        /*
         * Si estamos dentro del chat,
         * no mostramos las capas de Inicio.
         */
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
                    300
            );

            return;
        }

        showBigAskButton(
                decor,
                width,
                height
        );

        hideBottomAskTab(
                decor,
                width,
                height
        );
    }

    /**
     * Sustituye visualmente el botón grande
     * ASK LIFE original.
     *
     * Conservamos el estilo degradado,
     * pero ahora pone ASK LIFE IA.
     */
    private void showBigAskButton(
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
         * Medidas obtenidas de la interfaz LIFEAPP.
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

        /*
         * Posición del botón grande de Inicio.
         */
        int top =
                Math.round(
                        height * 0.778f
                );

        TextView button =
                new TextView(this);

        button.setText(
                "ASK LIFE IA"
        );

        button.setGravity(
                Gravity.CENTER
        );

        button.setTextColor(
                Color.WHITE
        );

        button.setTextSize(
                22f
        );

        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        /*
         * Degradado azul → violeta → rosa,
         * siguiendo el diseño original LIFEAPP.
         */
        GradientDrawable gradient =
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

        gradient.setCornerRadius(
                dp(28)
        );

        button.setBackground(
                gradient
        );

        button.setElevation(
                dp(8)
        );

        /*
         * El botón grande abre
         * directamente ASK LIFE IA.
         */
        button.setOnClickListener(v ->
                openAskLife()
        );

        askHomePopup =
                new PopupWindow(
                        button,
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

        askHomePopup.setElevation(
                dp(12)
        );

        askHomePopup.showAtLocation(
                decor,
                Gravity.TOP | Gravity.START,
                left,
                top
        );
    }

    /**
     * Oculta completamente la antigua
     * pestaña pequeña ASK LIFE de abajo.
     *
     * No mostramos ASK LIFE IA aquí.
     * Solo queda el botón grande.
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

        /*
         * Capa limpia del mismo tono
         * que la barra inferior.
         */
        FrameLayout blank =
                new FrameLayout(this);

        blank.setClickable(
                true
        );

        blank.setFocusable(
                false
        );

        blank.setBackgroundColor(
                Color.rgb(
                        249,
                        251,
                        255
                )
        );

        /*
         * Consumimos el toque.
         *
         * Así la antigua pestaña ASK LIFE
         * tampoco puede abrirse accidentalmente.
         */
        blank.setOnClickListener(v -> {
            // Intencionadamente vacío.
        });

        askBottomBlocker =
                new PopupWindow(
                        blank,
                        tabWidth,
                        tabHeight,
                        false
                );

        askBottomBlocker.setTouchable(
                true
        );

        askBottomBlocker.setFocusable(
                false
        );

        askBottomBlocker.setOutsideTouchable(
                false
        );

        askBottomBlocker.setClippingEnabled(
                false
        );

        askBottomBlocker.setBackgroundDrawable(
                new ColorDrawable(
                        Color.TRANSPARENT
                )
        );

        askBottomBlocker.setElevation(
                dp(10)
        );

        /*
         * Tercer cuarto de la barra inferior.
         */
        askBottomBlocker.showAtLocation(
                decor,
                Gravity.TOP | Gravity.START,
                width / 2,
                tabTop
        );
    }

    /**
     * Oculta las capas de Inicio
     * mientras ASK LIFE IA está abierto.
     */
    private void dismissAskControls() {

        if (
                askHomePopup != null &&
                askHomePopup.isShowing()
        ) {

            askHomePopup.dismiss();
        }

        if (
                askBottomBlocker != null &&
                askBottomBlocker.isShowing()
        ) {

            askBottomBlocker.dismiss();
        }
    }

    /**
     * Abre ASK LIFE IA.
     */
    private void openAskLife() {

        dismissAskControls();

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
         * Puente para que la X del chat
         * pueda cerrar ASK LIFE IA.
         */
        askWebView.addJavascriptInterface(
                new AskLifeBridge(),
                "LifeApp"
        );

        askDialog.setContentView(
                askWebView
        );

        /*
         * Botón Atrás:
         * cerramos ASK LIFE IA
         * y volvemos a LIFEAPP.
         */
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

                    /*
                     * Al volver a Inicio
                     * reaparece el botón grande.
                     */
                    getWindow()
                            .getDecorView()
                            .postDelayed(
                                    this::showAskControls,
                                    250
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

        /*
         * Cargamos la interfaz remota
         * gestionada desde Supabase.
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
                        this::showAskControls,
                        600
                );
    }

    @Override
    protected void onDestroy() {

        dismissAskControls();

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
