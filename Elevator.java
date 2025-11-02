package bluestaqTakeHome;

import java.util.ArrayList;
import java.util.List;

public class Elevator extends Thread {
    private final int numFloors;
    private final List<Integer> requests = new ArrayList<>();
    private int curFloor = 0;
    private int direction = 1;//1 = up, -1 = down

    public Elevator(int numFloors){
    	if (numFloors < 1) {
            throw new IllegalArgumentException("Building must have at least 1 floor.");
        }
    	
        this.numFloors = numFloors;
    }

    //user makes call
    public synchronized void call(int floor){
        if (floor < 0 || floor >= numFloors) {
            System.out.println("Invalid floor");
            return;
        }
        requests.add(floor);
        System.out.println("Call received for floor " + floor);
        notify();
    }

    @Override
    public void run(){
        while(true){
            int nextFloor;

            synchronized(this){
                //wait until there are requests
                while (requests.isEmpty()){
                    try{
                        System.out.println("No requests, elevator waiting");
                        wait();
                    }catch (InterruptedException e){
                        System.out.println("Elevator out of order");
                        return;
                    }
                }

                //when there are requests, pick one
                nextFloor = chooseNextFloor();
            }

            moveTo(nextFloor);
        }
    }

    //uses direction based logic
    private synchronized int chooseNextFloor(){
    	
    	if (requests.contains(curFloor)) {
    	    requests.remove(Integer.valueOf(curFloor));
    	}
    	
        //else if there is a called floor in the cur direction, go there
        int next = findFloorInDirection();
        if (next != -1) return next;

        //else if there is a called floor in the opposite direction, go there
        direction*= -1;
        next = findFloorInDirection();
        if (next != -1) return next;

        //else go to bottom floor
        return 0;
    }
    
    //given a direction, check if there are any calls that way
    private int findFloorInDirection(){
        int best = -1;
        int dist = Integer.MAX_VALUE;

        for(int f : requests){
            if((direction == 1 && f > curFloor) ||
                (direction == -1 && f < curFloor)){
                int curDist = Math.abs(f - curFloor);
                if (curDist < dist){
                    dist = curDist;
                    best = f;
                }
            }
        }

        if(best != -1){
        	requests.remove(Integer.valueOf(best));
        }
        return best;
    }

    private void moveTo(int floor){
        if (floor == curFloor){
        	return;
        }

        System.out.println("Moving from floor " + curFloor + " to floor " + floor);
        try{
            Thread.sleep(Math.abs(floor - curFloor) * 500L);//simulate time to move
        }catch (InterruptedException ignored) {}
        curFloor = floor;
        System.out.println("Arrived at floor " + curFloor);
    }

    public static void main(String[] args) throws InterruptedException{
        Elevator elevator = new Elevator(10);
        elevator.start();

        elevator.call(5);
        Thread.sleep(1000);
        elevator.call(2);
        Thread.sleep(1000);
        elevator.call(8);
    }
}
