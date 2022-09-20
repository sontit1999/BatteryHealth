package com.procharger.fastprocharrging.quickcharge.common.util

import com.procharger.fastprocharrging.quickcharge.data.model.AppSettingsModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

// Use object so we have a singleton instance
object RxBus {

    private val publisher = PublishSubject.create<Any?>()

    private fun publish(event: Any?) {
        event?.let {
            publisher.onNext(it)
        }
    }

    // Listen should return an Observable and not the publisher
    // Using ofType we filter only events that match that class type
    private fun <T> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)

    fun publishAppSettingsModel(model: AppSettingsModel) {
        publish(model)
    }

    fun listenAppSettingsChanged(): Observable<AppSettingsModel> {
        return listen(AppSettingsModel::class.java)
    }
}