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
     * Popup que tapa completamente el ASK LIFE nativo
     * y muestra un único ASK LIFE IA.
     */
    private PopupWindow askHomePopup;

    /*
     * Oculta solamente la antigua pestaña pequeña
     * ASK LIFE de la navegación inferior.
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

            showBigAskButton(
                    decor,
                    width,
                    height
            );

        } else {

            hideBigAskButton();
        }
    }

    /**
     * Sustituye visualmente el ASK LIFE original.
     *
     * El popup es deliberadamente más grande que el botón:
     * primero pinta el fondo de LIFEAPP encima del botón antiguo
     * y después coloca un único ASK LIFE IA en el centro.
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

        /*
         * La máscara es mayor que el botón original
         * para que no pueda asomar ninguna parte
         * del antiguo ASK LIFE.
         */
        int maskWidth =
                Math.round(
                        width * 0.84f
                );

        int maskHeight =
                buttonHeight +
                dp(30);

        int maskLeft =
                (width - maskWidth) / 2;

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
         * Posición del botón original.
         *
         * La máscara empieza algo más arriba
         * y termina algo más abajo.
         */
        int originalButtonTop =
                appBottom -
                bottomMenuHeight -
                buttonHeight -
                dp(40);

        int maskTop =
                originalButtonTop -
                dp(15);

        FrameLayout mask =
                new FrameLayout(this);

        /*
         * Fondo general de LIFEAPP.
         *
         * Esto borra visualmente el botón ASK LIFE original.
         */
        mask.setBackgroundColor(
                Color.rgb(
                        249,
                        251,
                        255
                )
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
                dp(10)
        );

        button.setOnClickListener(v ->
                openAskLife()
        );

        FrameLayout.LayoutParams buttonParams =
                new FrameLayout.LayoutParams(
                        buttonWidth,
                        buttonHeight,
                        Gravity.CENTER
                );

        mask.addView(
                button,
                buttonParams
        );

        askHomePopup =
                new PopupWindow(
                        mask,
                        maskWidth,
                        maskHeight,
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
         * Sin elevación en la máscara.
         * La sombra pertenece solamente al botón.
         */
        askHomePopup.setElevation(
                0
        );

        askHomePopup.showAtLocation(
                decor,
                Gravity.TOP | Gravity.START,
                maskLeft,
                maskTop
        );
    }

    private void hideBigAskButton() {

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
     * Oculta solamente la antigua pestaña inferior ASK LIFE.
     *
     * INICIO, EXPLORAR y PERFIL siguen funcionando
     * directamente sobre LIFEAPP.
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

        blank.setOnClickListener(v -> {
            // La antigua pestaña ASK LIFE queda anulada.
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

        askBottomBlocker.showAtLocation(
                decor,
                Gravity.TOP | Gravity.START,
                width / 2,
                tabTop
        );
    }

    /**
     * Detectamos cuándo cambia la sección.
     *
     * No consumimos estos eventos:
     * LIFEAPP sigue recibiendo los toques normalmente.
     */
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
                     * INICIO.
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
                     * EXPLORAR.
                     */
                    } else if (
                            x <
                            width * 0.50f
                    ) {

                        isHomeSection =
                                false;

                        hideBigAskButton();

                    /*
                     * PERFIL.
                     */
                    } else if (
                            x >=
                            width * 0.75f
                    ) {

                        isHomeSection =
                                false;

                        hideBigAskButton();
                    }
                }
            }
        }

        return super.dispatchTouchEvent(
                event
        );
    }

    private void dismissAskControls() {

        hideBigAskButton();

        if (
                askBottomBlocker != null &&
                askBottomBlocker.isShowing()
        ) {

            askBottomBlocker.dismiss();
        }

        askBottomBlocker =
                null;
    }

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

                    isHomeSection =
                            true;

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