package com.remal.portfolio.picocli.provider;

import picocli.CommandLine;

import java.io.IOException;
import java.util.jar.Manifest;

/**
 * Picocli version provider implementation that reads build info from the
 * MANIFEST.MF file.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class ManifestVersionProvider implements CommandLine.IVersionProvider {

    /**
     * Builds version info.
     *
     * @return an array that contains details about the build
     * @throws Exception throws in case of error
     */
    @Override
    public String[] getVersion() throws Exception {
        var resources = CommandLine.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
        var url = resources.nextElement();
        try {
            var manifest = new Manifest(url.openStream());
            var attributes = manifest.getMainAttributes();
            var applicationVersion = attributes.getValue("Application-Version");
            var createdBy = attributes.getValue("Created-By");
            var buildTimestamp = attributes.getValue("Build-Timestamp");
            return new String[] {
                "version: " + applicationVersion,
                "created by " + createdBy,
                "build time: " + buildTimestamp,
                "Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved"
            };
        } catch (IOException ex) {
            return new String[] { "Unable to read from " + url + ": " + ex };
        }
    }
}
