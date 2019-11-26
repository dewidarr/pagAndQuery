package com.example.monko.foreach;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView PostList;
    private LinearLayoutManager mLayoutManager;

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
    public final String mypreference = "mypref";
    private SharedPreferences sharedpreferences;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    //    private FirebaseRecyclerPagingAdapter<Post, PostViewHolder> firebaseRecyclerAdapter;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapterOptions;
    private ProgressBar loadingMoreDataProgressBar;
    private List<Post> postsList = new ArrayList<>();
    private PostsAdapter postsAdapter;
    public static boolean upOrDown;

    private int limit = 10;
    private int start = 0;
    //to know if reach the last data of database
    private boolean firstTime = true;

    private FloatingActionButton floatingActionButton;


    public void logOut(View view) {

        mAuth.signOut();
        // startActivity(new Intent(MainActivity.this, LoginActivity.class));
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadingMoreDataProgressBar = findViewById(R.id.loading_moreData_progress);
        sharedpreferences = getSharedPreferences(mypreference,
                Context.MODE_PRIVATE);

        if (sharedpreferences.contains("darkmood")) {
            if (sharedpreferences.getBoolean("darkmood", false) == true) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        }
        mSwipeRefreshLayout = findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onStart();
            }
        });
        floatingActionButton = findViewById(R.id.floating_action_bar);
        floatingActionButtonListner();

        prepareDatabase();
    }

    private void floatingActionButtonListner() {

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (upOrDown) {
                    Intent In = new Intent(MainActivity.this, PostActivity.class);
                    In.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(In);
                }else if (!upOrDown){
                    mLayoutManager.scrollToPositionWithOffset(0, 0);
                }
            }
        });
    }

    private void prepareDatabase() {
        mAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null) {

                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };

        Database = FirebaseDatabase.getInstance().getReference().child("Post");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Like");


        mDatabaseUsers.keepSynced(true);
        mDatabaseLike.keepSynced(true);
        Database.keepSynced(true);

        PostList = (RecyclerView) findViewById(R.id.post_list);
        PostList.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(MainActivity.this);


        PostList.setLayoutManager(mLayoutManager);
        PostList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

                if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN)) {
                    Query query1 = Database;
                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            start += limit + 1;
                            limit += 10;
                            Query query = Database.limitToLast(limit);
                            loadingMoreDataProgressBar.setVisibility(View.VISIBLE);
                            loadMorPosts(query, start, dataSnapshot.getChildrenCount());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }
        });
        Query query = Database.limitToLast(limit);
        loadPostsPagination(query);

    }

    @Override
    protected void onStart() {
        super.onStart();
        checksUserExist();
        mAuth.addAuthStateListener(mAuthStateListener);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        firebaseRecyclerAdapterOptions.stopListening();
    }

    private void checksUserExist() {


        if (mAuth.getCurrentUser() != null) {

            final String user_id = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {

                        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }


    public static class PostViewHolder extends RecyclerView.ViewHolder {

        View view;
        ImageView mlikeBtn;
        ImageView mUserImage;
        DatabaseReference mDatabaseLike;
        DatabaseReference Database;
        FirebaseAuth mAuth;


        public PostViewHolder(View itemView) {
            super(itemView);

            view = itemView;
            mlikeBtn = (ImageView) view.findViewById(R.id.like_btn);

            mUserImage = (ImageView) view.findViewById(R.id.user_Image);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.profileBtn) {

            Intent intent = new Intent(MainActivity.this, Profile_Activity.class);
            intent.putExtra("user_id", mAuth.getCurrentUser().getUid());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        } else if (item.getItemId() == R.id.logoutBtn) {

            mAuth.signOut();
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);

        } else {

            sharedpreferences = getSharedPreferences(mypreference,
                    Context.MODE_PRIVATE);

            if (sharedpreferences.contains("darkmood")) {
                if (sharedpreferences.getBoolean("darkmood", false) == true) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean("darkmood", false);
                    editor.apply();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                } else {
                    darkMood();
                }
            } else {
                darkMood();
            }


        }

        return super.onOptionsItemSelected(item);
    }

    private void darkMood() {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("darkmood", true);
        editor.apply();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    private void loadPostsPagination(Query query) {

     /*   FirebaseRecyclerOptions<Post> options =
                new FirebaseRecyclerOptions.Builder<Post>()
                        .setQuery(query, Post.class)
                        .build()*/
        ;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Post post = data.getValue(Post.class);
                    post.setKey(data.getKey());
                    postsList.add(post);
                }
                Collections.reverse(postsList);
                postsAdapter = new PostsAdapter(postsList);
                postsAdapter.setFloatingActionButton(floatingActionButton);
                PostList.setAdapter(postsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

/*        firebaseRecyclerAdapterOptions = new FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {


            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.post_row, parent, false);
                return new PostViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final PostViewHolder viewHolder, int position, @NonNull Post model) {
                final String post_key = getRef(position).getKey();

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

                        Intent singlePostIntent = new Intent(MainActivity.this, PostSingleActivity.class);
                        singlePostIntent.putExtra("Post_Id", post_key);

                        singlePostIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        startActivity(singlePostIntent);
                    }
                });

                viewHolder.mUserImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference likes = Database.child(post_key).child("uid");
                        likes.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String use = dataSnapshot.getValue(String.class);
                                Intent Intent = new Intent(MainActivity.this, Profile_Activity.class);
                                Intent.putExtra("user_id", use);
                                Intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(Intent);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


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

                                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

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
                                    notifyDataSetChanged();
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }

                        });
                    }
                });
            }


        };*/


//        firebaseRecyclerAdapterOptions.startListening();
//        PostList.setAdapter(firebaseRecyclerAdapterOptions);

        /*
         pagination
         */

    /*    Query baseQuery = Database.orderByChild("counter");
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(5)
                .setPageSize(5)
                .build();


        DatabasePagingOptions<Post> options = new DatabasePagingOptions.Builder<Post>()
                .setLifecycleOwner(this)
                .setQuery(baseQuery, config, Post.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerPagingAdapter<Post, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final PostViewHolder viewHolder, int position, @NonNull Post model) {
                final String post_key = getRef(position).getKey();

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

                        Intent singlePostIntent = new Intent(MainActivity.this, PostSingleActivity.class);
                        singlePostIntent.putExtra("Post_Id", post_key);

                        singlePostIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        startActivity(singlePostIntent);
                    }
                });

                viewHolder.mUserImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference likes = Database.child(post_key).child("uid");
                        likes.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String use = dataSnapshot.getValue(String.class);
                                Intent Intent = new Intent(MainActivity.this, Profile_Activity.class);
                                Intent.putExtra("user_id", use);
                                Intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(Intent);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


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

                                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

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
                                    notifyDataSetChanged();
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }

                        });
                    }
                });

            }

           *//* @Nullable
            @Override
            protected DataSnapshot getItem(int position) {
                return super.getItem(getItemCount() - 1 - position);
            }*//*

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {

                switch (state) {
                    case LOADING_MORE:
                        loadingMoreDataProgressBar.setVisibility(View.VISIBLE);
                    case LOADED:
                        Toast.makeText(MainActivity.this, "loaded", Toast.LENGTH_SHORT).show();
//                        loadingMoreDataProgressBar.setVisibility(View.INVISIBLE);

                }

            }

            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.post_row, parent, false);
                return new PostViewHolder(view);
            }
        };*/

    }

    private void loadMorPosts(Query query, final int start, final long allPostsCount) {

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (firstTime) {
                    if (dataSnapshot.getChildrenCount() == allPostsCount) {
                        firstTime = false;
                    }
                    List<Post> morePosts = new ArrayList<>();
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        Post post = data.getValue(Post.class);
                        post.setKey(data.getKey());
                        morePosts.add(post);
                    }
                    List<Post> reverseList = morePosts.subList(0, 10);
                    Collections.reverse(reverseList);
                    postsAdapter.setPostsList(reverseList);
                    loadingMoreDataProgressBar.setVisibility(View.INVISIBLE);
                } else {
                    loadingMoreDataProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, "No more Posts", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

