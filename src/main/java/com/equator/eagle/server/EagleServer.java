package com.equator.eagle.server;

import com.equator.eagle.configuration.ServerConfigurationHolder;
import sun.nio.ch.ThreadPool;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Author: Equator
 * @Date: 2019/12/4 19:59
 **/

public class EagleServer implements ServerAction {
    private ServerSocket eagleServer = null;

    private EagleServer() throws IOException {
        this.eagleServer = new ServerSocket(ServerConfigurationHolder.serverPort);
    }

    @Override
    public int start() throws Exception {
        Socket socket = null;
        while ((socket = this.eagleServer.accept()) != null) {

        }
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
}
