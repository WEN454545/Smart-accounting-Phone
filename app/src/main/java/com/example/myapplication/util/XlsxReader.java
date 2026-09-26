package com.example.myapplication.util;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Minimal .xlsx reader (no external dependencies) used by the bill import
 * feature to accept WeChat bill exports (.xlsx format).
 *
 * xlsx is a zip of XML files: sharedStrings.xml holds the string table and
 * xl/worksheets/sheet1.xml holds the cell grid. The WeChat table layout is:
 *   - about 17 metadata rows, then a header row containing a cell named
 *     交易时间 with columns 交易时间/交易类型/交易对方/商品/收/支/金额(元)/
 *     支付方式/当前状态/交易单号/商户单号/备注
 *   - the transaction time cell is an Excel serial number (days since 1899-12-30)
 *   - the direction column is 支出 / 收入 / 中性交易 (neutral rows are skipped)
 *
 * parseToCsv converts data rows into the app's own CSV line format
 * (date,type,,amount,note) so CsvImportPreviewActivity's existing pipeline
 * (preview / category inference / confirmation) works unchanged.
 */
public class XlsxReader {

    private static final long DAY_MS = 86400000L;
    /** Days between the Excel epoch (1899-12-30) and the Unix epoch (1970-01-01). */
    private static final long EXCEL_EPOCH_OFFSET_DAYS = 25569L;

    /** True when the bytes look like a zip container (PK\x03\x04), i.e. an .xlsx file. */
    public static boolean isZipBytes(byte[] data) {
        return data != null && data.length > 4
                && data[0] == 'P' && data[1] == 'K' && data[2] == 0x03 && data[3] == 0x04;
    }

    /**
     * Parses WeChat bill xlsx bytes and returns CSV text in the app import
     * format. Neutral transactions (充值/提现 etc.) are skipped. Returns an
     * empty string when no bill table is found.
     */
    public static String parseToCsv(byte[] data) throws Exception {
        List<String[]> rows = parseRows(data);
        int headerIdx = -1;
        Map<String, Integer> col = new LinkedHashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            String[] r = rows.get(i);
            // Tolerant header match: locate the "交易时间" cell with trim so
            // stray spaces in a future WeChat export cannot break detection
            int dateCol = -1;
            for (int c = 0; c < r.length; c++) {
                if (r[c].trim().contains("交易时间")) {
                    dateCol = c;
                    break;
                }
            }
            if (dateCol >= 0) {
                headerIdx = i;
                for (int c = 0; c < r.length; c++) {
                    if (!r[c].isEmpty() && !col.containsKey(r[c].trim())) col.put(r[c].trim(), c);
                }
                break;
            }
        }
        // Fuzzy fallback: if the exact "交易时间" keyword is missing (renamed
        // columns in a future WeChat export), detect the header row by generic
        // keywords and map the three essential columns positionally
        if (headerIdx < 0) {
            for (int i = 0; i < rows.size(); i++) {
                String[] r = rows.get(i);
                int dc = -1, tc = -1, ac = -1;
                for (int c = 0; c < r.length; c++) {
                    String h = r[c].trim();
                    if (dc < 0 && (h.contains("时间") || h.contains("日期"))) dc = c;
                    if (tc < 0 && h.contains("收") && h.contains("支")) tc = c;
                    if (ac < 0 && h.contains("金额")) ac = c;
                }
                if (dc >= 0 && tc >= 0 && ac >= 0) {
                    headerIdx = i;
                    col.put("交易时间", dc);
                    col.put("收/支", tc);
                    col.put("金额(元)", ac);
                    for (int c = 0; c < r.length; c++) {
                        if (c != dc && c != tc && c != ac && !r[c].trim().isEmpty() && !col.containsKey(r[c].trim())) {
                            col.put(r[c].trim(), c);
                        }
                    }
                    break;
                }
            }
        }
        if (headerIdx < 0) return "";

        StringBuilder sb = new StringBuilder();
        // parseCsv skips the first line as a header, so emit the template
        // header row to keep the first WeChat bill from being silently dropped
        sb.append("日期,类型,分类,金额,备注\n");
        for (int i = headerIdx + 1; i < rows.size(); i++) {
            String[] r = rows.get(i);
            String direction = cell(col, r, "收/支");
            boolean isExpense = "支出".equals(direction);
            boolean isIncome = "收入".equals(direction);
            if (!isExpense && !isIncome) continue; // 中性交易 / 空 / 其他

            String dateStr = toDateTime(cell(col, r, "交易时间"));
            String amount = cell(col, r, "金额(元)").replace("¥", "").replace("￥", "").trim();
            // note: prefer the merchant name, fall back to the product; "/" placeholders are ignored
            String note = cell(col, r, "交易对方");
            if (note.isEmpty() || "/".equals(note)) note = cell(col, r, "商品");
            if ("/".equals(note)) note = "";

            if (dateStr.isEmpty() || amount.isEmpty()) continue;
            try {
                Double.parseDouble(amount);
            } catch (NumberFormatException e) {
                continue;
            }

            sb.append(dateStr).append(',')
                    .append(isExpense ? "支出" : "收入").append(",,")
                    .append(amount).append(',')
                    .append(csvQuote(note)).append('\n');
        }
        return sb.toString();
    }

    /** Quote-aware CSV field splitter (also safe for unquoted template CSVs). */
    public static String[] splitCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    private static String csvQuote(String s) {
        if (s == null || s.isEmpty()) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }

    /** Excel serial number (or a plain date string) to "yyyy-MM-dd HH:mm". */
    private static String toDateTime(String v) {
        String s = v == null ? "" : v.trim();
        if (s.isEmpty()) return "";
        if (s.matches("\\d+(\\.\\d+)?")) {
            try {
                double days = Double.parseDouble(s);
                long ms = (long) ((days - EXCEL_EPOCH_OFFSET_DAYS) * DAY_MS);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
                // WeChat bills state "all times are UTC+08:00" (note in every
                // export) - render the fixed wall time regardless of device TZ
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+08:00"));
                return sdf.format(new Date(ms));
            } catch (NumberFormatException e) {
                return "";
            }
        }
        return s.length() >= 16 ? s.substring(0, 16) : s;
    }

    private static int indexOf(String[] r, String name) {
        for (int i = 0; i < r.length; i++) {
            if (name.equals(r[i])) return i;
        }
        return -1;
    }

    private static String cell(Map<String, Integer> col, String[] r, String name) {
        Integer idx = col.get(name);
        if (idx == null || idx >= r.length) return "";
        return r[idx] == null ? "" : r[idx].trim();
    }

    /** Parses the whole workbook into a grid of raw cell strings. */
    private static List<String[]> parseRows(byte[] data) throws Exception {
        List<String> shared = new ArrayList<>();
        byte[] sheetXml = null;
        ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(data));
        ZipEntry entry;
        while ((entry = zin.getNextEntry()) != null) {
            String name = entry.getName();
            if ("xl/sharedStrings.xml".equals(name)) {
                shared = readSharedStrings(zin);
            } else if (name.startsWith("xl/worksheets/sheet") && name.endsWith(".xml") && sheetXml == null) {
                sheetXml = readAll(zin);
            }
            zin.closeEntry();
        }
        zin.close();

        List<String[]> rows = new ArrayList<>();
        if (sheetXml == null) return rows;

        XmlPullParser p = XmlPullParserFactory.newInstance().newPullParser();
        p.setInput(new ByteArrayInputStream(sheetXml), null);

        Map<Integer, String> curRow = null;
        int cellCol = -1;
        String cellType = null;
        StringBuilder inline = null;

        int ev = p.getEventType();
        while (ev != XmlPullParser.END_DOCUMENT) {
            if (ev == XmlPullParser.START_TAG) {
                String n = p.getName();
                if ("row".equals(n)) {
                    curRow = new LinkedHashMap<>();
                } else if ("c".equals(n) && curRow != null) {
                    cellCol = colFromRef(p.getAttributeValue(null, "r"));
                    cellType = p.getAttributeValue(null, "t");
                } else if ("v".equals(n) && curRow != null) {
                    String text = p.nextText();
                    if (cellCol >= 0) {
                        if ("s".equals(cellType)) {
                            try {
                                int si = Integer.parseInt(text.trim());
                                curRow.put(cellCol, si >= 0 && si < shared.size() ? shared.get(si) : "");
                            } catch (NumberFormatException e) {
                                curRow.put(cellCol, "");
                            }
                        } else {
                            curRow.put(cellCol, text);
                        }
                    }
                    cellType = null;
                } else if ("is".equals(n) && curRow != null) {
                    inline = new StringBuilder();
                } else if ("t".equals(n) && inline != null) {
                    inline.append(p.nextText());
                }
            } else if (ev == XmlPullParser.END_TAG) {
                String n = p.getName();
                if ("is".equals(n) && curRow != null && inline != null) {
                    if (cellCol >= 0) curRow.put(cellCol, inline.toString());
                    inline = null;
                } else if ("row".equals(n) && curRow != null) {
                    int size = 0;
                    for (Integer k : curRow.keySet()) size = Math.max(size, k + 1);
                    String[] arr = new String[size];
                    for (int c = 0; c < size; c++) arr[c] = curRow.containsKey(c) ? curRow.get(c) : "";
                    rows.add(arr);
                    curRow = null;
                    cellCol = -1;
                    cellType = null;
                }
            }
            ev = p.next();
        }
        return rows;
    }

    /** Cell reference like "B18" to a zero-based column index (B -> 1). */
    private static int colFromRef(String ref) {
        if (ref == null) return -1;
        int idx = 0;
        for (int i = 0; i < ref.length(); i++) {
            char c = ref.charAt(i);
            if (c >= 'A' && c <= 'Z') idx = idx * 26 + (c - 'A' + 1);
            else if (c >= 'a' && c <= 'z') idx = idx * 26 + (c - 'a' + 1);
            else break;
        }
        return idx - 1;
    }

    private static List<String> readSharedStrings(InputStream in) throws Exception {
        List<String> out = new ArrayList<>();
        XmlPullParser p = XmlPullParserFactory.newInstance().newPullParser();
        p.setInput(in, null);
        StringBuilder sb = null;
        int ev = p.getEventType();
        while (ev != XmlPullParser.END_DOCUMENT) {
            if (ev == XmlPullParser.START_TAG) {
                if ("si".equals(p.getName())) {
                    sb = new StringBuilder();
                } else if ("t".equals(p.getName()) && sb != null) {
                    sb.append(p.nextText());
                }
            } else if (ev == XmlPullParser.END_TAG && "si".equals(p.getName())) {
                out.add(sb == null ? "" : sb.toString());
                sb = null;
            }
            ev = p.next();
        }
        return out;
    }

    private static byte[] readAll(InputStream in) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) bos.write(buf, 0, n);
        return bos.toByteArray();
    }
}
