package org.dave.compactmachines3.utility;

import com.google.common.io.ByteStreams;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.io.FileUtils;
import org.dave.compactmachines3.CompactMachines3;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarExtract {
    public static final boolean DEVELOPMENT = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

    public static int copy(String src, File dst) {
        if(DEVELOPMENT) {
            CompactMachines3.logger.info("Running in a development environment, copying '{}' from assets folder", src);
            File srcFile = new File(CompactMachines3.class.getResource("/" + src).getFile());
            try {
                FileUtils.copyDirectory(srcFile, dst);
            } catch(IOException e) {
                CompactMachines3.logger.error("Error while copy files from development environment:");
                e.printStackTrace();
            }
        } else {
            return extract(src, dst.getPath());
        }

        return -1;
    }

    private static int extract(String src, String dst) {
        URL srcUrl = CompactMachines3.class.getResource("/" + src);
        if(srcUrl == null || !srcUrl.getProtocol().equals("jar")) {
            CompactMachines3.logger.error("Error while extracting files from jar: unable to get Resource URL for '{}'.", src);
            return 0;
        }

        int count = 0;
        try {
            JarURLConnection jarURLConnection = (JarURLConnection) srcUrl.openConnection();
            ZipFile zipFile = jarURLConnection.getJarFile();
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

            while(zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                String zipName = zipEntry.getName();

                if(!zipName.startsWith(src)) {
                    continue;
                }

                File dstFile = new File(dst + File.separator + zipName.substring(src.length()));

                if(zipEntry.isDirectory()) {
                    dstFile.mkdir();
                } else {
                    InputStream inputStream = zipFile.getInputStream(zipEntry);
                    OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(dstFile));
                    ByteStreams.copy(inputStream, outputStream);
                    inputStream.close();
                    outputStream.close();
                    count++;
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        return count;
    }
}
