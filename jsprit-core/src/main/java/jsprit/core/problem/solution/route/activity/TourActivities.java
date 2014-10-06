/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.problem.solution.route.activity;

import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.activity.TourActivity.JobActivity;

import java.util.*;



/**
 * 
 * @author stefan schroeder
 * 
 */

public class TourActivities {

	public static TourActivities copyOf(TourActivities tourActivities){
		return new TourActivities(tourActivities);
	}

    public static class ReverseActivityIterator implements Iterator<TourActivity> {

		private List<TourActivity> acts;		
		private int currentIndex;
		
		public ReverseActivityIterator(List<TourActivity> acts) {
			super();
			this.acts = acts;
			currentIndex = acts.size()-1;
		}

		@Override
		public boolean hasNext() {
			if(currentIndex >= 0) return true;
			return false;
		}

		@Override
		public TourActivity next() {
			TourActivity act = acts.get(currentIndex);
			currentIndex--;
			return act;
		}
		
		public void reset(){
			currentIndex = acts.size()-1;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();			
		}
	}
	

	public static TourActivities emptyTour(){
		return new TourActivities();
	}
	
	private final ArrayList<TourActivity> tourActivities = new ArrayList<TourActivity>();

	private final Set<Job> jobs = new HashSet<Job>();

    private final Set<TourActivity> activities = new HashSet<TourActivity>();

	private ReverseActivityIterator backward;
	
	private TourActivities(TourActivities tour2copy) {
		for (TourActivity tourAct : tour2copy.getActivities()) {
			TourActivity newAct = tourAct.duplicate();
			this.tourActivities.add(newAct);
            addToActivitySet(newAct);
            addJob(newAct);
		}
	}

    private void addToActivitySet(TourActivity newAct) {
        activities.add(newAct);
    }

    private void removeFromActivitySet(TourActivity act) {
        activities.remove(act);
    }

    public TourActivities(){
		
	}
	
	public List<TourActivity> getActivities() {
		return Collections.unmodifiableList(tourActivities);
	}
	
	public Iterator<TourActivity> iterator(){
		return tourActivities.iterator();
	}

	public boolean isEmpty() {
		return (tourActivities.size() == 0);
	}
	
	public Collection<Job> getJobs(){
		return Collections.unmodifiableSet(jobs);
	}
	
	/**
	 * @param job that needs to be looked up
	 * @return true if job is in jobList, otherwise false.
	 */
	public boolean servesJob(Job job) {
		return jobs.contains(job);
	}

	@Override
	public String toString() {
		return "[nuOfActivities="+tourActivities.size()+"]";
	}

	/**
	 * Removes job AND belonging activity from tour.
	 * 
	 * @param job to be removed
	 * @return true if job has been removed, otherwise false.
	 */
	public boolean removeJob(Job job){
		boolean jobRemoved = false;
		if(!jobs.contains(job)){
			return false;
		}
		else{
			jobRemoved = jobs.remove(job);
		}
		boolean activityRemoved = false;
		List<TourActivity> acts = new ArrayList<TourActivity>(tourActivities);
		for(TourActivity c : acts){
			if(c instanceof JobActivity){
				if(job.equals(((JobActivity) c).getJob())){
					tourActivities.remove(c);
                    removeFromActivitySet(c);
                    activityRemoved = true;
				}
			}
		}
		assert jobRemoved == activityRemoved : "job removed, but belonging activity not.";
		return activityRemoved;
	}

    /**
     * Returns true if this contains specified activity.
     *
     * @param activity to be looked up
     * @return true if this contains specified activity, false otherwise
     */
    public boolean hasActivity(TourActivity activity){
        return activities.contains(activity);
    }

    /**
     * Removes activity from this activity sequence. Removes its corresponding job as well, if there are no other activities
     * related to this job.
     *
     * @param activity to be removed
     * @return true if activity has been removed, false otherwise
     */
    public boolean removeActivity(TourActivity activity) {
        Job job = null;
        if(activity instanceof JobActivity){
            job = ((JobActivity) activity).getJob();
        }
        boolean jobIsAlsoAssociateToOtherActs = false;
        boolean actRemoved = false;
        List<TourActivity> acts = new ArrayList<TourActivity>(tourActivities);
        for(TourActivity act : acts){
            if(act == activity){
                tourActivities.remove(act);
                removeFromActivitySet(act);
                actRemoved = true;
            }
            else{
                if(act instanceof JobActivity && job != null){
                    if(((JobActivity) act).getJob().equals(job)){
                        jobIsAlsoAssociateToOtherActs = true;
                    }
                }
            }
        }
        if(!jobIsAlsoAssociateToOtherActs && actRemoved){
            jobs.remove(job);
        }
        return actRemoved;
    }



    /**
	 * Inserts the specified activity add the specified insertionIndex. Shifts the element currently at that position (if any) and 
	 * any subsequent elements to the right (adds one to their indices). 
	 * <p>If specified activity instanceof JobActivity, it adds job to jobList.
	 * <p>If insertionIndex > tourActivitiies.size(), it just adds the specified act at the end.
	 * 
	 * @param insertionIndex index where activity needs to be inserted
	 * @param act activity to be inserted
	 * @throws IndexOutOfBoundsException if insertionIndex < 0;
	 */
	public void addActivity(int insertionIndex, TourActivity act) {

		assert insertionIndex >= 0 : "insertionIndex < 0, this cannot be";

		/*
		 * if 1 --> between start and act(0) --> act(0)
		 * if 2 && 2 <= acts.size --> between act(0) and act(1) --> act(1)
		 * if 2 && 2 > acts.size --> at actEnd
		 * ...
		 * 
		 */
		if(insertionIndex < tourActivities.size()) {
            tourActivities.add(insertionIndex, act);
        }
		else if(insertionIndex >= tourActivities.size()) {
            tourActivities.add(act);
        }
        addToActivitySet(act);
        addJob(act);
	}
	
	/**
	 * Adds specified activity at the end of activity-list. 
	 * <p>If act instanceof JobActivity, it adds underlying job also.
	 * @throws IllegalStateException if activity-list already contains act.
	 * @param act to be added
	 */
	public void addActivity(TourActivity act){
		if(tourActivities.contains(act)) throw new IllegalStateException("act " + act + " already in tour. cannot add act twice.");
		tourActivities.add(act);
        addToActivitySet(act);
        addJob(act);
	}

	private void addJob(TourActivity act) {
		if(act instanceof JobActivity){
			Job job = ((JobActivity) act).getJob();
			jobs.add(job);
		}
	}

	/**
	 * Returns number of jobs assiciated to activities in this activity sequence.
	 * 
	 * @return no. of jobs
	 */
	public int jobSize() {
		return jobs.size();
	}

	public Iterator<TourActivity> reverseActivityIterator(){
		if(backward == null) backward = new ReverseActivityIterator(tourActivities);
		else backward.reset();
		return backward;
	}



}
