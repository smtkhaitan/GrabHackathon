package com.example.test;

import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateMsqlTable {


    public boolean insertIntoRemoteDatabase(String id, String transactionCustomerId,
                                            String transactionAmount, String paidOrReceived) {
        String url = "http://192.168.43.164:8080/v1/grabWallet/updateTransaction?";
        url += "Id=" + id + "&";
        url += "transactionCustomerId=" + transactionCustomerId + "&";
        url += "transactionAmount=" + transactionAmount + "&";
        url += "paidOrReceived=" + paidOrReceived;
        System.out.println(url);

        try {
            doGetRequest(url);
        } catch (Exception e) {
            System.out.println("exception is " + e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean insertIntoRemoteDatabaseWallet(String id, String amount) {
        String url = "http://192.168.43.164:8080/v1/grabWallet/updateWallet?";
        url += "Id=" + id + "&";
        url += "amount=" + amount;
        System.out.println(url);

        try {
            doGetRequest(url);
        } catch (Exception e) {
            System.out.println("exception is " + e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    void doGetRequest(String url) throws IOException {
        final Request request = new Request.Builder()
                .url(url)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        String res = response.body().string();
                        System.out.println("response: " + res);
                    }
                });
    }
}
