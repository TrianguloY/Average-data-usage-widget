package com.trianguloy.continuousDataUsage.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.trianguloy.continuousDataUsage.R;

public class Main extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //clickable links
        for (int id : new int[]{R.id.stt_txt_info} ) {
            ((TextView) findViewById(id)).setMovementMethod(LinkMovementMethod.getInstance());
        }

    }

    public void onButtonClick(View view){
        switch (view.getId()){
            case R.id.m_btn_history:
                startActivity(new Intent(this, HistoryActivity.class));
                break;
            case R.id.m_btn_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }

    }
}
