package com.example.monko.foreach;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class RoomsActivity extends AppCompatActivity {
/*    private Button push;
    private EditText roomName;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> listOfRoom=new ArrayList<>();
    private DatabaseReference Room= FirebaseDatabase.getInstance().getReference().child("Rooms");

    private FirebaseAuth mauthh;
    private DatabaseReference mdatabseusers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);
        push=(Button)findViewById(R.id.push);
        roomName=(EditText)findViewById(R.id.roomName);
        listView=(ListView)findViewById(R.id.listView);
        arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,listOfRoom);
        listView.setAdapter(arrayAdapter);


        mauthh=FirebaseAuth.getInstance();
        final String user_id=mauthh.getCurrentUser().getUid();


        push.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (roomName.getText().toString()!=""){
                    Map<String,Object>map=new HashMap<String, Object>();
                    map.put(roomName.getText().toString(),"");
                    Room.updateChildren(map);
                    roomName.setText("");
                }

            }
        });
        Room.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String>set=new HashSet<String>();
                Iterator i=dataSnapshot.getChildren().iterator();
                while (i.hasNext()){

                    set.add(((DataSnapshot)i.next()).getKey());
                }

                listOfRoom.clear();
                listOfRoom.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
         listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 Intent chat=new Intent(getApplicationContext(),ChatRoomActivity.class);
                 chat.putExtra("roomName",((TextView)view).getText().toString());
                 chat.putExtra("user_id",user_id);
                 startActivity(chat);

             }
         });




    }*/
}
