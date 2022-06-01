package com.clevergo.vcode;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;

public class InfoBottomSheet extends BottomSheetDialogFragment {

    public OnInputListener inputListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            inputListener = (OnInputListener) getActivity();
        } catch (ClassCastException e) {

        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.info_bottom_sheet, container, false);

        ImageView fullScreen_imageView = v.findViewById(R.id.fullScreen_imageView);
        AutoCompleteTextView activeCodeView_Selector = v.findViewById(R.id.activeCodeView_Selector);
        TextInputLayout activeFile_TextInputLayout = v.findViewById(R.id.activeFile_TextInputLayout);
        activeFile_TextInputLayout.setVisibility(View.GONE);

        if (CodeViewActivity.selectedFileNames[0] != null && CodeViewActivity.isScreenSplit) {
            ArrayAdapter<String> myAdapter = new ArrayAdapter<>(getActivity(),
                    androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                    CodeViewActivity.selectedFileNames);

            activeCodeView_Selector.setAdapter(myAdapter);
            activeFile_TextInputLayout.setVisibility(View.VISIBLE);
            //activeCodeView_Selector.setText(myAdapter.getItem(CodeViewActivity.activeFilePosition));
            activeCodeView_Selector.setOnItemClickListener((parent, view, position, id) -> {
                CodeViewActivity.activeFilePosition = position;
                inputListener.sendInput(BottomSheetCode.SetActiveCodeViewFile);
                //dismiss();
            });
        }

        if (Helper.isFullScreen) {
            fullScreen_imageView.setImageResource(R.drawable.ic_fullscreen_exit);
        } else {
            fullScreen_imageView.setImageResource(R.drawable.ic_fullscreen_24);
        }

        fullScreen_imageView.setOnClickListener(a -> {
            inputListener.sendInput(BottomSheetCode.FullScreen);
            dismiss();
        });

        v.findViewById(R.id.closeBtmSht_imageView).setOnClickListener(a -> dismiss());

        v.findViewById(R.id.copy_imageView).setOnClickListener(a -> {
            inputListener.sendInput(BottomSheetCode.CopyAll);
            dismiss();
        });

        v.findViewById(R.id.addFile_imageView).setOnClickListener(a -> {
            inputListener.sendInput(BottomSheetCode.AddFile);
            dismiss();
        });

        v.findViewById(R.id.search_imageView).setOnClickListener(a -> {
            inputListener.sendInput(BottomSheetCode.Search);
            dismiss();
        });

        ImageView splitScreen_imageView = v.findViewById(R.id.splitScreen_imageView);
        splitScreen_imageView.setOnClickListener(a -> {
            inputListener.sendInput(BottomSheetCode.SplitScreen);
            dismiss();
        });

        splitScreen_imageView.setOnLongClickListener(a -> {
            inputListener.sendInput(BottomSheetCode.RemoveSplitScreen);
            dismiss();
            return false;
        });

        return v;
    }

    public interface OnInputListener {
        void sendInput(BottomSheetCode code);
    }
}
