package com.example.monko.foreach;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends FragmentActivity {

    private EditText mNameField;
    private EditText mmopilenumber;
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mConnfirmpassword;
    private TextView mSignIn;
    static String Birthdat;

    private Button mRegisterBtn;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ProgressDialog mProgress;

    public void setdatebtn(View view) {
        PickerDialog pickerDialog = new PickerDialog();
        pickerDialog.show(getSupportFragmentManager(), "date_picker");

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        mProgress = new ProgressDialog(this);

        mNameField = (EditText) findViewById(R.id.nameField);
        mmopilenumber = (EditText) findViewById(R.id.mopilenumber);
        mEmailField = (EditText) findViewById(R.id.emailField);
        mPasswordField = (EditText) findViewById(R.id.passwordField);
        mConnfirmpassword = (EditText) findViewById(R.id.confirmpassword);
        mRegisterBtn = (Button) findViewById(R.id.registerBtn);
        mSignIn = (TextView) findViewById(R.id.signInTxt);

        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                signInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(signInIntent);
            }
        });

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRegister();
            }
        });

    }

    private void startRegister() {

        final String name = mNameField.getText().toString().trim();
        final String number = mmopilenumber.getText().toString().trim();
        String email = mEmailField.getText().toString().trim();

        Pattern p = Pattern.compile("(.*?)@");
        final Matcher m = p.matcher(email);

        String password = mPasswordField.getText().toString().trim();
        String Connfirmpassword = mConnfirmpassword.getText().toString().trim();


        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(number) && !TextUtils.isEmpty(Birthdat) && !TextUtils.isEmpty(Connfirmpassword)) {
            if (number.length() == 11) {


                mProgress.setMessage("Signing Up.....");
                mProgress.show();

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            String user_id = mAuth.getCurrentUser().getUid();

                            DatabaseReference current_user_db = mDatabase.child(user_id);
                            current_user_db.child("name").setValue(name);
                            current_user_db.child("image").setValue("default");
                            /*
                            if (number.length() == 11) {
                                current_user_db.child("number").setValue(number);
                            } else if (number.length() != 11) {
                                Toast.makeText(getApplicationContext(), "Wrong number", Toast.LENGTH_LONG).show();

                            }
                            while (m.find()) {
                                current_user_db.child("emailkey").setValue(m.group(1));

                            }
                            if (!TextUtils.isEmpty(Birthdat)) {
                                current_user_db.child("birth").setValue(Birthdat);

                            }   */


                            mProgress.dismiss();

                            Intent mainIntent = new Intent(RegisterActivity.this, SetupActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(mainIntent);

                        } else {

                            mProgress.dismiss();
                            Toast.makeText(getApplicationContext(), "It's Already Exits", Toast.LENGTH_LONG).show();

                        }

                    }
                });

            } else {
                Toast.makeText(getApplicationContext(), "wrong number", Toast.LENGTH_LONG).show();

            }

        } else {

            Toast.makeText(getApplicationContext(), "fill all fields", Toast.LENGTH_LONG).show();

        }

    }

}