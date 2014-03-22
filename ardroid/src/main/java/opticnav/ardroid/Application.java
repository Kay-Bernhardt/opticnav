package opticnav.ardroid;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;
import opticnav.ardd.protocol.PassCode;
import opticnav.ardd.protocol.Protocol;
import opticnav.ardd.protocol.chan.Channel;
import opticnav.ardd.protocol.chan.ChannelUtil;
import opticnav.ardroid.connection.AsyncConnectionEvent;
import opticnav.ardroid.connection.CancellableSocket;
import opticnav.ardroid.connection.ServerUIHandler;
import opticnav.ardroid.ui.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Because Android development is brain damaged, it's very hard to keep the same state in Activitys during changes
 * within the Activity lifecycle (creation and destruction due to rotating the screen, and many more).
 *
 * _Bad_ suggestions to address the problem: Setting android:configChanges, using Services, using Fragments,
 * and anything else that mandates confusing flow or terrifying amounts of boilerplate code.
 *
 * Any variables that need to be persistent in the same Activity as it goes through lifecycle changes are kept here.
 * It's essentially global state, but it just goes to show how broken Android is. :/
 *
 * This class is not thread safe! Only use this class from the UI thread.
 */
public class Application extends android.app.Application {
    public static class Lifecycle<T extends Activity> {
        private T activity = null;

        public void onCreate(T activity) {
            if (this.activity != null) {
                throw new IllegalStateException("Activity already created");
            }
            this.activity = activity;
        }
        
        public void onDestroy(T activity) {
            this.activity = null;
        }

        public T getActivity() {
            return this.activity;
        }
    }

    private static final String PREFS_SERVERHOST = "serverHost";
    private static final String PREFS_SERVERPORT = "serverPort";
    private static final String PREFS_PASSCODE = "passCode";

    public final Lifecycle<WelcomeActivity> welcome = new Lifecycle<WelcomeActivity>();
    public final Lifecycle<ServerConfigActivity> serverConfig = new Lifecycle<ServerConfigActivity>();
    public final Lifecycle<MapActivity> map = new Lifecycle<MapActivity>();

    private SharedPreferences prefs;
    private ServerUIHandler server;

    @Override
    public void onCreate() {
        super.onCreate();
        this.prefs = getSharedPreferences("OpticNav", 0);
    }

    public CancellableSocket.Cancellable connectToServer(final String host, final int port) {
        // XXX
        if (this.server != null) {
            this.server.close();
            this.server = null;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREFS_SERVERHOST, host);
        editor.putInt(PREFS_SERVERPORT, port);
        editor.commit();

        SocketAddress addr = new InetSocketAddress(host, port);

        return CancellableSocket.connect(addr, 10000, new AsyncConnectionEvent(new CancellableSocket.ConnectionEvent() {
            @Override
            public void cancelled() {

            }

            @Override
            public void error(IOException e) {
                serverConfig.getActivity().setConnecting(false);
                Toast.makeText(Application.this, "Could not connect\nReason: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void connected(Socket socket) {
                try {
                    final Channel channel = ChannelUtil.fromSocket(socket);
                    server = new ServerUIHandler(Application.this, channel);
                    createConnectionNotification();
                    server.connect(getPasscode());
                } catch (IOException e) {
                    error(e);
                }
            }
        }));
    }

    public String getPreferencesServerHost() {
        return prefs.getString(PREFS_SERVERHOST, "");
    }

    public int getPreferencesServerPort() {
        return prefs.getInt(PREFS_SERVERPORT, Protocol.DEFAULT_ARD_PORT);
    }

    public PassCode getPasscode() {
        String passCodeString = prefs.getString(PREFS_PASSCODE, null);
        boolean isValid = passCodeString != null && PassCode.isStringCodeValid(passCodeString);
        if (isValid) {
            return new PassCode(passCodeString);
        } else {
            return null;
        }
    }

    private void createConnectionNotification() {
        NotificationManager notMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // TODO - a better way?
        if (Build.VERSION.SDK_INT >= 16) {
            /* notification builder only works for android SDK 16+ */

            Intent intent = new Intent(this, MapActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

            Notification not = new Notification.Builder(this)
                    .setContentTitle("Connected")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pIntent)
                    .build();

            not.flags |= Notification.FLAG_NO_CLEAR;

            notMgr.notify(1, not);
        }
    }
}
