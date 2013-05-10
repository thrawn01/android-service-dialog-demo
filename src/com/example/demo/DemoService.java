package com.example.demo;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import com.google.common.collect.ImmutableList;


public class DemoService extends IntentService
{
	public static final String SCAN = "com.example.demo.action.SCAN";
	public static final String SYNC = "com.example.demo.action.SYNC";
	public static final String BOTH = "com.example.demo.action.BOTH";

	public static final int ON_PROGRESS = 0x01;
	public static final int ON_COMPLETE = 0x02;
	public static final int ON_TASK_CHANGE = 0x03;

	private static final int NOTIFY_ID = 0xF0000000;

	private Message mLastMessage = null;
	private Handler mHandler;
	private NotificationManager mNotifyManager = null;
	private NotificationCompat.Builder mNotify;
	private String mCurrentTask = null;

	/**
	 * A constructor is required, and must call the super IntentService(String)
	 * constructor with a name for the worker thread.
	 */
	public DemoService()
	{
		super( "DemoService" );
	}

	/**
	 * Called when the service calls startService(), queues the task requested
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		onStart( intent, startId );
		return START_STICKY;
	}

	/**
	 * The IntentService calls this method from the default worker thread with
	 * the intent that started the service. When this method returns, IntentService
	 * stops the service, as appropriate.
	 */
	@Override
	protected void onHandleIntent(Intent intent)
	{
		if ( intent == null )
			return;

		// Prepare notifications
		setUpNotification();

		mCurrentTask = intent.getAction();
		if ( mCurrentTask.equals( SYNC ) ) {
			sync();
		}
		if ( mCurrentTask.equals( SCAN ) ) {
			scan();
		}
		if ( mCurrentTask.equals( BOTH ) ) {
			both();
		}
		tearDownNotification();
	}

	/**
	 * Run both tasks
	 */
	private void both()
	{
		setTask(SYNC);
		doWhile( ImmutableList.<String>of( "sync-one", "sync-two", "sync-three" ) );
		setTask(SCAN);
		doWhile( ImmutableList.<String>of( "scan-one", "scan-two", "scan-three" ) );
		send( ON_COMPLETE, 0, BOTH );
	}

	/**
	 * Execute the sync task
	 */
	private void sync()
	{
		doWhile( ImmutableList.<String>of( "sync-one", "sync-two", "sync-three" ) );
		send( ON_COMPLETE, 0, SYNC );
	}

	/**
	 * Execute the scan task
	 */
	private void scan()
	{
		doWhile( ImmutableList.<String>of( "scan-one", "scan-two", "scan-three" ) );
		send( ON_COMPLETE, 0, SCAN );
	}

	/**
	 * Helper method to do fake work
	 */
	private void doWhile(ImmutableList<String> files)
	{
		for ( String file : files ) {
			updateNotification( file );
			send( ON_PROGRESS, 0, file );
			sleep();
		}
	}

	/**
	 *  Normally we would do some work here, like download a file.
	 * For our demo, we just sleep for 5 seconds.
	 */
	private void sleep()
	{
		long endTime = System.currentTimeMillis() + 5 * 1000;
		while ( System.currentTimeMillis() < endTime ) {
			synchronized ( this ) {
				try {
					wait( endTime - System.currentTimeMillis() );
				} catch ( Exception e ) {
					// Do nothing
				}
			}
		}
	}

	/**
	 * This is called when service client calls bindService()
	 */
	@Override
	public IBinder onBind(Intent intent)
	{
		return new LocalBinder();
	}

	/**
	 * Send a message, and remember the last message sent
	 */
	private void send(int type, int arg1, String message)
	{
		// We do not obtain() a new message from the message pool
		// because we want to save the last message sent, incase we
		// must resend it when the client reconnects
		mLastMessage = new Message();
		mLastMessage.what = type;
		mLastMessage.obj = message;
		mLastMessage.arg1 = arg1;
		sendMessage( mLastMessage );
	}

	/**
	 * Send a message if the handler to the service client is connected
	 */
	private void sendMessage(Message message)
	{
		if ( message == null || mHandler == null ) {
			return;
		}
		mHandler.sendMessage( Message.obtain( message ) );
	}

	/**
	 * Tell the service client the current task
	 */
	private void setTask(String task) {
		mCurrentTask = task;
		send( ON_TASK_CHANGE, 0, mCurrentTask );
	}

	/**
	 * Update Notification card
	 */
	public void updateNotification(String message)
	{
		if ( mNotify != null ) {
			mNotify.setContentText( message );
			mNotifyManager.notify( NOTIFY_ID, mNotify.getNotification() );
		}
	}

	/**
	 * Tell android to remove the notification card from the notification bar
	 */
	public void tearDownNotification()
	{
		if ( mNotifyManager != null ) {
			mNotifyManager.cancelAll();
		}
		mNotifyManager = null;
		mNotify = null;
	}

	/**
	 * Tell android to create a place on the notification bar for us, and set an initial notification card
	 */
	public void setUpNotification()
	{
		mNotifyManager = (NotificationManager) getSystemService( NOTIFICATION_SERVICE );
		// If user clicks on the notification, it should start the main SuperGNES activity
		PendingIntent notifyIntent = PendingIntent.getActivity( getApplicationContext(), 0,
				new Intent( getApplicationContext(), MainActivity.class ),
				PendingIntent.FLAG_UPDATE_CURRENT );

		// Build the notification
		mNotify = new NotificationCompat.Builder( this );
		mNotify.setContentIntent( notifyIntent );
		mNotify.setContentTitle( getString( R.string.service_running ) )
				.setSmallIcon( R.drawable.download );
		// Set the notification
		mNotifyManager.notify( NOTIFY_ID, mNotify.getNotification() );
	}

	/**
	 * Binder interface that allows clients to control the service
	 */
	public class LocalBinder extends Binder
	{
		/**
		 * Called by the service client to tell the service about
		 */
		public void setHandler(Handler handler)
		{
			mHandler = handler;
			// Resend the last message after connect
			sendMessage( mLastMessage );
		}

		/**
		 * Called by the service client when disconnecting from the service
		 */
		public void clearHandler()
		{
			DemoService.this.mHandler = null;
		}

		/**
		 * Called by the service client to ask what task the service is currently running
		 */
		public String getTask()
		{
			return DemoService.this.mCurrentTask;
		}
	}
}
