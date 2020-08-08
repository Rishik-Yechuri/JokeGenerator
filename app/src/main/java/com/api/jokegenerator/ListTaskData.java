package com.api.jokegenerator;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ListTaskData {
    public static List<Task> createTasksList(){
        List<Task> tasks = new ArrayList<>();
        return  tasks;
    }
}
