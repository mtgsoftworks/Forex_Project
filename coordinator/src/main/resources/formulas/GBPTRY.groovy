// Dynamic formula for GBPTRY
// Variables available: pf1UsdBid, pf2UsdBid, pf1UsdAsk, pf2UsdAsk, pf1GbpUsdBid, pf2GbpUsdBid, pf1GbpUsdAsk, pf2GbpUsdAsk, timestamp

// Compute USDTRY mid price
 def usdTryMid = ((pf1UsdBid + pf2UsdBid) / 2 + (pf1UsdAsk + pf2UsdAsk) / 2) / 2
// Compute GBPTRY
 def gbpTryBid = usdTryMid * ((pf1GbpUsdBid + pf2GbpUsdBid) / 2)
 def gbpTryAsk = usdTryMid * ((pf1GbpUsdAsk + pf2GbpUsdAsk) / 2)

return String.format("GBPTRY|%.6f|%.6f|%s", gbpTryBid, gbpTryAsk, timestamp)
