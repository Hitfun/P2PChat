package ch.ethz.inf.vs.a1.rubfisch.p2pchat;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.io.BufferedReader;
import java.io.PrintWriter;

/**
 * Created by alexa on 11.12.2017.
 */

public class ChatClient {
    private BufferedReader dataIn;
    private PrintWriter dataOut;
    private String name;
    private Listener listener;
    private ArrayAdapter adapter;

    public ChatClient(String name,BufferedReader dataIn,PrintWriter dataOut,ArrayAdapter adapter){
        this.dataIn=dataIn;
        this.dataOut=dataOut;
        this.name=name;
        this.adapter=adapter;

        listener=new Listener(dataIn,adapter);
    }

    public String getName(){return name;}

    public BufferedReader getDataIn(){return dataIn;}

    public PrintWriter getDataOut(){return dataOut;}

    public void startListening(){
        listener.listening=true;
        listener.start();
    }

    public void stopListening(){
        listener.listening=false;
    }
}
