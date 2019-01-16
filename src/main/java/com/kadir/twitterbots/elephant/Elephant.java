package com.kadir.twitterbots.elephant;

import com.kadir.twitterbots.elephant.importer.Importer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author akadir
 * Date: 16/01/2019
 * Time: 19:49
 */
public class Elephant {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) {
        Elephant elephant = new Elephant();
        elephant.start();
    }

    private void start() {
        Importer importer = new Importer();
        logger.info("start import");
        importer.run();
    }
}
