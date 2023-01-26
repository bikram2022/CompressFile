package com.example.filecompress;

import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class SecondThread extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... strings) {
        String binaryString = strings[0];
        String parts[] = binaryString.split("01111100",2);

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


        HashMap<Character, String> table = new HashMap<Character, String>();

        Character key = null;
        String value = "";
        for(int i = 0; i < encodedHash.length(); i++){
            if(encodedHash.charAt(i) != '0' && encodedHash.charAt(i) != '1'){
                if(key != null){
                    table.put(key,value);
                    value = "";
                }
                key = encodedHash.charAt(i);
            }
            else{
                value += encodedHash.charAt(i);
            }
        }
        table.put(key,value);

        String ansString = "";
        String temp = "";
        for(int i = 0; i < parts[1].length(); i++){
            temp+=parts[1].charAt(i);

            for (Map.Entry<Character, String> entry : table.entrySet()) {
                if(temp.equals(entry.getValue())){
                    ansString += entry.getKey();
                    temp = "";
                    break;
                }
            }
        }

        return ansString;
    }

    @Override
    protected void onPostExecute(String s) {
        MainActivity.getInstance().onGettingResult(s);
    }

}
