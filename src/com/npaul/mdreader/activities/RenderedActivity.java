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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.markdownj.MarkdownProcessor;

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
    private class Renderer extends AsyncTask<String, Integer, CharSequence> {
        private MarkdownProcessor mdp;

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected CharSequence doInBackground(String... params) {
            String data = params[0];
            mdp = new MarkdownProcessor();
            // long startTime = System.currentTimeMillis();
            CharSequence out = mdp.markdown(data);
            // long endTime = System.currentTimeMillis();
            // System.out.println("took: " + (endTime-startTime) + "ms");
            src = out;
            return out;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
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
            w.getSettings().setBuiltInZoomControls(true);
            try {
                // get the file path
                String dir = getIntent().getData().getPath().toString();
                int i = dir.length() - 1;
                while (dir.charAt(i) != '/') {
                    i--;
                }
                dir = dir.substring(0, i);
                // load with images if its a file on the local system
                w.loadDataWithBaseURL("file://" + dir + "/", (String) result,
                        "text/html", "utf-8", null);
            } catch (NullPointerException e) {
                // load without baseURL (pictures won't work)
                w.loadData((String) result, "text/html", "utf-8");
                if (result.toString().contains("<img ")) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(context);
                    adb.setTitle("Images can't be read");
                    adb.setMessage(
                            "Save a local copy of the file to your device then try again")
                            .setCancelable(false)
                            .setIcon(R.drawable.file)
                            .setPositiveButton("OK",
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
            mdp = null; // delete the large amount of memory being used, next
            // time garbage collection comes along
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
        text = readInData(intent);
        new Renderer().execute(text);

        // Extract as many data as possible from the intent
        String scheme = intent.getScheme();
        setTitle("Untitled");
        if (scheme.equals("content")) {
            setTitle("Dowloaded Content");
        } else if (scheme.equals("file")) {
            Uri uri = intent.getData();
            file = new File(uri.getPath());
            this.filename = file.getName();
            setTitle(filename);
        }

        setContentView(R.layout.activity_rendered);
        w = (WebView) findViewById(R.id.webView);
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

        case R.id.menu_saveashtml:
            Uri data = getIntent().getData();
            String filename = data.getPath();
            int i = filename.length() - 1;
            while (filename.charAt(i) != '.') {
                i--;
            }
            filename = filename.substring(0, i);
            File file = new File(filename + ".html");
            try {
                OutputStream out = new FileOutputStream(file);
                if (!file.exists()) {
                    file.createNewFile();
                }
                byte[] content = src.toString().getBytes();
                out.write(content);
                out.flush();
                out.close();
                Toast.makeText(context, "Saved as: " + file.getPath(),
                        Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                Toast.makeText(context,
                        "Couldn't save the file for some reason",
                        Toast.LENGTH_LONG).show();
            } catch (IOException e) {

            }
            break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_CODE && resultCode == RESULT_OK) {
            text = readInData(data);
            new Renderer().execute(text);
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
     * Returns contents, as CharSequence, otherwise will return
     * <code>null</code>
     *
     * @param intent
     * @return
     */
    private String readInData(Intent intent) {
        try {
            // for the method when coming from the editActivity
            return intent.getExtras().get("text").toString();
        } catch (NullPointerException n) {
            try {
                InputStream in = getContentResolver().openInputStream(
                        intent.getData());
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
        }

        return null;
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
            outBuffer.append(text);
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

    private void editText() {
        Intent intent = new Intent(RenderedActivity.this, EditActivity.class);
        intent.setData(Uri.fromFile(file));
        intent.putExtra("text", text);
        RenderedActivity.this.startActivityForResult(intent, EDIT_CODE);
    }
}
