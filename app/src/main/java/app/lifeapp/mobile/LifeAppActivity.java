package app.lifeapp.mobile;

import android.app.NativeActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

/** Keeps the restored v3.1 native UI untouched and adds an editable Android bridge. */
public class LifeAppActivity extends NativeActivity {
    private Button contextualButton;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        contextualButton = new Button(this);
        contextualButton.setText("⚙");
        contextualButton.setTextSize(22f);
        contextualButton.setVisibility(Button.GONE);
        contextualButton.setOnClickListener(v -> startActivity(new Intent(this, LanguageSettingsActivity.class)));
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(dp(58), dp(58), Gravity.TOP | Gravity.END);
        p.setMargins(0, dp(18), dp(14), 0);
        addContentView(contextualButton, p);
    }

    @Override public boolean dispatchTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_UP) {
            int w = getWindow().getDecorView().getWidth();
            int h = getWindow().getDecorView().getHeight();
            if (w > 0 && h > 0 && e.getY() > h * 0.86f) {
                float section = e.getX() / w;
                if (section >= .75f) { // Perfil
                    contextualButton.setText("⚙");
                    contextualButton.setOnClickListener(v -> startActivity(new Intent(this, LanguageSettingsActivity.class)));
                    contextualButton.setVisibility(Button.VISIBLE);
                } else if (section >= .25f && section < .50f) { // Explorar
                    contextualButton.setText("♫");
                    contextualButton.setOnClickListener(v -> startActivity(new Intent(this, MusicHubActivity.class)));
                    contextualButton.setVisibility(Button.VISIBLE);
                } else {
                    contextualButton.setVisibility(Button.GONE);
                }
            }
        }
        return super.dispatchTouchEvent(e);
    }

    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
}
