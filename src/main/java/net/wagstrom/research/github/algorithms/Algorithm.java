package net.wagstrom.research.github.algorithms;

import net.wagstrom.research.github.BlueprintsDriver;

public class Algorithm {
    protected BlueprintsDriver driver;
    
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
