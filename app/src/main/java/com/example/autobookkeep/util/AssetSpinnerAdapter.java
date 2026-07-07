package com.example.autobookkeep.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.autobookkeep.database.AssetAccount;

import java.text.DecimalFormat;

public class AssetSpinnerAdapter extends ArrayAdapter<AssetAccount> {

    private static final int MAX_DROPDOWN_HEIGHT_DP = 260;
    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

    public AssetSpinnerAdapter(@NonNull Context context) {
        super(context, android.R.layout.simple_spinner_item);
    }

    public static void limitDropDownHeight(Spinner spinner) {
        int maxHeightPx = (int) (MAX_DROPDOWN_HEIGHT_DP * spinner.getContext().getResources().getDisplayMetrics().density);
        spinner.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                showCustomDropdown(spinner, maxHeightPx);
                return true;
            }
            return true;
        });
    }

    private static void showCustomDropdown(Spinner spinner, int maxHeightPx) {
        android.widget.ListPopupWindow popupWindow = new android.widget.ListPopupWindow(spinner.getContext());
        popupWindow.setAnchorView(spinner);
        popupWindow.setAdapter((android.widget.ListAdapter) spinner.getAdapter());
        popupWindow.setWidth(spinner.getWidth());
        int itemCount = spinner.getAdapter().getCount();
        if (itemCount <= 6) {
            popupWindow.setHeight(android.widget.ListPopupWindow.WRAP_CONTENT);
        } else {
            popupWindow.setHeight(maxHeightPx);
        }
        popupWindow.setModal(true);
        popupWindow.setOnItemClickListener((parent, view, position, id) -> {
            spinner.setSelection(position);
            popupWindow.dismiss();
        });
        popupWindow.show();
        if (popupWindow.getListView() != null) {
            popupWindow.getListView().setVerticalScrollBarEnabled(false);
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView tv = (TextView) super.getView(position, convertView, parent);
        AssetAccount asset = getItem(position);
        if (asset != null) {
            tv.setText(asset.name + " (" + decimalFormat.format(asset.amount) + ")");
        }
        return tv;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
        AssetAccount asset = getItem(position);
        if (asset != null) {
            tv.setText(asset.name + " (" + decimalFormat.format(asset.amount) + ")");
        }
        return tv;
    }
}