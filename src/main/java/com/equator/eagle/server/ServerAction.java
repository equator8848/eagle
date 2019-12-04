package com.equator.eagle.server;

import java.io.IOException;

/**
 * @Author: Equator
 * @Date: 2019/12/4 20:09
 **/

public interface ServerAction {
    int start() throws Exception;

    int reload() throws Exception;

    int stop() throws Exception;
}
