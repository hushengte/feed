package com.disciples.feed.web;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractView;

/**
 * Spring-MVC {@link View} that allows for response context to be rendered as the result
 * of downloading a file.
 * 
 * @author Ted Smith
 * @since 2.0.1
 */
public class FileDownloadView extends AbstractView {

    public static final String CONTENT_TYPE_EXCEL = "application/vnd.ms-excel";
    
    private File file;

    /**
     * Create a new {@link FileDownloadView} with content-type
     * @param file The file to download.
     * @param contentType Content-Type of the file.
     */
    protected FileDownloadView(File file, String contentType) {
        Assert.notNull(file, "File must not been null.");
        this.file = file;
        if (contentType != null) {
            setContentType(contentType);
        }
    }
    
    /**
     * Create a new {@link FileDownloadView} with default Content-type: {@link AbstractView#DEFAULT_CONTENT_TYPE}
     * @param file The file to download.
     */
    public static FileDownloadView of(File file) {
        return new FileDownloadView(file, null);
    }
    
    /**
     * Create a new {@link FileDownloadView} with content-type
     * @param file The file to download.
     * @param contentType Content-Type of the file.
     */
    public static FileDownloadView of(File file, String contentType) {
        return new FileDownloadView(file, contentType);
    }
    
    /**
     * Create a new {@link FileDownloadView} with Content-type: {@link MediaType#APPLICATION_OCTET_STREAM_VALUE}
     * @param file The file to download.
     */
    public static FileDownloadView stream(File file) {
        return new FileDownloadView(file, MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }
    
    /**
     * Create a new {@link FileDownloadView} with Content-type: {@link MediaType#TEXT_PLAIN_VALUE}
     * @param file The file to download.
     */
    public static FileDownloadView text(File file) {
        return new FileDownloadView(file, MediaType.TEXT_PLAIN_VALUE);
    }
    
    /**
     * Create a new {@link FileDownloadView} with Content-type: {@link FileDownloadView#CONTENT_TYPE_EXCEL}
     * @param file The file to download.
     */
    public static FileDownloadView excel(File file) {
        return new FileDownloadView(file, CONTENT_TYPE_EXCEL);
    }
    
    /**
     * Create a new {@link FileDownloadView} with Content-type: {@link MediaType#APPLICATION_PDF_VALUE}
     * @param file The file to download.
     */
    public static FileDownloadView pdf(File file) {
        return new FileDownloadView(file, MediaType.APPLICATION_PDF_VALUE);
    }
    
    /**
     * Create a new {@link FileDownloadView} with Content-type: {@link MediaType#IMAGE_JPEG_VALUE}
     * @param file The file to download.
     */
    public static FileDownloadView jpeg(File file) {
        return new FileDownloadView(file, MediaType.IMAGE_JPEG_VALUE);
    }
    
    /**
     * Create a new {@link FileDownloadView} with Content-type: {@link MediaType#IMAGE_GIF_VALUE}
     * @param file The file to download.
     */
    public static FileDownloadView gif(File file) {
        return new FileDownloadView(file, MediaType.IMAGE_GIF_VALUE);
    }
    
    /**
     * Create a new {@link FileDownloadView} with Content-type: {@link MediaType#IMAGE_PNG_VALUE}
     * @param file The file to download.
     */
    public static FileDownloadView png(File file) {
        return new FileDownloadView(file, MediaType.IMAGE_PNG_VALUE);
    }

    @Override
    protected boolean generatesDownloadContent() {
        return true;
    }

    @Override
    protected final void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        String encodedFilename = URLEncoder.encode(file.getName(), "UTF-8");
        String attachmentHeader = String.format("attachment;filename*=UTF-8''%s", encodedFilename);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, attachmentHeader);

        try (FileInputStream input = new FileInputStream(file)) {
            StreamUtils.copy(input, response.getOutputStream());
        }
    }

}
