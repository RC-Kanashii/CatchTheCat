package com.rc.catchthecat;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.rc.catchthecat.service.RankService;

public class RankActivity extends AppCompatActivity {
    private RankService rs;
    private SQLiteDatabase database;
    private ListView lv;
    private Button back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);

        initComponents();
        initAdapter();
    }

    private void initComponents() {
        lv = findViewById(R.id.lv);
        back_btn = findViewById(R.id.back);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RankActivity.this, GameActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initAdapter() {
        rs = new RankService(RankActivity.this, "rank", null, 1);
        database = rs.getReadableDatabase();
        // rs.add(database, 2);
        // rs.add(database, 3);
        // rs.add(database, 4);
        // rs.add(database, 4);
        // rs.add(database, 5);
        // rs.add(database, 6);
        Cursor cursor = rs.queryTop10(database);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.itemlayout, cursor,
                new String[]{"_id", "date", "steps", "difficulty"},
                new int[]{0, R.id.date, R.id.steps, R.id.difficulty});
        lv.setAdapter(adapter);
    }
}
