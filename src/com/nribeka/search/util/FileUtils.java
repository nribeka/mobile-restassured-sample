package com.nribeka.search.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Static methods used for common file operations.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class FileUtils {

    private final static String t = "FileUtils";

    // Used to validate and display valid form names.
    public static final String VALID_FILENAME = "[ _\\-A-Za-z0-9]*.x[ht]*ml";

    // Storage paths
    public static final String ODK_CLINIC_ROOT = Environment.getExternalStorageDirectory() + "/odk/clinic/";

    public static final String FORMS_PATH = ODK_CLINIC_ROOT + "forms/";

    public static final String INSTANCES_PATH = ODK_CLINIC_ROOT + "instances/";

    public static final String DATABASE_PATH = ODK_CLINIC_ROOT + "databases/";


    public static boolean storageReady() {
        String cardstatus = Environment.getExternalStorageState();
        if (cardstatus.equals(Environment.MEDIA_REMOVED)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTED)
                || cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)
                || cardstatus.equals(Environment.MEDIA_SHARED)) {
            return false;
        } else {
            return true;
        }
    }
}

