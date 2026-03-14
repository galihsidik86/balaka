package com.artivisi.accountingfinance.service;

import com.artivisi.accountingfinance.TestcontainersConfiguration;
import com.artivisi.accountingfinance.entity.DraftTransaction;
import com.artivisi.accountingfinance.repository.DraftTransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for DraftTransactionService.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("DraftTransactionService Integration Tests")
class DraftTransactionServiceTest {

    @Autowired
    private DraftTransactionService draftTransactionService;

    @Autowired
    private DraftTransactionRepository draftTransactionRepository;

    // Test draft IDs from V910__draft_test_data.sql
    private static final UUID PENDING_DRAFT_1 = UUID.fromString("d0000000-0000-0000-0000-000000000001");
    private static final UUID PENDING_DRAFT_2 = UUID.fromString("d0000000-0000-0000-0000-000000000002");
    private static final UUID PENDING_DRAFT_3 = UUID.fromString("d0000000-0000-0000-0000-000000000003");
    private static final UUID APPROVED_DRAFT = UUID.fromString("d0000000-0000-0000-0000-000000000004");
    private static final UUID REJECTED_DRAFT = UUID.fromString("d0000000-0000-0000-0000-000000000005");

    @Nested
    @DisplayName("Find Operations")
    class FindOperationsTests {

        @Test
        @DisplayName("Should throw exception for non-existent draft ID")
        void shouldThrowExceptionForNonExistentId() {
            UUID randomId = UUID.randomUUID();
            assertThatThrownBy(() -> draftTransactionService.findById(randomId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Draft not found");
        }

        @Test
        @DisplayName("Should find pending drafts")
        void shouldFindPendingDrafts() {
            Page<DraftTransaction> result = draftTransactionService.findPending(PageRequest.of(0, 10));
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should find drafts by filters with null status")
        void shouldFindDraftsByFiltersWithNullStatus() {
            Page<DraftTransaction> result = draftTransactionService.findByFilters(
                    null, PageRequest.of(0, 10));
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should find drafts by filters with PENDING status")
        void shouldFindDraftsByFiltersWithPendingStatus() {
            Page<DraftTransaction> result = draftTransactionService.findByFilters(
                    DraftTransaction.Status.PENDING, PageRequest.of(0, 10));
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should find drafts by filters with APPROVED status")
        void shouldFindDraftsByFiltersWithApprovedStatus() {
            Page<DraftTransaction> result = draftTransactionService.findByFilters(
                    DraftTransaction.Status.APPROVED, PageRequest.of(0, 10));
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should find drafts by filters with REJECTED status")
        void shouldFindDraftsByFiltersWithRejectedStatus() {
            Page<DraftTransaction> result = draftTransactionService.findByFilters(
                    DraftTransaction.Status.REJECTED, PageRequest.of(0, 10));
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should find drafts by user")
        void shouldFindDraftsByUser() {
            Page<DraftTransaction> result = draftTransactionService.findByUser("admin", PageRequest.of(0, 10));
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should find pending drafts by user")
        void shouldFindPendingDraftsByUser() {
            Page<DraftTransaction> result = draftTransactionService.findPendingByUser("admin", PageRequest.of(0, 10));
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should count pending drafts")
        void shouldCountPendingDrafts() {
            long count = draftTransactionService.countPending();
            assertThat(count).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should count pending drafts by user")
        void shouldCountPendingDraftsByUser() {
            long count = draftTransactionService.countPendingByUser("admin");
            assertThat(count).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperationsTests {

        @Test
        @DisplayName("Should throw exception when deleting non-existent draft")
        void shouldThrowExceptionWhenDeletingNonExistentDraft() {
            UUID randomId = UUID.randomUUID();
            assertThatThrownBy(() -> draftTransactionService.delete(randomId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Draft not found");
        }
    }

    @Nested
    @DisplayName("Approve/Reject Operations")
    class ApproveRejectTests {

        @Test
        @DisplayName("Should throw exception when approving non-existent draft")
        void shouldThrowExceptionWhenApprovingNonExistentDraft() {
            UUID randomId = UUID.randomUUID();
            assertThatThrownBy(() -> draftTransactionService.approve(
                    randomId, UUID.randomUUID(), "test", null, "admin"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Draft not found");
        }

        @Test
        @DisplayName("Should throw exception when rejecting non-existent draft")
        void shouldThrowExceptionWhenRejectingNonExistentDraft() {
            UUID randomId = UUID.randomUUID();
            assertThatThrownBy(() -> draftTransactionService.reject(randomId, "test reason", "admin"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Draft not found");
        }

        @Test
        @DisplayName("Should reject a pending draft")
        @WithMockUser(username = "admin")
        void shouldRejectPendingDraft() {
            DraftTransaction rejected = draftTransactionService.reject(
                    PENDING_DRAFT_2, "Invalid receipt", "admin");

            assertThat(rejected.getStatus()).isEqualTo(DraftTransaction.Status.REJECTED);
            assertThat(rejected.getRejectionReason()).isEqualTo("Invalid receipt");
            assertThat(rejected.getProcessedBy()).isEqualTo("admin");
            assertThat(rejected.getProcessedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when rejecting non-pending draft")
        @WithMockUser(username = "admin")
        void shouldThrowExceptionWhenRejectingNonPendingDraft() {
            assertThatThrownBy(() -> draftTransactionService.reject(
                    APPROVED_DRAFT, "test", "admin"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not pending");
        }

        @Test
        @DisplayName("Should throw exception when approving non-pending draft")
        @WithMockUser(username = "admin")
        void shouldThrowExceptionWhenApprovingNonPendingDraft() {
            assertThatThrownBy(() -> draftTransactionService.approve(
                    REJECTED_DRAFT, UUID.randomUUID(), "test", null, "admin"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not pending");
        }
    }

    @Nested
    @DisplayName("Find By ID Operations")
    class FindByIdTests {

        @Test
        @DisplayName("Should find existing pending draft by ID")
        void shouldFindExistingDraftById() {
            DraftTransaction draft = draftTransactionService.findById(PENDING_DRAFT_1);
            assertThat(draft).isNotNull();
            assertThat(draft.getStatus()).isEqualTo(DraftTransaction.Status.PENDING);
            assertThat(draft.getMerchantName()).isEqualTo("Toko Bangunan Jaya");
        }

        @Test
        @DisplayName("Should find approved draft by ID")
        void shouldFindApprovedDraftById() {
            DraftTransaction draft = draftTransactionService.findById(APPROVED_DRAFT);
            assertThat(draft).isNotNull();
            assertThat(draft.getStatus()).isEqualTo(DraftTransaction.Status.APPROVED);
        }
    }

    @Nested
    @DisplayName("Delete Pending Draft Operations")
    class DeletePendingDraftTests {

        @Test
        @DisplayName("Should delete a pending draft")
        void shouldDeletePendingDraft() {
            draftTransactionService.delete(PENDING_DRAFT_3);

            assertThatThrownBy(() -> draftTransactionService.findById(PENDING_DRAFT_3))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-pending draft")
        void shouldThrowExceptionWhenDeletingNonPendingDraft() {
            assertThatThrownBy(() -> draftTransactionService.delete(APPROVED_DRAFT))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only pending drafts can be deleted");
        }
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveTests {

        @Test
        @DisplayName("Should save a new draft transaction")
        void shouldSaveNewDraft() {
            DraftTransaction draft = new DraftTransaction();
            draft.setSource(DraftTransaction.Source.MANUAL);
            draft.setMerchantName("Test Merchant Save");
            draft.setAmount(new BigDecimal("100000"));
            draft.setTransactionDate(LocalDate.now());
            draft.setStatus(DraftTransaction.Status.PENDING);
            draft.setCreatedBy("testuser");

            DraftTransaction saved = draftTransactionService.save(draft);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getMerchantName()).isEqualTo("Test Merchant Save");
        }
    }
}
