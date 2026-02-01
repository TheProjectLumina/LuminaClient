package org.cloudburstmc.netty.util;

import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class SecureAlgorithmProvider {
    private static final String SECURITY_ALGORITHM;

    static {
        // SecureRandom algorithms in order of most preferred to least preferred.
        final List<String> preferredAlgorithms = Arrays.asList(
            "SHA1PRNG",
            "NativePRNGNonBlocking",
            "Windows-PRNG",
            "NativePRNG",
            "PKCS11",
            "DRBG",
            "NativePRNGBlocking"
        );

        SECURITY_ALGORITHM = Stream.of(Security.getProviders())
            .flatMap(provider -> provider.getServices().stream())
            .filter(service -> "SecureRandom".equals(service.getType()))
            .map(Provider.Service::getAlgorithm)
            .filter(preferredAlgorithms::contains)
            .min((s1, s2) -> Integer.compare(preferredAlgorithms.indexOf(s1), preferredAlgorithms.indexOf(s2)))
            .orElse(new SecureRandom().getAlgorithm());
    }

    public static String getSecurityAlgorithm() {
        return SECURITY_ALGORITHM;
    }
}
