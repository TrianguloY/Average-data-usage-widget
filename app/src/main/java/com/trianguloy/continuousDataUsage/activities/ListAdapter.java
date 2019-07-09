package com.trianguloy.continuousDataUsage.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.trianguloy.continuousDataUsage.R;
import com.trianguloy.continuousDataUsage.common.Utils;

import java.util.ArrayList;
import java.util.Locale;

/**
 * ListAdapter for the {@link HistoryActivity}
 */
public class ListAdapter extends BaseAdapter {

    /**
     * Each element in the list
     */
    class Item{
        /**
         * The usage
         */
        double usage;
        /**
         * The day
         */
        String date;

        Item(double usage, String date) {
            this.usage = usage;
            this.date = date;
        }
    }

    /**
     * Context used
     */
    private Context cntx;

    /**
     * Items in the list
     */
    private ArrayList<Item> items = new ArrayList<>();
    private ArrayList<Item> itemsTemp = new ArrayList<>();

    /**
     * Data per day
     */
    private float dataPerDay;

    //---------- Public -----------

    /**
     * Default constructor
     * @param cntx context
     */
    ListAdapter(Context cntx) {
        this.cntx = cntx;
    }

    /**
     * Adds an item, doesn't refresh
     * @param usage the usage of the item
     * @param date the date of the item
     */
    void addItem(double usage, String date){
        itemsTemp.add(0, new Item(usage, date));
    }

    /**
     * Removes all the items, doesn't refresh
     */
    void clearItems(){itemsTemp.clear();}

    /**
     * sets the data per day
     */
    void setDataPerDay(float dataPerDay) {
        this.dataPerDay = dataPerDay;
    }

    //------------ Adapter overrides ---------------


    @Override
    public void notifyDataSetChanged() {
        items = itemsTemp;
        itemsTemp = new ArrayList<>();
        super.notifyDataSetChanged();
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

        // get the Views
        TextView txt_date = convertView.findViewById(R.id.lv_txt_date);
        TextView txt_usage = convertView.findViewById(R.id.lv_txt_usage);
        ProgressBar pgb_positive = convertView.findViewById(R.id.lv_pgb_positive);
        ProgressBar pgb_negative = convertView.findViewById(R.id.lv_pgb_negative);

        // sets the properties
        txt_date.setText(currentItem.date);

        txt_usage.setText(String.format(Locale.US, "%.2f / %.2f MB", currentItem.usage, dataPerDay));

        double rate = currentItem.usage / dataPerDay;
        if(rate > 1){
            pgb_negative.setProgress(0);
            pgb_positive.setProgress(Utils.dbl2int ( (rate % 1) * pgb_positive.getMax()) );
            pgb_positive.setSecondaryProgress( rate > 2 ? pgb_positive.getMax() : 0);
        }else{
            pgb_positive.setProgress(0);
            pgb_negative.setProgress(Utils.dbl2int( (1 - rate) * pgb_negative.getMax() ));
            pgb_positive.setSecondaryProgress(0);
        }

        // returns the view for the current row
        return convertView;
    }
}
