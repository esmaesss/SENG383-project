package com.kidtask.services;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.kidtask.models.Task;
import com.kidtask.models.User;
import com.kidtask.models.Wish;

public class FileHandler {
    private static final String DIR = "data/";
    public static void saveTasks(List<Task> t) { save(DIR + "tasks.dat", t); }
    public static List<Task> loadTasks() { return load(DIR + "tasks.dat"); }
    public static void saveWishes(List<Wish> w) { save(DIR + "wishes.dat", w); }
    public static List<Wish> loadWishes() { return load(DIR + "wishes.dat"); }
    public static void saveUsers(List<User> u) { save(DIR + "users.dat", u); }
    public static List<User> loadUsers() { return load(DIR + "users.dat"); }

    private static void save(String path, Object obj) {
        try {
            new File(DIR).mkdirs();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
            oos.writeObject(obj);
            oos.close();
        } catch (IOException e) { e.printStackTrace(); }
    }
    @SuppressWarnings("unchecked")
    private static <T> List<T> load(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) return new ArrayList<>();
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            List<T> list = (List<T>) ois.readObject();
            ois.close();
            return list;
        } catch (Exception e) { return new ArrayList<>(); }
    }
}