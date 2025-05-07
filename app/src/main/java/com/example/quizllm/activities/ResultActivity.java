package com.example.quizllm.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizllm.R;
import com.example.quizllm.SelectTopicActivity;

public class ResultActivity extends AppCompatActivity {

    private TextView scoreTextView;
    private Button backToTopicBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        scoreTextView = findViewById(R.id.scoreTextView);
        backToTopicBtn = findViewById(R.id.backToTopicBtn);

        int score = getIntent().getIntExtra("score", 0);
        int total = getIntent().getIntExtra("total", 0);

        scoreTextView.setText("You scored " + score + " out of " + total);

        backToTopicBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, SelectTopicActivity.class);
            startActivity(intent);
            finish();  // Optional: prevents going back to result again via back button
        });
    }
}
