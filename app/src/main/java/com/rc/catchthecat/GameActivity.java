package com.rc.catchthecat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private Intent intent;

    private Button setting_btn, rank_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(new Playground(this));
        setContentView(R.layout.activity_game);

        initComponents();

        intent = new Intent(this, PlayMusicService.class);
        startService(intent);
    }

    private void initComponents() {
        setting_btn = findViewById(R.id.setting);
        rank_btn = findViewById(R.id.rank);

        setting_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GameActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        rank_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GameActivity.this, RankActivity.class);
                startActivity(intent);
            }
        });
    }

}
