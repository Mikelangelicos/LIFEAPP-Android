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

/**
 * Mantiene la interfaz original de LIFEAPP
 * y añade ASK LIFE IA gestionado desde Supabase.
 */
public class LifeAppActivity extends NativeActivity {

    private static final String ASK_UI_URL =
            "https://rtggrizfrkplropltwdc.supabase.co/functions/v1/ask-life-web";

    private static final String ASK_BASE_URL =
            "https://rtggrizfrkplropltwdc.supabase.co/";

    /*
     * Supabase convierte HTML devuelto por Edge Functions en text/plain.
     *
     * Por eso LIFEAPP descarga el contenido mediante fetch()
     * y después lo renderiza dentro del WebView.
     */
    private static final String ASK_LOADER =
            "<!doctype html>" +
            "<html>" +
            "<head>" +
            "<meta charset='utf-8'>" +
            "<meta name='viewport' " +
            "content='width=device-width,initial-scale=1'>" +

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

            ".then(function(r){" +
            "return r.text();" +
            "})" +

            ".then(function(html){" +
            "document.open();" +
            "document.write(html);" +
            "document.close();" +
            "})" +

            ".catch(function(){" +
            "document.body.innerHTML=" +
            "'<div>No se pudo cargar ASK LIFE IA.</div>';" +
            "});" +

            "</script>" +

            "</body>" +
            "</html>";

    private Button contextualButton;

    private FrameLayout askOverlay;
    private WebView askWebView;

    @Override
    protected void onCreate(Bundle state) {

        super.onCreate(state);

        /*
         * Botón contextual utilizado en Explorar / Perfil.
         */
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

        /*
         * Preparamos ASK LIFE IA.
         */
        buildAskLife();
    }

    /**
     * Detecta qué sección de la barra inferior
     * original de LIFEAPP ha pulsado el usuario.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        /*
         * Si ASK LIFE IA está abierto,
         * dejamos que WebView gestione los toques.
         */
        if (
                askOverlay != null &&
                askOverlay.getVisibility() == View.VISIBLE
        ) {

            return super.dispatchTouchEvent(event);
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {

            int width =
                    getWindow()
                            .getDecorView()
                            .getWidth();

            int height =
                    getWindow()
                            .getDecorView()
                            .getHeight();

            /*
             * Barra inferior LIFEAPP.
             */
            if (
                    width > 0 &&
                    height > 0 &&
                    event.getY() > height * 0.86f
            ) {

                float section =
                        event.getX() / width;

                /*
                 * ASK LIFE
                 *
                 * Tercer botón de la barra.
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
                else if (section >= 0.75f) {

                    contextualButton.setText("⚙");

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

                    contextualButton.setText("♫");

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

        return super.dispatchTouchEvent(event);
    }

    /**
     * Crea la pantalla interna de ASK LIFE IA.
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

        /*
         * WebView donde renderizamos
         * la interfaz gestionada desde Supabase.
         */
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

        /*
         * Necesario para el chat
         * y la comunicación con Supabase.
         */
        settings.setJavaScriptEnabled(true);

        /*
         * Permite mantener el historial
         * de conversación mediante localStorage.
         */
        settings.setDomStorageEnabled(true);

        /*
         * No necesitamos acceso a archivos
         * locales del dispositivo.
         */
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);

        /*
         * Puente JavaScript -> Android.
         *
         * Permite que ASK LIFE IA pueda
         * cerrar su pantalla y volver a LIFEAPP.
         */
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

        /*
         * Añadimos ASK LIFE IA encima
         * de la interfaz nativa original.
         */
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

        askOverlay.bringToFront();

        /*
         * Cargamos un pequeño HTML local
         * que descarga la interfaz real desde Supabase.
         */
        askWebView.loadDataWithBaseURL(
                ASK_BASE_URL,
                ASK_LOADER,
                "text/html",
                "UTF-8",
                null
        );
    }

    /**
     * Cierra ASK LIFE IA.
     */
    private void hideAskLife() {

        askOverlay.setVisibility(
                View.GONE
        );
    }

    /**
     * Puente accesible desde JavaScript.
     */
    private final class AskLifeBridge {

        @JavascriptInterface
        public void closeAskLife() {

            runOnUiThread(() ->
                    hideAskLife()
            );
        }
    }

    /**
     * Botón Atrás de Android.
     */
    @Override
    public void onBackPressed() {

        /*
         * Si estamos dentro de ASK LIFE IA,
         * primero cerramos ASK LIFE.
         */
        if (
                askOverlay != null &&
                askOverlay.getVisibility() == View.VISIBLE
        ) {

            if (askWebView.canGoBack()) {

                askWebView.goBack();

            } else {

                hideAskLife();
            }

            return;
        }

        super.onBackPressed();
    }

    /**
     * Liberamos WebView al cerrar LIFEAPP.
     */
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

    /**
     * Conversión dp -> px.
     */
    private int dp(int value) {

        return Math.round(
                value *
                getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}
