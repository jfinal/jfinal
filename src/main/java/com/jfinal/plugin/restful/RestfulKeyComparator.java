package com.jfinal.plugin.restful;

import java.util.Comparator;

public class RestfulKeyComparator implements Comparator<String> {
    @Override
    public int compare(String str1, String str2) {
        String[] segments1 = str1.split("/");
        String[] segments2 = str2.split("/");

        int diff = segments1.length - segments2.length;
        if (diff != 0) {
            return -diff;
        }

        for (int i = 0, j = segments1.length; i < j; i++) {
            if (segments2[i].startsWith(":")) {
                continue;
            }
            if (segments2[i].equals(segments1[i])) {
                continue;
            }
            return 1;
        }
        return 0;
    }
}