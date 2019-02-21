package com.example.sohail.instagram;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnKeyListener {


    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private Map<String,String> userMap;

    EditText email;
    EditText password;
    EditText username;

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_ENTER&&event.getAction()==KeyEvent.ACTION_DOWN){
            onClick(v);
        }

        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            Intent intent = new Intent(this,userListActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            Toast.makeText(getApplicationContext(),"SUCCESS",Toast.LENGTH_LONG).show();
        }
    }

    public void onClickRemoveKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
    }

    public void onClickLogin(View view){
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
    }

    public void onClick(View view){

        Context currentContext = this;

        final String userUsername = username.getText().toString();
        final String userEmail = email.getText().toString();
        final String userPassword = password.getText().toString();

        if(!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
            Toast.makeText(getApplicationContext(),"Enter a Valid Email",Toast.LENGTH_SHORT).show();
            return;
        }
        if(userPassword.isEmpty()&&userPassword.length()<=4){
            Toast.makeText(getApplicationContext(),"Enter Password (Length > 4)",Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference userReference = mFirestore.collection("user");
        Query query = userReference.whereEqualTo("username",userUsername);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(!task.getResult().isEmpty())
                        Toast.makeText(getApplicationContext(),"Username Already Used",Toast.LENGTH_LONG).show();
                    else{

                        mAuth.createUserWithEmailAndPassword(userEmail,userPassword)
                                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){

                                            FirebaseUser currentUser = mAuth.getCurrentUser();

                                            userMap.put("username",userUsername);
                                            userMap.put("ID",currentUser.getUid().toString());
                                            userMap.put("images","[]");

                                            mFirestore.collection("user").add(userMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Toast.makeText(getApplicationContext(),"Registration Successful",Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                        else{
                                            Toast.makeText(getApplicationContext(), "Email Already Registered", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                    }

                }
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageView = findViewById(R.id.imageView);
        ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout);

        FirebaseApp.initializeApp(this);

        email = findViewById(R.id.emailEditView);
        password = findViewById(R.id.passwordEditView);
        username = findViewById(R.id.usernameEditText);

        password.setOnKeyListener(this);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userMap = new HashMap<>();
    }
}


//        userMap.put("SexyOne","Zaki");
//        userMap.put("ILove","Zaki");
//        userMap.put("HottestOne","Zaki");
//
//        mFirestore.collection("mylove").add(userMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//            @Override
//            public void onSuccess(DocumentReference documentReference) {
//                Toast.makeText(getApplicationContext(),"SUCCESS",Toast.LENGTH_LONG).show();
//            }
//        });

//        mFirestore.collection("mylove")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if(task.isSuccessful()){
//                            for(QueryDocumentSnapshot document: task.getResult()){
//                                Log.i("Data: ",document.get("SexyOne").toString());
//                            }
//                        }else{
//                            Log.i("Error","FAILURE");
//                        }
//                    }
//                });