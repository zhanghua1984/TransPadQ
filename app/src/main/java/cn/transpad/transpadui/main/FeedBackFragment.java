package cn.transpad.transpadui.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.http.Request;
import cn.transpad.transpadui.http.Rst;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.ScreenUtil;
import cn.transpad.transpadui.util.TPUtil;
import cn.transpad.transpadui.view.FeedbackDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by ctccuser on 2015/4/6.
 */
public class FeedBackFragment extends BaseFragment {
    //    EditText contactEditText;
//    EditText problemsEditText;
    private final String TAG = FeedBackFragment.class.getSimpleName();
    float scale;
    float scaledDensity;
    FeedbackDialog feedbackDialog;
    //        问题类型 5 ： pad问题 6 :  软件问题 7： 内容问题 8 ：  其他
    public static final int PROBLEM_PAD = 5;
    public static final int PROBLEM_SOFT = 6;
    public static final int PROBLEM_CONTENT = 7;
    public static final int PROBLEM_ELSE = 8;
    String contact;
    String problems;
    //    RadioGroup radioGroup;
    Context context;

    private long lastToastTime;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.feedback_layout, container, false);
        ButterKnife.inject(this, mView);
        L.v(TAG, "onCreateView", "radioGroup= " + radioGroup);
        scale = getActivity().getResources().getDisplayMetrics().density;
        scaledDensity = getActivity().getResources().getDisplayMetrics().scaledDensity;
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
//        Log.v(TAG,"height : " + dm.heightPixels);
//        Log.v(TAG,"width : " + dm.widthPixels);
//        Log.v(TAG,"height dp: "+px2dip(dm.heightPixels));
//        Log.v(TAG,"width dp: "+px2dip(dm.widthPixels));
        problemsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = problemsEditText.getText().toString();
                if (text != null && text.length() > 500 && System.currentTimeMillis() - lastToastTime > 2000) {
                    lastToastTime = System.currentTimeMillis();
                    Toast.makeText(getActivity(), R.string.settings_feedback_tooLong, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });
        return mView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        L.v(TAG,"onCreate",""+isQQ("826555209"));
//        L.v(TAG,"onCreate",""+isQQ("82"));
//        L.v(TAG,"onCreate",""+isEmail("@sina.com"));
//        L.v(TAG,"onCreate",""+isEmail("qq@sina.com"));
//        L.v(TAG,"onCreate",""+isMobileNO("11001141235"));
//        L.v(TAG,"onCreate",""+isMobileNO("18046523026"));
        L.v(TAG, "onCreate", "" + isTest("4"));
        L.v(TAG, "onCreate", "" + isTest("1"));
        L.v(TAG, "onCreate", "" + isTest("a"));
        this.context = getActivity();
        feedbackDialog = new FeedbackDialog(getActivity(), R.style.myDialog);

    }

    @InjectView(R.id.feedback_contact)
    EditText contactEditText;
    @InjectView(R.id.feedback_problems)
    EditText problemsEditText;
    @InjectView(R.id.feedback_radio_group)
    RadioGroup radioGroup;
    @InjectView(R.id.feedback_button)
    Button button;

    @OnClick(R.id.feedback_button)
    void feedbackCommit() {
//        提交反馈信息
        contact = contactEditText.getText().toString();
        problems = problemsEditText.getText().toString();
        L.v(TAG, "feedbackCommit", "contact= " + contact + "problems= " + problems);
        if (!isValidateData()) {
            return;
        }
        L.v(TAG, "feedbackCommit", "id=" + radioGroup.getCheckedRadioButtonId());
        L.v(TAG, "feedbackCommit", "net work" + TPUtil.isNetOkWithToast());
        if (TPUtil.isNetOkWithToast()) {

//            feedbackDialog = new FeedbackDialog(getActivity(), R.style.myDialog);
//            feedbackDialog.setContentView(R.layout.feedback_dialog);
            feedbackDialog.setClickListener(new FeedbackDialog.ClickListener() {
                @Override
                public void ok() {
                    feedbackDialog.dismiss();
                    if (getActivity() instanceof HomeActivity) {
                        HomeActivity homeActivity = (HomeActivity) getActivity();
                        homeActivity.onBackPressed();
                    }
                }
            });
            Window dialogWindow = feedbackDialog.getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.dimAmount = 0.5f;
            lp.y = ScreenUtil.dp2px(-10);

            String contactType = getContactType(contact);
            int problemType = getProblemType();
            L.v(TAG, "feedbackCommit", "cT" + contactType + "pT" + problemType);
            if (contactType != null && problemType != -1 && problemType != 0) {
                Request.getInstance().fb(problems, contact, contactType, problemType, new Callback<Rst>() {

                    @Override
                    public void success(Rst rst, Response response) {
                        L.v(TAG, "success", "rst=" + rst);
                        if (rst.result != 0) {
                            Toast.makeText(context, R.string.feedback_failed, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        feedbackDialog.show();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        L.v(TAG, "failure", "error=" + error + "error body=" + error.getBody());
                        L.v(TAG, "failure", "error kind=" + error.getKind());
                        Toast.makeText(context, R.string.feedback_failed, Toast.LENGTH_SHORT).show();
                    }
                });

            }

        } else {
            L.v(TAG, "feedbackCommit", "net work else" + TPUtil.isNetOkWithToast());

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (feedbackDialog != null && feedbackDialog.isShowing()) {
            feedbackDialog.dismiss();
        }
    }

    /**
     * 判断邮箱是否合法
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {

        if (null == email || "".equals(email)) return false;
        //Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配
        Pattern p = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配
        Matcher m = p.matcher(email);

        return m.matches();
    }

    public static boolean isMobileNO(String mobiles) {

        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);

        return m.matches();
    }

    public static boolean isQQ(String qq) {

        Pattern p = Pattern.compile("^[1-9][0-9]{4,9}$");
        Matcher m = p.matcher(qq);

        return m.matches();
    }

    public static boolean isTest(String test) {

        Pattern p = Pattern.compile("^[^4,\\D]$");
        Matcher m = p.matcher(test);

        return m.matches();
    }

    public boolean isValidateData() {
        if (contact.isEmpty()) {
            Toast.makeText(getActivity(), R.string.settings_feedback_noContact, Toast.LENGTH_SHORT).show();
            return false;
        } else if (problems.isEmpty()) {
            Toast.makeText(getActivity(), R.string.settings_feedback_noProblems, Toast.LENGTH_SHORT).show();
            return false;

        } else if (!isEmail(contact) && !isMobileNO(contact) && !isQQ(contact)) {
            Toast.makeText(getActivity(), R.string.settings_feedback_invalidContact, Toast.LENGTH_SHORT).show();
            return false;

        } else if (problems.length() > 500) {
            Toast.makeText(getActivity(), R.string.settings_feedback_tooLong, Toast.LENGTH_SHORT).show();
            return false;

        } else if (radioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(getActivity(), R.string.feedback_problems_case, Toast.LENGTH_SHORT).show();
            return false;

        }
        return true;
    }

    public String getContactType(String contact) {
        if (isEmail(contact)) {
            return "email";
        } else if (isMobileNO(contact)) {
            return "phone";
        } else if (isQQ(contact)) {
            return "qq";
        }
        return null;
    }

    public int getProblemType() {
//        问题类型 5 ： pad问题 6 :  软件问题 7： 内容问题 8 ：  其他
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.problems_pad:
                return PROBLEM_PAD;
//            break;
            case R.id.problems_soft:
                return PROBLEM_SOFT;
            case R.id.problems_content:
                return PROBLEM_CONTENT;
            case R.id.problems_else:
                return PROBLEM_ELSE;
        }
        return 0;
    }
}
