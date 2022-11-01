package com.entertainment.basemvvmproject.utils;

/*
public class MyFirebaseService extends FirebaseMessagingService {
    private final String TAG = "MyFirebaseService";
    private int mCount;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        Log.d(TAG, "onMessageReceived: " + data.toString());
        String type = data.get("type");
        String title = data.get("title");
        String bodyMess = data.get("body");
        String source_id = data.get("source_id");
        String notification_id = data.get("notification_id");
        int int_noti_id = 0;
        try {
            int_noti_id = Integer.parseInt(notification_id);
        } catch (Exception e) {
            //do nothing
        }
        sendNotification(title, bodyMess, type, source_id, data);
    }

    @Override
    public void onNewToken(@NotNull String token) {

        SharedPreferencesUtil.getInstance().put(Constants.PREFERENCE_DEVICES_TOKEN, token);
        sendRegistrationToServer(token);
    }

    @SuppressLint("HardwareIds")
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
        IFCMService ifcmService = RetrofitClient.getInstance().create(IFCMService.class);
        Call<ApiObj> updateFCMCall = ifcmService.update_FCM_token(token,
                Settings.Secure.getString(AeonApplication.getContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID), Constants.DEVICE_TYPE);
        updateFCMCall.enqueue(new MyCallback<ApiObj>() {
            @Override
            protected void onSuccess(Call<ApiObj> call, Response<ApiObj> response) {
                Logger.e("xxxxx " + response.toString());
            }

            @Override
            protected void onError(Call<ApiObj> call, Object object) {

            }
        });
    }

    private void sendNotification(String title, String bodyMessage, String type, String source_id, Map<String, String> data) {
        Intent intent = new Intent();
        boolean isShowNotification = false;
        if ("event_lifestyle".equals(type)) {
            if (isAppVisible()) {
                Intent intent1 = new Intent();
                intent1.setAction(ACTION_CHECK_IN_LIFE_STYLE_EVENT_SUCCESS);
                intent1.putExtra(DetailEventLifeStyleActivity.PARAM_RESULT_CHECK_IN_SUCCESS, data.get("voucher"));
                intent1.putExtra(DetailEventLifeStyleActivity.PARAM_EVENT_NAME_SUCCESS, data.get("event_name"));
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);
                return;
            }
            isShowNotification = true;
            intent.setClass(this, DetailEventLifeStyleActivity.class);
            intent.putExtra(DetailEventLifeStyleActivity.EVENT_ID_PARAM, Integer.parseInt(source_id));
            intent.putExtra(DetailEventLifeStyleActivity.PARAM_RESULT_CHECK_IN_SUCCESS, data.get("voucher"));
            intent.putExtra(DetailEventLifeStyleActivity.PARAM_EVENT_NAME_SUCCESS, data.get("event_name"));
        }else {
            isShowNotification = true;
            try {
                intent.setClass(this, EventDetailActivity.class);
                intent.putExtra(PARAM_EVENT_DETAIL_ID, source_id);
            } catch (Exception e) {
                //do nothing
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationManager mNotifyManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        //For Android Version Orio and greater than Orio.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    getString(R.string.notification_channel_id),
                    getString(R.string.notification_push_name),
                    NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription(getString(R.string.notification_push_des));
            mNotifyManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,
                getString(R.string.notification_channel_id));
        mBuilder.setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bodyMessage))
                .setContentText(bodyMessage)
                .setSmallIcon(R.drawable.icon_launcher)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setChannelId(getString(R.string.notification_channel_id))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        if (isShowNotification) {
            mNotifyManager.notify(mCount, mBuilder.build());
            mCount++;
        }
    }

    boolean isAppVisible() {
        return ProcessLifecycleOwner
                .get()
                .getLifecycle()
                .getCurrentState()
                .isAtLeast(Lifecycle.State.STARTED);
    }
}
 */
