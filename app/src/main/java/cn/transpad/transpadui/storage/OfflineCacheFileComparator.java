package cn.transpad.transpadui.storage;

import java.util.Comparator;

import cn.transpad.transpadui.entity.OfflineCache;


/**
 * 根据排序索引排序
 *
 * @author wangyang
 * @since 2014年6月5日
 */
public class OfflineCacheFileComparator implements Comparator<OfflineCache> {

    @Override
    public int compare(OfflineCache offlineCache1, OfflineCache offlineCache2) {
        int result = 0;
        if (offlineCache1 == null || offlineCache2 == null) {
            return result;
        }

//		if (offlineCache1.getSortIndex() > offlineCache2.getSortIndex()) {
//			result = 1;
//		} else if (offlineCache1.getSortIndex() < offlineCache2.getSortIndex()) {
//			result = -1;
//		}

        return result;
    }
}
