package com.angel.gooded;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends ArrayAdapter<LatLonitem> {
    private final List<LatLonitem> mItems;
    private final Context mContext;
    private final LayoutInflater inflater;

    public SearchAdapter(Context context, int resourceId, ArrayList<LatLonitem> items) {
        super(context, resourceId, items);

        mContext = context;
        mItems = items;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_row_dialog, null);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.city_name_dialog);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LatLonitem item = getItem(position);
        if (item != null) {
            // This is where you set up the views.
            // This is just an example of what you could do.
            holder.text.setText(item.getName());
            holder.lat = item.getLat();
            holder.lng = item.getLng();

        }

        return convertView;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public LatLonitem getItem(int position) {
        return mItems.get(position);
    }

    public class ViewHolder {
        TextView text;
        String lat;
        String lng;
    }
}