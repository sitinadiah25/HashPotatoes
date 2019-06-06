package com.example.hashpotatoesv20.Profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.hashpotatoesv20.R;
import com.example.hashpotatoesv20.Utils.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

public class EditProfileFragment extends Fragment {

    private static final String TAG = "EditProfileFragment";

    private ImageView mProfilePhoto;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile, container,false);
        mProfilePhoto = (ImageView) view.findViewById(R.id.profilePhoto);

        //setProfileImage();

        //back arrow for navigating back to "Profile Activity"
        ImageView backArrow = (ImageView) view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to ProfileActivity");
                getActivity().finish();
            }
        });

        return view;
    }

    private void setProfileImage() {
        Log.d(TAG, "setProfileImage: setting profile image");
        String imgUrl = "https://cdn.shopify.com/s/files/1/0553/1817/products/Whistle_Flute_KawaiiCat_FlatBrimCap_2048x2048.png?v=1514921387";
        UniversalImageLoader.setImage(imgUrl, mProfilePhoto, null, "");
    }
}
