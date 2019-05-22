package main.learningbigdata.java.javabook.sort;

/**
 * FileName: BUbbleSort
 * Author:   hadoop
 * Email:    3165845957@qq.com
 * Date:     19-3-9 下午10:45
 * Description:
 */
public class BUbbleSort {
    public static void main(String[] args){
        int[] arr = {2,4,5,1,9,7,3,6,8};
        int tmp = 0;
        for(int i = 0; i < arr.length-1;i++){
            for (int j = 0; j < arr.length - i -1; j++){
                if (arr[i] > arr[j]){
                    tmp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = tmp;
                }
            }

        }
        System.out.println(arr.toString());
    }
}
