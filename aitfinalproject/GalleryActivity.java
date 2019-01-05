package us.ait.android.aitfinalproject;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import us.ait.android.aitfinalproject.adapter.GalleryAdapter;
import us.ait.android.aitfinalproject.data.Post;
import us.ait.android.aitfinalproject.location.PostInfoDialog;

public class GalleryActivity extends AppCompatActivity{

    GalleryAdapter galleryAdapter;
    RecyclerView recyclerView;
    Context context;
    public static final String POST_TO_DISPLAY = "POST_TO_DISPLAY";
    public static final String ADAPTER_SRC = "ADAPTER_SRC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerImages);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        initRecyclerAdapter();
    }

    private void initRecyclerAdapter() {
        new Thread() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        galleryAdapter = new GalleryAdapter(GalleryActivity.this);
                        recyclerView.setAdapter(galleryAdapter);

                        getPostList();
                    }
                });
            }
        }.start();
    }

    private void getPostList() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(getString(R.string.posts));
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Post post = dataSnapshot.getValue(Post.class);
                galleryAdapter.addPost(post, dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    public void initializeGalleryMoreInfoDialog(Post post){
        PostInfoDialog postInfoDialog = new PostInfoDialog();
        Bundle bundle = new Bundle();
        bundle.putSerializable(POST_TO_DISPLAY, post);
        bundle.putSerializable(ADAPTER_SRC, galleryAdapter);
        postInfoDialog.setArguments(bundle);
        postInfoDialog.show(getFragmentManager(), getString(R.string.tag_show));
    }


}
