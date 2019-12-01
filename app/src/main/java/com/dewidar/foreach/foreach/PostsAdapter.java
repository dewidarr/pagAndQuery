package com.dewidar.foreach.foreach;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

    private List<Post> postsList;
    private DatabaseReference Database = FirebaseDatabase.getInstance().getReference().child("Post");
    private int counter;
    private boolean mProcessLike = false;
    private DatabaseReference mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Like");
    private WeakReference<FirebaseAuth> mAuth = new WeakReference<FirebaseAuth>(FirebaseAuth.getInstance());
    private FloatingActionButton floatingActionButton;
    private boolean isPostExist = true;


    private InterstitialAd ads;
    int lastItemPosition = -1;

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

            mDatabaseLike.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (mAuth.getCurrentUser() != null) {

                        if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

                            mlikeBtn.setImageResource(R.drawable.favorit);

                        } else {

                            mlikeBtn.setImageResource(R.drawable.unfav);

                        }
                        mDatabaseLike.removeEventListener(this);
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

        public void setCommentsNumber(String number) {
            TextView post_comment = view.findViewById(R.id.comments_number);
            if (!number.equals("0")) {
                post_comment.setText(number);
            } else {
                post_comment.setText("");
            }
        }

        public void setDate(String date) {

            TextView post_date = (TextView) view.findViewById(R.id.textDate);
            post_date.setText(date);
        }

        public void setCounter(String counter) {
            TextView post_like = view.findViewById(R.id.likesCount);
            if (!counter.equals("0")) {
                post_like.setText(counter);
            } else {
                post_like.setText("");
            }
        }

        public void setUsername(String username) {

            TextView post_username = (TextView) view.findViewById(R.id.post_username);
            post_username.setText(username);

        }

        public void setImage(final String image) {
            final ImageView post_image = view.findViewById(R.id.post_image);

            Picasso.get().load(image).into(post_image);


        }

        public void setUserImage(String post_key) {

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

            final DatabaseReference s = ref.child("Post").child(post_key).child("userimage");
            s.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final String imagee = dataSnapshot.getValue(String.class);

                    final ImageView post_userImage = view.findViewById(R.id.user_Image);

                    Picasso.get().load(imagee).into(post_userImage);
                    s.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


    public PostsAdapter(List<Post> postsList) {
        this.postsList = postsList;
        mDatabaseLike.keepSynced(true);
        Database.keepSynced(true);
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_row, parent, false);

        return new PostViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull final PostViewHolder viewHolder, int position) {

        if (position > lastItemPosition) {
            // Scrolled Down
            floatingActionButton.setImageDrawable(viewHolder.view.getContext().getResources().getDrawable(R.drawable.logo_transparent));
            MainActivity.upOrDown = true;
        } else {
            floatingActionButton.setImageDrawable(viewHolder.view.getContext().getResources().getDrawable(R.drawable.ic_arrow_upward_black_24dp));
            MainActivity.upOrDown = false;
            // Scrolled Up
        }
        lastItemPosition = position;

        Post model = postsList.get(position);
        final String post_key = model.getKey();
        final DatabaseReference likes = Database.child(post_key).child("likes");

        likes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    counter = dataSnapshot.getValue(Integer.class);
                    viewHolder.setCounter(String.valueOf(counter));
                    likes.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        viewHolder.setCommentsNumber(String.valueOf(model.getCommentsNumber()));
        viewHolder.setDesc(model.getDesc());
        viewHolder.setDate(model.getDate());
        viewHolder.setImage(model.getImage());
        viewHolder.setUsername(model.getUsername());
        viewHolder.setUserImage(post_key);
        viewHolder.setLikeBtn(post_key);


        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent singlePostIntent = new Intent(viewHolder.view.getContext(), PostSingleActivity.class);
                singlePostIntent.putExtra("Post_Id", post_key);
                singlePostIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                viewHolder.view.getContext().startActivity(singlePostIntent);
                showAds();
            }
        });

        viewHolder.mUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference likes = Database.child(post_key).child("uid");
                likes.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            String use = dataSnapshot.getValue(String.class);
                            Intent Intent = new Intent(viewHolder.view.getContext(), Profile_Activity.class);
                            Intent.putExtra("user_id", use);
                            Intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            viewHolder.view.getContext().startActivity(Intent);
                            showAds();
                            likes.removeEventListener(this);
                        }
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
                isPostExist = true;
                mProcessLike = true;

                DatabaseReference likes = Database.child(post_key).child("likes");
                likes.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() != null) {
                            counter = dataSnapshot.getValue(Integer.class);
                        } else {
                            Toast.makeText(viewHolder.view.getContext(), "sorry it seem that post is no longer available", Toast.LENGTH_SHORT).show();
                            isPostExist = false;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mDatabaseLike.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (mProcessLike && isPostExist) {

                            if (dataSnapshot.child(post_key).hasChild(mAuth.get().getCurrentUser().getUid())) {

                                counter--;
                                //viewHolder.setLikesCount(counter);
                                Database.child(post_key).child("likes").setValue(counter);

                                mDatabaseLike.child(post_key).child(mAuth.get().getCurrentUser().getUid()).removeValue();

                                mProcessLike = false;

                            } else {

                                counter++;
                                //viewHolder.setLikesCount(counter);

                                Database.child(post_key).child("likes").setValue(counter);

                                mDatabaseLike.child(post_key).child(mAuth.get().getCurrentUser().getUid()).setValue("random value");

                                mProcessLike = false;
                            }
                            notifyDataSetChanged();
                            mDatabaseLike.removeEventListener(this);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });

            }
        });

    }


    @Override
    public int getItemCount() {
        return postsList.size();
    }

    public List<Post> getPostsList() {
        return postsList;
    }

    public void setPostsList(List<Post> postsList) {
        this.postsList.addAll(postsList);
        notifyDataSetChanged();
    }

    public void refreshPosts(List<Post> newItems) {
        for (int i = 0; i < newItems.size(); i++) {
            this.postsList.add(i, newItems.get(i));
        }

        notifyDataSetChanged();
    }


    public FloatingActionButton getFloatingActionButton() {
        return floatingActionButton;
    }

    public void setFloatingActionButton(FloatingActionButton floatingActionButton) {
        this.floatingActionButton = floatingActionButton;
    }

    public void setAds(InterstitialAd ads) {
        this.ads = ads;
    }

    private void showAds() {
        try {
            if (ads.isLoaded()) {
                ads.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
