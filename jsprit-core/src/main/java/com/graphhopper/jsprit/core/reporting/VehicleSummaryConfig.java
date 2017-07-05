package com.graphhopper.jsprit.core.reporting;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;

import hu.vissy.texttable.column.ColumnDefinition;
import hu.vissy.texttable.contentformatter.CellContentFormatter;
import hu.vissy.texttable.dataconverter.NumberDataConverter;
import hu.vissy.texttable.dataconverter.StringDataConverter;


public class VehicleSummaryConfig extends ColumnConfigBase {

    public static final EnumSet<DisplayMode> MODE_SET_HUMAN = EnumSet.<DisplayMode>of(
            DisplayMode.NUMERIC, DisplayMode.HUMAN_READABLE);

    public static final EnumSet<DisplayMode> MODE_SET_ALL = EnumSet.<DisplayMode>of(
            DisplayMode.NUMERIC, DisplayMode.HUMAN_READABLE, DisplayMode.PERCENT_ROUTE,
            DisplayMode.PERCENT_SHIFT);

    public enum DisplayMode {
        GENERIC(""), NUMERIC(""), HUMAN_READABLE(" (H)"), PERCENT_ROUTE(" (R%)"), PERCENT_SHIFT(
                " (S%)");

        private String postfix;

        private DisplayMode(String postfix) {
            this.postfix = postfix;
        }

        public String getPostfix() {
            return postfix;
        }
    }

    public enum Column {
        ROUTE_NUMBER(null) {
            @Override
            public List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> createColumns(
                    VehicleSummaryConfig vehicleSummaryConfig) {
                return Collections.singletonList(
                        new ColumnDefinition.StatelessBuilder<VehicleSummaryRecord, Integer>()
                        .withTitle("route #")
                        .withDataExtractor(r -> r.getRouteNr())
                        .withCellContentFormatter(CellContentFormatter.rightAlignedCell())
                        .withDataConverter(NumberDataConverter.defaultIntegerFormatter())
                        .build());
            }
        },

        VEHICLE_NAME(null) {
            @Override
            public List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> createColumns(
                    VehicleSummaryConfig vehicleSummaryConfig) {
                return Collections.singletonList(
                        new ColumnDefinition.StatelessBuilder<VehicleSummaryRecord, String>()
                        .withTitle("vehicle")
                        .withDataExtractor(r -> r.getVehicle().getId())
                        .withDataConverter(new StringDataConverter())
                        .build());
            }
        },

        VEHICLE_TYPE(null) {
            @Override
            public List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> createColumns(
                    VehicleSummaryConfig vehicleSummaryConfig) {
                return Collections.singletonList(
                        new ColumnDefinition.StatelessBuilder<VehicleSummaryRecord, String>()
                        .withTitle("vehicle type")
                        .withDataExtractor(r -> r.getVehicle().getType().getTypeId())
                        .withDataConverter(new StringDataConverter())
                        .build());
            }
        },

        DRIVER(null) {
            @Override
            public List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> createColumns(
                    VehicleSummaryConfig vehicleSummaryConfig) {
                return Collections.singletonList(
                        new ColumnDefinition.StatelessBuilder<VehicleSummaryRecord, String>()
                        .withTitle("driver")
                        .withDataExtractor(r -> r.getDriver().getId())
                        .withDataConverter(new StringDataConverter())
                        .build());
            }
        },

        ACTIVITY_COUNT(null) {
            @Override
            public List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> createColumns(
                    VehicleSummaryConfig vehicleSummaryConfig) {
                return Collections.singletonList(
                        new ColumnDefinition.StatelessBuilder<VehicleSummaryRecord, Integer>()
                        .withTitle("act count")
                        .withDataExtractor(r -> r.getActivityCount())
                        .withCellContentFormatter(CellContentFormatter.rightAlignedCell())
                        .withDataConverter(NumberDataConverter.defaultIntegerFormatter())
                        .build());
            }
        },

        ACTIVITY_COUNT_BY_TYPE(null) {
            @Override
            public List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> createColumns(
                    VehicleSummaryConfig vehicleSummaryConfig) {
                return Collections.singletonList(
                        new ColumnDefinition.StatelessBuilder<VehicleSummaryRecord, Map<String, Integer>>()
                        .withTitle("act stat")
                        .withDataExtractor(r -> r.getActivityCountByType())
                        .withDataConverter(d -> d.entrySet().stream()
                                .map(en -> "[" + en.getKey() + "=" + en.getValue() + "]")
                                .collect(Collectors.joining()))
                        .build());
            }
        },

        TRAVEL_DISTANCE(null) {
            @Override
            public List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> createColumns(
                    VehicleSummaryConfig vehicleSummaryConfig) {
                return Collections.singletonList(
                        new ColumnDefinition.StatelessBuilder<VehicleSummaryRecord, Long>()
                            .withTitle("travel dist")
                            .withDataExtractor(r -> r.getTravelDistance())
                            .withCellContentFormatter(CellContentFormatter.rightAlignedCell())
                            .withDataConverter(NumberDataConverter.defaultLongFormatter())
                            .build());
            }
        },

        SHIFT_TIME_WINDOW(MODE_SET_HUMAN) {

            @Override
            public List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> createColumns(
                    VehicleSummaryConfig vehicleSummaryConfig) {
                return createTimeWindowColumn(this, "shift tw", vehicleSummaryConfig,
                        r -> new TimeWindow(r.getVehicle().getEarliestDeparture(),
                                r.getVehicle().getLatestArrival()));
            }
        },

        SHIFT_DURATION(MODE_SET_HUMAN) {

            @Override
            public List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> createColumns(
                    VehicleSummaryConfig vehicleSummaryConfig) {
                return createDurationColumn(this, "shift dur", vehicleSummaryConfig,
                        r -> r.getShiftDuration());
            }
        },

        ROUTE_TIME_WINDOW(MODE_SET_HUMAN) {

            @Override
            public List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> createColumns(
                    VehicleSummaryConfig vehicleSummaryConfig) {
                return createTimeWindowColumn(this, "route tw", vehicleSummaryConfig,
                        r -> new TimeWindow(r.getStart(), r.getEnd()));
            }
        },

        ROUTE_DURATION(MODE_SET_ALL) {

            @Override
            public List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> createColumns(
                    VehicleSummaryConfig vehicleSummaryConfig) {
                return createDurationColumn(this, "route", vehicleSummaryConfig,
                        r -> r.getRouteDuration());
            }
        },

        TRAVEL_DURATION(MODE_SET_ALL) {

            @Override
            public List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> createColumns(
                    VehicleSummaryConfig vehicleSummaryConfig) {
                return createDurationColumn(this, "travel", vehicleSummaryConfig,
                        r -> r.getTravelDuration());
            }
        },

        OPERATION_DURATION(MODE_SET_ALL) {

            @Override
            public List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> createColumns(
                    VehicleSummaryConfig vehicleSummaryConfig) {
                return createDurationColumn(this, "operation", vehicleSummaryConfig,
                        r -> r.getOperationDuration());
            }
        },

        ACTIVE_DURATION(MODE_SET_ALL) {

            @Override
            public List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> createColumns(
                    VehicleSummaryConfig vehicleSummaryConfig) {
                return createDurationColumn(this, "active", vehicleSummaryConfig,
                        r -> r.getActiveDuration());
            }
        },

        IDLE_DURATION(MODE_SET_ALL) {

            @Override
            public List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> createColumns(
                    VehicleSummaryConfig vehicleSummaryConfig) {
                return createDurationColumn(this, "idle", vehicleSummaryConfig,
                        r -> r.getIdleDuration());
            }
        },

        ;

        private EnumSet<DisplayMode> enabledFormats;


        private Column(EnumSet<DisplayMode> enabledFormats) {
            this.enabledFormats = enabledFormats;
        }

        public abstract List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> createColumns(
                VehicleSummaryConfig vehicleSummaryConfig);

        public EnumSet<DisplayMode> getEnabledFormats() {
            return enabledFormats;
        }

        private static List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> createDurationColumn(
                Column column, String title, VehicleSummaryConfig vehicleSummaryConfig,
                Function<VehicleSummaryRecord, Long> extractor) {

            EnumSet<DisplayMode> modes = composeDisplayModes(column, vehicleSummaryConfig);

            List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> cols = new ArrayList<>();
            for (DisplayMode m : MODE_SET_ALL) {
                ColumnDefinition.StatelessBuilder<VehicleSummaryRecord, ?> b = null;
                if (modes.contains(m)) {
                    switch (m) {
                    case NUMERIC:
                        b = new ColumnDefinition.StatelessBuilder<VehicleSummaryRecord, Long>()
                        .withDataExtractor(extractor)
                        .withDataConverter(NumberDataConverter.defaultLongFormatter());

                        break;
                    case HUMAN_READABLE:
                        b = new ColumnDefinition.StatelessBuilder<VehicleSummaryRecord, Long>()
                        .withDataExtractor(extractor)
                        .withDataConverter(
                                d -> vehicleSummaryConfig.formatDurationHuman(d));
                        break;
                    case PERCENT_SHIFT:
                        b = new ColumnDefinition.StatelessBuilder<VehicleSummaryRecord, Double>()
                        .withDataExtractor(
                                r -> (double) extractor.apply(r) / r.getShiftDuration())
                        .withDataConverter(defaultPercentFormatter());
                        // TODO
                        break;
                    case PERCENT_ROUTE:
                        b = new ColumnDefinition.StatelessBuilder<VehicleSummaryRecord, Double>()
                        .withDataExtractor(
                                r -> (double) extractor.apply(r) / r.getRouteDuration())
                        .withDataConverter(defaultPercentFormatter());
                        // TODO
                        break;
                    default:
                        break;
                    }
                    if (b != null) {
                        b.withCellContentFormatter(CellContentFormatter.rightAlignedCell());
                        cols.add(b.withTitle(title + m.postfix).build());
                    }
                }
            }
            return cols;
        }


        private static NumberDataConverter<Double> defaultPercentFormatter() {
            NumberFormat formatter = NumberFormat.getPercentInstance();
            formatter.setMaximumFractionDigits(2);
            formatter.setMinimumFractionDigits(2);
            formatter.setGroupingUsed(false);
            formatter.setRoundingMode(RoundingMode.HALF_UP);
            return new NumberDataConverter<>(formatter);
        }

        private static List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> createTimeWindowColumn(
                Column column, String title, VehicleSummaryConfig vehicleSummaryConfig,
                Function<VehicleSummaryRecord, TimeWindow> extractor) {

            EnumSet<DisplayMode> modes = composeDisplayModes(column, vehicleSummaryConfig);

            List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> cols = new ArrayList<>();
            for(DisplayMode m : MODE_SET_ALL) {
                if (modes.contains(m)) {
                    ColumnDefinition.StatelessBuilder<VehicleSummaryRecord, TimeWindow> b = new ColumnDefinition.StatelessBuilder<>();
                    switch (m) {
                    case NUMERIC:
                        b.withDataConverter(
                                tw -> vehicleSummaryConfig.formatTimeWindowNumeric(tw));
                        break;
                    case HUMAN_READABLE:
                        b.withDataConverter(
                                tw -> vehicleSummaryConfig.formatTimeWindowHuman(tw));
                        break;

                    default:
                        break;
                    }
                    if (b != null) {
                        b.withCellContentFormatter(CellContentFormatter.rightAlignedCell());
                        b.withDataExtractor(extractor);
                        cols.add(b.withTitle(title + m.postfix).build());
                    }
                }
            }
            return cols;
        }


        private static EnumSet<DisplayMode> composeDisplayModes(Column column,
                VehicleSummaryConfig vehicleSummaryConfig) {
            EnumSet<DisplayMode> modes = EnumSet.copyOf(column.getEnabledFormats());
            modes.retainAll(vehicleSummaryConfig.getDisplayModes());
            return modes;
        }


    }

    public static class Builder {
        private LocalDateTime humanReadableOrigin = LocalDateTime.of(LocalDate.now(),
                LocalTime.MIDNIGHT);
        private EnumSet<DisplayMode> displayModes = MODE_SET_ALL;
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

        public Builder withTimeDisplayModes(EnumSet<DisplayMode> displayModes) {
            this.displayModes = displayModes;
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


        public VehicleSummaryConfig build() {
            return new VehicleSummaryConfig(this);
        }
    }

    private EnumSet<DisplayMode> displayModes;
    private List<Column> columns;

    private VehicleSummaryConfig(Builder builder) {
        this.displayModes = builder.displayModes;
        this.columns = builder.columns;
        setTimeFormatter(
                new HumanReadableTimeFormatter(builder.humanReadableOrigin, builder.lowUnit));
        setDurationFormatter(new HumanReadableDurationFormatter(builder.lowUnit, builder.highUnit));
    }


    public EnumSet<DisplayMode> getDisplayModes() {
        return displayModes;
    }

    public List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> getColumns() {
        List<ColumnDefinition<VehicleSummaryRecord, ?, ?>> columns = new ArrayList<>();
        this.columns.forEach(c -> columns.addAll(c.createColumns(this)));
        return columns;
    }


}