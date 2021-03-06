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
package cn.transpad.transpadui.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.laplanete.mobile.pageddragdropgrid.Item;
import ca.laplanete.mobile.pageddragdropgrid.PagedDragDropGridAdapter;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.App;
import cn.transpad.transpadui.entity.NoApp;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.entity.Page;
import cn.transpad.transpadui.entity.PageView;
import cn.transpad.transpadui.http.SoftRst;
import cn.transpad.transpadui.main.HomeActivity;
import cn.transpad.transpadui.main.TransPadApplication;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.TPUtil;
import cn.transpad.transpadui.view.DongleHomeAppPage;
import cn.transpad.transpadui.view.DongleHomePage;
import cn.transpad.transpadui.view.HomePage2;
import de.greenrobot.event.EventBus;

public class DonglePagedDragDropGridAdapter implements PagedDragDropGridAdapter {

    private static final String TAG = "DonglePagedDragDropGridAdapter";

    private Context context;
    List<Page> pages = new ArrayList<Page>();

    private HomePage2 homePage2;

    private List<DongleHomeAppPage> dongleHomeAppPages;

    private DongleHomePage dongleHomePage;

    private SoftRst softRst;

    private int onlinePageSize;

    public DonglePagedDragDropGridAdapter(Context context, List<Page> appPages) {
        super();
        this.context = context;
        softRst = TransPadApplication.getTransPadApplication().getSoftRst();
        L.v(TAG, "softRst = " + softRst);
        //软件推荐页
        if (softRst != null && softRst.cols != null && softRst.cols.size() > 0) {
            onlinePageSize += softRst.cols.size();
            dongleHomeAppPages = new ArrayList<DongleHomeAppPage>();
            for (SoftRst.Col col : softRst.cols) {
                dongleHomeAppPages.add(new DongleHomeAppPage(context, col, softRst.host, softRst.shost));
                Page page1 = new Page();
                List<Item> items = new ArrayList<Item>();
                Item item = new PageView();
                items.add(item);
                page1.setItems(items);
                pages.add(page1);
            }
        }

        //媒体页
        if (TransPadApplication.getTransPadApplication().getShowmedia().equals("1")) {
            onlinePageSize += 1;
            Page page2 = new Page();
            List<Item> items = new ArrayList<Item>();
            items = new ArrayList<Item>();
            Item item2 = new PageView();
            items.add(item2);
            page2.setItems(items);
            pages.add(page2);
        }


        //首页
        Page page3 = new Page();
        List<Item> items = new ArrayList<Item>();
        items = new ArrayList<Item>();
        Item item3 = new PageView();
        items.add(item3);
        page3.setItems(items);
        pages.add(page3);

        if (appPages != null && appPages.size() > 0) {
            for (Page p : appPages){
                if (p.getItems().size() <= 0){
                    Item i = new NoApp();
                    p.addItem(i);
                }
                pages.add(p);
            }
        }

    }

    @Override
    public int pageCount() {
        return pages.size();
    }

    private List<Item> itemsInPage(int page) {
        if (pages.size() > page) {
            return pages.get(page).getItems();
        }
        return Collections.emptyList();
    }


    @Override
    public View view(int page, int index) {
        if (page < onlinePageSize) {
            //软件推荐页和媒体页
            if (dongleHomeAppPages != null && page < dongleHomeAppPages.size()) {
                return dongleHomeAppPages.get(page);
            } else if (TransPadApplication.getTransPadApplication().getShowmedia().equals("1")) {
                //媒体页
                if (homePage2 == null) {
                    homePage2 = new HomePage2(context);
                }
                return homePage2;
            }
        } else {
            //首页和自定义页
            if (page == onlinePageSize) {
                if (dongleHomePage == null) {
                    dongleHomePage = new DongleHomePage(context);
                }
                return dongleHomePage;
            }
        }
        Item item = (Item) getItemAt(page, index);
        View itemView;
        if (item.getType() == 3){
            itemView = LayoutInflater.from(context).inflate(R.layout.drag_app_no_app_layout, null);
        }else {
            itemView = LayoutInflater.from(context).inflate(R.layout.home_application_layout, null);
            TextView label = (TextView) itemView.findViewById(R.id.home_app_name);
            label.setText(item.getName());

            RoundedImageView imageView = (RoundedImageView) itemView.findViewById(R.id.home_app_icon);
            if (item.getType() == Item.TYPE_APPLICATION) {//应用
                App app = (App) item;
                if (app.isInstall()) {
                    imageView.setImageDrawable(TPUtil.getDrawableByPackageName(context, app.getPackageName()));
                }
            }
        }
        return itemView;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setViewBackground(LinearLayout layout) {
    }

    private Item getItem(int page, int index) {
        List<Item> items = itemsInPage(page);
        return items.get(index);
    }

    @Override
    public int rowCount() {
        return 3;
    }

    @Override
    public int columnCount() {
        return 6;
    }

    @Override
    public int itemCountInPage(int page) {
        return itemsInPage(page).size();
    }

    public void printLayout() {
        int i = 0;
        for (Page page : pages) {
            Log.d("Page", Integer.toString(i++));

            for (Item item : page.getItems()) {
                Log.d("Item", Long.toString(item.getId()));
            }
        }
    }

    public Page getPage(int pageIndex) {
        return pages.get(pageIndex);
    }

    @Override
    public void swapItems(int pageIndex, int itemIndexA, int itemIndexB) {
        L.v(TAG, "swapItems " + pageIndex + "   " + itemIndexA + "   " + itemIndexB);
        getPage(pageIndex).swapItems(itemIndexA, itemIndexB);
    }

    @Override
    public void moveItemToPreviousPage(int pageIndex, int itemIndex) {
        L.v(TAG, "moveItemToPreviousPage " + pageIndex + "   " + itemIndex);
        int leftPageIndex = pageIndex - 1;
        if (leftPageIndex >= 0) {
            Page startpage = getPage(pageIndex);
            Page landingPage = getPage(leftPageIndex);

            Item item = startpage.removeItem(itemIndex);
            L.v(TAG, "start item = " + item.getName());

            if (landingPage.getItems().size() == rowCount() * columnCount()) {
                Item removeItem = landingPage.getItems().remove(landingPage.getLastIndexItemPostion());
                L.v(TAG, "removeItem item = " + removeItem.getName());
                startpage.addItemToFirstIndex(removeItem);
            }
            item.setIndex(landingPage.getMaxIndex() + 1);
            landingPage.addItem(item);
        }
    }

    @Override
    public void moveItemToNextPage(int pageIndex, int itemIndex) {
        int rightPageIndex = pageIndex + 1;
        if (rightPageIndex < pageCount()) {
            Page startpage = getPage(pageIndex);
            Page landingPage = getPage(rightPageIndex);

            Item item = startpage.removeItem(itemIndex);

            if (landingPage.getItems().size() == rowCount() * columnCount()) {
                //超出
                Item removeItem = landingPage.getItems().remove(landingPage.getLastIndexItemPostion());
                startpage.addItemToFirstIndex(removeItem);
            }
            item.setIndex(landingPage.getMaxIndex() + 1);
            landingPage.addItem(item);
        }
    }

    @Override
    public void deleteItem(int pageIndex, int itemIndex) {
        L.v("aaaaaaaa", "pageIndex = " + pageIndex + "  itemIndex = " + itemIndex);
        Page page = getPage(pageIndex);
        StorageModule.getInstance().deleteItem((int) (page.getItems().get(itemIndex).getId()));
        page.deleteItem(itemIndex);
        if(page.getItems().size() == 0){
            if (getAppPages().size() > 1) {
                //删除这个page
                pages.remove(pageIndex);
                StorageModule.getInstance().deletePage(page.getId());
                Message message = new Message();
                message.what = HomeActivity.MSG_WHAT_PAGE_DELETED;
                message.arg1 = pageIndex;
                EventBus.getDefault().post(message);
            }else {
                //显示默认视图
                Message message = new Message();
                message.what = HomeActivity.MSG_WHAT_ADD_NOITEMVIEW;
                message.arg1 = pageIndex;
                EventBus.getDefault().post(message);
            }
        }
    }

    @Override
    public int deleteDropZoneLocation() {
        return TOP;
    }

    @Override
    public boolean showRemoveDropZone() {
        return true;
    }

    @Override
    public int getPageWidth(int page) {
        return 0;
    }

    @Override
    public Object getItemAt(int page, int index) {
        if (getPage(page).getItems() == null || getPage(page).getItems().size() == 0) {
            return null;
        }
        return getPage(page).getItems().get(index);
    }

    @Override
    public boolean disableZoomAnimationsOnChangePage() {
        return true;
    }

    public void addPage(Page page) {
        pages.add(page);
    }

    public List<Page> getAppPages(){
        List<Page> appPages = new ArrayList<Page>();
        for (Page page : pages){
            if(page.getItems() == null || page.getItems().size() == 0 || page.getItems().get(0).getType() == Item.TYPE_APPLICATION || page.getItems().get(0).getType() == Item.TYPE_NOAPP){
                appPages.add(page);
            }
        }
        return appPages;
    }

    /**
     * 应用更新了
     *
     * @param isInstall
     * @param packageName
     */
    public void applicationUpdate(boolean isInstall, String packageName) {
        if (softRst != null && softRst.cols != null) {
            for (int i = 0; i < softRst.cols.size(); i++) {
                if (softRst.cols.get(i).cnts != null && softRst.cols.get(i).cnts.cntList != null) {

                    for (SoftRst.Cnt cnt : softRst.cols.get(i).cnts.cntList) {
                        if (!TextUtils.isEmpty(packageName) && packageName.equals(cnt.pkname)) {
                            dongleHomeAppPages.get(i).applicationUpdate(isInstall, packageName);
                        }
                    }
                }
            }
        }
    }

    /**
     * 更新下载进度
     *
     * @param oc
     */
    public void updateDownloadProgress(OfflineCache oc) {
        if (softRst != null && softRst.cols != null) {
            for (int i = 0; i < softRst.cols.size(); i++) {
                if (softRst.cols.get(i).cnts != null && softRst.cols.get(i).cnts.cntList != null) {
                    for (SoftRst.Cnt cnt : softRst.cols.get(i).cnts.cntList) {
                        if (!TextUtils.isEmpty(oc.getCachePackageName()) && oc.getCachePackageName().equals(cnt.pkname)) {
                            dongleHomeAppPages.get(i).updateDownloadProgress(oc);
                        }
                    }
                }
            }
        }
    }

    /**
     * 有下载被删除了
     */
    public void cacheStateChanged() {
        for (DongleHomeAppPage page : dongleHomeAppPages){
            page.cacheStateChanged();
        }
    }


    /**
     * 更新下载进度列表
     *
     * @param offlineCacheList
     */
    public void updateDownloadProgress(List<OfflineCache> offlineCacheList) {
        if (offlineCacheList != null && offlineCacheList.size() > 0) {
            for (OfflineCache oc : offlineCacheList) {
                updateDownloadProgress(oc);
            }
        }
    }

    /**
     * 更新红色指示点
     */
    public void updateHomeRedDot(){
        if (dongleHomePage != null){
            dongleHomePage.updateRedDot();
        }
    }
}
