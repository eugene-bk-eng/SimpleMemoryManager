import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestPortfolioCalculator {
		
	private final static Logger logger = LogManager.getLogger(PortfolioCalculator.class);
	
	/**
	 * 
	 * 
	 * @param date
	 * @param snap
	 * @param mapPrice
	 */
	public static TestPortfolioStat calculateStatsOnPortfolio(PortfolioSnapshot snap, Map<Inst,Double> mapPrice) throws KException, IOException {
		
		TestPortfolioStat stat=new TestPortfolioStat();

			// add up all holdings for this date
			BigDecimal sumMktValue=BigDecimal.ZERO, 
					   sumGross=BigDecimal.ZERO;
			
			// iterate over instruments in snapshot
			for (Entry<Inst,Integer> entry: snap.mapPosition.entrySet()) {
				Inst instrument=entry.getKey();
				int position=entry.getValue();					
				
				if( mapPrice.containsKey(instrument) ) {						
					// long position has positive market value, short has negative;
					sumMktValue=sumMktValue.add(
								BigDecimal.valueOf(position).
									multiply(new BigDecimal(mapPrice.get(instrument))));
				}else{
					logger.warn("No market data on " + snap.startDate + ", " + instrument);
				}
			}
			
			// gross: cash + market value of open position for this day.
			sumGross=sumGross.add(snap.portfolioCash).add(sumMktValue);			
			
			stat.date=snap.startDate;
			stat.countTrades=snap.cntTrades;
			stat.portfolioCash=snap.portfolioCash;
			stat.mktValue=sumMktValue;
			stat.gross=sumGross;

		return stat;
	}
	
	/**
	 * Calculate return and update nexr 
	 */
	public static void calculateReturn(TestPortfolioStat dp, TestPortfolioStat next) {
				
		BigDecimal grossLast=dp.gross;
		BigDecimal gross=next.gross;
		
		if( grossLast.compareTo(BigDecimal.ZERO)!=0 ) {
			next.dailyReturn=gross.subtract(grossLast).abs().
					divide(grossLast.abs(), RoundingMode.HALF_DOWN );
			// set correct sign
			if( gross.compareTo(grossLast)<0 ) {
				next.dailyReturn=next.dailyReturn.negate();
			}						
		}
	}

	public static List<Inst> getInstruments(PortfolioSnapshot snap) {
		List<Inst> list = new ArrayList<>();
			snap.mapPosition.entrySet().
				stream().
					forEach(entry->list.add(entry.getKey()));
		return list;
	}	
}