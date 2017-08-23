package com.ocean927.f
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * This is calculated in the trade currency. If trades are done in 
 * different currencies, you need to explicitly include currency and 
 * normalize final answer to USD.
 */

public class TradePNLStatistics {
	
	public Trade 	trade;				// trade
	
	public int 		tradesProcessed;	// count of trades
	public Instant 	rangeFrom;			// first trade time
	public Instant	rangeTo;			// last trade time
	
	public double 	avgPx;				// avg across sides
	public double 	avgBotPrice;		// avg bot price
	public double 	avgSoldPrice;		// avg sold price
	
	public int 		sumSharesBot;
	public int 		sumSharesSold;				
	public int 		position;				// remaining open position
	
	public double 	realizedPNL;		// P&L earned so far. when position is zero, this is gross P&L
										// this covers all netted trades so far, not only this trade.
	
	public double 	unRealizedPNL;		// P&L remaining in the open position. when position is zero, this is zero.
										// keyword it describes remaining open position, not only this trade.
	
	public double 	grossPNL;			// final P&L, sum of realized + unrealized
	public double 	grossPNLCHECK;		// gross P&L check
	
	public double 	grossMoneySpent; 	// gross money spent (bought shares)
	public double 	grossMoneyEarned; 	// gross money earned (sold shares)
	public double 	grossFunds; 		// gross money turned around
	
	public double 	marketPx;  			// market price at now 
	public double 	mktValue;  			// if open position, market value of net position
	
	@Override
	public String toString() {
		return "PNLStatsHolding [" + (trade != null ? "trade=" + trade + ", " : "") + "tradesProcessed="
				+ tradesProcessed + ", " + (rangeFrom != null ? "rangeFrom=" + rangeFrom + ", " : "")
				+ (rangeTo != null ? "rangeTo=" + rangeTo + ", " : "") + "avgPx=" + avgPx + ", avgBotPrice="
				+ avgBotPrice + ", avgSoldPrice=" + avgSoldPrice + ", sumSharesBot=" + sumSharesBot + ", sumSharesSold="
				+ sumSharesSold + ", position=" + position + ", realizedPNL=" + realizedPNL + ", unRealizedPNL="
				+ unRealizedPNL + ", grossPNL=" + grossPNL + ", grossPNLCHECK=" + grossPNLCHECK + ", grossMoneySpent="
				+ grossMoneySpent + ", grossMoneyEarned=" + grossMoneyEarned + ", grossFunds=" + grossFunds
				+ ", marketPx=" + marketPx + ", mktValue=" + mktValue + "]";
	}
	
	public static List<String> printHeader() {
		List<String> list=new ArrayList<>();
		// HEADER
		StringBuilder sb=new StringBuilder();
		sb.append(padInBack("TRADE", 6));
		sb.append(padInBack("DATE", 12));
		sb.append(padInBack("TIME", 14));
		sb.append(padInBack("SYMBOL", 8));
		sb.append(padInBack("ACCOUNT", 8));		
		sb.append(padInBack("SIDE", 6));
		sb.append(padInBack("SHARE", 6));		
		sb.append(padInBack("PRICE", 8));
		sb.append(padInBack("POS", 8));
		sb.append(padInBack("AVG_PX", 8));
		sb.append(padInBack("MKT_PX", 10));		
		sb.append(padInBack("REALIZED", 12));		
		sb.append(padInBack("UNREALZD", 16));
		sb.append(padInBack("MKT_VALUE", 16));
		sb.append(padInBack("FUNDS", 16));
		sb.append(padInBack("GROSSPNL", 16));		
		sb.append(padInBack("SEP", 5));
		sb.append(padInBack("GROSS(CHK)", 16));		
		list.add(sb.toString());
		return list;	
	}
	
	public static List<String> printTrades(List<TradePNLStatistics> listStats) {
		List<String> list=new ArrayList<>();
		StringBuilder sb=new StringBuilder(); 

		// DATA		
		for (int i = 0; i < listStats.size(); i++) {
			sb.setLength(0);
			TradePNLStatistics s=listStats.get(i);
			
			List<String> listS=printIndividualTrade(i+1, s);
			
			list.addAll(listS);
		}
		return list;
	}
	
	public static List<String> printIndividualTrade(int index, TradePNLStatistics stat) {
		List<String> list=new ArrayList<>();
		StringBuilder sb=new StringBuilder(); 

		// DATA		
			sb.setLength(0);
			//
			sb.append(padInBack(index, 6));
			sb.append(padInBack(UtilsDateTime.toLocalDate(stat.trade.instant).toString(), 12));
			sb.append(padInBack(UtilsDateTime.toLocalTime(stat.trade.instant).toString(), 14));
			sb.append(padInBack(stat.trade.instrument.getTradingSymbol(), 8));
			sb.append(padInBack(stat.trade.account, 8));
			sb.append(padInBack(stat.trade.side.getValue(), 6));
			sb.append(padInBack(""+stat.trade.shares, 6));		
			sb.append(padInBack(CurrencyFormatter.format(stat.trade.price), 8));
			sb.append(padInBack(""+stat.position, 8));
			sb.append(padInBack(CurrencyFormatter.format(stat.avgPx), 8));
			sb.append(padInBack(CurrencyFormatter.format(stat.marketPx), 10));			
			sb.append(padInBack(CurrencyFormatter.format(stat.realizedPNL), 12));
			sb.append(padInBack(CurrencyFormatter.format(stat.unRealizedPNL), 16));
			sb.append(padInBack(CurrencyFormatter.format(stat.mktValue), 16));
			sb.append(padInBack(CurrencyFormatter.format(stat.grossFunds), 16));
			sb.append(padInBack(CurrencyFormatter.format(stat.grossPNL), 16));
			sb.append(padInBack(" | ", 5));			
			sb.append(padInBack(CurrencyFormatter.format(stat.grossPNLCHECK), 16));			
			//
			list.add(sb.toString());
		return list;
	}	
}