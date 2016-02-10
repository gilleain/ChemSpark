package com.lordjoe.distributed.hydra;

import com.lordjoe.distributed.*;
import com.lordjoe.utilities.JarHandler;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * com.lordjoe.distributed.hydra.HydraDeployer
 * User: Steve
 * Date: 10/9/2014
 */
public class HydraDeployer {

    public static void add(String name, File source, JarOutputStream target, Set<String> present) throws IOException {

        if (source.isDirectory()) {
            File[] files = source.listFiles();
            if (files == null)
                return;
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                add(name + "/" + source.getName(), file, target, present);
            }
        } else {
            BufferedInputStream in = null;
            if (present.contains(source.getName()))
                return;
            present.add(source.getName());

            JarEntry entry = new JarEntry(name);
            entry.setTime(source.lastModified());
            target.putNextEntry(entry);

            in = new BufferedInputStream(new FileInputStream(source));
            byte[] buffer = new byte[1024];
            while (true) {
                int count = in.read(buffer);
                if (count == -1)
                    break;
                target.write(buffer, 0, count);
            }
            target.closeEntry();

            if (in != null)
                in.close();
        }
    }


    private static void addCDKFilesToJar(String jarName) {
        File cdkDir = new File("cdkClasses.jar");
        if(!cdkDir.exists())
            throw new IllegalStateException("problem"); // ToDo change

        File existing = new File(jarName);
        File dest = new File(jarName + ".old");
        existing.renameTo(dest);
        try {
            JarHandler jh = new JarHandler(dest) ;
            OutputStream outputStream = new FileOutputStream(jarName);
            JarOutputStream out = new JarOutputStream(outputStream);
            Set<String> added = new HashSet<String>();
            jh.addToJar(out,added);

            jh = new JarHandler(cdkDir) ;
            jh.addToJar(out,added);

            out.close();
            dest.delete();


        } catch (IOException e) {
            throw new RuntimeException(e);

        }


    }

    public static void main(String[] args) {
        SparkDeployer.main(args); // this makes sure we pick up hydra path and dependencies
        addCDKFilesToJar( args[0]);

    }

}
