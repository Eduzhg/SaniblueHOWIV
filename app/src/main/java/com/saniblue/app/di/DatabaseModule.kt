package com.saniblue.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.saniblue.app.data.local.database.DatabasePrePopulate
import com.saniblue.app.data.local.database.SaniblueDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SaniblueDatabase {
        return Room.databaseBuilder(
            context,
            SaniblueDatabase::class.java,
            SaniblueDatabase.DATABASE_NAME
        )
            .addCallback(object : RoomDatabase.Callback() {
                // onOpen roda SEMPRE após as tabelas existirem (fresh install,
                // migração destrutiva, etc.). Populamos aqui só quando está vazio.
                // (Não dá para popular em onCreate/onDestructiveMigration: este último
                //  é chamado durante o dropAllTables, antes de recriar as tabelas.)
                override fun onOpen(database: SupportSQLiteDatabase) {
                    super.onOpen(database)
                    prePopulateSeVazio(database)
                }
            })
            .fallbackToDestructiveMigration()
            .build()
    }

    /** Popula apenas se a tabela de usuários estiver vazia (idempotente). */
    private fun prePopulateSeVazio(database: SupportSQLiteDatabase) {
        val vazio = database.query("SELECT COUNT(*) FROM usuarios").use { cursor ->
            cursor.moveToFirst() && cursor.getInt(0) == 0
        }
        if (vazio) prePopulate(database)
    }

    /** Insere modelos de hidrômetro e usuários padrão usando SQL direto (síncrono). */
    private fun prePopulate(database: SupportSQLiteDatabase) {
        DatabasePrePopulate.getModelosHidrometro().forEach { m ->
            database.execSQL(
                """INSERT INTO hidrometro_modelos
                   (nome, descricao, norma, letra, classe_r,
                    vazao_nominal, vazao_transicao, vazao_minima, ativo, created_at)
                   VALUES (?,?,?,?,?,?,?,?,?,?)""",
                arrayOf(
                    m.nome, m.descricao, m.norma, m.letra, m.classeR,
                    m.vazaoNominal, m.vazaoTransicao, m.vazaoMinima,
                    if (m.ativo) 1 else 0, m.createdAt
                )
            )
        }
        DatabasePrePopulate.getUsuariosDefault().forEach { u ->
            database.execSQL(
                """INSERT INTO usuarios
                   (nome, login, senha_hash, cargo, email, ativo, created_at)
                   VALUES (?,?,?,?,?,?,?)""",
                arrayOf(
                    u.nome, u.login, u.senhaHash, u.cargo, u.email,
                    if (u.ativo) 1 else 0, u.createdAt
                )
            )
        }
    }

    @Provides
    fun provideUsuarioDao(db: SaniblueDatabase) = db.usuarioDao()

    @Provides
    fun provideHidrometroModeloDao(db: SaniblueDatabase) = db.hidrometroModeloDao()

    @Provides
    fun provideEnsaioDao(db: SaniblueDatabase) = db.ensaioDao()

    @Provides
    fun provideVazaoEnsaioDao(db: SaniblueDatabase) = db.vazaoEnsaioDao()
}
