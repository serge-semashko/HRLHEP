package myiss;

import java.io.File;
import java.io.IOException;

import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;

import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.Orientation;

import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class ExcelJob {

	private static WritableWorkbook workbook; // переменная рабочей книги
	public static WritableSheet sheet;
	public static WritableCellFormat arial12BoldFormat;
	public static Label label;

	
	public static void exelSet() throws WriteException // метод создает книгу с одной раб страницей
	{
		WorkbookSettings ws = new WorkbookSettings();
		ws.setLocale(new Locale("ru", "RU"));

		try {
			workbook = Workbook.createWorkbook(new File("c:/111My-tmp.xls"), ws);		//имя и путь файла
			sheet = workbook.createSheet("Отрицательные остатки", 0);	//название листа

			/*
			 * здесь необходимо перечислить методы, которые будут выполняться
			 * для заполнения листа
			 */

			test();
			
		} catch (IOException e) {
			// TODO Автоматически созданный блок catch
			e.printStackTrace();
		}
		
		try {
			workbook.write();
			workbook.close();
		} catch (IOException e) {
			// TODO Автоматически созданный блок catch
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Автоматически созданный блок catch
			e.printStackTrace();
		} 
	
	}
	
	public static void test() throws WriteException
	{
		//установка шрифта
		WritableFont arial12ptBold = new WritableFont(WritableFont.ARIAL, 12, WritableFont.BOLD);
		arial12BoldFormat = new WritableCellFormat(arial12ptBold);
		//выравнивание по центру
    	arial12BoldFormat.setAlignment(Alignment.CENTRE);
    	//перенос по словам если не помещается
    	arial12BoldFormat.setWrap(true);
    	//установить цвет
    	arial12BoldFormat.setBackground(Colour.GRAY_50);
    	//рисуем рамку
    	arial12BoldFormat.setBorder(Border.ALL, BorderLineStyle.MEDIUM);
		//поворот текста
    	arial12BoldFormat.setOrientation(Orientation.PLUS_90);
    	
    	//пример добавления в ячейки
    	
    	int i;
    	int q;
    	q = 10;
    	    	
		for(i=9;i<22;i++)
		{
			Label label = new Label(1, i, "С "+ i + " по " + q, arial12BoldFormat);
			sheet.addCell(label);
			q++;
		}
    		
   		Label label1 = new Label(2, 2, "значение", arial12BoldFormat);
   		try {
			sheet.addCell(label1);	//добавление данных в лист sheet с обработкой исключений
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}

	}
	
}