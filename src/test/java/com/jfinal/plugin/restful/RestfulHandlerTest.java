package com.jfinal.plugin.restful;

import org.junit.Test;

import java.util.TreeMap;

public class RestfulHandlerTest {
    @Test
    public void test() {
        RestfulKeyComparator cpr = new RestfulKeyComparator();

        String a = "@GET/v1/company/:companyId/staff/:staffId";
        String b = "@GET/v1/company/1234/staff/1234";
        String c = "@GET/v1/company/1234/staff/1234/test";
        String d = "@GET/v1/company/1234/staff/1234/test/1324";
        String e = "@GET/v1/company/1234/test";

        System.out.println(cpr.compare(b, a));
        System.out.println(cpr.compare(c, a));
        System.out.println(cpr.compare(d, a));
        System.out.println(cpr.compare(e, a));

        TreeMap<String, String> treeMap = new TreeMap<String, String>(cpr);

        treeMap.put("@GET/v1/company/:companyId", "@GET/v1/company/:companyId");
        treeMap.put("@GET/v1/company", "@GET/v1/company");
        treeMap.put("@GET/v1/company/:companyId/staff/:staffId", "@GET/v1/company/:companyId/staff/:staffId");

        System.out.println(treeMap.toString());

        System.out.println(treeMap.get("@GET/v1/company/1234123/staff/214/test"));
        System.out.println(treeMap.get("@GET/v1/company/1234123/staff/214"));
        System.out.println(treeMap.get("@GET/v1/company/1234123"));
        System.out.println(treeMap.get("@GET/v1/company"));
    }
}
