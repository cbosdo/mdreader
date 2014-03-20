/*
 * This software is provided under a Creative Commons Attribution-NonCommercial-
 * ShareAlike 3.0 Unported license.
 *
 * You are free to Share and Remix this software, as long as you attribute the
 * original owner, and release it under the same license. You may not use this
 * work for commercial purposes.
 *
 * Full license available at: http://creativecommons.org/licenses/by-nc-sa/3.0/legalcode
 *
 * Copyright (c) 2013 Nathan Paul
 */
package com.npaul.mdreader.adapters;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.npaul.mdreader.R;


/**
 * @author Nathan Paul
 *
 */
public class FileListAdapter extends ArrayAdapter<File> {

    private Activity mContext;
    private List<File> files;
    private FileItemHolder fih;

    /**
     * @param context
     * @param fileBrowserItem
     * @param files
     */
    public FileListAdapter(Activity context, int fileBrowserItem, List<File> files) {
        super(context, fileBrowserItem, files);

        mContext = context;
        this.files = files;

    }

    private static class FileItemHolder {
        TextView name;
        TextView detail;
        ImageView image;
        CheckBox checkbox;
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        ListView listview = ((ListView)parent);
        boolean showCheckboxes = listview.getChoiceMode() != AbsListView.CHOICE_MODE_NONE;

        View v = convertView;
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
            v = mInflater.inflate(R.layout.file_browser_item, null);

            fih = new FileItemHolder();
            fih.checkbox = (CheckBox) v.findViewById(R.id.file_checkbox);
            fih.name = (TextView) v.findViewById(R.id.file_name);
            fih.detail = (TextView) v.findViewById(R.id.file_detail);
            fih.image = (ImageView) v.findViewById(R.id.icon_listitem);
            v.setTag(fih);
        } else {
            fih = (FileItemHolder) v.getTag();
        }

        fih.checkbox.setVisibility(showCheckboxes ? View.VISIBLE : View.INVISIBLE);
        File fli = files.get(pos);

        if (fli != null){
            fih.name.setText(fli.getName());
            if (fli.isDirectory()){
                fih.detail.setText(R.string.directory);
                fih.detail.setTextColor(Color.GRAY);
                fih.image.setImageResource(R.drawable.folder);
            } else {
                long lastModified = fli.lastModified();
                String date = DateFormat.getDateFormat(getContext()).format(lastModified);
                String time = DateFormat.getTimeFormat(getContext()).format(lastModified);
                String dateTime = date + " " + time;
                fih.detail.setText(String.format(getContext().getString(R.string.last_modified),
                                                 dateTime));
                fih.detail.setTextColor(Color.BLACK);
                fih.image.setImageResource(R.drawable.file);
            }
        }

        return v;
    }

}
