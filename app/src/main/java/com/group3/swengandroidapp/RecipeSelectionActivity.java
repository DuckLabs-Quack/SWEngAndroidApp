package com.group3.swengandroidapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.group3.swengandroidapp.XMLRenderer.*;
import com.group3.swengandroidapp.XMLRenderer.Recipe;

import java.util.ArrayList;

public class RecipeSelectionActivity extends AppCompatActivity {
    private String id;
    private ImageView icon;
    private ImageDownloaderListener imageDownloaderListener;
    private Recipe recipe;
    private ArrayList<String> ingredientsList;
    private String previousActivity;

    public  android.os.Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_selection);

        Intent intent = getIntent();
        id = intent.getStringExtra(PythonClient.ID);

        vibrator = (android.os.Vibrator) getSystemService(android.content.Context.VIBRATOR_SERVICE);

        final Button startButton = findViewById(R.id.recipe_selection_start_button);
        startButton.setOnClickListener(v -> {
            // Code here executes on main thread after user presses button
            AudioPlayer.touchSound();
            if (!AudioPlayer.isVibrationOff()){
                vibrator.vibrate(20);
             }
            Intent newIntent = new Intent(getApplicationContext(), PresentationActivity.class);
            newIntent.putExtra(PythonClient.ID, id);

            if (previousActivity.equals("MyRecipesActivity")) {
                newIntent.putExtra("FROM_ACTIVITY", "MyRecipesActivity");
            } else {
                newIntent.putExtra("FROM_ACTIVITY", "HomeActivity");
            }

            startActivity(newIntent);
        });

        final Button listButton = findViewById(R.id.recipe_selection_addToList_button);
        listButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AudioPlayer.touchSound();
                if (!AudioPlayer.isVibrationOff()){
                    vibrator.vibrate(20);
                 }
                ShoppinglistHandler.getInstance().addToShoppingList(recipe.getIngredients());
                Toast.makeText(getApplicationContext(),"Recipes added to shopping list!", Toast.LENGTH_SHORT).show();

            }
        });



        final ImageButton favourites = findViewById(R.id.recipe_selection_thumbnail_favourites_button);
        favourites.setOnClickListener(v -> {
            if(id!=null){
                AudioPlayer.favouritesSound();
                if (!AudioPlayer.isVibrationOff()){
                    vibrator.vibrate(20);
                 }
                FavouritesHandler.getInstance().toggleFavourite(id);
                if (FavouritesHandler.getInstance().contains(id)) {
                    favourites.setImageResource(R.drawable.favfull);
                } else {
                    favourites.setImageResource(R.drawable.favempty);
                }
            }
        });
        if(id!=null){
            if (FavouritesHandler.getInstance().contains(id)) {
                favourites.setImageResource(R.drawable.favfull);
            } else {
                favourites.setImageResource(R.drawable.favempty);
            }
        }else{
            favourites.setImageResource(R.drawable.favempty);
        }
    }

    @Override
    protected void onStart(){
        super.onStart();

        // Fill the screen with all the information!

        icon = findViewById(R.id.recipe_selection_thumbnail_image);
        TextView time = findViewById(R.id.recipe_selection_timer_time);
        TextView description = findViewById(R.id.recipe_selection_description);
        TextView author = findViewById(R.id.recipe_selection_author);
        ImageView spicy = findViewById(R.id.recipe_selection_thumbnail_filter_spicy);
        ImageView veg = findViewById(R.id.recipe_selection_thumbnail_filter_vegetarian);
        ImageView vegan = findViewById(R.id.recipe_selection_thumbnail_filter_vegan);
        ImageView lactose = findViewById(R.id.recipe_selection_thumbnail_filter_lactose);
        ImageView nuts = findViewById(R.id.recipe_selection_thumbnail_filter_nuts);
        ImageView gluten = findViewById(R.id.recipe_selection_thumbnail_filter_gluten);
        TextView ingredients = findViewById(R.id.recipe_selection_ingredients);
        TextView tags = findViewById(R.id.recipe_selection_tags_text);

        Intent intent = getIntent();
        previousActivity= intent.getStringExtra("FROM_ACTIVITY"); // Get name of previous activity
        if (previousActivity.equals("MyRecipesActivity")){
            recipe = RemoteFileManager.getInstance().getMyRecipe(id);
        }else{
            recipe = RemoteFileManager.getInstance().getRecipe(id);
        }

        if(recipe==null){
            recipe = new Recipe("Recipe not found!", "n/a", ("UPDATED_RECIPE_ID: " + id), "n/a", "n/a");
        }else{
            // Recipe is found, add to history
            HistoryHandler.getInstance().append(recipe.getID());
        }

        setTitle(recipe.getTitle());        // Set page title to the recipe title
        time.setText(recipe.getTime());
        author.setText(recipe.getAuthor());
        description.setText(recipe.getDescription());
        ingredients.setText(recipe.generateIngredientsString());

        // FILTERS
        // If any filters are true, replace filter icon with the "on" versions (default = off)
        if(recipe.getSpicy()){
            spicy.setImageResource(R.drawable.spicy_filter);
        }
        if(recipe.getVegetarian()){
            veg.setImageResource(R.drawable.vegetarian_filter);
        }
        if(recipe.getVegan()){
            vegan.setImageResource(R.drawable.vegan_filter);
        }
        if(recipe.getLactose()){
            lactose.setImageResource(R.drawable.lactosefree_filter);
        }
        if(recipe.getNuts()){
            nuts.setImageResource(R.drawable.nutfree_filter);
        }
        if(recipe.getGluten()){
            gluten.setImageResource(R.drawable.glutenfree_filter);
        }

        // Tags
        StringBuilder sb = new StringBuilder();
        for(String s : recipe.getTags()){
            sb.append(s);
            sb.append(", ");
        }
        if(sb.length()>2) {
            sb.delete(sb.length() - 2, sb.length());
        }
        tags.setText(sb.toString());
    }

    @Override
    public void onResume(){
        super.onResume();
        imageDownloaderListener = new ImageDownloaderListener(this) {
            @Override
            public void onBitmapReady(String id, String filePath) {
                icon.setImageDrawable(ImageDownloaderService.fetchBitmapDrawable(filePath));
            }
        };

        if(icon.getDrawable() == null){ // if there is no icon, fetch
            Intent downloadreq = new Intent(this, ImageDownloaderService.class);
            downloadreq.setAction(ImageDownloaderService.GET_BITMAP_READY);
            downloadreq.putExtra(Recipe.ID, id);
            startService(downloadreq);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        imageDownloaderListener.unRegister();
    }

    @Override
    public void onBackPressed(){
        AudioPlayer.touchSound();
        super.onBackPressed();
    }

}
