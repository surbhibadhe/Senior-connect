package com.project.seniorconnect.Service;

import com.project.seniorconnect.DTO.PageResponse;
import com.project.seniorconnect.DTO.ResourceCreateRequest;
import com.project.seniorconnect.DTO.ResourceResponse;
import com.project.seniorconnect.Entity.Resource;
import com.project.seniorconnect.Entity.ResourceCategory;
import com.project.seniorconnect.Entity.ResourceType;
import com.project.seniorconnect.Entity.Role;
import com.project.seniorconnect.Entity.User;
import com.project.seniorconnect.Repository.ResourceRepository;
import com.project.seniorconnect.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @TempDir
    Path tempDir;

    private ResourceService resourceService;

    private User juniorUser;
    private User seniorUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        resourceService = new ResourceService(resourceRepository, userRepository);
        ReflectionTestUtils.setField(resourceService, "uploadDir", tempDir.toString());

        juniorUser = new User();
        ReflectionTestUtils.setField(juniorUser, "id", 1L);
        juniorUser.setGoogleId("google-junior");
        juniorUser.setEmail("junior@despu.edu.in");
        juniorUser.setName("Junior Student");
        juniorUser.setRole(Role.JUNIOR);

        seniorUser = new User();
        ReflectionTestUtils.setField(seniorUser, "id", 2L);
        seniorUser.setGoogleId("google-senior");
        seniorUser.setEmail("senior@despu.edu.in");
        seniorUser.setName("Senior Student");
        seniorUser.setRole(Role.SENIOR);

        adminUser = new User();
        ReflectionTestUtils.setField(adminUser, "id", 3L);
        adminUser.setGoogleId("google-admin");
        adminUser.setEmail("admin@despu.edu.in");
        adminUser.setName("Admin User");
        adminUser.setRole(Role.ADMIN);
    }

    // --- 1. Resource Creation Permissions ---

    @Test
    void createResource_juniorStudentCanCreateTextResource() {
        when(userRepository.findByGoogleId("google-junior")).thenReturn(Optional.of(juniorUser));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> {
            Resource r = invocation.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 100L);
            return r;
        });

        ResourceCreateRequest request = new ResourceCreateRequest(
                "Algorithms Notes",
                "Chapter 1 to 5 notes",
                ResourceCategory.NOTES,
                ResourceType.TEXT,
                "Introduction to algorithms and data structures...",
                null
        );

        ResourceResponse response = resourceService.createResource(request, "google-junior");

        assertNotNull(response);
        assertEquals("Algorithms Notes", response.title());
        assertEquals(juniorUser.getName(), response.uploadedByName());
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    void createResource_seniorStudentCanCreateLinkResource() {
        when(userRepository.findByGoogleId("google-senior")).thenReturn(Optional.of(seniorUser));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> {
            Resource r = invocation.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 101L);
            return r;
        });

        ResourceCreateRequest request = new ResourceCreateRequest(
                "Reference Material Link",
                "Useful reference website",
                ResourceCategory.REFERENCE_MATERIALS,
                ResourceType.EXTERNAL_LINK,
                null,
                "https://example.com/study-material"
        );

        ResourceResponse response = resourceService.createResource(request, "google-senior");

        assertNotNull(response);
        assertEquals("Reference Material Link", response.title());
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    void createResource_textTypeWithoutContent_throwsBadRequest() {
        when(userRepository.findByGoogleId("google-junior")).thenReturn(Optional.of(juniorUser));

        ResourceCreateRequest request = new ResourceCreateRequest(
                "Empty Notes",
                "Description",
                ResourceCategory.NOTES,
                ResourceType.TEXT,
                "   ",
                null
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                resourceService.createResource(request, "google-junior"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Text content is required for TEXT resources", ex.getReason());
    }

    @Test
    void createResource_linkTypeWithInvalidUrl_throwsBadRequest() {
        when(userRepository.findByGoogleId("google-junior")).thenReturn(Optional.of(juniorUser));

        ResourceCreateRequest request = new ResourceCreateRequest(
                "Bad Link",
                "Description",
                ResourceCategory.NOTES,
                ResourceType.EXTERNAL_LINK,
                null,
                "not-a-valid-url"
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                resourceService.createResource(request, "google-junior"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Please provide a valid HTTP or HTTPS URL", ex.getReason());
    }

    @Test
    void createResource_fileTypeInJson_throwsBadRequest() {
        when(userRepository.findByGoogleId("google-junior")).thenReturn(Optional.of(juniorUser));

        ResourceCreateRequest request = new ResourceCreateRequest(
                "PDF via JSON",
                "Description",
                ResourceCategory.NOTES,
                ResourceType.PDF,
                null,
                null
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                resourceService.createResource(request, "google-junior"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("PDF, DOC, and DOCX uploads are not supported yet", ex.getReason());
    }

    @Test
    void createResource_unknownUser_throwsNotFound() {
        when(userRepository.findByGoogleId("unknown")).thenReturn(Optional.empty());

        ResourceCreateRequest request = new ResourceCreateRequest(
                "Title", "Desc", ResourceCategory.NOTES, ResourceType.TEXT, "Content", null
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                resourceService.createResource(request, "unknown"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("User profile not found", ex.getReason());
    }

    // --- 2. File Upload Permissions & Safety ---

    @Test
    void uploadResource_juniorStudentCanUploadPdf() {
        when(userRepository.findByGoogleId("google-junior")).thenReturn(Optional.of(juniorUser));
        when(resourceRepository.saveAndFlush(any(Resource.class))).thenAnswer(invocation -> {
            Resource r = invocation.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 200L);
            return r;
        });

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "exam_paper.pdf",
                "application/pdf",
                "%PDF-1.4 sample content".getBytes()
        );

        ResourceResponse response = resourceService.uploadResource(
                "Midsem Exam Paper",
                "2026 Question paper",
                ResourceCategory.QUESTION_PAPERS,
                file,
                "google-junior"
        );

        assertNotNull(response);
        assertEquals("Midsem Exam Paper", response.title());
        assertEquals(ResourceType.PDF, response.type());
        assertEquals("exam_paper.pdf", response.originalFileName());
        verify(resourceRepository).saveAndFlush(any(Resource.class));
    }

    @Test
    void uploadResource_seniorStudentCanUploadDocx() {
        when(userRepository.findByGoogleId("google-senior")).thenReturn(Optional.of(seniorUser));
        when(resourceRepository.saveAndFlush(any(Resource.class))).thenAnswer(invocation -> {
            Resource r = invocation.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 201L);
            return r;
        });

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "assignment1.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "docx binary content".getBytes()
        );

        ResourceResponse response = resourceService.uploadResource(
                "Assignment 1 Solution",
                "Java assignment solution",
                ResourceCategory.ASSIGNMENTS,
                file,
                "google-senior"
        );

        assertNotNull(response);
        assertEquals("Assignment 1 Solution", response.title());
        assertEquals(ResourceType.DOCX, response.type());
        assertEquals("assignment1.docx", response.originalFileName());
    }

    @Test
    void uploadResource_unsupportedExtension_throwsBadRequest() {
        when(userRepository.findByGoogleId("google-junior")).thenReturn(Optional.of(juniorUser));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "script.sh",
                "application/x-sh",
                "echo hello".getBytes()
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                resourceService.uploadResource("Script", "Desc", ResourceCategory.NOTES, file, "google-junior"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Only PDF, DOC, and DOCX files are allowed", ex.getReason());
    }

    @Test
    void uploadResource_mismatchedContentType_throwsBadRequest() {
        when(userRepository.findByGoogleId("google-junior")).thenReturn(Optional.of(juniorUser));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.pdf",
                "text/plain",
                "fake pdf content".getBytes()
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                resourceService.uploadResource("Notes", "Desc", ResourceCategory.NOTES, file, "google-junior"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("File content type does not match the file extension", ex.getReason());
    }

    @Test
    void uploadResource_blankTitle_throwsBadRequest() {
        when(userRepository.findByGoogleId("google-junior")).thenReturn(Optional.of(juniorUser));

        MockMultipartFile file = new MockMultipartFile(
                "file", "notes.pdf", "application/pdf", "content".getBytes()
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                resourceService.uploadResource("   ", "Desc", ResourceCategory.NOTES, file, "google-junior"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Title is required", ex.getReason());
    }

    @Test
    void uploadResource_databaseFailureCleansUpFile() {
        when(userRepository.findByGoogleId("google-junior")).thenReturn(Optional.of(juniorUser));
        when(resourceRepository.saveAndFlush(any(Resource.class)))
                .thenThrow(new RuntimeException("Database constraint violation"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "notes.pdf", "application/pdf", "sample content".getBytes()
        );

        assertThrows(RuntimeException.class, () ->
                resourceService.uploadResource("Notes", "Desc", ResourceCategory.NOTES, file, "google-junior"));

        // Verify that no orphaned files remain in tempDir
        try (var stream = Files.list(tempDir)) {
            assertEquals(0, stream.count(), "Uploaded file must be cleaned up if database save fails");
        } catch (IOException e) {
            fail(e);
        }
    }

    // --- 3. Resource Update Permissions ---

    @Test
    void updateResource_studentCanUpdateOwnResource() {
        Resource existing = new Resource();
        ReflectionTestUtils.setField(existing, "id", 10L);
        existing.setTitle("Old Title");
        existing.setUploadedBy(juniorUser);
        existing.setType(ResourceType.TEXT);
        existing.setCategory(ResourceCategory.NOTES);

        when(userRepository.findByGoogleId("google-junior")).thenReturn(Optional.of(juniorUser));
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(i -> i.getArgument(0));

        ResourceCreateRequest updateReq = new ResourceCreateRequest(
                "Updated Title",
                "Updated Description",
                ResourceCategory.NOTES,
                ResourceType.TEXT,
                "Updated Content",
                null
        );

        ResourceResponse updated = resourceService.updateResource(10L, updateReq, "google-junior");

        assertEquals("Updated Title", updated.title());
        assertEquals("Updated Description", updated.description());
        assertEquals("Updated Content", updated.textContent());
    }

    @Test
    void updateResource_studentForbiddenFromUpdatingAnotherStudentResource() {
        Resource existing = new Resource();
        ReflectionTestUtils.setField(existing, "id", 10L);
        existing.setTitle("Junior's Resource");
        existing.setUploadedBy(juniorUser); // owned by junior

        when(userRepository.findByGoogleId("google-senior")).thenReturn(Optional.of(seniorUser));
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(existing));

        ResourceCreateRequest updateReq = new ResourceCreateRequest(
                "Senior Hijack Attempt",
                "Desc",
                ResourceCategory.NOTES,
                ResourceType.TEXT,
                "New Content",
                null
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                resourceService.updateResource(10L, updateReq, "google-senior"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("You can update only resources you uploaded", ex.getReason());
        verify(resourceRepository, never()).save(any());
    }

    @Test
    void updateResource_adminCanUpdateOtherStudentResource() {
        Resource existing = new Resource();
        ReflectionTestUtils.setField(existing, "id", 10L);
        existing.setTitle("Student's Resource");
        existing.setUploadedBy(juniorUser);
        existing.setType(ResourceType.TEXT);
        existing.setCategory(ResourceCategory.NOTES);

        when(userRepository.findByGoogleId("google-admin")).thenReturn(Optional.of(adminUser));
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(i -> i.getArgument(0));

        ResourceCreateRequest updateReq = new ResourceCreateRequest(
                "Admin Moderated Title",
                "Admin cleaned description",
                ResourceCategory.NOTES,
                ResourceType.TEXT,
                "Moderated Content",
                null
        );

        ResourceResponse updated = resourceService.updateResource(10L, updateReq, "google-admin");

        assertEquals("Admin Moderated Title", updated.title());
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    void updateResource_missingResource_throwsNotFound() {
        when(userRepository.findByGoogleId("google-junior")).thenReturn(Optional.of(juniorUser));
        when(resourceRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceCreateRequest updateReq = new ResourceCreateRequest(
                "Title", "Desc", ResourceCategory.NOTES, ResourceType.TEXT, "Content", null
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                resourceService.updateResource(999L, updateReq, "google-junior"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Resource not found", ex.getReason());
    }

    // --- 4. Resource Deletion Permissions & File Safety ---

    @Test
    void deleteResource_studentCanDeleteOwnResourceWithFile() throws IOException {
        Path testFile = tempDir.resolve("sample-stored.pdf");
        Files.writeString(testFile, "dummy pdf content");

        Resource existing = new Resource();
        ReflectionTestUtils.setField(existing, "id", 15L);
        existing.setUploadedBy(juniorUser);
        existing.setStoredFileName("sample-stored.pdf");

        when(userRepository.findByGoogleId("google-junior")).thenReturn(Optional.of(juniorUser));
        when(resourceRepository.findById(15L)).thenReturn(Optional.of(existing));

        resourceService.deleteResource(15L, "google-junior");

        verify(resourceRepository).delete(existing);
        verify(resourceRepository).flush();
        assertFalse(Files.exists(testFile), "Uploaded file must be deleted from disk upon deletion");
    }

    @Test
    void deleteResource_studentForbiddenFromDeletingAnotherStudentResource() {
        Resource existing = new Resource();
        ReflectionTestUtils.setField(existing, "id", 15L);
        existing.setUploadedBy(juniorUser); // owned by junior

        when(userRepository.findByGoogleId("google-senior")).thenReturn(Optional.of(seniorUser));
        when(resourceRepository.findById(15L)).thenReturn(Optional.of(existing));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                resourceService.deleteResource(15L, "google-senior"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("You can delete only resources you uploaded", ex.getReason());
        verify(resourceRepository, never()).delete(any());
    }

    @Test
    void deleteResource_adminCanDeleteOtherStudentResource() {
        Resource existing = new Resource();
        ReflectionTestUtils.setField(existing, "id", 15L);
        existing.setUploadedBy(juniorUser);

        when(userRepository.findByGoogleId("google-admin")).thenReturn(Optional.of(adminUser));
        when(resourceRepository.findById(15L)).thenReturn(Optional.of(existing));

        resourceService.deleteResource(15L, "google-admin");

        verify(resourceRepository).delete(existing);
        verify(resourceRepository).flush();
    }

    @Test
    void deleteResource_missingResource_throwsNotFound() {
        when(userRepository.findByGoogleId("google-admin")).thenReturn(Optional.of(adminUser));
        when(resourceRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                resourceService.deleteResource(999L, "google-admin"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Resource not found", ex.getReason());
    }

    // --- 5. Retrieval, Pagination, Search, Filtering, and Sorting ---

    @Test
    void getResources_defaultPaginationAndSort_newestFirst() {
        Resource r1 = new Resource();
        ReflectionTestUtils.setField(r1, "id", 1L);
        r1.setTitle("Resource 1");
        r1.setUploadedBy(juniorUser);
        r1.setCategory(ResourceCategory.NOTES);

        Page<Resource> pageMock = new PageImpl<>(List.of(r1), org.springframework.data.domain.PageRequest.of(0, 10), 1);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(resourceRepository.findAll(pageableCaptor.capture())).thenReturn(pageMock);

        PageResponse<ResourceResponse> response = resourceService.getResources(null, null, 0, 10, "newest");

        assertNotNull(response);
        assertEquals(0, response.page());
        assertEquals(10, response.size());
        assertEquals(1, response.totalElements());
        assertEquals(1, response.totalPages());
        assertTrue(response.first());
        assertTrue(response.last());
        assertFalse(response.empty());
        assertEquals(1, response.content().size());

        Pageable captured = pageableCaptor.getValue();
        assertEquals(0, captured.getPageNumber());
        assertEquals(10, captured.getPageSize());
        Sort.Order order = captured.getSort().getOrderFor("createdAt");
        assertNotNull(order);
        assertTrue(order.isDescending());
    }

    @Test
    void getResources_customPageAndSize() {
        Page<Resource> emptyPage = new PageImpl<>(List.of(), org.springframework.data.domain.PageRequest.of(2, 5), 12);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(resourceRepository.findAll(pageableCaptor.capture())).thenReturn(emptyPage);

        PageResponse<ResourceResponse> response = resourceService.getResources(null, null, 2, 5, "newest");

        assertEquals(2, response.page());
        assertEquals(5, response.size());
        assertEquals(12, response.totalElements());
        assertEquals(3, response.totalPages());
        assertFalse(response.first());
        assertTrue(response.last());

        Pageable captured = pageableCaptor.getValue();
        assertEquals(2, captured.getPageNumber());
        assertEquals(5, captured.getPageSize());
    }

    @Test
    void getResources_negativePage_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                resourceService.getResources(null, null, -1, 10, "newest"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Page index must not be less than zero", ex.getReason());
    }

    @Test
    void getResources_zeroOrNegativeSize_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                resourceService.getResources(null, null, 0, 0, "newest"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Page size must be greater than zero", ex.getReason());

        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class, () ->
                resourceService.getResources(null, null, 0, -5, "newest"));
        assertEquals(HttpStatus.BAD_REQUEST, ex2.getStatusCode());
    }

    @Test
    void getResources_excessiveSize_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                resourceService.getResources(null, null, 0, 51, "newest"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Page size must not exceed 50", ex.getReason());
    }

    @Test
    void getResources_invalidSortOption_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                resourceService.getResources(null, null, 0, 10, "malicious_col"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Invalid sort option"));
    }

    @Test
    void getResources_sortingOptions_allSupportedModes() {
        Page<Resource> emptyPage = new PageImpl<>(List.of());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        // oldest
        when(resourceRepository.findAll(captor.capture())).thenReturn(emptyPage);
        resourceService.getResources(null, null, 0, 10, "oldest");
        assertTrue(captor.getValue().getSort().getOrderFor("createdAt").isAscending());

        // title_asc
        when(resourceRepository.findAll(captor.capture())).thenReturn(emptyPage);
        resourceService.getResources(null, null, 0, 10, "title_asc");
        assertTrue(captor.getValue().getSort().getOrderFor("title").isAscending());

        // title_desc
        when(resourceRepository.findAll(captor.capture())).thenReturn(emptyPage);
        resourceService.getResources(null, null, 0, 10, "title_desc");
        assertTrue(captor.getValue().getSort().getOrderFor("title").isDescending());
    }

    @Test
    void getResources_searchMatchesTitleOrDescription_withoutCategory() {
        Resource r1 = new Resource();
        ReflectionTestUtils.setField(r1, "id", 1L);
        r1.setTitle("Spring Boot Guide");
        r1.setDescription("Deep dive into Spring MVC");
        r1.setUploadedBy(seniorUser);
        r1.setCategory(ResourceCategory.NOTES);

        when(resourceRepository.findBySearch(eq("spring"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(r1)));

        PageResponse<ResourceResponse> response = resourceService.getResources(null, "  spring  ", 0, 10, "newest");

        assertEquals(1, response.content().size());
        assertEquals("Spring Boot Guide", response.content().get(0).title());
        verify(resourceRepository).findBySearch(eq("spring"), any(Pageable.class));
    }

    @Test
    void getResources_categoryFilterOnly() {
        Resource r1 = new Resource();
        ReflectionTestUtils.setField(r1, "id", 2L);
        r1.setTitle("Lab Manual 1");
        r1.setUploadedBy(juniorUser);
        r1.setCategory(ResourceCategory.LAB_MANUALS);

        when(resourceRepository.findByCategory(eq(ResourceCategory.LAB_MANUALS), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(r1)));

        PageResponse<ResourceResponse> response = resourceService.getResources(ResourceCategory.LAB_MANUALS, null, 0, 10, "newest");

        assertEquals(1, response.content().size());
        assertEquals(ResourceCategory.LAB_MANUALS, response.content().get(0).category());
        verify(resourceRepository).findByCategory(eq(ResourceCategory.LAB_MANUALS), any(Pageable.class));
    }

    @Test
    void getResources_combinedCategoryAndSearch() {
        Resource r1 = new Resource();
        ReflectionTestUtils.setField(r1, "id", 3L);
        r1.setTitle("Java Question Paper");
        r1.setUploadedBy(juniorUser);
        r1.setCategory(ResourceCategory.QUESTION_PAPERS);

        when(resourceRepository.findByCategoryAndSearch(eq(ResourceCategory.QUESTION_PAPERS), eq("java"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(r1)));

        PageResponse<ResourceResponse> response = resourceService.getResources(ResourceCategory.QUESTION_PAPERS, "java", 0, 10, "newest");

        assertEquals(1, response.content().size());
        assertEquals("Java Question Paper", response.content().get(0).title());
        verify(resourceRepository).findByCategoryAndSearch(eq(ResourceCategory.QUESTION_PAPERS), eq("java"), any(Pageable.class));
    }

    @Test
    void getResources_blankSearchTreatedAsNoSearch() {
        when(resourceRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        PageResponse<ResourceResponse> response = resourceService.getResources(null, "   ", 0, 10, "newest");

        assertNotNull(response);
        verify(resourceRepository).findAll(any(Pageable.class));
        verify(resourceRepository, never()).findBySearch(any(), any());
    }

    @Test
    void getResources_outOfRangePage_returnsEmptyContentWithMetadata() {
        // total 5 elements, page 10 of size 10 has 0 items
        Page<Resource> emptyPage = new PageImpl<>(List.of(), org.springframework.data.domain.PageRequest.of(10, 10), 5);
        when(resourceRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        PageResponse<ResourceResponse> response = resourceService.getResources(null, null, 10, 10, "newest");

        assertNotNull(response);
        assertEquals(10, response.page());
        assertEquals(10, response.size());
        assertEquals(5, response.totalElements());
        assertEquals(1, response.totalPages());
        assertTrue(response.empty());
        assertTrue(response.content().isEmpty());
    }

    @Test
    void getResources_backwardCompatibleMethod() {
        Resource r1 = new Resource();
        ReflectionTestUtils.setField(r1, "id", 1L);
        r1.setTitle("Backward Compatible");
        r1.setUploadedBy(juniorUser);
        r1.setCategory(ResourceCategory.NOTES);

        when(resourceRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(r1)));

        List<ResourceResponse> list = resourceService.getResources(null, null);

        assertEquals(1, list.size());
        assertEquals("Backward Compatible", list.get(0).title());
    }

    @Test
    void getDownloadFile_pathTraversalAttempt_throwsBadRequest() {
        Resource resource = new Resource();
        ReflectionTestUtils.setField(resource, "id", 50L);
        resource.setStoredFileName("../outside.txt");

        when(resourceRepository.findById(50L)).thenReturn(Optional.of(resource));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                resourceService.getDownloadFile(50L));
        assertTrue(ex.getStatusCode() == HttpStatus.BAD_REQUEST || ex.getStatusCode() == HttpStatus.NOT_FOUND);
    }

    @Test
    void getResourcesByContributor_success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        Resource r1 = new Resource();
        ReflectionTestUtils.setField(r1, "id", 101L);
        r1.setTitle("Contributor Resource 1");
        r1.setCategory(ResourceCategory.NOTES);
        r1.setUploadedBy(juniorUser);

        when(resourceRepository.findByUploadedById(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(r1)));

        PageResponse<ResourceResponse> response =
                resourceService.getResourcesByContributor(1L, 0, 10, "newest");

        assertNotNull(response);
        assertEquals(1, response.totalElements());
        assertEquals("Contributor Resource 1", response.content().get(0).title());
    }

    @Test
    void getResourcesByContributor_customSorting() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(resourceRepository.findByUploadedById(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        assertDoesNotThrow(() -> resourceService.getResourcesByContributor(1L, 0, 10, "oldest"));
        assertDoesNotThrow(() -> resourceService.getResourcesByContributor(1L, 0, 10, "title_asc"));
        assertDoesNotThrow(() -> resourceService.getResourcesByContributor(1L, 0, 10, "title_desc"));
    }

    @Test
    void getResourcesByContributor_invalidSort_throwsBadRequest() {
        when(userRepository.existsById(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                resourceService.getResourcesByContributor(1L, 0, 10, "malicious_column"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Invalid sort option"));
    }

    @Test
    void getResourcesByContributor_contributorNotFound_throws404() {
        when(userRepository.existsById(999L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                resourceService.getResourcesByContributor(999L, 0, 10, "newest"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Contributor not found with id: 999", ex.getReason());
    }

    @Test
    void getResourcesByContributor_invalidPageNegative_throwsBadRequest() {
        when(userRepository.existsById(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                resourceService.getResourcesByContributor(1L, -1, 10, "newest"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Page index must not be less than zero", ex.getReason());
    }

    @Test
    void getResourcesByContributor_invalidPageSizeZero_throwsBadRequest() {
        when(userRepository.existsById(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                resourceService.getResourcesByContributor(1L, 0, 0, "newest"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Page size must be between 1 and 50", ex.getReason());
    }

    @Test
    void getResourcesByContributor_invalidPageSizeOver50_throwsBadRequest() {
        when(userRepository.existsById(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                resourceService.getResourcesByContributor(1L, 0, 51, "newest"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Page size must be between 1 and 50", ex.getReason());
    }

    @Test
    void getResourcesByContributor_emptyResources_returnsEmptyPageResponse() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(resourceRepository.findByUploadedById(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        PageResponse<ResourceResponse> response =
                resourceService.getResourcesByContributor(1L, 0, 10, "newest");

        assertNotNull(response);
        assertEquals(0, response.totalElements());
        assertTrue(response.empty());
        assertTrue(response.content().isEmpty());
    }
}

