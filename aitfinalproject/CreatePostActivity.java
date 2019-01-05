package us.ait.android.aitfinalproject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import us.ait.android.aitfinalproject.data.Post;
import us.ait.android.aitfinalproject.location.LocationMonitor;

public class CreatePostActivity extends AppCompatActivity implements LocationMonitor.LocationObserver{

    double latitude = 0;
    double longitude = 0;
    private Context context;

    @BindView(R.id.tvLocation)
    TextView tvLocation;

    @BindView(R.id.imgAttach)
    ImageView imgAttach;

    @BindView(R.id.etBody)
    EditText etBody;

    @BindView(R.id.btnSubmit)
    Button btnSubmit;

    private LocationMonitor locationMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        ButterKnife.bind(this);
        locationMonitor = new LocationMonitor();
        requestNeededPermission();

        // when the activity first starts, the camera should happen
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intentCamera, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 101 && resultCode == RESULT_OK){
            Bundle extra = data.getExtras();
            Bitmap image = (Bitmap) extra.get(getString(R.string.data));
            imgAttach.setImageBitmap(image);
            imgAttach.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.btnSubmit)
    void onSubmit(){
        if(imgAttach.getVisibility() == View.GONE){
            uploadPost();
        }else{
            try {
                uploadPostWithImage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void requestNeededPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Toast...
            }

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    101);
        } else {
            // we have the permission
            btnSubmit.setEnabled(true);
            locationMonitor.startLocationMonitoring(this, (LocationManager) getSystemService(LOCATION_SERVICE));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show();

                // start our job, we have the permission
                locationMonitor.startLocationMonitoring(this, (LocationManager) getSystemService(LOCATION_SERVICE));
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                btnSubmit.setEnabled(false);
            }
        }
    }

    private void uploadPost(String... imgUrl) {
        // when you push a new element, it has an automatically created key (that we're finding in the next line)
        String key = FirebaseDatabase.getInstance().getReference().child(getString(R.string.posts)).push().getKey();

        // creating new post POJO to insert into firebase
        Post post = new Post(new Date(System.currentTimeMillis()).toString(), latitude, longitude,
                etBody.getText().toString(), null);

        if(imgUrl != null && imgUrl.length > 0){
            post.setImgUrl(imgUrl[0]);
        }

        // inserting post POJO into firebase under our new post that was created when we found the key
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.posts)).child(key).setValue(post).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreatePostActivity.this, getString(R.string.data_upload_failed)+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        finish();
    }


    public void uploadPostWithImage() throws Exception {
        byte[] imageInBytes = loadImageAndConvertToBytes();
        StorageReference newImageImagesRef = convertImageToStorageRef();
        uploadImageToFirebase(imageInBytes, newImageImagesRef);
    }

    private void uploadImageToFirebase(byte[] imageInBytes, StorageReference newImageImagesRef) {
        UploadTask uploadTask = newImageImagesRef.putBytes(imageInBytes);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(CreatePostActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                uploadPost(taskSnapshot.getDownloadUrl().toString());
            }
        });
    }

    @NonNull
    private StorageReference convertImageToStorageRef() throws UnsupportedEncodingException {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String newImage = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8")+".jpg";
        StorageReference newImageRef = storageRef.child(newImage);
        StorageReference newImageImagesRef = storageRef.child("images/"+newImage);
        newImageRef.getName().equals(newImageImagesRef.getName());    // true
        newImageRef.getPath().equals(newImageImagesRef.getPath());    // false
        return newImageImagesRef;
    }

    private byte[] loadImageAndConvertToBytes() {
        imgAttach.setDrawingCacheEnabled(true);
        imgAttach.buildDrawingCache();
        Bitmap bitmap = imgAttach.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    @Override
    public void locationAvailable(Location location) {
        btnSubmit.setEnabled(true);
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Geocoder gc = new Geocoder(CreatePostActivity.this, Locale.getDefault());
        List<Address> addressList = null;
        if(latitude != 0 && longitude != 0){
            try {
                addressList = gc.getFromLocation(latitude, longitude, 5);
                if(addressList.size() != 0){
                    tvLocation.setText(addressList.get(0).getAddressLine(0));
                }else{
                    tvLocation.setText(R.string.no_location);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void locationInfo(String info) {

    }
}
