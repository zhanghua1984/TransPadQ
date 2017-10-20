package cn.transpad.transpadui.util;

import java.text.CollationKey;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import java.util.Locale;

import cn.transpad.transpadui.entity.MediaFile;

/**
 * Created by user on 2015/4/20.
 */
public class ModifyNameComparator implements Comparator {
    public static final int SORT_UP_TYPE = 1;
    public static final int SORT_DOWN_TYPE = 2;

    private int mSortType;
    private RuleBasedCollator ruleBasedCollator;

    public ModifyNameComparator() {
        ruleBasedCollator = (RuleBasedCollator) Collator.getInstance(Locale.getDefault());
    }

//    public ModifyNameComparator(Locale locale) {
//        ruleBasedCollator = (RuleBasedCollator) Collator.getInstance(locale);
//    }

    public ModifyNameComparator(int mSortType) {
        this.mSortType = mSortType;
        ruleBasedCollator = (RuleBasedCollator) Collator.getInstance(Locale.getDefault());
    }

    @Override
    public int compare(Object object1, Object object2) {
        MediaFile mediaFile1 = (MediaFile) object1;
        MediaFile mediaFile2 = (MediaFile) object2;
        CollationKey collationKey1 = ruleBasedCollator.getCollationKey(((MediaFile) object1).getMediaFileName());
        CollationKey collationKey2 = ruleBasedCollator.getCollationKey(((MediaFile) object2).getMediaFileName());
        int sort = 0;
        switch (mSortType) {
            case SORT_UP_TYPE:
                sort= -ruleBasedCollator.compare(collationKey1.getSourceString(),collationKey2.getSourceString());
                break;
            case SORT_DOWN_TYPE:
                sort= ruleBasedCollator.compare(collationKey1.getSourceString(),collationKey2.getSourceString());
                break;
        }
        return sort;
    }
}
