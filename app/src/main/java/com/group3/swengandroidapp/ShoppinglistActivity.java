package com.group3.swengandroidapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class ShoppinglistActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> arrayList;
    ArrayAdapter<String> arrayAdapter;
    String messageText;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list_gui_new);
        listView = findViewById(R.id.listView);
        arrayList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(arrayAdapter);

        //When something is clicked on in the list, the EditMessage method is called so that the message can be edited.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent();
                intent.setClass(ShoppinglistActivity.this,EditMessageClass.class);
                intent.putExtra(Intent_Constants.INTENT_MESSAGE_DATA, arrayList.get(position));
                intent.putExtra(Intent_Constants.INTENT_ITEM_POSITION,position);
                startActivityForResult(intent,Intent_Constants.INTENT_REQUEST_CODE_TWO);
            }
        });

        //This will read the items from the ShoppingList.txt file that we create and will add all of the items to the list
        //when the app is opened again.
        try {
            Scanner sc = new Scanner(openFileInput("ShoppingList.txt"));
            //Is their something in the file?
            while(sc.hasNextLine()){
                //If so, pass the scanner to the next line and pass the data to the string data.
                String data = sc.nextLine();
                //Then add the data to the arrayList to show on the shopping list.
                arrayAdapter.add(data);
            }
            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //When the user exits the application, any data they have left in the shopping list will be saved to a text file called ShoppingList.txt
    @Override
    public void onBackPressed(){
        try{
            deleteFile("ShoppingList.txt");
            PrintWriter pw = new PrintWriter(openFileOutput("ShoppingList.txt", Context.MODE_PRIVATE));
            for(String data : arrayList){
                pw.println(data);
            }
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finish();
    }

    //When the 'add' button is clicked from the shopping list gui, this code will run and send an intent request to the EditField Class.
    public void onClick(View v){
        Intent intent = new Intent();
        intent.setClass(ShoppinglistActivity.this,EditFieldClass.class);
        startActivityForResult(intent,Intent_Constants.INTENT_REQUEST_CODE);
    }

    //When the clear all button is clicked from the shopping list gui, this code will run and clear the list.
    public void clearAllButtonClicked(View v){
        int i=arrayList.size();
        while(i!=0){
            arrayList.remove(i-1);
            i--;
            arrayAdapter.notifyDataSetChanged();
        }
    }

    //After the button has been clicked, the activity is established via request/result code and the appropriate method run.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //If it is result code 1, then the user is trying to add some text to the list, so this code should run.
        if(resultCode==Intent_Constants.INTENT_REQUEST_CODE){
            messageText=data.getStringExtra(Intent_Constants.INTENT_MESSAGE_FIELD);
            arrayList.add(messageText);
            arrayAdapter.notifyDataSetChanged();
        }
        //If it is result code 2, then the user is trying to modify their text, so this code should run.
        else if(resultCode==Intent_Constants.INTENT_REQUEST_CODE_TWO){
            messageText = data.getStringExtra(Intent_Constants.INTENT_CHANGED_MESSAGE);
            position = data.getIntExtra(Intent_Constants.INTENT_ITEM_POSITION,-1);
            arrayList.remove(position);
            arrayList.add(position,messageText);
            arrayAdapter.notifyDataSetChanged();
        }

        //If it is result code 3, then the user is trying to delete their text, so this code should run.
        else if(resultCode==Intent_Constants.INTENT_REQUEST_CODE_THREE){
            //noinspection StatementWithEmptyBody
            if(arrayList.size()==0){
                //Do Nothing
            }
            else{
                position = data.getIntExtra(Intent_Constants.INTENT_ITEM_POSITION,-1);
                arrayList.remove(position);
                arrayAdapter.notifyDataSetChanged();
            }
        }

        //If it is result code 3, then the user is trying to delete their text, so this code should run.
        else //noinspection StatementWithEmptyBody
            if(resultCode==Intent_Constants.INTENT_REQUEST_CODE_FOUR) {
            //Do Nothing
        }
    }
}