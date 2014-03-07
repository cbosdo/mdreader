package com.npaul.mdreader.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
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
	private class Renderer extends AsyncTask<Intent, Integer, CharSequence> {
		private MarkdownProcessor mdp;

		/*
		 * (non-Javadoc)
		 *
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected CharSequence doInBackground(Intent... params) {
			String data = readInData(params[0]);
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
			String title = getDocTitle(result);
			if (title != null) {
				setTitle(title);
			} else {
				setTitle("Rendered Text");
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

	final Context context = this;
	private WebView w;

	private CharSequence src;

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Intent intent = getIntent();

		new Renderer().execute(intent);

		super.onCreate(savedInstanceState);

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
		}
		return true;
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
				String result = reader.readLine() + "\n";
				String line;
				while ((line = reader.readLine()) != null) {
					result += "\n" + line;
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
}