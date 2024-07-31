package net.orandja.obor.codec.decoder

// TODO: Create exception for each of decode variant
// TODO: Force index error info inside CborDecoderException
/** A class holding all CborDecoderException */
sealed class CborDecoderException(message: String) : IllegalStateException(message) {
    class Default : CborDecoderException("Error occurred while parsing")
}