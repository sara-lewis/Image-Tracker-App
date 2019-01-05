package us.ait.android.aitfinalproject.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import us.ait.android.aitfinalproject.GalleryActivity;
import us.ait.android.aitfinalproject.R;
import us.ait.android.aitfinalproject.data.Post;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> implements Serializable{

    public static final int BAD_ERROR = 1000;
    private List<Post> postsList;
    private List<String> postKeys;
    private Context context;

    public GalleryAdapter(Context context) {
        this.context = context;
        this.postsList = new ArrayList<Post>();
        this.postKeys = new ArrayList<String>();
    }

    @NonNull
    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View imageView =
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.recycler_cell, parent, false);
        return new ViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull final GalleryAdapter.ViewHolder holder, final int position) {
        Picasso.get().load(postsList.get(position).getImgUrl()).resize(275, 450)
                .centerCrop().into(holder.ivImageIcon);
        holder.ivImageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((GalleryActivity) context).initializeGalleryMoreInfoDialog(postsList.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    public void addPost(Post post, String key) {
        postsList.add(post);
        postKeys.add(key);
        notifyItemInserted(postsList.size()-1);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivImageIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            ivImageIcon = itemView.findViewById(R.id.ivImageIcon);
        }
    }

    public void deletePost(String imgUrl){
        int index = findIndexByImgUrl(imgUrl);
        if(index != BAD_ERROR){
            FirebaseDatabase.getInstance().getReference(context.getString(R.string.posts)).child(postKeys.get(index)).removeValue();
            postsList.remove(index);
            postKeys.remove(index);
            notifyItemRemoved(index);
        }
    }

    public int findIndexByImgUrl(String imgUrl){
        for(int i = 0; i < postsList.size(); i++){
            if(postsList.get(i).getImgUrl() == imgUrl){
                return i;
            }
        }
        return BAD_ERROR;
    }
}
