package net.snaglobal.trile.wizeye.utils

/**
 * @author lmtri
 * @since Oct 11, 2018 at 9:40 AM
 */
open class SingletonHolder<out T, in A>(creator: (A) -> T) {
    private var creator: (A) -> T = creator
    @Volatile private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                created
            }
        }
    }

    fun resetInstance() {
        synchronized(this) {
            instance = null
        }
    }
}