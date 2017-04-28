package net.torvald.terrarum.blockproperties

/**
 * Created by minjaesong on 16-02-16.
 */
class BlockProp {

    var id: Int = 0

    var nameKey: String = ""

    /**
     * @param opacity Raw RGB value, without alpha
     */
    var opacity: Int = 0 // colour attenuation

    var strength: Int = 0
    var density: Int = 0
    var viscosity: Int = 0

    var isFluid: Boolean = false
    var isSolid: Boolean = false
    var isWallable: Boolean = false
    var isVertFriction: Boolean = false

    /**
     * @param luminosity Raw RGB value, without alpha
     */
    var luminosity: Int = 0
        set(value) {
            field = value
        }
        get() = BlockPropUtil.getDynamicLumFunc(field, dynamicLuminosityFunction)

    var drop: Int = 0

    var isFallable: Boolean = false

    var friction: Int = 0

    var dynamicLuminosityFunction: Int = 0

    var material: String = ""
}