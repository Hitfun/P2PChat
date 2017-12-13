package ch.ethz.inf.vs.a1.rubfisch.p2pchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;


public class BroadcastReceiverActivity extends AppCompatActivity implements PeerListListener {
    WifiP2pManager mManager;
    Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    private String name;
    private WifiP2pDeviceList peers;//new ArrayList();
    private ListView mListView;
    private ArrayAdapter<String> WifiP2parrayAdapter;
    private WifiP2pDevice ConnectedPartner;
    private int PORT = 8888;

    private PeerListListener peerListListener = new PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            // Out with the old, in with the new.
            peers = new WifiP2pDeviceList(peerList);
            WifiP2parrayAdapter.clear();
            for (WifiP2pDevice peer : peerList.getDeviceList()) {


                WifiP2parrayAdapter.add(peer.deviceName); //+ "\n" + peer.deviceAddress

                // set textbox search_result.setText(peer.deviceName);


            }
            // If an AdapterView is backed by this data, notify it
            // of the change.  For instance, if you have a ListView of available
            // peers, trigger an update.
            //((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
            //if (peers.size() == 0) {
                //no devices found
                //return;
            //}
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_receiver);

        // get name entered by user in MainActivity
        Bundle extras = getIntent().getExtras();
        name = extras.getString("nameText");
        Log.d("name", name);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiBroadcastReceiver(mManager, mChannel, this);  //Setting up Wifi Receiver
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //will not provide info about who it discovered
            }

            @Override
            public void onFailure(int reasonCode) {

            }
        });
        mListView = (ListView) findViewById(R.id.ListView);
        WifiP2parrayAdapter = new ArrayAdapter<String>(this, R.layout.fragment_peer);
        mListView.setAdapter(WifiP2parrayAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                //Get string from textview
                TextView tv = (TextView) arg1;
                WifiP2pDevice device = null;
                for(WifiP2pDevice wd : peers.getDeviceList())
                {
                    if(wd.deviceName.equals(tv.getText()))
                        device = wd;
                }
                if(device != null)
                {
                    //Connect to selected peer
                    connectToPeer(device);

                }
                else
                {
                    //dialog.setMessage("Failed");
                    //dialog.show();

                }
                //Log.d("############","Items " +  MoreItems[arg2] );
            }

        });
        receiveConnectRequest.execute();



    }

    public void connectToPeer(final WifiP2pDevice wifiPeer)
    {
        this.ConnectedPartner = wifiPeer;

        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = wifiPeer.deviceAddress;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener()  {
            public void onSuccess() {
                // TODO: Msg saying "Waiting for *name* to respond."
                AsyncTask<Void, Void, Void> sendConnectRequest = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            Socket socket = new Socket();
                            socket.connect((new InetSocketAddress(config.deviceAddress, PORT)), 10000);
                            socket.setSoTimeout(10000);
                            PrintWriter dataOut = new PrintWriter(socket.getOutputStream(), true);
                            BufferedReader dataIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String request = "";
                            try {
                                request = new JSONObject()
                                        .put("type", "connection request")
                                        .put("name", name).toString();
                            } catch (JSONException e) {
                                Log.d("##BroadcastRecieverAct", "creating connection request failed :" + e.getMessage());
                            }
                            dataOut.println(request);
                            String in;
                            try {
                                while (true) {
                                    if ((in = dataIn.readLine()) != null) {
                                        String ack;
                                        try {
                                            ack = new JSONObject(in).get("type").toString();
                                        } catch (JSONException e) {
                                            ack = "";
                                        }
                                        if (ack.equals("ack")) {
                                            //TODO: transition to ChatActivity
                                            break;
                                        } else if (ack.equals("decline")) {
                                            //TODO: *name* declined

                                            break;
                                        }
                                    }
                                }
                            } catch (SocketTimeoutException e) {
                                //TODO: Notify user that a timeout occurred.
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
                sendConnectRequest.execute();
                //setClientStatus("Connection to " + targetDevice.deviceName + " sucessful");
            }

            public void onFailure(int reason) {
                //setClientStatus("Connection to " + targetDevice.deviceName + " failed");
                //TODO: Notify the user the connection failed.

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        List<WifiP2pDevice> devices = (new ArrayList<>());
        devices.addAll(peerList.getDeviceList());

        //do something with the device list
    }





    public void onRefresh(View view) {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //will not provide info about who it discovered
            }

            @Override
            public void onFailure(int reasonCode) {

            }
        });
    }

    AsyncTask<Void, Void, Void> receiveConnectRequest = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                ServerSocket server = new ServerSocket();
                Socket client = server.accept();
                BufferedReader dataIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter dataOut = new PrintWriter(client.getOutputStream(), true);
                String in;
                while(true) {
                    if ((in = dataIn.readLine()) != null) {
                        String request;
                        String name;
                        try {
                            JSONObject json = new JSONObject(in);
                            request = json.getString("request");
                            name = json.getString("name");
                        } catch (JSONException e) {
                            request = "";
                            name = "";
                        }
                        if (request.equals("connection request")) {
                            //TODO: *name* wants to connect to you. (Accept/Decline)
                            String ack = "";
                            try {
                                ack = new JSONObject()
                                        .put("type", "ack").toString();
                            } catch (JSONException e) {
                                Log.d("##BroadcastRecieverAct", "creating ack failed :" + e.getMessage());
                            }
                            dataOut.println(ack);
                            //TODO: transition to ChatActivity
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }

    };

}
