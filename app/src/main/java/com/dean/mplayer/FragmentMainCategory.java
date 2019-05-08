package com.dean.mplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class FragmentMainCategory extends Fragment {

    public FragmentMainCategory() {
        // Required empty public constructor
    }

    private View fragmentMainCategory;
    private RecyclerView mainCategoryRecyclerView;
    private Activity activityMain;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fragmentMainCategory = inflater.inflate(R.layout.fragment_main_category, container, false);
        return fragmentMainCategory;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainCategoryRecyclerView = fragmentMainCategory.findViewById(R.id.main_list_category);
        setListAdapter();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityMain = (Activity)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    // 歌曲列表显示适配器
    public void setListAdapter() {
        List<String> category = new ArrayList<>();
        category.add("本地音乐");
        category.add("音乐人");
        category.add("美国Billboard周榜");
        LinearLayoutManager musicListLocalRecyclerLayoutManager = new LinearLayoutManager(activityMain);
        mainCategoryRecyclerView.setLayoutManager(musicListLocalRecyclerLayoutManager);
        BaseRecyclerAdapter baseRecyclerAdapter = new BaseRecyclerAdapter(category);
        baseRecyclerAdapter.setOnItemClickListener(((view, position) -> {
            ActivityOptionsCompat optionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(activityMain, view, "CategoryName");
            switch (position){
                case 0:
                    Intent intentMusicLocal = new Intent(activityMain, ActivityMusicLocal.class);
                    startActivity(intentMusicLocal, optionsCompat.toBundle());
                    break;
                case 1:
                    Intent intentMusicArtist = new Intent(activityMain, ActivityMusicArtist.class);
                    startActivity(intentMusicArtist, optionsCompat.toBundle());
                    break;
                    // TODO
                case 2:
                    Intent intentMusicOnlineTopBillboard = new Intent(activityMain, ActivityMusicOnlineTopBillboard.class);
                    startActivity(intentMusicOnlineTopBillboard, optionsCompat.toBundle());
                    break;
            }
        }));
        mainCategoryRecyclerView.setAdapter(baseRecyclerAdapter);
        baseRecyclerAdapter.notifyDataSetChanged();
    }

}
