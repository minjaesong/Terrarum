package net.torvald.terrarum.tileproperties

/**
 * Created by minjaesong on 16-09-11.
 */
object TilePropCSV {
    operator fun invoke() = """
 "id";"dmg";"name"                     ;  "opacity";"strength";"dsty";"mate";"fluid";"solid";"wall";  "lumcolor";"drop";"ddmg";"fall";"dlfn";"friction"
  "0";  "0";"TILE_AIR"                 ;  "8396808";       "0";   "1";"null";    "0";    "0";   "0";         "0";   "0";   "0";   "0";   "0";"4"
  "1";  "0";"TILE_STONE"               ; "33587232";      "25";"2400";"rock";    "0";    "1";   "1";         "0";   "1";   "0";   "0";   "0";"16"
  "1";  "1";"TILE_STONE_QUARRIED"      ; "33587232";      "25";"2400";"rock";    "0";    "1";   "1";         "0";   "1";   "1";   "0";   "0";"16"
  "1";  "2";"TILE_STONE_TILE_WHITE"    ; "33587232";      "25";"2400";"rock";    "0";    "1";   "1";         "0";   "1";   "2";   "0";   "0";"16"
  "1";  "3";"TILE_STONE_BRICKS"        ; "33587232";      "25";"2400";"rock";    "0";    "1";   "1";         "0";   "1";   "3";   "0";   "0";"16"
  "2";  "0";"TILE_DIRT"                ; "33587232";       "6";"1400";"dirt";    "0";    "1";   "1";         "0";   "2";   "0";   "0";   "0";"16"
  "2";  "1";"TILE_GRASS"               ; "33587232";       "6";"1400";"grss";    "0";    "1";   "1";         "0";   "2";   "1";   "0";   "0";"16"
  "2";  "2";"TILE_FOLIAGE_GREEN"       ; "33587232";       "6";"1400";"grss";    "0";    "1";   "1";         "0";   "2";   "2";   "0";   "0";"16"
  "2";  "3";"TILE_FOLIAGE_LIME"        ; "33587232";       "6";"1400";"grss";    "0";    "1";   "1";         "0";   "2";   "3";   "0";   "0";"16"
  "2";  "4";"TILE_FOLIAGE_GOLD"        ; "33587232";       "6";"1400";"grss";    "0";    "1";   "1";         "0";   "2";   "4";   "0";   "0";"16"
  "2";  "5";"TILE_FOLIAGE_RED"         ; "33587232";       "6";"1400";"grss";    "0";    "1";   "1";         "0";   "2";   "5";   "0";   "0";"16"
  "2";  "6";"TILE_FOLIAGE_ICEBLUE"     ; "33587232";       "6";"1400";"grss";    "0";    "1";   "1";         "0";   "2";   "6";   "0";   "0";"16"
  "2";  "7";"TILE_FOLIAGE_PURPLE"      ; "33587232";       "6";"1400";"grss";    "0";    "1";   "1";         "0";   "2";   "7";   "0";   "0";"16"
  "3";  "0";"TILE_PLANK_NORMAL"        ; "33587232";      "12"; "740";"wood";    "0";    "1";   "1";         "0";   "3";   "0";   "0";   "0";"16"
  "3";  "1";"TILE_PLANK_EBONY"         ; "33587232";      "12";"1200";"wood";    "0";    "1";   "1";         "0";   "3";   "1";   "0";   "0";"16"
  "3";  "2";"TILE_PLANK_BIRCH"         ; "33587232";      "12"; "670";"wood";    "0";    "1";   "1";         "0";   "3";   "2";   "0";   "0";"16"
  "3";  "3";"TILE_PLANK_BLOODROSE"     ; "33587232";      "12"; "900";"wood";    "0";    "1";   "1";         "0";   "3";   "3";   "0";   "0";"16"
  "4";  "0";"TILE_TRUNK_NORMAL"        ; "33587232";      "12"; "740";"wood";    "0";    "1";   "0";         "0";   "3";   "0";   "0";   "0";"16"
  "4";  "1";"TILE_TRUNK_EBONY"         ; "33587232";      "12";"1200";"wood";    "0";    "1";   "0";         "0";   "3";   "1";   "0";   "0";"16"
  "4";  "2";"TILE_TRUNK_BIRCH"         ; "33587232";      "12"; "670";"wood";    "0";    "1";   "0";         "0";   "3";   "2";   "0";   "0";"16"
  "4";  "3";"TILE_TRUNK_BLOODROSE"     ; "33587232";      "12"; "900";"wood";    "0";    "1";   "0";         "0";   "3";   "3";   "0";   "0";"16"
  "5";  "0";"TILE_SAND"                ; "33587232";       "6";"2400";"sand";    "0";    "1";   "0";         "0";   "5";   "0";   "1";   "0";"16"
  "5";  "1";"TILE_SAND_WHITE"          ; "33587232";       "6";"2400";"sand";    "0";    "1";   "0";         "0";   "5";   "1";   "1";   "0";"16"
  "5";  "2";"TILE_SAND_RED"            ; "33587232";       "6";"2400";"sand";    "0";    "1";   "0";         "0";   "5";   "2";   "1";   "0";"16"
  "5";  "3";"TILE_SAND_DESERT"         ; "33587232";       "6";"2400";"sand";    "0";    "1";   "0";         "0";   "5";   "3";   "1";   "0";"16"
  "5";  "4";"TILE_SAND_BLACK"          ; "33587232";       "6";"2400";"sand";    "0";    "1";   "0";         "0";   "5";   "4";   "1";   "0";"16"
  "5";  "5";"TILE_SAND_GREEN"          ; "33587232";       "6";"2400";"sand";    "0";    "1";   "0";         "0";   "5";   "5";   "1";   "0";"16"
  "6";  "0";"TILE_GRAVEL"              ; "33587232";       "6";"2400";"grvl";    "0";    "1";   "0";         "0";   "6";   "0";   "1";   "0";"16"
  "6";  "1";"TILE_GRAVEL_GREY"         ; "33587232";       "6";"2400";"grvl";    "0";    "1";   "0";         "0";   "6";   "1";   "1";   "0";"16"
  "7";  "0";"TILE_ORE_MALACHITE"       ; "33587232";      "25";"2400";"rock";    "0";    "1";   "0";         "0";   "7";   "0";   "0";   "0";"16"
  "7";  "1";"TILE_ORE_HEMATITE"        ; "33587232";      "25";"2400";"rock";    "0";    "1";   "0";         "0";   "7";   "1";   "0";   "0";"16"
  "7";  "2";"TILE_ORE_NATURAL_GOLD"    ; "33587232";      "25";"2400";"rock";    "0";    "1";   "0";         "0";   "7";   "2";   "0";   "0";"16"
  "7";  "3";"TILE_ORE_NATURAL_SILVER"  ; "33587232";      "25";"2400";"rock";    "0";    "1";   "0";         "0";   "7";   "3";   "0";   "0";"16"
  "7";  "4";"TILE_ORE_RUTILE"          ; "33587232";      "25";"2400";"rock";    "0";    "1";   "0";         "0";   "7";   "4";   "0";   "0";"16"
  "7";  "5";"TILE_ORE_AURICHALCUMITE"  ; "33587232";      "25";"2400";"rock";    "0";    "1";   "0";         "0";   "7";   "5";   "0";   "0";"16"
  "8";  "0";"TILE_GEM_RUBY"            ; "33587232";      "25";"2400";"rock";    "0";    "1";   "0";         "0";   "8";   "0";   "0";   "0";"16"
  "8";  "1";"TILE_GEM_EMERALD"         ; "33587232";      "25";"2400";"rock";    "0";    "1";   "0";         "0";   "8";   "1";   "0";   "0";"16"
  "8";  "2";"TILE_GEM_SAPPHIRE"        ; "33587232";      "25";"2400";"rock";    "0";    "1";   "0";         "0";   "8";   "2";   "0";   "0";"16"
  "8";  "3";"TILE_GEM_TOPAZ"           ; "33587232";      "25";"2400";"rock";    "0";    "1";   "0";         "0";   "8";   "3";   "0";   "0";"16"
  "8";  "4";"TILE_GEM_DIAMOND"         ; "33587232";      "25";"2400";"rock";    "0";    "1";   "0";         "0";   "8";   "4";   "0";   "0";"16"
  "8";  "5";"TILE_GEM_AMETHYST"        ; "33587232";      "25";"2400";"rock";    "0";    "1";   "0";         "0";   "8";   "5";   "0";   "0";"16"
  "9";  "0";"TILE_SNOW"                ; "33587232";       "6"; "500";"snow";    "0";    "1";   "1";         "0";   "9";   "0";   "0";   "0";"16"
  "9";  "1";"TILE_ICE_FRAGILE"         ; "13644813";       "1"; "930";"icei";    "0";    "1";   "0";         "0";   "9";   "1";   "0";   "0";"16"
  "9";  "2";"TILE_ICE_NATURAL"         ; "27289626";      "25"; "930";"icei";    "0";    "1";   "1";         "0";   "9";   "2";   "0";   "0"; "8"
  "9";  "3";"TILE_ICE_CLEAR_MAGICAL"   ; "33587232";      "25";"3720";"icex";    "0";    "1";   "1";  "19955770";   "9";   "3";   "0";   "0"; "8"
  "9";  "4";"TILE_GLASS_CRUDE"         ;  "3146755";       "1";"2500";"glas";    "0";    "1";   "1";         "0";   "9";   "4";   "0";   "0";"16"
  "9";  "5";"TILE_GLASS_CLEAN"         ;  "1049601";       "1";"2203";"glas";    "0";    "1";   "1";         "0";   "9";   "5";   "0";   "0";"16"
 "10";  "0";"TILE_PLATFORM_STONE"      ;  "8396808";       "1"; "N/A";"rock";    "0";    "0";   "0";         "0";  "10";   "0";   "0";   "0";"16"
 "10";  "1";"TILE_PLATFORM_WOODEN"     ;  "8396808";       "1"; "N/A";"wood";    "0";    "0";   "0";         "0";  "10";   "1";   "0";   "0";"16"
 "10";  "2";"TILE_PLATFORM_EBONY"      ;  "8396808";       "1"; "N/A";"wood";    "0";    "0";   "0";         "0";  "10";   "2";   "0";   "0";"16"
 "10";  "3";"TILE_PLATFORM_BIRCH"      ;  "8396808";       "1"; "N/A";"wood";    "0";    "0";   "0";         "0";  "10";   "3";   "0";   "0";"16"
 "10";  "4";"TILE_PLATFORM_BLOODROSE"  ;  "8396808";       "1"; "N/A";"wood";    "0";    "0";   "0";         "0";  "10";   "4";   "0";   "0";"16"
 "11";  "0";"TILE_TORCH"               ;  "8396808";       "0"; "N/A";"fxtr";    "0";    "0";   "0"; "267553792";  "11";   "0";   "0";   "1";"16"
 "11";  "1";"TILE_TORCH_FROST"         ;  "8396808";       "0"; "N/A";"fxtr";    "0";    "0";   "0";  "81916159";  "11";   "1";   "0";   "1";"16"
 "12";  "0";"TILE_TORCH"               ;  "8396808";       "0"; "N/A";"fxtr";    "0";    "0";   "0";         "0";  "11";   "0";   "0";   "0";"16"
 "12";  "1";"TILE_TORCH_FROST"         ;  "8396808";       "0"; "N/A";"fxtr";    "0";    "0";   "0";         "0";  "11";   "1";   "0";   "0";"16"
 "13";  "0";"TILE_ILLUMINATOR_WHITE"   ;  "8396808";       "0"; "N/A";"glas";    "0";    "1";   "1"; "239319274";  "13";   "0";   "0";   "0";"16"
 "13";  "1";"TILE_ILLUMINATOR_YELLOW"  ;  "8396808";       "0"; "N/A";"glas";    "0";    "1";   "1"; "267607040";  "13";   "1";   "0";   "0";"16"
 "13";  "2";"TILE_ILLUMINATOR_ORANGE"  ;  "8396808";       "0"; "N/A";"glas";    "0";    "1";   "1"; "267546624";  "13";   "2";   "0";   "0";"16"
 "13";  "3";"TILE_ILLUMINATOR_RED"     ;  "8396808";       "0"; "N/A";"glas";    "0";    "1";   "1"; "246415360";  "13";   "3";   "0";   "0";"16"
 "13";  "4";"TILE_ILLUMINATOR_FUCHSIA" ;  "8396808";       "0"; "N/A";"glas";    "0";    "1";   "1"; "246415543";  "13";   "4";   "0";   "0";"16"
 "13";  "5";"TILE_ILLUMINATOR_PURPLE"  ;  "8396808";       "0"; "N/A";"glas";    "0";    "1";   "1"; "191889643";  "13";   "5";   "0";   "0";"16"
 "13";  "6";"TILE_ILLUMINATOR_BLUE"    ;  "8396808";       "0"; "N/A";"glas";    "0";    "1";   "1";     "52479";  "13";   "6";   "0";   "0";"16"
 "13";  "7";"TILE_ILLUMINATOR_CYAN"    ;  "8396808";       "0"; "N/A";"glas";    "0";    "1";   "1";    "219391";  "13";   "7";   "0";   "0";"16"
 "13";  "8";"TILE_ILLUMINATOR_GREEN"   ;  "8396808";       "0"; "N/A";"glas";    "0";    "1";   "1";  "56884224";  "13";   "8";   "0";   "0";"16"
 "13";  "9";"TILE_ILLUMINATOR_GREEN_DARK";"8396808";       "0"; "N/A";"glas";    "0";    "1";   "1";  "33660928";  "13";   "9";   "0";   "0";"16"
 "13"; "10";"TILE_ILLUMINATOR_BROWN"   ;  "8396808";       "0"; "N/A";"glas";    "0";    "1";   "1";  "89161728";  "13";  "10";   "0";   "0";"16"
 "13"; "11";"TILE_ILLUMINATOR_TAN"     ;  "8396808";       "0"; "N/A";"glas";    "0";    "1";   "1"; "157392948";  "13";  "11";   "0";   "0";"16"
 "13"; "12";"TILE_ILLUMINATOR_GREY_LIGHT";"8396808";       "0"; "N/A";"glas";    "0";    "1";   "1"; "198374589";  "13";  "12";   "0";   "0";"16"
 "13"; "13";"TILE_ILLUMINATOR_GREY_MED";  "8396808";       "0"; "N/A";"glas";    "0";    "1";   "1"; "122803317";  "13";  "13";   "0";   "0";"16"
 "13"; "14";"TILE_ILLUMINATOR_GREY_DARK"; "8396808";       "0"; "N/A";"glas";    "0";    "1";   "1";  "68224065";  "13";  "14";   "0";   "0";"16"
 "13"; "15";"TILE_ILLUMINATOR_BLACK"   ;  "8396808";       "0"; "N/A";"glas";    "0";    "1";   "1"; "116392191";  "13";  "15";   "0";   "0";"16"
 "14";  "0";"TILE_ILLUMINATOR_WHITE"   ; "33587232";       "0"; "N/A";"glas";    "0";    "1";   "1";         "0";  "13";   "0";   "0";   "0";"16"
 "14";  "1";"TILE_ILLUMINATOR_YELLOW"  ; "33587232";       "0"; "N/A";"glas";    "0";    "1";   "1";         "0";  "13";   "1";   "0";   "0";"16"
 "14";  "2";"TILE_ILLUMINATOR_ORANGE"  ; "33587232";       "0"; "N/A";"glas";    "0";    "1";   "1";         "0";  "13";   "2";   "0";   "0";"16"
 "14";  "3";"TILE_ILLUMINATOR_RED"     ; "33587232";       "0"; "N/A";"glas";    "0";    "1";   "1";         "0";  "13";   "3";   "0";   "0";"16"
 "14";  "4";"TILE_ILLUMINATOR_FUCHSIA" ; "33587232";       "0"; "N/A";"glas";    "0";    "1";   "1";         "0";  "13";   "4";   "0";   "0";"16"
 "14";  "5";"TILE_ILLUMINATOR_PURPLE"  ; "33587232";       "0"; "N/A";"glas";    "0";    "1";   "1";         "0";  "13";   "5";   "0";   "0";"16"
 "14";  "6";"TILE_ILLUMINATOR_BLUE"    ; "33587232";       "0"; "N/A";"glas";    "0";    "1";   "1";         "0";  "13";   "6";   "0";   "0";"16"
 "14";  "7";"TILE_ILLUMINATOR_CYAN"    ; "33587232";       "0"; "N/A";"glas";    "0";    "1";   "1";         "0";  "13";   "7";   "0";   "0";"16"
 "14";  "8";"TILE_ILLUMINATOR_GREEN"   ; "33587232";       "0"; "N/A";"glas";    "0";    "1";   "1";         "0";  "13";   "8";   "0";   "0";"16"
 "14";  "9";"TILE_ILLUMINATOR_GREEN_DARK";"33587232";      "0"; "N/A";"glas";    "0";    "1";   "1";         "0";  "13";   "9";   "0";   "0";"16"
 "14"; "10";"TILE_ILLUMINATOR_BROWN"   ; "33587232";       "0"; "N/A";"glas";    "0";    "1";   "1";         "0";  "13";  "10";   "0";   "0";"16"
 "14"; "11";"TILE_ILLUMINATOR_TAN"     ; "33587232";       "0"; "N/A";"glas";    "0";    "1";   "1";         "0";  "13";  "11";   "0";   "0";"16"
 "14"; "12";"TILE_ILLUMINATOR_GREY_LIGHT";"33587232";      "0"; "N/A";"glas";    "0";    "1";   "1";         "0";  "13";  "12";   "0";   "0";"16"
 "14"; "13";"TILE_ILLUMINATOR_GREY_MED"; "33587232";       "0"; "N/A";"glas";    "0";    "1";   "1";         "0";  "13";  "13";   "0";   "0";"16"
 "14"; "14";"TILE_ILLUMINATOR_GREY_DARK";"33587232";       "0"; "N/A";"glas";    "0";    "1";   "1";         "0";  "13";  "14";   "0";   "0";"16"
 "14"; "15";"TILE_ILLUMINATOR_BLACK"   ; "33587232";       "0"; "N/A";"glas";    "0";    "1";   "1";         "0";  "13";  "15";   "0";   "0";"16"
 "15";  "0";"TILE_SANDSTONE"           ; "33587232";      "25";"1900";"rock";    "0";    "1";   "1";         "0";  "15";   "0";   "0";   "0";"16"
 "15";  "1";"TILE_SANDSTONE_WHITE"     ; "33587232";      "25";"1900";"rock";    "0";    "1";   "1";         "0";  "15";   "1";   "0";   "0";"16"
 "15";  "2";"TILE_SANDSTONE_RED"       ; "33587232";      "25";"1900";"rock";    "0";    "1";   "1";         "0";  "15";   "2";   "0";   "0";"16"
 "15";  "3";"TILE_SANDSTONE_DESERT"    ; "33587232";      "25";"1900";"rock";    "0";    "1";   "1";         "0";  "15";   "3";   "0";   "0";"16"
 "15";  "4";"TILE_SANDSTONE_BLACK"     ; "33587232";      "25";"1900";"rock";    "0";    "1";   "1";         "0";  "15";   "4";   "0";   "0";"16"
 "15";  "5";"TILE_SANDSTONE_BLACK"     ; "33587232";      "25";"1900";"rock";    "0";    "1";   "1";         "0";  "15";   "5";   "0";   "0";"16"
 "16";  "0";"TILE_LANTERN_IRON_REGULAR";  "8396808";       "0"; "N/A";"fxtr";    "0";    "0";   "0"; "266453040";  "16";   "0";   "0";   "0";"16"
 "16";  "1";"TILE_SUNSTONE"            ; "33587232";       "0"; "N/A";"rock";    "0";    "1";   "0";         "0";  "16";   "1";   "0";   "2";"16"
 "16";  "2";"TILE_DAYLIGHT_CAPACITOR"  ; "33587232";       "0"; "N/A";"glas";    "0";    "1";   "0";         "0";  "16";   "2";   "0";   "3";"16"
"254";  "0";"TILE_LAVA"                ;"260301048";     "100";"2600";"rock";    "1";    "0";   "0"; "205574144"; "N/A"; "N/A";   "0";   "0";"16"
"254";  "1";"TILE_LAVA"                ;"260301048";     "100";"2600";"rock";    "1";    "0";   "0"; "205574144"; "N/A"; "N/A";   "0";   "0";"16"
"254";  "2";"TILE_LAVA"                ;"260301048";     "100";"2600";"rock";    "1";    "0";   "0"; "205574144"; "N/A"; "N/A";   "0";   "0";"16"
"254";  "3";"TILE_LAVA"                ;"260301048";     "100";"2600";"rock";    "1";    "0";   "0"; "205574144"; "N/A"; "N/A";   "0";   "0";"16"
"254";  "4";"TILE_LAVA"                ;"260301048";     "100";"2600";"rock";    "1";    "0";   "0"; "205574144"; "N/A"; "N/A";   "0";   "0";"16"
"254";  "5";"TILE_LAVA"                ;"260301048";     "100";"2600";"rock";    "1";    "0";   "0"; "205574144"; "N/A"; "N/A";   "0";   "0";"16"
"254";  "6";"TILE_LAVA"                ;"260301048";     "100";"2600";"rock";    "1";    "0";   "0"; "205574144"; "N/A"; "N/A";   "0";   "0";"16"
"254";  "7";"TILE_LAVA"                ;"260301048";     "100";"2600";"rock";    "1";    "0";   "0"; "205574144"; "N/A"; "N/A";   "0";   "0";"16"
"254";  "8";"TILE_LAVA"                ;"260301048";     "100";"2600";"rock";    "1";    "0";   "0"; "205574144"; "N/A"; "N/A";   "0";   "0";"16"
"254";  "9";"TILE_LAVA"                ;"260301048";     "100";"2600";"rock";    "1";    "0";   "0"; "205574144"; "N/A"; "N/A";   "0";   "0";"16"
"254"; "10";"TILE_LAVA"                ;"260301048";     "100";"2600";"rock";    "1";    "0";   "0"; "205574144"; "N/A"; "N/A";   "0";   "0";"16"
"254"; "11";"TILE_LAVA"                ;"260301048";     "100";"2600";"rock";    "1";    "0";   "0"; "205574144"; "N/A"; "N/A";   "0";   "0";"16"
"254"; "12";"TILE_LAVA"                ;"260301048";     "100";"2600";"rock";    "1";    "0";   "0"; "205574144"; "N/A"; "N/A";   "0";   "0";"16"
"254"; "13";"TILE_LAVA"                ;"260301048";     "100";"2600";"rock";    "1";    "0";   "0"; "205574144"; "N/A"; "N/A";   "0";   "0";"16"
"254"; "14";"TILE_LAVA"                ;"260301048";     "100";"2600";"rock";    "1";    "0";   "0"; "205574144"; "N/A"; "N/A";   "0";   "0";"16"
"254"; "15";"TILE_LAVA"                ;"260301048";     "100";"2600";"rock";    "1";    "0";   "0"; "205574144"; "N/A"; "N/A";   "0";   "0";"16"
"255";  "0";"TILE_WATER"               ; "27282445";     "100";"1000";"watr";    "1";    "0";   "0";         "0"; "N/A"; "N/A";   "0";   "0";"16"
"255";  "1";"TILE_WATER"               ; "27282445";     "100";"1000";"watr";    "1";    "0";   "0";         "0"; "N/A"; "N/A";   "0";   "0";"16"
"255";  "2";"TILE_WATER"               ; "27282445";     "100";"1000";"watr";    "1";    "0";   "0";         "0"; "N/A"; "N/A";   "0";   "0";"16"
"255";  "3";"TILE_WATER"               ; "27282445";     "100";"1000";"watr";    "1";    "0";   "0";         "0"; "N/A"; "N/A";   "0";   "0";"16"
"255";  "4";"TILE_WATER"               ; "27282445";     "100";"1000";"watr";    "1";    "0";   "0";         "0"; "N/A"; "N/A";   "0";   "0";"16"
"255";  "5";"TILE_WATER"               ; "27282445";     "100";"1000";"watr";    "1";    "0";   "0";         "0"; "N/A"; "N/A";   "0";   "0";"16"
"255";  "6";"TILE_WATER"               ; "27282445";     "100";"1000";"watr";    "1";    "0";   "0";         "0"; "N/A"; "N/A";   "0";   "0";"16"
"255";  "7";"TILE_WATER"               ; "27282445";     "100";"1000";"watr";    "1";    "0";   "0";         "0"; "N/A"; "N/A";   "0";   "0";"16"
"255";  "8";"TILE_WATER"               ; "27282445";     "100";"1000";"watr";    "1";    "0";   "0";         "0"; "N/A"; "N/A";   "0";   "0";"16"
"255";  "9";"TILE_WATER"               ; "27282445";     "100";"1000";"watr";    "1";    "0";   "0";         "0"; "N/A"; "N/A";   "0";   "0";"16"
"255"; "10";"TILE_WATER"               ; "27282445";     "100";"1000";"watr";    "1";    "0";   "0";         "0"; "N/A"; "N/A";   "0";   "0";"16"
"255"; "11";"TILE_WATER"               ; "27282445";     "100";"1000";"watr";    "1";    "0";   "0";         "0"; "N/A"; "N/A";   "0";   "0";"16"
"255"; "12";"TILE_WATER"               ; "27282445";     "100";"1000";"watr";    "1";    "0";   "0";         "0"; "N/A"; "N/A";   "0";   "0";"16"
"255"; "13";"TILE_WATER"               ; "27282445";     "100";"1000";"watr";    "1";    "0";   "0";         "0"; "N/A"; "N/A";   "0";   "0";"16"
"255"; "14";"TILE_WATER"               ; "27282445";     "100";"1000";"watr";    "1";    "0";   "0";         "0"; "N/A"; "N/A";   "0";   "0";"16"
"255"; "15";"TILE_WATER"               ; "27282445";     "100";"1000";"watr";    "1";    "0";   "0";         "0"; "N/A"; "N/A";   "0";   "0";"16"
"256";  "0";"TILE_NULL"                ;        "0";      "-1";"2600";"null";    "0";    "0";   "0";         "0"; "N/A"; "N/A";   "0";   "0";"16"

## Notes ##

# Friction: 0: frictionless, <16: slippery, 16: regular, >16: sticky

# Opacity/Lumcolor: 30-bit RGB. Only the light diffusers have a opacity value of ZERO.

# Solid: whether the tile has full collision

# movr: Movement resistance, (walkspeedmax) / (1 + (n/16)), 16 halves movement speed

# dsty: density. As we are putting water an 1000, it is identical to specific gravity. [g/l]

# dlfn: dynamic luminosity function.
#    0-static, 1-torch flicker, 2-current global light (sun, star, moon), 3-daylight at noon,
#    4-slow breath, 5-pulsate

# mate: material, four-letter code


## Illuminators ##

# Illuminator white: RGB(228,238,234), simulation of a halophosphate FL lamp (If you want high CRI lamp, collect a daylight!)
# Defalut torch : Y 64 x 0.55183 y 0.40966 (Planckian ~1 770 K); real candlelight colour taken from Spyder5 colorimeter
# Sunstone: Artificial sunlight, change colour over time in sync with sunlight. The light is set by game's code.
# Sunlight capacitor: daylight at noon. Set by game's code.


## Tiles ##

# 16 colour palette : Old Apple Macintosh 16-colour palette
# Magical ice: theoretical __metallic__ ice that might form under super-high pressure (> 5 TPa). Its density is a wild guess.


## References ##

#     * Density of various woods : http://www.engineeringtoolbox.com/wood-density-d_40.html
#     * Density of various phases of ice : http://www1.lsbu.ac.uk/water/ice_phases.html
"""
}