package com.example.care_share;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.care_share.ProfileDetailsActivity.rotateImage;

public class SettingsActivity extends AppCompatActivity {
    CircleImageView dp;
    EditText name,age,modelName,regNo,manYr;
    ImageView dlFront,dlBack,carImage;
    ParseQuery<ParseObject> query;
    String phoneNumber= FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(),imageURL;
    StorageReference imageRef= FirebaseStorage.getInstance().getReference().child("ProfilePhoto/"+phoneNumber);
    StorageReference RefFront= FirebaseStorage.getInstance().getReference().child("DLFront/"+phoneNumber);
    StorageReference RefBack= FirebaseStorage.getInstance().getReference().child("DLBack/"+phoneNumber);
    StorageReference CarRef;
    RecyclerView recyclerView;
    Bitmap bitmap,rotatedBitmap;
    ArrayList<String> VehicleDetails=new ArrayList<>(), VehicleUrls=new ArrayList<>(),RegNos=new ArrayList<>();
    View myView;
    boolean mydialog=false;
    VehicleViewAdapter adapter;
    int selected=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        dp=findViewById(R.id.circleImageView);
        name=findViewById(R.id.editText3);age=findViewById(R.id.editText4);
        dlFront=findViewById(R.id.LicenseFront);dlBack=findViewById(R.id.LicenseBack);
        adapter=new VehicleViewAdapter(VehicleDetails,VehicleUrls,getApplicationContext());
        query=ParseQuery.getQuery("Users");
        query.whereEqualTo("PhoneNo",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null) {
                        for(ParseObject object:objects){
                            name.setText(object.getString("Username"));
                            String myage=object.getString("Age");
                            if(myage!=null)
                            age.setText(myage);
                        }
                }
            }
        });

        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(dp);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                try{
                    Toast.makeText(SettingsActivity.this, "Display Picture has not been set!", Toast.LENGTH_SHORT).show();}catch (Exception el){
                    el.printStackTrace();
                }
            }
        });
        RefFront.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(dlFront);
            }
        });

        RefBack.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(dlBack);
            }
        });

        dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected=1;
                takePictureIntent();
            }
        });
        dlFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected=2;
                takePictureIntent();
            }
        });
        dlBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected=3;
                takePictureIntent();
            }
        });
        ParseQuery<ParseObject> query=ParseQuery.getQuery("Vehicles");
        query.whereEqualTo("PhoneNo",phoneNumber);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null) {
                    for(ParseObject object:objects) {
                        VehicleDetails.add(object.getString("ModelName") + "\n" + object.getString("RegNo") + "\n" + object.getString("ManYr"));
                        VehicleUrls.add(object.getString("VehicleURL"));
                        RegNos.add(object.getString("RegNo"));
                    }
                    adapter.notifyDataSetChanged();
                }
                }
    });
        initRecyclerView();
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, final int position) {
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Remove Vehicle")
                        .setMessage("Do you want to remove this vehicle?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String myreg = RegNos.get(position);
                                RegNos.remove(position);
                                VehicleDetails.remove(position);
                                VehicleUrls.remove(position);
                                adapter.notifyDataSetChanged();
                                ParseQuery<ParseObject> query = ParseQuery.getQuery("Vehicles");
                                query.whereEqualTo("RegNo", myreg);
                                query.findInBackground(new FindCallback<ParseObject>() {
                                    @Override
                                    public void done(List<ParseObject> objects, ParseException e) {
                                        if (e == null) {
                                            for(ParseObject object:objects){
                                                if(object.getString("PhoneNo").equalsIgnoreCase(phoneNumber))
                                                    object.deleteInBackground();
                                            }
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }));
    }
    private void initRecyclerView() {
        recyclerView = findViewById(R.id.vehiclesrecycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    public void takePictureIntent () {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        else
            getphoto();
    }
    public void getphoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Toast.makeText(this, "Loading of image takes a while\nDo not select the image twice!", Toast.LENGTH_LONG).show();
        startActivityForResult(intent, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getphoto();
        }
    }
    @Override
    protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        try {
            Uri selectedImage=data.getData();
            if(requestCode==1 && resultCode==RESULT_OK && data!=null) {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                InputStream input = getContentResolver().openInputStream(selectedImage);
                ExifInterface ei;
                if (Build.VERSION.SDK_INT > 23)
                    ei = new ExifInterface(input);
                else
                    ei = new ExifInterface(selectedImage.getPath());
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);


                switch (orientation) {

                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotatedBitmap = rotateImage(bitmap, 90);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotatedBitmap = rotateImage(bitmap, 180);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotatedBitmap = rotateImage(bitmap, 270);
                        break;

                    case ExifInterface.ORIENTATION_NORMAL:
                    default:
                        rotatedBitmap = bitmap;
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        switch (selected){
            case 1:Glide.with(getApplicationContext()).load(rotatedBitmap).into(dp);
                    if(rotatedBitmap!=null)
                    uploadImageAndSaveUri(rotatedBitmap,imageRef);
                    return;
            case 2:Glide.with(getApplicationContext()).load(rotatedBitmap).into(dlFront);
                if(rotatedBitmap!=null)
                    uploadImageAndSaveUri(rotatedBitmap,RefFront);
                    return;
            case 3:Glide.with(getApplicationContext()).load(rotatedBitmap).into(dlBack);
                if(rotatedBitmap!=null)
                    uploadImageAndSaveUri(rotatedBitmap,RefBack);
                    return;
            case 4:Glide.with(getApplicationContext()).load(rotatedBitmap).into(carImage);
                return;
        }
    }
    public void uploadImageAndSaveUri(Bitmap imageBitmap,StorageReference ref) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask=ref.putBytes(data);
        if(mydialog){
            mydialog=false;
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        CarRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> uriTask) {
                                if(uriTask.getResult()!=null){
                                    imageURL = uriTask.getResult().toString();
                                    VehicleUrls.add(imageURL);
                                    VehicleDetails.add(modelName.getText().toString()+"\n"+regNo.getText().toString()+"\n"+manYr.getText().toString());
                                    adapter.notifyDataSetChanged();
                                    RegNos.add(regNo.getText().toString());
                                    ParseObject vehicles = new ParseObject("Vehicles");
                                    vehicles.put("ModelName", modelName.getText().toString());
                                    vehicles.put("RegNo", regNo.getText().toString());
                                    vehicles.put("ManYr", manYr.getText().toString());
                                    vehicles.put("PhoneNo",phoneNumber);
                                    vehicles.put("VehicleURL",imageURL);
                                    vehicles.put("Owner",name.getText().toString());
                                    vehicles.saveInBackground();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null)
                    for (ParseObject object : objects) {
                        object.put("Username", name.getText().toString());
                        object.put("Age",age.getText().toString());
                        object.saveInBackground();
                    }
                Toast.makeText(SettingsActivity.this, "Saved settings successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void addVehicle(View view){
        myView=view;
        rotatedBitmap=null;
        final AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        LayoutInflater inflater = SettingsActivity.this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_vehicle,null);
        modelName=dialogView.findViewById(R.id.editText5);
        regNo=dialogView.findViewById(R.id.editText6);
        manYr=dialogView.findViewById(R.id.editText7);
        builder.setView(dialogView);
        builder.setCancelable(false);
        carImage=dialogView.findViewById(R.id.imageView3);
        carImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected=4;
                takePictureIntent();
            }
        });
        builder.setCancelable(false);
        builder.setPositiveButton("Add Vehicle", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(regNo.getText().length()==0){
                    addVehicle(myView);
                    regNo.setError("Enter Reg. No.");regNo.requestFocus();
                }
                else{
                CarRef= FirebaseStorage.getInstance().getReference().child("CarImages/"+phoneNumber+"/"+regNo.getText().toString());
                if(rotatedBitmap!=null){
                    mydialog=true;
                uploadImageAndSaveUri(rotatedBitmap,CarRef);}
                else{
                    VehicleUrls.add("");
                    VehicleDetails.add(modelName.getText().toString()+"\n"+regNo.getText().toString()+"\n"+manYr.getText().toString());
                    adapter.notifyDataSetChanged();
                    RegNos.add(regNo.getText().toString());
                    ParseObject vehicles = new ParseObject("Vehicles");
                    vehicles.put("ModelName", modelName.getText().toString());
                    vehicles.put("RegNo", regNo.getText().toString());
                    vehicles.put("ManYr", manYr.getText().toString());
                    vehicles.put("PhoneNo",phoneNumber);
                    vehicles.put("VehicleURL","");
                    vehicles.put("Owner",name.getText().toString());
                    vehicles.saveInBackground();
                }
                    }
            }
        }).setNeutralButton("Cancel",null);
        builder.show();
    }
    public void allRides(View view){
        Intent intent=new Intent(getApplicationContext(),AllRidesActivity.class);
        startActivity(intent);
    }
}