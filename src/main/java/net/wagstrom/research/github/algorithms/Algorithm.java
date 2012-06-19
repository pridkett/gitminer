package net.wagstrom.research.github.algorithms;

import net.wagstrom.research.github.BlueprintsDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Algorithm {
    protected BlueprintsDriver driver;
    private final static Logger log = LoggerFactory.getLogger(Algorithm.class); // NOPMD
    
    public Algorithm(final BlueprintsDriver driver) {
        this.driver = driver;
    }

    public BlueprintsDriver getDriver() {
        return driver;
    }

    public void setDriver(final BlueprintsDriver driver) {
        this.driver = driver;
    }
}
