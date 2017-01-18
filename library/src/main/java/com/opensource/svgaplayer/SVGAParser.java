package com.opensource.svgaplayer;

import android.content.Context;
import android.support.v4.util.LruCache;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by PonyCui_Home on 16/6/18.
 */
public class SVGAParser {

    private Context mContext;

    public SVGAParser(Context context) {
        this.mContext = context;
    }

    public SVGAVideoEntity parse(URL url) throws Exception {
        if (cacheDir(cacheKey(url)).exists()) {
            return parse(null, cacheKey(url));
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(20 * 1000);
            conn.setRequestMethod("GET");
            conn.connect();
            return parse(conn.getInputStream(), cacheKey(url));
        } catch (Exception e) {
            throw e;
        }
    }

    public SVGAVideoEntity parse(InputStream inputStream, String cacheKey) throws Exception {
        if (!cacheDir(cacheKey).exists()) {
            unzip(inputStream, cacheKey);
        }
        final File cacheDir = new File(this.mContext.getCacheDir().getAbsolutePath() + "/" + cacheKey + "/");
        final File jsonFile = new File(cacheDir, "movie.spec");
        FileInputStream fileInputStream = new FileInputStream(jsonFile);
        SVGAVideoEntity videoItem = null;
        try {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int size;
            byte[] buffer = new byte[2048];
            while ((size = fileInputStream.read(buffer, 0, buffer.length)) != -1) {
                byteArrayOutputStream.write(buffer, 0, size);
            }
            String JSONString = byteArrayOutputStream.toString();
            JSONObject obj = new JSONObject(JSONString);
            videoItem = new SVGAVideoEntity(obj, cacheDir);
            videoItem.resetImages(obj);
            videoItem.resetSprites(obj);
        } finally {
            fileInputStream.close();
        }
        return videoItem;
    }

    private String cacheKey(URL url) throws Exception {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(url.toString().getBytes("UTF-8"));
            byte[] digest = messageDigest.digest();
            StringBuffer sb = new StringBuffer();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw e;
        }
    }

    private File cacheDir(String cacheKey) {
        return new File(this.mContext.getCacheDir().getAbsolutePath() + "/" + cacheKey + "/");
    }

    private void unzip(InputStream inputStream, String cacheKey) throws Exception {
        File cacheDir = this.cacheDir(cacheKey);
        cacheDir.mkdirs();
        try {
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));
            ZipEntry zipItem;
            while ((zipItem = zipInputStream.getNextEntry()) != null) {
                File file = new File(cacheDir, zipItem.getName());
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                byte buff[] = new byte[2048];
                int readBytes;
                while ((readBytes = zipInputStream.read(buff)) > 0) {
                    fileOutputStream.write(buff, 0, readBytes);
                }
                fileOutputStream.close();
                zipInputStream.closeEntry();
            }
            zipInputStream.close();
        } catch (Exception e) {
            throw e;
        }
    }

}