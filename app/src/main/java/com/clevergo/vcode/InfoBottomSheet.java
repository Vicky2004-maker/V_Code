package com.clevergo.vcode;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.clevergo.vcode.editorfiles.BottomSheetCode;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

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

        if(Helper.isFullScreen(requireActivity())) {
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

        return v;
    }

    public interface OnInputListener {
        void sendInput(BottomSheetCode code);
    }
}
