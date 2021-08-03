package com.docshifter.core.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Julian Isaac on 29.07.2021
 */
@Log4j2
public class NetworkUtils {

    private static final String[] HOSTNAME_ENV_VARS = {
            "COMPUTERNAME", // Windows
            "HOSTNAME" // Unix
    };

    /**
     *
     * {@link InetAddress#getLocalHost()}{@link InetAddress#getHostName() .getHostName()} is notoriously known for
     * erroring out or returning wrong values, especially in virtualized or cloud environments. This first checks
     * the result of that method, and if we get an error or "localhost" back we alternatively try resolving
     * well-known OS-specific environment variables to see if they yield a better result.
     * @see <a href="https://stackoverflow.com/questions/7348711/recommended-way-to-get-hostname-in-java">This entire StackOverflow discussion about this topic.</a>
     * @return Either a specific hostname or "localhost". Never returns null or an empty value
     */
    public static String getLocalHostName() {
        String origHostname = "localhost";
        try {
            origHostname = InetAddress.getLocalHost().getHostName();
            // Try to get a more specific name first if this returned localhost
            if (StringUtils.isNotBlank(origHostname) && !origHostname.equalsIgnoreCase("localhost")) {
                return origHostname;
            }
        }
        catch (UnknownHostException uhe) {
            log.warn("We weren't able to get a proper hostname, so will default to localhost", uhe);
        }

        for (String envVar : HOSTNAME_ENV_VARS) {
            String hostname = System.getenv(envVar);
            if (StringUtils.isNotBlank(hostname)) {
                return hostname;
            }
        }
        return origHostname;
    }
}
