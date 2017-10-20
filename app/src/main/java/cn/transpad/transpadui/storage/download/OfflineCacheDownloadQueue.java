package cn.transpad.transpadui.storage.download;

import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * 
 * 缓存下载队列(包括活跃队列和非活跃队列)
 * 
 * @author wangyang
 * @since 2014-8-19
 */
public class OfflineCacheDownloadQueue<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = 1L;

	public V removeFirst() {
		LinkedList<K> linkedList = new LinkedList<K>(keySet());
		V v = null;
		if (linkedList != null && linkedList.size() > 0) {
			// 得到队首
			K k = linkedList.removeFirst();
			// 根据键获取值
			v = get(k);
			// 删除缓存数据
			remove(k);

		}
		return v;
	}

	public V removeLast() {
		LinkedList<K> linkedList = new LinkedList<K>(keySet());
		V v = null;
		if (linkedList != null && linkedList.size() > 0) {
			// 得到队首
			K k = linkedList.removeLast();
			// 根据键获取值
			v = get(k);
			// 删除缓存数据
			remove(k);

		}
		return v;
	}

}
