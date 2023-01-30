package com.example.filecompress;

import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.util.HashMap;

public class Util {
    public static String prepareContentToWrite(String encoded, HashMap<Character, String> table) {
        StringBuilder content = new StringBuilder();
        String withLeadingZeros;
        String binaryString;
        for (HashMap.Entry<Character, String> entry : table.entrySet()) {


            char key = Character.valueOf(entry.getKey());
            String value = entry.getValue();

            binaryString = Integer.toBinaryString(key);
            withLeadingZeros = String.format("%8s", binaryString).replace(' ', '0');

            content.append(withLeadingZeros);

            for (int i = 0; i < value.length(); i++) {
                binaryString = Integer.toBinaryString(value.charAt(i));
                withLeadingZeros = String.format("%8s", binaryString).replace(' ', '0');
                content.append(withLeadingZeros);
            }
        }
        //Separator
        content.append("0000000011111111");

        content.append(encoded);

        return String.valueOf(content);

    }

}
