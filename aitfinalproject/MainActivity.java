package us.ait.android.aitfinalproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import us.ait.android.aitfinalproject.data.Post;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    private GoogleMap mMap;
    private List<Marker> markerList = new ArrayList<Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
    }

    private void initUI() {
        Toolbar toolbar = initToolbarAndFab();
        initDrawerAndNavView(toolbar);
        initMap();
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initDrawerAndNavView(Toolbar toolbar) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private Toolbar initToolbarAndFab() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CreatePostActivity.class));
            }
        });
        return toolbar;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            startActivity(new Intent(MainActivity.this, CreatePostActivity.class));
        } else if (id == R.id.nav_gallery) {
            startActivity(new Intent(MainActivity.this, GalleryActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setTrafficEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        initPosts();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mMap.setInfoWindowAdapter(MainActivity.this);
                marker.showInfoWindow();
                return true;
            }
        });
    }

    private void initPosts(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(getString(R.string.posts));
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                createAndAddMapMarker(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                removeMarker(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void removeMarker(DataSnapshot dataSnapshot) {
        Post removedPost = dataSnapshot.getValue(Post.class);
        Marker marker = findMarkerByPostTag(removedPost);
        if(marker != null){
            marker.remove();
        }
    }

    private void createAndAddMapMarker(DataSnapshot dataSnapshot) {
        Post post = dataSnapshot.getValue(Post.class); // get inserted data as a post object
        LatLng newPostLocation = new LatLng(post.getLat(), post.getLan());
        MarkerOptions opt = new MarkerOptions().position(newPostLocation);
        Marker marker = mMap.addMarker(opt);
        marker.setTag(post);
        markerList.add(marker);
    }

    private Marker findMarkerByPostTag(Post removedPost) {
        for(int i = 0; i < markerList.size(); i++){
            Post post = (Post) markerList.get(i).getTag();
            if(removedPost.getImgUrl() == post.getImgUrl()){
                return markerList.get(i);
            }
        }
        return null;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null; // leave as is
    }

    @Override
    public View getInfoContents(final Marker marker) {
        View markerWindow = initInfoWindowUI(marker);
        return markerWindow;
    }

    @NonNull
    private View initInfoWindowUI(Marker marker) {
        final Post post = (Post) marker.getTag();
        View markerWindow = getLayoutInflater().inflate(R.layout.info_window, null);

        // Initialize all parts of the marker window
        ImageView ivImage = markerWindow.findViewById(R.id.ivImage);
        initNewUserImage(marker, post, ivImage);

        initInfoWindowTextViews(post, markerWindow);
        return markerWindow;
    }

    private void initInfoWindowTextViews(Post post, View markerWindow) {
        TextView tvTimestamp = markerWindow.findViewById(R.id.tvTimestamp);
        tvTimestamp.setText(post.getTimestamp());

        // convert location from lat and lan to address
        TextView tvLocation = markerWindow.findViewById(R.id.tvLocation);
        tvLocation.setText(getAddressByLatLan(post.getLat(), post.getLan()));

        TextView tvBody = markerWindow.findViewById(R.id.tvBody);
        tvBody.setText(post.getBody());
    }

    private void initNewUserImage(final Marker marker, Post post, ImageView ivImage) {
        Glide.with(this).load(post.getImgUrl()).asBitmap().override(50,50).listener(
                new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        e.printStackTrace();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if(!isFromMemoryCache) marker.showInfoWindow();
                        return false;
                    }
                }).into(ivImage);
    }

    public String getAddressByLatLan(Double lat, Double lan){
        Geocoder gc = new Geocoder(MainActivity.this, Locale.getDefault());
        String retAddress;
        List<Address> addressList = null;
        try {
            addressList = gc.getFromLocation(lat, lan, 5);
            if(addressList.size() != 0){
                retAddress = addressList.get(0).getAddressLine(0);
            }else{
                retAddress = getString(R.string.no_location);
            }
        } catch (IOException e) {
            e.printStackTrace();
            retAddress = getString(R.string.error);
        }
        return retAddress;
    }

}
