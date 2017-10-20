package cn.transpad.transpadui.storage;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import cn.transpad.transpadui.entity.PlayRecord;
import cn.transpad.transpadui.util.L;

/**
 * 播放记录表适配器
 *
 * @author wangyang
 * @since 2014年4月29日
 */
class PlayRecordDataBaseAdapter implements IFoneDatabase {
    private static final String TAG = PlayRecordDataBaseAdapter.class
            .getSimpleName();
    private static final PlayRecordDataBaseAdapter mInstance = new PlayRecordDataBaseAdapter();
    private OperateDataBaseTemplate mGeneralDataBaseTemplate = OperateDataBaseTemplate
            .getInstance();

    private PlayRecordDataBaseAdapter() {

    }

    static PlayRecordDataBaseAdapter getInstance() {
        return mInstance;
    }

    public void exeSQL(String sql) {
        mGeneralDataBaseTemplate.open();
        mGeneralDataBaseTemplate.execSQL(sql);
        mGeneralDataBaseTemplate.close();
    }

    /**
     * 添加播放记录
     *
     * @param playRecord 播放记录对象
     * @return int 插入结果<br>
     * 1 插入成功 <br>
     * -1 插入异常
     */
    public int addPlayRecord(PlayRecord playRecord) {
        L.v(TAG, "addPlayRecord" + playRecord.toString());
        try {
            PlayRecord oldRecord;
            if (playRecord.getPlayRecordCid() != 0){
                oldRecord = getPlayRecordByCid(playRecord.getPlayRecordCid());
            }else {
                oldRecord = getPlayRecordByPlayUrl(playRecord.getPlayRecordPlayUrl());
            }
            List<PlayRecord> updatePlayRecordList = new ArrayList<PlayRecord>();
            List<PlayRecord> addPlayRecordList = new ArrayList<PlayRecord>();
            if (oldRecord != null) {
                oldRecord.setPlayRecordAlreadyPlayTime(playRecord.getPlayRecordAlreadyPlayTime());
                updatePlayRecordList.add(oldRecord);
                // 更新
            } else {
                // 添加
                addPlayRecordList.add(playRecord);
            }
            if (updatePlayRecordList.size() > 0) {
                updatePlayRecordList(updatePlayRecordList);
            }
            if (addPlayRecordList.size() > 0) {
                addPlayRecordList(addPlayRecordList);
            }

        } catch (Exception e) {
            e.printStackTrace();
            L.v(TAG, e.getMessage());
            return -1;
        } finally {

        }
        return 1;
    }

    /**
     * 添加播放记录集合
     *
     * @param playRecordList 播放记录对象
     * @return int 插入结果<br>
     * 1 插入成功 <br>
     * -1 插入异常
     */
    private int addPlayRecordList(List<PlayRecord> playRecordList) {
        try {
            mGeneralDataBaseTemplate.open();
            mGeneralDataBaseTemplate.beginTransaction();
            for (PlayRecord playRecord : playRecordList) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(PLAY_RECORD_TYPE,
                        playRecord.getPlayRecordType());
                contentValues.put(PLAY_RECORD_PLAY_URL,
                        playRecord.getPlayRecordPlayUrl());
                contentValues.put(PLAY_RECORD_ALREADY_PLAY_TIME,
                        playRecord.getPlayRecordAlreadyPlayTime());
                contentValues.put(PLAY_RECORD_TOTAL_TIME,
                        playRecord.getPlayRecordTotalTime());
                contentValues.put(PLAY_RECORD_CTEATE_TIME,
                        playRecord.getPlayRecordCreateTime());
                contentValues.put(PLAY_RECORD_CID,
                        playRecord.getPlayRecordCid());
                mGeneralDataBaseTemplate.insert(TB_PLAY_RECORD, null, contentValues);
            }
            mGeneralDataBaseTemplate.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
            if (e != null) {
                L.v(TAG, e.getMessage());
            }
            return -1;
        } finally {
            mGeneralDataBaseTemplate.endTransaction();
            mGeneralDataBaseTemplate.close();
        }
        return 1;
    }

    /**
     * 更新播放记录集合
     *
     * @param playRecordList 播放记录对象
     * @return int 更新结果<br>
     * 1 更新成功 <br>
     * -1 更新异常
     */
    private int updatePlayRecordList(List<PlayRecord> playRecordList) {

        synchronized (TAG) {
            try {
                mGeneralDataBaseTemplate.open();
                int result = 0;
                for (PlayRecord playRecord : playRecordList) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(PLAY_RECORD_TYPE,
                            playRecord.getPlayRecordType());
                    contentValues.put(PLAY_RECORD_ALREADY_PLAY_TIME,
                            playRecord.getPlayRecordAlreadyPlayTime());
                    contentValues.put(PLAY_RECORD_CTEATE_TIME,
                            playRecord.getPlayRecordCreateTime());
                    StringBuilder where = new StringBuilder();
                    where.append(PLAY_RECORD_ID);
                    where.append("=");
                    where.append(playRecord.getPlayRecordId());
                    result = mGeneralDataBaseTemplate.update(
                            TB_PLAY_RECORD, contentValues,
                            where.toString(),
                            null);
                    L.v(TAG, "updatePlayRecordList",
                            "result=" + result + " playRecordUrl="
                                    + playRecord.getPlayRecordPlayUrl());

                }
            } catch (Exception e) {
                e.printStackTrace();
                L.v(TAG, e.getMessage());
                return -1;
            } finally {
                mGeneralDataBaseTemplate.close();
            }
            return 1;
        }
    }

    /**
     * 根据类型和连接删除播放记录 <br>
     * 同步方法
     *
     * @param playRecord 播放记录对象
     * @return int 操作结果<br>
     * 1 删除成功<br>
     * -1 删除异常<br>
     */
    public int deletePlayRecord(PlayRecord playRecord) {
        try {

            // 删除本地数据库
            StringBuilder sql = new StringBuilder();
            sql.append("delete from ").append(TB_PLAY_RECORD);
            sql.append(" where ");
            sql.append(PLAY_RECORD_TYPE).append("=");
            sql.append(playRecord.getPlayRecordType());
            sql.append(" and ");
            sql.append(PLAY_RECORD_PLAY_URL);
            sql.append("='");
            sql.append(playRecord.getPlayRecordPlayUrl());
            sql.append("';");
            mGeneralDataBaseTemplate.open();
            mGeneralDataBaseTemplate.delete(sql.toString());

            return 1;

        } catch (Exception e) {
            e.printStackTrace();
            L.e(TAG, "deletePlayRecord", e.getMessage());
            return -1;
        } finally {
            mGeneralDataBaseTemplate.close();
        }
    }

    /**
     * 根据播放地址获得播放记录
     *
     * @param playUrl 播放地址
     * @return PlayRecord 播放记录
     */
    public PlayRecord getPlayRecordByPlayUrl(String playUrl) {
        StringBuilder sql = new StringBuilder("select * from ");
        sql.append(TB_PLAY_RECORD);
        sql.append(" where ");
        sql.append(PLAY_RECORD_PLAY_URL);
        sql.append("='");
        sql.append(playUrl);
        sql.append("'");
        PlayRecord playRecord = null;
        Cursor cursor = null;
        try {
            mGeneralDataBaseTemplate.open();
            cursor = mGeneralDataBaseTemplate.select(sql.toString());

            if (cursor == null) {
                return null;
            }
            // 读取数据
            while (cursor.moveToNext()) {
                playRecord = getPlayRecord(cursor);
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mGeneralDataBaseTemplate.close();
        }
        return playRecord;
    }

    /**
     * 根据视频CID获得播放记录
     *
     * @param cid 视频CID
     * @return PlayRecord 播放记录
     */
    public PlayRecord getPlayRecordByCid(long cid) {
        StringBuilder sql = new StringBuilder("select * from ");
        sql.append(TB_PLAY_RECORD);
        sql.append(" where ");
        sql.append(PLAY_RECORD_CID);
        sql.append("=");
        sql.append(cid);
        sql.append("");
        PlayRecord playRecord = null;
        Cursor cursor = null;
        try {
            mGeneralDataBaseTemplate.open();
            cursor = mGeneralDataBaseTemplate.select(sql.toString());

            if (cursor == null) {
                return null;
            }
            // 读取数据
            while (cursor.moveToNext()) {
                playRecord = getPlayRecord(cursor);
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mGeneralDataBaseTemplate.close();
        }
        return playRecord;
    }

    private PlayRecord getPlayRecord(Cursor cursor) {

        PlayRecord playRecord = new PlayRecord();
        playRecord.setPlayRecordId(cursor.getLong(cursor
                .getColumnIndex(PLAY_RECORD_ID)));
        playRecord.setPlayRecordType(cursor.getInt(cursor
                .getColumnIndex(PLAY_RECORD_TYPE)));
        playRecord.setPlayRecordPlayUrl(cursor.getString(cursor
                .getColumnIndex(PLAY_RECORD_PLAY_URL)));
        playRecord.setPlayRecordAlreadyPlayTime(cursor.getLong(cursor
                .getColumnIndex(PLAY_RECORD_ALREADY_PLAY_TIME)));
        playRecord.setPlayRecordTotalTime(cursor.getLong(cursor
                .getColumnIndex(PLAY_RECORD_TOTAL_TIME)));
        playRecord.setPlayRecordCreateTime(cursor.getLong(cursor
                .getColumnIndex(PLAY_RECORD_CTEATE_TIME)));
        return playRecord;
    }
}
