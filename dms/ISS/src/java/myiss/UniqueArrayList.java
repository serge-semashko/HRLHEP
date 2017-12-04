package myiss;

import java.util.ArrayList;
import java.util.Collection;


public class UniqueArrayList extends ArrayList {
    /**
     * Only add the object if there is not
     * another copy of it in the list
     */
    public boolean add(Object obj) {
        for (int i = 0; i < size(); i++) {
            if (obj.equals(get(i))) {
                return false;
            }
        }
        return super.add(obj);
    }

    public boolean addAll(Collection c) {
        boolean result = true;
        for (Object t : c) {
            if (!add(t)) {
                result = false;
            }
        }
        return result;
    }
}