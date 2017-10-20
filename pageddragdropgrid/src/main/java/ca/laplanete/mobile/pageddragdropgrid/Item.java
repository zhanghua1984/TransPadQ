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
package ca.laplanete.mobile.pageddragdropgrid;

public interface Item {

    /**
     * 0:应用
     */
    int TYPE_APPLICATION = 0;
    /**
     * 1:全屏页
     */
    int TYPE_VIEW = 1;
    /**
     * 2：目录
     */
    int TYPE_FOLDER = 2;

    /**
     * 没有app时显示的文字
     */
    int TYPE_NOAPP = 3;

    long getId();

    void setId(long id);

    String getName();

    void setName(String name);

    int getType();

    void setIndex(int index);

    int getIndex();

    int getPageId();

    void setPageId(int pageId);

}
