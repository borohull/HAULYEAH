package model;

import model.enums.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FinanceLedger Tests")
class FinanceLedgerTest {

    @Test
    @DisplayName("Balance above zero should not be bankrupt")
    void testPositiveBalanceIsNotBankrupt() {
        FinanceLedger ledger = new FinanceLedger(100.0);

        assertFalse(ledger.isBankrupt(), "Positive balance should not be bankrupt");
    }

    @Test
    @DisplayName("Balance at zero or below should be bankrupt")
    void testZeroOrNegativeBalanceIsBankrupt() {
        FinanceLedger zeroLedger = new FinanceLedger(10.0);
        zeroLedger.spend(10.0, TransactionType.OTHER, "Test expense");

        FinanceLedger negativeLedger = new FinanceLedger(10.0);
        negativeLedger.spend(11.0, TransactionType.OTHER, "Test expense");

        assertTrue(zeroLedger.isBankrupt(), "Zero balance should be bankrupt");
        assertTrue(negativeLedger.isBankrupt(), "Negative balance should be bankrupt");
    }
}


