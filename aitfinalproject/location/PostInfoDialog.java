package us.ait.android.aitfinalproject.location;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import us.ait.android.aitfinalproject.GalleryActivity;
import us.ait.android.aitfinalproject.R;
import us.ait.android.aitfinalproject.adapter.GalleryAdapter;
import us.ait.android.aitfinalproject.data.Post;

public class PostInfoDialog extends DialogFragment {

    ImageView ivImage;
    TextView tvTimestamp;
    TextView tvLocation;
    TextView tvBody;
    ImageButton btnDelete;
    private Context context;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.post_info_dialog, null);

        initViews(rootView);
        setInitializedViews();
        builder.setView(rootView);

        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        return builder.create();
    }

    private void setInitializedViews() {
        final Post postToDisplay = (Post) getArguments().getSerializable(GalleryActivity.POST_TO_DISPLAY);
        tvBody.setText(postToDisplay.getBody());
        tvTimestamp.setText(postToDisplay.getTimestamp());
        tvLocation.setText(getAddressByLatLan(postToDisplay.getLat(), postToDisplay.getLan()));
        Picasso.get().load(postToDisplay.getImgUrl()).resize(40, 60).centerCrop().into(ivImage);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GalleryAdapter galleryAdapter = (GalleryAdapter) getArguments().getSerializable(GalleryActivity.ADAPTER_SRC);
                galleryAdapter.deletePost(postToDisplay.getImgUrl());
                dismiss();
            }
        });
    }

    private void initViews(View rootView) {
        ivImage = rootView.findViewById(R.id.ivDialogImage);
        tvTimestamp = rootView.findViewById(R.id.tvDialogTimestamp);
        tvLocation = rootView.findViewById(R.id.tvDialogLocation);
        tvBody = rootView.findViewById(R.id.tvDialogBody);
        btnDelete = rootView.findViewById(R.id.dialogBtnDelete);

    }

    public String getAddressByLatLan(Double lat, Double lan){
        Geocoder gc = new Geocoder(getActivity(), Locale.getDefault());
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
