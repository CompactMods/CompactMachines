#version 450

#extension GL_ARB_separate_shader_objects : require

// pride_stripes — horizontal stripes from a Java-controlled palette.
//
// Palette is fed in via the `FlagPalette` uniform block (uploaded each frame
// from the resolved FlagShader entry in the datapack registry). This shader
// has no compile-time variants — one pipeline serves every registered flag.
//
// Layout (std140):
//   paletteMeta.x   — active stripe count, 1..10. Slots [N..9] are ignored.
//   paletteColors[i].rgb — normalised [0,1] colour for stripe i, top-to-bottom
//                          (i = 0 is the top stripe).
//   paletteColors[i].a  — currently unused; reserved for per-stripe opacity.

#include <minecraft:globals.glsl>

layout(location = 0) in vec3 worldPos;   // absolute world coordinates; identical at the seam
                                         // between adjacent machines, so the stripes line up
                                         // continuously across a vertical wall.
layout(location = 1) in vec3 faceNormal; // outward normal of the pane.

layout(location = 0) out vec4 fragColor;

const float OPACITY = 0.3;
const int MAX_STRIPES = 10;

// std140-laid-out palette block. Java side mirrors the field order exactly
// (see MachineShaderRenderer#writePaletteUbo): an int promoted to ivec4 to
// dodge the 16-byte alignment that std140 forces on the following array,
// then the colour array itself.
layout(std140) uniform FlagPalette {
    ivec4 paletteMeta;                // .x = stripe count; .yzw reserved
    vec4  paletteColors[MAX_STRIPES]; // .rgb = colour; .a reserved
};

void main() {
    int N = clamp(paletteMeta.x, 1, MAX_STRIPES);

    // Side faces vary along Y naturally — use the fractional part of worldPos.y
    // so stripes repeat per-block (one full flag per machine in the vertical).
    // Cap faces are constant in Y, so use the outward normal to pick the
    // extreme stripe: +Y normal → top stripe (palette[0]); -Y → bottom.
    float y;
    if (faceNormal.y > 0.5) {
        y = 0.999;
    } else if (faceNormal.y < -0.5) {
        y = 0.001;
    } else {
        y = fract(worldPos.y);
    }

    // Invert so palette[0] is at the top of the block (matches how flags
    // are described top-to-bottom).
    int idx = int((1.0 - y) * float(N));
    idx = clamp(idx, 0, N - 1);

    fragColor = vec4(paletteColors[idx].rgb, OPACITY);
}
