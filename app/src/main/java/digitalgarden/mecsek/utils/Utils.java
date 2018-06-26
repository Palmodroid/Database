package digitalgarden.mecsek.utils;

import java.util.Iterator;
import java.util.List;


public class Utils
    {
    public static int[] convertToIntArray(List<Integer> integerList)
        {
        int[] intArray = new int[integerList.size()];
        Iterator<Integer> iterator = integerList.iterator();
        for (int i = 0; i < intArray.length; i++)
            {
            intArray[i] = iterator.next();
            }
        return intArray;
        }
    }
