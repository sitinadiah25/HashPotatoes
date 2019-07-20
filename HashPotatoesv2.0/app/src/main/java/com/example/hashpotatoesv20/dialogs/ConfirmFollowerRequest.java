package com.example.hashpotatoesv20.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.hashpotatoesv20.Models.Tag;
import com.example.hashpotatoesv20.R;

public class ConfirmFollowerRequest extends DialogFragment {

    private static final String TAG ="ConfirmFollowerRequest";

    public interface OnAllowFollowerListener{
        public void onAllowFollower();
    }
    OnAllowFollowerListener mOnAllowFollowerListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_follower_request, container, false);

        Log.d(TAG, "onCreateView: started.");


        TextView allowDialog = (TextView) view.findViewById(R.id.dialogAllow);
        allowDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Allow user to follow tag");

                    mOnAllowFollowerListener.onAllowFollower();
                    getDialog().dismiss();

            }
        });

        TextView ignoreDialog = (TextView) view.findViewById(R.id.dialogIgnore);
        ignoreDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the dialog");
                getDialog().dismiss();
            }
        });


        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnAllowFollowerListener = (OnAllowFollowerListener) getActivity();
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage() );
        }
    }
}
