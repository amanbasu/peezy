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
import java.util.Random;

class MyCustomAdapter extends BaseAdapter {

    private  ArrayList<String> mData;
    private LayoutInflater mInflater;
    private Context context;
    private int[] avatars = {R.drawable.boy, R.drawable.boy1, R.drawable.boy2, R.drawable.man,
                             R.drawable.girl};
    Random gen = new Random();

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

        String name = mData.get(position);
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        int rnd = gen.nextInt(avatars.length);

        holder.os_text.setText(name);
        holder.os_img.setImageResource(avatars[rnd]);

        return rowView;
    }

}
