package com.example.femail.labels;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.femail.Converters;

@Database(entities = {LabelItem.class}, version = 6)
@TypeConverters({Converters.class})

public abstract class LabelDatabase extends RoomDatabase {
    public abstract LabelDao labelDao();

    private static volatile LabelDatabase INSTANCE;

    public static LabelDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (LabelDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    LabelDatabase.class, "label_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
