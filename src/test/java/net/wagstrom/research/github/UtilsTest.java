package net.wagstrom.research.github;

import org.junit.Test;
import static org.junit.Assert.*;

import junit.framework.TestCase;

public class UtilsTest extends TestCase {
    @Test
    public void testGravatarHash() {
        assertEquals(Utils.gravatarHash("foo@bar.com"), "f3ada405ce890b6f8204094deb12d8a8");
        assertEquals(Utils.gravatarHash("  foo@BAr.CoM"), "f3ada405ce890b6f8204094deb12d8a8");
    }
    
    @Test
    public void testGravatarIdExtract() {
        assertEquals(Utils.gravatarIdExtract("e3e98bfa99e82ac8b0cb63660dc23b14"),  "e3e98bfa99e82ac8b0cb63660dc23b14");
        assertEquals(Utils.gravatarIdExtract("https://secure.gravatar.com/avatar/ee85853909657f47c8a68e8a9bc7d992?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-140.png"), "ee85853909657f47c8a68e8a9bc7d992");
    }
}
