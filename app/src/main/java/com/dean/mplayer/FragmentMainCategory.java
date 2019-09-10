package com.dean.mplayer;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dean.mplayer.base.BaseFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EFragment(R.layout.fragment_main_category)
public class FragmentMainCategory extends BaseFragment {

    @ViewById(R.id.main_list_category)
    RecyclerView mainCategoryRecyclerView;

    @AfterViews
    void initViews() {
        setListAdapter();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void setListAdapter() {
        List<String> category = new ArrayList<>();
        category.add("本地音乐");
        category.add("音乐人");
        category.add("专辑");
        category.add("美国Billboard周榜");
        LinearLayoutManager musicListLocalRecyclerLayoutManager = new LinearLayoutManager(_mActivity);
        mainCategoryRecyclerView.setLayoutManager(musicListLocalRecyclerLayoutManager);
        BaseRecyclerAdapter baseRecyclerAdapter = new BaseRecyclerAdapter(category);
        baseRecyclerAdapter.setOnItemClickListener(((view, position) -> {
            switch (position) {
                case 0:
                    ActivityMusicLocal_.intent(_mActivity).start();
                    break;
                case 1:
                    ActivityMusicArtist_.intent(_mActivity).start();
                    break;
                case 2:
                    ActivityMusicAlbum_.intent(_mActivity).start();
                    break;
                case 3:
                    ActivityMusicOnlineTopBillboard_.intent(_mActivity).start();
                    break;
            }
        }));
        mainCategoryRecyclerView.setAdapter(baseRecyclerAdapter);
        baseRecyclerAdapter.notifyDataSetChanged();
    }

}
