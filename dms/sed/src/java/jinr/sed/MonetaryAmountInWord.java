/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jinr.sed;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * Денежная сумма прописью
 * 
 * Абстрактные классы и реализация для конкретных языков и валют.
 * Код честно утянут из инета, поправлены названия методов кое-что по мелочи.
 */

public interface MonetaryAmountInWord {
    String numToString(Number centFlag, Number sum);
    // sum - денежная сумма
    // centFlag : 1 - копейки прописью; 0 - копейки цифрами
    
}

// нечто абстрактное
abstract class AbstractMonetaryAmountInWord implements MonetaryAmountInWord {
    // получение единиц, 11-19, десятков, сотен
    abstract protected String getS1(int n, int gender);
    abstract protected String getS11(int n);
    abstract protected String getS10(int n);
    abstract protected String getS100(int n);
    // преобразование триады в слова
    protected String triadToString(int n, int gender, boolean acceptZero) {
        if (!acceptZero && n == 0) return "";
        String res = "";
        if (n % 1000 > 99) {
            res += getS100(n % 1000 / 100) + " ";
        }
        if (n % 100 > 10 && n % 100 < 20) {
            return res + getS11(n % 10) + " ";
        }
        if (n % 100 > 9) {
            res += getS10(n % 100 / 10) + " ";
        }
        if (res.length() == 0 || n % 10 > 0) {
            res = res + getS1(n % 10, gender) + " ";
        }
        return res;
    }
    // получение юнита (название триады или валюта)
    abstract protected String getUnit(int idx, long count);
    // форма юнита (для русского языка - пол)
    abstract protected int getUnitGender(int idx);
    // наш главный метод
    public String numToString(Number centFlag, Number sum) {
        String res = "";
        String centRes = "";
        long rubSum = sum.longValue();
        long centSum = 0;
        
        int idx = 0;
        double newDD = new BigDecimal((sum.doubleValue() - rubSum)).setScale(2, RoundingMode.HALF_UP).doubleValue();
        centSum = (long)(newDD*100);
        
        String centTriad = triadToString((int)(centSum), getUnitGender(idx), idx < 1);

        // centFlag : 1 - копейки прописью; 0 - копейки цифрами
        if (centFlag.equals(1)){
            centRes = centTriad + getUnit(idx, centSum);
        }
        if (centFlag.equals(0)){
            if (!(centSum == 0)){
                centRes = centSum + " " + getUnit(idx, centSum);
            }
            if (centSum == 0){
                centRes = "00 " + getUnit(idx, centSum);
            }
        }
        
        if (rubSum == 0) {
            res = getS1(0, 0) + " " + getUnit(1, 0) + " ";
        }
        
        idx = 1;
        while (rubSum > 0) {
            String triad = triadToString((int)(rubSum % 1000),
                                            getUnitGender(idx), idx < 1);
            res = triad + getUnit(idx, rubSum % 1000) + " " + res;
            rubSum = rubSum / 1000; 
            idx++;
        }
        
        res = res + centRes;
        
	if(!(res == null || res.isEmpty()))
        {
            res = res.substring(0, 1).toUpperCase() + res.substring(1);
        }
        
        return res;
    }

}

// Реализация для русского языка:
abstract class MonetaryAmountInWordRu extends AbstractMonetaryAmountInWord {
    final String[][] str1 = {
        {"ноль","один","два","три","четыре","пять","шесть","семь","восемь","девять"},
        {"ноль","одна","две","три","четыре","пять","шесть","семь","восемь","девять"},
    };
    final String[] str100 = {"", "сто", "двести", "триста", "четыреста", "пятьсот",
            "шестьсот", "семьсот", "восемьсот", "девятьсот"};
    String[] str11 = {"", "одиннадцать", "двенадцать", "тринадцать", "четырнадцать",
            "пятнадцать", "шестнадцать", "семнадцать", "восемнадцать", "девятнадцать",
            "двадцать"};
    String[] str10 = {"", "десять", "двадцать", "тридцать", "сорок", "пятьдесят",
            "шестьдесят", "семьдесят", "восемьдесят", "девяносто"};
    final String[][] forms = {
        {"", "", "", "0"},
        {"", "", "", "0"},
        {"тысяча", "тысячи", "тысяч", "1"},
        {"миллион", "миллиона", "миллионов", "0"},
        {"миллиард", "миллиарда", "миллиардов", "0"},
        {"триллион", "триллиона", "триллионов", "0"},
    };
    protected String getS1(int n, int gender) {
        return str1[gender][n];
    }
    protected String getS11(int n) {
        return str11[n];
    }
    protected String getS10(int n) {
        return str10[n];
    }
    protected String getS100(int n) {
        return str100[n];
    }
    protected int getUnitGender(int idx) {
        return new Integer(forms[idx][3]);
    }
    protected String getUnit(int idx, long cnt) {
        if (cnt % 100 > 4 && cnt % 100 < 21)
            return forms[idx][2];
        switch ((int)(cnt % 10)) {
            case 1:
                return forms[idx][0];
            case 2:
            case 3:
            case 4:
                return forms[idx][1];
            default:
                return forms[idx][2];
        }
    }
}

class MonetaryAmountInWordRuRUB extends MonetaryAmountInWordRu {
    {
        forms[0] = new String[]{"копейка", "копейки", "копеек", "1"};
        forms[1] = new String[]{"рубль", "рубля", "рублей", "0"};
    }
}

class MonetaryAmountInWordRuUSD extends MonetaryAmountInWordRu {
    {
        forms[0] = new String[]{"цент", "цента", "центов", "0"};
        forms[1] = new String[]{"доллар", "доллара", "долларов", "0"};
    }
}

class MonetaryAmountInWordRuEUR extends MonetaryAmountInWordRu {
    {
        forms[0] = new String[]{"цент", "цента", "центов", "0"};
        forms[1] = new String[]{"евро", "евро", "евро", "0"};
    }
}

class MonetaryAmountInWordRuCHF extends MonetaryAmountInWordRu {
    {
        forms[0] = new String[]{"сантим", "сантима", "сантимов", "0"};
        forms[1] = new String[]{"франк", "франка", "франков", "0"};
    }
}

// Реализация для английского языка (он наследуется от русского, но, 
// по-хорошему, надо бы от AbstractMonetaryAmountInWord):

//abstract class MonetaryAmountInWordEn extends MonetaryAmountInWord {
abstract class MonetaryAmountInWordEn extends MonetaryAmountInWordRu {
    final String[] s1 = {"zero", "one", "two", "three", "four", "five", "six", "seven",
            "eight", "nine"};
    {
        str11 = new String[]{"", "eleven", "twelve", "thirteen", "fourteen", "fifteen",
            "sixteen", "seventeen", "eighteen", "nineteen"};
        str10 = new String[]{"", "ten", "twenty", "thirty", "fourty", "fifty", "sixty",
            "seventy", "eighty", "ninety"};
        for (int i = 0; i < s1.length; i++) {
            str100[i] = s1[i] + " hundred";
        }
    }
    final String[] s4 = {"", "", "thousand", "million", "billion"};
    @Override
    protected String getS1(int n, int gender) {
        return s1[n];
    }
    @Override
    protected int getUnitGender(int idx) {
        return 0;
    }
    @Override
    protected String getUnit(int idx, long count) {
        return s4[idx] + (count != 1 ? "s" : "");
    }
}

class MonetaryAmountInWordEnRUB extends MonetaryAmountInWordEn {
    {
        s4[0] = "kop";
        s4[1] = "rouble";
    }
    @Override
    protected String getUnit(int idx, long count) {
        return s4[idx] + (idx != 0 && count != 1 ? "s" : "");
    }
}

class MonetaryAmountInWordEnUSD extends MonetaryAmountInWordEn {
    {
        s4[0] = "cent";
        s4[1] = "dollar";
    }
}

class MonetaryAmountInWordEnEUR extends MonetaryAmountInWordEn {
    {
        s4[0] = "cent";
        s4[1] = "euro";
    }
}

class MonetaryAmountInWordEnCHF extends MonetaryAmountInWordEn {
    {
        s4[0] = "centime";
        s4[1] = "franc";
    }
}