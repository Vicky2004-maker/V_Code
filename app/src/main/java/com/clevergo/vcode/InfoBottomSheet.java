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
        ImageView splitScreen_ImageView = v.findViewById(R.id.splitScreen_imageView);
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
            activeCodeView_Selector.setListSelection(CodeViewActivity.activeFilePosition);
            activeCodeView_Selector.setOnItemClickListener((parent, view, position, id) -> {
                CodeViewActivity.activeFilePosition = position;
                CodeViewActivity.activeSplitScreenFileName = myAdapter.getItem(position);
                inputListener.sendInput(BottomSheetCode.SetActiveCodeViewFile);
                //dismiss();
            });
        }

        if (Helper.isFullScreen) {
            fullScreen_imageView.setImageResource(R.drawable.ic_fullscreen_exit);
        } else {
            fullScreen_imageView.setImageResource(R.drawable.ic_fullscreen_24);
        }

        if(CodeViewActivity.isScreenSplit) {
            splitScreen_ImageView.setImageResource(R.drawable.ic_close_split_screen);
        } else {
            splitScreen_ImageView.setImageResource(R.drawable.ic_split_screen_01);
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

        v.findViewById(R.id.deleteFile_imageView).setOnClickListener(a -> {
            inputListener.sendInput(BottomSheetCode.DeleteFile);
            dismiss();
        });

        v.findViewById(R.id.edit_imageView).setOnClickListener(a -> {
            inputListener.sendInput(BottomSheetCode.Edit);
            dismiss();
        });

        v.findViewById(R.id.compile_imageView).setOnClickListener(a -> {
            inputListener.sendInput(BottomSheetCode.Compile);
            dismiss();
        });

        v.findViewById(R.id.convertPDF_ImageView).setOnClickListener(a -> {
            inputListener.sendInput(BottomSheetCode.ConvertToPDF);
            dismiss();
        });

        splitScreen_ImageView.setOnClickListener(a -> {
            if (CodeViewActivity.isScreenSplit) {
                inputListener.sendInput(BottomSheetCode.RemoveSplitScreen);
            } else {
                inputListener.sendInput(BottomSheetCode.SplitScreen);
            }
            dismiss();
        });

        return v;
    }

    public interface OnInputListener {
        void sendInput(BottomSheetCode code);
    }
}
