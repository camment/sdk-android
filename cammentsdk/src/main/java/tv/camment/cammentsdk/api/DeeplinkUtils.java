package tv.camment.cammentsdk.api;


import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

final class DeeplinkUtils {

    static String calculateMD5(String in) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(in.getBytes());
            byte[] a = digest.digest();
            int len = a.length;
            StringBuilder sb = new StringBuilder(len << 1);
            for (byte anA : a) {
                sb.append(Character.forDigit((anA & 0xf0) >> 4, 16));
                sb.append(Character.forDigit(anA & 0x0f, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    static String getMyExternalIP() {
        try (Scanner s = new Scanner(new URL("https://api.ipify.org").openStream(), "UTF-8").useDelimiter("\\A")) {
            return s.next();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
