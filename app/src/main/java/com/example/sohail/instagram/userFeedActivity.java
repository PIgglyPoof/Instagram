package com.example.sohail.instagram;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class userFeedActivity extends AppCompatActivity {

    FirebaseFirestore mFirestore;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feed);

        final LinearLayout linearLayout = findViewById(R.id.linearLayout);
        mFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        CollectionReference reference = mFirestore.collection("user");
        Query query = reference.whereEqualTo("username",username);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot d: task.getResult()){
                        String imageString = d.get("images").toString();
                        imageString = imageString.substring(1,imageString.length()-1);
                        String[] imageList = imageString.split(", ");
                        for(int i=1;i<imageList.length;++i){

                            StorageReference storageReference = storage.getReference().child(d.get("ID")+"/"+imageList[i]+".png");
                            final long ONE_MEGABYTE = 5024*1024;
                            storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    ImageView imageView = new ImageView(userFeedActivity.this);
                                    Bitmap imageBitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);

                                    double aspectRatio  = (double)imageBitmap.getWidth()/(double)imageBitmap.getHeight();
                                    int imageWidth = linearLayout.getWidth();
                                    int imageHeight = (int)((double)imageWidth/aspectRatio);
                                    Log.i("Message: ",imageHeight+"");

                                    imageView.setLayoutParams(new ViewGroup.LayoutParams(
                                            imageWidth,imageHeight
                                    ));
                                    imageView.setImageBitmap(imageBitmap);
                                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                    linearLayout.addView(imageView);
                                    Toast.makeText(getApplicationContext(),"SUCCESS",Toast.LENGTH_LONG).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(),"Something Went Wrong",Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(),"Somethign went wrong",Toast.LENGTH_LONG).show();
                }
            }
        });


    }
}
