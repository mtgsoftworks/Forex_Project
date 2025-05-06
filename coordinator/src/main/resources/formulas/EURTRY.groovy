// Dynamic formula for EURTRY
// Variables available: pf1UsdBid, pf2UsdBid, pf1UsdAsk, pf2UsdAsk, pf1EurUsdBid, pf2EurUsdBid, pf1EurUsdAsk, pf2EurUsdAsk, timestamp

// Compute USDTRY mid price
 def usdTryMid = ((pf1UsdBid + pf2UsdBid) / 2 + (pf1UsdAsk + pf2UsdAsk) / 2) / 2
// Compute EURTRY
 def eurTryBid = usdTryMid * ((pf1EurUsdBid + pf2EurUsdBid) / 2)
 def eurTryAsk = usdTryMid * ((pf1EurUsdAsk + pf2EurUsdAsk) / 2)

return String.format("EURTRY|%.6f|%.6f|%s", eurTryBid, eurTryAsk, timestamp)
