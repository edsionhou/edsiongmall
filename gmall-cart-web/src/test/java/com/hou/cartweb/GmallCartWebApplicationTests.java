package com.hou.cartweb;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GmallCartWebApplicationTests {

    @Test
 public    void contextLoads() {
      char[] arr = new char[]{'0','A','B','C','D','E','F','G'}; //8个 index 7
        int count =1; //索引位
        int summ  = 0; //每行 几个数  1 3 5 7 9  9  9
       while(count<arr.length){
           summ =(count<<1) -1;
//           System.out.println("sum= "+summ);
           for(int i=0 ; i<summ;i++){
                System.out.print(arr[count]+" ");
           }
           System.out.println("");
           count++;
       }


    }

}
