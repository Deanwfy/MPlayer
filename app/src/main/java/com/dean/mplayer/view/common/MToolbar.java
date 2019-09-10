package com.dean.mplayer.view.common;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;

import com.dean.mplayer.R;
import com.dean.mplayer.base.BaseActivity;
import com.google.android.material.appbar.AppBarLayout;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.toolbar)
public class MToolbar extends AppBarLayout {

    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    @ViewById(R.id.title)
    AppCompatTextView titleTextView;

    @ViewById(R.id.image_view_title)
    AppCompatImageView titleImageView;

    @ViewById(R.id.image_button_left)
    AppCompatImageButton leftImageButton;

    @ViewById(R.id.image_button_right)
    AppCompatImageButton rightImageButton;

    public MToolbar(Context context) {
        super(context);
    }

    public MToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private static final int NONE = 0;
    private int backgroundColor;
    private int titleRes;
    private int titleImageRes;
    private int leftImageRes;
    private OnClickListener leftClickListener;
    private int rightImageRes;
    private OnClickListener rightClickListener;
    private boolean hasBack;

    public MToolbar setBackgroundcolor(int color) {
        this.backgroundColor = color;
        return this;
    }

    public MToolbar setTitle(int titleRes) {
        this.titleRes = titleRes;
        return this;
    }

    public MToolbar setTitleImage(int titleImageRes) {
        this.titleImageRes = titleImageRes;
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
        if (titleRes == NONE) {
            titleTextView.setVisibility(GONE);
        } else {
            titleTextView.setVisibility(VISIBLE);
            titleTextView.setText(titleRes);
        }

        // update title image
        if (titleImageRes == NONE) {
            titleImageView.setVisibility(GONE);
        } else {
            titleImageView.setVisibility(VISIBLE);
            titleImageView.setImageResource(titleImageRes);
        }

        // update left item
        if (hasBack) {
            leftImageRes = R.drawable.ic_back;
            leftClickListener = view -> ((BaseActivity) getContext()).onBackPressed();
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

}
