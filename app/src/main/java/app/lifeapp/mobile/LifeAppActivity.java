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
import android.os.SystemClock;
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
import android.widget.LinearLayout;
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
     * Barra inferior visual reconstruida:
     * INICIO · EXPLORAR · PERFIL.
     */
    private PopupWindow askBottomBlocker;

    private Dialog askDialog;

    private WebView askWebView;

    private int activeNavIndex = 0;

    @Override
    protected void onCreate(Bundle state) {

        super.onCreate(state);

        buildContextButton();

        /*
         * No esperamos 1 segundo.
         *
         * El retraso anterior hacía visible durante un instante
         * el antiguo botón "Ask LIFE" antes de dibujar "ASK LIFE IA".
         */
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
     * Altura de la barra de estado.
     */
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

    /**
     * Android 16 fuerza edge-to-edge para targetSdk 36.
     *
     * LIFEAPP usa una superficie nativa, así que compensamos
     * explícitamente las barras del sistema para evitar que la fila
     * superior (Hola, indicador amarillo, etc.) quede demasiado arriba.
     */
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

    /**
     * Crea:
     *
     * 1. botón grande ASK LIFE IA
     * 2. navegación inferior centrada sin la antigua pestaña ASK LIFE
     */
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
                    150
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
     * Sustituye visualmente el botón grande ASK LIFE original.
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

        int statusInset =
                getStatusBarInset();

        int navigationInset =
                getNavigationBarInset();

        int usableHeight =
                Math.max(
                        1,
                        height -
                        statusInset -
                        navigationInset
                );

        /*
         * Posición calculada dentro del área útil,
         * no debajo de las barras del sistema.
         */
        int top =
                statusInset +
                Math.round(
                        usableHeight * 0.778f
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
     * Oculta la barra inferior nativa de cuatro huecos y la sustituye
     * visualmente por tres destinos centrados.
     *
     * Los toques se reenvían a las posiciones nativas originales,
     * por lo que conservamos la navegación existente sin modificar
     * liblifeapp.so.
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

        int tabHeight =
                Math.max(
                        dp(58),
                        Math.round(
                                height * 0.060f
                        )
                );

        int appBottom =
                height -
                navigationInset;

        int tabTop =
                appBottom -
                tabHeight;

        if (tabTop < 0) {
            tabTop = 0;
        }

        LinearLayout nav =
                new LinearLayout(this);

        nav.setOrientation(
                LinearLayout.HORIZONTAL
        );

        nav.setGravity(
                Gravity.CENTER
        );

        nav.setBackgroundColor(
                Color.rgb(
                        249,
                        251,
                        255
                )
        );

        addCenteredNavItem(
                nav,
                "INICIO",
                0,
                decor,
                width,
                height,
                0.125f
        );

        addCenteredNavItem(
                nav,
                "EXPLORAR",
                1,
                decor,
                width,
                height,
                0.375f
        );

        addCenteredNavItem(
                nav,
                "PERFIL",
                2,
                decor,
                width,
                height,
                0.875f
        );

        askBottomBlocker =
                new PopupWindow(
                        nav,
                        width,
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

        askBottomBlocker.showAtLocation(
                decor,
                Gravity.TOP | Gravity.START,
                0,
                tabTop
        );
    }

    private void addCenteredNavItem(
            LinearLayout nav,
            String title,
            int index,
            View decor,
            int width,
            int height,
            float nativeXFraction
    ) {

        TextView item =
                new TextView(this);

        item.setGravity(
                Gravity.CENTER
        );

        item.setTextSize(
                11.5f
        );

        item.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        item.setOnClickListener(v -> {

            activeNavIndex =
                    index;

            refreshBottomNav();

            dispatchNativeNavTap(
                    decor,
                    width,
                    height,
                    nativeXFraction
            );
        });

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1f
                );

        nav.addView(
                item,
                params
        );

        styleNavItem(
                item,
                title,
                index == activeNavIndex
        );
    }

    private void styleNavItem(
            TextView item,
            String title,
            boolean selected
    ) {

        item.setText(
                (selected ? "●\n" : "○\n") +
                title
        );

        item.setTextColor(
                selected
                        ? Color.rgb(
                                108,
                                82,
                                226
                        )
                        : Color.rgb(
                                92,
                                99,
                                115
                        )
        );
    }

    private void refreshBottomNav() {

        if (
                askBottomBlocker == null ||
                !askBottomBlocker.isShowing()
        ) {
            return;
        }

        View content =
                askBottomBlocker.getContentView();

        if (!(content instanceof LinearLayout)) {
            return;
        }

        LinearLayout nav =
                (LinearLayout) content;

        String[] titles = {
                "INICIO",
                "EXPLORAR",
                "PERFIL"
        };

        for (
                int i = 0;
                i < nav.getChildCount() &&
                i < titles.length;
                i++
        ) {

            View child =
                    nav.getChildAt(i);

            if (child instanceof TextView) {

                styleNavItem(
                        (TextView) child,
                        titles[i],
                        i == activeNavIndex
                );
            }
        }
    }

    /**
     * Envía el toque a la posición original de la barra nativa.
     */
    private void dispatchNativeNavTap(
            View decor,
            int width,
            int height,
            float xFraction
    ) {

        int navigationInset =
                getNavigationBarInset();

        int appBottom =
                height -
                navigationInset;

        int y =
                appBottom -
                dp(30);

        float x =
                width *
                xFraction;

        long now =
                SystemClock.uptimeMillis();

        MotionEvent down =
                MotionEvent.obtain(
                        now,
                        now,
                        MotionEvent.ACTION_DOWN,
                        x,
                        y,
                        0
                );

        MotionEvent up =
                MotionEvent.obtain(
                        now,
                        now + 40,
                        MotionEvent.ACTION_UP,
                        x,
                        y,
                        0
                );

        decor.dispatchTouchEvent(
                down
        );

        decor.dispatchTouchEvent(
                up
        );

        down.recycle();
        up.recycle();
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

                    /*
                     * Reponemos las capas en el siguiente frame.
                     * Ya no hay un periodo visible con el antiguo Ask LIFE.
                     */
                    getWindow()
                            .getDecorView()
                            .post(
                                    this::showAskControls
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