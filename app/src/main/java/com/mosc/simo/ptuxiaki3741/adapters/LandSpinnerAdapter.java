package com.mosc.simo.ptuxiaki3741.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mosc.simo.ptuxiaki3741.models.Land;

import java.util.List;

public class LandSpinnerAdapter extends ArrayAdapter<Land> {
    private static class ViewHolder {
        TextView text;
    }
    public LandSpinnerAdapter(Context context, List<Land> lands){
        super(context,android.R.layout.simple_list_item_1,lands);
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Land land = getItem(position);

        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            viewHolder.text = view.findViewById(android.R.id.text1);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.text.setText(land.toString());

        return view;
    }
}
