package ch.ethz.inf.vs.a1.rubfisch.p2pchat;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener, WifiP2pManager.ConnectionInfoListener{

    private ListView listView;
    //boolean myMessage = true;
    private List<ChatMessage> Messages;
    private ArrayAdapter<ChatMessage> adapter;
    static private WifiP2pInfo info;
    private WifiP2pManager.Channel channel;
    private WifiP2pManager manager;
    private List<ChatClient> clients;
    private ServerSocket serverSocket;
    private Socket socket;
    private String name;
    InetAddress groupOwner = null;
    private int PORT = 8000;
    BufferedReader fromGroupOwner;
    PrintWriter toGroupOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Setup the Actionbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ff6e00")));

        //Set Button listener
        ImageButton send = (ImageButton) findViewById(R.id.send);
        send.setOnClickListener(this);

        Messages = new ArrayList<>();
        listView = (ListView) findViewById(R.id.chat);

        //set ListView adapter first
        adapter = new ChatMessageAdapter(this, R.layout.foreign_bubble, Messages);
        listView.setAdapter(adapter);

        //TODO: Get the P2pManager and Channel Objects and your own name for the connection information
        manager.requestConnectionInfo(channel,this);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo information) {
        info = information;
        if(info.groupOwnerAddress != groupOwner) {
            groupOwner=info.groupOwnerAddress;
            initialize();
        }
    }

    public void initialize (){
        clients.clear();
        if(info.isGroupOwner){
            //wait for clients to make contact and add them to a list
            try {
                serverSocket = new ServerSocket(PORT);
            }catch(Exception e){
                e.printStackTrace();

            }



            getClientInfo.execute();

        }else{
            //make contact with group owner
            try{
                InetAddress groupOwner=info.groupOwnerAddress;
                socket = new Socket(groupOwner,PORT);
                fromGroupOwner=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                toGroupOwner= new PrintWriter(socket.getOutputStream(),true);
                toGroupOwner.println(name);

                listenToGroupOwner.execute();


            }catch(Exception e){
                e.printStackTrace();
            }

        }
    }

    AsyncTask<Void,Void,Void> getClientInfo =new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... voids) {

            try{
                Socket clientSocket = serverSocket.accept();
                BufferedReader dataIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter dataOut = new PrintWriter(clientSocket.getOutputStream(),true);
                String clientName= dataIn.readLine();
                ChatClient client = new ChatClient(clientName,dataIn,dataOut);
                client.startListening();

                clients.add(client);

                if(clients.size()==1){
                    getSupportActionBar().setTitle(clientName);
                }else{
                    getSupportActionBar().setTitle("Group Chat");
                }

            }catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }

    };

    AsyncTask<Void,Void,Void> listenToGroupOwner = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... voids) {
            try{
                while(true){
                    String data;
                    if((data=fromGroupOwner.readLine())!=null){
                        receive(data);
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
    };



    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.send) {
            //get the text
            EditText editText = (EditText) findViewById(R.id.editText);
            String text = editText.getText().toString();

            //get current time ( I used the deprecated Class Time to ensure backwards compatability)
            Time today = new Time(Time.getCurrentTimezone());
            today.setToNow();
            String time = today.hour + ":" + today.minute;

            //add message to the list
            ChatMessage ChatMessage = new ChatMessage(text.toString(), true, "", time);
            Messages.add(ChatMessage);
            adapter.notifyDataSetChanged();
            editText.setText("");
            /*TODO: Send your message "text" with your Name and a timestamp to the chat partners
             * If you are the group owner send your message to all chat partners in clients list.
             * If you are not the group owner send your message only to him
             */
            if(info.isGroupOwner){

            }else{

            }


        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.add_people:
                // TODO: Add action of adding people to the chat: call InviteUserActvity;
                //   Also change the title of the activity to include the new ChatPartner: getSupportActionBar().setTitle("");                                                                   */
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds our add_people button to the action bar
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /* TODO: When a message is received, the following has to be done:
     * 1. If you are the group owner forward message to all other chat partners in the clients list
     * 2. Retreive the following values from the message: the "text", the "sender", the "time_stamp"
     * 3. Create a new ChatMessage msg = new ChatMessage(text.toString(), false, sender, time_stamp);
     * 4. Add this message to our message list: Messages.add(msg);
     * 5. Notify the adapter, so that it can draw the new message on the listview: adapter.notifyDataSetChanged();
    */
    public static void receive(String data){
        if(info.isGroupOwner){

        }else{

        }
    }
}
