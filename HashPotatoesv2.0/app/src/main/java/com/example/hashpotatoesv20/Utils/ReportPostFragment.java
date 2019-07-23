package com.example.hashpotatoesv20.Utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.hashpotatoesv20.Models.Post;
import com.example.hashpotatoesv20.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ReportPostFragment extends Fragment {

    private static final String TAG = "ReportPostFragment";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    //widgets
    private Post mPost;
    private ListView mListView;
    private Button mCancel, mReport;
    private ImageView mBackArrow;
    private Context mContext;

    //variables
    private String chosenOption;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report_post, container, false);
        mBackArrow = (ImageView) view.findViewById(R.id.backArrow);
        mCancel = (Button) view.findViewById(R.id.btn_cancel);
        mReport = (Button) view.findViewById(R.id.btn_report);
        mListView = (ListView) view.findViewById(R.id.listView);
        mContext = getActivity();
        mPost = getPostFromBundle();

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to 'ViewPostFragment'");
                getActivity().onBackPressed();
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to 'ViewPostFragment'");
                getActivity().onBackPressed();
            }
        });

        //set up listview
        final ArrayList<String> options = new ArrayList<>();
        options.add("Hate speech or symbols");
        options.add("Violence or threat of violence");
        options.add("Harassment or bullying");
        options.add("Intellectual property violation");
        ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_list_item_activated_1, options);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                chosenOption = options.get(position);
            }
        });

        mReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chosenOption.isEmpty()) {
                    Log.d(TAG, "onClick: no option chosen");
                    Toast.makeText(mContext, "Please select a reason for reporting this post.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.d(TAG, "onClick: sending report to gmail");
                    String fromEmail = "hashpotatoes98@gmail.com";
                    String fromPassword = "98jannad98";
                    String toEmail = "hashpotatoes98@gmail.com";
                    String emailSubject = "Report Post: " + mPost.getPost_id();
                    String emailBody = "Reported by UserID: " + FirebaseAuth.getInstance().getCurrentUser().getUid()
                            + "\nReason: " + chosenOption + "\nPostID: " + mPost.getPost_id() + " \nPost Content: " + mPost.getDiscussion();
                    new SendMailTask(getActivity()).execute(fromEmail, fromPassword, toEmail
                            ,emailSubject, emailBody);
                    getActivity().onBackPressed();
                    Toast.makeText(mContext, "Report sent. Please wait while we review the report.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        setupFirebaseAuth();

        return view;
    }

    /**
     * retrieve the post from the incoming bundle from profileactivity interface
     * @return
     */
    private Post getPostFromBundle() {
        Log.d(TAG, "getPostFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.post));
        }
        else {
            return null;
        }
    }

    /*
    ---------------------------------------firebase------------------------------------------------
     */

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    //User is signed in
                    Log.d(TAG, "onAuthStateChanged: signed_in:" + user.getUid());
                } else {
                    //User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
