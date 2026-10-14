package com.project.seniorconnect.Service;

import com.project.seniorconnect.DTO.BookmarkStatusResponse;
import com.project.seniorconnect.DTO.HelpfulStatusResponse;
import com.project.seniorconnect.DTO.PageResponse;
import com.project.seniorconnect.DTO.ResourceResponse;
import com.project.seniorconnect.Entity.*;
import com.project.seniorconnect.Repository.BookmarkRepository;
import com.project.seniorconnect.Repository.ResourceHelpfulRepository;
import com.project.seniorconnect.Repository.ResourceRepository;
import com.project.seniorconnect.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EngagementServiceTest {

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private ResourceHelpfulRepository resourceHelpfulRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResourceService resourceService;

    @InjectMocks
    private EngagementService engagementService;

    private User student;
    private Resource resource;

    @BeforeEach
    void setUp() {
        student = new User();
        ReflectionTestUtils.setField(student, "id", 1L);
        student.setName("Student Tester");
        student.setEmail("tester@despu.edu.in");
        student.setGoogleId("google-tester");

        resource = new Resource();
        ReflectionTestUtils.setField(resource, "id", 10L);
        resource.setTitle("Operating Systems Notes");
        resource.setUploadedBy(student);
    }

    @Test
    void bookmarkResource_success() {
        when(userRepository.findByGoogleId("google-tester")).thenReturn(Optional.of(student));
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(resource));
        when(bookmarkRepository.existsByUserIdAndResourceId(1L, 10L)).thenReturn(false);

        BookmarkStatusResponse response = engagementService.bookmarkResource(10L, "google-tester");

        assertNotNull(response);
        assertEquals(10L, response.resourceId());
        assertTrue(response.bookmarked());
        assertEquals("Resource bookmarked successfully", response.message());
        verify(bookmarkRepository).save(any(Bookmark.class));
    }

    @Test
    void bookmarkResource_duplicateIsSafe() {
        when(userRepository.findByGoogleId("google-tester")).thenReturn(Optional.of(student));
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(resource));
        when(bookmarkRepository.existsByUserIdAndResourceId(1L, 10L)).thenReturn(true);

        BookmarkStatusResponse response = engagementService.bookmarkResource(10L, "google-tester");

        assertNotNull(response);
        assertTrue(response.bookmarked());
        assertEquals("Resource is already bookmarked", response.message());
        verify(bookmarkRepository, never()).save(any());
    }

    @Test
    void bookmarkResource_missingResource_throws404() {
        when(userRepository.findByGoogleId("google-tester")).thenReturn(Optional.of(student));
        when(resourceRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                engagementService.bookmarkResource(999L, "google-tester"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Resource not found with id: 999", ex.getReason());
    }

    @Test
    void removeBookmark_success() {
        when(userRepository.findByGoogleId("google-tester")).thenReturn(Optional.of(student));
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(resource));

        BookmarkStatusResponse response = engagementService.removeBookmark(10L, "google-tester");

        assertNotNull(response);
        assertFalse(response.bookmarked());
        verify(bookmarkRepository).deleteByUserIdAndResourceId(1L, 10L);
    }

    @Test
    void getBookmarkStatus_returnsTrueWhenBookmarked() {
        when(userRepository.findByGoogleId("google-tester")).thenReturn(Optional.of(student));
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(resource));
        when(bookmarkRepository.existsByUserIdAndResourceId(1L, 10L)).thenReturn(true);

        BookmarkStatusResponse response = engagementService.getBookmarkStatus(10L, "google-tester");

        assertTrue(response.bookmarked());
    }

    @Test
    void getBookmarkStatus_returnsFalseWhenNotBookmarked() {
        when(userRepository.findByGoogleId("google-tester")).thenReturn(Optional.of(student));
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(resource));
        when(bookmarkRepository.existsByUserIdAndResourceId(1L, 10L)).thenReturn(false);

        BookmarkStatusResponse response = engagementService.getBookmarkStatus(10L, "google-tester");

        assertFalse(response.bookmarked());
    }

    @Test
    void getBookmarkedResources_successWithPagination() {
        when(userRepository.findByGoogleId("google-tester")).thenReturn(Optional.of(student));
        when(resourceService.resolveSort("newest")).thenReturn(Sort.by(Sort.Order.desc("createdAt")));

        Bookmark bookmark = new Bookmark(student, resource);
        when(bookmarkRepository.findByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(bookmark)));

        ResourceResponse mockResourceResponse = new ResourceResponse(
                10L, "Operating Systems Notes", null, null, null, null, null, null, "Student Tester", null, null, 5L
        );
        when(resourceService.toResponse(resource)).thenReturn(mockResourceResponse);

        PageResponse<ResourceResponse> pageResponse =
                engagementService.getBookmarkedResources("google-tester", 0, 10, "newest");

        assertNotNull(pageResponse);
        assertEquals(1, pageResponse.totalElements());
        assertEquals("Operating Systems Notes", pageResponse.content().get(0).title());
    }

    @Test
    void getBookmarkedResources_negativePage_throws400() {
        when(userRepository.findByGoogleId("google-tester")).thenReturn(Optional.of(student));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                engagementService.getBookmarkedResources("google-tester", -1, 10, "newest"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void getBookmarkedResources_sizeOver50_throws400() {
        when(userRepository.findByGoogleId("google-tester")).thenReturn(Optional.of(student));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                engagementService.getBookmarkedResources("google-tester", 0, 51, "newest"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void markHelpful_success_incrementsCount() {
        when(userRepository.findByGoogleId("google-tester")).thenReturn(Optional.of(student));
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(resource));
        when(resourceHelpfulRepository.existsByUserIdAndResourceId(1L, 10L)).thenReturn(false);
        when(resourceHelpfulRepository.countByResourceId(10L)).thenReturn(1L);

        HelpfulStatusResponse response = engagementService.markHelpful(10L, "google-tester");

        assertNotNull(response);
        assertTrue(response.markedHelpful());
        assertEquals(1L, response.helpfulCount());
        verify(resourceHelpfulRepository).save(any(ResourceHelpful.class));
    }

    @Test
    void markHelpful_duplicateIsSafe() {
        when(userRepository.findByGoogleId("google-tester")).thenReturn(Optional.of(student));
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(resource));
        when(resourceHelpfulRepository.existsByUserIdAndResourceId(1L, 10L)).thenReturn(true);
        when(resourceHelpfulRepository.countByResourceId(10L)).thenReturn(1L);

        HelpfulStatusResponse response = engagementService.markHelpful(10L, "google-tester");

        assertTrue(response.markedHelpful());
        assertEquals(1L, response.helpfulCount());
        verify(resourceHelpfulRepository, never()).save(any());
    }

    @Test
    void markHelpful_missingResource_throws404() {
        when(userRepository.findByGoogleId("google-tester")).thenReturn(Optional.of(student));
        when(resourceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                engagementService.markHelpful(999L, "google-tester"));
    }

    @Test
    void removeHelpful_success_decrementsCount() {
        when(userRepository.findByGoogleId("google-tester")).thenReturn(Optional.of(student));
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(resource));
        when(resourceHelpfulRepository.countByResourceId(10L)).thenReturn(0L);

        HelpfulStatusResponse response = engagementService.removeHelpful(10L, "google-tester");

        assertNotNull(response);
        assertFalse(response.markedHelpful());
        assertEquals(0L, response.helpfulCount());
        verify(resourceHelpfulRepository).deleteByUserIdAndResourceId(1L, 10L);
    }

    @Test
    void getHelpfulStatus_returnsCountAndUserStatus() {
        when(userRepository.findByGoogleId("google-tester")).thenReturn(Optional.of(student));
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(resource));
        when(resourceHelpfulRepository.existsByUserIdAndResourceId(1L, 10L)).thenReturn(true);
        when(resourceHelpfulRepository.countByResourceId(10L)).thenReturn(12L);

        HelpfulStatusResponse response = engagementService.getHelpfulStatus(10L, "google-tester");

        assertEquals(10L, response.resourceId());
        assertEquals(12L, response.helpfulCount());
        assertTrue(response.markedHelpful());
    }
}
