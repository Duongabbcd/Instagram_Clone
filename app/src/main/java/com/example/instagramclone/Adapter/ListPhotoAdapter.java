package com.example.instagramclone.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagramclone.ListPhoto;
import com.example.instagramclone.R;

import java.util.List;

public class ListPhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
     public static final int MID_GRID =1 ;
    public static final int RIGHT_GRID =2;
    public static final int LEFT_GRID =3;

    private List<ListPhoto> mlistPhotos ;
    public void setData(List<ListPhoto> listPhotos)  {
        this.mlistPhotos = listPhotos ;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(MID_GRID ==viewType){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_mid ,parent,false) ;
            return new PhotoGridViewHolder(view) ;
        }else if(RIGHT_GRID == viewType){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_right ,parent,false) ;
            return new PhotoRightHolder(view) ;
        }else if(LEFT_GRID ==viewType){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_left ,parent,false) ;
            return new PhotoLeftHolder(view) ;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListPhoto listPhoto  = mlistPhotos.get(position) ;
        if(MID_GRID == listPhoto.getType()){
            PhotoGridViewHolder photoGridViewHolder = (PhotoGridViewHolder) holder ;
            photoGridViewHolder.img1.setImageResource(listPhoto.getPhotos().get(0).getResourceid());
            photoGridViewHolder.img2.setImageResource(listPhoto.getPhotos().get(1).getResourceid());
            photoGridViewHolder.img3.setImageResource(listPhoto.getPhotos().get(2).getResourceid());
        }else if(RIGHT_GRID == listPhoto.getType()){
            PhotoRightHolder photoRightHolder = (PhotoRightHolder) holder ;
            photoRightHolder.img1.setImageResource(listPhoto.getPhotos().get(0).getResourceid());
            photoRightHolder.img2.setImageResource(listPhoto.getPhotos().get(1).getResourceid());
            photoRightHolder.img3.setImageResource(listPhoto.getPhotos().get(2).getResourceid());
        }else if(LEFT_GRID == listPhoto.getType()){
            PhotoLeftHolder photoLeftHolder = (PhotoLeftHolder) holder ;
            photoLeftHolder.img1.setImageResource(listPhoto.getPhotos().get(0).getResourceid());
            photoLeftHolder.img2.setImageResource(listPhoto.getPhotos().get(1).getResourceid());
            photoLeftHolder.img3.setImageResource(listPhoto.getPhotos().get(2).getResourceid());
        }
    }

    @Override
    public int getItemViewType(int position) {
        ListPhoto listPhoto = mlistPhotos.get(position) ;

        return listPhoto.getType() ;
    }

    @Override
    public int getItemCount() {
      if(mlistPhotos != null) {
          return mlistPhotos.size() ;
      }
      return  0 ;
    }

    public class PhotoGridViewHolder extends RecyclerView.ViewHolder{
            private ImageView img1  ,img2 ,img3 ;
        public PhotoGridViewHolder(@NonNull View itemView) {
            super(itemView);

            img1 = itemView.findViewById(R.id.img_1) ;
            img2 = itemView.findViewById(R.id.img_2) ;
            img3 = itemView.findViewById(R.id.img_3) ;
        }
    }

    public class PhotoRightHolder extends RecyclerView.ViewHolder{
        private ImageView img1  ,img2 ,img3 ;
        public PhotoRightHolder(@NonNull View itemView) {
            super(itemView);

            img1 = itemView.findViewById(R.id.img_1) ;
            img2 = itemView.findViewById(R.id.img_2) ;
            img3 = itemView.findViewById(R.id.img_3) ;
        }
    }


    public class PhotoLeftHolder extends RecyclerView.ViewHolder{
        private ImageView img1  ,img2 ,img3 ;
        public PhotoLeftHolder(@NonNull View itemView) {
            super(itemView);

            img1 = itemView.findViewById(R.id.img_1) ;
            img2 = itemView.findViewById(R.id.img_2) ;
            img3 = itemView.findViewById(R.id.img_3) ;
        }
    }

}
