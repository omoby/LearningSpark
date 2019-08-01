package main.learningbigdata.java.test;

import org.apache.commons.collections.map.HashedMap;

import java.util.HashMap;
import java.util.Map;

/**
 * FileName: MapTest
 * Author:   hadoop
 * Email:    3165845957@qq.com
 * Date:     19-7-30 下午10:53
 * Description:
 */
public class MapTest {
    public static void main(String[] args){
        Map<String, Integer> map = new HashMap<>();
        map.put("熊大", 1);
        map.put("熊二", 2);
        String keys="[";
        String values="[";
        for(Map.Entry<String, Integer> entry : map.entrySet()){
            keys += "'"+entry.getKey()+"',";
            values += entry.getValue()+",";
        }
        keys = keys.substring(0,keys.length()-1)+"]";
        values =values.substring(0,values.length()-1)+"]";
        Map<String,String> data = new HashedMap();
        data.put("arrAx",keys);
        data.put("values",values);
        System.out.println(keys+"\n"+values);

    }
}
