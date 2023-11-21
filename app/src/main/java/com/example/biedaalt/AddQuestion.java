package com.example.biedaalt;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AddQuestion extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private EditText question;
    private Button chooseImageButton, addQuestionButton;
    private EditText opOne;
    private EditText opTwo, opThree, opFour, correctAnswer;

    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);

        question = (EditText) findViewById(R.id.question);
        opOne = (EditText) findViewById(R.id.optionOne);
        opTwo = (EditText) findViewById(R.id.optionTwo);
        opThree = (EditText) findViewById(R.id.optionThree);
        opFour = (EditText) findViewById(R.id.optionFour);
        correctAnswer = (EditText) findViewById(R.id.correctAnswer);
        chooseImageButton = (Button) findViewById(R.id.chooseImageButton);
        imageView = findViewById(R.id.imageView);

        addQuestionButton = (Button) findViewById(R.id.addQuestionBtn);
        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        addQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImageUri != null) {
                    // Upload the image to Firebase Storage
                    uploadImageToFirebase("images", selectedImageUri);
                } else {
                    Toast.makeText(AddQuestion.this, "Please select an image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();

            // Get the file name from the URI
            String fileName = getFileName(selectedImageUri);

            // Display the file name
            Toast.makeText(this, "Selected File: " + fileName, Toast.LENGTH_SHORT).show();

            // Set the selected image to the ImageView
            imageView.setImageURI(selectedImageUri);
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void uploadImageToFirebase(String folderPath, Uri imageUri) {
        // Display a progress dialog while uploading
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Image...");
        progressDialog.show();

        // Create a reference to the specified path
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Get the original file name from the URI
        String originalFileName = getFileName(imageUri);

        // Set the reference to the desired path with the original file name
        StorageReference storageReference = storage.getReference().child("images").child(originalFileName);

        // Upload the file to Firebase Storage
        storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Image uploaded successfully
                progressDialog.dismiss();
                Toast.makeText(AddQuestion.this, "Image uploaded to Firebase Storage", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle unsuccessful uploads
                progressDialog.dismiss();
                Toast.makeText(AddQuestion.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        createJsonObject(originalFileName);
    }


    private void createJsonObject(String image) {
        // Get values from EditText fields
        String questionText = question.getText().toString();
        String optionOneText = opOne.getText().toString();
        String optionTwoText = opTwo.getText().toString();
        String optionThreeText = opThree.getText().toString();
        String optionFourText = opFour.getText().toString();
        int correctAnswerText = Integer.parseInt(correctAnswer.getText().toString());

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("questions");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long questionsCount = dataSnapshot.getChildrenCount();

                // Set values directly to Firebase using the length as the key
                databaseReference.child(String.valueOf(questionsCount)).child("id").setValue(questionsCount+1);
                databaseReference.child(String.valueOf(questionsCount)).child("question").setValue(questionText);
                databaseReference.child(String.valueOf(questionsCount)).child("optionOne").setValue(optionOneText);
                databaseReference.child(String.valueOf(questionsCount)).child("optionTwo").setValue(optionTwoText);
                databaseReference.child(String.valueOf(questionsCount)).child("optionThree").setValue(optionThreeText);
                databaseReference.child(String.valueOf(questionsCount)).child("optionFour").setValue(optionFourText);
                databaseReference.child(String.valueOf(questionsCount)).child("correctAnswer").setValue(correctAnswerText);
                databaseReference.child(String.valueOf(questionsCount)).child("image").setValue("https://firebasestorage.googleapis.com/v0/b/bie-daalt-8d8e7.appspot.com/o/images%2F" + image + "?alt=media");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddQuestion.this, "Error retrieving data", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
