package com.example.autobookkeep.util;

import android.app.AlertDialog;
import android.content.Context;

import android.os.Build;
import android.view.WindowManager;
import android.widget.Button;

import java.util.HashMap;
import java.util.Map;

public class CurrencyUtils {

    private static final Map<String, String> SYMBOL_TO_CODE = buildSymbolToCodeMap();

    private static Map<String, String> buildSymbolToCodeMap() {
        Map<String, String> map = new HashMap<>();
        map.put("¥", "CNY"); map.put("$", "USD"); map.put("€", "EUR"); map.put("£", "GBP");
        map.put("HK$", "HKD"); map.put("NT$", "TWD"); map.put("JP¥", "JPY"); map.put("₩", "KRW");
        map.put("C$", "CAD"); map.put("A$", "AUD"); map.put("S$", "SGD"); map.put("NZ$", "NZD");
        map.put("₹", "INR"); map.put("₽", "RUB"); map.put("฿", "THB"); map.put("₫", "VND");
        map.put("₱", "PHP"); map.put("R$", "BRL"); map.put("Rp", "IDR"); map.put("RM", "MYR");
        map.put("CHF", "CHF"); map.put("₺", "TRY"); map.put("₪", "ILS"); map.put("kr", "SEK");
        map.put("zł", "PLN"); map.put("Kč", "CZK"); map.put("Ft", "HUF"); map.put("lei", "RON");
        map.put("лв", "BGN"); map.put("₴", "UAH"); map.put("L", "MDL");
        map.put("KD", "KWD"); map.put("SR", "SAR"); map.put("DH", "AED"); map.put("R", "ZAR");
        map.put("₦", "NGN"); map.put("E£", "EGP");
        return java.util.Collections.unmodifiableMap(map);
    }

    public static String symbolToCode(String symbol) {
        if (symbol == null || symbol.isEmpty()) return "CNY";
        String code = SYMBOL_TO_CODE.get(symbol);
        return code != null ? code : "CNY";
    }

    public static final String[] CURRENCY_DISPLAY = {
            "¥ 人民币", "$ 美元", "€ 欧元", "£ 英镑", "HK$ 港币", "NT$ 新台币",
            "JP¥ 日元", "₩ 韩元", "C$ 加元", "A$ 澳元", "S$ 新加坡元", "NZ$ 新西兰元",
            "₹ 印度卢比", "₽ 俄卢布", "฿ 泰铢", "₫ 越南盾", "₱ 比索", "R$ 雷亚尔",
            "Rp 印尼盾", "RM 林吉特", "CHF 瑞郎", "₺ 土耳其里拉", "₪ 谢克尔",
            "SEK 瑞典克朗", "zł 兹罗提", "Kč 捷克克朗", "Ft 福林", "lei 列伊", "лв 列夫",
            "₴ 格里夫纳", "L 列伊", "KD 科威特第纳尔", "SR 沙特里亚尔", "DH 迪拉姆", "R 兰特", "₦ 奈拉", "E£ 埃及镑"
    };

    public static final String[] CURRENCY_SYMBOLS = {
            "¥", "$", "€", "£", "HK$", "NT$",
            "JP¥", "₩", "C$", "A$", "S$", "NZ$",
            "₹", "₽", "฿", "₫", "₱", "R$",
            "Rp", "RM", "CHF", "₺", "₪",
            "SEK", "zł", "Kč", "Ft", "lei", "лв",
            "₴", "L", "KD", "SR", "DH", "R", "₦", "E£"
    };

    /**
     * 显示自定义货币选择弹窗（简化版，使用 AlertDialog）
     */
    public static void showCurrencyDialog(Context context, Button targetBtn, boolean isOverlay) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("选择货币");
        builder.setItems(CURRENCY_DISPLAY, (dialog, which) -> {
            if (which >= 0 && which < CURRENCY_SYMBOLS.length) {
                targetBtn.setText(CURRENCY_SYMBOLS[which]);
            }
        });
        AlertDialog dialog = builder.create();
        if (isOverlay) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            } else {
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
            }
        }
        dialog.show();
    }
}