package com.mosc.simo.ptuxiaki3741.util;

import java.util.ArrayList;
import java.util.List;

public final class ListUtils {
    private ListUtils(){}
    public static <T> List<T> intersection(List<T> list1, List<T> list2){
        List<T> result = new ArrayList<>();
        if(list1.size()>0 && list2.size()>0 ){
            if(list1.size() >= list2.size()){
                for(T temp : list2){
                    if(list1.contains(temp))
                        result.add(temp);
                }
            }else{
                for(T temp : list1){
                    if(list2.contains(temp))
                        result.add(temp);
                }
            }
        }
        return result;
    }
    public static <T> boolean arraysMatch(List<T> list1, List<T> list2) {
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
