package com.dewidar.foreach.foreach;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView PostList;
    private LinearLayoutManager mLayoutManager;
    static boolean active = false;

    private DatabaseReference Database;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseLike;
    //    private static FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    public final String mypreference = "mypref";
    private SharedPreferences sharedpreferences;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar loadingMoreDataProgressBar;
    private List<Post> postsList = new ArrayList<>();
    private PostsAdapter postsAdapter;
    public static boolean upOrDown;
    private DatabaseReference commentRef;

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private final WeakReference<FirebaseAuth> mAuth = new WeakReference<FirebaseAuth>(FirebaseAuth.getInstance());
    ;
    private final WeakReference<MainActivity> mainActivityWeakReference = new WeakReference<MainActivity>(MainActivity.this);
    ;
    private ValueEventListener mDatabaseUsersValueEventListener;


    private int limit = 15;
    private int start = 0;
    //to know if reach the last data of database
    private boolean firstTime = true;
    private long isThereNewPosts = 0;
    private int numOfPosts = 0;

    private FloatingActionButton floatingActionButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-1081849494088451/8566184685");
//        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });
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
                            numOfPosts = (int) dataSnapshot.getChildrenCount();
                        } else if (numOfPosts != 0 && dataSnapshot.getChildrenCount() < numOfPosts){
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }else {
                            Toast.makeText(mainActivityWeakReference.get(), "no more posts yet ):", Toast.LENGTH_SHORT).show();
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
                    Intent In = new Intent(mainActivityWeakReference.get(), PostActivity.class);
                    In.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(In);
                } else if (!upOrDown) {
                    mLayoutManager.smoothScrollToPosition(PostList, new RecyclerView.State(), 0);
                }
            }
        });
    }

    private void prepareDatabase() {

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null) {

                    Intent loginIntent = new Intent(mainActivityWeakReference.get(), LoginActivity.class);
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
        mLayoutManager = new LinearLayoutManager(mainActivityWeakReference.get());


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
        active= true;
        checksUserExist();
        mAuth.get().addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        active =false;
        try {
            mAuth.get().removeAuthStateListener(mAuthStateListener);
            mDatabaseUsers.removeEventListener(mDatabaseUsersValueEventListener);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checksUserExist() {


        if (mAuth.get().getCurrentUser() != null) {

            final String user_id = mAuth.get().getCurrentUser().getUid();
            mDatabaseUsersValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {

                        Intent setupIntent = new Intent(mainActivityWeakReference.get(), SetupActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mDatabaseUsers.addValueEventListener(mDatabaseUsersValueEventListener);

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

            Intent intent = new Intent(mainActivityWeakReference.get(), Profile_Activity.class);
            intent.putExtra("user_id", mAuth.get().getCurrentUser().getUid());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        } else if (item.getItemId() == R.id.logoutBtn) {

            mAuth.get().signOut();
            Intent i = new Intent(mainActivityWeakReference.get(), LoginActivity.class);
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

    private void loadPostsPagination(final Query query) {

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
                postsAdapter.setAds(mInterstitialAd);
                postsAdapter.setFloatingActionButton(floatingActionButton);
                PostList.setAdapter(postsAdapter);
                query.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                query.removeEventListener(this);

            }
        });

    }

    private void loadMorPosts(final Query query, final int start, final long allPostsCount) {

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
                    query.removeEventListener(this);
                } else {
                    loadingMoreDataProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(mainActivityWeakReference.get(), "No more Posts", Toast.LENGTH_SHORT).show();
                    query.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                query.removeEventListener(this);
            }
        });
    }

    private void refreshForNewPosts(final Query q) {
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
                q.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                q.removeEventListener(this);

            }
        });
    }

}

