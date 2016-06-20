package com.opensource.svgaplayer;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

/**
 * Created by PonyCui_Home on 16/6/18.
 */
public class SVGAParser {

    public SVGAVideoEntity parse(InputStream inputStream) throws Exception {
        try {
            GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int size;
            byte[] buffer = new byte[2048];
            while ((size = gzipInputStream.read(buffer, 0, buffer.length)) != -1) {
                byteArrayOutputStream.write(buffer, 0, size);
            }
            String JSONString = byteArrayOutputStream.toString();
            JSONObject obj = new JSONObject(JSONString);

            SVGAVideoEntity videoItem = new SVGAVideoEntity(obj);
            videoItem.resetImages(obj);
            videoItem.resetSprites(obj);
            return videoItem;
        } catch (Exception e) {
            throw e;
        }
    }

    public SVGAVideoEntity parse(byte[] data) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        try {
            return parse(inputStream);
        } catch (Exception e) {
            throw e;
        }
    }

}
