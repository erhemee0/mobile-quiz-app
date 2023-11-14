package com.example.biedaalt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText etName = findViewById(R.id.et_name);
        Button btnStart = findViewById(R.id.btn_start);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etName.getText().length() > 0) {
                    Intent intent = new Intent(MainActivity.this, QuizQuestionActivity.class);
                    intent.putExtra(Constants.USER_NAME, etName.getText().toString());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Нэрээ оруулна уу", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
