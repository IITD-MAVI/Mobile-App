package com.nipungupta.helloworld;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class TestAsync extends AsyncTask<String, Integer, String>
{
    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static InputStreamReader inputStreamReader;
    private static BufferedReader bufferedReader;

    protected void onPreExecute (){
       // Log.d("PreExceute", "On pre Exceute......");
        try {
            serverSocket = new ServerSocket(9876);
        } catch (IOException e) {
            System.out.println("Could not listen on port 9876");
        }
    }

    protected String doInBackground(String... string) {
        Log.d("DoINBackGround",string[0]);

/*        HttpClient httpclient = new DefaultHttpClient();
        // specify the URL you want to post to
        HttpPost httppost = new HttpPost("localhost");
        try {
            // create a list to store HTTP variables and their values
            List nameValuePairs = new ArrayList();
            // add an HTTP variable and value pair
            nameValuePairs.add(new BasicNameValuePair("myHttpData", string[0]));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            // send the variable and value, in other words post, to the URL
            HttpResponse response = httpclient.execute(httppost);
        } catch (ClientProtocolException e) {
            // process execption
        } catch (IOException e) {
            // process execption
        }*/

        while(true) {
            try {
                clientSocket = serverSocket.accept();
                inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
                bufferedReader = new BufferedReader(inputStreamReader);
                String message = bufferedReader.readLine();

                System.out.println(message);
                inputStreamReader.close();
                clientSocket.close();
                return message;
            } catch(IOException ex) {
                System.out.println("Problem in message reading");
            }
        }

//        return string[0];
    }

    protected void onProgressUpdate(Integer...a){
//        Log.d("You are in progress update ... " + a[0]);
    }

    protected void onPostExecute(String result) {
        //Toast.makeText()
//        Log.d(""+result);
}
        }