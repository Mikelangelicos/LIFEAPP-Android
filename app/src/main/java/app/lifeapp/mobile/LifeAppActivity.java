package app.lifeapp.mobile;

import android.app.NativeActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
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

    private FrameLayout askOverlay;
    private WebView askWebView;

    /*
     * Esta capa queda físicamente por encima
     * del botón ASK LIFE de la interfaz nativa.
     */
    private FrameLayout askTabInterceptor;

    @Override
    protected void onCreate(Bundle state) {

        super.onCreate(state);

        contextualButton = new Button(this);

        contextualButton.setText("⚙");
        contextualButton.setTextSize(22f);
        contextualButton.setVisibility(Button.GONE);

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

        buildAskLife();

        /*
         * Muy importante:
         * creamos el interceptor DESPUÉS
         * de la interfaz nativa.
         */
        installAskLifeTabInterceptor();
    }

    /**
     * Crea una zona Android encima del tercer
     * botón de la navegación inferior.
     *
     * Al estar encima del SurfaceView nativo,
     * LIFEAPP IA recibe el toque antes que
     * la interfaz antigua.
     */
    private void installAskLifeTabInterceptor() {

        getWindow()
                .getDecorView()
                .post(() -> {

                    if (askTabInterceptor != null) {
                        positionAskLifeTabInterceptor();
                        return;
                    }

                    askTabInterceptor =
                            new FrameLayout(this);

                    /*
                     * El fondo principal es transparente
                     * para conservar el icono original.
                     */
                    askTabInterceptor.setBackgroundColor(
                            Color.TRANSPARENT
                    );

                    askTabInterceptor.setClickable(true);
                    askTabInterceptor.setFocusable(true);

                    askTabInterceptor.setOnClickListener(v ->
                            showAskLife()
                    );

                    /*
                     * Cubrimos únicamente el texto
                     * ASK LIFE antiguo.
                     */
                    TextView label =
                            new TextView(this);

                    label.setText("ASK LIFE IA");

                    label.setTextSize(11f);

                    label.setGravity(
                            Gravity.CENTER
                    );

                    label.setTextColor(
                            Color.rgb(
                                    58,
                                    78,
                                    120
                            )
                    );

                    /*
                     * Fondo prácticamente idéntico
                     * al menú inferior original.
                     */
                    label.setBackgroundColor(
                            Color.rgb(
                                    252,
                                    252,
                                    253
                            )
                    );

                    FrameLayout.LayoutParams labelParams =
                            new FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.MATCH_PARENT,
                                    dp(25),
                                    Gravity.BOTTOM
                            );

                    labelParams.setMargins(
                            0,
                            0,
                            0,
                            dp(4)
                    );

                    askTabInterceptor.addView(
                            label,
                            labelParams
                    );

                    positionAskLifeTabInterceptor();

                    /*
                     * Garantizamos que quede encima
                     * de la interfaz nativa.
                     */
                    askTabInterceptor.bringToFront();
                });
    }

    private void positionAskLifeTabInterceptor() {

        int width =
                getWindow()
                        .getDecorView()
                        .getWidth();

        if (width <= 0) {
            return;
        }

        int tabWidth =
                width / 4;

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        tabWidth,
                        dp(82),
                        Gravity.BOTTOM | Gravity.START
                );

        /*
         * Tercer botón:
         *
         * 0 - 25 %  Inicio
         * 25 - 50 % Explorar
         * 50 - 75 % ASK LIFE
         * 75 - 100 % Perfil
         */
        params.leftMargin =
                width / 2;

        if (askTabInterceptor.getParent() == null) {

            addContentView(
                    askTabInterceptor,
                    params
            );

        } else {

            askTabInterceptor.setLayoutParams(
                    params
            );
        }
    }

    @Override
    public void onWindowFocusChanged(
            boolean hasFocus
    ) {

        super.onWindowFocusChanged(
                hasFocus
        );

        if (
                hasFocus &&
                askTabInterceptor != null &&
                askOverlay.getVisibility()
                        != View.VISIBLE
        ) {

            positionAskLifeTabInterceptor();

            askTabInterceptor.bringToFront();
        }
    }

    @Override
    public boolean dispatchTouchEvent(
            MotionEvent event
    ) {

        /*
         * ASK LIFE IA abierto:
         * WebView recibe los eventos.
         */
        if (
                askOverlay != null &&
                askOverlay.getVisibility()
                        == View.VISIBLE
        ) {

            return super.dispatchTouchEvent(
                    event
            );
        }

        if (
                event.getAction()
                        == MotionEvent.ACTION_UP
        ) {

            int width =
                    getWindow()
                            .getDecorView()
                            .getWidth();

            int height =
                    getWindow()
                            .getDecorView()
                            .getHeight();

            if (
                    width > 0 &&
                    height > 0 &&
                    event.getY()
                            > height * 0.86f
            ) {

                float section =
                        event.getX()
                                / width;

                /*
                 * Fallback adicional.
                 *
                 * Normalmente el interceptor
                 * ya habrá capturado este toque.
                 */
                if (
                        section >= 0.50f &&
                        section < 0.75f
                ) {

                    showAskLife();

                    return true;
                }

                /*
                 * PERFIL
                 */
                else if (
                        section >= 0.75f
                ) {

                    contextualButton.setText(
                            "⚙"
                    );

                    contextualButton.setOnClickListener(v ->
                            startActivity(
                                    new Intent(
                                            this,
                                            LanguageSettingsActivity.class
                                    )
                            )
                    );

                    contextualButton.setVisibility(
                            Button.VISIBLE
                    );
                }

                /*
                 * EXPLORAR
                 */
                else if (
                        section >= 0.25f &&
                        section < 0.50f
                ) {

                    contextualButton.setText(
                            "♫"
                    );

                    contextualButton.setOnClickListener(v ->
                            startActivity(
                                    new Intent(
                                            this,
                                            MusicHubActivity.class
                                    )
                            )
                    );

                    contextualButton.setVisibility(
                            Button.VISIBLE
                    );
                }

                /*
                 * INICIO
                 */
                else {

                    contextualButton.setVisibility(
                            Button.GONE
                    );
                }
            }
        }

        return super.dispatchTouchEvent(
                event
        );
    }

    /**
     * Crea ASK LIFE IA.
     */
    private void buildAskLife() {

        askOverlay =
                new FrameLayout(this);

        askOverlay.setBackgroundColor(
                Color.rgb(
                        11,
                        13,
                        20
                )
        );

        askOverlay.setVisibility(
                View.GONE
        );

        askOverlay.setClickable(true);
        askOverlay.setFocusable(true);

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

        askOverlay.addView(
                askWebView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        addContentView(
                askOverlay,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );
    }

    /**
     * Abre ASK LIFE IA.
     */
    private void showAskLife() {

        contextualButton.setVisibility(
                Button.GONE
        );

        askOverlay.setVisibility(
                View.VISIBLE
        );

        /*
         * Fundamental:
         * el chat pasa por encima incluso
         * del interceptor inferior.
         */
        askOverlay.bringToFront();

        askWebView.loadDataWithBaseURL(
                ASK_BASE_URL,
                ASK_LOADER,
                "text/html",
                "UTF-8",
                null
        );
    }

    /**
     * Vuelve a LIFEAPP.
     */
    private void hideAskLife() {

        askOverlay.setVisibility(
                View.GONE
        );

        /*
         * Recuperamos el botón ASK LIFE IA.
         */
        if (askTabInterceptor != null) {

            askTabInterceptor.bringToFront();
        }
    }

    private final class AskLifeBridge {

        @JavascriptInterface
        public void closeAskLife() {

            runOnUiThread(() ->
                    hideAskLife()
            );
        }
    }

    @Override
    public void onBackPressed() {

        if (
                askOverlay != null &&
                askOverlay.getVisibility()
                        == View.VISIBLE
        ) {

            hideAskLife();

            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {

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
