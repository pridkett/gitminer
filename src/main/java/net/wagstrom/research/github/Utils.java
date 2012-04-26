package net.wagstrom.research.github;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);  // NOPMD
    private static final Pattern GRAVATAR_PATTERN = Pattern.compile("([a-f0-9]{32})");

    /**
     * takes an email address and creates the appropriate gravatar hash
     * 
     * @param email
     * @return
     */
    public static String gravatarHash(final String email) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(email.trim().toLowerCase().getBytes("UTF-8"));
            return new BigInteger(1, digest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            log.error("No such algorithm MD5", e);
        } catch (UnsupportedEncodingException e) {
            log.error("Error decoding from UTF-8", e);
        }
        return null;
    }
    
    /**
     * Given a string attempts to return the gravatar id
     * 
     * gravatarId's can be problematic as they can be either of:
     * https://secure.gravatar.com/avatar/ee85853909657f47c8a68e8a9bc7d992?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-140.png
     * e3e98bfa99e82ac8b0cb63660dc23b14
     *
     * This really just looks for the first 32 digit long hex string
     * 
     * @param gravatarId
     * @return
     */
    public static String gravatarIdExtract(final String gravatarId) {
        Matcher matcher = GRAVATAR_PATTERN.matcher(gravatarId);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }
}
