package com.example.filecompress;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    String originalString = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    static final int REQUEST_FILE_OPEN = 1;

    public void openFile(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Only the system receives the ACTION_OPEN_DOCUMENT, so no need to test.
        startActivityForResult(intent, REQUEST_FILE_OPEN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if ( requestCode == REQUEST_FILE_OPEN
                &&resultCode == Activity.RESULT_OK){
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                // Perform operations on the document using its URI.
                Log.d("Message::",uri.toString());
            }
            String result = new String();
            try {
                result = readTextFromUri(uri);
                originalString = result;
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("Message::",result);
        }
    }
    private String readTextFromUri(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream =
                     getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }
    public void compress(View view){
        Huffman h = new Huffman(originalString);
        String encodedResult = h.encode();
        Log.d("Message::",encodedResult);
    }
    public void decompress(View view){

    }
}