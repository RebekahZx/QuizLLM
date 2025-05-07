package com.example.quizllm.Models;



import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

public class Question implements Parcelable {
    private String question;
    private List<String> options;
    private String correct_answer;

    // Constructors
    public Question() {}

    public Question(String question, List<String> options, String correct_answer) {
        this.question = question;
        this.options = options;
        this.correct_answer = correct_answer;
    }

    // Getters
    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getCorrect_answer() {
        return correct_answer;
    }

    // Setters
    public void setQuestion(String question) {
        this.question = question;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public void setCorrect_answer(String correct_answer) {
        this.correct_answer = correct_answer;
    }

    // Parcelable implementation
    protected Question(Parcel in) {
        question = in.readString();
        options = in.createStringArrayList();
        correct_answer = in.readString();
    }

    public static final Creator<Question> CREATOR = new Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(question);
        dest.writeStringList(options);
        dest.writeString(correct_answer);

    }


}

