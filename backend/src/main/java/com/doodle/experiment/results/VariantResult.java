package com.doodle.experiment.results;

/**
 * @param seen      visitors who saw the button
 * @param clicked   visitors who saw the button and clicked it
 * @param clickRate clicked / seen, or 0 when nobody saw it yet
 */
public record VariantResult(long seen, long clicked, double clickRate) {

    public static VariantResult of(long seen, long clicked) {
        return new VariantResult(seen, clicked, seen == 0 ? 0 : (double) clicked / seen);
    }
}
