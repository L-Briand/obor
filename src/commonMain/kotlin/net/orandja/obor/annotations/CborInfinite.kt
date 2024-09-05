package net.orandja.obor.annotations


@Deprecated(
    message = "@CborInfinite was renamed to @CborIndefinite",
    replaceWith = ReplaceWith("CborIndefinite", "net.orandja.obor.annotations.CborIndefinite")
)
typealias CborInfinite = CborIndefinite