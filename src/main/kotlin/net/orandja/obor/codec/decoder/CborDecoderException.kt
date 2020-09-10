package net.orandja.obor.codec.decoder

// TODO: Create exception for each of decode variant
sealed class CborDecoderException(message: String) : IllegalStateException(message) {
    object Default : CborDecoderException("Error occurred while parsing")
}