package com.example.biedaalt;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class QuizQuestionActivity extends AppCompatActivity implements View.OnClickListener {

    private int mCurrentPosition = 1;
    private ArrayList<Question> mQuestionList = null;
    private int mSelectedOptionPosition = 0;
    private boolean isSelectedAnswer = false;
    private String mUserName = null;
    private int mCorrectAnswer = 0;

    private ProgressBar progressBar;
    private TextView tvProgress;
    private TextView tvQuestion;
    private ImageView ivImage;
    private TextView tvOptionOne;
    private TextView tvOptionTwo;
    private TextView tvOptionThree;
    private TextView tvOptionFour;
    private Button btnSubmit;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFirebase();
        getQuestionsFromFirebase();

    }

    private void setUpView() {
        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tv_progress);
        tvQuestion = findViewById(R.id.tv_question);
        ivImage = findViewById(R.id.iv_question);
        tvOptionOne = findViewById(R.id.tv_option_one);
        tvOptionTwo = findViewById(R.id.tv_option_two);
        tvOptionThree = findViewById(R.id.tv_option_three);
        tvOptionFour = findViewById(R.id.tv_option_four);
        btnSubmit = findViewById(R.id.btn_submit);

        tvOptionOne.setOnClickListener(this);
        tvOptionTwo.setOnClickListener(this);
        tvOptionThree.setOnClickListener(this);
        tvOptionFour.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
    }

    private void initFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("questions");
    }

    private void setPassingRetrieveDataIntent() {
        Intent intent = getIntent();
        mUserName = intent.getStringExtra(Constants.USER_NAME);
    }

    private void getQuestionsFromFirebase() {
        final Handler handler = new Handler();

        // Run the following code after a delay of 3 seconds
        handler.postDelayed(() -> {
            // Check if mQuestionList is still null after the delay
            if (mQuestionList == null) {
                // Data not fetched successfully, navigate back to MainActivity

                goToMainActivity();
            }
        }, 5000); // 3000 milliseconds (3 seconds) delay
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mQuestionList = new ArrayList<>();
                Toast.makeText(QuizQuestionActivity.this, String.valueOf(dataSnapshot.getChildrenCount()), Toast.LENGTH_SHORT).show();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Question question = new Question(
                                snapshot.child("id").getValue(Long.class).intValue(),
                                snapshot.child("question").getValue(String.class),
                                snapshot.child("image").getValue(String.class),  // Change here to get the image URL directly
                                snapshot.child("optionOne").getValue(String.class),
                                snapshot.child("optionTwo").getValue(String.class),
                                snapshot.child("optionThree").getValue(String.class),
                                snapshot.child("optionFour").getValue(String.class),
                                snapshot.child("correctAnswer").getValue(Integer.class)
                        );

                        if (question != null) {
                            mQuestionList.add(question);
                        }
                    } catch (Exception e) {
                        Log.e("FirebaseData", "Error processing question: " + e.getMessage());
                    }
                }

                if (mQuestionList.isEmpty()) {
                    // Show a toast and navigate back to the main activity
                    Toast.makeText(QuizQuestionActivity.this, "Асуулт олдсонгүй", Toast.LENGTH_SHORT).show();
                    // Cancel the delayed navigation if data is fetched successfully
                    handler.removeCallbacksAndMessages(null);
                    goToMainActivity();
                } else {
                    // Data retrieval was successful, proceed to set the question list
                    setContentView(R.layout.activity_quiz_question);
                    setUpView();
                    setPassingRetrieveDataIntent();
                    defaultOptionsView();
                    setQuestionList();
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Toast.makeText(QuizQuestionActivity.this, "Холболт амжилтгүй", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToMainActivity() {
        Toast.makeText(QuizQuestionActivity.this, "Холболт амжилтгүй", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(QuizQuestionActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    // Method to get resource identifier, handle the case where the resource is not found
    private int getResourceIdentifier(String imageName) {
        int resourceId = getResources().getIdentifier(imageName, "drawable", getPackageName());
        return (resourceId != 0) ? resourceId : R.drawable.loading; // Provide a default image resource
    }


    private void setQuestionList() {
        defaultOptionsView();
        if (mQuestionList != null) {
            ArrayList<Question> questionsList = mQuestionList;
            int currentPosition = mCurrentPosition;
            Question question = questionsList.get(currentPosition - 1);

            // Use Picasso to load the image from the URL stored in Firebase Storage
            Picasso.get().load(question.getImage()).into(ivImage);

            progressBar.setProgress(currentPosition);

            tvProgress.setText(currentPosition + "/" + questionsList.size());
            tvQuestion.setText(question.getQuestion());
            tvOptionOne.setText(question.getOptionOne());
            tvOptionTwo.setText(question.getOptionTwo());
            tvOptionThree.setText(question.getOptionThree());
            tvOptionFour.setText(question.getOptionFour());

            if (currentPosition > questionsList.size()) {
                btnSubmit.setText("Дуусгах");
            } else {
                btnSubmit.setText("Сонгох");
            }
        }
    }

    private void defaultOptionsView() {
        ArrayList<TextView> options = new ArrayList<>();
        if (tvOptionOne != null) {
            options.add(0, tvOptionOne);
        }
        if (tvOptionTwo != null) {
            options.add(1, tvOptionTwo);
        }
        if (tvOptionThree != null) {
            options.add(2, tvOptionThree);
        }
        if (tvOptionFour != null) {
            options.add(3, tvOptionFour);
        }

        for (TextView option : options) {
            option.setTextColor(Color.parseColor("#7A8089"));
            option.setTypeface(Typeface.DEFAULT);
            option.setBackground(ContextCompat.getDrawable(this, R.drawable.default_option_border_bg));
        }
    }

    private void selectedOptionView(TextView tv, int selectedOptionNum) {
        defaultOptionsView();
        mSelectedOptionPosition = selectedOptionNum;
        isSelectedAnswer = true;
        tv.setTextColor(Color.parseColor("#363A43"));
        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        tv.setBackground(ContextCompat.getDrawable(this, R.drawable.selected_option_border_bg));
    }

    @Override
    public void onClick(View view) {
    boolean isSubmitPhase = btnSubmit.getText().equals("Сонгох");
    int viewId = view.getId();

    // Handling Option Selections
    if (isSubmitPhase) {
        int selectedOption = 0;
        if (viewId == R.id.tv_option_one) selectedOption = 1;
        else if (viewId == R.id.tv_option_two) selectedOption = 2;
        else if (viewId == R.id.tv_option_three) selectedOption = 3;
        else if (viewId == R.id.tv_option_four) selectedOption = 4;

        if (selectedOption > 0) {
            selectedOptionView(new TextView[]{tvOptionOne, tvOptionTwo, tvOptionThree, tvOptionFour}[selectedOption - 1], selectedOption);
        }
    }

    // Handling Submit Button Logic
    if (viewId == R.id.btn_submit && btnSubmit != null) {
        if (mSelectedOptionPosition == 0) {
            if (!isSelectedAnswer) {
                Toast.makeText(this, "Хариултаа сонгоно уу", Toast.LENGTH_SHORT).show();
                return;
            }
            mCurrentPosition++;
            if (mCurrentPosition <= mQuestionList.size()) {
                setQuestionList();
            } else {
                finishQuiz();
            }
        } else {
            Question question = mQuestionList.get(mCurrentPosition - 1);
            if (question != null) {
                answerView(mSelectedOptionPosition, question.getCorrectAnswer() != mSelectedOptionPosition ? R.drawable.wrong_option_border_bg : R.drawable.correct_option_border_bg);
                if (question.getCorrectAnswer() == mSelectedOptionPosition) mCorrectAnswer++;
            }
            btnSubmit.setText(mCurrentPosition == mQuestionList.size() ? "Дуусгах" : "Дараагийн асуулт");
            mSelectedOptionPosition = 0;
        }
    }
}

private void finishQuiz() {
    Intent intent = new Intent(this, ResultActivity.class);
    intent.putExtra(Constants.USER_NAME, mUserName);
    intent.putExtra(Constants.CORRECT_ANSWER, mCorrectAnswer);
    intent.putExtra(Constants.TOTAL_QUESTIONS, mQuestionList.size());
    startActivity(intent);
    finish();
}


    private void answerView(int answer, int drawableView) {
        TextView selectedOption = null;
    
        switch (answer) {
            case 1:
                selectedOption = tvOptionOne;
                break;
    
            case 2:
                selectedOption = tvOptionTwo;
                break;
    
            case 3:
                selectedOption = tvOptionThree;
                break;
    
            case 4:
                selectedOption = tvOptionFour;
                break;
        }
    
        if (selectedOption != null) {
            selectedOption.setBackground(ContextCompat.getDrawable(this, drawableView));
        }
    }

}
