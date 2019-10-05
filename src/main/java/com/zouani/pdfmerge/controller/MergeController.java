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
    private static final String UPLOADED_FOLDER = "\\tmp_merge";

    @RequestMapping(value = "uploadMultipleFiles", method = RequestMethod.POST)
    public String uploadMultipleFiles(@RequestParam("file") MultipartFile[] files)throws IOException {

        String status = "";
        File dir = new File(UPLOADED_FOLDER);
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            
            try {
                byte[] bytes = file.getBytes();

                if (!dir.exists())
                    dir.mkdirs();

                File uploadFile = new File(dir.getAbsolutePath()
                        + File.separator + file.getOriginalFilename());
                BufferedOutputStream outputStream = new BufferedOutputStream(
                        new FileOutputStream(uploadFile));
                outputStream.write(bytes);
                outputStream.close();

                status = status + "You successfully uploaded file=" + file.getOriginalFilename();
            } catch (Exception e) {
                status = status + "Failed to upload " + file.getOriginalFilename()+ " " + e.getMessage();
            }
        }
        System.out.println(dir.getAbsolutePath());
        File[] fills = dir.listFiles();
        File mergedFile = File.createTempFile("merged_pdf", ".pdf");
        PDFMergerUtility PDFmerger = new PDFMergerUtility();
        PDFmerger.setDestinationFileName(mergedFile.getAbsolutePath());
        for (File fill : fills) {
            System.out.println(fill.getAbsolutePath());
            PDDocument doc = PDDocument.load(fill);
            PDFmerger.addSource(fill);
            doc.close();
        }
         PDFmerger.mergeDocuments();
        System.out.println(mergedFile.getAbsolutePath());
        merged = mergedFile;
        dir.delete();

        return "download";
        
    }

//    @RequestMapping(value = "download")
//    public String index(Model model) {
//        model.addAttribute("title", "download");
//        return "download";
//    }
    @RequestMapping(value = "upload")
    public String upload(Model model) {
        return "upload";
    }

//    @RequestMapping(value = "merge", method = RequestMethod.POST)
//    public String showFiles(@RequestParam List<String> pdfs) throws IOException {
//        for (String file : pdfs) {
//            System.out.println("haaa l origine => " + file);
//        }
//    
//        return "test";
//       File mergedFile = File.createTempFile("merged_pdf", ".pdf"); 
//       PDFMergerUtility PDFmerger = new PDFMergerUtility();
//       PDFmerger.setDestinationFileName(mergedFile.getAbsolutePath());
//       
////        for (File file : pdfs) {
////            System.out.println("haaa l origine"+file.getAbsolutePath());
////        }
//
////        for (File file : pdfs) {
////            File filee = new File(file.getAbsolutePath());
//            
//            PDDocument doc = PDDocument.load(pdfs.getResource().getFile());
//            PDFmerger.addSource(pdfs.getResource().getFile());
//            doc.close();
////        }
//        
//        PDFmerger.mergeDocuments();
//        System.out.println(mergedFile.getAbsolutePath());
//        merged = mergedFile;
//
//        return "redirect:\\download";
//    }
    @RequestMapping(value = "download", method = RequestMethod.POST)
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
}
