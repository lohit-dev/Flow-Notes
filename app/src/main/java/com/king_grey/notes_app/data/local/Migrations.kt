package com.king_grey.notes_app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 1 to 2: the `locked` column on notes is gone. SQLite before Android 15 cannot drop a column,
 * so the table is rebuilt without it and the notes are copied across.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `notes_new` (" +
                "`id` INTEGER NOT NULL, `title` TEXT NOT NULL, `blocks_json` TEXT NOT NULL, " +
                "`folder_id` INTEGER, `pinned` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, " +
                "`edited_at` INTEGER NOT NULL, `deleted_at` INTEGER, PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`folder_id`) REFERENCES `folders`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )",
        )
        db.execSQL(
            "INSERT INTO `notes_new` (`id`, `title`, `blocks_json`, `folder_id`, `pinned`, " +
                "`created_at`, `edited_at`, `deleted_at`) " +
                "SELECT `id`, `title`, `blocks_json`, `folder_id`, `pinned`, " +
                "`created_at`, `edited_at`, `deleted_at` FROM `notes`",
        )
        db.execSQL("DROP TABLE `notes`")
        db.execSQL("ALTER TABLE `notes_new` RENAME TO `notes`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_folder_id` ON `notes` (`folder_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_deleted_at` ON `notes` (`deleted_at`)")
    }
}
