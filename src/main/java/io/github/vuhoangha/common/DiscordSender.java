package io.github.vuhoangha.common;

import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class DiscordSender {

    private final int timeout;
    private final URL url;

    private final BlockingQueue<String> messageQueue;
    private final ExecutorService executorService;
    private final AtomicBoolean isSending = new AtomicBoolean(false);

    private static DiscordSender instance;


    private DiscordSender(String webhookUrl, int timeout, int queueCapacity, ExecutorService executorService) throws MalformedURLException {
        this.timeout = timeout;
        this.url = new URL(webhookUrl);
        this.messageQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.executorService = executorService;
    }


    private void _send(String message) {
        messageQueue.offer(message);
        if (isSending.compareAndSet(false, true)) {
            executorService.submit(this::_process);
        }
    }

    private void _process() {
        while (!messageQueue.isEmpty()) {
            try {
                String message = messageQueue.poll();
                if (message != null)
                    _sendToDiscord(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Message processing thread interrupted");
                break;
            } catch (Exception e) {
                log.error("Error sending message to Discord: {}", e.getMessage());
            }
        }
        isSending.set(false);
    }

    private void _sendToDiscord(String message) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
            connection.setDoOutput(true);

            String jsonPayload = String.format("{\"content\":\"%s\"}", message);
            byte[] outputBytes = jsonPayload.getBytes(StandardCharsets.UTF_8);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(outputBytes);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 200 && responseCode != 204) {
                throw new Exception("Failed to send message to Discord. Response code: " + responseCode);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    private void _shutdown() {
        executorService.shutdownNow();
        try {
            if (!executorService.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                log.error("Executor did not terminate in the specified time.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Shutdown interrupted");
        }
    }


    public static synchronized DiscordSender init(String webhookUrl, int timeout, int queueCapacity, ExecutorService executorService) throws MalformedURLException {
        if (instance == null) {
            instance = new DiscordSender(webhookUrl, timeout, queueCapacity, executorService);
        }
        return instance;
    }

    public static void send(String message) {
        instance._send(message);
    }

    public static String send(String message, Object... arguments) {
        String wrapMsg = MessageFormat.format(message, arguments);
        instance._send(wrapMsg);
        return wrapMsg;
    }

    public static void error(String message) {
        instance._send(message);
        log.error(message);
    }

    public static String error(String message, Object... arguments) {
        String wrapMsg = send(message, arguments);
        log.error(wrapMsg);
        return wrapMsg;
    }

    public static String error(Throwable ex, String message, Object... arguments) {
        String wrapMsg = send(message, arguments);
        log.error(wrapMsg, ex);
        return wrapMsg;
    }


    public static void shutdown() {
        instance._shutdown();
    }

}
