package ch.ethz.inf.vs.a1.rubfisch.p2pchat;

import android.widget.ArrayAdapter;

import java.io.BufferedReader;

/**
 * Created by alexa on 11.12.2017.
 */

public class Listener extends Thread{
    BufferedReader input;
    boolean listening;
    ArrayAdapter adapter;

    public Listener(BufferedReader input,ArrayAdapter adapter){
        this.input=input;
        this.adapter=adapter;
    }

    public void run(){
        while(listening){
            try{
                String data;
                if((data=input.readLine())!=null){
                    ChatActivity.receive(data);
                    adapter.notifyDataSetChanged();

                }

            }catch(Exception e){
                e.printStackTrace();
            }

        }
    }
}

