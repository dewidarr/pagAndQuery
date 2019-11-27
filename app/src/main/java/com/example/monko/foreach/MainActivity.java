package com.example.monko.foreach;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    public final String mypreference = "mypref";
    private SharedPreferences sharedpreferences;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar loadingMoreDataProgressBar;
    private List<Post> postsList = new ArrayList<>();
    private PostsAdapter postsAdapter;
    public static boolean upOrDown;
    private DatabaseReference commentRef;

    private int limit = 15;
    private int start = 0;
    //to know if reach the last data of database
    private boolean firstTime = true;
    private long isThereNewPosts = 0;
    private int numOfPosts = 0;

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
                Database.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (numOfPosts != 0 && dataSnapshot.getChildrenCount() > numOfPosts) {
                            Query query
                                    = Database.limitToLast((int) (dataSnapshot.getChildrenCount() - numOfPosts));
                            refreshForNewPosts(query);
                        } else {
                            Toast.makeText(MainActivity.this, "no more posts yet ):", Toast.LENGTH_SHORT).show();
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                postsAdapter.notifyDataSetChanged();
            }
        });
        floatingActionButton = findViewById(R.id.floating_action_bar);
        floatingActionButtonListner();

        prepareDatabase();

        saveNumberOfPosts();
    }

    private void saveNumberOfPosts() {
        Query q = Database;
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    numOfPosts = (int) dataSnapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void floatingActionButtonListner() {

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (upOrDown) {
                    Intent In = new Intent(MainActivity.this, PostActivity.class);
                    In.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(In);
                } else if (!upOrDown) {
                    mLayoutManager.smoothScrollToPosition(PostList, new RecyclerView.State(), 0);
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
        commentRef = FirebaseDatabase.getInstance().getReference().child("Comment");


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
                            limit += 15;
                            Query query = Database.limitToLast(limit);
                            loadingMoreDataProgressBar.setVisibility(View.VISIBLE);
                            isThereNewPosts = dataSnapshot.getChildrenCount();
                            loadMorPosts(query, start, dataSnapshot.getChildrenCount());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else if (mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    floatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.logo_transparent));
                    upOrDown = true;
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

    }

    private void loadMorPosts(Query query, final int start, final long allPostsCount) {

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int lastPosts = 0;
                if (firstTime) {
                    if (dataSnapshot.getChildrenCount() == allPostsCount) {
                        firstTime = false;
                        lastPosts = (int) (allPostsCount - (limit - 15));
                    }
                    List<Post> morePosts = new ArrayList<>();
                    if (lastPosts == 0) {
                        lastPosts = 15;
                    }
                    int fromTo = 0;
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (fromTo < lastPosts) {
                            Post post = data.getValue(Post.class);
                            post.setKey(data.getKey());
                            morePosts.add(post);
                            fromTo += 1;
                        } else {
                            break;
                        }

                    }
                    Collections.reverse(morePosts);
                    postsAdapter.setPostsList(morePosts);
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

    private void refreshForNewPosts(Query q) {
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Post> morePosts = new ArrayList<>();
                int first = 0;
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Post post = data.getValue(Post.class);
                    post.setKey(data.getKey());
                    morePosts.add(post);
                    first += 1;
                }
                Collections.reverse(morePosts);
                postsAdapter.refreshPosts(morePosts);
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

