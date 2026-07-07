# -*- coding: utf-8 -*-
import os

def write_file(path, content):
    with open(path, 'wb') as f:
        f.write(content.encode('utf-8'))

auto_settings = """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/root_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp"
    android:background="@color/bar_background">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center_vertical"
        android:paddingBottom="24dp"
        android:paddingTop="8dp">
        <ImageView
            android:layout_width="32dp"
            android:layout_height="32dp"
            android:src="@drawable/ic_settings"
            android:tint="?android:attr/textColorPrimary"
            android:layout_marginEnd="12dp"/>
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Zi Dong Ji Zhang She Zhi"
            android:textSize="28sp"
            android:textStyle="bold"
            android:textColor="?android:attr/textColorPrimary"/>
    </LinearLayout>

        <androidx.cardview.widget.CardView
        android:id="@+id/card_category"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:cardCornerRadius="20dp"
        app:cardElevation="0dp"
        app:cardBackgroundColor="@color/white"
        android:layout_marginBottom="16dp"
        android:clickable="true"
        android:focusable="true"
        android:foreground="?attr/selectableItemBackground">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:padding="20dp">

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical">
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Ji Zhang Fen Lei Yu She"
                    android:textSize="18sp"
                    android:textStyle="bold"
                    android:textColor="?android:attr/textColorPrimary"/>
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Guan Li Shou Ru He Zhi Chu"
                    android:textSize="13sp"
                    android:textColor="@color/text_hint"
                    android:layout_marginTop="6dp"/>
            </LinearLayout>

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text=">"
                    android:textSize="20sp"
                    android:textColor="@color/text_divider"/>
        </LinearLayout>
    </androidx.cardview.widget.CardView>

    <androidx.cardview.widget.CardView
        android:id="@+id/card_assistant"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:cardCornerRadius="20dp"
        app:cardElevation="0dp"
        app:cardBackgroundColor="@color/white"
        android:layout_marginBottom="16dp"
        android:clickable="true"
        android:focusable="true"
        android:foreground="?attr/selectableItemBackground">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:padding="20dp">

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical">
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Zi Dong Ji Zhang Zhu Shou"
                    android:textSize="18sp"
                    android:textStyle="bold"
                    android:textColor="?android:attr/textColorPrimary"/>
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Guan Li Zi Dong Ji Zhang Kai Guan"
                    android:textSize="13sp"
                    android:textColor="@color/text_hint"
                    android:layout_marginTop="6dp"/>
            </LinearLayout>

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text=">"
                    android:textSize="20sp"
                    android:textColor="@color/text_divider"/>
        </LinearLayout>
    </androidx.cardview.widget.CardView>

    <androidx.cardview.widget.CardView
        android:id="@+id/card_budget"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:cardCornerRadius="20dp"
        app:cardElevation="0dp"
        app:cardBackgroundColor="@color/white"
        android:layout_marginBottom="16dp"
        android:clickable="true"
        android:focusable="true"
        android:foreground="?attr/selectableItemBackground">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:padding="20dp">

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical">
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="月度预算设置"
                    android:textSize="18sp"
                    android:textStyle="bold"
                    android:textColor="?android:attr/textColorPrimary"/>
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="She Zhi Dang Yue Yue Du Yu Suan Shang Xian"
                    android:textSize="13sp"
                    android:textColor="@color/text_hint"
                    android:layout_marginTop="6dp"/>
            </LinearLayout>

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text=">"
                    android:textSize="20sp"
                    android:textColor="@color/text_divider"/>
        </LinearLayout>
    </androidx.cardview.widget.CardView>

    <androidx.cardview.widget.CardView
        android:id="@+id/card_log"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:cardCornerRadius="20dp"
        app:cardElevation="0dp"
        app:cardBackgroundColor="@color/white"
        android:clickable="true"
        android:focusable="true"
        android:foreground="?attr/selectableItemBackground">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:padding="20dp">

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical">
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Zi Dong Ji Zhang Shi Pei Ri Zhi"
                    android:textSize="18sp"
                    android:textStyle="bold"
                    android:textColor="?android:attr/textColorPrimary"/>
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Cha Kan Wu Zhang Ai Fu Wu Sao Miao"
                    android:textSize="13sp"
                    android:textColor="@color/text_hint"
                    android:layout_marginTop="6dp"/>
            </LinearLayout>

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text=">"
                    android:textSize="20sp"
                    android:textColor="@color/text_divider"/>
        </LinearLayout>
    </androidx.cardview.widget.CardView>

</LinearLayout>"""

assistant_manager = """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/root_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:importantForAccessibility="noHideDescendants"
    android:padding="16dp"
    android:background="@color/bar_background">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center_vertical"
        android:paddingBottom="24dp"
        android:paddingTop="8dp">
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Ji Zhang Zhu Shou She Zhi"
            android:textSize="28sp"
            android:textStyle="bold"
            android:textColor="?android:attr/textColorPrimary"/>
    </LinearLayout>

    <androidx.cardview.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:cardCornerRadius="20dp"
        app:cardElevation="0dp"
        app:cardBackgroundColor="@color/white"
        android:layout_marginBottom="16dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:padding="20dp">
                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:orientation="vertical">
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Kai Qi Ping Mu Zi Dong Ji Zhang"
                        android:textSize="16sp"
                        android:textColor="?android:attr/textColorPrimary"/>
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Xu Kai Qi Wu Zhang Ai Fu Wu"
                        android:textSize="12sp"
                        android:textColor="@color/text_hint"
                        android:layout_marginTop="4dp"/>
                </LinearLayout>
                <androidx.appcompat.widget.SwitchCompat
                    android:id="@+id/switchAutoTrack"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:thumb="@drawable/switch_thumb_custom"
                    app:track="@drawable/switch_track_custom"
                    app:showText="false"
                    app:thumbTint="@null"
                    app:trackTint="@null"
                    android:background="@null"/>
            </LinearLayout>

            <View android:layout_width="match_parent" android:layout_height="1dp" android:background="@color/divider" android:layout_marginHorizontal="20dp"/>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:padding="20dp">
                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:orientation="vertical">
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Kai Qi Zi Chan Gong Neng"
                        android:textSize="16sp"
                        android:textColor="?android:attr/textColorPrimary"/>
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Zai Di Bu Dao Hang Lan Xian Shi"
                        android:textSize="12sp"
                        android:textColor="@color/text_hint"
                        android:layout_marginTop="4dp"/>
                </LinearLayout>
                <androidx.appcompat.widget.SwitchCompat
                    android:id="@+id/switchAssets"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:thumb="@drawable/switch_thumb_custom"
                    app:track="@drawable/switch_track_custom"
                    app:showText="false"
                    app:thumbTint="@null"
                    app:trackTint="@null"
                    android:background="@null"/>
            </LinearLayout>

            <View android:layout_width="match_parent" android:layout_height="1dp" android:background="@color/divider" android:layout_marginHorizontal="20dp"/>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:padding="20dp">
                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:orientation="vertical">
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Kai Qi Ming Xi Gong Neng"
                        android:textSize="16sp"
                        android:textColor="?android:attr/textColorPrimary"/>
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Zai Di Bu Dao Hang Lan Xian Shi"
                        android:textSize="12sp"
                        android:textColor="@color/text_hint"
                        android:layout_marginTop="4dp"/>
                </LinearLayout>
                <androidx.appcompat.widget.SwitchCompat
                    android:id="@+id/switchDetails"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:thumb="@drawable/switch_thumb_custom"
                    app:track="@drawable/switch_track_custom"
                    app:showText="false"
                    app:thumbTint="@null"
                    app:trackTint="@null"
                    android:background="@null"/>
            </LinearLayout>

            <View android:layout_width="match_parent" android:layout_height="1dp" android:background="@color/divider" android:layout_marginHorizontal="20dp"/>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:padding="20dp"
                android:clickable="true"
                android:focusable="true"
                android:foreground="?attr/selectableItemBackground">
                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:orientation="vertical">
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Xu Fu Chuang Quan Xian"
                        android:textSize="16sp"
                        android:textColor="?android:attr/textColorPrimary"/>
                    <TextView
                        android:id="@+id/tv_overlay_status"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Dian Ji Kai Qi"
                        android:textSize="12sp"
                        android:textColor="@color/text_hint"
                        android:layout_marginTop="4dp"/>
                </LinearLayout>
                <Button
                    android:id="@+id/btn_overlay"
                    android:layout_width="wrap_content"
                    android:layout_height="36dp"
                    android:text="Qu Kai Qi"
                    android:textColor="@color/text_white"
                    android:textSize="12sp"
                    app:cornerRadius="12dp"
                    android:backgroundTint="?android:attr/colorPrimary"/>
            </LinearLayout>

        </LinearLayout>
    </androidx.cardview.widget.CardView>

    <androidx.cardview.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        app:cardCornerRadius="20dp"
        app:cardElevation="0dp"
        app:cardBackgroundColor="@color/white">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:orientation="vertical"
            android:padding="20dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:gravity="center_vertical"
                android:layout_marginBottom="12dp">

                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="Ping Mu Du Qu Guan Jian Zi"
                    android:textSize="14sp"
                    android:textColor="@color/text_secondary"/>

                <Button
                    android:id="@+id/btnAddKeyword"
                    android:layout_width="wrap_content"
                    android:layout_height="42dp"
                    android:text="+ Tian Jia"
                    android:textColor="@color/text_white"
                    android:textSize="12sp"
                    app:cornerRadius="16dp"
                    android:backgroundTint="?android:attr/colorPrimary"
                    android:layout_marginEnd="8dp"/>

            </LinearLayout>

            <androidx.recyclerview.widget.RecyclerView
                android:id="@+id/rvKeywords"
                android:layout_width="match_parent"
                android:layout_height="match_parent"/>
        </LinearLayout>
    </androidx.cardview.widget.CardView>

</LinearLayout>"""

base_path = r'd:\programme\Android_Project\Android\Working\app\src\main\res\layout'
write_file(os.path.join(base_path, 'activity_auto_settings.xml'), auto_settings)
write_file(os.path.join(base_path, 'activity_assistant_manager.xml'), assistant_manager)
print('Done')
