package com.example.sohail.instagram;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class userListActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore mFirestore;
    FirebaseStorage storage;
    FirebaseUser currentUser;
    ArrayList<String> userList;
    ArrayAdapter<String> arrayAdapter;
    ListView userListView;

    public void getPhoto(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri selectedImage = data.getData();
        if(requestCode == 1 && resultCode == RESULT_OK && data!=null){
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
                final byte[] byteArray = stream.toByteArray();

                final Date currentTime = Calendar.getInstance().getTime();

                final CollectionReference userReference = mFirestore.collection("user");
                Query query = userReference.whereEqualTo("ID",currentUser.getUid().toString());

                Log.i("ID",currentUser.getUid().toString());

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            if(!task.getResult().isEmpty()){
                                for(final QueryDocumentSnapshot d: task.getResult()){
                                    String imageString = d.get("images").toString();
                                    imageString = imageString.substring(1,imageString.length()-1);
                                    String[] imageList = imageString.split(", ");
                                    final ArrayList<String> newImageList = new ArrayList<String>(Arrays.asList(imageList));
                                    newImageList.add(currentTime.toString());

                                    Log.i("Message: ",d.getId());
                                    Log.i("Message: ",newImageList.toString());

                                    StorageReference storageRef = storage.getReference().child(currentUser.getUid()+"/"+currentTime.toString()+".png");

                                    UploadTask uploadTask = storageRef.putBytes(byteArray);

                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            // UPDATING DATABASE
                                            userReference.document(d.getId()).update("images",newImageList.toString());
                                            Log.i("Message: ","Successfully Uploaded");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i("Message: ","Failure");
                                        }
                                    });


                                }

                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"Authentication Failed",Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1){
            if(grantResults.length>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
                getPhoto();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.share_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.share){
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            }
            else{
                getPhoto();
            }

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);


        FirebaseApp.initializeApp(this);

        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();
        userListView = findViewById(R.id.userListView);

        final CollectionReference userReference = mFirestore.collection("user");
        userReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot d: task.getResult())
                        userList.add(d.get("username").toString());

                    arrayAdapter = new ArrayAdapter<String>(userListActivity.this,android.R.layout.simple_list_item_1,userList);
                    userListView.setAdapter(arrayAdapter);

                    userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getApplicationContext(),userFeedActivity.class);
                            intent.putExtra("username",userList.get(position));
                            startActivity(intent);
                        }
                    });

                }
                else{
                    Toast.makeText(getApplicationContext(),"Authentication Failed",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
