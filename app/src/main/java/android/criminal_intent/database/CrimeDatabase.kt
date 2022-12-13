package android.criminal_intent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.criminal_intent.Crime

private const val DATABASE_NAME = "crime-database"


@Database(entities = [ Crime::class ], version = 2)
@TypeConverters(CrimeTypeConverters::class)
abstract class CrimeDatabase : RoomDatabase(){

    abstract fun crimeDao(): CrimeDao
}

val migration_1_2 = object : Migration(1, 2){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE Crime ADD COLUMN suspect TEXT NOT NULL DEFAULT ''"
        )
    }
}