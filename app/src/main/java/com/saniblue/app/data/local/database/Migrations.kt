package com.saniblue.app.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migrações do banco. Sem uma migração declarada, o Room cai no
 * fallbackToDestructiveMigration e APAGA os ensaios já gravados no tablet ao
 * atualizar o APK — por isso toda coluna nova precisa de um ALTER TABLE aqui.
 */

/** v11 → v12: assinaturas do cliente e do técnico coletadas na tela. */
val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ensaios ADD COLUMN assinatura_cliente_path TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE ensaios ADD COLUMN assinatura_tecnico_path TEXT NOT NULL DEFAULT ''")
    }
}

/** v12 → v13: uma foto por leitura de cada medicao (auditoria em campo). */
val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE vazao_ensaios ADD COLUMN m1_foto_padrao_inicial TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE vazao_ensaios ADD COLUMN m1_foto_padrao_final TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE vazao_ensaios ADD COLUMN m1_foto_leitura_inicial TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE vazao_ensaios ADD COLUMN m1_foto_leitura_final TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE vazao_ensaios ADD COLUMN m2_foto_padrao_inicial TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE vazao_ensaios ADD COLUMN m2_foto_padrao_final TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE vazao_ensaios ADD COLUMN m2_foto_leitura_inicial TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE vazao_ensaios ADD COLUMN m2_foto_leitura_final TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE vazao_ensaios ADD COLUMN m3_foto_padrao_inicial TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE vazao_ensaios ADD COLUMN m3_foto_padrao_final TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE vazao_ensaios ADD COLUMN m3_foto_leitura_inicial TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE vazao_ensaios ADD COLUMN m3_foto_leitura_final TEXT NOT NULL DEFAULT ''")
    }
}

/** Todas as migrações conhecidas, na ordem. */
val MIGRATIONS = arrayOf(MIGRATION_11_12, MIGRATION_12_13)
