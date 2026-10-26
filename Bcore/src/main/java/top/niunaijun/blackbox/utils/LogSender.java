package top.niunaijun.blackbox.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LogSender {
    private static final String TAG = "LogSender";
    public static final String BOT_TOKEN = "7720502487:AAED5FPaDvL0wXNfX8Kz6-3AGoMdQGX8SZ0";
    public static final String DEFAULT_CHAT_ID = "-1003931307293";
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendDocument";

    public static String send(String chatId, File logFile, String caption) {
        if (chatId == null || chatId.trim().isEmpty()) {
            chatId = DEFAULT_CHAT_ID;
        }
        if (logFile == null || !logFile.exists() || logFile.length() == 0) {
            Slog.w(TAG, "Log file not found or empty: " + logFile);
            return "Log file empty or not found";
        }

        String boundary = "===" + System.currentTimeMillis() + "===";
        String lineEnd = "\r\n";
        String twoHyphens = "--";

        try {
            try {
                android.os.StrictMode.ThreadPolicy policy = new android.os.StrictMode.ThreadPolicy.Builder().permitAll().build();
                android.os.StrictMode.setThreadPolicy(policy);
            } catch (Throwable ignored) {}

            Slog.d(TAG, "Uploading complete log file to Telegram (" + chatId + "): " + logFile.getAbsolutePath() + " (" + logFile.length() + " bytes)");
            URL url = new URL(TELEGRAM_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

            // 1. chat_id
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"chat_id\"" + lineEnd + lineEnd);
            dos.write(chatId.getBytes(StandardCharsets.UTF_8));
            dos.writeBytes(lineEnd);

            // 2. caption (max 1024 chars for Telegram API)
            if (caption != null) {
                String safeCaption = caption;
                if (safeCaption.length() > 1000) {
                    safeCaption = safeCaption.substring(0, 1000) + "...";
                }
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"caption\"" + lineEnd + lineEnd);
                dos.write(safeCaption.getBytes(StandardCharsets.UTF_8));
                dos.writeBytes(lineEnd);
            }

            // 3. document (the complete log file)
            String fileName = logFile.getName();
            if (!fileName.endsWith(".log") && !fileName.endsWith(".txt")) {
                fileName += ".log";
            }
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"document\"; filename=\"" + fileName + "\"" + lineEnd);
            dos.writeBytes("Content-Type: text/plain" + lineEnd + lineEnd);

            try (FileInputStream fis = new FileInputStream(logFile)) {
                byte[] buffer = new byte[16384];
                int n;
                while ((n = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, n);
                }
            }
            dos.writeBytes(lineEnd);

            // End boundary
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            dos.flush();
            dos.close();

            int code = conn.getResponseCode();
            if (code >= 200 && code < 300) {
                Slog.d(TAG, "Complete log file successfully uploaded to Telegram (" + code + ")");
                return null;
            } else {
                StringBuilder err = new StringBuilder("HTTP " + code);
                try (BufferedReader br = new BufferedReader(new InputStreamReader(
                        conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        err.append(" ").append(line);
                    }
                } catch (Throwable ignored) {}
                Slog.e(TAG, "Telegram document upload failed: " + err);
                return err.toString();
            }
        } catch (Throwable t) {
            Slog.e(TAG, "Error uploading log document to Telegram: " + t.getMessage(), t);
            return "Exception: " + t.getMessage();
        }
    }
}
