package com.graphhopper.jsprit.core.reporting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.job.AbstractJob;
import com.graphhopper.jsprit.core.problem.solution.route.activity.AbstractActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

import hu.vissy.texttable.column.ColumnDefinition;
import hu.vissy.texttable.contentformatter.CellContentFormatter;
import hu.vissy.texttable.dataconverter.DataConverter;
import hu.vissy.texttable.dataconverter.NumberDataConverter;
import hu.vissy.texttable.dataextractor.StatefulDataExtractor;


public class RouteDetailsConfig {

    private static final String[] PRIORITY_NAMES = new String[] { "", /* 1 */ "highest",
            /* 2 */ "very high", /* 3 */ "high", /* 4 */ "above medium", /* 5 */ "medium",
            /* 6 */ "below medium", /* 7 */ "low", /* 8 */ "very low", /* 9 */ "extreme low",
            /* 10 */ "lowest", };

    private static class SizeDimensionAggregator {
        SizeDimension size;
    }

    private static class PrevActivityHolder {
        TourActivity prevAct;
    }

    private static class CostAggregator {
        int cost;
        TourActivity prevAct;
    }

    private static final DataConverter<SizeDimension> SIZE_DIMENSION_CONVERTER = sd -> {
        if (sd != null)
            return IntStream.range(0, sd.getNuOfDimensions()).mapToObj(i -> "" + sd.get(i))
                    .collect(Collectors.joining(", ", "[", "]"));
        else
            return null;
    };


    public enum DisplayMode {
        NUMERIC {
            @Override
            List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> pickColumns(
                    ColumnDefinition<RouteDeatailsRecord, ?, ?> numeric,
                    ColumnDefinition<RouteDeatailsRecord, ?, ?> human) {
                return Collections.singletonList(numeric);
            }
        },
        HUMAN {
            @Override
            List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> pickColumns(
                    ColumnDefinition<RouteDeatailsRecord, ?, ?> numeric,
                    ColumnDefinition<RouteDeatailsRecord, ?, ?> human) {
                return Collections.singletonList(human);
            }
        },
        BOTH {
            @Override
            List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> pickColumns(
                    ColumnDefinition<RouteDeatailsRecord, ?, ?> numeric,
                    ColumnDefinition<RouteDeatailsRecord, ?, ?> human) {
                List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> res = new ArrayList<>();
                res.add(numeric);
                res.add(human);
                return res;
            }
        };

        abstract List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> pickColumns(
                ColumnDefinition<RouteDeatailsRecord, ?, ?> numeric,
                ColumnDefinition<RouteDeatailsRecord, ?, ?> human);
    }

    public enum Column {
        ROUTE_NUMBER {

            @Override
            public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                    RouteDetailsConfig routeDetailsConfig) {
                return Collections.singletonList(
                        new ColumnDefinition.StatelessBuilder<RouteDeatailsRecord, Integer>()
                        .withTitle("route")
                        .withDataExtractor(r -> r.getRoute().getId())
                        .withCellContentFormatter(CellContentFormatter.rightAlignedCell())
                        .withDataConverter(NumberDataConverter.defaultIntegerFormatter())
                        .build());
            }

        },
        VEHICLE_NAME {

            @Override
            public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                    RouteDetailsConfig routeDetailsConfig) {
                return Collections.singletonList(
                        new ColumnDefinition.StatelessBuilder<RouteDeatailsRecord, String>()
                        .withTitle("vehicle")
                        .withDataExtractor(r -> r.getRoute().getVehicle().getId()).build());
            }

        },
        ACTIVITY_TYPE {

            @Override
            public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                    RouteDetailsConfig routeDetailsConfig) {
                return Collections.singletonList(
                        new ColumnDefinition.StatelessBuilder<RouteDeatailsRecord, String>()
                        .withTitle("activity")
                        .withDataExtractor(
                                r -> ((AbstractActivity) r.getActivity()).getType())
                        .build());
            }

        },
        JOB_NAME {

            @Override
            public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                    RouteDetailsConfig routeDetailsConfig) {
                return Collections.singletonList(
                        new ColumnDefinition.StatelessBuilder<RouteDeatailsRecord, String>()
                        .withTitle("job name").withDataExtractor(r -> {
                            AbstractJob job = r.getJob();
                            return job == null ? null : job.getId();
                        })
                        .build());
            }

        },
        JOB_TYPE {

            @Override
            public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                    RouteDetailsConfig routeDetailsConfig) {
                return Collections.singletonList(
                        new ColumnDefinition.StatelessBuilder<RouteDeatailsRecord, String>()
                        .withTitle("job type").withDataExtractor(r -> {
                            AbstractJob job = r.getJob();
                            return job == null ? null : job.getClass().getSimpleName();
                        }).build());
            }

        },
        JOB_PRIORITY {

            @Override
            public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                    RouteDetailsConfig routeDetailsConfig) {
                Function<RouteDeatailsRecord, Integer> dataExtractorCallback = r -> {
                    AbstractJob job = r.getJob();
                    return job == null ? null : job.getPriority();
                };
                return routeDetailsConfig.displayMode.pickColumns(
                        new ColumnDefinition.StatelessBuilder<RouteDeatailsRecord, Integer>()
                        .withTitle("priority")
                        .withCellContentFormatter(CellContentFormatter.centeredCell())
                        .withDataExtractor(dataExtractorCallback)
                        .build(),
                        new ColumnDefinition.StatelessBuilder<RouteDeatailsRecord, Integer>()
                        .withTitle("priority (HR)")
                        .withCellContentFormatter(CellContentFormatter.centeredCell())
                        .withDataConverter(data -> data == null ? ""
                                : PRIORITY_NAMES[data] + "(" + data + ")")
                        .withDataExtractor(dataExtractorCallback).build()
                        );
            }
        },

        LOCATION {

            @Override
            public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                    RouteDetailsConfig routeDetailsConfig) {
                return Collections.singletonList(
                        new ColumnDefinition.StatelessBuilder<RouteDeatailsRecord, String>()
                        .withTitle("location").withDataExtractor(r -> {
                            TourActivity act = r.getActivity();
                            Location loc = act.getLocation();
                            return loc == null ? null : loc.getId();
                        }).build());
            }

        },
        LOAD_CHANGE {

            @Override
            public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                    RouteDetailsConfig routeDetailsConfig) {
                return Collections.singletonList(
                        new ColumnDefinition.StatelessBuilder<RouteDeatailsRecord,SizeDimension>()
                        .withTitle("load change")
                        .withDataConverter(SIZE_DIMENSION_CONVERTER)
                        .withDataExtractor(r -> {
                            TourActivity act = r.getActivity();
                            if (act instanceof Start)
                                return r.calculateInitialLoad();
                            else
                                return act.getLoadChange();
                        }).build());
            }

        },
        ROUTE_LOAD {

            @Override
            public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                    RouteDetailsConfig routeDetailsConfig) {
                return Collections.singletonList(
                        new ColumnDefinition.StatefulBuilder<RouteDeatailsRecord, SizeDimensionAggregator, SizeDimension>()
                        .withTitle("load").withDataConverter(SIZE_DIMENSION_CONVERTER)
                        .withDataExtractor(new StatefulDataExtractor<>((r, s) -> {
                            TourActivity act = r.getActivity();
                            if (act instanceof Start) {
                                s.size = r.calculateInitialLoad();
                            } else {
                                s.size = s.size.add(act.getLoadChange());
                            }
                            return s.size;
                        }, SizeDimensionAggregator::new, (s) -> null)).build());
            }

        },

        TIME_WINDOWS {

            @Override
            public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                    RouteDetailsConfig routeDetailsConfig) {
                Function<RouteDeatailsRecord, Collection<TimeWindow>> dataExtractorCallback = r -> {
                    TourActivity act = r.getActivity();
                    if (act instanceof JobActivity)
                        return ((JobActivity) act).getTimeWindows();
                    else
                        return null;
                };
                return routeDetailsConfig.displayMode.pickColumns(
                        new ColumnDefinition.StatelessBuilder<RouteDeatailsRecord, Collection<TimeWindow>>()
                        .withTitle("time windows").withDataConverter(
                                tws -> routeDetailsConfig.formatTimeWindowsNumeric(tws))
                        .withDataExtractor(dataExtractorCallback).build(),
                        new ColumnDefinition.StatelessBuilder<RouteDeatailsRecord, Collection<TimeWindow>>()
                        .withTitle("time windows (HR)").withDataConverter(
                                tws -> routeDetailsConfig.formatTimeWindowsHuman(tws))
                        .withDataExtractor(dataExtractorCallback).build()
                        );
            }

        },

        OPERATION_DURATION {
            @Override
            public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                    RouteDetailsConfig routeDetailsConfig) {
                return createTimeColumns(routeDetailsConfig, "opTime",
                        routeDetailsConfig.getDurationFormatter(), r -> {
                            TourActivity act = r.getActivity();
                            return (long) act.getOperationTime();
                        });
            }
        },

        TRAVEL_DURATION {
            @Override
            public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                    RouteDetailsConfig routeDetailsConfig) {
                return createStatefulDurationColumns(routeDetailsConfig, "travel",
                        new StatefulDataExtractor<RouteDeatailsRecord, PrevActivityHolder, Long>(
                                (r, s) -> {
                                    TourActivity act = r.getActivity();
                                    if (act instanceof Start) {
                                        s.prevAct = null;
                                    }
                                    long val = (long) (r
                                            .getTransportTime(s.prevAct));
                                    s.prevAct = act;
                                    return val;
                                }, PrevActivityHolder::new, (s) -> null));
            }
        },

        WAITING {
            @Override
            public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                    RouteDetailsConfig routeDetailsConfig) {
                return createTimeColumns(routeDetailsConfig, "waitng",
                        routeDetailsConfig.getDurationFormatter(), (r) -> {
                            TourActivity act = r.getActivity();
                            if (act instanceof Start || act instanceof End)
                                return null;
                            else
                                return (long) (act.getEndTime() - act.getOperationTime()
                                        - act.getArrTime());
                        });
            }
        },

        ACTIVITY_DURATION {
            @Override
            public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                    RouteDetailsConfig routeDetailsConfig) {
                return createStatefulDurationColumns(routeDetailsConfig, "duration",
                        new StatefulDataExtractor<RouteDeatailsRecord, PrevActivityHolder, Long>(
                                (r, s) -> {
                                    TourActivity act = r.getActivity();
                                    if (act instanceof Start) {
                                        s.prevAct = null;
                                    }
                                    long val = (long) (r.getTransportTime(s.prevAct)
                                            + act.getOperationTime());
                                    s.prevAct = act;
                                    return val;
                                }, PrevActivityHolder::new, (s) -> null));
            }
        },

        ARRIVAL_TIME {
            @Override
            public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                    RouteDetailsConfig routeDetailsConfig) {
                return createTimeColumns(routeDetailsConfig, "arrival",
                        routeDetailsConfig.getTimeFormatter(), r -> {
                            TourActivity act = r.getActivity();
                            if (act instanceof Start)
                                return null;
                            else
                                return (long) act.getArrTime();
                        });
            }
        },
        START_TIME {
            @Override
            public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                    RouteDetailsConfig routeDetailsConfig) {
                return createTimeColumns(routeDetailsConfig, "start",
                        routeDetailsConfig.getTimeFormatter(), r -> {
                            TourActivity act = r.getActivity();
                            if (act instanceof End)
                                return null;
                            else
                                return (long) (act.getEndTime() - act.getOperationTime());
                        });
            }
        },
        END_TIME {
            @Override
            public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                    RouteDetailsConfig routeDetailsConfig) {
                return createTimeColumns(routeDetailsConfig, "end",
                        routeDetailsConfig.getTimeFormatter(), r -> {
                            TourActivity act = r.getActivity();
                            if (act instanceof End)
                                return null;
                            else
                                return (long) act.getEndTime();
                        });
            }
        },

        SELECTED_TIME_WINDOW {
            @Override
            public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                    RouteDetailsConfig routeDetailsConfig) {
                Function<RouteDeatailsRecord, TimeWindow> dataExtractorCallback = r -> {
                    TourActivity act = r.getActivity();
                    if (act instanceof JobActivity) {
                        Optional<TimeWindow> optTw = ((JobActivity) act)
                                .getTimeWindows().stream()
                                .filter(tw -> tw.contains(
                                        act.getEndTime() - act.getOperationTime()))
                                .findAny();
                        if (optTw.isPresent())
                            return optTw.get();
                        else
                            return null;
                    } else
                        return null;
                };
                return routeDetailsConfig.displayMode.pickColumns(
                        new ColumnDefinition.StatelessBuilder<RouteDeatailsRecord, TimeWindow>()
                        .withTitle("selected tw")
                        .withDataConverter(
                                tw -> routeDetailsConfig.formatTimeWindowNumeric(tw))
                        .withDataExtractor(dataExtractorCallback).build(),
                        new ColumnDefinition.StatelessBuilder<RouteDeatailsRecord, TimeWindow>()
                        .withTitle("selected tw (HR)")
                        .withDataConverter(
                                tw -> routeDetailsConfig.formatTimeWindowHuman(tw))
                        .withDataExtractor(dataExtractorCallback).build());
            }
        },

        TRANSPORT_COST {
            @Override
            public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                    RouteDetailsConfig routeDetailsConfig) {
                return createStatefulCostColumns(routeDetailsConfig, "transCost",
                        new StatefulDataExtractor<RouteDeatailsRecord, PrevActivityHolder, Integer>(
                                (r, s) -> {
                                    TourActivity act = r.getActivity();
                                    if (act instanceof Start) {
                                        s.prevAct = null;
                                    }
                                    double res = r.getTransportCost(s.prevAct);
                                    s.prevAct = act;
                                    return (int) res;
                                }, PrevActivityHolder::new, (s) -> null));
            }
        },


        ACTIVITY_COST {
            @Override
            public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                    RouteDetailsConfig routeDetailsConfig) {
                return Collections.singletonList(
                        new ColumnDefinition.StatelessBuilder<RouteDeatailsRecord,Integer>()
                        .withTitle("actCost")
                        .withDataExtractor(r -> (int) r.getActivityCost()).build());
            }
        },

        ROUTE_COST {
            @Override
            public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                    RouteDetailsConfig routeDetailsConfig) {
                return Collections.singletonList(
                        new ColumnDefinition.StatefulBuilder<RouteDeatailsRecord, CostAggregator, Integer>()
                        .withTitle("routeCost")
                        .withCellContentFormatter(CellContentFormatter.rightAlignedCell())
                        .withDataExtractor(new StatefulDataExtractor<RouteDeatailsRecord, CostAggregator, Integer>(
                                (r, s) -> {
                                    TourActivity act = r.getActivity();
                                    if (act instanceof Start) {
                                        s.prevAct = null;
                                        s.cost = 0;
                                    }

                                    Double trCost = r.getTransportCost(s.prevAct);
                                    s.prevAct = act;
                                    if (trCost != null) {
                                        s.cost += trCost;
                                    }
                                    s.cost += r.getActivityCost();
                                    return s.cost;
                                }, CostAggregator::new, (s) -> null)
                                ).build());
            }
        },
        ;


        public abstract List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createColumns(
                RouteDetailsConfig routeDetailsConfig);

        private static List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createTimeColumns(
                RouteDetailsConfig routeDetailsConfig, String title, DataConverter<Long> converter,
                Function<RouteDeatailsRecord, Long> getter) {
            return routeDetailsConfig.displayMode.pickColumns(
                    new ColumnDefinition.StatelessBuilder<RouteDeatailsRecord, Long>()
                    .withTitle(title).withDataExtractor(getter)
                    .withCellContentFormatter(CellContentFormatter.rightAlignedCell())
                    .build(),
                    new ColumnDefinition.StatelessBuilder<RouteDeatailsRecord, Long>()
                    .withTitle(title + " (HR)")
                    .withCellContentFormatter(CellContentFormatter.rightAlignedCell())
                    .withDataConverter(converter)
                    .withDataExtractor(getter).build());
        }


        private static List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createStatefulDurationColumns(
                RouteDetailsConfig routeDetailsConfig, String title,
                StatefulDataExtractor<RouteDeatailsRecord, PrevActivityHolder, Long> getter) {
            return routeDetailsConfig.displayMode.pickColumns(
                    new ColumnDefinition.StatefulBuilder<RouteDeatailsRecord, PrevActivityHolder, Long>()
                    .withTitle(title)
                    .withCellContentFormatter(CellContentFormatter.rightAlignedCell())
                    .withDataExtractor(getter)
                    .build(),
                    new ColumnDefinition.StatefulBuilder<RouteDeatailsRecord, PrevActivityHolder, Long>()
                    .withTitle(title+" (HR)")
                    .withCellContentFormatter(CellContentFormatter.rightAlignedCell())
                    .withDataConverter(dur -> routeDetailsConfig.formatDurationHuman(dur))
                    .withDataExtractor(getter)
                    .build());
        }

        private static List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> createStatefulCostColumns(
                RouteDetailsConfig routeDetailsConfig, String title,
                StatefulDataExtractor<RouteDeatailsRecord, PrevActivityHolder, Integer> getter) {
            return Collections.singletonList(
                    new ColumnDefinition.StatefulBuilder<RouteDeatailsRecord, PrevActivityHolder, Integer>()
                    .withTitle(title)
                    .withCellContentFormatter(CellContentFormatter.rightAlignedCell())
                    .withDataExtractor(getter).build());
        }
    }

    public static class Builder {
        private LocalDateTime humanReadableOrigin = LocalDateTime.of(LocalDate.now(),
                LocalTime.MIDNIGHT);
        private DisplayMode displayMode = DisplayMode.NUMERIC;
        private List<Column> columns;
        private ChronoUnit lowUnit = ChronoUnit.SECONDS;
        private ChronoUnit highUnit = ChronoUnit.HOURS;

        public Builder() {
            this.columns = new ArrayList<>();

        }

        public Builder withHumanReadableOrigin(LocalDateTime humanReadableOrigin) {
            this.humanReadableOrigin = humanReadableOrigin;
            return this;
        }

        public Builder withTimeDisplayMode(DisplayMode displayMode) {
            this.displayMode = displayMode;
            return this;
        }

        public Builder withLowUnit(ChronoUnit lowUnit) {
            this.lowUnit = lowUnit;
            return this;
        }

        public Builder withHighUnit(ChronoUnit highUnit) {
            this.highUnit = highUnit;
            return this;
        }

        public Builder withColumn(Column columns) {
            this.columns.add(columns);
            return this;
        }

        public Builder withColumns(Column... columns) {
            for (Column c : columns) {
                withColumn(c);
            }
            return this;
        }


        public RouteDetailsConfig build() {
            return new RouteDetailsConfig(this);
        }
    }

    private DisplayMode displayMode;
    private LocalDateTime humanReadableOrigin;
    private ChronoUnit lowUnit;
    private ChronoUnit highUnit;
    private List<Column> columns;

    private HumanReadableTimeFormatter timeFormatter;
    private HumanReadableDurationFormatter durationFormatter;

    private RouteDetailsConfig(Builder builder) {
        this.humanReadableOrigin = builder.humanReadableOrigin;
        this.displayMode = builder.displayMode;
        this.columns = builder.columns;
        this.lowUnit = builder.lowUnit;
        this.highUnit = builder.highUnit;
        timeFormatter = new HumanReadableTimeFormatter(humanReadableOrigin, lowUnit);
        durationFormatter = new HumanReadableDurationFormatter(lowUnit, highUnit);
    }




    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public LocalDateTime getHumanReadableOrigin() {
        return humanReadableOrigin;
    }

    public ChronoUnit getLowUnit() {
        return lowUnit;
    }

    public ChronoUnit getHighUnit() {
        return highUnit;
    }

    public HumanReadableTimeFormatter getTimeFormatter() {
        return timeFormatter;
    }

    public HumanReadableDurationFormatter getDurationFormatter() {
        return durationFormatter;
    }

    public List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> getColumns() {
        List<ColumnDefinition<RouteDeatailsRecord, ?, ?>> columns = new ArrayList<>();
        this.columns.forEach(c -> columns.addAll(c.createColumns(this)));
        return columns;
    }

    protected String formatTimeWindowsNumeric(Collection<TimeWindow> timeWindows) {
        if (timeWindows == null || timeWindows.isEmpty())
            return "";
        return timeWindows.stream().map(tw -> formatTimeWindowNumeric(tw))
                .collect(Collectors.joining());
    }

    protected String formatTimeWindowsHuman(Collection<TimeWindow> timeWindows) {
        if (timeWindows == null || timeWindows.isEmpty())
            return "";
        return timeWindows.stream().map(tw -> formatTimeWindowHuman(tw))
                .collect(Collectors.joining());
    }

    private String formatTimeWindowNumeric(TimeWindow tw) {
        String res = "";
        if (tw != null) {
            res = "[" + (long) tw.getStart() + "-";
            if (tw.getEnd() == Double.MAX_VALUE) {
                res += "";
            } else {
                res += (long) tw.getEnd();
            }
            res += "]";
        }
        return res;
    }

    private String formatTimeWindowHuman(TimeWindow tw) {
        String res = "";
        if (tw != null) {
            res = "[" + timeFormatter.convert((long) tw.getStart()) + "-";
            if (tw.getEnd() == Double.MAX_VALUE) {
                res += "";
            } else {
                res += timeFormatter.convert((long) tw.getEnd());
            }
            res += "]";
        }
        return res;
    }

    protected String formatDurationHuman(Long data) {
        return durationFormatter.convert(data);
    }

    protected String formatTimeHuman(Long data) {
        return timeFormatter.convert(data);
    }

}