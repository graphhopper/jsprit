/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.core.algorithm.listener;

import com.graphhopper.jsprit.core.algorithm.SearchStrategy;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;

import java.util.*;


public class VehicleRoutingAlgorithmListeners {

    public static class PrioritizedVRAListener {

        Priority priority;
        VehicleRoutingAlgorithmListener l;

        public PrioritizedVRAListener(Priority priority, VehicleRoutingAlgorithmListener l) {
            super();
            this.priority = priority;
            this.l = l;
        }

        public Priority getPriority() {
            return priority;
        }

        public VehicleRoutingAlgorithmListener getListener() {
            return l;
        }

    }

    public enum Priority {
        HIGH, MEDIUM, LOW
    }


    private TreeSet<PrioritizedVRAListener> algorithmListeners = new TreeSet<PrioritizedVRAListener>(new Comparator<PrioritizedVRAListener>() {

        @Override
        public int compare(PrioritizedVRAListener o1, PrioritizedVRAListener o2) {
            if (o1 == o2) return 0;
            if (o1.getPriority() == Priority.HIGH && o2.getPriority() != Priority.HIGH) {
                return -1;
            } else if (o2.getPriority() == Priority.HIGH && o1.getPriority() != Priority.HIGH) {
                return 1;
            } else if (o1.getPriority() == Priority.MEDIUM && o2.getPriority() != Priority.MEDIUM) {
                return -1;
            } else if (o2.getPriority() == Priority.MEDIUM && o1.getPriority() != Priority.MEDIUM) {
                return 1;
            }
            return 1;
        }
    });


    public Collection<VehicleRoutingAlgorithmListener> getAlgorithmListeners() {
        List<VehicleRoutingAlgorithmListener> list = new ArrayList<VehicleRoutingAlgorithmListener>();
        for (PrioritizedVRAListener l : algorithmListeners) {
            list.add(l.getListener());
        }
        return Collections.unmodifiableCollection(list);
    }

    public void remove(PrioritizedVRAListener listener) {
        boolean removed = algorithmListeners.remove(listener);
        if (!removed) {
            throw new IllegalStateException("cannot remove listener");
        }
    }

    public void addListener(VehicleRoutingAlgorithmListener listener, Priority priority) {
        algorithmListeners.add(new PrioritizedVRAListener(priority, listener));
    }

    public void addListener(VehicleRoutingAlgorithmListener listener) {
        addListener(listener, Priority.LOW);
    }

    public void algorithmEnds(VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        for (PrioritizedVRAListener l : algorithmListeners) {
            if (l.getListener() instanceof AlgorithmEndsListener) {
                ((AlgorithmEndsListener) l.getListener()).informAlgorithmEnds(problem, solutions);
            }
        }

    }

    public void iterationEnds(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        for (PrioritizedVRAListener l : algorithmListeners) {
            if (l.getListener() instanceof IterationEndsListener) {
                ((IterationEndsListener) l.getListener()).informIterationEnds(i, problem, solutions);
            }
        }
    }


    public void iterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        for (PrioritizedVRAListener l : algorithmListeners) {
            if (l.getListener() instanceof IterationStartsListener) {
                ((IterationStartsListener) l.getListener()).informIterationStarts(i, problem, solutions);
            }
        }
    }


    public void algorithmStarts(VehicleRoutingProblem problem, VehicleRoutingAlgorithm algorithm, Collection<VehicleRoutingProblemSolution> solutions) {
        for (PrioritizedVRAListener l : algorithmListeners) {
            if (l.getListener() instanceof AlgorithmStartsListener) {
                ((AlgorithmStartsListener) l.getListener()).informAlgorithmStarts(problem, algorithm, solutions);
            }
        }
    }

    public void add(PrioritizedVRAListener l) {
        algorithmListeners.add(l);
    }

    public void addAll(Collection<PrioritizedVRAListener> algorithmListeners) {
        for (PrioritizedVRAListener l : algorithmListeners) {
            this.algorithmListeners.add(l);
        }
    }

    public void selectedStrategy(SearchStrategy.DiscoveredSolution discoveredSolution, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        for (PrioritizedVRAListener l : algorithmListeners) {
            if (l.getListener() instanceof StrategySelectedListener) {
                ((StrategySelectedListener) l.getListener()).informSelectedStrategy(discoveredSolution, problem, solutions);
            }
        }
    }
}
