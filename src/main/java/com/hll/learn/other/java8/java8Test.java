package com.hll.learn.other.java8;

import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author hell
 * @date 2019/4/11
 */
public class java8Test {

    public static void main(String[] args) {
        
        String a = "a.b.c.d";
        String[] arr = a.split("\\.");
        int c = arr.length;
        arraryToList();
    }

    /**
     *
     * @param list
     */
    public static void listToArray(List<String> list){
        String[] listArr = list.toArray(new String[list.size()]);
        String[] ss = list.stream().toArray(String[]::new);
    }

    /**
     * 数组转list
     */
    public static void arraryToList(){
        String[] arr = new String[]{"a","b","c"};

        // 这两种list是java.util.Arrays类中一个私有静态内部类java.util.Arrays.ArrayList，
        // 它并非java.util.ArrayList类。java.util.Arrays.ArrayList类
        // 具有 set()，get()，contains()等方法，但是不具有添加add()或删除remove()方法,
        // 所以调用add()或remove()方法会报错。
        List<String> arrlist = Arrays.asList(arr);
        List cList = CollectionUtils.arrayToList(arr);

        //arrlist.add("bb");

        // 使用Stream中的Collector收集器转换后的List
        // 属于 java.util.ArrayList 能进行正常的增删查操作
        List<String> sList = Stream.of(arr).collect(Collectors.toList());
        sList.add("bb");

        List<String> s = sList.stream().filter(v -> Objects.equals("bb", v)).collect(Collectors.toList());

        listToArray(sList);

        // java8 list中的操作
    }

    public class User{
        String name;
        Integer age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }
}
