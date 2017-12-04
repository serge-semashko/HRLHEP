package myiss;

import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;

import jxl.format.Border;
import jxl.format.BorderLineStyle;

import jxl.write.DateFormats;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormat;
import jxl.write.NumberFormats;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;


public class ExcelTools {
    private static WritableCellFormat setBorder(int col, int row, int maxcol, 
                                         int maxrow, 
                                         WritableCellFormat cf) throws WriteException {
       // cf.setShrinkToFit(true);
        cf.setBorder(Border.ALL, BorderLineStyle.THIN);
        if (col == 0)
            cf.setBorder(Border.LEFT, BorderLineStyle.THICK);
        if (col == maxcol - 1)
            cf.setBorder(Border.RIGHT, BorderLineStyle.THICK);
        if (row == 0)
            cf.setBorder(Border.TOP, BorderLineStyle.THICK);
        if (row == maxrow - 1)
            cf.setBorder(Border.BOTTOM, BorderLineStyle.THICK);
        cf.setWrap(false);
        return cf;
    }
    
    public static void doExcel(List<HashMap> in, 
                         String sheetName, OutputStream os) throws IOException, 
                                                           WriteException, 
                                                           RowsExceededException {
        if(sheetName == null) sheetName = "Лист 1";
        doExcel(in, null, sheetName, null, true, 1,1, os);
    }

    public static void doExcel(List<HashMap> in, OutputStream os) throws IOException, 
                                                           WriteException, 
                                                           RowsExceededException {
    doExcel(in, null, "Лист 1", null, true, 1,1, os);
    }

    private static void doExcel(List<HashMap> in, ArrayList<String> cols, 
                         String sheetName, 
                         ArrayList<String> headers,
                         boolean drawHeaders, int dx, int dy, OutputStream os) throws IOException, 
                                                           WriteException, 
                                                           RowsExceededException {
        WorkbookSettings ws = new WorkbookSettings();
        NumberFormat floatFormat = new NumberFormat("#.###################");
        ws.setLocale(new Locale("ru", "RU"));
        WritableWorkbook wb = 
        Workbook.createWorkbook(os,ws);
            //Workbook.createWorkbook(new File("c:/___My-tmp.xls"), ws);
        WritableSheet s = wb.createSheet(sheetName, 0);

        //установка шрифтов
        WritableFont wf = 
            new WritableFont(WritableFont.ARIAL, WritableFont.DEFAULT_POINT_SIZE, 
                             WritableFont.NO_BOLD);
        WritableFont hwf = 
            new WritableFont(WritableFont.ARIAL, WritableFont.DEFAULT_POINT_SIZE, 
                             WritableFont.BOLD);
        if (in.size() > 0) {
            HashMap hm = in.get(0);

            ArrayList<String> set;

            if (cols == null) {
                set = new ArrayList(hm.keySet());
            } else {
                set = cols;
            }

            if (headers == null) {
                headers = new ArrayList(hm.keySet());
            }
            if(drawHeaders){
                
                for (int i = 0; i < headers.size(); i++)  {
                    WritableCellFormat cf = new WritableCellFormat(hwf);
                    cf = setBorder(i, 0, headers.size(), 1, cf);
                    Label label = 
                        new Label(dx+i, dy, headers.get(i), cf);
                    s.addCell(label);
                }
                dy++;
                
            }
            if (set.size() > 0) {
                for (int i = 0; i < in.size(); i++) {
                    hm = in.get(i);
                    Iterator iter = set.iterator();
                    int j = 0;
                    while (iter.hasNext()) {
                        String key = (String)iter.next();
                        if (hm.get(key) instanceof String) {
                            WritableCellFormat cf = new WritableCellFormat(wf);
                            cf = setBorder(j, i, set.size(), in.size(), cf);
                            Label label = 
                                new Label(j+dx, i+dy, (String)hm.get(key), cf);
                            s.addCell(label);
                        } else if (hm.get(key) instanceof Integer) {
                            WritableCellFormat cf = 
                                new WritableCellFormat(NumberFormats.INTEGER);
                            cf = setBorder(j, i, set.size(), in.size(), cf);
                            Number n = 
                                new Number(j+dx, i+dy, (Integer)hm.get(key), cf);

                            s.addCell(n);
                        }

                        else if (hm.get(key) instanceof Double) {
                            WritableCellFormat cf = 
                                new WritableCellFormat(NumberFormats.FLOAT);
                            cf = setBorder(j, i, set.size(), in.size(), cf);
                            Number n = 
                                new Number(j+dx, i+dy, (Double)hm.get(key), cf);
                            s.addCell(n);
                        }

                        else if (hm.get(key) instanceof Float) {

                            WritableCellFormat cf = 
                                new WritableCellFormat(floatFormat);
                            cf = setBorder(j, i, set.size(), in.size(), cf);
                            Number n = 
                                new Number(j+dx, i+dy, (Float)hm.get(key), cf);
                            s.addCell(n);
                        } else if (hm.get(key) instanceof Date) {
                            WritableCellFormat cf = 
                                new WritableCellFormat(DateFormats.DEFAULT);
                            cf = setBorder(j, i, set.size(), in.size(), cf);
                            DateTime dt = 
                                new DateTime(j+dx, i+dy, (java.util.Date)hm.get(key), 
                                             cf);
                            s.addCell(dt);
                        } else {
                            WritableCellFormat cf = new WritableCellFormat(wf);

                            cf = setBorder(j, i, set.size(), in.size(), cf);

                            Label label = new Label(j+dx, i+dy, "", cf);
                            s.addCell(label);

                        }
                        j++;
                    }

                }

            }
        }
        wb.write();
        wb.close();

    }

    public ExcelTools() {
    }
}
