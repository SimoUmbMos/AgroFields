package com.mosc.simo.ptuxiaki3741.data.util;

import java.util.ArrayList;
import java.util.List;

public final class ListUtils {
    private ListUtils(){}
    public static <T> boolean arraysMatch(List<T> list1, List<T> list2) {
        if(list1 == null){
            list1 = new ArrayList<>();
        }
        if(list2 == null){
            list2 = new ArrayList<>();
        }
        if (list1.size() != list2.size()) {
            return false;
        }
        List<T> work = new ArrayList<>(list2);
        for (T item : list1) {
            if (!work.remove(item)) {
                return false;
            }
        }
        return work.isEmpty();
    }
}
