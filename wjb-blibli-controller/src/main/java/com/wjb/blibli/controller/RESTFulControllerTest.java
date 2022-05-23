package com.wjb.blibli.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RESTFulControllerTest {

    private Map<Integer,Map<String,Object>> dataMap;

    public RESTFulControllerTest(){
        dataMap = new HashMap<>();

        for (int i = 0; i < 2; i++) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("id", i);
            data.put("name", "name"+i);
            dataMap.put(i,data);
        }
    }
    @GetMapping("/getid")
    public Map<String,Object> getId(Integer id){
        return dataMap.get(id);
    }

    @DeleteMapping("/deleteid")
    public String deleteId(Integer id){
        dataMap.remove(id);
        return "good";
    }


    @PostMapping("/postid")
    public String postId(@RequestBody Map<String,Object> data){
        Integer[] dataKeySet =dataMap.keySet().toArray(new Integer[0]);
        int dataIndex=dataKeySet[dataKeySet.length-1]+1;
        dataMap.put(dataIndex, data);
        return "good";
    }

    @PutMapping("/putid")
    public String putId(@RequestBody Map<String,Object> data){
        Map<String,Object> curData=dataMap.get(Integer.valueOf(String.valueOf(data.get("id"))));
        if(curData!=null){
            dataMap.put(Integer.valueOf(String.valueOf(data.get("id"))), data);
        }else {
            Integer[] dataKeySet = dataMap.keySet().toArray(new Integer[0]);
            int dataIndex = dataKeySet[dataKeySet.length - 1] + 1;
            dataMap.put(dataIndex, data);
        }
        return "good";
    }
}
