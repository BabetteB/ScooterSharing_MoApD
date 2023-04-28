/**
 * MIT License
 *
 * Copyright (c) [2023] [Babette & Freyja]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package dk.itu.moapd.scootersharing.babb.model

import android.widget.Chronometer
import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Date
import java.util.Timer
import java.util.UUID


/**
 * Scooter.
 *
 * This class creates a Scooter instance, a Scooter has a name : String and a location : String, and saves the time it was created : Long
 *
 * @property name : String is the name of the Scooter.
 * @property location : String is the location of the Scooter.
 * @property createdAt : Long is the time in which the object is created. Defaults to 0
 */
@IgnoreExtraProperties
data class Scooter (
    val id : String? = null,
    var name : String? = null,
    var location : String? = null,
    var reserved : Boolean? = null,
    var createdAt : Long? = null,
    var lastUpdateTimeStamp : Date? = null,
    var assignedToUserID : String? = null,
    var elapsedTime : StopWatch? = null
) : Serializable {

}

