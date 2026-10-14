package com.example.quizllm.activities;

import android.content.Intent;
import android.os.Bundle;
import retrofit2.Call;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizllm.Models.Question;
import com.example.quizllm.Network.ApiService;
import com.example.quizllm.BuildConfig;
import com.example.quizllm.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Callback;


import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class QuizActivity extends AppCompatActivity {

    private TextView questionTextView;
    private RadioGroup optionsGroup;
    private RadioButton optionA, optionB, optionC, optionD;
    private Button nextButton;

    private final List<Question> questions = new ArrayList<>();
    private int currentIndex = 0;
    private int score = 0;
    private String selectedAnswer = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        questionTextView = findViewById(R.id.questionText);
        optionsGroup = findViewById(R.id.optionsGroup);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        nextButton = findViewById(R.id.nextBtn);

        nextButton.setEnabled(false); // enabled once questions have loaded
        questionTextView.setText("Generating your quiz…");

        String topic = getIntent().getStringExtra("topic");
        if (topic == null || topic.isEmpty()) topic = "default";

        loadQuiz(topic);

        nextButton.setOnClickListener(v -> {
            if (currentIndex >= questions.size()) return;
            int selectedId = optionsGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(QuizActivity.this, "Please select an option", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedRadioButton = findViewById(selectedId);
            selectedAnswer = getOptionLetter(selectedRadioButton);

            if (selectedAnswer.equalsIgnoreCase(questions.get(currentIndex).getCorrect_answer())) {
                score++;
            }

            currentIndex++;
            if (currentIndex < questions.size()) {
                displayQuestion(currentIndex);
            } else {
                // Redirect to ResultActivity
                Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
                intent.putExtra("score", score);
                intent.putExtra("total", questions.size());
                startActivity(intent);
                finish();  // Optional: remove from back stack
            }

        });
    }



    private void loadQuiz(String topic) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.QUIZ_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);

        Call<ApiService.ApiResponse> call = api.getQuiz(topic);

        call.enqueue(new Callback<ApiService.ApiResponse>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse> call, Response<ApiService.ApiResponse> response) {
                List<Question> loaded = response.isSuccessful() && response.body() != null
                        ? response.body().getQuiz() : null;
                if (loaded != null) {
                    for (Question q : loaded) {
                        // The screen has exactly four options; skip anything else.
                        if (q != null && q.getOptions() != null && q.getOptions().size() == 4) {
                            questions.add(q);
                        }
                    }
                }
                if (questions.isEmpty()) {
                    showLoadError("Couldn't load a quiz for this topic. Please try again.");
                    return;
                }
                nextButton.setEnabled(true);
                displayQuestion(currentIndex);
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse> call, Throwable t) {
                showLoadError("Can't reach the quiz server (" + BuildConfig.QUIZ_API_URL + "). Is it running?");
            }
        });
    }


    private void showLoadError(String message) {
        questionTextView.setText(message);
        Toast.makeText(QuizActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private void displayQuestion(int index) {
        Question q = questions.get(index);
        questionTextView.setText(q.getQuestion());
        optionA.setText("A. " + q.getOptions().get(0));
        optionB.setText("B. " + q.getOptions().get(1));
        optionC.setText("C. " + q.getOptions().get(2));
        optionD.setText("D. " + q.getOptions().get(3));
        optionsGroup.clearCheck();
    }

    private String getOptionLetter(RadioButton selected) {
        if (selected == optionA) return "A";
        if (selected == optionB) return "B";
        if (selected == optionC) return "C";
        if (selected == optionD) return "D";
        return "";
    }
}
