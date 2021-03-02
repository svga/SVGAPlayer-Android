package com.example.ponycui_home.svgaplayer;

import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.utils.log.SVGALogger;

import java.util.ArrayList;

class SampleItem {

    String title;
    Intent intent;

    public SampleItem(String title, Intent intent) {
        this.title = title;
        this.intent = intent;
    }

}

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<SampleItem> items = new ArrayList();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setupData();
        this.setupListView();
        this.setupSVGAParser();
        this.setupLogger();
        setContentView(listView);
    }

    void setupData() {
        this.items.add(new SampleItem("Animation From Assets", new Intent(this, AnimationFromAssetsActivity.class)));
        this.items.add(new SampleItem("Animation From Network", new Intent(this, AnimationFromNetworkActivity.class)));
        this.items.add(new SampleItem("Animation From Layout XML", new Intent(this, AnimationFromLayoutActivity.class)));
        this.items.add(new SampleItem("Animation With Dynamic Image", new Intent(this, AnimationWithDynamicImageActivity.class)));
        this.items.add(new SampleItem("Animation With Dynamic Click", new Intent(this, AnimationFromClickActivity.class)));
    }

    void setupListView() {
        this.listView = new ListView(this);
        this.listView.setAdapter(new ListAdapter() {
            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }

            @Override
            public boolean isEnabled(int i) {
                return false;
            }

            @Override
            public void registerDataSetObserver(DataSetObserver dataSetObserver) {

            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

            }

            @Override
            public int getCount() {
                return MainActivity.this.items.size();
            }

            @Override
            public Object getItem(int i) {
                return null;
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @Override
            public View getView(final int i, View view, ViewGroup viewGroup) {
                LinearLayout linearLayout = new LinearLayout(MainActivity.this);
                TextView textView = new TextView(MainActivity.this);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MainActivity.this.startActivity(MainActivity.this.items.get(i).intent);
                    }
                });
                textView.setText(MainActivity.this.items.get(i).title);
                textView.setTextSize(24);
                textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                linearLayout.addView(textView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (55 * getResources().getDisplayMetrics().density)));
                return linearLayout;
            }

            @Override
            public int getItemViewType(int i) {
                return 1;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }
        });
        this.listView.setBackgroundColor(Color.WHITE);
    }

    void setupSVGAParser() {
        SVGAParser.Companion.shareParser().init(this);
    }

    private void setupLogger() {
        SVGALogger.INSTANCE.setLogEnabled(true);
    }

}
