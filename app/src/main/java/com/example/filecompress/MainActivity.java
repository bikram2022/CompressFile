package com.example.filecompress;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.BitSet;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    String originalString = null;
    Huffman h = null;
    String encodedResult = null;
    Uri originalFileUri = null;
    Uri encodedFileUri = null;
    String contentToWrite = null;
    static final int REQUEST_FILE_OPEN = 1;
    private static final int CREATE_FILE = 2;
    private static final int CREATE_FILE_FINAL = 3;
    private static MainActivity instance;
    public ProgressBar decodingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        decodingProgressBar = (ProgressBar) findViewById(R.id.loading_indicator);
    }

    public void openFile(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
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
            }
            String result = new String();
            try {
                result = readTextFromUri(uri);
                originalString = result;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == CREATE_FILE
                && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            if (resultData != null) {
                encodedFileUri = resultData.getData();
            }
            writeFile(contentToWrite);
        }

        if (requestCode == CREATE_FILE_FINAL
                && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            if (resultData != null) {
                encodedFileUri = resultData.getData();
            }
            try {
                ParcelFileDescriptor pfd = getContentResolver().
                        openFileDescriptor(encodedFileUri, "w");
                FileOutputStream fileOutputStream =
                        new FileOutputStream(pfd.getFileDescriptor());
                fileOutputStream.write(contentToWrite.getBytes());
                // Let the document provider know you're done by closing the stream.
                fileOutputStream.close();
                pfd.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String readTextFromUri(Uri uri) throws IOException {
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

        contentToWrite = Util.prepareContentToWrite(encodedResult, h.hmapCode);
        createFile(null);
    }

    public void writeFile(String content) {

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
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "test_encoded.txt");

        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        startActivityForResult(intent, CREATE_FILE);
    }

    public void decompress(View view) {

        Log.d("Iam","here first");
        decodingProgressBar.setVisibility(View.VISIBLE);


        new SecondThread().execute(String.valueOf(originalFileUri));

    }

    public static MainActivity getInstance() {
        return instance;
    }

    public void onGettingResult(String s) {

        decodingProgressBar.setVisibility(View.INVISIBLE);

        contentToWrite = s;

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "test_decoded.txt");

        startActivityForResult(intent, CREATE_FILE_FINAL);

    }

}