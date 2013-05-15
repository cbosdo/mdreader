/**
 * FileListAdapter.java
 */
package com.npaul.mdreader.adapters;

import java.io.File;
import java.util.List;

import com.npaul.mdreader.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


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

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		if(files == null)
			return 0;
		else
			return files.size();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public File getItem(int item) {
		if(files == null)
			return null;
		else
			return files.get(item);
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private static class FileItemHolder {
		TextView name;
		TextView detail;
		ImageView image;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		
		View v = convertView;
        if (convertView == null) {
        	LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        	v = mInflater.inflate(R.layout.file_browser_item, null);
            
            fih = new FileItemHolder();
            fih.name = (TextView) v.findViewById(R.id.file_name);
            fih.detail = (TextView) v.findViewById(R.id.file_detail);
            fih.image = (ImageView) v.findViewById(R.id.icon_listitem);
            v.setTag(fih);
        } else fih = (FileItemHolder) v.getTag();
        
        File fli = files.get(pos);
                 
        if (fli != null){
        		fih.name.setText(fli.getName());
        	if (fli.isDirectory()){
        		fih.detail.setText("Directory");
        		fih.detail.setTextColor(Color.GRAY);
        		fih.image.setImageResource(R.drawable.folder);
        	} else {
        		long lastModified = fli.lastModified();
        		fih.detail.setText("Last Modified: " + DateFormat.getDateFormat(getContext()).format(lastModified) +
        				" " + DateFormat.getTimeFormat(getContext()).format(lastModified));
        		fih.detail.setTextColor(Color.BLACK);
        		fih.image.setImageResource(R.drawable.file);
        	}
        }

        return v;
		

	}

}
