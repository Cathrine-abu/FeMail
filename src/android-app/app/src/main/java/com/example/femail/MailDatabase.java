package com.example.femail;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.femail.MailDao;
import com.example.femail.MailItem;
import com.example.femail.User;
import com.example.femail.UserDao;

@Database(entities = {MailItem.class, User.class}, version = 2)
@TypeConverters({Converters.class})

public abstract class MailDatabase extends RoomDatabase {
    public abstract MailDao mailDao();
    public abstract UserDao userDao();

    private static volatile MailDatabase INSTANCE;

    public static MailDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MailDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    MailDatabase.class, "mail_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
