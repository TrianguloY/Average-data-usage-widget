package com.trianguloy.continuousDataUsage.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.trianguloy.continuousDataUsage.R;
import com.trianguloy.continuousDataUsage.common.Preferences;
import com.trianguloy.continuousDataUsage.common.Tweaks;
import com.trianguloy.continuousDataUsage.common.Utils;

import java.util.ArrayList;

/**
 * ListAdapter for the {@link HistoryActivity}
 */
public class ListAdapter extends BaseAdapter {

    /**
     * total data of the full period
     */
    private float totalData;

    /**
     * Context used
     */
    private final Context cntx;

    /**
     * Preferences
     */
    private final Preferences pref;

    /**
     * Items in the list
     */
    private ArrayList<Item> items = new ArrayList<>();
    private ArrayList<Item> itemsTemp = new ArrayList<>();

    /**
     * Data per day
     */
    private float dataPerDay;

    /**
     * Adds an average item, doesn't refresh
     */
    void addAverageItem(double usage, String date) {
        final Item item = new Item(usage, date, Item.Type.AVERAGE);
        itemsTemp.add(0, item);

        // update max width
        String text = item.usageString();
        if (text.length() > dummy_usage_text.length()) {
            dummy_usage_text = text;
        }
    }

    /**
     * Dummy view to extract sizes
     */
    private View dummy;
    private TextView dummy_usage;
    private String dummy_usage_text;

    //---------- Public -----------

    /**
     * Default constructor
     *
     * @param cntx context
     */
    ListAdapter(Context cntx) {
        this.cntx = cntx;
        this.pref = new Preferences(cntx);
    }

    /**
     * Adds a total item, doesn't refresh
     */
    void addTotalItem(double usage, String label) {
        var item = new Item(usage, label, Item.Type.TOTAL);
        itemsTemp.add(0, item);


        // update max width
        String text = item.usageString();
        if (text.length() > dummy_usage_text.length()) {
            dummy_usage_text = text;
        }
    }

    /**
     * Adds a separator, doesn't refresh
     */
    void addSeparator() {
        itemsTemp.add(0, new Item(0, "", Item.Type.SEPARATOR));
    }

    /**
     * sets the total data
     */
    void setTotalData(float totalData) {
        this.totalData = totalData;
    }

    /**
     * Removes all the items, doesn't refresh
     */
    void clearItems() {
        itemsTemp.clear();
        dummy_usage_text = "";
    }

    /**
     * sets the data per day
     */
    void setDataPerDay(float dataPerDay) {
        this.dataPerDay = dataPerDay;
    }

    /**
     * Sets the dummy view
     */
    public void setDummyView(View dummy) {
        this.dummy = dummy;
        dummy_usage = dummy.findViewById(R.id.lv_txt_usage);
    }

    //------------ Adapter overrides ---------------


    @Override
    public void notifyDataSetChanged() {
        items = itemsTemp;
        itemsTemp = new ArrayList<>();
        dummy_usage.setText(dummy_usage_text);
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public long getItemId(int i) {
        return items.get(i).label.hashCode();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // inflate the layout for each list row
        if (convertView == null) {
            convertView = LayoutInflater.from(cntx).
                    inflate(R.layout.lv_item, parent, false);

            for (int id : new int[]{R.id.lv_txt_date, R.id.lv_txt_usage, R.id.lv_pgb_negative, R.id.lv_pgb_positive}) {
                View view = convertView.findViewById(id);
                ViewGroup.LayoutParams lp = view.getLayoutParams();
                lp.width = dummy.findViewById(id).getMeasuredWidth();
                view.setLayoutParams(lp);
            }
        }

        // get current item to be displayed
        Item currentItem = (Item) getItem(position);

        // get the Views
        TextView txt_date = convertView.findViewById(R.id.lv_txt_date);
        TextView txt_usage = convertView.findViewById(R.id.lv_txt_usage);
        ProgressBar pgb_positive = convertView.findViewById(R.id.lv_pgb_positive);
        ProgressBar pgb_negative = convertView.findViewById(R.id.lv_pgb_negative);

        if (currentItem.type == Item.Type.SEPARATOR) {
            // separator, just hide and return
            txt_date.setVisibility(View.GONE);
            txt_usage.setVisibility(View.GONE);
            pgb_positive.setVisibility(View.GONE);
            pgb_negative.setVisibility(View.GONE);
            return convertView;
        } else {
            // non-separator, unhide
            txt_date.setVisibility(View.VISIBLE);
            txt_usage.setVisibility(View.VISIBLE);
            pgb_positive.setVisibility(View.VISIBLE);
            pgb_negative.setVisibility(View.VISIBLE);
        }

        // sets the properties
        txt_date.setText(currentItem.label);
        txt_usage.setText(currentItem.usageString());

        double rate = currentItem.usage / (currentItem.type == Item.Type.TOTAL ? totalData : dataPerDay);
        if (rate > 1) {
            // more than average/total
            pgb_negative.setProgress(0);
            pgb_positive.setProgress(Utils.dbl2int((rate % 1) * pgb_positive.getMax()));
            pgb_positive.setSecondaryProgress(rate > 2 ? pgb_positive.getMax() : 0);
        } else {
            // less than average
            pgb_negative.setProgress(Utils.dbl2int((1 - rate) * pgb_negative.getMax()));
            pgb_positive.setProgress(0);
            pgb_positive.setSecondaryProgress(0);
        }

        // tweaks
        if (pref.getTweak(Tweaks.Tweak.capNoWarp) && pgb_positive.getSecondaryProgress() > 0) {
            // cap
            pgb_positive.setProgress(pgb_positive.getMax());
            pgb_positive.setSecondaryProgress(0);
        }

        // returns the view for the current row
        return convertView;
    }

    /**
     * Each element in the list
     */
    class Item {
        /**
         * The day
         */
        String label;

        /**
         * The usage
         */
        double usage;
        /**
         * Type of the row
         */
        Type type;
        Item(double usage, String label, Type type) {
            this.usage = usage;
            this.label = label;
            this.type = type;
        }

        /**
         * @return the usage string
         */
        String usageString() {
            return Utils.formatData(pref, "{0} / {M}", usage, (double) (type == Type.TOTAL ? totalData : dataPerDay));
        }

        /**
         * Type of the row
         */
        enum Type {
            /**
             * An average value
             */
            AVERAGE,
            /**
             * A total value
             */
            TOTAL,
            /**
             * A separator
             */
            SEPARATOR
        }
    }
}
