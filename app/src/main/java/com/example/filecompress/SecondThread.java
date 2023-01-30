package com.example.filecompress;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class SecondThread extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... strings) {

        Uri uri = Uri.parse(strings[0]);
        InputStream inputStream = null;
        try {
            inputStream = MainActivity.getInstance().getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = MainActivity.getInstance().getApplicationContext().getContentResolver().openAssetFileDescriptor(uri , "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int fileSize = (int) fileDescriptor.getLength();


        byte[] bytes = new byte[fileSize];
        try {
            inputStream.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder binaryStringBuilder = new StringBuilder();
        BitSet set = BitSet.valueOf(bytes);
        for (int i = 0; i <= set.length(); i++) {
            if (set.get(i)) {
                binaryStringBuilder.append("1");
            } else {
                binaryStringBuilder.append("0");
            }
        }

        String binaryString = binaryStringBuilder.toString();
        String parts[] = binaryString.split("0000000011111111",2);

//        StringBuilder encodedHashBuilder = new StringBuilder();
        String encodedHash = "";

        for(int i = 0; i < parts[0].length(); i = i + 8){
            String s = "";

            s += binaryString.charAt(i);
            s += binaryString.charAt(i+1);
            s += binaryString.charAt(i+2);
            s += binaryString.charAt(i+3);
            s += binaryString.charAt(i+4);
            s += binaryString.charAt(i+5);
            s += binaryString.charAt(i+6);
            s += binaryString.charAt(i+7);

            char c = (char) Integer.parseInt(s,2);

            encodedHash += c;

        }

        HashMap<String, Character> table = new HashMap<String, Character>();

        Character value = null;

        StringBuilder keyBuilder = new StringBuilder();
        for(int i = 0; i < encodedHash.length(); i++){
            if(encodedHash.charAt(i) == '0' || encodedHash.charAt(i) == '1'){
                keyBuilder.append(encodedHash.charAt(i));
            }
            else{
                if(value != null){
                    table.put(String.valueOf(keyBuilder),value);
                    keyBuilder.setLength(0);
                }
                value = encodedHash.charAt(i);
            }
        }
        table.put(String.valueOf(keyBuilder),value);

        StringBuilder ansStringBuilder = new StringBuilder();
        String temp = "";
        for(int i = 0; i < parts[1].length(); i++){
            temp+=parts[1].charAt(i);
            if(table.containsKey(temp)){
                ansStringBuilder.append(table.get(temp));
                temp = "";
            }
        }

        return ansStringBuilder.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPreExecute();
        MainActivity.getInstance().onGettingResult(s);
    }
}
