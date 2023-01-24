package com.example.filecompress;

import static android.app.PendingIntent.getActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    String originalString = null;
    Huffman h = null;
    String encodedResult = null;
    String decodedResult = null;
    Uri originalFileUri = null;
    Uri encodedFileUri = null;
    String contentToWrite =null;
    static final int REQUEST_FILE_OPEN = 1;
    // Request code for creating a PDF document.
    private static final int CREATE_FILE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


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
        if (requestCode == REQUEST_FILE_OPEN
                && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                originalFileUri = uri;
                // Perform operations on the document using its URI.
                Log.d("Message::", uri.toString());
            }
            String result = new String();
            try {
                result = readTextFromUri(uri);
                originalString = result;
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("Message::", result);
        }

        if (requestCode == CREATE_FILE
                && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            if (resultData != null) {
                encodedFileUri = resultData.getData();
                // Perform operations on the document using its URI.
                Log.d("Message::", String.valueOf(encodedFileUri));
            }
            writeFile(contentToWrite);
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

    public void compress(View view) {
        h = new Huffman(originalString);
        encodedResult = h.encode();
        Log.d("Message::", encodedResult);
        Log.d("Message::", String.valueOf(h.hmapCode));

        contentToWrite = prepareContentToWrite(encodedResult, h.hmapCode);
        createFile(null);
    }

    public String prepareContentToWrite(String encoded, HashMap<Character, String> table) {
        StringBuilder content = new StringBuilder();

        for (HashMap.Entry<Character, String> entry : table.entrySet()) {
            String withLeadingZeros;
            String binaryString;

            char key = Character.valueOf(entry.getKey());
            String value = entry.getValue();

            binaryString = Integer.toBinaryString(key);
            withLeadingZeros = String.format("%8s", binaryString).replace(' ', '0');

            content.append(withLeadingZeros);
            Log.d("Message::", withLeadingZeros);

            for (int i = 0; i < value.length(); i++) {
                binaryString = Integer.toBinaryString(value.charAt(i));
                withLeadingZeros = String.format("%8s", binaryString).replace(' ', '0');
                content.append(withLeadingZeros);
            }
        }
        content.append("+");

        content.append(encoded);

        Log.d("Message::", String.valueOf(content));
        return String.valueOf(content);

    }

    public void writeFile(String content) {
        Log.d("Message::", "control is here");

        BitSet bitSet = new BitSet(content.length());
        int bitcounter = 0;
        for (Character c : content.toCharArray()) {
            if (c.equals('1')) {
                bitSet.set(bitcounter);
            }
            bitcounter++;
        }
        byte[] bytes = bitSet.toByteArray();

        try {
            ParcelFileDescriptor pfd = getContentResolver().
                    openFileDescriptor(encodedFileUri, "w");
            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write(bytes);
            // Let the document provider know you're done by closing the stream.
            fileOutputStream.close();
            pfd.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFile(Uri pickerInitialUri) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/encoded");
        intent.putExtra(Intent.EXTRA_TITLE, "test.encoded");

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        startActivityForResult(intent, CREATE_FILE);
    }

}