/*
 * The MIT License
 *
 * Copyright (c) 2015-2024 JOML
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.joml;

/**
 * Contains fast approximations of some {@link java.lang.Math} operations.
 * <p>
 * By default, {@link java.lang.Math} methods will be used by all other JOML classes. In order to use the approximations in this class, start the JVM with the parameter <code>-Djoml.fastmath</code>.
 * <p>
 * There are two algorithms for approximating sin/cos:
 * <ol>
 * <li>arithmetic <a href="http://www.java-gaming.org/topics/joml-1-8-0-release/37491/msg/361815/view.html#msg361815">polynomial approximation</a> contributed by roquendm 
 * <li>theagentd's <a href="http://www.java-gaming.org/topics/extremely-fast-sine-cosine/36469/msg/346213/view.html#msg346213">linear interpolation</a> variant of Riven's algorithm from
 * <a href="http://www.java-gaming.org/topics/extremely-fast-sine-cosine/36469/view.html">http://www.java-gaming.org/</a>
 * </ol>
 * By default, the first algorithm is being used. In order to use the second one, start the JVM with <code>-Djoml.sinLookup</code>. The lookup table bit length of the second algorithm can also be adjusted
 * for improved accuracy via <code>-Djoml.sinLookup.bits=&lt;n&gt;</code>, where &lt;n&gt; is the number of bits of the lookup table.
 *
 * @author Kai Burjack
 */
public class Math {

    public static final double PI = java.lang.Math.PI;
    public static final double PI_TIMES_2 = PI * 2.0;
    public static final float PI_f = (float) java.lang.Math.PI;
    public static final float PI_TIMES_2_f = PI_f * 2.0f;
    public static final double PI_OVER_2 = PI * 0.5;
    public static final float PI_OVER_2_f = (float) (PI * 0.5);
    public static final double PI_OVER_4 = PI * 0.25;
    public static final float PI_OVER_4_f = (float) (PI * 0.25);
    public static final double ONE_OVER_PI = 1.0 / PI;
    public static final float ONE_OVER_PI_f = (float) (1.0 / PI);

    private static final double c1 = Double.longBitsToDouble(-4628199217061079772L);
    private static final double c2 = Double.longBitsToDouble(4575957461383582011L);
    private static final double c3 = Double.longBitsToDouble(-4671919876300759001L);
    private static final double c4 = Double.longBitsToDouble(4523617214285661942L);
    private static final double c5 = Double.longBitsToDouble(-4730215272828025532L);
    private static final double c6 = Double.longBitsToDouble(4460272573143870633L);
    private static final double c7 = Double.longBitsToDouble(-4797767418267846529L);

    public static float acos(float r) {
        return (float) java.lang.Math.acos(r);
    }
    public static double acos(double r) {
        return java.lang.Math.acos(r);
    }

    public static float safeAcos(float v) {
        if (v < -1.0f)
            return Math.PI_f;
        else if (v > +1.0f)
            return 0.0f;
        else
            return acos(v);
    }
    public static double safeAcos(double v) {
        if (v < -1.0)
            return Math.PI;
        else if (v > +1.0)
            return 0.0;
        else
            return acos(v);
    }

    public static float sin(float rad) {
        return (float) java.lang.Math.sin(rad);
    }
    public static double sin(double rad) {
        return java.lang.Math.sin(rad);
    }

    public static float cosFromSin(float sin, float angle) {
        return cosFromSinInternal(sin, angle);
    }
    private static float cosFromSinInternal(float sin, float angle) {
        // sin(x)^2 + cos(x)^2 = 1
        float cos = sqrt(1.0f - sin * sin);
        float a = angle + PI_OVER_2_f;
        float b = a - (int)(a / PI_TIMES_2_f) * PI_TIMES_2_f;
        if (b < 0.0)
            b = PI_TIMES_2_f + b;
        if (b >= PI_f)
            return -cos;
        return cos;
    }
    public static double cosFromSin(double sin, double angle) {
        // sin(x)^2 + cos(x)^2 = 1
        double cos = sqrt(1.0 - sin * sin);
        double a = angle + PI_OVER_2;
        double b = a - (int)(a / PI_TIMES_2) * PI_TIMES_2;
        if (b < 0.0)
            b = PI_TIMES_2 + b;
        if (b >= PI)
            return -cos;
        return cos;
    }

    public static float sqrt(float r) {
        return (float) java.lang.Math.sqrt(r);
    }
    public static double sqrt(double r) {
        return java.lang.Math.sqrt(r);
    }

    public static float invsqrt(float r) {
        return 1.0f / (float) java.lang.Math.sqrt(r);
    }
    public static double invsqrt(double r) {
        return 1.0 / java.lang.Math.sqrt(r);
    }

    public static float fma(float a, float b, float c) {
        return a * b + c;
    }

    public static double fma(double a, double b, double c) {
        return a * b + c;
    }

    public static float toRadians(float angles) {
        return (float) java.lang.Math.toRadians(angles);
    }
    public static double toRadians(double angles) {
        return java.lang.Math.toRadians(angles);
    }

    public static float abs(float r) {
        return java.lang.Math.abs(r);
    }
    public static double abs(double r) {
        return java.lang.Math.abs(r);
    }

    public static float atan2(float y, float x) {
        return (float) java.lang.Math.atan2(y, x);
    }
    public static double atan2(double y, double x) {
        return java.lang.Math.atan2(y, x);
    }


}