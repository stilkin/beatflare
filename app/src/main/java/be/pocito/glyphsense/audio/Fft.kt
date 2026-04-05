package be.pocito.glyphsense.audio

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Minimal in-place Cooley-Tukey radix-2 FFT. Size must be a power of two.
 *
 * Reusable across calls to avoid allocation. Not thread-safe — use one instance per thread.
 */
class Fft(private val size: Int) {
    init {
        require(size > 0 && (size and (size - 1)) == 0) { "size must be a power of two" }
    }

    private val real = FloatArray(size)
    private val imag = FloatArray(size)
    /** Pre-computed Hann window: 0.5 * (1 - cos(2πn / (N-1))) */
    private val window = FloatArray(size) { n ->
        (0.5 * (1.0 - cos(2.0 * PI * n / (size - 1)))).toFloat()
    }
    /** Output magnitude spectrum, size/2 bins (0..Nyquist). */
    val magnitudes = FloatArray(size / 2)

    /**
     * Computes the magnitude spectrum for [samples], applying a Hann window.
     * [samples] must contain at least [size] values; extra samples are ignored.
     */
    fun compute(samples: ShortArray) {
        val n = size
        for (i in 0 until n) {
            real[i] = samples[i].toFloat() * window[i]
            imag[i] = 0f
        }
        fftInPlace(real, imag, n)
        val halfN = n / 2
        for (k in 0 until halfN) {
            val re = real[k]
            val im = imag[k]
            magnitudes[k] = sqrt(re * re + im * im)
        }
    }

    companion object {
        /**
         * In-place iterative Cooley-Tukey FFT. Bit-reversal permutation followed by
         * log2(n) butterfly stages.
         */
        private fun fftInPlace(re: FloatArray, im: FloatArray, n: Int) {
            // Bit-reversal permutation
            var j = 0
            for (i in 1 until n) {
                var bit = n shr 1
                while (j and bit != 0) {
                    j = j xor bit
                    bit = bit shr 1
                }
                j = j xor bit
                if (i < j) {
                    var t = re[i]; re[i] = re[j]; re[j] = t
                    t = im[i]; im[i] = im[j]; im[j] = t
                }
            }
            // Butterflies
            var len = 2
            while (len <= n) {
                val half = len / 2
                val angle = (-2.0 * PI / len).toFloat()
                val wRe0 = cos(angle.toDouble()).toFloat()
                val wIm0 = sin(angle.toDouble()).toFloat()
                var i = 0
                while (i < n) {
                    var wRe = 1f
                    var wIm = 0f
                    for (k in 0 until half) {
                        val aRe = re[i + k]
                        val aIm = im[i + k]
                        val bRe = re[i + k + half]
                        val bIm = im[i + k + half]
                        // t = w * b
                        val tRe = wRe * bRe - wIm * bIm
                        val tIm = wRe * bIm + wIm * bRe
                        re[i + k] = aRe + tRe
                        im[i + k] = aIm + tIm
                        re[i + k + half] = aRe - tRe
                        im[i + k + half] = aIm - tIm
                        // w *= w0
                        val nwRe = wRe * wRe0 - wIm * wIm0
                        val nwIm = wRe * wIm0 + wIm * wRe0
                        wRe = nwRe
                        wIm = nwIm
                    }
                    i += len
                }
                len = len shl 1
            }
        }
    }
}
