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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import com.npaul.mdreader.R;
import com.npaul.mdreader.util.FoldingFilter;
import com.npaul.mdreader.util.Formatter;
import com.npaul.mdreader.util.StyleFilter;

/**
 * An activity that renders markdown on screen using MarkdownJ -
 * http://www.markdownj.org/
 *
 * @author Nathan Paul
 * @version 1.1
 */
public class RenderedActivity extends Activity {

    /**
     * The renderer renders the markdown asynchronously to the main thread
     *
     * @author Nathan Paul
     */
    private class Renderer extends AsyncTask<Intent, Integer, CharSequence> {

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected CharSequence doInBackground(Intent... params) {
            text = readInData(params[0]);
            Formatter formatter = new Formatter();
            formatter.addFilter(new StyleFilter());
            formatter.addFilter(new FoldingFilter());

            src = formatter.format(text);
            return src;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @SuppressLint("SetJavaScriptEnabled")
        @Override
        protected void onPostExecute(CharSequence result) {
            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);

            // Try to set a meaningful title if we have no filename
            if (filename == null) {
                String title = getDocTitle(result);
                if (title != null) {
                    setTitle(title);
                }
            }
            WebSettings ws = w.getSettings();
            ws.setTextZoom(70);
            ws.setMinimumFontSize(10);
            ws.setJavaScriptEnabled(true);
            ws.setBuiltInZoomControls(true);

            String resultString = "";
            if (result != null)
                resultString = result.toString();

            try {
                // get the file path
                String dir = getIntent().getData().getPath().toString();
                int i = dir.length() - 1;
                while (dir.charAt(i) != '/') {
                    i--;
                }
                dir = dir.substring(0, i);
                // load with images if its a file on the local system
                w.loadDataWithBaseURL("file://" + dir + "/", resultString,
                        "text/html", "utf-8", null);
            } catch (NullPointerException e) {
                // load without baseURL (pictures won't work)
                w.loadData(resultString, "text/html", "utf-8");
                if (resultString.contains("<img ")) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(context);
                    adb.setTitle(R.string.images_read_error_title);
                    adb.setMessage(
                            R.string.images_read_error_message)
                            .setCancelable(false)
                            .setIcon(R.drawable.file)
                            .setPositiveButton(R.string.ok,
                                    new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(
                                        DialogInterface dialog,
                                        int which) {
                                    dialog.cancel();

                                }
                            }).show();
                }
            }
        }

        private String getDocTitle(CharSequence result) {

            Matcher matcher = Pattern.compile("<h1[^<]*>([^<]+)</h1>").matcher(result);
            if (matcher.find()) {
                String title = matcher.group(1).trim();
                if (!title.isEmpty())
                    return title;
            }

            return null;
        }
    }

    private static final int EDIT_CODE = 1;

    final Context context = this;
    private WebView w;

    private CharSequence src;
    private File file;
    private String filename;
    String text = new String();
    private boolean textChanged = false;
    private boolean exitOnSave;

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Start rendering
        Intent intent = getIntent();
        new Renderer().execute(intent);

        // Extract as many data as possible from the intent
        String scheme = intent.getScheme();
        setTitle(R.string.untitled_title);
        if (scheme.equals("content")) {
            setTitle(R.string.dowloaded_content_title);
        } else if (scheme.equals("file")) {
            Uri uri = intent.getData();
            file = new File(uri.getPath());
            this.filename = file.getName();
            setTitle(filename);
        }

        setContentView(R.layout.activity_rendered);
        w = (WebView) findViewById(R.id.webView);

        // Don't show the application icon
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_rendered, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean result = super.onPrepareOptionsMenu(menu);

        MenuItem item = menu.findItem(R.id.menu_save);
        if (item != null)
            item.setVisible(textChanged);

        return result;
    }
    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        case R.id.menu_edit:
            editText();
            break;

        case R.id.menu_save:
            if (file != null) {
                saveFile(file);
            } else {
                saveAsCopy();
            }
            break;

        case R.id.menu_saveAs:
            saveAsCopy();
            break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_CODE && resultCode == RESULT_OK) {
            textChanged = true;
            invalidateOptionsMenu();

            new Renderer().execute(data);
        }
    }

    /**
     * Overrides the finish function to ensure that the changes in edit text
     * have been saved
     */
    @Override
    public void finish() {
        if (textChanged == true) {
            AlertDialog.Builder adb = new AlertDialog.Builder(context);
            adb.setTitle(R.string.save_changes_title);
            adb.setMessage(R.string.save_changes_message)
               .setNegativeButton(R.string.no,
                    new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog,
                        int which) {
                    textChanged = false; // to stop endless loop
                    finish();
                }
            })
            .setPositiveButton(R.string.yes,
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
     * Returns contents, as CharSequence, otherwise will return
     * <code>null</code>
     *
     * @param intent
     * @return
     */
    private String readInData(Intent intent) {
        if (intent.getExtras() != null && intent.getExtras().getString("text") != null) {
            // for the method when coming from the editActivity
            return intent.getExtras().getString("text");
        }

        try {
            InputStream in = null;
            if (intent.getData().getScheme().equals("file") ||
                    intent.getData().getScheme().equals("content")) {
                in = getContentResolver().openInputStream(
                        intent.getData());
            } else if (intent.getData().getScheme().startsWith("http")) {
                URL url = new URL(intent.getData().toString());
                URLConnection connection = url.openConnection();

                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();

                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    in = httpConnection.getInputStream();
                }
            }

            if (in == null)
                return null;

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in));
            String result = new String();
            String line = reader.readLine();
            while (line != null) {
                result += line + "\n";
                line = reader.readLine();
            }
            return result;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Shows a dialog box to enter a new name for a file
     */
    private void saveAsCopy() {

        AlertDialog.Builder newBuilder = new AlertDialog.Builder(this);

        newBuilder.setTitle(R.string.enter_filename);
        final EditText input = new EditText(this);

        if (file == null) {
            input.setText(Environment.getExternalStorageDirectory().getPath()
                    + "/mdreader/" + ".md");
        } else {
            input.setText(file.getAbsolutePath());
        }
        newBuilder.setView(input).setPositiveButton(R.string.ok,
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
        newBuilder.setNegativeButton(R.string.cancel,
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
            outBuffer.append(text);
            outBuffer.close();
            out.close();
        } catch (IOException e) {
            Toast.makeText(context,
                    R.string.saved_as_info,
                    Toast.LENGTH_LONG).show();
        } finally {
            this.filename = fileToWrite.getName();
            setTitle(filename);
            textChanged = false;
            Toast.makeText(context,
                    String.format(getString(R.string.saved_as_info),
                                  fileToWrite.getAbsolutePath()),
                    Toast.LENGTH_LONG).show();
            if (exitOnSave) {
                finish();
            }
        }
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
        fe.setTitle(R.string.file_already_exists)
        .setMessage(R.string.overwrite)
        .setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog,
                    int which) {
                saveFile(newFile);

            }
        })
        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveAsCopy();
            }
        }).show();
    }

    private void editText() {
        Intent intent = new Intent(RenderedActivity.this, EditActivity.class);
        intent.setData(getIntent().getData());
        intent.putExtra("text", text);
        RenderedActivity.this.startActivityForResult(intent, EDIT_CODE);
    }
}
