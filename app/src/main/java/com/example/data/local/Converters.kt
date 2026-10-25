package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AccountType
import com.example.data.model.DebtStatus
import com.example.data.model.DebtType
import com.example.data.model.FeeCategory
import com.example.data.model.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType?): String = type?.name ?: TransactionType.EXPENSE.name

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType =
        value?.let { runCatching { TransactionType.valueOf(it) }.getOrDefault(TransactionType.EXPENSE) }
            ?: TransactionType.EXPENSE

    @TypeConverter
    fun fromAccountType(type: AccountType?): String = type?.name ?: AccountType.CASH.name

    @TypeConverter
    fun toAccountType(value: String?): AccountType =
        value?.let { runCatching { AccountType.valueOf(it) }.getOrDefault(AccountType.CASH) }
            ?: AccountType.CASH

    @TypeConverter
    fun fromFeeCategory(cat: FeeCategory?): String = cat?.name ?: FeeCategory.TUITION.name

    @TypeConverter
    fun toFeeCategory(value: String?): FeeCategory =
        value?.let { runCatching { FeeCategory.valueOf(it) }.getOrDefault(FeeCategory.TUITION) }
            ?: FeeCategory.TUITION

    @TypeConverter
    fun fromDebtType(type: DebtType?): String = type?.name ?: DebtType.DEBTOR.name

    @TypeConverter
    fun toDebtType(value: String?): DebtType =
        value?.let { runCatching { DebtType.valueOf(it) }.getOrDefault(DebtType.DEBTOR) }
            ?: DebtType.DEBTOR

    @TypeConverter
    fun fromDebtStatus(status: DebtStatus?): String = status?.name ?: DebtStatus.ACTIVE.name

    @TypeConverter
    fun toDebtStatus(value: String?): DebtStatus =
        value?.let { runCatching { DebtStatus.valueOf(it) }.getOrDefault(DebtStatus.ACTIVE) }
            ?: DebtStatus.ACTIVE
}
