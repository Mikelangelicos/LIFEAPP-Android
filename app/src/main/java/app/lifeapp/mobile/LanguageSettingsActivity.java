package app.lifeapp.mobile;

import android.app.Activity;
import android.app.LocaleManager;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Button;
import java.util.Locale;

public class LanguageSettingsActivity extends Activity {
    private final String[][] langs = {
        {"", "Automático (sistema)"}, {"es", "Español"}, {"en", "English"}, {"fr", "Français"},
        {"de", "Deutsch"}, {"it", "Italiano"}, {"pt", "Português"}, {"ca", "Català"},
        {"nl", "Nederlands"}, {"pl", "Polski"}, {"ro", "Română"}, {"sv", "Svenska"},
        {"no", "Norsk"}, {"da", "Dansk"}, {"fi", "Suomi"}, {"cs", "Čeština"},
        {"el", "Ελληνικά"}, {"tr", "Türkçe"}, {"ru", "Русский"}, {"uk", "Українська"},
        {"ar", "العربية"}, {"he", "עברית"}, {"hi", "हिन्दी"}, {"zh-CN", "简体中文"},
        {"zh-TW", "繁體中文"}, {"ja", "日本語"}, {"ko", "한국어"}
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(24),dp(30),dp(24),dp(24));
        TextView title = new TextView(this); title.setText("Configuración · Idioma"); title.setTextSize(26); root.addView(title);
        Spinner spinner = new Spinner(this);
        String[] labels = new String[langs.length]; for(int i=0;i<langs.length;i++) labels[i]=langs[i][1];
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels)); root.addView(spinner);
        Button apply = new Button(this); apply.setText("Aplicar idioma"); root.addView(apply);
        apply.setOnClickListener(v -> { setLanguage(langs[spinner.getSelectedItemPosition()][0]); recreate(); });
        setContentView(root);
    }

    private void setLanguage(String tag) {
        if (Build.VERSION.SDK_INT >= 33) {
            LocaleManager lm = getSystemService(LocaleManager.class);
            lm.setApplicationLocales(tag.isEmpty() ? LocaleList.getEmptyLocaleList() : LocaleList.forLanguageTags(tag));
        } else {
            getSharedPreferences("lifeapp", MODE_PRIVATE).edit().putString("language", tag).apply();
            if(!tag.isEmpty()) { Locale locale=Locale.forLanguageTag(tag); Locale.setDefault(locale); android.content.res.Configuration c=new android.content.res.Configuration(getResources().getConfiguration()); c.setLocale(locale); getResources().updateConfiguration(c,getResources().getDisplayMetrics()); }
        }
    }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
}
