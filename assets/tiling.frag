/*

*/

#version 120
#ifdef GL_ES
    precision mediump float;
#endif
#extension GL_EXT_gpu_shader4 : enable

//layout(origin_upper_left) in vec4 gl_FragCoord; // commented; requires #version 150 or later
// gl_FragCoord is origin to bottom-left

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;


uniform vec2 screenDimension;
uniform vec2 tilesInAxes; // basically a screen dimension; vec2(tiles_in_horizontal, tiles_in_vertical)

uniform ivec2 tilemapDimension;
uniform sampler2D tilemap; // RGBA8888

uniform sampler2D tilesAtlas; // terrain, wire, fluids, etc.
uniform sampler2D tilesBlendAtlas; // alternative terrain for the weather mix (e.g. yellowed grass)
uniform float tilesBlend = 0.0; // percentage of blending [0f..1f]. 0: draws tilesAtlas, 1: draws tilesBlendAtlas

uniform ivec2 tilesInAtlas = ivec2(256, 256);
uniform ivec2 atlasTexSize = ivec2(4096, 4096);
ivec2 tileSizeInPx = atlasTexSize / tilesInAtlas; // should be like ivec2(16, 16)

uniform vec4 colourFilter = vec4(1, 1, 1, 1); // used by WALL to darken it

uniform ivec2 cameraTranslation = ivec2(0, 0); // used to offset the drawing

uniform float drawBreakage = 1.0; // set it to 0f to not draw breakage, 1f to draw it; NEVER set to any other values.

uniform float zoom = 1.0;


ivec2 getTileXY(int tileNumber) {
    return ivec2(tileNumber % int(tilesInAtlas.x), tileNumber / int(tilesInAtlas.x));
}

// return: int=0xaarrggbb
int _colToInt(vec4 color) {
    return int(color.b * 255) | (int(color.g * 255) << 8) | (int(color.r * 255) << 16) | (int(color.a * 255) << 24);
}

// 0x0rggbb where int=0xaarrggbb
// return: [0..1048575]
int getTileFromColor(vec4 color) {
    return _colToInt(color) & 0xFFFFF;
}

// 0x00r00000 where int=0xaarrggbb
// return: [0..15]
int getBreakageFromColor(vec4 color) {
    return (_colToInt(color) >> 20) & 0xF;
}

void main() {

    // READ THE FUCKING MANUAL, YOU DONKEY !! //
    // This code purposedly uses flipped fragcoord. //
    // Make sure you don't use gl_FragCoord unknowingly! //
    // Remember, if there's a compile error, shader SILENTLY won't do anything //


    // default gl_FragCoord takes half-integer (represeting centre of the pixel) -- could be useful for phys solver?
    // This one, however, takes exact integer by rounding down. //
    vec2 overscannedScreenDimension = tilesInAxes * tileSizeInPx; // how many tiles will fit into a screen; one used by the tileFromMap
    vec2 flippedFragCoord = vec2(gl_FragCoord.x, screenDimension.y - gl_FragCoord.y) + cameraTranslation; // NO IVEC2!!; this flips Y
    //vec2 pxCoord = flippedFragCoord.xy; // TODO do I actually need 'pxCoord'?
    vec2 zoomVec = vec2(zoom);

    // get required tile numbers //

    vec4 tileFromMap = texture2D(tilemap, flippedFragCoord / overscannedScreenDimension); // raw tile number
    int tile = getTileFromColor(tileFromMap);
    int breakage = getBreakageFromColor(tileFromMap);
    ivec2 tileXY = getTileXY(tile);
    ivec2 breakageXY = getTileXY(breakage + 5); // +5 is hard-coded constant that depends on the atlas

    // cauculate the UV coord value for texture sampling //

    //vec2 coordInTile = mod(pxCoord, tileSizeInPx) / tileSizeInPx; // 0..1 regardless of tile position in atlas  // TODO do I actually need 'pxCoord'?
    vec2 coordInTile = mod(flippedFragCoord, tileSizeInPx) / tileSizeInPx; // 0..1 regardless of tile position in atlas

    // don't really need highp here; read the GLES spec
    vec2 singleTileSizeInUV = vec2(1) / tilesInAtlas; // constant 0.00390625 for unmodified default uniforms

    vec2 uvCoordForTile = coordInTile * singleTileSizeInUV; // 0..0.00390625 regardless of tile position in atlas

    vec2 uvCoordOffsetTile = tileXY * singleTileSizeInUV; // where the tile starts in the atlas, using uv coord (0..1)
    vec2 uvCoordOffsetBreakage = breakageXY * singleTileSizeInUV;

    // get final UV coord for the actual sampling //

    vec2 finalUVCoordForTile = (uvCoordForTile + uvCoordOffsetTile);// where we should be actually looking for in atlas, using UV coord (0..1)
    vec2 finalUVCoordForBreakage = (uvCoordForTile + uvCoordOffsetBreakage);

    // blending a breakage tex with main tex //

    vec4 tileCol = texture2D(tilesAtlas, finalUVCoordForTile);
    vec4 tileAltCol = texture2D(tilesBlendAtlas, finalUVCoordForTile);

    vec4 finalTile = mix(tileCol, tileAltCol, tilesBlend);

    vec4 finalBreakage = drawBreakage * texture2D(tilesAtlas, finalUVCoordForBreakage); // drawBreakeage = 0 to not draw, = 1 to draw

    vec4 finalColor = vec4(mix(finalTile.rgb, finalBreakage.rgb, finalBreakage.a), finalTile.a);

    gl_FragColor = colourFilter * finalColor;

}
