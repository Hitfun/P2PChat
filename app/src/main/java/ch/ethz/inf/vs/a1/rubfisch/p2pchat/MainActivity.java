package ch.ethz.inf.vs.a1.rubfisch.p2pchat;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View bv = findViewById(R.id.mainActivity);
        bv.setBackgroundColor(getResources().getColor(R.color.colorLightGrey));

        EditText editText = (EditText) findViewById(R.id.editText);
        int greyColor =  getResources().getColor(R.color.colorGrey);
        editText.setBackgroundColor(greyColor);

        Button bt = (Button) findViewById(R.id.joinButton);
        int orangeColor =  getResources().getColor(R.color.colorOrange);
        bt.setBackgroundColor(orangeColor);
    }

    public void joinPressed(View view) {
        Intent intent = new Intent(MainActivity.this, BroadcastReceiverActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        if (!message.trim().equals("")){
            intent.putExtra("nameText", message);
            startActivity(intent);

        }
    }
}
