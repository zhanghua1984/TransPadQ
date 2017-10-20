package cn.transpad.transpadui.entity;

import ca.laplanete.mobile.pageddragdropgrid.Item;

/**
 * Created by Kongxiaojun on 2015/4/15.
 * 代表一个整页的Item
 */
public class PageView implements Item {
    @Override
    public long getId() {
        return 0;
    }

    @Override
    public void setId(long id) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public int getType() {
        return TYPE_VIEW;
    }

    @Override
    public void setIndex(int index) {

    }

    @Override
    public int getIndex() {
        return 0;
    }

    @Override
    public int getPageId() {
        return 0;
    }

    @Override
    public void setPageId(int pageId) {

    }
}
