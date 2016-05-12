import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;


public class queuemodel {
	double waitingtime;
	double lastVol;
	double nLane_old; // lane number
	public static void main(String[] args) throws MatlabConnectionException, MatlabInvocationException
	{
	
	}
	
	public void cal_queue(double vol, double lastVol, double nLane_old) throws MatlabConnectionException, MatlabInvocationException{
		MatlabProxyFactory factory = new MatlabProxyFactory();
	    MatlabProxy proxy = factory.getProxy();
	    proxy.setVariable("Vol", vol);
	    proxy.setVariable("lastVol", lastVol);
	    proxy.setVariable("nLane_old", nLane_old);
	    proxy.setVariable("waitingtime",0);
	    proxy.setVariable("numV",0);
	    proxy.setVariable("nLane_new",0);
	    proxy.eval("[waitingtime,numV,nLane_new]=queueingmodel(Vol,lastVol,nLane_old)");
	    waitingtime=((double[]) proxy.getVariable("waitingtime"))[0];
	    lastVol=((double[]) proxy.getVariable("numV"))[0];
	    nLane_old=((double[]) proxy.getVariable("nLane_new"))[0];
	}
	
	public void splitVol(double vol) throws MatlabConnectionException, MatlabInvocationException{
		MatlabProxyFactory factory = new MatlabProxyFactory();
	    MatlabProxy proxy = factory.getProxy();
	    proxy.setVariable("Vol", vol);
	    proxy.setVariable("lastVol", lastVol);
	    proxy.setVariable("nLane_old", nLane_old);
	    proxy.setVariable("waitingtime",0);
	    proxy.setVariable("numV",0);
	    proxy.setVariable("nLane_new",0);
	    proxy.eval("[waitingtime,numV,nLane_new]=queueingmodel(Vol,lastVol,nLane_old)");
	    waitingtime=((double[]) proxy.getVariable("waitingtime"))[0];
	    lastVol=((double[]) proxy.getVariable("numV"))[0];
	    nLane_old=((double[]) proxy.getVariable("nLane_new"))[0];
	}
}
