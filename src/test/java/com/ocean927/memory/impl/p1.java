import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ocean927.framework.services.oms.messages.fix.FixSide;
import com.ocean927.framework.services.position.IHolding;
import com.ocean927.framework.services.position.impl.Trade;

// TODO: algorithm is inefficient, making N^2 passes over data
public class TestCalc {
	
	public TestCalc() {
		
	}

	public List<TradePNLStatistics> calculate(IHolding holding, Map<Instant,Double> mapPx) {
		List<TradePNLStatistics> list=new ArrayList<>();
			
		if( holding.getTrades().size()>0 ) {
										
				// MAKE RESULT PER TRADE
				for (int i = 0; i < holding.getTrades().size(); i++) {
					
					// ADD TRADE TO STATS
					TradePNLStatistics result=new TradePNLStatistics();
					list.add(result);
					
					// SET GLOBAL STUFF
					Trade trade=holding.getTrades().get(i);
					result.rangeFrom=holding.getTrades().get(0).instant;			
					result.rangeTo=trade.instant;
					result.tradesProcessed=i+1;								
					result.trade=trade;
					
					// SHARES AND AVG PRICE BY SIDE
					for (int j = i; j>=0; j--) {
						Trade t=holding.getTrades().get(j);
						if( t.side==FixSide.BUY ) {
							result.grossMoneySpent+=t.shares*t.price;
							result.sumSharesBot+=t.shares;
							result.avgBotPrice=result.grossMoneySpent/result.sumSharesBot;						
						}else
						if( t.side==FixSide.SELL || t.side==FixSide.SELLSHORT ) {
							result.grossMoneyEarned+=t.shares*t.price;
							result.sumSharesSold+=t.shares;
							result.avgSoldPrice=result.grossMoneyEarned/result.sumSharesSold;
						}			
									
						// TOTAL MONEY
						result.position = result.sumSharesBot - result.sumSharesSold;
						result.grossFunds = result.grossMoneyEarned - result.grossMoneySpent;						
					}
					
					// AVG PRICE
					result.avgPx = calculateAvgPx(holding.getTrades(), i+1);
					
					// CALC REALIZED P&L. IT IS TRICKY
					result.realizedPNL=calculateRealizedProfitLoss(holding.getTrades(),i+1);
					
					// MARKET VALUE
					result.marketPx=mapPx.get(trade.instant);
					result.mktValue=result.marketPx*result.position;
					
					// CALCULATE UNREALIZED BY LOOKING BACK
					result.unRealizedPNL=calculateUnrealizedProfilLoss(holding.getTrades(),i+1, result.marketPx);
						
					// ADD UP PNLs
					result.grossPNL=result.realizedPNL + result.unRealizedPNL;
					result.grossPNLCHECK=result.grossFunds + result.mktValue;
				}								
			}
		return list;
	}
	
	public double calculateAvgPx(List<Trade> trades, int len) {
		double result=0;
			int netpos=0; 
			double sum=0;
			for (int i = 0; i<len && i<trades.size(); i++) {
				Trade t=trades.get(i);
				if( t.side==FixSide.BUY ) {
					netpos+=t.shares;
					sum+=t.shares*t.price;
				}else
				if( t.side==FixSide.SELL || t.side==FixSide.SELLSHORT ) {
					netpos+=t.shares;
					sum+=t.shares*t.price;
				}
			}
			if( netpos!=0 ) {
				result=sum/netpos;
			}
		return result;
	}
	
	public double calculateUnrealizedProfilLoss(List<Trade> trades, int len, double mktPx) {
		double unrealized=0;
				
		int netpos=trades.get(0).shares; 
		if( trades.get(0).side!=FixSide.BUY ){ netpos*=-1; }		
		double avgpx=trades.get(0).price;
		if( trades.get(0).side==FixSide.BUY ) {
			unrealized=netpos*(mktPx-avgpx);
		}else{
			unrealized=netpos*(avgpx-mktPx);
		}
		
		for (int i = 1; i<len; i++) {
			Trade t=trades.get(i);	
			// netpos positive
			if( netpos>=0 && t.side==FixSide.BUY ) {
				// position gets longer, update avg price, no change to realized 
				avgpx = (avgpx*netpos+t.shares*t.price)/(netpos+t.shares);
				netpos+=t.shares;
				unrealized=netpos*(mktPx-avgpx);
			}else 
			if( netpos>=0 && t.side==FixSide.SELL ) {
				// shares flipped
				if( netpos>=t.shares ) {
					// avg buy price does not change, shared removed
					netpos-=t.shares;
					unrealized=netpos*(mktPx-avgpx);
				}else{
					// netpos becomes negative, avg price will become equal to new trade					
					netpos-=t.shares;
					avgpx=t.price;
					unrealized=Math.abs(netpos)*(avgpx-mktPx);
				}
			}
			//
			// netpos negative
			else 
			if( netpos<0 && t.side==FixSide.BUY ) {
				// shares flipped
				if( Math.abs(netpos)>=t.shares ) {
					// avg price remains as netpos remains short
					netpos+=t.shares;
					unrealized=Math.abs(netpos)*(avgpx-mktPx);
				}else{
					// netpos becomes positive, avg price will become equal to new trade					
					netpos+=t.shares;
					avgpx=t.price; // reset
					unrealized=netpos*(mktPx-avgpx);
				}
			}else
			if( netpos<0 && t.side==FixSide.SELL ) {
				// position gets shorter, update avg price, no change to realized
				avgpx = (avgpx*Math.abs(netpos)+t.shares*t.price)/(Math.abs(netpos)+t.shares);
				netpos-=t.shares;
				unrealized=Math.abs(netpos)*(avgpx-mktPx);
			}
		}
		
		return unrealized;
	}
	
	public double calculateRealizedProfitLoss(List<Trade> trades, int len) {
		double realized=0;
		
		int netpos=trades.get(0).shares;
		if( trades.get(0).side!=FixSide.BUY ){ netpos*=-1; }
		double avgPx=trades.get(0).price;
		
		for (int i = 1; i<len; i++) {
			Trade t=trades.get(i);	
			// netpos positive
			if( netpos>=0 && t.side==FixSide.BUY ) {
				// position gets longer, update avg price, no change to realized 
				avgPx = (avgPx*netpos+t.shares*t.price)/(netpos+t.shares);
				netpos+=t.shares;
			}else 
			if( netpos>=0 && t.side==FixSide.SELL ) {
				// shares flipped
				if( netpos>=t.shares ) {
					// avg price remains as netpos remains long
					realized +=t.shares*(t.price-avgPx);
					netpos-=t.shares;
				}else{
					// netpos becomes negative, avg price will become equal to new trade
					realized +=netpos*(t.price-avgPx);
					netpos-=t.shares;
					avgPx=t.price;
				}
			}
			//
			// netpos negative
			else 
			if( netpos<0 && t.side==FixSide.BUY ) {
				// shares flipped
				if( Math.abs(netpos)>=t.shares ) {
					// avg price remains as netpos remains short
					realized +=t.shares*(avgPx-t.price);
					netpos+=t.shares;
				}else{
					// netpos becomes positive, avg price will become equal to new trade
					realized +=Math.abs(netpos)*(avgPx-t.price);
					netpos+=t.shares;
					avgPx=t.price;
				}
			}else
			if( netpos<0 && t.side==FixSide.SELL ) {
				// position gets shorter, update avg price, no change to realized 
				avgPx = (avgPx*netpos-t.shares*t.price)/(netpos-t.shares);
				netpos-=t.shares;
			}
		}
		return realized;
	}
}
