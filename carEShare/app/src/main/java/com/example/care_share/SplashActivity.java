package com.example.care_share;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    String userName,myToken;
    DocumentReference userRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash);
                    if(FirebaseAuth.getInstance().getCurrentUser()!=null){
                        userRef=db.document("Users/"+FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                        userRef.get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if(documentSnapshot.exists()){
                                            userName=documentSnapshot.getString("Username");
                                            myToken=documentSnapshot.getString("MyToken");
                                            if(userName==null|| userName.length()==0 || myToken==null){
                                                gotoProfileActivity();
                                            }
                                            else {
                                                Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }
                                        }
                                        if(!documentSnapshot.exists()){
                                            gotoProfileActivity();
                                        }
                                    }
                                });
                    }
                    else{
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
                public void gotoProfileActivity(){
                    Intent intent=new Intent(getApplicationContext(),ProfileDetailsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
}
