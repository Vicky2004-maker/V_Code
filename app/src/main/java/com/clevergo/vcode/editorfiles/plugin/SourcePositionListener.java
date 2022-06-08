package com.clevergo.vcode.editorfiles.plugin;

import android.text.Layout;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.EditText;

public class SourcePositionListener {

    private final EditText editText;
    private OnPositionChanged onPositionChanged;
    private final View.AccessibilityDelegate viewAccessibility = new View.AccessibilityDelegate() {

        @Override
        public void sendAccessibilityEvent(View host, int eventType) {
            super.sendAccessibilityEvent(host, eventType);
            if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED && onPositionChanged != null) {
                int selectionStart = editText.getSelectionStart();
                Layout layout = editText.getLayout();
                if (layout == null) return;
                int line = editText.getLayout().getLineForOffset(selectionStart);
                int column = selectionStart - editText.getLayout().getLineStart(line);
                onPositionChanged.onPositionChange(line + 1, column + 1);
            }
        }
    };

    public SourcePositionListener(EditText editText) {
        this.editText = editText;
        editText.setAccessibilityDelegate(viewAccessibility);
    }

    public void setOnPositionChanged(OnPositionChanged listener) {
        onPositionChanged = listener;
    }

    @FunctionalInterface
    public interface OnPositionChanged {
        void onPositionChange(int line, int column);
    }
}
