package com.example.monko.foreach;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PostSingleActivity extends AppCompatActivity {

    private Button push;
    private EditText writeComment;

    private RecyclerView commentList;

    private String mPost_key = null;

    private DatabaseReference Database;
    private DatabaseReference mDatabaseLike;
    private DatabaseReference mDatabaseComment;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDataBaseUser;
    private DatabaseReference mDatabaseCom;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private ImageView mPostSingleImage;
    private TextView mPostSingleDesc;
    private Button mPostRemoveBtn;
    String comment_key = "";

    private FirebaseRecyclerAdapter<Comment, commentViewHolder> firebaseRecyclerAdapter;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_single);

        mPost_key = getIntent().getExtras().getString("Post_Id");
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };
        if (mAuth.getCurrentUser() != null) {
            mCurrentUser = mAuth.getCurrentUser();


            push = (Button) findViewById(R.id.pushComment);
            writeComment = (EditText) findViewById(R.id.writeComment);
            push.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    createComment();
                }
            });


            Database = FirebaseDatabase.getInstance().getReference().child("Post");
            mDataBaseUser = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentUser.getUid());
            Database.keepSynced(true);

            commentList = (RecyclerView) findViewById(R.id.comment_list);
            commentList.setHasFixedSize(true);
            commentList.setLayoutManager(new LinearLayoutManager(this));


            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Like");
            mDatabaseComment = FirebaseDatabase.getInstance().getReference().child("Comment");
            mDatabaseCom = mDatabaseComment.child(mPost_key);

        }


        mPostSingleImage = (ImageView) findViewById(R.id.singlePostImage);
        mPostSingleDesc = (TextView) findViewById(R.id.singlePostDesc);
        mPostRemoveBtn = (Button) findViewById(R.id.singleRemoveBtn);

        //Toast.makeText(getApplicationContext() , post_key , Toast.LENGTH_LONG).show();

        Database.child(mPost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String post_image = (String) dataSnapshot.child("image").getValue();
                String post_desc = (String) dataSnapshot.child("desc").getValue();
                String post_uid = (String) dataSnapshot.child("uid").getValue();

                mPostSingleDesc.setText(post_desc);

                Picasso.get().load(post_image).into(mPostSingleImage);
                if (mAuth.getCurrentUser().getUid().equals(post_uid)) {

                    mPostRemoveBtn.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });

        mPostRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Database.child(mPost_key).removeValue();
                mDatabaseLike.child(mPost_key).removeValue();
                mDatabaseComment.child(mPost_key).removeValue();

                Intent mainIntent = new Intent(PostSingleActivity.this, MainActivity.class);

                startActivity(mainIntent);
            }
        });

        loadSinglePost();

    }

    private void createComment() {
        //update comments counter in child "post"
        Query query = Database.child(mPost_key).child("commentsNumber");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             if (dataSnapshot.getValue() != null){
                 int num = dataSnapshot.getValue(Integer.class);
                 Database.child(mPost_key).child("commentsNumber").setValue(num +1);
             }
             else {
                 Database.child(mPost_key).child("commentsNumber").setValue(1);
             }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        final String comment = writeComment.getText().toString().trim();
        if (!TextUtils.isEmpty(comment)) {

            final DatabaseReference newComment = mDatabaseCom.push();

            mDataBaseUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    newComment.child("comm").setValue(comment);
                    newComment.child("uid").setValue(mCurrentUser.getUid());
                    newComment.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                            } else {
                                Toast.makeText(getApplicationContext(), "sorry try again later", Toast.LENGTH_LONG);

                            }

                        }
                    });
                    newComment.child("userimage").setValue(dataSnapshot.child("image").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                writeComment.setText("");
                            } else {
                                Toast.makeText(getApplicationContext(), "sorry try again later", Toast.LENGTH_LONG);

                            }

                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);

        firebaseRecyclerAdapter.startListening();
        commentList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    public static class commentViewHolder extends RecyclerView.ViewHolder {

        View view;
        DatabaseReference Database;
        FirebaseAuth mAuth;

        public commentViewHolder(View itemView) {
            super(itemView);

            view = itemView;
            Database = FirebaseDatabase.getInstance().getReference().child("Comment");
            mAuth = FirebaseAuth.getInstance();
        }

        public void setComment(String comment_key, String mPost_key) {


            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

            DatabaseReference s = ref.child("Comment").child(mPost_key).child(comment_key).child("comm");
            s.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final String comment = dataSnapshot.getValue(String.class);

                    TextView comment_desc = (TextView) view.findViewById(R.id.comComment);
                    comment_desc.setText(comment);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }


        public void setUsername(String comment_key, String mPost_key) {


            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

            DatabaseReference s = ref.child("Comment").child(mPost_key).child(comment_key).child("username");
            s.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final String username = dataSnapshot.getValue(String.class);

                    TextView comment_desc = (TextView) view.findViewById(R.id.comUsername);
                    comment_desc.setText(username);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }


        public void setUserImage(final Context c, String comment_key, String post_key) {

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

            DatabaseReference s = ref.child("Comment").child(post_key).child(comment_key).child("userimage");
            s.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final String imagee = dataSnapshot.getValue(String.class);

                    final ImageView comment_userImage = (ImageView) view.findViewById(R.id.comImage);

                    Picasso.get().load(imagee).into(comment_userImage);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void loadSinglePost() {
        Query query = mDatabaseCom;
        FirebaseRecyclerOptions<Comment> options =
                new FirebaseRecyclerOptions.Builder<Comment>()
                        .setQuery(query, Comment.class)
                        .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, commentViewHolder>(options) {

            @NonNull
            @Override
            public commentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.comment_row, parent, false);
                return new commentViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull commentViewHolder viewHolder, int position, @NonNull Comment comment) {
                final String comment_key = getRef(position).getKey();

                viewHolder.setComment(comment_key, mPost_key);
                viewHolder.setUsername(comment_key, mPost_key);
                viewHolder.setUserImage(getApplicationContext(), comment_key, mPost_key);
            }

        };

    }
}