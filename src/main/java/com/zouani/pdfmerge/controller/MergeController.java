/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zouani.pdfmerge.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author ismail
 */
@Controller
public class MergeController {

    private static File merged;
    private static String UPLOADED_FOLDER = "\\tmp_merge" + System.currentTimeMillis();

    @RequestMapping(value = "merge", method = RequestMethod.POST)
    public ResponseEntity<Object> uploadMultipleFiles(@RequestParam("file") MultipartFile[] files, Model model) throws IOException {
        //_______________________Upload All Files________________________________
        File dir = new File(UPLOADED_FOLDER);
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            byte[] bytes = file.getBytes();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File uploadFile = new File(dir.getAbsolutePath()
                    + File.separator + file.getOriginalFilename());
            BufferedOutputStream outputStream = new BufferedOutputStream(
                    new FileOutputStream(uploadFile));
            outputStream.write(bytes);
            outputStream.close();
        //______________________Rename Each File_________________________________

            File newFile = new File(uploadFile.getParent(), "new-file-" + i + ".pdf");
            Files.move(uploadFile.toPath(), newFile.toPath());
        }
        //_____________merge files from uploaded dir
        mergePdfsFromDir(dir);
        //________________remove uploaded dir___________
        removeDirWithContent(dir);
        //model.addAttribute("isHidden", "false");
        return download();
//        return "upload";
    }

    @RequestMapping(value = "upload")
    public String upload(Model model) {
        model.addAttribute("isHidden", "true");
        return "upload";
    }

    //@RequestMapping(value = "download", method = RequestMethod.POST)
    public ResponseEntity<Object> download() throws IOException {
        InputStreamResource resource = new InputStreamResource(new FileInputStream(merged));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", merged.getName()));
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().headers(headers).contentLength(merged.length()).contentType(MediaType.parseMediaType("application/pdf")).body(resource);

        return responseEntity;
    }

    private Boolean removeDirWithContent(File dir) {
        String[] entries = dir.list();
        for (String s : entries) {
            File currentFile = new File(dir.getPath(), s);
            currentFile.delete();
        }
        return dir.delete();
    }

    private void mergePdfsFromDir(File dir) throws IOException {
        File[] fills = dir.listFiles();
        File mergedFile = File.createTempFile("merged_pdf", ".pdf");
        PDFMergerUtility PDFmerger = new PDFMergerUtility();
        PDFmerger.setDestinationFileName(mergedFile.getAbsolutePath());
        for (File fill : fills) {
            PDDocument doc = PDDocument.load(fill);
            PDFmerger.addSource(fill);
            doc.close();
        }
        PDFmerger.mergeDocuments();
        merged = mergedFile;
    }
}
