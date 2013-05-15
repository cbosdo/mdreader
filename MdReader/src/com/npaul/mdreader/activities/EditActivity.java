package com.npaul.mdreader.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.npaul.mdreader.R;

/**
 * An activity to help editing Markdown documents
 * 
 * @author Nathan Paul
 * @version 1.1
 * 
 */
public class EditActivity extends Activity {

	final Context context = this;
	private EditText t;
	private File file;
	private boolean textChanged = false;
	private String filename;
	private boolean exitOnSave;

	/**
	 * Used to surround items in the edit field with the parameters set
	 * 
	 * @param text
	 *            The characters to surround the text with
	 */
	private void editTextHelper(String text) {
		int start = t.getSelectionStart();
		int end = t.getSelectionEnd();

		t.getText().insert(end, text);
		t.getText().insert(start, text);
		t.setSelection(end + text.length());
	}

	/**
	 * Displays a dialog saying the file exists and requests user choice on
	 * whether to replace
	 * 
	 * @param newFile
	 *            The file attempting to be written to
	 */
	private void fileExistsDialog(final File newFile) {
		AlertDialog.Builder fe = new AlertDialog.Builder(this);
		fe.setTitle("File already exists")
				.setMessage("Overwrite?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								saveFile(newFile);

							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						saveAsCopy();
					}
				}).show();
	}

	/**
	 * Overrides the finish function to ensure that the changes in edit text
	 * have been saved
	 */
	@Override
	public void finish() {
		if (textChanged == true) {
			AlertDialog.Builder adb = new AlertDialog.Builder(context);
			adb.setTitle("Save Changes?");
			adb.setMessage("All unsaved work will be lost")
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									textChanged = false; // to stop endless loop
									finish();
								}
							})
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									exitOnSave = true; // set this because all
														// UI is run
														// asynchronously
									saveAsCopy();
								}
							}).show();
		} else {
			super.finish();
		}
	}

	/**
	 * Used to help the initial setup of the UI. It adds <code>listener</code>s
	 * to all of the buttons in the text field bar
	 */
	private void initButtons() {
		final Button btn_tab = (Button) findViewById(R.id.btn_tab);
		final Button btn_bold = (Button) findViewById(R.id.btn_bold);
		final Button btn_italic = (Button) findViewById(R.id.btn_italic);
		final Button btn_url = (Button) findViewById(R.id.btn_url);
		final Button btn_code = (Button) findViewById(R.id.btn_code);
		final Button btn_hash = (Button) findViewById(R.id.btn_hash);

		btn_tab.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int start = t.getSelectionStart();
				int end = t.getSelectionEnd();
				t.getText().replace(start, start, "\t");
				t.setSelection(end + 1);
			}
		});

		btn_bold.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				editTextHelper("**");
			}
		});

		btn_italic.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				editTextHelper("_");
			}
		});

		btn_hash.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				t.getText().insert(t.getSelectionStart(), "#");
			}
		});

		btn_url.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// set up the text fields for entry
				final EditText text = new EditText(context);
				text.setHint("[Text]");
				final EditText url = new EditText(context);
				url.setHint("(http://www.google.com/)");
				url.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS); // switch
																			// off
																			// auto-suggest
																			// for
																			// this
																			// field
				url.setOnFocusChangeListener(new OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						url.setText("http://www.");
						url.setSelection(url.getText().length() - 1);

					}

				});

				// build a view for this entry
				LinearLayout layout = new LinearLayout(context);
				layout.setOrientation(LinearLayout.VERTICAL);
				layout.addView(text);
				layout.addView(url);

				// get start and end selection points
				final int start = t.getSelectionStart();
				final int end = t.getSelectionEnd();

				// build the dialog
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Enter URL:");
				builder.setView(layout).setPositiveButton("OK",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								t.getText().replace(
										start,
										end,
										"[" + text.getText() + "]("
												+ url.getText() + ")");
								t.setSelection(start + 4 + text.length()
										+ url.length());
							}
						});

				// workaround to show keyboard
				AlertDialog ad = builder.create();
				ad.getWindow()
						.setSoftInputMode(
								WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				ad.show();

			}
		});

		btn_code.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				editTextHelper("`");
			}
		});

	}

	/**
	 * Initialises the text area by adding a <code>listener</code> for when the
	 * contents are modified
	 */
	private void initTextArea() {
		t.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				// do nothing
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// do nothing
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (textChanged == false) {
					textChanged = true;
					setTitle("*" + getTitle());
				}

			}

		});
	}

	/**
	 * Called by the Android system when the activity is opened by an intent
	 * 
	 * @param intent
	 *            The intent passed to the application
	 */
	public void newIntent(Intent intent) {
		setIntent(intent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_edit);
		t = (EditText) findViewById(R.id.editTextArea);

		Intent intent = getIntent();
		String scheme = intent.getScheme();
		if (scheme.equals("content")) {
			readInData(intent);
			setTitle("Dowloaded Content");
		} else if (scheme.equals("file")) {
			Uri uri = intent.getData();
			file = new File(uri.getPath());
			this.filename = file.getName();
			setTitle(filename);
			// prevents 'null' appearing in edit when creating a new file or
			// opening an empty file
			if (file.length() != 0) {
				readInData(intent);
			}
		}
		initTextArea();
		initButtons();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_edit, menu);
		return true;
	}

	/**
	 * Responds to a menu press
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);

			return true;

		case R.id.menu_save:
			if (file != null) {
				saveFile(file);
			} else {
				saveAsCopy();
			}
			return true;

		case R.id.menu_render:
			try {
				renderText(true);
			} catch (IOException e) {
				// should work
			} catch (NullPointerException e) {
				try {
					renderText(false);
				} catch (NullPointerException e1) {
					// something went horribly wrong
				} catch (IOException e1) {
					// something went horribly wrong
				}
			}
			return true;

		case R.id.menu_saveAs:
			saveAsCopy();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Reads in the data from the intent and displays it in the text area
	 * 
	 * @param intent
	 *            The intent passed to the activity
	 */
	private void readInData(Intent intent) {
		try {
			InputStream in = getContentResolver().openInputStream(
					intent.getData());
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			String result = reader.readLine() + "\n";
			String line;
			while ((line = reader.readLine()) != null) {
				result += "\n" + line;
			}
			t.setText(result);
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
	}

	private void renderText(boolean content) throws IOException,
			NullPointerException {
		CharSequence text = t.getText();

		Intent intent = new Intent(EditActivity.this, RenderedActivity.class);
		if (content) {
			intent.setData(Uri.parse(file.toURI().toString()));
		}
		intent.putExtra("text", text);
		EditActivity.this.startActivity(intent);
	}

	/**
	 * Shows a dialog box to enter a new name for a file
	 */
	private void saveAsCopy() {

		AlertDialog.Builder newBuilder = new AlertDialog.Builder(this);

		newBuilder.setTitle("Enter filename:");
		final EditText input = new EditText(this);

		if (file == null) {
			input.setText(Environment.getExternalStorageDirectory().getPath()
					+ "/mdreader/" + ".md");
		} else {
			input.setText(file.getAbsolutePath());
		}
		newBuilder.setView(input).setPositiveButton("OK",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String value = input.getText().toString();
						File newFile = new File(value);
						try {
							if (newFile.createNewFile()) {
								saveFile(newFile);
							} else {
								fileExistsDialog(newFile);
							}
						} catch (IOException e) {

						}
					}

				});
		newBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						exitOnSave = false;
						// otherwise this would cause a bug where the
						// editActivity closed when the user hit "Save" next
					}
				});

		newBuilder.show();

	}

	/**
	 * Saves a file to disk using the contents of the <code>EditText</code>
	 * 
	 * @param fileToWrite
	 *            The file where the data should be written
	 */
	private void saveFile(File fileToWrite) {
		try {
			FileOutputStream out = new FileOutputStream(fileToWrite);
			OutputStreamWriter outBuffer = new OutputStreamWriter(out);
			outBuffer.append(t.getText());
			outBuffer.close();
			out.close();
		} catch (IOException e) {
			Toast.makeText(context,
					"Couldn't save your file, try another location",
					Toast.LENGTH_LONG).show();
		} finally {
			this.filename = fileToWrite.getName();
			setTitle(filename);
			textChanged = false;
			Toast.makeText(context,
					"Saved as: " + fileToWrite.getAbsolutePath(),
					Toast.LENGTH_LONG).show();
			if (exitOnSave) {
				finish();
			}
		}

	}
}
