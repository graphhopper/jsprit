package com.graphhopper.jsprit.core.problem;

import java.util.Arrays;

import com.graphhopper.jsprit.core.problem.SizeDimension.SizeDimensionSign;

/**
 * Legacy class of {@linkplain SizeDimension}.
 *
 * @author balage
 *
 * @deprecated Use {@linkplain SizeDimension}, instead.
 */
@Deprecated
public abstract class Capacity {


    public abstract SizeDimension getPositiveDimensions();

    public abstract SizeDimension getNegativeDimensions();

    public abstract double divide(SizeDimension denominator);

    public abstract SizeDimension abs();

    public abstract SizeDimension invert();

    public abstract SizeDimension subtract(SizeDimension sizeToSubstract);

    public abstract SizeDimension add(SizeDimension sizeToAdd);

    public abstract SizeDimensionSign sign();

    public abstract boolean isGreaterOrEqual(SizeDimension toCompare);

    public abstract boolean isLessOrEqual(SizeDimension toCompare);

    public abstract int get(int index);

    public abstract int getNuOfDimensions();

    /**
     * @deprecated Use {@linkplain SizeDimension#EMPTY}, instead.
     */
    @Deprecated
    public static final SizeDimension EMPTY = SizeDimension.EMPTY;

    /**
     * <b>Legacy class.</b> Builder that builds SizeDimension
     *
     * @author schroeder
     * @author balage
     * @deprecated Use {@linkplain SizeDimension.Builder}, instead.
     */
    @Deprecated
    public static abstract class Builder {

        // This is the real trick: this is a fake builder which facade the real
        // SizeDimension.Builder

        public static SizeDimension.Builder newInstance() {
            return new SizeDimension.Builder();
        }

        public abstract SizeDimension build();

        public abstract Builder setDimensions(SizeDimension other);

        public abstract Builder addDimension(int index, int dimValue);
    }


    /**
     * @deprecated Use {@linkplain SizeDimension#add(SizeDimension)}, instead.
     */
    @Deprecated
    public static SizeDimension addup(Capacity cap1, Capacity cap2) {
        return ((SizeDimension) cap1).add((SizeDimension) cap2);
    }

    /**
     * @deprecated Use {@linkplain SizeDimension#subtract(SizeDimension)},
     *             instead.
     */
    @Deprecated
    public static SizeDimension subtract(Capacity cap, Capacity cap2subtract) {
        return ((SizeDimension) cap).subtract((SizeDimension) cap2subtract);
    }

    /**
     * @deprecated Use {@linkplain SizeDimension#invert()}, instead.
     */
    @Deprecated
    public static SizeDimension invert(Capacity cap2invert) {
        return ((SizeDimension) cap2invert).invert();
    }

    /**
     * @deprecated Use
     *             {@linkplain SizeDimension#divide(SizeDimension, SizeDimension)}
     *             , instead.
     */
    @Deprecated
    public static double divide(Capacity numerator, Capacity denominator) {
        return numerator.divide((SizeDimension) denominator);
    }

    /**
     * @deprecated Use
     *             {@linkplain SizeDimension#max(SizeDimension,SizeDimension)},
     *             instead.
     */
    @Deprecated
    public static SizeDimension max(Capacity size1, Capacity size2) {
        return SizeDimension.max((SizeDimension) size1, (SizeDimension) size2);
    }

    /**
     * @deprecated Use
     *             {@linkplain SizeDimension#min(SizeDimension, SizeDimension)},
     *             instead.
     */
    @Deprecated
    public static SizeDimension min(Capacity size1, Capacity size2) {
        return SizeDimension.min((SizeDimension) size1, (SizeDimension) size2);
    }

    /**
     * @deprecated Use {@linkplain SizeDimension#copyOf(SizeDimension)},
     *             instead.
     */
    @Deprecated
    public static SizeDimension copyOf(Capacity sizeDimension) {
        if (sizeDimension == null) {
            return null;
        }
        return new SizeDimension((SizeDimension) sizeDimension);
    }

}