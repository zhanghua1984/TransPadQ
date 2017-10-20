package cn.transpad.transpadui.entity;

import ca.laplanete.mobile.pageddragdropgrid.Item;

/**
 * Created by Kongxiaojun on 2015/6/9.
 */
public class NoApp implements Item {

    int pageId;

    int index;

    @Override
    public long getId() {
        return 87787;
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
        return TYPE_NOAPP;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getPageId() {
        return pageId;
    }

    @Override
    public void setPageId(int pageId) {
        this.pageId = pageId;
    }
}
