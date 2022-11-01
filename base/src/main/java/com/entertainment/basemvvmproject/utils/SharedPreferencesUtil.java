package com.entertainment.basemvvmproject.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {
    private static final String PREF_FILE = "Sondz" + System.currentTimeMillis();
    private static SharedPreferencesUtil instance;
    private final SharedPreferences mSp;

    private SharedPreferencesUtil(Context context, String nameShare) {
        this.mSp = context.getSharedPreferences(nameShare, Context.MODE_PRIVATE);
    }

    public static SharedPreferencesUtil getInstance(Context context) {
        if (instance == null)
            instance = new SharedPreferencesUtil(context, "saveLogin");
        return instance;
    }

    private boolean executeWithEditor(Executable executable) {
        SharedPreferences.Editor edit = this.mSp.edit();
        executable.excute(edit);
        return edit.commit();
    }

    public void clearALL() {
        this.executeWithEditor(SharedPreferences.Editor::clear);
    }

    public SharedPreferences getSharedPreferences() {
        return this.mSp;
    }

    public SharedPreferences.Editor getEditor() {
        return this.mSp.edit();
    }

    public boolean getBoolean(String key, boolean defValue) {
        return this.mSp.getBoolean(key, defValue);
    }


    public float getFloat(String key, float defValue) {
        return this.mSp.getFloat(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return this.mSp.getInt(key, defValue);
    }

    public long getLong(String key, long defValue) {
        return this.mSp.getLong(key, defValue);
    }

    public String getString(String key, String defValue) {
        return this.mSp.getString(key, defValue);
    }

    public boolean put(final String key, final float value) {
        return this.executeWithEditor(editor -> editor.putFloat(key, value));
    }

    public boolean put(final String key, final int value) {
        return this.executeWithEditor(editor -> editor.putInt(key, value));
    }

    public boolean put(final String key, final long value) {
        return this.executeWithEditor(editor -> editor.putLong(key, value));
    }

    public boolean put(final String key, final String value) {
        return this.executeWithEditor(editor -> editor.putString(key, value));
    }

    public boolean put(final String key, final boolean value) {
        return this.executeWithEditor(editor -> editor.putBoolean(key, value));
    }

    public void remove(final String key) {
        this.executeWithEditor(editor -> editor.remove(key));
    }

    interface Executable {
        void excute(SharedPreferences.Editor editor);
    }


}
