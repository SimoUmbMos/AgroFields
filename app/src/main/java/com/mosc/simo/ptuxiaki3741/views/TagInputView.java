package com.mosc.simo.ptuxiaki3741.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.google.android.material.chip.Chip;
import com.mosc.simo.ptuxiaki3741.R;
import com.mosc.simo.ptuxiaki3741.databinding.ViewTagInputBinding;
import com.mosc.simo.ptuxiaki3741.models.entities.TagData;
import com.mosc.simo.ptuxiaki3741.util.DataUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TagInputView extends LinearLayout {
    private final ViewTagInputBinding binding;
    private final List<Chip> chips;
    private final List<TagData> tags, tagsHints;
    private OnTagUpdateListener onTagsUpdate;

    public TagInputView(Context context) {
        super(context);
        binding = ViewTagInputBinding.bind(
                inflate(getContext(), R.layout.view_tag_input, this)
        );
        chips = new ArrayList<>();
        tags = new ArrayList<>();
        tagsHints = new ArrayList<>();
        onTagsUpdate = null;
        init();
    }
    public TagInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        binding = ViewTagInputBinding.bind(
                inflate(getContext(), R.layout.view_tag_input, this)
        );
        chips = new ArrayList<>();
        tags = new ArrayList<>();
        tagsHints = new ArrayList<>();
        onTagsUpdate = null;
        init();
    }
    public TagInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        binding = ViewTagInputBinding.bind(
                inflate(getContext(), R.layout.view_tag_input, this)
        );
        chips = new ArrayList<>();
        tags = new ArrayList<>();
        tagsHints = new ArrayList<>();
        onTagsUpdate = null;
        init();
    }

    public void setTags(List<TagData> tags) {
        this.tags.clear();
        if(tags != null) {
            this.tags.addAll(tags);
        }
        updateChipsTags();
    }
    public void setOnTagsUpdate(OnTagUpdateListener onTagsUpdate) {
        this.onTagsUpdate = onTagsUpdate;
    }
    public void setSelectedTags(List<TagData> tags) {
        if(chips.size()>0){
            for(Chip chip : chips) {
                binding.flChipContainer.removeView(chip);
            }
            chips.clear();
        }
        for (TagData tag : tags){
            addChip(tag, false);
        }
    }

    private void init(){
        binding.etChipInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String text = binding.etChipInput.getText().toString();
                if(text.length()>0){
                    addChip(new TagData(text),true);
                    binding.etChipInput.getText().clear();
                }
            }
            return false;
        });
        binding.etChipInput.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                text = DataUtil.dividersToSpace(text);
                if(text.contains(" ")){
                    List<String> tags = new ArrayList<>(Arrays.asList(text.split(" ")));

                    s.clear();
                    if(tags.size() > 1){
                        for(int i = 0; i < tags.size()-1; i++){
                            addChip(new TagData(tags.get(i)),true);
                        }
                        s.append(tags.get(tags.size()-1));
                    }else if(tags.size() == 1){
                        addChip(new TagData(tags.get(0)),true);
                    }
                }
                populateTagHints(s.toString());
            }
        });
    }
    private void addChip(TagData tag, boolean triggerUpdate){
        if(tag == null) {
            return;
        }
        if(tag.getLabel().isEmpty()) {
            return;
        }
        if(getContext() == null) {
            return;
        }
        for(Chip temp:chips){
            if(temp.getTag().equals(tag)) {
                return;
            }
        }

        Chip chip = new Chip(getContext());
        chip.setTag(tag);
        chip.setText(tag.getLabel());
        chip.setCloseIconVisible(true);
        chip.setClickable(true);
        chip.setCheckable(false);

        binding.flChipContainer.addView(chip,binding.flChipContainer.getChildCount()-1);
        chips.add(chip);

        chip.setOnCloseIconClickListener(v-> removeChip(chip));

        if(triggerUpdate && onTagsUpdate != null){
            onTagsUpdate.onTagAdded(tag);
        }
    }
    private void removeChip(Chip chip) {
        if(chip.getTag().getClass() == TagData.class && onTagsUpdate != null){
            onTagsUpdate.onTagRemoved((TagData) chip.getTag());
        }
        binding.flChipContainer.removeView(chip);
        chips.remove(chip);
    }

    private void updateChipsTags(){
        for(Chip chip : chips){
            if(chip.getTag() == null) continue;
            TagData chipTag = (TagData) chip.getTag();
            if(chipTag.getId() != 0) continue;
            for(TagData tag : tags){
                if (chipTag.getLabel().equals(tag.getLabel())) {
                    chip.setTag(tag);
                    break;
                }
            }
        }

    }
    private void populateTagHints(String text){
        tagsHints.clear();
        String search = text.trim().toLowerCase();
        if(search.length() == 0) return;

        String tempLabel;
        for(TagData tag : tags){
            if(tag == null) continue;

            tempLabel = tag.getLabel().toLowerCase();
            if(tempLabel.contains(search)){
                tagsHints.add(tag);
            }
        }
        for(Chip chip : chips){
            if(chip.getTag() == null) continue;
            if(chip.getTag().getClass() != TagData.class) continue;

            tempLabel = ((TagData) chip.getTag()).getLabel();
            for(TagData tag : tags){
                if(tempLabel.equals(tag.getLabel())){
                    tagsHints.remove(tag);
                    break;
                }
            }
        }
    }

    public interface OnTagUpdateListener{
        void onTagAdded(TagData tag);
        void onTagRemoved(TagData tag);
    }
}
