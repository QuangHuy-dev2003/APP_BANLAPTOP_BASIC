package com.example.quanlycuahanglaptop.util;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quanlycuahanglaptop.R;

public class CustomToast {

    private static void showInternal(Context context, String message, int layoutRes) {
        if (!(context instanceof Activity)) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            return;
        }
        Activity activity = (Activity) context;
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(layoutRes, null);

        TextView tv = layout.findViewById(R.id.tvToast);
        tv.setText(message);

        Toast toast = new Toast(context);
        toast.setView(layout);
        toast.setDuration(Toast.LENGTH_LONG);

        // Center horizontally, show near top with dp offset for consistency across densities
        int yOffsetPx = (int) (activity.getResources().getDisplayMetrics().density * 72); // 72dp
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, yOffsetPx);
        toast.show();
    }

    public static void showSuccess(Context context, String message) {
        showInternal(context, message, R.layout.view_toast_success);
    }

    public static void showError(Context context, String message) {
        showInternal(context, message, R.layout.view_toast_error);
    }

    public static void showWarning(Context context, String message) {
        showInternal(context, message, R.layout.view_toast_warning);
    }
}


