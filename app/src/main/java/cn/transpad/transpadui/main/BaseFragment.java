package cn.transpad.transpadui.main;

import android.app.Fragment;
import android.content.Context;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import de.greenrobot.event.EventBus;

/**
 * Created by user on 2015/5/27.
 */
public class BaseFragment extends Fragment {

    View mView;

    private Context context = TransPadApplication.getTransPadApplication();
    /**
     * 标志该Fragment是否销毁了
     */
    protected boolean destory = false;

    /**
     * Toast短显示
     *
     * @param pResId
     */
    protected void showShortToast(int pResId) {
        showShortToast(context.getString(pResId));
    }

    /**
     * Toast短显示
     *
     * @param  pMsg
     */
    protected void showShortToast(String pMsg) {
        Toast.makeText(context, pMsg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Toast长显示
     *
     * @param pResId
     */
    protected void showLongToast(int pResId) {
        showLongToast(context.getString(pResId));
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getSimpleName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getSimpleName());
    }

    /**
     * Toast长显示
     *
     * @param pMsg
     */
    protected void showLongToast(String pMsg) {
        Toast.makeText(context, pMsg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destory = true;
        context = null;
        mView = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mView != null) {
            ViewGroup parentViewGroup = (ViewGroup) mView.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeAllViews();
            }
        }
    }

    public void onBack(){
        Message message = new Message();
        message.what = HomeActivity.MSG_WHAT_GO_BACK;
        EventBus.getDefault().post(message);
    }

}

