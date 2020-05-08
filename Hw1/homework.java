package hw1;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

class Location{
	private long X;
	private long Y;

	public Location(long x, long y) {
		this.X = x;
		this.Y = y;
	}
	
	public long getX(){
		return this.X;
	}

	public long getY(){
		return this.Y;
	}
	
	public void printLocationInfo(){
		System.out.println(X + " " + Y);
	}
	
	public String toString(){
		return String.valueOf(X)+" "+String.valueOf(Y);
	}
}


class YearLocation{
	
	private long year;
	private Location location;
	 
	public YearLocation(long year, Location location) {
		this.year = year;
		this.location = location;
	}
	
	public long getYear(){
		return this.year;
	}
	
	public Location getLocation(){
		return this.location;
	}
	
	public void printLocationInfo(){
		System.out.println(year + " " + location.toString());
	}
	
	public String toString(){
		return String.valueOf(year)+ " " + location.toString();
	}
}


/*
 * Map<Year, Map<location, ArrayList<Year> > >
 * 
 * 
 * 
 * */
class TimeStructure{
	private long timeChannelNum;
	private Map<Long, Map<String, ArrayList<Long>>> timeChannel;

	public TimeStructure(long time) {
		this.timeChannelNum = time;
		timeChannel = new HashMap<Long, Map<String, ArrayList<Long>>>();
	}
	
	public void addChannel(long location1, long x, long y, long location2){
		String point = new Location(x, y).toString();
	
		if (!timeChannel.containsKey(location1)) {
			timeChannel.put(location1, new HashMap<String, ArrayList<Long>>());
		}
		if (!timeChannel.get(location1).containsKey(point)) {
			timeChannel.get(location1).put(point, new ArrayList<Long>());
		}
		timeChannel.get(location1).get(point).add(location2);
		
		if (!timeChannel.containsKey(location2)) {
			timeChannel.put(location2, new HashMap<String, ArrayList<Long>>());
		}
		if (!timeChannel.get(location2).containsKey(point)) {
			timeChannel.get(location2).put(point, new ArrayList<Long>());
		}
		timeChannel.get(location2).get(point).add(location1);
	}

	public long getChannelNum(){
		return this.timeChannelNum;
	}
	
	public boolean isChannel (long year, String local){
		return timeChannel.containsKey(year) && timeChannel.get(year).containsKey(local);
	}
	
	public ArrayList<Long> getNextYears(long year, String local) {
		return timeChannel.get(year).get(local);
	}
	
	public ArrayList<Long> getNextAllYears(long year) {
		ArrayList<Long> results = new ArrayList<>();
		for(Map.Entry<String, ArrayList<Long>> list: timeChannel.get(year).entrySet()){
			results.addAll(list.getValue());
		}
		return results;
	}

	public Map<String, ArrayList<Long>> getChannels(long year){
		return timeChannel.get(year);
	}
	
	public void printChannels(){
		System.out.println("Channel number is "+ this.timeChannelNum);
		System.out.println("Channel  "+ timeChannel.size());
		
		for(Map.Entry<Long, Map<String, ArrayList<Long>>> entry : timeChannel.entrySet()){
			System.out.println("From: " + entry.getKey());
			
			for(Map.Entry<String, ArrayList<Long>> list: entry.getValue().entrySet()){
				System.out.print( list.getKey()  + " to: " );
				for (Long y : list.getValue()) {
					System.out.println(y);
				}
			}
		}
	}
}    



/*
 * 
 * currentPath =>  "year x y cost"
 * 
 * */
class Path{
	private long totalCost;
	private ArrayList<String> currentPath;
	
	
	
	/**
	 * construct a initial Path item, and only save start node
	 * total cost 0
	 * current path year x y 0
	 * 
	 * */
	public Path(String initial) {
		this.currentPath = new ArrayList<String>();
		this.totalCost = 0;
		this.currentPath.add(initial+" 0");
	}
	
	/**
	 * 
	 * construct a duplicate Path item
	 * 
	 * */
	public Path(Path last) {
		this.totalCost = last.getCost();
		this.currentPath = new ArrayList<>(last.getPath());
	}
	
	public void update(long cost, String nextPlace){
		this.totalCost += cost;
		this.currentPath.add(nextPlace + " " + String.valueOf(cost));
	}
	
	
	/**
	 * get total cost
	 * @return long
	 * 
	 * */
	public long getCost(){
		return totalCost;
	}
	
	/**
	 * get current step number
	 * @return long
	 * */
	public long getStep(){
		return currentPath.size();
	}
	
	public ArrayList<String> getPath(){
		return currentPath;
	}
	
	public void showPath(){
		System.out.println("Path: ");
		for(String str : currentPath){
			System.out.println(str);
		}
	}
	
}



public class homework {


	private static final int SEARCH_METHOD_BFS = 0;
	private static final int SEARCH_METHOD_UCS = 1;
	private static final int SEARCH_METHOD_A = 2;

	/**
	 * 
	 * initial data
	 * 
	 * */
	private static int searchMethod;
	private static long gridWidth;
	private static long gridHeight;
	private static YearLocation initialYear;
	private static YearLocation targetYear;
	private static TimeStructure timeStructure;
	
	
	private static Map<Long, Long> yearCost;
	
	/*
	 * 
	 * result data
	 * 
	 * */
	private static boolean resultFound = false; 
	private static long totalCost;
	private static long totalStep;
	private static ArrayList<String> travelPath = new ArrayList<>();

	

	public static void main (String args[]){
		try
		{
//			File file = new File("./input.txt");
			File file = new File("C:\\Users\\Frank\\Desktop\\561\\Hw1\\code\\hw1\\src\\hw1\\input.txt");
			
			Long filelength = file.length();
	        byte[] filecontent = new byte[filelength.intValue()];
			if(file.isFile() && file.exists()){
				FileInputStream in = new FileInputStream(file);				
				in.read(filecontent);
				in.close();
				
			}else{
				System.out.println("no file");
			}
			/*
			 * windows \r\n
			 * Linux \n
			 * MacOs   \r
			 * 
			 * */
			String[] fileContentArr = new String(filecontent).split("\r\n");
			
			/*
			 * assign the method
			 * 
			 * */
			if(fileContentArr[0].equals("BFS")){
				searchMethod = SEARCH_METHOD_BFS;
			}else if (fileContentArr[0].equals("UCS")) {
				searchMethod = SEARCH_METHOD_UCS;
			}else if (fileContentArr[0].equals("A*")) {
				searchMethod = SEARCH_METHOD_A;
			}
			
			/*
			 * assign Width and Height
			 * 
			 * */
			String [] WH = fileContentArr[1].split(" ");
			gridWidth = Long.parseLong(WH[0]); 
			gridHeight = Long.parseLong(WH[1]);
			
			/*
			 * assign initial and target location
			 * assign time structure
			 * */
			String [] inital = fileContentArr[2].split(" ");
			String [] target = fileContentArr[3].split(" ");
			initialYear = new YearLocation(Long.parseLong(inital[0]), new Location(Long.parseLong(inital[1]), Long.parseLong(inital[2])));
			targetYear =new YearLocation(Long.parseLong(target[0]), new Location(Long.parseLong(target[1]), Long.parseLong(target[2])));
			
			timeStructure = new TimeStructure(Long.parseLong(fileContentArr[4]));
			for(int i = 5; i < fileContentArr.length; i++){
				String [] channel = fileContentArr[i].split(" ");
				timeStructure.addChannel(Long.parseLong(channel[0]), Long.parseLong(channel[1]), Long.parseLong(channel[2]), Long.parseLong(channel[3]));	
			}
			
			System.out.println("fileContentArr.length " + fileContentArr.length);
			for(String str: fileContentArr){
				System.out.println(str);
			}
		}
		catch(Exception e)
		{
			System.out.println("read file error");
		}	
//		
//		System.out.println("-------------Before------------");
//		printTestResult();
//		System.out.println("--------------------------------------------------");
//		System.out.println();
//		
		switch (searchMethod) {
		case SEARCH_METHOD_BFS:
			BFS();
			break;
			
		case SEARCH_METHOD_UCS:
			UCS();
			break;
			
			
		case SEARCH_METHOD_A:
			yearCost = yearCostEst();
			A();
			break;

		default:
			break;
		}
		
		System.out.println("-------------After------------");
		printTestResult();	
		System.out.println("--------------------------------------------------");

		if (resultFound) {
			WriteResultToFile();
		}else{
			WriteToFileNoResult();
		}
	}
	
	
	/**
	 * BFS (Queue)
	 * 
	 * issues ->  change check order, make it within expand 
	 * */
	
	public static void BFS (){
		
		Map<String, Path> map = new HashMap<String, Path>();
		
		Queue<YearLocation> queue = new LinkedList<YearLocation>();
		
		Path initialPath = new Path(initialYear.toString());
		map.put(initialYear.toString(), initialPath);
		queue.add(initialYear);
		
		int [] offsetX = {1,1,-1,-1,0,0,1,-1};
		int [] offsetY = {1,-1,1,-1,1,-1,0,0};
		
		while(!queue.isEmpty() && !resultFound){
			
			YearLocation yl = queue.poll();
			
			//check 
			
			if (yl.toString().equals(targetYear.toString())) {
				resultFound = true;
				Path result = map.get(yl.toString());
				totalCost = result.getCost();
				totalStep = result.getStep();
				travelPath = result.getPath();
			}
			
			//add new node
			////8 neighbors 
			for (int i = 0; i < 8; i++) {
				if(yl.getLocation().getX() + offsetX[i] >=0 && yl.getLocation().getX() + offsetX[i] < gridWidth 
						&& yl.getLocation().getY() + offsetY[i] >=0 && yl.getLocation().getY() + offsetY[i] < gridHeight){
					YearLocation nextLocation = new YearLocation(yl.getYear(), new Location(yl.getLocation().getX() + offsetX[i], yl.getLocation().getY() + offsetY[i] ));
					if (!map.containsKey(nextLocation.toString())) {
						Path next = new Path(map.get(yl.toString()));
						next.update(1, nextLocation.toString());
						map.put(nextLocation.toString(), next);
						queue.add(nextLocation);
					}
				}
			}
			
			////jaunt
			if(timeStructure.isChannel(yl.getYear(), yl.getLocation().toString())){
				ArrayList<Long> nextYear = timeStructure.getNextYears(yl.getYear(), yl.getLocation().toString());
				
				for(Long year : nextYear){
					YearLocation nextLocation = new YearLocation(year, yl.getLocation());
					if(!map.containsKey(nextLocation.toString())){
						Path next = new Path(map.get(yl.toString()));
						next.update(1, nextLocation.toString());
						map.put(nextLocation.toString(), next);
						queue.add(nextLocation);
					}
				}
			}
		}
	}
	
	
	/**
	 * 
	 * map => Map<String, Path> : Map<"Year X Y", Path>
	 *
	 * 
	 * could be improved by updating next path cost if it is better
	 * */
	public static void UCS(){
		Map<String, Path> map = new HashMap<String, Path>();
		Set<String> visited = new HashSet<String>();
		
		
		PriorityQueue<YearLocation> queue = new PriorityQueue<>(new Comparator<YearLocation>() {
			@Override
			public int compare(YearLocation o1, YearLocation o2) {	
				return (int)(map.get(o1.toString()).getCost() - map.get(o2.toString()).getCost());
			}
		});
		
		Path initialPath = new Path(initialYear.toString());
		map.put(initialYear.toString(), initialPath);
		queue.add(initialYear);
		/**
		 * straight cost 10
		 * diagonal cost 14
		 * jaunting action will cost the number of years it time-travels
		 * */
		int [] offsetX_S = {0,0,1,-1};
		int [] offsetY_S = {1,-1,0,0};

		int [] offsetX_D = {1,1,-1,-1};
		int [] offsetY_D = {1,-1,1,-1,};
		
		while(!queue.isEmpty() && !resultFound){
			YearLocation yl = queue.poll();
			if (visited.contains(yl.toString())) {
				continue;
			}
//			System.out.println(yl.toString() + " " + map.get(yl.toString()).getCost());
			
			if (yl.toString().equals(targetYear.toString())) {
				resultFound = true;
				Path result = map.get(yl.toString());
				totalCost = result.getCost();
				totalStep = result.getStep();
				travelPath = result.getPath();
				break;
			}
			
			//add nodes
			//s
			for (int i = 0; i < 4; i++) {
				if(yl.getLocation().getX() + offsetX_S[i] >=0 && yl.getLocation().getX() + offsetX_S[i] < gridWidth 
						&& yl.getLocation().getY() + offsetY_S[i] >=0 && yl.getLocation().getY() + offsetY_S[i] < gridHeight){
					YearLocation nextLocation = new YearLocation(yl.getYear(), new Location(yl.getLocation().getX() + offsetX_S[i], yl.getLocation().getY() + offsetY_S[i] ));
					if (!visited.contains(nextLocation.toString())) {
						Path next = new Path(map.get(yl.toString()));
						next.update(10, nextLocation.toString());

						if (map.containsKey(nextLocation.toString()) ){
							if (map.get(nextLocation.toString()).getCost() > next.getCost()){
								map.put(nextLocation.toString(), next);
							}
						}else {
							map.put(nextLocation.toString(), next);
							queue.add(nextLocation);
						}
					}
				}
			}
			
			for (int i = 0; i < 4; i++) {
				if(yl.getLocation().getX() + offsetX_D[i] >=0 && yl.getLocation().getX() + offsetX_D[i] < gridWidth 
						&& yl.getLocation().getY() + offsetY_D[i] >=0 && yl.getLocation().getY() + offsetY_D[i] < gridHeight){
					YearLocation nextLocation = new YearLocation(yl.getYear(), new Location(yl.getLocation().getX() + offsetX_D[i], yl.getLocation().getY() + offsetY_D[i] ));
					if (!visited.contains(nextLocation.toString())) {
						Path next = new Path(map.get(yl.toString()));
						next.update(14, nextLocation.toString());

						if (map.containsKey(nextLocation.toString()) ){
							if (map.get(nextLocation.toString()).getCost() > next.getCost()){
								map.put(nextLocation.toString(), next);
							}
						}else{
							map.put(nextLocation.toString(), next);
							queue.add(nextLocation);
						}
					}
				}
			}
			
			
			if(timeStructure.isChannel(yl.getYear(), yl.getLocation().toString())){
				ArrayList<Long> nextYear = timeStructure.getNextYears(yl.getYear(), yl.getLocation().toString());
				
				for(Long year : nextYear){
					YearLocation nextLocation = new YearLocation(year, yl.getLocation());
					if(!visited.contains(nextLocation.toString())){
						Path next = new Path(map.get(yl.toString()));
						next.update(Math.abs(year - yl.getYear()), nextLocation.toString());

						if (map.containsKey(nextLocation.toString()) ){
							if (map.get(nextLocation.toString()).getCost() > next.getCost()){
								map.put(nextLocation.toString(), next);
							}
						}else{
							map.put(nextLocation.toString(), next);
							queue.add(nextLocation);
						}
					}
				}
			}
			
			visited.add(yl.toString());
			map.remove(yl.toString());
		}
	}
	
	
	/**
	 * A*
	 * 
	 * 
	 */
	public static void A(){
	
		Map<String, Path> map = new HashMap<String, Path>();
		Set<String> visited = new HashSet<String>();
		Map<String, Long> estimatedCost = new HashMap<>();
		
		PriorityQueue<YearLocation> queue = new PriorityQueue<>(new Comparator<YearLocation>() {
			@Override
			public int compare(YearLocation o1, YearLocation o2) {
				return (int)(estimatedCost.get(o1.toString()) + map.get(o1.toString()).getCost() - estimatedCost.get(o2.toString()) - map.get(o2.toString()).getCost());
			}
		});
		

		Path initialPath = new Path(initialYear.toString());
		map.put(initialYear.toString(), initialPath);
		
		estimatedCost.put(initialYear.toString(), eCost(initialYear));
		queue.add(initialYear);
		
		/**
		 * straight cost 10
		 * diagonal cost 14
		 * jaunting action will cost the number of years it time-travels
		 * */
		int [] offsetX_S = {0,0,1,-1};
		int [] offsetY_S = {1,-1,0,0};

		int [] offsetX_D = {1,1,-1,-1};
		int [] offsetY_D = {1,-1,1,-1,};
		
		while(!queue.isEmpty() && !resultFound){
			YearLocation yl = queue.poll();
			if (visited.contains(yl.toString())) {
				continue;
			}

			if (yl.toString().equals(targetYear.toString())) {
				resultFound = true;
				Path result = map.get(yl.toString());
				totalCost = result.getCost();
				totalStep = result.getStep();
				travelPath = result.getPath();
				break;
			}
			
			//add nodes
			//s
			for (int i = 0; i < 4; i++) {
				if(yl.getLocation().getX() + offsetX_S[i] >=0 && yl.getLocation().getX() + offsetX_S[i] < gridWidth 
						&& yl.getLocation().getY() + offsetY_S[i] >=0 && yl.getLocation().getY() + offsetY_S[i] < gridHeight){
					YearLocation nextLocation = new YearLocation(yl.getYear(), new Location(yl.getLocation().getX() + offsetX_S[i], yl.getLocation().getY() + offsetY_S[i] ));
					if (!visited.contains(nextLocation.toString())) {
						Path next = new Path(map.get(yl.toString()));
						next.update(10, nextLocation.toString());

						if (map.containsKey(nextLocation.toString()) ){
							if (map.get(nextLocation.toString()).getCost() > next.getCost()){
								map.put(nextLocation.toString(), next);
							}
						}else{
							map.put(nextLocation.toString(), next);
							estimatedCost.put(nextLocation.toString(), eCost(nextLocation));
							queue.add(nextLocation);
						}
					}
				}

			}
			
			for (int i = 0; i < 4; i++) {
				if(yl.getLocation().getX() + offsetX_D[i] >=0 && yl.getLocation().getX() + offsetX_D[i] < gridWidth 
						&& yl.getLocation().getY() + offsetY_D[i] >=0 && yl.getLocation().getY() + offsetY_D[i] < gridHeight){
					YearLocation nextLocation = new YearLocation(yl.getYear(), new Location(yl.getLocation().getX() + offsetX_D[i], yl.getLocation().getY() + offsetY_D[i] ));
					if (!visited.contains(nextLocation.toString())) {
						Path next = new Path(map.get(yl.toString()));
						next.update(14, nextLocation.toString());

						if (map.containsKey(nextLocation.toString()) ){
							if (map.get(nextLocation.toString()).getCost() > next.getCost()){
								map.put(nextLocation.toString(), next);
							}
						}else{
							map.put(nextLocation.toString(), next);
							estimatedCost.put(nextLocation.toString(), eCost(nextLocation));
							queue.add(nextLocation);
						}
					}
				}
			}
			
			
			if(timeStructure.isChannel(yl.getYear(), yl.getLocation().toString())) {
				ArrayList<Long> nextYear = timeStructure.getNextYears(yl.getYear(), yl.getLocation().toString());

				for (Long year : nextYear) {
					YearLocation nextLocation = new YearLocation(year, yl.getLocation());
					if (!visited.contains(nextLocation.toString())) {
						Path next = new Path(map.get(yl.toString()));
						next.update(Math.abs(year - yl.getYear()), nextLocation.toString());

						if (map.containsKey(nextLocation.toString())) {
							if (map.get(nextLocation.toString()).getCost() > next.getCost()) {
								map.put(nextLocation.toString(), next);
							}
						} else {
							map.put(nextLocation.toString(), next);
							estimatedCost.put(nextLocation.toString(), eCost(nextLocation));
							queue.add(nextLocation);
						}
					}
				}
			}
			visited.add(yl.toString());
			map.remove(yl.toString());
		}
		
	}
	
	
	public static long eCost(YearLocation yearL){
		if(!yearCost.containsKey(yearL.getYear())){
			return Long.MAX_VALUE;
		}
		
		long X = Math.abs( yearL.getLocation().getX() - targetYear.getLocation().getX() );
		long Y = Math.abs( yearL.getLocation().getY() - targetYear.getLocation().getY() );
		
		return	X + Y +  Math.abs( targetYear.getYear() - yearL.getYear());
	}
	
	public static Map<Long, Long> yearCostEst(){
		Set<Long> visited = new HashSet<>();
		Map<Long, Long> costBetweenYear = new HashMap<Long, Long>();
		Long tarYear = targetYear.getYear();
		
		PriorityQueue<Long> pq = new PriorityQueue<Long>(new Comparator<Long>() {
			@Override
			public int compare(Long o1, Long o2) {
				return (int) (costBetweenYear.get(o1) - costBetweenYear.get(o2));
			}
			
		});
		pq.add(tarYear);
		costBetweenYear.put(tarYear, tarYear - tarYear);
		
		while(!pq.isEmpty()){
			Long year = pq.poll();
			
			if(!visited.contains(year)){
				for ( Long y : timeStructure.getNextAllYears(year)){
					
					if(!costBetweenYear.containsKey(y)){
						costBetweenYear.put(y, Math.abs(y-year) + costBetweenYear.get(year));
						pq.offer(y);
					}else{
						if (costBetweenYear.get(y) > Math.abs(y-year) + costBetweenYear.get(year)) {
							costBetweenYear.put(y, Math.abs(y-year) + costBetweenYear.get(year));
						}
					}
				}
			}
			visited.add(year);
		}
		return costBetweenYear;
	}
	
	/**
	 * write to file with FAIL
	 * 
	 * */
	public static void WriteToFileNoResult() {
        try {
            File file = new File("output.txt");
            PrintStream ps = new PrintStream(new FileOutputStream(file));
            ps.append("FAIL");
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }
	
	/**
	 * write to file with result
	 * 
	 * */
	public static void WriteResultToFile() {
        try {
            File file = new File("output.txt");
            PrintStream ps = new PrintStream(new FileOutputStream(file));
            
            ps.println(totalCost);
            ps.println(totalStep);
            for(int i = 0; i< travelPath.size()-1; i ++){
            	ps.println(travelPath.get(i));
            }
        
            ps.append(travelPath.get(travelPath.size()-1));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
	
	public static void printChannelInfo(){
		System.out.println("searchMethod:" + searchMethod);
		System.out.println("grid:" + gridWidth + " " + gridHeight);
		initialYear.printLocationInfo();
		targetYear.printLocationInfo();
		timeStructure.printChannels();
	}
	
	
	/**
	 * print Test result
	 */
	public static void printTestResult(){
		System.out.println("Result:");
		System.out.println("resultFound: " + resultFound);
		System.out.println("totalCost: " + totalCost);
		System.out.println("totalStep: " + totalStep);	
		for(String str : travelPath){
			System.out.println(str);
		}
	}
}
