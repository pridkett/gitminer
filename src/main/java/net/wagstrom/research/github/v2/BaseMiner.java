package net.wagstrom.research.github.v2;

/**
 * @author Patrick Wagstrom <patrick@wagstrom.net>
 * @deprecated
 */
public abstract class BaseMiner {

    /**
     * Splits a project name
     * 
     * @param proj
     * @return
     */
    protected String[] projsplit(String proj) {
        String [] projsplit = proj.split("/");
        return projsplit;
    }

}
