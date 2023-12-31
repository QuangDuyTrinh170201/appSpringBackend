package com.example.shopapp.controllers;

import com.example.shopapp.dtos.ProductDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/products")
public class ProductController {
    @GetMapping("")
    public ResponseEntity<String> getProducts(
            @RequestParam("page") int page,
            @RequestParam("limit") int limit
    ){
        return ResponseEntity.ok("Get products here");
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getProductById(@PathVariable("id")String productId){
        return ResponseEntity.ok("Product with ID: "+ productId);
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @Valid @ModelAttribute ProductDTO productDTO,
            BindingResult result
    ){
        try{
            if(result.hasErrors()){
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages);
            }
            List<MultipartFile> files = productDTO.getFiles();

            //kiểm tra nếu giá trị file là rỗng
            files = files == null ? new ArrayList<MultipartFile>() : files;

            //xử lý duyệt từng phần trong mảng
            for(MultipartFile file:files){
                if(file.getSize() == 0){
                    continue;
                }

                //kiểm tra kích thước và định dạng file

                if (file.getSize() > 10 * 1024 * 1024){ //kích thước >10mb
                    return  ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                            .body("File is to large! Maximum size is 10MB.");
                }
                String contentType = file.getContentType();
                if(contentType == null || !contentType.startsWith("image/")){
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                            .body("File must be an image");
                }
                String filename = storeFile(file); //Thay thế hàm này với code gốc để lưu file
                //lư vào đối tượng product trong DB => xử lí sau
                //lưu vào bảng proudct_images
            }

            return ResponseEntity.ok("Product created successfully");
        }catch(Exception ex){
            return ResponseEntity.badRequest().body(ex.getMessage());

        }
    }

    private String storeFile(MultipartFile file) throws IOException{
        String filename = StringUtils.cleanPath(file.getOriginalFilename());

        //thêm UUID vào trước tên file để đảm bảo tên file là duy nhất
        String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
        java.nio.file.Path uploadDir = Paths.get("uploads");

        //kiểm tra vào tạo thư mục nếu nó không tồn tại
        if(!Files.exists(uploadDir)){
            Files.createDirectories(uploadDir);
        }

        //Đường dẫn đầy đủ đến file
        java.nio.file.Path destination = Paths.get(uploadDir.toString(), uniqueFilename);

        //Sao chép file vào thư mục đích
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String>deleteProduct(@PathVariable long id){
        return ResponseEntity.status(HttpStatus.OK).body(String.format("Product deleted with id = %d successfully", id));
    }

}
