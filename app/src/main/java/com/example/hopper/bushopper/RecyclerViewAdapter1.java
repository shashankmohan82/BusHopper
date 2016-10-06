package com.example.hopper.bushopper;

import android.support.v7.widget.RecyclerView;



        import android.graphics.Bitmap;
        import android.support.v7.widget.RecyclerView;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.TextView;

        import java.util.ArrayList;

/**
 * Created by sHIVAM on 4/10/2016.
 */
public class RecyclerViewAdapter1 extends RecyclerView.Adapter<RecyclerViewAdapter1.ViewHolder> {


    ArrayList<String> data = new ArrayList<String>();
    Integer imagedata;

    private static final int TYPE_HEADER = 0;  // Declaring Variable to Understand which View is being worked on
    // IF the view under inflation and population is header or Item
    private static final int TYPE_ITEM = 1;


    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView menuName;
        private ImageView icon;

        private int Holder_Id;


        public ViewHolder(View v, int viewType) {
            super(v);

            if (viewType == TYPE_ITEM) {
                menuName = (TextView) v.findViewById(R.id.textView1);

                Holder_Id = 1;


            } else {
                icon = (ImageView) v.findViewById(R.id.imageView1);

                Holder_Id = 0;
            }


            v.setClickable(true);


        }


    }

    public RecyclerViewAdapter1.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // create a new view

        // set the view's size, margins, paddings and layout parameters

        if (viewType == TYPE_ITEM) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler1, parent, false);
            ViewHolder vh = new ViewHolder(v, TYPE_ITEM);
            return vh;
        }
        if (viewType == TYPE_HEADER) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.header1, parent, false);
            ViewHolder vh = new ViewHolder(v, TYPE_HEADER);
            return vh;
        }

        return null;
    }

    public RecyclerViewAdapter1(ArrayList<String> dataset, Integer imageResource)  {
        data = dataset;
        imagedata = imageResource;


    }
    public void onBindViewHolder(ViewHolder v, int position) {
        if (v.Holder_Id == 1) {                              // as the list view is going to be called after the header view so we decrement the
            // position by 1 and pass it to the holder while setting the text and image
            v.menuName.setText(data.get(position - 1)); // Setting the Text with the array of our Titles
            // Settimg the image with array of our icons
        } else {

            // Similarly we set the resources for header view
            v.icon.setImageResource(imagedata);

        }

        // v.menuName.setText(data.get(position1));
        //v.icon.setImageResource(imagedata.get(position));


    }

    public int getItemCount() {
        return data.size() + 1;

    }


    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }



}