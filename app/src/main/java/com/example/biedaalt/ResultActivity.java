package com.example.biedaalt;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView tvName = findViewById(R.id.tv_name);
        TextView tvScore = findViewById(R.id.tv_score);
        Button btnFinish = findViewById(R.id.btn_finish);

        tvName.setText(getIntent().getStringExtra(Constants.USER_NAME));
        int totalQuestions = getIntent().getIntExtra(Constants.TOTAL_QUESTIONS, 0);
        int correctAnswers = getIntent().getIntExtra(Constants.CORRECT_ANSWER, 0);
        tvScore.setText(totalQuestions+"-c " + correctAnswers +" зөв хариуллаа");

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ResultActivity.this, MainActivity.class));
            }
        });
    }
}

