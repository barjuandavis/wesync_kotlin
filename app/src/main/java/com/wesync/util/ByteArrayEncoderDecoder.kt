package com.wesync.util

class ByteArrayEncoderDecoder {
    companion object {
        const val TWO_14: Long = 16383

        private fun typeToSize(type: Byte): Int {
            if (type == PayloadType.CONFIG) {
                return PayloadSizes.CONFIG
            }
                return PayloadSizes.PING
        }

        fun encodeTimestampByteArray(time: Long, type: Byte): ByteArray {
            val t = time % TWO_14 // 2^14
            var b = t.toString(2)
            val arr = ByteArray(typeToSize(type)){0}
            if (b.length < 14) {
                b = b.padStart(14,'0')
            }
            var lim = 6

            for (i in 1..2) {
                val curr = b.substring((lim-(lim-1))..lim)
                arr[i] = curr.toByte(2)
                lim += 7
            }

            arr[0] = type
            return arr
        }
        fun encodeConfigByteArray(bpm: Long, isPlaying: Boolean): ByteArray {
            var b = bpm.toString(2)
            val arr = ByteArray(PayloadSizes.CONFIG){0}
            if (b.length < 14) {
                b = b.padStart(14,'0')
            }
            var lim = 6

            for (i in 1..2) {
                val curr = b.substring((lim-(lim-1))..lim)
                arr[i] = curr.toByte(2)
                lim += 7
            }

            arr[0] = PayloadType.CONFIG

            if (isPlaying) arr[4] = 1

            return arr
        }

        fun decodeTimestamp(arr: ByteArray): Long {
            var str = "0"
            if(arr[0] != PayloadType.CONFIG) {
                str = ""
                for (i in 1..2) {
                    str += arr[i].toString(2).padStart(7, '0')
                }
            }
            return str.toLong(2)
        }
        fun decodeConfig(arr: ByteArray): Pair<Long,Boolean> {
            var bpm: Long = Tempo.DEFAULT_BPM
            var isPlaying = false
            //...
            if(arr[0] == PayloadType.CONFIG) {
               var bpmStr = ""
                for (i in 1..2) {
                    bpmStr += arr[i].toString(2).padStart(7, '0')
                }
                bpm = bpmStr.toLong(2)
                if (arr[3] == 1.toByte()) isPlaying = true
            }
            return Pair(bpm, isPlaying)
        }
    }
}