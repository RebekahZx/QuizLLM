package com.example.quizllm.Network;




import retrofit2.Call;


import com.example.quizllm.Models.Question;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;


public interface ApiService {
    @GET("getQuiz")
    Call<ApiResponse> getQuiz(@Query("topic") String topic);

    class ApiResponse {
        private List<Question> quiz;
        public List<Question> getQuiz() {
            return quiz;
        }
    }
}

