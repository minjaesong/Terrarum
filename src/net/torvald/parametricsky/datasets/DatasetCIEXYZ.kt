/*
This source is published under the following 3-clause BSD license.

Copyright (c) 2012 - 2013, Lukas Hosek and Alexander Wilkie
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.
* None of the names of the contributors may be used to endorse or promote
  products derived from this software without specific prior written
  permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES
LOSS OF USE, DATA, OR PROFITS OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/


/* ============================================================================

This file is part of a sample implementation of the analytical skylight and
solar radiance models presented in the SIGGRAPH 2012 paper


           "An Analytic Model for Full Spectral Sky-Dome Radiance"

and the 2013 IEEE CG&A paper

       "Adding a Solar Radiance Function to the Hosek Skylight Model"

                                   both by

                       Lukas Hosek and Alexander Wilkie
                Charles University in Prague, Czech Republic


                        Version: 1.4a, February 22nd, 2013

Version history:

1.4a  February 22nd, 2013
      Removed unnecessary and counter-intuitive solar radius parameters
      from the interface of the colourspace sky dome initialisation functions.

1.4   February 11th, 2013
      Fixed a bug which caused the relative brightness of the solar disc
      and the sky dome to be off by a factor of about 6. The sun was too
      bright: this affected both normal and alien sun scenarios. The
      coefficients of the solar radiance function were changed to fix this.

1.3   January 21st, 2013 (not released to the public)
      Added support for solar discs that are not exactly the same size as
      the terrestrial sun. Also added support for suns with a different
      emission spectrum ("Alien World" functionality).

1.2a  December 18th, 2012
      Fixed a mistake and some inaccuracies in the solar radiance function
      explanations found in ArHosekSkyModel.h. The actual source code is
      unchanged compared to version 1.2.

1.2   December 17th, 2012
      Native RGB data and a solar radiance function that matches the turbidity
      conditions were added.

1.1   September 2012
      The coefficients of the spectral model are now scaled so that the output
      is given in physical units: W / (m^-2 * sr * nm). Also, the output of the
      XYZ model is now no longer scaled to the range [0...1]. Instead, it is
      the result of a simple conversion from spectral data via the CIE 2 degree
      standard observer matching functions. Therefore, after multiplication
      with 683 lm / W, the Y channel now corresponds to luminance in lm.

1.0   May 11th, 2012
      Initial release.


Please visit http://cgg.mff.cuni.cz/projects/SkylightModelling/ to check if
an updated version of this code has been published!

============================================================================ */


/*

This file contains the coefficient data for the spectral version of the model.

*/

// Uses Sep 9 pattern / Aug 23 mean dataset

package net.torvald.parametricsky.datasets

import kotlin.test.assertEquals

object DatasetCIEXYZ {

    val datasetXYZ1 = DatasetOp.readDatasetFromFile("./work_files/skylight/hosek_model_source/datasetXYZ1.bin")
    val datasetXYZ2 = DatasetOp.readDatasetFromFile("./work_files/skylight/hosek_model_source/datasetXYZ2.bin")
    val datasetXYZ3 = DatasetOp.readDatasetFromFile("./work_files/skylight/hosek_model_source/datasetXYZ3.bin")

    val datasetXYZRad1 = DatasetOp.readDatasetFromFile("./work_files/skylight/hosek_model_source/datasetXYZRad1.bin")
    val datasetXYZRad2 = DatasetOp.readDatasetFromFile("./work_files/skylight/hosek_model_source/datasetXYZRad2.bin")
    val datasetXYZRad3 = DatasetOp.readDatasetFromFile("./work_files/skylight/hosek_model_source/datasetXYZRad3.bin")

    init {
        assertEquals(1080, datasetXYZ2.size, "Dataset size mismatch: expected 1080, got ${datasetXYZ2.size}")
        assertEquals(120, datasetXYZRad2.size, "Dataset size mismatch: expected 120, got ${datasetXYZRad2.size}")

        assertEquals(	-1.117001e+000, datasetXYZ1[0], "Dataset not parsed correctly - expected ${-1.117001e+000}, got ${datasetXYZ1[0]}")
    }


    val datasetsXYZ = arrayOf(
            datasetXYZ1,
            datasetXYZ2,
            datasetXYZ3
    )

    val datasetsXYZRad = arrayOf(
            datasetXYZRad1,
            datasetXYZRad2,
            datasetXYZRad3
    )
}