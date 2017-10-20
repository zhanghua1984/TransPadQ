/**
 * Copyright 2012
 * <p/>
 * Nicolas Desjardins
 * https://github.com/mrKlar
 * <p/>
 * Facilite solutions
 * http://www.facilitesolutions.com/
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package cn.transpad.transpadui.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ca.laplanete.mobile.pageddragdropgrid.Item;
import cn.transpad.transpadui.util.L;

public class Page {


    private int id;

    private String name = "page";

    private List<Item> items = new ArrayList<Item>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public void addItem(Item item) {
        item.setPageId(id);
        items.add(item);
    }

    public void addItemToFirstIndex(Item item){
        for (Item i : items){
            i.setIndex(i.getIndex() + 1);
        }
        item.setIndex(0);
        item.setPageId(id);
        L.v(name, "addItemToFirstIndex item name = " + item.getName() + "   item.getIndex() = " + item.getIndex() + "   item.getPageId() = " + item.getPageId());
        items.add(item);
        Collections.sort(items, new Comparator<Item>() {
            @Override
            public int compare(Item lhs, Item rhs) {
                return lhs.getIndex()<rhs.getIndex()?-1:1;
            }
        });
    }

    public void swapItems(int itemA, int itemB) {
        final int size = items.size();
        if (itemA < 0 || itemA >= size || itemB < 0 || itemB >= size) {
            return;
        }
        if (itemA == itemB){
            return;
        }
        items.get(itemA).setIndex(itemB);
        items.get(itemB).setIndex(itemA);

        Collections.swap(items, itemA, itemB);
    }

    public Item removeItem(int itemIndex) {
        Item item = items.get(itemIndex);
        items.remove(itemIndex);
        return item;
    }

    public int getLastIndexItemPostion(){
        int postion = 0;
        int maxIndex = 0;
        for(int i=0;i<items.size();i++){
            if (items.get(i).getIndex() > maxIndex){
                maxIndex = items.get(i).getIndex();
                postion = i;
            }
        }
        return postion;
    }

    public int getMaxIndex(){
        int maxIndex = 0;
        for(int i=0;i<items.size();i++){
            if (items.get(i).getIndex() > maxIndex){
                maxIndex = items.get(i).getIndex();
            }
        }
        return maxIndex;
    }

    public void deleteItem(int itemIndex) {
        Item item = items.remove(itemIndex);
    }

}
