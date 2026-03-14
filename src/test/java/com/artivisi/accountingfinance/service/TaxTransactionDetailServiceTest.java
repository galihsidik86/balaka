package com.artivisi.accountingfinance.service;

import com.artivisi.accountingfinance.TestcontainersConfiguration;
import com.artivisi.accountingfinance.entity.TaxTransactionDetail;
import com.artivisi.accountingfinance.enums.TaxType;
import com.artivisi.accountingfinance.repository.TaxTransactionDetailRepository;
import com.artivisi.accountingfinance.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for TaxTransactionDetailService.
 * Uses test data from V907__tax_report_test_data.sql.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("TaxTransactionDetailService Integration Tests")
class TaxTransactionDetailServiceTest {

    @Autowired
    private TaxTransactionDetailService taxDetailService;

    @Autowired
    private TaxTransactionDetailRepository taxDetailRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    // Test data IDs from V907
    private static final UUID TRANSACTION_1_ID = UUID.fromString("c0000000-0000-0000-0000-000000000001");
    private static final UUID TRANSACTION_2_ID = UUID.fromString("c0000000-0000-0000-0000-000000000002");
    private static final UUID TRANSACTION_3_ID = UUID.fromString("c0000000-0000-0000-0000-000000000003");
    private static final UUID DETAIL_1_ID = UUID.fromString("d0000000-0000-0000-0000-000000000001");
    private static final UUID DETAIL_2_ID = UUID.fromString("d0000000-0000-0000-0000-000000000002");
    private static final UUID DETAIL_3_ID = UUID.fromString("d0000000-0000-0000-0000-000000000003");

    @Nested
    @DisplayName("Find Operations")
    class FindOperationsTests {

        @Test
        @DisplayName("Should find tax details by transaction ID")
        void shouldFindByTransactionId() {
            List<TaxTransactionDetail> details = taxDetailService.findByTransactionId(TRANSACTION_1_ID);

            assertThat(details).isNotEmpty();
            assertThat(details.getFirst().getTaxType()).isEqualTo(TaxType.PPN_KELUARAN);
        }

        @Test
        @DisplayName("Should find tax detail by ID")
        void shouldFindById() {
            TaxTransactionDetail detail = taxDetailService.findById(DETAIL_1_ID);

            assertThat(detail).isNotNull();
            assertThat(detail.getTaxType()).isEqualTo(TaxType.PPN_KELUARAN);
            assertThat(detail.getFakturNumber()).isEqualTo("010.000-24.00000001");
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException for non-existent detail")
        void shouldThrowForNonExistentDetail() {
            UUID randomId = UUID.randomUUID();

            assertThatThrownBy(() -> taxDetailService.findById(randomId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Tax detail tidak ditemukan");
        }

        @Test
        @DisplayName("Should return empty for transaction without details")
        void shouldReturnEmptyForTransactionWithoutDetails() {
            List<TaxTransactionDetail> details = taxDetailService.findByTransactionId(UUID.randomUUID());

            assertThat(details).isEmpty();
        }
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperationsTests {

        @Test
        @DisplayName("Should save PPN Keluaran detail")
        void shouldSavePpnKeluaranDetail() {
            TaxTransactionDetail detail = new TaxTransactionDetail();
            detail.setTaxType(TaxType.PPN_KELUARAN);
            detail.setTransactionCode("01");
            detail.setDpp(new BigDecimal("5000000"));
            detail.setPpn(new BigDecimal("550000"));
            detail.setCounterpartyName("PT Test Client");
            detail.setCounterpartyNpwp("12.345.678.9-012.000");
            detail.setCounterpartyIdType("TIN");

            TaxTransactionDetail saved = taxDetailService.save(TRANSACTION_1_ID, detail);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getTaxType()).isEqualTo(TaxType.PPN_KELUARAN);
            assertThat(saved.getDpp()).isEqualByComparingTo(new BigDecimal("5000000"));
        }

        @Test
        @DisplayName("Should save PPh 23 detail")
        void shouldSavePph23Detail() {
            TaxTransactionDetail detail = new TaxTransactionDetail();
            detail.setTaxType(TaxType.PPH_23);
            detail.setGrossAmount(new BigDecimal("1000000"));
            detail.setTaxRate(new BigDecimal("2.00"));
            detail.setTaxAmount(new BigDecimal("20000"));
            detail.setCounterpartyName("CV Vendor Test");
            detail.setCounterpartyNpwp("98.765.432.1-098.000");
            detail.setCounterpartyIdType("TIN");

            TaxTransactionDetail saved = taxDetailService.save(TRANSACTION_3_ID, detail);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getTaxType()).isEqualTo(TaxType.PPH_23);
        }

        @Test
        @DisplayName("Should throw for non-existent transaction")
        void shouldThrowForNonExistentTransaction() {
            UUID randomId = UUID.randomUUID();
            TaxTransactionDetail detail = new TaxTransactionDetail();
            detail.setTaxType(TaxType.PPN_KELUARAN);
            detail.setDpp(BigDecimal.ONE);
            detail.setPpn(BigDecimal.ONE);
            detail.setCounterpartyName("Test");

            assertThatThrownBy(() -> taxDetailService.save(randomId, detail))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Transaksi tidak ditemukan");
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should reject detail without tax type")
        void shouldRejectDetailWithoutTaxType() {
            TaxTransactionDetail detail = new TaxTransactionDetail();
            detail.setTaxType(null);
            detail.setCounterpartyName("Test");

            assertThatThrownBy(() -> taxDetailService.save(TRANSACTION_1_ID, detail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Jenis pajak (taxType) wajib diisi");
        }

        @Test
        @DisplayName("Should reject detail without counterparty name")
        void shouldRejectDetailWithoutCounterpartyName() {
            TaxTransactionDetail detail = new TaxTransactionDetail();
            detail.setTaxType(TaxType.PPN_KELUARAN);
            detail.setDpp(BigDecimal.ONE);
            detail.setPpn(BigDecimal.ONE);
            detail.setCounterpartyName(null);

            assertThatThrownBy(() -> taxDetailService.save(TRANSACTION_1_ID, detail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nama lawan transaksi wajib diisi");
        }

        @Test
        @DisplayName("Should reject e-Faktur without DPP")
        void shouldRejectEFakturWithoutDpp() {
            TaxTransactionDetail detail = new TaxTransactionDetail();
            detail.setTaxType(TaxType.PPN_KELUARAN);
            detail.setDpp(null);
            detail.setPpn(new BigDecimal("110000"));
            detail.setCounterpartyName("Test");

            assertThatThrownBy(() -> taxDetailService.save(TRANSACTION_1_ID, detail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("DPP wajib diisi untuk e-Faktur");
        }

        @Test
        @DisplayName("Should reject e-Faktur without PPN")
        void shouldRejectEFakturWithoutPpn() {
            TaxTransactionDetail detail = new TaxTransactionDetail();
            detail.setTaxType(TaxType.PPN_MASUKAN);
            detail.setDpp(new BigDecimal("1000000"));
            detail.setPpn(null);
            detail.setCounterpartyName("Test");

            assertThatThrownBy(() -> taxDetailService.save(TRANSACTION_1_ID, detail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("PPN wajib diisi untuk e-Faktur");
        }

        @Test
        @DisplayName("Should reject e-Bupot without gross amount")
        void shouldRejectEBupotWithoutGrossAmount() {
            TaxTransactionDetail detail = new TaxTransactionDetail();
            detail.setTaxType(TaxType.PPH_23);
            detail.setGrossAmount(null);
            detail.setTaxRate(new BigDecimal("2.00"));
            detail.setTaxAmount(new BigDecimal("20000"));
            detail.setCounterpartyName("Test");

            assertThatThrownBy(() -> taxDetailService.save(TRANSACTION_1_ID, detail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Jumlah bruto wajib diisi untuk e-Bupot");
        }

        @Test
        @DisplayName("Should reject e-Bupot without tax rate")
        void shouldRejectEBupotWithoutTaxRate() {
            TaxTransactionDetail detail = new TaxTransactionDetail();
            detail.setTaxType(TaxType.PPH_23);
            detail.setGrossAmount(new BigDecimal("1000000"));
            detail.setTaxRate(null);
            detail.setTaxAmount(new BigDecimal("20000"));
            detail.setCounterpartyName("Test");

            assertThatThrownBy(() -> taxDetailService.save(TRANSACTION_1_ID, detail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Tarif pajak wajib diisi untuk e-Bupot");
        }

        @Test
        @DisplayName("Should reject e-Bupot without tax amount")
        void shouldRejectEBupotWithoutTaxAmount() {
            TaxTransactionDetail detail = new TaxTransactionDetail();
            detail.setTaxType(TaxType.PPH_23);
            detail.setGrossAmount(new BigDecimal("1000000"));
            detail.setTaxRate(new BigDecimal("2.00"));
            detail.setTaxAmount(null);
            detail.setCounterpartyName("Test");

            assertThatThrownBy(() -> taxDetailService.save(TRANSACTION_1_ID, detail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Jumlah pajak wajib diisi untuk e-Bupot");
        }

        @Test
        @DisplayName("Should reject duplicate faktur number on create")
        void shouldRejectDuplicateFakturNumberOnCreate() {
            TaxTransactionDetail detail = new TaxTransactionDetail();
            detail.setTaxType(TaxType.PPN_KELUARAN);
            detail.setFakturNumber("010.000-24.00000001"); // Already exists in V907
            detail.setDpp(new BigDecimal("1000000"));
            detail.setPpn(new BigDecimal("110000"));
            detail.setCounterpartyName("Test");

            assertThatThrownBy(() -> taxDetailService.save(TRANSACTION_1_ID, detail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nomor faktur sudah digunakan");
        }

        @Test
        @DisplayName("Should reject duplicate bupot number on create")
        void shouldRejectDuplicateBupotNumberOnCreate() {
            TaxTransactionDetail detail = new TaxTransactionDetail();
            detail.setTaxType(TaxType.PPH_23);
            detail.setBupotNumber("BP-2024-00001"); // Already exists in V907
            detail.setGrossAmount(new BigDecimal("1000000"));
            detail.setTaxRate(new BigDecimal("2.00"));
            detail.setTaxAmount(new BigDecimal("20000"));
            detail.setCounterpartyName("Test");

            assertThatThrownBy(() -> taxDetailService.save(TRANSACTION_1_ID, detail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nomor bukti potong sudah digunakan");
        }

        @Test
        @DisplayName("Should reject invalid NPWP format")
        void shouldRejectInvalidNpwpFormat() {
            TaxTransactionDetail detail = new TaxTransactionDetail();
            detail.setTaxType(TaxType.PPN_KELUARAN);
            detail.setDpp(new BigDecimal("1000000"));
            detail.setPpn(new BigDecimal("110000"));
            detail.setCounterpartyName("Test");
            detail.setCounterpartyNpwp("12345"); // Too short

            assertThatThrownBy(() -> taxDetailService.save(TRANSACTION_1_ID, detail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("NPWP harus 15 atau 16 digit");
        }

        @Test
        @DisplayName("Should accept valid 15-digit NPWP")
        void shouldAcceptValid15DigitNpwp() {
            TaxTransactionDetail detail = new TaxTransactionDetail();
            detail.setTaxType(TaxType.PPN_KELUARAN);
            detail.setDpp(new BigDecimal("1000000"));
            detail.setPpn(new BigDecimal("110000"));
            detail.setCounterpartyName("Test Valid NPWP");
            detail.setCounterpartyNpwp("01.234.567.8-012.000"); // 15 digits with formatting

            TaxTransactionDetail saved = taxDetailService.save(TRANSACTION_1_ID, detail);
            assertThat(saved.getId()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateOperationsTests {

        @Test
        @DisplayName("Should update existing tax detail")
        void shouldUpdateExistingTaxDetail() {
            TaxTransactionDetail updated = new TaxTransactionDetail();
            updated.setTaxType(TaxType.PPN_KELUARAN);
            updated.setTransactionCode("01");
            updated.setDpp(new BigDecimal("20000000"));
            updated.setPpn(new BigDecimal("2200000"));
            updated.setCounterpartyName("Updated Client Name");
            updated.setCounterpartyNpwp("01.234.567.8-901.000");
            updated.setCounterpartyIdType("TIN");

            TaxTransactionDetail result = taxDetailService.update(DETAIL_1_ID, updated);

            assertThat(result.getDpp()).isEqualByComparingTo(new BigDecimal("20000000"));
            assertThat(result.getPpn()).isEqualByComparingTo(new BigDecimal("2200000"));
            assertThat(result.getCounterpartyName()).isEqualTo("Updated Client Name");
        }

        @Test
        @DisplayName("Should throw for updating non-existent detail")
        void shouldThrowForUpdatingNonExistentDetail() {
            UUID randomId = UUID.randomUUID();
            TaxTransactionDetail updated = new TaxTransactionDetail();
            updated.setTaxType(TaxType.PPN_KELUARAN);
            updated.setDpp(BigDecimal.ONE);
            updated.setPpn(BigDecimal.ONE);
            updated.setCounterpartyName("Test");

            assertThatThrownBy(() -> taxDetailService.update(randomId, updated))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Tax detail tidak ditemukan");
        }

        @Test
        @DisplayName("Should allow same faktur number on update for same detail")
        void shouldAllowSameFakturNumberOnUpdateForSameDetail() {
            TaxTransactionDetail updated = new TaxTransactionDetail();
            updated.setTaxType(TaxType.PPN_KELUARAN);
            updated.setFakturNumber("010.000-24.00000001"); // Same number as existing DETAIL_1
            updated.setTransactionCode("01");
            updated.setDpp(new BigDecimal("15000000"));
            updated.setPpn(new BigDecimal("1650000"));
            updated.setCounterpartyName("Test Update Same Faktur");
            updated.setCounterpartyNpwp("01.234.567.8-901.000");
            updated.setCounterpartyIdType("TIN");

            // Should not throw - updating same record with same faktur number
            TaxTransactionDetail result = taxDetailService.update(DETAIL_1_ID, updated);
            assertThat(result.getDpp()).isEqualByComparingTo(new BigDecimal("15000000"));
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperationsTests {

        @Test
        @DisplayName("Should delete tax detail")
        void shouldDeleteTaxDetail() {
            // Create a new detail first
            TaxTransactionDetail detail = new TaxTransactionDetail();
            detail.setTaxType(TaxType.PPN_KELUARAN);
            detail.setDpp(new BigDecimal("1000000"));
            detail.setPpn(new BigDecimal("110000"));
            detail.setCounterpartyName("To Delete");
            TaxTransactionDetail saved = taxDetailService.save(TRANSACTION_1_ID, detail);

            taxDetailService.delete(saved.getId());

            assertThat(taxDetailRepository.findById(saved.getId())).isEmpty();
        }

        @Test
        @DisplayName("Should throw for deleting non-existent detail")
        void shouldThrowForDeletingNonExistentDetail() {
            UUID randomId = UUID.randomUUID();

            assertThatThrownBy(() -> taxDetailService.delete(randomId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Tax detail tidak ditemukan");
        }
    }

    @Nested
    @DisplayName("Find Transaction IDs With Details")
    class FindTransactionIdsTests {

        @Test
        @DisplayName("Should find transaction IDs that have tax details")
        void shouldFindTransactionIdsWithDetails() {
            Set<UUID> ids = taxDetailService.findTransactionIdsWithDetails(
                    List.of(TRANSACTION_1_ID, TRANSACTION_2_ID, UUID.randomUUID()));

            assertThat(ids).contains(TRANSACTION_1_ID, TRANSACTION_2_ID);
        }

        @Test
        @DisplayName("Should return empty set for null input")
        void shouldReturnEmptySetForNullInput() {
            Set<UUID> ids = taxDetailService.findTransactionIdsWithDetails(null);
            assertThat(ids).isEmpty();
        }

        @Test
        @DisplayName("Should return empty set for empty input")
        void shouldReturnEmptySetForEmptyInput() {
            Set<UUID> ids = taxDetailService.findTransactionIdsWithDetails(List.of());
            assertThat(ids).isEmpty();
        }
    }

    @Nested
    @DisplayName("Auto Populate and Suggest")
    class AutoPopulateTests {

        @Test
        @DisplayName("Should skip auto-populate when details already exist")
        void shouldSkipAutoPopulateWhenDetailsExist() {
            var transaction = transactionRepository.findById(TRANSACTION_1_ID).orElseThrow();

            int count = taxDetailService.autoPopulateFromTransaction(transaction);

            // Transaction 1 already has tax details from V907
            assertThat(count).isZero();
        }

        @Test
        @DisplayName("Should suggest from transaction with PPN template")
        void shouldSuggestFromTransactionWithPpnTemplate() {
            var transaction = transactionRepository.findById(TRANSACTION_1_ID).orElseThrow();

            List<TaxTransactionDetailService.TaxDetailSuggestion> suggestions =
                    taxDetailService.suggestFromTransaction(transaction);

            // Should have suggestions based on template name containing "PPN"
            assertThat(suggestions).isNotNull();
        }
    }
}
