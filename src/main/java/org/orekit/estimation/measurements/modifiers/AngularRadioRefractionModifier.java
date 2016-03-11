/* Copyright 2002-2016 CS Systèmes d'Information
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
package org.orekit.estimation.measurements.modifiers;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.orekit.errors.OrekitException;
import org.orekit.estimation.Parameter;
import org.orekit.estimation.measurements.Angular;
import org.orekit.estimation.measurements.Evaluation;
import org.orekit.estimation.measurements.EvaluationModifier;
import org.orekit.estimation.measurements.GroundStation;
import org.orekit.models.AtmosphericRefractionModel;
import org.orekit.propagation.SpacecraftState;

/** Class modifying theoretical angular measurement with ionospheric radio refractive index.
 * A radio ray passing through the lower (non-ionized) layer of the atmosphere undergoes bending
 * caused by the gradient of the relative index. Since the refractive index varies mainly with
 * altitude, only the vertical gradient of the refractive index is considered here.
 * The effect of ionospheric correction on the angular measurement is computed directly
 * through the computation of the apparent elevation angle.
 * Recommendation ITU-R P.453-11 (07/2015) and Recommendation ITU-R P.834-7 (10/2015)
 *
 *
 * @author Thierry Ceolin
 * @since 7.2
 */
public class AngularRadioRefractionModifier implements EvaluationModifier<Angular> {
    /** Tropospheric refraction model. */
    private final AtmosphericRefractionModel atmosModel;

    /** Constructor.
    *
    * @param model  tropospheric refraction model appropriate for the current angular measurement method.
    */
    public AngularRadioRefractionModifier(final AtmosphericRefractionModel model) {
        atmosModel = model;
    }

    /** Compute the measurement error due to troposphere refraction.
    * @param station station
    * @param state spacecraft state
    * @return the measurement error due to ionosphere
    * @throws OrekitException  if frames transformations cannot be computed
    */
    private double angularErrorRadioRefractionModel(final GroundStation station,
                                                    final SpacecraftState state)
                                                                    throws OrekitException {

        final Vector3D position = state.getPVCoordinates().getPosition();

        // elevation in radians
        final double elevation = station.getBaseFrame().getElevation(position,
                                                                     state.getFrame(),
                                                                     state.getDate());

        // angle correction (rad)
        return atmosModel.getRefraction(elevation);
    }

    @Override
    public List<Parameter> getSupportedParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void modify(final Evaluation<Angular> evaluation)
                    throws OrekitException {
        // TODO Auto-generated method stub
        final Angular         measure = evaluation.getMeasurement();
        final GroundStation   station = measure.getStation();
        final SpacecraftState state   = evaluation.getState();
        final double correction = angularErrorRadioRefractionModel(station, state);

        // update measurement value taking into account the tropospheric elevation corection.
        // The tropospheric elevation correction is directly added to the elevation.
        final double[] oldValue = evaluation.getValue();
        final double[] newValue = oldValue.clone();

        // consider only effect on elevation
        newValue[1] = newValue[1] + correction;
        evaluation.setValue(newValue[0], newValue[1]);
    }
}