package com.example.hashpotatoesv20.Utils;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.example.hashpotatoesv20.Main.MainActivity;
import com.example.hashpotatoesv20.R;
import com.hololo.tutorial.library.Step;

public class TutorialActivity extends com.hololo.tutorial.library.TutorialActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addFragment(new Step.Builder().setTitle("Tutorial Walk-through")
                .setContent("Yay Walk-through!")
                .setBackgroundColor(Color.parseColor("#ABD1ED")) // int background color
                .setDrawable(R.drawable.welcome_tutorial) // int top drawable
                .setSummary("Continue")
                .build());

        addFragment(new Step.Builder().setTitle("Create a Post")
                .setContent("To create a post, head over to the Mainfeed and click on the top-right hand corner.\nType in your post content, the relevant tag(s) and you anonymity. \nMake sure you include at least one tag!")
                .setBackgroundColor(Color.parseColor("#ABD1ED")) // int background color
                .setDrawable(R.drawable.tutorial1) // int top drawable
                .setSummary("Tags...? Continue...")
                .build());

        addFragment(new Step.Builder().setTitle("Create a Tag")
                .setContent("Under your profile, you can create a custom tag. \nA tag is like a group, you can create one just for your group of friends and make it private so that others aren't able to view them. \nPublic tags however are viewable to all.")
                .setBackgroundColor(Color.parseColor("#ABD1ED")) // int background color
                .setDrawable(R.drawable.tutorial2) // int top drawable
                .setSummary("Private tags?? Hmm.. Continue...")
                .build());

        addFragment(new Step.Builder().setTitle("Private Tags")
                .setContent("As seen above, tags that are Private aren't viewable to those who aren't following. \nTo follow a tag, click the Follow button and a request will be forwarded to the owner of the tag!")
                .setBackgroundColor(Color.parseColor("#ABD1ED")) // int background color
                .setDrawable(R.drawable.tutorial3) // int top drawable
                .setSummary("I see.. Ok Continue")
                .build());

        addFragment(new Step.Builder().setTitle("Reporting and Editing of Posts")
                .setContent("To keep this platform a safe and peaceful environment, we would like our users to report posts that they find offensive or hurtful. Our team will try our best to review the post! \nUsers are also able to edit their posts :)")
                .setBackgroundColor(Color.parseColor("#ABD1ED")) // int background color
                .setDrawable(R.drawable.tutorial4) // int top drawable
                .setSummary("Continue")
                .build());

        addFragment(new Step.Builder().setTitle("Thank you!")
                .setContent("Thank you for helping us test our app! \nHelp us find bugs and suggest features that can be added into our to make it a better and more productive platform for all to use :)")
                .setBackgroundColor(Color.parseColor("#ABD1ED")) // int background color
                .setDrawable(R.drawable.tutorial5) // int top drawable
                .setSummary("Start!")
                .build());

        setFinishText("START");
        setCancelText("");
    }

    @Override
    public void finishTutorial() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }


    @Override
    public void currentFragmentPosition(int position) {

    }
}
