
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rosuda.REngine.REngineException;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;


// TODO: class is heavy. Refactor into 
// smaller pieces. It should become a separate library independent on back-testing
public class PrinterStrategyResults {
	
	private final Logger logger = LogManager.getLogger(PrinterStrategyResults.class);
	
	/** CREATE REPORT */
	public void createReport(
						     ShortStrategyRunSummary results,
							 // plugin details
							 UUID pluginID,	
							 String pluginName,
							 
							 // back-test setup
							 StrategyParameters strategyParameters,
							 LocalDate start, 
							 LocalDate end,
							 int tradingDays, 
							 String reportDir,
							 
							 // actual run details
							 List<IHolding> listPositions,
							 TreeMap<LocalDate,PortfolioSnapshot> portfolioDailySnapshots,
							 OMSTracker omsTracker,
							 
							 // market data service
							 IAnalytics analyticsMgr) {
		try {			
			// CREATE REPORT
			//String reportfile=reportDir + File.separatorChar + "text" + File.separatorChar + 
			//"PNL"+ "_" + UtilsDateTime.format(Instant.now()).replaceAll(":", ".").replaceAll("-", "_")  + "_" + pluginID + "_" + start.toString().replaceAll("-","") + "_" + end.toString().replaceAll("-","") + ".txt";				
			
			String reportfile=reportDir + File.separatorChar + "text" + File.separatorChar + 
					"PNL"+ "_" + UtilsDateTime.format(Instant.now()).replaceAll(":", ".").replaceAll("-", "_") + ".txt";
			PrintWriter out=new PrintWriter(new BufferedOutputStream(new FileOutputStream(reportfile))); 
			try {
									
			// GENERAL
			printGeneralHeader(out, results, portfolioDailySnapshots, pluginName, start, end, strategyParameters, tradingDays, analyticsMgr );
			
			// OMS
			printOMSStats(out, omsTracker);
				
			// PROCESS TRADES
			Map<Inst,List<TradePNLStatistics>> mapResults=processPosition( out, listPositions, analyticsMgr );
			
			// PRINT SUMMARY PER HOLDING PNL
			printSummaryForEachHolding(out, mapResults);			

			// PORTFOLIO RETURNS
			printPortfolioSnapshotsReturnsMatrix(out, results, portfolioDailySnapshots, analyticsMgr );
						
			// PROCESS STRATEGY DRAWDOWN				
			processDrawDown(out, portfolioDailySnapshots, analyticsMgr,start,end);
			
			// PORTFOLIO SUMMARY
			printPortfolioSnapshots(out, portfolioDailySnapshots, analyticsMgr );
							
			// PRINT INDIVIDUAL TRADE PNL..long list
			printTradePNL(out, mapResults);
			
			}finally{
				if( out!=null ) { out.flush(); out.close(); }
			}
			
			//
			//String reportfilePDF=reportDir + File.separatorChar + "pdf" + File.separatorChar +
			//"PNL"+ "_" + UtilsDateTime.format(Instant.now()).replaceAll(":", ".").replaceAll("-", "_")  + "_" + plugin.getPlugin().getPluginUUID() + "_" + start.toString().replaceAll("-","") + "_" + end.toString().replaceAll("-","") + ".pdf";			
			//PdfCreator.convertTextToPDF(reportfile, reportfilePDF);			
		}catch(Exception e) {
			logger.error(e.getLocalizedMessage(), e);	
		}
	}
	
	public void printGeneralHeader(PrintWriter out, ShortStrategyRunSummary results, TreeMap<LocalDate,PortfolioSnapshot> portfolioDailySnapshots, String pluginName, LocalDate start, LocalDate end, StrategyParameters strategyParameters, int tradingDays, IAnalytics analyticsMgr) throws KException, IOException, ExecutionException {
		List<String> list=new ArrayList<>();
		StringBuilder sb=new StringBuilder();
		list.add("----------------------------------");
		list.add("REPORT: BACK-TEST PERFORMANCE.");
		list.add("----------------------------------");		
		list.add("DATE/TIME: " + UtilsDateTime.format(Instant.now()) );
		list.add("PLUGIN NAME: " + pluginName );		
		list.add("----------------------------------");
		list.add("$AUM ON START : " + CurrencyFormatter.format(strategyParameters.getParamBigDecimal("cash").doubleValue()) );
		list.add("$AUM AT FINISH: " + CurrencyFormatter.format(getFinalPNL(portfolioDailySnapshots, analyticsMgr).doubleValue()) );
		list.add("TRADING DAYS: " + tradingDays + " IN " + "[" + start + ", " + end + "]" );
		// TODO: this must be parameterized
		list.add("UPPER BAND: " + strategyParameters.getParamDouble("upper_band_multiplier") );
		list.add("LOWER BAND: " + strategyParameters.getParamDouble("lower_band_multiplier") );
		list.add("MVA WINDOW: " + strategyParameters.getParamInt("mva_window") );
		
		results.finalPNL=getFinalPNL(portfolioDailySnapshots, analyticsMgr);
		
		sb.setLength(0);
		for (String s: list) {
			sb.append(s); sb.append("\n");
		}
		out.write(sb.toString()+"\n");		
		logger.info(sb.toString()+"\n");
	}
	
	public void printOMSStats(PrintWriter out, OMSTracker omsTracker) {
		
		List<String> list=new ArrayList<>();
		StringBuilder sb=new StringBuilder();
		list.add("----------------------------------");
		list.add("ORDER MANAGEMENT MESSAGING STATISTICS");
		list.add("----------------------------------");
		list.addAll( omsTracker.printShortSummary() );
		list.addAll( omsTracker.printSummaryPerInstrument() );
		
		//
		sb.setLength(0);
		for (String s: list) {
			sb.append(s); sb.append("\n");
		}
		out.write(sb.toString()+"\n");		
		logger.info(sb.toString()+"\n");
	}
	
	public void printPortfolioSnapshots(PrintWriter out, TreeMap<LocalDate,PortfolioSnapshot> portfolioDailySnapshots, IAnalytics analyticsMgr) throws KException, IOException, ExecutionException {
		
		List<String> list=new ArrayList<>();
		StringBuilder sb=new StringBuilder();
		list.add("----------------------------------");
		list.add("");
		list.add("----------------------------------");
		
		// HEADER
		sb.setLength(0);
		sb.append(FrameworkUtils.padInBack("DATE", 12));
		sb.append(FrameworkUtils.padInBack("TRADES", 10));
		sb.append(FrameworkUtils.padInBack("CASH", 18));
		sb.append(FrameworkUtils.padInBack("MKTVALUE", 18));
		sb.append(FrameworkUtils.padInBack("PORTFOLIO", 18));
		sb.append(FrameworkUtils.padInBack("DAILY_RTN%", 18));
		list.add(sb.toString());
		sb.setLength(0);
		
		List<PortfolioStat> dailyReturns=PrinterUtil.getDailyPNL(portfolioDailySnapshots, analyticsMgr);
		// update header line
		list.set(1, "PORTFOLIO DAILY (" + dailyReturns.size() + ") PROFIT/LOSS REPORT TAKEN AT EOD");  
		
		for(PortfolioStat stat: dailyReturns ) {
			//
			sb.setLength(0);
			sb.append(FrameworkUtils.padInBack(stat.date.toString(), 12));
			sb.append(FrameworkUtils.padInBack(stat.countTrades, 10));
			sb.append(FrameworkUtils.padInBack(CurrencyFormatter.format(stat.portfolioCash.doubleValue()), 18));
			sb.append(FrameworkUtils.padInBack(CurrencyFormatter.format(stat.mktValue.doubleValue()), 18));
			sb.append(FrameworkUtils.padInBack(CurrencyFormatter.format(stat.gross.doubleValue()), 18));
			
			// make output prettier
			double rt=stat.dailyReturn.multiply(BigDecimal.valueOf(100)).doubleValue();
			if( Math.abs(rt)>=0.01d ) {
				sb.append(FrameworkUtils.padInBack(DoubleFormatting.round(rt,2), 18));				
			}else{
				if( rt!=0 ) {
					sb.append(FrameworkUtils.padInBack(DoubleFormatting.round(rt,5), 18));
				}else{
					sb.append(FrameworkUtils.padInBack("0", 18));
				}
			}
			list.add(sb.toString());
		}
		
		//
		sb.setLength(0);
		for (String s: list) {
			sb.append(s); sb.append("\n");
		}
		out.write(sb.toString()+"\n");		
		logger.info(sb.toString()+"\n");
	}

	public void printPortfolioSnapshotsReturnsMatrix(PrintWriter out, ShortStrategyRunSummary results, TreeMap<LocalDate,PortfolioSnapshot> portfolioDailySnapshots, IAnalytics analyticsMgr) throws KException, IOException, ExecutionException, REngineException {
		
		List<String> listOut=new ArrayList<>();
		StringBuilder sb=new StringBuilder();
		listOut.add("----------------------------------");
		listOut.add("PORTFOLIO RETURNS MATRIX");
		listOut.add("----------------------------------");
		
		List<PortfolioStat> dailyPNL=PrinterUtil.getDailyPNL(portfolioDailySnapshots, analyticsMgr);
		
		// add header
		//listOut.add("------------------------------------------------------------------------------------------------------");
		sb.setLength(0);			
		sb.append(FrameworkUtils.padInBack("YEAR%", 6));
		for(int i=0;i<Month.values().length;i++) { 
			sb.append(FrameworkUtils.padInBack( 
					Month.values()[i].toString().substring(0, 3), 7));
		}
		sb.append(FrameworkUtils.padInBack("ANNUAL", 10));
		sb.append(FrameworkUtils.padInBack("STDEV", 10));
		listOut.add(sb.toString());
		listOut.add("---------------------------------------------------------------------------------------------------------");

		// 1st method of calculation
		List<LocalDate> l1 = new ArrayList<>();
		List<Double> l2 = new ArrayList<>();
		for (PortfolioStat p:dailyPNL) {				
			l1.add(p.date);l2.add(p.dailyReturn.doubleValue());			
		}
		
		// calculate returns 
		List<Tuple2<LocalDate,Double>> annualReturns=ReturnsSeries.calcAnnualReturns(l1,l2);
		List<Tuple2<LocalDate,Double>> monthlyReturns=ReturnsSeries.calcMonthlyReturns(l1,l2);
		Map<LocalDate,Double> mapMonthlyReturns=new HashMap<>();
		monthlyReturns.stream().forEach((Tuple2<LocalDate,Double> k)->mapMonthlyReturns.put(k.x,k.y) );
		
		// test conversion
		List<String> tmp = new ArrayList<>();
		mapMonthlyReturns.keySet().stream().forEach((e)->tmp.add(e.toString()));
		//String x[]=tmp.toArray(new String[mapMonthlyReturns.keySet().size()]);
		//double y[]=mapMonthlyReturns.values().stream().mapToDouble(Double::doubleValue).toArray();
		//analyticsMgr.getr().assign("x", x);
		//analyticsMgr.getr().assign("y", y);
		//analyticsMgr.getr().eval("plot(y)");
		
		// data
		List<Double> listAnnualReturns = new ArrayList<>();
		BigDecimal totalReturn=BigDecimal.ONE;
		for (Tuple2<LocalDate,Double> annual: annualReturns) {
			totalReturn=totalReturn.multiply(
						BigDecimal.valueOf(annual.y).add(BigDecimal.ONE));
			sb.setLength(0);			
			sb.append(FrameworkUtils.padInBack(annual.x.getYear(), 6));
			List<Double> listMonhtlyReturns = new ArrayList<>();
			for(int month=1;month<=Month.values().length;month++) {
				LocalDate key=null;
				// find last day of month where portfolio stat was reported
				key=LocalDate.of(annual.x.getYear(), month, 1).with( lastDayOfMonth() );
				LocalDate stopMonth=key.minusMonths(1);
				while( key.isAfter(stopMonth) ) {					
					if( mapMonthlyReturns.containsKey(key)) { break; }
					key=key.minusDays(1);
				}				
				//
				if( mapMonthlyReturns.containsKey(key)) {
					double monthlyReturnDbl=mapMonthlyReturns.get(key);
					
					// add up	
					listMonhtlyReturns.add(monthlyReturnDbl);
					
					// output
					sb.append(FrameworkUtils.padInBack( 
							DoubleFormatting.round(monthlyReturnDbl*100,2), 7));
				}else{
					// strategy may not have run in this month
					sb.append(FrameworkUtils.padInBack("-", 7));
				}
			}
			
			// add up			
			listAnnualReturns.add(annual.y);
			
			double annualStdev=(new StandardDeviation(true)).evaluate(Doubles.toArray(listMonhtlyReturns));
			
			// output
			sb.append(FrameworkUtils.padInBack( 
							DoubleFormatting.round(annual.y*100,4), 10));			
			
			sb.append(FrameworkUtils.padInBack( 
					DoubleFormatting.round(annualStdev*100,4), 10));
			
			listOut.add(sb.toString());
		}
		totalReturn=totalReturn.subtract(BigDecimal.ONE);
		double totalStdev=(new StandardDeviation(true)).evaluate(Doubles.toArray(listAnnualReturns));		
		
		results.totalReturn=totalReturn.add(BigDecimal.ONE);
		results.totalStdev=totalStdev;
		
		listOut.add("TOTAL RETURN: " + DoubleFormatting.round((1+totalReturn.doubleValue())*100,4) + " %" + 		
						 ", STDEV: " + DoubleFormatting.round(totalStdev*100,4) + " %" );
						
		//
		sb.setLength(0);
		for (String s: listOut) {
			sb.append(s); sb.append("\n");
		}
		out.write(sb.toString()+"\n");		
		logger.info(sb.toString()+"\n");
	}
	
	public BigDecimal getFinalPNL(TreeMap<LocalDate,PortfolioSnapshot> mapSnapshots, IAnalytics analyticsMgr) throws KException, IOException, ExecutionException {
		if( mapSnapshots.size()>0 ) {
			LocalDate lastDay=(LocalDate) mapSnapshots.lastKey();
		    PortfolioSnapshot lastSnap=mapSnapshots.get(lastDay);
		    
		    BigDecimal sumMktValue=BigDecimal.ZERO;
			for (Entry<Inst,Integer> e2: lastSnap.mapPosition.entrySet()) {
				Inst instrument=e2.getKey();
				int position=e2.getValue();
				//
				BigDecimal px=new BigDecimal(analyticsMgr.getTradeBar(lastDay, instrument).getAdjClose());
				BigDecimal posBig=BigDecimal.valueOf(position);
				sumMktValue=sumMktValue.add(posBig.multiply(px)); // long position has positive market value, short has negative;				
			}
			
			// gross: cash + market value of open position for this day.
			BigDecimal sumGross=lastSnap.portfolioCash.add(sumMktValue);
			return sumGross;
		}
		return BigDecimal.ZERO;
	}
	
	public void printSummaryForEachHolding(PrintWriter out, Map<Inst,List<TradePNLStatistics>> mapResults) {
		List<String> list=new ArrayList<>();
		StringBuilder sb=new StringBuilder();
		
		list.add("----------------------------------");
		list.add("SYMBOL PROFIT/LOSS SUMMARY");
		list.add("HOLDING=SYMBOL+ACCOUNT+CURRENCY...");
		list.add("----------------------------------");
				
		// HEADER
		sb.setLength(0);
		sb.append(FrameworkUtils.padInBack("SYMBOL", 8));
		sb.append(FrameworkUtils.padInBack("TRADES", 12));
		sb.append(FrameworkUtils.padInBack("POSITION", 10));
		sb.append(FrameworkUtils.padInBack("MKT_PX", 10));
		sb.append(FrameworkUtils.padInBack("RZLD_PNL", 18));
		sb.append(FrameworkUtils.padInBack("UNRLZD_PNL", 18));
		sb.append(FrameworkUtils.padInBack("SYMBOL_PNL", 18));
		list.add(sb.toString());
		sb.setLength(0);
		
		List<Inst> sortedKey=new ArrayList<>(mapResults.keySet());
		Collections.sort(sortedKey);
		
		int sumTrades=0;
		double sumRlz=0, sumUnrlz=0, sumGross=0;
		for (Inst instrument: sortedKey) {
			
			List<TradePNLStatistics> listTradeStats=mapResults.get(instrument);
			
			sb.append(FrameworkUtils.padInBack(instrument.toString(), 8));
			
			if( listTradeStats!=null && listTradeStats.size()>0 ) {
				
				sb.append(FrameworkUtils.padInBack("" + listTradeStats.size(), 12));				
				
				sumTrades+=listTradeStats.size();				
				
				TradePNLStatistics last=listTradeStats.get(listTradeStats.size()-1);
				sb.append(FrameworkUtils.padInBack(last.position, 10));
				sb.append(FrameworkUtils.padInBack(CurrencyFormatter.format(last.marketPx), 10));
				sb.append(FrameworkUtils.padInBack(CurrencyFormatter.format(last.realizedPNL),18));
				sb.append(FrameworkUtils.padInBack(CurrencyFormatter.format(last.unRealizedPNL),18));
				sb.append(FrameworkUtils.padInBack(CurrencyFormatter.format(last.grossPNL),18));
				
				sumRlz+=last.realizedPNL;
				sumUnrlz+=last.unRealizedPNL;
				sumGross+=last.grossPNL;
				
															
			}else{
				sb.append(FrameworkUtils.padInBack("0", 12));
				sb.append(FrameworkUtils.padInBack("0", 10));
				sb.append(FrameworkUtils.padInBack("0", 10));
				sb.append(FrameworkUtils.padInBack("0", 18));
				sb.append(FrameworkUtils.padInBack("0", 18));
				sb.append(FrameworkUtils.padInBack("0", 18));
			}	
			list.add(sb.toString());
			sb.setLength(0);
		}
		
		// add summary line
		list.add("----------------------------------");
		sb.setLength(0);		
		sb.append(FrameworkUtils.padInBack("TOTAL", 8));
		sb.append(FrameworkUtils.padInBack(sumTrades, 12));
		sb.append(FrameworkUtils.padInBack("-", 10));
		sb.append(FrameworkUtils.padInBack("-", 10));
		sb.append(FrameworkUtils.padInBack(CurrencyFormatter.format(sumRlz),18));
		sb.append(FrameworkUtils.padInBack(CurrencyFormatter.format(sumUnrlz),18));
		sb.append(FrameworkUtils.padInBack(CurrencyFormatter.format(sumGross),18));
		list.add(sb.toString());
		
		sb.setLength(0);
		for (String s: list) {
			sb.append(s); sb.append("\n");		
		}		
		out.write(sb.toString()+"\n");		
		logger.info(sb.toString()+"\n");
	}
	
	/**
	 */
	public void processDrawDown(PrintWriter out, TreeMap<LocalDate,PortfolioSnapshot> portfolioDailySnapshots, IAnalytics analyticsMgr, LocalDate start, LocalDate end) throws KException, IOException, ExecutionException{				
		List<String> list=new ArrayList<>();
		StringBuilder sb=new StringBuilder();
		
		List<PortfolioStat> dailyPortfolioStats=PrinterUtil.getDailyPNL(portfolioDailySnapshots, analyticsMgr);
		
		if( dailyPortfolioStats.size()>0 ) {
			list.add("----------------------------------");
			list.add("PEAK PROFIT/LOSS AND DRAW DOWN PERIODS");
			list.add("----------------------------------");

			// MIN/MAX GLOBAL
			double minGross=0, maxGross=0;
			double data[]=new double[dailyPortfolioStats.size()];
			for (int i=0;i<dailyPortfolioStats.size();i++) {
				double value=dailyPortfolioStats.get(i).gross.doubleValue();
				data[i]=value;
				if( i==0 ) { minGross=value; maxGross=value; }
				else{
					if( value<minGross){ minGross=value; }
					if( value>maxGross){ maxGross=value; }
				}
			}				
			
			list.add("MIN P&L: " + CurrencyFormatter.format(minGross));
			list.add("MAX P&L: " + CurrencyFormatter.format(maxGross));
			
			// DRAWDOWN
			SeriesDrawDown pdd=new SeriesDrawDown();
			List<MinMaxPair> listDrawdowns=pdd.findDrawdowns(data);
			
			sb.setLength(0);
			sb.append(FrameworkUtils.padInBack("TYPE", 12));
			sb.append(FrameworkUtils.padInBack("BEGIN", 14));
			sb.append(FrameworkUtils.padInBack("END", 14));
			sb.append(FrameworkUtils.padInBack("DAYS", 12));
			sb.append(FrameworkUtils.padInBack("TOP", 14));
			sb.append(FrameworkUtils.padInBack("BOTTOM", 14));
			sb.append(FrameworkUtils.padInBack("DRAWDOWN", 14));			
			list.add(sb.toString());
			
			// sort them 
			Collections.sort(listDrawdowns);
			listDrawdowns = Lists.reverse(listDrawdowns);
			
			int cnt=0;
			for(MinMaxPair d: listDrawdowns) {
				cnt++;
				if( d.isFound() ) {
					
					sb.setLength(0);
					sb.append(FrameworkUtils.padInBack("TOP-" + cnt, 12));
					sb.append(FrameworkUtils.padInBack(dailyPortfolioStats.get(d.getStartIndex()).date.toString(), 14));
					sb.append(FrameworkUtils.padInBack(dailyPortfolioStats.get(d.getEndIndex()).date.toString(), 14));
					sb.append(FrameworkUtils.padInBack(d.length(), 12));
					sb.append(FrameworkUtils.padInBack(CurrencyFormatter.format(d.getStart()), 14));
					sb.append(FrameworkUtils.padInBack(CurrencyFormatter.format(d.getEnd()), 14));
					sb.append(FrameworkUtils.padInBack(CurrencyFormatter.format(d.getDrawdown()), 14));			
					list.add(sb.toString());					
				}
				if( cnt>=5 ) { break; }
			}
			
			// output
			sb.setLength(0);
			for (String s: list) {
				sb.append(s); sb.append("\n");		
			}
			out.write(sb.toString()+"\n");		
			logger.info(sb.toString()+"\n");
		}
	}
	
	
	public void printTradePNL(PrintWriter out, Map<Inst,List<TradePNLStatistics>> mapResults) {
		List<String> list=new ArrayList<>();
		StringBuilder sb=new StringBuilder();
		
		list.add("----------------------------------");
		list.add("TRADES PROFIT/LOSS TALLY");
		list.add("----------------------------------");
			
		// HEADER
		for (String s:TradePNLStatistics.printHeader() ) { 
			list.add(s);
		}			
		
		List<Inst> sortedKeyInstr=new ArrayList<>(mapResults.keySet());
		Collections.sort(sortedKeyInstr);
				
		Set<LocalDate> setDates=new HashSet<>();
		for (Inst instrument: sortedKeyInstr) {			
			List<TradePNLStatistics> listTradeStats=mapResults.get(instrument);
			for (TradePNLStatistics s: listTradeStats) {
				setDates.add( UtilsDateTime.toLocalDate(s.trade.instant) );
			}
		}
		List<LocalDate> sortedKeyDates=new ArrayList<>(setDates);
		Collections.sort(sortedKeyDates);
		
		// ITERATE
		boolean BY_SYMBOL=false;
		
		if( BY_SYMBOL ) {
			// BY SYMBOL
			for (Inst instrument: sortedKeyInstr) {
				
				List<TradePNLStatistics> listTradeStats=mapResults.get(instrument);
				//logger.info("print(), sy=" + instrument + ", found trades:=" + listTradeStats.size());
				
				// TRADES
				if( listTradeStats!=null && listTradeStats.size()>0 ) {
					 List<String> results=TradePNLStatistics.printTrades(listTradeStats);
					 for(String s: results) {
						 list.add(s);	 
					 }
				}	
			}
		}else{
			// BY DATE
			int cnt=0;
			for (LocalDate date: sortedKeyDates) {
				for (Inst instrument: sortedKeyInstr) {			
					List<TradePNLStatistics> listTradeStats=mapResults.get(instrument);
					for (TradePNLStatistics s: listTradeStats) {
						LocalDate tradeDate=UtilsDateTime.toLocalDate(s.trade.instant);
						if( tradeDate.equals(date)) {
							// take it
							if( listTradeStats!=null && listTradeStats.size()>0 ) {
								 cnt++;	
								 List<String> results=TradePNLStatistics.
										 	printIndividualTrade(cnt, s);
								 list.addAll(results);
							}	
						}
					}
				}
			}
		}
		
		
		
		sb.setLength(0);
		for (String s: list) {
			sb.append(s); sb.append("\n");		
		}
		out.write(sb.toString()+"\n");		
		//logger.info(sb.toString()+"\n");
	}
	
	/** */
	public Map<Inst,List<TradePNLStatistics>> processPosition(PrintWriter out, List<IHolding> listPositions, IAnalytics analyticsMgr) {
		
		// ANALYZE
		Map<Inst,List<TradePNLStatistics>> map=analyticsMgr.calculatePNL(listPositions);		
		
		return map;
	}
}
