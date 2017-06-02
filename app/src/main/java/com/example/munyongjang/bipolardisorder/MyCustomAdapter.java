package com.example.munyongjang.bipolardisorder;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by munyongjang on 3/28/17.
 */

public class MyCustomAdapter extends BaseAdapter implements ListAdapter {
    private List<String> textList = new ArrayList<String>();
    private List<Integer> idList = new ArrayList<>();
    private Context context;
    private EpisodeLogDatabaseHandler mELDH;

    public MyCustomAdapter(List<String> list, List<Integer> idList , Context context, EpisodeLogDatabaseHandler eldh) {
        this.textList = list;
        this.idList = idList;
        this.context = context;
        mELDH = eldh;
    }

    @Override
    public int getCount() {
        return textList.size();
    }

    @Override
    public Object getItem(int pos) {
        return textList.get(pos);
    }

    // not sure if this is the correct way
    @Override
    public long getItemId(int pos) {
        return 0;
        //just return 0 if your list items do not have an Id variable.
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.phase_listing, null);
        }

        //Handle TextView and display string from your list
        TextView listItemText = (TextView)view.findViewById(R.id.list_item_string);
        listItemText.setText(textList.get(position));

        // Handle buttons and add onClickListeners
        ImageButton deleteBtn = (ImageButton)view.findViewById(R.id.delete_btn);
        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                textList.remove(position);
                int id = idList.get(position);
                idList.remove(position);
                Log.d("is this getting deleted", "" + mELDH.deleteEntry(id));
                notifyDataSetChanged();
            }
        });

        return view;
    }
}
