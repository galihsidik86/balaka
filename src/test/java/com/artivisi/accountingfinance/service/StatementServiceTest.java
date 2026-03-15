package com.artivisi.accountingfinance.service;

import com.artivisi.accountingfinance.TestcontainersConfiguration;
import com.artivisi.accountingfinance.dto.AccountStatement;
import com.artivisi.accountingfinance.dto.StatementEntry;
import com.artivisi.accountingfinance.entity.Bill;
import com.artivisi.accountingfinance.entity.BillPayment;
import com.artivisi.accountingfinance.entity.Client;
import com.artivisi.accountingfinance.entity.Invoice;
import com.artivisi.accountingfinance.entity.InvoicePayment;
import com.artivisi.accountingfinance.entity.Vendor;
import com.artivisi.accountingfinance.enums.BillStatus;
import com.artivisi.accountingfinance.enums.InvoiceStatus;
import com.artivisi.accountingfinance.enums.PaymentMethod;
import com.artivisi.accountingfinance.repository.BillPaymentRepository;
import com.artivisi.accountingfinance.repository.BillRepository;
import com.artivisi.accountingfinance.repository.ClientRepository;
import com.artivisi.accountingfinance.repository.InvoicePaymentRepository;
import com.artivisi.accountingfinance.repository.InvoiceRepository;
import com.artivisi.accountingfinance.repository.VendorRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for StatementService.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("StatementService Integration Tests")
class StatementServiceTest {

    @Autowired
    private StatementService statementService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoicePaymentRepository invoicePaymentRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillPaymentRepository billPaymentRepository;

    @Nested
    @DisplayName("Client Statement")
    class ClientStatementTests {

        @Test
        @DisplayName("Should generate empty client statement for non-existent client")
        void shouldGenerateEmptyStatementForNonExistentClient() {
            UUID randomId = UUID.randomUUID();
            LocalDate dateFrom = LocalDate.of(2024, 1, 1);
            LocalDate dateTo = LocalDate.of(2024, 12, 31);

            AccountStatement statement = statementService.generateClientStatement(
                    randomId, "CLI-000", "Non-existent Client", dateFrom, dateTo);

            assertThat(statement).isNotNull();
            assertThat(statement.entityType()).isEqualTo("CLIENT");
            assertThat(statement.entityCode()).isEqualTo("CLI-000");
            assertThat(statement.entityName()).isEqualTo("Non-existent Client");
            assertThat(statement.dateFrom()).isEqualTo(dateFrom);
            assertThat(statement.dateTo()).isEqualTo(dateTo);
            assertThat(statement.openingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(statement.entries()).isEmpty();
            assertThat(statement.closingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should generate client statement with invoices and payments")
        void shouldGenerateClientStatementWithInvoicesAndPayments() {
            Client client = createTestClient();
            Invoice invoice = createTestInvoice(client, LocalDate.of(2024, 3, 1),
                    new BigDecimal("10000000"), InvoiceStatus.PAID);
            createTestInvoicePayment(invoice, LocalDate.of(2024, 3, 15),
                    new BigDecimal("10000000"));

            AccountStatement statement = statementService.generateClientStatement(
                    client.getId(), client.getCode(), client.getName(),
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            assertThat(statement).isNotNull();
            assertThat(statement.entityType()).isEqualTo("CLIENT");
            assertThat(statement.entries()).hasSize(2);

            // First entry should be invoice
            StatementEntry invoiceEntry = statement.entries().get(0);
            assertThat(invoiceEntry.type()).isEqualTo("INVOICE");
            assertThat(invoiceEntry.invoiceAmount()).isEqualByComparingTo(new BigDecimal("10000000"));
            assertThat(invoiceEntry.paymentAmount()).isEqualByComparingTo(BigDecimal.ZERO);

            // Second entry should be payment
            StatementEntry paymentEntry = statement.entries().get(1);
            assertThat(paymentEntry.type()).isEqualTo("PAYMENT");
            assertThat(paymentEntry.invoiceAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(paymentEntry.paymentAmount()).isEqualByComparingTo(new BigDecimal("10000000"));

            // Closing balance should be zero (fully paid)
            assertThat(statement.closingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should calculate opening balance from prior invoices and payments")
        void shouldCalculateOpeningBalanceFromPriorData() {
            Client client = createTestClient();
            // Invoice before the statement period
            createTestInvoice(client, LocalDate.of(2024, 1, 15),
                    new BigDecimal("5000000"), InvoiceStatus.SENT);

            AccountStatement statement = statementService.generateClientStatement(
                    client.getId(), client.getCode(), client.getName(),
                    LocalDate.of(2024, 6, 1), LocalDate.of(2024, 12, 31));

            assertThat(statement.openingBalance()).isEqualByComparingTo(new BigDecimal("5000000"));
        }

        @Test
        @DisplayName("Should merge invoices and payments chronologically")
        void shouldMergeInvoicesAndPaymentsChronologically() {
            Client client = createTestClient();
            Invoice inv1 = createTestInvoice(client, LocalDate.of(2024, 3, 1),
                    new BigDecimal("8000000"), InvoiceStatus.PAID);
            createTestInvoice(client, LocalDate.of(2024, 3, 20),
                    new BigDecimal("5000000"), InvoiceStatus.SENT);
            createTestInvoicePayment(inv1, LocalDate.of(2024, 3, 10),
                    new BigDecimal("8000000"));

            AccountStatement statement = statementService.generateClientStatement(
                    client.getId(), client.getCode(), client.getName(),
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            assertThat(statement.entries()).hasSize(3);
            // Should be: invoice(Mar 1), payment(Mar 10), invoice(Mar 20)
            assertThat(statement.entries().get(0).type()).isEqualTo("INVOICE");
            assertThat(statement.entries().get(1).type()).isEqualTo("PAYMENT");
            assertThat(statement.entries().get(2).type()).isEqualTo("INVOICE");
        }

        @Test
        @DisplayName("Should handle only invoices without payments")
        void shouldHandleOnlyInvoicesWithoutPayments() {
            Client client = createTestClient();
            createTestInvoice(client, LocalDate.of(2024, 3, 1),
                    new BigDecimal("15000000"), InvoiceStatus.SENT);

            AccountStatement statement = statementService.generateClientStatement(
                    client.getId(), client.getCode(), client.getName(),
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            assertThat(statement.entries()).hasSize(1);
            assertThat(statement.entries().get(0).type()).isEqualTo("INVOICE");
            assertThat(statement.closingBalance()).isEqualByComparingTo(new BigDecimal("15000000"));
        }

        @Test
        @DisplayName("Should handle only payments without invoices in period")
        void shouldHandleOnlyPaymentsWithoutInvoicesInPeriod() {
            Client client = createTestClient();
            // Invoice before the period
            Invoice invoice = createTestInvoice(client, LocalDate.of(2024, 1, 1),
                    new BigDecimal("10000000"), InvoiceStatus.PAID);
            // Payment within the period
            createTestInvoicePayment(invoice, LocalDate.of(2024, 6, 15),
                    new BigDecimal("10000000"));

            AccountStatement statement = statementService.generateClientStatement(
                    client.getId(), client.getCode(), client.getName(),
                    LocalDate.of(2024, 6, 1), LocalDate.of(2024, 12, 31));

            assertThat(statement.entries()).hasSize(1);
            assertThat(statement.entries().get(0).type()).isEqualTo("PAYMENT");
            assertThat(statement.openingBalance()).isEqualByComparingTo(new BigDecimal("10000000"));
            assertThat(statement.closingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should show running balance in entries")
        void shouldShowRunningBalanceInEntries() {
            Client client = createTestClient();
            Invoice inv = createTestInvoice(client, LocalDate.of(2024, 3, 1),
                    new BigDecimal("20000000"), InvoiceStatus.PARTIAL);
            createTestInvoicePayment(inv, LocalDate.of(2024, 3, 15),
                    new BigDecimal("5000000"));

            AccountStatement statement = statementService.generateClientStatement(
                    client.getId(), client.getCode(), client.getName(),
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            assertThat(statement.entries()).hasSize(2);
            // After invoice: balance = 20M
            assertThat(statement.entries().get(0).runningBalance()).isEqualByComparingTo(new BigDecimal("20000000"));
            // After payment: balance = 15M
            assertThat(statement.entries().get(1).runningBalance()).isEqualByComparingTo(new BigDecimal("15000000"));
        }

        @Test
        @DisplayName("Should use dash for payment without reference number")
        void shouldUseDashForPaymentWithoutReferenceNumber() {
            Client client = createTestClient();
            Invoice invoice = createTestInvoice(client, LocalDate.of(2024, 3, 1),
                    new BigDecimal("5000000"), InvoiceStatus.PAID);
            createTestInvoicePaymentWithoutRef(invoice, LocalDate.of(2024, 3, 15),
                    new BigDecimal("5000000"));

            AccountStatement statement = statementService.generateClientStatement(
                    client.getId(), client.getCode(), client.getName(),
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            StatementEntry paymentEntry = statement.entries().stream()
                    .filter(e -> "PAYMENT".equals(e.type()))
                    .findFirst()
                    .orElseThrow();
            assertThat(paymentEntry.referenceNumber()).isEqualTo("-");
        }
    }

    @Nested
    @DisplayName("Vendor Statement")
    class VendorStatementTests {

        @Test
        @DisplayName("Should generate empty vendor statement for non-existent vendor")
        void shouldGenerateEmptyStatementForNonExistentVendor() {
            UUID randomId = UUID.randomUUID();
            LocalDate dateFrom = LocalDate.of(2024, 1, 1);
            LocalDate dateTo = LocalDate.of(2024, 12, 31);

            AccountStatement statement = statementService.generateVendorStatement(
                    randomId, "VND-000", "Non-existent Vendor", dateFrom, dateTo);

            assertThat(statement).isNotNull();
            assertThat(statement.entityType()).isEqualTo("VENDOR");
            assertThat(statement.entityCode()).isEqualTo("VND-000");
            assertThat(statement.openingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(statement.entries()).isEmpty();
            assertThat(statement.closingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should generate vendor statement with bills and payments")
        void shouldGenerateVendorStatementWithBillsAndPayments() {
            Vendor vendor = createTestVendor();
            Bill bill = createTestBill(vendor, LocalDate.of(2024, 4, 1),
                    new BigDecimal("7000000"), BillStatus.PAID);
            createTestBillPayment(bill, LocalDate.of(2024, 4, 20),
                    new BigDecimal("7000000"));

            AccountStatement statement = statementService.generateVendorStatement(
                    vendor.getId(), vendor.getCode(), vendor.getName(),
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            assertThat(statement).isNotNull();
            assertThat(statement.entityType()).isEqualTo("VENDOR");
            assertThat(statement.entries()).hasSize(2);

            // First entry should be bill
            StatementEntry billEntry = statement.entries().get(0);
            assertThat(billEntry.type()).isEqualTo("BILL");
            assertThat(billEntry.invoiceAmount()).isEqualByComparingTo(new BigDecimal("7000000"));

            // Second entry should be payment
            StatementEntry paymentEntry = statement.entries().get(1);
            assertThat(paymentEntry.type()).isEqualTo("PAYMENT");
            assertThat(paymentEntry.paymentAmount()).isEqualByComparingTo(new BigDecimal("7000000"));

            assertThat(statement.closingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should calculate vendor opening balance from prior bills")
        void shouldCalculateVendorOpeningBalance() {
            Vendor vendor = createTestVendor();
            createTestBill(vendor, LocalDate.of(2024, 1, 10),
                    new BigDecimal("3000000"), BillStatus.APPROVED);

            AccountStatement statement = statementService.generateVendorStatement(
                    vendor.getId(), vendor.getCode(), vendor.getName(),
                    LocalDate.of(2024, 6, 1), LocalDate.of(2024, 12, 31));

            assertThat(statement.openingBalance()).isEqualByComparingTo(new BigDecimal("3000000"));
        }

        @Test
        @DisplayName("Should merge bills and payments chronologically")
        void shouldMergeBillsAndPaymentsChronologically() {
            Vendor vendor = createTestVendor();
            Bill bill1 = createTestBill(vendor, LocalDate.of(2024, 4, 1),
                    new BigDecimal("6000000"), BillStatus.PAID);
            createTestBill(vendor, LocalDate.of(2024, 4, 25),
                    new BigDecimal("4000000"), BillStatus.APPROVED);
            createTestBillPayment(bill1, LocalDate.of(2024, 4, 15),
                    new BigDecimal("6000000"));

            AccountStatement statement = statementService.generateVendorStatement(
                    vendor.getId(), vendor.getCode(), vendor.getName(),
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            assertThat(statement.entries()).hasSize(3);
            // bill(Apr 1), payment(Apr 15), bill(Apr 25)
            assertThat(statement.entries().get(0).type()).isEqualTo("BILL");
            assertThat(statement.entries().get(1).type()).isEqualTo("PAYMENT");
            assertThat(statement.entries().get(2).type()).isEqualTo("BILL");
        }

        @Test
        @DisplayName("Should handle only bills without payments")
        void shouldHandleOnlyBillsWithoutPayments() {
            Vendor vendor = createTestVendor();
            createTestBill(vendor, LocalDate.of(2024, 4, 1),
                    new BigDecimal("12000000"), BillStatus.APPROVED);

            AccountStatement statement = statementService.generateVendorStatement(
                    vendor.getId(), vendor.getCode(), vendor.getName(),
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            assertThat(statement.entries()).hasSize(1);
            assertThat(statement.entries().get(0).type()).isEqualTo("BILL");
            assertThat(statement.closingBalance()).isEqualByComparingTo(new BigDecimal("12000000"));
        }

        @Test
        @DisplayName("Should use dash for bill payment without reference number")
        void shouldUseDashForBillPaymentWithoutReferenceNumber() {
            Vendor vendor = createTestVendor();
            Bill bill = createTestBill(vendor, LocalDate.of(2024, 4, 1),
                    new BigDecimal("3000000"), BillStatus.PAID);
            createTestBillPaymentWithoutRef(bill, LocalDate.of(2024, 4, 15),
                    new BigDecimal("3000000"));

            AccountStatement statement = statementService.generateVendorStatement(
                    vendor.getId(), vendor.getCode(), vendor.getName(),
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            StatementEntry paymentEntry = statement.entries().stream()
                    .filter(e -> "PAYMENT".equals(e.type()))
                    .findFirst()
                    .orElseThrow();
            assertThat(paymentEntry.referenceNumber()).isEqualTo("-");
        }
    }

    // Helper methods

    private Client createTestClient() {
        Client client = new Client();
        client.setCode("CLI-TEST-" + System.nanoTime());
        client.setName("Test Client " + System.currentTimeMillis());
        client.setActive(true);
        return clientRepository.save(client);
    }

    private Vendor createTestVendor() {
        Vendor vendor = new Vendor();
        vendor.setCode("VND-TEST-" + System.nanoTime());
        vendor.setName("Test Vendor " + System.currentTimeMillis());
        vendor.setActive(true);
        return vendorRepository.save(vendor);
    }

    private Invoice createTestInvoice(Client client, LocalDate invoiceDate,
                                       BigDecimal amount, InvoiceStatus status) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-TEST-" + System.nanoTime());
        invoice.setClient(client);
        invoice.setInvoiceDate(invoiceDate);
        invoice.setDueDate(invoiceDate.plusDays(30));
        invoice.setAmount(amount);
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setStatus(status);
        return invoiceRepository.save(invoice);
    }

    private void createTestInvoicePayment(Invoice invoice, LocalDate paymentDate,
                                           BigDecimal amount) {
        InvoicePayment payment = new InvoicePayment();
        payment.setInvoice(invoice);
        payment.setPaymentDate(paymentDate);
        payment.setAmount(amount);
        payment.setPaymentMethod(PaymentMethod.TRANSFER);
        payment.setReferenceNumber("REF-" + System.nanoTime());
        invoicePaymentRepository.save(payment);
    }

    private void createTestInvoicePaymentWithoutRef(Invoice invoice, LocalDate paymentDate,
                                                     BigDecimal amount) {
        InvoicePayment payment = new InvoicePayment();
        payment.setInvoice(invoice);
        payment.setPaymentDate(paymentDate);
        payment.setAmount(amount);
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setReferenceNumber(null);
        invoicePaymentRepository.save(payment);
    }

    private Bill createTestBill(Vendor vendor, LocalDate billDate,
                                 BigDecimal amount, BillStatus status) {
        Bill bill = new Bill();
        bill.setBillNumber("BILL-TEST-" + System.nanoTime());
        bill.setVendor(vendor);
        bill.setBillDate(billDate);
        bill.setDueDate(billDate.plusDays(30));
        bill.setAmount(amount);
        bill.setTaxAmount(BigDecimal.ZERO);
        bill.setStatus(status);
        return billRepository.save(bill);
    }

    private void createTestBillPayment(Bill bill, LocalDate paymentDate,
                                        BigDecimal amount) {
        BillPayment payment = new BillPayment();
        payment.setBill(bill);
        payment.setPaymentDate(paymentDate);
        payment.setAmount(amount);
        payment.setPaymentMethod(PaymentMethod.TRANSFER);
        payment.setReferenceNumber("REF-" + System.nanoTime());
        billPaymentRepository.save(payment);
    }

    private void createTestBillPaymentWithoutRef(Bill bill, LocalDate paymentDate,
                                                  BigDecimal amount) {
        BillPayment payment = new BillPayment();
        payment.setBill(bill);
        payment.setPaymentDate(paymentDate);
        payment.setAmount(amount);
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setReferenceNumber(null);
        billPaymentRepository.save(payment);
    }
}
