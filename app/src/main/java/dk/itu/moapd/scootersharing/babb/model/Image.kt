package dk.itu.moapd.scootersharing.babb.model

import com.google.firebase.database.IgnoreExtraProperties


/**
 * A model class with all parameters to represent a `Image` object in the database.
 */
@IgnoreExtraProperties
data class Image(val url: String? = null, val path: String? = null, val createdAt: Long? = null)