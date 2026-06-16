package org.example.yci_web.Utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ConvertByteToBase64 {
    private static final int MAX_BASE64_LENGTH = 5 * 1024 * 1024; // 5MB

    public static String toBase64(byte[] image) {

        String base64Image = null;

        if (image != null && image.length > 0) {

            try {

                if (isAlreadyBase64(image)) {
                    base64Image = new String(
                            image,
                            StandardCharsets.UTF_8
                    );
                } else {
                    base64Image = Base64.getEncoder()
                            .encodeToString(image);
                }

                if (isBase64TooLarge(base64Image)) {
                    base64Image = compressAndEncodeImage(image);
                }

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        return base64Image;
    }

    private static boolean isAlreadyBase64(byte[] data) {

        try {

            String str = new String(
                    data,
                    StandardCharsets.UTF_8
            );

            Base64.getDecoder().decode(str);

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    private static boolean isBase64TooLarge(String base64) {

        return base64 != null
                && base64.length() > MAX_BASE64_LENGTH;
    }

    private static String compressAndEncodeImage(byte[] image) {

        return Base64.getEncoder()
                .encodeToString(image);
    }
}
