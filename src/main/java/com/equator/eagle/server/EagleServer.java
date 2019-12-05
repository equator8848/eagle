package com.equator.eagle.server;

import com.equator.eagle.configuration.ServerConfigurationHolder;
import com.equator.eagle.threadpool.DefaultThreadPool;
import com.equator.eagle.threadpool.EagleThreadPool;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.FileSystems;

/**
 * @Author: Equator
 * @Date: 2019/12/4 19:59
 **/

@Slf4j
public class EagleServer implements ServerAction {
    private EagleThreadPool<HttpRequestHandler> requestHandlerEagleThreadPool = new DefaultThreadPool<>();
    private ServerSocket eagleServerSocket = null;
    private boolean isRunning = true;

    public EagleServer() throws IOException {
        this.eagleServerSocket = new ServerSocket(ServerConfigurationHolder.serverPort);
    }

    @Override
    public int start() throws Exception {
        Socket socket = null;
        while (isRunning && (socket = this.eagleServerSocket.accept()) != null) {
            requestHandlerEagleThreadPool.execute(new HttpRequestHandler(socket));
        }
        eagleServerSocket.close();
        requestHandlerEagleThreadPool.shutdown();
        return 0;
    }

    @Override
    public int reload() {
        return 0;
    }

    @Override
    public int stop() {
        return 0;
    }

    class HttpRequestHandler implements Runnable {
        private Socket socket;

        public HttpRequestHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            BufferedReader readerForClient = null;
            BufferedReader readerForServer = null;
            PrintWriter out = null;
            InputStream in = null;
            try {
                readerForClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // 请求头格式 GET /index.html HTTP/1.1
                String header = readerForClient.readLine();
                String path = ServerConfigurationHolder.rootPath + header.split(" ")[1];
                log.debug("path {}", path);
                out = new PrintWriter(socket.getOutputStream());
                if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".ico")) {
                    in = new FileInputStream(path);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    while (in.read(buffer) != -1) {
                        baos.write(buffer);
                    }
                    byte[] content = baos.toByteArray();
                    out.println("HTTP/1.1 200 OK");
                    out.println("Server: Eagle");
                    out.println("Content-Type: text/html;charset=utf-8");
                    out.println("Content-Length: " + content.length);
                    socket.getOutputStream().write(content, 0, content.length);
                } else if (path.endsWith(".html") || path.endsWith(".htm")) {
                    responseHtml(readerForServer, path, out);
                } else {
                    URL file = this.getClass().getResource("/static/50x.html");
                    log.debug("file getPath {}", file.getPath());
                    responseHtml(readerForServer, file.getPath(), out);
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    responseHtml(readerForServer, "X:/IDEAWorkspace/eagle/www/40x.html", out);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } finally {
                close(readerForClient);
            }
        }

        private void responseHtml(BufferedReader readerForServer, String path, PrintWriter out) throws IOException {
            readerForServer = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            out.println("HTTP/1.1 200 OK");
            out.println("Server: Eagle");
            out.println("Content-Type: text/html;charset=utf-8");
            out.println("");
            String line = null;
            while ((line = readerForServer.readLine()) != null) {
                out.println(line);
            }
            out.flush();
        }

        private void close(Closeable... closeables) {
            if (closeables != null) {
                for (Closeable closeable : closeables) {
                    try {
                        closeable.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        EagleServer eagleServer = new EagleServer();
        eagleServer.start();
    }
}
