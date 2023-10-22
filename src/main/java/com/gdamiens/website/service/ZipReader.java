package com.gdamiens.website.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipInputStream;

public class ZipReader {


    public ZipReader() {
    }

    public ZipInputStream readFromUrl(String link) throws IOException {
        URL url = new URL(link);
        InputStream inputStream = url.openStream();

        return new ZipInputStream(inputStream);
    }
}
