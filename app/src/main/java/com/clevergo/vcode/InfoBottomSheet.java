package com.clevergo.vcode;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.info_bottom_sheet, container, true);

        ImageView compileCode = v.findViewById(R.id.compile_imageView);
        compileCode.setOnClickListener(a -> {
            inputListener.sendInput(BottomSheetCode.Compile);
            dismiss();
        });

        return v;
    }

    public interface OnInputListener {
        void sendInput(BottomSheetCode code);
    }
}
