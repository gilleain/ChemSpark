package com.lordjoe.utilities;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

/**
 * com.lordjoe.utilities.JarHandler
 * User: Steve
 * Date: 2/9/2016
 */
public class JarHandler {
    public static final JarHandler[] EMPTY_ARRAY = {};


    private final JarInputStream inp;
    private boolean closed;

    public JarHandler(final File pInp) throws IOException {
        this(new FileInputStream(pInp));

    }

    public JarHandler(InputStream is) throws IOException {
        this(new JarInputStream(is));
    }

    public JarHandler(final JarInputStream pInp) {
        inp = pInp;
    }

    public boolean isClosed() {
        return closed;
    }

    private void setClosed(final boolean pClosed) {
        closed = pClosed;
    }

    public void close() {
        if (!isClosed()) {
            try {
                inp.close();
            } catch (IOException e) {
            }
            setClosed(true);
        }
    }

    public void unzipTo(File targetFolder) {
        if (isClosed())
            throw new IllegalStateException("Handler Closed");
        if (!targetFolder.exists()) {
            if (!targetFolder.mkdirs())
                throw new IllegalStateException("cannot create targetFolder " + targetFolder.getAbsolutePath());
        }

        try {
            ZipInputStream zipInputStream = inp;
            ZipEntry zipEntry = null;

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                File zipEntryFile = new File(targetFolder, zipEntry.getName());


                if (zipEntry.isDirectory()) {
                    if (!zipEntryFile.exists() && !zipEntryFile.mkdirs())
                        throw new IllegalStateException("cannot make entry");

                } else {
                    FileOutputStream fileOutputStream = new FileOutputStream(zipEntryFile);

                    byte buffer[] = new byte[1024];
                    int count;

                    while ((count = zipInputStream.read(buffer, 0, buffer.length)) != -1)
                        fileOutputStream.write(buffer, 0, count);

                    fileOutputStream.flush();
                    fileOutputStream.close();
                    zipInputStream.closeEntry();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

        close();

        // post-stuff
    }


    private void add(String name, JarEntry source, JarOutputStream target, Set<String> present) throws IOException {
        if (present.contains(source.getName()))
            return;
        present.add(source.getName());

            JarEntry entry = new JarEntry(source);
            entry.setTime(source.getTime());
            target.putNextEntry(entry);



            byte[] buffer = new byte[1024];
            while (true) {
                int count = inp.read(buffer);
                if (count == -1)
                    break;
                target.write(buffer, 0, count);
            }
            inp.closeEntry();
            target.closeEntry();

    }





    /**
     * return a set of entry names - used to prevent overwriing
     *
     * @param jarFile
     * @return
     */
    public static Set<String> getEntryNames(File jarFile) {
        Set<String> ret = new HashSet<String>();
        try {
            JarInputStream is = new JarInputStream(new FileInputStream(jarFile));
            JarEntry en = is.getNextJarEntry();
            while (en != null) {
                ret.add(en.getName());
                en = is.getNextJarEntry();
            }
            is.close();
            return ret;
        } catch (IOException e1) {
            throw new RuntimeException(e1);

        }
    }

    public void addToJar(JarOutputStream out, Set<String> added) {
        try {
            JarEntry current = inp.getNextJarEntry();
            while(current != null)    {
                add(current.getName(), current, out, added);
                current = inp.getNextJarEntry();
            }
            close();
           } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

//    public void unzipTo(JarOutputStream ot,Set<String> written  ) {
//
//            if (isClosed())
//                throw new IllegalStateException("Handler Closed");
//            String name = "/";
//
//            try {
//                ZipInputStream zipInputStream = inp;
//                ZipEntry zipEntry = null;
//
//                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
//                    add(zipEntry.getName(),inp,ot,)
//                        FileOutputStream fileOutputStream = new FileOutputStream(zipEntryFile);
//
//                        byte buffer[] = new byte[1024];
//                        int count;
//
//                        while ((count = zipInputStream.read(buffer, 0, buffer.length)) != -1)
//                            fileOutputStream.write(buffer, 0, count);
//
//                        fileOutputStream.flush();
//                        fileOutputStream.close();
//                        zipInputStream.closeEntry();
//                    }
//                }
//            }
//            catch (IOException e) {
//                throw new RuntimeException(e);
//
//            }
//
//            close();
//
//            // post-stuff
//        }

}
