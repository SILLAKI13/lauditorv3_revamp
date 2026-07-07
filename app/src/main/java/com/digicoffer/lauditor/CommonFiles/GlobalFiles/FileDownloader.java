package com.digicoffer.lauditor.CommonFiles.GlobalFiles;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class FileDownloader {

    // ── Primary entry point — use when you have filename + mime from the API ──
    public static void downloadFile(Context context,
                                    String fileUrl,
                                    String fileName,
                                    String mimeType) {
        try {
            // Clean filename — only strip truly illegal filesystem characters,
            // keep spaces, dots, parentheses so the name stays readable.
            if (fileName == null || fileName.trim().isEmpty()) {
                fileName = "downloaded_file";
            }
            fileName = fileName.trim().replaceAll("[\\\\/:*?\"<>|]", "_");

            DownloadManager.Request request =
                    new DownloadManager.Request(Uri.parse(fileUrl));

            request.setTitle(fileName);
            request.setDescription("Downloading...");

            // VISIBILITY_VISIBLE    → shows progress notification while downloading
            // VISIBILITY_VISIBLE_NOTIFY_COMPLETED → shows "Download complete" when done
            request.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE |
                            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            // Save to the public Downloads folder (visible in Files app)
            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, fileName);

            // Allow on both WiFi and mobile data
            request.setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI |
                            DownloadManager.Request.NETWORK_MOBILE);

            // Allow download even on metered/roaming connections
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(true);

            // DO NOT add Authorization header — S3 pre-signed URLs already
            // carry auth in X-Amz-Signature query param. Adding Bearer causes 403.

            // Set MIME so the OS knows how to open the file after download
            if (mimeType == null || mimeType.isEmpty()) {
                mimeType = guessMimeFromFileName(fileName);
            }
            if (mimeType != null && !mimeType.isEmpty()) {
                request.setMimeType(mimeType);
            }

            DownloadManager dm =
                    (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm != null) {
                dm.enqueue(request);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Legacy entry point — when you only have the URL (parses filename from it) ──
    public static void downloadFile(Context context,
                                    String fileUrl,
                                    String fileName) {
        // Guess MIME
        String extension = MimeTypeMap.getFileExtensionFromUrl(fileName);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
//        if (mimeType != null) {
//            request.setMimeType(mimeType);
//        }
//        String fileName = getFileNameFromUrl(fileUrl);
//        if (fileName == null || fileName.isEmpty()) {
//            fileName = fallbackFileName;
//        }
//        // Derive mime from whatever filename we resolved
//        String mimeType = guessMimeFromFileName(fileName);
        downloadFile(context, fileUrl, fileName, mimeType);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private static String guessMimeFromFileName(String fileName) {
        if (fileName == null) return null;
        // MimeTypeMap needs just the extension, with no spaces or query junk
        int dot = fileName.lastIndexOf('.');
        if (dot == -1 || dot == fileName.length() - 1) return null;
        String ext = fileName.substring(dot + 1).toLowerCase().trim();
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
    }

    private static String getFileNameFromUrl(String url) {
        try {
            Uri uri = Uri.parse(url);

            // 1. Try Content-Disposition query param (S3 pre-signed URLs carry this)
            String disposition = uri.getQueryParameter("response-content-disposition");
            if (disposition != null) {
                disposition = URLDecoder.decode(disposition, "UTF-8");

                // RFC 5987 extended form:  filename*=UTF-8''foo%20bar.pdf
                if (disposition.contains("filename*=")) {
                    int idx = disposition.indexOf("filename*=") + "filename*=".length();
                    String val = disposition.substring(idx).trim();
                    if (val.contains("''")) {
                        val = val.substring(val.indexOf("''") + 2);
                    }
                    val = URLDecoder.decode(val.replace("\"", "").trim(), "UTF-8");
                    if (val.contains(";")) val = val.substring(0, val.indexOf(";")).trim();
                    if (!val.isEmpty()) return val;
                }

                // Standard form:  filename="foo bar.pdf"  or  filename=foo.pdf
                if (disposition.contains("filename=")) {
                    int idx = disposition.indexOf("filename=") + "filename=".length();
                    String val = disposition.substring(idx)
                            .replace("\"", "").trim();
                    if (val.contains(";")) val = val.substring(0, val.indexOf(";")).trim();
                    if (!val.isEmpty()) return val;
                }
            }

            // 2. Fallback: last path segment before query string
            String path = uri.getPath();
            if (path != null && path.contains("/")) {
                String segment = path.substring(path.lastIndexOf('/') + 1);
                if (!segment.isEmpty()) {
                    return URLDecoder.decode(segment, "UTF-8");
                }
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}