/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jinr.sed;

/**
 *
 * @author  Яковлев А.В.
 * Денежная сумма прописью
 * Надстройка над абстрактными классами из MonetaryAmountInWord
 * Все сведено в один класс и одну функцию
 * 
 */
public class MonetaryAmount {
    
    public enum monCurrency { RUB, USD, EUR, CHF}; // валюта
    public enum monLanguage { Ru, En}; // язык
    public enum monCentFlag { Dig, Let}; // копейки числом или прописью
    
    private String classOverload(Class cls, monCentFlag monCentFlag, Number sum) {
        
        String res = "";
        MonetaryAmountInWord mnw;
        Number centFlag = 0;
        
        if (monCentFlag.equals(monCentFlag.Dig))
            centFlag = 0;
        if (monCentFlag.equals(monCentFlag.Let))
            centFlag = 1;
        
        try 
            {
                mnw = (MonetaryAmountInWord)cls.newInstance();
                res = mnw.numToString(centFlag, sum);
            } 
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return res;
    }
    
    public String sumToString(monLanguage monLang, monCurrency monCur, 
            monCentFlag monCentFlag, Number sum){
        
        String res = "";
        
        if (monCur.equals(monCurrency.RUB))
            {
                if (monLang.equals(monLanguage.Ru))
                {
                    res = classOverload(MonetaryAmountInWordRuRUB.class, monCentFlag, sum);
                }
                if (monLang.equals(monLanguage.En))
                {
                    res = classOverload(MonetaryAmountInWordEnRUB.class, monCentFlag, sum);
                }
            }
        if (monCur.equals(monCurrency.USD))
            {
                if (monLang.equals(monLanguage.Ru))
                {
                    res = classOverload(MonetaryAmountInWordRuUSD.class, monCentFlag, sum);
                }
                if (monLang.equals(monLanguage.En))
                {
                    res = classOverload(MonetaryAmountInWordEnUSD.class, monCentFlag, sum);
                }
            }
        if (monCur.equals(monCurrency.EUR))
            {
                if (monLang.equals(monLanguage.Ru))
                {
                    res = classOverload(MonetaryAmountInWordRuEUR.class, monCentFlag, sum);
                }
                if (monLang.equals(monLanguage.En))
                {
                    res = classOverload(MonetaryAmountInWordEnEUR.class, monCentFlag, sum);
                }
            }
        if (monCur.equals(monCurrency.CHF))
            {
                if (monLang.equals(monLanguage.Ru))
                {
                    res = classOverload(MonetaryAmountInWordRuCHF.class, monCentFlag, sum);
                }
                if (monLang.equals(monLanguage.En))
                {
                    res = classOverload(MonetaryAmountInWordEnCHF.class, monCentFlag, sum);
                }
            }
        return res;
        
    }

}

