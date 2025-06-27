package com.example.femail;

import androidx.room.TypeConverter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Converters {

    @TypeConverter
    public static String fromList(List<String> list) {
        return list == null ? null : String.join(",", list);
    }

    @TypeConverter
    public static List<String> toList(String data) {
        return data == null || data.isEmpty()
                ? null
                : Arrays.stream(data.split(",")).collect(Collectors.toList());
    }
}
