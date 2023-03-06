package com.shahardror.wetrip;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import static androidx.core.content.ContextCompat.getDrawable;

public class ProfileFragment extends Fragment {

    AlertDialog bigImageDialog;
    File file;
    boolean canTakePicture = true;
    final int WRITE_PERMISSION_REQUEST =1;
    private FirebaseAuth firebaseAuth;
    private StorageReference mStorageRef  = FirebaseStorage.getInstance().getReference("users");
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference usersTable = database.getReference("users");
    CircleImageView userImage ;
    ImageButton addFriendImage , openChatImage;
    TextView ageTv , countryTv,genderTv;
    TextView userNameTextView;
    Bitmap bigImageBitmap;
    ProgressBar progressBar;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        firebaseAuth = FirebaseAuth.getInstance();

        View root = inflater.inflate(R.layout.profile_layout,container,false);
        userImage = root.findViewById(R.id.profile_layout_user_image);
        addFriendImage = root.findViewById(R.id.profile_layout_add_friend_btn);
        openChatImage = root.findViewById(R.id.profile_layout_chat_btn);
        userNameTextView = root.findViewById(R.id.profile_layout_user_name);
        ageTv = root.findViewById(R.id.profile_layout_user_age);
        countryTv = root.findViewById(R.id.profile_layout_user_country);
        genderTv = root.findViewById(R.id.profile_layout_user_gender);
        progressBar = root.findViewById(R.id.profile_layout_progress_bar);
        openChatImage.setVisibility(View.GONE);
        addFriendImage.setVisibility(View.GONE);
          // inject profile name
        String name = firebaseAuth.getCurrentUser().getDisplayName();
        userNameTextView.setText(name);
        // Logic Of Reload Image and Reload Text Description
        downloadProfileImage();




        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              //  Toast.makeText(getContext(), "basjhd", Toast.LENGTH_SHORT).show();
                openDialogFunction();

                if(Build.VERSION.SDK_INT >= 23){
                    int hasWritePermission = checkSelfPermission(getContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    if(hasWritePermission != PackageManager.PERMISSION_GRANTED){

                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_PERMISSION_REQUEST);
                        // Open Dialog
                        canTakePicture = false;

                    } else { canTakePicture = true; }
                } else { canTakePicture = true; }
            }
        });

        return root;
    }


    public void downloadProfileImage(){
        usersTable.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                if( dataSnapshot.hasChild("gender")){
                    genderTv.setText( dataSnapshot.child("gender").getValue().toString());
                }if ( dataSnapshot.hasChild("age")){
                    ageTv.setText( dataSnapshot.child("age").getValue().toString());
                }if( dataSnapshot.hasChild("country")){
                    countryTv.setText( dataSnapshot.child("country").getValue().toString());
                }

                if  ( dataSnapshot.hasChild("photo") ){ // if user have profile Image
                   final DataSnapshot dataSnapshot1 = dataSnapshot.child("photo");
                    String imageStr = "";
                    if(firebaseAuth.getCurrentUser() != null){
                        imageStr = "https://firebasestorage.googleapis.com/v0/b/wetrip-id.appspot.com/o/users%2F"+firebaseAuth.getCurrentUser().getUid() +".jpg?alt=media&token=48fe0e1f-9787-4734-91d0-bd4fcf609249";
                   }

                    if(getContext() != null){
                        RequestQueue requestQueueImage = Volley.newRequestQueue(getContext());
                        ImageRequest imageRequest = new ImageRequest(imageStr, new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap response) {

                                if(dataSnapshot1.child("fromExternal").getValue().toString().equals("false")){
                                    response = RotateBitmap(response , 90);
                                }
                                userImage.setImageBitmap(response);
                                bigImageBitmap = response;

                            }
                        }, 2000, 2000, ImageView.ScaleType.FIT_XY, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //Toast.makeText(getContext(), "ata ba kivon ahi", Toast.LENGTH_SHORT).show();
                            }
                        });
                        requestQueueImage.add(imageRequest);
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                           }
        });
    }

    public void openDialogFunction(){ // Profile Image Dialog here

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.edit_image_layout,null);
        ImageView imageViewProfileImageBig = dialogView.findViewById(R.id.profile_image_edit_layout);
        ImageButton imageButtonBack = dialogView.findViewById(R.id.back_btn_edit_layout);
        ImageButton imageButtonEditImage = dialogView .findViewById(R.id.edit_profile_image_edit_layout);
        builder.setView(dialogView);
        bigImageDialog = builder.create();
        bigImageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        bigImageDialog.show();

        imageViewProfileImageBig.setImageBitmap(bigImageBitmap);
        imageButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bigImageDialog.dismiss();
            }
        });
        imageButtonEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(canTakePicture)
                openExternalInternalDialog();
            }
        });
    }

    public void openExternalInternalDialog(){ // How you wish to Take Photo From By camera or from external
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle(s(R.string.select_pic));
        alertDialog.setMessage(s(R.string.choose_files));
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,s(R.string.take_photo) ,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        file =   new File( Environment.getExternalStorageDirectory() , "pic.jpg");
                        Uri fileUri = Uri.fromFile(file);
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);
                        startActivityForResult(intent,1);
                        dialog.dismiss();
                        bigImageDialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, s(R.string.my_files),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent , 2);
                        dialog.dismiss();
                        bigImageDialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();

        matrix.postRotate(angle);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(source, source.getWidth(), source.getHeight(), true);

        return Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == WRITE_PERMISSION_REQUEST){
            if(grantResults[0] !=  PackageManager.PERMISSION_GRANTED){
                MainActivity.safeToast.show(getContext(),s(R.string.no_premission));
                canTakePicture = false;
            }else{
                canTakePicture = true;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1 && RESULT_OK == resultCode){
            Picasso.with(getContext()).load(file).into(userImage);
            progressBar.setVisibility(View.VISIBLE);
            uploadFileFromCamera(Uri.fromFile(file));
            userImage.setVisibility(View.GONE);
        }
        else if(requestCode == 2 && resultCode == RESULT_OK ){
            Uri imageUri = data.getData();
            Log.i("tag" , imageUri.toString());
            progressBar.setVisibility(View.VISIBLE);
            userImage.setVisibility(View.GONE);
            Picasso.with(getContext()).load(imageUri).into(userImage);
            uploadFile(imageUri);
        }
    }

    private void uploadNew(){

        Bitmap bitmap  = ((BitmapDrawable)userImage.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] data = baos.toByteArray();

        mStorageRef.child(firebaseAuth.getCurrentUser().getUid() + ".png").putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //    progressBar.setVisibility(View.GONE);
                        Upload upload = new Upload(false , taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());
                        usersTable.child(firebaseAuth.getCurrentUser().getUid()).child("photo").setValue(upload);
                        MainActivity.safeToast.show(getContext(),s(R.string.image_upload));
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //     progressBar.setVisibility(View.GONE);
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

            }
        });
    }
    private void uploadFile(final Uri imageUriLocal ){

        if(imageUriLocal != null && firebaseAuth.getCurrentUser() !=null){
            InputStream inputStream ;
            try {
                inputStream = getContext().getContentResolver().openInputStream(imageUriLocal);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }

            Log.i("tag" , inputStream.toString());
            mStorageRef.child(firebaseAuth.getCurrentUser().getUid() + "." + getFileExtension(imageUriLocal))
                    .putStream(inputStream).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Upload upload = new Upload(true , taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());
                    usersTable.child(firebaseAuth.getCurrentUser().getUid()).child("photo").setValue(upload);
                    MainActivity.safeToast.show(getContext(),s(R.string.image_upload));
                    userImage.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {


                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                }
            });

        }else{
            MainActivity.safeToast.show(getContext(),s(R.string.no_file_select));
        }
    }

    private  String getFileExtension(Uri uri){
        ContentResolver cr = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return  mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }




    public void uploadFileFromCamera(final Uri uri){
       // progressBar.setVisibility(View.VISIBLE);
        mStorageRef.child(firebaseAuth.getCurrentUser().getUid() + ".jpg" ).putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //    progressBar.setVisibility(View.GONE);
                        Upload upload = new Upload(false , taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());
                        usersTable.child(firebaseAuth.getCurrentUser().getUid()).child("photo").setValue(upload);
                       // Picasso.with(getContext()).load(uri).into(userImage);
                        MainActivity.safeToast.show(getContext(),s(R.string.image_upload));
                        userImage.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

           //     progressBar.setVisibility(View.GONE);
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

            }
        });
    }

    private String s(int id)
    {
        return this.getResources().getString(id);
    }
    @Override
    public void onStart() {
        super.onStart();
        getActivity().findViewById(R.id.floating_button).setVisibility(View.GONE);
        getActivity().findViewById(R.id.refresh_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.find_appropriate_trips_btn_main_activity).setVisibility(View.GONE);

        NavigationView navigationView = getActivity().findViewById(R.id.navigation_view);
        navigationView.setCheckedItem(R.id.item_profile);
        MainActivity.actionBar.setTitle(s(R.string.my_profile));
    }
}
