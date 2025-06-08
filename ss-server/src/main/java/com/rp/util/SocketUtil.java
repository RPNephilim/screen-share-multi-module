package com.rp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class SocketUtil {
    private static final Logger logger = LoggerFactory.getLogger(SocketUtil.class);

    public static boolean isOpen(Object object) throws IOException {
        if (Objects.nonNull(object)) {
            return true;
        }
        logger.info("Resource is closed, returning false");
        return false;
    }
}
