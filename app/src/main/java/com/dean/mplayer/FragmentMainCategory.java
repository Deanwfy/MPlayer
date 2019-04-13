package com.dean.mplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
        category.add("在线音乐");
        LinearLayoutManager musicListLocalRecyclerLayoutManager = new LinearLayoutManager(activityMain);
        mainCategoryRecyclerView.setLayoutManager(musicListLocalRecyclerLayoutManager);
        MenuClockRecyclerAdapter menuClockRecyclerAdapter = new MenuClockRecyclerAdapter(category);
        menuClockRecyclerAdapter.setOnItemClickListener(((view, position) -> {
            switch (position){
                case 0:
                    Intent intentMusicLocal = new Intent(activityMain, ActivityMusicLocal.class);
                    startActivity(intentMusicLocal);
                    break;
                case 1:
                    Toast.makeText(activityMain, "开发中...", Toast.LENGTH_SHORT).show();
                    break;
            }
        }));
        mainCategoryRecyclerView.setAdapter(menuClockRecyclerAdapter);
        menuClockRecyclerAdapter.notifyDataSetChanged();
    }

}