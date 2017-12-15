package ch.ethz.inf.vs.a1.rubfisch.p2pchat;

import java.io.BufferedReader;

/**
 * Created by alexa on 11.12.2017.
 */

public class Listener extends Thread{
    BufferedReader input;
    boolean listening;

    public Listener(BufferedReader input){
        this.input=input;
    }

    public void run(){
        while(listening){
            try{
                String data;
                if((data=input.readLine())!=null){
                    ChatActivity.receive(data);

                }

            }catch(Exception e){
                e.printStackTrace();
            }

        }
    }
}

