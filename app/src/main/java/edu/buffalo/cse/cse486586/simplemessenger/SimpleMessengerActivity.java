package edu.buffalo.cse.cse486586.simplemessenger;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class SimpleMessengerActivity extends Activity {
    static final String TAG = SimpleMessengerActivity.class.getSimpleName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final int SERVER_PORT = 10000;

    private RecyclerView recyclerView;
    private MsgAdapter adapter;
    private List<Message> msgList = new ArrayList<>();

    /** Called when the Activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }

        final EditText editText = (EditText) findViewById(R.id.input_text);

        Button send = (Button) findViewById(R.id.send);
        this.recyclerView = (RecyclerView) findViewById(R.id.msg_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
        this.adapter = new MsgAdapter(msgList);
        recyclerView.setAdapter(adapter);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = editText.getText().toString();
                if (!"".equals(content)) {
                    Message msg = new Message(content, Message.TYPE_SENT);
                    msgList.add(msg);
                    adapter.notifyItemChanged(msgList.size() - 1);
                    recyclerView.scrollToPosition(msgList.size() - 1);
                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg.getContent(), myPort);
                    editText.setText("");
                }
            }
        });
    }

    /***
     * ServerTask is an AsyncTask that should handle incoming message.
     */
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            while (true) {
                ServerSocket serverSocket = sockets[0];
            
            /*
             * Receive messages and passes them to onProgressUpdate().
             */
                try {
                    // Accept TCP connection
                    Socket client = serverSocket.accept();
                    // Get input stream
                    InputStream in = client.getInputStream();
                    // Read the message to buf array
                    byte[] buf = new byte[128];
                    int length = in.read(buf);
                    String msg = new String(buf, 0, length);
                    // Pass messages to onProgessUpdate
                    publishProgress(msg);
                    // Close connection
                    client.close();
                } catch (IOException e) {
                    Log.e(TAG, "Message receive failed.");
                }
            }
        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            Message msgRece = new Message(strReceived, Message.TYPE_RECEIVED);
            msgList.add(msgRece);
            adapter.notifyItemChanged(msgList.size() - 1);
            recyclerView.scrollToPosition(msgList.size() - 1);
            return;
        }
    }

    /**
     * ClientTask is an AsyncTask that should send a string over the network.
     */
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String remotePort = REMOTE_PORT0;
                if (msgs[1].equals(REMOTE_PORT0))
                    remotePort = REMOTE_PORT1;

                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(remotePort));
                String msgToSend = msgs[0];

                /*
                 * Output stream to socket
                 */
                OutputStream out = socket.getOutputStream();
                out.write(msgToSend.getBytes());
                socket.close();

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;
        }
    }
}