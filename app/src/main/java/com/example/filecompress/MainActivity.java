package com.example.filecompress;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.BitSet;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    String originalString = null;
    Huffman h = null;
    String encodedResult = null;
    Uri selectedFileUri = null;
    Uri createdFileUri = null;
    String contentToWrite = null;
    static final int REQUEST_FILE_OPEN = 1;
    private static final int CREATE_FILE_ENCODED = 2;
    private static final int CREATE_FILE_DECODED = 3;
    private static MainActivity instance;
    public ProgressBar decodingProgressBar;
    public TextView nameView;
    public TextView detailsView;
    float uncompressedFileSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        decodingProgressBar = (ProgressBar) findViewById(R.id.loading_indicator);
        nameView = (TextView) findViewById(R.id.name_view);
        detailsView = (TextView) findViewById(R.id.details_view);
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
                selectedFileUri = uri;

                uncompressedFileSize = getFileSize(uri);
                nameView.setText(getFileName(uri));
                float fileSize = uncompressedFileSize;

                detailsView.setText("Original file Size: " + getUnitFileSize(fileSize));

                String mimeType = getContentResolver().getType(uri);
                detailsView.append("\nMIME type: "+mimeType);

                try {
                    String result = readTextFromUri(uri);
                    originalString = result;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }

        if (requestCode == CREATE_FILE_ENCODED
                && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            if (resultData != null) {
                createdFileUri = resultData.getData();
            }
            writeFile(contentToWrite);
        }

        if (requestCode == CREATE_FILE_DECODED
                && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            if (resultData != null) {
                createdFileUri = resultData.getData();
            }
            try {
                ParcelFileDescriptor pfd = getContentResolver().
                        openFileDescriptor(createdFileUri, "w");
                FileOutputStream fileOutputStream =
                        new FileOutputStream(pfd.getFileDescriptor());
                fileOutputStream.write(contentToWrite.getBytes());
                // Let the document provider know you're done by closing the stream.
                fileOutputStream.close();
                pfd.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(this,"File saved successfully!",Toast.LENGTH_SHORT).show();
        }
    }

    public String readTextFromUri(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream =
                     getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;

            boolean flag = false;
            while ((line = reader.readLine()) != null) {
                if (flag) stringBuilder.append("\n");
                stringBuilder.append(line);
                flag = true;
            }
        }
        return stringBuilder.toString();
    }

    public void compress(View view) {
        if(selectedFileUri != null){
            decodingProgressBar.setVisibility(View.VISIBLE);
            h = new Huffman(originalString);
            encodedResult = h.encode();

            contentToWrite = Util.prepareContentToWrite(encodedResult, h.hmapCode);
            createFile();
        }
        else{
            Toast.makeText(this,"Please select a file",Toast.LENGTH_SHORT).show();
        }
    }

    public void writeFile(String content) {

        BitSet bitSet = new BitSet(content.length());
        int bitCounter = 0;
        for (Character c : content.toCharArray()) {
            if (c.equals('1')) {
                bitSet.set(bitCounter);
            }
            bitCounter++;
        }
        byte[] bytes = bitSet.toByteArray();

        try {
            ParcelFileDescriptor pfd = getContentResolver().
                    openFileDescriptor(createdFileUri, "w");
            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write(bytes);
            // Let the document provider know you're done by closing the stream.
            fileOutputStream.close();
            pfd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        detailsView.append("\n\n\nFILE COMPRESSED SUCCESSFULLY!|\n\n");
        float compressedFileSize = getFileSize(createdFileUri);
        detailsView.append("Reduced file Size: "+ getUnitFileSize(compressedFileSize));
        float percent = (uncompressedFileSize-compressedFileSize)/uncompressedFileSize *100;
        detailsView.append("\nReduction percentage: "+String.format("%.02f", percent)+"%");

        decodingProgressBar.setVisibility(View.INVISIBLE);
        selectedFileUri = null;
        Toast.makeText(this,"File saved successfully!",Toast.LENGTH_SHORT).show();
    }

    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "test_encoded.txt");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, (Uri) null);
        }

        startActivityForResult(intent, CREATE_FILE_ENCODED);
    }

    public void decompress(View view) {

        if(selectedFileUri != null){
            decodingProgressBar.setVisibility(View.VISIBLE);

            new SecondThread().execute(String.valueOf(selectedFileUri));
        }
        else{
            Toast.makeText(this,"Please select a file",Toast.LENGTH_SHORT).show();
        }
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

        startActivityForResult(intent, CREATE_FILE_DECODED);

    }
    public float getFileSize(Uri uri){
        Cursor returnCursor =
                getContentResolver().query(uri, null, null, null, null);

        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        float fileSize = returnCursor.getFloat(sizeIndex); // in bytes

        return fileSize;
    }
    public String getFileName(Uri uri){
        Cursor returnCursor =
                getContentResolver().query(uri, null, null, null, null);

        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();

        return returnCursor.getString(nameIndex);
    }
    public String getUnitFileSize(float fileSize){
        String fileSizeString = String.format("%.02f", fileSize) + " Bytes";
        if(fileSize >= 1024){
            fileSize = fileSize/1024;                   // in KB
            fileSizeString = String.format("%.02f", fileSize) + " KB";
        }
        if(fileSize >= 1024){
            fileSize = fileSize/1024;                   // in MB
            fileSizeString = String.format("%.02f", fileSize) + " MB";
        }
        return fileSizeString;
    }

}