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
 * Copyright (c) 2014 Cedric Bosdonnat <cedric@bosdonnat.fr>
 */
package com.npaul.mdreader.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
    private String filename;
    private boolean textChanged = false;

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
                url.setHint("(http://acme.foobar.com/)");
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
            setTitle("Dowloaded Content");
        } else if (scheme.equals("file")) {
            Uri uri = intent.getData();
            file = new File(uri.getPath());
            this.filename = file.getName();
            setTitle(filename);
        }
        readInData(intent);

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
            break;

        case R.id.menu_done:
            Intent data = new Intent();
            data.setData(getIntent().getData());
            data.putExtra("text", t.getText().toString());
            setResult(RESULT_OK, data);
            finish();
            break;

        default:
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * Reads in the data from the intent and displays it in the text area
     *
     * @param intent
     *            The intent passed to the activity
     */
    private void readInData(Intent intent) {
        String result = new String();
        if (intent.getExtras() != null && intent.getExtras().containsKey("text")) {
            result = intent.getExtras().getString("text");
        } else {
            try {
                InputStream in = getContentResolver().openInputStream(
                        intent.getData());
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(in));
                String line = reader.readLine();
                while (line != null) {
                    result += "\n" + line;
                    line = reader.readLine();
                }
            } catch (FileNotFoundException e) {

            } catch (IOException e) {

            }
        }
        t.setText(result);
    }
}
