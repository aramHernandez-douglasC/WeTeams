package com.example.weteams.logic;

import java.util.Collections;
import java.util.List;

//Quick sort for sorting list of events according to due date ascending
public class SortEvents {
    public static void quickSort(List<Event> array, int start, int end)
    {
        if (start < end)
        {
            int pivot = start;
            int left = start + 1;
            int right = end;

            while (true)
            {
                while (array.get(left).getDeadline().compareTo(array.get(pivot).getDeadline()) <= 0 && left < right)
                    left++;
                while (array.get(right).getDeadline().compareTo(array.get(pivot).getDeadline()) > 0 && left < right)
                    right--;
                if (left < right)
                {
                    Collections.swap(array,left,right);
                }
                else
                {
                    pivot = (array.get(pivot).getDeadline().compareTo(array.get(left).getDeadline())) > 0 ? left : left - 1;
                    Collections.swap(array, start, pivot);
                    quickSort(array, start, pivot - 1);
                    quickSort(array, pivot + 1, end);
                    return;
                }
            }
        }
    }

}
