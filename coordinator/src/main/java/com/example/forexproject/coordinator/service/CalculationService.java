package com.example.forexproject.coordinator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;
import com.example.forexproject.coordinator.service.FormulaService;

@Service
public class CalculationService {

    @Value("${calculation.scriptEngine:groovy}")
    private String scriptEngineName;

    @Autowired
    private FormulaService formulaService;

    /**
     * İki değer arasındaki fark %1'den küçükse, yeni değeri döndürür; aksi takdirde eski değeri korur.
     */
    /**
     * If difference between old and new value is <1% of old value, allow update; otherwise keep old.
     */
    public double checkTolerance(double newValue, double oldValue) {
        if (oldValue == 0) return newValue;
        double diffPct = Math.abs(newValue - oldValue) / Math.abs(oldValue);
        return diffPct < 0.01 ? newValue : oldValue;
    }

    /**
     * Statik hesaplama yöntemi ile USD/TRY mesajı hazırlar.
     */
    public String prepareUsdTryMessage(double pf1UsdBid, double pf2UsdBid, double pf1UsdAsk, double pf2UsdAsk, String timestamp) {
        double bid = (pf1UsdBid + pf2UsdBid) / 2;
        double ask = (pf1UsdAsk + pf2UsdAsk) / 2;
        return String.format("USDTRY|%.6f|%.6f|%s", bid, ask, timestamp);
    }

    /**
     * Statik hesaplama yöntemi ile EUR/TRY mesajı hazırlar.
     */
    public String prepareEurTryMessage(double pf1UsdBid, double pf2UsdBid, double pf1UsdAsk, double pf2UsdAsk,
                                       double pf1EurUsdBid, double pf2EurUsdBid, double pf1EurUsdAsk, double pf2EurUsdAsk,
                                       String timestamp) {
        double usdTryBid = (pf1UsdBid + pf2UsdBid) / 2;
        double usdTryAsk = (pf1UsdAsk + pf2UsdAsk) / 2;
        double usdMid = (usdTryBid + usdTryAsk) / 2;
        double eurUsdBidAvg = (pf1EurUsdBid + pf2EurUsdBid) / 2;
        double eurUsdAskAvg = (pf1EurUsdAsk + pf2EurUsdAsk) / 2;
        double eurTryBid = usdMid * eurUsdBidAvg;
        double eurTryAsk = usdMid * eurUsdAskAvg;
        return String.format("EURTRY|%.6f|%.6f|%s", eurTryBid, eurTryAsk, timestamp);
    }

    /**
     * Statik hesaplama yöntemi ile GBP/TRY mesajı hazırlar.
     */
    public String prepareGbpTryMessage(double pf1UsdBid, double pf2UsdBid, double pf1UsdAsk, double pf2UsdAsk,
                                       double pf1GbpUsdBid, double pf2GbpUsdBid, double pf1GbpUsdAsk, double pf2GbpUsdAsk,
                                       String timestamp) {
        double usdTryBid = (pf1UsdBid + pf2UsdBid) / 2;
        double usdTryAsk = (pf1UsdAsk + pf2UsdAsk) / 2;
        double usdMid = (usdTryBid + usdTryAsk) / 2;
        double gbpUsdBidAvg = (pf1GbpUsdBid + pf2GbpUsdBid) / 2;
        double gbpUsdAskAvg = (pf1GbpUsdAsk + pf2GbpUsdAsk) / 2;
        double gbpTryBid = usdMid * gbpUsdBidAvg;
        double gbpTryAsk = usdMid * gbpUsdAskAvg;
        return String.format("GBPTRY|%.6f|%.6f|%s", gbpTryBid, gbpTryAsk, timestamp);
    }
    
    /**
     * Compute dynamic calculation using a named script loaded by FormulaService.
     * @param formulaName name of the formula script (filename without extension)
     * @param variables map of variables to bind into script
     * @return script evaluation result as String
     * @throws ScriptException if script execution fails
     */
    public String computeDynamicCalculation(String formulaName, Map<String, Object> variables) throws ScriptException {
        String script = formulaService.getFormula(formulaName);
        if (script == null) throw new IllegalArgumentException("Formula not found: " + formulaName);
        ScriptEngine engine = new ScriptEngineManager().getEngineByName(scriptEngineName);
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            engine.put(entry.getKey(), entry.getValue());
        }
        Object result = engine.eval(script);
        return result.toString();
    }
}
