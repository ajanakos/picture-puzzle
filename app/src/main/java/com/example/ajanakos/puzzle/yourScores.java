package com.example.ajanakos.puzzle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ajanakos on 3/13/16.
 */
public class yourScores extends Activity {

    private ArrayList<ListElement> aList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scores);

        aList = new ArrayList<ListElement>();
        aa = new MyAdapter(this, R.layout.list_element, aList);
        ListView myListView = (ListView) findViewById(R.id.listView);
        myListView.setAdapter(aa);

        SQLiteDatabase db = openOrCreateDatabase("puz", MODE_PRIVATE, null);

        try {
            Cursor resultSet = db.rawQuery("Select * from scores", null);

            String puzzlePic[] = new String[resultSet.getCount()];
            String solveTime[] = new String[resultSet.getCount()];
            String moveCount[] = new String[resultSet.getCount()];
            String difficulty[] = new String[resultSet.getCount()];

            int i = 0;
            resultSet.moveToFirst();
            while (!resultSet.isAfterLast()) {
                puzzlePic[i] = resultSet.getString(0);
                solveTime[i] = resultSet.getString(1);
                moveCount[i] = resultSet.getString(2);
                difficulty[i] = resultSet.getString(3);
                i++;
                resultSet.moveToNext();
            }
            db.close();


            for (int x = 0; puzzlePic.length > x; x++) {
                ListElement le = new ListElement();
                le.textLabel = moveCount[x] + " moves";
                le.buttonLabel = "Solved in " + solveTime[x] + " seconds";
                le.imageUri = Uri.parse(puzzlePic[x]);
                int d = (int) Math.sqrt(Integer.parseInt(difficulty[x]));
                le.dimension = ""+d+" x "+d;

                aList.add(le);
            }
            aa.notifyDataSetChanged();


        } catch (android.database.sqlite.SQLiteException noTable) {}
    }

    // Send us back to home
    public void goBack(View v) {
        // Create new intent for activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private class ListElement {
        ListElement() {};

        ListElement(String tl, String bl, Uri uri, String dim) {
            textLabel = tl;
            buttonLabel = bl;
            imageUri = uri;
            dimension = dim;
        }

        public String textLabel;
        public String buttonLabel;
        public Uri imageUri;
        public String dimension;
    }
    private class MyAdapter extends ArrayAdapter<ListElement> {

        int resource;
        Context context;

        public MyAdapter(Context _context, int _resource, List<ListElement> items) {
            super(_context, _resource, items);
            resource = _resource;
            context = _context;
            this.context = _context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout newView;

            ListElement w = getItem(position);

            // Inflate a new view if necessary.
            if (convertView == null) {
                newView = new LinearLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
                vi.inflate(resource,  newView, true);
            } else {
                newView = (LinearLayout) convertView;
            }

            // Fills in the view.
            TextView tv = (TextView) newView.findViewById(R.id.itemText);
            Button b = (Button) newView.findViewById(R.id.itemButton1);
            ImageView iv = (ImageView) newView.findViewById(R.id.itemPic);
            TextView tv1 = (TextView) newView.findViewById(R.id.itemText1);
            tv.setText(w.textLabel);
            tv1.setText(w.dimension);
            b.setText(w.buttonLabel);
            iv.setImageURI(w.imageUri);

            // Sets a listener for the button, and a tag for the button as well.
            b.setTag(new Integer(position));


            // Set a listener for the whole list item.
            newView.setTag(w.textLabel);
            newView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String s = v.getTag().toString();
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, s, duration);
                    toast.show();
                }
            });

            return newView;
        }
    }

    private MyAdapter aa;
}
