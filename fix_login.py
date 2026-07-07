# -*- coding: utf-8 -*-
content = '''<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/bg"
    android:gravity="center"
    android:orientation="vertical"
    android:padding="24dp">

    <TextView
        android:layout_width="72dp"
        android:layout_height="72dp"
        android:background="@drawable/bg_avatar"
        android:gravity="center"
        android:text="记账"
        android:textSize="28sp" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="@string/login_title"
        android:textColor="@color/ink"
        android:textSize="24sp"
        android:textStyle="bold" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="4dp"
        android:alpha="0.6"
        android:text="@string/login_subtitle"
        android:textColor="@color/muted"
        android:textSize="14sp" />

    <EditText
        android:id="@+id/et_username"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="32dp"
        android:background="@drawable/bg_input"
        android:hint="@string/login_username"
        android:inputType="text"
        android:padding="12dp"
        android:textColor="@color/ink"
        android:textColorHint="@color/muted" />

    <EditText
        android:id="@+id/et_password"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="12dp"
        android:background="@drawable/bg_input"
        android:hint="@string/login_password"
        android:inputType="textPassword"
        android:padding="12dp"
        android:textColor="@color/ink"
        android:textColorHint="@color/muted" />

    <Button
        android:id="@+id/btn_login"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:backgroundTint="@color/primary"
        android:text="@string/login_btn"
        android:textColor="@android:color/white"
        android:textSize="16sp" />

    <Button
        android:id="@+id/btn_register"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:backgroundTint="@color/bg"
        android:text="@string/register_btn"
        android:textColor="@color/muted"
        android:textSize="14sp" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:alpha="0.5"
        android:text="@string/login_admin_hint"
        android:textColor="@color/muted"
        android:textSize="12sp" />

</LinearLayout>'''

with open(r'd:\programme\Android_Project\Android\Working\app\src\main\res\layout\activity_login.xml', 'wb') as f:
    f.write(content.encode('utf-8'))
print('Done')