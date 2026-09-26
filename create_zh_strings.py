# -*- coding: utf-8 -*-
content = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">绝希</string>
    <string name="action_settings">设置</string>
    <string name="accessibility_service_description">自动记账无障碍服务：用于自动识别微信、支付宝、拼多多等App的支付页面</string>
    <string name="usage_tips">使用步骤：\n1. 开启无障碍服务\n2. 开启悬浮窗权限\n3. 打开支付应用\n4. 确认并记录账单</string>

    <string name="auto_settings_title">自动记账设置</string>
    <string name="category_settings_title">记账分类预设</string>
    <string name="category_settings_desc">管理收入和支出</string>
    <string name="assistant_settings_title">自动记账助手</string>
    <string name="assistant_settings_desc">管理自动记账开关</string>
    <string name="budget_settings_title">月度预算设置</string>
    <string name="budget_settings_desc">设置月度预算上限</string>
    <string name="log_settings_title">自动记账适配日志</string>
    <string name="log_settings_desc">查看无障碍服务扫描</string>

    <string name="assistant_manager_title">记账助手设置</string>
    <string name="auto_track_title">开启屏幕自动记账</string>
    <string name="auto_track_desc">需开启无障碍服务</string>
    <string name="assets_title">开启资产功能</string>
    <string name="assets_desc">在底部导航栏显示</string>
    <string name="details_title">开启明细功能</string>
    <string name="details_desc">在底部导航栏显示</string>
    <string name="overlay_title">悬浮窗权限</string>
    <string name="overlay_desc">点击开启</string>
    <string name="overlay_btn">去开启</string>
    <string name="overlay_enabled">已开启</string>
    <string name="overlay_enabled_status">悬浮窗权限已开启</string>
    <string name="keywords_title">屏幕读取关键字</string>
    <string name="add_keyword">+ 添加</string>

    <string name="accessibility_title">无障碍服务未开启</string>
    <string name="accessibility_message">请在系统设置中开启绝希的无障碍服务</string>
    <string name="accessibility_btn">去设置</string>

    <string name="track_enabled">已开启屏幕自动记账</string>
    <string name="track_disabled">已关闭屏幕自动记账</string>
    <string name="assets_enabled">已开启资产功能</string>
    <string name="assets_disabled">已关闭资产功能</string>
    <string name="details_enabled">已开启明细功能</string>
    <string name="details_disabled">已关闭明细功能</string>

    <string name="overlay_already_enabled">悬浮窗权限已开启</string>
    <string name="overlay_enabled_btn">已开启</string>
    <string name="overlay_disabled">点击开启悬浮窗权限</string>

    <string name="app_wechat">微信</string>
    <string name="app_alipay">支付宝</string>
    <string name="app_pinduoduo">拼多多</string>
    <string name="app_jingdong">京东</string>
    <string name="app_unionpay">云闪付</string>

    <string name="income_label">收入</string>
    <string name="expense_label">支出</string>

    <string name="enter_keyword">请输入关键字</string>
    <string name="keyword_added">关键字已添加</string>
    <string name="keyword_updated">关键字已更新</string>
    <string name="keyword_deleted">关键字已删除</string>

    <string name="delete_keyword">删除关键字</string>
    <string name="delete_confirm">确定删除 [%1$s - %2$s]?</string>
    <string name="delete_btn">删除</string>
    <string name="cancel_btn">取消</string>

    <string name="home_this_month">本月账单</string>
    <string name="home_view_all">查看全部</string>
    <string name="home_income">本月收入</string>
    <string name="home_expense">本月支出</string>
    <string name="home_budget">月度预算</string>
    <string name="home_budget_remaining">剩余</string>
    <string name="home_budget_used">已用</string>

    <string name="cal_prev_month">上一月</string>
    <string name="cal_next_month">下一月</string>
    <string name="cal_income">收入</string>
    <string name="cal_expense">支出</string>
    <string name="cal_empty_hint">点击日期查看账单</string>

    <string name="stats_week">周</string>
    <string name="stats_month">月</string>
    <string name="stats_year">年</string>
    <string name="stats_expense_category">支出类别占比</string>
    <string name="stats_income_category">收入类别占比</string>
    <string name="stats_trend">收支趋势</string>

    <string name="add_bill_title">添加账单</string>
    <string name="add_bill_expense">支出</string>
    <string name="add_bill_income">收入</string>
    <string name="add_bill_amount">金额</string>
    <string name="add_bill_amount_hint">0.00</string>
    <string name="add_bill_category">分类</string>
    <string name="add_bill_date">日期</string>
    <string name="add_bill_date_hint">选择日期</string>
    <string name="add_bill_note">备注</string>
    <string name="add_bill_note_hint">输入备注</string>
    <string name="add_bill_save">保存</string>

    <string name="login_title">绝希</string>
    <string name="login_subtitle">登录您的账号</string>
    <string name="login_username">用户名</string>
    <string name="login_password">密码</string>
    <string name="login_btn">登录</string>
    <string name="register_btn">注册新账号</string>
    <string name="login_admin_hint">管理员: admin / 123456</string>

    <string name="all_bills_title">全部账单</string>
    <string name="back">返回</string>
    <string name="delete_all">删除全部</string>
</resources>"""

with open(r'd:\programme\Android_Project\Android\Working\app\src\main\res\values-zh\strings.xml', 'wb') as f:
    f.write(content.encode('utf-8'))
print('Done')