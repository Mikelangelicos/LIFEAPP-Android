package app.lifeapp.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MusicHubActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(24),dp(28),dp(24),dp(24));
        TextView title=new TextView(this); title.setText("LIFEAPP Música"); title.setTextSize(30); root.addView(title);
        TextView sub=new TextView(this); sub.setText("Tu música, todos tus servicios."); sub.setTextSize(17); root.addView(sub);
        addProvider(root,"Spotify"); addProvider(root,"Apple Music"); addProvider(root,"YouTube Music"); addProvider(root,"Amazon Music");
        setContentView(root);
    }
    private void addProvider(LinearLayout root,String name){ Button b=new Button(this); b.setText(name+"  ·  Conectar"); b.setAllCaps(false); b.setEnabled(false); root.addView(b); }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
}
