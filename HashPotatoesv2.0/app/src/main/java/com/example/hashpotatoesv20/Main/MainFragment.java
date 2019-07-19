package com.example.hashpotatoesv20.Main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.hashpotatoesv20.Models.Comment;
import com.example.hashpotatoesv20.Models.Like;
import com.example.hashpotatoesv20.Models.Post;
import com.example.hashpotatoesv20.Models.Tag;
import com.example.hashpotatoesv20.Models.UserAccountSettings;
import com.example.hashpotatoesv20.R;
import com.example.hashpotatoesv20.Utils.MainfeedListAdapter;
import com.example.hashpotatoesv20.Utils.ViewPostFragment;
import com.example.hashpotatoesv20.Utils.ViewTagFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.security.auth.login.LoginException;

public class MainFragment extends Fragment{
    private static final String TAG = "MainFragment";
    private static final int ACTIVITY_NUM = 0;


    public interface onListPostSelectedListener {
        void onPostSelected(Post post, int activity_number);
    }
    onListPostSelectedListener mOnListPostSelectedListener;

    @Override
    public void onAttach(Context context) {
        try {
            mOnListPostSelectedListener = (onListPostSelectedListener) getActivity();
        }
        catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException" + e.getMessage());
        }
        super.onAttach(context);
    }


    //widgets
    private SwipeRefreshLayout pullToRefresh;

    //vars
    private ArrayList<Post> mPosts;
    private ArrayList<Post> mPaginatedPosts;
    private ArrayList<String> mFollowing;
    private ListView mListView;
    private MainfeedListAdapter mAdapter;
    private int mResults;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container,false);
        mListView = (ListView) view.findViewById(R.id.listView);
        pullToRefresh = (SwipeRefreshLayout) view.findViewById(R.id.pullToRefresh);
        mFollowing = new ArrayList<>();
        mPosts = new ArrayList<>();

        pullToRefresh.setDistanceToTriggerSync(20);

        getFollowing();

        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPosts();
                pullToRefresh.setRefreshing(false);
            }
        });

        return view;
    }

    private void getFollowing() {
        Log.d(TAG, "getFollowing: getting user following");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_user_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found tag: " +
                            ds.child(getString(R.string.field_tag_id)).getValue());

                    mFollowing.add(ds.child(getString(R.string.field_tag_id)).getValue().toString());
                }
                mFollowing.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                //get posts
                getPosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPosts() {
        Log.d(TAG, "getPosts: getting posts");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for (int i = 0; i < mFollowing.size(); i++) {
            final int count = i;
            final String currTagID = mFollowing.get(i);
            Query query = reference.child(getString(R.string.dbname_tag_post))
                    .child(currTagID);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: hi");
                        Post post = new Post();
                        Map<String,Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                        post.setDiscussion(objectMap.get(getString(R.string.field_discussion)).toString());
                        post.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        post.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        post.setPost_id(objectMap.get(getString(R.string.field_post_id)).toString());
                        post.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        post.setAnonymity(objectMap.get(getString(R.string.field_anonymity)).toString());

                        ArrayList<String> tagIDList = new ArrayList<>();
                        for (DataSnapshot dataSnapshot1 : singleSnapshot
                                .child(getString(R.string.field_tag_list)).getChildren()) {
                            Log.d(TAG, "onDataChange: post taglist" + dataSnapshot1.getValue());
                            tagIDList.add(dataSnapshot1.getValue().toString());
                        }

                        List<Like> likesList = new ArrayList<Like>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.field_likes)).getChildren()){
                            Like like = new Like();
                            like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                            likesList.add(like);
                        }

                        //find the one with comments pls lol
                        List<Comment> commentList = new ArrayList<>();
                        for (DataSnapshot dataSnapshot1 :
                                singleSnapshot.child(getString(R.string.field_comments)).getChildren()) {
                            Comment comment = new Comment();
                            comment.setUser_id(dataSnapshot1.getValue(Comment.class).getUser_id());
                            comment.setComment(dataSnapshot1.getValue(Comment.class).getComment());
                            comment.setDate_created(dataSnapshot1.getValue(Comment.class).getDate_created());
                        }

                        post.setTag_list(tagIDList);
                        post.setComments(commentList);
                        post.setLikes(likesList);

                        boolean duplicate = false;

                        //check for duplicates (not efficient, need a more efficient way)
                        for (int j = 0; j < mPosts.size(); j++) {
                            if (mPosts.get(j).getPost_id().equals(post.getPost_id())) {
                                duplicate = true;
                            }
                        }

                        if (!duplicate) {
                            mPosts.add(post);
                        }
                    }
                    if (count >= mFollowing.size() - 1) {
                        displayPosts();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }

    private void displayPosts() {
        mPaginatedPosts = new ArrayList<>();
        if (mPosts != null) {
            try {
                Collections.sort(mPosts, new Comparator<Post>() {
                    @Override
                    public int compare(Post o1, Post o2) {
                        return o2.getDate_created().compareTo(o1.getDate_created());
                    }
                });

                int iteration = mPosts.size();
                Log.d(TAG, "displayPosts: Checking post size: " + mPosts.size());

                if (iteration > 10){
                    iteration = 10;
                }
                mResults = 10;
                for (int i = 0; i < iteration; i ++){
                    mPaginatedPosts.add(mPosts.get(i));
                }
                mAdapter = new MainfeedListAdapter(getActivity(), R.layout.layout_mainfeed_listitem, mPaginatedPosts);
                mListView.setAdapter(mAdapter);
                setListViewHeightBasedOnChildren(mListView);

                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        int actPosition = mPaginatedPosts.size() - position - 1;
                        Log.d(TAG, "onItemClick: position:" + actPosition);
                        //Log.d(TAG, "onItemClick: post: " + mPaginatedPosts.get(actPosition));
                        mOnListPostSelectedListener.onPostSelected(mPaginatedPosts.get(position), ACTIVITY_NUM);
                        //((MainActivity)getActivity()).showLayout();
                    }
                });

            } catch (NullPointerException e) {
                Log.e(TAG, "displayPosts: NullPointerException: " + e.getMessage());
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "displayPosts: IndexOutOfBoundsException: " + e.getMessage());
            }
        }
    }

   /* private void onPostSelected(Post post, int activity_number) {
        Log.d(TAG, "onPostSelected: selected a post from listview " + post.toString());

        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.post), post);
        args.putInt(getString(R.string.activity_number), ACTIVITY_NUM);
        fragment.setArguments(args);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();
    }*/

    public void displayMorePosts(){
        Log.d(TAG, "displayMorePosts: displaying more posts");
        try{

            if(mPosts.size() > mResults && mPosts.size() > 0){
                int iterations;
                if(mPosts.size() > (mResults + 10)) {
                    Log.d(TAG, "displayMorePhotos: there are greater than 10 more photos");
                    iterations = 10;
                }else{
                    Log.d(TAG, "displayMorePhotos: there is less than 10 more photos");
                    iterations = mPosts.size() - mResults;
                }

                //add the new posts to the paginated results
                for (int i = mResults; i < mResults + iterations; i++){
                    mPaginatedPosts.add(mPosts.get(i));
                }
                mResults = mResults + iterations;
                mAdapter.notifyDataSetChanged();
            }

        }catch (NullPointerException e) {
            Log.e(TAG, "displayPosts: NullPointerException: " + e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "displayPosts: IndexOutOfBoundsException: " + e.getMessage());
        }
    }

    private static void setListViewHeightBasedOnChildren (ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
                View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0) view.setLayoutParams(new
                    ViewGroup.LayoutParams(desiredWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight() + 25;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();

        int width = listAdapter.getCount() - 1;

        params.height = totalHeight + (listView.getDividerHeight() * (width));

        listView.setLayoutParams(params);
        listView.requestLayout();
    }


}
