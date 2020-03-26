package com.example.care_share;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    FirebaseUser currentUser= FirebaseAuth.getInstance().getCurrentUser();
    ArrayList<String> PresentGroups;
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.i("New token",s);
        sendRegistrationToServer(s);
    }
    public void sendRegistrationToServer(String token){
        final String mytoken=token;
        if(currentUser!=null && currentUser.getPhoneNumber()!=null) {
            final DocumentReference currentUserRef=db.collection("Users").document(currentUser.getPhoneNumber());
            currentUserRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()){
                        HashMap<String, String> map = new HashMap<>();
                        map.put("MyToken", mytoken);
                        currentUserRef.set(map,SetOptions.merge());
                        PresentGroups=new ArrayList<>();
                        Object object=documentSnapshot.get("Group_IDs");
                        if(object!=null)
                            PresentGroups.addAll(((ArrayList<String>) object));
                        for(final String string:PresentGroups){
                            db.collection("Groups").document(string).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.get("AllTokens")!=null && !(((ArrayList<String>)documentSnapshot.get("AllTokens")).contains(mytoken))){
                                        db.collection("Groups").document(string).update("AllTokens", FieldValue.arrayUnion(mytoken));
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.i("Title","Inside Message Received");
        Log.i("Title",remoteMessage.getNotification().getTitle());
        Log.i("Body",remoteMessage.getNotification().getBody());
        //Log.i("GroupID",remoteMessage.getData().get("Group_ID"));
        //sendNotification(remoteMessage.getData());
    }
    private void sendNotification(Map<String, String> data) {
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("Group_ID",data.get("Group_ID"));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, "NextMatch_Channel_ID")
                        //.setSmallIcon(R.drawable.myfootspace)
                        .setContentTitle(data.get("title"))
                        .setContentText(data.get("body")+"My Custom Notification!!")
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("NextMatch_Channel_ID",
                    "Next Match Details",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
