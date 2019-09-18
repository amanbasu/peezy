package com.apposite.weartest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

class MyCustomAdapter extends BaseAdapter {

    private  ArrayList<String> mData;
    private LayoutInflater mInflater;
    private Context context;

    public MyCustomAdapter(Context context, ArrayList<String> results) {
        this.mData = results;
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public class Holder
    {
        TextView os_text;
        ImageView os_img;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder = new Holder();
        View rowView;

        rowView = mInflater.inflate(R.layout.custom_grid_layout, null);
        holder.os_text = rowView.findViewById(R.id.textView);
        holder.os_img = rowView.findViewById(R.id.imageView);

        holder.os_text.setText(mData.get(position));
        holder.os_img.setImageResource(R.drawable.boy);

        return rowView;
    }

}
