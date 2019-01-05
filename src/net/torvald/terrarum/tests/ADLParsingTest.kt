package net.torvald.terrarum.tests

import net.torvald.spriteassembler.ADProperties
import java.io.StringReader

/**
 * Created by minjaesong on 2019-01-05.
 */
class ADLParsingTest {

    val TEST_STR = """
        SPRITESHEET=sprites/test
        EXTENSION=.tga.gz

        # note to self: don't implement skeleton hierarchy: there's too many exceptions
        # besides, you have "ALL" key.

        ! a skeleton also defines what body parts (images) be used.
        SKELETON_STAND=HEAD 0,32;UPPER_TORSO 0,23;LOWER_TORSO 0,15;LEG_LEFT 2,7;LEG_RIGHT -2,7;FOOT_LEFT 2,2;FOOT_RIGHT -2,2;ARM_REST_LEFT 5,24;HAND_REST_LEFT 6,12;ARM_REST_RIGHT -7,23;HAND_REST_RIGHT -6,11

        # skeleton_stand is used for testing purpose
        ANIM_RUN=DELAY 0.15;ROW 2;SKELETON SKELETON_STAND
        ANIM_RUN_1=LEG_RIGHT 1,-1;LEG_LEFT -1,0
        ANIM_RUN_2=ALL 0,-1;LEG_RIGHT 0,1;LEG_LEFT 0,-1
        ANIM_RUN_3=LEG_RIGHT -1,0;LEG_LEFT 1,-1
        ANIM_RUN_4=ALL 0,-1;LEG_RIGHT 0,-1;LEG_LEFT 0,1

        ANIM_IDLE=DELAY 2;ROW 1;SKELETON SKELETON_STAND
        ANIM_IDLE_1=
        ! ANIM_IDLE_1 will not make any transformation
        ANIM_IDLE_2=UPPER_TORSO 0,-1

        ANIM_CROUCH=DELAY 1;ROW 3;SKELETON SKELETON_CROUCH
        ANIM_CROUCH_1=
    """.trimIndent()

    operator fun invoke() {
        val prop = ADProperties(StringReader(TEST_STR))

        prop.forEach { s, list ->
            println(s)

            list.forEach {
                println("\t$it")
            }
        }
    }

}

fun main(args: Array<String>) {
    ADLParsingTest().invoke()
}