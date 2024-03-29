package com.group3.swengandroidapp;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.group3.swengandroidapp.ShoppingList.Intent_Constants;
import com.group3.swengandroidapp.XMLRenderer.Recipe;
import com.group3.swengandroidapp.XMLRenderer.RemoteFileManager;

import java.util.HashMap;
import java.util.Random;

/**
 * The home screen of the app.
 * <p>
 *     Displays a history of recently viewed recipes (as contained within {@link HistoryHandler}) and a
 *     "recommended" list of recipes (Produced within {@link RemoteFileManager}).
 * </p>
 */
public class HomeActivity extends MainActivity implements RecipeRecyclerViewAdaper.ItemClickListener{

    private RecipeRecyclerViewAdaper suggestedAdapter;          // adapter to Suggested Recipes recyclerview
    private RecipeRecyclerViewAdaper historyAdapter;            // Adapter to History recyclerview
    private ImageDownloaderListener imageDownloaderListener;    // Listens for BITMAP_READY messages from ImageDownloaderService
    HashMap<String, Recipe.Icon> icons = new HashMap<>();       // Contains all icons that are to be deployed on this page

    static boolean viewed = false;
    int min = 0; int max = 7;
    Random r = new Random();                    // these variables are used for the possible welcome messages
    int i = r.nextInt(max - min + 1) + min;

    /**
     * Method called when a recipe is clicked from the home screen either in the history or main section.
     * Opens an instance of {@link RecipeSelectionActivity}
     */
    @Override
    public void onItemClick(String recipeId){
        AudioPlayer.touchSound();
        if (!AudioPlayer.isVibrationOff()){
            vibrator.vibrate(20);
        }
        Log.d("HomeActivity","Clicked on recipe " + recipeId);
        Intent intent = new Intent();
        intent.setClass(this,RecipeSelectionActivity.class);                   // Set new activity destination
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);                                    // Delete previous activities
        intent.putExtra(PythonClient.ID, recipeId);       // Set recipe id
        intent.putExtra("FROM_ACTIVITY", "HomeActivity");      // Tell new activity that this was the previous activity
        startActivityForResult(intent, Intent_Constants.INTENT_REQUEST_CODE);                // switch activities
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        super.onCreateDrawer();

        setTitle("Home");

        // Setup Recommended Recipes view
        RecyclerView recyclerView = findViewById(R.id.home_suggested_view);                // Get suggested recipe view
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);    // Set as a 2-collumn grid
        recyclerView.setHasFixedSize(false);
        recyclerView.setVerticalScrollBarEnabled(true);
        suggestedAdapter = new RecipeRecyclerViewAdaper(this);                     // Initialise the adapter for the view
        suggestedAdapter.setClickListener(this);                                          // Set the click listener for the adapter
        recyclerView.setAdapter(suggestedAdapter);                                        // Assign adapter to the view

        // Setup History
        RecyclerView historyView = findViewById(R.id.home_history_view);
        historyView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)); // Set view layout
        historyAdapter = new RecipeRecyclerViewAdaper(this);
        historyAdapter.setClickListener(this);
        historyView.setAdapter(historyAdapter);

        // Set swipe refreshing
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                Intent intent = new Intent(HomeActivity.this, PythonClient.class);
                intent.putExtra(PythonClient.ACTION,PythonClient.LOAD_ALL);
                startService(intent);

                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // 5 possible starting messages
        if(!viewed) {
            AlertDialog.Builder welcomeM = new AlertDialog.Builder(this);
            welcomeM.setTitle("HANDS OFF");
            welcomeM.setPositiveButton("OK!", null);

            switch (i) {
                case 0:
                    welcomeM.setMessage("Please consider rating HANDS OFF 5 stars on the Google Play Store").show();
                    break;
                case 1:
                    welcomeM.setMessage("We'd love if you tweeted about HANDS OFF!").show();
                    break;
                case 2:
                    welcomeM.setMessage("Favourite all your preferred recipes to easily view them at any time!").show();
                    break;
                case 3:
                    welcomeM.setMessage("Have you tried our fantastic Recipe of the Day?").show();
                    break;
                case 4:
                    welcomeM.setMessage("Try the amazing shopping list feature!").show();
                    break;
            }   // note it is possible that no message appears 0-7 rand
        }
        //set flag as the user only gets one possible pop up per app startup
        viewed = true;
    }

    @Override
    public void onStart(){
        super.onStart();

        // Get all needed recipe ids
        String recipeOfTheDay = RemoteFileManager.getInstance().getRecipeOfTheDay();
        String[] histories = HistoryHandler.getInstance().getHistory();
        String[] suggested;
        if (histories == null){
            suggested = RemoteFileManager.getInstance().getSuggestedRecipes(0, histories);
            Log.d("history", "nothing in history, loading all recipes instead");
        }else{
            Log.d("history", "entered into else statement");

            int historySize = histories.length;
            Log.d("history", "number of recipes in history:" + historySize);
            suggested = RemoteFileManager.getInstance().getSuggestedRecipes(historySize, histories);
        }


        // Process recipe of the day
        Recipe rotd = RemoteFileManager.getInstance().getRecipe(recipeOfTheDay).clone(); // Copy the recipe
        rotd.setTitle("Recipe of the day:\n"+rotd.getTitle());
        icons.put(recipeOfTheDay, Recipe.produceDescriptor(this, rotd));
        // Add recipe of the day in the history view
        historyAdapter.addIcon(icons.get(recipeOfTheDay));

        // Process the rest of the history view (if there are histories to load)
        if(histories != null){                          // if there are histories to load
            for(String id : histories){                 // for each history recipe
                if(!icons.containsKey(id)){             // if corresponding icon doesn't already exist, create
                    icons.put(id, Recipe.produceDescriptor(this, RemoteFileManager.getInstance().getRecipe(id)));
                }
                historyAdapter.addIcon(icons.get(id));  // Add icon to the adapter
            }
        }

        // Process the suggested view

        for(String id : suggested){
            if(!icons.containsKey(id)){
                icons.put(id, Recipe.produceDescriptor(this, RemoteFileManager.getInstance().getRecipe(id)));
            }
            suggestedAdapter.addIcon(icons.get(id));
        }

        // Notify the adapters to update themselves.
        historyAdapter.notifyDataSetChanged();
        suggestedAdapter.notifyDataSetChanged();




    }

    @Override
    public void onResume(){
        super.onResume();
        viewed = true;
        // Startup imageDownloaderListener
        imageDownloaderListener = new ImageDownloaderListener(this) {
            @Override
            public void onBitmapReady(String id, String absolutePath){
                if(icons.containsKey(id)) {
                    icons.get(id).setDrawable(ImageDownloaderService.fetchBitmapDrawable(absolutePath));
                    suggestedAdapter.notifyIconChanged(id);
                    historyAdapter.notifyIconChanged(id);
                }
            }

            @Override
            public void onIconChanged(String id){
                suggestedAdapter.notifyIconChanged(id);
                historyAdapter.notifyIconChanged(id);
            }
        };

        // Background load the icon thumbnails
        for(String id : icons.keySet()){
            requestBitmapFile(id);
        }

    }

    @Override
    public void onPause(){
        super.onPause();
        // TODO: Unregistered in onPause, not re-registered in onResume?
        imageDownloaderListener.unRegister();
    }

    private BroadcastReceiver MessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        // Get extra data included in the Intent
        String message = intent.getStringExtra("message");
        if(message != null) Log.d("receiver", "Got message: " + message);

        if (intent.getStringExtra(PythonClient.ACTION).matches(PythonClient.FETCH_RECIPE)) {
            Intent newIntent = new Intent(context, RecipeSelectionActivity.class);
            startActivity(newIntent);
        }else {
            Log.d("ASDLKA", intent.getStringExtra(PythonClient.ACTION));
        }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewed = true;
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }
}
