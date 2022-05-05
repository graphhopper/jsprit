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
package com.graphhopper.jsprit.instance.util;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.util.BenchmarkInstance;
import com.graphhopper.jsprit.instance.reader.ChristofidesReader;
import com.graphhopper.jsprit.instance.reader.CordeauReader;
import com.graphhopper.jsprit.instance.reader.SolomonReader;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class Instances {

    /**
     * Returns a collection of {@link BenchmarkInstance} which are Cordeau's p instances.
     * <p>Note that this assumes that within the folder 'inputFolder' 23 p-instances are located with their original name, i.e. p01,p02,...,p23.
     * <p>It also assumes that solution files are also located in inputFolder ending with .res
     *
     * @param inputFolder where cordeau's p instances are located. It must end without '/' such as instances/cordeau.
     * @return a collection of {@link BenchmarkInstance}
     */
    public static Collection<BenchmarkInstance> getAllCordeauP(String inputFolder) {
        Collection<BenchmarkInstance> instances = new ArrayList<BenchmarkInstance>();
        for (int i = 0; i < 23; i++) {
            VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
            String file = inputFolder + "/p" + getInstanceNu(i + 1);
            new CordeauReader(builder).read(file);
            VehicleRoutingProblem p = builder.build();
            instances.add(new BenchmarkInstance("p" + getInstanceNu(i + 1), p, getBestKnown(file), null));
        }
        return instances;
    }


    private static double getBestKnown(String file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(file + ".res")));
            String first = reader.readLine();
            Double result = Double.valueOf(first);
            reader.close();
            return result;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static String getInstanceNu(int i) {
        if (i < 10) return "0" + i;
        return "" + i;
    }

    /**
     * Returns a collection of {@link BenchmarkInstance} which are Cordeau's pr instances.
     * <p>Note that this assumes that within the folder 'inputFolder' 10 p-instances are located with their original name, i.e. pr01,pr02,...,pr10.
     * <p>It also assumes that solution files are also located in inputFolder ending with .res
     *
     * @param inputFolder
     * @param inputFolder where cordeau's pr instances are located. It must end without '/' such as instances/cordeau.
     * @return a collection of {@link BenchmarkInstance}
     */
    public static Collection<BenchmarkInstance> getAllCordeauPR(String inputFolder) {
        Collection<BenchmarkInstance> instances = new ArrayList<BenchmarkInstance>();
        for (int i = 0; i < 10; i++) {
            VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
            String file = inputFolder + "/pr" + getInstanceNu(i + 1);
            new CordeauReader(builder).read(file);
            VehicleRoutingProblem p = builder.build();
            instances.add(new BenchmarkInstance("pr" + getInstanceNu(i + 1), p, getBestKnown(file), null));
        }
        return instances;
    }

    /**
     * Returns a collection of {@link BenchmarkInstance} which are Christofides vrpnc instances.
     * <p>Note that this assumes that within the folder 'inputFolder' 14 vrpnc-instances are located with their original name, i.e. vrpnc1,vrpnc2,...,vrpnc14.
     *
     * @param inputFolder where christofides vrpnc instances are located. It must end without '/' such as instances/christofides.
     * @return a collection of {@link BenchmarkInstance}
     */
    public static Collection<BenchmarkInstance> getAllChristofides(String inputFolder) {
        List<Double> bestKnown = Arrays.asList(524.61, 835.26, 826.14, 1028.42, 1291.29, 555.43, 909.68, 865.49, 1162.55, 1395.85, 1042.11, 819.56, 1541.14, 866.37);
        Collection<BenchmarkInstance> instances = new ArrayList<BenchmarkInstance>();
        for (int i = 0; i < 14; i++) {
            VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
            String file = inputFolder + "/vrpnc" + (i + 1) + ".txt";
            new ChristofidesReader(builder).read(file);
            VehicleRoutingProblem p = builder.build();
            instances.add(new BenchmarkInstance("vrpnc" + getInstanceNu(i + 1), p, bestKnown.get(i).doubleValue(), null));
        }
        return instances;
    }

    /**
     * Returns a collection of {@link BenchmarkInstance} which are Solomon instances.
     * <p>Note that this assumes that within the folder 'inputFolder' 9 C1-instances are located with their original name, i.e. C101.txt,C102.txt,...,C109.txt.
     * <p>Note that unlike the original problems, a fixed-cost value of 1000 is set for each employed vehicle.
     *
     * @param inputFolder where solomon C1 instances are located. It must end without '/' such as instances/solomon.
     * @return a collection of {@link BenchmarkInstance}
     */
    public static Collection<BenchmarkInstance> getAllSolomonC1(String inputFolder) {
        List<Double> bestKnown = Arrays.asList(828.94, 828.94, 828.06, 824.78, 828.94, 828.94, 828.94, 828.94, 828.94);
        List<Double> bestKnowVehicles = Arrays.asList(10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0);
        Collection<BenchmarkInstance> instances = new ArrayList<BenchmarkInstance>();
        for (int i = 0; i < 9; i++) {
            VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
            String file = inputFolder + "/C1" + getInstanceNu(i + 1) + ".txt";
            new SolomonReader(builder).read(file);
            VehicleRoutingProblem p = builder.build();
            instances.add(new BenchmarkInstance("C1" + getInstanceNu(i + 1), p, bestKnown.get(i).doubleValue(), bestKnowVehicles.get(i).doubleValue()));
        }
        return instances;
    }

    /**
     * Returns a collection of {@link BenchmarkInstance} which are Solomon instances.
     * <p>Note that this assumes that within the folder 'inputFolder' 8 C2-instances are located with their original name, i.e. C201.txt,C202.txt,...,C208.txt.
     * <p>Note that unlike the original problems, a fixed-cost value of 1000 is set for each employed vehicle.
     *
     * @param inputFolder where solomon C2 instances are located. It must end without '/' such as instances/solomon.
     * @return a collection of {@link BenchmarkInstance}
     */
    public static Collection<BenchmarkInstance> getAllSolomonC2(String inputFolder) {
        List<Double> bestKnown = Arrays.asList(591.56, 591.56, 591.17, 590.60, 588.88, 588.49, 588.29, 588.32);
        List<Double> bestKnowVehicles = Arrays.asList(3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0);
        Collection<BenchmarkInstance> instances = new ArrayList<BenchmarkInstance>();
        for (int i = 0; i < 8; i++) {
            VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
            String file = inputFolder + "/C2" + getInstanceNu(i + 1) + ".txt";
            new SolomonReader(builder).read(file);
            VehicleRoutingProblem p = builder.build();
            instances.add(new BenchmarkInstance("C2" + getInstanceNu(i + 1), p, bestKnown.get(i).doubleValue(), bestKnowVehicles.get(i).doubleValue()));
        }
        return instances;
    }

    /**
     * Returns a collection of {@link BenchmarkInstance} which are Solomon instances.
     * <p>Note that this assumes that within the folder 'inputFolder' 12 R1-instances are located with their original name, i.e. R101.txt,R102.txt,...,R112.txt.
     * <p>Note that unlike the original problems, a fixed-cost value of 1000 is set for each employed vehicle.
     *
     * @param inputFolder where solomon R1 instances are located. It must end without '/' such as instances/solomon.
     * @return a collection of {@link BenchmarkInstance}
     */
    public static Collection<BenchmarkInstance> getAllSolomonR1(String inputFolder) {
        List<Double> bestKnown = Arrays.asList(1650.80, 1486.12, 1292.68, 1007.31, 1377.11, 1252.03, 1104.66, 960.88, 1194.73, 1118.84, 1096.72, 982.14);
        List<Double> bestKnowVehicles = Arrays.asList(19.0, 17.0, 13.0, 9.0, 14.0, 12.0, 10.0, 9.0, 11.0, 10.0, 10.0, 9.0);
        Collection<BenchmarkInstance> instances = new ArrayList<BenchmarkInstance>();
        for (int i = 0; i < 12; i++) {
            VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
            String file = inputFolder + "/R1" + getInstanceNu(i + 1) + ".txt";
            new SolomonReader(builder).read(file);
            VehicleRoutingProblem p = builder.build();
            instances.add(new BenchmarkInstance("R1" + getInstanceNu(i + 1), p, bestKnown.get(i).doubleValue(), bestKnowVehicles.get(i).doubleValue()));
        }
        return instances;
    }

    /**
     * Returns a collection of {@link BenchmarkInstance} which are Solomon instances.
     * <p>Note that this assumes that within the folder 'inputFolder' 11 R1-instances are located with their original name, i.e. R201.txt,R202.txt,...,R111.txt.
     * <p>Note that unlike the original problems, a fixed-cost value of 1000 is set for each employed vehicle.
     *
     * @param inputFolder
     * @param inputFolder where solomon R2 instances are located. It must end without '/' such as instances/solomon.
     * @return a collection of {@link BenchmarkInstance}
     */
    public static Collection<BenchmarkInstance> getAllSolomonR2(String inputFolder) {
        List<Double> bestKnown = Arrays.asList(1252.37, 1191.70, 939.50, 825.52, 994.42, 906.14, 890.61, 726.82, 909.16, 939.37, 885.71);
        List<Double> bestKnowVehicles = Arrays.asList(4.0, 3.0, 3.0, 2.0, 3.0, 3.0, 2.0, 2.0, 3.0, 3.0, 2.0);
        Collection<BenchmarkInstance> instances = new ArrayList<BenchmarkInstance>();
        for (int i = 0; i < 11; i++) {
            VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
            String file = inputFolder + "/R2" + getInstanceNu(i + 1) + ".txt";
            new SolomonReader(builder).read(file);
            VehicleRoutingProblem p = builder.build();
            instances.add(new BenchmarkInstance("R2" + getInstanceNu(i + 1), p, bestKnown.get(i).doubleValue(), bestKnowVehicles.get(i).doubleValue()));
        }
        return instances;
    }

    /**
     * Returns a collection of {@link BenchmarkInstance} which are Solomon instances.
     * <p>Note that this assumes that within the folder 'inputFolder' 8 RC1-instances are located with their original name, i.e. RC101.txt,RC102.txt,...,RC108.txt.
     * <p>Note that unlike the original problems, a fixed-cost value of 1000 is set for each employed vehicle.
     *
     * @param inputFolder where solomon RC1 instances are located. It must end without '/' such as instances/solomon.
     * @return a collection of {@link BenchmarkInstance}
     */
    public static Collection<BenchmarkInstance> getAllSolomonRC1(String inputFolder) {
        List<Double> bestKnown = Arrays.asList(1696.94, 1554.75, 1261.67, 1135.48, 1629.44, 1424.73, 1230.48, 1139.82);
        List<Double> bestKnowVehicles = Arrays.asList(14.0, 12.0, 11.0, 10.0, 13.0, 11.0, 11.0, 10.0);
        Collection<BenchmarkInstance> instances = new ArrayList<BenchmarkInstance>();
        for (int i = 0; i < 8; i++) {
            VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
            String file = inputFolder + "/RC1" + getInstanceNu(i + 1) + ".txt";
            new SolomonReader(builder).read(file);
            VehicleRoutingProblem p = builder.build();
            instances.add(new BenchmarkInstance("RC1" + getInstanceNu(i + 1), p, bestKnown.get(i).doubleValue(), bestKnowVehicles.get(i).doubleValue()));
        }
        return instances;
    }

    /**
     * Returns a collection of {@link BenchmarkInstance} which are Solomon instances.
     * <p>Note that this assumes that within the folder 'inputFolder' 8 RC2-instances are located with their original name, i.e. RC201.txt,RC202.txt,...,RC208.txt.
     * <p>Note that unlike the original problems, a fixed-cost value of 1000 is set for each employed vehicle.
     *
     * @param inputFolder
     * @param inputFolder where solomon RC2 instances are located. It must end without '/' such as instances/solomon.
     * @return a collection of {@link BenchmarkInstance}
     */
    public static Collection<BenchmarkInstance> getAllSolomonRC2(String inputFolder) {
        List<Double> bestKnown = Arrays.asList(1406.94, 1365.65, 1049.62, 798.46, 1297.65, 1146.32, 1061.14, 828.14);
        List<Double> bestKnowVehicles = Arrays.asList(4.0, 3.0, 3.0, 3.0, 4.0, 3.0, 3.0, 3.0);
        Collection<BenchmarkInstance> instances = new ArrayList<BenchmarkInstance>();
        for (int i = 0; i < 8; i++) {
            VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
            String file = inputFolder + "/RC2" + getInstanceNu(i + 1) + ".txt";
            new SolomonReader(builder).read(file);
            VehicleRoutingProblem p = builder.build();
            instances.add(new BenchmarkInstance("RC2" + getInstanceNu(i + 1), p, bestKnown.get(i).doubleValue(), bestKnowVehicles.get(i).doubleValue()));
        }
        return instances;
    }
}
