package com.musclefit.Helper;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;

public class CollapsibleTextView extends AppCompatTextView implements View.OnClickListener {

    private static final int MAX_LINES_COLLAPSED = 2; // Adjust as needed
    private static final String MORE_TEXT = "more";
    private static final String LESS_TEXT = "less";
    private boolean isExpanded;

    public CollapsibleTextView(Context context) {
        super(context);
        init();
    }

    public CollapsibleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CollapsibleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnClickListener(this);
        toggleExpandedState(false);
    }

    @Override
    public void onClick(View v) {
        toggleExpandedState(!isExpanded);
    }

    public void toggleExpandedState(boolean expanded) {
        isExpanded = expanded;
        if (expanded) {
            setMaxLines(Integer.MAX_VALUE);
            setEllipsize(null);
            setText(createCollapsibleText(LESS_TEXT));
        } else {
            setMaxLines(MAX_LINES_COLLAPSED);
            setEllipsize(TextUtils.TruncateAt.END);
            setText(createCollapsibleText(MORE_TEXT));
        }
    }

    private SpannableStringBuilder createCollapsibleText(String label) {
        CharSequence originalText = getText();
        if (getLayout() != null && getLayout().getLineCount() > MAX_LINES_COLLAPSED) {
            int start = getLayout().getLineStart(MAX_LINES_COLLAPSED);
            int end = getLayout().getLineEnd(MAX_LINES_COLLAPSED);

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(originalText);
            spannableStringBuilder.replace(start, end, label);

            spannableStringBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    toggleExpandedState(!isExpanded);
                }
            }, start, start + label.length(), 0);

            return spannableStringBuilder;
        }
        return new SpannableStringBuilder(originalText);
    }
}
