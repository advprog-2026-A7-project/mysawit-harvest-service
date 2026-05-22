package com.mysawit.harvest.service.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceImplTest {
    @Mock
    private S3Client s3Client;

    @InjectMocks
    private StorageServiceImpl storageService;

    private final String BUCKET_NAME = "harvest-proofs";
    private final String ENDPOINT = "https://xyz.storage.supabase.co/s3";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(storageService, "bucketName", BUCKET_NAME);
        ReflectionTestUtils.setField(storageService, "endpoint", ENDPOINT);
    }

    @Test
    void uploadFile_Success_ShouldReturnCorrectPublicUrl() throws IOException {
        String originalFileName = "foto_sawit.jpg";
        String contentType = "image/jpeg";
        byte[] content = "mock-image-bytes".getBytes();
        MockMultipartFile mockFile = new MockMultipartFile("file", originalFileName, contentType, content);

        String resultUrl = storageService.uploadFile(mockFile);

        String expectedBaseUrl = "https://xyz.supabase.co/object/public/" + BUCKET_NAME + "/";
        assertThat(resultUrl).startsWith(expectedBaseUrl);
        assertThat(resultUrl).endsWith(".jpg");

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client, times(1)).putObject(requestCaptor.capture(), any(RequestBody.class));

        PutObjectRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.bucket()).isEqualTo(BUCKET_NAME);
        assertThat(capturedRequest.contentType()).isEqualTo(contentType);
        assertThat(capturedRequest.key()).endsWith(".jpg");
    }

    @Test
    void uploadFile_EmptyFile_ShouldThrowIllegalArgumentException() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        assertThatThrownBy(() -> storageService.uploadFile(emptyFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot upload empty file");

        verifyNoInteractions(s3Client);
    }

    @Test
    void uploadFile_S3ThrowsException_ShouldThrowRuntimeException() {
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.png", "image/png", "bytes".getBytes());

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("S3 Error").build());

        assertThatThrownBy(() -> storageService.uploadFile(mockFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to upload file to Supabase Storage");
    }

    @Test
    void uploadFile_IOExceptionOnInputStream_ShouldThrowRuntimeException() throws IOException {
        MultipartFile damagedFile = mock(MultipartFile.class);
        when(damagedFile.isEmpty()).thenReturn(false);
        when(damagedFile.getOriginalFilename()).thenReturn("error.jpg");
        when(damagedFile.getInputStream()).thenThrow(new IOException("Stream closed"));

        assertThatThrownBy(() -> storageService.uploadFile(damagedFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to upload file to Supabase Storage");
    }

    @Test
    void uploadFiles_MultipleFiles_ShouldReturnListOfUrls() {
        MockMultipartFile file1 = new MockMultipartFile("files", "file1.jpg", "image/jpeg", "data1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "file2.png", "image/png", "data2".getBytes());
        List<MultipartFile> fileList = List.of(file1, file2);

        List<String> resultUrls = storageService.uploadFiles(fileList);

        assertThat(resultUrls).hasSize(2);
        assertThat(resultUrls.get(0)).contains(".jpg");
        assertThat(resultUrls.get(1)).contains(".png");
        verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadFile_WithNullFileName_ShouldUseNoExtension() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(null);
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getSize()).thenReturn(100L);
        when(mockFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("data".getBytes()));

        String result = storageService.uploadFile(mockFile);

        assertNotNull(result);
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadFile_WithNoExtensionFileName_ShouldUseNoExtension() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("bukti_panen_tanpa_ekstensi");
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getSize()).thenReturn(100L);
        when(mockFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("data".getBytes()));

        String result = storageService.uploadFile(mockFile);

        assertNotNull(result);
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}