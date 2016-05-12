import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;

import org.rosuda.JRI.Rengine;


import  java.io.BufferedReader;  
import  java.io.IOException;  
import  java.io.InputStreamReader;  
import  java.net.MalformedURLException;  
import  java.net.URL;  
import  java.net.URLConnection;  
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import  java.util.ArrayList;  
import java.util.Calendar;
import  java.util.List;  
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import  java.util.regex.Matcher;  
import  java.util.regex.Pattern;

public class arimaR {
	//should be run after xx:50, so it can predict the next 15 mins' delay set by m52
	 static Calendar cal1=Calendar.getInstance();
	 static int hh1=cal1.get(Calendar.HOUR_OF_DAY);
	 static int day1=cal1.get(Calendar.DAY_OF_YEAR);
	static int j=day1*24+hh1+1; // 9:42, find which point 9 am is, the useful volume info is vol at 11 which is 10 am to 11 am, so add 1 here, later will add another 1
	static int preH=0;
	static int flag=0;
	static double [][] VOL=new double [12][4]; // auto usa; truc usa; auto can; truc can;
	static int [] thWait=new int [4]; // take the current waiting time+10 as a threshold
	static double lastVol1=0; // the vehicle number of previous hour
	static double nLane_old1=1; // lane number
	static double lastVol2=0; // the vehicle number of previous hour
	static double nLane_old2=1; // lane number
	static double lastVol3=0; // the vehicle number of previous hour
	static double nLane_old3=1; // lane number
	static double lastVol4=0; // the vehicle number of previous hour
	static double nLane_old4=1; // lane number
	static double waitingtime_auto_toUSA=0; // waiting time at the end of each time interval
	static double waitingtime_truc_toUSA=0; // waiting time at the end of each time interval
	static double waitingtime_auto_toCAN=0; // waiting time at the end of each time interval
	static double waitingtime_truc_toCAN=0; // waiting time at the end of each time interval
	static double cost=0; // cost for each time interval
	public   static  String getHtmlSource(String url)  
    {  
          
        StringBuffer codeBuffer = null ;  
        BufferedReader in=null ;  
        try   
        {  
            URLConnection uc = new  URL(url).openConnection();    
            uc.setRequestProperty("User-Agent" ,  
                    "Mozilla/4.0 (compatible; MSIE 5.0; Windows XP; DigExt)" );  
  
            // è¯»å�–urlæµ�å†…å®?  
            in = new  BufferedReader( new  InputStreamReader(uc  
                    .getInputStream(), "gb2312" ));  
            codeBuffer = new  StringBuffer();  
            String tempCode = "" ;  
            // æŠŠbufferå†…çš„å€¼è¯»å�–å‡ºæ�?ä¿�å­˜åˆ°codeä¸?  
            while  ((tempCode = in.readLine()) !=  null )  
            {  
                codeBuffer.append(tempCode).append("\n" );  
            }  
            in.close();  
        }  
        catch  (MalformedURLException e)  
        {  
            e.printStackTrace();  
        }  
        catch  (IOException e)  
        {  
            e.printStackTrace();  
        }  
          
        return  codeBuffer.toString();  
    }  

    public   static  String regex(String sRegex)  
    {  
        String googleRegex = sRegex ;  
        return  googleRegex;  
    }  
    

    public  static  List<String> getWaitingtime_pb(String website, String sRegex)  
    {  
        List<String> newsList = new  ArrayList<String>();  
        String allHtmlSource = arimaR.getHtmlSource(website);  
        //String sRegex="<div class=g>(.*?)href=\"(.*?)\"(.*?)\">(.*?)</a>(.*?)<div class=std>(.*?)<br>";
        Pattern pattern = Pattern.compile(regex(sRegex));  
        Matcher matcher = pattern.matcher(allHtmlSource);  
        //System.out.println(allHtmlSource);
        while  (matcher.find())  
        {  
            String st1=matcher.group(0);
            //System.out.println(st1);
            //int start=st1.indexOf("<b>");
            st1 = st1.replace("&nbsp;", "");
            st1 = st1.replace("</b>", "");
            int end=st1.indexOf("</td>");
            int start=0;
            int i=end-1;
            String st2=st1.substring(i,i+1);
            while (!st2.contains(">")){
            	i=i-1;
            	st2=st1.substring(i,i+1);
            }
            st2=st1.substring(i+1,end);
            //System.out.println(st2);
            newsList.add(st2);
        }  
        System.out.println(newsList);
        return  newsList;  
    }
    
    public  static  List<String> getWaitingtime_Rainbow_whirl(String website, String sRegex)  
    {  
        List<String> newsList = new  ArrayList<String>();  
        String allHtmlSource = arimaR  
                .getHtmlSource(website);  
        //String sRegex="<div class=g>(.*?)href=\"(.*?)\"(.*?)\">(.*?)</a>(.*?)<div class=std>(.*?)<br>";
        Pattern pattern = Pattern.compile(regex(sRegex));  
        Matcher matcher = pattern.matcher(allHtmlSource);  
        //System.out.println(allHtmlSource);
        while  (matcher.find())  
        {  
            String st1=matcher.group(0);
            st1=st1.replace("<b>", "");
            st1=st1.replace("</b>", "");
            //System.out.println(st1);
            //int start=st1.indexOf("<b>");
            int end=st1.indexOf("</td>");
            int start=0;
            int i=end-1;
            String st2=st1.substring(i,i+1);
            while (!st2.contains(">") && !st2.contains(";")){
            	i=i-1;
            	st2=st1.substring(i,i+1);
            }
            st2=st1.substring(i+1,end);
            //System.out.println(st2);
            newsList.add(st2);
        }  
        return  newsList;  
    } 
    public  static  String getTIMEDATE(String website, String sRegex)  
    {  
        String newsList=null;  
        String allHtmlSource = arimaR  
                .getHtmlSource(website);  
        Pattern pattern = Pattern.compile(regex(sRegex));  
        Matcher matcher = pattern.matcher(allHtmlSource);  
        while  (matcher.find())  
        {  
            String st1=matcher.group(0);
            int end=st1.indexOf("</b>");
            int start=0;
            int i=end-1;
            String st2=st1.substring(i,i+1);
            while (!st2.contains(">")){
            	i=i-1;
            	st2=st1.substring(i,i+1);
            }
            st2=st1.substring(i+1,end);
            newsList=st2; 
        }  
        return  newsList;  
    }
    
    public  static  String getcurWeather(String website, String sRegex)  
    {  
        String newsList=null;  
        String allHtmlSource = arimaR  
                .getHtmlSource(website);  
        Pattern pattern = Pattern.compile(regex(sRegex));  
        Matcher matcher = pattern.matcher(allHtmlSource);  
        while  (matcher.find())  
        {  
            String st1=matcher.group(0);
            int end=st1.indexOf("</div>");
            int start=0;
            int i=end-1;
            String st2=st1.substring(i,i+1);
            while (!st2.contains(">")){
            	i=i-1;
            	st2=st1.substring(i,i+1);
            }
            st2=st1.substring(i+1,end);
            newsList=st2; 
        }  
        return  newsList;  
    }
    
    public  static  String getcurTemp(String website, String sRegex)  
    {  
        String newsList=null;  
        String allHtmlSource = arimaR  
                .getHtmlSource(website);  
        Pattern pattern = Pattern.compile(regex(sRegex));  
        Matcher matcher = pattern.matcher(allHtmlSource);  
        while  (matcher.find())  
        {  
            String st1=matcher.group(0);
            int start=st1.indexOf("value=\"");
            //int start=0;
            int i=start+7;
            String st2=st1.substring(i,i+1);
            while (!st2.contains("\"")){
            	i=i+1;
            	st2=st1.substring(i,i+1);
            }
            st2=st1.substring(start+7,i);
            newsList=st2; 
        }  
        return  newsList;  
    }
    
    public  static  String getcurVisibility(String website, String sRegex)  
    {  
        String newsList=null;  
        String allHtmlSource = arimaR  
                .getHtmlSource(website);  
        Pattern pattern = Pattern.compile(regex(sRegex));  
        Matcher matcher = pattern.matcher(allHtmlSource);  
        while  (matcher.find())  
        {  
            String st1=matcher.group(0);
            int end=st1.indexOf("</span>");
            int start=0;
            int i=end-1;
            String st2=st1.substring(i,i+1);
            while (!st2.contains(">")){
            	i=i-1;
            	st2=st1.substring(i,i+1);
            }
            st2=st1.substring(i+1,end);
            newsList=st2; 
        }  
        return  newsList;  
    }
    
    public static void connectionTOMYSQL(String Year, String Month, String Day, String Time, String Weekday, String weaCondition, String weaTemp, String weaVisib, String USA_auto_PB,String USA_truck_PB,String USA_nexus_PB,String USA_auto_LQ,String USA_truck_LQ,String USA_nexus_LQ,String USA_auto_Rainbow,String USA_truck_Rainbow,String USA_nexus_Rainbow,String USA_nexus_whirlpool,
    		String CAN_auto_PB,String CAN_truck_PB,String CAN_nexus_PB,String CAN_auto_LQ,String CAN_truck_LQ,String CAN_nexus_LQ, String CAN_auto_Rainbow, String CAN_truck_Rainbow, String CAN_nexus_Rainbow, String CAN_nexus_whirlpool) throws SQLException {
    	
    	String host="jdbc:mysql://23.229.141.38:3306/borderwait";
    	//String host="jdbc:mysql://localhost:3306/mysql";
    	String username="ubtransport";
    	String password="ketter204";
    	//try{
    		Connection connect=DriverManager.getConnection(host,username,password);
    		PreparedStatement statement=connect.prepareStatement("INSERT INTO bordercrossing(Year, Month, Day, Time, Weekday, weaCondition, weaTemp, weaVisib, USA_auto_PB,USA_truck_PB,USA_nexus_PB,USA_auto_LQ,USA_truck_LQ,USA_nexus_LQ,USA_auto_Rainbow,USA_truck_Rainbow,USA_nexus_Rainbow,USA_nexus_whirlpool,CAN_auto_PB,CAN_truck_PB,CAN_nexus_PB,CAN_auto_LQ,CAN_truck_LQ,CAN_nexus_LQ, CAN_auto_Rainbow,  CAN_truck_Rainbow,  CAN_nexus_Rainbow,  CAN_nexus_whirlpool)VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
    		statement.setString(1,Year);
    		statement.setString(2,Month);
    		statement.setString(3,Day);
    		statement.setString(4,Time);
    		statement.setString(5,Weekday);
    		statement.setString(6,weaCondition);
    		statement.setString(7,weaTemp);
    		statement.setString(8,weaVisib);
    		statement.setString(9,USA_auto_PB);
    		thWait[0]=Integer.parseInt(USA_auto_PB)+10; //the threshold is current delay +10 minutes
    		statement.setString(10,USA_truck_PB);
    		thWait[1]=Integer.parseInt(USA_truck_PB)+10;//the threshold is current delay +10 minutes
    		statement.setString(11,USA_nexus_PB);
    		statement.setString(12,USA_auto_LQ);
    		statement.setString(13,USA_truck_LQ);
    		statement.setString(14,USA_nexus_LQ);
    		statement.setString(15, USA_auto_Rainbow);
    		statement.setString(16, USA_truck_Rainbow);
    		statement.setString(17, USA_nexus_Rainbow);
    		statement.setString(18, USA_nexus_whirlpool);
    		statement.setString(19,CAN_auto_PB);
    		thWait[2]=Integer.parseInt(CAN_auto_PB)+10;//the threshold is current delay +10 minutes
    		statement.setString(20,CAN_truck_PB);
    		thWait[3]=Integer.parseInt(CAN_truck_PB)+10;//the threshold is current delay +10 minutes
    		statement.setString(21,CAN_nexus_PB);
    		statement.setString(22,CAN_auto_LQ);
    		statement.setString(23,CAN_truck_LQ);
    		statement.setString(24,CAN_nexus_LQ);
    		statement.setString(25, CAN_auto_Rainbow);
    		statement.setString(26, CAN_truck_Rainbow);
    		statement.setString(27, CAN_nexus_Rainbow);
    		statement.setString(28, CAN_nexus_whirlpool);
    		statement.executeUpdate();
    		statement.close();
    		connect.close();
    		System.out.println("downloading works!");
    	//}
    	
    }
    
    public static void connectionTOMYSQL_pre(String Year, String Month, String Day, String hh, String mm, String Weekday, 
    		String preWaiting_auto_toUSA, String preWaiting_truc_toUSA, String preWaiting_auto_toCAN, String preWaiting_truc_toCAN) throws SQLException {
    	
    	String host="jdbc:mysql://23.229.141.38:3306/borderwait";
    	//String host="jdbc:mysql://localhost:3306/mysql";
    	String username="ubtransport";// set this at Godaddy, add the ip of the computer to the trust list at Godaddy, too
    	String password="ketter204";
    	//try{
    		Connection connect=DriverManager.getConnection(host,username,password);
    		PreparedStatement statement=connect.prepareStatement("INSERT INTO predictedwaitingtime(Year, Month, Day, hh, mm, Weekday, preWaiting_auto_toUSA, preWaiting_truc_toUSA, preWaiting_auto_toCAN, preWaiting_truc_toCAN)VALUES(?,?,?,?,?,?,?,?,?,?)");
    		statement.setString(1,Year);
    		statement.setString(2,Month);
    		statement.setString(3,Day);
    		statement.setString(4,hh);// current time, not the predicted time, which is 15 minutes later
    		statement.setString(5,mm);
    		statement.setString(6,Weekday);
    		statement.setString(7, preWaiting_auto_toUSA);
    		statement.setString(8, preWaiting_truc_toUSA);
    		statement.setString(9, preWaiting_auto_toCAN);
    		statement.setString(10, preWaiting_truc_toCAN);
    		statement.executeUpdate();
    		statement.close();
    		connect.close();
    		System.out.println("prediction storage works!");
    	//}
    	
    }
    
    public   static   void  main(String[] args) throws IOException, MatlabConnectionException, MatlabInvocationException
    {  
    	
        final String web1="http://www.peacebridge.com/2012Traffic/index.php";//new website 04/02/2016, lq brdige and peace bridge
        final String web2="http://www.niagarafallsbridges.com/";//rainbow bridge and whirlpool bridge
        final String web3="http://www.wunderground.com/US/NY/Buffalo.html";
        final String sRegexPB="<td class.*?\n*?.*?</td>"; // new html format 04/02/2016, .* any number of character, \n*, any number of \n, (?!--) means no "--" 
        //? greedy and lazy mode, as few as possible
        //final String sRegex2="<td  colspan=3 align=center>Real-time traffic conditions as of:  <br /><b>.*</b></td>";
        final String sRegex2="<th scope.*Real-time traffic conditions as of:  <br /><b>.*</b></th>";//updated 04/02/2016
        //final String sRegex3="<td class.*?</td>";//</td> å‰�é�¢åŠ é—®å�?é�žè´ªå©ªæ¨¡å¼?
        final String sRegex3="<td class[\\s\\S]*?</td>";//</td> å‰�é�¢åŠ é—®å�?é�žè´ªå©ªæ¨¡å¼?[\\s\\S]* åŒ¹é…�ä»»æ„�å­—ç¬¦ï¼ŒåŒ…æ‹¬æ�¢è¡?
        final String sRegex_curcond="<div id=\"curCond\">.*</div>";//weather condition
        final String sRegex_curtemp="<div id=\"tempActual\">.*value=\".*\">";//tempurature
        final String sRegex_curvisi="<span class=\"nobr\"><span class=\"b\">.*</span>&nbsp;miles</span>";//visibility
        
        
       
	    
        // run the program every 5 minutes this is for download the waitint time from the official website
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				//System.out.println(sRegexPB);
				List<String> wTime=getWaitingtime_pb(web1,sRegexPB);
		        String timeDATE=getTIMEDATE(web1,sRegex2);
		        String currWeather=getcurWeather(web3,sRegex_curcond);
		        String currTemp=getcurTemp(web3,sRegex_curtemp);
		        String currVisi=getcurVisibility(web3,sRegex_curvisi);
		        if (currWeather==null){
		        	currWeather="N/A";
		        }
		        if (currTemp==null){
		        	currTemp="N/A";
		        }
		        if (currVisi==null){
		        	currVisi="N/A";
		        }
		        List<String> wTime1=getWaitingtime_Rainbow_whirl(web2,sRegex3);
		        wTime1=wTime1.subList(0, 24);
		        // N/A, Unknown, "", replaced with "-1"
		        // closed,replaced with "10000"
		        // No Delay
		         Calendar cal=Calendar.getInstance();
			     String year=String.valueOf(cal.get(Calendar.YEAR));
			     String month=null;
			     if (cal.get(Calendar.MONTH)==0){
			    	 month="Jan";
			     }
			     else if (cal.get(Calendar.MONTH)==1){
			    	 month="Feb";
			     }
			     else if (cal.get(Calendar.MONTH)==2){
			    	 month="Mar";
			     }
			     else if (cal.get(Calendar.MONTH)==3){
			    	 month="Apr";
			     }
			     else if (cal.get(Calendar.MONTH)==4){
			    	 month="May";
			     }
			     else if (cal.get(Calendar.MONTH)==5){
			    	 month="Jun";
			     }
			     else if (cal.get(Calendar.MONTH)==6){
			    	 month="Jul";
			     }
			     else if (cal.get(Calendar.MONTH)==7){
			    	 month="Aug";
			     }
			     else if (cal.get(Calendar.MONTH)==8){
			    	 month="Sep";
			     }
			     else if (cal.get(Calendar.MONTH)==9){
			    	 month="Oct";
			     }
			     else if (cal.get(Calendar.MONTH)==10){
			    	 month="Nov";
			     }
			     else if (cal.get(Calendar.MONTH)==11){
			    	 month="Dec";
			     }
			     
			     String day=String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			     String weekday=null;
			     int dayofweek=cal.get(Calendar.DAY_OF_WEEK);
			     if (dayofweek==2){
			    	 weekday="Mon";
			     }
			     else if (dayofweek==3){
			    	 weekday="Tue";
			     }
			     else if (dayofweek==4){
			    	 weekday="Wed";
			     }
			     else if (dayofweek==5){
			    	 weekday="Thu";
			     }
			     else if (dayofweek==6){
			    	 weekday="Fri";
			     }
			     else if (dayofweek==7){
			    	 weekday="Sat";
			     }
			     else if (dayofweek==1){
			    	 weekday="Sun";
			     }
			    
			     String hh=String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
			     String mm=String.valueOf(cal.get(Calendar.MINUTE));
			     int m6=cal.get(Calendar.MINUTE)/5;
			     String HHMM=null;
			     if (m6==1 || m6==0){
			    	HHMM=hh+":"+"0"+String.valueOf(m6*5);
			     }
			     else {
			    	HHMM=hh+":"+String.valueOf(m6*5); 
			     }
		        int size_wTime=wTime.size();
		        int size_wTime1=wTime1.size();
		        //wTime.set(1, "2hr 45min");
		        
		        for (int i=0;i<size_wTime;i++){
		       	 String s=wTime.get(i);
		       	 if (s.equals("N/A") || s.equals("Unknown") ||s.equals(""))
		       	 {
		       		 wTime.set(i, "-1");
		       	 }
		       	 else if (s.equals("CLOSED"))
		       	 {
		       		 wTime.set(i,"10000");
		       	 }
		       	 else if (s.equals("No Delay")){
		       		 wTime.set(i,"0");
		       	 }
		       	 else if (s.contains("hr")){
		       		 String [] splithour=s.split("hr");
		       		 splithour[1]=splithour[1].substring(1,3);
		       		 int i1= Integer.parseInt(splithour[0])*60;
		       		 int cao=Integer.parseInt(splithour[1]);
		       		 int i2=i1+cao;
		       		 wTime.set(i, String.valueOf(i2));
		       	 }
		       	 else if (!s.contains("hr")){
		       		 s=s.replace(" min", "");
		       		 wTime.set(i, s);
		       	 }
		        }
		        
		        for (int i=0;i<size_wTime1;i++){
		       	 String s=wTime1.get(i);
		       	 if (s.equals("N/A") || s.equals("Unknown") ||s.equals(""))
		       	 {
		       		 wTime1.set(i, "-1");
		       	 }
		       	 else if (s.equals("CLOSED"))
		       	 {
		       		 wTime1.set(i,"10000");
		       	 }
		       	 else if (s.equals("No Delay")){
		       		 wTime1.set(i,"0");
		       	 }
		       	 else if (s.contains("hr")){
		       		 String [] splithour=s.split("hr");
		       		 splithour[1]=splithour[1].substring(1,3);
		       		 int i1= Integer.parseInt(splithour[0])*60;
		       		 int cao=Integer.parseInt(splithour[1]);
		       		 int i2=i1+cao;
		       		 wTime1.set(i, String.valueOf(i2));
		       	 }
		       	 else if (!s.contains("hr")){
		       		 s=s.replace(" min", "");
		       		 wTime1.set(i, s);
		       	 }
		        }
		        timeDATE=timeDATE.replace(".","");
		        timeDATE=timeDATE.replace(",", "");
		        String [] timeDATE1=timeDATE.split(" ");
		        /*timeDATE=timeDATE.replace(",","");
		        timeDATE=timeDATE.replace(":","");*/
		        if (timeDATE1[5].equals("PM")){
		       	int i= Integer.parseInt(timeDATE1[4].substring(0, 2))+12;
		       	
		       	String s=String.valueOf(i)+timeDATE1[4].substring(2, timeDATE1.length-1);
		       	timeDATE1[4]=s;
		        }
		        
		        try {
         			connectionTOMYSQL(year,month,day,HHMM,weekday,currWeather,currTemp,currVisi,wTime.get(1),wTime.get(4),wTime.get(7),wTime.get(2),wTime.get(5),wTime.get(8),wTime1.get(1),wTime1.get(5),wTime1.get(9),wTime1.get(10),wTime.get(10),wTime.get(13),wTime.get(16),wTime.get(11),wTime.get(14),wTime.get(17),wTime1.get(13),wTime1.get(17),wTime1.get(21),wTime1.get(22));
         		} catch (SQLException e) {
         			// TODO Auto-generated catch block
         			e.printStackTrace();
         		}
		        
		      
		        
		        
			}
        	
        }, 0, 5, TimeUnit.MINUTES);
        
      
     // run the program every 15 minutes, this is for predicting the waiting time from bridges
        MatlabProxyFactory factory = new MatlabProxyFactory();
	    final MatlabProxy proxy = factory.getProxy();
	    //final MatlabProxy proxy= factory.getProxy();
	    
        // new R-engine
		// integrated with the data crawl function, update every 15 minutes, 
		final Rengine re;
	    String[] dummyArgs = new String[1];
	    dummyArgs[0] = "--vanilla";
	    re = new Rengine(dummyArgs, false, null);
	    
	    re.eval(".libPaths('C:/Users/zhenhuaz/Documents/R/win-library/3.2');");// updated 04/03/2016
	    re.eval("library(forecast)"); // this works, DBI will load
	    //re.eval("A<-read.csv('D:/adroid app/workspace/borderwaitingtimeprediction/ave_data.csv',header=FALSE);");
	    re.eval("A<-read.csv('C:/Program Files/eclipse-standard-luna-SR2-win32-x86_64/Border crossing/borderwaitingtimeprediction/ave_data.csv',header=FALSE);");
	    
        ScheduledExecutorService ses_pre = Executors.newSingleThreadScheduledExecutor();
	    //re.eval("x1=c();"); 
        ses_pre.scheduleAtFixedRate(new Runnable() {
	    
		@Override
		public void run() {
			// TODO Auto-generated method stub
			// find if the next hour is between 7 am-21 pm 
			  //--------------prediction-------------------------
		     // TODO Auto-generated method stub
				// find if the next hour is between 7 am-21 pm 
				
				Calendar cal2=Calendar.getInstance();
			     String year2=String.valueOf(cal2.get(Calendar.YEAR));
			     int month1=cal2.get(Calendar.MONTH);
			     String month2=String.valueOf(month1+1); //0 jan, 1 feb, 2 march
			     		     
			     String day2=String.valueOf(cal2.get(Calendar.DAY_OF_MONTH));
			     String weekday2=null;
			     int dayofweek2=cal2.get(cal2.DAY_OF_WEEK);
			     if (dayofweek2==2){
			    	 weekday2="Mon";
			     }
			     else if (dayofweek2==3){
			    	 weekday2="Tue";
			     }
			     else if (dayofweek2==4){
			    	 weekday2="Wed";
			     }
			     else if (dayofweek2==5){
			    	 weekday2="Thu";
			     }
			     else if (dayofweek2==6){
			    	 weekday2="Fri";
			     }
			     else if (dayofweek2==7){
			    	 weekday2="Sat";
			     }
			     else if (dayofweek2==1){
			    	 weekday2="Sun";
			     }
			     String hh2=String.valueOf(cal2.get(Calendar.HOUR_OF_DAY));
			    
			     int m52=cal2.get(Calendar.MINUTE)/5;
			     String mm2=String.valueOf(m52*5);
				
			    if (j==8760) { // a new year starts
			    	j=0;
			    }
			    if (m52==10){ // need start to predict the volume and calculate the delay for the next 15 minutes, for example, 11:50, I need know delay at 12:05
			    	preH=j%24;
			    	j=j+1; // one hour passed
			    }
			    if (7<=preH && preH<=20){// start from 6:50, preH=7, j=8, ...until 19:50, preH=20, j=21
			    	if (m52==10) 
				    {
			    		double pre_auto_toUSA=0;
			    		double pre_truc_toUSA=0;
			    		double pre_auto_toCAN=0;
			    		double pre_truc_toCAN=0;
			    		re.eval("i="+String.valueOf(j)+";");
			    		re.eval("a=A[i,2]");//auto to USA
			    		pre_auto_toUSA=re.eval("a").asDouble(); 
			    		re.eval("a=A[i,3]");//truck to USA
			    		pre_truc_toUSA=re.eval("a").asDouble(); 
			    		re.eval("a=A[i,4]");// auto to CAN
			    		pre_auto_toCAN=re.eval("a").asDouble(); 
			    		re.eval("a=A[i,5]");// truck to CAN
			    		pre_truc_toCAN=re.eval("a").asDouble(); 
			    		//here we dont want to predict any more, why not use the data directly. 
			    		/*if (j>240){
			    		re.eval("i="+String.valueOf(j)+";");
				        re.eval("a1=i-240;");
				        //System.out.println (re.eval ("i").asDouble ());
				        //System.out.println (re.eval ("a1").asDouble ());
				        re.eval("a2=i-1;");
				        re.eval("model<-auto.arima(A[a1:a2,],d=NA,D=NA,max.p=10,max.q=10,max.P=10,max.Q=10,max.order=5,start.p=2,start.q=2, start.P=1,start.Q=1,stationary=FALSE,seasonal=TRUE,ic=c(\"aicc\",\"aic\",\"bic\"),stepwise=TRUE);");
				        re.eval("x=forecast(model,h=1);");
				        re.eval("a=x[['mean']];");
				        pre=re.eval("a").asDouble(); //get the prediction of next hour
				        }
				        else if (j<=240)// take the historical value as the prediction
				        {
				        	re.eval("i="+String.valueOf(j)+";");
				        	re.eval("a=A[i]");
				        	pre=re.eval("a").asDouble(); 
				        }*/
				        	// split the predicted volume into 15 minutes interval
				        try {
							proxy.setVariable("Vol", pre_auto_toUSA);
							proxy.setVariable("vol1", 0);
						    proxy.setVariable("vol2", 0);
						    proxy.setVariable("vol3", 0);
						    proxy.setVariable("vol4", 0);
						    proxy.setVariable("vol5", 0);
						    proxy.setVariable("vol6", 0);
						    proxy.setVariable("vol7", 0);
						    proxy.setVariable("vol8", 0);
						    proxy.setVariable("vol9", 0);
						    proxy.setVariable("vol10", 0);
						    proxy.setVariable("vol11", 0);
						    proxy.setVariable("vol12", 0);
						    
						    proxy.eval("[vol1,vol2,vol3,vol4,vol5,vol6,vol7,vol8,vol9,vol10,vol11,vol12]=splitVol(Vol)"); //D:\adroid app\workspace\borderwaitingtimeprediction
						    VOL[0][0]=((double[]) proxy.getVariable("vol1"))[0];
						    VOL[1][0]=((double[]) proxy.getVariable("vol2"))[0];
						    VOL[2][0]=((double[]) proxy.getVariable("vol3"))[0];
						    VOL[3][0]=((double[]) proxy.getVariable("vol4"))[0];
						    VOL[4][0]=((double[]) proxy.getVariable("vol5"))[0];
						    VOL[5][0]=((double[]) proxy.getVariable("vol6"))[0];
						    VOL[6][0]=((double[]) proxy.getVariable("vol7"))[0];
						    VOL[7][0]=((double[]) proxy.getVariable("vol8"))[0];
						    VOL[8][0]=((double[]) proxy.getVariable("vol9"))[0];
						    VOL[9][0]=((double[]) proxy.getVariable("vol10"))[0];
						    VOL[10][0]=((double[]) proxy.getVariable("vol11"))[0];
						    VOL[11][0]=((double[]) proxy.getVariable("vol12"))[0];
						    
						    proxy.setVariable("Vol", pre_truc_toUSA);
							proxy.setVariable("vol1", 0);
						    proxy.setVariable("vol2", 0);
						    proxy.setVariable("vol3", 0);
						    proxy.setVariable("vol4", 0);
						    proxy.setVariable("vol5", 0);
						    proxy.setVariable("vol6", 0);
						    proxy.setVariable("vol7", 0);
						    proxy.setVariable("vol8", 0);
						    proxy.setVariable("vol9", 0);
						    proxy.setVariable("vol10", 0);
						    proxy.setVariable("vol11", 0);
						    proxy.setVariable("vol12", 0);
						    
						    proxy.eval("[vol1,vol2,vol3,vol4,vol5,vol6,vol7,vol8,vol9,vol10,vol11,vol12]=splitVol(Vol)"); //D:\adroid app\workspace\borderwaitingtimeprediction
						    VOL[0][1]=((double[]) proxy.getVariable("vol1"))[0];
						    VOL[1][1]=((double[]) proxy.getVariable("vol2"))[0];
						    VOL[2][1]=((double[]) proxy.getVariable("vol3"))[0];
						    VOL[3][1]=((double[]) proxy.getVariable("vol4"))[0];
						    VOL[4][1]=((double[]) proxy.getVariable("vol5"))[0];
						    VOL[5][1]=((double[]) proxy.getVariable("vol6"))[0];
						    VOL[6][1]=((double[]) proxy.getVariable("vol7"))[0];
						    VOL[7][1]=((double[]) proxy.getVariable("vol8"))[0];
						    VOL[8][1]=((double[]) proxy.getVariable("vol9"))[0];
						    VOL[9][1]=((double[]) proxy.getVariable("vol10"))[0];
						    VOL[10][1]=((double[]) proxy.getVariable("vol11"))[0];
						    VOL[11][1]=((double[]) proxy.getVariable("vol12"))[0];
						    
						    proxy.setVariable("Vol", pre_auto_toCAN);
							proxy.setVariable("vol1", 0);
						    proxy.setVariable("vol2", 0);
						    proxy.setVariable("vol3", 0);
						    proxy.setVariable("vol4", 0);
						    proxy.setVariable("vol5", 0);
						    proxy.setVariable("vol6", 0);
						    proxy.setVariable("vol7", 0);
						    proxy.setVariable("vol8", 0);
						    proxy.setVariable("vol9", 0);
						    proxy.setVariable("vol10", 0);
						    proxy.setVariable("vol11", 0);
						    proxy.setVariable("vol12", 0);
						    
						    proxy.eval("[vol1,vol2,vol3,vol4,vol5,vol6,vol7,vol8,vol9,vol10,vol11,vol12]=splitVol(Vol)"); //D:\adroid app\workspace\borderwaitingtimeprediction
						    VOL[0][2]=((double[]) proxy.getVariable("vol1"))[0];
						    VOL[1][2]=((double[]) proxy.getVariable("vol2"))[0];
						    VOL[2][2]=((double[]) proxy.getVariable("vol3"))[0];
						    VOL[3][2]=((double[]) proxy.getVariable("vol4"))[0];
						    VOL[4][2]=((double[]) proxy.getVariable("vol5"))[0];
						    VOL[5][2]=((double[]) proxy.getVariable("vol6"))[0];
						    VOL[6][2]=((double[]) proxy.getVariable("vol7"))[0];
						    VOL[7][2]=((double[]) proxy.getVariable("vol8"))[0];
						    VOL[8][2]=((double[]) proxy.getVariable("vol9"))[0];
						    VOL[9][2]=((double[]) proxy.getVariable("vol10"))[0];
						    VOL[10][2]=((double[]) proxy.getVariable("vol11"))[0];
						    VOL[11][2]=((double[]) proxy.getVariable("vol12"))[0];
						    
						    proxy.setVariable("Vol", pre_truc_toCAN);
							proxy.setVariable("vol1", 0);
						    proxy.setVariable("vol2", 0);
						    proxy.setVariable("vol3", 0);
						    proxy.setVariable("vol4", 0);
						    proxy.setVariable("vol5", 0);
						    proxy.setVariable("vol6", 0);
						    proxy.setVariable("vol7", 0);
						    proxy.setVariable("vol8", 0);
						    proxy.setVariable("vol9", 0);
						    proxy.setVariable("vol10", 0);
						    proxy.setVariable("vol11", 0);
						    proxy.setVariable("vol12", 0);
						    
						    proxy.eval("[vol1,vol2,vol3,vol4,vol5,vol6,vol7,vol8,vol9,vol10,vol11,vol12]=splitVol(Vol)"); //D:\adroid app\workspace\borderwaitingtimeprediction
						    VOL[0][3]=((double[]) proxy.getVariable("vol1"))[0];
						    VOL[1][3]=((double[]) proxy.getVariable("vol2"))[0];
						    VOL[2][3]=((double[]) proxy.getVariable("vol3"))[0];
						    VOL[3][3]=((double[]) proxy.getVariable("vol4"))[0];
						    VOL[4][3]=((double[]) proxy.getVariable("vol5"))[0];
						    VOL[5][3]=((double[]) proxy.getVariable("vol6"))[0];
						    VOL[6][3]=((double[]) proxy.getVariable("vol7"))[0];
						    VOL[7][3]=((double[]) proxy.getVariable("vol8"))[0];
						    VOL[8][3]=((double[]) proxy.getVariable("vol9"))[0];
						    VOL[9][3]=((double[]) proxy.getVariable("vol10"))[0];
						    VOL[10][3]=((double[]) proxy.getVariable("vol11"))[0];
						    VOL[11][3]=((double[]) proxy.getVariable("vol12"))[0];
						    
						} catch (MatlabInvocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				       
				        flag=0; 
				       
				    }
			    	// run the queueing model next
			    	try {
						proxy.setVariable("Vol", VOL[flag][0]);
						proxy.setVariable("th",thWait[0]);
					    proxy.setVariable("lastVol", lastVol1);
					    proxy.setVariable("nLane_old", nLane_old1);
					    proxy.setVariable("waitingtime",0);
					    proxy.setVariable("numV",0);
					    proxy.setVariable("nLane_new",0);
					    proxy.setVariable("cost", 0);
					    proxy.eval("[waitingtime,numV,nLane_new]=queueingmodel(Vol,lastVol,nLane_old,th)");
					    waitingtime_auto_toUSA=((double[]) proxy.getVariable("waitingtime"))[0];
					    waitingtime_auto_toUSA=Math.round(waitingtime_auto_toUSA);
					    lastVol1=((double[]) proxy.getVariable("numV"))[0];
					    nLane_old1=((double[]) proxy.getVariable("nLane_new"))[0];
					    cost=((double[]) proxy.getVariable("cost"))[0];
					    System.out.println("waitingtime_auto_toUSA: " + waitingtime_auto_toUSA);
					    //-------------------------------truck_toUSA, auto_toCAN, truck_toCAN can be calculated in 5 minutes due to the computation ability of the computer ----------
					    proxy.setVariable("Vol", VOL[flag][1]);
						//flag=flag+1;
					    proxy.setVariable("th",thWait[1]);
					    proxy.setVariable("lastVol", lastVol2);
					    proxy.setVariable("nLane_old", nLane_old2);
					    proxy.setVariable("waitingtime",0);
					    proxy.setVariable("numV",0);
					    proxy.setVariable("nLane_new",0);
					    proxy.setVariable("cost", 0);
					    proxy.eval("[waitingtime,numV,nLane_new]=queueingmodel(Vol,lastVol,nLane_old,th)");
					    waitingtime_truc_toUSA=((double[]) proxy.getVariable("waitingtime"))[0];
					    waitingtime_truc_toUSA=Math.round(waitingtime_truc_toUSA);
					    lastVol2=((double[]) proxy.getVariable("numV"))[0];
					    nLane_old2=((double[]) proxy.getVariable("nLane_new"))[0];
					    cost=((double[]) proxy.getVariable("cost"))[0];
					    System.out.println("waitingtime_truc_toUSA: " + waitingtime_truc_toUSA);
					    
					    proxy.setVariable("Vol", VOL[flag][2]);
						//flag=flag+1;
					    proxy.setVariable("th",thWait[2]);
					    proxy.setVariable("lastVol", lastVol3);
					    proxy.setVariable("nLane_old", nLane_old3);
					    proxy.setVariable("waitingtime",0);
					    proxy.setVariable("numV",0);
					    proxy.setVariable("nLane_new",0);
					    proxy.setVariable("cost", 0);
					    proxy.eval("[waitingtime,numV,nLane_new]=queueingmodel(Vol,lastVol,nLane_old,th)");
					    waitingtime_auto_toCAN=((double[]) proxy.getVariable("waitingtime"))[0];
					    waitingtime_auto_toCAN=Math.round(waitingtime_auto_toCAN);
					    lastVol3=((double[]) proxy.getVariable("numV"))[0];
					    nLane_old3=((double[]) proxy.getVariable("nLane_new"))[0];
					    cost=((double[]) proxy.getVariable("cost"))[0];
					    System.out.println("waitingtime_auto_toCAN: " + waitingtime_auto_toCAN);
					    
					    proxy.setVariable("Vol", VOL[flag][3]);
					    proxy.setVariable("th",thWait[3]);
						//flag=flag+1;
					    proxy.setVariable("lastVol", lastVol4);
					    proxy.setVariable("nLane_old", nLane_old4);
					    proxy.setVariable("waitingtime",0);
					    proxy.setVariable("numV",0);
					    proxy.setVariable("nLane_new",0);
					    proxy.setVariable("cost", 0);
					    proxy.eval("[waitingtime,numV,nLane_new]=queueingmodel(Vol,lastVol,nLane_old,th)");
					    waitingtime_truc_toCAN=((double[]) proxy.getVariable("waitingtime"))[0];
					    waitingtime_truc_toCAN=Math.round(waitingtime_truc_toCAN);
					    lastVol4=((double[]) proxy.getVariable("numV"))[0];
					    nLane_old4=((double[]) proxy.getVariable("nLane_new"))[0];
					    cost=((double[]) proxy.getVariable("cost"))[0];
					    System.out.println("waitingtime_truc_toCAN: " + waitingtime_truc_toCAN);
					    flag=flag+1;
					} catch (MatlabInvocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    	try {
	         			//connectionTOMYSQL_pre(year2,month2,day2,hh2,mm2,weekday2,String.valueOf(waitingtime_auto_toUSA),String.valueOf(waitingtime_truc_toUSA),String.valueOf(waitingtime_auto_toCAN), String.valueOf(waitingtime_truc_toCAN));
	         			connectionTOMYSQL_pre(year2,month2,day2,hh2,mm2,weekday2,String.valueOf(waitingtime_auto_toUSA),"NA","NA","NA");
			    	} catch (SQLException e) {
	         			// TODO Auto-generated catch block
	         			e.printStackTrace();
	         		}
			    }
			    else {
			    	//not predict
		        	lastVol1=0;
		        	nLane_old1=0;
		        	lastVol2=0;
		        	nLane_old2=0;
		        	lastVol3=0;
		        	nLane_old3=0;
		        	lastVol4=0;
		        	nLane_old4=0;
		        	try {
	         			connectionTOMYSQL_pre(year2,month2,day2,hh2,mm2,weekday2,"NA","NA","NA","NA");
	         		} catch (SQLException e) {
	         			// TODO Auto-generated catch block
	         			e.printStackTrace();
	         		}
		        }
			
		}
        }, 0, 5, TimeUnit.MINUTES);
        
        
	    
		/*
        
        MatlabProxyFactory factory = new MatlabProxyFactory();
	    final MatlabProxy proxy = factory.getProxy();
	    
        // new R-engine
		// integrated with the data crawl function, update every 15 minutes, 
		final Rengine re;
	    String[] dummyArgs = new String[1];
	    dummyArgs[0] = "--vanilla";
	    re = new Rengine(dummyArgs, false, null);
	    
	    re.eval(".libPaths('C:/Users/lei/Documents/R/win-library/2.15');");
	    re.eval("library(forecast)"); // this works, DBI will load
	    re.eval("A<-read.csv('D:/adroid app/workspace/borderwaitingtimeprediction/ave_data.csv',header=FALSE);");// does not recognize D:\\20091.csv, which is weird
	    // in future, this should be aggregated averaged hourly traffic volume if we use ARIMA model
	    // if use SVR model, the prediction case should be the averaged hourly traffic volume
	  
		
		
    	*/
	    
	    //System.out.println (re.eval ("x1").asDouble ());
        //re.eval("write.csv(x1,\"D:/prediction1.csv\")");
        //System.out.println (re.eval ("x1").asDouble ());
        
        //-------------------------------**************************************** done...******************************//
        //re.end();
        //proxy.disconnect();
        //ses.shutdown();
        //ses_pre.shutdown();
        //-------------------------------**************************************** done...*****************************//
	    //REXP x;
        //Disconnect the proxy from MATLAB
	    
	    //System.out.println(x=re.eval("library(\"forecast\")"));
        
        //System.out.println (re.eval ("typeof(a)").asString());
        //re.eval("A=list(2)");
        //System.out.println (re.eval ("A[[1]][1]").asDouble ());
        //re.eval("for (i in 1201:1201)");
        //re.eval("{");
        //re.eval("model<-auto.arima(A[a1:a2,],d=NA,D=NA,max.p=10,max.q=10,max.P=10,max.Q=10,max.order=5,max.d=5,max.D=5,start.p=2,start.q=2, start.P=1,start.Q=1,stationary=FALSE,seasonal=TRUE,ic=c(\"aicc\",\"aic\",\"bic\"),stepwise=TRUE);");
        //re.eval("a=x[["mean"]];")
        //re.eval("model<-auto.arima(A,d=NA,D=NA,max.p=2,max.q=2,max.P=2,max.Q=2,max.order=2,max.d=2,max.D=2,start.p=2,start.q=2, start.P=1,start.Q=1,stationary=FALSE,seasonal=TRUE,ic=c(\"aicc\",\"aic\",\"bic\"),stepwise=TRUE);");
        // print a random number from uniform distribution
        //re.eval("model<-auto.arima(A,d=NA,D=NA,max.p=2,max.q=2,max.P=2,max.Q=2,max.order=2,start.p=2,start.q=2, start.P=1,start.Q=1,stationary=FALSE,seasonal=TRUE,ic=c('aicc','aic','bic'),stepwise=TRUE);");
        //re.eval("model<-auto.arima(A)");
        // System.out.println (re.eval ("typeof(model)").asString());
        //re.eval("x=forecast(model,h=1);");
        //re.eval("a=-1"); 
        //System.out.println (re.eval ("x[['mean']]").asDouble ());
        //re.eval("x1[i-1200]=a[1];");
        //System.out.println (re.eval ("x1").asDouble ());      
        //re.eval("}");
        //re.eval("a<-c(1);");
        //re.eval("x1=a[1];"); 
        // when finished
    }

}
