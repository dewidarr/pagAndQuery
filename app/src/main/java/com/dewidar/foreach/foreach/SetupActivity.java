package com.dewidar.foreach.foreach;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.lang.ref.WeakReference;

public class SetupActivity extends AppCompatActivity {
    private ImageButton msetupimagebtn;
    private ImageButton backgroundimagebtn;
    private EditText mNamefiled;
    private Button mSubmitbtn;

    private Uri mimageUri = null;
    private Uri mimageGrounduri = null;

    private static final int GALLERY_REQUIST = 1;
    private static final int GALLERY = 1;

    private final WeakReference<FirebaseAuth> mauth = new WeakReference<FirebaseAuth>(FirebaseAuth.getInstance());
    private DatabaseReference mdatabaseusers;
    private StorageReference msorageImage;
    private StorageReference mstorGroundImage;

    private ProgressDialog mprogressDialog;
    private static boolean profileImage;
    private static boolean backgroundImage;

    private Boolean stat1 = false;
    private Boolean stat2 = false;
    private Handler handler = new Handler();
    private Toolbar toolbar;

    private static final String TAG = "SetupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mdatabaseusers = FirebaseDatabase.getInstance().getReference().child("users");
        msorageImage = FirebaseStorage.getInstance().getReference().child("profile_images");
        mstorGroundImage = FirebaseStorage.getInstance().getReference().child("profile_ground");
        mprogressDialog = new ProgressDialog(this);


        msetupimagebtn = findViewById(R.id.setupimagebtn);
        backgroundimagebtn = findViewById(R.id.imagebackground);

        mNamefiled = findViewById(R.id.setupNamefiled);
        mSubmitbtn = findViewById(R.id.SetupSubmitbtn);


        mSubmitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mprogressDialog.setMessage("Updating Account..");
                mprogressDialog.show();
                startSetupAccount();
            }
        });

        msetupimagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryintent = new Intent();
                galleryintent.setAction(Intent.ACTION_GET_CONTENT);
                galleryintent.setType("image/+");
                startActivityForResult(galleryintent, GALLERY_REQUIST);
                stat1 = true;
                stat2 = false;

            }
        });
        backgroundimagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent();
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/+");
                startActivityForResult(gallery, GALLERY);
                stat2 = true;
                stat1 = false;
            }
        });

        toolbar = findViewById(R.id.edit_profile_toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (stat1 == true) {
            if (requestCode == GALLERY_REQUIST && resultCode == RESULT_OK) {
                Uri imageUri = data.getData();
                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this);
            }
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    mimageUri = result.getUri();
                    msetupimagebtn.setImageURI(mimageUri);

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }
        }
        if (stat2 == true) {
            if (requestCode == GALLERY && resultCode == RESULT_OK) {
                Uri imageUr = data.getData();
                CropImage.activity(imageUr)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(3, 2)
                        .start(this);
            }
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    mimageGrounduri = result.getUri();
                    backgroundimagebtn.setImageURI(mimageGrounduri);

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }
        }


    }

    private void startSetupAccount() {
        final String name = mNamefiled.getText().toString().trim();
        final String user_id = mauth.get().getCurrentUser().getUid();
        if (mimageUri != null || mimageGrounduri != null || !TextUtils.isEmpty(name)) {

            if (mimageUri != null && mimageGrounduri != null) {
                waitUploadingFinish(false, true);
            } else if (mimageUri != null || mimageGrounduri != null) {
                if (!(mimageUri == null && mimageGrounduri == null)) {
                    waitUploadingFinish(true, false);
                }
            } else {
                mprogressDialog.dismiss();
                Toast.makeText(SetupActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
            }

            if (mimageUri != null) {
                /*
                 * delete old profile picture
                 * */

                deleteOldImage("image");

                /*
                 * update the new selected profile picture py user
                 * **/
                final StorageReference filepath = msorageImage.child(mimageUri.getLastPathSegment());
                filepath.putFile(mimageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        mdatabaseusers.child(user_id).child("image").setValue(uri.toString());
                                        profileImage = true;
                                        updatePostsOfThatUser(user_id,uri.toString());
                                    }
                                });


                            }
                        });
            }
            if (mimageGrounduri != null) {
                /*
                 * delete old background image
                 * */

                deleteOldImage("ground");

                /*
                 * update the new background image selected by user
                 * */
                final StorageReference file = mstorGroundImage.child(mimageGrounduri.getLastPathSegment());
                file.putFile(mimageGrounduri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                mdatabaseusers.child(user_id).child("ground").setValue(uri.toString());
                                backgroundImage = true;
                            }
                        });


                    }
                });
            }
            if (!TextUtils.isEmpty(name)) {
                mdatabaseusers.child(user_id).child("name").setValue(name);
            }
        }
        if (mimageUri == null && mimageGrounduri == null && TextUtils.isEmpty(name)) {
            Toast.makeText(this, "please upload your profile picture", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SetupActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

    }

    private void updatePostsOfThatUser(String user_id, final String image) {
        final Query query = FirebaseDatabase.getInstance().getReference().child("Post").orderByChild("uid").equalTo(user_id);

        new Thread(new Runnable() {
            @Override
            public void run() {
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try{

                            if (dataSnapshot.exists()&& dataSnapshot.getValue() != null){
                                for (DataSnapshot post : dataSnapshot.getChildren()){
                                    FirebaseDatabase.getInstance().getReference().child("Post").child(post.getKey()).child("userimage").setValue(image);
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        }).start();
    }

    private void deleteOldImage(String image) {
        mdatabaseusers.child(mauth.get().getCurrentUser().getUid()).child(image).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && dataSnapshot.exists()) {
                    try{
                        StorageReference mStorage = FirebaseStorage.getInstance().getReferenceFromUrl(String.valueOf(dataSnapshot.getValue()));
                        mStorage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i(TAG, "onSuccess: updated profile picture and removed old one from storage");
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void waitUploadingFinish(final boolean singleImage, final boolean multibleImage) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (singleImage) {
                    do {
                        if (profileImage || backgroundImage) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mprogressDialog.dismiss();
                                    Toast.makeText(SetupActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                                    goToProfile();
                                }
                            });
                        }
                    } while (!profileImage && !backgroundImage);

                } else if (multibleImage) {
                    do {
                        if (profileImage == true && backgroundImage == true) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mprogressDialog.dismiss();
                                    Toast.makeText(SetupActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                                    goToProfile();
                                }
                            });

                        }

                    } while (profileImage != true || backgroundImage != true);
                }
            }
        }).start();
    }

    private void goToProfile() {
        Log.i(TAG, "goToProfile: chked");
            Intent intent = new Intent(SetupActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
    }

}