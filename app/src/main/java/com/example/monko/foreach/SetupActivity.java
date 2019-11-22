package com.example.monko.foreach;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetupActivity extends AppCompatActivity {
    private ImageButton msetupimagebtn;
    private ImageButton backgroundimagebtn;
    private EditText mNamefiled;
    private Button mSubmitbtn;

    private Uri mimageUri = null;
    private Uri mimageGrounduri = null;

    private static final int GALLERY_REQUIST = 1;
    private static final int GALLERY = 1;

    private FirebaseAuth mauth;
    private DatabaseReference mdatabaseusers;
    private StorageReference msorageImage;
    private StorageReference mstorGroundImage;

    private ProgressDialog mprogressDialog;
    private static boolean profileImage;
    private static boolean backgroundImage;

    private Boolean stat1 = false;
    private Boolean stat2 = false;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mauth = FirebaseAuth.getInstance();
        mdatabaseusers = FirebaseDatabase.getInstance().getReference().child("users");
        msorageImage = FirebaseStorage.getInstance().getReference().child("profile_images");
        mstorGroundImage = FirebaseStorage.getInstance().getReference().child("profile_ground");
        mprogressDialog = new ProgressDialog(this);


        msetupimagebtn = (ImageButton) findViewById(R.id.setupimagebtn);
        backgroundimagebtn = (ImageButton) findViewById(R.id.imagebackground);

        mNamefiled = (EditText) findViewById(R.id.setupNamefiled);
        mSubmitbtn = (Button) findViewById(R.id.SetupSubmitbtn);
//        backToprofile=(Button)findViewById(R.id.backprofile);

       /* backToprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetupActivity.this, Profile_Activity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });*/

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
        final String user_id = mauth.getCurrentUser().getUid();
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
                            }
                        });


                    }
                });
            }
            if (mimageGrounduri != null) {
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

            Intent intent = new Intent(SetupActivity.this, Profile_Activity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

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
                                    finish();
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
                                    finish();
                                }
                            });

                        }

                    } while (profileImage != true || backgroundImage != true);
                }
            }
        }).start();
    }

    private void goToProfile() {
        Intent intent = new Intent(SetupActivity.this, Profile_Activity.class);
        intent.putExtra("user_id", mauth.getCurrentUser().getUid());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}