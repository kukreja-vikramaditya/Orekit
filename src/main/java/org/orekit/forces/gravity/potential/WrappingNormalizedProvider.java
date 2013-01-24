/* Copyright 2002-2013 CS Systèmes d'Information
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
package org.orekit.forces.gravity.potential;

import org.orekit.errors.OrekitException;
import org.orekit.time.AbsoluteDate;

/** Wrapper providing normalized coefficients.
 * <p>
 * The caller <em>must</em> ensure by itself that the raw provider already
 * stores (and provides) normalized coefficients.
 * </p>
 * @author Luc Maisonobe
 * @since 6.0
 */
class WrappingNormalizedProvider implements NormalizedSphericalHarmonicsProvider {

    /** Raw provider to which everything is delegated. */
    private final RawSphericalHarmonicsProvider rawProvider;

    /** Simple constructor.
     * @param rawProvider raw provider to which everything is delegated
     */
    public WrappingNormalizedProvider(final RawSphericalHarmonicsProvider rawProvider) {
        this.rawProvider = rawProvider;
    }

    /** {@inheritDoc} */
    public int getMaxDegree() {
        return rawProvider.getMaxDegree();
    }

    /** {@inheritDoc} */
    public int getMaxOrder() {
        return rawProvider.getMaxOrder();
    }

    /** {@inheritDoc} */
    public double getMu() {
        return rawProvider.getMu();
    }

    /** {@inheritDoc} */
    public double getAe() {
        return rawProvider.getAe();
    }

    /** {@inheritDoc} */
    public AbsoluteDate getReferenceDate() {
        return rawProvider.getReferenceDate();
    }

    /** {@inheritDoc} */
    public double getOffset(AbsoluteDate date) {
        return rawProvider.getOffset(date);
    }

    /** {@inheritDoc} */
    public double getNormalizedCnm(double dateOffset, int n, int m)
            throws OrekitException {
        // no conversion is done here
        return rawProvider.getRawCnm(dateOffset, n, m);
    }

    /** {@inheritDoc} */
    public double getNormalizedSnm(double dateOffset, int n, int m)
            throws OrekitException {
        // no conversion is done here
        return rawProvider.getRawSnm(dateOffset, n, m);
    }

}
