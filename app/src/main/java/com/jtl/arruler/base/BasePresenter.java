package com.jtl.arruler.base;


import java.lang.ref.WeakReference;

/**
 * @param <T>
 */
public abstract class BasePresenter <T> implements BasePresenterImpl {
    private WeakReference<T> t;

    public BasePresenter(T t1){
        onAttach(t1);
        start();
    }


    /**
     * 绑定
     * @param t1
     */
    public void onAttach(T t1){
        t=new WeakReference<T>(t1);
    }

    /**
     * 解绑
     */
    public void onDetach(){
        t.clear();
    }

    public T getView(){
        return t.get();
    }
}
