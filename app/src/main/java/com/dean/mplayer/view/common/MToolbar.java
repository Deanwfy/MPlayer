package com.dean.mplayer.view.common;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.dean.mplayer.R;
import com.dean.mplayer.base.BaseActivity;
import com.dean.mplayer.util.Utils;
import com.google.android.material.appbar.AppBarLayout;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.toolbar)
public class MToolbar extends AppBarLayout {

    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    @ViewById(R.id.title)
    AppCompatTextView titleTextView;

    @ViewById(R.id.image_button_left)
    AppCompatImageButton leftImageButton;

    @ViewById(R.id.image_button_right)
    AppCompatImageButton rightImageButton;

    @ViewById(R.id.search_view)
    SearchView searchView;

    private Context context;

    public MToolbar(Context context) {
        super(context);
    }

    public MToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    private static final int NONE = 0;
    private int backgroundColor;
    private int titleRes;
    private String title;
    private int leftImageRes;
    private OnClickListener leftClickListener;
    private int rightImageRes;
    private OnClickListener rightClickListener;
    private boolean hasBack;
    private String queryHint;
    private SearchView.OnQueryTextListener queryTextListener;

    public MToolbar setBackgroundcolor(int color) {
        this.backgroundColor = color;
        return this;
    }

    public MToolbar setTitle(int titleRes) {
        this.titleRes = titleRes;
        return this;
    }

    public MToolbar setTitle(String title) {
        this.title = title;
        return this;
    }

    public MToolbar setLeftItem(int imageRes, OnClickListener clickListener) {
        this.leftImageRes = imageRes;
        this.leftClickListener = clickListener;
        return this;
    }

    public MToolbar setRightItem(int imageRes, OnClickListener clickListener) {
        this.rightImageRes = imageRes;
        this.rightClickListener = clickListener;
        return this;
    }

    public MToolbar setHasBack(boolean hasBack) {
        this.hasBack = hasBack;
        return this;
    }

    public MToolbar setSearchView(String queryHint, SearchView.OnQueryTextListener queryTextListener) {
        this.queryHint = queryHint;
        this.queryTextListener = queryTextListener;
        return this;
    }

    public void build() {
        // update backgroundColor
        if (backgroundColor == NONE) {
            backgroundColor = R.color.colorPrimary;
            titleTextView.setTextColor(getContext().getResources().getColor(R.color.colorBarText));
        } else {
            titleTextView.setTextColor(getContext().getResources().getColor(R.color.colorBarText));
        }
        toolbar.setBackgroundColor(getContext().getResources().getColor(backgroundColor));

        // update title
        if (titleRes == NONE && title == null) {
            titleTextView.setVisibility(GONE);
        } else {
            titleTextView.setVisibility(VISIBLE);
            if (titleRes != NONE) titleTextView.setText(titleRes);
            if (title != null) titleTextView.setText(title);
        }

        // update left item
        if (hasBack) {
            leftImageRes = R.drawable.ic_back;
            leftClickListener = view -> ((BaseActivity) getContext()).onBackPressed();
        }

        if (queryHint == null) {
            searchView.setVisibility(GONE);
        } else {
            updateSearchView(queryHint, queryTextListener);
        }

        if (leftImageRes == NONE) {
            hiddenLeftItem();
        } else {
            updateLeftItem(leftImageRes, leftClickListener);
        }

        // update right item
        if (rightImageRes == NONE) {
            hiddenRightItem();
        } else {
            updateRightItem(rightImageRes, rightClickListener);
        }
    }

    private int getRippleEffect() {
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
        return typedValue.resourceId;
    }

    public void updateRightItem(int imageRes, OnClickListener clickListener) {
        rightImageRes = imageRes;
        rightClickListener = clickListener;
        rightImageButton.setVisibility(VISIBLE);
        rightImageButton.setImageResource(imageRes);
        rightImageButton.setBackgroundResource(getRippleEffect());
        rightImageButton.setOnClickListener(clickListener);
    }

    public void hiddenRightItem() {
        rightImageButton.setVisibility(GONE);
    }

    public void unSelectRightItem() {
        rightImageButton.setSelected(false);
    }

    public AppCompatImageButton getRightImageButton() {
        return rightImageButton;
    }

    public void updateLeftItem(int imageRes, OnClickListener clickListener) {
        leftImageRes = imageRes;
        leftClickListener = clickListener;
        leftImageButton.setVisibility(VISIBLE);
        leftImageButton.setImageResource(imageRes);
        leftImageButton.setBackgroundResource(getRippleEffect());
        leftImageButton.setOnClickListener(clickListener);
    }

    public void hiddenLeftItem() {
        leftImageButton.setVisibility(GONE);
    }

    public void updateSearchView(String queryHint, SearchView.OnQueryTextListener queryTextListener) {
        searchView.setQueryHint(queryHint);
        searchView.setOnQueryTextListener(queryTextListener);
        searchView.setVisibility(VISIBLE);
        searchView.setMaxWidth(Utils.dp2px(context, Utils.px2dp(context, Utils.screenSize(context).getWidth()) - 32));
        if (titleRes != NONE || title != null) {
            searchView.setOnSearchClickListener(view -> titleTextView.setVisibility(GONE));
            searchView.setOnCloseListener(() -> {
                titleTextView.setVisibility(VISIBLE);
                return false;
            });
        }
        EditText editText = searchView.findViewById(R.id.search_src_text);
        editText.setTextColor(ContextCompat.getColor(context, R.color.drawerArrowStyle));
        editText.setHintTextColor(ContextCompat.getColor(context, R.color.editNoticeText));
    }

}
