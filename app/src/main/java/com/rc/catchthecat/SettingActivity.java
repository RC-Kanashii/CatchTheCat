package com.rc.catchthecat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {
    private Button back_btn;
    private RadioGroup radioGroup;
    private RadioButton easy, normal, hard;
    private String difficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initComponents();
    }

    private void initComponents() {
        back_btn = findViewById(R.id.back);
        radioGroup = findViewById(R.id.radio_group);
        easy = findViewById(R.id.easy);
        normal = findViewById(R.id.normal);
        hard = findViewById(R.id.hard);

        SharedPreferences sp = getSharedPreferences("difficulty", MODE_PRIVATE);
        difficulty = sp.getString("difficulty", "easy");

        switch (difficulty) {
            case "easy": easy.toggle(); break;
            case "normal": normal.toggle(); break;
            case "hard": hard.toggle(); break;
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == easy.getId()) {
                    difficulty = "easy";
                } else if (i == normal.getId()) {
                    difficulty = "normal";
                } else if (i == hard.getId()) {
                    difficulty = "hard";
                }

                SharedPreferences sp = getSharedPreferences("difficulty", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("difficulty", difficulty);
                editor.commit();
            }
        });

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, GameActivity.class);
                startActivity(intent);
            }
        });
    }
}
