package com.mosc.simo.ptuxiaki3741.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHistoryItemBinding;

public class HistoryItemView extends ConstraintLayout {
    private final ViewHistoryItemBinding binding;
    private String mAction,mDate;

    public HistoryItemView(Context context) {
        super(context);
        binding = ViewHistoryItemBinding.bind(
                inflate(getContext(), R.layout.view_history_item, this)
        );
        initData();
        initView();
    }
    public HistoryItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        binding = ViewHistoryItemBinding.bind(
                inflate(getContext(), R.layout.view_history_item, this)
        );
        initData(context, attrs);
        initView();
    }
    public HistoryItemView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        binding = ViewHistoryItemBinding.bind(
                inflate(getContext(), R.layout.view_history_item, this)
        );
        initData(context, attrs);
        initView();
    }
    public HistoryItemView(Context context, String mDate, String mAction) {
        super(context);
        binding = ViewHistoryItemBinding.bind(
                inflate(getContext(), R.layout.view_history_item, this)
        );
        initData(mDate,mAction);
        initView();
    }
    public String getAction() {
        return mAction;
    }
    public String getDate() {
        return mDate;
    }
    public void setAction(String mAction) {
        this.mAction = mAction;
        binding.tvHistoryEntryAction.setText(this.mAction);
    }
    public void setDate(String mDate) {
        this.mDate = mDate;
        binding.tvHistoryEntryDate.setText(this.mDate);
    }

    private void initData() {
        mDate = "";
        mAction = "";
    }
    private void initData(String mDate, String mAction) {
        this.mDate = mDate;
        this.mAction = mAction;
    }
    private void initData(Context context, @Nullable AttributeSet attrs) {
        initData();
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.HistoryItem,
                0,
                0
        );
        try {
            mAction = a.getString(R.styleable.HistoryItem_setAction);
            mDate = a.getString(R.styleable.HistoryItem_setDate);
        } finally {
            a.recycle();
        }
    }
    private void initView() {
        binding.tvHistoryEntryDate.setText(mDate);
        binding.tvHistoryEntryAction.setText(mAction);
    }
}
