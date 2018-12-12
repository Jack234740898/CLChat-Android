package com.cleven.clchat.clchat.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RadioGroup;

import com.cleven.clchat.clchat.R;
import com.cleven.clchat.clchat.base.CLBaseFragment;
import com.cleven.clchat.clchat.fragment.CLContactFragment;
import com.cleven.clchat.clchat.fragment.CLDiscoverFragment;
import com.cleven.clchat.clchat.fragment.CLHomeFragment;
import com.cleven.clchat.clchat.fragment.CLProfileFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {

    private FrameLayout frameLayout;
    private RadioGroup radioGroup;

    private int currentSelectIndex = 0;
    /// 记录当前显示的fragment
    private CLBaseFragment preFragment;
    private List<CLBaseFragment> fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /// 取消标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFragments();

        findViews();

    }

    private void findViews() {
        frameLayout = (FrameLayout)findViewById( R.id.frameLayout );
        radioGroup = (RadioGroup)findViewById( R.id.radioGroup );
        /// 设置监听事件
        radioGroup.setOnCheckedChangeListener(new MyOnClickListener());
        /// 默认选中第一个
        radioGroup.check(R.id.rb_main_home);
    }

    private class MyOnClickListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId){
                case R.id.rb_main_home:
                    currentSelectIndex = 0;
                    break;
                case R.id.rb_main_contact:
                    currentSelectIndex = 1;
                    break;
                case R.id.rb_main_discover:
                    currentSelectIndex = 2;
                    break;
                case R.id.rb_main_profile:
                    currentSelectIndex = 3;
                    break;
            }
            /// 获取Fragment
            CLBaseFragment fragment = getFragment(currentSelectIndex);
            /// 切换
            switchFragment(preFragment,fragment);
        }

    }

    /// 根据下标获取fragment
    private CLBaseFragment getFragment(int index){
        if (fragments != null && fragments.size() > 0){
            return fragments.get(index);
        }
        return null;
    }

    /// 切换Fragment
    private void switchFragment(Fragment fromFragment, CLBaseFragment nextFragment){
        if (preFragment != nextFragment){
            preFragment = nextFragment;
            if (nextFragment != null){
                /// 获取Fragment管理器
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                // 判断nextFragment是否添加
                if (!nextFragment.isAdded()){
                    //隐藏当前Fragment
                    if (fromFragment != null){
                        transaction.hide(fromFragment);
                    }
                    // 添加下一个Fragment
                    transaction.add(R.id.frameLayout,nextFragment).commit();
                }else {
                    //隐藏当前Fragment
                    if (fromFragment != null){
                        transaction.hide(fromFragment);
                    }
                    /// 如果已经添加过,直接显示
                    transaction.show(nextFragment).commit();
                }
            }
        }
    }

    private void initFragments() {
        fragments = new ArrayList<>();
        fragments.add(new CLHomeFragment());
        fragments.add(new CLContactFragment());
        fragments.add(new CLDiscoverFragment());
        fragments.add(new CLProfileFragment());
    }


}