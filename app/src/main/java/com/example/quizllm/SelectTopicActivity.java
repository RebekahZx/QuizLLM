package com.example.quizllm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizllm.activities.QuizActivity;

public class SelectTopicActivity extends AppCompatActivity {

    TextView greetingText;
    EditText topicInput;
    Button startQuizBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_topic);

        greetingText = findViewById(R.id.greetingText);
        topicInput = findViewById(R.id.topicInput);
        startQuizBtn = findViewById(R.id.startQuizBtn);

        // Get username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "User");
        String username = email.split("@")[0];  // extract name before @
        greetingText.setText("Hello, " + username + "!");

        startQuizBtn.setOnClickListener(v -> {
            String topic = topicInput.getText().toString().trim();
            if (!topic.isEmpty()) {
                Intent intent = new Intent(SelectTopicActivity.this, QuizActivity.class);
                intent.putExtra("topic", topic);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please enter a topic", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

