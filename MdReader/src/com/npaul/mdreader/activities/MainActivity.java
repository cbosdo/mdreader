package com.npaul.mdreader.activities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.npaul.mdreader.R;
import com.npaul.mdreader.adapters.FileListAdapter;
import com.npaul.mdreader.util.FileStringComparator;

/**
 * An activity where the user can choose a file to edit using a file browser
 * 
 * @author Nathan Paul
 * @version 1.1
 */
public class MainActivity extends Activity {

	final Context context = this;
	private List<File> files;
	private ListView lv;

	private String currentDirectory;
	private Stack<String> dirHistory = new Stack<String>();
	private int index;

	/**
	 * Changes the directory to another and updates the file list as well
	 * 
	 * @param directory
	 *            the new directory to display
	 * @param isPrevious
	 *            whether the directory is a previous directory, and should not
	 *            be added to the dirHistory stack
	 */
	private void changeDirectory(String directory, boolean isPrevious) {
		if (!isPrevious) {
			dirHistory.push(currentDirectory);
		}
		currentDirectory = directory;
		int i = currentDirectory
				.lastIndexOf("/", currentDirectory.length() - 1);
		setTitle("..."
				+ currentDirectory.subSequence(i, currentDirectory.length()));
		if (getFiles(directory) == null) {
			findViewById(R.id.nofiles).setVisibility(View.VISIBLE);
			findViewById(R.id.list).setVisibility(View.INVISIBLE);
		} else {
			findViewById(R.id.nofiles).setVisibility(View.INVISIBLE);
			findViewById(R.id.list).setVisibility(View.VISIBLE);
			files = getFiles(currentDirectory);
			initFileListView();
		}
	}

	/**
	 * Gets the files in a given directory and returns them in an arraylist
	 * 
	 * @param DirectoryPath
	 *            the full path to the directory
	 * @return the file array, null if the directory is empty
	 * @throws NullPointerException
	 */
	private ArrayList<File> getFiles(String DirectoryPath)
			throws NullPointerException {
		ArrayList<File> fileArray = new ArrayList<File>();
		File f = new File(DirectoryPath);

		f.mkdirs();
		File[] files = f.listFiles();
		if ((files.length == 0) || (files == null)) {

			return null;
		} else {
			for (int i = 0; i < files.length; i++) {
				if (!files[i].isHidden()) {
					fileArray.add(files[i]);
				}
			}
		}

		Collections.sort(fileArray, new FileStringComparator());
		return fileArray;
	}

	/**
	 * Updates the file list view in the UI and attaches a listener to each one
	 */
	private void initFileListView() {
		lv = (ListView) findViewById(R.id.list);
		lv.setAdapter(new FileListAdapter(this, R.layout.file_browser_item,
				files));

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {

				if (files.get(position).isDirectory()) {
					changeDirectory(files.get(position).getPath(), false);
				} else {
					openFile(files.get(position));
				}

			}

		});

		lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v,
					final int position, long id) {
				AlertDialog.Builder builder = new AlertDialog.Builder(v
						.getContext());
				builder.setTitle(R.string.delete_title);
				builder.setMessage("Are you sure you want to delete "
						+ files.get(position).getName() + "?");
				builder.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								files.get(position).delete();
								refreshFileList();
							}
						});
				builder.setNegativeButton("No",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// do nothing
							}
						});

				builder.show();

				return true;
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// set the current directory
		currentDirectory = Environment.getExternalStorageDirectory().getPath();

		// make a directory named mdreader if it doesn't already exist
		File mdReaderDir = new File(Environment.getExternalStorageDirectory()
				+ "/mdreader/");
		if (!mdReaderDir.exists()) {
			mdReaderDir.mkdir();
		}

		// initialise the stack with the root directory then the mdreader
		// directory
		try {
			changeDirectory(currentDirectory, false);
			changeDirectory(mdReaderDir.toString(), false);
		} catch (NullPointerException e) {
			Toast.makeText(this, "There was an error", Toast.LENGTH_LONG)
					.show();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!dirHistory.isEmpty()) {
				changeDirectory(dirHistory.pop(), true);
				if (dirHistory.size() == 0) {
					Toast.makeText(context, "Press \"back\" once more to exit",
							Toast.LENGTH_LONG).show();
					return true;
				}
				return true; // say to android that the program has dealt with
								// the event
			}

		}
		return super.onKeyDown(keyCode, event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// get the button that was pressed
		switch (item.getItemId()) {

		// about
		case R.id.menu_about:
			WebView wv = new WebView(context);
			InputStream is;
			try {
				is = getAssets().open("about.html");
				int size = is.available();

				byte[] buffer = new byte[size];
				is.read(buffer);
				is.close();
				String text = new String(buffer);

				wv.loadData(text, "text/html", null);
			} catch (IOException e1) {
				// should never get here though
				Toast.makeText(context, "Something went wrong",
						Toast.LENGTH_LONG).show();
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.menu_about);
			builder.setView(wv);
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					});
			builder.show();
			return true;

			// refresh
		case R.id.menu_refresh:
			refreshFileList();
			return true;

			// new
		case R.id.menu_new:
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle("Enter filename:");
			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS); // switch off autosuggest for this field
			input.setText(".md");
			adb.setView(input);
			adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					String value = input.getText().toString();
					File newFile = new File(currentDirectory + "/" + value);
					try {
						if (!newFile.createNewFile()) {
							Toast.makeText(context, "This file already exists",
									Toast.LENGTH_LONG).show();
						}
					} catch (IOException e) {
						Toast.makeText(context, "Something went wrong",
								Toast.LENGTH_LONG).show();
					}
					openFile(newFile);
				}
			});
			adb.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// do nothing

						}
					});
			// workaround to show keyboard
			AlertDialog ad = adb.create();
			ad.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			ad.show();
			return true;

		case R.id.menu_newfolder:
			// create editText
			final EditText et = new EditText(context);
			et.setHint("Folder name");

			AlertDialog.Builder adb2 = new AlertDialog.Builder(context);

			adb2.setTitle("Enter folder name")
					.setView(et)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									File folder = new File(currentDirectory
											+ "/" + et.getText());
									if (!folder.exists()) {
										folder.mkdir();
										refreshFileList();
									} else {
										Toast.makeText(context,
												"This folder already exists",
												Toast.LENGTH_LONG).show();
									}
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// do nothing

								}
							}).create();

			// workaround for keyboard
			AlertDialog ad2 = adb2.create();
			ad2.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			ad2.show();

			return true;

			// capture if not defined yet - mainly used for development, the
			// user should never see this message
		default:
			Toast.makeText(context,
					"This hasn't been defined yet. ID: " + item.getTitle(),
					Toast.LENGTH_LONG).show();
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		refreshFileList();
		super.onResume();
	}

	/**
	 * Opens a given file in the edit activity
	 * 
	 * @param file
	 *            the file to be opened
	 */
	private void openFile(File file) {
		Intent intent = new Intent(MainActivity.this, EditActivity.class);
		intent.setData(Uri.fromFile(file));
		MainActivity.this.startActivity(intent);

	}

	/**
	 * Refreshes the file list and keeps the position in the file list to
	 * prevent the user from scrolling to the position they were in<br />
	 * <br />
	 * Called in <code>onResume()</code> to pick up any external changes in the
	 * filesystem
	 */
	private void refreshFileList() {
		// keep the list view in the same Y position when refreshing
		index = lv.getFirstVisiblePosition();

		changeDirectory(currentDirectory, true);

		// return to that position
		if (lv.getCount() > index) {
			lv.setSelectionFromTop(index, 0);
		} else {
			lv.setSelectionFromTop(0, 0);
		}
	}
}
