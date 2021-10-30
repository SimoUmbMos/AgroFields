package com.mosc.simo.ptuxiaki3741.view;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewHistoryEntryBinding;

public class HistoryEntryView extends ConstraintLayout {
    public HistoryEntryView(@NonNull Context context) {
        super(context);
        ViewHistoryEntryBinding binding = ViewHistoryEntryBinding.bind(
                inflate(getContext(), R.layout.view_history_entry, this)
        );
        binding.clHistoryEntryData.setVisibility(GONE);
        binding.tvHistoryEntryAction.setVisibility(GONE);
    }
    public HistoryEntryView(
            @NonNull Context context,
            String username,
            String date,
            String action,
            OnClickListener listener
    ) {
        super(context);
        ViewHistoryEntryBinding binding = ViewHistoryEntryBinding.bind(
                inflate(getContext(), R.layout.view_history_entry, this)
        );
        binding.tvHistoryEntryUser.setText(username);
        binding.tvHistoryEntryDate.setText(date);
        binding.tvHistoryEntryAction.setText(action);
        binding.clHistoryEntryData.setOnClickListener(listener);
        binding.tvHistoryEntryAction.setOnClickListener(listener);
    }
}
