package com.example.ponycui_home.svgaplayer;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGARVImageView;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miaojun on 2020-01-08.
 * mail:1290846731@qq.com
 */
public class AnimationFromRVActivity extends Activity {
    RecyclerView mSvgaRecycleView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSvgaRecycleView = new RecyclerView(this);
        setContentView(mSvgaRecycleView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mSvgaRecycleView.setLayoutManager(linearLayoutManager);
        SVGAListAdapter adapter = new SVGAListAdapter();
        adapter.setDataList(getList());
        mSvgaRecycleView.setAdapter(adapter);
    }

    private List<ItemSVGABean> getList() {
        List<ItemSVGABean> list = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            ItemSVGABean bean = new ItemSVGABean();
            bean.name = "test2.svga";
            bean.isNeedResume = true;
            list.add(bean);
        }
        return list;
    }

    class SVGAListAdapter extends RecyclerView.Adapter<SVGAListAdapter.ViewHolder> {
        List<ItemSVGABean> dataList = new ArrayList<>();

        void setDataList(List<ItemSVGABean> list) {
            dataList.clear();
            dataList.addAll(list);
        }

        @NonNull
        @Override
        public SVGAListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_svga, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SVGAListAdapter.ViewHolder viewHolder, int i) {
            viewHolder.setItem(dataList.get(i));
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }


        class ViewHolder extends RecyclerView.ViewHolder {

            ViewHolder(@NonNull View itemView) {
                super(itemView);
            }

            void setItem(ItemSVGABean bean) {
                ((SVGARVImageView) itemView).setNeedResume(bean.isNeedResume);
                SVGAParser parser = new SVGAParser(itemView.getContext());
                parser.decodeFromAssets(bean.name, new SVGAParser.ParseCompletion() {
                    @Override
                    public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                        ((SVGARVImageView) itemView).setVideoItem(videoItem);
                        ((SVGARVImageView) itemView).startAnimation();
                    }

                    @Override
                    public void onError() {

                    }
                });
            }

        }
    }

    class ItemSVGABean {
        String name;
        boolean isNeedResume;
    }
}
