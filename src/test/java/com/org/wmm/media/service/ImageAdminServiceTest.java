package com.org.wmm.media.service;

import com.org.wmm.audit.service.AuditService;
import com.org.wmm.common.error.BadRequestException;
import com.org.wmm.common.error.ResourceNotFoundException;
import com.org.wmm.content.items.entity.ImageEntity;
import com.org.wmm.content.items.entity.ImageTranslationEntity;
import com.org.wmm.content.items.entity.ItemEntity;
import com.org.wmm.content.items.repository.ItemRepository;
import com.org.wmm.languages.entity.LanguageEntity;
import com.org.wmm.languages.service.LanguageQueryService;
import com.org.wmm.media.dto.*;
import com.org.wmm.media.mapper.ImageMapper;
import com.org.wmm.media.repository.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageAdminServiceTest {

    @Mock
    private ImageRepository imageRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private StorageService storageService;
    @Mock
    private LanguageQueryService languageQueryService;
    @Mock
    private ImageMapper imageMapper;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private ImageAdminService service;

    private ItemEntity item;
    private ImageEntity image;
    private AdminImageDto imageDto;
    private LanguageEntity polish;

    @BeforeEach
    void setUp() {
        item = ItemEntity.builder().id(1L).slug("talisker-10").build();

        image = ImageEntity.builder()
                .id(1L)
                .item(item)
                .filePath("items/1/abc123.jpg")
                .fileName("talisker.jpg")
                .fileSizeBytes(100000L)
                .mimeType("image/jpeg")
                .isPrimary(false)
                .displayOrder(0)
                .imageTranslations(new ArrayList<>())
                .build();

        imageDto = AdminImageDto.builder()
                .id(1L).itemId(1L)
                .filePath("items/1/abc123.jpg")
                .fileName("talisker.jpg")
                .url("http://localhost:8080/media/items/1/abc123.jpg")
                .isPrimary(false).displayOrder(0)
                .translations(List.of())
                .build();

        polish = LanguageEntity.builder().id(1L).code("pl").name("Polish").isDefault(true).isActive(true).build();
    }

    @Nested
    @DisplayName("uploadImages")
    class UploadImages {

        @Test
        @DisplayName("should upload image successfully")
        void shouldUploadImage() throws IOException {
            MockMultipartFile file = new MockMultipartFile(
                    "files", "talisker.jpg", "image/jpeg", new byte[]{1, 2, 3});

            when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(imageRepository.countByItemId(1L)).thenReturn(0);
            when(imageRepository.findMaxDisplayOrderByItemId(1L)).thenReturn(-1);
            when(imageRepository.findPrimaryByItemId(1L)).thenReturn(Optional.empty());
            when(storageService.store(any(MultipartFile.class), eq("items/1"))).thenReturn("items/1/abc123.jpg");
            when(imageRepository.save(any(ImageEntity.class))).thenReturn(image);
            when(imageMapper.toAdminDto(any(ImageEntity.class))).thenReturn(imageDto);

            List<AdminImageDto> result = service.uploadImages(1L, new MultipartFile[]{file});

            assertThat(result).hasSize(1);
            verify(storageService).store(any(), eq("items/1"));
            verify(imageRepository).save(any(ImageEntity.class));
            verify(auditService).logCreate(eq("image"), anyLong(), anyString());
        }

        @Test
        @DisplayName("should throw when item not found")
        void shouldThrowWhenItemNotFound() {
            MockMultipartFile file = new MockMultipartFile(
                    "files", "test.jpg", "image/jpeg", new byte[]{1});

            when(itemRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.uploadImages(99L, new MultipartFile[]{file}))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when no files provided")
        void shouldThrowWhenNoFiles() {
            when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

            assertThatThrownBy(() -> service.uploadImages(1L, new MultipartFile[]{}))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("No files");
        }

        @Test
        @DisplayName("should reject unsupported mime type")
        void shouldRejectUnsupportedMimeType() {
            MockMultipartFile file = new MockMultipartFile(
                    "files", "doc.pdf", "application/pdf", new byte[]{1, 2, 3});

            when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(imageRepository.countByItemId(1L)).thenReturn(0);
            when(imageRepository.findMaxDisplayOrderByItemId(1L)).thenReturn(-1);
            when(imageRepository.findPrimaryByItemId(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.uploadImages(1L, new MultipartFile[]{file}))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Unsupported file type");
        }

        @Test
        @DisplayName("should reject empty file")
        void shouldRejectEmptyFile() {
            MockMultipartFile file = new MockMultipartFile(
                    "files", "empty.jpg", "image/jpeg", new byte[]{});

            when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(imageRepository.countByItemId(1L)).thenReturn(0);
            when(imageRepository.findMaxDisplayOrderByItemId(1L)).thenReturn(-1);
            when(imageRepository.findPrimaryByItemId(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.uploadImages(1L, new MultipartFile[]{file}))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("empty");
        }
    }

    @Nested
    @DisplayName("updateImage")
    class UpdateImage {

        @Test
        @DisplayName("should set image as primary")
        void shouldSetPrimary() {
            UpdateImageRequest request = UpdateImageRequest.builder().isPrimary(true).build();

            when(imageRepository.findByIdWithTranslations(1L)).thenReturn(Optional.of(image));
            when(imageRepository.save(any(ImageEntity.class))).thenReturn(image);
            when(imageMapper.toAdminDto(any(ImageEntity.class))).thenReturn(imageDto);

            service.updateImage(1L, request);

            verify(imageRepository).clearPrimaryForItem(1L);
            assertThat(image.getIsPrimary()).isTrue();
        }

        @Test
        @DisplayName("should update display order")
        void shouldUpdateDisplayOrder() {
            UpdateImageRequest request = UpdateImageRequest.builder().displayOrder(5).build();

            when(imageRepository.findByIdWithTranslations(1L)).thenReturn(Optional.of(image));
            when(imageRepository.save(any(ImageEntity.class))).thenReturn(image);
            when(imageMapper.toAdminDto(any(ImageEntity.class))).thenReturn(imageDto);

            service.updateImage(1L, request);

            assertThat(image.getDisplayOrder()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("reorderImages")
    class ReorderImages {

        @Test
        @DisplayName("should reorder images")
        void shouldReorder() {
            ImageEntity img1 = ImageEntity.builder().id(1L).item(item).displayOrder(0).build();
            ImageEntity img2 = ImageEntity.builder().id(2L).item(item).displayOrder(1).build();
            ImageEntity img3 = ImageEntity.builder().id(3L).item(item).displayOrder(2).build();

            when(itemRepository.existsById(1L)).thenReturn(true);
            when(imageRepository.findByItemIdOrdered(1L)).thenReturn(List.of(img1, img2, img3));
            when(imageRepository.findByItemIdWithTranslations(1L)).thenReturn(List.of(img3, img1, img2));
            when(imageMapper.toAdminDto(any(ImageEntity.class))).thenReturn(imageDto);

            ReorderImagesRequest request = ReorderImagesRequest.builder()
                    .imageIds(List.of(3L, 1L, 2L)).build();

            service.reorderImages(1L, request);

            assertThat(img3.getDisplayOrder()).isEqualTo(0);
            assertThat(img1.getDisplayOrder()).isEqualTo(1);
            assertThat(img2.getDisplayOrder()).isEqualTo(2);
            verify(imageRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("should throw when image does not belong to item")
        void shouldThrowWhenImageNotInItem() {
            ImageEntity img1 = ImageEntity.builder().id(1L).item(item).displayOrder(0).build();

            when(itemRepository.existsById(1L)).thenReturn(true);
            when(imageRepository.findByItemIdOrdered(1L)).thenReturn(List.of(img1));

            ReorderImagesRequest request = ReorderImagesRequest.builder()
                    .imageIds(List.of(1L, 99L)).build();

            assertThatThrownBy(() -> service.reorderImages(1L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("does not belong");
        }
    }

    @Nested
    @DisplayName("updateImageTranslations")
    class UpdateImageTranslations {

        @Test
        @DisplayName("should upsert translations")
        void shouldUpsertTranslations() {
            when(imageRepository.findByIdWithTranslations(1L)).thenReturn(Optional.of(image));
            when(languageQueryService.resolveLanguage("pl")).thenReturn(polish);
            when(imageRepository.save(any(ImageEntity.class))).thenReturn(image);
            when(imageMapper.toAdminDto(any(ImageEntity.class))).thenReturn(imageDto);

            UpdateImageTranslationsRequest request = UpdateImageTranslationsRequest.builder()
                    .translations(List.of(
                            ImageTranslationRequest.builder()
                                    .languageCode("pl").altText("Alt PL").caption("Caption PL").build()
                    )).build();

            service.updateImageTranslations(1L, request);

            assertThat(image.getImageTranslations()).hasSize(1);
            verify(imageRepository).save(any(ImageEntity.class));
        }
    }

    @Nested
    @DisplayName("deleteImage")
    class DeleteImage {

        @Test
        @DisplayName("should delete image and file")
        void shouldDeleteImage() throws IOException {
            when(imageRepository.findById(1L)).thenReturn(Optional.of(image));

            service.deleteImage(1L);

            verify(storageService).delete("items/1/abc123.jpg");
            verify(imageRepository).delete(image);
            verify(auditService).logDelete("image", 1L);
        }

        @Test
        @DisplayName("should promote next image when primary is deleted")
        void shouldPromoteNextWhenPrimaryDeleted() throws IOException {
            image.setIsPrimary(true);
            ImageEntity nextImage = ImageEntity.builder()
                    .id(2L).item(item).isPrimary(false).displayOrder(1).build();

            when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
            when(imageRepository.findByItemIdOrdered(1L)).thenReturn(List.of(nextImage));
            when(imageRepository.save(nextImage)).thenReturn(nextImage);

            service.deleteImage(1L);

            assertThat(nextImage.getIsPrimary()).isTrue();
            verify(imageRepository).save(nextImage);
        }

        @Test
        @DisplayName("should throw when image not found")
        void shouldThrowWhenNotFound() {
            when(imageRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteImage(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}


