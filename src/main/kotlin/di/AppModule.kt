package com.alsoug.di

import com.alsoug.transaction.TransactionService
import com.alsoug.transaction.db.TransactionRepository
import org.koin.dsl.module

val appModule = module {
    single { TransactionRepository() }
    single { TransactionService(get()) }
}
