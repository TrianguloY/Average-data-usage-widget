package com.trianguloy.continuousDataUsage.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.trianguloy.continuousDataUsage.R;

import java.util.ArrayList;
import java.util.Locale;

public class ListAdapter extends BaseAdapter {

    class Item{
        double usage;
        String date;

        public Item(double usage, String date) {
            this.usage = usage;
            this.date = date;
        }
    }

    //---------------------
    private Context cntx;

    private ArrayList<Item> items = new ArrayList<>();

    public ListAdapter(Context cntx) {
        this.cntx = cntx;
    }

    public void addItem(double usage, String date){
        items.add(new Item(usage, date));
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return items.get(i).date.hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // inflate the layout for each list row
        if (convertView == null) {
            convertView = LayoutInflater.from(cntx).
                    inflate(R.layout.lv_item, parent, false);
        }

        // get current item to be displayed
        Item currentItem = (Item) getItem(position);

        // get the TextView for item name and item description
        TextView txt_date = convertView.findViewById(R.id.lv_txt_date);
        TextView txt_usage = convertView.findViewById(R.id.lv_txt_usage);

        //sets the text for item name and item description from the current item object
        txt_date.setText(currentItem.date);

        txt_usage.setText(String.format(Locale.US, "%.2f MB", currentItem.usage));

        // returns the view for the current row
        return convertView;
    }
}
