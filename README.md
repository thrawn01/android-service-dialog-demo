
## Android Application Demo
This application demo shows how to accomplish the following

### Service Example
This demo demonstrates how to create a service that will carry out several workloads in a sequential
manner using a ServiceIntent(). In addition the service notifies the Activity Thread when it is 
working using a handler passed via bindService() 

### Progress Dialog Example
This demo demonstrates how to create a progress dialog that survives orientation change and
application exit and re-entry

### Notification Example
This demo demonstrates how to create a notification bar while a service is running. 
*NOTE: This Notification example does not demo PendingIntent, but instead marks the MainActivity as
android:launchMode="singleTask" to avoid issues that may arrise when using the back button after
opening the MainActivity via the Notification*
