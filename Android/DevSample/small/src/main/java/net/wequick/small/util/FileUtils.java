/*
 * Copyright 2015-present wequick.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package net.wequick.small.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import net.wequick.small.Small;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * This class consists exclusively of static methods that operate on file.
 */
public final class FileUtils {
    private static final String DOWNLOAD_PATH = "small_patch";

    public interface OnProgressListener {
        void onProgress(int length);
    }

    public static void unZipFolder(File zipFile, String outPath) throws Exception {
        unZipFolder(new FileInputStream(zipFile), outPath, null);
    }

    public static void unZipFolder(InputStream inStream,
                                   String outPath,
                                   OnProgressListener listener) throws Exception {
        ZipInputStream inZip = new ZipInputStream(inStream);
        ZipEntry zipEntry;
        while ((zipEntry = inZip.getNextEntry()) != null) {
            String szName = zipEntry.getName();
            if (szName.startsWith("META-INF")) continue;

            if (zipEntry.isDirectory()) {
                // get the folder name of the widget
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(outPath + File.separator + szName);
                folder.mkdirs();
            } else {
                File file = new File(outPath + File.separator + szName);
                if (!file.createNewFile()) {
                    System.err.println("Failed to create file: " + file);
                    return;
                }
                // get the output stream of the file
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024];
                // read (len) bytes into buffer
                while ((len = inZip.read(buffer)) != -1) {
                    // write (len) byte from buffer at the position 0
                    out.write(buffer, 0, len);
                    out.flush();
                    if (listener != null) {
                        listener.onProgress(len);
                    }
                }
                out.close();
            }
        }
        inZip.close();
    }

    public static File getInternalFilesPath(String dir) {
        File file = Small.getContext().getDir(dir, Context.MODE_PRIVATE);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    public static File getDownloadBundlePath() {
        return getInternalFilesPath(DOWNLOAD_PATH);
    }

    public static void fillSoFile(String file, File path) {
        StringBuffer soPath1 = new StringBuffer("lib/");
        StringBuffer soPath2 = new StringBuffer("lib/");
        if (Build.VERSION.SDK_INT >= 21) {
            String[] abis =  Build.SUPPORTED_ABIS;
            soPath1.append(abis[0]);
            soPath2.append(abis[1]);
        } else {
            soPath1.append(Build.CPU_ABI);
            soPath2.append(Build.CPU_ABI2);
        }
        boolean isSuccess = copySo(file, path, soPath1.toString());
        if(!isSuccess) copySo(file, path, soPath2.toString());
    }

    public static boolean copySo(String file, File path, String soPath) {
        Log.d("copySo-before", System.currentTimeMillis()+"");
        boolean isSuccess = false;
        try {
            ZipFile zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry zipEntry =  enumeration.nextElement();
                String zipEntryName = zipEntry.getName();
                if(zipEntryName.startsWith(soPath) && zipEntryName.endsWith(".so")) {
                    String soName = zipEntryName.substring(zipEntryName.lastIndexOf("/") + 1);
                    Log.d("soname", soName);
                    if(!path.exists()) path.mkdirs();
                    File soFile = new File(path,soName);
                    FileOutputStream out = new FileOutputStream(soFile);
                    InputStream is = zipFile.getInputStream(zipEntry);
                    int len;
                    byte[] buffer = new byte[1024];
                    while ((len = is.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                        out.flush();
                    }
                    out.close();
                    is.close();
                    isSuccess = true;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("copySo-after", System.currentTimeMillis()+"");
        return isSuccess;
    }
}
