package com.equator.eagle.server;

import java.net.Socket;

/**
 * @Author: Equator
 * @Date: 2019/12/4 20:11
 **/

public class HttpRequestHandler implements Runnable {
    private Socket socket;

    public HttpRequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

    }
}
