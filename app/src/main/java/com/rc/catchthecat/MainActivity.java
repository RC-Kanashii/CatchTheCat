package com.rc.catchthecat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Button start_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        // getSupportActionBar().hide(); // 隐藏状态栏
        // setContentView(new Playground(this));
    }

    private void init() {
        start_btn = findViewById(R.id.start);
        start_btn.setOnClickListener(clickEvent_start_btn);
    }

    View.OnClickListener clickEvent_start_btn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setContentView(new Playground(MainActivity.this));
        }
    };
}
