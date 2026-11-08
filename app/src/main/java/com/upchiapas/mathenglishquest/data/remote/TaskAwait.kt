package com.upchiapas.mathenglishquest.data.remote

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Convierte una [Task] de Google Play Services en una función `suspend`.
 *
 * Así se puede usar `val user = auth.signInWithEmailAndPassword(...).awaitTask()`
 * dentro de una corrutina sin agregar la librería `kotlinx-coroutines-play-services`.
 */
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { value ->
        if (cont.isActive) cont.resume(value)
    }
    addOnFailureListener { error ->
        if (cont.isActive) cont.resumeWithException(error)
    }
}
