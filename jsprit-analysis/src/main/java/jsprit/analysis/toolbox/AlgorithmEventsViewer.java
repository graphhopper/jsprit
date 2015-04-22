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

package jsprit.analysis.toolbox;

import org.graphstream.graph.Graph;
import org.graphstream.stream.Sink;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.ui.view.Viewer;

import java.io.IOException;

public class AlgorithmEventsViewer {

    private static class DelayContainer {

        long delay = 0;

    }

    public static class DelaySink implements Sink {

        private DelayContainer delayContainer;

        private long delay = 2;

        private long ruinDelay = 2;

        private long recreateDelay = 2;

        public DelaySink(DelayContainer delayContainer) {
            this.delayContainer = delayContainer;
        }

        public void setRuinDelay(long ruinDelay){
            this.ruinDelay = ruinDelay;
        }

        public void setRecreateDelay(long recreateDelay) {
            this.recreateDelay = recreateDelay;
        }

        public void setDelay(long delay) {
            this.delay = delay;
        }

        @Override
        public void graphAttributeAdded(String sourceId, long timeId, String attribute, Object value) {

        }

        @Override
        public void graphAttributeChanged(String sourceId, long timeId, String attribute, Object oldValue, Object newValue) {

        }

        @Override
        public void graphAttributeRemoved(String sourceId, long timeId, String attribute) {

        }

        @Override
        public void nodeAttributeAdded(String sourceId, long timeId, String nodeId, String attribute, Object value) {

        }

        @Override
        public void nodeAttributeChanged(String sourceId, long timeId, String nodeId, String attribute, Object oldValue, Object newValue) {

        }

        @Override
        public void nodeAttributeRemoved(String sourceId, long timeId, String nodeId, String attribute) {

        }

        @Override
        public void edgeAttributeAdded(String sourceId, long timeId, String edgeId, String attribute, Object value) {

        }

        @Override
        public void edgeAttributeChanged(String sourceId, long timeId, String edgeId, String attribute, Object oldValue, Object newValue) {

        }

        @Override
        public void edgeAttributeRemoved(String sourceId, long timeId, String edgeId, String attribute) {

        }

        @Override
        public void nodeAdded(String sourceId, long timeId, String nodeId) {

        }

        @Override
        public void nodeRemoved(String sourceId, long timeId, String nodeId) {

        }

        @Override
        public void edgeAdded(String sourceId, long timeId, String edgeId, String fromNodeId, String toNodeId, boolean directed) {

        }

        @Override
        public void edgeRemoved(String sourceId, long timeId, String edgeId) {

        }

        @Override
        public void graphCleared(String sourceId, long timeId) {

        }

        @Override
        public void stepBegins(String sourceId, long timeId, double step) {
            if(step == AlgorithmEventsRecorder.RECREATE) {
                delayContainer.delay = recreateDelay;
            }
            if(step == AlgorithmEventsRecorder.RUIN){
                delayContainer.delay = ruinDelay;
            }
            else if(step == AlgorithmEventsRecorder.CLEAR_SOLUTION){
                delayContainer.delay = delay;
            }
            else if(step == AlgorithmEventsRecorder.BEFORE_RUIN_RENDER_SOLUTION){
                delayContainer.delay = delay;
            }
        }
    }

    private double zoomFactor;

    private double scaling = 1.0;

    private long delayRecreation = 5;

    private long delayRuin = 5;

    private long delay = 2;

    public void setRecreationDelay(long delay_in_ms){
        this.delayRecreation = delay_in_ms;
    }

    public void setRuinDelay(long delay_in_ms){
        this.delayRuin = delay_in_ms;
    }

    public void display(String dgsFile){
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        Graph graph = GraphStreamViewer.createMultiGraph("g", GraphStreamViewer.StyleSheets.BLUE_FOREST);
        Viewer viewer = graph.display();
        viewer.disableAutoLayout();

        FileSource fs = new FileSourceDGS();
        fs.addSink(graph);

        DelayContainer delayContainer = new DelayContainer();
        DelaySink delaySink = new DelaySink(delayContainer);
        delaySink.setDelay(delay);
        delaySink.setRecreateDelay(delayRecreation);
        delaySink.setRuinDelay(delayRuin);
        fs.addSink(delaySink);

        try {
            fs.begin(dgsFile);
            while (fs.nextEvents()) {
                sleep(delayContainer.delay);
            }
        } catch( IOException e) {
            e.printStackTrace();
        }

        try {
            fs.end();
        } catch( IOException e) {
            e.printStackTrace();
        } finally {
            fs.removeSink(graph);
        }
    }

    public static void main(String[] args) throws IOException {
        AlgorithmEventsViewer viewer = new AlgorithmEventsViewer();
        viewer.setRuinDelay(10);
        viewer.setRecreationDelay(5);
        viewer.display("output/events.dgs.gz");
    }

    private static void sleep(long renderDelay_in_ms2) {
        try {
            Thread.sleep(renderDelay_in_ms2);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
