package com.example.facedetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button btn_takePicture, btn_fromGallery, btn_detect;
    Bitmap myBitmap;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        btn_takePicture = findViewById(R.id.takePicture);
        btn_fromGallery = findViewById(R.id.fromGallery);
        btn_detect = findViewById(R.id.detect);
        textView = findViewById(R.id.textView);

        btn_takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 0);

            }
        });

        btn_fromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 1);//one can be replaced with any action code
            }
        });

        btn_detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    imageView.setImageBitmap(myBitmap);
                    runFaceDetector(myBitmap);
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case 0:

                if (requestCode == 0 && resultCode == RESULT_OK) {
                    myBitmap = (Bitmap) imageReturnedIntent.getExtras().get("data");
                    imageView.setImageBitmap(myBitmap);
                }

                break;

            case 1:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();

                    try {
                        myBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    imageView.setImageBitmap(myBitmap);
                }
                break;
        }
    }

    private void runFaceDetector(Bitmap bitmap) {

        FirebaseVisionFaceDetectorOptions highAccuracyOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();

        // Real-time contour detection of multiple faces
        FirebaseVisionFaceDetectorOptions realTimeOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                        .build();


        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(myBitmap);
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(highAccuracyOpts);
        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> faces) {
                // myTextView.setText(runFaceRecog(faces));
                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                textView.setText(runFaceRecog(faces));

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure
                    (@NonNull Exception exception) {
                Toast.makeText(MainActivity.this,
                        "Exception", Toast.LENGTH_LONG).show();
            }
        });
    }

    private String runFaceRecog(List<FirebaseVisionFace> faces) {
        StringBuilder result = new StringBuilder();
        float smilingProbability = 0;
        float rightEyeOpenProbability = 0;
        float leftEyeOpenProbability = 0;

        for (FirebaseVisionFace face : faces) {
            if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                smilingProbability = face.getSmilingProbability();
            }
            if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                rightEyeOpenProbability = face.getRightEyeOpenProbability();
            }
            if (face.getLeftEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                leftEyeOpenProbability = face.getLeftEyeOpenProbability();
            }


            result.append("Smile: ");
            if (smilingProbability > 0.5) {
                result.append("Yes \n  Probability: " + smilingProbability);
            } else {
                result.append("No");
            }
            result.append("\n\nRight eye: ");
            if (rightEyeOpenProbability > 0.5) {
                result.append("Open \nProbability: " + rightEyeOpenProbability);
            } else {
                result.append("Close");
            }
            result.append("\n\nLeft eye: ");
            if (leftEyeOpenProbability > 0.5) {
                result.append("Open \nProbability: " + leftEyeOpenProbability);
            } else {
                result.append("Close");
            }
            result.append("\n\n");
        }
        return result.toString();
    }

}
