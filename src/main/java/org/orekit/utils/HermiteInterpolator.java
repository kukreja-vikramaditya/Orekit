/* Copyright 2002-2012 CS Systèmes d'Information
 * Licensed to CS Systèmes d'Information (CS) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * CS licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orekit.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.DifferentiableUnivariateVectorFunction;
import org.apache.commons.math3.analysis.UnivariateVectorFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.util.ArithmeticUtils;
import org.orekit.errors.OrekitException;
import org.orekit.errors.OrekitMessages;

/** Polynomial interpolator using both sample values and sample derivatives.
 * <p>
 * Note that this class is <em>not</em> expected to remain in Orekit. It rather
 * belongs to Apache Commons Math. However, since as of writing (June 2012) Orekit
 * depends on version 3.0 of Apache Commons Math and this class is not provided by
 * this version of the library, it is implemented as a package private class in
 * Orekit. Once the class is included in an officially released version of Apache
 * Commons Math, it will be removed from Orekit.
 * </p>
 * <p>
 * The interpolation polynomials match all sample points, including both values
 * and provided derivatives. There is one polynomial for each component of
 * the values vector. All polynomial have the same degree. The degree of the
 * polynomials depends on the number of points and number of derivatives at each
 * point. For example the interpolation polynomials for n sample points without
 * any derivatives all have degree n-1. The interpolation polynomials for n
 * sample points with the two extreme points having value and first derivative
 * and the remaining points having value only all have degree n+1. The
 * interpolation polynomial for n sample points with value, first and second
 * derivative for all points all have degree 3n-1.
 * </p>
 * @author Luc Maisonobe
 */
class HermiteInterpolator implements DifferentiableUnivariateVectorFunction {

    /** Sample abscissae. */
    private final List<Double> abscissae;

    /** Top diagonal of the divided differences array. */
    private final List<double[]> topDiagonal;

    /** Bottom diagonal of the divided differences array. */
    private final List<double[]> bottomDiagonal;

    /** Create an empty interpolator.
     */
    public HermiteInterpolator() {
        this.abscissae      = new ArrayList<Double>();
        this.topDiagonal    = new ArrayList<double[]>();
        this.bottomDiagonal = new ArrayList<double[]>();
    }

    /** Add a sample point.
     * <p>
     * This method must be called once for each sample point. It is allowed to
     * mix some calls with values only with calls with values and first
     * derivatives.
     * </p>
     * <p>
     * The point abscissae for all calls <em>must</em> be different.
     * </p>
     * @param x abscissa of the sample point
     * @param y value and derivatives of the sample point
     * (if only one row is passed, it is the value, if two rows are
     * passed the first one is the value and the second the derivative
     * and so on)
     * @exception IllegalArgumentException if the abscissa is equals to a previously
     * added sample point
     */
    public void addSamplePoint(final double x, final double[] ... value)
        throws IllegalArgumentException {

        for (int i = 0; i < value.length; ++i) {

            final double[] y = value[i].clone();
            if (i > 1) {
                double inv = 1.0 / ArithmeticUtils.factorial(i);
                for (int j = 0; j < y.length; ++j) {
                    y[j] *= inv;
                }
            }

            // update the bottom diagonal of the divided differences array
            final int n = abscissae.size();
            bottomDiagonal.add(n - i, y);
            double[] bottom0 = y;
            for (int j = i; j < n; ++j) {
                final double[] bottom1 = bottomDiagonal.get(n - (j + 1));
                final double inv = 1.0 / (x - abscissae.get(n - (j + 1)));
                if (Double.isInfinite(inv)) {
                    throw OrekitException.createIllegalArgumentException(OrekitMessages.DUPLICATED_ABSCISSA, x);
                }
                for (int k = 0; k < y.length; ++k) {
                    bottom1[k] = inv * (bottom0[k] - bottom1[k]);
                }
                bottom0 = bottom1;
            }

            // update the top diagonal of the divided differences array
            topDiagonal.add(bottom0.clone());

            // update the abscissae array
            abscissae.add(x);

        }

    }

    /** Compute the interpolation polynomials.
     * @return interpolation polynomials array
     * @exception IllegalStateException if sample is empty
     */
    public PolynomialFunction[] getPolynomials()
        throws IllegalStateException {

        // safety check
        checkInterpolation();

        // iteration initialization
        final PolynomialFunction zero = polynomial(0);
        PolynomialFunction[] polynomials = new PolynomialFunction[topDiagonal.get(0).length];
        for (int i = 0; i < polynomials.length; ++i) {
            polynomials[i] = zero;
        }
        PolynomialFunction coeff = polynomial(1);

        // build the polynomials by iterating on the top diagonal of the divided differences array
        for (int i = 0; i < topDiagonal.size(); ++i) {
            double[] tdi = topDiagonal.get(i);
            for (int k = 0; k < polynomials.length; ++k) {
                polynomials[k] = polynomials[k].add(coeff.multiply(polynomial(tdi[k])));
            }
            coeff = coeff.multiply(polynomial(-abscissae.get(i), 1.0));
        }

        return polynomials;

    }

    /** Interpolate value at a specified abscissa.
     * <p>
     * Calling this method is equivalent to call the {@link PolynomialFunction#value(double)
     * value} methods of all polynomials returned by {@link #getPolynomials() getPolynomials},
     * except it does not build the intermediate polynomials, so this method is faster and
     * numerically more stable.
     * </p>
     * @param x interpolation abscissa
     * @return interpolated value
     * @exception IllegalStateException if sample is empty
     */
    public double[] value(double x)
        throws IllegalStateException {

        // safety check
        checkInterpolation();

        final double[] value = new double[topDiagonal.get(0).length];
        double valueCoeff = 1;
        for (int i = 0; i < topDiagonal.size(); ++i) {
            double[] dividedDifference = topDiagonal.get(i);
            for (int k = 0; k < value.length; ++k) {
                value[k] += dividedDifference[k] * valueCoeff;
            }
            final double deltaX = x - abscissae.get(i);
            valueCoeff *= deltaX;
        }

        return value;

    }

    /** Interpolate first derivative at a specified abscissa.
     * <p>
     * Calling this method is equivalent to call the {@link PolynomialFunction#value(double)
     * value} methods of the derivatives of all polynomials returned by {@link
     * #getPolynomials() getPolynomials}, except it builds neither the intermediate
     * polynomials nor their derivatives, so this method is faster and numerically more stable.
     * </p>
     * @param x interpolation abscissa
     * @return interpolated derivative
     * @exception IllegalStateException if sample is empty
     */
    public double[] derivative(double x)
        throws IllegalStateException {

        // safety check
        checkInterpolation();

        final double[] derivative = new double[topDiagonal.get(0).length];
        double valueCoeff      = 1;
        double derivativeCoeff = 0;
        for (int i = 0; i < topDiagonal.size(); ++i) {
            double[] dividedDifference = topDiagonal.get(i);
            for (int k = 0; k < derivative.length; ++k) {
                derivative[k] += dividedDifference[k] * derivativeCoeff;
            }
            final double deltaX = x - abscissae.get(i);
            derivativeCoeff = valueCoeff + derivativeCoeff * deltaX;
            valueCoeff *= deltaX;
        }

        return derivative;

    }

    /** {@inheritDoc}} */
    public UnivariateVectorFunction derivative() {
        return new UnivariateVectorFunction() {

            /** {@inheritDoc}} */
            public double[] value(double x) {
                return derivative(x);
            }
            
        };
    }

    /** Check interpolation can be performed.
     * @exception IllegalStateException if interpolation cannot be performed
     * because sample is empty
     */
    private void checkInterpolation() throws IllegalStateException {        
        if (abscissae.isEmpty()) {
            throw OrekitException.createIllegalStateException(OrekitMessages.EMPTY_INTERPOLATION_SAMPLE);
        }
    }

    /** Create a polynomial from its coefficients.
     * @param c polynomials coefficients
     * @return polynomial
     */
    private PolynomialFunction polynomial(double ... c) {
        return new PolynomialFunction(c);
    }

}
