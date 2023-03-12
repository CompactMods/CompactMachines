package dev.compactmods.machines.forge.util;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

public class VersionUtil {
    public static boolean checkMajor(String version, ArtifactVersion art) {
        try {
            final var v = new DefaultArtifactVersion(version);
            return v.getMajorVersion() >= art.getMajorVersion();
        }

        catch(Exception e) {
            return false;
        }
    }
}
