// Dynamic formula for USDTRY
// Variables available: pf1UsdBid, pf2UsdBid, pf1UsdAsk, pf2UsdAsk, timestamp

def bid = (pf1UsdBid + pf2UsdBid) / 2 

def ask = (pf1UsdAsk + pf2UsdAsk) / 2 

return String.format("USDTRY|%.6f|%.6f|%s", bid, ask, timestamp)
