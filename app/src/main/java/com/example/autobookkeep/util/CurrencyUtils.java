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

    /**
     * Approximate exchange rates to CNY (1 unit of foreign currency = X CNY).
     * These are rough estimates and should be updated periodically.
     * For CNY (yuan), the rate is 1.0.
     */
    private static final Map<String, Double> SYMBOL_TO_CNY_RATE = buildRateMap();

    private static Map<String, String> buildSymbolToCodeMap() {
        Map<String, String> map = new HashMap<>();
        map.put("\u00A5", "CNY"); map.put("$", "USD"); map.put("\u20AC", "EUR"); map.put("\u00A3", "GBP");
        map.put("HK$", "HKD"); map.put("NT$", "TWD"); map.put("JP\u00A5", "JPY"); map.put("\u20A9", "KRW");
        map.put("C$", "CAD"); map.put("A$", "AUD"); map.put("S$", "SGD"); map.put("NZ$", "NZD");
        map.put("\u20B9", "INR"); map.put("\u20BD", "RUB"); map.put("\u0E3F", "THB"); map.put("\u20AB", "VND");
        map.put("\u20B1", "PHP"); map.put("R$", "BRL"); map.put("Rp", "IDR"); map.put("RM", "MYR");
        map.put("CHF", "CHF"); map.put("\u20BA", "TRY"); map.put("\u20AA", "ILS"); map.put("kr", "SEK");
        map.put("z\u0142", "PLN"); map.put("K\u010D", "CZK"); map.put("Ft", "HUF"); map.put("lei", "RON");
        map.put("\u043B\u0432", "BGN"); map.put("\u20B4", "UAH"); map.put("L", "MDL");
        map.put("KD", "KWD"); map.put("SR", "SAR"); map.put("DH", "AED"); map.put("R", "ZAR");
        map.put("\u20A6", "NGN"); map.put("E\u00A3", "EGP");
        return java.util.Collections.unmodifiableMap(map);
    }

    private static Map<String, Double> buildRateMap() {
        Map<String, Double> map = new HashMap<>();
        // 1 unit = X CNY (approximate)
        map.put("\u00A5", 1.0);        // CNY
        map.put("$", 7.25);            // USD
        map.put("\u20AC", 7.85);       // EUR
        map.put("\u00A3", 9.20);       // GBP
        map.put("HK$", 0.93);          // HKD
        map.put("NT$", 0.22);          // TWD
        map.put("JP\u00A5", 0.049);    // JPY
        map.put("\u20A9", 0.0054);     // KRW
        map.put("C$", 5.30);           // CAD
        map.put("A$", 4.75);           // AUD
        map.put("S$", 5.40);           // SGD
        map.put("NZ$", 4.35);          // NZD
        map.put("\u20B9", 0.087);      // INR
        map.put("\u20BD", 0.080);      // RUB
        map.put("\u0E3F", 0.20);       // THB
        map.put("\u20AB", 0.00029);    // VND
        map.put("\u20B1", 0.013);      // PHP
        map.put("R$", 1.45);           // BRL
        map.put("Rp", 0.00045);        // IDR
        map.put("RM", 1.55);           // MYR
        map.put("CHF", 8.20);          // CHF
        map.put("\u20BA", 0.21);       // TRY
        map.put("\u20AA", 1.95);       // ILS
        map.put("kr", 0.68);           // SEK
        map.put("z\u0142", 1.80);      // PLN
        map.put("K\u010D", 0.30);      // CZK
        map.put("Ft", 0.0020);         // HUF
        map.put("lei", 1.60);          // RON
        map.put("\u043B\u0432", 4.25); // BGN
        map.put("\u20B4", 0.018);      // UAH
        map.put("L", 0.40);            // MDL
        map.put("KD", 23.5);           // KWD
        map.put("SR", 1.93);           // SAR
        map.put("DH", 1.97);           // AED
        map.put("R", 0.039);           // ZAR
        map.put("\u20A6", 0.0044);     // NGN
        map.put("E\u00A3", 0.15);      // EGP
        return java.util.Collections.unmodifiableMap(map);
    }

    public static String symbolToCode(String symbol) {
        if (symbol == null || symbol.isEmpty()) return "CNY";
        String code = SYMBOL_TO_CODE.get(symbol);
        return code != null ? code : "CNY";
    }

    /**
     * Converts an amount in the given currency to CNY (yuan).
     * @param amount the amount in the foreign currency
     * @param currencySymbol the currency symbol (e.g. "$", "EUR")
     * @return the equivalent amount in CNY
     */
    public static double convertToCNY(double amount, String currencySymbol) {
        if (amount == 0) return 0;
        if (currencySymbol == null || currencySymbol.isEmpty()) return amount;
        Double rate = SYMBOL_TO_CNY_RATE.get(currencySymbol);
        if (rate == null) return amount; // unknown currency, return as-is
        return amount * rate;
    }

    public static final String[] CURRENCY_DISPLAY = {
            "\u00A5 \u4eba\u6c11\u5e01", "$ \u7f8e\u5143", "\u20AC \u6b27\u5143", "\u00A3 \u82f1\u9555", "HK$ \u6e2f\u5e01", "NT$ \u65b0\u53f0\u5e01",
            "JP\u00A5 \u65e5\u5143", "\u20A9 \u97e9\u5143", "C$ \u52a0\u5143", "A$ \u6fb3\u5143", "S$ \u65b0\u52a0\u5761\u5143", "NZ$ \u65b0\u897f\u5170\u5143",
            "\u20B9 \u5370\u5ea6\u5362\u6bd4", "\u20BD \u4fc4\u5362\u5e03", "\u0E3F \u6cf0\u94e2", "\u20AB \u8d8a\u5357\u76fe", "\u20B1 \u6bd4\u7d22", "R$ \u96f7\u4e9a\u5c14",
            "Rp \u5370\u5c3c\u76fe", "RM \u6797\u5409\u7279", "CHF \u745e\u90ce", "\u20BA \u571f\u8033\u5176\u91cc\u62c9", "\u20AA \u8c22\u514b\u5c14",
            "SEK \u745e\u5178\u514b\u6717", "z\u0142 \u5179\u7f57\u65af", "K\u010D \u6377\u514b\u514b\u6717", "Ft \u798f\u6797", "lei \u5217\u4f0a", "\u043B\u0432 \u5217\u592b",
            "\u20B4 \u683c\u91cc\u592b\u7eb3", "L \u5217\u4f0a", "KD \u79d1\u5a01\u7279\u7b2c\u7eb3\u5c14", "SR \u6c99\u7279\u91cc\u4e9a\u5c14", "DH \u8fea\u62c9\u59c6", "R \u5170\u7279", "\u20A6 \u5948\u62c9", "E\u00A3 \u57c3\u53ca\u9555"
    };

    public static final String[] CURRENCY_SYMBOLS = {
            "\u00A5", "$", "\u20AC", "\u00A3", "HK$", "NT$",
            "JP\u00A5", "\u20A9", "C$", "A$", "S$", "NZ$",
            "\u20B9", "\u20BD", "\u0E3F", "\u20AB", "\u20B1", "R$",
            "Rp", "RM", "CHF", "\u20BA", "\u20AA",
            "SEK", "z\u0142", "K\u010D", "Ft", "lei", "\u043B\u0432",
            "\u20B4", "L", "KD", "SR", "DH", "R", "\u20A6", "E\u00A3"
    };

    /**
     * Displays a custom currency selection dialog.
     */
    public static void showCurrencyDialog(Context context, Button targetBtn, boolean isOverlay) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("\u9009\u62e9\u8d27\u5e01");
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
