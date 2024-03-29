package com.group3.swengandroidapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.app.AlertDialog;
import android.content.DialogInterface;
import com.group3.swengandroidapp.XMLRenderer.Presentation;
import com.group3.swengandroidapp.XMLRenderer.RemoteFileManager;

public class PresentationActivity extends AppCompatActivity {

    private Presentation presentation;
    static boolean showTips = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentation);

        Intent receivedIntent = getIntent();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("XML-event-name"));

        Intent intent = new Intent(this, PythonClient.class);

        if(receivedIntent.getStringExtra("FROM_ACTIVITY").equals("MyRecipesActivity")) {
            intent.putExtra(PythonClient.ACTION,PythonClient.FETCH_MY_PRESENTATION);
        } else {
            intent.putExtra(PythonClient.ACTION,PythonClient.FETCH_PRESENTATION);
        }

        intent.putExtra(PythonClient.ID,receivedIntent.getStringExtra(PythonClient.ID));
        startService(intent);

        // instructional pop up
        if (showTips){new AlertDialog.Builder(this)
                .setTitle("Recipe Viewer")
                .setMessage("To progress through the presentation tap the screen once and to go back tap twice, how easy is that?")
                .setPositiveButton("Let's Cook!", null )

                /* the negative button will set a flag so that this doesn't show again
                 ideally this would be saved in the user's preferences on the server, as it stands this
                 will reset every time the app is restarted */

                .setNegativeButton("Don't show me this again", new DialogInterface.OnClickListener()
                {
                    // this is just an override for the 'no' button in the dialoginterface to end the activity
                    @Override
                    public void onClick(DialogInterface dialog, int x) {
                        showTips = false;
                    }
                }).show();}
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        // Get extra data included in the Intent
        String message = intent.getStringExtra(PythonClient.ACTION);
        Log.d("PresentationActivity","received presentation" + intent.getStringExtra(PythonClient.ACTION));

            if (message.matches(PythonClient.FETCH_PRESENTATION)) {
                String presentationID = intent.getStringExtra(PythonClient.ID);
                presentation = RemoteFileManager.getInstance().getPresentation(presentationID);
                presentation.draw(PresentationActivity.this);
            } else if (message.matches(PythonClient.FETCH_MY_PRESENTATION)) {
                String presentationID = intent.getStringExtra(PythonClient.ID);
                presentation = RemoteFileManager.getInstance().getMyPresentation(presentationID);
                presentation.draw(PresentationActivity.this);
            }

        }
    };

    @Override
    protected void onResume(){
        super.onResume();
        AudioPlayer.baguette(); // oui oui
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        AudioPlayer.stop();
    }

    // this overrides the default back button utility whilst in a presentation, prompting a box to ensure the user
    // wishes to leave the presentation
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
               //.setIcon(android.R.drawable.ic_dialog_alert) // this is an image !
                .setTitle("Exit Recipe")
                .setMessage("Are you sure you want to exit this recipe ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    // this is just an override for the yes button in the dialoginterface to end the activity
                    @Override
                    public void onClick(DialogInterface dialog, int x) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
    //this makes the other back button (top left in portrait) also have a confirmation message
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}

