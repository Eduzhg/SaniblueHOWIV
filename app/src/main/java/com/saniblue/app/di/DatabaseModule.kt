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
                // Popula de forma SÍNCRONA (SQL direto) — garante que os dados
                // existam antes de qualquer consulta (ex.: primeira tentativa de login).
                override fun onCreate(database: SupportSQLiteDatabase) {
                    super.onCreate(database)
                    prePopulate(database)
                }

                // fallbackToDestructiveMigration recria as tabelas sem chamar onCreate;
                // por isso também populamos aqui ao subir a versão do banco.
                override fun onDestructiveMigration(database: SupportSQLiteDatabase) {
                    super.onDestructiveMigration(database)
                    prePopulate(database)
                }
            })
            .fallbackToDestructiveMigration()
            .build()
    }

    /** Insere modelos de hidrômetro e usuários padrão usando SQL direto (síncrono). */
    private fun prePopulate(database: SupportSQLiteDatabase) {
        DatabasePrePopulate.getModelosHidrometro().forEach { m ->
            database.execSQL(
                """INSERT INTO hidrometro_modelos
                   (nome, descricao, vazao_nominal, vazao_transicao, vazao_minima,
                    limite_nominal_min, limite_nominal_max,
                    limite_transicao_min, limite_transicao_max,
                    limite_minima_min, limite_minima_max, ativo, created_at)
                   VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)""",
                arrayOf(
                    m.nome, m.descricao, m.vazaoNominal, m.vazaoTransicao, m.vazaoMinima,
                    m.limiteNominalMin, m.limiteNominalMax,
                    m.limiteTransicaoMin, m.limiteTransicaoMax,
                    m.limiteMinimaMin, m.limiteMinimaMax,
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

    @Provides
    fun provideFotoEnsaioDao(db: SaniblueDatabase) = db.fotoEnsaioDao()
}
