package com.dewidar.foreach.foreach;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class Profile_Activity extends MainActivity {

    private ImageView profileimage;
    private ImageView groundimage;
    private Button Gosetup;
    private Button GoMain;
    private TextView username;
    private FirebaseAuth mauthh;
    private DatabaseReference mdatabseusers;
    //***********************************
    private DatabaseReference Database;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseLike;
    private DatabaseReference mDatabaseCurrentUser;
    private DatabaseReference mDatabaseCounterLike;
    private Query mQueryCurrentUser;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private boolean mProcessLike = false;
    private int counter;
    private RecyclerView PostList;
    private FloatingActionButton floatingActionButton;

    private FirebaseRecyclerAdapter<Post, PostViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_);

        mAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null) {

                    Intent loginIntent = new Intent(Profile_Activity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };
//dewa work******************************************************************************

        Gosetup = (Button) findViewById(R.id.gosetup);

        mauthh = FirebaseAuth.getInstance();
        String user_id = getIntent().getExtras().get("user_id").toString();
        if (mAuth.getCurrentUser().getUid().equals(user_id)) {

            Gosetup.setVisibility(View.VISIBLE);

        }

        profileimage = (ImageView) findViewById(R.id.imageprofilepic);
        groundimage = (ImageView) findViewById(R.id.imageground);
        username = (TextView) findViewById(R.id.textNameprof);
        floatingActionButton = findViewById(R.id.profile_floating_action_button);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent In = new Intent(Profile_Activity.this, PostActivity.class);
                In.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(In);
            }
        });

//        GoMain=(Button)findViewById(R.id.button4);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        mdatabseusers = ref.child("users").child(user_id).child("name");
        mdatabseusers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.getValue(String.class);
                username.setText(name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mdatabseusers = ref.child("users").child(user_id).child("image");
        mdatabseusers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String imagee = dataSnapshot.getValue(String.class);
                Picasso.get().load(imagee).into(profileimage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mdatabseusers = ref.child("users").child(user_id).child("ground");
        mdatabseusers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String ground = dataSnapshot.getValue(String.class);
                Picasso.get().load(ground).into(groundimage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        Gosetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Profile_Activity.this, SetupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });



        mAuth = FirebaseAuth.getInstance();


        Database = FirebaseDatabase.getInstance().getReference().child("Post");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Like");
        //  String currentUserId=mAuth.getCurrentUser().getUid();

        mDatabaseCurrentUser = FirebaseDatabase.getInstance().getReference().child("Post");
        mQueryCurrentUser = mDatabaseCurrentUser.orderByChild("uid").equalTo(user_id);


        mDatabaseUsers.keepSynced(true);
        mDatabaseLike.keepSynced(true);
        Database.keepSynced(true);

        PostList = (RecyclerView) findViewById(R.id.profile_post_list);
        PostList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        PostList.setLayoutManager(linearLayoutManager);

        loadProfilePosts();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthStateListener);
        firebaseRecyclerAdapter.startListening();
        PostList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        View view;
        ImageView mlikeBtn;
        DatabaseReference mDatabaseLike;
        DatabaseReference Database;
        FirebaseAuth mAuth;


        public PostViewHolder(View itemView) {
            super(itemView);

            view = itemView;
            mlikeBtn = (ImageView) view.findViewById(R.id.like_btn);
            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Like");
            Database = FirebaseDatabase.getInstance().getReference().child("Post");
            mAuth = FirebaseAuth.getInstance();
            mDatabaseLike.keepSynced(true);

        }


        public void setLikeBtn(final String post_key) {

            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (mAuth.getCurrentUser() != null) {

                        if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

                            mlikeBtn.setImageResource(R.drawable.favorit);

                        } else {

                            mlikeBtn.setImageResource(R.drawable.unfav);

                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }

        public void setDesc(String desc) {

            TextView post_desc = (TextView) view.findViewById(R.id.post_desc);
            post_desc.setText(desc);
        }
        public void setDate(String date) {

            TextView post_date = (TextView) view.findViewById(R.id.textDate);
            post_date.setText(date);
        }
        public void setCounter(String counter) {

            TextView post_like = (TextView) view.findViewById(R.id.likesCount);
            post_like.setText(counter);
        }

        public void setUsername(String username) {

            TextView post_username = (TextView) view.findViewById(R.id.post_username);
            post_username.setText(username);

        }

        public void setImage(final Context context, final String image) {

            final ImageView post_image = (ImageView) view.findViewById(R.id.post_image);
            //Picasso.with(context).load(image).into(post_image);

            Picasso.get().load(image).into(post_image);


        }

        public void setUserImage(final Context c, String post_key) {

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

            DatabaseReference s = ref.child("Post").child(post_key).child("userimage");
            s.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final String imagee = dataSnapshot.getValue(String.class);

                    final ImageView post_userImage = (ImageView) view.findViewById(R.id.user_Image);

                    Picasso.get().load(imagee).into(post_userImage);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void loadProfilePosts() {

        Query query = mQueryCurrentUser;

        FirebaseRecyclerOptions<Post> options =
                new FirebaseRecyclerOptions.Builder<Post>()
                        .setQuery(query, Post.class)
                        .build();
         firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {
            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_row,parent,false);
                return new PostViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final PostViewHolder viewHolder, int position, @NonNull Post model) {
                final String post_key = getRef(position).getKey();
                Log.i("positomn", String.valueOf(position));
                Log.i("post key", post_key);

                DatabaseReference likes = Database.child(post_key).child("likes");
                likes.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        counter = dataSnapshot.getValue(Integer.class);
                        viewHolder.setCounter(String.valueOf(counter));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                viewHolder.setDesc(model.getDesc());
                viewHolder.setDate(model.getDate());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setUserImage(getApplicationContext(), post_key);
                viewHolder.setLikeBtn(post_key);

                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(MainActivity.this,post_key,Toast.LENGTH_LONG).show();

                        Intent singlePostIntent = new Intent(Profile_Activity.this, PostSingleActivity.class);
                        singlePostIntent.putExtra("Post_Id", post_key);
                        startActivity(singlePostIntent);
                    }
                });
                viewHolder.mlikeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mProcessLike = true;

                        DatabaseReference likes = Database.child(post_key).child("likes");
                        likes.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                counter = dataSnapshot.getValue(Integer.class);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        mDatabaseLike.addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (mProcessLike) {

                                    if (dataSnapshot.child(post_key).hasChild(mauthh.getCurrentUser().getUid())) {

                                        counter--;
                                        //viewHolder.setLikesCount(counter);
                                        Database.child(post_key).child("likes").setValue(counter);

                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();

                                        mProcessLike = false;

                                    } else {

                                        counter++;
                                        //viewHolder.setLikesCount(counter);

                                        Database.child(post_key).child("likes").setValue(counter);

                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("random value");

                                        mProcessLike = false;
                                    }

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }

                        });
                    }
                });


            }
        };

    }
}