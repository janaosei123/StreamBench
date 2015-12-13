package fi.aalto.dmg.frame;

import backtype.storm.topology.TopologyBuilder;
import fi.aalto.dmg.frame.bolts.*;
import fi.aalto.dmg.frame.functions.*;
import fi.aalto.dmg.util.TimeDurations;
import org.apache.log4j.Logger;

/**
 * Created by yangjun.wang on 31/10/15.
 */
public class StormOperator<T> extends OperatorBase implements WorkloadOperator<T> {
    private static final long serialVersionUID = 3305991729931748598L;
    protected TopologyBuilder topologyBuilder;
    protected String preComponentId;

    public StormOperator(TopologyBuilder builder, String previousComponent){
        this.topologyBuilder = builder;
        this.preComponentId = previousComponent;
    }

    @Override
    public <R> WorkloadOperator<R> map(MapFunction<T, R> fun, String componentId, boolean logThroughput) {
        if(logThroughput) {
            topologyBuilder.setBolt(componentId, new MapBolt<>(fun, Logger.getLogger(componentId)))
                    .localOrShuffleGrouping(preComponentId);
        } else {
            topologyBuilder.setBolt(componentId, new MapBolt<>(fun))
                    .localOrShuffleGrouping(preComponentId);
        }
        return new StormOperator<>(topologyBuilder, componentId);
    }

    @Override
    public <R> WorkloadOperator<R> map(MapFunction<T, R> fun, String componentId) {
        return map(fun, componentId, false);
    }

    @Override
    public <K, V> PairWorkloadOperator<K, V> mapToPair(MapPairFunction<T, K, V> fun, String componentId, boolean logThroughput) {
        if(logThroughput) {
            topologyBuilder.setBolt(componentId, new MapToPairBolt<>(fun, Logger.getLogger(componentId)))
                    .localOrShuffleGrouping(preComponentId);
        } else {
            topologyBuilder.setBolt(componentId, new MapToPairBolt<>(fun))
                    .localOrShuffleGrouping(preComponentId);
        }
        return new StormPairOperator<>(topologyBuilder, componentId);
    }

    @Override
    public <K, V> PairWorkloadOperator<K, V> mapToPair(MapPairFunction<T, K, V> fun, String componentId) {
        return mapToPair(fun, componentId, false);
    }

    @Override
    public WorkloadOperator<T> reduce(ReduceFunction<T> fun, String componentId, boolean logThroughput) {
        if(logThroughput) {
            topologyBuilder.setBolt(componentId, new ReduceBolt<>(fun, Logger.getLogger(componentId)))
                    .localOrShuffleGrouping(preComponentId);

        } else {
            topologyBuilder.setBolt(componentId, new ReduceBolt<>(fun))
                    .localOrShuffleGrouping(preComponentId);
        }
        return new StormOperator<>(topologyBuilder, componentId);
    }

    @Override
    public WorkloadOperator<T> reduce(ReduceFunction<T> fun, String componentId) {
        return reduce(fun, componentId, false);
    }

    @Override
    public WorkloadOperator<T> filter(FilterFunction<T> fun, String componentId, boolean logThroughput) {
        if(logThroughput){
            topologyBuilder.setBolt(componentId, new FilterBolt<>(fun, Logger.getLogger(componentId)))
                    .localOrShuffleGrouping(preComponentId);
        } else {
            topologyBuilder.setBolt(componentId, new FilterBolt<>(fun))
                    .localOrShuffleGrouping(preComponentId);
        }
        return new StormOperator<>(topologyBuilder, componentId);
    }

    @Override
    public WorkloadOperator<T> filter(FilterFunction<T> fun, String componentId) {
        return  filter(fun, componentId, false);
    }

    @Override
    public WorkloadOperator<T> iterative(MapFunction<T, T> mapFunction, FilterFunction<T> iterativeFunction, String componentId) {
        topologyBuilder.setBolt(componentId, new IteractiveBolt<>(mapFunction, iterativeFunction))
                .localOrShuffleGrouping(preComponentId)
                .shuffleGrouping(componentId, IteractiveBolt.ITERATIVE_STREAM);
        return new StormOperator<>(topologyBuilder, componentId);
    }

    @Override
    public <R> WorkloadOperator<R> flatMap(FlatMapFunction<T, R> fun, String componentId, boolean logThroughput) {
        if(logThroughput) {
            topologyBuilder.setBolt(componentId, new FlatMapBolt<>(fun, Logger.getLogger(componentId)))
                    .localOrShuffleGrouping(preComponentId);
        } else {
            topologyBuilder.setBolt(componentId, new FlatMapBolt<>(fun))
                    .localOrShuffleGrouping(preComponentId);
        }
        return new StormOperator<>(topologyBuilder, componentId);
    }

    @Override
    public <R> WorkloadOperator<R> flatMap(FlatMapFunction<T, R> fun, String componentId) {
        return flatMap(fun, componentId, false);
    }

    @Override
    public WindowedWorkloadOperator<T> window(TimeDurations windowDuration) {
        return window(windowDuration, windowDuration);
    }

    @Override
    public WindowedWorkloadOperator<T> window(TimeDurations windowDuration, TimeDurations slideDuration) {
        return new StormWindowedOperator<T>(topologyBuilder, preComponentId, windowDuration, slideDuration);
    }

    @Override
    public void print() {
        topologyBuilder.setBolt("print", new PrintBolt<T>()).localOrShuffleGrouping(preComponentId);
    }

    @Override
    public void sink() {

    }
}
