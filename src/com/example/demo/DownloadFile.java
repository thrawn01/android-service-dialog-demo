package com.example.demo;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Class to handle file download and the progress dialog
 */
public class DownloadFile extends AsyncTask<String, Integer, String>
{
	private static volatile ProgressDialog mProgressDialog = null;
	private final OnCompleteInterface mCallBack;
	private Context mContext;

	/**
	 * Manages the progress bar
	 *
	 * Always call this constructor in the onCreate() of your Activity, it
	 * will re-display the progress dialog on orientation change
	 */
	public DownloadFile(Context context, OnCompleteInterface callBack)
	{
		mContext = context;
		mCallBack = callBack;

		// If progress dialog is showing, this means the async thread is still running
		if ( mProgressDialog != null ) {
			// Run onPreExecute() to setup the progress dialog again
			onPreExecute();
		}
	}

	/**
	 * This code runs in a thread separate from the UI, and preforms the actual download
	 *
	 * Returns a string if there was an error
	 */
	@Override
	protected String doInBackground(String... sUrl)
	{
		try {
			URLConnection connection = new URL( sUrl[0] ).openConnection();
			connection.connect();
			int fileLength = connection.getContentLength();

			InputStream input = connection.getInputStream();
			byte[] buffer = new byte[4096];
			int n, total = 0;

			FileOutputStream output = mContext.openFileOutput( "localFile.txt", Context.MODE_PRIVATE );
			while ( (n = input.read( buffer )) != -1 ) {
				publishProgress( (int) ((total += n) * 100 / fileLength) );
				output.write( buffer, 0, n );
			}
			output.close();

		} catch ( Exception e ) {
			return e.getLocalizedMessage();
		}
		return null;
	}

	/**
	 * This code runs in the UI thread and displays the Progress dialog
	 */
	protected void onPreExecute()
	{
		mProgressDialog = new ProgressDialog( mContext );
		mProgressDialog.setCancelable( false );
		mProgressDialog.setMessage( mContext.getString( R.string.retrieving_data ) );
		mProgressDialog.setIndeterminate( false );
		mProgressDialog.setMax( 100 );
		mProgressDialog.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
		mProgressDialog.show();
	}

	/**
	 * This code runs in the UI thread and updates the download progress
	 */
	protected void onProgressUpdate(Integer... progress)
	{
		mProgressDialog.setProgress( progress[0] );
	}

	/**
	 * This code runs in the UI thread and is called when the download is complete
	 */
	protected void onPostExecute(String result)
	{
		if ( mProgressDialog != null ) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}

		// Notify the activity the download is complete
		if ( mCallBack != null )
			mCallBack.onComplete( 0, result );
	}

	/**
	 * Call this from your Activity's onPause() method
	 */
	public void dismiss()
	{
		if ( mProgressDialog != null ) {
			mProgressDialog.dismiss();
		}
	}
}
